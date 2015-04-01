package com.raritan.tdz.lookup.json;


import java.util.Map;

import com.raritan.tdz.exception.BusinessValidationException;

/*
 * JSOM adapter
 * 
 */
public interface LookupAdapter {
	Map<String, Object> getLkuByIdAPI( Long id ) throws BusinessValidationException;
	Map<String, Object> getLkuByValueAPI( String lkuValue ) throws BusinessValidationException;
	Map<String, Object> getLkuByTypeAPI( String lkuValue ) throws BusinessValidationException;
}
