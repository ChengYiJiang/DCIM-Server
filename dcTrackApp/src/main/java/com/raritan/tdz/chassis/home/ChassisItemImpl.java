package com.raritan.tdz.chassis.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.chassis.home.ChassisHomeImpl.SlotReAssignmentInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItItemObject;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Chassis Item implementation.
 * @author Andrew Cohen
 */
public class ChassisItemImpl extends ItItemObject implements ChassisItem {
	
	ChassisHome chassisHome;

	public ChassisItemImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public ChassisHome getChassisHome() {
		return chassisHome;
	}

	public void setChassisHome(ChassisHome chassisHome) {
		this.chassisHome = chassisHome;
	}

	/*
	 * Return all items contained in the chassis.
	 * @see com.raritan.tdz.item.home.CompositeItem#getChildItems()
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<Item> getChildItems() throws DataAccessException {
		List<Item> items = null;
		
		Session session = sessionFactory.getCurrentSession();
		try {
			Criteria c = session.createCriteria(ItItem.class);
			c.createAlias("bladeChassis", "bladeChassis");
			c.add( Restrictions.eq("bladeChassis.itemId", item.getItemId() ));
			c.add( Restrictions.ne("itemId", item.getItemId() ));
			c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			items = c.list();
		}
		catch(Throwable t){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
		
		if (items == null) {
			items = Collections.emptyList();
		}
		
		return items;
	}
	
	@Override
	public int getChildItemCount() throws DataAccessException {
		// TODO: optimize using count query
		return getChildItems().size();
	}
	
	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.SubClass.CHASSIS );
		codes.add( SystemLookup.SubClass.BLADE_CHASSIS );
		return Collections.unmodifiableSet( codes );
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ItItem.class);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		super.validate(target, errors);
		ItItem itItem = (ItItem) item;
		if (itItem.getSkipValidation() == null || !itItem.getSkipValidation()){
			try {
				SlotReAssignmentInfo slotInfo = chassisHome.validateChassisModelChange(item.getItemId(), item.getModel().getModelDetailId());
				if (slotInfo.slotEventDetailsList.size() > 0) {
					// report that some blades are falling out
					String bladeList = new String("\n");
					for (ChassisHomeImpl.SlotEventDetails evt: slotInfo.slotEventDetailsList) {
						// bladeList.concat(evt.bladeItemName + "\n\t");
						bladeList += (evt.bladeItemName + "\n");
					}
					Object[] errorArgs = { bladeList };
					errors.rejectValue("cmbModel", "ItemValidator.invalidDefinitionModelChassis", errorArgs, "The following blades will not fit with the change in model");
				}
			} catch (Throwable t) {
				
			}
		}
		//We need to validate if the chassis slots for the model is assigned.
	}
	
	@Override
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException {
		// final boolean isUpdate = (itemId != null &&  itemId > 0);
		// Item origItem = isUpdate ? SavedItemData.getCurrentItem().getSavedItem() : null;
		ItItem itItem = (ItItem) itemDomain;
/*		boolean updateGroupName = (null == origItem) || 
				(null != origItem && origItem.getGroupingName().equals(itItem.getGroupingName()));*/
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		try {
			if (null != itItem) {
				// Chassis model could have changed, make sure to update the layout information
				chassisHome.updateSlotNumberForBladesInChassis(itItem.getItemId());
				chassisHome.updateChassisGroupName(itItem.getItemId());
				chassisHome.updateCabinetAndLocationForBladesInChassis(itItem.getItemId());
			}
		}
		catch (Throwable t) {
			
		}
	}

	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		long itemId = item.getItemId();
		
		Session session = this.sessionFactory.getCurrentSession();

		//Update items that have this chassis as the parent
		Query q = session.createSQLQuery("update dct_items_it set chassis_id = null where chassis_id = :itemId");
		q.setLong("itemId", itemId);
		int deleted = q.executeUpdate();
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated " + deleted + " child items");
		}
		
		return super.deleteItem();
	}
}
