package com.raritan.tdz.item.itemState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ItemStateITDevice implements ItemState {

	private ItemState itemStateCommon;
	

	public ItemState getItemStateCommon() {
		return itemStateCommon;
	}

	public void setItemStateCommon(ItemState itemStateCommon) {
		this.itemStateCommon = itemStateCommon;
	}

	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
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
		if (item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)
				&& item.getStatusLookup() != null && item.getStatusLookup().getLkpValueCode().equals(SystemLookup.ItemStatus.STORAGE)){
			return false; //You cannot transition a VM to storage.
		}
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
}
