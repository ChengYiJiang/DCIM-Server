package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;

public class ItemObjectSaveValidatorsFactoryImpl implements
		ItemObjectSaveValidatorsFactory {

	Map<Long, Validator> saveValidatorsMap;

	public Map<Long, Validator> getSaveValidatorsMap() {
		return saveValidatorsMap;
	}

	public void setSaveValidatorsMap(Map<Long, Validator> saveValidatorsMap) {
		this.saveValidatorsMap = saveValidatorsMap;
	}

	@Override
	public Validator getValidators(Item item) {
		if (item == null) return null;
		
		return saveValidatorsMap.get(item.getClassMountingFormFactorValue());
	}

}
