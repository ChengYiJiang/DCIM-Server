/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Payload;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.client.RestClientException;

import com.raritan.tdz.assetstrip.home.AssetStripAutoAssociation;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventStatus;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.itemObject.ItItemDeleteBehavior;
import com.raritan.tdz.item.home.itemObject.ItemDeleteCommonBehavior;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.exceptions.CannotDeleteItem;
import com.raritan.tdz.piq.exceptions.InstalledPDUWithoutIPException;
import com.raritan.tdz.piq.exceptions.PIQIPAddressConflictException;
import com.raritan.tdz.piq.home.PIQSyncPorts.TYPE;
import com.raritan.tdz.piq.jobs.PIQJobHandler;
import com.raritan.tdz.piq.jobs.PIQJobPoller;
import com.raritan.tdz.piq.json.DeviceJSON;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.piq.json.Inlet;
import com.raritan.tdz.piq.json.JobJSON;
import com.raritan.tdz.piq.json.Outlet;
import com.raritan.tdz.piq.json.PDUIPAddressJSON;
import com.raritan.tdz.piq.json.RackJSON;
import com.raritan.tdz.piq.json.PDUIPAddressJSON.PduIpAddress;
import com.raritan.tdz.piq.json.PIQInfoJSON;
import com.raritan.tdz.piq.json.PduJSON;
import com.raritan.tdz.piq.json.PduJSON.Pdu;
import com.raritan.tdz.piq.json.PdusJSON;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.VersionCheck;


/**
 * This class provides all the methods required to sync a dcTrack Item to PIQ 
 * by calling PIQ rest calls.
 * @author Andrew Cohen
 */
public class PIQSyncPDUClientImpl extends PIQSyncBase implements PIQSyncPDUClient {

	//private Logger log = Logger.getLogger(this.getClass());
	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	PIQProbeLookup probeLookup;

	private PIQSyncRackClient piqSyncRackClient = null;
	private PIQSyncOutletClient piqSyncOutletClient = null;
	private PIQJobPoller jobsPoller = null;
	private PIQJobHandler jobHandler = null;
	private ItemHome itemHome;
	private PIQItemNotInSync piqNotInSync = null;
	private AssetStripAutoAssociation assetStripAutoAssociation = null;
	private PortHome portHome = null;
	private PIQAsyncTaskService taskService;
	private List<PIQSyncPorts> syncPorts;
	private Map<TYPE, PIQSyncPorts> syncPortsMap;

	private Map<Long, Event> processedEvents = new HashMap<Long, Event>(); 

	@Autowired
	private ApplicationSettings appSettings;
	
	@Autowired
	private PIQSyncPIQVersion piqSyncPIQVersion;
	
	@Autowired
	private ItItemDeleteBehavior meItemDeleteBehavior;
	
	@Autowired
	private ItemDeleteCommonBehavior itemDeleteCommonBehavior;
	
	private static int piqSyncChunkLimit = 50;
	
	private List<String> validationWarningCodes;
	
	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}

	public List<PIQSyncPorts> getSyncPorts() {
		return syncPorts;
	}

	public void setSyncPorts(List<PIQSyncPorts> syncPorts) {
		this.syncPorts = syncPorts;
	}
	
	public Map<TYPE, PIQSyncPorts> getSyncPortsMap() {
		return syncPortsMap;
	}

	public void setSyncPortsMap(Map<TYPE, PIQSyncPorts> syncPortsMap) {
		this.syncPortsMap = syncPortsMap;
	}

	@Autowired
	private PIQSyncInletClient piqSyncInletClient;

	@Autowired
	private PIQSyncSensorClient piqSyncSensorClient;
	
	@Autowired//		String externalHtmlKey = HtmlUtils.htmlEscape(externalKey);
