package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.CircuitItemViewData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemViewData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.item.json.MobileSearchItemInfo;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.piq.dto.SyncAllPDUReadingsStatusDTO;
import com.raritan.tdz.piq.home.PIQSyncPorts.TYPE;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;
import com.raritan.tdz.util.RequestDTO;

/**
 * 
 * @author Andrew Cohen
 */
public interface ItemHome extends com.raritan.tdz.home.ItemHome {

	/**
	 * Get list of items.
	 * @param locationId
	 * @return List of items
	 * @throws DataAccessException
	 */
	public List<CircuitItemViewData> viewItemsForLocation(Long locationId, boolean activeItemOnly, Long itemId, Long portClassValueCode) throws DataAccessException;	
	
	/**
	 * Get list of data ports for an item.
	 * @param itemId
	 * @return List of data ports
	 * @throws DataAccessException
	 */
	@Transactional(readOnly = true)
	public List<DataPort> viewDataPortsForItem(Long itemId, boolean freePortOnly, List<Long>portList) throws DataAccessException;
	
	/**
	 * Get list of power ports for an item.
	 * @param itemId
	 * @return List of power ports
	 * @throws DataAccessException
	 */
	@Transactional(readOnly = true)
	public List<PowerPort> viewPowerPortsForItem(Long itemId, boolean freePortOnly, List<Long>portList) throws DataAccessException;
	
	public Map<Integer, String> getDataPortNetInfoAsMap(Long itemId);
	
	public HashMap<Long, Long> getItemPortCount(Long portClass, boolean freePortOnly);
	
	
	/**
	 * Get Available U Positions
	 * @param cabinetId
	 * @param itemRUHeight
	 * @param itemId
	 * @param railsUsed
	 * @param reservationId 
	 * @return Collection<Long>
	 * @throws DataAccessException
	 */
	public Collection<Long> getAvailableUPositions(Long cabinetId, int itemRUHeight, long itemId, long railsUsed, Long reservationId) throws DataAccessException;

	/**
	 * Returns a factory object that can be used to obtain business classes for various types of items.
	 * @return 
	 */
	public ItemObjectFactory getFactory();
	
	/**
	 * Get list of items.
	 * @param locationId
	 * @return An Item
	 * @throws DataAccessException
	 */
	public ItemViewData viewItems(Long itemId) throws DataAccessException;

	CircuitItemViewData viewCircuitItems(Long itemId)
			throws DataAccessException;	
	
	/**
	 * This will return a list of all the item classes in the system from 
	 * the Lookup table.
	 * @return
	 */
	public List<SystemLookupDTO> getAllItemClassLookup();
	
	/**
	 * This will return a list of all the item classes in the system from 
	 * the Lookup table.
	 * @return
	 */
	public List<SystemLookupDTO> getSystemLookup(String lkpValue);
	
