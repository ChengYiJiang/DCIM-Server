/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.home.PIQItemNotInSync;
import com.raritan.tdz.piq.home.PIQSyncRackClient;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 * This subscribes for all events related to cabinets.
 */
public class CabinetItemSubscriber implements ItemSubscriber, ItemWriter<Item> {

	PIQSyncRackClient piqSycRackClient = null;
	SessionFactory sessionFactory;
	
	private static String setExtKey = "SET_EXT_KEY";
	private static String resetExtKey = "RESET_EXT_KEY";
	
	public PIQSyncRackClient getPiqSycRackClient() {
		return piqSycRackClient;
	}

	public void setPiqSycRackClient(PIQSyncRackClient piqSycRackClient) {
		this.piqSycRackClient = piqSycRackClient;
	}

	private Logger log = Logger.getLogger(this.getClass());
	
	CabinetItemSubscriber(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleInsertEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleInsertEvent(Session session, Item item) throws RemoteDataAccessException {
		if (piqSycRackClient != null && piqSycRackClient.isAppSettingsEnabled()){		
			//Add the rack
			String piq_id = piqSycRackClient.addRack(item, false);
			if (piq_id != null) {
				item.setPiqId(new Integer(piq_id));	
			}
			log.debug("Processing Insert cabinet item");
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vbjavabridge.subscribers.ItemSubscriber#handleUpdateEvent(org.hibernate.Session, com.raritan.tdz.domain.Item)
	 */
	@Override
	public void handleUpdateEvent(Session session, Item item, LNEvent lnEvent) throws RemoteDataAccessException {
		if (piqSycRackClient != null && piqSycRackClient.isAppSettingsEnabled()){
			
			String customField1 = lnEvent.getCustomField1();
			String customField2 = lnEvent.getCustomField2();
			
			if (customField1 != null && customField1.equals(setExtKey)){
				//If PIQ wizard maps, just set the external key
				piqSycRackClient.updateExternalKey(item, null, false);
			} else if (customField1 != null && customField1.equals(resetExtKey)){
				//If PIQ wizard unmaps, reset the external key
				piqSycRackClient.updateExternalKey(item, customField2, true);
			} else {
				//Update the rack
				piqSycRackClient.updateRack(item, false);
			}
			
			log.debug("Processing Update cabinet item");
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
		if (piqSycRackClient != null && piqSycRackClient.isAppSettingsEnabled()) {
			
			// The PIQ Rack ID is stored by the items trigger in the second event custom field
			if (event.getCustomField2() != null && !event.getCustomField2().isEmpty()) {
				Integer rackId = Integer.parseInt( event.getCustomField2() );
				
				if (rackId != null && rackId > 0) {
					piqSycRackClient.deleteRack( rackId.toString() );
					log.debug("Processing Delete cabinet item");
				}
			}
		}
		else{
			log.debug("No DcTrackPIQSyncItemClient attached");
		}
	}

	@Override
	public void write(List<? extends Item> items) throws Exception {
		
		log.debug("In PIQSyncRackClientImpl write method: List Size is : " + items.size());
		
		PIQItemNotInSync piqItemNotInSync = piqSycRackClient.getPIQItemsInSync();
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			
			for (Item item:items){
				try {
					if (piqItemNotInSync.isNotInSync(item)){
						if (item.getPiqId() != null && piqItemNotInSync.isFound(item) == true)
							piqSycRackClient.updateRack(item, true);
						else if (item.getPiqId() != null && piqItemNotInSync.isFound(item) == false){
							String piq_id = piqSycRackClient.addRack(item,true);
							if (piq_id != null) {
								item.setPiqId(Integer.parseInt(piq_id));
								session.merge(item);
							}
						}
						else {
							String piq_id = piqSycRackClient.addRack(item, true);
							if (piq_id != null) {
								item.setPiqId(Integer.parseInt(piq_id));
								session.merge(item);
							}
						}
					}
				} catch (RemoteDataAccessException e) {
					if (log.isDebugEnabled())
						e.printStackTrace();
					throw new Exception(e);
				}
			}
			
			session.flush();
		}
		
	}
}