//	try {
//	externalHtmlKey = UriUtils.encodeQueryParam(externalKey, "UTF-8");
//} catch (UnsupportedEncodingException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//
	private EventHome eventHome;

	
	public PIQSyncPDUClientImpl(ApplicationSettings appSettings, PIQSyncRackClient piqSyncRackClient,
			PIQItemNotInSync piqNotInSync) 
		throws DataAccessException {
		super(appSettings);
		this.piqSyncRackClient = piqSyncRackClient;
		this.piqNotInSync  = piqNotInSync;
	}
	
	public void setJobsPoller(PIQJobPoller jobsPoller) {
		this.jobsPoller = jobsPoller;
	}
	
	public void setJobHandler(PIQJobHandler jobHandler) {
		this.jobHandler = jobHandler;
	}
	
	public void setPiqSyncOutletClient(PIQSyncOutletClient piqSyncOutletClient) {
		this.piqSyncOutletClient = piqSyncOutletClient;
	}
	
	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	
	public void setAssetStripAutoAssociation(AssetStripAutoAssociation assetStripAutoAssociation) {
		this.assetStripAutoAssociation = assetStripAutoAssociation;
	}
	
	public PortHome getPortHome() {
		return portHome;
	}

	public void setPortHome(PortHome portHome) {
		this.portHome = portHome;
	}
	
	public void setTaskService(PIQAsyncTaskService taskService) {
		this.taskService = taskService;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.home.PIQPDUClient#AddPDU(com.raritan.tdz.domain.Item)
	 */
	@Override
	public String addRPDU(Item pduItem, DataPort dataPort, String ipAddress) throws RemoteDataAccessException, InstalledPDUWithoutIPException {
		
		String jobId = null;
		
		if (log.isDebugEnabled())
			log.debug("Add PDU " + pduItem);
		
		if (isAppSettingsEnabled() && 
				!isItemsPiqHostChanged(pduItem.getItemId())){
			
			if (!canSyncItem(pduItem)) {
				if (log.isDebugEnabled()) {
					log.debug("Will not sync PDU " + pduItem);
				}
				return null;
			}
			
			//Check to see if the external key is setup in dcTrack database
			//If so, we need to just sync the PowerIQ id and setup our external key
			if (pduItem.getPiqExternalKey() != null && !pduItem.getPiqExternalKey().isEmpty()
					&& pduItem.getPiqId() == null){
				try {
					String piqId = mapUsingExternalKey(pduItem,ipAddress);
					if (piqId == null) {
						deleteItem(pduItem);
					}
					return piqId;
				} catch (NumberFormatException ne){
					if (log.isDebugEnabled()) log.debug("Mapping using externalKey failed");
					
					addExternalKeyNotInSyncEvent(pduItem);
					if (pduItem.getPiqId() == null)
					deleteItem(pduItem);
					return null;
				}
			}
			
			//Check to see if this RPDU exist. If it does don't add
			Integer pduId = pduItem.getPiqId();
			boolean isPDUExist = isRPDUExist(pduId);
			
			if (!isPDUExist) {
				
				validateInstalledPDUHasIPAddress(pduItem, ipAddress);
				
				if (ipAddress != null) {
					// Now Add the rack item
					PduJSON pdujson = new PduJSON(pduItem, dataPort, ipAddress, false);
					JobJSON jobJson = null;
					
					try {
						jobJson = (JobJSON) doRestPost(pdujson, "v2/pdus", "?async=true", JobJSON.class);
					}
					catch (RemoteDataAccessException e) {
						ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
						
						// Handle license error
						if (isPIQLicenseError( error )) {
							throw e;
						}
						
						// Handle External Key Exists.
						if (isPIQExternalKeyExist( error )) {
							String piq_id = handleExternalKeyExist(pduItem,
									pdujson.getPdu().getExternalKey(), false);
							postProcessAddPDU(null, pduItem, Integer.parseInt(piq_id));
						}else {
							// Handle IP Address conflict
							if (isPIQIpAddressConflict( error )) {
								PIQIPAddressConflictException ex = new PIQIPAddressConflictException(error, pduItem);
								ex.setIpAddress( ipAddress );
								ex.setProxyIndex( pdujson.getPdu().getProxyIndex() );
								Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
								getPiqSysEventLogger().logIPAddressConflict( eventId, ex, eventSource );
								e.setRemoteExceptionDetail( ex );
							}
							else if (isSnmpError( error )) {
								Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
								String proxyIndex = pdujson.getPdu().getProxyIndex() != null ? pdujson.getPdu().getProxyIndex().toString() : null;
								getPiqSysEventLogger().logGenericItemError(error, eventId, pduItem, ipAddress, proxyIndex, eventSource);
							}
							else {
								Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
								String proxyIndex = pdujson.getPdu().getProxyIndex() != null ? pdujson.getPdu().getProxyIndex().toString() : null;
								getPiqSysEventLogger().logGenericItemError(error, eventId, pduItem, ipAddress, proxyIndex, eventSource);
								throw e;
							}
						}
					}
					
					if (jobJson != null){
						// Update the PDU piq_id when the PIQ job is complete
						jobId = jobJson.getJob().getId();
						
						Map<String, Object> jobData = new HashMap<String, Object>(2);
						jobData.put("ipAddress", ipAddress);
						jobData.put("pduItem", pduItem);
						
						Map<String, Object> results = jobsPoller.addJob(jobId, jobHandler, jobData);
						
						if (results != null) {
							Integer piqId = (Integer)results.get("piqId");
							
							postProcessAddPDU(null, pduItem, piqId);
						}
					}
				}
			}
		}
		
		return jobId;		
	}


	@Override
	public String updateRPDU(Item pduItem, DataPort dataPort, String ipAddress)
			throws RemoteDataAccessException, InstalledPDUWithoutIPException {
		if (log.isDebugEnabled()) {
			log.debug("Updating RPDU " + pduItem + "IPAddress: " + ipAddress);
		}
		
		String piqId = null;
//		String jobId = null;
		
		if (isAppSettingsEnabled() && pduItem != null && 
				!isItemsPiqHostChanged(pduItem.getItemId())){
			
			if (!canSyncItem(pduItem)) {
				if (log.isDebugEnabled()) {
					log.debug("Will not sync PDU " + pduItem);
				}
				return null;
			}

			Integer pdu_piq_id = pduItem.getPiqId();
			
			//First try to map it since the external key may have changed during an import operation.
			//Check to see if the external key is setup in dcTrack database and that external key does 
			// not match with the external key that is supposed to be in the record. Then this is the case
			// for mapping. 
			//If so, we need to just sync the PowerIQ id and setup our external key
			String generatedExternalKey = PduJSON.externalKeyPrefix + pduItem.getItemId();
			if (pdu_piq_id == null && pduItem.getPiqExternalKey() != null && !pduItem.getPiqExternalKey().isEmpty() 
					&& !pduItem.getPiqExternalKey().equals(generatedExternalKey)){
				return reMapUsingExternalKey(pduItem, ipAddress, generatedExternalKey);
			}
			
			
			if (pdu_piq_id == null && pduItem.getPiqExternalKey() != null && !pduItem.getPiqExternalKey().isEmpty()){
				mapUsingExternalKey(pduItem, ipAddress);
			}
			
			boolean updateFailed = false;
			
			if (pdu_piq_id != null && isRPDUInSync(pdu_piq_id.toString())){
				//Now update the rack item
				PduJSON pdujson = new PduJSON(pduItem, dataPort, ipAddress, true);
				JobJSON jobJson = null;
				
				try {
					jobJson = (JobJSON)doRestPut(pdujson, "v2/pdus", pdu_piq_id.toString()+"?async=true",JobJSON.class);
					//doRestPut(pdujson, "v2/pdus", pdu_piq_id.toString()+"/rescan");
					
					//Make sure that dcTrack external key column is always in sync
					pduItem.setPiqExternalKey(generatedExternalKey);
				}
				catch (RemoteDataAccessException e) {
					updateFailed = true;
					ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
					// Handle External Key Exists.
					if (isPIQExternalKeyExist( error )) {
						String piq_id = handleExternalKeyExistUpdate(pduItem,
								pdujson.getPdu().getExternalKey(), dataPort, ipAddress, false);
						postProcessAddPDU(null, pduItem, Integer.parseInt(piq_id));
					} else if (isJobError( error )) {
						//We need to probably log it here. For now ignore this.
					} else {
						// Handle IP Address conflict
						if (isPIQIpAddressConflict( error )) {
							PIQIPAddressConflictException ex = new PIQIPAddressConflictException(error, pduItem);
							ex.setIpAddress( ipAddress );
							ex.setProxyIndex( pdujson.getPdu().getProxyIndex() );
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							getPiqSysEventLogger().logIPAddressConflict( eventId, ex, eventSource );
							e.setRemoteExceptionDetail( ex );
						}
						else if (isSnmpError( error )) {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							String proxyIndex = pdujson.getPdu().getProxyIndex() != null ? pdujson.getPdu().getProxyIndex().toString() : null;
							getPiqSysEventLogger().logGenericItemError(error, eventId, pduItem, ipAddress, proxyIndex, eventSource);
						}
						else {
							Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
							String proxyIndex = pdujson.getPdu().getProxyIndex() != null ? pdujson.getPdu().getProxyIndex().toString() : null;
							getPiqSysEventLogger().logGenericItemError(error, eventId, pduItem, ipAddress, proxyIndex, eventSource);
							throw e;
						}
					}
				}
//				
//				if (jobJson != null){
//					// Update the PDU piq_id when the PIQ job is complete
//					jobId = jobJson.getJob().getId();
//					
//					Map<String, Object> jobData = new HashMap<String, Object>(2);
//					jobData.put("ipAddress", ipAddress);
//					jobData.put("pduItem", pduItem);
//					
//					Map<String, Object> results = jobsPoller.addJob(jobId, jobHandler, jobData);
//					
//					if (results != null) {
//						Integer piqid = (Integer)results.get("piqId");
//						
//						postProcessAddPDU(pduItem, piqid);
//					}
//				}
			}
			
			if (!updateFailed) {
				//Since the "GET" operation on a PDU never gives a parent, we would like to ensure that it is under
				//correct parent. The following ensures that.
				if (pdu_piq_id != null && pduItem.getParentItem() != null && pduItem.getParentItem().getPiqId() != null){
					moveRPDUTo(pdu_piq_id.toString(), pduItem.getParentItem().getPiqId().toString());
					
					//Also perform the post processing of associations as well. CR 41615
					postProcessUpdatePDU(null, pduItem, pdu_piq_id);
				}
				
				//First time add from dcTrack will fail since there is still 
				//no parent cabinet assigned. When the parent is updated, we need
				//to add the device to PIQ. The following will take care of this
				//case.
				if (pdu_piq_id == null){
					piqId = addRPDU(pduItem, null, ipAddress);
				}
				else {
					//For any PowerIQ version greater than 3.1.0 we do not rescan
					if (!doesPIQSupport("3.1.0")){
						// Since we updated, perform a rescan of the PDU on a separate thread
						taskService.runDelayedTask( new RescanTask(pdu_piq_id, pduItem), 3 );
					}
				}
			}
		}
		
		return piqId;
	}

	@Override
	public boolean isRPDUInSync(String pduId) throws RemoteDataAccessException {
		boolean result = false;
		if (isAppSettingsEnabled()){
			PduJSON pdujson = null;
			try {
				pdujson = getPduJSON(pduId);
			} catch (RemoteDataAccessException e){
				ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
				Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
				getPiqSysEventLogger().logGenericItemError(error, eventId, null, null, null, eventSource);
				
				ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
						.getExceptionContext().getExceptionItem(
								ExceptionContext.APPLICATIONCODEENUM);
				
				//If we get a 400 error from the PIQ, it means the pdu that should
				//have been in PIQ no longer exists. If not, something else is wrong.
				if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
				{
					logError(e);
					result = false;
					pdujson = null;
				} else {
					throw e;
				}
			}
			
			if (pdujson != null){
				if (pdujson != null && pdujson.getPdu() != null
						&& pdujson.getPdu().getId().equals(pduId)) 
					result = true;
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("isPDUInSync: " + result);
		}
		
		return result;
	}


	@Override
	public void deletePDU(String pduId) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (pduId != null)) {
			doRestDelete( pduId, "v2/pdus" );
		}
	}

	@Override
	public void moveRPDUTo(String pduId, String rackId)
			throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && (pduId != null)  && (rackId != null)
				&& (isRPDUExist(Integer.parseInt(pduId)) && isRackInSync(Integer.parseInt(rackId)))){
			//Construct a rack object out of the rackId
			DeviceJSON.Device.Parent rackjson = new DeviceJSON.Device.Parent(rackId);
			
			//Then perform a move_to
			doRestPut(rackjson, "v2/pdus", pduId + "/move_to");
		}
		
	}
	
	@Override
	public void areRPDUsInSync(List<PIQItem> piqItems)
			throws RemoteDataAccessException {
		
		//First clear all the item ids in the piqNotInSync object
		piqNotInSync.clear();
		
		// verify whether piq integration is enabled.
		if (isAppSettingsEnabled()){
			//PduJSON pdujson = null;
			ResponseEntity<?> restResult = null;
			boolean piqSupportsIpChangeAPI = doesPIQSupportIpAddressChangeAPI() ;
			
			//Get all pdu devices from PIQ
			//restResult = doRestGet(null,PduJSON.class);
	
			//if (restResult != null){
			//	pdujson = (PduJSON) restResult.getBody();

				
				// List of PDU received from PIQ
				//HashMap<String, PduJSON.Pdu> pdus = getRPDUs(pdujson.getPdus());
				
				
				List<PduJSON.Pdu> allPDUs = getAllPDUs();
			
				HashMap<String, PduJSON.Pdu> pdus = getRPDUs(allPDUs);
				
				// for each of the PDUs in dcTrack verify if it is in sync. 
				for (PIQItem piqItem: piqItems) {
					if (piqItem.getItem().getPiqId() != null){
						// get matching PDU from the list obtained from PIQ
						PduJSON.Pdu pdu = pdus.get(piqItem.getItem().getPiqId().toString());
						if (pdu != null) {
							if (!isProxiedItem(piqItem.getItem()) && piqSupportsIpChangeAPI == true && 
									pdu.hasIPAddressChanged(piqItem.getItem(), piqItem.getIpAddress())) {
								// proxyIndex and piqId matched. But,IP address is changed, mark for IP update
								piqNotInSync.addItem(piqItem.getItem(), true, pdu.getIpAddress(), piqItem.getIpAddress());
							} 
							else if (pdu.isReplace(piqItem.getItem(), piqItem.getIpAddress())){
								// IpAddress and/or proxyIndex changed match mark for replacing this PDU
								piqNotInSync.addItem(piqItem.getItem(),true,true);
							}
							else if (pdu != null) {
								// IpAddress and proxyIndex is good, mark for update
								piqNotInSync.addItem(piqItem.getItem(), true);
							}
						} else {
							// item has piqId, but not found in PIQ.. mark for add
							piqNotInSync.addItem(piqItem.getItem(), false);
						} 
					} else {
						// new item, mark for add
						piqNotInSync.addItem(piqItem.getItem(),false);
					}
				}
			}
		//}
	}

	private List<PduJSON.Pdu> getAllPDUs() throws RemoteDataAccessException{
		if (!isAppSettingsEnabled()) return null;
		
		//Check to see the version. If version is less than 3.1.0, we should get all pdus once. Meaning we cannot chunk it.
		if (!doesPIQSupport("3.1.0")) {
			return getAllPDUs(-1);
		}
		
		//Here we try to get the PDUs in chunks. First we try out 200 then, if that fails 100, 20, 10 and 1. If all fail then throw exception
		List<Integer> limitList = new ArrayList<Integer>(){{ add(200); add(100); add(20); add(10); add(1);}};
		List<PduJSON.Pdu> allPDUs = new ArrayList<PduJSON.Pdu>();
		boolean done = false;
		RemoteDataAccessException remoteDataAccessException = null;
		
		for (Integer limit:limitList){
			try {
				allPDUs = getAllPDUs(limit);
				done = true;
				break;
			} catch (RemoteDataAccessException e){
				log.warn("Getting all PDUs timed out. Retrying by reducing the limit");
				if (((RestClientException)e.getExceptionContext().getExceptionItem("EXCEPTION")).getMessage().contains(TIMEOUT_MSG))
					remoteDataAccessException = e;
				else
					throw e;
			}
		}
		
		if (!done && remoteDataAccessException != null) throw remoteDataAccessException;
		
		return allPDUs;
	}

	@Override
	public PIQItemNotInSync getPIQItemsInSync() {
		return piqNotInSync;
	}
	
	@Override
	public String updateExternalKey(Item pduItem, String ipAddress,
			String piqIdForReset, boolean reset) throws RemoteDataAccessException {
		String piq_id = null;
		if (isAppSettingsEnabled() && pduItem != null){
			
			if (!canSyncItem(pduItem)) {
				if (log.isDebugEnabled()) {
					log.debug("Will not sync PDU " + pduItem);
				}
				return null;
			}
			
			Integer it_piq_id = pduItem.getPiqId();
			
			String pduId = null;

			if (it_piq_id != null){
				
				pduId = it_piq_id.toString();
			
			} else if (piqIdForReset != null){
				pduId = piqIdForReset;
			}
			
			
			if (pduId != null  && isRPDUExist(Integer.parseInt(pduId))){
				//Now update the device item external key
				PduJSON pdujson = new PduJSON();
				
				pdujson.setExternalKey(pduItem, ipAddress, reset, pduId);
				
				pduItem.setPiqExternalKey(pdujson.getPdu().getExternalKey());
				
				//Sync only if external key is different.
				if (!isExternalKeyInSync(pduId,pdujson.getPdu().getExternalKey()))
				{
					doRestPut(pdujson, pduId);
				}
			}
		
		}
			
		return piq_id;
	}
	
	@Override
	public boolean isProxyIndexInSync(Item pduItem) throws RemoteDataAccessException {
		boolean proxyIdxInSync = true;
		PduJSON pdujson = null;
		
		if (pduItem != null && pduItem.getPiqId() != null){
			try{
			pdujson = getPduJSON(pduItem.getPiqId().toString());
			}catch (RemoteDataAccessException e){
				ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
				Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
				getPiqSysEventLogger().logGenericItemError(error, eventId, pduItem, null, null, eventSource);
				
				ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
						.getExceptionContext().getExceptionItem(
								ExceptionContext.APPLICATIONCODEENUM);
				
				//If we get a 400 error from the PIQ, it means the rack that should
				//have been in PIQ no longer exists. If not, something else is wrong.
				if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
				{
					logError(e);
					proxyIdxInSync = false;
					pdujson = null;
				} else {
					throw e;
				}
			}
		}
		
		if (pduItem.getGroupingNumber() != null && pdujson != null){
			Integer proxyIdx = (pduItem.getGroupingNumber().length() > 0) ? Integer.parseInt(pduItem.getGroupingNumber()) : null;
			proxyIdxInSync = pdujson.getPdu().isProxyIndexInSync(proxyIdx);
		}
		
		return proxyIdxInSync;
	}
	
	@Override
	public Integer lookupByIPAddress(String ipAddress) throws RemoteDataAccessException {
		Integer piq_id = null;
		ResponseEntity<?> resp = doRestGet("?ip_address_eq=" + ipAddress, PdusJSON.class);
		
		if (resp != null) {
			PdusJSON json = (PdusJSON)resp.getBody();
			if (json != null) {
				List<com.raritan.tdz.piq.json.PdusJSON.Pdu> pdus = json.getPdus();
				if (pdus != null && !pdus.isEmpty()) {
					for (com.raritan.tdz.piq.json.PdusJSON.Pdu pdu : pdus) {
						if (pdu.getProxyIndex() == null) {
							piq_id = pdu.getId();
						}
					}
				}
			}
		}
		
		return piq_id;
	}

	@Override
	public boolean isPduIntegratedWithPIQ(String ipAddress, Integer piqId) throws RemoteDataAccessException {
		boolean result = false;
		if (ipAddress == null || piqId == null) return result;
		
		List<com.raritan.tdz.piq.json.PdusJSON.Pdu> pdus = lookupPdusWithIpInPiq (ipAddress);
		if (pdus != null && !pdus.isEmpty()) {
			for (com.raritan.tdz.piq.json.PdusJSON.Pdu pdu : pdus) {
				if (pdu.getId().longValue() == piqId.longValue()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public Integer lookupByIPAddressAndProxyIndex(String ipAddress, String proxyIdx) throws RemoteDataAccessException {
		Integer piq_id = null;
		if (proxyIdx == null) {
			return lookupByIPAddress(ipAddress);
		}
		
		ResponseEntity<?> resp = doRestGet("?ip_address_eq=" + ipAddress + "&proxy_index_eq=" + proxyIdx, PdusJSON.class);
		
		if (resp != null) {
			PdusJSON json = (PdusJSON)resp.getBody();
			if (json != null) {
				List<com.raritan.tdz.piq.json.PdusJSON.Pdu> pdus = json.getPdus();
				if (pdus != null && !pdus.isEmpty()) {
					piq_id = pdus.get(0).getId();
				}
			}
		}
		
		return piq_id;
	}
	
	@Override
	public void postProcessAddPDU(Item probeItem, Item pduItem, Integer piqId)
			throws RemoteDataAccessException {
		if (piqId != null) {
			
			if (pduItem.getPiqId() == null || !pduItem.getPiqId().equals( piqId ) ) {
				pduItem.setPiqId( piqId );
				pduItem.setPiqExternalKey(PduJSON.externalKeyPrefix + pduItem.getItemId());
			}
			
			if (pduItem.getParentItem() != null) {
				String piqIdStr = piqId != null ? piqId.toString() : null;
				String parentPiqId = pduItem.getParentItem().getPiqId() != null ? pduItem.getParentItem().getPiqId().toString() : null;
				moveRPDUTo(piqIdStr, parentPiqId);
			}

			// Find and link any power ports on this PDU or EMX item with outlets
			linkPDUPowerPorts( pduItem );
			
			// link sensor ports to sensors connected on this PDU
			piqSyncSensorClient.linkSensorPorts (probeItem, pduItem );
			
			// If there is an asset strip attached to this PDU or EMX,
			// then create the appropriate asset strip connect events in the system log
			List<Long> eventIds = assetStripAutoAssociation.createAssetStripConnectedEvents( pduItem );
			for (Long eventId : eventIds) {
				if (eventId != null) {
					// We found an asset strip attached, create a sensor port for this item
					// Give the LNevent thread enough time to complete the task
					taskService.runDelayedTask(new AssetStripLinkTask(eventId), 3);
				}
			}
		}   
	}
	
	private void postProcessUpdatePDU(Item probeItem, Item pduItem, Integer piqId)
			throws RemoteDataAccessException {
		if (piqId != null) {
			
			if (pduItem.getPiqId() == null || !pduItem.getPiqId().equals( piqId ) ) {
				pduItem.setPiqId( piqId );
				pduItem.setPiqExternalKey(PduJSON.externalKeyPrefix + pduItem.getItemId());
			}
			// Find and link any power ports on this PDU or EMX item with outlets
			linkPDUPowerPorts( pduItem );
		}
	}
	
	/** Tracks PDU rescans in progress so we don't rescan the same PDU more than once at a time **/
	private Set<Integer> rescansInProgress = new HashSet<Integer>();
	
	@Override
	public String rescan(Integer pduId) throws RemoteDataAccessException {
		if (pduId == null) throw new IllegalArgumentException("Rescan of null pduId");
		String health = null;
		boolean isRescanInProgress = false;
		
		synchronized(rescansInProgress) {
			if (rescansInProgress.contains(pduId)) {
				isRescanInProgress = true;
			}
			else {
				rescansInProgress.add( pduId );
			}
		}
		
		if (!isRescanInProgress) {
			try {
				ResponseEntity<?> restResult = doRestGet("rescan/" + pduId, PduJSON.class);
				if (restResult != null) {
					PduJSON pduJson = (PduJSON)restResult.getBody();
					if (pduJson != null) {
						PduJSON.Pdu pdu = pduJson.getPdu();
						if (pdu.getHealth() != null) {
							health = pdu.getHealth().getOverall();
						}
					}
				}
			}
			catch (RemoteDataAccessException e) {
				log.error("Rescan PDU failed: " + e.getMessage());
			}
			finally {
				synchronized(rescansInProgress) {
					rescansInProgress.remove( pduId );
				}
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Rescan of PDU " + pduId + "is already in progress");
			}
		}
		
		return health;
	}

	@Override
	public List<String> syncPduReadings (List<Item> pduItems) throws DataAccessException, RemoteDataAccessException, Exception {
		
		if (!isAppSettingsEnabled()) return (new ArrayList<String>());
		long startTime = System.currentTimeMillis();
		// initialize errors object
		MapBindingResult errors = getErrorObject(); 

		// check whether item has piq_id and exists in Power IQ
		verifyPIQIntegration(pduItems, errors);
		
		try {
			// No PIQ integration errors, proceed with synchronizing the readings.
			if (!errors.hasErrors()) {
				// syncPorts property is set via homes.xml
				for (PIQSyncPorts piqSyncPort: syncPorts) {
					piqSyncPort.syncPortReadings(pduItems, errors);
				} 
			}
			else {
				// throw business validation exception.
				throwBusinessValidationException(errors);
			}
		} catch (BusinessValidationException e){
			throw new Exception(e);
		} catch (RemoteDataAccessException re) {
			throw new Exception(re);
		} catch (DataAccessException de){
			throw new Exception(de);
		}
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		
	    if (log.isDebugEnabled()) log.debug("SyncPduReadings took about" + elapsedTime + " miliseconds");
		
		return syncReadingResponse(errors); 
	}

	@Override
	public List<String> syncAllPduReadings(@Payload("#this[0]") String piqHost, @Payload("#this[1]") String locationName, @Payload("#this[2]") List<TYPE> types) throws Exception {
		if (!isAppSettingsEnabled()) return (new ArrayList<String>());
		long startTime = System.currentTimeMillis();
		// initialize errors object
		MapBindingResult errors = getErrorObject(); 
		
		try {
			
			if (types.contains(TYPE.ALL)){
				// syncPorts property is set via homes.xml
				for (PIQSyncPorts piqSyncPort: syncPorts) {
					piqSyncPort.syncAllPortReadings(piqSyncChunkLimit, errors);;
				} 
					
				logPIQSyncReadingsEvent(piqHost,locationName,types, true);
			} else {
				// syncPorts property is set via homes.xml
				for (TYPE type: types) {
					PIQSyncPorts piqSyncPort = syncPortsMap.get(type);
					if (piqSyncPort != null)
						piqSyncPort.syncAllPortReadings(piqSyncChunkLimit, errors);;
				} 
					
				logPIQSyncReadingsEvent(piqHost,locationName,types, true);
			}
		}catch (RemoteDataAccessException re) {
			//logPIQSyncReadingsEvent(piqHost,locationName,types, false);
			throw new Exception(re);
		} catch (DataAccessException de){
			//logPIQSyncReadingsEvent(piqHost,locationName,types, false);
			throw new Exception(de);
		}
		
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		if (log.isDebugEnabled())
			log.debug("SyncPduReadings took: " + elapsedTime + " milliseconds");
		
		return syncReadingResponse(errors); 
	}

	@Override
	public void updateIpAddress (String ip, DataPort  dPort) throws RemoteDataAccessException {
		Integer pduId = dPort.getItem().getPiqId();
		if (pduId != null && pduId > 0){
			PduJSON pduJson = getPduJSON(pduId.toString());
			if ( ! (pduJson.getPdu().getIpAddress()).equals((ip))) {
				// here update only the ip address
				updateIpAddress (pduJson.getPdu().getIpAddress(),  ip);
			}
			
			try {
				// here update other parameters like snmp community strings etc,.
				updateRPDU(dPort.getItem(), dPort, ip);
			} 
			catch (InstalledPDUWithoutIPException e) {
				// We've already handled and logged this exception earlier.
				// We can ignore it here because it will not bubble up to initial sync.
			}

		}
	}
	
	@Override
	public void updateIpAddress (String oldIpAddress, String newIpAddress) throws RemoteDataAccessException {
		PDUIPAddressJSON pduIpAddressJson = new PDUIPAddressJSON();
		
		PduIpAddress pduIp = new PduIpAddress(oldIpAddress, newIpAddress);
		List<PduIpAddress> pduIpAddresses = new ArrayList<PduIpAddress>();
		pduIpAddresses.add(pduIp);
		
		pduIpAddressJson.setPduIpAddresses(pduIpAddresses);
		updateIPAddresses (pduIpAddressJson);
	}

	@Override
	public void updateIPAddresses(PDUIPAddressJSON pduIpAddressJson) throws RemoteDataAccessException {
		
		if (log.isDebugEnabled()) log.debug("update pdu IP addresses");
			
		// return when PIQ integration is not enabled
		if (!isAppSettingsEnabled()) return;
		
		if (pduIpAddressJson == null || 
				pduIpAddressJson.getPduIpAddresses() == null ||
				pduIpAddressJson.getPduIpAddresses().size() <= 0) {
			return;
		}
			
		try {
			PdusJSON pdusJson = (PdusJSON)doRestPost(pduIpAddressJson, "v2/pdus/update_ip_addresses", null, PdusJSON.class);
			 // we don't use return value at this time. just assert to make sure it is not null
			assert(pdusJson != null); 
		}
		catch (RemoteDataAccessException e) {
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			// Handle license error
			if (isPIQLicenseError( error )) {
				throw e;
			}
			// if this error is in response to ipAddressChange then log event, otherwise re-throw error
			if ( isPIQIpAddressChangeResponse (error)) {
				List<String> formatedMsg = new ArrayList<String>();
				for (String msg: error.getMessages()) {
					if (msg != null) {
						msg = msg.replace("Source ", "");
						msg = msg.replace("Destination ", "");
						formatedMsg.add(msg);
					}
				}
				error.setMessage(formatedMsg);

				Long eventId = (Long)e.getExceptionContext().getExceptionItem("eventId");
				getPiqSysEventLogger().logGenericItemError(error, eventId, null, null, null, eventSource);
			} else {
				throw e;
			}
		}
	}
	
	@Override
	public boolean isProxiedItem (Item item) {
		String proxyIdx = null;
		if (item != null) proxyIdx = item.getGroupingNumber();
		return (proxyIdx != null && !proxyIdx.isEmpty());
	}
	
	@Override
	public boolean doesPIQSupportIpAddressChangeAPI () {
		boolean result = false;
		try {
			// get piq version from database dct_app_settings table
			String reportedVersion = appSettings.getProperty(Name.PIQ_VERSION);
			
			// piq ip_address_change api supported version 4.1.0 or greater
			final String baseVersion = "4.1.0";
			
			if (reportedVersion == null || reportedVersion.isEmpty()) {
				piqSyncPIQVersion.syncPIQVersion();
				reportedVersion = appSettings.getProperty(Name.PIQ_VERSION);
			}
			
			// verify if reportedVersion  >= baseVersion (ip_address_change api supported version)
			result = VersionCheck.verifyVersionGreaterOrEqual (reportedVersion, baseVersion); 
					
		} catch (DataAccessException e) {
			// If system raises  exception, while getting PIQ info 
			// return false. This will set the system to handle 
			// ip address change old way (i.e delete and add rpdu)
			return result;
		}
		return result;
	}
	
	@Override
	public boolean isPIQResponding () {
		int numOfTries = 4;
		
		while (numOfTries != 0) {
			try {
				Thread.sleep(30000); // 30 seconds
				PIQInfoJSON piqInfoJson = piqSyncPIQVersion.getPIQInfoJSON();
				assert(piqInfoJson != null);
				return true; 
			} catch (RemoteDataAccessException e) {
				log.error("Operation to check if PIQ is responding got RemoteDataAccessException, retry...");
				numOfTries --;
			} catch (InterruptedException e) {
				log.error("Operation to check if PIQ is responding is interrupted, retry...");
				numOfTries --;
			}
		}
		return false;
	}
	
	
	/******************************** Private methods **************************************/

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
				logError(e);
				rackInSync = false;
			}
		}
		return rackInSync;
	}
	
	
	private PduJSON getPduJSON(String pduId)
			throws RemoteDataAccessException {
		ResponseEntity<?> restResult = null;
		PduJSON pdujson = null;
		
		if (pduId != null && (new Integer(pduId)).intValue() > 0){
			restResult = doRestGet("v2/pdus", pduId, PduJSON.class);
		}
		
		if (restResult != null){
			pdujson = (PduJSON)restResult.getBody();
		}
		
		return pdujson;
	}
	
	/**
	 * @param pduId
	 */
	private boolean isRPDUExist(Integer pduId) {
		boolean pduInSync = false;
		try {
			if (pduId != null)
				pduInSync = isRPDUInSync(pduId.toString());
		} catch (RemoteDataAccessException e){
			
			ApplicationCodesEnum appCodeEnum = (ApplicationCodesEnum) e
					.getExceptionContext().getExceptionItem(
							ExceptionContext.APPLICATIONCODEENUM);
			
			//If we get a 400 error from the PIQ, it means the locavalidateInstalledPDUHasIPAddresstion that should
			//have been in PIQ no longer exists. If not, something else is wrong.
			pduInSync = true;
			if (appCodeEnum == ApplicationCodesEnum.REST_CALL_FAILED_CLIENT_ERROR)
			{
				logError(e);
				pduInSync = false;
			}
		}
		return pduInSync;
	}
	
	private boolean isExternalKeyInSync(String pduId, String externalKey){
		boolean extKeyInSync = false;
		if (isAppSettingsEnabled()){
			PduJSON pdujson = null;
			if (pduId != null && (new Integer(pduId)).intValue() > 0){
				try {
					pdujson = getPduJSON(pduId);
				} catch (RemoteDataAccessException e) {
					if (log.isDebugEnabled())
						e.printStackTrace();
					//Consume the exception if any and set the result to false
					extKeyInSync = false;
				}
			}
			
			if (pdujson != null && pdujson.getPdu() != null
					&& pdujson.getPdu().isExternalKeyInSync(externalKey)) 
				extKeyInSync = true;
		}
		return extKeyInSync;
	}
	
	private void updateOutletPiqId (Integer pduPiqId, PowerPort port) {
		try {
			// Equating power port "sort order" with PIQ outlet number to fString addressind the corresponding outlet
			Outlet outlet = piqSyncOutletClient.findOutlet( pduPiqId, port.getSortOrder() );
			if (outlet != null && port != null) {
				port.setPiqId( outlet.getId() );
				//We need to explicitly save this power port so that it has the correct PIQ id
				portHome.savePowerPort(port);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Could not find PIQ outlet for port " + port);
				}
			}
		}
		catch (RemoteDataAccessException e) {
			log.error("", e);
		} catch (DataAccessException e) {
			log.error("", e);
		}
	}
	
	private void updateInletPiqId (Integer pduPiqId, PowerPort port) {
		try {
			// Equating power port "sort order" with PIQ outlet number to find the corresponding outlet
			Inlet inlet = piqSyncInletClient.findInlet( pduPiqId, port.getSortOrder() );
			if (inlet != null && port != null) {
				port.setPiqId( inlet.getId() );
				//We need to explicitly save this power port so that it has the correct PIQ id
				portHome.savePowerPort(port);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("Could not find PIQ outlet for port " + port);
				}
			}
		}
		catch (RemoteDataAccessException e) {
			log.error("", e);
		} catch (DataAccessException e) {
			log.error("", e);
		}
	}
	
