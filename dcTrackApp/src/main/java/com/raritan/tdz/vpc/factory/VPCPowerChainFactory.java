/**
 * 
 */
package com.raritan.tdz.vpc.factory;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * @author prasanna
 *
 */
public interface VPCPowerChainFactory {

	/**
	 * Creates the VPC power chain and persist it. 
	 * Any errors will be filled in the errors object
	 * @param locationId
	 * @param errors
	 * @throws BusinessValidationException 
	 */
	public void create(Long locationId, Errors errors) throws BusinessValidationException;

	/**
	 * delete all vpc items, all its ports and connections
	 * @param locationId
	 * @param userInfo
	 * @param errors
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public void delete(Long locationId, UserInfo userInfo, Errors errors)
			throws ClassNotFoundException, BusinessValidationException,
			Throwable;
}
