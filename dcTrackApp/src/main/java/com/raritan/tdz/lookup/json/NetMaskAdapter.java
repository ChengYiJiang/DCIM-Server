package com.raritan.tdz.lookup.json;

import java.util.Map;

import com.raritan.tdz.exception.BusinessValidationException;

public interface NetMaskAdapter {
	Map<String, Object> getNetMaskByIdAPI( Long id ) throws BusinessValidationException;
	Map<String, Object> getNetMaskByMaskAPI( String mask ) throws BusinessValidationException;
	Map<String, Object> getNetMaskByCidrAPI( Long cidr ) throws BusinessValidationException;
	Map<String, Object> getAllNetMasks() throws BusinessValidationException;
}
