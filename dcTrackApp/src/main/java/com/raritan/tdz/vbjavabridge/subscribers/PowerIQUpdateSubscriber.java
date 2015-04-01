package com.raritan.tdz.vbjavabridge.subscribers;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.web.client.HttpClientErrorException;

import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQAsyncTaskService;
import com.raritan.tdz.piq.integration.PowerIQRouterImpl;
import com.raritan.tdz.piq.service.PIQBulkSyncService;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;
/**
 * This is to trigger the piq update
 * @author prasanna
 *
 */
public class PowerIQUpdateSubscriber implements LNEventSubscriber, ApplicationContextAware  {
	public static String PIQ_UPDATE_FAKE_TABLE = "piq_update";
	
	private static enum OPERATION { START, STOP };

	private PIQBulkSyncService piqBulkSyncService;
	private LNHome lnHome;
	private SessionFactory sessionFactory;
	
	@Autowired(required=true)
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private PIQAsyncTaskService taskService;
	
	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"" + PIQ_UPDATE_FAKE_TABLE + "\"";

	private ApplicationContext applicationContext;
	
	public PowerIQUpdateSubscriber(SessionFactory sessionFactory, LNHome lnHome, PIQBulkSyncService piqBulkSyncService) {
		this.lnHome = lnHome;
		this.sessionFactory = sessionFactory;
		this.piqBulkSyncService = piqBulkSyncService;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),tableName,this);
			lnHome.subscribe(SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),tableName,this);
			session.close();
		}
	}

	
	protected void processInsertEvent(LNEvent event)
			throws RemoteDataAccessException {
		if(event.getTableName().equals(PIQ_UPDATE_FAKE_TABLE)) {
			taskService.runImmediateTask(new PIQUpdateAsyncTask(OPERATION.START));
			deleteEvent(event);
		}
	}
	
	protected void processDeleteEvent(LNEvent event) throws RemoteDataAccessException {
		if(event.getTableName().equals(PIQ_UPDATE_FAKE_TABLE)) {
			taskService.runImmediateTask(new PIQUpdateAsyncTask(OPERATION.STOP));
			deleteEvent(event);
		}
	}

	@Override
	public void handleEvent(LNEvent event) {
		if (event.getOperationLks().getLkpValueCode() == SystemLookup.VBJavaBridgeOperations.INSERT){
			try {
				processInsertEvent(event);
				deleteEvent(event);
			} catch (RemoteDataAccessException e) {
				handleRemoteDataAccessException(event, e);
			}
		} else if (event.getOperationLks().getLkpValueCode() == SystemLookup.VBJavaBridgeOperations.DELETE){
			try {
				processDeleteEvent(event);
				deleteEvent(event);
			} catch (RemoteDataAccessException e) {
				handleRemoteDataAccessException(event, e);
			}
		}
		
	}
	
	/**
	 * Make sure that user is notified of any 4xx errors
	 * An entry in the event table will provide user with
	 * detail. <p>For example 404 occur when there is a total mismatch
	 * in the piqIds between what is there in dcTrack and
	 * PIQ, we should handle this.</p><p> In the sync clients
	 * we generally try to handle this situation gracefully
	 * When we cannot we let the user know what to do and clear the event. 
	 * <p> We have seen such situations in the field where it goes to
	 * a never ending loop and with a GotoMeeting we need to delete the entries
	 * in event table</p>
	 * @param event
	 * @param session
	 * @param e
	 */
	private void handleRemoteDataAccessException(LNEvent event,
			RemoteDataAccessException e) {
		ExceptionContext ec = e.getExceptionContext();
		if (ec != null) {
			Object ex = ec.getExceptionItem( ExceptionContext.EXCEPTION );
			
			if ((ex != null) && (ex instanceof HttpClientErrorException)) {
			
				//Log it
				String msg = messageSource.getMessage("remote.error",
						new Object[] { e.getUrl(), e.getMessage() },
						null
				);
				log.error( msg );
				
				//Notify user with CRITICAL severity. They could atleast try to sync this
				//manually via classic client by unmapping and re-mapping method.
				//NOTE: Entering the event is taken care by the RestClient object. So no need to handle it here
				//@See: handleError method
				
				
				//Delete the event
				deleteEvent(event);
					
			}
		} else {
		
			// When we have a remote API failure, we do not delete the event.
			String msg = messageSource.getMessage("remote.error",
					new Object[] { e.getUrl(), e.getMessage() },
					null
			);
			log.error( msg );
		}
	}
	
	private void deleteEvent(LNEvent event){
		StatelessSession statelessSession = sessionFactory.openStatelessSession();
		Query eventQry = statelessSession.createQuery("delete from LNEvent where eventId = :id");
		eventQry.setLong("id", event.getEventId());
		eventQry.executeUpdate();
		statelessSession.close();
	}
	
	
	private class PIQUpdateAsyncTask implements Runnable {
		
		private OPERATION op = OPERATION.START;
		
		public PIQUpdateAsyncTask(OPERATION op){
			this.op = op;
		}
		
		@Override
		public void run() {
			try {
				String ipAddress = applicationContext.getEnvironment().getProperty(PowerIQRouterImpl.HOST);
				if (op == OPERATION.START)
					piqBulkSyncService.updatePIQData(ipAddress);
				else if (op == OPERATION.STOP){
					piqBulkSyncService.stopPIQDataUpdate(ipAddress);
				}
			} catch (ServiceLayerException e) {
				if (log.isDebugEnabled())
					e.printStackTrace();
				log.error("A problem occured during the powerIQ update");
				this.notify();
			}
			
		}
		
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		
		this.applicationContext = applicationContext;
		
	}
}
