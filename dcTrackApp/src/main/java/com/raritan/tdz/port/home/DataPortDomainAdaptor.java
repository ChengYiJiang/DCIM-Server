package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.ip.dao.IPAddressDAO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

public class DataPortDomainAdaptor implements ValueIDFieldToDomainAdaptor, PortDomainAdaptor {

	@Autowired
	private UtilHome utilHome;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired(required=true)
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;
	
	@Autowired(required=true)
	private IPAddressDAO ipAddressDAO;

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	private ResourceBundleMessageSource messageSource;
	
	public ResourceBundleMessageSource getMessageSource() {
        return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
	}

	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, DataAccessException {
		if (null == dbObject || null == valueIdDTO) {
			return null;
		}
		convertDataPortsForAnItem(dbObject, valueIdDTO);
		return null;
	}
	
	public boolean validateDto(Item item, List<DataPortDTO> dataPortDTOList) {
		long itemId = item.getItemId();
		boolean invalidArg = false;
		
		for (DataPortDTO dataPortDTO: dataPortDTOList) {
			if ((itemId <= 0 && dataPortDTO.getItemId() > 0) ||
					(itemId > 0 && dataPortDTO.getItemId() <= 0)) {
				invalidArg = true;
				break;
			}
			else if ((itemId > 0 && dataPortDTO.getItemId() > 0) &&
					(itemId != dataPortDTO.getItemId())) {
				invalidArg = true;
				break;
			}
		}
		if (invalidArg) {
			throw new InvalidPortObjectException("Invalid port dto: port id and item ids are not correct");
		}
		return !invalidArg;
	}


	public IPortInfo convertOnePortForAnItem( Object itemObj, Long portId, PortInterface dto) throws DataAccessException{
		Item item = (Item)itemObj;
		DataPortDTO dataPortDTO = (DataPortDTO) dto;
		if (dataPortDTO == null) return null;
		
		java.util.Date date= new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		
		DataPort dp = null;
		if( portId == null || portId.longValue() <= 0){
			dp = PortsAdaptor.adaptDataPortDTOToNewItemDomain(item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);
			dp.setCreationDate(timeStamp);
			dp.setPortId(null);
			item.addDataPort(dp);
		}else{
			Set<DataPort> dpSet = item.getDataPorts();
			DataPort idp = null;
			for (DataPort editdp: dpSet) {
				if (editdp.getPortId() != null && editdp.getPortId().longValue() == portId.longValue()) {
					idp = editdp;
					break;
				}
			}
			if (idp != null ) {
				dp = PortsAdaptor.updateDataPortDTOToDomain(idp, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);
				dp.setUpdateDate(timeStamp);
			}
			else  Assert.isTrue(false);
		}
		return dp;
	}
	
	/**
	 * sets the snmp community string to the given dataport and reset from the rest
	 * @param dp
	 * @param dataPortWithSnmp
	 * @param snmpV2CommString
	 * @return
	 */
	private boolean setSnmpCommStr(DataPortDTO dataPortDTO, String dataPortWithSnmp, String snmpV2CommString) {
		
		boolean found = false;
		
		if (null == dataPortWithSnmp || dataPortWithSnmp.length() == 0) {
			return found;
		}
		
		if (dataPortDTO.getPortName().equals(dataPortWithSnmp)) {
			found = true;
			
			if (null != snmpV2CommString) {
				dataPortDTO.setCommunityString(snmpV2CommString);
			}
		}
		
		return found;
	}
	
	private void convertDataPortsForAnItem(Object itemObj, ValueIdDTO dto) throws ClassNotFoundException, DataAccessException, BusinessValidationException {
		Item item = (Item)itemObj;

		String dataPortWithSnmp = item.getDataPortNameWithSnmp();
		String snmpV2CommString = item.getSnmpV2CommunityString();
		boolean portFoundToSetCommStr = false;
		
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) dto.getData();

		if (!validateDto(item, dataPortDTOList)) {
			return;
		}
		/* Delete all the data ports from the database that are not in the DTOs */
		deleteDataPortsNotInDTO(item, dataPortDTOList);

