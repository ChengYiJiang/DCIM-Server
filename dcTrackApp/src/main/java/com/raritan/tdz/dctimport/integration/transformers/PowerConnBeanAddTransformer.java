package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.PowerConnImport;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.user.home.UserHome;

public class PowerConnBeanAddTransformer extends PowerConnBeanTransformer {

	@Autowired
	private UserHome userHome;
	
	@Autowired
	private PowerCircuitDAO powerCircuitDAO;

	public PowerConnBeanAddTransformer(String uuid) {
		super(uuid);
		setUuid(uuid);
	}

	@Override
	public Object[] convert(DCTImport beanObj, Errors errors) throws DataAccessException, Exception, ServiceLayerException {
		CircuitDTO circuit = null;
		
		UserInfo userInfo = userHome.getCurrentUserInfo(getUuid());

		PowerConnImport dcImport = (PowerConnImport) beanObj;
		
		dcImport.checkRequiredFields(errors);
		
		if(errors.hasErrors()) return null;

		CircuitViewData pc = powerCircuitDAO.getPowerCircuitForStartPort(dcImport.getStartingItemLocation(), dcImport.getStartingItemName(), dcImport.getStartingPortName());
		
		if (pc != null) {
			Object[] errorArgs = {  dcImport.getStartingItemName() + ":" + dcImport.getStartingPortName() };
			errors.reject("Import.Circuit.CircuitExist", errorArgs, "duplicate circuit");
		}		
		else{
			circuit = newPowerCircuitDtoFromDCImport(dcImport, errors, userInfo);
		}
		
		Object[] parameters = {circuit, userInfo};
		
		return parameters;
	}
	
}
