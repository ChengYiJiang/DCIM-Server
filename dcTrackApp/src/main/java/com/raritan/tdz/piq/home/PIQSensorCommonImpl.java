package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.AssetStrip;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.util.UnitConverterLookup;

public class PIQSensorCommonImpl implements PIQSensorCommon {
	
	@Autowired
	private EventHome eventHome;
	
	@Autowired
	private SensorEventsHelper sensorEventsHelper;
	
	@Autowired
	private PIQSensorUtil piqSensorUtil;

	@Autowired
	private MessageSource messageSource;
	
	private static Logger log = Logger.getLogger("PIQSensorCommon");
	
	@Override
	public void deleteSensorsNotInSync(Item item, List<Sensor> sensors, Errors errors) throws DataAccessException {

		if (item == null) {
			if (log.isDebugEnabled()) {
				log.debug("Item is null, cannot delete sensors");
			}
			return;
		}
		
		Set<SensorPort>  spSet = item.getSensorPorts();
		if (spSet == null) {
			if (log.isDebugEnabled()) {
				log.debug("Item's  sensor ports list is null");
			}
			return;
		}

		List<SensorPort> sensorsForDeletion = new ArrayList<SensorPort>();

		for (SensorPort sp: spSet) {
			boolean matched = false;
			for (Sensor sensor: sensors) {
				Long sensorClass = UnitConverterLookup.sensorSubClass.get(sensor.getAttributeName());
				if ( sp.getSortOrder() == sensor.getOrdinal() && sp.getPiqId() != null && 
					 sp.getPiqId().longValue() == sensor.getId() &&  
					(sp.getPortSubClassLookup() != null && sensorClass != null &&
					 sp.getPortSubClassLookup().getLkpValueCode().longValue() == sensorClass.longValue()) &&
					sensor.getRemoved() == null) { 
					matched = true;
					break;
				} 
			}

			/* remove sensor port that did not match any of discovered sensor and is not asset strip*/
			if (!matched && ! (sp.getPortSubClassLookup() != null && sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP)) {
				piqSensorUtil.deleteUnsyncSensorPortsEvent(item, sp, errors);
				sensorsForDeletion.add(sp);
			}
   		}
		piqSensorUtil.removeSensors(item, sensorsForDeletion);
	}
	
	@Override
	public void deleteAllSensors(Item item, Errors errors ) throws DataAccessException {
		/*
		Set<SensorPort>  spSet = item.getSensorPorts();
		for (SensorPort sp: spSet) {
			deleteUnsyncSensorPortsEvent(item, sp, errors);
		}*/
		item.removeAllSensorPorts();
	}
	
	@Override
	public void deleteAssetStripNotInSync(Item item, List<AssetStrip> sensors, Errors errors) throws DataAccessException {
		List<SensorPort> sensorsForDeletion = new ArrayList<SensorPort>();

		Set<SensorPort>  spSet = item.getSensorPorts();
		for (SensorPort sp : spSet) {
			boolean matched = false;
			for (AssetStrip sensor: sensors) {
				if ( (sp.getSortOrder() == sensor.getOrdinal() &&   sp.getPiqId() != null && 
						sp.getPiqId().longValue() == sensor.getId() && 
					sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP && 
					sensor.getAttributeName() == null && 
					sensor.getState().equals("available")) ) { 
					matched = true;
					break;
				} 
			}
			/* remove sensor port that did not match any of discovered sensor */
			if (!matched && sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP)  {
				piqSensorUtil.deleteUnsyncSensorPortsEvent(item, sp, errors);
				sensorsForDeletion.add(sp);
			}
   		}
		piqSensorUtil.removeSensors(item, sensorsForDeletion);
	}

	@Override
	public void deleteAllAssetStripSensors(Item item, Errors errors) throws DataAccessException {
		List<SensorPort> sensorsForDeletion = new ArrayList<SensorPort>();
		Set<SensorPort>  spSet = item.getSensorPorts();
		for (SensorPort sp : spSet) {
			/* sp.getPiqId() == sensor.getId() && */ 
			if (sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.ASSET_STRIP) {
				sensorsForDeletion.add(sp);
			}
   		}
		if (sensorsForDeletion.size() > 0) {
			piqSensorUtil.removeSensors(item, sensorsForDeletion);
		}
	}
	
	@Override
	public void deleteAllSensors(Item item, long excludeSubClass, Errors errors)throws DataAccessException {
		List<SensorPort> sensorsForDeletion = new ArrayList<SensorPort>();
		Set<SensorPort>  spSet = item.getSensorPorts();
		for (SensorPort sp : spSet) {
			/* sp.getPiqId() == sensor.getId() && */ 
			if (sp != null && sp.getPortSubClassLookup()!= null && 
					(sp.getPortSubClassLookup().getLkpValueCode() != excludeSubClass)) {
				sensorsForDeletion.add(sp);
			}
   		}
		if (sensorsForDeletion.size() > 0) {
			piqSensorUtil.removeSensors(item, sensorsForDeletion);
		}
		
	}

}
