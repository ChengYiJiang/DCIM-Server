package com.raritan.tdz.item.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.DataPortInterface;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortInterface;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.UniqueValidatorDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dto.BladeDTO;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.item.dto.ChassisItemDTO;
import com.raritan.tdz.item.dto.ChassisSlotDTO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTOImpl;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.dto.StackItemDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.reports.json.JSONReportFilterConfig;
import com.raritan.tdz.reports.json.JSONReportFilterResult;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.views.ItemObject;

/**
 * Item Service for BlazeDS. This currently extends the ItemService that exists
 * in ManageAssetsService so that all of the legacy web service APIs
 * are also exposed via BlazeDS.
 * 
 * @author Andrew Cohen
 */
public interface ItemService extends com.raritan.tdz.service.ItemService {

	/**
	 * Returns list of cabinets where sensor can be placed\
	 * @param siteCode. If null, means all sites
	 * @param sensorTypeLksValueCode - sensor type's lks_value code
	 * @param includeCabinetId - Include cabinet record for this cabinet Id
	 * @return List of ValueIDDTO where label is cabinet name and data is cabinet Id
	 * @throws ServiceLayerException
	 */
	public List<ValueIdDTO> getAvailableCabinetsForSensor(String siteCode, Long sensorTypeLksValueCode, Long includeCabinetId) throws ServiceLayerException;
	
	/**
	 * Adds item ports.
	 * @param a list of ports
	 * @return a list of port ids
	 */
	public List<Long> addItemPorts(List<PortInterface> port) throws ServiceLayerException;
	
	/**
	 * Deletes item ports.
	 * @param dataPortIds list of ports
	 * @return the number of ports successfully deleted
	 * @throws ServiceLayerException
	 */
	public long deleteItemPorts(List<PortInterface> ports) throws ServiceLayerException;
	
	/**
	 * Get list of items for a cabinet
	 * @param cabinetId
	 * @return List of items
	 * @throws ServiceLayerException
	 */
	public List<ItemObject> viewItemsForLocation(Long locationId, Long portClassValueCode) throws ServiceLayerException;
	
	/**
	 * Get list of data ports for an item
	 * @param itemId
	 * @return List of data ports
	 * @throws ServiceLayerException
	 */
	public List<DataPortInterface> viewDataPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;

	/**
	 * Get list of power ports for an item
	 * @param itemId
	 * @return List of power ports
	 * @throws ServiceLayerException
	 */
	public List<PowerPortInterface> viewPowerPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;
	
	/**
	 * Get item ports.
	 * @param itemId
	 * @param freePortOnly
	 * @return
	 * @throws ServiceLayerException
	 */
	public List<PortInterface> viewPortListForItem(Long itemId, boolean freePortOnly) throws ServiceLayerException;
	
	/**
	 * Fetch available positions for the cabinet
	 * @param cabinetId
	 * @param itemRUHeight
	 * @return
	 * @throws ServiceLayerException
	 */
	public Collection<Long> getAvailUPositionsForItem(long itemId, int itemRUHeight, int itemUPosition, int blockSize, String itemMounting,long railsUsed) throws ServiceLayerException;

	/**
	 * Fetch available positions for the cabinet
	 * @param cabinetId
	 * @param modelId
	 * @param railsCode
	 * @param editItemId
	 * @return Collection<Long>
	 * @throws ServiceLayerException
	 */
	Collection<ValueIdDTO> getAvailUPositionsForNRItem(long parenItemId, long modelId, long railsCode, long editItemId)
			throws ServiceLayerException;
	
	/**
	 * Fetch all available item class lookup values as a map of valuecode and value (lkpValue)
	 * @return - A Map of ValueCode and the lkpValue
	 */
	public List<SystemLookupDTO> getAllItemClassLookup();
	
	/**
	 * Fetch all available item status lookup values as a map of valuecode and value (lkpValue)
	 * @return - A Map of ValueCode and the lkpValue
	 */
	public List<SystemLookupDTO> getAllStatusLookup();
	
