/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.itemObject.ItItemDeleteBehavior;
import com.raritan.tdz.item.home.itemObject.ItemDeleteCommonBehavior;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.CannotDeleteItem;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.json.DeviceJSON;
import com.raritan.tdz.piq.json.RackJSON;
import com.raritan.tdz.piq.json.SiteParentJSON;
import com.raritan.tdz.piq.json.DeviceJSON.Device;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


/**
 * @author prasanna
 * This class provides all the methods required to sync a dcTrack Item to PIQ 
 * by calling PIQ rest calls.
 */
public class PIQSyncDeviceClientImpl extends PIQSyncBase implements PIQSyncDeviceClient {
	
	public static final int deviceNonExistent = 0x0;
	public static final int deviceExist = 0x1;
	public static final int deviceInSync = 0x2;
	
	public static final int rackNonExistent = 0x0;
	public static final int rackExist = 0x1;
	public static final int rackInSync = 0x2;
	
	private PIQItemNotInSync piqNotInSync = null;
	
	@Autowired
	private ItItemDeleteBehavior itItemDeleteBehavior;
	
	@Autowired
	private ItemDeleteCommonBehavior itemDeleteCommonBehavior;
	
	@Autowired
	PIQProbeLookup probeLookup;

	//private Logger log = Logger.getLogger(this.getClass());
	
	private PIQSyncRackClient piqSyncRackClient = null;
	
	private PIQProbeMapper probeMapper = null;
	
	private SessionFactory sessionFactory = null;
	
	private PIQAsyncTaskService taskService = null;
	
	
	public PIQSyncDeviceClientImpl(ApplicationSettings appSettings, PIQSyncRackClient piqSyncRackClient,
			PIQItemNotInSync piqNotInSync, PIQProbeMapper probeMapper, SessionFactory sessionFactory, 
			PIQAsyncTaskService taskService)
			throws DataAccessException {
		super(appSettings);
		this.piqSyncRackClient = piqSyncRackClient;
		this.piqNotInSync = piqNotInSync;
		this.probeMapper = probeMapper;
		this.sessionFactory = sessionFactory;
		this.taskService = taskService;
	}

	@Override
	public DeviceJSON getDevice(String deviceId) throws RemoteDataAccessException {
		DeviceJSON devicejson = null;
		ResponseEntity<?> restResult = null;

		if (deviceId != null && (new Integer(deviceId)).intValue() > 0) {
			restResult = doRestGet(deviceId, DeviceJSON.class);
		}

		if (restResult != null) {
			devicejson = (DeviceJSON) restResult.getBody();
		}

		return devicejson;
	}

