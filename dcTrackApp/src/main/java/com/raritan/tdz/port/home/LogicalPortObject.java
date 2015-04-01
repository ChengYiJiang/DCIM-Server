package com.raritan.tdz.port.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

public class LogicalPortObject extends DataPortObject {

	@Override
	public Set<Long> getPortSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.PortSubClass.LOGICAL );
		return Collections.unmodifiableSet( codes );
	}

	private Set<Long> getSupportingItemClassMountingFormFactor() {
		Set<Long> supportingItemClassMountingFormFactor = new HashSet<Long>(1);
		supportingItemClassMountingFormFactor.add(105L); // Device Blade
		supportingItemClassMountingFormFactor.add(205L); // Network Blade
		
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

		// Validate supported item class:subclass
		validateItemClassSubclass(target, errors);
		
	}

	@Override
	public boolean isConnectorValid() {
		return true; // logical ports are ok not to have connector
	}

	@Override
	public void init(IPortInfo port, Errors errors) {
		super.init(port, errors);
		
	}

	@Override
	public Set<Long> getItemClassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( -1L );
		return Collections.unmodifiableSet( codes );
	}
	
}
