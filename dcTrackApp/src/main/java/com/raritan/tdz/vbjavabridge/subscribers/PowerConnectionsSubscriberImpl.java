/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQAssociationNotInSync;
import com.raritan.tdz.piq.home.PIQSyncOutletClient;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * @author prasanna
 *
 */
public class PowerConnectionsSubscriberImpl extends LNSubscriberBase implements ItemWriter<PowerConnection> {

	private PIQSyncOutletClient piqSyncOutletClient = null;
	private ItemHome itemHome;
	

	private Logger log = Logger.getLogger(this.getClass());
	

	public PowerConnectionsSubscriberImpl(SessionFactory sessionFactory,
			LNHome lnHome,
			PIQSyncOutletClient piqSyncOutletClient,
			ItemHome itemHome) {
		super(sessionFactory, lnHome);
		this.piqSyncOutletClient = piqSyncOutletClient;
		this.itemHome = itemHome;
	}

	/**
	 * @param piqSyncPDUClient
	 */
	public final void setPiqSycItemClient(PIQSyncOutletClient piqSyncPDUClient) {
		this.piqSyncOutletClient = piqSyncPDUClient;
	}


	/**
	 * @return the itemHome
	 */
	public final ItemHome getItemHome() {
		return itemHome;
	}


	/**
	 * @param itemHome the itemHome to set
	 */
	public final void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber#subscribe()
	 */
	@Override
	public void subscribe() {
		if (sessionFactory != null){
			Session session = sessionFactory.openSession();
			//Subscribe for Insert, update and delete events.
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.INSERT),lnHome);
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.UPDATE),lnHome);
			subscribe(sessionFactory, PowerConnection.class, SystemLookup.getLksData(session, SystemLookup.VBJavaBridgeOperations.DELETE),lnHome);			
			session.close();
		}
	}

	private boolean isValidConnection(PowerPort srcPort, PowerPort destPort){
		return isValidSrcPort(srcPort) && isValidDestPort(destPort) ? true : false;
	}
	
	private boolean isValidDestPort(PowerPort destPort){
		boolean valid = false;
		Item item = destPort.getItem();
	
		if(item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU){
			valid = true;
			log.debug("Valid destination port: " + SystemLookup.Class.RACK_PDU);
		}		
		return valid;
	}
	
	private boolean isValidSrcPort(PowerPort srcPort){
		boolean valid = false;
		Item item = srcPort.getItem();
	
		if(( item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK) ||
				(item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE) ||
				(item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)){
			valid = true;
			log.debug("Valid source port: " + item.getClassLookup().getLkpValueCode());
		}		
		return valid;
	}
	
	private void processInsertOrUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException{
		//NOTE: Source Port must be of a Device/Probe/Network
		//NOTE: Destination Port must be of RACK PDU
		log.debug("--processInsertOrUpdateEvent invoked");
		
		// check if the object exist. A load cannot be used to check because it either returns
		// persistent object or proxy and not null. To find if the object exist, get shall be used
		if (null == session.get(PowerConnection.class, event.getTableRowId())) return;

		PowerConnection connPort = (PowerConnection) session.load(PowerConnection.class, event.getTableRowId());
		PowerPort srcPowerPort = connPort.getSourcePowerPort();
		PowerPort destPowerPort = connPort.getDestPowerPort();
		if(isValidConnection(srcPowerPort, destPowerPort)){
			log.trace("Syncing with outlet client [ update]");
			piqSyncOutletClient.updatePowerConnection(srcPowerPort, destPowerPort);
		}		
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processInsertEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processInsertEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {		
		processInsertOrUpdateEvent(session, event);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		//NOTE: Destination Port (customField2) must be of RACK PDU
		log.debug("--processDeleteEvent invoked");
		String customField2 = event.getCustomField2();
		if (customField2 != null) {
			Long destPortId = Long.parseLong( customField2 ); 
			PowerPort destPort = (PowerPort)session.load(PowerPort.class, destPortId);
			try {
				if( destPort != null && isValidDestPort(destPort) ){
					log.trace("Syncing with outlet client [delete] portId=" + destPort.getPortId());
					piqSyncOutletClient.deletePowerConnection(destPort);
				}
			}catch (HibernateException e){
				log.error("### deleting Power Connection : " + e.getMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.LNSubscriberBase#processUpdateEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	protected void processUpdateEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {	
		processInsertOrUpdateEvent(session, event);
	}

	@Override
	public void write(List<? extends PowerConnection> powerConnections) throws Exception {
		log.debug("In PIQSyncDeviceClientImpl write method: List Size is : " + powerConnections.size());
		PIQAssociationNotInSync piqAssocationNotInSync = piqSyncOutletClient.getPIQAssociationNotInSync();
		
		for (PowerConnection pc:powerConnections){
			log.debug("Source port: " + pc.getSourcePowerPort() + 
					" Destination Port: " + pc.getDestPowerPort());
			try {
				
				if (piqAssocationNotInSync.isNotInSync(pc)){
					PowerPort srcPowerPort = pc.getSourcePowerPort();
					PowerPort destPowerPort = pc.getDestPowerPort();
					if(isValidConnection(srcPowerPort, destPowerPort)){
						log.trace("Syncing with outlet ");
						piqSyncOutletClient.updatePowerConnection(srcPowerPort, destPowerPort);
					}		
				}

			} catch (RemoteDataAccessException e) {
				if (log.isDebugEnabled())
					e.printStackTrace();
				
				throw new Exception(e);
			}
		}
	}

}
