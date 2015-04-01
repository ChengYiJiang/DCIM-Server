package com.raritan.tdz.controllers.assetmgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.home.IPServiceRESTAPI;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.json.LookupAdapter;

@Controller
@RequestMapping("/v1/lookups")
public class LookupController extends BaseController {
	
	private final Logger log = Logger.getLogger(LookupController.class);
	
	@Autowired(required = true)
	private LookupAdapter lookupAdapter;
	
	@Autowired(required = true)
	private IPServiceRESTAPI ipServiceRESTAPI;

    @Autowired(required = true)
    private ItemService itemService;

	/**
	 *  QUERY LKU info
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/lookups/lku?id=<lku_id>&type_name=<lku_type_name>&value=<lku_value>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/lookups/lku?id=<lku_id>&type_name=<lku_type_name>&value=<lku_value>
	 *  
	 *  examples of usage:
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/api/v1/lookups/lku?id=1049
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/api/v1/lookups/lku?typeName=DOMAIN
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/api/v1/lookups/lku?value=nta.com
	 *  
	 */
	@RequestMapping(value="lkus", method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> queryLku(
			@RequestParam(value="id", required=false) String lkuId,
			@RequestParam(value="typeName", required=false) String lkuTypeName,
			@RequestParam(value="value", required=false) String lkuValue,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: queryLks() lksId=" + lkuId + ", lkuTypeName=" + lkuTypeName + ", lkuValue=" + lkuValue );
		}
		checkAcceptMediaType(request);
		
		try{
			//TODO: Do we need combination of two properties to be send ?
			if( lkuId != null ) ret = lookupAdapter.getLkuByIdAPI(Long.valueOf(lkuId));
			else if( lkuValue != null ) ret = lookupAdapter.getLkuByValueAPI( lkuValue );			
			else if( lkuTypeName != null) ret = lookupAdapter.getLkuByTypeAPI(lkuTypeName);
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		//TODO: Implement get all lku's when needed
		if ( lkuId == null && lkuValue == null && lkuTypeName == null ) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;
	}
	
	/**
	 *  QUERY LKU info
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/lookups/netmask?id=<id>&mask=<mask>&cidr=<cidr>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/netmask?id=<id>&mask=<mask>&cidr=<cidr>
	 *  
	 *  examples of usage:
	 *    
	 */
	@RequestMapping(value="netmasks",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> queryLku(
			@RequestParam(value="id", required=false) Long id,
			@RequestParam(value="mask", required=false) String mask,
			@RequestParam(value="cidr", required=false) Long cidr,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query netmask() id=" + id + ", mask=" + mask + ", cidr=" + cidr );
		}
		checkAcceptMediaType(request);

		try{
			if( id != null ){
				ret = ipServiceRESTAPI.getNetMaskByIdExtAPI(id);
			}else if( cidr != null ){
				ret = ipServiceRESTAPI.getNetMaskByCidrExtAPI(cidr);
			}else if( mask != null ){
				ret = ipServiceRESTAPI.getNetMaskByMaskExtAPI(mask);
			} else{
				ret = ipServiceRESTAPI.getAllNetMasksExtAPI(); 
			}

		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		return ret;
	}

    /**
     * QUERY System Lookup info
     * 
     * API GENERAL FORMAT:
     * http://localhost:8080/dcTrackApp/api/v1/lookups/systemLookup?lkpValue=<lkpValue>
     *  or remote:
     * https://<ip_addr>/dcTrackApp/api/v1/systemLookup?lkpValue=<lkpValue>
     * 
     * examples of usage:
     * curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/dcTrackApp/api/v1/lookups/systemLookup?lkpValue=CLASS
     */
    @RequestMapping(value = "systemLookup", method = RequestMethod.GET)
    public @ResponseBody
    List<SystemLookupDTO> getSystemLookup(
            @RequestParam(value = "lkpValue", required = true) String lkpValue,
            HttpServletRequest request, HttpServletResponse response)
            throws Throwable {

        List<SystemLookupDTO> ret = new ArrayList<SystemLookupDTO>();
        if (log.isDebugEnabled()) {
            log.debug("REST GET call: getSystemLookup() lkpValue=" + lkpValue);
        }
        checkAcceptMediaType(request);

        try {
            ret = itemService.getSystemLookup(lkpValue);

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                log.error(e.getMessage());
            }
            throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }

        return ret;
    }

    /**
     * QUERY "class" lookup info for Item Details Report
     * 
     * API GENERAL FORMAT:
     * http://localhost:8080/dcTrackApp/api/v1/lookups/systemLookup/class
     *  or remote:
     * https://<ip_addr>/dcTrackApp/api/v1/systemLookup/class
     * 
     * examples of usage:
     * curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/dcTrackApp/api/v1/lookups/systemLookup/class
     */
    @RequestMapping(value = "systemLookup/class", method = RequestMethod.GET)
    public @ResponseBody
    List<SystemLookupDTO> getClassForItemDetailsReport(
            HttpServletRequest request, HttpServletResponse response)
            throws Throwable {

        List<SystemLookupDTO> ret = new ArrayList<SystemLookupDTO>();
        if (log.isDebugEnabled()) {
            log.debug("REST GET call: getClassForItemDetailsReport()");
        }
        checkAcceptMediaType(request);

        try {
            List<SystemLookupDTO> temp = itemService.getSystemLookup("CLASS");

            // Available classes for Item Details Report, the same as Items List besides "Cabinet".
            Set<Long> availableSet = new HashSet<Long>();
            availableSet.add(SystemLookup.Class.CRAC);
            availableSet.add(SystemLookup.Class.DATA_PANEL);
            availableSet.add(SystemLookup.Class.DEVICE);
            availableSet.add(SystemLookup.Class.NETWORK);
            availableSet.add(SystemLookup.Class.FLOOR_OUTLET); // Power Outlet
            availableSet.add(SystemLookup.Class.PROBE);
            availableSet.add(SystemLookup.Class.RACK_PDU);
            availableSet.add(SystemLookup.Class.UPS);

            for (SystemLookupDTO dto : temp) {
                if (availableSet.contains(dto.getData())) {
                    ret.add(dto);
                }
            }

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                log.error(e.getMessage());
            }
            throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }

        return ret;
    }

