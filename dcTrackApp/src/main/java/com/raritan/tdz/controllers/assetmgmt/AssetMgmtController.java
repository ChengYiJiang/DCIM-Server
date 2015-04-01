/**
 * 
 */
package com.raritan.tdz.controllers.assetmgmt;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.item.json.MobileSearchItemInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.service.PaginatedService;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * @author bozana
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/v1/items")
public class AssetMgmtController extends BaseController {
	
	private final Logger log = Logger.getLogger(AssetMgmtController.class);
	
	@Autowired(required=true)
	private ItemHome itemHome;
	
	@Autowired(required=true)
	private PaginatedService paginatedService;
	

	
	/**
	 *  QUERY ITEM
	 *   
	 *  API call (example):
	 * 	http://localhost:8080/dcTrackApp/api/v1/items?search=BRH1&locations=1,2,3"
	 */
	@RequestMapping(params = {"search"}, method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> queryItem(
			@RequestParam(value="search", required=true) String searchString,			
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: queryItem() searchString=" + searchString );
		}
		checkAcceptMediaType(request);
		
		try{
			Set<BasicItemInfo>items = itemHome.searchItemsExtAPI( searchString, RESTAPIUserSessionContext.getUser());
			ret.put("items", items);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
	}
	
	
	@RequestMapping(value = "/search", method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> queryItem(
			@RequestParam(value="search", required=true) String searchString,
			@RequestParam(value="locations", required=true) String locationString,
			@RequestParam(value="limit", required=true) int limit,
			@RequestParam(value="offset", required=true) int offset,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: queryItem() searchString=" + searchString );
		}
		checkAcceptMediaType(request);
		
		
		Map<String, Set<MobileSearchItemInfo>> map = new HashMap<String, Set<MobileSearchItemInfo>>();	
		
		try{
			Set<MobileSearchItemInfo> items = itemHome.searchItemsWithLocationExtAPI( searchString, locationString, RESTAPIUserSessionContext.getUser(), limit, offset);
			//seperate by location here
			//Map<String, Set<MobileSearchItemInfo>> map = new HashMap<String, Set<MobileSearchItemInfo>>();
			for (MobileSearchItemInfo item : items){
				if (map.containsKey(item.getTiLocationRef()))
					map.get(item.getTiLocationRef()).add(item);
				else{
					Set<MobileSearchItemInfo> itemsSet = new LinkedHashSet<MobileSearchItemInfo>();
					itemsSet.add(item);
					map.put(item.getTiLocationRef(), itemsSet);
				}
			}
			
			ret.put("items", map);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
	}
	
	
	
	
	/**
	 *  GET ALL ITEMS
	 *  
	 *  API call:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/items
	 *  
	 *  or in eclipse env
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items
	 * 	http://localhost:8080/dcTrackApp/api/v1/items
	 */
	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody Object getAllItems(
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		if(log.isDebugEnabled()){
			log.debug("REST GET call: getAllItems() invoked");
		}
		checkAcceptMediaType(request);
		ListResultDTO retval = null;
		try{
			return itemHome.getAllItemsExtAPI("itemList");
		}catch(DataAccessException e){
			log.error("PaginatedService throwed DataAccessException");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}catch(Exception e){
			log.error("Unknown exception thrown from PagaintedService");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}
	
	/**
	 *  GET ITEM CHILDREN
	 * 
	 *  API call (example):
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/items/10/children?include_container=true
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/children?include_parent=true&include_grandchildren=true
	 *  
	 *  NOTE: include_container parameter is optional. If not set, then respose will not include only info about children
	 *        and container info will be excluded.
	 */
	@RequestMapping(value="/{id}/children", method=RequestMethod.GET)
	public @ResponseBody Object getItemChildren(HttpServletRequest request, 
			@PathVariable("id") long itemId,
			@RequestParam(value="include_parent", required=false) Boolean includeContainer,
			@RequestParam(value="include_grandchildren", required=false) Boolean includeGrandchildren,
			HttpServletResponse response) throws BusinessValidationException, Throwable {
		if(log.isDebugEnabled()){
			log.info("REST GET call: getItem(" + itemId +") invoked");
		}
		checkAcceptMediaType(request);
		
		try{
			if( includeContainer == null ) includeContainer = false;
			if( includeGrandchildren == null ) includeGrandchildren = false;
			return itemHome.getItemsChildrenExtAPI(itemId, includeContainer, includeGrandchildren, RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
	}
	

	/**
	 *  GET ITEM 
	 * 
	 *  API call (example):
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/items/10
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getItem(HttpServletRequest request, @PathVariable("id") long itemId, HttpServletResponse response) throws BusinessValidationException, Throwable {
		if(log.isDebugEnabled()){
			log.info("REST GET call: getItem(" + itemId +") invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			Map<String, Object>itemInfo = itemHome.getItemDetailsExtAPI(itemId, RESTAPIUserSessionContext.getUser());
			ret.put("item", itemInfo);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		return ret;
	}
	
	/**
	 *   UPDATE ITEM
	 *   
	 *   API call (example):
	 *   
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -c@json_file https://admin:raritan@192.168.51.220/api/v1/items/10
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10

	 */
	@RequestMapping(value="/{id}", method=RequestMethod.PUT)
	public @ResponseBody Map<String,Object> updateItem(HttpServletRequest request, @PathVariable("id") long id, @RequestBody Map<String, Object> itemDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST PUT call: updateItem() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			Map<String, Object>itemInfo = itemHome.updateItemExtAPI(id, itemDetails, RESTAPIUserSessionContext.getUser());
			if(itemInfo != null  && itemInfo.size() > 0 ) {
				ret.put("item", itemInfo);
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
	 *   ADD NEW ITEM
	 *   
	 *   Api call example
	 *   
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -c@json_file https://admin:raritan@192.168.51.220/api/v1/items
	 *  
	 *   or in eclipse env:
	 *  
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items
	 *
	 */
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody Map<String,Object> createItem(HttpServletRequest request, @RequestBody Map<String, Object> itemDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST POST call: createItem() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			long id = -1L;

			Map<String, Object>itemInfo = itemHome.saveItemExtAPI(id, itemDetails, 
					RESTAPIUserSessionContext.getUser(), SystemLookup.ItemOrigen.API);
			if(itemInfo != null  && itemInfo.size() > 0 ) {
				ret.put("item", itemInfo);
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
	 *   DELETE ITEM
	 *   
	 *   API call (example):
	 *   
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE https://admin:raritan@192.168.51.220/api/v1/items/10
	 *  
	 *   or in eclipse env:
	 *  
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10  
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteItem(HttpServletRequest request, @PathVariable("id") long id) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST DELETE call: deleteItem() invoked");
		}
		checkAcceptMediaType(request);
		
		try{
			itemHome.deleteItem(id, true, RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
			if (log.isDebugEnabled())
				e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if (log.isDebugEnabled())
				e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
	}
	

	/**
	 *  GET DATA PORT
	 * 
	 *  API call (example):
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/items/10/dataports/1
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/dataports/1
	 */
	@RequestMapping(value="/{itemId}/dataports/{portId}", method=RequestMethod.GET)
	public @ResponseBody Map<String, Object> getDataPort(HttpServletRequest request, @PathVariable("itemId") long itemId, @PathVariable("portId") long portId, HttpServletResponse response) throws BusinessValidationException, Throwable {
		if(log.isDebugEnabled()){
			log.info("REST GET call: getDataPort(" + itemId +", " + portId  + ") invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			DataPortDTO retDTO = itemHome.getItemPortDetailsExtAPI(itemId, portId, RESTAPIUserSessionContext.getUser());
			ret.put("dataport", retDTO);
		}catch(BusinessValidationException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		return ret;
	}
	
	/**
	 *   UPDATE DATA PORT
	 *   
	 *   API call (example):
	 *   
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -c@json_file https://admin:raritan@192.168.51.220/api/v1/items/10/dataports/1
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/dataports/1

	 */
	@RequestMapping(value="/{itemId}/dataports/{portId}", method=RequestMethod.PUT)
	public @ResponseBody Map<String,Object> updateDataPort(HttpServletRequest request, @PathVariable("itemId") long itemId, 
			@PathVariable("portId") long portId, @RequestBody DataPortDTO portDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST PUT call: updateDataPort(" + itemId + ", " + portId + ") invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();
		
		try{
			DataPortDTO retDTO = itemHome.updateItemDataPortExtAPI(itemId, portId, portDetails, RESTAPIUserSessionContext.getUser());
			if( retDTO != null ) {
				ret.put("dataport", retDTO);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
		}catch(BusinessInformationException e){

			DataPortDTO retDTO = (DataPortDTO) e.getDomainObject();
			if(retDTO != null ) {
				ret.put("dataport", retDTO);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
			
			e.printStackTrace();
			// ignore the business information exception, this is only to provide information

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
	 *   ADD NEW DATA PORT
	 *   
	 *   Api call example
	 *   
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -c@json_file https://admin:raritan@192.168.51.220/api/v1/items/10/dataports
	 *  
	 *   or in eclipse env:
	 *  
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/dataports
	 *
	 */
	@RequestMapping(value="/{itemId}/dataports", method=RequestMethod.POST)
	public @ResponseBody Map<String,Object> createDataPort(HttpServletRequest request, @PathVariable("itemId") long itemId, @RequestBody DataPortDTO portDetails) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST POST call: createItem() invoked");
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		checkAcceptMediaType(request);

		try{	
			DataPortDTO retDTO = itemHome.createItemDataPortExtAPI(itemId, portDetails, RESTAPIUserSessionContext.getUser());
			if(retDTO != null ) {
				ret.put("dataport", retDTO);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
		}catch(BusinessInformationException e){
			
			DataPortDTO retDTO = (DataPortDTO) e.getDomainObject();
			if(retDTO != null ) {
				ret.put("dataport", retDTO);
			}else{
				throw(new DCTRestAPIException("Unknown server error", HttpStatus.INTERNAL_SERVER_ERROR));
			}
			
			e.printStackTrace();
			// ignore the business information exception, this is only to provide information
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
	 *   DELETE ITEM PORT
	 *   
	 *   API call (example):
	 *   
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE https://admin:raritan@192.168.51.220/api/v1/items/10/dataports/1
	 *  
	 *   or in eclipse env:
	 *  
	 *   curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/dataports/1
	 */
	@RequestMapping(value="/{itemId}/dataports/{portId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteDataPort(HttpServletRequest request, @PathVariable("itemId") long itemId, 
			@PathVariable("portId") long portId,
			@RequestParam(value="skipValidation",defaultValue="false",required=false) boolean skipValidation) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST DELETE call: deleteDataPort() invoked");
		}
		checkAcceptMediaType(request);

		
		try{
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
			if (log.isDebugEnabled())
				e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if (log.isDebugEnabled())
				e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
	}
	
	
	/**
	 *  GET ALL DATA PORTS
	 *  
	 *  API call:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/v1/items/10/dataports
	 *  
	 *  or in eclipse env
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/items/10/dataports
	 * 	http://localhost:8080/dcTrackApp/api/v1/items
	 */
	@RequestMapping(value="/{itemId}/dataports", method=RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllDataPorts(HttpServletRequest request, @PathVariable("itemId") long itemId,
			HttpServletResponse response) throws Throwable {
		if(log.isDebugEnabled()){
			log.debug("REST GET call: getAllDataPorts() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> retval = new HashMap<String, Object>();
		
		try{
			Object portList = itemHome.getAllItemDataPortsExtAPI(itemId, RESTAPIUserSessionContext.getUser());
			retval.put("dataports", portList);
			
		}catch(BusinessValidationException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(DataAccessException e){
			log.error("PaginatedService throwed DataAccessException");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}catch(Exception e){
			log.error("Unknown exception thrown from PagaintedService");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR));
		}
		return retval;
	}
	
	@RequestMapping(value="/{itemId}/secondTest", method=RequestMethod.GET)
	public @ResponseBody Map<String, Object> secondTest(HttpServletRequest request, @PathVariable("itemId") long itemId,
			HttpServletResponse response) throws Throwable {
		if(log.isDebugEnabled()){
			log.debug("REST GET call: getAllDataPorts() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> retval = new HashMap<String, Object>();
		
		try{
			Object portList = itemHome.getAllItemDataPortsExtAPI(itemId, RESTAPIUserSessionContext.getUser());
			retval.put("second test", portList);
			
		}catch(BusinessValidationException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(DataAccessException e){
			log.error("PaginatedService throwed DataAccessException");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}catch(Exception e){
			log.error("Unknown exception thrown from PagaintedService");
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR));
		}
		return retval;
	}
}
