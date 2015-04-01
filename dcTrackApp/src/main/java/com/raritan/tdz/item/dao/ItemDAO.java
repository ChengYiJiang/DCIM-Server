package com.raritan.tdz.item.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.item.dto.UPSBankDTO;

/**
 * @author Santo Rosario
 * This will be a DAO class for Item object
 */
	
public interface ItemDAO extends Dao<Item> {
	/**
	 * Load/Read an existing item from database using lazying loading and a new hibernate session
	 * @param id - Id of item to be loaded
	 * @return Item - could be ItItem, MeItem, CabinetItem
	 */	
	public Item loadItem(Long id); 

	/**
	 * Save item to database
	 * @param item - item to be save
	 * @return ItemId of saved item
	 */	
	public Long saveItem(Item item); 
	
	/**
	 * Load/Read an existing item from database using lazying loading and a new hibernate session
	 * @param Class Type - Item.class, ItItem.class, MeItem.Class or CabinetItem.Class
	 * @param id - Id of item to be loaded
	 * @param readOnly - load item in read only mode
	 * @return Item, could be ItItem, MeItem, CabinetItem
	 */		
	public Object loadItem(Class<?> domainType, Long id, boolean readOnly);

	/**
	 * Get list of item that match the stackItem sibling_item_id 
	 * @param stackItem - Stack Item for 
	 * @return List of ItItem
	 */		
	public List<ItItem> getSiblingItems(Item stackItem) throws DataAccessException;

	/**
	 * Get list of item where the parent_item_id match parentItem 
	 * @param parentItem - Item for which to find children 
	 * @return List of Item (ItItem, MeItem, CabinetItem)
	 */		
	public List<Item> getChildrenItems(Item parentItem) throws DataAccessException;

	/**
	 * Get list of item where the chassis_id match chassis item 
	 * @param chassis - Item for which to find blades 
	 * @return List of ItItem (blade items)
	 */			
	public List<ItItem> getChassisItems(ItItem chassis) throws DataAccessException;

	/**
	 * Clone an existing item.  
	 * @param  cloningCriteria - object that indicates how the cloning should be done and for which item
	 * @param  currentUserName - User name under which items are going to be created. User that is login and doing cloning.
	 * @return ItemId of first item created by cloning.
	 */				
	public Long cloneItem(CloneItemDTO cloningCriteria, String currentUserName);

	/**
	 * Load/Read an existing item from database using current hibernate session
	 * @param id - Id of item to be loaded
	 * @return Item, could be ItItem, MeItem, CabinetItem
	 */		
	public Item getItem(Long id);	
	

	/**
	 * Get list of item where the mounting type is NON-RACKABLE
	 * @param cabinetId - cabinet id
	 * @param uPosition - position in cabinet where items are placed
	 * @param railsLkpValueCode - Rail position used by items
	 * @return List of ItItem (blade items)
	 */		
	public List<Item> getNonRackableItems (long cabinetId, long uPosition, long railsLkpValueCode);
	

	/**
	 * Check item tag verified field status
	 * @param itemId - Id of item to check
	 * @return True if item tag was verified
	 */	
	public Boolean isItemTagVerified(Long itemId);


	/**
	 * Get all stack items where the sibling_ite_id equal the primary item id. This method use projection for speed.
	 * This method is similar to getSiblingItems()
	 * @param primaryItemId - Id of primary stack item,  sibling_item_id
	 * @return List of stack items
	 */	
	public List<?> getAllStackItems(Long primaryItemId);


	/**
	 * Get all stack item IDs in database. 
	 * @param none
	 * @return List of stack item Ids
	 */	
	public List<Object> getStackablesItemIds();


	/**
	 * Get a list of stackable items that have sibling id occurred only once. 
	 * @param none
	 * @return List of stack items
	 */		
	public List<Object[]> getAddableStackItems();


	/**
	 * Get last stack item from a network stack. 
	 * @param primaryItemId - Id of primary stack item,  sibling_item_id
	 * @return stack item
	 */		
	public Object getLastStackItemNumber(long primaryItemId);


	/**
	 * Similar to loadItem, but load an item that exactly match the specs of a network stack 
	 * @param itemId - Id of item to load
	 * @return network stack item
	 */		
	public Item getNetworkStackItem(long itemId);


