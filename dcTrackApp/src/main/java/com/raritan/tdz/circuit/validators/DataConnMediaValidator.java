/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;

/**
 * @author  Santo Rosario
 *
 */
public class DataConnMediaValidator implements Validator {
	
	@Autowired
	DataPortDAO dataPortDAO;

	public DataConnMediaValidator() {
		super();
	}
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return DataPort.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		DataConnection conn = (DataConnection)targetMap.get(DataConnection.class.getName());
				
		DataPort sPort = conn.getSourceDataPort();
		DataPort dPort = conn.getDestDataPort();
		
		if(dPort == null || sPort.isVirtual() || sPort.isLogical()){
			return;
		}

		if(sPort.getMediaId().getLksId() != dPort.getMediaId().getLksId() && dPort.isPhysical()){
			String errMsg = ApplicationCodesEnum.DATA_CIR_MISMATCH_MEDIA.value();
			errMsg = errMsg.replaceAll("<ItemName1>", sPort.getItem().getItemName());
			errMsg = errMsg.replaceAll("<ItemName2>", dPort.getItem().getItemName());
			errMsg = errMsg.replaceAll("<PortName1>", sPort.getPortName());
			errMsg = errMsg.replaceAll("<PortName2>", dPort.getPortName());
			errMsg = errMsg.replaceAll("<Media1>", sPort.getMediaId().getLkpValue());
			errMsg = errMsg.replaceAll("<Media2>", dPort.getMediaId().getLkpValue());

			errors.reject("XXX", errMsg);
		}
		
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a data connection target");
	}

}
