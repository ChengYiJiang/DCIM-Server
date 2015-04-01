/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.circuit.dao.DataConnDAO;
import com.raritan.tdz.circuit.home.CircuitProc;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * @author  Santo Rosario
 *
 */
public class DataConnConnectorValidator implements Validator {
	
	@Autowired
	DataConnDAO dataConnDAO;
	
	@Autowired
	DataPortDAO dataPortDAO;

	public DataConnConnectorValidator() {
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return DataConnection.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		validateArgs(target);
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		DataConnection dataConn = (DataConnection)targetMap.get(DataConnection.class.getName());
		
		validateSrcDestPort(dataConn, errors);
		
		if (dataConn.isLinkTypeExplicit() && dataConn.getSourceDataPort() != null 
				&& dataConn.getDestDataPort() != null){
			DataPort srcPort = dataPortDAO.read(dataConn.getSourceDataPort().getPortId()); 
			DataPort dstPort = dataPortDAO.read(dataConn.getDestDataPort().getPortId());
			validateConnectors(srcPort, dstPort, errors);
		}
	}
	
	private void validateArgs(Object target) {
		if (!(target instanceof Map)) throw new IllegalArgumentException("You must provide a Map of String and object for this validator");
		
		Map<String,Object> targetMap = (Map<String,Object>)target;
		
		DataConnection dataConn = (DataConnection)targetMap.get(DataConnection.class.getName());
		
		if (dataConn == null) throw new IllegalArgumentException("You must provide a data connection target");
		
		Integer nodeNumber = (Integer)targetMap.get(Integer.class.getName());
		
		if (nodeNumber == null) throw new IllegalArgumentException("You must provide a node number for this target");
	}
	
	private void validateSrcDestPort(DataConnection dataConn, Errors errors) {
		if (dataConn.getSourcePort() == null){
			errors.reject("dataProc.missingPort");
		}
		
		if (dataConn.getDestDataPort() == null){
			errors.reject("dataProc.missingPort");
		}
	}
	
	private void validateConnectors(DataPort sPort, DataPort dPort, Errors errors) {
		if (sPort != null && dPort != null){
			if(sPort.isVirtual() || sPort.isLogical()){
				return;
			}
			
			//checks connectors on both ends. if not the same and are not compatible per tlkpConnectorCompat then disallow
			if(sPort.getConnectorLookup() != null && dPort.getConnectorLookup() != null
					&& dataConnDAO.areConnectorsCompatible(sPort.getConnectorLookup(), dPort.getConnectorLookup()) == false){
				
				String sPortItemName = sPort.getItem() != null ? sPort.getItem().getItemName() : "<Unknown>";
				String dPortItemName = dPort.getItem() != null ? dPort.getItem().getItemName() : "<Unknown>";
				
				Object[] errorArgs = new Object[] {
						sPortItemName,
						sPort.getPortName(),
						dPortItemName,
						dPort.getPortName()
						};
				
				errors.reject("powerProc.incompatibleConnector", errorArgs,"Connectors are incompatibility");
			}
		}
	}

}