	@Override
	public String addDevice(Item itItem, String ipAddress, Integer powerRating, boolean skipSyncCheck) throws RemoteDataAccessException{
		if (log.isDebugEnabled()) log.debug("Add IT Item " + itItem);
		String piq_id = null;

		if (isAppSettingsEnabled() && 
				!isItemsPiqHostChanged(itItem.getItemId())){
			
			if (!canSyncItem(itItem)) { // bunty - use this to find if sync shall be performed
				if (log.isDebugEnabled()) {
					log.debug("Will not sync item " + itItem);
				}
				return null;
			}
			
			//Check to see if the external key is setup in dcTrack database
			//If so, we need to just sync the PowerIQ id and setup our external key
			if (itItem.getPiqExternalKey() != null && !itItem.getPiqExternalKey().isEmpty()
					&& itItem.getPiqId() == null){
				try {
					piq_id = mapUsingExternalKey(itItem);
					
					if (piq_id == null) {
						deleteItem(itItem);
					}
					
					return piq_id;
				} catch (NumberFormatException ne){
					if (log.isDebugEnabled()) log.debug("Mapping using externalKey failed. Trying to add");
					//Add into event log and return null
					addExternalKeyNotInSyncEvent(itItem);
					deleteItem(itItem);
					return null;
				}
			}
			
			
			int rackSync = rackExist | rackInSync;
			
			if (!skipSyncCheck && itItem != null){
				//First check to see if the rack exists.
				//If it does not add it.
				rackSync = isRackInSync(itItem.getParentItem());
			}
				
			
			if (rackSync == rackNonExistent && itItem.getParentItem() != null){
				//If not create the rack and add this cabinet in that
				Item rackItem = itItem.getParentItem();
				String piqId = piqSyncRackClient.addRack(rackItem, skipSyncCheck);
				if (piqId != null){
					rackItem.setPiqId(new Integer(piqId));
				}
			} else if (rackSync == rackExist && itItem.getParentItem() != null){
				//If we have a rack that exists but not in sync, try to sync.
				Item rackItem = itItem.getParentItem();
				String piqId = piqSyncRackClient.updateRack(rackItem, skipSyncCheck);
			}
			
			//Check to see if this device exist. If it does don't add
			Integer deviceId = itItem.getPiqId();
			int isDeviceInSync = deviceNonExistent; 
			
			if (!skipSyncCheck){
				isDeviceInSync = isDeviceExist(itItem, ipAddress, powerRating);
			}
			
			//Also we may need to validate if the parent exist. For PIQ
			//The cabinet is mandatory for a device.
			Item cabinetItem = itItem.getParentItem();
			
			
			if (log.isDebugEnabled()) log.debug("isDeviceInSync = " + isDeviceInSync);
			if (isDeviceInSync == deviceNonExistent && cabinetItem != null && cabinetItem.getPiqId() != null){
				//Now Add the device item
				DeviceJSON devicejson = new DeviceJSON(itItem, ipAddress, powerRating, false);
				DeviceJSON response = null;
				
				try {
					response = (DeviceJSON) doRestPostRequest(devicejson, DeviceJSON.class);
					
					if (response != null && response.getDevice() != null)
						piq_id =  response.getDevice().getId();
					else
						piq_id = null;
					
				}
				catch (RemoteDataAccessException e) {
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					// Handle External Key Exists.
					if (isPIQExternalKeyExist( error )) {
						piq_id = handleExternalKeyExist(itItem,
								devicejson.getDevice().getExternalKey(), skipSyncCheck);
					} else {
						// Handle IP Address conflict
						if (isPIQIpAddressConflict( error )) {
							PIQIPAddressConflictException ex = new PIQIPAddressConflictException( error, itItem );
							ex.setIpAddress( ipAddress );
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logIPAddressConflict( eventId, ex, eventSource );
							e.setRemoteExceptionDetail( ex );
						}
						else if (isSnmpError( error )) {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logGenericItemError(error, eventId, itItem, ipAddress, null, eventSource);
						}
						else {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logGenericItemError(error, eventId, itItem, ipAddress, null, eventSource);
							throw e;
						}
					}
				}

			}
		}
		
		return piq_id;
	}




	
	@Override
	public boolean isDeviceInSync(String deviceId) throws RemoteDataAccessException {
		boolean result = false;
		
		if (isAppSettingsEnabled()){
			DeviceJSON devicejson = null;
			ResponseEntity<?> restResult = null;
			
			if (deviceId != null && (new Integer(deviceId)).intValue() > 0){
				try {
					restResult = doRestGet(deviceId, DeviceJSON.class);
				}
				catch(RemoteDataAccessException e) {
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
					getPiqSysEventLogger().logGenericItemError(error, eventId, null, null, null, eventSource);
					throw e;
				}
			}
			
			if (restResult != null){
				devicejson = (DeviceJSON) restResult.getBody();
				
				if (devicejson != null && devicejson.getDevice() != null
						&& devicejson.getDevice().getId().equals(deviceId)) 
					result = true;
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("isDeviceInSync: " + result);
		}
		
		return result;
	}
	
	@Override
	public String updateDevice(Item itItem, String ipAddress,
			Integer powerRating, boolean skipSyncCheck) throws RemoteDataAccessException {
		if (log.isDebugEnabled()) log.debug("updateDevice: " + itItem + ipAddress + powerRating);
		String piq_id = null;
		if (isAppSettingsEnabled() && itItem != null && 
				!isItemsPiqHostChanged(itItem.getItemId())){
			
			if (!canSyncItem(itItem)) {
				if (log.isDebugEnabled()) {
					log.debug("Will not sync PDU " + itItem);
				}
				return null;
			}
			//Check to see if the external key is setup in dcTrack database and that external key does 
			// not match with the external key that is supposed to be in the record. Then this is the case
			// for mapping. 
			//If so, we need to just sync the PowerIQ id and setup our external key
			String generatedExternalKey = DeviceJSON.externalKeyPrefix + itItem.getItemId();
			if (itItem.getPiqExternalKey() != null && !itItem.getPiqExternalKey().isEmpty() 
					&& !itItem.getPiqExternalKey().equals(generatedExternalKey)){
				return reMapUsingExternalKey(itItem, generatedExternalKey);
			}
			
			Integer it_piq_id = itItem.getPiqId();
			
			if (it_piq_id == null && itItem.getPiqExternalKey() != null && !itItem.getPiqExternalKey().isEmpty()){
				mapUsingExternalKey(itItem);
			}
			
			int inSync = deviceExist;
			
			if (!skipSyncCheck && it_piq_id != null) {
				if (it_piq_id != null) {
					inSync = isDeviceInSync(itItem, ipAddress, powerRating);
				}
				
				//Check if rack is in sync
				if (itItem.getParentItem() != null){
					int rackSync = isRackInSync(itItem.getParentItem());
					
					
					if (rackSync == rackNonExistent && itItem.getParentItem() != null){
						//If not create the rack and add this cabinet in that
						Item rackItem = itItem.getParentItem();
						String piqId = piqSyncRackClient.addRack(rackItem, skipSyncCheck);
						if (piqId != null){
							rackItem.setPiqId(new Integer(piqId));
						}
						
						moveDeviceTo(itItem, it_piq_id.toString());
					} else if (rackSync == rackExist && itItem.getParentItem() != null){
						//If we have a rack that exists but not in sync, try to sync.
						Item rackItem = itItem.getParentItem();
						String piqId = piqSyncRackClient.updateRack(rackItem, skipSyncCheck);
						
						moveDeviceTo(itItem, it_piq_id.toString());
					}
					
				}
				
				
			}
			
			//First time add from dcTrack will fail since there is still 
			//no parent cabinet assigned. When the parent is updated, we need
			//to add the device to PIQ. The following will take care of this
			//case.
			if (it_piq_id == null || inSync == deviceNonExistent){
				piq_id = addDevice(itItem, ipAddress, powerRating, skipSyncCheck);
			} else if (it_piq_id != null && inSync != (deviceExist|deviceInSync)){
				//Now update the rack item
				DeviceJSON devicejson = new DeviceJSON(itItem, ipAddress, powerRating, true);
				
				try {
					doRestPut(devicejson, it_piq_id.toString());
					
					//Make sure that dcTrack external key column is always in sync
					itItem.setPiqExternalKey(devicejson.getDevice().getExternalKey());
				}
				catch (RemoteDataAccessException e) {
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					// Handle External Key Exists.
					if (isPIQExternalKeyExist( error )) {
						piq_id = handleExternalKeyExistUpdate(itItem, devicejson.getDevice().getExternalKey(), ipAddress, powerRating,
								skipSyncCheck);
					} else {
					// Handle IP Address conflict
						if (isPIQIpAddressConflict( error )) {
							PIQIPAddressConflictException ex = new PIQIPAddressConflictException(error, itItem);
							ex.setIpAddress( ipAddress );
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logIPAddressConflict( eventId, ex, eventSource );
							e.setRemoteExceptionDetail( ex );
						}
						else if (isSnmpError( error )) {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logGenericItemError(error, eventId, itItem, ipAddress, null, eventSource);
						}
						else {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logGenericItemError(error, eventId, itItem, ipAddress, null, eventSource);
							throw e;
						}
					}
				}
						
				piq_id = it_piq_id.toString();
				
				//Make sure that the device is under correct rack
				if (itItem.getParentItem() != null){
					moveDeviceTo(itItem, it_piq_id.toString());
				}
			}
			

		}
		
		return piq_id;
	}



	@Override
	public void deleteDevice(String deviceId) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (deviceId != null)) {
			doRestDelete( deviceId );
		}
	}

	@Override
	public void moveDeviceTo(Item itItem, String deviceId)
			throws RemoteDataAccessException {
		
		String rackId = itItem.getParentItem().getPiqId().toString();
		
		if (isAppSettingsEnabled() && (deviceId != null)  && (rackId != null)
				&& (isDeviceExist(Integer.parseInt(deviceId)) && isRackInSync(Integer.parseInt(rackId)))){
			//Construct a rack object out of the rackId
			DeviceJSON.Device.Parent rackjson = new DeviceJSON.Device.Parent(rackId);
			
			//Then perform a move_to
			doRestPut(rackjson, deviceId + "/move_to");
			
			// If this item is a probe, we also need to move the associated dummy rack PDU
			if (itItem.getClassLookup() != null && 
					itItem.getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE) {
				taskService.runDelayedTask(new MoveProbeTask(itItem.getItemId()), 3);
			}
		}
	}
	
	@Override
	public int isDeviceInSync(Item device, String ipAddress, Integer powerRating) throws RemoteDataAccessException {
		int result = deviceNonExistent;
		
		if (isAppSettingsEnabled()){
			DeviceJSON devicejson = null;
			ResponseEntity<?> restResult = null;
			Integer deviceId = device.getPiqId();
			
			if (deviceId != null && deviceId.intValue() > 0){
				try {
					restResult = doRestGet(deviceId.toString(), DeviceJSON.class);
				}
				catch (RemoteDataAccessException e) {
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
					getPiqSysEventLogger().logGenericItemError(error, eventId, device, null, null, eventSource);
					return deviceNonExistent;
				}
			}
			
			if (restResult != null){
				devicejson = (DeviceJSON) restResult.getBody();
				
				if (devicejson != null && devicejson.getDevice().isIdInSync(deviceId.toString()))
					
					result = deviceExist;
				
					if (devicejson.isDeviceInSync(device, ipAddress, powerRating)) 
						result |= deviceInSync;
			}
		}
		if (log.isDebugEnabled()) log.debug("isDeviceInSync: " + result);
		return result;
	}
	
	
	@Override
	public void areDevicesInSync(List<PIQItem> piqItems) throws RemoteDataAccessException {
		
		//First clear all the item ids in the piqNotInSync object
		piqNotInSync.clear();
		
		//Now get all the devices from PIQ
		if (isAppSettingsEnabled()){
			DeviceJSON devicejson = null;
			ResponseEntity<?> restResult = null;

			restResult = doRestGet(null,DeviceJSON.class);
	
			//Check if each of the devices from PIQ are in sync with the items
			if (restResult != null){
				devicejson = (DeviceJSON) restResult.getBody();
				HashMap<String, DeviceJSON.Device> devices = getDevices(devicejson.getDevices());
				for (PIQItem piqItem: piqItems){
					if (piqItem.getItem().getPiqId() != null){
						DeviceJSON.Device device = devices.get(piqItem.getItem().getPiqId().toString());
						if (device != null && !device.isDeviceInSync(piqItem.getItem(), piqItem.getIpAddress(), piqItem.getPowerRating())){
							piqNotInSync.addItem(piqItem.getItem(), true);
						} else if (device == null){
							piqNotInSync.addItem(piqItem.getItem(), false);
						}
						
						//If this is a probe, make sure that we have corresponding dummy rack pdu. If not we should add it.
						if (piqItem.getItem().getClassLookup() != null && piqItem.getItem().getClassLookup().getLkpValueCode() == SystemLookup.Class.PROBE){
							Item probeItem = probeLookup.getDummyRackPDUForProbeItem(piqItem.getItem().getItemId());
							if (probeItem == null) piqNotInSync.addItem(piqItem.getItem(), true);
							if (log.isDebugEnabled()) log.debug("Adding dummy rackpdu for a probe: " + piqItem.getItem().getItemName());
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
	public String updateExternalKey(Item itItem, boolean reset, String piqIdForReset)
			throws RemoteDataAccessException {
	String piq_id = null;
	if (isAppSettingsEnabled() && itItem != null){
		
		if (!canSyncItem(itItem)) {
			if (log.isDebugEnabled()) {
				log.debug("Will not sync PDU " + itItem);
			}
			return null;
		}
		
		Integer it_piq_id = itItem.getPiqId();
		
		String deviceId = null;

		if (it_piq_id != null){
			
			deviceId = it_piq_id.toString();
		
		} else if (piqIdForReset != null){
			deviceId = piqIdForReset;
		}
		
		if (deviceId != null && isDeviceExist(Integer.parseInt(deviceId))) {
			
			updateExternalKeyOnPIQ(itItem, deviceId, reset);
		}
	}
	
	return piq_id;

	}

	private void updateExternalKeyOnPIQ(Item itItem, String deviceId, boolean reset) throws RemoteDataAccessException {
		//Now update the device item external key
		DeviceJSON devicejson = new DeviceJSON();
		
		devicejson.setExternalKey(itItem, reset, deviceId);
		
		ResponseEntity<?> restResult = null;
		restResult = doRestGet(deviceId, DeviceJSON.class);
		if (restResult != null){
			DeviceJSON deviceJsonPIQ = (DeviceJSON) restResult.getBody();
			if (deviceJsonPIQ != null && deviceJsonPIQ.getDevice() != null){
				
				//Let us ensure that we touch only the External key
				//Rest should be exactly same as what we receive from PIQ.
				DeviceJSON.Device deviceToUpdate = devicejson.getDevice();
				DeviceJSON.Device deviceOnPIQ = deviceJsonPIQ.getDevice();
				
				//FIXME: There should be a better way of copying things (need improvement here) 
				//       For now, this is the quickest that I could get for the 2.6 release :-)
				deviceToUpdate.setName(deviceOnPIQ.getName());
				deviceToUpdate.setCustomer(deviceOnPIQ.getCustomer());
				deviceToUpdate.setDeviceType(deviceOnPIQ.getDeviceType());
				deviceToUpdate.setPowerRating(deviceOnPIQ.getPowerRating());
				deviceToUpdate.setDecommissioned(deviceOnPIQ.getDecommissioned());
				deviceToUpdate.setCustomField1(deviceOnPIQ.getCustomField1());
				deviceToUpdate.setCustomField2(deviceOnPIQ.getCustomField2());
				
				if (itItem.getClassLookup() != null && itItem.getClassLookup().getLkpValueCode() != SystemLookup.Class.PROBE) {
					deviceToUpdate.setIpAddress(deviceOnPIQ.getIpAddress());
				}
				
				deviceToUpdate.setAssetTagId(deviceOnPIQ.getAssetTagId());
				
				doRestPut(devicejson, deviceId);
				
				itItem.setPiqExternalKey(devicejson.getDevice().getExternalKey());
			}
		}
	}



	/******************************** Private methods **************************************/
	
	/**
	 * @param deviceId
	 */
	private boolean isDeviceExist(Integer deviceId) {
		boolean deviceInSync = false;
		try {
			if (deviceId != null)
				deviceInSync = isDeviceInSync(deviceId.toString());
		} catch (RemoteDataAccessException e){
			
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the location that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			deviceInSync = true;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				if (log.isDebugEnabled())
					e.printStackTrace();
				deviceInSync = false;
			}
		}
		return deviceInSync;
	}
	

	/**
	 * @param deviceId
	 */
	private int isDeviceExist(Item itItem, String ipAddress, Integer powerRating) {
		int deviceInSync = deviceNonExistent;
		try {
			if (itItem != null)
				deviceInSync = isDeviceInSync(itItem, ipAddress, powerRating);
		} catch (RemoteDataAccessException e){
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
			getPiqSysEventLogger().logGenericItemError(error, eventId, itItem, ipAddress, null, eventSource);
			
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the item that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			deviceInSync = deviceNonExistent;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				if (log.isDebugEnabled())
					e.printStackTrace();
				deviceInSync = deviceNonExistent;
			}
		}
		return deviceInSync;
	}
	
	private int isRackInSync(Item item){
		int rackInSync = rackNonExistent;
		try {
			if ((item != null)
					&& piqSyncRackClient.isRackInSync(item))
				rackInSync = rackExist | rackInSync;
		} catch (RemoteDataAccessException e) {
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
			getPiqSysEventLogger().logGenericItemError(error, eventId, item, null, null, eventSource);
			
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the rack that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			rackInSync = rackExist;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				if (log.isDebugEnabled())
					e.printStackTrace();
				rackInSync = rackNonExistent;
			}
		}
		return rackInSync;
	}

	/**
	 * @param piqId
	 * @return
	 */
	private boolean isRackInSync(Integer piqId) {
		boolean rackInSync = false;
		try {
			if ((piqId != null)
					&& piqSyncRackClient.isRackInSync(piqId.toString()))
				rackInSync = true;
		} catch (RemoteDataAccessException e) {
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
			getPiqSysEventLogger().logGenericItemError(error, eventId, null, null, null, eventSource);
			
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the rack that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			rackInSync = true;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				if (log.isDebugEnabled())
					e.printStackTrace();
				rackInSync = false;
			}
		}
		return rackInSync;
	}


	private HashMap<String,DeviceJSON.Device> getDevices(List<DeviceJSON.Device> devices){
		HashMap<String,DeviceJSON.Device> devicesHashmap = new HashMap<String,DeviceJSON.Device>();
		if (devices != null){
			for (DeviceJSON.Device device:devices){
				if (device != null)
					devicesHashmap.put(device.getId(), device);
			}
		}
		return devicesHashmap;
	}
	
	
	private String findDeviceId(String externalKey) throws RemoteDataAccessException {
		String deviceId = null;
//		String externalHtmlKey = null;
//		try {
//			externalHtmlKey = UriUtils.encodeQueryParam(externalKey, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, DeviceJSON.class);
		
		if (resp != null) {
			DeviceJSON json = (DeviceJSON)resp.getBody();
			if (json != null) {
				List<Device> devices = json.getDevices();
				if (devices != null && !devices.isEmpty()) {
					deviceId = devices.get(0).getId();
				}
			}
		}
		
		return deviceId;
	}
	

	private String handleExternalKeyExist(Item deviceItem, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findDeviceId(externalKey);
		
		//Set it to rackItem
		deviceItem.setPiqId(Integer.parseInt(id));
		
		//Call update to make sure everything is in sync.
		//updateRack(rackItem, skipSyncCheck);
		
		
		return id;
	}
	
	private String handleExternalKeyExistUpdate(Item deviceItem, String externalKey, String ipAddress,
			Integer powerRating, boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findDeviceId(externalKey);
		
		//Rest the old unmapped device's external key
		DeviceJSON devicejson = new DeviceJSON(deviceItem, ipAddress, powerRating, true);
		devicejson.getDevice().setExternalKey("IT Device -- " + id);
		doRestPut(devicejson, id);
		
		
		//Update the device again
		updateDevice(deviceItem, ipAddress, powerRating, skipSyncCheck);
		
		return id;
	}
	
	private String mapUsingExternalKey(Item itItem)
			throws RemoteDataAccessException {
		String id = findDeviceId(itItem.getPiqExternalKey());
		if (id != null){
			itItem.setPiqId(Integer.parseInt(id));
			updateExternalKeyOnPIQ(itItem, id, false);
			itItem.setPiqExternalKey(DeviceJSON.externalKeyPrefix + itItem.getItemId());
			mapParent(itItem);
		} else {
			//Log it to say we could not map it. in the event log
			addExternalKeyNotInSyncEvent(itItem);
		}
		return id;
	}
	
	private void deleteItem(Item itItem) {
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
	
	private String mapParent(Item itItem) throws RemoteDataAccessException{
		if (itItem.getParentItem() != null){
			String id = getParentPIQId(itItem);
			itItem.getParentItem().setPiqId(Integer.parseInt(id));
			log.info(new StringBuilder("Map: ").append(itItem.getItemName()).append("to: ").append(id));
			if (itItem.getParentItem().getPiqId() != null)
				piqSyncRackClient.updateExternalKey(itItem.getParentItem(), null, false);
			piqSyncRackClient.mapParent(itItem.getParentItem());
			return id;
		} else
			return null;
		
	}
	
	private String reMapUsingExternalKey(Item itItem,
			String generatedExternalKey) throws RemoteDataAccessException {
		try {
			String piqid = mapUsingExternalKey(itItem);
			
			if (piqid != null){
				itItem.setPiqId(Integer.parseInt(piqid));
				log.info(new StringBuilder("Map: ").append(itItem.getItemName()).append("to: ").append(piqid));
			}
			else {
				itItem.setPiqExternalKey(generatedExternalKey);
				addExternalKeyNotInSyncEvent(itItem);
			}
			return piqid;
		} catch (NumberFormatException ne){
			if (log.isDebugEnabled()) log.debug("Mapping using externalKey failed. Trying to add");
			//Add into event log and return null
			addExternalKeyNotInSyncEvent(itItem);
			return null;
		}
	}
	
	private String getParentPIQId(Item itItem) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && itItem != null && itItem.getPiqId() != null){
			String url = itItem.getPiqId().toString() + "/parent";
			
			ResponseEntity<?> resp = doRestGet(url, RackJSON.class);
			
			if (resp != null) {
				RackJSON json = (RackJSON)resp.getBody();
				if (json != null && json.getRack() != null) {
					return json.getRack().getId();
				}
			}
		}
		return null;
	}

	/**
	 * Update probe Rack PDU cabinet item.
	 * @author Andrew Cohen
	 */
	private class MoveProbeTask implements Runnable {
		private long probeItemId;
		
		public MoveProbeTask(long probeItemId) {
			this.probeItemId = probeItemId;
		}
		
		@Override
		public void run() {
			try {
				probeMapper.updateProbeRPDUCabinet( probeItemId );
			}
			catch (Throwable t) {
				log.error("Error updating probe RPDU Cabinet", t);
			}
		}
	}
}
