package com.raritan.tdz.piq.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.PIQInfoJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;

public class PIQSyncPIQVersionImpl extends PIQRestClientBase implements PIQSyncPIQVersion  {
	
	public PIQSyncPIQVersionImpl() {}
	
	
	public PIQSyncPIQVersionImpl(ApplicationSettings appSettings) throws DataAccessException {
		super(appSettings);
	}

	public void syncPIQVersion () {
		try {
			// get version from PIQ using REST call
			String piqVersion = getPIQVersion();
			
			// get version in our database
			String piqVersionInDB = appSettings.getProperty(Name.PIQ_VERSION);
			
			if (piqVersion != null && piqVersionInDB != null &&
					!piqVersionInDB.equals(piqVersion)) {
				// piq version differs (either PIQ is upgraded or changed to different PIQ installation) 
				appSettings.setProperty(Name.PIQ_VERSION, piqVersion);
			} else if (piqVersionInDB == null && piqVersion != null){
				appSettings.setProperty(Name.PIQ_EVENT_QUERY_DATE, piqVersion);
			}
		} catch (RemoteDataAccessException e) {
			// do nothing 
		} catch (DataAccessException e ) {
			// do nothing 
		}
	}

	@Override
	public String getPIQVersion() throws RemoteDataAccessException {
		PIQInfoJSON piqInfoJson = null;
		if (isAppSettingsEnabled())  {
			piqInfoJson = getPIQInfoJSON();
		}
		return ((piqInfoJson != null) ? piqInfoJson.getPowerIqVersion() : null);
	}

	@Override
	public PIQInfoJSON getPIQInfoJSON() throws RemoteDataAccessException {
		ResponseEntity<?> restResult = null;
		PIQInfoJSON pdujson = null;
		
		restResult = doRestGet("v2/system_info", null, PIQInfoJSON.class);
		
		if (restResult != null){
			pdujson = (PIQInfoJSON)restResult.getBody();
		}
		return pdujson;
	}
	
}