		boolean updateFreeDataPort = false;
		java.util.Date date= new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		int freeDataPortCount = 0;
		for (DataPortDTO dataPortDTO: dataPortDTOList) {
			DataPort dp = null;
			
			if (!portFoundToSetCommStr) {
				portFoundToSetCommStr = setSnmpCommStr(dataPortDTO, dataPortWithSnmp, snmpV2CommString);
			}
			
			/* New Data port */
			if (dataPortDTO.getPortId() == null || dataPortDTO.getPortId() <= 0) {
				dp = PortsAdaptor.adaptDataPortDTOToNewItemDomain(item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);
				dp.setCreationDate(timeStamp);
				dp.setPortId(null);
				item.addDataPort(dp);
				/* The free data port count is limited for new item. 
				 * For an existing item  input port count will be updated via the trigger. 
				 * TODO:: This change is for 3.0 to limit the number of changes. Once the trigger starts working this code should be removed */
				if (item.getItemId() <= 0) {
					updateFreeDataPort = true;
					freeDataPortCount++;
				}
			}
			/* Edit ports */
			else {
				Set<DataPort> dpSet = item.getDataPorts();
				DataPort idp = null;
				for (DataPort editdp: dpSet) {
					if (null != editdp.getPortId() && editdp.getPortId().longValue() == dataPortDTO.getPortId().longValue()) {
						idp = editdp;
						break;
					}
				}
				if (null != idp) {
					dp = PortsAdaptor.updateDataPortDTOToDomain(idp, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);
					dp.setUpdateDate(timeStamp);
				}
				else {
					/* FIXME:: should never this point of the code */
					DataPort dataPort = null; // (DataPort) session.get(DataPort.class, dataPortDTO.getPortId());
					/* New port for existing item */
					if (null == dataPort) {
						/* For a existing item, new data port create request, create the data port */
						dp = PortsAdaptor.adaptDataPortDTOToNewItemDomain(item, dataPortDTO, itemDAO, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO, ipAddressDAO);
						dp.setCreationDate(timeStamp);
						item.addDataPort(dp);
					}
				}
			}
		}

		// update free data port count
		if (updateFreeDataPort) {
			item.setFreeDataPortCount(freeDataPortCount);
		}
		
		
		// The data port that required to have community string is set then
		// reset the values of data port name that shall have community string to null
		// If it was not set then we need to pick the data port with minimum index value
		// and set the community string. Since the index values are corrected by the server
		// during the pre-save, we cannot make a decision here as to which port will
		// have minimum index value
		if (portFoundToSetCommStr) {
			item.setDataPortNameWithSnmp(null);
			item.setSnmpV2CommunityString(null);
		}
		else {

			// If user provided the port to use to set the snmp and the port do not exist, throw an exception
			if (null != dataPortWithSnmp && dataPortWithSnmp.length() > 0) {
				
				Errors errors = businessExceptionHelper.getErrorObject(Item.class);
				
				Object[] errorArgs = { item.getItemName(), dataPortWithSnmp };
				errors.rejectValue("port", "PortValidator.portDoesNotExistInItem", errorArgs, "Port not found");
				businessExceptionHelper.throwBusinessValidationException(null, errors, null);
			}
		}
		
	}
	
	private void deleteDataPortsNotInDTO(Item item, List<DataPortDTO> dataPortDTOList) throws BusinessValidationException {
		if (item.getItemId() <= 0) {
			return;
		}
		
		List<Long> portIds = new ArrayList<Long>();
		for (DataPortDTO dto: dataPortDTOList) {
			portIds.add(dto.getPortId());
		}
		
		Set<DataPort> sList = item.getDataPorts();
		List<Long> delPortIds = new ArrayList<Long>();
		for (DataPort pp: sList) {
			if (!portIds.contains(pp.getPortId())) {
				delPortIds.add(pp.getPortId().longValue());
			}
		}
		for (Long delPortId: delPortIds) {
			deletePort(item, delPortId.longValue());
		}
	}

	@Override
	public void deletePort(Item item, long dataPortId) throws BusinessValidationException {
		// 1. Port should not be in use. 
		// 2. If input port is deleted all corresponding output ports should be deleted 
		Set<DataPort> dpSet = item.getDataPorts();
		DataPort dp = null;
		Iterator<DataPort> itr = dpSet.iterator();
		while (itr.hasNext()) {
			dp = itr.next();
			if (null != dp.getPortId() && dp.getPortId().longValue() == dataPortId) {
				itr.remove();
			}
		}
	}

	@Override
	public void deleteOnePortForAnItem(Item item, PortInterface dto)
			throws BusinessValidationException {

		DataPortDTO dataPortDTO = (DataPortDTO) dto;
		
		deletePort(item, dataPortDTO.getPortId());
		
	}

}
