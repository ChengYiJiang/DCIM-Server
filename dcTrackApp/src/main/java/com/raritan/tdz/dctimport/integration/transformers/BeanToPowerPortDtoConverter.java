package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.validation.Errors;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author bunty
 *
 */
public interface BeanToPowerPortDtoConverter {

	/**
	 * converts the power port import data to the power port dto
	 * @param beanObj
	 * @param errors TODO
	 * @return
	 * @throws DataAccessException 
	 */
	public PowerPortDTO convertBeanToPowerPortDTO(DCTImport beanObj, Errors errors) throws DataAccessException;
	
}