	/**
	 * Fetch the status lookup values for the Current state.
	 * If itemId = -1 then return status lookups for new state.
	 * @return
	 * @throws DataAccessException
	 * @throws ClassNotFoundException 
	 */
	public List<SystemLookupDTO> getStatusLookupForCurrentState(Long itemId, Long modelId) throws DataAccessException, ClassNotFoundException;
	
	/**
	 * Fetch all available  lookup values as a map of valuecode and value (lkpValue)
	 * @return - A Map of ValueCode and the lkpValue
	 */
	public List<SystemLookupDTO> getSystemLookup(String lkpTypeName);
	
	/**
	 * Given a class lkpValue code, returns the total count of items across all sites.
	 * @param classLkpValueCode
	 * @return
	 */
	public Long getTotalItemCountForClass(Long classLkpValueCode);

	
	/**
	 * Performs a search given the criteria.
	 * @param criteriaDTO - This is the criteria against which items will be returned 
	 * @return A list of ItemSearchResultDTO
	 * @throws A service layer exception that could be a business validation exception.
	 */
	public List<ItemSearchResultDTO> search(ItemSearchCriteriaDTOImpl criteriaDTO) throws ServiceLayerException;
	
	public String getItem(Long itemId, String xPath) throws Throwable;
	
//	public String getItem(Object[] itemId);

	public Map<String, UiComponentDTO> getItem(Object[] itemId) throws Throwable;

	
	public Boolean isUnique( UniqueValidatorDTO uniqueValidatorDTO) throws Throwable;
	
	
	/**
	 * Get all the cabinets given a location id
	 * @param locationId
	 * @return
	 */
	public List<ValueIdDTO> getAllCabinets(Long locationId);
        
    /**
	 * Get all the available cabinet row labels associated to a location.
	 * @param locationId
	 * @return
	 */
	public List<ValueIdDTO> getCabinetRowLabels(Long locationId);        
        
    /**
	 * Get available positions for row labels associated to a location given a location Id
	 * @param cabinetId
	 * @return
	 */
	public List<ValueIdDTO> getCabinetPositionInRows(Long locationId, String rowLabel);        
	
	public List<ValueIdDTO> getCabinetPositionInRows(Long locationId, String rowLabel, Integer availPos);
	
	/**
	 * Get all the locations (rooms)
	 * @return
	 */
	public List<ValueIdDTO> getAllLocations();
	
	/**
	 * Get the available u positions given the following:
	 * Cabinet Id
	 * Height of the item that is getting placed
	 * Any exclusion of UPosition and block size
	 * @param cabinetId
	 * @param railsCode TODO
	 * @return
	 * @throws DataAccessException 
	 */
	
	/**
	 * Get the available u positions given the following:
	 * Cabinet Id
	 * Height of the item that is getting placed
	 * Any exclusion of UPosition and block size
	 * @param cabinetId
	 * @param ruHeight
	 * @param itemId
	 * @param railsCode
	 * @param reservationId
	 * @return List<ValueIdDTO> 
	 * @throws DataAccessException
	 */
	public List<ValueIdDTO> getAvailableUPositions(Long cabinetId,int ruHeight, long itemId, long railsCode, Long reservationId) throws DataAccessException;
	
	/**
	 * Get the item details giving itemId
	 * @param itemId
	 * @return
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO> getItemDetails(Long itemId)
			throws Throwable;
	
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList) throws ClassNotFoundException, BusinessValidationException, Throwable;

	Boolean getIsTagVerified(Long itemId);
	
	/**
	 * Get all project_number associated to a location given a location Id
	 * @param locationId
	 * @return
	 */
	public List<ValueIdDTO> getProjectNumbers(Long locationId);        


