/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.AvailablePorts;

/**
 * @author prasanna
 *
 */
public class CabinetItemObject extends ItemObjectBase {

	ItemHome itemHome;
	private CabinetHome cabinetHome;
	
	public CabinetHome getCabinetHome() {
		return cabinetHome;
	}

	public void setCabinetHome(CabinetHome cabinetHome) {
		this.cabinetHome = cabinetHome;
	}

	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public CabinetItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ItItem.class);
	}
	
	@Override
	public void validate(Object target, Errors errors){
		super.validate(target, errors);
		//ItemId <= 0 means this a new cabinet 
		if (((Item)target).getItemId() <= 0){
			if (!itemHome.isLicenseAvailable(1)){
				Integer licenses = itemHome.getLicenseCount();
				Object[] errorArgs = {licenses};
				errors.reject("ItemValidator.licenseExceeded", errorArgs, "You are exceeding the current license limit");
			}
		}
		
		if (!validateCabinetPlacementInfo((CabinetItem)target)) {
			errors.reject("ItemValidator.noAvailableRowPosition", null, "The Cabinet placement information is not valid");
		}
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.CABINET);
		codes.add( SystemLookup.SubClass.CONTAINER);
		return Collections.unmodifiableSet( codes );
	}
	
	@Override
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException {
		Item item = (Item) itemDomain;
		
		//If this cabinet is a container (contains free-standing device), we need to replicate the location and the location ref to the containing
		//device
		if (isUpdate && item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.CONTAINER)){
			Session session = sessionFactory.getCurrentSession();
			
			Criteria c = session.createCriteria(Item.class);
			c.createAlias("parentItem", "parent");
			c.add(Restrictions.eq("parent.itemId", itemId));
			
			Item deviceItem = (Item) c.uniqueResult();
			
			if(deviceItem != null){
				deviceItem.setLocationReference(item.getLocationReference());
				deviceItem.setDataCenterLocation(item.getDataCenterLocation());
				deviceItem.setStatusLookup(item.getStatusLookup());
				
				session.merge(deviceItem);
				
				session.flush();
			}			
		}
		
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
	}
	
	//
	// Private methods
	//
	
	private synchronized boolean validateCabinetPlacementInfo(CabinetItem cabinet) {
		final String rowLabel = cabinet.getRowLabel();
		final Integer rowPos = cabinet.getPositionInRow();
		
		// No row label specified for cabinet, okay to place with just location
		if (!StringUtils.hasText(rowLabel)) return true;
		
		// Get available positions
		Collection<Long> rowPositions;
		
		if (cabinet.getItemId() > 0) {
			SavedItemData savedData = SavedItemData.getCurrentItem();
			CabinetItem origCabItem = (CabinetItem)savedData.getSavedItem();
			
			if (isRowLabelChanged(origCabItem, cabinet)) {
				// If row label changed, get available row positions for the new row label
				rowPositions = getCabinetPositionsInRow(cabinet, rowLabel, rowPos != null ? rowPos.longValue() : null);
			}
			else {
				if (!isRowPositionChanged(origCabItem, cabinet)) return true;
				// If row label is saved, get the saved available positions
				rowPositions = savedData.getAvailablePositions();
			}
		}
		else {
			rowPositions = getCabinetPositionsInRow(cabinet, rowLabel, null);
		}
		
		// No occupied row positions for specified row label - cannot place this cabinet!
		if (rowPositions.isEmpty()) return true;
		
		// Specified row number is not available
		if (rowPos > 0 && rowPositions.contains( Long.valueOf(rowPos) )) return false;
		
		return true;
	}
	
	private boolean isRowLabelChanged(CabinetItem origItem, CabinetItem item) {
		final String origLabel = origItem.getRowLabel() != null ? origItem.getRowLabel() : "";
		final String curLabel = item.getRowLabel() != null ? item.getRowLabel() : "";
		return !origLabel.equals( curLabel );
	}
	
	private boolean isRowPositionChanged(CabinetItem origItem, CabinetItem item) {
		return (origItem.getPositionInRow() != item.getPositionInRow()); // No change
	}
	
	private List<Long> getCabinetPositionsInRow(CabinetItem cabinet, String rowLabel, Long exceptionIndex) {
		if(cabinet == null || cabinet.getDataCenterLocation() == null) return new ArrayList<Long>();
		
		List<Integer> tmp = cabinetHome.getCabinetPositionInRows(cabinet.getDataCenterLocation().getDataCenterLocationId(), rowLabel);
		List<Long> rowPositions = new ArrayList<Long>( tmp.size() );
		
		for (Integer i : tmp) {
			rowPositions.add( i.longValue() );
		}
		
		if (exceptionIndex != null && exceptionIndex > 0) {
			rowPositions.remove( exceptionIndex );
		}
		
		return rowPositions;
	}

	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		
		Session session = this.sessionFactory.getCurrentSession();

		//Update items that have this cabinet as the parent
		Query q = session.createSQLQuery("update dct_items set parent_item_id = null where parent_item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " child items");
		}
		
		q = session.createSQLQuery("delete from dct_items_cabinet where item_id = :itemId");
		q.setLong("itemId", itemId);
		deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " Cabinet Item");
		}

		return super.deleteItem();
	}
	
}
