/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.home.PIQItemNotInSync;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.piq.home.PIQSystemEventLogger;
import com.raritan.tdz.piq.json.PDUIPAddressJSON;
import com.raritan.tdz.piq.json.PDUIPAddressJSON.PduIpAddress;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class PDUDataPortSubscriber implements DataPortSubscriber, ItemWriter<Item>{

	private PIQSyncPDUClient piqSyncPDUClient = null;
	private SessionFactory sessionFactory = null;
	private ItemHome itemHome;
	private PIQSystemEventLogger piqSysEventLogger;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public PDUDataPortSubscriber() {
	}
	
	public void setPiqSysEventLogger(PIQSystemEventLogger piqSysEventLogger) {
		this.piqSysEventLogger = piqSysEventLogger;
	}

	public void setPiqSyncPDUClient(PIQSyncPDUClient piqSyncPDUClient) {
		this.piqSyncPDUClient = piqSyncPDUClient;
	}

	/**
	 * @return the sessionFactory
	 */
	public final SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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

	@Override
	public void handleInsertEvent(Session session, Item item, DataPort dPort) throws RemoteDataAccessException {
		if (piqSyncPDUClient != null && piqSyncPDUClient.isAppSettingsEnabled()){
			log.debug("Processing Insert PDU item");
			
			String ip = getDataPortNetInfo(dPort);
			
			try {
				piqSyncPDUClient.addRPDU(item, dPort, ip);
			} 
			catch (InstalledPDUWithoutIPException e) {
				// We've already handled and logged this exception earlier.
				// We can ignore it here because it will not bubble up to initial sync.
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}

	@Override
	public void handleUpdateEvent(Session session, Item item, DataPort dPort) throws RemoteDataAccessException {
		if (piqSyncPDUClient != null && piqSyncPDUClient.isAppSettingsEnabled()){
			log.debug("Processing Update PDU item");
			String ip = getDataPortNetInfo(dPort);
			
			try {
				
				//Reverting the fix done in 41698 since PowerIQ fixed this issue. Please refer to 44484.
				
//				//We need to delete and add the Rack pdu since
//				//PIQ does not take the change in the community string
//				//via just a PUT operation on the PDU
//				//CR Number: 41698
//				if (item.getPiqId() != null){
//					String pduId = item.getPiqId().toString();
//					piqSyncPDUClient.deletePDU(pduId);
//					piqSyncPDUClient.addRPDU(item, dPort, ip);
//				}
				
				//CR Number: 41698.Original code to perform 
				//           update instead of delete and add
				
				piqSyncPDUClient.updateRPDU(item, dPort, ip);
			} 
			catch (InstalledPDUWithoutIPException e) {
				// We've already handled and logged this exception earlier.
				// We can ignore it here because it will not bubble up to initial sync.
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	public void handleDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		// When data port is deleted, and it did not have assigned ipaddress, 
		// then there is nothing to handle. 
		// When user deletes dataPort with assigned ipaddress, and this rpdu 
		// is integrated with PIQ, then this condition is handled in 
		// IPTeamingSubscriberImpl.processDeleteEvent() function.

		log.debug("PDUDataPortSubscriber::handleDeleteEvent called.");
	}
	
	private void processIpAddressChange (List<? extends Item> items, PIQItemNotInSync piqItemNotInSync) throws RemoteDataAccessException {
		
		if ( piqSyncPDUClient.doesPIQSupportIpAddressChangeAPI() == false ) return;
		
		List<PduIpAddress> pduIpAddresses = new ArrayList<PduIpAddress>();
		
		for (Item item:items){
			if (item.getPiqId() != null && 
					piqItemNotInSync.isFound(item) == true && 
					piqItemNotInSync.isIpChanged(item) == true) {
				String oldIp = piqItemNotInSync.getoldIP(item);
				String newIp = piqItemNotInSync.getnewIP(item);
				if (oldIp != null && newIp != null) {
					PduIpAddress pduIp = new PduIpAddress(oldIp, newIp);
					pduIpAddresses.add(pduIp);
				}
				// dont remove this pdu from piqItemNotInSYnc or else it may miss updating 
				// other parameter change e.g. snmp community string.
			}
		}
		if (pduIpAddresses.size() > 0) {
			PDUIPAddressJSON ips = new PDUIPAddressJSON(pduIpAddresses);
			piqSyncPDUClient.updateIPAddresses(ips);
			
			// PIQ restarts poller after executing IP Address change REAT API. 
			// wait for the service to start before continuing to send 
			// additional REST request to PIQ.
			// This is ugly .. needs review /refactor
			if (! piqSyncPDUClient.isPIQResponding()) {
				// TODO: should we exit from here??? review
				log.error("PIQ REST API service not responding.");
			}
		}
		
	}
	
	@Override
	public void write(List<? extends Item> items) throws Exception {
		PIQItemNotInSync piqItemNotInSync = piqSyncPDUClient.getPIQItemsInSync();
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			String ip = null;

			try {  
				// process IP address change first
				processIpAddressChange(items, piqItemNotInSync);
				
				for (Item item:items){
					if (piqItemNotInSync.isNotInSync(item)) {
						if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.RACK_PDU){
							if (item.getPiqId() != null && piqItemNotInSync.isFound(item) == true && piqItemNotInSync.isReplace(item) == true){
								//replace the PDU as ipAddress/proxyIndex has changed
								ip = getDataPortNetInfo(item);
								if (item.getPiqId() != null){
									String pduId = item.getPiqId().toString();
									piqSyncPDUClient.deletePDU(pduId);
									item.setPiqId(null);
								}
								piqSyncPDUClient.addRPDU(item, getDataPort(item.getItemId(),ip, session), ip);
								session.merge(item);
								//Check to see if we just need to update the PDU. 
								//NOTE: Unfortunately, since PowerIQ REST API does not provide data for other than IP/Proxy index to validate
								//      this specific section may be called even though there is nothing changed on dcTrack :-(
							} else if (item.getPiqId() != null && piqItemNotInSync.isFound(item) == true && piqItemNotInSync.isReplace(item) == false) {
								ip = getDataPortNetInfo(item);
								piqSyncPDUClient.updateRPDU(item, getDataPort(item.getItemId(),ip, session), ip);
								session.merge(item);
							}
							//Else we need to add the PDU. Log it and then add it.
							else {
								if( item.getPiqId() != null && piqItemNotInSync.isFound(item) == false ){
									log.warn("rPDU " + item.getItemName() + " (" + item.getItemId() + ") NOT found in PIQ");
									piqSysEventLogger.logPDUDoesNotExistInPIQ( item, ip, piqSyncPDUClient.getEventSource() );	
								}
								ip = getDataPortNetInfo(item);
								piqSyncPDUClient.addRPDU(item, getDataPort(item.getItemId(),ip, session), ip);
								session.merge(item);
							}					
						}
					}
				}
			} catch (RemoteDataAccessException e) {
				if (log.isDebugEnabled())
					e.printStackTrace();
				
				if (e.getRemoteExceptionDetail() instanceof PIQIPAddressConflictException ){
					throw (PIQIPAddressConflictException) e.getRemoteExceptionDetail();
				} else {
					throw new Exception(e);
				}
			}
			session.flush();
		}
	}
	
	//
	// Private methods
	//
	
	/**
	 * @param dPort
	 * @return
	 */
	private String getDataPortNetInfo(DataPort dPort) {
		//Get the IPAddress
		String ip = null;
		List<String> ips = itemHome.getDataPortNetInfo(dPort.getItem().getItemId());
		if (ips != null && ips.size() > 0)
		{
			for (String ipaddress: ips){
				if (ipaddress != null){
					ip = ips.get(0);
					break;
				}
			}
		}
		return ip;
	}
	
	private String getDataPortNetInfo(Item item) {
		//Get the IPAddress
		String ip = null;
		List<String> ips = itemHome.getDataPortNetInfo(item.getItemId());
		if (ips != null && ips.size() > 0)
		{
			for (String ipaddress: ips){
				if (ipaddress != null){
					ip = ips.get(0);
					break;
				}
			}
		}
		return ip;
	}

	private DataPort getDataPort(Long itemId, String ip, Session session){
		DataPort dPort = null;
		
		if (ip != null && !ip.isEmpty() && session != null){
			String queryStr = "select dp.port_data_id from dct_ports_data dp inner join tblipteaming on tblipteaming.portid = dp.port_data_id " +
					 " inner join tblipaddresses on tblipaddresses.id = tblipteaming.ipaddressid "
					+ "inner join dct_items i on i.item_id = dp.item_id "
					 + " where tblipaddresses.ipaddress = '" + ip + "' and i.item_id = " + itemId + " order by tblipteaming.ipaddressid";
			Query query = session.createSQLQuery(queryStr);
			
			List dPorts = query.list();
			BigInteger portId = (BigInteger)dPorts.get(0);
			dPort = (DataPort) session.load(DataPort.class, portId.longValue());
		}
		
		return dPort;
	}
}
