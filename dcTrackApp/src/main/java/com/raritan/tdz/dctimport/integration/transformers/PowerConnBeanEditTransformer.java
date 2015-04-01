package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.PowerConnImport;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortDTOBase;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.user.home.UserHome;

public class PowerConnBeanEditTransformer extends PowerConnBeanTransformer {

	@Autowired
	private UserHome userHome;
	
	@Autowired
	private PowerCircuitDAO powerCircuitDAO;

	public PowerConnBeanEditTransformer(String uuid) {
		super(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception, ServiceLayerException {
		CircuitDTO circuit = null;
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());
		PowerConnImport dcImport = (PowerConnImport) beanObj;
		
		dcImport.checkRequiredFields(errors);
		
		if(errors.hasErrors()) return null;

		CircuitViewData pc = powerCircuitDAO.getPowerCircuitForStartPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());
		
		if (pc == null) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("Import.Circuit.CircuitNotExist", errorArgs, "missing circuit");
		}		
		else{
			dcImport.setCircuitId(pc.getCircuitUID().floatValue());
			dcImport.setStatusLksValueCode(pc.getStatusLksCode());
			circuit = newPowerCircuitDtoFromDCImport(dcImport, errors, userInfo);
			setNodeUsedFlag(circuit, pc.getCircuitTrace());
		}
		
		Object[] parameters = {circuit, userHome.getCurrentUserInfo(), userInfo};
		
		return parameters;
	}

	private void setNodeUsedFlag(CircuitDTO circuit, String oldTrace) throws DataAccessException {
		String connId;
		PortDTOBase node;
		PowerConnection conn;
		
		for(Object obj:circuit.getNodeList()) {
			if(obj instanceof PortDTOBase) {
				node = (PortDTOBase)obj;
				node.setUsed(true);
				conn = null;
				
				for(PowerConnection c: powerConnDAO.getConnectionsForSourcePort(node.getPortId())){
					conn = c;
					break;
				}
				
				if(conn == null) {
					node.setUsed(false);
					continue;
				}
				
				connId = "," + String.valueOf(conn.getPowerConnectionId()) + ",";
				
				if(oldTrace.indexOf(connId) == -1) {
					//connection is not part of the existing circuit
					node.setUsed(false);
				}
			}
		}
	}
	
}
