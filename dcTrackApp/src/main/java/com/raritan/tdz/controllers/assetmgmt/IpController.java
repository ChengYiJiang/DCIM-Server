package com.raritan.tdz.controllers.assetmgmt;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.home.IPServiceRESTAPI;
import com.raritan.tdz.ip.json.JSONIpAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

@Controller
@RequestMapping("/v1/ip")
public class IpController extends BaseController {

	private final Logger log = Logger.getLogger(this.getClass());

	@Autowired(required = true)
	private IPServiceRESTAPI ipServiceRESTAPI;

	/**
	 *  QUERY ipaddresses by 
	 *  	- itemId or
	 *  	- dataPortId or 
	 *  	- name and location 
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipaddresses?id=<id>&dataPortId=<dataPort>&itemId=<itemId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipaddresses?id=<id>&dataPortId=<dataPortId>&itemId=<itemId>
	 *  
	 *  examples of usage:
	 *  get all ipaddresses 
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i 
	 *  -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ip/ipaddresses?itemId=233
	 *    
	 */
	@RequestMapping(value="ipaddresses",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getIpAddress(
			@RequestParam(value="id", required=false) Long ipId,
			@RequestParam(value="dataPortId", required=false) Long portId,
			@RequestParam(value="itemId", required=false) Long itemId,
			@RequestParam(value="ipAddress", required=false) String ipAddress,
			@RequestParam(value="locationId", required=false) Long locationId,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query ipaddresses() id=" + ipId + ", portId=" + portId + ", itemId=" + itemId );
		}
		checkAcceptMediaType(request);

		try{
			if( ipId != null ){
				ret = ipServiceRESTAPI.getIpAddressByIdExtAPI(ipId);
			}else if( portId != null ){
				ret = ipServiceRESTAPI.getAllIpAddressesForDataPortExtAPI(portId);
			}else if( itemId != null ){
				ret = ipServiceRESTAPI.getAllIpAddressesForItemExtAPI(itemId);
			}else if( ipAddress != null && ipAddress.length() > 0){
				ret = ipServiceRESTAPI.getIpAddressByNameExtAPI(ipAddress, locationId);
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		//TODO: Implement get all ip's when needed
		if ( ipId == null && portId == null && itemId == null && (ipAddress == null)) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;
	}

	/**
	 *  QUERY ipassignments by:
	 *  - itemId or 
	 *  - ipaddress and location
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipassignments?itemId=<itemId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipassignments?itemId=<itemId>
	 *  
	 *  examples of usage:
	 *  get all ipassignments 
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i 
	 *  -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ip/ipassignments?itemId=233
	 *    
	 */
	@RequestMapping(value="ipassignments",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getIpAssignments(
			@RequestParam(value="itemId", required=false) Long itemId,
			@RequestParam(value="ipAddress", required=false) String ipAddress,
			@RequestParam(value="locationId", required=false) Long locationId,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query ipassignments(), itemId=" + itemId );
		}
		checkAcceptMediaType(request);

		try{
			if( itemId != null ){
				ret = ipServiceRESTAPI.getIpAssignmentsForItemExtAPI(itemId);
			}else if( ipAddress != null && locationId != null ){
				ret = ipServiceRESTAPI.getIpAssignmentsForIpAddress( ipAddress, locationId);
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		if( itemId == null && (ipAddress == null || locationId == null )) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;
	}

	/**
	 *  QUERY available, managed ip addresses from the ip address pool
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipaddresses/availableIps?subnetId=<subnetId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipaddresses/availableIps?subnetId=<subnetId>
	 *  
	 *  examples of usage:
	 *  get all available managed ipaddresses 
	 *  curl -H Content-Type:application/json -H Accept:application/json -s -i 
	 *  -k http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ip/ipaddresses/availableIps?subnetId=2
	 *    
	 */
	@RequestMapping(value="ipaddresses/availableIps",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getIpAddress(
			@RequestParam(value="subnetId", required=true) Long subnetId,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query available ipaddresses() for subnet =" + subnetId );
		}
		checkAcceptMediaType(request);

		try{
			if( subnetId != null ){
				ret = ipServiceRESTAPI.getAvailableManagedIpAddressesExtAPI(subnetId);
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}

		if ( subnetId == null) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;
	}


	/**
	 *  CREATE an ipassignment
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipassignments?createAs=<team|duplicate|null>&isGateway=<true|false>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipassignments?createAs=<team|duplicate|null>&isGateway=<true|false>
	 *  
	 *  examples of usage:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST 
	 *  	-d@json_file https://admin:raritan@192.168.51.220/api/v1/ipassignments?createAs=team&isGateway=false
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST 
	 *  	-d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ipassignments?createAs=team&isGateway=false   
	 */
	@RequestMapping(value="ipassignments", method=RequestMethod.POST)
	public @ResponseBody Map<String,Object> createIPAssignment(HttpServletRequest request, 
			@RequestParam(value="createAs", required=true) String createAs,
			@RequestParam(value="isGateway", required=true) Boolean isGateway,
			@RequestBody JSONIpAssignment ipAssignmentInfo) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST POST call: createIpAddress() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();

		try{
			if( createAs != null &&  isGateway!= null ){
				ret = ipServiceRESTAPI.saveIpAssignmentExtAPI(ipAssignmentInfo, createAs, isGateway, RESTAPIUserSessionContext.getUser());
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch (IllegalArgumentException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));	
		}catch(Exception e){
			e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		if( createAs == null && isGateway == null ) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;

	}




	/**
	 *  MODIFY existing ipassignment
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipassignments?createAs=<team|duplicate|null>&isGateway=<true|false>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipassignments?createAs=<team|duplicate|null>&isGateway=<true|false>
	 *  
	 *  examples of usage:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT 
	 *  	-d@json_file https://admin:raritan@192.168.51.220/api/v1/ipassignments?createAs=team&isGateway=false
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X PUT 
	 *  	-d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ipassignments?createAs=team&isGateway=false   
	 */
	@RequestMapping(value="ipassignments", method=RequestMethod.PUT)
	public @ResponseBody Map<String,Object> modifyIPAssignment(HttpServletRequest request, 
			@RequestParam(value="createAs", required=true) String createAs,
			@RequestParam(value="isGateway", required=true) Boolean isGateway,
			@RequestBody JSONIpAssignment ipAssignment ) throws Throwable {
		if(log.isDebugEnabled()){
			log.info("REST POST call: modifyIpAddress() invoked");
		}
		checkAcceptMediaType(request);
		Map<String, Object> ret = new HashMap<String, Object>();

		try{	
			ret = ipServiceRESTAPI.editIpAssignmentExtAPI(ipAssignment, createAs, isGateway, RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
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
	 *  DELETE ipassignment
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/ipassignment?id=<ipassignementId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/ipassignment?id=<ipassignementId>
	 *  
	 *  examples of usage:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE 
	 *  	https://admin:raritan@192.168.51.220/api/v1/ipassignment?id=2
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X DELETE 
	 *  	http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/ipassignment?id=2    
	 */
	@RequestMapping(value="ipassignments", method=RequestMethod.DELETE)
	public @ResponseBody void deleteIPAssignment(HttpServletRequest request,
			@RequestParam(value="id", required=true) Long teamId)throws Throwable {

		if(log.isDebugEnabled()){
			log.info("REST POST call: deleteIpAddress() invoked ");
		}
		checkAcceptMediaType(request);

		try{
			ipServiceRESTAPI.deleteIpAssignmentExtAPI( teamId, RESTAPIUserSessionContext.getUser());
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch (IllegalArgumentException e){
			e.printStackTrace();
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));	
		}catch(Exception e){
			e.printStackTrace();
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
	}

	/**
	 *  QUERY subnet
	 *   
	 *  API GENERAL FORMAT:
	 * 	http://localhost:8080/dcTrackApp/api/v1/ip/subnets?ipAddress=<ipId>&locationId=<locationId>
	 *   or remote:
	 *  https://<ip_addr>/api/v1/subnets?id=<ipId>&locationId=<locationId>
	 *  
	 *  examples of usage:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json"  
	 *  	https://admin:raritan@192.168.51.220/api/v1/subnets?ipId=539&locationId=1
	 *  
	 *  or in eclipse env:
	 *  
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json"  
	 *  	http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/v1/subnet?ipId=539&locationId=1    
	 */
	@RequestMapping(value="subnets",method=RequestMethod.GET)
	public @ResponseBody Map<String,Object> getSubnetInfo(
			@RequestParam(value="ipAddress", required=false) String ipAddress,
			@RequestParam(value="locationId", required=true) Long locationId,
			HttpServletRequest request,
			HttpServletResponse response) throws Throwable {		
		Map<String, Object> ret = new HashMap<String, Object>();
		if(log.isDebugEnabled()){
			log.debug("REST GET call: query subnet() ipAddress=" + ipAddress + ", locationId=" + locationId );
		}
		checkAcceptMediaType(request);

		try{
			if( ipAddress != null && locationId != null ){
				ret = ipServiceRESTAPI.getSubnetForIPAndLocationExtAPI(ipAddress, locationId);
			}else if( locationId != null){
				ret = ipServiceRESTAPI.getAllSubnetsInLocationExtAPI(locationId);
			}
		}catch(BusinessValidationException e){
			throw(new DCTRestAPIException(HttpStatus.BAD_REQUEST, e));
		}catch(Exception e){
			if( e.getMessage() != null && ! e.getMessage().isEmpty() ) log.error(e.getMessage());
			throw(new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		if( locationId == null ) throw(new DCTRestAPIException(HttpStatus.NOT_IMPLEMENTED));
		return ret;
	}

}
