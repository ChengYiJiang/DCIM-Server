package com.raritan.tdz.piq.home;

 import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.AssetStrip;
import com.raritan.tdz.piq.json.AssetStripsJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * REST Client implementation for Asset Strip services using Spring RestTemplate.
 * 
 * @author Andrew Cohen
 */
public class PIQAssetStripClientImpl extends PIQRestClientBase implements PIQAssetStripClient, PIQSyncPorts  {

	private PIQSensorProcessor assetStripProcessor;
	
	@Autowired
	private PIQProbeLookup probeLookup;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private EventHome eventHome;
	
	@Autowired
	private PIQSensorCommon piqSensorCommon;

	public PIQSensorProcessor getAssetStripProcessor() {
		return assetStripProcessor;
	}

	public void setAssetStripProcessor(PIQSensorProcessor assetStripProcessor) {
		this.assetStripProcessor = assetStripProcessor;
	}

	public PIQAssetStripClientImpl(ApplicationSettings appSettings) throws DataAccessException {
		super( appSettings );
	}
	
	@Override
	public List<AssetStrip> getAssetStrips(String piqId) throws RemoteDataAccessException  {
		List<AssetStrip> assetStrips = null;
		
		ResponseEntity<?> resp = doRestGet(getRestURL("v2/pdus", piqId + "/asset_strips"), getHttpHeaders(), eventSource, AssetStripsJSON.class);
		if (resp != null) {
			AssetStripsJSON body = (AssetStripsJSON)resp.getBody();
			if (body != null) {
				assetStrips = body.getAssetStrips();
			}
		}
		
		return assetStrips;
	}
	
	@Override
	public void setLedOn(int rackUnitId, LedOnState state, LedColor color) throws RemoteDataAccessException {
		// TODO: Enable LED status when PIQ issue is fixed
//		setLedInternal(rackUnitId, state, color);
	}

	@Override
	public void setLedOff(int rackUnitId) throws RemoteDataAccessException {
		// TODO: Enable LED status when PIQ issue is fixed
//		setLedInternal(rackUnitId, null, null);
	}
	
	@Override
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException, RemoteDataAccessException  {
		for (Item item: items) {
			syncAssetStripSensors(item, errors);
		}
	}

	
	@Override
	public void syncAllPortReadings(int chunkLimit, Errors errors)
			throws DataAccessException, RemoteDataAccessException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Internal method for setting LED state and color.
	 */
	private void setLedInternal(int rackUnitId, LedOnState state, LedColor color) throws RemoteDataAccessException {
		Map<String, Object> rackUnit = new LinkedHashMap<String, Object>();
		rackUnit.put("led_state", state != null ? state.toString() : "off");
		if (color != null) {
			rackUnit.put("led_color", color.toString());
		}
		
		// Build the body
		Map<String, Object> body = new LinkedHashMap<String, Object>(1);
		body.put("rack_unit", rackUnit);
		
		// Make the REST PUT call
		doRestPut( body, Integer.toString(rackUnitId) );
	}
	
	private void syncAssetStripSensors(Item item, Errors errors) throws DataAccessException, RemoteDataAccessException {
		if (item == null) {
			if (log.isDebugEnabled()) {
				log.debug("[syncAssetStripSensors]: Item is null, cannot delete sensors");
			}
			return;
		}
		Item probeItem = probeLookup.getProbeItemForDummyRackPDU( item.getItemId() );
		Item itemToProcess = probeItem != null ? probeItem : item;
		
		// get sensors for this item from PIQ
		List<AssetStrip> sensors = getAssetStrips(item.getPiqId().toString());
		if (sensors == null || sensors.isEmpty()) {
			/* nothing to sync, remove existing sensor for this item */
			if (log.isDebugEnabled()) {
				log.debug("[syncAssetStripSensors]: No asset strips returned from PIQ for item so, deleting asset strips");
			}	
			piqSensorCommon.deleteAllAssetStripSensors(item, errors);
			return;
		}
		
		/* delete sensors that are not in sync */
		piqSensorCommon.deleteAssetStripNotInSync(itemToProcess, sensors, errors);
		
		// For each sensors discovered in PIQ, update dcTrack sensorPort data
		for (AssetStrip sensor: sensors) {
			if (sensor != null) {
				// process this asset strip 
				if (assetStripProcessor != null) {
					assetStripProcessor.process(itemToProcess, sensor, errors);
				}
				else {
					// discovered unknown sensor report error
					String code = "piqSync.invalidSensorResponse";
					Object[] args = { itemToProcess.getItemName(), sensor.getName(), sensor.getOrdinal() };
					errors.rejectValue("SyncSensor", code, args, "Invalid sensor type provide by Power IQ");
					String evtSummary = messageSource.getMessage(code,args, Locale.getDefault());
					
					Event ev = AddEvent (item.getItemName(), sensor.getName(), sensor.getOrdinal(), evtSummary);
					eventHome.saveEvent(ev);
				}
			}
		}
	}

	private Event AddEvent (String itemName, String sensorName, int ordinal, String evtSummary) throws DataAccessException {
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		Event ev = eventHome.createEvent(createdAt, EventType.INVALID_SENSOR_RESPONSE, EventSeverity.CRITICAL, "dcTrack");
		ev.setSummary(evtSummary);
		ev.addParam("Item Name", itemName);
		ev.addParam("Sensor Name", sensorName);
		ev.addParam("Index Number", new Integer(ordinal).toString());
		ev.addParam("Type", "ASSET_STRIP");
		return ev;
	}


}
