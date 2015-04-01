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
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;

public class InputCordPortObject extends PowerPortObject {

	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
	}

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.INPUT_CORD );
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
		powerPortObjectHelper.validateEdit(getPortInfo(), errors, powerPortDAO, fields, "PortValidator.connectedPowerPortCannotEdit", getSavedPort());

	}
	
	@Override
	public void validateSave(Object target, Errors errors) {
		// Validate Item class, subclass and mounting
		validateItemClassSubclass(target, errors);
		
		super.validateSave(target, errors);

		// Validate invalid field
		validateMoreInvalidFields(errors);

	}

	@Override
	public void validateDelete(Object target, Errors errors) {
		super.validateDelete(target, errors);

		Item origItem = (null != SavedItemData.getCurrentItem()) ? SavedItemData.getCurrentItem().getSavedItem() : null;
	
		if (null == origItem) {
			return;
		}
		
		// get the map between the inlet cord and outlet(s) from the item currently in the database 
		Map<Long, ArrayList<Long>> inputOutputIdMap = new HashMap<Long, ArrayList<Long>>();
		Map<Long, ArrayList<PowerPort>> inputOutputPPMap = new HashMap<Long, ArrayList<PowerPort>>();
		Set<PowerPort> dbPowerPortList = origItem.getPowerPorts();
		for (PowerPort pp: dbPowerPortList) {
			if (pp.isInputCord() && null != pp.getPortId()) {
				inputOutputIdMap.put(pp.getPortId(), new ArrayList<Long>());
				ArrayList<PowerPort> dtoList = new ArrayList<PowerPort>();
				dtoList.add(pp);
				inputOutputPPMap.put(pp.getPortId(), dtoList);
			}
		}
		for (PowerPort pp: dbPowerPortList) {
			if (!pp.isInputCord() && pp.getInputCordPortId() != null && pp.getInputCordPortId() > 0) {
				inputOutputIdMap.get(pp.getInputCordPortId()).add(pp.getPortId());
				inputOutputPPMap.get(pp.getInputCordPortId()).add(pp);
			}
		}
		
		// get deleted power port ids
		List<Long> delPortIds = new ArrayList<Long>();
		Item itemObj = (Item) target;
		Set<PowerPort> powerPortList = itemObj.getPowerPorts();
		List<Long> portIds = new ArrayList<Long>();
		for (PowerPort dto: powerPortList) {
			portIds.add(dto.getPortId());
		}
		for (PowerPort pp: dbPowerPortList) {
			if (!portIds.contains(pp.getPortId())) {
				// Remove all the output first and then the input, otherwise hibernate will add the output back using cascade
				if (pp.isInputCord()) {
					delPortIds.add(pp.getPortId().longValue());
				}
				else {
					delPortIds.add(0, pp.getPortId().longValue());
				}
			}
		}

		for (Map.Entry<Long, ArrayList<Long>> inputOutputs: inputOutputIdMap.entrySet()) {
			if (delPortIds.contains(inputOutputs.getKey()) && !delPortIds.containsAll(inputOutputs.getValue())) {
				// throw business validation error
				String inputName = new String();
				String outputNameList = new String();
				ArrayList<PowerPort> ppList = inputOutputPPMap.get(inputOutputs.getKey());
				for (PowerPort pp: ppList) {
					if (pp.isInputCord()) {
						inputName = pp.getPortName();
					}
					else {
						outputNameList += (" " + pp.getPortName());
					}
				}
				Object errorArgs[]  = { inputName, outputNameList };
				errors.rejectValue("tabPowerPorts", "PortValidator.inletDeletion", errorArgs, "Inlet and all of its associated outlets must be deleted");
			}
		}


	}

	@Override
	public boolean isConnectorValid() {
		PowerPort powerPort = getPowerPort();
		return (powerPort.getConnectorLookup() != null);
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
		codes.add( -1L );
		// codes.add( SystemLookup.Class.RACK_PDU );
		return Collections.unmodifiableSet( codes );
	}
	
}
