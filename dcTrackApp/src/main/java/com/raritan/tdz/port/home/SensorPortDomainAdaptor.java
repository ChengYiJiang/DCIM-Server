package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.dto.SensorPortDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

public class SensorPortDomainAdaptor implements ValueIDFieldToDomainAdaptor{

	@Autowired
	private ItemHome itemHome;

	@Autowired
	private UtilHome utilHome;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired(required=true)
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired(required=true)
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;
	
	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, DataAccessException {
		if (null == dbObject || null == valueIdDTO) {
			return null;
		}
		convertSensorPortsForAnItem(dbObject, valueIdDTO);
		return null;
	}

	private void convertSensorPortsForAnItem(Object itemObj, ValueIdDTO dto) throws BusinessValidationException, DataAccessException {
		Item item = (Item)itemObj;
		
		@SuppressWarnings("unchecked")
		List<SensorPortDTO> sensorPortDTOList = (List<SensorPortDTO>) dto.getData();

		if (!validateDto(item, sensorPortDTOList)) {
			return;
		}
		// Delete all the data ports from the database that are not in the DTOs 
		deleteSensorPortsNotInDTO(item, sensorPortDTOList);

		//boolean updateFreeDataPort = false;
		java.util.Date date= new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		
		for (SensorPortDTO sensorPortDTO: sensorPortDTOList) {
			SensorPort sp = null;
			//create sensor port
			if (sensorPortDTO.getPortId() == null || sensorPortDTO.getPortId() <= 0) {
				sp = PortsAdaptor.adaptSensorPortDTOToNewItemDomain(item, sensorPortDTO, itemHome, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
				sp.setCreationDate(timeStamp);
				sp.setPortId(null);
				item.addSensorPort(sp);
				
			}
			//edit existing sensor port
			else {
				Set<SensorPort> spSet = item.getSensorPorts();
				for (SensorPort editsp: spSet) {
					if (null != editsp.getPortId() && editsp.getPortId().longValue() == sensorPortDTO.getPortId().longValue()) {
						sp = PortsAdaptor.updateSensorPortDTOToDomain(editsp, sensorPortDTO, itemHome, utilHome, systemLookupFinderDAO, userLookupFinderDAO, connectorLookupFinderDAO);
						sp.setUpdateDate(timeStamp);
						break;
					}
				}
			}
		}
	}

	//FIXME: This is common method for Data/Power and Sensor Ports. Shoudl we extract it?
	public boolean validateDto(Item item, List<SensorPortDTO> sensorPortDTOList) {
		long itemId = item.getItemId();
		boolean invalidArg = false;
		
		for (SensorPortDTO sensorPortDTO: sensorPortDTOList) {
			if ((itemId <= 0 && (sensorPortDTO.getItemId() == null || sensorPortDTO.getItemId() > 0)) ||
					(itemId > 0 && (sensorPortDTO.getItemId() == null || sensorPortDTO.getItemId() <= 0))) {
				invalidArg = true;
				break;
			}
			else if ((itemId > 0 && sensorPortDTO.getItemId() > 0) &&
					(itemId != sensorPortDTO.getItemId())) {
				invalidArg = true;
				break;
			}
		}
		if (invalidArg) {
			throw new InvalidPortObjectException("Invalid port dto: port id and item ids are not correct");
		}
		return !invalidArg;
	}
	
	private void deleteSensorPortsNotInDTO(Item item, List<SensorPortDTO> sensorPortDTOList) throws BusinessValidationException {
		if (item.getItemId() <= 0) {
			return;
		}
		
		List<Long> portIds = new ArrayList<Long>();
		for (SensorPortDTO dto: sensorPortDTOList) {
			portIds.add(dto.getPortId());
		}
		
		Set<SensorPort> portList = item.getSensorPorts();
		List<Long> delPortIds = new ArrayList<Long>();
		for (SensorPort port : portList) {
			if (!portIds.contains(port.getPortId())) {
				delPortIds.add(port.getPortId().longValue());
			}
		}
		for (Long delPortId: delPortIds) {
			deleteSensorPort(item, delPortId.longValue());
		}
	}

	private void deleteSensorPort(Item item, long sensorPortId) throws BusinessValidationException {
		Set<SensorPort> portsSet = item.getSensorPorts();
		SensorPort port = null;
		Iterator<SensorPort> itr = portsSet.iterator();
		while (itr.hasNext()) {
			port = itr.next();
			if (null != port.getPortId() && port.getPortId().longValue() == sensorPortId) {
				itr.remove();
				break;
			}
		}
	}

}
