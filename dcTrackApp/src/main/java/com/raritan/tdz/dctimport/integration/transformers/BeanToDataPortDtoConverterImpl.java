package com.raritan.tdz.dctimport.integration.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.dao.DAORuntimeException;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.DataPortImport;
import com.raritan.tdz.dctimport.processors.ImportProcessorImpl;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;

/**
 * 
 * @author KC
 *
 */
public class BeanToDataPortDtoConverterImpl implements BeanToDataPortDtoConverter {

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private DataPortDAO dataPortDAO;
	
	@Autowired
	private LksCache lksCache;
	
	private String operation;
	
	public BeanToDataPortDtoConverterImpl(String operation) {
		super();
		this.operation = operation;
	}

	
	/**
	 * Convert the import bean to the value object.
	 * @param beanObj The data port bean generated from the csv or xls file. 
	 */
	@Override
	public DataPortDTO convertBeanToDataPortDTO(DCTImport beanObj, Errors errors) throws DataAccessException {
		DataPortImport datImport = (DataPortImport) beanObj;
		DataPortDTO datDto = new DataPortDTO();

		Long itemId = itemDAO.getItemId(datImport.getItemName(), datImport.getItemLocation());

		if (null == itemId) {
			Object[] errorArgs = { };
			errors.rejectValue("item", "Import.itemNotFound", errorArgs, "Cannot find item");
			return null;
		}

		Long portId = dataPortDAO.getPortId(datImport.getItemLocation(), datImport.getItemName(), datImport.getPortName());
		
		datDto.setItemId(itemId);
		datDto.setPortId(portId);
		
		// port id shall be available for edit operation
		if (null != operation && 
				(
						operation.equals(ImportProcessorImpl.EDIT_OPERATION) ||
						operation.equals(ImportProcessorImpl.DELETE_OPERATION)
				)
				&& null == portId) {
			Object[] errorArgs = { };
			errors.rejectValue("port", "Import.DataPort.InvalidPortType", errorArgs, "Port not found");
			return null;
		}
		
		if (null != datImport.getPortType()) {
			
			datDto.setPortSubClassLksDesc(datImport.getPortType());
			
			try {
				LksData portSubclassLks = lksCache.getLksDataUsingLkpAndType(datImport.getPortType(), SystemLookup.LkpType.PORT_SUBCLASS);
				if (null != portSubclassLks) {
					datDto.setPortSubClassLksValueCode(portSubclassLks.getLksId());
				}
			}
			catch (DAORuntimeException ex) {
				
				Object[] errorArgs = { };
				errors.rejectValue("port", "Import.PortImport.InvalidPortType", errorArgs, "Port Type not available");
				return null;
			}
			
		}
		
		if (null != operation && operation.equals(ImportProcessorImpl.EDIT_OPERATION)) {
			datDto.setPortName((null != datImport.getNewPortName() && datImport.getNewPortName().length() > 0) ? datImport.getNewPortName() : datImport.getPortName());
		}
		else {
			datDto.setPortName(datImport.getPortName());	
		}
		
		if (null != datImport.getPortName()){
			if(datImport.getPortName().trim().length()>64){
				Object[] errorArgs = { };
				errors.rejectValue("port", "Import.DataPort.DataPortNameLength", errorArgs, "The length of port name is limited");
				return null;
			}
		}
		
		if (null != datImport.getItemName()) datDto.setItemName(datImport.getItemName());
		
		if (null != datImport.getProtocol()){
			datDto.setProtocolLkuDesc(datImport.getProtocol());
		}
		
		if (null != datImport.getDataRate()) 	datDto.setSpeedLkuDesc(datImport.getDataRate());
		
		if (null != datImport.getConnector()) 	datDto.setConnectorName(datImport.getConnector());
		
		if (null != datImport.getMedia()) 	datDto.setMediaLksDesc(datImport.getMedia());
		
		if (null != datImport.getItemLocation()) 	datDto.setLocation(datImport.getItemLocation());
		
		if (null != datImport.getIndex()) datDto.setSortOrder(datImport.getIndex());
		
		if (null != datImport.getColorCode()) datDto.setColorLkuDesc(datImport.getColorCode());
		
		if (null != datImport.getGroupingVLan()) datDto.setVlanLkuDesc(datImport.getGroupingVLan());
		
		if (null != datImport.getMacAddress()) datDto.setMacAddress(datImport.getMacAddress());
		
		if (null != datImport.getIpAddress()) datDto.setIpAddressImport(datImport.getIpAddress());
		
		if (null != datImport.getProxyIndex()) datDto.setProxyIndex(datImport.getProxyIndex());
		
		if (null != datImport.getSnmpCommunity()) datDto.setCommunityString(datImport.getSnmpCommunity());
		
		if (null != datImport.getComment()) datDto.setComments(datImport.getComment());
		
		return datDto;
		
	}

	
	
	

}
