package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

public class ItemObjectValidatorsFactoryImpl implements
		ItemObjectValidatorsFactory {
	
	Map<Long, Validator> deleteValidatorsMap;
	Map<Long, Validator> archiveValidatorsMap;
	
	public Map<Long, Validator> getDeleteValidatorsMap() {
		return deleteValidatorsMap;
	}

	public void setDeleteValidatorsMap(Map<Long, Validator> deleteValidatorsMap) {
		this.deleteValidatorsMap = deleteValidatorsMap;
	}

	public Map<Long, Validator> getArchiveValidatorsMap() {
		return archiveValidatorsMap;
	}

	public void setArchiveValidatorsMap(Map<Long, Validator> archiveValidatorsMap) {
		this.archiveValidatorsMap = archiveValidatorsMap;
	}

	@Override
	public Validator getArchiveValidators(Item item) {
		if (item == null || archiveValidatorsMap == null) return null;
		return archiveValidatorsMap.get(item.getClassMountingFormFactorValue());
	}
	
	@Override
	public Validator getStorageValidators(Item item) {
		if (item == null || archiveValidatorsMap == null) return null;
		return archiveValidatorsMap.get(item.getClassMountingFormFactorValue());
	}

	@Override
	public Validator getDeleteValidators(Item item) {
		if (item == null || deleteValidatorsMap == null) return null;
		return deleteValidatorsMap.get(item.getClassMountingFormFactorValue());
	}

}
