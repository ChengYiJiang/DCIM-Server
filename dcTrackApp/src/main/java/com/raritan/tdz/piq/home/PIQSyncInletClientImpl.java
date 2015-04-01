package com.raritan.tdz.piq.home;
 
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.Inlet;
import com.raritan.tdz.piq.json.InletsJSON;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author basker
 */
public class PIQSyncInletClientImpl extends PIQSyncBase implements PIQSyncInletClient, PIQSyncPorts{
	
	@Autowired
	PowerPortDAO powerPortDao;
	
	@Autowired
	LocationDAO locationDAO;
	
	@Autowired
	private PowerPortFinderDAO powerPortFinder; 
	
	public PIQSyncInletClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	@Override
	public Inlet findInlet(long pduPiqId, int inletNumber) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return null;
		Inlet inlet = null;
		int inlet_ordinal = inletNumber > 0 ? inletNumber : 1;

		// Find PDU inlet ID based on the sort order of the power
		StringBuffer url = new StringBuffer();
		url.append( Long.toString(pduPiqId) );
		url.append("/inlets?ordinal_eq=");
		url.append( inlet_ordinal );
		
		try {
			ResponseEntity<?> resp = doRestGet(getRestURL("v2/pdus", url.toString()), getHttpHeaders(), eventSource, InletsJSON.class);
			if (resp != null) {
				InletsJSON body = (InletsJSON)resp.getBody();
				if (body != null) {
					List<Inlet> inlets = body.getInlets();
					if (inlets != null && !inlets.isEmpty()) {
						inlet = inlets.get(0);
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get intlets for PDU", e);
			throw e;
		}
		
		return inlet;
	}
	
	private List<Inlet> getAllInlets(List<Integer> pduPiqIds) throws RemoteDataAccessException {
		List<Inlet> inlets = new ArrayList<Inlet>();

		if (!isAppSettingsEnabled()) return null;
		if (pduPiqIds == null) return null;
		
		String service = "v2/pdus";
		try {
			for (Integer piqId: pduPiqIds) {
				if (piqId != null) {
					StringBuffer path = new StringBuffer(piqId.toString()).append("/inlets");  
					ResponseEntity<?> resp = doRestGet(service, path.toString(), InletsJSON.class);
					if (resp != null) {
						InletsJSON body = (InletsJSON)resp.getBody();
						if (body != null) {
							// accumulate inlets for each pdu
							List<Inlet> pduInlets = body.getInlets();
							if (pduInlets != null) inlets.addAll(pduInlets);
						}
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get inlets", e);
			throw e;
		}
		return inlets;
	}

	private void syncInletsForPduItem(Item item) throws DataAccessException, RemoteDataAccessException {
		
		if (item == null) {
			log.debug("[syncInletsForPduItem]: Item is null, cannot sync inlets.");
			return;
		}
		
		Long itemId = item.getItemId();
		List<Integer> pduPiqIds = new ArrayList<Integer>();
		pduPiqIds.add(item.getPiqId());
		List<Inlet> inlets = getAllInlets(pduPiqIds);
		
		// nothing to update
		if (inlets.size() == 0) {
			log.debug("[syncInletsForPduItem]: No inlets returned from PIQ nothing to update ");
			return;
		}
		
		// for each inlet, update power port input cord reading 
		for (Inlet inlet: inlets) {
			List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqIdAndSortOrder(itemId, inlet.getId(), inlet.getOrdinal(), SystemLookup.PortSubClass.INPUT_CORD); 
			if (inlet != null) {
				if (((ppList != null && ppList.size() > 0) ||
					(ppList = powerPortFinder.findPowerPortBySortOrder(itemId, inlet.getOrdinal(),  SystemLookup.PortSubClass.INPUT_CORD)) != null && 
					ppList.size() > 0)) {
					PowerPort pp = ppList.get(0);
					pp.setPiqId(inlet.getId());
					pp.setAmpsActual(inlet.getReading().getCurrent());
				}
			}
		}
	}
	
	@Override
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException,	RemoteDataAccessException {
		for (Item item: items) {
			if (item != null) {
				syncInletsForPduItem(item);
				
			}
		}
	}
	
	@Override
	public void syncAllPortReadings(int chunkLimit, Errors errors)
			throws DataAccessException, RemoteDataAccessException {
		syncAllInlets(chunkLimit);
	}
	
	private void syncAllInlets(int limit) throws RemoteDataAccessException {
		List<Inlet> inlets = getAllInlets(50);
		updateReadings (inlets);
	}
	
	private List<Inlet> getAllInlets(int limit) throws RemoteDataAccessException {
		List<Inlet> inlets = new ArrayList<Inlet>();
		
		if (!isAppSettingsEnabled()) return inlets;
		
		//If the PowerIQ version is less than 3.1.0 then we have to fall back on the old way of getting pdus in bulk
		if (limit < 0){
			ResponseEntity<?> result = doRestGet(null,InletsJSON.class);
			if (result != null){
				InletsJSON json = (InletsJSON)result.getBody();
				inlets = json.getInlets();
			}
			
			return inlets;
		}
		
		long lastPiqId = 0;
		Boolean done = false;
		
		while (!done){
			StringBuilder prefix = new StringBuilder("?order=id.asc&limit=").append(limit).append("&id_gt=").append(lastPiqId);
			ResponseEntity<?> result = doRestGet(prefix.toString(), InletsJSON.class);
			if (result != null){
				InletsJSON json = (InletsJSON)result.getBody();
				List<Inlet> incInlets = json.getInlets();
				if (incInlets != null && incInlets.size() > 0){
					lastPiqId = incInlets.get(incInlets.size() - 1).getId();
					inlets.addAll(incInlets);
				}
				else 
					done = true;
			} else {
				done = true;
			}
		}
		return inlets;
	}
	
	void updateReadings (List<Inlet> inlets) {
		List<Long> locationIds = locationDAO.getLocationIdByPIQHost(getIpAddress()) ;
		// for each inlet, update power port input cord reading 
		for (Inlet inlet: inlets) {
			if (inlet != null) {
				for (Long locationId:locationIds){
					List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqId(inlet.getId(), inlet.getOrdinal(), SystemLookup.PortSubClass.INPUT_CORD, locationId); 
					if (ppList != null && ppList.size() == 1) {
						PowerPort pp = ppList.get(0);
						pp.setPiqId(inlet.getId());
						pp.setAmpsActual(inlet.getReading().getCurrent());
					}
				}
			}
		}		
	}

	
}
