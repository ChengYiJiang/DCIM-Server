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
 * @author Santo Rosario
 *
 */
public class DataConnPortValidator implements Validator {
	
	@Autowired
	DataPortDAO dataPortDAO;
	
	Map<Long, Validator> itemSpecificPortValidator;
	

	public DataConnPortValidator() {
		
	}
	
	public DataConnPortValidator(Map<Long, Validator> itemSpecificPortValidator) {
		this.itemSpecificPortValidator = itemSpecificPortValidator;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz.equals(DataConnection.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		DataPort srcPortClient = (DataPort)targetMap.get(DataPort.class.getName());
				
		if (srcPortClient != null){
			DataPort srcPort = dataPortDAO.read(srcPortClient.getPortId());
				
			if(srcPort.isVirtual() || srcPort.isLogical()){
				return;
			}

			validateConnectorLookup(srcPort, errors);			
			validateMediaLookup(srcPort,errors);			
			validateSpeedLookup(srcPort,errors);
			validateProtocolLookup(srcPort,errors);
			
			Long srcPortSubClassValueCode = srcPort != null && srcPort.getPortSubClassLookup() != null ?
					srcPort.getPortSubClassLookup().getLkpValueCode() : null;
			if (srcPortSubClassValueCode != null && itemSpecificPortValidator != null && itemSpecificPortValidator.get(srcPortSubClassValueCode) != null){
				itemSpecificPortValidator.get(srcPortSubClassValueCode).validate(srcPort, errors);
			}
		}
	}


	//---------------- Private methods -----------------------------
	private void validateMediaLookup(DataPort srcPort, Errors errors) {
		if(srcPort.getMediaId() == null){ //For Device and Network items, check for Budget Watts
			String errMsg = ApplicationCodesEnum.DATA_CIR_PORT_MISSING_MEDIA.value();
			errMsg = errMsg.replaceAll("<ItemName>", srcPort.getItem().getItemName());
			errMsg = errMsg.replaceAll("<PortName>", srcPort.getPortName());
			errors.reject("XXX", errMsg);			
		}
	}

	private void validateSpeedLookup(DataPort srcPort, Errors errors) {
		if(srcPort.isDataPanel() == false){
			if(srcPort.getSpeedId() == null){
				String errMsg = ApplicationCodesEnum.DATA_CIR_PORT_MISSING_SPEED.value();
				errMsg = errMsg.replaceAll("<ItemName>", srcPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<PortName>", srcPort.getPortName());
				errors.reject("XXX", errMsg);
			}
		}	
	}

	private void validateProtocolLookup(DataPort srcPort, Errors errors) {
		if(srcPort.isDataPanel() == false){
			if(srcPort.getProtocolID() == null){
				String errMsg = ApplicationCodesEnum.DATA_CIR_PORT_MISSING_PROTOCOL.value();
				errMsg = errMsg.replaceAll("<ItemName>", srcPort.getItem().getItemName());
				errMsg = errMsg.replaceAll("<PortName>", srcPort.getPortName());
				errors.reject("XXX", errMsg);
			}
		}			
	}
	
	private void validateConnectorLookup(DataPort srcPort, Errors errors) {
		if (srcPort.getConnectorLookup() == null){
			String errMsg = ApplicationCodesEnum.DATA_CIR_PORT_MISSING_CONNECTOR.value();
			errMsg = errMsg.replaceAll("<ItemName>", srcPort.getItem().getItemName());
			errMsg = errMsg.replaceAll("<PortName>", srcPort.getPortName());
			errors.reject("XXX", errMsg);;
		}
	}

	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		DataPort port = (DataPort)targetMap.get(DataPort.class.getName());
		
		if (port == null) throw new IllegalArgumentException("You must provide a data port target");
		
		Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		if (nodeNumber == null) throw new IllegalArgumentException("You must provide a node number for this target");
	}

}