    /**
     * QUERY "status" lookup info for Item Details Report
     * 
     * API GENERAL FORMAT:
     * http://localhost:8080/dcTrackApp/api/v1/lookups/systemLookup/status
     *  or remote:
     * https://<ip_addr>/dcTrackApp/api/v1/systemLookup/status
     * 
     * examples of usage:
     * curl -H Content-Type:application/json -H Accept:application/json -s -i -k https://192.168.60.28/dcTrackApp/api/v1/lookups/systemLookup/status
     */
    @RequestMapping(value = "systemLookup/status", method = RequestMethod.GET)
    public @ResponseBody
    List<SystemLookupDTO> getStatusForItemDetailsReport(
            HttpServletRequest request, HttpServletResponse response)
            throws Throwable {

        List<SystemLookupDTO> ret = new ArrayList<SystemLookupDTO>();
        if (log.isDebugEnabled()) {
            log.debug("REST GET call: getStatusForItemDetailsReport()");
        }
        checkAcceptMediaType(request);

        try {
            List<SystemLookupDTO> temp = itemService
                    .getSystemLookup("ITEM_STATUS");

            // Ignore statuses for Item Details Report.
            Set<Long> ignoreSet = new HashSet<Long>();
            ignoreSet.add(SystemLookup.ItemStatus.ALL);
            ignoreSet.add(SystemLookup.ItemStatus.HIDDEN);
            ignoreSet.add(SystemLookup.ItemStatus.TO_BE_REMOVED);

            for (SystemLookupDTO dto : temp) {
                if (! ignoreSet.contains(dto.getData())) {
                    ret.add(dto);
                }
            }

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                log.error(e.getMessage());
            }
            throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }

        return ret;
    }
}