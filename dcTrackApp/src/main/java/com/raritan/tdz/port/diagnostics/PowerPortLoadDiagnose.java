package com.raritan.tdz.port.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.raritan.tdz.diagnostics.Diagnostics;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.events.dao.EventDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class PowerPortLoadDiagnose implements Diagnostics {

	@Autowired(required=true)
	PowerPortDAO powerPortDAO;

	@Autowired(required=true)
	Validator powerPortLoadValidator;
	
	@Autowired(required=true)
	PowerPortDiagnosticsDAO powerPortDiagnosticsDAO;
	
	@Autowired(required=true)
	private EventDAO eventDAO;
	
	List<Long> portSubClass = null;
	
	public List<Long> getPortSubClass() {
		return portSubClass;
	}

	public void setPortSubClass(List<Long> portSubClass) {
		this.portSubClass = portSubClass;
	}

	@Override
	public void diagnose(Errors errors) {
		
		Errors allErrors = getPortErrorObject();
		
		// validate all the request port subclass
		List<PowerPort> ports = powerPortDAO.getPorts(portSubClass);
		for (PowerPort port: ports) {
			
			validatePort(port, allErrors);
		}
		
		eventDAO.generatePowerChainDiagnosticsEvent("Power Port Load Diagnostics", 0L, Long.valueOf(allErrors.getErrorCount()), 0L);

	}
	
	private void validatePort(PowerPort port, Errors allErrors) {
		
		if (null != port) {
			Errors errors = getPortErrorObject();

			Map<String, Object> target = getValidatorTargetMap(port, null);
			
			powerPortLoadValidator.validate(target, errors);
			
			powerPortDiagnosticsDAO.reportError(port, errors, "WARNING");
			
			allErrors.addAllErrors(errors);
			
		}
		
	}

	private Map<String, Object> getValidatorTargetMap(PowerPort port, UserInfo userInfo) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerPort.class.getName(), port);
		targetMap.put(UserInfo.class.getName(), userInfo);
		return targetMap;
	}

	private MapBindingResult getPortErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, PowerPort.class.getName() );
		return errors;
		
	}


}
