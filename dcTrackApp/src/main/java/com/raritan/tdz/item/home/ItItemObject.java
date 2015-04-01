/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class ItItemObject extends ItemObjectBase {

	ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public ItItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemObject#getSubclassLookupValueCodes()
	 */
	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.SubClass.RACKABLE );
		codes.add( SystemLookup.SubClass.BLADE_CHASSIS );
		codes.add( SystemLookup.SubClass.BLADE );
		codes.add( SystemLookup.SubClass.BLADE_SERVER );
		codes.add(SystemLookup.SubClass.CHASSIS);
		codes.add(SystemLookup.SubClass.NETWORK_STACK);
		//codes.add(SystemLookup.SubClass.STORAGE); //CR 48367:No such subclass exist in lks. So commenting out for now.
		codes.add(SystemLookup.SubClass.VIRTUAL_MACHINE);
		codes.add(SystemLookup.SubClass.BLANKING_PLATE);
		//TODO: Since we do not have a way to handle the class lookup values
		//TODO: in the base, we are returning the DATA_PANEL as the subclass
		//TODO: We need to fix this in future
		codes.add( SystemLookup.Class.DATA_PANEL);
		codes.add(SystemLookup.Class.PASSIVE);
		codes.add(SystemLookup.Class.DEVICE);
		codes.add(SystemLookup.Class.NETWORK);
		codes.add(SystemLookup.Class.PROBE);
		return Collections.unmodifiableSet( codes );
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ItItem.class);
	}
	
	@Override
	public void validate(Object target, Errors errors){
		super.validate(target, errors);
		
		//We need to validate if we can update EAssetTag
		validateEAssetTag(target, errors);
	}

	
	private void validateEAssetTag(Object target, Errors errors) {
		Item item = (Item)target;
		
		if (item.getItemId() > 0){
			
			Session session = sessionFactory.getCurrentSession();
			
			Criteria c = session.createCriteria(Item.class);
			c.setProjection(Projections.property("raritanAssetTag"));
			c.add(Restrictions.eq("itemId", item.getItemId()));

			String raritanAssetTag = (String) c.uniqueResult();
			
			if (raritanAssetTag != null && itemHome.isItemTagVerified(item.getItemId()) && !raritanAssetTag.equals(item.getRaritanAssetTag())){
				Object[] errorArgs = { item.getRaritanAssetTag(), raritanAssetTag };
				errors.rejectValue("tieAssetTag", "ItemValidator.eAssetTagLocked", errorArgs, "Electronic AssetTag cannot be changed.");
			}
		}
		
	}

	@Override
	public boolean deleteItem() throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		Session session = this.sessionFactory.getCurrentSession();
				
		Query q = session.createSQLQuery("delete from dct_items_it where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " IT Item");
		}

		return super.deleteItem();
	}	
}
