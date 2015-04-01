package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.PowerPortImport;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.GlobalUtils;

/**
 * 
 * @author bunty
 *
 */
public class BeanToPowerPortDtoConverterImpl implements BeanToPowerPortDtoConverter {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private LksCache lksCache;
	
	Boolean updatePowerPort;

	
	
	public BeanToPowerPortDtoConverterImpl(Boolean updatePowerPort) {
		super();
		this.updatePowerPort = updatePowerPort;
	}

	
	
	@Override
	public PowerPortDTO convertBeanToPowerPortDTO(DCTImport beanObj, Errors errors) throws DataAccessException {

		PowerPortImport ppImport = (PowerPortImport) beanObj;
		PowerPortDTO dto = new PowerPortDTO();

		Long itemId = itemDAO.getItemId(ppImport.getItemName(), ppImport.getItemLocation());
		if (null == itemId) {
			Object[] errorArgs = { };
			errors.rejectValue("item", "Import.itemNotFound", errorArgs, "Cannot find item");
			return null;
		}

		LksData portSubclassLks = lksCache.getLksDataUsingLkpAndType(ppImport.getPortType(), SystemLookup.LkpType.PORT_SUBCLASS);
		if (null == portSubclassLks) {
			Object[] errorArgs = { };
			errors.rejectValue("port", "Import.PortImport.InvalidPortType", errorArgs, "Invalid port type");
			return null;
		}
		
		Long portId = powerPortDAO.getPortId(itemId, portSubclassLks.getLkpValueCode(), ppImport.getPortName());
		if (null != updatePowerPort && updatePowerPort && null == portId) {
			Object[] errorArgs = { };
			errors.rejectValue("port", "Import.PowerPortImport.InvalidPort", errorArgs, "Port not found");
			return null;
		}
		
		dto.setItemId(itemId);
		dto.setPortId(portId);
		if (null != ppImport.getPortType()) dto.setPortSubClassLksDesc(ppImport.getPortType());
		if (null != updatePowerPort && updatePowerPort) {
			dto.setPortName((null != ppImport.getNewPortName() && ppImport.getNewPortName().length() > 0) ? ppImport.getNewPortName() : ppImport.getPortName());
		}
		else {
			dto.setPortName(ppImport.getPortName());	
		}
		if (null != ppImport.getIndex()) dto.setSortOrder(ppImport.getIndex()); else dto.setSortOrder(0);
		if (null != ppImport.getColorCode()) dto.setColorLkuDesc(ppImport.getColorCode());
		if (null != ppImport.getConnector()) dto.setConnectorName(ppImport.getConnector());
		if (null != ppImport.getPhaseType()) dto.setPhaseLksDesc(ppImport.getPhaseType());
		if (null != ppImport.getVolts()) dto.setVoltsLksDesc(ppImport.getVolts());
		if (null != ppImport.getPowerFactor()) dto.setPowerFactor(ppImport.getPowerFactor()); else dto.setPowerFactor(0.0); 
		if (null != ppImport.getWattsNameplate()) dto.setWattsNameplate(ppImport.getWattsNameplate()); else dto.setWattsNameplate(0); 
		if (null != ppImport.getWattsBudget()) dto.setWattsBudget(getWattsBudget(ppImport.getWattsBudget(), ppImport.getWattsNameplate(), errors));
		if (null != ppImport.getComment()) dto.setComments(ppImport.getComment());
		if (null != ppImport.getItemRedundancy()) dto.setPsRedundancy(ppImport.getItemRedundancy());
		
		return dto;
	}

	private Long getWattsBudget(String wattsBudget, Long wattsNamePlate, Errors errors) {
		
		if (null == wattsNamePlate || null == wattsBudget) return 0L;
		
		wattsBudget = wattsBudget.replaceAll("\\s+", "");

		// validate watts budget as either number or number%
		if (GlobalUtils.isNumeric(wattsBudget)) {
			
			return new Long(wattsBudget);
		}
		else if (GlobalUtils.isNumericPercent(wattsBudget)) {
				
			wattsBudget = wattsBudget.substring(0, wattsBudget.length() - 1);
				
			return new Double(wattsNamePlate * (new Double(wattsBudget) / 100)).longValue();
				
		}
		else {
			Object[] errorArgs = {};
			errors.rejectValue("wattsBudget", "Import.PowerPortImport.InvalidWattsBudget", errorArgs, "Invalid Watts Budget");
		}

		return 0L;
	}
	

}
