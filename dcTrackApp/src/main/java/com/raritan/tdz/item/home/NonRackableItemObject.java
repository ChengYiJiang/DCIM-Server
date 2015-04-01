/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Bunty Nasta
 *
 */
public class NonRackableItemObject extends ItItemObject {

	public NonRackableItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemObject#getSubclassLookupValueCodes()
	 */
	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		// Note: refer to itemObjectFactory bean definition in homes.xml 
		//       for mapping.
		Set<Long> codes = new HashSet<Long>(1);
		
		codes.add(SystemLookup.Class.DEVICE);
		codes.add( SystemLookup.SubClass.RACKABLE); // Change RACKABLE TO STANDARD
		codes.add(SystemLookup.Class.NETWORK);
		codes.add(SystemLookup.Class.PROBE);
		codes.add( SystemLookup.Class.DATA_PANEL);
		codes.add(SystemLookup.Class.RACK_PDU);
		codes.add(SystemLookup.SubClass.NETWORK_STACK);

		return Collections.unmodifiableSet( codes );
	}
	
	@Override
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Long> availablePositions = null;
		// Do not validate U position if the parent (Cabinet) is not provided
		if (null == item.getParentItem()) {
			return;
		}
		Item origItem = (null != SavedItemData.getCurrentItem()) ? SavedItemData.getCurrentItem().getSavedItem() : null;
		try {
			if (null != item.getParentItem() && null != item.getModel() && null != item.getMountedRailLookup()) {
				availablePositions = itemHome.getNonRackableUPosition(item.getParentItem().getItemId(), 
					item.getModel().getRuHeight(), item.getMountedRailLookup().getLkpValueCode(), (null != origItem) ? origItem.getItemId() : -1);
			}
			else {
				availablePositions = new ArrayList<Long>();
			}
			// Add positions that always available
			availablePositions.add( -1L ); // Above Cabinet
			availablePositions.add( -2L ); // Below Cabinet
			availablePositions.add( -9L ); // No U-position selected
			if (null != availablePositions) {
				if (!availablePositions.contains(item.getuPosition())) {
					Object[] errorArgs = {item.getuPosition(), item.getParentItem() != null ? item.getParentItem().getItemName() : "<Unknown>" };
					errors.rejectValue("cmbCabinet", "ItemValidator.noAvailableUPosition", errorArgs, "The UPosition is not available");
				}
			}
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void validateShelfPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Integer> availablePositions = null;

		// Do not validate Shelf position if the parent (Cabinet) is not provided OR if the U position is not selected
		if (null == item.getParentItem() || item.getuPosition() == -9) {
			return;
		}
		try {
			if (null != item.getParentItem() && null != item.getModel() && null != item.getMountedRailLookup()) {
				availablePositions = itemHome.getAvailableShelfPosition(item.getParentItem().getItemId(), item.getuPosition(), item.getMountedRailLookup().getLkpValueCode(), item.getItemId());
			}
			else {
				availablePositions = new ArrayList<Integer>();
			}
			if (null != availablePositions) {
				int itemShelfPos = item.getShelfPosition();
				if (!availablePositions.contains(itemShelfPos) || itemShelfPos <= 0) {
					if (itemShelfPos <= 0 && item.getuPosition() != -9L) {
						Object[] errorArgs = {itemShelfPos, item.getParentItem() != null ? item.getParentItem().getItemName() : "", item.getuPosition() };
						errors.rejectValue("cmbOrder", "ItemValidator.noAvailableShelfPosition", errorArgs, "The Shelf Position is not available");
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void validateRailsUsed(Object target, Errors errors) {
		Item item = (Item)target;

		// Do not validate Rails if the parent (Cabinet) is not provided
		if (null == item.getParentItem()) {
			return;
		}
		if (item.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.FRONT && 
				item.getMountedRailLookup().getLkpValueCode() != SystemLookup.RailsUsed.REAR) {
			Object[] errorArgs = {item.getMountedRailLookup().getLkpValue(), item.getClassLookup().getLkpValue() };
			errors.rejectValue("cabinet", "ItemValidator.incorrectRailsUsed", errorArgs, "The selected Rails Used is not allowed");
		}
	}

	public void validateExtra(Object target, Errors errors) {
		/* Validate the Non-Rackable item
		1. Validate if the shelf position is valid
		2. Validate U position 
		3. Validate Rails Used */
		validateRailsUsed(target, errors);
		validateShelfPosition(target, errors);
	}
	
	@Override
	public void validate(Object target, Errors errors){
		super.validate(target, errors);
		validateExtra(target, errors);
	}
	
	@Override
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException {
			Item origItem = isUpdate ? SavedItemData.getCurrentItem().getSavedItem() : null;
			Item item = (Item) itemDomain;
			if (null != item) {
				long cabinetId = (null != item.getParentItem()) ? item.getParentItem().getItemId() : -1;
				long uPosition = item.getuPosition();
				long railsLkpValueCode = item.getMountedRailLookup().getLkpValueCode();
				itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, itemDomain, null);
			}
			if (isUpdate && null != origItem && null != item && 
					null != origItem.getParentItem() && null != item.getParentItem() && 
					null != origItem.getMountedRailLookup() && null != item.getMountedRailLookup() && 
					(origItem.getParentItem().getItemId() != item.getParentItem().getItemId() || 
					origItem.getuPosition() != item.getuPosition() || 
					origItem.getMountedRailLookup().getLkpValueCode() != item.getMountedRailLookup().getLkpValueCode())) {
				long cabinetId = (null != origItem.getParentItem()) ? origItem.getParentItem().getItemId() : -1;
				long uPosition = origItem.getuPosition();
				long railsLkpValueCode = origItem.getMountedRailLookup().getLkpValueCode();
				itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, itemDomain, null);
			}
			super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
	}
	
	@Override
	public boolean deleteItem() throws ClassNotFoundException, BusinessValidationException,	Throwable {
		/* update the shelf position of all the items @ this cabinet and u-position */
		long cabinetId = (null != item.getParentItem()) ? item.getParentItem().getItemId() : -1;
		long uPosition = item.getuPosition();
		long railsLkpValueCode = item.getMountedRailLookup().getLkpValueCode();
		boolean deleted = super.deleteItem();
		if (deleted) {
			itemHome.updateShelfPosition(cabinetId, uPosition, railsLkpValueCode, null, null);
		}
		return deleted;
	}	

}