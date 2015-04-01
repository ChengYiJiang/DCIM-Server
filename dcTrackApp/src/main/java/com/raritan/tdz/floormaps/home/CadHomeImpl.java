package com.raritan.tdz.floormaps.home;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.floormaps.dto.CadHandleDTO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.home.PIQSyncFloorMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.codehaus.jackson.map.ObjectMapper;

import com.raritan.tdz.domain.Item;
/**
 * 
 * @author Brian Wang
 */
public class CadHomeImpl implements CadHome {

	protected Logger log = Logger.getLogger( this.getClass() );
	protected SessionFactory sessionFactory;	
	
	private PIQSyncFloorMap piqSyncFloorMap;
	private EventHome eventHome;
	
	@Autowired
	private LocationDAO locationDAO;
	
	
	public CadHomeImpl(SessionFactory sessionFactory ){
		this.sessionFactory = sessionFactory;
		log.info("init...");
		
	}
	
	

	public PIQSyncFloorMap getPiqSyncFloorMap() {
		return piqSyncFloorMap;
	}



	public void setPiqSyncFloorMap(PIQSyncFloorMap piqSyncFloorMap) {
		this.piqSyncFloorMap = piqSyncFloorMap;
	}



	public EventHome getEventHome() {
		return eventHome;
	}



	public void setEventHome(EventHome eventHome) {
		this.eventHome = eventHome;
	}



