/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItItemDeleteBehavior;
import com.raritan.tdz.item.home.itemObject.ItemDeleteCommonBehavior;
import com.raritan.tdz.piq.exceptions.CannotDeleteItem;
import com.raritan.tdz.piq.json.DeviceJSON;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.piq.json.RackJSON;
import com.raritan.tdz.piq.json.SiteParentJSON;
import com.raritan.tdz.piq.json.RackJSON.Rack;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


/**
 * @author prasanna
 * This class provides all the methods required to sync a dcTrack Item to PIQ 
 * by calling PIQ rest calls.
 */
@Transactional
public class PIQSyncRackClientImpl extends PIQSyncBase implements PIQSyncRackClient {

	private Logger log = Logger.getLogger(this.getClass());
	
	private PIQSyncLocationClient piqSyncLocationClient = null;
	private PIQItemNotInSync piqNotInSync = null;
	
	@Autowired
	private ItItemDeleteBehavior itItemDeleteBehavior;
	
	@Autowired
	private ItemDeleteCommonBehavior itemDeleteCommonBehavior;
	
	public PIQSyncRackClientImpl(ApplicationSettings appSettings,
			PIQSyncLocationClient piqSyncLocationClient,
			PIQItemNotInSync piqNotInSync)
			throws DataAccessException {
		super(appSettings);
		this.piqSyncLocationClient = piqSyncLocationClient;
		this.piqNotInSync = piqNotInSync;
	}


	@Override
	public boolean isRackInSync(String piq_id) throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			RackJSON rackJson = null;
			ResponseEntity<?> restResult = null;
			
			if (piq_id != null && (new Integer(piq_id)).intValue() > 0){
				restResult = doRestGet(piq_id, RackJSON.class);
			}
			
