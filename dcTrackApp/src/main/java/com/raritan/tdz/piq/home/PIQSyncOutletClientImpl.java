package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.ErrorJSON;
import com.raritan.tdz.piq.json.Outlet;
import com.raritan.tdz.piq.json.Outlet.Reading;
import com.raritan.tdz.piq.json.OutletJSON;
import com.raritan.tdz.piq.json.OutletsJSON;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author Andrew Cohen
 */
public class PIQSyncOutletClientImpl extends PIQSyncBase implements PIQSyncOutletClient, PIQSyncPorts {

	private PIQAssociationNotInSync piqAssociationNotInSync = null;
	private PortHome portHome;

	@Autowired
	PowerPortDAO powerPortDao;
	
	@Autowired
	private LocationDAO locationDAO;

	@Autowired
	private PowerPortFinderDAO powerPortFinder; 
	
	public PIQSyncOutletClientImpl(ApplicationSettings appSettings)
			throws DataAccessException {
		super(appSettings);
	}

	public PortHome getPortHome() {
		return portHome;
	}

	public void setPortHome(PortHome portHome) {
		this.portHome = portHome;
	}

	@Override
	public void updatePowerConnection(PowerPort sourcePort, PowerPort destPort) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return;
		if (sourcePort == null || destPort == null || sourcePort.getItem() == null) return;
		
		Integer deviceId = sourcePort.getItem().getPiqId();
		
		if (deviceId == null) {
			if (log.isDebugEnabled()) {
				log.debug("PIQ ID is null on PDU item");
			}
			return;
		}
		
		if (destPort.getPiqId() == null) {
			if (log.isDebugEnabled()) {
				log.debug("Destination Port has no PIQ ID");
			}
			return;
		}
		
		if (!isRPDUOutput(destPort)) {
			log.warn("Destination port of power connection is not a Rack PDU Output! PIQ will not updated.");
			return;
		}
		
		Outlet outlet = null;
		try {
			outlet = getOutlet( destPort.getPiqId() );
		} catch (RemoteDataAccessException e){
			//See if this is a 404 error
			ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
			if (error != null && destPort.getItem().getPiqId() != null){
				//Find the outlet by the outlet number
				outlet = findOutlet (destPort.getItem().getPiqId(),destPort.getSortOrder());
				
				//update the current outlet with the correct PIQ id
				if (outlet != null){
					destPort.setPiqId(outlet.getId());
					try {
						portHome.savePowerPort(destPort);
					} catch (DataAccessException e1) {
						log.error("",e1);
					}
				}
			
				//proceed with the connection
			} else {
				throw e;
			}
		}
		