	/**
	 * This performs a search on a given criteria and gives back a list of items via ItemSearchResultDTO
	 * @param criteraDTO
	 * @return
	 * @throws DataAccessException
	 * @throws BusinessValidationException
	 * @throws SystemException 
	 */
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTO criteraDTO) throws BusinessValidationException, SystemException;
	
	/**
	 * Given a class lkpValue code, returns the total count of items across all sites.
	 * @param classLkpValueCode
	 * @return
	 */
	public Long getTotalItemCountForClass(Long classLkpValueCode);
	
	public String getItem(Long itemId, String xPath, UserInfo userInfo) throws Throwable;
	
	public Map<String, UiComponentDTO> getItem(Long itemId, UserInfo userInfo) throws Throwable;
	
	public Boolean isUnique(String uiId, Object value, String siteCode, Long parentId, String ignoreProperty, Object ignorePropertyValue) throws DataAccessException, ClassNotFoundException;
	
	public String getGeneratedStorageItemName() throws DataAccessException, BusinessValidationException, ClassNotFoundException;
	
	public List<ValueIdDTO> getAllLocations();

	public List<SystemLookupDTO> getStatusLookupForCurrentState(Long itemId, Long modelId, UserInfo userInfo) throws DataAccessException, ClassNotFoundException;

	public Map<String, UiComponentDTO> getItemDetails(Long itemId, UserInfo userInfo) throws Throwable;

	/**
	 * Saves an item. Also validates before saving an item.
	 * @param itemId
	 * @param dtoList
	 * @param sessionUser TODO
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo sessionUser) throws ClassNotFoundException, BusinessValidationException, Throwable;

	public Boolean isItemTagVerified(Long itemId);

	public Integer getLicenseCount();
	
	public Boolean isLicenseAvailable(int requestedCabQty);
	
	public Collection<Long> getAvailableZeroUPositions(Long cabinetId,
			long depth_lkp_value, long side_lkp_value, int itemRUHeight, 
			Long exceptionItemId)
			throws DataAccessException;

	public List<String> getProjectNumbers(Long locationId);
	public List<String> getContractNumbers(Long locationId);
	
	//<- Stackables APIs 
	@Transactional(readOnly = true)
	public List<?> getAllStackItems(long itemId) throws DataAccessException, BusinessValidationException;

	@Transactional(readOnly = true)
	public List<?> getAddableStackItems() throws DataAccessException;
	
	@Transactional
	public List<?> addStackItem(long newItemId, long siblingItemId) throws ClassNotFoundException, DataAccessException, BusinessValidationException;

	@Transactional
	public List<?> removeStackItem(long itemId, String newName) throws  ClassNotFoundException, DataAccessException, BusinessValidationException;

	@Transactional
	public List<?> removeStackItem(long itemId) throws  ClassNotFoundException, DataAccessException, BusinessValidationException;
	
	@Transactional
	public List<?> setStackName(long primaryItemId, String stackName) throws ClassNotFoundException, DataAccessException, BusinessValidationException;
	
	@Transactional
	public List<?> setStackNumber(long itemId, long stackNumber) throws  ClassNotFoundException, DataAccessException, BusinessValidationException;

	@Transactional
	public List<ValueIdDTO> getAllAvailableCabinetForZeroUModel(long locationId, long zeroUModelId, long depth_lkp_value, long side_lkp_value, long zeroUId) throws DataAccessException;

	
	/**
	 * Delete item with itemId.
	 * WARNING: Do not use this API, it may not work in all scenarios. This API is used by unit tests 
	 *          If you want to delete Item and all associated items (e.g FPDU, panel, breakers, outlets) then use deleteItems API.
	 *        
	 * @param itemId
	 * @param validate
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	@Transactional
	boolean deleteItem(long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable;

	@Transactional
	public Collection<Integer> getAvailableShelfPosition(Long cabinetId, long uPosition, long railsUsed, long editItemId);

	@Transactional
	void updateShelfPosition(long cabinetId, long uPosition, long railsLkpValueCode, Object itemDomain, Item ignoreItem) throws DataAccessException;
	
	Collection<Long> getNonRackableUPosition(Long cabinetId, long modelId,
			long railsUsed, long editItemId) throws DataAccessException;

	public Map<String, Object>getItemDetailsExtAPI(Long itemId ,UserInfo user) throws Throwable;
	
	public Map<String, Object>getItemsChildrenExtAPI(Long itemId, boolean includeContainer, boolean includeGrandchildren, UserInfo user) throws Throwable;

	public Object getAllItemsExtAPI(String listType) throws DataAccessException;
	
	public Map<String, Object> saveItemExtAPI(long itemId, Map<String, Object> itemDetails,
			UserInfo user, long origin) throws BusinessValidationException,
			ClassNotFoundException, Throwable;

	public Map<String, Object> updateItemExtAPI(long itemId,
			Map<String, Object> itemDetails, UserInfo user)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable;

	public Map<String, Object> itemRequest( List<Long> itemIds, String typeOfRequest, boolean warningConfirmed ) throws BusinessValidationException, DataAccessException;

	public Boolean doesItemStageAllowModification(Long itemId)
			throws BusinessValidationException, DataAccessException;
	
	public Boolean getEditableStatusForAnItem(long itemId, UserInfo userInfo)
			throws BusinessValidationException, DataAccessException;

	public Boolean getDeletableStatusForAnItem(long itemId, UserInfo userInfo )
			throws BusinessValidationException, DataAccessException;

	public Long getItemClass(long itemId);

	public CloneItemDTO cloneItem(CloneItemDTO itemToClone, UserInfo userInfo)
			throws DataAccessException, BusinessValidationException;

	public List<PowerPortDTO> getPowerPortDTOs(long itemId);

	public List<DataPortDTO> getDataPortDTOs(long itemId);

	public Collection<Long> getAvailUPositionsForItem(Long cabinetId,
                        int itemRUHeight, int itemUPosition, int blockSize, long railsUsed)
                        throws DataAccessException;


	/**
	 * Synchronize inlets outlets and sensors from PIQ. This function updates readings.
	 * When US2017 is implemented this function will dynamically add new sensors as well.
	 * @param ids: List of RackPDU/Probe item ids for which readings are required
	 * @return
	 * @throws BusinessValidationException
	 * @throws RemoteDataAccessException 
	 * @throws DataAccessException 
	 * @throws Exception 
	 */
	public List<String> syncPduReadings(List<Long> ids) throws BusinessValidationException, DataAccessException, RemoteDataAccessException, Exception;
	
	/**
	 * Synchronize all inlets, outlets and sensors from all configured and enabled PowerIQ
	 * @param locationId TODO
	 * @param portTypes TODO
	 * @return
	 * @throws BusinessValidationException
	 */
	public List<SyncAllPDUReadingsStatusDTO> syncAllPDUReadings(long locationId, List<TYPE> portTypes) throws BusinessValidationException;
	
	/**
	 * Get the status of sync all pdu readings given a location. Location can be -1 in which case it will return results of all the locations
	 * @param locationId
	 * @return
	 * @throws BusinessValidationException
	 */
	public List<SyncAllPDUReadingsStatusDTO> getSyncAllPDUReadingsStatus(long locationId) throws BusinessValidationException;
	
	/**
	 * Get list of sensor ports for an item.
	 * @param itemId
	 * @return List of sensor ports
	 * @throws DataAccessException
	 */
	@Transactional(readOnly = true)
	public List<SensorPort> viewSensorPortsForItem(Long itemId) throws DataAccessException;

	public List<ValueIdDTO> getAvailableCabinetsForSensor(String siteCode, Long sensorTypeLksValueCode, Long includeCabinetId);

	List<CloneItemDTO> cloneItemRemoveDup(List<CloneItemDTO> recList)
			throws DataAccessException;

	public List<UPSBankDTO> getAllUpsBanks( Long minAmpsRating );
	
	
	@Transactional(readOnly = true)
	public List<BreakerDTO> getAllBreakers(Long ampsRating, Boolean[] isUsed, Long[] breakerLkpValueCodes,  Long[] phases, Long breakerPortId, Long fpduItemId);

	public DataPortDTO getItemPortDetailsExtAPI(long itemId,
			long portId, UserInfo user) throws DataAccessException, BusinessValidationException;

	/**
	 * create the data port using the port dto
	 * @param itemId
	 * @param portDetails
	 * @param user
	 * @return
	 * @throws DataAccessException
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	public DataPortDTO createItemDataPortExtAPI(long itemId, DataPortDTO portDetails,	UserInfo user) throws DataAccessException, BusinessValidationException, ClassNotFoundException, Throwable;

	/**
	 * update the data port using the port dto
	 * @param itemId
	 * @param portId
	 * @param portDetails
	 * @param user
	 * @return
	 * @throws DataAccessException
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	public DataPortDTO updateItemDataPortExtAPI(long itemId, long portId, DataPortDTO portDetails, UserInfo user) 
			throws DataAccessException, BusinessValidationException, ClassNotFoundException, Throwable;

	public Object getAllItemDataPortsExtAPI(long itemId, UserInfo userInfo) throws DataAccessException, BusinessValidationException;
	
	public Set<BasicItemInfo> searchItemsExtAPI(String searchString, UserInfo user) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, BusinessValidationException, DataAccessException;

	public Set<MobileSearchItemInfo> searchItemsWithLocationExtAPI(String searchString, String locationString, UserInfo user, int limit, int offset) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,  BusinessValidationException, DataAccessException;
	/**
	 * Delete items in the itemIds list. 
	 * Note: this function returns warning in some cases asking for user confirmation (Yes, No, Cancel).
	 *       The warnings in BusinessValidation Exception may contain the API to be called along with 
	 *       the list of item Ids.  
	 * @param itemIds
	 * @param userInfo
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	List<Long> deleteItems(List<Long> itemIds, UserInfo userInfo) throws ClassNotFoundException,
			BusinessValidationException, Throwable;

	/**
	 * Delete items that are confirmed to be deleted after processing the warning message returned in 
	 * (deleteItems) in the first step of delete process.
	 * 
	 * @param itemIds
	 * @param deleteAssociatedItems
	 * @param validate
	 * @param userInfo
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	List<Long> deleteItemsConfirmed(List<Long> itemIds,
			Boolean deleteAssociatedItems, Boolean validate, UserInfo userInfo)
			throws ClassNotFoundException, BusinessValidationException,
			Throwable;

	/**
	 * Check if move request is allowed for the list of itemId(s)
	 * @param itemIds
	 * @param userInfo 
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public Boolean isMoveRequestAllowed(List<Integer> itemIds, UserInfo userInfo) throws BusinessValidationException, DataAccessException;

	/**
	 * process the request bypass for requests in the dtos
	 * @param requestDTOs
	 * @param bvex TODO
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public void processRequest(List<RequestDTO> requestDTOs, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException;

	/**
	 * process the request bypass for requests in the request ids
	 * @param requestDTOs
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public void processRequestUsingIds(List<Long> requestIds)
			throws BusinessValidationException, DataAccessException;

    /**
     * Get all items in a cabinet.
     * 
     * @param cabinetId
     * @return a list of item data mappings
     * @throws Throwable
     * 
     * @see com.raritan.tdz.page.service.PaginatedService#getPageList(ListCriteriaDTO, String)
     */
    public List<Map<String, Object>> getCabinetItemList(Long cabinetId) throws Throwable;

    /**
     * Save a passive item.
     * 
     * @param itemId
     * @param dtoList
     *          ValueIdDTO list
     * @param userInfo
     * @return item id
     * @throws Throwable
     */
    public Long savePassiveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo userInfo) throws Throwable;

    /**
     * Delete a passive item.
     * 
     * @param itemId
     * @throws Throwable
     */
    public void deletePassiveItem(Long itemId) throws Throwable;
    
    /**
     * This API sets the request bypass for the user.
     * @param value
     * @param userInfo
     * @throws Throwable
     */
    public void setUserRequestBypassSetting(Boolean value, UserInfo userInfo) throws Throwable;
    
    /**
     * This API gives information whether PowerIQ integration is enabled 
     * for the given item.
     * @param itemId
     * @return true when PowerIQ integration is enabled otherwise false
     * @throws Throwable
     */
    public Boolean isPIQIntegrationEanabled(Long itemId) throws Throwable;

    /**
     * Get items for item filter.
     * 
     * @param filterConfig
     * @return
     * @throws DataAccessException
     */
    List<JSONReportFilterResult> getItemForReport(JSONReportFilterConfig filterConfig) throws DataAccessException;

    /**
     * create single power port
     * @param itemId
     * @param portDetails
     * @param userInfo
     * @return
     * @throws Throwable
     */
	public PowerPortDTO createItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo) throws Throwable;

	/**
	 * edit single power port
	 * @param portDetails
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	public PowerPortDTO updateItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable;

	/**
	 * delete the power port 
	 * @param portDetails TODO
	 * @param userInfo
	 * @param portDetails
	 * @throws BusinessValidationException 
	 * @throws DataAccessException 
	 * @throws BusinessInformationException 
	 */
	public void deleteItemPowerPortExtAPI(PowerPortDTO portDetails, UserInfo userInfo) throws BusinessValidationException, DataAccessException, BusinessInformationException;

	/**
	 * save item
	 * @param itemId
	 * @param itemDetails
	 * @param user
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	Map<String, Object> saveItemImportExtAPI(long itemId,
			Map<String, Object> itemDetails, UserInfo user)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable;

	/**
	 * update item 
	 * @param itemId
	 * @param itemDetails
	 * @param user
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	Map<String, Object> updateItemImportExtAPI(long itemId,
			Map<String, Object> itemDetails, UserInfo user)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable;
	
	/**
	 * delete items in the itemIds list.
	 * Note: this API handles does not show any warnings to the user. It will continue with 
	 *       deleting items.
	 *       TODO: Refactor the code to work for both restAPI and file import. Use itemDetails
	 *            to extract itemIds. 
	 * @param itemIds
	 * @param itemDetails
	 * @param userInfo
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	
	List<Long> deleteItemsImportExtAPI(List<Long> itemIds, Map<String, Object> itemDetails, UserInfo userInfo) throws ClassNotFoundException,
				BusinessValidationException, Throwable;
	
	/**
	 * unmaps the item and all associated items to PIQ
	 * @param locationCode
	 * @param itemName
	 * @param userInfo
	 * @throws BusinessValidationException
	 */
	public void unmapItem(String locationCode, String itemName, UserInfo userInfo) throws BusinessValidationException;
	
	/**
	 * delete the data port 
	 * @param itemId
	 * @param portId
	 * @param skipValidation
	 * @param userInfo
	 * @throws BusinessValidationException
	 * @throws BusinessInformationException 
	 * @throws DataAccessException 
	 */
	public void deleteItemDataPortExtAPI(Long itemId, Long portId, boolean skipValidation, UserInfo userInfo) throws BusinessValidationException, BusinessInformationException, DataAccessException;
	
	
}