	/**
	 * Get list of items that exactly match the specs of a network stack item 
	 * @param primaryItemId - Id of first/primary item of stack, sibling_item_id
	 * @return network stack item
	 */		
	public List<Item> getNetworkStackItems(Long primaryItemId);


	/**
	 * Get item name for an item 
	 * @param itemId - Id of item
	 * @return name of item
	 */	
	public String getItemName(long itemId);


	/**
	 * Get list of item name where the status of item does not allow them to be deleted 
	 * @param itemIdList - List of item Id to be deleted
	 * @return List name of item
	 */	
	public List<String> getItemsToDeleteInvalidStages(List<Long> itemIdList);


	/**
	 * Get list of item Id that are the children, stacks, or blades of an item to be deleted 
	 * @param itemId - item Id to be deleted
	 * @return List of item Ids associated to item to be deleted
	 */		
	public List<Long> getItemIdsToDelete(long itemId);


	/**
	 * Check that a list of item to be deleted is in the new item status.
	 * @param itemIds - List of item to be deleted
	 * @return List of item names that cannot be deleted since status is not new/planned
	 */	
	public List<String> getItemsToDeleteNotNew(List<Long> itemIds);


	/**
	 * Check that a list of item to be deleted are not connected (have circuits, connections).
	 * @param itemIds - List of item to be deleted
	 * @return List of item names that cannot be deleted since they are connected
	 */	
	public List<String> getItemToDeleteConnected(List<Long> itemIds);


	/**
	 * Get the class value code for an item
	 * @param itemId - item Id
	 * @return numeric value code for class, dct_lks_data.lkp_value_code
	 */	
	public Long getItemClass(long itemId);

	/**
	 * Get the class value (class name) for an item
	 * @param itemId - item Id
	 * @return numeric value code for class, dct_lks_data.lkp_value_code
	 */	
	public String getItemClassName(long itemId);

