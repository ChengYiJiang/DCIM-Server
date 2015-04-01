/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class PCToArchivedValidator implements
		ParentChildConstraintValidator {
	
	SessionFactory sessionFactory;
	
	public PCToArchivedValidator(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.ParentChildConstraintValidator#validateParentChildConstraint(com.raritan.tdz.domain.Item, java.lang.Long, org.springframework.validation.Errors)
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void validateParentChildConstraint(Item item,
			Long newStatusLkpValueCode, Errors errors, String errorCodePrefix)
			throws DataAccessException {
		String itemName = item != null ? item.getItemName():"<Unknown>";
		List<Item> children = null;		
		
		
		if (item != null){
			Criteria chassisItemCriteria = getChassisItemCriteria(item);
			
			if (chassisItemCriteria != null){

				children = chassisItemCriteria.list();
				
				if (children != null && children.size() > 0){ //This is a chassis
					validate(item, errors, errorCodePrefix, itemName, children);
				} else {
					//Check if this is a parent item 
					Criteria parentItemCriteria = getParentItemCriteria(item);

					children = parentItemCriteria.list();
					
					validate(item, errors, errorCodePrefix, itemName, children);
				}
				
			}
			else {
				//Check if this is a parent item 
				Criteria parentItemCriteria = getParentItemCriteria(item);
				
				if (null == parentItemCriteria) {
					return;
				}
				
				children = parentItemCriteria.list();
				
				if (null != children) {
					validate(item, errors, errorCodePrefix, itemName, children);
				}
			}
		}

	}

	private void validate(Item item, Errors errors, String errorCodePrefix,
			String itemName, List<Item> children) {
		if (children.size() > 0){
			String parentClassSubClass = null;
			String childClassSubClass = null;
			parentClassSubClass = getClassSubClass(item);
			
			//If so, make sure that all the children are in "ARCHIVED" 
			boolean notInArchive = false;
			
			for (Item child:children){
				if (child.getStatusLookup() == null){
					childClassSubClass = getClassSubClass(child);
					
					notInArchive = true;
					break;
				} else	if (!child.getStatusLookup().getLkpValueCode().equals(SystemLookup.ItemStatus.ARCHIVED)){
					childClassSubClass = getClassSubClass(child);
					notInArchive = true;
					break;
				}
			}
			
			//If not error
			if (notInArchive == true){
				Object errorArgs[] = { itemName, childClassSubClass, parentClassSubClass };
				errors.reject(errorCodePrefix + ".decomissionToArchived", errorArgs, "Cannot put this item to archived");
			}
			
		}
	}

	private Criteria getParentItemCriteria(Item item) {
		
		// for floor pdu, all panels and panel's outlets will be archived with the floor pdu
		// for the container class (free-standing cabinet), the device/network item will be moved to archived with the cabinet 
		if ((null != item.getClassLookup() && item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.FLOOR_PDU) ||
				(item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.CONTAINER)) {
			return null;
		}
		
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		
		// for cabinet item, all passive items will be archived with the cabinet
		criteria.createAlias("classLookup", "classLookup");
		criteria.createAlias("parentItem", "parentItem");
		criteria.add(Restrictions.eq("parentItem.itemId", item.getItemId()));
		criteria.add(Restrictions.ne("classLookup.lkpValueCode", SystemLookup.Class.PASSIVE)); 
		return criteria;
	}
	
	private Criteria getChassisItemCriteria(Item item) {
		if (item instanceof ItItem){
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Item.class);
			
			criteria.createAlias("bladeChassis", "bladeChassis");
			criteria.add(Restrictions.eq("bladeChassis.itemId", item.getItemId()));
			return criteria;
		}
		
		return null;
	}

	private String getClassSubClass(Item item) {
	boolean isChassisOrBlade = false;
		
		
		if (item.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.DeviceBladeChassisRackable)
				|| item.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.NetworkChassisRackable)
				|| item.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.DeviceBladeServer)
				|| item.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.NetworkBlade)){
			isChassisOrBlade = true;
		}
		
		//A cabinet can have differnt kinds of items, so we return Item as the string
		if (!isChassisOrBlade && item.getParentItem() != null 
				&& item.getParentItem().getClassLookup() != null 
				&& item.getParentItem().getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)){
			return "Item";
		}
		
		String classSubClass;
		if (item.getSubclassLookup() != null){
			classSubClass = item.getSubclassLookup().getLkpValue();
		} else if (item.getClassLookup() != null){
			classSubClass = item.getClassLookup().getLkpValue();
		} else {
			classSubClass = "<Unknown>";
		}
		return classSubClass;
	}
}
