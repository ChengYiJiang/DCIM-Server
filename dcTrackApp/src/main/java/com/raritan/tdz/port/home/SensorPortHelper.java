package com.raritan.tdz.port.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.SensorPortDAO;



public class SensorPortHelper {
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private SensorPortDAO sensorPortDAO;
	
	/**
	 * For all sensors but assert strip sensors return list of all cabinets
	 * For asset strip sensor return all cabinets that do not have asset strip sensor
	 * 
	 * @param sensorTypeLksValueCode - sensor type
	 * @return - return ValueIdDTO where label contains cabinet id and data cabinet name
	 */
	
	public List<ValueIdDTO> getAvailableCabinetsForSensor(String siteCode, Long sensorTypeLksValueCode, List<Long> excludeSensorId, Long includeCabinetId){
		//List<Item> allCabinets = itemDAO.getAllCabinets(siteCode);
		List<Map<String, Object>> cabinetItemDataList = itemDAO.getAllCabinetsIdNameForSiteCode(siteCode);
		
		HashMap<Long, String> cabinetsMap = new HashMap<Long, String>();
		
		List<Long> allCabinetId = new ArrayList<Long>();
		for (Map<String, Object> cabinet : cabinetItemDataList ){
			allCabinetId.add((Long)cabinet.get("itemId"));
			cabinetsMap.put((Long)cabinet.get("itemId"), (String)cabinet.get("itemName"));
		}
		
		//For Asset Strip sensor type we have to send list of all cabinets excluding those that
		//already have asset strip sensor
		if( sensorTypeLksValueCode == SystemLookup.PortSubClass.ASSET_STRIP){
			//get list of cabints that do have asset strip sensor
			List<Long> cabinetsWithAssetStripSensors = sensorPortDAO.getAllAssetStripSensorCabinets(siteCode, excludeSensorId);
		
			if(includeCabinetId != null && includeCabinetId > 0){
				for(Long x:cabinetsWithAssetStripSensors){
					if(x.equals(includeCabinetId) == false){
						allCabinetId.remove(x);
					}
				}
			}
			else{
				allCabinetId.removeAll(cabinetsWithAssetStripSensors);		
			}
		}
		
		List<ValueIdDTO> retval = new ArrayList<ValueIdDTO>();
		for( Long cabinetId :  allCabinetId){
			ValueIdDTO retvalDTO = new ValueIdDTO();
			retvalDTO.setLabel(cabinetsMap.get(cabinetId));
			retvalDTO.setData(cabinetId);
			retval.add(retvalDTO);
		}
		return retval;
		
	}
}
