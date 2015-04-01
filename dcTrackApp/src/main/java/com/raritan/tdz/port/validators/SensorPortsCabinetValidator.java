package com.raritan.tdz.port.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.home.SensorPortHelper;

public class SensorPortsCabinetValidator implements Validator {
	
	@Autowired
	private SensorPortHelper sensorPortHelper;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Item.class.equals(clazz);
	}

	//Validate client did send duplicate cabinets in case of asset strip sensors.
	//For other type of sensors it is ok
	@Override
	public void validate(Object target, Errors errors) {
		
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item)targetMap.get(errors.getObjectName());
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
		
		// Item item = (Item)target;
		Set<SensorPort> allSensors = item.getSensorPorts();
		if( allSensors == null || allSensors.size() == 0 ) return;
		
		Set<Long> allPotantalyUsedCabinets = new HashSet<Long>();
		Set<String> dupCabinets = new HashSet<String>();
		
		for ( SensorPort sensor : allSensors ){
			if( sensor.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP ){
				Long cabinetId = sensor.getCabinetItemId();
				if( cabinetId != null ){
					if( ! allPotantalyUsedCabinets.contains(cabinetId) ){
						allPotantalyUsedCabinets.add(cabinetId);
					}else{
						dupCabinets.add(sensor.getCabinetItem().getItemName());
					}
				}
			}
		}
		if(dupCabinets.size()  == 0 ){
			//validate none of requested cabinets has been used
			validateCabinetsAreNotUsed(item, allSensors, errors);
		}else{
			for( String dupCabinet : dupCabinets ){
				Object[] errorArgs = { dupCabinet };
				errors.rejectValue("tabSensorPorts", "PortValidator.cabinetAlreadyHasAssetStrip", errorArgs, 
						"Cabinet already has asset strip. Cannot place another one.");
			}
		}
	}
	
	/*
	 * From the set of sensors, return a map of those that are asset strip
	 * sensors and contain cabinets. Key is sensors id, value is cabinetId. 
	 */
	private Map<Long, Long> extractAssetStripSensorsWithCabinets(Set<SensorPort> sensorPorts){
		Map <Long, Long>retval = new HashMap<Long, Long>();
		for( SensorPort sp : sensorPorts ){
			Long cabinetId = sp.getCabinetItemId();

			if( cabinetId != null && cabinetId.longValue() > 0 &&
					sp.getPortSubClassLookup() != null &&
							sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP){
				retval.put(sp.getPortId(), cabinetId);
			}
		}
		return retval;
	}
	//Validate that none of requested cabinets for AssetStrip sensor is used
	private void validateCabinetsAreNotUsed(Item item, Set<SensorPort> sensorPorts, Errors errors) {
		Map <Long, Long>excludeSensorIdMap= extractAssetStripSensorsWithCabinets(sensorPorts);
		List<Long> excludeSensorIdList = null;
		//If this is edit, then exclude from search AssetStrip sensors that have cabinets
		if( item.getItemId() > 0 ) {
			excludeSensorIdList = new ArrayList<Long>(excludeSensorIdMap.keySet());
		}
		//get list of all available cabinets
		List<ValueIdDTO> cabinetsList = sensorPortHelper.getAvailableCabinetsForSensor(
				item.getDataCenterLocation().getCode(), 
				SystemLookup.PortSubClass.ASSET_STRIP, 
				excludeSensorIdList, null);

		//check if these asset strip sensor with cabinets are in the list
		Set<String> requestedAssetStripCabinets = getAssetStripCabinets(sensorPorts);
		Set<String> availableCabinets  = new HashSet<String>();
		for(ValueIdDTO dto : cabinetsList ){
			availableCabinets.add((String)dto.getLabel());
		}
		for( String rc : requestedAssetStripCabinets ){
			if( ! availableCabinets.contains(rc)){
				Object[] errorArgs = {rc};
				errors.rejectValue("tabSensorPorts", "PortValidator.cabinetAlreadyHasAssetStrip", errorArgs, 
					"Cabinet already has an asset strip.");
			}
		}

	}
	
	//From set of sensors, return those that are asset strip and belong to a cabinet
	private Set<String> getAssetStripCabinets(Set<SensorPort> sensorPorts) {
		Set <String>retval = new HashSet<String>();
		for( SensorPort sp : sensorPorts ){
			Item cabinet = sp.getCabinetItem();

			if( cabinet != null && cabinet.getItemName() != null &&
					sp.getPortSubClassLookup() != null &&
							sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP){
				retval.add(cabinet.getItemName());
			}
		}
		return retval;
	}
}
