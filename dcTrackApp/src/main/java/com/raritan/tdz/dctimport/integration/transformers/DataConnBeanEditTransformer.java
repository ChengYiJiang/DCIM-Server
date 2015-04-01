package com.raritan.tdz.dctimport.integration.transformers;


import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DataConnImport;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

public class DataConnBeanEditTransformer extends DataConnBeanTransformer {
	public DataConnBeanEditTransformer(String uuid) {
		super(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception {
		CircuitDTO circuit = null;
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());

		DataConnImport dcImport = (DataConnImport) beanObj;
		
		dcImport.checkRequiredFields(errors);
		
		if(errors.hasErrors()) return null;

		CircuitViewData pc = dataCircuitDAO.getDataCircuitForStartPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());
		
		if(pc == null) {//check again if there are an existing circuit using the port id - needed to cover case when circuit is reserved.
			circuit = newDataCircuitDtoFromDCImport(dcImport, errors, userInfo);
			
			if(circuit != null) {
				pc = dataCircuitDAO.getDataCircuitForStartPortId(circuit.getStartPortId());
			}
		}		
		
		if (pc == null) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("Import.Circuit.CircuitNotExist", errorArgs, "missing circuit");
		}
		else {
			dcImport.setCircuitId(pc.getCircuitUID().floatValue());
			dcImport.setStatusLksValueCode(pc.getStatusLksCode());
			
			circuit = newDataCircuitDtoFromDCImport(dcImport, errors, userInfo);
			
			setNodeUsedFlag(circuit, pc.getCircuitTrace());
		}

		Object[] parameters = {circuit, userInfo};
		
		return parameters;	
	}
	
}
