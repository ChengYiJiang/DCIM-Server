package com.raritan.tdz.controllers.mobile;


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

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.item.json.MobileSearchItemInfo;
import com.raritan.tdz.location.home.LocationHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.service.PaginatedService;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * @author Chengyi 
 * 
 */

@Controller
@RequestMapping("/v1/mobile")
public class MobileSearchController extends BaseController{

	private final Logger log = Logger.getLogger(MobileSearchController.class);
	
	@Autowired(required=true)
	private ItemHome itemHome;
	
	@Autowired(required=true)
	private ChassisHome chassisHome;
	
	@Autowired(required=true)
	private LocationHome locationHome;
	
	@Autowired(required=true)
	private PaginatedService paginatedService;
	
	
	
	
	/**
	 *  QUERY ITEM
	 *   
	 *  API call (example):
	 * 	http://localhost:8080/dcTrackApp/api/v1/mobile/search?search=BRH1&locations=1,2,3"
	 */
	
	/*
	@RequestMapping(value = "/search", method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> queryItem(
			@RequestParam(value="search", required=true) String searchString,
			@RequestParam(value="locations", required=true) String locationString,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: queryItem() searchString=" + searchString );
		}
		checkAcceptMediaType(request);
		
		Map<String, Set<MobileSearchItemInfo>> map = new HashMap<String, Set<MobileSearchItemInfo>>();		
		
		
		try{			
			Set<MobileSearchItemInfo> items = itemHome.searchItemsWithLocationExtAPI( searchString, locationString, RESTAPIUserSessionContext.getUser());
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
	*/
	
}
