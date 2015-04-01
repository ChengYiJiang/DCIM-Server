package com.raritan.tdz.piq.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * 
 * @author bunty
 *
 */
public interface PIQLocationUnmap extends PIQUnmap {

	/**
	 * unmap the given location and all its items from PIQ
	 * @param locationId
	 * @param userInfo TODO
	 * @param itemName
	 * @throws BusinessValidationException 
	 */
	public void unmap(Long locationId, UserInfo userInfo, Errors errors) throws BusinessValidationException;

}
