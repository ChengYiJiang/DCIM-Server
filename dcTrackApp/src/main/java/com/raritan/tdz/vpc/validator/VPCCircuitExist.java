package com.raritan.tdz.vpc.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.location.dao.LocationDAO;

public class VPCCircuitExist implements Validator {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private LocationDAO locationDAO;
	
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> targetMap = (Map<String, Object>) target;
		Long locationId = (Long) targetMap.get(DataCenterLocaleDetails.class.getName());
		
		boolean circuitExist = itemDAO.isVpcInUse(locationId);

		if (circuitExist) {
			String siteCode = locationDAO.getLocationCode(locationId);
			Object[] errorArgs = {siteCode};
			errors.reject("VPC.circuitExistInLocation", errorArgs, "Circuit(s) are created using VPC items in location " + siteCode + ". Cannot disable VPC at location " + siteCode + ".");
		}
		
	}

}
