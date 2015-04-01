package com.raritan.tdz.item.home;

import java.util.Map;

import org.springframework.validation.Errors;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public interface ItemDataResolver {
	
	public void resolve(Map<String, Object> data, Errors errors) throws BusinessValidationException, DataAccessException;

}
