package com.raritan.tdz.ip.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.util.RequiredFieldsValidator;


public class IPServiceImpl implements IPServiceRESTAPI {

	@Autowired(required=true)
	private JSONIPAdapter jsonNetworkAdapter;

	@Autowired(required=true)
	private IPHome ipHome;
	
	@Autowired(required=true)
	private RequiredFieldsValidator jsonAssgSaveOpReqFieldsValidator;
	
	/*** REST APIs for netmask ***/
	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByIdExtAPI(Long id)
			throws BusinessValidationException {

		List<NetMask> netMasksList = ipHome.getNetMaskById(id);
		return jsonNetworkAdapter.adaptNetmaskListToJSONArray(netMasksList);
	}

	
	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByMaskExtAPI(String mask)
			throws BusinessValidationException {

		List <NetMask> netMasksList = ipHome.getNetMaskByMask(mask);
		return jsonNetworkAdapter.adaptNetmaskListToJSONArray(netMasksList);		
	}

	
	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getNetMaskByCidrExtAPI(Long cidr)
			throws BusinessValidationException {

		List <NetMask> netMasksList = ipHome.getNetMaskByCidr(cidr);
		return jsonNetworkAdapter.adaptNetmaskListToJSONArray(netMasksList);
	}

	
	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getAllNetMasksExtAPI() throws BusinessValidationException {

		List <NetMask> netMasksList = ipHome.getAllNetMasks();
		return jsonNetworkAdapter.adaptNetmaskListToJSONArray(netMasksList);
	}

	
	/*** REST APIs for ipaddress ***/
	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getIpAddressByIdExtAPI(Long ipAddressId) 
			throws BusinessValidationException {

		IPAddressDetails ipAddress = ipHome.getIpAddress(ipAddressId);

		return jsonNetworkAdapter.adaptIpAddressToJSONArray(ipAddress);
	}

	
	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getAllIpAddressesForItemExtAPI(Long itemId) 
			throws BusinessValidationException {

		List<IPAddressDetails> ips = ipHome.getAllIpAddressesForItem(itemId);

		return jsonNetworkAdapter.adaptIpAddressListToJSONArray(ips);
	}


	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getIpAddressByNameExtAPI(String ipAddress, Long locationId)
			throws BusinessValidationException {
		List<IPAddressDetails> ipDetails = ipHome.getIpAddressByName(ipAddress, locationId);

		return jsonNetworkAdapter.adaptIpAddressListToJSONArray(ipDetails);
	}

	
	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getAllIpAddressesForDataPortExtAPI(Long dataPortId)
			throws BusinessValidationException {
		List<IPAddressDetails> ips = ipHome.getAllIpAddressesForDataPort(dataPortId);
		return jsonNetworkAdapter.adaptIpAddressListToJSONArray(ips);
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getAvailableManagedIpAddressesExtAPI(Long subnetId) throws BusinessValidationException {
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		List<String> availableIps = ipHome.getAvailableManagedIpAddresses(subnetId, errors);
		return jsonNetworkAdapter.adaptAvailabeIpsToJSONArray(availableIps);
	}

	/*** REST API from ipassignments ***/
	@Override
	@Transactional(readOnly=true)
	public Map<String, Object> getIpAssignmentsForItemExtAPI(Long itemId) 
			throws BusinessValidationException {

		List<IPTeaming> ips = ipHome.getAllIpAssignmentsForItem(itemId);

		return jsonNetworkAdapter.adaptIPTeamingArrayToJSONAssignmtArray(ips);
	}

	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getIpAssignmentsForIpAddress(String ipAddress,
			Long locationId) throws BusinessValidationException {
		List<IPAddressDetails> ips = ipHome.getIpAddressByName(ipAddress, locationId);
		return jsonNetworkAdapter.adaptIpAddressListToJSONAssignmtsArray(ips);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Map<String, Object> saveIpAssignmentExtAPI(
			JSONIpAssignment ipAssignment, String createAs,
			Boolean isGateway, UserInfo userInfo)
					throws BusinessValidationException {
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		return ipHome.saveIpAssignementExt(ipAssignment, createAs, isGateway, userInfo, errors);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public Map<String, Object> editIpAssignmentExtAPI(
			JSONIpAssignment ipAssignment, String createAs, Boolean isGateway, UserInfo userInfo)
			throws BusinessValidationException {
		if( ipAssignment == null ){
			throw new IllegalArgumentException("ipAssignement is null, please provide ipassignement");
		}
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		return ipHome.editIpAssignmentExtAPI(ipAssignment, createAs, isGateway, userInfo, errors);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteIpAssignmentExtAPI(Long teamId, 
			UserInfo userInfo) throws BusinessValidationException {

		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		ipHome.deleteIpAssignment(teamId, userInfo, errors);

	}


	/*** REST APIs for subnet ***/
	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getSubnetForIPAndLocationExtAPI(String ipAddress, Long locationId) throws BusinessValidationException{
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		List<Networks> networksList = ipHome.getSubnetForIPAndLocation(ipAddress, locationId, errors);
		return jsonNetworkAdapter.adaptNetworksToJSONArray(networksList);
	}


	@Override
	@Transactional( readOnly=true )
	public Map<String, Object> getAllSubnetsInLocationExtAPI(Long locationId) throws BusinessValidationException {
		MapBindingResult errors = getErrorsObject(IPAddressDetails.class);
		List<Networks> networksList = ipHome.getAllSubnetsInLocation(locationId, errors);
		return jsonNetworkAdapter.adaptNetworksToJSONArray(networksList);
	}


	/*** PRIVATE METHODS START HERE ***/

	private MapBindingResult getErrorsObject(Class<?> errorBindingClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, errorBindingClass.getName());
		return errors;
	}


}

