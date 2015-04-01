package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.validation.Errors;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author KC
 *
 */
public interface BeanToDataPortDtoConverter {

	/**
	 * converts the power port import data to the power port dto
	 * @param beanObj
	 * @return
	 * @throws DataAccessException 
	 */

	DataPortDTO convertBeanToDataPortDTO(DCTImport beanObj, Errors errors)
			throws DataAccessException;
	
}