	public CadHandleDTO getCadHandles(String locationId) throws DataAccessException {
		List<Map> dataList=new ArrayList<Map>();
		
		CadHandleDTO cadHandleDTO = new CadHandleDTO();
		
		Session session = this.sessionFactory.getCurrentSession();
		
		log.info("getCadHandles() locationId=" + locationId);

        cadHandleDTO.setLocationId(locationId);
        
		Criteria criteria = session.createCriteria( Item.class );
		
		criteria.createAlias("dataCenterLocation","dataCenterLocation", Criteria.LEFT_JOIN);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.createAlias("subclassLookup", "subclassLookup", Criteria.LEFT_JOIN);
		
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("itemId"))
				.add(Projections.property("itemName"))
				.add(Projections.property("cadHandle"))
				.add(Projections.property("cadName"))
				.add(Projections.property("classLookup.lkpValueCode"))
				.add(Projections.property("subclassLookup.lkpValueCode"))
				.add(Projections.property("classLookup.lkpValue"))));
		
		criteria.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", Long.parseLong(locationId)));
		
		criteria.add(Restrictions.in("classLookup.lkpValueCode", new Long[]{
				SystemLookup.Class.CABINET,
				SystemLookup.Class.FLOOR_PDU,
				SystemLookup.Class.CRAC,
				SystemLookup.Class.UPS
			}));
		
		List<Map> results = criteria.list();
		
		for ( Object result : results ) {
		
			Map<String,Object> map=new HashMap<String,Object>();
			
			Object[] row = (Object[]) result;
			
			map.put("itemId", row[0]);
			map.put("itemName", row[1]);
			map.put("cadHandle", row[2]);
			map.put("cadName", row[3]);
			map.put("itemClass", row[4]);
			map.put("itemSubClass", row[5]);
			map.put("objectType", row[6]);
			
			dataList.add(map);
			
		}
		
		cadHandleDTO.setData(dataList);
        
        return cadHandleDTO;
	}
	
	public int setCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException {
		Session session = this.sessionFactory.getCurrentSession();
		List<Map> data = cadHandleDTO.getData();
		
		log.info("setCadHandles() locationId=" + cadHandleDTO.getLocationId());		
		
		int cnt = 0;
		/* example
		Item item = (Item)session.get(Item.class, (long)3);		// Cabinet 1A
		// update cad handle
		item.setCadHandle("4F8");		// original value is 4F7
		*/
		for (Map<String,Object> map : data) {
			// FIXME: If Integer is not long enough, need to find another way to parse JSON data
			Object idobj = (Object)map.get("itemId");
			Long itemId = null;
			
			if (idobj instanceof Integer)
				itemId = ((Integer)idobj).longValue();
			else if (idobj instanceof Long)
				itemId = (Long)idobj;
			else
				itemId = new Long(-1);

			String cadHandle = (String)map.get("cadHandle");
			String cadName = (String)map.get("cadName");
			Item item = (Item)session.get(Item.class, itemId);
			if (item != null) {
				long lkpValueCode = item.getClassLookup().getLkpValueCode(); 
				if (lkpValueCode == SystemLookup.Class.CABINET ||
					lkpValueCode == SystemLookup.Class.FLOOR_PDU ||
					lkpValueCode == SystemLookup.Class.CRAC ||
					lkpValueCode == SystemLookup.Class.UPS ) {
					
					// TODO: Ignore cases that are the same
					
					item.setCadHandle(cadHandle);
					item.setCadName(cadName);
					session.update(item);
					
					log.info("Set cadHandle=\"" + cadHandle + "\", cadName=\"" + cadName + "\" where itemId=" + itemId);
					
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	protected boolean getDoNotUpdateDwgFilename() throws DataAccessException {
		boolean isDoNotUpdate=false;

		Session session = this.sessionFactory.getCurrentSession();
		
		String querySQL="SELECT parameter FROM tblsettings where setting='DoNotUpdateDwgFilename'";
		SQLQuery sqlQuery = session.createSQLQuery( querySQL );
		sqlQuery.addScalar("parameter", new StringType());
	
		String doNotUpdateDwgFilename = (String)sqlQuery.uniqueResult();
		isDoNotUpdate="true".equals(doNotUpdateDwgFilename) ? true : false;
		
		log.info("DoNotUpdateDwgFilename="+doNotUpdateDwgFilename+" isDoNotUpdate="+isDoNotUpdate);
		
		return isDoNotUpdate;
	}
	
    public boolean updateLocation(String locationId,String filePath) throws DataAccessException {
		boolean hasUpdated=false;
		
		Session session = this.sessionFactory.getCurrentSession();
		
		if( !getDoNotUpdateDwgFilename() ) {
			//update dct_locations
			String hql="update DataCenterLocationDetails set dwgFileName =:dwgFileName "+
						"where dataCenterLocationId =:locationId";
			
			Query query = session.createQuery( hql );
			query.setString("dwgFileName",filePath);
			query.setLong("locationId",Long.parseLong(locationId));
			
			int rowCount = query.executeUpdate();
			log.info("executeUpdate rowCount="+rowCount);
			
			hasUpdated=true;
			
			//Make sure we upload the file to PowerIQ.
			//RT: If this is not the right place to do this, please move this to where you want to make this call.
			//Not implemented here
		  	//uploadFloorMapToPIQ(locationId, filePath);
		}
			
    	return hasUpdated;
    }




    
    public CadHandleDTO syncCadHandles(CadHandleDTO cadHandleDTO) throws DataAccessException{
    	CadHandleDTO dbDTO = getCadHandles(cadHandleDTO.getLocationId()); 
    	
    	Map<Long,Object> syncedMap = new HashMap<Long,Object>();
    	List<Map> rstList = new ArrayList<Map>();		// synced objects
    	
		for (Map<String,Object> map : cadHandleDTO.getData()) {
			String cadName = (String)map.get("cadName");
			String cadHandle = (String)map.get("cadHandle");

			// Filter out wrong objects
			String objType = (String)map.get("objectType");
			if (objType == null || objType.length() == 0) {
				// Not defined?
				objType = "cabinet";
			}
			
			if (objType.equalsIgnoreCase("cabinet") == false && 
					objType.equalsIgnoreCase("floor pdu") == false &&
					objType.equalsIgnoreCase("ups") == false &&
					objType.equalsIgnoreCase("crac") == false) {
				
				// Ignore it 
				continue;
			}
			
			
			int syncRule = 0;
			Long itemId = null;
			
			for (Map<String,Object> dbMap: dbDTO.getData()) {
				itemId = (Long)dbMap.get("itemId");
				String cadNameDB = (String)dbMap.get("cadName");
				String itemNameDB = (String)dbMap.get("itemName");
				String cadHandleDB = (String)dbMap.get("cadHandle");
				Long itemClassDB = (Long)dbMap.get("itemClass");
				Long itemSubClassDB = (Long)dbMap.get("itemSubClass");				
				
				if (cadName != null && cadName.equalsIgnoreCase(cadNameDB)) {
					/*
					 * Look in DB for an item with "CAD name" matching the name of the drawing object.  If a match is found, this object is synced.
					 */
					syncRule = 1;
					break;
				} else if (cadName != null && cadName.equalsIgnoreCase(itemNameDB)) {
					/*
					 * Else, look in DB for an item with "Name" matching the name of the drawing object.  If a match is found, this object is synced.
					 */
					syncRule = 2;
					break;
				} else if (cadName != null && itemNameDB != null && itemNameDB.equalsIgnoreCase(cadName + "-cabinet") && itemClassDB == SystemLookup.Class.CABINET &&
						itemSubClassDB == SystemLookup.SubClass.CONTAINER) {
					/*
					 * Else, look in DB for an item with "Name" matching the name of the drawing object with "-cabinet" appended AND class="Cabinet" 
					 * and subclass="Container".  If a match is found, this object is synced.
					 */
					syncRule = 3;
					break;
				} else if (cadHandle != null && cadHandle.equalsIgnoreCase(cadHandleDB)) {
					/*
					 * Else, look in DB for an item with "CAD handle" matching the CAD handle of the drawing object.  If a match is found, this object is synced. 
					 */
					syncRule = 4;
					break;
				} else {
					/*
					 * Unsynced
					 */
					
				}
			}
			
			if (syncRule != 0) {
				// Use the itemID from database
				// FIXME: need to be the same as JSON from http
				map.put("itemId", itemId);
				map.put("syncRule", syncRule);
				
				/*
				 * For each synced object, update the item's "CAD handle" and "CAD name" fields to match those of the associated CAD object.
				 */
				rstList.add(map);
				
				// To speed up the unsynced objects
				syncedMap.put(itemId, map);
			}
		}

		CadHandleDTO rstDTO = new CadHandleDTO();
		rstDTO.setLocationId(cadHandleDTO.getLocationId());
		

		// For items not in the synced map
		CadHandleDTO cadHandlesDB2 = getCadHandles(cadHandleDTO.getLocationId()); 
		for(Map<String,Object> map : cadHandlesDB2.getData()) {
			Long itemId = (Long)map.get("itemId");
			if (syncedMap.get(itemId) == null) {
				/*
				 * After creating all available associations, clear the "CAD handle" and "CAD name" fields of all the items in the DB that are not synced.
				 */
				String cadName = (String)map.get("cadName");
				String cadHandle = (String)map.get("cadHandle");
				if ((cadName == null || cadName.length() == 0) &&
					(cadHandle == null || cadHandle.length() == 0)) {
					// Ignore the one already empty
				} else {
					map.put("cadName", "");
					map.put("cadHandle", "");
					rstList.add(map);
				}
			}
		}

		// Update database
		rstDTO.setData(rstList);
		setCadHandles(rstDTO);
		
		return rstDTO;
    }    
    
    public void syncCadHandleByItem(Item item) throws DataAccessException {
    	if (item == null || 
    			item.getDataCenterLocation() == null || 
    			item.getItemName() == null) {
    		log.warn("Data in item is not enough for this function: " + item);
    		return;
    	}
    	
    	long locationid = item.getDataCenterLocation().getDataCenterLocationId().longValue();
		log.info("sync_cadhandle...");
		log.info("locationid "+locationid);
    	
    	try {
	    	// Call rest API of floormaps to get all cad objects
	    	DefaultHttpClient httpclient = new DefaultHttpClient();
	    	HttpGet request = new HttpGet("http://localhost:8080/floorMapsService/restful/cad_handle?siteCode=" + locationid);
	    	HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = new BasicResponseHandler().handleResponse(response);

			HashMap<String,Object> result = new ObjectMapper().readValue(responseString, HashMap.class);
			
			String siteCode = (String)result.get("siteCode");
			List<Map> cadhandles = (List<Map>)result.get("data");		// cadhnaldes from floormaps
			
			// Find out unsynced objects
			List<Map> unsyncedList = new ArrayList<Map>();
			CadHandleDTO dbDTO = getCadHandles(String.valueOf(locationid));
			for (Map<String,Object> map : cadhandles) {
				String cadHandle = (String)map.get("cadHandle");

				// Filter out wrong objects
				String objType = (String)map.get("objectType");
				if (objType == null || objType.length() == 0) {
					// Not defined?
					objType = "cabinet";
				}
				
				if (objType.equalsIgnoreCase("cabinet") == false && 
						objType.equalsIgnoreCase("floor pdu") == false &&
						objType.equalsIgnoreCase("ups") == false &&
						objType.equalsIgnoreCase("crac") == false) {
					
					// Ignore it 
					continue;
				}
				
				int synced = 0;
				for(Map<String,Object> dbmap : dbDTO.getData()) {
					if (cadHandle == dbmap.get("cadHandle")) {
						synced = 1;
						break;
					}
				}
				if (synced == 0) {
					unsyncedList.add(map);
					log.debug("Unsynced cadhanle: " + cadHandle);
				}
			}
			
			// Map for this item
			Map<String,Object> itemMap = new HashMap<String,Object>();
			itemMap.put("itemId", new Long(item.getItemId()));
			
			// Step 1: If the item already has a CAD association and the item's site is changed, then the old association is removed.
			itemMap.put("cadName", "");
			itemMap.put("cadHandle", "");
			
			for (Map<String,Object> map : unsyncedList) {
				String cadName = (String)map.get("cadName");
				String cadHandle = (String)map.get("cadHandle");
				
				// Step 2.1 the object in the drawing has the same name as the item
				if (cadName != null && cadName.equalsIgnoreCase(item.getItemName())) {
					itemMap.put("cadName", cadName);
					itemMap.put("cadHandle", cadHandle);
					// Done
					break;

				} else 	if (item.getClassLookup() != null && 
						item.getSubclassLookup() != null &&
						item.getItemName().toLowerCase().endsWith("-cabinet") &&
						item.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.CABINET &&
						item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.CONTAINER &&
						item.getItemName().equalsIgnoreCase(cadName + "-cabinet")) {
						//	Step 2.2 All of the following are true
						//		1. The item name ends with "-cabinet"
						//		2. The item's class is "Cabinet"
						//		3. The item's subclass is "Container"
						//		4. the object in the drawing has the same name as the item with "-cabinet" removed
						itemMap.put("cadName", cadName);
						itemMap.put("cadHandle", cadHandle);
						break;
				}
				// Next one
			}
			log.info("Sync result: " + itemMap.get("itemId") + " " + itemMap.get("cadName") + " " + itemMap.get("cadHandle"));
			
			CadHandleDTO rstDTO = new CadHandleDTO();
			rstDTO.setLocationId(String.valueOf(locationid));
			List<Map> rstList = new ArrayList<Map>();
			rstList.add(itemMap);
			rstDTO.setData(rstList);
			
			// Update database
			setCadHandles(rstDTO);						
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }
    
    public Map getParameters(String locationid) throws DataAccessException {
    	Map<String,Object> map = new HashMap<String,Object>();
    	
    	Session session = this.sessionFactory.getCurrentSession();
    	
		String sql = "SELECT parameter FROM tblsettings WHERE setting='FloorMapDrawingUnits' AND siteid=:locationId";
		SQLQuery sqlQuery = session.createSQLQuery( sql );
		sqlQuery.setLong("locationId", Long.parseLong(locationid));
		String drawingUnits = (String)sqlQuery.uniqueResult();
		
		map.put("drawingUnits", drawingUnits);

		sql = "SELECT parameter FROM tblsettings WHERE setting='FloorMapGridLetterLabeling' AND siteid=:locationId";
		sqlQuery = session.createSQLQuery( sql );
		sqlQuery.setLong("locationId", Long.parseLong(locationid));
		String gridLetterLabeling = (String)sqlQuery.uniqueResult();
		map.put("gridLetterLabeling", gridLetterLabeling);
		
		
		sql = "select is_xaxis_numbers from dct_locations where location_id = :locationId";
		sqlQuery = session.createSQLQuery( sql );
		sqlQuery.setLong("locationId", Long.parseLong(locationid));
		Boolean isXNums = (Boolean)sqlQuery.uniqueResult();
		map.put("isXNums", isXNums);
    	
    	return map;
    }
    
	public Map getPIQLocationInfo(String locationId) {
		Map returnMap=new HashMap();
		
		Long id=Long.parseLong(locationId);
		
		String piqHost = locationDAO.getPiqHostByLocationId(id);
		String piqUsername = locationDAO.getPiqSettingByLocationId(id, SystemLookup.ApplicationSettings.PIQ_USERNAME);
		String piqPassword = locationDAO.getPiqSettingByLocationId(id, SystemLookup.ApplicationSettings.PIQ_PASSWORD);
		
		DataCenterLocationDetails location=locationDAO.getLocation(id);
		String piqId=location.getPiqId();
		String dwgFileName=location.getDwgFileName();
		
		if(piqId != null) {
			piqId=piqId.replaceAll("DataCenter:","data_center-");
			piqId=piqId.replaceAll("Room:","room-");
			piqId=piqId.replaceAll("Floor:","floor-");
		}
		
		returnMap.put("piqHost",piqHost);
		returnMap.put("piqId",piqId);
		returnMap.put("piqUsername",piqUsername);
		returnMap.put("piqPassword",piqPassword);
		returnMap.put("dwgFileName",dwgFileName);
		
		return returnMap;
	}
    
	private void uploadFloorMapToPIQ(String locationId, String filePath) throws DataAccessException {
		String piqHost = locationDAO.getPiqHostByLocationId(Long.parseLong(locationId));
		
//		if (piqSyncFloorMap != null)
//			try {
//				piqSyncFloorMap.uploadFloorMap(piqHost, filePath);
//			} catch (RemoteDataAccessException e) {
//				// TODO Auto-generated catch block
//				// RT: Please include the inserting of an event to event log here.
//				//     The Event Home is injected to this class.
//				e.printStackTrace();
//			}
	}
    
}
