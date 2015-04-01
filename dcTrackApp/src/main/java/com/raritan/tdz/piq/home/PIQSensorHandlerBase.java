package com.raritan.tdz.piq.home;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.SensorBase;
import com.raritan.tdz.port.dao.SensorPortDAO;
import com.raritan.tdz.port.dao.SensorPortFinderDAO;
import com.raritan.tdz.port.home.PortNameUniquenessValidator;
import com.raritan.tdz.util.UnitConverterLookup;


public abstract class PIQSensorHandlerBase implements PIQSensorHandler{
	
	@Autowired
	protected SystemLookupFinderDAO systemLookupFinder;

	@Autowired
	protected SensorPortFinderDAO sensorPortFinder;
	
	@Autowired
	protected SensorPortDAO sensorPortDao;
	
	@Autowired
	protected EventHome eventHome;
	
	@Autowired
	protected PortNameUniquenessValidator portNameUniquenessValidator;
	
	@Autowired
	protected MessageSource messageSource;
	
	@Autowired
	PIQProbeLookup probeLookup;
	
	private List<String> validationWarningCodes;
	
	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}
	
	protected void setSensorName(SensorPort sp, Item item, String sensorName, String attributeName, int ordinal, Errors errors) {

		// set sensor port name
		String name = sensorName;
		if (name == null || name.length() == 0) {
			name = generateSensorName(attributeName, ordinal);
		}

		String itemName = item.getItemName();
		String portName = sp.getPortName();
		if (portName != null && !portName.equals(name)) {
			/* port name changed, report event */
			String code = "piqSync.sensorRename";
			Object[] args = { itemName, sp.getPortName(), sp.getSortOrder(), name };
			errors.rejectValue("SyncSensor", code, args, "Port name mismatch.");
			String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());
			
			AddSensorEvent(itemName, name, sp.getPortName(), sp.getSortOrder(), EventType.SENSOR_UPDATE, EventSeverity.INFORMATIONAL, evtSummary, null);
		}
		
		// port name uniqueness verification and corrections is done
		// on sensors received form PIQ. No check is done here.
		sp.setPortName(name);
	}

	protected void setSensorPortData(SensorPort sp, Item item, SensorBase sb, Errors errors) {
		
		// set item
		if (sp.getItem() == null) sp.setItem(item);

		Long portSubClass = UnitConverterLookup.sensorSubClass.get(sb.getAttributeName());

		// set subClass
		if (sp.getPortSubClassLookup() == null && portSubClass != null) {
			sp.setPortSubClassLookup(systemLookupFinder.findByLkpValueCode(portSubClass).get(0));
		}
	}
	
	protected String generateSensorName(String sensorAttribute, Integer ordinal) {
		StringBuffer portNameBuf = new StringBuffer(sensorAttribute);
		portNameBuf.append(" ");
		portNameBuf.append(ordinal.toString());
		return portNameBuf.toString();
	}
 	
	private boolean setSensorData (SensorPort sp, Item item, SensorBase sb, Errors errors) {
		sp.setSortOrder(sb.getOrdinal());
		setSensorSpecificData(sp, item, sb, errors);
		setSensorPortData(sp, item, sb, errors);
		sp.setPiqId(sb.getId());
		return true;
	}

	abstract protected void setSensorSpecificData(SensorPort sp, Item item, SensorBase sb, Errors errors); 
	abstract protected Long getPortSubClass(SensorBase sb);
	
	private Timestamp getCurrentTS() {
		return new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
	}
	
	private SensorPort findPort(Item item, SensorBase sb, long subClass) {
		SensorPort sensorPort = null;
		Set<SensorPort> sSet = item.getSensorPorts();
		
		for (SensorPort sp: sSet) {
			if ((sp.getSortOrder() == sb.getOrdinal() &&
					sp.getPiqId() == sb.getId() && 
					sp.getPortSubClassLookup().getLkpValueCode().longValue() == subClass) ||
					(sp.getSortOrder() == sb.getOrdinal() &&
					sp.getPortSubClassLookup().getLkpValueCode().longValue() == subClass)) {					
				sensorPort = sp;
				break;
			}
		}
		return sensorPort;
	}
	
	@Override
	public void AddOrUpdate (Item item, SensorBase sb, Errors errors) {
		/* find sensor port in dcTrack that matches (itemId, piqId, sortOrder and portSubclass) or 
		 * (itemId, sortOrder and portSubclass) from discovered sensor 'sb'.
		 */
		SensorPort sp = findSensorPort(item, sb, getPortSubClass(sb)); //findPort(item, sb, getPortSubClass(sb));
		if (sp != null) {
			// found existing sensor port, update new data
			if (setSensorData (sp, item, sb, errors) == true) {
				sp.setUpdateDate(getCurrentTS());
			}
		} else {
			// new sensor, add to port list 
			sp = new SensorPort();
			if (setSensorData(sp, item, sb, errors) == true) {
				sp.setCreationDate( getCurrentTS());
				
				String code = "piqSync.sensorAdd";
				String itemName = item.getItemName();
				Object[] args = { itemName, sp.getPortName(), sp.getSortOrder()};
				errors.rejectValue("Sensor", code, args, "Sensor Add");
				
				item.getSensorPorts().add(sp);
				
				String evtSummary = messageSource.getMessage(code, args, Locale.getDefault());
				AddSensorEvent(itemName, sp.getPortName(), null, sp.getSortOrder(), EventType.SENSOR_UPDATE, EventSeverity.INFORMATIONAL, evtSummary, null);
			} 
			else {
				sp = null;
			}
		}
	}
	
	public boolean canProcess(SensorBase sb) {
		Sensor s = (Sensor)sb;
		/* process sensor only if not removed */
		return (s != null && s.getRemoved() == null);
	}
	
	protected  SensorPort findSensorPort(Item item, SensorBase s, Long subclass) {
        Set<SensorPort> sensorPorts = item.getSensorPorts(); 
		if (sensorPorts != null) {
			for (SensorPort sp : sensorPorts) {
				if (sp != null && sp.getSortOrder() == s.getOrdinal() && 
						/*sp.getPiqId().longValue() == s.getId() && */ 
						(sp.getPortSubClassLookup() != null && 
						sp.getPortSubClassLookup().getLkpValueCode().longValue() == subclass.longValue())) {
					return sp;
				}
			}
		}
		return null;
	}
	
	protected void AddSensorEvent(String itemName, String sensorName, String oldName, Integer sortOrder, EventType type, EventSeverity severity, String summary, String uom) {
		try {
			Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
			Event ev = eventHome.createEvent(createdAt, type, severity, "dcTrack");
			ev.setSummary(summary);
			ev.addParam("Item Name", itemName);
			if (oldName != null && !sensorName.equals(oldName)) {
				ev.addParam("Old Name", oldName);
			}
			ev.addParam("New Name", sensorName);
			ev.addParam("Index Number", sortOrder.toString());
			if (uom != null && uom.length() > 0) {
				ev.addParam("UOM", uom);
			}
			eventHome.saveEvent(ev);
		} catch (DataAccessException e) {
			// TODO: ignore this ???
		}
	}
	
	private boolean isPortNameUnique (Long itemId, Long sensorSubclass, int ordinal, String name, Errors errors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult nameErrors = new MapBindingResult(errorMap, SensorPort.class.getName());

		/* findPorts will get sensorPorts for a given sensorSubclass excluding sensor at a given ordinal */
		List<SensorPort> sensorPorts = (List<SensorPort>)sensorPortFinder.findPorts (itemId, sensorSubclass, ordinal);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ports", (Object)sensorPorts);
		map.put("name", (Object)name);
		portNameUniquenessValidator.validate(map, nameErrors);

		if (nameErrors.hasErrors()) {
			errors.addAllErrors(nameErrors);
			return false;
		}
		return true;
	}
}
