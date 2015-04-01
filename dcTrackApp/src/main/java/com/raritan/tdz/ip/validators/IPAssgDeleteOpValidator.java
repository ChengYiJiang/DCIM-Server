package com.raritan.tdz.ip.validators;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon.ValidationParams;

public class IPAssgDeleteOpValidator implements Validator{

	@Autowired(required=true)
	IPAddressValidatorCommon ipAddressValidatorCommon;

	private final Logger log = Logger.getLogger(this.getClass());

	@Override
	public boolean supports(Class<?> clazz) {
		return IPAddressDetails.class.equals(clazz);
	}
	

	@Override
	public void validate(Object target, Errors errors) {
		Map<ValidationParams, Object> paramsMap = (Map<ValidationParams, Object>)target;
		
		ValidationParams op = (ValidationParams)paramsMap.get(ValidationParams.OP_TYPE);
		if( op != ValidationParams.OP_DELETE_ASSIGNMENT ){
			throw new IllegalArgumentException("IpAddressValidator: Invalid operation");
		}
		
		if( paramsMap.size() < 2 ) {
			throw new IllegalArgumentException("IpAddressValidator: No enough arguments for validation");
		}
		
		Long teamId = (Long) paramsMap.get(ValidationParams.PARAM_TEAM_ID);
		if( teamId == null ) {
			throw new IllegalArgumentException("IpAddressValidator: teamId is missing");
		}
		
		UserInfo userInfo = (UserInfo)paramsMap.get(ValidationParams.PARAM_USER_INFO);
		if(userInfo == null) {
			throw new IllegalArgumentException("IpAddressValidator: userInfo is missing");
		}
		
		validateDelete( teamId,  userInfo, errors );
	
	}
	
	private void validateDelete( Long teamId, UserInfo userInfo, Errors errors){
		IPTeaming teaming = ipAddressValidatorCommon.validateAndGetTeaming( teamId, errors );
		if( teaming != null ){
			DataPort dataPort = teaming.getDataPort();
			assert( dataPort != null );
			Item item = ipAddressValidatorCommon.validateAndGetItem( dataPort, errors);
			assert( item != null );
			ipAddressValidatorCommon.validateItemEditability( item, userInfo, errors);
		}else{
			log.error("teamId " + teamId + " does not exist" );
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidIPAssignement", errorArgs, "Specified ipassignment does not exist");
		}
				
	}

}