		if (outlet != null) {
			outlet.setDeviceId( deviceId.longValue() );
			doRestPut( new OutletJSON(outlet), Long.toString( outlet.getId() ) );
		}
	}
	
	@Override
	public void deletePowerConnection(PowerPort destPort) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return;
		if (destPort == null) return;
		
		if (destPort.getPiqId() != null) {
			
			if (!isRPDUOutput(destPort)) {
				log.warn("Destination port of power connection is not a Rack PDU Output! Will not update this power association in PIQ.");
				return;
			}
			
			Outlet outlet = null;
			try {
				outlet = getOutlet( destPort.getPiqId() );
			} catch (RemoteDataAccessException e){
				ErrorJSON error = (ErrorJSON) e.getRemoteExceptionDetail();
				if (error != null && destPort.getItem().getPiqId() != null){
					//Find the outlet by the outlet number
					outlet = findOutlet (destPort.getItem().getPiqId(),destPort.getSortOrder());
					//update the current outlet with the correct PIQ id
					if (outlet != null){
						destPort.setPiqId(outlet.getId());
						try {
							portHome.savePowerPort(destPort);
						} catch (DataAccessException e1) {
							log.error("",e1);
						}
					}
					
					//proceed with the connection
				} else {
					throw e;
				}
			}
			
			if (outlet != null) {
				outlet.setDeviceId( null );
				doRestPut( new OutletJSON(outlet), Long.toString( outlet.getId() ) );
			}
		}
	}

	@Override
	public Outlet findOutlet(long pduPiqId, int outletNumber) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return null;
		Outlet outlet = null;
		int outletId = outletNumber > 0 ? outletNumber : 1;
		
		// Find PDU outlet ID based for the sort order of the power
		StringBuffer url = new StringBuffer();
		url.append( Long.toString(pduPiqId) );
		url.append("/outlets?outlet_id_eq=");
		url.append( outletId );
		
		try {
			ResponseEntity<?> resp = doRestGet(getRestURL("v2/pdus", url.toString()), getHttpHeaders(), eventSource, OutletsJSON.class);
			if (resp != null) {
				OutletsJSON body = (OutletsJSON)resp.getBody();
				if (body != null) {
					List<Outlet> outlets = body.getOutlets();
					if (outlets != null && !outlets.isEmpty()) {
						outlet = outlets.get(0);
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get outlets for PDU", e);
			throw e;
		}
		
		return outlet;
	}

	@Override
	public Outlet getOutlet(long outletId) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled()) return null;
		Outlet outlet = null;
		
		try {
			ResponseEntity<?> resp = doRestGet(Long.toString(outletId), OutletJSON.class);
			if (resp != null) {
				OutletJSON body = (OutletJSON)resp.getBody();
				if (body != null) {
					outlet = body.getOutlet();
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get outlets for PDU", e);
			throw e;
		}
		
		return outlet;
	}

	@Override
	public PIQAssociationNotInSync areAssociationsInSync(
			List<PowerConnection> powerConnections, List<Integer> piqPduIds)
			throws RemoteDataAccessException {
		piqAssociationNotInSync = new PIQAssociationNotInSync();
		
		//First get the outlets from PowerIQ
		List<Outlet> outlets = getAllOutlets(piqPduIds);
		
		//Make a hash table out of the outlets and its ids
		Map<Long,Outlet> outletMap = getAllOutletsHash(outlets);
		
		//Go through the powerConnections and get the outlet corresponding to 
		//power IQ id of destination from the above hash table
		for (PowerConnection conn:powerConnections){
		
			//From the source port, get the item and get its piq_id
			PowerPort sourcePort = conn.getSourcePowerPort();
			PowerPort destPort = conn.getDestPowerPort();
			
			Integer deviceId = sourcePort.getItem().getPiqId();
			Long portId = destPort.getPiqId();
			
			Outlet outlet = outletMap.get(portId);
			
			if (deviceId != null && outlet != null && outlet.getDeviceId() != null && deviceId.intValue() != outlet.getDeviceId().intValue()){
				//If they are not equal, then put an entry in PIQAssociationNotInSync.
				piqAssociationNotInSync.addItem(conn);
			} else if (outlet != null && outlet.getDeviceId() == null){
				//This says that we have no association.
				piqAssociationNotInSync.addItem(conn);
			} else if (outlet == null){
				piqAssociationNotInSync.addItem(conn);
			}

		}
		
		return piqAssociationNotInSync;
	}

	@Override
	public PIQAssociationNotInSync getPIQAssociationNotInSync() {
		return piqAssociationNotInSync;
	}

	@Override
	public Double getOutletCurrentReading(long pduPiqId, int outletId) throws RemoteDataAccessException {
		
		if (!isAppSettingsEnabled() || outletId == 0) return null;

		Outlet outlet = findOutlet( pduPiqId, outletId);
		Reading reading = ((outlet!= null) ? outlet.getReading(): null);
		return ((reading != null) ? reading.getCurrentAmps() : null);
	}
	
	@Override	
	public Double getOutletCurrentReading(long pduPiqId) throws RemoteDataAccessException {
		if (!isAppSettingsEnabled() || pduPiqId == 0) return null;
		
		Outlet outlet = getOutlet(pduPiqId);
		Reading reading = ((outlet!=null) ? outlet.getReading(): null);
		return ((reading != null) ? reading.getCurrentAmps() : null);
	}

	// JSON request to get all of the PDU outlet details from PIQ times out, when PIQ
	// has hundreds of PDUs, due to large data in response (CR-49378)
	// With new changes this function now make one JSON request per PDU to get its
	// outlet details.

	private List<Outlet> getAllOutlets(List<Integer> pduPiqIds) throws RemoteDataAccessException {
		List<Outlet> outlets = new ArrayList<Outlet>();

		if (!isAppSettingsEnabled()) return null;
		if (pduPiqIds == null) return null;
		
		String service = "v2/pdus";
		try {
			for (Integer piqId: pduPiqIds) {
				if (piqId != null) {
					StringBuffer path = new StringBuffer(piqId.toString()).append("/outlets");  
					ResponseEntity<?> resp = doRestGet(service, path.toString(), OutletsJSON.class);
					if (resp != null) {
						OutletsJSON body = (OutletsJSON)resp.getBody();
						if (body != null) {
							// accumulate outlets for each pdu
							List<Outlet> pduOutlets = body.getOutlets();
							if (pduOutlets != null) outlets.addAll(pduOutlets);
						}
					}
				}
			}
		} 
		catch (RemoteDataAccessException e) {
			log.error("Failed to get outlets", e);
			throw e;
		}
		return outlets;
	}
	
	private Map<Long,Outlet> getAllOutletsHash(List<Outlet> outlets) throws RemoteDataAccessException {
		Map<Long,Outlet> outletMap = new HashMap<Long,Outlet>();
		if (outlets != null){
			for (Outlet outlet:outlets){
				if (outlet != null)
					outletMap.put(outlet.getId(), outlet);
			}
		}
		return outletMap;
	}
	
	private boolean isRPDUOutput(PowerPort port) {
		if (port == null) return false;
		LksData subClass = port.getPortSubClassLookup();
		if (subClass == null) return false;
		Long subClassValueCode = subClass.getLkpValueCode();
		if (subClassValueCode == null) return false;
		return subClassValueCode == SystemLookup.PortSubClass.RACK_PDU_OUTPUT;
	}
	
	private void syncOutletsForAnItem(Item item) throws DataAccessException, RemoteDataAccessException {
		if (item == null) {
			if (log.isDebugEnabled()) {
				log.debug("[syncOutletsForAnItem]: Item is null, cannot sync outlets.");
			}
			return;
		}

		Long itemId = item.getItemId();
		Integer pduPiqId = item.getPiqId();
		
		// The getAllOutlets function below takes array list of pduPiqIds 
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(pduPiqId);
		
		/* get outlets from PIQ */
		List<Outlet> outlets = getAllOutlets(ids);
		
		// nothing to update 
		if (outlets.size() == 0) {
			if (log.isDebugEnabled()) {
				log.debug("[syncOutletsForAnItem]: no outlets returned from PIQ, nothing to sync.");
			}
			return;
		}

		for(Outlet o: outlets) {
			if (o != null) {
				// find port in dctrack which matches sortOrder and piqId
				List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqIdAndSortOrder(itemId, o.getId(), o.getOutletId(), SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
				
				// update port that matches piqId and sortOrder or just the sortOrder.
				if ( (ppList != null && ppList.size() > 0)  ||
						(ppList = powerPortFinder.findPowerPortBySortOrder(itemId, o.getOutletId(), SystemLookup.PortSubClass.RACK_PDU_OUTPUT)) != null && 
						ppList.size() > 0 ) {
					PowerPort pp = ppList.get(0);
					pp.setPiqId(o.getId());
					pp.setAmpsActual(o.getReading().getCurrentAmps());
				} 
			}
		}
	}

	@Override
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException, RemoteDataAccessException {
		for (Item item: items) {
			if (item != null) syncOutletsForAnItem(item);
		}
	}

	
	@Override
	public void syncAllPortReadings(int chunkLimit, Errors errors)
			throws DataAccessException, RemoteDataAccessException {
		syncAllOutlets(50);
	}
	
	private void syncAllOutlets(int limit) throws RemoteDataAccessException {
		List<Outlet> outlets = getAllOutlets(limit);
		updateReadings (outlets);
	}
	
	private List<Outlet> getAllOutlets(int limit) throws RemoteDataAccessException {
		List<Outlet> outlets = new ArrayList<Outlet>();
		
		if (!isAppSettingsEnabled()) return outlets;
		
		//If the PowerIQ version is less than 3.1.0 then we have to fall back on the old way of getting pdus in bulk
		if (limit < 0){
			ResponseEntity<?> result = doRestGet(null,OutletsJSON.class);
			if (result != null){
				OutletsJSON json = (OutletsJSON)result.getBody();
				outlets = json.getOutlets();
			}
			
			return outlets;
		}
		
		long lastPiqId = 0;
		Boolean done = false;
		
		while (!done){
			StringBuilder prefix = new StringBuilder("?order=id.asc&limit=").append(limit).append("&id_gt=").append(lastPiqId);
			ResponseEntity<?> result = doRestGet(prefix.toString(), OutletsJSON.class);
			if (result != null){
				OutletsJSON json = (OutletsJSON)result.getBody();
				List<Outlet> incInlets = json.getOutlets();
				if (incInlets != null && incInlets.size() > 0){
					lastPiqId = incInlets.get(incInlets.size() - 1).getId();
					outlets.addAll(incInlets);
				}
				else 
					done = true;
			} else {
				done = true;
			}
		}
		return outlets;
		
	}
	
	private void updateReadings(List<Outlet> outlets) {
		List<Long> locationIds = locationDAO.getLocationIdByPIQHost(getIpAddress()) ;
		// for each inlet, update power port input cord reading 
		for (Outlet outlet: outlets) {
			if (outlet != null) {
				for (Long locationId:locationIds){
					List<PowerPort> ppList = powerPortFinder.findPowerPortByPiqId(outlet.getId(), outlet.getOutletId(), SystemLookup.PortSubClass.RACK_PDU_OUTPUT, locationId); 
					if (ppList != null && ppList.size() == 1) {
						PowerPort pp = ppList.get(0);
						pp.setPiqId(outlet.getId());
						pp.setAmpsActual(outlet.getReading().getCurrentAmps());
					}
				}
			}
		}		
	}


	
}
