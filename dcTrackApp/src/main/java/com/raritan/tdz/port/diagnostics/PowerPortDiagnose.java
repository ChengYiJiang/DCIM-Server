package com.raritan.tdz.port.diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.diagnostics.Diagnostics;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.events.dao.EventDAO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.validators.PowerPortValidator;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.home.IPortObject;
import com.raritan.tdz.port.home.IPortObjectFactory;

public class PowerPortDiagnose implements Diagnostics {

	@Autowired(required=true)
	PowerPortValidator powerPortValidator; 
	
	@Autowired(required=true)
	ItemDAO itemDAO;

	@Autowired(required=true)
	PowerPortDAO powerPortDAO;

	@Autowired(required=true)
	PowerPortDiagnosticsDAO powerPortDiagnosticsDAO;
	
	@Autowired(required=true)
	private IPortObjectFactory portObjectFactory;
	
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
		
		eventDAO.generatePowerChainDiagnosticsEvent("Power Port Diagnostics", Long.valueOf(allErrors.getErrorCount()), 0L, 0L);
		
	}
	
	private void validatePort(PowerPort port, Errors allErrors) {
		
		if (null != port) {
			Errors errors = getPortErrorObject();
			
			IPortObject portObject = portObjectFactory.getPortObject(port, errors);
			
			portObject.validateSave(port.getItem(), errors);
			
			powerPortDiagnosticsDAO.reportError(port, errors, "ERROR");
			
			allErrors.addAllErrors(errors);
			
		}
		
	}
	
	private MapBindingResult getPortErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, PowerPort.class.getName() );
		return errors;
		
	}


}
