package com.raritan.tdz.port.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.GlobalUtils;

public class RackPduOutputPortObject extends PowerPortObject {


	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
	}

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.RACK_PDU_OUTPUT );
		return Collections.unmodifiableSet( codes );	
	}

	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(501L); // Rack PDU R
		supportingItemClassMountingFormFactor.add(502L); // Rack PDU NR
		supportingItemClassMountingFormFactor.add(506L); // Rack PDU ZU
		return Collections.unmodifiableSet( supportingItemClassMountingFormFactor );
	}

	@Override
	public void validateItemClassSubclass(Object target, Errors errors) {
		Item item = null;
		if ((null != target) && (target instanceof Item)) {
			item = (Item) target;
		}
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		Set<Long> supportingItemClassMountingFormFactor = getSupportingItemClassMountingFormFactor();
		
		if (!supportingItemClassMountingFormFactor.contains(classMountingFormFactorValue)) {
			Object[] errorArgs = {};
			errors.rejectValue("tabDataPorts", "PortValidator.inputCordPortUnsupportedItemClass", errorArgs, "Input Cord can only be created for Rack PDU.");
		}
	}

	private void validateMoreInvalidFields(Errors errors) {

		// validate amps nameplate range
		powerPortObjectHelper.validateFieldValueRange(getPortInfo(), errors, "ampsNameplate", "Amps(Rated)", "PortValidator.powerIncorrectFieldValue", 0L, null);
		
	}
	
	@Override
	public void validateEdit(Errors errors) {
		
		super.validateEdit(errors);
		
		List<String> fields = new ArrayList<String>();
		fields.add("ampsNameplate");
		fields.add("ampsBudget");
		fields.add("phaseLegsLookup");
		fields.add("breakerPort");
		powerPortObjectHelper.validateEdit(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.connectedRPDUOutputPowerPortCannotEdit", getSavedPort());

	}

	
	@Override
	public void validateSave(Object target, Errors errors) {
		
		// Validate Item class, subclass and mounting
		validateItemClassSubclass(target, errors);

		super.validateSave(target, errors);

		PowerPort powerPort = getPowerPort();
		
		// validate incorrect field values
		validateMoreInvalidFields(errors);
		
		// validate voltage
		Item itemObj = (Item) target;
		if (!isVoltageValid()) {
			if (itemObj.getSkipValidation() == null || !itemObj.getSkipValidation()) {
				String modelName = "";
				String mfrName = "";
				if (null != itemObj.getModel()) {
					if (null != itemObj.getModel().getModelName()) {
						modelName = itemObj.getModel().getModelName(); 
					}
					if (null != itemObj.getModel().getModelMfrDetails()) {
						mfrName = itemObj.getModel().getModelMfrDetails().getMfrName();
					}
				}
				Object[] errorArgs = { mfrName, modelName };
				errors.rejectValue("cmbModel", "ItemValidator.invalidPowerPortVolt", errorArgs, "Model has incorrect voltage for power ports");
			}
		}
		
		// validate the input cord
		if (null == powerPort.getInputCordPort() || null == powerPort.getInputCordPort().getItem() || null == powerPort.getItem() || 
				powerPort.getItem().getItemId() != powerPort.getInputCordPort().getItem().getItemId()) {
			Object[] errorArgs = { powerPort.getPortName() };
			errors.rejectValue("cmbModel", "PortValidator.invalidInputCord", errorArgs, "Port has incorrect input cord");
		}

	}

	@Override
	public boolean isConnectorValid() {
		PowerPort powerPort = getPowerPort();
		return (powerPort.getConnectorLookup() != null);
	}
	
	private boolean isVoltageValid() {
		boolean invalidOutputVolt = false;
		PowerPort powerPort = getPowerPort();
		
		if (null != powerPort.getVoltsLookup() && null != powerPort.getVoltsLookup().getLkpValue() && !GlobalUtils.isNumeric(powerPort.getVoltsLookup().getLkpValue().trim())) {
			invalidOutputVolt = true;
		}
		return !invalidOutputVolt;
	}

	@Override
	public boolean isModified() {
		// TODO:: check if data port was modified
		return false;
	}
	
	@Override
	public void validateCommonAttributes(IPortInfo refPort, Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		/*fields.put("powerFactor", "Power Factor");
		fields.put("wattsNameplate", "Watts Nameplate");
		fields.put("wattsBudget", "Watts Budget");*/
		
		powerPortObjectHelper.validateCommonAttributes(getPortInfo(), refPort, errors, fields, "PortValidator.commonAttributeVoilations");
		
	}

	@Override
	public void applyCommonAttributes(IPortInfo refPort, Errors errors) {
		
		Map<String, String> fields = new HashMap<String, String>();
		/*fields.put("powerFactor", "Power Factor");
		fields.put("wattsNameplate", "Watts Nameplate");
		fields.put("wattsBudget", "Watts Budget");*/
		
		powerPortObjectHelper.applyCommonAttributes(getPortInfo(), refPort, errors, fields, "PortValidator.commonAttributeVoilations");
		
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.RACK_PDU );
		return Collections.unmodifiableSet( codes );
	}
	
}
