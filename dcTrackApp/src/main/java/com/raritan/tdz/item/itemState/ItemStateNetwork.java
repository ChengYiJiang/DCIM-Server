package com.raritan.tdz.item.itemState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

public class ItemStateNetwork implements ItemState {
	
	@Autowired(required=true)
	private ItemDAO itemDAO;

	private ItemState itemStateCommon;
	

	public ItemState getItemStateCommon() {
		return itemStateCommon;
	}

	public void setItemStateCommon(ItemState itemStateCommon) {
		this.itemStateCommon = itemStateCommon;
	}

	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		adjustSibling(item);
		itemStateCommon.onSave(item);
	}


	@Override
	public boolean supports(Class<?> clazz) {
		return itemStateCommon.supports(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		itemStateCommon.validate(target, errors);
		
	}

	@Override
	public RulesNodeEditability getEditability(Item item) {
		return itemStateCommon.getEditability(item);
	}

	@Override
	public boolean canTransition(Item item) {
		return itemStateCommon.canTransition(item);
	}

	@Override
	public List<Long> getAllowableStates() {
		return itemStateCommon.getAllowableStates();
	}

	@Override
	public boolean isTransitionPermittedForUser(Item item, Long newState,
			UserInfo userInfo) {
		return itemStateCommon.isTransitionPermittedForUser(item, newState, userInfo);
	}

	@Override
	public void validateMandatoryFields(Item item, Errors errors,
			Long newStatusLkpValueCode) throws DataAccessException,
			ClassNotFoundException {
		itemStateCommon.validateMandatoryFields(item, errors, newStatusLkpValueCode);
		
	}

	@Override
	public void validateParentChildConstraint(Item item, Errors errors,
			Long newStatusLkpValueCode, String errorCodePrefix)
			throws DataAccessException, ClassNotFoundException {
		itemStateCommon.validateParentChildConstraint(item, errors, newStatusLkpValueCode, errorCodePrefix);
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo userSession,
			Errors errors) {
		itemStateCommon.validateAllButReqFields(target, userSession, errors);
	}
	
	private void adjustSibling(Item item) {
		Long classMountingFormFactor = item.getClassMountingFormFactorValue();
		Long statusLookupValueCode = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValueCode() : null;
		//If it is a stackable network item and the status is either storage or archived.
		if ((classMountingFormFactor.equals(SystemLookup.ModelUniqueValue.NetworkStackFreeStanding)
				|| classMountingFormFactor.equals(SystemLookup.ModelUniqueValue.NetworkStackNonRackable)
				|| classMountingFormFactor.equals(SystemLookup.ModelUniqueValue.NetworkStackRackable))
				&& statusLookupValueCode != null
				&& (statusLookupValueCode.equals(SystemLookup.ItemStatus.STORAGE)
						|| statusLookupValueCode.equals(SystemLookup.ItemStatus.ARCHIVED))){
			
			//Perform the adjustment of sibling for stackable
			if (isMaster(item)){
				switchSiblingItemId(item);
			}
			
			//Make the item become master.
			item.setCracNwGrpItem(item);
		}
	}

	private boolean isMaster(Item item) {
		return item.getCracNwGrpItem() != null && item.getCracNwGrpItem().getItemId() == item.getItemId();
	}
	
	//Private methods
	private void switchSiblingItemId(Item item){
		//Set the sibling_item_id	
		//This function return the sibling stacks link to this item
		//List of order by num_ports, item_id
		List<Item> stackList = (List<Item>) itemDAO.getNetworkStackItems(item.getItemId());
		
		if(stackList.size() > 1){
			Item sibling = stackList.get(1);
			
			if((item.getItemId() == sibling.getCracNwGrpItem().getItemId())){
				itemDAO.switchSiblingItemId(item.getItemId(), sibling.getItemId());
				itemDAO.setUPositon(sibling.getItemId(), item.getUPosition());
				if (item.getFacingLookup() != null)
					itemDAO.setOrientation(sibling.getItemId(), item.getFacingLookup().getLksId());
			}
		}
	}

}
