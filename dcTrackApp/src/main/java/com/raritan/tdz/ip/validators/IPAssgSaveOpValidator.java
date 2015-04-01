package com.raritan.tdz.ip.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon.ValidationParams;
import com.raritan.tdz.util.RequiredFieldsValidator;

public class IPAssgSaveOpValidator implements Validator{

	@Autowired(required=true)
	IPAddressValidatorCommon ipAddressValidatorCommon;

	@Override
	public boolean supports(Class<?> clazz) {		
		return IPAddressDetails.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Map<ValidationParams, Object> paramsMap = (Map<ValidationParams, Object>)target;
		
		ValidationParams op = (ValidationParams)paramsMap.get(ValidationParams.OP_TYPE);
		if( op != ValidationParams.OP_SAVE_IPASSIGNMENT ){
			throw new IllegalArgumentException("IpAddressValidator: Invalid operation");
		}
		if( paramsMap.size() < 3 ) {
			throw new IllegalArgumentException("IpAddressValidator: No enough arguments for validation");
		}
				
		IPTeaming team = (IPTeaming) paramsMap.get(ValidationParams.PARAM_IPTEAM);
		if(team ==  null) {
			throw new IllegalArgumentException("IpAddressValidator: team is missing");
		}

		UserInfo userInfo = (UserInfo)paramsMap.get(ValidationParams.PARAM_USER_INFO);
		if(userInfo == null) {
			throw new IllegalArgumentException("IpAddressValidator: userInfo is missing");
		}
		validateSave( team, userInfo, errors );
	}

	private void validateSave( IPTeaming team, UserInfo userInfo, Errors errors ){

		if( errors.hasErrors())	return; //no point to proceed with validation if this one is invalid
		
		DataPort dp = team.getDataPort();
		IPAddressDetails ipDetails = team.getIpAddress();
		assert( dp != null );
		assert( ipDetails != null );
		
		ipAddressValidatorCommon.validateIpAddressFormat(ipDetails.getIpAddress(), errors);
		ipAddressValidatorCommon.validateGWFormat( ipDetails.getGateway(), errors);
		if( errors.hasErrors()) return; //No point to proceed with validation since ip has invalid format. Cannot do further check;

		Item item = ipAddressValidatorCommon.validateAndGetItem(dp, errors);
		if( errors.hasErrors()) return; //no point to proceed with validation if this one is invalid

		Long locationId = ipAddressValidatorCommon.validateAndGetLocationId(item, errors);
		if( errors.hasErrors())	return; //no point to proceed with validation if this one is invalid
	
		ipAddressValidatorCommon.validateItemEditability( item, userInfo, errors);		
		ipAddressValidatorCommon.validateIsDuplicateInSite( ipDetails, dp.getPortId(), locationId, errors);
		ipAddressValidatorCommon.validateIfGW( ipDetails, locationId, errors );
	}
}
