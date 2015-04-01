package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.InletPole;
import com.raritan.tdz.piq.json.InletPolesJSON;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.settings.home.ApplicationSettings;

public class PIQSyncInletPoleClientImpl extends PIQSyncBase implements
		PIQSyncPorts {

	@Autowired
	private PowerPortFinderDAO powerPortFinder; 
	
	@Autowired
	private LocationDAO locationDAO;

	public PIQSyncInletPoleClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		
		super(appSettings);
		
	}

	
	private List<InletPole> getAllInletPoles(List<Integer> pduPiqIds) throws RemoteDataAccessException {
		List<InletPole> inletPoles = new ArrayList<InletPole>();

		if (!isAppSettingsEnabled()) return null;
		if (pduPiqIds == null) return null;
		
		// String service = "v2/inlet_poles";
		try {
			for (Integer piqId: pduPiqIds) {
				if (piqId != null) {
					StringBuffer path =  new StringBuffer("/?pdu_id_eq=").append(piqId.toString()); //new StringBuffer(piqId.toString()).append("/inlet_poles");  
					ResponseEntity<?> resp = doRestGet(path.toString(), InletPolesJSON.class);
					if (resp != null) {
						InletPolesJSON body = (InletPolesJSON)resp.getBody();
						if (body != null) {
							// accumulate inlets for each pdu
							List<InletPole> pduInletPoles = body.getInletPoles();
							if (pduInletPoles != null) inletPoles.addAll(pduInletPoles);
						}
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get inlet poles", e);
			throw e;
		}
		return inletPoles;
	}

	
	private void syncInletPolesForPduItem(Item item) throws DataAccessException, RemoteDataAccessException {
		
		if (item == null) {
			log.debug("[syncInletsForPduItem]: Item is null, cannot sync inlets.");
			return;
		}
		
		Long itemId = item.getItemId();
		List<Integer> pduPiqIds = new ArrayList<Integer>();
		pduPiqIds.add(item.getPiqId());
		List<InletPole> inletPoles = getAllInletPoles(pduPiqIds);
		
		// nothing to update
		if (inletPoles.size() == 0) {
			log.debug("[syncInletsForPduItem]: No inlets returned from PIQ nothing to update ");
			return;
		}
		
		// for each inlet, update power port input cord reading 
		for (InletPole inletPole: inletPoles) {
			List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqIdAndSortOrder(itemId, inletPole.getInletId(), inletPole.getReading().getInletOrdinal(), SystemLookup.PortSubClass.INPUT_CORD); 
			if (inletPole != null) {
				if (((ppList != null && ppList.size() > 0) ||
					(ppList = powerPortFinder.findPowerPortBySortOrder(itemId, inletPole.getReading().getInletOrdinal(),  SystemLookup.PortSubClass.INPUT_CORD)) != null && 
					ppList.size() > 0)) {
					PowerPort pp = ppList.get(0);
					pp.setPiqId(inletPole.getId());
					setPoleAmps(inletPole, pp);
				}
			}
		}
	}
	

	private void setPoleAmps(InletPole pole, PowerPort pp) {
		
		if (pole.getReading().getInletPoleOrdinal() == 1) { // A
			pp.setAmpsActualA(pole.getReading().getCurrent());
		}
		else if (pole.getReading().getInletPoleOrdinal() == 2) { // B
			pp.setAmpsActualB(pole.getReading().getCurrent());
		}
		else if (pole.getReading().getInletPoleOrdinal() == 3) { // C
			pp.setAmpsActualC(pole.getReading().getCurrent());
		}
		else if (pole.getReading().getInletPoleOrdinal() == 4) { // N
			pp.setAmpsActualN(pole.getReading().getCurrent());
		}
		
	}
	
	@Override
	public void syncPortReadings(List<Item> items, Errors errors)
			throws DataAccessException, RemoteDataAccessException,
			BusinessValidationException {
		for (Item item: items) {
			if (item != null) {
				syncInletPolesForPduItem(item);
			}
		}
	}
	
	
	@Override
	public void syncAllPortReadings(int chunkLimit, Errors errors)
			throws DataAccessException, RemoteDataAccessException {
		syncAllInletPoles(50);
		
	}
	
	private void syncAllInletPoles(int limit) throws RemoteDataAccessException {
		List<InletPole> inlets = getAllInlets(50);
		updateReadings (inlets);
	}
	
	private List<InletPole> getAllInlets(int limit) throws RemoteDataAccessException {
		List<InletPole> inletsPole = new ArrayList<InletPole>();
		
		if (!isAppSettingsEnabled()) return inletsPole;
		
		//If the PowerIQ version is less than 3.1.0 then we have to fall back on the old way of getting pdus in bulk
		if (limit < 0){
			ResponseEntity<?> result = doRestGet(null,InletPolesJSON.class);
			if (result != null){
				InletPolesJSON json = (InletPolesJSON)result.getBody();
				inletsPole = json.getInletPoles();
			}
			
			return inletsPole;
		}
		
		long lastPiqId = 0;
		Boolean done = false;
		
		while (!done){
			StringBuilder prefix = new StringBuilder("?order=id.asc&limit=").append(limit).append("&id_gt=").append(lastPiqId);
			ResponseEntity<?> result = doRestGet(prefix.toString(), InletPolesJSON.class);
			if (result != null){
				InletPolesJSON json = (InletPolesJSON)result.getBody();
				List<InletPole> incInlets = json.getInletPoles();
				if (incInlets != null && incInlets.size() > 0){
					lastPiqId = incInlets.get(incInlets.size() - 1).getId();
					inletsPole.addAll(incInlets);
				}
				else 
					done = true;
			} else {
				done = true;
			}
		}
		return inletsPole;
	}
	
	void updateReadings (List<InletPole> inletsPole) {
		List<Long> locationIds = locationDAO.getLocationIdByPIQHost(getIpAddress()) ;
		// for each inlet, update power port input cord reading 
		for (InletPole inletPole: inletsPole) {
			if (inletPole != null) {
				for (Long locationId:locationIds){
					List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqId(inletPole.getId(), inletPole.getOrdinal(), SystemLookup.PortSubClass.INPUT_CORD, locationId); 
					if (ppList != null && ppList.size() == 1) {
						PowerPort pp = ppList.get(0);
						pp.setPiqId(inletPole.getId());
						setPoleAmps(inletPole, pp);
					}
				}
			}
		}		
	}




	
	
}
