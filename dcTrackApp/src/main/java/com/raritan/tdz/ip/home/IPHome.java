package com.raritan.tdz.ip.home;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.json.JSONIpAssignment;

public interface IPHome {
	/*** NetMask APIs ***/
	List<NetMask> getNetMaskById(Long id) throws BusinessValidationException;
	List <NetMask> getNetMaskByMask(String mask) throws BusinessValidationException;
	List <NetMask> getNetMaskByCidr(Long cidr) throws BusinessValidationException;
	List <NetMask> getAllNetMasks() throws BusinessValidationException;
	
	/*** IPAddress APIs ***/
	IPAddressDetails getIpAddress(Long ipAddressId) throws BusinessValidationException ;
	List<IPAddressDetails> getIpAddressByName(String ipAddress, Long locationId) throws BusinessValidationException ;
	List<IPAddressDetails> getAllIpAddressesForItem(Long itemId) throws BusinessValidationException;
	List<IPAddressDetails> getAllIpAddressesForDataPort(Long dataPortId) throws BusinessValidationException;
	List<String> getAvailableManagedIpAddresses(Long subnetId, Errors errors)throws BusinessValidationException;
	boolean dataPortContainsIpAddress(String ipAddress, Long dataPortId)throws BusinessValidationException;
	
	IPTeaming saveIpAssignment(IPTeaming team, UserInfo userInfo, Errors errors) throws BusinessValidationException;
	Map<String, Object> editIpAssignmentExtAPI(JSONIpAssignment ipAssignment, String createAs, Boolean isGateway, 
			UserInfo userInfo, Errors errors) throws BusinessValidationException;
	List<IPTeaming> editIpAddress(IPTeaming team, UserInfo userInfo, Errors errors) throws BusinessValidationException;
	void deleteIpAssignment(Long teamId, UserInfo userInfo, Errors errors) throws BusinessValidationException;
	Map<String, Object> saveIpAssignementExt(JSONIpAssignment ipAssignment,
			String createAs, Boolean isGateway, UserInfo userInfo, Errors errors) throws BusinessValidationException;
	
	JSONIpAssignment saveIpAddressAndProxyForDataPort(DataPort dataPort,
			String ipAddress, String proxyIndex, UserInfo userInfo,
			Errors errors) throws BusinessValidationException;
	
	/*** Subnet APIs ***/
	List<Networks> getSubnetForIPAndLocation(String ipAddress, Long locationId, Errors errors) throws BusinessValidationException;
	List<Networks> getAllSubnetsInLocation(Long locationId,	Errors errors) throws BusinessValidationException;
	IPTeaming getTeam(Long ipId, Long dataPortId);
	
	void throwBusinessValidationException(Errors errors) throws BusinessValidationException;
	List<IPTeaming> getAllIpAssignmentsForItem(Long itemId) throws BusinessValidationException;
	
	
}
