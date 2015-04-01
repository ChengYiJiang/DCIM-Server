package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.home.ExternalTicketsLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

public class ExternalTicketsSubscriber extends LNSubscriberBase {
	private ItemHome itemHome = null;
	private Logger log = Logger.getLogger(this.getClass());
	private String tableName="\"dct_tickets\"";
	
	public ExternalTicketsSubscriber(SessionFactory sessionFactory, LNHome lnHome, ItemHome itemHome) {
		super(sessionFactory, lnHome);
		
		this.itemHome = itemHome;
	}

	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			
			//Subscribe for Delete events
			List<String> deleteActions = new ArrayList<String>();
			deleteActions.add(ExternalTicketsLookup.Action.EXTERNAL_TICKETS_DELETE_ITEM);

			subscribe(session, SystemLookup.VBJavaBridgeOperations.DELETE, deleteActions);
			
			session.close();
		}
	}
	
	private void subscribe(Session session, long operation, List<String> actions) {
		LksData lksData = SystemLookup.getLksData(session, operation);
		
		for (String action: actions) {
			lnHome.subscribe(lksData, tableName, action, this);
		}
	}

	/*private void processEvent(LNEvent event) {
		try {
			itemHome.processLNEvent(event);
		}
		catch (BusinessValidationException ex) {
			log.error("Error processing event " + event.toString(), ex);
		}
	}*/
	
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		// processEvent(event);
	}

	
	private UserInfo getTicketAdminUser() {
		UserInfo user = new UserInfo();
		user.setUserName("ticketAdmin");
		user.setUserId("1"); //This field is the users.id, not a string field
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		return user;
	}

	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		try {
			
			Long itemId = event.getTableRowId();
			itemHome.deleteItem(itemId, true, getTicketAdminUser());
		}
		catch (BusinessValidationException ex) {
			
			log.error("Error processing event " + event.toString(), ex);
		} catch (Throwable ex) {
			
			log.error("Error processing event " + event.toString(), ex);
			ex.printStackTrace();
		}
	}

	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		// processEvent(event);
	}
	

}
