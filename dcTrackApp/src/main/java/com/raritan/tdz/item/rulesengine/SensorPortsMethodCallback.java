package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.dto.SensorPortDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.UnitIntf;

public class SensorPortsMethodCallback implements RemoteRefMethodCallback {
	private ItemDAO itemDAO;
	
	private Map<Long, UnitConverterIntf> unitConverterMap;
	
	@Autowired
	private UnitIntf unit;
	
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
		Long itemId = (Long)filterValue;
		Item item = itemDAO.getItem(itemId);
	
		Set<SensorPort> itemSensorPorts = item.getSensorPorts();
		List<SensorPortDTO> sensorPortDTOList = new ArrayList<SensorPortDTO>();

		if (null != itemSensorPorts) {
			for(SensorPort sensorPort : itemSensorPorts ){
				UnitConverterIntf unitConverter = unitConverterMap.get(sensorPort.getPortSubClassLookup().getLkpValueCode());
				SensorPortDTO sensorPortDTO = PortsAdaptor.adaptSensorPortDomainToDTO(sensorPort, itemDAO, unitConverter, unit, additionalArgs);
				sensorPortDTOList.add(sensorPortDTO);
				
			}		
			Collections.sort(sensorPortDTOList, new Comparator<SensorPortDTO>(){
				  public int compare(SensorPortDTO s1, SensorPortDTO s2) {
				    return s1.getPortName().compareToIgnoreCase(s2.getPortName());
				  }
			});
		}

		uiViewComponent.getUiValueIdField().setValue(sensorPortDTOList);
	}
	
	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}

	public Map<Long, UnitConverterIntf> getUnitConverterMap() {
		return unitConverterMap;
	}

	public void setUnitConverterMap(Map<Long, UnitConverterIntf> unitConverterMap) {
		this.unitConverterMap = unitConverterMap;
	}

}