//	private void updateSensorPiqId (Integer pduPiqId, SensorPort port) {
//		try {
//			// Equating  sensor port's "sensor type and sort order" 
//			// with PIQ sensor type and ordinal to find the corresponding sensor
//			// connected to the PDU.
//			Sensor sensor = piqSyncSensorClient.findSensor( pduPiqId, port.getAddress(),port.getSortOrder());
//			if (sensor != null && port != null) {
//				Long sensorSubClass = piqSyncSensorClient.getSensorSubClass(sensor); 
//				if (sensorSubClass != null && 
//						sensorSubClass == port.getPortSubClassLookup().getLkpValueCode()) {
//					port.setPiqId(sensor.getId());
//					//We need to explicitly save this power port so that it has the correct PIQ id
//					portHome.saveSensorPort(port);
//				} else {
//					// log event.
//				}
//			}
//			else {
//				if (log.isDebugEnabled()) {
//					log.debug("Could not find PIQ outlet for port " + port);
//				}
//			}
//		}
//		catch (RemoteDataAccessException e) {
//			log.error("", e);
//		} catch (DataAccessException e) {
//			log.error("", e);
//		}
//	}


	/**
	 * Iterate through the power ports of the "linked" PDU item and map to outlets as necessary. 
	 * @param pduItem
	 */
	@Transactional
	private void linkPDUPowerPorts(Item pduItem) {
		Integer pduPiqId = pduItem.getPiqId();
		if (pduPiqId == null) {
			if (log.isDebugEnabled()) {
				log.debug("linkPDUPowerPorts: PIQ ID is null");
			}
			return;
		}
		
		List<PowerPort> powerPorts = null;
		try {
			powerPorts = itemHome.viewPowerPortsForItem(pduItem.getItemId(), false, null);
		}
		catch (DataAccessException e) {
			log.error("", e);
		}
		
		if (powerPorts != null) {
			
			for (PowerPort port : powerPorts) {
				
				if (isRPDUOutput(port)) {
					updateOutletPiqId(pduPiqId, port);
				} else if (isRPDUInput(port)) {
					updateInletPiqId(pduPiqId, port);
				}
			}
		}
	}

	private HashMap<String,PduJSON.Pdu> getRPDUs(List<PduJSON.Pdu> pdus){
		HashMap<String,PduJSON.Pdu> devicesHashmap = new HashMap<String,Pdu>();
		if (pdus != null){
			for (Pdu device:pdus){
				if (device != null)
					devicesHashmap.put(device.getId(), device);
			}
		}
		return devicesHashmap;
	}

	/**
	 * Checks if the PDU is installed with data port abd no IP address. If this condition is detected
	 * then it will log an error to the system event log and throw an exception.
	 * @param pduItem the PDU
	 * @param dataPort the data port
	 * @param ipAddress the IP Address
	 * @throws InstalledPDUWithoutIPException
	 */
	private void validateInstalledPDUHasIPAddress(Item pduItem, String ipAddress) throws InstalledPDUWithoutIPException {
		if (ipAddress != null) {
			// check if there was no ip address event raised earlier if so, clear it now because we have ip address
			processedEvents.remove(pduItem.getItemId());
			return;
		}
		LksData pduStatus = pduItem.getStatusLookup();
		
		if ((pduStatus != null) && (pduStatus.getLkpValueCode() == SystemLookup.ItemStatus.INSTALLED)) {
			
			// Get data port count
			long dataPortCount = 0;
			try {
				dataPortCount = portHome.getDataPortCountForItemByClass(pduItem.getItemId(), null);
			}
			catch (DataAccessException e) {
				log.warn("" , e);
				dataPortCount = 0;
			}
			
			if (dataPortCount > 0 && processedEvents.get(pduItem.getItemId()) == null)  {
				InstalledPDUWithoutIPException ex = new InstalledPDUWithoutIPException( pduItem );
				Event ev = getPiqSysEventLogger().logInstalledPDUHasNoIpAddress( pduItem, eventSource );
				getPiqSysEventLogger().addPIQUpdateExceptionEventParams(ev, ex);
				// we are not using ev in the below map as of now.. it will be use in future to compare 
				// event type along with item Id.
				processedEvents.put (pduItem.getItemId(), ev);
				throw ex;
			}
		}
	}
	
	private String findPDUId(String externalKey) throws RemoteDataAccessException {
		String pduId = null;
		ResponseEntity<?> resp = doRestGet("?external_key_eq=" + externalKey, PduJSON.class);
		
		if (resp != null) {
			PduJSON json = (PduJSON)resp.getBody();
			if (json != null) {
				List<Pdu> pdus = json.getPdus();
				if (pdus != null && !pdus.isEmpty()) {
					pduId = pdus.get(0).getId();
				}
			}
		}
		
		return pduId;
	}
	

	private String handleExternalKeyExist(Item pduItem, String externalKey,
			boolean skipSyncCheck) throws RemoteDataAccessException {
		//Find the power IQ Id 
		String id = findPDUId(externalKey);
		
		//Set it to rackItem
		pduItem.setPiqId(Integer.parseInt(id));
		
		//Call update to make sure everything is in sync.
		//updateRack(rackItem, skipSyncCheck);
		
		return id;
	}
	
	private String handleExternalKeyExistUpdate(Item pduItem, String externalKey, DataPort dataPort,
			String ipAddress, boolean skipSyncCheck) throws RemoteDataAccessException, InstalledPDUWithoutIPException {
		//Find the power IQ Id 
		String id = findPDUId(externalKey);
		
		//First reset the old pdu's external key
		//Since we are resetting only the external key, we do not need to wait for the job to be completed.
		//Now update the rack item
		PduJSON pdujson = new PduJSON(pduItem, dataPort, ipAddress, true);
		pdujson.getPdu().setExternalKey(ipAddress);
		JobJSON jobJson = null;
		jobJson = (JobJSON)doRestPut(pdujson, "v2/pdus", id+"?async=true",JobJSON.class);
		
		
		//Then update the PDU
		updateRPDU(pduItem, dataPort, ipAddress);
		
		return id;
	}
	
	private String mapUsingExternalKey(Item pduItem, String ipAddress)
			throws RemoteDataAccessException, InstalledPDUWithoutIPException {
		validateInstalledPDUHasIPAddress(pduItem, ipAddress);
		String piqId = findPDUId(pduItem.getPiqExternalKey());
		pduItem.setPiqId(Integer.parseInt(piqId));
		updateExternalKey(pduItem, ipAddress, null, false);
		postProcessAddPDU(null, pduItem, Integer.parseInt(piqId));
		mapParent(pduItem);
		return piqId;
	}
	
	private String mapParent(Item pduItem) throws RemoteDataAccessException{
		if (pduItem.getParentItem() != null){
			String id = getParentPIQId(pduItem);
			pduItem.getParentItem().setPiqId(Integer.parseInt(id));
			log.info(new StringBuilder("Map: ").append(pduItem.getItemName()).append("to: ").append(id));
			if (pduItem.getParentItem().getPiqId() != null)
				piqSyncRackClient.updateExternalKey(pduItem.getParentItem(), null, false);
			piqSyncRackClient.mapParent(pduItem.getParentItem());
			return id;
		} else
			return null;
		
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
	
	private String reMapUsingExternalKey(Item pduItem, String ipAddress,
				String generatedExternalKey) throws RemoteDataAccessException, InstalledPDUWithoutIPException {
		try {
			String piqid = mapUsingExternalKey(pduItem,ipAddress);
			
			if (piqid != null){
				pduItem.setPiqId(Integer.parseInt(piqid));
				log.info(new StringBuilder("Map: ").append(pduItem.getItemName()).append("to: ").append(piqid));
			}
			else {
				pduItem.setPiqExternalKey(generatedExternalKey);
				addExternalKeyNotInSyncEvent(pduItem);
			}
			return piqid;
		} catch (NumberFormatException ne){
			if (log.isDebugEnabled()) log.debug(new StringBuffer("Mapping using externalKey failed: ")
													.append(pduItem.getItemName())
													.append(" for external key ")
													.append(pduItem.getPiqExternalKey()));
			addExternalKeyNotInSyncEvent(pduItem);
			return null;
		}
	}
	
	private String getParentPIQId(Item pduItem) throws RemoteDataAccessException {
		if (isAppSettingsEnabled() && pduItem != null && pduItem.getPiqId() != null){
			String url = pduItem.getPiqId().toString() + "/parent";
			
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
	
	
	private void deleteItem(Item itItem) {
		try {
			meItemDeleteBehavior.deleteItem(itItem);
			itemDeleteCommonBehavior.deleteItem(itItem);
		} catch (BusinessValidationException e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			else {
				log.warn(new StringBuffer("Could not delete item: ")
					.append(itItem.getItemName())
					.append(" for external key ")
					.append(itItem.getPiqExternalKey())
					.append(" during the mapping of PowerIQ Id"));
			}
			throw new CannotDeleteItem(e);
		} catch (Throwable e) {
			if (log.isDebugEnabled())
				e.printStackTrace();
			else {
				log.warn(new StringBuffer("Could not delete item: ")
									.append(itItem.getItemName())
									.append(" for external key ")
									.append(itItem.getPiqExternalKey())
									.append(" during the mapping of PowerIQ Id"));
			}
			throw new CannotDeleteItem(e);
		}
	}
	
	private void logPIQSyncReadingsEvent(String host, String locationName, List<TYPE> types, boolean success) throws DataAccessException{
		
		EventSeverity severity = success ? EventSeverity.INFORMATIONAL : EventSeverity.CRITICAL;
		String errorCode = success ? "piqSync.syncAllReadings.success" : "piqSync.syncAllReadings.failed";
		Event event = eventHome.createEvent(EventType.SYNC_FLOORMAP_DATA, severity, eventSource);
		String summary = eventHome.getMessageSource().getMessage(errorCode, new Object[] { locationName },
							"Measured readings update", Locale.getDefault());
		event.setSummary(summary);
		event.addParam("Port Type(s)", types.toString().replace("[", "").replace("]", ""));
		
		eventHome.saveEvent(event);
	}
	
	
	/**
	 * A runnable task for creating and linking a new asset strip sensor port.
	 * @author Andrew COhen
	 */
	private class AssetStripLinkTask implements Runnable {
		private long eventId; // Event ID of "asset strip attached" event
		
		AssetStripLinkTask(long eventId) {
			this.eventId = eventId;
		}
		
		@Override
		public void run() {
			try {
				Event ev = getEventHome().getEventDetail( eventId );
				if (ev == null) {
					log.warn("AssetStripLinker: Could not find with with ID=" + eventId +". Asset strip port will not be created!");
					return;
				}
				assetStripAutoAssociation.addAssociation( ev );
			}
			catch (Throwable t) {
				log.error("AssetStripLinkTask error", t);
			}
		}
	}
	
	/**
	 * A task for rescanning a PDU.
	 * @author Andrew Cohen
	 */
	private class RescanTask implements Runnable {
		private int pduId;
		private Item pduItem;
		
		RescanTask(int pduId, Item pduItem) {
			this.pduId = pduId;
			this.pduItem = pduItem;
		}
		
		@Override
		@Transactional(propagation = Propagation.REQUIRED)
		public void run() {
			try {
				String health = rescan( pduId );
				if (log.isDebugEnabled()) {
					log.warn("Rescan of PDU " + pduId + " returned health: '" +  health + "'");
				}
			}
			catch (Throwable t) {
				log.error("Rescan Task Error", t);
			}
			finally {
				postRescan();
			}
		}
		
		private void postRescan() {
			linkPDUPowerPorts( pduItem );
			
			List<Long> eventIds = null;
			
			try {
				eventIds = assetStripAutoAssociation.createAssetStripConnectedEvents( pduItem );
			}
			catch (RemoteDataAccessException e) {
				String msg = getMessageSource().getMessage("remote.error",
						new Object[] { e.getUrl(), e.getMessage() },
						null
				);
				log.error(msg);
				return;
			}
			
			for (Long eventId : eventIds) {
				if (eventId != null) {
					Event ev;
					try {
						ev = getEventHome().getEventDetail( eventId );
						if (ev != null) {
							assetStripAutoAssociation.addAssociation( ev );
						}
					} 
					catch (DataAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}
	
	private boolean isRPDUOutput(PowerPort port) {
		if (port == null) return false;
		LksData subClass = port.getPortSubClassLookup();
		if (subClass == null) return false;
		Long subClassValueCode = subClass.getLkpValueCode();
		if (subClassValueCode == null) return false;
		return subClassValueCode == SystemLookup.PortSubClass.RACK_PDU_OUTPUT;
	}
	
	private boolean isRPDUInput(PowerPort port) {
		if (port == null) return false;
		LksData subClass = port.getPortSubClassLookup();
		if (subClass == null) return false;
		Long subClassValueCode = subClass.getLkpValueCode();
		if (subClassValueCode == null) return false;
		return subClassValueCode == SystemLookup.PortSubClass.INPUT_CORD;
	}

	private void verifyPIQIntegration( List<Item> pduItems, Errors errors) throws RemoteDataAccessException, DataAccessException {
		
		for (Item item: pduItems) {
			if (item != null) {
				// add event and inform user about missing piq_id or 
				// item not not found in Power IQ.
				Integer piqId = item.getPiqId();
				if (piqId == null) {
					AddSensorEvent(item,"piqSync.itemNotIntegrationWitPIQ", errors, "Item not integrated with Power IQ.");
				} else if (isRPDUInSync(piqId.toString()) == false) {
					AddSensorEvent(item,"piqSync.itemNotFoundInPIQ", errors, "Item not found in Power IQ.");
				}
			}
		}
	}

	private void AddSensorEvent(Item item, String code, Errors errors, String defaultMsg) throws DataAccessException {
		Item probeItem = probeLookup.getProbeItemForDummyRackPDU( item.getItemId() );

		Object[] args = {probeItem != null ? probeItem.getItemName() : item.getItemName()};
		errors.rejectValue("SyncSensor", code, args, defaultMsg);
		String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());

		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Event ev = eventHome.createEvent(createdAt, EventType.INVALID_SENSOR_REQUEST, EventSeverity.CRITICAL, "dcTrack");
		ev.setSummary(evtSummary);
		ev.addParam("Item Name", (probeItem != null) ? probeItem.getItemName() : item.getItemName());
		eventHome.saveEvent(ev);
	}
	
	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, SensorPort.class.getName());
		return errors;
	}

	private void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.setCallbackURL("itemService.saveItem"); //<-- ignore this ???
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			throw e;
		}
	}
	
	private List<String> syncReadingResponse(Errors errors) {
		List<String> response = new ArrayList<String>();
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				response.add(msg);
			}
		}
		return response;
	}
	
	
	private List<com.raritan.tdz.piq.json.PdusJSON.Pdu> lookupPdusWithIpInPiq (String ipAddress) throws RemoteDataAccessException {

		List<com.raritan.tdz.piq.json.PdusJSON.Pdu> pdus = null;
		ResponseEntity<?> resp = doRestGet("?ip_address_eq=" + ipAddress, PdusJSON.class);
		if (resp != null) {
			PdusJSON json = (PdusJSON)resp.getBody();
			if (json != null) {
				pdus = json.getPdus();
			}
		}
		return pdus;
	}
	

	
	// JSON request to get all of the PDU details from PIQ times out, when PIQ
	// has hundreds of PDUs, due to large data in response (CR-49378)
	// With new changes this function now get them in smaller chunks and collect them

	private List<PduJSON.Pdu> getAllPDUs(int limit) throws RemoteDataAccessException {
		List<PduJSON.Pdu> pdus = new ArrayList<PduJSON.Pdu>();

		if (!isAppSettingsEnabled()) return null;
		
		//If the PowerIQ version is less than 3.1.0 then we have to fall back on the old way of getting pdus in bulk
		if (limit < 0){
			ResponseEntity<?> result = doRestGet(null,PduJSON.class);
			if (result != null){
				PduJSON json = (PduJSON)result.getBody();
				pdus = json.getPdus();
			}
			
			return pdus;
		}
		
		String lastPiqId = "0";
		Boolean done = false;
		
		
		while (!done){
			StringBuilder prefix = new StringBuilder("?order=id.asc&limit=").append(limit).append("&id_gt=").append(lastPiqId);
			ResponseEntity<?> result = doRestGet(prefix.toString(), PduJSON.class);
			if (result != null) {
				PduJSON json = (PduJSON)result.getBody();
				List<PduJSON.Pdu> localPdus = json.getPdus();
				if (localPdus != null && localPdus.size() > 0){
					lastPiqId = localPdus.get(localPdus.size() - 1).getId();
					pdus.addAll(localPdus);
				}
				else 
					done = true;
			} else {
				done = true;
			}
		}
		
		
		return pdus;
	}
	
	private boolean doesPIQSupport (String baseVersion) {
		boolean result = false;
		try {
			// get piq version from database dct_app_settings table
			String reportedVersion = appSettings.getProperty(Name.PIQ_VERSION);
			
			
			// verify if reportedVersion  >= baseVersion (ip_address_change api supported version)
			result = VersionCheck.verifyVersionGreaterOrEqual (reportedVersion, baseVersion); 
					
		} catch (DataAccessException e) {
			// If system raises  exception, while getting PIQ info 
			// return false.
			return result;
		}
		return result;
	}

}
