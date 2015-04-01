package com.raritan.tdz.piq.home;

 import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.Sensor;
import com.raritan.tdz.piq.json.SensorJSON;
import com.raritan.tdz.piq.json.SensorsJSON;
import com.raritan.tdz.port.home.PortNameUniquenessValidator;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author basker
 */
public class PIQSyncSensorClientImpl extends PIQSyncBase implements PIQSyncSensorClient, PIQSyncPorts {

	private Map<String, PIQSensorProcessor> sensorProcessors;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	protected PortNameUniquenessValidator portNameUniquenessValidator;

	@Autowired
	private PIQProbeLookup probeLookup;
	
	@Autowired
	private PIQSensorCommon piqSensorCommon;
	
	@Autowired
	private SensorEventsHelper sensorEventsHelper;
	
	@Autowired
	private PIQSensorUtil piqSensorUtil;
	
	List<SensorPortEventDetails> speEventList = null;
	
	@Autowired
	private ItemFinderDAO itemfinderDao;

	public class SensorPortEventDetails {
		public long id;
		public String sensorName;
		public String sensorType;
		public int sortOrder;
		public String errCause;
	}

	public Map<String, PIQSensorProcessor> getSensorProcessors() {
		return sensorProcessors;
	}

	public void setSensorProcessors(Map<String, PIQSensorProcessor> sensorProcessors) {
		this.sensorProcessors = sensorProcessors;
	}

	public PIQSyncSensorClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	@Override
	public Sensor findSensor(long pduPiqId, String address, int sensorNumber) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return null;
		Sensor sensor = null;
		int sensorId = sensorNumber > 0 ? sensorNumber : 1;
		
		// Find PDU sensor ID based for the sort order of the power
		StringBuffer url = new StringBuffer();
		url.append( Long.toString(pduPiqId) );
		url.append("/sensors?sensor_id_eq=");
		url.append( sensorId );
		url.append("&&attribute_name_eq=");
		url.append(address.toUpperCase());
		
