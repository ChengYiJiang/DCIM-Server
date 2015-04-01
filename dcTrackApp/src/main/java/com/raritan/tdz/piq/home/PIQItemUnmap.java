package com.raritan.tdz.piq.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * 
 * @author bunty
 *
 */
public interface PIQItemUnmap extends PIQUnmap {

	/**
	 * unmap the item from the given location from PIQ
	 * @param locationCode
	 * @param itemName
	 * @param userInfo TODO
	 * @throws BusinessValidationException 
	 */
	public void unmap(String locationCode, String itemName, UserInfo userInfo, Errors errors) throws BusinessValidationException;
	
}
