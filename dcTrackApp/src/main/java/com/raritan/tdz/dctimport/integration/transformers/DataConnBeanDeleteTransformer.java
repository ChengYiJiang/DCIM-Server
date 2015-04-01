package com.raritan.tdz.dctimport.integration.transformers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DataConnImport;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

public class DataConnBeanDeleteTransformer extends DataConnBeanTransformer {
	public DataConnBeanDeleteTransformer(String uuid) {
		super(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception {
		List<CircuitCriteriaDTO> recList = null;
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());
		DataConnImport dcImport = (DataConnImport) beanObj;
		
		dcImport.checkRequiredFields(errors);
		
		if(errors.hasErrors()) return null;

		DataCircuit dc = getCircuit(dcImport, errors);

		if(!errors.hasErrors()){
			recList = new ArrayList<CircuitCriteriaDTO>();
			CircuitCriteriaDTO rec = new CircuitCriteriaDTO();
			rec.setCircuitId(dc.getCircuitUID().floatValue());
			rec.setCircuitType(dc.getCircuitType());
			rec.setUserInfo(userInfo);
			recList.add(rec);	
		}
		
		Object[] parameters = {recList, userInfo};
		
		return parameters;
	}

	private DataCircuit getCircuit(DataConnImport dcImport, Errors errors) throws DataAccessException, Exception {
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());
		
		DataPort p1 = dataPortDAO.getPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());
		DataPort p2 = dataPortDAO.getPort(dcImport.getEndingItemLocation(), dcImport.getEndingItemName(), dcImport.getEndingPortName());
		DataCircuit dc = null;

		if(p1 != null && p2 != null) {
			dc = dataCircuitDAO.viewDataCircuitByPortIds(p1.getPortId(), p2.getPortId());
		}
		
		if(dc == null) {//check again if there are an existing circuit using the port id - needed to cover case when circuit is reserved.
			CircuitDTO circuit = newDataCircuitDtoFromDCImport(dcImport, errors, userInfo);
			
			if(circuit != null) {
				for(DataCircuit cx:dataCircuitDAO.viewDataCircuitByStartPortId(circuit.getStartPortId())) {
					dc = cx;
					break;
				}
			}
		}		
				
		if (dc == null) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("Import.Circuit.CircuitNotExist", errorArgs, "missing circuit");
		}		
		
		return dc;
	}
}
