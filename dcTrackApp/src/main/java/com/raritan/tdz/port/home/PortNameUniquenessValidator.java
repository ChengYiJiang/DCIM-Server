/**
 * 
 */
package com.raritan.tdz.port.home;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;

/**
 * @author basker
 *
 */
public class PortNameUniquenessValidator implements Validator {
	
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// Supports DataPort.class, PowerPort.class, SensorPort.class
		return (DataPort.class.equals(clazz) || PowerPort.class.equals(clazz) || SensorPort.class.equals(clazz));
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 * This function validates for port name uniqueness
	 * It takes a target in form of Map <String, Object> with 2 elements
	 * 1. ports , List<IportInfo> : List of ports to be verified against 
	 *                             (NOTE: this list excludes the port that is currently worked with.
	 *                              e.g. if your want to check port name uniqueness for sortOrder 2, it will 
	 *                              exclude the port with sortOrder 2 and verify uniqueness with others)
	 * 2. name , port/sensor Name : name to be assigned for a new port
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object target, Errors errors) {
		if (target instanceof Map){
			Map<String, Object> map = (Map<String,Object>)target;
			
			List<IPortInfo> piList = (List<IPortInfo>) map.get("ports");
			String name= (String)map.get("name");
			
			if (piList == null || name == null) throw new IllegalArgumentException();
			
			for (IPortInfo port: piList ) {
				IPortInfo portInfo = (IPortInfo) port;
				String portName = (portInfo != null) ? portInfo.getPortName(): null;
				/* if any of existing ports name matches newName report error */
				if (portName != null && portName.equals(name)) {
					Object[] errorArgs = {portName};
					errors.rejectValue("Ports", "PortValidator.duplicatePortName", errorArgs, "Port name is not unique");
					break;
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
