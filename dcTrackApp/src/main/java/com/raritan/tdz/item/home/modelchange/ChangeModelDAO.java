/**
 * 
 */
package com.raritan.tdz.item.home.modelchange;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 * This will be a DAO class to perform the actual change model
 * The individual ChangeModel implementations use the methods here
 * to perform the actual business logic task
 */
public interface ChangeModelDAO {
	
	/**
	 * This will be used by updateUPosition method to determine to keep the uPosition option or Clear UPosition
	 * @author prasanna
	 *
	 */
	public enum UPositionEnum{ KEEP_UPOSITION, CLEAR_UPOSITION };
	
	/**
	 * This method covers the following actions:
	 * Clear UPosition if the UPositionEnum is CLEAR_UPOSITION
	 * Keep UPosition if it still fits when UPositionEnum is KEEP_UPOSITION
	 * @param itemInDB
	 * @param itemToSave
	 * @param uPositionEnum
	 * @throws BusinessValidationException
	 */
	public void updateUPosition(Item itemInDB, Item itemToSave, UPositionEnum uPositionEnum) throws BusinessValidationException;
	
	/**
	 * This method covers the action of deleting power ports
	 * if connected prevent that subclass change (i.e. do not save item)
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void deletePowerPorts(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method covers the action of changing data ports subclass to virtual
	 * If connected prevent that subclass change (i.e. do not save item) and throw an exception
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void changeDPSubclassToVirtual(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method covers the action of changing data ports subclass to active
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void changeDPSubclassToActive(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method covers the deleting of DataStore
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void deleteDataStore(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method covers the delete of data store from VM Item
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void deleteDataStoreFromVMItem(Item itemToSave ) throws BusinessValidationException;
	
	/**
	 * This method covers the delete of chassis reference from blade items
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void deleteChassisRefFromBladeItems(Item itemToSave ) throws BusinessValidationException;
	
	/**
	 * This method clears the chassis id
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearChassisId(Item itemToSave ) throws BusinessValidationException;
	
	/**
	 * This method creates a phantom cabinet
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void createPhantomCabinet(Item itemToSave ) throws BusinessValidationException;
	
	/**
	 * This method delete the phantom cabinet. 
	 * @param itemInDB
	 * @throws BusinessValidationException
	 */
	public void deletePhantomCabinet(Item itemInDB) throws BusinessValidationException;
	
	/**
	 * This method clear sibling id
	 * @param itemInDB
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearSiblingId(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method clear device config fields
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearDeviceConfigurationFields(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * This method clears the slot position
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearSlotPosition(Item itemToSave ) throws BusinessValidationException;
	
	/**
	 * This method clear the cabinet id
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearCabinetId(Item itemToSave ) throws BusinessValidationException;

	/**
	 * This method clear vm cluster field
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearVmClusterId(Item itemToSave) throws BusinessValidationException;

	/**
	 * This method clear model id field
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */	
	void clearModelId(Item itemToSave) throws BusinessValidationException;
	
	/**
	 * Check to see if item has connected ports
	 * @param itemId
	 * @throws BusinessValidationException
	 */	
	void validatePortUsage(Long itemId) throws BusinessValidationException;	
	
	/**
	 * This method clear shelf position field
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void clearShelfPosition(Item itemToSave, Item itemDB) throws BusinessValidationException;

	/**
	 * update the shelf position when item is changed from rackable to non-rackable
	 * @param itemToSave
	 * @throws BusinessValidationException
	 */
	public void updateShelfPosition(Item itemToSave) throws BusinessValidationException;

}