	/**
	 * Get list of blades for a given chassis item id
	 * @param chassisItemId - item id of chassis
	 * @return List of blade items
	 */	
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId);

	/**
	 * Get list of blades for a given chassis item id place on front or back
	 * @param chassisItemId - item Id of chassis
	 * @param faceLksValueCode - facing (front, back) where blades are placed
	 * @return List of blade items
	 */	
	public Collection<ItItem> getAllBladesForChassis(long chassisItemId, Long faceLksValueCode);

	/**
	 * Get list of all chassis placed in a cabinet for a given type of blade
	 * @param cabinetItemId - item id of cabinet
	 * @param bladeTypeLkpValueCode - type of blades (device/network subclasses)
	 * @return List of device or network blade items
	 */	
	public Collection<ItItem> getAllChassisInCabinet(long cabinetItemId, long bladeTypeLkpValueCode);

	/**
	 * Get list of all item that have an specific mounting
	 * @param mounting - model mounting type
	 * @return List of items
	 */	
	public Collection<Item> getItemsWithMounting(String mounting);

	/**
	 * Get list of all chassis in a location for a given type of blade
	 * @param locationId - location id
	 * @param bladeTypeLkpValueCode - type of blades (device/network subclasses)
	 * @return List of device or network blade items
	 */	
	public List<Object[]> getAllChassis(long locationId, long bladeTypeLkpValueCode);


	/**
	 * Get list of model ids for chassis where chassis have blades with a given model
	 * @param bladeModelId - model id associated with blade item
	 * @return List of model ids associated with chassis
	 */	
	public List<Long> getAllChassisModelIdsForBladeModel(long bladeModelId); //old name getAllChassisModelForBladeModel


	/**
	 * Get list of item ids for chassis where chassis have blades with a given model
	 * @param bladeModelId - model id associated with blade item
	 * @return List of item ids of chassis
	 */	
	public List<Long> getAllChassisItemIdsForBladeModel(long bladeModelId);  //old name getAllChassisItemIdForBladeModelId


	/**
	 * Update the chassis_id field to null for blade items associated with this chassis
	 * @param chassis - chassis item that has blades
	 * @return none
	 */	
	public void removeBladesFromChassis(Item chassis);

	
	/**
	 * Update the parent_item_id field to null for items associated with this parent item
	 * @param parentItem - item that has children
	 * @return none
	 */	
	public void removeChildrenFromItem(Item parentItem);


	/**
	 * Delete dct_items_cabinet record from DB
	 * @param item - cabinet item record
	 * @return none
	 */	
	public void deleteCabinetRecord(Item item);


	/**
	 * Delete dct_items_me record from DB
	 * @param item - me item record
	 * @return none
	 */	
	public void deleteMeRecord(Item item);


	/**
	 * Delete dct_items_it record from DB
	 * @param item - it item record
	 * @return none
	 */	
	public void deleteItRecord(Item item);


	/**
	 * Get location code for an item
	 * @param itemId - item Id
	 * @return location code 
	 */	
	public String getLocationCode(Long itemId);


	/**
	 * Update the sibling_item_id field
	 * @param oldSiblingId - current value for sibling_item_id in DB
	 * @param newSiblingId - new value for sibling_item_id
	 * @return none
	 */	
	public void updateSiblingItemId(long oldSiblingId, long newSiblingId);


	/**
	 * Return all cabinets
	 * @return
	 */
	public List<Item> getAllCabinets(String siteCode);

	public List<Long> getAssociatedItemIds(long itemId);

	/**
	 * get all floor pdu item id
	 * @return
	 */	public List<Long> getAllFloorPDUItemIds();

	/**
	 * get all floor pdu having ups bank
	 * @return
	 */
	public Map<Long, Long> getAllFloorPDUWithUPSBank();

	/**
	 * get all power panels
	 * @return
	 */
	public List<Long> getAllPowerPanelItemIds();

	/**
	 * get the item object with power port and power connections loaded
	 * @param id
	 * @return
	 */
	Item getItemWithPortConnections(Long id);

	/**
	 * Initialize power ports and connections proxies for lazy loading
	 * @param item
	 */
	public void initPowerPortsAndConnectionsProxy(Item item);

	/**
	 * gets the list of panels in the floor pdu that are connected
	 * @param itemId
	 * @return
	 */
	List<String> getFPDUItemToDeleteConnected(Long itemId);

	/**
	 * get the list of power outlets that are associated with the power panel
	 * @param panelItemId
	 * @return
	 */
	List<Long> getPowerPanelConnectedPowerOutlet(List<Long> panelItemIds);

	/**
	 * get list of branch circuit breaker of the panel connected
	 * @param itemIds
	 * @return
	 */
	List<String> getPowerPanelItemToDeleteConnected(Long itemId);
	
	/**
	 * Initialize Location's Proxy for lazy loading
	 * @param item
	 */
	public void initLocationProxy( Item item );

	/**
	 * get all the panels connected to outlets and/or power supply (CRAC)
	 * @param itemId
	 * @return
	 */
	public List<String> getPanelsConnectedToCircuitedOutlets(Long itemId);
	
	/**
	 * return all UPS Bank items that match specified rating
	 * @param minAmpsRating
	 * @return
	 */
	public List<UPSBankDTO> getAllUPSBanksMatchingRating(Long minAmpsRating);

	/**
	 * Search all items using the search string. Search is done according to the following rules:
	 *   - search by item name - partially matching the string
	 *   - search by raritan asset tag - exactly match the string
	 *   - search by serial number exactly matching the string
	 *   - search by asset number exactly matching the string
	 * @param searchString
	 * @return
	 */
	public List<Item> searchItemsBySearchString(String searchString);

	
	/**
	 * Search all items using the search string with location filter Search is done according to the following rules:
	 *   - search by item name - partially matching the string
	 *   - search by raritan asset tag - exactly match the string
	 *   - search by serial number exactly matching the string
	 *   - search by asset number exactly matching the string
	 * @param searchString
	 * @return
	 */
	public List<Item> searchItemsBySearchStringWithLocation(String searchString, String locationString, int limit, int offset);
	/**
	 * Find all children of the cabinet and sort them looking by facing and uPosition
	 * First sort by facing and then by uPosition.
	 * Return also blades that are placed inside a that belongs to the cabinet
	 * Return also blades that are orphans, i.e. not assigned to any chassis yet
	 * 
	 * @param cabinetId
	 * @return
	 */
	public List<Item> getCabinetChildrenSorted(long cabinetId );
	
	
	/**
	 * Find all children of the cabinet, but exclude blades that are already placed in 
	 * a chassis.
	 * Return also blades that are orphans, i.e. not assigned to any chassis yet
	 * 
	 * @param cabinetId
	 * @return
	 */
	public List<Item> getCabinetChildrenWithoutBladesSorted(long cabinetId );
	
	/**
	 * clear the panel placement
	 * @param panelItems
	 */
	void clearPanelPlacement(List<Long> panelItems);

	/**
	 * clear the power outlet's placement
	 * @param powerOutletItems
	 */
	void clearPowerOutletPlacement(List<Long> powerOutletItems);

	/**
	 * set the item state
	 * @param items
	 * @param itemState
	 */
	void setItemState(List<Long> items, Long itemState);

	/**
	 * get the child item ids that will be deleted, archived or stored with the given item
	 * @param itemId
	 * @return
	 */
	List<Long> getChildItemIds(long itemId);

	/**
	 * get the panels item ids that have parent id not set but is connected to FPDU via the port
	 * @param itemId
	 * @return
	 */
	List<Long> getPanelItemIdsToDelete(long itemId);

	/**
	 * clear all the connection to and from the panel
	 * @param panelItems
	 */
	void clearPanelConnections(List<Long> panelItems);

	void initPowerPortsAndSourceConnectionsProxy(Item item);

	/**
	 * set the status of item's ports
	 * @param items
	 * @param itemState
	 */
	void setPortState(List<Long> items, Long itemState);

	/**
	 * set the upsBankId for panels that belongs to floorPdu with fpduId
	 * @param fpduId
	 * @param upsBankId
	 * @return
	 */
	public int setPanelUPSBankId (Long fpduId, Long upsBankId );

	/**
	 * get the count of number of children that are delta phase
	 * @param parentItemId
	 * @return
	 */
	Long getNumOfChildWithDeltaPhase(Long parentItemId);

	/**
	 * get number of children for a given item
	 * @param parentItem
	 * @return
	 */
	Long getNumOfChildren(Item parentItem);

	/**
	 * clear all associations of the panel with the power outlet
	 * @param powerOutletItems
	 */
	void clearPowerOutletAssociationWithPanel(List<Long> powerOutletItems);

	/**
	 * get the list of power outlets that are connected to the busway panels in the panel list provided
	 * @param panelItemIds
	 * @return
	 */
	List<Long> getBuswayPowerPanelConnectedPowerOutlet(List<Long> panelItemIds);

	/**
	 * get the list of power outlets that are connected to the local panels in the panel list provided
	 * @param panelItemIds
	 * @return
	 */
	List<Long> getLocalPowerPanelConnectedPowerOutlet(List<Long> panelItemIds);
	
	/**
	 * get the list of power outlets that are connected to the remote panels in the panel list provided
	 * @param panelItemIds
	 * @return
	 */
	List<Long> getRemotePowerPanelConnectedPowerOutlet(List<Long> panelItemIds);

	/**
	 * get the list of panels to be deleted along with the item ids passed
	 * @param itemIds
	 * @return
	 */
	List<Long> getPanelItemIdsToDelete(List<Long> itemIds);

	/**
	 * get all the child and blades item associated with the passed item ids
	 * @param itemIds
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	List getItemIdsToDelete(List itemIds);
	
	/**
	 * get layout for an item.
	 * @param itemId
	 * @return ItemLayout
	 */
	public Map<String, Object> getItemLayout (long itemId);
	
	/**
	 * get all cabinets Id and name from site as in siteCode
	 * @param siteCode
	 * @return
	 */
	
	public List<Map<String, Object>> getAllCabinetsIdNameForSiteCode(String siteCode);
	
	/**
	 * Switches the sibling id of a network stackable item
	 * @param oldSiblingId
	 * @param newSiblingId
	 */
	public void switchSiblingItemId(long oldSiblingId, long newSiblingId);
	
	/**
	 * Updates the UPosition of an item given its item id and new uPosition
	 * @param itemId
	 * @param uPosition
	 */
	public void setUPositon(long itemId, long uPosition);
	
	/**
	 * Updates the Orientation of an item given its id.
	 * @param itemId
	 * @param uPosition
	 */
	public void setOrientation(long itemId, long facingLksId);

	/**
	 * Get the free standing item id associated with an item
	 * @param itemId
	 * @return itemId
	 */
	public Long getFreeStandingItemIdForItem(Long itemId);

	/**
	 * get the list of passive items in the cabinet
	 * @param itemId
	 * @return
	 */
	public List<Long> getPassiveChildItemIds(Long itemId);

	/**
	 * get the status of all children items 
	 * @param parentItem
	 * @return
	 * @throws DataAccessException
	 */
	public List<Long> getChildrenItemsStatus(Item parentItem) 
			throws DataAccessException;

	/**
	 * get status for the list of items
	 * @param itemIds
	 * @return
	 */
	public Map<Long, Long> getItemsStatus(List<Long> itemIds);
	
	/**
	 * get the ip address of the powerIQ which is controlling this item.
	 * @param item
	 * @return
	 */
	public String getItemsPiqHost(long itemId);

	/**
	 * get list of circuits Id for this item
	 * @param item Id
	 * @return list of CircuitCriteriaDTO
	 */
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForItem(long itemId);
	
    /**
     * Save a passive item.
     * 
     * @param item
     *          Item domain object
     * @return item id
     */
    public long savePassiveItem(Item item);

    /**
     * Delete a passive item.
     * 
     * @param itemId
     */
    public void deletePassiveItem(Long itemId);
    
    /**
     * get PowerIQ integration status for a given item
     * @param item
     * @return true when integration is enabled otherwise false
     */
    public boolean getItemsPIQIntegrationStatus(long itemId);

    /**
     * get children item of a given item class
     * @param parent item
     * @paran class value code
     * @return List of items 
     */
	public List<Item> getChildrenItemsOfClass(Item parentItem, Long classValueCode)	throws DataAccessException;

	/**
	 * get the value of the field for a given item in a map of alias to value object
	 * @param itemId
	 * @param dbToAliasField
	 * @return
	 */
	public Map<String, Object> getFieldsValue(Long itemId, Map<String, String> dbToAliasField);

	/**
	 * get list of IP Address Ids for the item
	 * @param itemId
	 * @return
	 */
	public List<Long> getIpAddressId(Long itemId);

	/**
	 * get the circuit count for an item
	 * @param itemId
	 * @return
	 */
	public int getAssociatedCircuitsCountForItem(long itemId);

	/**
	 * get item ids of all children
	 * @param parentItem
	 * @return
	 * @throws DataAccessException
	 */
	public List<Long> getChildrenItemIds(Item parentItem) throws DataAccessException;

	public void propagateParentLocationToChildren(long parentItemId, long locationId);

	/**
	 * get list of circuits dto for list of items
	 * @param itemIdList
	 * @return
	 */
	public List<CircuitCriteriaDTO> getAssociatedCircuitsForItems(List<Long> itemIdList);

	/**
	 * get the number of circuits for an item that are not in planned state
	 * or is in the planned state but have a pending request
	 * @param itemId
	 * @return
	 */
	public int getNumOfAssociatedNonPlannedNonRequestCircuitsForItem(long itemId);

	/**
	 * get list of circuit Ids for an item that are not in planned state
	 * or is in the planned state but have a pending request
	 * @param itemId
	 * @return
	 */
	public List<Long> getAssociatedNonPlannedNonRequestCircuitsForItem(long itemId);
	
	/**
	 * get list of VPC item ids in a given location
	 * @param locationId
	 * @return
	 */
	public List<Long> getVPCItems(Long locationId);

	/**
	 * get list of VPC item ids in a given location and power chain path
	 * @param locationId
	 * @param powerChainLabel
	 * @return
	 */
	public List<Long> getVPCItemsButUpsBank(Long locationId, String powerChainLabel);

	/**
	 * get only vpc ups bank item in a given location and power path
	 * @param locationId
	 * @param powerChainLabel
	 * @return
	 */
	public List<Item> getVPCUpsBank(Long locationId, String powerChainLabel);

	/**
	 * delete vpc items
	 * @param locationId
	 * @param powerChainLabel
	 */
	public void deleteVPCItems(Long locationId, String powerChainLabel);

	/**
	 * informs if vpc items in a given location has a circuit
	 * @param locationId
	 * @return
	 */
	public boolean isVpcInUse(Long locationId);

	/**
	 * get the vpc item at a given location and chain whose port has the same volt and phase as the provided source port
	 * @param locationId
	 * @param powerChainLabel
	 * @param itemClass
	 * @param srcPort
	 * @return
	 */
	public Item getVPCItem(Long locationId, String powerChainLabel, Long itemClass,
			PowerPort srcPort);

	/**
	 * informs if the given itemId is a power outlet VPC item
	 * @param itemId
	 * @param locationId
	 * @return
	 */
	public boolean isVpcPowerOutlet(Long itemId, Long locationId);

	/**
	 * get the VPC outlet items 
	 * @return
	 */
	public List<Item> vpcPowerOutlets();

	/**
	 * get non-logical circuits of the blade 
	 * @param itemId
	 * @return
	 */
	public List<CircuitCriteriaDTO> getBladeNonLogicalCircuits(long itemId);

	/**
	 * get all logical circuits that has no pending requests
	 * @param itemId
	 * @return
	 */
	public List<CircuitCriteriaDTO> getBladeNonRequestLogicalCircuits(long itemId);

	/**
	 * get non approved logical circuits request ids for an item
	 * @param itemId
	 * @return
	 */
	public List<Long> getBladeNonApprovedLogicalCircuitsRequest(Long itemId);

	/**
	 * set the status of all passive item to be the same as parent item
	 * @param parentItemId
	 * @param locationId
	 */
	public void propagateParentStatusToPassiveChildren(long parentItemId, long locationId);

	/**
	 * get the subclass value code of the item
	 */
	public Long getItemSubClass(long itemId);

	/**
	 * get number of non-planned circuits for an item
	 * @param itemId
	 * @return
	 */
	public int getNumOfAssociatedNonPlannedForItem(long itemId);

	/**
	 * get effective budgeted watts (excludes redundant port's budgeted watts)
	 * @param itemId
	 * @return
	 */
	public Integer getEffectiveBudgetedWattsForAnItem(long itemId);
	
	/**
	 * Delete the reservations associated with a cabient.
	 * @param cabinetItemId
	 */
	public void deleteReservations(long cabinetItemId);

	/**
	 * delete ip address and teaming associated with the item
	 * @param itemId
	 */
	public void deleteItemIPAddressAndTeaming(long itemId);

	/**
	 * get the item id using name and location code
	 * @param itemName
	 * @param locationCode
	 * @return
	 */
	public Long getItemId(String itemName, String locationCode);
	
	/**
	 * get Id of an item using item name and location
	 * @param location
	 * @param itemName
	 * @return itemId 
	 */
	public Long getItemByLocationAndName (String location, String itemName);

	/**
	 * unmaps all the given items with PIQ. It clears the piq_id and piq_external_key fields
	 * @param associatedItemIds
	 */
	public void unmapItemWithPIQ(List<Long> associatedItemIds);

	/**
	 * Get the count of number of non-planned items of the site. 
	 * 
	 * @param locationId
	 * @return
	 */
	public Long getNumOfNonPlannedItems (Long locationId);

	/**
	 * if the item getting removed is the primary item in the sibling, update all other items
	 * that are refenced to this item. Update the sibling_item_id and grouping_name
	 * @param primaryItemId
	 * @return
	 */
	public int removePrimaryStackItem(Long primaryItemId);
	
	/**
	 * Check if there are items with name exists at the specified location
	 * @param itemName
	 * @param locationId
	 * @return
	 */
	public boolean doesItemWithNameExistsAtLocation( String itemName, Long locationId);

	/**
	 * get item by location code and item name
	 * @param itemName
	 * @param locationCode
	 * @return
	 */
	public Item getItem(String itemName, String locationCode);
	
	/**
	 * Uniquely identify the item based on the parameters provided and return the item Id. 
	 * @param locationCode
	 * @param itemName
	 * @param cabinetName
	 * @param uPosition
	 * @return itemId
	 */
	public Long getUniqueItemId(String locationCode, String itemName, String cabinetName, String uPosition, String mountedRails);
	
	/**
	 * Check if a item with name exists above or below the cabinet
	 * @param itemName
	 * @param parentItemId
	 * @param uPosition
	 * @return true if present else false
	 */
	public boolean doesItemWithNameExistsAboveOrBelowCabinet(String itemName, Long parentItemId, Long uPosition);

	/**
	 * Check cabinet of the item with itemId is same ad cabinetId
	 * @param itemId
	 * @param cabinetId
	 * @return true if changed else false
	 */
	boolean isCabinetChanged(Long itemId, Long cabinetId);

	/**
	 * Check location of item in database with itemId is save as currentLocationId
	 * @param itemId
	 * @param currentLocationId
	 * @return true if changed else false
	 */
	boolean isLocationChanged(Long itemId, Long currentLocationId);
	
	/**
	 * Check if an item with itemId is passive item.
	 * @param itemId
	 * @return true if Passive item else false.
	 */
	public boolean isPassiveItem(Long itemId);
}
