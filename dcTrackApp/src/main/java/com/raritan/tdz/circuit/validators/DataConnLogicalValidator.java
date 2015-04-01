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
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;

/**
 * @author  Santo Rosario
 *
 */
public class DataConnLogicalValidator implements Validator {
	
	@Autowired
	DataPortDAO dataPortDAO;

	public DataConnLogicalValidator() {
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

		if(!sPort.isLogical()) return;

		ItItem item1 =(ItItem)sPort.getItem();
		long chassis1 = item1.getBladeChassis()==null ? 0 : item1.getBladeChassis().getItemId();
		
		ItItem item2 =(ItItem)dPort.getItem();
		long chassis2 = item2.getBladeChassis()==null ? -1 : item2.getBladeChassis().getItemId();

		//Check that two logical ports are in the same chassis
		if(dPort.isLogical()){ //blade logical to blade logical
			if(chassis1 != chassis2){
				String errMsg = ApplicationCodesEnum.DATA_CIR_LOGICAL_TO_LOGICAL.value();
				errMsg = errMsg.replaceAll("<ItemName1>", sPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<ItemName2>", dPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<PortName1>", sPort.getPortName());
				errMsg = errMsg.replaceAll("<PortName2>", dPort.getPortName());
				errors.reject("XXX", errMsg);
			}
		}

		//check that for logical to physical connection is in the same chassis
		if(dPort.isPhysical()){//check blade logical to chassis physical
			if(chassis1 != item2.getItemId()){
				String errMsg = ApplicationCodesEnum.DATA_CIR_LOGICAL_TO_BLADE_PHYSICAL.value();
				errMsg = errMsg.replaceAll("<ItemName1>", sPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<ItemName2>", dPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<PortName1>", sPort.getPortName());
				errMsg = errMsg.replaceAll("<PortName2>", dPort.getPortName());
				errors.reject("XXX", errMsg);
			}	
		}		
	}
	
	private void validateArgs(Object target) {
		if (target == null) throw new IllegalArgumentException("You must provide a data connection target");
	}

}
