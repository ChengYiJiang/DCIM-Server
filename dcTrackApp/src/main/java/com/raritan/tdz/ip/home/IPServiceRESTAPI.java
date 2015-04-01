package com.raritan.tdz.ip.home;

import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.json.JSONIpAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAssignment;

public interface IPServiceRESTAPI {
	
	/*** REST APIs for ipassignments ***/
	Map<String, Object> getIpAssignmentsForItemExtAPI(Long itemId) throws BusinessValidationException;
	Map<String, Object> getIpAssignmentsForIpAddress(String ipAddress, Long locationId) throws BusinessValidationException;
	Map<String, Object> saveIpAssignmentExtAPI(JSONIpAssignment ipAssignementInfo, String createAs,	Boolean isGateway, UserInfo user) throws BusinessValidationException;
	Map<String, Object> editIpAssignmentExtAPI(JSONIpAssignment ipAssignment,
			String createAs, Boolean isGateway, UserInfo user) throws BusinessValidationException;
	void deleteIpAssignmentExtAPI(Long teamId, UserInfo user) throws BusinessValidationException;
	
	/*** REST APIs for ipaddress ***/
	Map<String, Object> getIpAddressByIdExtAPI( Long ipAddressId ) throws BusinessValidationException;
	Map<String, Object> getIpAddressByNameExtAPI( String ipAddress, Long locationId ) throws BusinessValidationException;
	Map<String, Object> getAllIpAddressesForItemExtAPI( Long itemId ) throws BusinessValidationException;	 
	Map<String, Object> getAvailableManagedIpAddressesExtAPI(Long subnetId)throws BusinessValidationException;
	Map<String, Object> getAllIpAddressesForDataPortExtAPI( Long portId ) throws BusinessValidationException;
	
	/*** REST APIs for subnet ***/
	Map<String, Object> getSubnetForIPAndLocationExtAPI(String ipAddress, Long locationId) throws BusinessValidationException;
	Map<String, Object> getAllSubnetsInLocationExtAPI(Long locationId) throws BusinessValidationException;

	/*** REST APIs for netmask ***/
	Map<String, Object> getNetMaskByIdExtAPI( Long id ) throws BusinessValidationException;
	Map<String, Object> getNetMaskByMaskExtAPI( String mask ) throws BusinessValidationException;
	Map<String, Object> getNetMaskByCidrExtAPI( Long cidr ) throws BusinessValidationException;
	Map<String, Object> getAllNetMasksExtAPI() throws BusinessValidationException;
	
}
