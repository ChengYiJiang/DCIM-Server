package com.raritan.tdz.piq.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;

/**
 * 
 * @author bunty
 *
 */
public class PIQLocationUnmapImpl implements PIQLocationUnmap {

	@Autowired
	private LocationDAO locationDAO;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	private List<Validator> validators;
	
	
	
	public PIQLocationUnmapImpl(List<Validator> validators) {
		super();

		this.validators = validators;
	}

	@Override
	public void unmap(Long locationId, UserInfo userInfo, Errors errors) throws BusinessValidationException {

		
		// check permission
		validate(locationId, userInfo, errors);

		// check errors
		businessExceptionHelper.throwBusinessValidationException(null, errors, null);
		
		// unmap location and all items in it 
		locationDAO.unmapLocationWithPIQ(locationId);
		
	}
	
	/**
	 * run through the list of validators
	 * @param locationId
	 * @param userInfo
	 * @param errors
	 */
	private void validate(Long locationId, UserInfo userInfo, Errors errors) {
		
		if (null == validators) return;
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		targetMap.put(UserInfo.class.getName(), userInfo);
		targetMap.put(Long.class.getName(), locationId);
		
		for (Validator validator: validators) {
			
			validator.validate(targetMap, errors);
			
		}
		
	}
	

}
