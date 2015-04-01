package com.raritan.tdz.dctimport.integration.transformers;


import org.springframework.validation.Errors;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * common interface to get the parameters for a given transformer
 * @author bunty
 *
 */
public interface ImportBeanToParameter {

	/**
	 * converts the DCT Import data to the parameter list expected by the called interface
	 * @param beanObj
	 * @param errors
	 * @return
	 * @throws DataAccessException
	 * @throws Exception 
	 */
	Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception, ServiceLayerException;
	
}
