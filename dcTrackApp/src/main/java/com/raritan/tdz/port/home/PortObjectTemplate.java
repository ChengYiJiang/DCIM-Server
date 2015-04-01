package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * port processor
 * @author bunty
 *
 */
public interface PortObjectTemplate {

	/**
	 * save (add or edit) the port using the port dto in the given item 
	 * @param itemId
	 * @param portId 
	 * @param portDto
	 * @param userInfo
	 * @param errors 
	 * @return
	 * @throws DataAccessException 
	 * @throws BusinessInformationException 
	 */
	public PortInterface save(Long itemId, Long portId, PortInterface portDto, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException, BusinessInformationException;
	
	/**
	 * save (add or edit) the port using the port dto in the given item and applies the common attributes to all the ports of the same type 
	 * @param portDto
	 * @param userInfo
	 * @param errors 
	 * @return
	 * @throws DataAccessException 
	 * @throws BusinessInformationException 
	 */
	public IPortInfo saveApplyCommonAttribute(PortInterface portDto, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException, BusinessInformationException;
	
	/**
	 * delete the port in the given item
	 * @param portDetails 
	 * @param userInfo
	 * @param errors 
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 * @throws BusinessInformationException 
	 */
	public void delete(PowerPortDTO portDetails, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException, BusinessInformationException;

	/**
	 * delete the port 
	 * @param itemId
	 * @param portId
	 * @param userInfo
	 * @param skipValidation
	 * @param errors
	 * @throws BusinessValidationException
	 * @throws BusinessInformationException
	 * @throws DataAccessException 
	 */
	public void delete(Long itemId, Long portId, UserInfo userInfo, Boolean skipValidation, Errors errors) throws BusinessValidationException, BusinessInformationException, DataAccessException;
	
}