		try {
			ResponseEntity<?> resp = doRestGet(getRestURL("v2/pdus", url.toString()), getHttpHeaders(), eventSource, SensorsJSON.class);
			if (resp != null) {
				SensorsJSON body = (SensorsJSON)resp.getBody();
				if (body != null) {
					List<Sensor> sensors = body.getSensors();
					if (sensors != null && !sensors.isEmpty()) {
						sensor = sensors.get(0);
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get sensors for PDU", e);
			throw e;
		}
		
		return sensor;
	}

	@Override
	public Sensor getSensor(long sensorId) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return null;
		Sensor sensor = null;
		
		try {
			ResponseEntity<?> resp = doRestGet(Long.toString(sensorId), SensorJSON.class);
			if (resp != null) {
				SensorJSON body = (SensorJSON)resp.getBody();
				if (body != null) {
					sensor = body.getSensor();
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get sensors for PDU", e);
			throw e;
		}
		
		return sensor;
	}
	
	@Override
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException, RemoteDataAccessException  {
		for (Item item: items) {
			syncSensors(null, item, errors);
		}
	}
	
	
	@Override
	public void syncAllPortReadings(int chunkLimit, Errors errors) throws DataAccessException,
			RemoteDataAccessException {
		syncAllSensors(chunkLimit,errors);
	}
	
	
	/**
	 * Iterate through the sensor ports of the "linked" PDU item and map to outlets as necessary. 
	 * @param pduItem
	 * @throws BusinessValidationException 
	 */
	@Override
	@Transactional
	public void linkSensorPorts(Item probeItem, Item pduItem) {
		try {
			//TODO verify what to do with errors.
			MapBindingResult errors = getErrorObject(); 
			syncSensors (probeItem, pduItem, errors);
		} catch (RemoteDataAccessException e) {
			// Is this okay ???
			log.error("", e);
		} catch (DataAccessException e) {
			// Is this okay ???
			log.error("", e);
		}
	}

	private List<Sensor> getAllSensors(List<Integer> pduPiqIds) throws RemoteDataAccessException {
		List<Sensor> sensors = new ArrayList<Sensor>();

		if (!isAppSettingsEnabled()) return null;
		if (pduPiqIds == null) return null;
		
		String service = "v2/pdus";
		try {
			for (Integer piqId: pduPiqIds) {
				if (piqId != null) {
					StringBuffer path = new StringBuffer(piqId.toString()).append("/sensors");  
					ResponseEntity<?> resp = doRestGet(service, path.toString(), SensorsJSON.class);
					if (resp != null) {
						SensorsJSON body = (SensorsJSON)resp.getBody();
						if (body != null)  {
							// accumulate sensors of each pdu
							List<Sensor> pdusensors = body.getSensors();
							if (pdusensors != null) sensors.addAll(pdusensors);
						}
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get sensors", e);
			throw e;
		}
		return sensors;
	}

	private List<Sensor> getAllSensorsPduPiqId(Integer pduPiqId) throws DataAccessException, RemoteDataAccessException {
		List<Integer> pduPiqIds = new ArrayList<Integer>();
		pduPiqIds.add(pduPiqId);
		return getAllSensors(pduPiqIds);
	}
	
	private MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, SensorPort.class.getName());
		return errors;
	}

	private void syncSensors(Item probeItem, Item item, Errors errors) throws DataAccessException, RemoteDataAccessException {
		if (item == null) {
			if (log.isDebugEnabled()) {
				log.debug("[syncSensors]: Item is null, cannot sync sensors");
			}
			return;
		}
		if (probeItem == null) {
			probeItem = probeLookup.getProbeItemForDummyRackPDU( item.getItemId() );
		}
		Item itemToProcess = probeItem != null ? probeItem : item;
		
		/* get sensors for this item from PIQ */
		List<Sensor> sensors = getAllSensorsPduPiqId(item.getPiqId());

		if (sensors == null || sensors.isEmpty()) {
			/* nothing to sync, remove existing sensor for this item */
			if (log.isDebugEnabled()) {
				log.debug("[syncSensors]: No sensors returned from PIQ for item so, deleting sensors");
			}
			// delete all sensors except asset strips
			piqSensorCommon.deleteAllSensors(itemToProcess, SystemLookup.PortSubClass.ASSET_STRIP, errors );
			return; 
		}

		/* check whether sensors from have distinct sort order */
		piqSensorUtil.isSorOrderDistinct(itemToProcess, sensors, errors);
		
		if (errors.hasErrors()) return;

		/* correct sensor names if needed */
		piqSensorUtil.correctSensorNames(itemToProcess, sensors, errors);
		
		/* delete sensors not in sync */
		piqSensorCommon.deleteSensorsNotInSync(itemToProcess, sensors, errors);
		
		updateSensorData(itemToProcess, sensors, errors);

	}
	
	private void updateSensorData(Item itemToProcess, List<Sensor> sensors, Errors errors) throws DataAccessException {
		/* For each sensors from PIQ, update sensor data */
		for (Sensor sensor: sensors) {
			if (sensor != null) {
				/* find the sensor processor that can process this request and call process */
				PIQSensorProcessor sp = sensorProcessors.get(sensor.getAttributeName());
				if (sp != null) {
					sp.process(itemToProcess, sensor, errors);
				}
				else {
					/* discovered unknown sensor report event */
					String code = "piqSync.invalidSensorResponse";
					Object[] args = { itemToProcess.getItemName(), sensor.getLabel(), sensor.getOrdinal() };
					errors.rejectValue("SyncSensor", code, args, "Invalid sensor type provide by Power IQ");
					String evtSummary = messageSource.getMessage(code,args, Locale.getDefault());
					sensorEventsHelper.AddInvalidSensorEvent(itemToProcess, sensor, evtSummary);
				}
			}
		}
	}

	private void syncAllSensors(int limit, Errors errors) throws DataAccessException, RemoteDataAccessException {
		/* get sensors for this item from PIQ */
		List<Sensor> sensors = getAllSensors(limit);

		Map<Integer, List<Sensor>> map = getSensorsByPduId (sensors);
		
		for (Map.Entry<Integer, List<Sensor>> entry : map.entrySet()) {
			List<Item> items = itemfinderDao.findItemByPIQId(entry.getKey(), 
					appSettings.getPowerIQHost(), SystemLookup.ApplicationSettings.PIQ_IPADDRESS);
			
			if (entry.getValue() != null && items !=null && items.size()> 0) {
				processSensorData(items.get(0), entry.getValue(), errors); 
			}
		}
	}
		
	void processSensorData(Item itemToProcess, List<Sensor> sensors, Errors errors) throws DataAccessException {
		
		/* check whether sensors from have distinct sort order */
		piqSensorUtil.isSorOrderDistinct(itemToProcess, sensors, errors);
		
		if (errors.hasErrors()) return;

		/* correct sensor names if needed */
		piqSensorUtil.correctSensorNames(itemToProcess, sensors, errors);
		
		/* delete sensors not in sync */
		piqSensorCommon.deleteSensorsNotInSync(itemToProcess, sensors, errors);
		
		updateSensorData(itemToProcess, sensors, errors);

	}
	
	Map<Integer, List<Sensor>> getSensorsByPduId (List<Sensor> sensors) {
		Map<Integer, List<Sensor>> map = new HashMap <Integer, List<Sensor>> ();
		for (Sensor s : sensors) {
			List<Sensor> pduSensors = map.get(new Integer((int)s.getPduId()));
			if (pduSensors == null) {
				List<Sensor> sList = new ArrayList<Sensor>();
				sList.add(s);
 				map.put(new Integer((int)s.getPduId()), sList);
			} else {
				pduSensors.add(s);
			}
		}
		return map;
	}

	private List<Sensor> getAllSensors(int limit) throws RemoteDataAccessException {
		List<Sensor> sensors = new ArrayList<Sensor>();
		ResponseEntity<?> result = doRestGet("", SensorsJSON.class);
		if (result != null){
			SensorsJSON json = (SensorsJSON)result.getBody();
			sensors = json.getSensors();
		}
		
//		int i = 0;
//		if (!isAppSettingsEnabled()) return sensors;
//		
//		//If the PowerIQ version is less than 3.1.0 then we have to fall back on the old way of getting pdus in bulk
//		if (limit < 0){
//			ResponseEntity<?> result = doRestGet(null,SensorsJSON.class);
//			if (result != null){
//				SensorsJSON json = (SensorsJSON)result.getBody();
//				sensors = json.getSensors();
//			}
//			
//			return sensors;
//		}
//		
//		long lastPiqId = -1;
//		Boolean done = false;
//		
//		while (!done){
//			StringBuilder prefix = new StringBuilder(""); //new StringBuilder("?order=id.asc&limit=").append(limit).append("&id_gt=").append(lastPiqId);
//			ResponseEntity<?> result = doRestGet(prefix.toString(), SensorsJSON.class);
//			if (result != null){
//				SensorsJSON json = (SensorsJSON)result.getBody();
//				List<Sensor> lSensors = json.getSensors();
//				if (lSensors != null && lSensors.size() > 0){
//					lSensors.get(lSensors.size() - 1).getId();
//					sensors.addAll(lSensors);
//					if (lastPiqId == -1) done = true;
//				}
//				else 
//					done = true;
//			} else {
//				done = true;
//			}
//		}
		return sensors;
	}


}
