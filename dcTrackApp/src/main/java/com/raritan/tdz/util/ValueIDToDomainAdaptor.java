/**
 * 
 */
package com.raritan.tdz.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * @author prasanna
 * This is a generic interface for converting a list of valueIDDTO to its corresponding domain object
 */
public interface ValueIDToDomainAdaptor {
	
	/**
	 * Convert from a list of valueIDDTO to its corresponding Domain object
	 * <p> The returned value is the actual domain object.
	 * @param dbObject The database id so that we can load an existing record. Will be null in case of a new record.
	 * @param valueIdDTOList
	 * @return
	 * @throws BusinessValidationException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 * @throws ClassNotFoundException 
	 * @throws DataAccessException 
	 */
	public Object convert(Object dbObject, List<ValueIdDTO> valueIdDTOList, String unit)
			throws BusinessValidationException, IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException, ClassNotFoundException, DataAccessException;
	
	/**
	 * This will get any validation errors occured during convert.
	 * @return
	 */
	public MapBindingResult getValidationErrors();
}