	/**
	 * Get all contract_number associated to a location given a location Id
	 * @param locationId
	 * @return
	 */
	public List<ValueIdDTO> getContractNumbers(Long locationId);

	/**
	 * Get all zero U position available to place the zero U item
	 * @param cabinetId
	 * @param depthLksValue
	 * @param sideLksValue
	 * @param itemRUHeight
	 * @param exceptionItemId. Used to get  current position taken by the same item
	 * @return Collection<Long>
	 * @throws ServiceLayerException 
	 */
	public List<ValueIdDTO> getAvailableZeroUPositionsForItem(Long cabinetId,
			long depth_lkp_value, long side_lkp_value, int itemRUHeight, 
			Long exceptionItemId) throws ServiceLayerException;

	/**
	 * Get all slot positions available to place the blade in the chassis
	 * @param chassisId - chassis where the blade is to be placed
	 * @param bladeModelId - the blade model id to be placed
	 * @param faceLksValueCode - the face lks value code for the chassis - FRONT = 20501 REAR = 20502
	 * @param bladeId - the blade being edited or -1 for the new item
	 * @return Map<Long, String> - map of slot# (key) and slot label  
	 * @throws ServiceLayerException 
	 */
	public Map<Long, String> getAvailableChassisSlotPositionsForBlade(Long chassisId,
			long bladeModelId, long faceLksValueCode, long bladeId)
			throws ServiceLayerException;

	/**
	 * List Stack items associated to an item itemId 
	 * @param itemId - item id of the stack item
	 * @return List<StackableItemDTO>
	 * @throws ServiceLayerException
	 */
	public List<StackItemDTO> getAllStackItems(Long itemId) throws ServiceLayerException;
	
	/**
	 * Add the stackable item to the stack group
	 * @param newItemId - the item to add to the stack group
	 * @param siblingItemId - the sibling item
	 * @return List<StackItemDTO>
	 * @throws ServiceLayerException
	 */
	public List<StackItemDTO> addStackItem(long newItemId, long siblingItemId) throws ClassNotFoundException, ServiceLayerException;

	/**
	 * Remove the stackable item from the stack group
	 * @param itemId - item to remove
	 * @param newName - new name for the network item removed from the stack
	 * @return List<StackItemDTO>. 
	 * @throws ServiceLayerException
	 */
	public List<StackItemDTO> removeStackItem(long itemId, String newName) throws  ClassNotFoundException, ServiceLayerException;

	/**
	 * Set stack name to the stack group
	 * @param itemId -The itemId of stack group to be renamed
	 * @param stackName - new stackName to be set for group
	 * @return List<StackItemDTO>. 
	 * @throws ServiceLayerException
	 */
	public List<StackItemDTO> setStackName(Long itemId, String stackName) throws ClassNotFoundException, ServiceLayerException;

	/**
	 * Set new stack number for an item.
	 * @param itemId - Id of an item for which new stack number to be set
	 * @param stackNumber - New stack number to be set for the item
	 * @return List<StackItemDTO>. 
	 * @throws ServiceLayerException
	 */
	public List<StackItemDTO> setStackNumber(Long itemId, Long stackNumber) throws  ClassNotFoundException, ServiceLayerException;
	
	/**
	 * get the list of all blade items for a given chassis or 
	 * get the list of all blades from a chassis that the passed itemId is in
	 * @param itemId - chassis item id OR blade item id
	 * @return List<BladeDTO>
	 * 				BladeDTO will have following data
	 * 					- item id
	 * 					- item name
	 * 					- item class
	 * 					- position (slot #s)
	 * 					- facing
	 * 					- form factor
	 * 					- item status
	 * @throws ServiceLayerException
	 */
	public List<BladeDTO> getAllBladeItem(long itemId) throws ServiceLayerException;

