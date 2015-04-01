package com.raritan.tdz.diagnostics;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public class DiagnosticsHomeImpl implements DiagnosticsHome {

	private Map<String, Diagnostics> diagnosers;
	
	public DiagnosticsHomeImpl(Map<String, Diagnostics> diagnosers) {

		this.diagnosers = diagnosers;
	}

	public Map<String, Diagnostics> getDiagnosers() {
		return diagnosers;
	}

	public void setDiagnosers(Map<String, Diagnostics> diagnosers) {
		this.diagnosers = diagnosers;
	}

	@Override
	public void processLNEvent(LNEvent event) throws BusinessValidationException {

		MapBindingResult errors = getErrorObject();
		
		if (null == event || null == event.getOperationLks() || null == event.getOperationLks().getLkpValueCode() || null == event.getAction()) {
			noActionHandlerException(event, errors);
		}

		Diagnostics diagnoster = diagnosers.get(event.getAction());
		
		if (null != diagnoster) {
			diagnoster.diagnose(errors);
		}
		
	}
	
	private void noActionHandlerException(LNEvent event, Errors errors) throws BusinessValidationException {
		Object[] errorArgs = { event.toString() };
		errors.rejectValue("Diagnostics", "Diagnostics.noActionHandler", errorArgs, "No action handler defined for event " + event.toString());
	}

	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Diagnostics.class.getName() );
		return errors;
		
	}


}
