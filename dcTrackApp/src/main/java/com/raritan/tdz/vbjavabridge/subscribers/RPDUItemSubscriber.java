/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.home.PIQProbeMapper;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSyncPDUClient;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class RPDUItemSubscriber implements ItemSubscriber {

	private PIQSyncPDUClient piqSyncPDUClient = null;
	private PIQSyncDeviceClient piqSyncDeviceClient = null;
	private ItemHome itemHome;
	private Logger log = Logger.getLogger(this.getClass());
	private PIQProbeMapper probeMapper;
	
	private static String setExtKey = "SET_EXT_KEY";
	private static String resetExtKey = "RESET_EXT_KEY";
	
	RPDUItemSubscriber() {
	}
	
	public void setPiqSyncPDUClient(PIQSyncPDUClient piqSyncPDUClient) {
		this.piqSyncPDUClient = piqSyncPDUClient;
	}
	
	public void setPiqSyncDeviceClient(PIQSyncDeviceClient piqSyncDeviceClient) {
		this.piqSyncDeviceClient = piqSyncDeviceClient;
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
	
	public void setProbeMapper(PIQProbeMapper probeMapper) {
		this.probeMapper = probeMapper;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleInsertEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleInsertEvent(Session session, Item item)
			throws RemoteDataAccessException {
		//We do not handle insert event. This is handled in PDUDataPortSubscriber
		//This is because, without IPAddress, we cannot add a pdu and IPAddress is 
		//always associated with the data port

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleUpdateEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleUpdateEvent(Session session, Item item, LNEvent lnEvent)
			throws RemoteDataAccessException {
		if (piqSyncPDUClient != null && piqSyncPDUClient.isAppSettingsEnabled()){
			log.debug("Processing Update PDU item");
			String ip = getDataPortNetInfo(getDataPort(session, item));
			
			String customField1 = lnEvent.getCustomField1();
			String customField2 = lnEvent.getCustomField2();
			
			if (customField1 != null && customField1.equals(setExtKey)){			
				piqSyncPDUClient.updateExternalKey(item, ip, null, false);
			} else if (customField1 != null && customField1.equals(resetExtKey)){
				piqSyncPDUClient.updateExternalKey(item, ip, customField2, true);
			} else if (!piqSyncPDUClient.isProxyIndexInSync(item)){
				//Delete and re-add the Rack PDU on PowerIQ since proxy index has changed on dcTrack
				if (item.getPiqId() != null && piqSyncPDUClient.isRPDUInSync(item.getPiqId().toString())){
					piqSyncPDUClient.deletePDU(item.getPiqId().toString());
					item.setPiqId(null);
				}
				
				try {
					piqSyncPDUClient.addRPDU(item, getDataPort(session, item), ip);
				} catch (InstalledPDUWithoutIPException e) {
					// We've already handled and logged this exception earlier.
					// We can ignore it here because it will not bubble up to initial sync.
				}
			} else {			
				try {
					piqSyncPDUClient.updateRPDU(item, getDataPort(session, item), ip);
				}
				catch (InstalledPDUWithoutIPException e) {
					// We've already handled and logged this exception earlier.
					// We can ignore it here because it will not bubble up to initial sync.
				}
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	public void handleDeleteEvent(Session session, LNEvent event)
			throws RemoteDataAccessException {
		if (piqSyncPDUClient != null) {
			log.debug("Processing Delete PDU item");
			
			// The PIQ PDU id is stored by the items trigger in the second custom field of the LNEvent
			Integer pduId = Integer.parseInt( event.getCustomField2() != null ? event.getCustomField2() : "0" );
			
			if (pduId != null && pduId > 0) {
				piqSyncPDUClient.deletePDU( pduId.toString() );
			}
			else {
				handleProbeUnmapRequest( session, event.getTableRowId() );
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}		

	}
	
	//
	// Private methods
	//
	
	/**
	 * A probe's associated dummy Rack PDU that had no piqId was deleted.
	 * This means Windows Client has unmapped an existing association
	 * for this probe in PIQ. So, we will also delete the IT Device for
	 * the probe in PIQ in order to prevent a duplicate IT Device from
     * being created when the probe is re-mapped.
     * @param session the hibernate session
	 * @param probeRPDUItemId id of the dummy rack PDU
	 * @throws RemoteDataAccessException
	 */
	private void handleProbeUnmapRequest(Session session, long probeRPDUItemId) throws RemoteDataAccessException {
		Item probe = probeMapper.getLookup().getProbeItemForDummyRackPDU( probeRPDUItemId );
		if (probe != null) {
			// Remove probe->RPDU link record and delete the IT Device on PIQ
			Long probePiqId = probeMapper.getLookup().getUnmappedProbePIQId( probe.getItemId() );
			if (probePiqId != null && probePiqId > 0) {
				probeMapper.deleteUnmappedProbePDULink( probePiqId );
				piqSyncDeviceClient.deleteDevice( probePiqId.toString() );
			}
			
			// Reset the external ID of the Probe's PDU in PIQ (CR #44143)
			DataPort probeDataPort = getDataPort( session, probe );
			if (probeDataPort != null) {
				String ip = getDataPortNetInfo( probeDataPort );
				if (ip != null) {
					Integer pduPiqId = piqSyncPDUClient.lookupByIPAddress( ip );
					if (pduPiqId != null && pduPiqId > 0) {
						Item pduItem = new Item();
						pduItem.setPiqId( pduPiqId );
						pduItem.setItemId( probeRPDUItemId );
						piqSyncPDUClient.updateExternalKey(pduItem, ip, pduPiqId.toString(), true);
					}
				}
			}
		}
	}
	
	/**
	 * @param dPort
	 * @return
	 */
	private String getDataPortNetInfo(DataPort dPort) {
		String ip = null;
		
		if (dPort != null){
			//Get the IPAddress		
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
		}
		return ip;
	}
	
	/**
	 * @param session
	 * @param item
	 * @return
	 * @throws HibernateException
	 */
	private DataPort getDataPort(Session session, Item item)
			throws HibernateException {
		DataPort dPort = null;
		
		Criteria criteria = session.createCriteria(DataPort.class);
		criteria.add(Restrictions.eq("item", item));
		
		List<DataPort> dPorts = criteria.list();
		
		if (dPorts != null && dPorts.size() > 0){
			for (DataPort port: dPorts){
				String ip = getDataPortNetInfo(port);
				if (ip != null && !ip.isEmpty() ){
					dPort = port;
					break;
				}
			}
		}
			
		
		return dPort;
	}

}
