package com.raritan.tdz.port.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

public class PassivePortObject extends DataPortObject {
	
	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
	}

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.PASSIVE );
		return Collections.unmodifiableSet( codes );
	}

	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(301L); // Data Panel - Rackable
		supportingItemClassMountingFormFactor.add(302L); // Data Panel - NR
		supportingItemClassMountingFormFactor.add(306L); // Data Panel - ZU
		
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
			errors.rejectValue("tabDataPorts", "PortValidator.logicalOnlyForBlades", errorArgs, "Logical Data Port can be created only for blade");
		}
	}

	@Override
	public void validateSave(Object target, Errors errors) {

		super.validateSave(target, errors);

		validateItemClassSubclass(target, errors);
		
	}

	@Override
	public boolean isConnectorValid() {
		DataPort dataPort = getDataPort();
		return (dataPort.getConnectorLookup() != null);
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( -1L );
		return Collections.unmodifiableSet( codes );
	}

}
