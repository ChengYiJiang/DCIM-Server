package com.raritan.tdz.controllers.assetmgmt;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.location.home.LocationHome;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

@Controller
@RequestMapping("/v1/locations")
public class LocationMgmtController extends BaseController  {

	private final Logger log = Logger.getLogger(LocationMgmtController.class);

	@Autowired(required=true)
	private LocationHome locationHome;
	
	/**
	 *  GET LOCATION 
	 * 
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/locations/10
	 *  or eclipse env:locationId
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/locations/10
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getLocation(HttpServletRequest request, @PathVariable("id") long id, HttpServletResponse response) throws BusinessValidationException, Throwable {
		if(log.isDebugEnabled()){
			log.info("REST GET call: getLocation(" + id +") invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			Map<String, Object>locationInfo = locationHome.getLocationDetailsExt(id, RESTAPIUserSessionContext.getUser());
			ret.put("location", locationInfo);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		return ret;
	}
	
	/**
	 *  GET ALL LOCATION
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/locations
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/locations
	 */
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody Object getAllLocations(
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		
		if(log.isDebugEnabled()){			
			log.debug("REST GET call: getAllLocations()");
		}
		checkAcceptMediaType(request);
		
		try{
			return locationHome.getAllLocationsDetailsExt(RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}	
	}
	
	
	/**
	 *   DELETE LOCATION
	 *   
	 *   API call (example):
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/locations/10
	 *   or eclipse env:
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/locations/10
	 *   
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteLocation(HttpServletRequest request, @PathVariable("id") long id) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST DELETE call: deleteLocation(" + id + ") invoked");
		}
		checkAcceptMediaType(request);
		
		//TODO: implement & return a map of strings instead of a String
		throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));		
	}
	
	/**
	 *   UPDATE LOCATION
	 *   
	 *  API call (example):  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -c@json_file https://admin:raritan@192.168.51.220/api/v1/locations/10
	 *  or in eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/locations/10
	 *
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.PUT)
	public @ResponseBody Map<String,Object> updateLocation(HttpServletRequest request, @PathVariable("id") long id, @RequestBody Map<String, Object> locationDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST PUT call: updateLocation() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			Map<String, Object>locationInfo = locationHome.updateLocationExtAPI(id, locationDetails, RESTAPIUserSessionContext.getUser());
			if(locationInfo != null  && locationInfo.size() > 0 ) {
				ret.put("location", locationInfo);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
		}catch(BusinessValidationException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch (IllegalArgumentException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
		
	}
	

	/**
	 *   ADD NEW LOCATION
	 *   
	 *   API call (example): 
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -c@json_file https://admin:raritan@192.168.51.220/api/v1/locations
	 *   or in eclipse env:
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/locations
	 *
	 */
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Map<String,Object> createLocation(HttpServletRequest request, @RequestBody Map<String, Object> locationDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST POST call: createLocation() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			long id = -1L;

			Map<String, Object>locationInfo = locationHome.saveLocationExtAPI(id, locationDetails, RESTAPIUserSessionContext.getUser());
			if(locationInfo != null  && locationInfo.size() > 0 ) {
				ret.put("location", locationInfo);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
		}catch(BusinessValidationException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch (IllegalArgumentException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));	
		}catch(Exception e){
			e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
		
	}
}
