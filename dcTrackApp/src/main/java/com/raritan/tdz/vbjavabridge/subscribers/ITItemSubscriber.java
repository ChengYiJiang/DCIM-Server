/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.home.PIQItemNotInSync;
import com.raritan.tdz.piq.home.PIQProbeMapper;
import com.raritan.tdz.piq.home.PIQSyncDeviceClient;
import com.raritan.tdz.piq.home.PIQSystemEventLogger;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class ITItemSubscriber implements ItemSubscriber, ItemWriter<Item> {

	private PIQSyncDeviceClient piqSycItemClient = null;
	private ItemHome itemHome;
	private Logger log = Logger.getLogger(this.getClass());
	private SessionFactory sessionFactory;
	private PIQSystemEventLogger piqSysEventLogger;
	private PIQProbeMapper piqProbeMapper;
	
	private static String setExtKey = "SET_EXT_KEY";
	private static String resetExtKey = "RESET_EXT_KEY";
	
	@Autowired
	ItemDAO itemDAO;
	
	ITItemSubscriber(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public void setPiqSysEventLogger(PIQSystemEventLogger piqSysEventLogger) {
		this.piqSysEventLogger = piqSysEventLogger;
	}
	
	public PIQSyncDeviceClient getPiqSycItemClient() {
		return piqSycItemClient;
	}

	public void setPiqSycItemClient(PIQSyncDeviceClient piqSycItemClient) {
		this.piqSycItemClient = piqSycItemClient;
	}
	
	public void setPiqProbeMapper(PIQProbeMapper piqProbeMapper) {
		this.piqProbeMapper = piqProbeMapper;
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
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleInsertEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleInsertEvent(Session session, Item item) throws RemoteDataAccessException {
		if (piqSycItemClient != null && piqSycItemClient.isAppSettingsEnabled()){
			
			if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE
				|| item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE
				|| item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK){
				
				String ip = getDataPortNetInfo(item);		
				Integer powerRating = getPowerRating(item);
				
				
				String piq_id = piqSycItemClient.addDevice(item, ip, powerRating, false );
				
				if (piq_id != null) {
					item.setPiqId(new Integer(piq_id));	
				}
				log.debug("Processing Update IT item");
			} else {
				log.debug("This is not an IT item");
			}
		}
		else{
			log.debug("No REST client attached");
		}

	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleUpdateEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleUpdateEvent(Session session, Item item, LNEvent lnEvent) throws RemoteDataAccessException {
		if (piqSycItemClient != null && piqSycItemClient.isAppSettingsEnabled()){
			
			if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE
				|| item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE
				|| item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK){
				
				String customField1 = lnEvent.getCustomField1();
				String customField2 = lnEvent.getCustomField2();
				
				String ip = getDataPortNetInfo(item);
				Integer powerRating = getPowerRating(item);
				
				if (customField1 != null && customField1.equals(setExtKey)){
					//If PIQ wizard maps, just set the external key
					piqSycItemClient.updateExternalKey(item, false, null);
				} else if (customField1 != null && customField1.equals(resetExtKey)){
					//If PIQ wizard unmaps, reset the external key
					piqSycItemClient.updateExternalKey(item, true, customField2);
				} else {
					String piq_id = piqSycItemClient.updateDevice(item, ip, powerRating, false);
					if (piq_id != null)
						item.setPiqId(new Integer(piq_id));
					
					// For probes, create a dummy rack PDU item in dcTrack
					if ((item.getClassLookup() != null) && (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE)) {
						piqProbeMapper.updateProbeRackPDU(item.getItemId(), ip);
					}
				}
				log.debug("Processing Update IT item");
			} else {
				log.debug("This is not an IT item");
			}
		}
		else{
			log.debug("No REST client attached");
		}


	}

	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleDeleteEvent(org.hibernate.Session, com.raritan.tdz.vbjavabridge.domain.LNEvent)
	 */
	@Override
	public void handleDeleteEvent(Session session, LNEvent event) throws RemoteDataAccessException {
		if (piqSycItemClient != null && piqSycItemClient.isAppSettingsEnabled()) {
			
			// The PIQ device id is stored by the items trigger in the second custom field of the LNEvent
			Integer deviceId = Integer.parseInt( event.getCustomField2() == null ? "0" :  event.getCustomField2());
			
			if (deviceId != null && deviceId > 0) {
				piqSycItemClient.deleteDevice( deviceId.toString() );
				
				// This deletes the dummy rack PDU associated with the probe item
				piqProbeMapper.deleteProbeRackPDUForProbeItemId( event.getTableRowId() );
			}
		}
		else {
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}
	
	@Override
	public void write(List<? extends Item> items) throws Exception {
		PIQItemNotInSync piqItemNotInSync = piqSycItemClient.getPIQItemsInSync();
		String ip = null;
		Integer powerRating = null;
		
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			
			for (Item item:items){
				log.debug(item.getItemId() + ": " + item.getItemName());
				try {
					
					boolean isProbe = item.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE;
					boolean isProbeCreate = false;
					ip = getDataPortNetInfo(item);
					
					if (piqItemNotInSync.isNotInSync(item)){
						
						
						powerRating = getPowerRating(item);
						
						
						if (isProbe || item.getClassLookup().getLkpValueCode() == SystemLookup.Class.DEVICE
								|| item.getClassLookup().getLkpValueCode() == SystemLookup.Class.NETWORK){
							
							if (item.getPiqId() != null && piqItemNotInSync.isFound(item) == true){
								piqSycItemClient.updateDevice(item, ip, powerRating, true);
							}
							else {
								if(item.getPiqId() != null && piqItemNotInSync.isFound(item) == false){
									log.warn("Item " + item.getItemName() + " (" + item.getItemId() + ") Not found in PIQ");
									piqSysEventLogger.logItemDoesNotExistInPIQ( item, ip, piqSycItemClient.getEventSource());
								}
								String piq_id = piqSycItemClient.addDevice(item, ip, powerRating, true);
								if (piq_id != null){
									item.setPiqId(Integer.parseInt(piq_id));
									session.merge(item);
									session.flush();
								}
							}
							
							// For probes, create a dummy rack PDU item in dcTrack
							if (isProbe) {
								piqProbeMapper.queueProbeRequest(item.getItemId(), ip);
								isProbeCreate = true;
							}
						}
					}
					
					//The reason for this if check outside the isNotInSync check is because, the probe could be in sync, however the dummy rack pdu may not be in sync
					//during an update operation.
					if (isProbe && !isProbeCreate){
						piqProbeMapper.updateProbeRackPDU(item.getItemId(), ip);
					}

				} catch (RemoteDataAccessException e) {
					if (log.isDebugEnabled())
						e.printStackTrace();
					session.flush();
					if (e.getRemoteExceptionDetail() instanceof PIQIPAddressConflictException ) {
						throw (PIQIPAddressConflictException) e.getRemoteExceptionDetail();
					} else {
						throw new Exception(e);
					}
				}
			}
			
			piqProbeMapper.flushQueuedProbeRequests();
		}
	}
	

	/**
	 * @param item
	 * @return
	 */
	private Integer getPowerRating(Item item) {
		return itemDAO.getEffectiveBudgetedWattsForAnItem(item.getItemId());
	}

	/**
	 * @param item
	 * @return
	 */
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

}
