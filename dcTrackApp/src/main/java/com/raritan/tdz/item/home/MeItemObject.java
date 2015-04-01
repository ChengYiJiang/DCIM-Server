/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 *
 */
public class MeItemObject extends ItemObjectBase {

	ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public MeItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(MeItem.class);
	}
	
	@Override
	public void validate(Object target, Errors errors){
		super.validate(target, errors);
		
		validateNumberInGroup(target, errors);
	}

	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		Session session = this.sessionFactory.getCurrentSession();
		
		Query q = session.createSQLQuery("delete from dct_items_me where item_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Deleted " + deleted + " ME Item");
		}

		return super.deleteItem();
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.SubClass.LOCAL );
		codes.add( SystemLookup.SubClass.BUSWAY );
		codes.add( SystemLookup.SubClass.REMOTE );
		codes.add(SystemLookup.SubClass.BUSWAY_OUTLET);
		codes.add(SystemLookup.SubClass.WHIP_OUTLET);
		codes.add(SystemLookup.SubClass.WIRE_MANAGER);
//==== CR 48367:No such subclass exist in lks. So commenting out for now.
//		codes.add(SystemLookup.SubClass.CEILING_WALL_MOUNT);
//		codes.add(SystemLookup.SubClass.CRAC_GROUP);
//		codes.add(SystemLookup.SubClass.DELTA_CONVERSION_ONLINE);
//		codes.add(SystemLookup.SubClass.DOUBLE_CONVERSION_ONLINE);
//		codes.add(SystemLookup.SubClass.DRY_TYPE);
//		codes.add(SystemLookup.SubClass.DUAL_PDU);
//		codes.add(SystemLookup.SubClass.FERRORESONANT);
//		codes.add(SystemLookup.SubClass.FLOOR_MOUNT);
//		codes.add(SystemLookup.SubClass.GENERATOR);
//		codes.add(SystemLookup.SubClass.IGBT);
//		codes.add(SystemLookup.SubClass.LINE_INTERACTIVE_UPS);
//		codes.add(SystemLookup.SubClass.MODULAR);
//		codes.add(SystemLookup.SubClass.NON_MODULAR);
//		codes.add(SystemLookup.SubClass.OIL_FILLED);
//		codes.add(SystemLookup.SubClass.PORTABLE);
//		codes.add(SystemLookup.SubClass.PVC_FLOOR_PERFORATED_TILE);
//		codes.add(SystemLookup.SubClass.SINGLE);
//		codes.add(SystemLookup.SubClass.STANDBY_UPS);
//		codes.add(SystemLookup.SubClass.STEEL_GRATE);
//		codes.add(SystemLookup.SubClass.TRUE_ONLINE_UPS);
//===CR 48367:No such subclass exist in lks. So commenting out for now.
		codes.add(SystemLookup.Class.RACK_PDU);
		
		codes.add(SystemLookup.Class.CRAC);
		codes.add(SystemLookup.Class.CRAC_GROUP);
		codes.add(SystemLookup.Class.FLOOR_OUTLET);
		codes.add(SystemLookup.Class.FLOOR_PDU);
		codes.add(SystemLookup.Class.PERFORATED_TILES);
		codes.add(SystemLookup.Class.RACK_PDU);
		codes.add(SystemLookup.Class.UPS);
		codes.add(SystemLookup.Class.UPS_BANK);		

		return Collections.unmodifiableSet( codes );
	}
	
	//
	// Private methods
	//
	
	/*
	 * Validate the number in group is in the range 0-9999. (US1409)
	 * 
	 * NOTE: Unfortunately we cannot use bean annotation validation for the number range
	 * (using the @Max annotation) because the "num_ports" field is overloaded in the database.
	 */
	private void validateNumberInGroup(Object target, Errors errors) {
		if (target == null || errors == null) return;
		final Item item = (Item)target;
		
		// Only applies to Rack PDU?
		if (item.getClassLookup() == null || item.getClassLookup().getLkpValueCode() != SystemLookup.Class.RACK_PDU) 
			return;
		
		final int numInGroup = item.getNumPorts();
		
		if (numInGroup < 0 || numInGroup > 9999) {
			Object[] errorArgs = { };
			errors.rejectValue("numPorts", "ItemValidator.invalidNumberInGroup", errorArgs, "The Number in Group field must be in the range 0-9999");
		}
	}
}