	/**
	 * get all the chassis item in the cabinet's front or rear
	 * @param cabinetItemId - item id of the cabinet
	 * @param bladeModelId - blade model to be placed on this chassis
	 * @param bladeId - blade that is current being edited or -1 for new item
	 * @return List of ValueIdDTO with the chassis item id and chassis name
	 * @throws ServiceLayerException 
	 */
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeModelId, long bladeId) throws ServiceLayerException;
	
	/**
	 * get all the chassis item in the cabinet's front or rear
	 * @param cabinetItemId - item id of the cabinet
	 * @param bladeTypeLkpValueCode - the blade type class lkp value code. Eg: 1200 - Device, 1300 - Network
	 * @return List of ValueIdDTO with the chassis item id and chassis name
	 */
	public List<ValueIdDTO> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode);

	/**
	* get a list of Stackable items that have sibling id occurred only once.
	* it will return all items that can be added to the network stack and is currently not in any network stack
	* @return List<StackableItemDTO>
	* @throws DataAccessException
	*/
	public List<StackItemDTO> getAddableStackItems() throws ServiceLayerException;

	/**
	 * get the chassis dto for a given chassis item
	 * @param chassisItemId - chassis item id
	 * @param faceLksValueCode - chassis face, FRONT = 20501 and REAR = 20502 
	 * @return ChassisItemDTO
	 */
	public ChassisItemDTO getChassisInfo(long chassisItemId, long faceLksValueCode)  throws ServiceLayerException;
	
	/**
	 * Get a list of ChassisSlotsDTO for a given Chassis item.
	 * @param chassisId
	 * @param faceLksValueCode - chassis face, FRONT = 20501 and REAR = 20502
	 * @return List<ChassisSlotDTO>
	 * @throws ServiceLayerException
	 * 
	 * reports business validation error which must be checked by client. 
	 */
	public List<ChassisSlotDTO> getChassisSlotDetails(Long chassisId, Long faceLksValueCode) throws ServiceLayerException;

	/**
	 * get the list of all cabinet that can hold a given blade model (form factor, blade type - N/W, Device). 
	 * Also if trying to edit a blade id that is already been placed in a cabinet, then pass the blade id
	 * @param locationId -site id where the blade is to be placed
	 * @param bladeModelId - the model id of the blade that is to be placed
	 * @param bladeId - if trying to edit the placement for a given blade then pass the exception blade id 
	 * 					that should be included in the cabinet list
	 * @return list of cabinet id and the respective cabinet name
	 * @throws ServiceLayerException 
	 */
	public List<ValueIdDTO> getAllAvailableCabinetForBladeModel(long locationId, long bladeModelId, long bladeId) throws ServiceLayerException;

	/**
	 * get the list of all cabinets that can fit the zero u model in a given location. 
	 * client will also pass the item id the zero u item that is been edited (if edit mode)
	 * @param locationId - location where the zero u item is to be placed
	 * @param zeroUModelId - model id of the zero u item that will be placed
	 * @param depth_lkp_value - the depth in the cabinet
	 * @param side_lkp_value - the side of the cabinet
	 * @param zeroUId - the exceptional zero u item id if the zero u item will be edited
	 * @return List<ValueIdDTO> containing the cabinet item id and cabinet name
	 * @throws ServiceLayerException
	 */
	public List<ValueIdDTO> getAllAvailableCabinetForZeroUModel(long locationId,
			long zeroUModelId, long depth_lkp_value, long side_lkp_value,
			long zeroUId) throws ServiceLayerException;
	
	/**
	 * Delete items.
	 * @param itemIdList list of item IDs
	 * @return List of itemId deleted
	 * @throws ServiceLayerException
	 */
	public List<Long> deleteItems(List<Integer> itemIdList) throws ClassNotFoundException,	BusinessValidationException, Throwable;

	/**
	 * Delete items.
	 * @param itemIdList list of item IDs
	 * @return List of itemId deleted
	 * @throws ServiceLayerException
	 */
	// public List<Long> deleteItems(List<Integer> itemIdList, Boolean skipDeleteAssociatedItems) throws ClassNotFoundException,	BusinessValidationException, Throwable;

	/**
	 * get the first blade model for a given make. First here mean the model that have lowest model_id
	 * @param makeId
	 * @return first blade's BladeDTO for a given make
	 * @throws ServiceLayerException
	 */
	public BladeDTO getFirstBladeModelForMake(long makeId)
			throws ServiceLayerException;

	/**
	 * get the available shelf position for a non-rackable item in a given u position
	 * @param cabinetId - cabinet where the non-rackable item is to be placed
	 * @param uPosition - u position where the non-rackable item is to be placed
	 * @param railsUsed - rails of the cabinet
	 * @param editItemId - the item currently getting edited
	 * @return Collection<Integer> the list of all available shelf position
	 */
	Collection<ValueIdDTO> getAvailableShelfPosition(Long cabinetId,
			int uPosition, long railsUsed, long editItemId);

	/**
	 * informs if the rear of the chassis is defined
	 * @param chassisItemId
	 * @return boolean, true if the chassis rear is defined, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isChassisRearDefined(long chassisItemId)
			 throws ServiceLayerException;

	/**
	 * informs if a given blade model is allowed to be placed in the rear of the chassis 
	 * @param chassisItemId
	 * @param bladeModelId
	 * @param bladeItemId
	 * @return boolean, true if the blade is allowed in chassis rear, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isBladeAllowedinChassisRear(long chassisItemId, long bladeModelId, long bladeItemId)
			throws ServiceLayerException;

	/**
	 * informs if a given blade model is allowed to be placed in the front of the chassis 
	 * @param chassisItemId
	 * @param bladeModelId
	 * @param bladeItemId
	 * @return boolean, true if the blade is allowed in chassis front, false otherwise
	 * @throws ServiceLayerException
	 */
	boolean isBladeAllowedinChassisFront(long chassisItemId, long bladeModelId, long bladeItemId)
			throws ServiceLayerException;

	/**
	*
	*/
	public String getItemActionMenuStatus( List<Long> itemIdList );
	
	public List<RequestDTO> itemRequest( List<Long> itemIds, String typeOfRequest ) throws BusinessValidationException, DataAccessException;

	/**
	 * inform if item is allowed to edit. The current item request stage may not allow item edit
	 * @param itemId
	 * @return true: if item edit allowed, false if item edit is not allowed
	 * @throws ServiceLayerException
	 */
	public Boolean isItemRequestStageAllowEdit(Long itemId)
			throws ServiceLayerException;

	/**
	 * get the first blade model for a given make and class. First here mean the model that have lowest model_id 
	 * @param makeId
	 * @param classLkpValueCode
	 * @return BladeDTO
	 * @throws ServiceLayerException
	 */
	public BladeDTO getFirstBladeModelForMakeAndClass(long makeId,
			long classLkpValueCode) throws ServiceLayerException;
	

	/**
	 * get the item class
	 * @param itemId
	 * @return classLkpValueCode
	 * @throws ServiceLayerException
	 */
	public Long getItemClass(long itemId)  throws ServiceLayerException;

	/**
	 * Clone items.
	 * @param itemList list of item DTO
	 * @return List of item DTO cloned
	 * @throws ServiceLayerException
	 */
	public List<CloneItemDTO> cloneItems(List<CloneItemDTO> itemList) throws BusinessValidationException, Throwable;
	
	/**
	 * Update PDU reading for inlets, outlets and connected sensors that are 
	 * already configured in dcTrack. Add any newly discovered sensors.
	 * @param ids - list of item Id of PDU
	 * @return List<String>: List of error/warning message for display.
	 * @throws BusinessValidationException
	 * @throws RemoteDataAccessException 
	 * @throws DataAccessException 
	 * @throws Exception 
	 */
	public List<String> syncPduReadings(Long[] ids) throws BusinessValidationException, DataAccessException, RemoteDataAccessException, Exception;

	/**
	 * 
	 * @return list of all UPS bank items that have rating higer or equal 
	 * than minAmpsRating
	 * @throws ServiceLayerException
	 */
	public List<UPSBankDTO> getAllUpsBanks( Long minAmpsRating) throws ServiceLayerException;

	/**
	 * Get Breaker with ratings greater or equal to input ampsRating. Note that the
	 * API gets breaker ports only for local and remote Power Panel item subclass.
	 * This API is very generic, it returns port based on not just ampsRating but includes
	 * additional criteria listed below.
	 *   
	 * @param ampsRating : if NULL, return all breakers
	 * @param isUsed : if false, return breakers with no connections
	 * 				   if true, return breakers that have connections
	 *                 if (true && false ) return all breakers with or without connection
	 * @param breakerLkpValueCodes : breaker port subclass lookup value codes 
	 *                               e.g: 20001 - Branch Circuit Breaker
	 * @param phases : breaker phase e.g:  7022 - 3Phase Delta, 7023 - 3Phase Wye
	 * @param breakerPortId : if NOT null, include data for this breaker port with breakerPortId in 
	 * 						  the response. When user is editing an item, the floor pdu configuration for 
	 *                        "power feed from" drop down for breakerport must list the 
	 *                        breaker port that is already assigned to the Floor PDU.
	 * @param fpduItemId : If -1 will return all the breakers. If > 0 then it will return breaker ports
	 * 					   that does not belong to that fpduItem
	 *  
	 * @return list of BreakerPortDTO
	 * @throws ServiceLayerException
	 */
	
	public List<BreakerDTO> getAllBreakers(Long ampsRating, Boolean[] isUsed, Long[] breakerLkpValueCodes,  Long[] phases, Long breakerPortId, Long fpduItemId) throws ServiceLayerException;

	/**
	 * delete the items after confirming the follow up action for the associated items
	 * @param itemIdList
	 * @param deleteAssociatedItems
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public List<Long> deleteItemsExt(List<Integer> itemIdList, Boolean deleteAssociatedItems) throws ClassNotFoundException, BusinessValidationException, Throwable; 

	/**
	 * check if move request is allowed for itemId(s)
	 * @param itemId
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public Boolean isMoveRequestAllowed(List<Integer> itemId) throws BusinessValidationException, DataAccessException;

    /**
     * Get all items in a cabinet.
     * 
     * @param cabinetId
     * @return a list of item data mappings
     * @throws Throwable
     */
    public List<Map<String, Object>> getCabinetItemList(Long cabinetId) throws Throwable;

    /**
     * Save a passive item.
     * 
     * @param itemId
     * @param dtoList
     *          ValueIdDTO list
     * @return item id
     * @throws Throwable
     */
    public Long savePassiveItem(Long itemId, List<ValueIdDTO> dtoList) throws Throwable;

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
     * @throws Throwable
     *TODO: move to appropriate server layer.
     */
    public void setUserRequestBypassSetting(Boolean value) throws Throwable;
    
    /**
     * This API gives information whether PowerIQ integration is enabled 
     * for the given item.
     * @param itemId
     * @return true when PowerIQ integration is enabled otherwise false
     * @throws Throwable
     */
    public Boolean isPIQIntegrationEanabled(Long itemId) throws Throwable;

    /**
     * use this API after user confirms Yes to the warning message
     * @param itemIds
     * @param typeOfRequest
     * @return
     * @throws BusinessValidationException
     * @throws DataAccessException
     */
	public List<RequestDTO> itemRequestConfirmed(List<Long> itemIds,
			String typeOfRequest) throws BusinessValidationException,
			DataAccessException;

    /**
     * Get items for item filter.
     * 
     * @param filterConfig
     * @return
     * @throws DataAccessException
     */
    List<JSONReportFilterResult> getItemForReport(JSONReportFilterConfig filterConfig) throws DataAccessException;
}