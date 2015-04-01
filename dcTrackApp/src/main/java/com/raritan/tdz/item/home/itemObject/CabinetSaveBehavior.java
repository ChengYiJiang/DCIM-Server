/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketsDAO;

/**
 * @author prasanna
 *
 */
public class CabinetSaveBehavior implements ItemSaveBehavior {
	@Autowired
	ItemFinderDAO itemFinderDAO;

	@Autowired
	ItemDAO itemDAO;

	@Autowired
	private TicketsDAO ticketsDAO;

	@Autowired
	private ItemSaveBehavior itemSaveTicketBehavior;

	@Autowired
	ChassisHome chassisHome;

	private final Logger log = Logger.getLogger(this.getClass());
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preValidateUpdate(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to do at this time.

	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#preSave(com.raritan.tdz.domain.Item, java.lang.Object[])
	 */
	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// Nothing to be done here

	}
	
	private void makeItemNamesUnique(Item parentItem) throws DataAccessException {
		// get children items.
		List<Item> items = itemDAO.getChildrenItems(parentItem);
		for (Item item: items) {
			// check if the name alreay exists at new location 
			String itemName = item.getItemName();
			Boolean itemExists = itemDAO.doesItemWithNameExistsAtLocation (itemName, parentItem.getDataCenterLocation().getDataCenterLocationId().longValue());
			if (itemExists == true) {
				// make item name unique.
				StringBuilder sb = new StringBuilder(itemName);
				sb.append("^^");
				sb.append(item.getItemId());
				item.setItemName(sb.toString());
				itemDAO.saveItem(item);
			}
		}
	}

	//update children location if cabinet in Planned or Storage state and location has changed
	private void updateChildrensLocation(Item item) throws DataAccessException{
		assert( SavedItemData.getCurrentItem() != null );
		Item origItem = SavedItemData.getCurrentItem().getSavedItem();
		if( (item.getStatusLookup().getLkpValueCode().longValue() == SystemLookup.ItemStatus.PLANNED ||
				item.getStatusLookup().getLkpValueCode().longValue() == SystemLookup.ItemStatus.IN_STORAGE )
				&& origItem.getDataCenterLocation() != item.getDataCenterLocation()
				&& item.getDataCenterLocation().getDataCenterLocationId() != null){
			if (item.getDataCenterLocation().getDataCenterLocationId() != origItem.getDataCenterLocation().getDataCenterLocationId()) {
				// when you move item from one location to another,
				// if item name already exists make it unique. 
				makeItemNamesUnique(item);
			}
			itemDAO.propagateParentLocationToChildren(item.getItemId(), item.getDataCenterLocation().getDataCenterLocationId().longValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#postSave(com.raritan.tdz.domain.Item)
	 */
	@Override
	public void postSave(Item item, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException, DataAccessException {
		boolean isUpdate = SavedItemData.getCurrentItem() != null ? true : false;

		if( isUpdate ) updateChildrensLocation(item);

		//If this cabinet is a container (contains free-standing device), we need to replicate the location and the location ref to the containing
		//device
		if (isUpdate && item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CONTAINER)){
			//Get the device item whose parent is this item.
			List<Item> devChildItemList = itemFinderDAO.findChildItemsFromParentIdAndClass(item.getItemId(),SystemLookup.Class.DEVICE);
			List<Item> netChildItemList = itemFinderDAO.findChildItemsFromParentIdAndClass(item.getItemId(),SystemLookup.Class.NETWORK);
			Item deviceItem = null;
			if (devChildItemList.size() == 1){
				deviceItem = (Item)devChildItemList.get(0);
			} else if (netChildItemList.size() == 1){
				deviceItem = (Item)netChildItemList.get(0);
			}

			if(deviceItem != null){
				deviceItem.setLocationReference(item.getLocationReference());
				deviceItem.setDataCenterLocation(item.getDataCenterLocation());
				deviceItem.setStatusLookup(item.getStatusLookup());

				itemDAO.saveItem(deviceItem);

				updateTicketFieldsForDevice(deviceItem, sessionUser, additionalArgs);

			}
		}
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemSaveBehavior#canSupportDomain(java.lang.String[])
	 */
	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		String[] names = domainObjectNames;
		boolean canSupport = false;
		for (String name:names){
			if (name.equals(CabinetItem.class.getName())){
				canSupport = true;
			}
		}

		return canSupport;
	}

	private void updateTicketFieldsForDevice(Item deviceItem, UserInfo sessionUser, Object... additionalArgs) throws BusinessValidationException, DataAccessException {
		Long ticketId = ticketsDAO.getTicketId(deviceItem.getItemId());

		if (null == ticketId) return;

		deviceItem.setTicketId(ticketId);

		itemSaveTicketBehavior.postSave(deviceItem, sessionUser, additionalArgs);

	}

}
