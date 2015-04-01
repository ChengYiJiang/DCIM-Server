package com.raritan.tdz.dctimport.integration.transformers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.PowerConnImport;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.user.home.UserHome;

public class PowerConnBeanDeleteTransformer extends PowerConnBeanTransformer {
	@Autowired
	private UserHome userHome;
	
	@Autowired
	private PowerCircuitDAO powerCircuitDAO;

	public PowerConnBeanDeleteTransformer(String uuid) {
		super(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception {
		List<CircuitCriteriaDTO> recList = null;
		
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
			recList = new ArrayList<CircuitCriteriaDTO>();
			CircuitCriteriaDTO rec = new CircuitCriteriaDTO();
			rec.setCircuitId(pc.getCircuitUID().floatValue());
			rec.setCircuitType(pc.getCircuitType());
			rec.setUserInfo(userInfo);
			recList.add(rec);	
		}
		
		Object[] parameters = {recList, userInfo};
		
		return parameters;
	}
	
}