			if (restResult != null){
				rackJson = (RackJSON) restResult.getBody();
				
				if (rackJson != null && rackJson.getRack() != null 
						&& rackJson.getRack().getId().equals(piq_id)) 
					result = true;
			}
		}
		if (log.isDebugEnabled()) log.debug("isRackInSync: " + result);
		return result;
	}
	
	@Override
	public String addRack(Item rackItem, boolean skipSyncCheck) throws RemoteDataAccessException {

		if (log.isDebugEnabled()) log.debug("Add Rack " + rackItem);
		
		String piq_id = null;
		
		if (isAppSettingsEnabled() && rackItem != null && 
				!isItemsPiqHostChanged(rackItem.getItemId())){
			
			//Check to see if the external key is setup in dcTrack database
			//If so, we need to just sync the PowerIQ id and setup our external key
			if (rackItem.getPiqExternalKey() != null && !rackItem.getPiqExternalKey().isEmpty()
					&& rackItem.getPiqId() == null){
				try {
					piq_id = mapUsingExternalKey(rackItem);
					if (piq_id == null) deleteItem(rackItem);
					return piq_id;
				} catch (NumberFormatException ne){
					if (log.isDebugEnabled()) {
						log.debug("Mapping using externalKey failed. Trying to add");
					}
					
					//Add into event log and return null
					addExternalKeyNotInSyncEvent(rackItem);
					deleteItem(rackItem);
					return null;
				}
			}
			
			//First check to see if the location exists.
			boolean locationInSync = true;
			
			if (!skipSyncCheck){
				locationInSync = isLocationInSync(rackItem);
			}
	
			//If it does not add it.
			if (!locationInSync && rackItem.getDataCenterLocation() != null){
				//If not create the location and add this cabinet in that
				DataCenterLocationDetails location = rackItem.getDataCenterLocation();
				String piqId = piqSyncLocationClient.addDataCenter(location);
				if (piqId != null){
					location.setPiqId(piqId);
				}
			}

			
			//Next let us check if the rack exist
			Integer rackId = rackItem.getPiqId();
			boolean rackExist = false;
			
			if (!skipSyncCheck){
				rackExist = isRackExist(rackItem);
			}
			
			if (log.isDebugEnabled()) log.debug("rackExists? : " + rackExist);
			
			//Only if rack does not exist then add the rack
			if (!rackExist){
				//Now Add the rack item
				RackJSON rackjson = new RackJSON(rackItem, false);
				RackJSON response = null;
				
				try {
					response = (RackJSON) doRestPostRequest(rackjson, RackJSON.class);
					
					if (response != null && response.getRack() != null)
						piq_id = response.getRack().getId();
					else
						piq_id = null;
					
				} catch (RemoteDataAccessException e){
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					// Handle External Key Exists.
					if (isPIQExternalKeyExist( error )) {
						piq_id = handleExternalKeyExist(rackItem,
								rackjson.getRack().getExternalKey(), skipSyncCheck);
					}else {
						throw e;
					}
				}

			} else {
				piq_id = rackId != null ? rackId.toString():null;
			}
		}
		return piq_id;
	}




	/**
	 * @param rack
	 * @return
	 */
	private boolean isRackExist(Item rack) {
		boolean rackInSync = false;
		try{
			//Next let us check if the rack exists 
			//and is in sync with powerIQ
			
			if (rack != null)
				rackInSync = isRackInSync(rack);
		} catch (RemoteDataAccessException e){
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the location that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			rackInSync = true;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				e.printStackTrace();
				rackInSync = false;
			}
		}
		return rackInSync;
	}
	
	@Override
	public boolean isRackInSync(Item item) throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			RackJSON rackJson = null;
			ResponseEntity<?> restResult = null;
			Integer piq_id = item.getPiqId();
			
			if (piq_id != null && piq_id.intValue() > 0){
				restResult = doRestGet(piq_id.toString(), RackJSON.class);
			}
			
			if (restResult != null){
				rackJson = (RackJSON) restResult.getBody();
				
				if (rackJson != null && rackJson.getRack().isRackInSync(item)) 
					result = true;
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("isRackInSync: " + result);
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQSyncRackClient#deleteRack(java.lang.String)
	 */
	@Override
	public void deleteRack(String piqRackId) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (piqRackId != null)) {
			doRestDelete( piqRackId );
			if (log.isDebugEnabled()) {
				log.debug("deleteRack: Deleting PIQ rack with id=" + piqRackId);
			}
		}
	}
	
	//
	// Private methods
	//

	private boolean isLocationInSync(Item item){
		boolean locationInSync = false;
		if (item != null){
			
			try{
				DataCenterLocationDetails location = item.getDataCenterLocation();
				
				if (location != null && piqSyncLocationClient != null){ 
					if (piqSyncLocationClient.isLocationInSync(location))
						locationInSync = true;
				}
			}catch(RemoteDataAccessException e){
				ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
						.getExceptionContext().getExceptionItem(
								ExceptionContext.APPLICATIONCODEENUM);
				
				//If we get a 400 error from the PIQ, it means the location that should
				//have been in PIQ no longer exists. If not, something else is wrong.
				locationInSync = true;
				if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
				{
					e.printStackTrace();
					locationInSync = false;
				}
			}
		}
		return locationInSync;
	}
	
	@Override
	public String updateRack(Item rackItem, boolean skipSyncCheck) throws RemoteDataAccessException { 
		String piq_id = null;
		if (isAppSettingsEnabled() && rackItem != null && 
				!isItemsPiqHostChanged(rackItem.getItemId())){
			
			Integer rack_piq_id = rackItem.getPiqId();
			
			//Check to see if the external key is setup in dcTrack database and that external key does 
			// not match with the external key that is supposed to be in the record. Then this is the case
			// for mapping. 
			//If so, we need to just sync the PowerIQ id and setup our external key
			String generatedExternalKey = RackJSON.externalKeyPrefix + "-- " + rackItem.getItemId();
			if (rack_piq_id == null && rackItem.getPiqExternalKey() != null && !rackItem.getPiqExternalKey().isEmpty() 
					&& !rackItem.getPiqExternalKey().equals(generatedExternalKey)){
				return reMapUsingExternalKey(rackItem, generatedExternalKey);
			}
			
			
			if (rack_piq_id == null && rackItem.getPiqExternalKey() != null && !rackItem.getPiqExternalKey().isEmpty()){
				mapUsingExternalKey(rackItem);
			}
			
			if (rack_piq_id != null) {
				RackJSON rackjson = new RackJSON(rackItem, true);
				
				if (!isRackInSync(rackItem)) {
					try{
						doRestPut(rackjson, rack_piq_id.toString());
					
						piq_id = rack_piq_id.toString();
					} catch (RemoteDataAccessException e){
						ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
						// Handle External Key Exists.
						if (isPIQExternalKeyExist( error )) {
							piq_id = handleExternalKeyExistUpdate(rackItem,
									rackjson.getRack().getExternalKey(), skipSyncCheck);
						}else {
							throw e;
						}
					}
				}
				else {
					RackJSON rackJson = null;
					ResponseEntity<?> restResult = null;
						
					restResult = doRestGet(rack_piq_id.toString(), RackJSON.class);
						
					if (restResult != null) {
						rackJson = (RackJSON) restResult.getBody();
					}
					if (null == rackItem.getPiqExternalKey() || !rackItem.getPiqExternalKey().equals(rackJson.getRack().getExternalKey()) ) {
						rackItem.setPiqExternalKey(rackJson.getRack().getExternalKey());
					}
				}
			}
		}

		return piq_id;
	}


	@Override
	public void areRacksInSync(List<PIQItem> piqItems)
			throws RemoteDataAccessException {
		//First clear all the item ids in the piqNotInSync object
		piqNotInSync.clear();
		
		//Now get all the devices from PIQ
		if (isAppSettingsEnabled()){
			RackJSON rackjson = null;
			ResponseEntity<?> restResult = null;

			restResult = doRestGet(null,RackJSON.class);
	
			//Check if each of the devices from PIQ are in sync with the items
			if (restResult != null){
				rackjson = (RackJSON) restResult.getBody();
				HashMap<String, RackJSON.Rack> racks = getRacks(rackjson.getRacks());
				for (PIQItem piqItem: piqItems){
					if (piqItem.getItem().getPiqId() != null){
						RackJSON.Rack rack = racks.get(piqItem.getItem().getPiqId().toString());
						if (rack != null && piqItem != null && !rack.isRackInSync(piqItem.getItem())){
							piqNotInSync.addItem(piqItem.getItem(), true);
						} else if (rack == null){
							piqNotInSync.addItem(piqItem.getItem(), false);
						}
					}
				}
			}
		}
		
	}


	@Override
	public PIQItemNotInSync getPIQItemsInSync() {
		// TODO Auto-generated method stub
		return piqNotInSync;
	}
	
	@Override
	public String updateExternalKey(Item rackItem, String piqIdForReset, boolean reset) throws RemoteDataAccessException {
		String piq_id = null;
		if (isAppSettingsEnabled() && rackItem != null){
			
			Integer rack_piq_id = rackItem.getPiqId();
			
			if (rack_piq_id != null){
				RackJSON rackjson = new RackJSON();
				rackjson.setExternalKey(rackItem,reset, piqIdForReset);
				
				doRestPut(rackjson, rack_piq_id.toString());
					
				piq_id = rack_piq_id.toString();
				
				rackItem.setPiqExternalKey(rackjson.getRack().getExternalKey());
			} else if (piqIdForReset != null){
				RackJSON rackjson = new RackJSON();
				rackjson.setExternalKey(rackItem,reset, piqIdForReset);
				
				doRestPut(rackjson, piqIdForReset);
			}
		}

		return piq_id;
	}

	@Override
	public String getParentPIQId(Item rackItem)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && rackItem != null && rackItem.getPiqId() != null){
			String url = rackItem.getPiqId().toString() + "/parent";
			
			ResponseEntity<?> resp = doRestGet(url, SiteParentJSON.class);
			
			if (resp != null) {
				SiteParentJSON json = (SiteParentJSON)resp.getBody();
				if (json != null) {
					return piqSyncLocationClient.getParent(json.getId());
				}
			}
		}
		return null;
	}

	@Override
	public String mapParent(Item rackItem)
			throws RemoteDataAccessException {
			
			String id = getParentPIQId(rackItem);
			if (id != null){
				rackItem.getDataCenterLocation().setPiqId(id);
			}
			
			return id;
		}

	private HashMap<String,RackJSON.Rack> getRacks(List<RackJSON.Rack> racks){
		HashMap<String,RackJSON.Rack> devicesHashmap = new HashMap<String,RackJSON.Rack>();
		if (racks != null){
			for (RackJSON.Rack rack:racks){
				if (rack != null)
					devicesHashmap.put(rack.getId(), rack);
			}
		}
		return devicesHashmap;
	}
	
	private String findRackId(String externalKey) throws RemoteDataAccessException {
		String rackId = null;
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, RackJSON.class);
		
		if (resp != null) {
			RackJSON json = (RackJSON)resp.getBody();
			if (json != null) {
				List<Rack> racks = json.getRacks();
				if (racks != null && !racks.isEmpty()) {
					rackId = racks.get(0).getId();
				}
			}
		}
		
		return rackId;
	}
	

	private String handleExternalKeyExist(Item rackItem, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findRackId(externalKey);
		
		//Set it to rackItem
		rackItem.setPiqId(Integer.parseInt(id));
		
		//Call update to make sure everything is in sync.
		//updateRack(rackItem, skipSyncCheck);
		
		
		return id;
	}
	
	
	private String handleExternalKeyExistUpdate(Item rackItem, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findRackId(externalKey);
		
		//Rest the old unmapped rack's external key
		RackJSON rackjson = new RackJSON(rackItem, true);
		rackjson.getRack().setExternalKey("Rack -- " + id);
		
		doRestPut(rackjson, id);
		
		
		//update the new rack with correct external key
		updateRack(rackItem, skipSyncCheck);
		
		
		
		return id;
	}

	
	private String mapUsingExternalKey(Item rackItem)
			throws RemoteDataAccessException {
		String id = findRackId(rackItem.getPiqExternalKey());
		if (id != null){
		rackItem.setPiqId(Integer.parseInt(id));
		updateExternalKey(rackItem, id, false);
		rackItem.setPiqExternalKey(RackJSON.externalKeyPrefix + "-- " + rackItem.getItemId());
		mapParent(rackItem);
		} else {
			//Log it to say we could not map it. in the event log
			addExternalKeyNotInSyncEvent(rackItem);
		}
		return id;
	}

	private void addExternalKeyNotInSyncEvent(Item item){
		try {
			Event event = getEventHome().createEvent(EventType.CANNOT_ACCESS_RESOURCE, EventSeverity.CRITICAL, eventSource);
			MessageSource messageSource = getEventHome().getMessageSource();
			
			event.setSummary( messageSource.getMessage("piqSync.externalKeyNotFound",
					new Object[]{item.getPiqExternalKey(),item.getItemName()}, Locale.getDefault()));
			getEventHome().saveEvent(event);
		} catch (DataAccessException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
	}
	
	private String reMapUsingExternalKey(Item item,
			String generatedExternalKey) throws RemoteDataAccessException {
		try {
			String piqid = mapUsingExternalKey(item);
			
			if (piqid != null){
				item.setPiqId(Integer.parseInt(piqid));
				log.info(new StringBuilder("Map: ").append(item.getItemName()).append("to: ").append(piqid));
			}
			else {
				item.setPiqExternalKey(generatedExternalKey);
				addExternalKeyNotInSyncEvent(item);
			}
			return piqid;
		} catch (NumberFormatException ne){
			if (log.isDebugEnabled()) log.debug("Mapping using externalKey failed. Trying to add");
			//Add into event log and return null
			addExternalKeyNotInSyncEvent(item);
			return null;
		}
	}

	private void deleteItem(Item itItem){
		try {
			itItemDeleteBehavior.deleteItem(itItem);
			itemDeleteCommonBehavior.deleteItem(itItem);
		} catch (BusinessValidationException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			else
				log.warn(new StringBuilder("Could not delete item: ")
					.append(itItem.getItemName())
					.append(" for External key ")
					.append(itItem.getPiqExternalKey())
					.append(" during the mapping of PowerIQ Id"));
			throw new CannotDeleteItem(e);
		} catch (Throwable e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			else
				log.warn(new StringBuilder("Could not delete item: ")
							.append(itItem.getItemName())
							.append(" for External key ")
							.append(itItem.getPiqExternalKey())
							.append(" during the mapping of PowerIQ Id"));
			throw new CannotDeleteItem(e);
		}
	}
}
