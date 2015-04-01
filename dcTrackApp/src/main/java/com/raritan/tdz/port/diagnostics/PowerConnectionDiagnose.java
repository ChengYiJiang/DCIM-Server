package com.raritan.tdz.port.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.validators.PowerConnCompatibilityValidatorFactory;
import com.raritan.tdz.diagnostics.Diagnostics;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.events.dao.EventDAO;

public class PowerConnectionDiagnose implements Diagnostics {

	@Autowired
	PowerConnCompatibilityValidatorFactory compValidatorFactory;

	@Autowired
	PowerConnDAO powerConnDAO;
	
	@Autowired(required=true)
	CircuitDiagnosticsDAO circuitDiagnosticsDAO;
	
	@Autowired(required=true)
	private EventDAO eventDAO;
	
	Map<Long, List<Long>> portSubclassConnection = null;
	
	Map<String, Validator> connectionCompatibilityValidators = null;
	
	List<String> informationalErrorCode = null;
	
	public List<String> getInformationalErrorCode() {
		return informationalErrorCode;
	}

	public void setInformationalErrorCode(List<String> informationalErrorCode) {
		this.informationalErrorCode = informationalErrorCode;
	}

	public Map<String, Validator> getConnectionCompatibilityValidators() {
		return connectionCompatibilityValidators;
	}

	public void setConnectionCompatibilityValidators(
			Map<String, Validator> connectionCompatibilityValidators) {
		this.connectionCompatibilityValidators = connectionCompatibilityValidators;
	}

	public Map<Long, List<Long>> getPortSubclassConnection() {
		return portSubclassConnection;
	}

	public void setPortSubclassConnection(
			Map<Long, List<Long>> portSubclassConnection) {
		this.portSubclassConnection = portSubclassConnection;
	}

	@Override
	public void diagnose(Errors errors) {
		
		Errors allErrors = getPowerConnErrorObject();
		
		for (Map.Entry<Long, List<Long>> entry: 	portSubclassConnection.entrySet()) {
			Long srcPortSubclass = entry.getKey();
			List<Long> dstPortSubClasses = entry.getValue();
			for (Long dstPortSubClass: dstPortSubClasses) {
				List<PowerConnection> conns = powerConnDAO.getConnBetweenPortSubclass(srcPortSubclass, dstPortSubClass);
				
				for (PowerConnection conn: conns) {
					validateCompatibility(conn, allErrors);
				}				
			}
		}

		// Calculate information and errors in the error list
		Long informationCodeCount = 0L;
		Long errorCodeCount = 0L;
		if (null != informationalErrorCode && informationalErrorCode.size() > 0) {
			List<ObjectError> objectErrors = allErrors.getAllErrors();
			for (ObjectError error: objectErrors) {
				if (informationalErrorCode.contains(error.getCode())) {
					informationCodeCount++;
				}
				else {
					errorCodeCount++;
				}
			}
		}
		else {
			errorCodeCount = Long.valueOf(allErrors.getErrorCount());
		}
		
		eventDAO.generatePowerChainDiagnosticsEvent("Power Connection Diagnostics", errorCodeCount, 0L, informationCodeCount);
		
	}
	
	private void validateCompatibility(PowerConnection conn, Errors allErrors) {
		Errors errors = getPowerConnErrorObject();
		
		Validator connValidator = getConnValidator(conn); //compValidatorFactory.getCompatibilityValidator(conn);
		Map<String, Object> targetMap = getTargetMap(conn);
		if (connValidator != null) {
			connValidator.validate(targetMap, errors);
			
			circuitDiagnosticsDAO.reportError(conn, errors, "ERROR", informationalErrorCode);
			
			allErrors.addAllErrors(errors);
			
		}
		
	}

	private Map<String, Object> getTargetMap(PowerConnection conn) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), conn);
		targetMap.put(Integer.class.getName(), 0);
		return targetMap;
	}
	
	private MapBindingResult getPowerConnErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, PowerConnection.class.getName() );
		return errors;
		
	}

	private Validator getConnValidator(PowerConnection conn) {
		String key = null;
		if (null != conn && 
				null != conn.getSourcePowerPort() && null != conn.getDestPowerPort() &&
				null != conn.getSourcePowerPort().getPortSubClassLookup() && null != conn.getDestPowerPort().getPortSubClassLookup() &&
				null != conn.getSourcePowerPort().getPortSubClassLookup().getLkpValueCode() && null != conn.getDestPowerPort().getPortSubClassLookup().getLkpValueCode()) {
			key = conn.getSourcePowerPort().getPortSubClassLookup().getLkpValueCode().toString() + ":" + conn.getDestPowerPort().getPortSubClassLookup().getLkpValueCode().toString();
			return connectionCompatibilityValidators.get(key);
		}

		return null;
	}

}
