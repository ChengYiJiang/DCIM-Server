package com.raritan.tdz.ip.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.dao.IPTeamingDAO;
import com.raritan.tdz.ip.dao.NetMaskDAO;
import com.raritan.tdz.ip.dao.NetworksDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon.ValidationParams;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.BusinessExceptionHelper;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.RequiredFieldsValidator;

public class IPHomeImpl implements IPHome {

	@Autowired(required=true)
	IPAddressDetailsDAO ipAddressDetailsDAO;

	@Autowired(required=true)
	DataPortDAO dataPortDAO;

	@Autowired(required=true)
	IPTeamingDAO ipTeamingDAO;

	@Autowired(required=true)
	private NetMaskDAO netMaskDAO;

	@Autowired(required=true)
	NetworksDAO networksDAO;

	@Autowired(required=true)
	Validator ipAssgDeleteOpValidator;

	@Autowired(required=true)
	Validator ipAssgSaveOpValidator;

	@Autowired(required=true)
	Validator ipAssgEditOpValidator;

	@Autowired(required=true)
	IPAddressValidatorCommon ipAddressValidatorCommon;

	@Autowired(required=true)
	IPUtils ipUtils;

	@Autowired(required=true)
	private RequiredFieldsValidator jsonAssgSaveOpReqFieldsValidator;

	@Autowired(required=true)
	private JSONIPAdapter jsonNetworkAdapter;

	@Autowired(required=true)
	private RequiredFieldsValidator jsonAssgEditOpReqFieldsValidator;
	
	@Autowired(required=true)
	private Validator itemIpAddrAndProxyValidator;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;

	private final Logger log = Logger.getLogger(this.getClass());

	private ResourceBundleMessageSource messageSource;

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	//This is the list of code that are supposed to be sent as warning not as default error codes
	protected List<String> validationWarningCodes;

	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<NetMask> getNetMaskById(Long id) throws BusinessValidationException {

		List<NetMask> netMasksList = null;
		if( id != null ) netMasksList = netMaskDAO.getById(id);

		return netMasksList;
	}

	@Override
	public List<NetMask> getNetMaskByMask(String mask) throws BusinessValidationException {

		List <NetMask> netMasksList = null;
		if( mask != null ) netMasksList = netMaskDAO.getByMask(mask);
		return netMasksList;		
	}

	@Override
	public List<NetMask> getNetMaskByCidr(Long cidr) throws BusinessValidationException {

		List <NetMask> netMasksList = null;

		if( cidr != null ) netMasksList = netMaskDAO.getByCidr(cidr);
		return netMasksList;
	}

	@Override
	public  List<NetMask> getAllNetMasks() throws BusinessValidationException {

		List <NetMask> netMasksList = null;

		netMasksList = netMaskDAO.getAll();
		return netMasksList;
	}

	@Override
	public IPAddressDetails getIpAddress(Long ipAddressId) 
			throws BusinessValidationException {

		IPAddressDetails ipAddress = null;

		if( ipAddressId != null ){
			ipAddress = ipAddressDetailsDAO.read(ipAddressId);
		}

		return ipAddress;
	}

	@Override
	public List<IPAddressDetails> getIpAddressByName(String ipAddress, Long locationId) 
			throws BusinessValidationException {

		List<IPAddressDetails> ipDetailsList = new ArrayList<IPAddressDetails>();

		if( ipAddress != null && locationId != null ){
			ipDetailsList = ipAddressDetailsDAO.getIpAddressByName(ipAddress, locationId);
		}

		return ipDetailsList;
	}

	@Override
	public List<IPAddressDetails> getAllIpAddressesForItem(Long itemId) 
			throws BusinessValidationException {

		List<IPAddressDetails> ips = null;
		if( itemId != null ){
			ips = ipAddressDetailsDAO.getIpAddressForItem(itemId);
		}
		return ips;
	}

	@Override
	public List<IPTeaming> getAllIpAssignmentsForItem(Long itemId) 
			throws BusinessValidationException {

		List<IPTeaming> teams = null;
		if( itemId != null ){
			teams = ipAddressDetailsDAO.getIpAssignmentsForItem(itemId);
		}
		return teams;
	}
	
	@Override
	public boolean dataPortContainsIpAddress(String ipAddress, Long dataPortId)
			throws BusinessValidationException {
		boolean retval = false;
		List<IPAddressDetails> allIps = getAllIpAddressesForDataPort(dataPortId);
		if( allIps != null ) for( IPAddressDetails ip : allIps){
			if( ipAddress != null && ipAddress.equals(ip.getIpAddress())){
				retval = true;
				break;
			}
		}
		return retval;
	}
	
	
	
	/**
	 * - Required fields should be already validated if present and NOT null
	 * - In saveIpAssignment we validate fields values (if correct) and if they match business logic
	 */
	@Override
	public IPTeaming saveIpAssignment(IPTeaming team, UserInfo userInfo, Errors errors) 
			throws BusinessValidationException{

		assert( errors.hasErrors() == false); //should be already handled

		Map<ValidationParams, Object> paramsMap = new HashMap<ValidationParams, Object>();
		paramsMap.put(ValidationParams.OP_TYPE, ValidationParams.OP_SAVE_IPASSIGNMENT);
		paramsMap.put(ValidationParams.PARAM_IPTEAM, team);
		paramsMap.put(ValidationParams.PARAM_USER_INFO, userInfo);

		ipAssgSaveOpValidator.validate( paramsMap, errors);

		if( errors.hasErrors()) throwBusinessValidationException(errors);
		DataPort dp = team.getDataPort();
		IPAddressDetails newDomainIpAddr = team.getIpAddress();

		Long locationId = ipAddressValidatorCommon.validateAndGetLocationId(dp.getPortId(), errors);
		Long retIpId = null;

		//either team with existing assignment, or dup it
		List<IPAddressDetails> allExistingIPAddresses = 
				ipAddressDetailsDAO.getIpAddressByName(newDomainIpAddr.getIpAddress(), locationId);
		if( allExistingIPAddresses.size() == 1 && newDomainIpAddr.getIsTeamingAllowed() == true ){
			//team ip and data port
			IPAddressDetails existingIPAddress = allExistingIPAddresses.get(0);
			retIpId = existingIPAddress.getId();
			teamIPAddressAndDataPort( existingIPAddress, team);
			ipAddressDetailsDAO.merge(existingIPAddress);
		}else{//create new ip and team it with port
			teamIPAddressAndDataPort(newDomainIpAddr, team);
			retIpId = ipAddressDetailsDAO.create(newDomainIpAddr);
		}
		IPTeaming retTeam = ipTeamingDAO.getTeamForIpAndDataPort(retIpId, dp.getPortId());
		return retTeam;
	}


	private IPAddressDetails teamIPAddressAndDataPort( IPAddressDetails ipAddrDetails, IPTeaming team ){
		Set<IPTeaming> teams = ipAddrDetails.getIpTeaming();
		if( teams == null ) teams = new HashSet<IPTeaming>();
		ipAddrDetails.setIpTeaming(teams);
		team.setIpAddress(ipAddrDetails);
		teams.add(team);

		return ipAddrDetails;
	}

	@Override
	public void deleteIpAssignment(Long teamId, UserInfo userInfo,
			Errors errors) throws BusinessValidationException {
		Map<ValidationParams, Object> paramsMap = new HashMap<ValidationParams, Object>();
		paramsMap.put(ValidationParams.OP_TYPE, ValidationParams.OP_DELETE_ASSIGNMENT);
		paramsMap.put(ValidationParams.PARAM_TEAM_ID, teamId);
		paramsMap.put(ValidationParams.PARAM_USER_INFO, userInfo);
		ipAssgDeleteOpValidator.validate(paramsMap, errors);

		if( errors.hasErrors()) throwBusinessValidationException(errors);

		IPTeaming team = ipTeamingDAO.read(teamId);

		IPAddressDetails ipAddressInfo = team.getIpAddress();
		if( ipAddressInfo.getIpTeaming().size() > 0 ){
			ipAddressInfo.getIpTeaming().remove(team);
		}
		ipTeamingDAO.delete(team);

		if( ipAddressInfo.getIpTeaming().size() == 0 ){
			ipAddressDetailsDAO.delete(ipAddressInfo);
		}
	}

	@Override
	public List<IPAddressDetails> getAllIpAddressesForDataPort(Long dataPortId) throws BusinessValidationException{
		List<IPAddressDetails> ips = null;
		if( dataPortId != null ){
			ips = ipAddressDetailsDAO.getIpAddressForDataPort(dataPortId);
		}
		return ips;
	}

	public Map<String, Object> editIpAssignmentExtAPI(JSONIpAssignment ipAssignment, String createAs, Boolean isGateway, 
			UserInfo userInfo, Errors errors)
			throws BusinessValidationException {

		//validate that required fields are present in JSON object and are NOT null
		jsonAssgEditOpReqFieldsValidator.validate(ipAssignment, errors);
		if( errors.hasErrors() ) throwBusinessValidationException(errors);
	
		IPTeaming team = jsonNetworkAdapter.adaptJSONIpAssgToExistingIPTeaming(ipAssignment, createAs, isGateway,  errors);
		if( errors.hasErrors() ) throwBusinessValidationException(errors);
		
		List<IPTeaming> retTeams = editIpAddress(team, userInfo, errors);
		return jsonNetworkAdapter.adaptIPTeamingArrayToJSONAssignmtArray(retTeams);

	}

	/**
	 * legend:
	 * 	=  represents two or more duplicate ip addresses (i.e. 2 or more records in tblipaddresses table
	 *            for each one there is only one corresponding record in tblipteaming table)
	 *  <  represents shared ipaddress between two or more assignements (i.e. 1 record in tblipaddresses
	 *            table shared between 2 or more records in tblipteaming table)
	 *  -  represents single ipaddress (i.e. there is only one record in tblipaddresse table and only one
	 *                corresponding record in tblipteaming table)
	 * 
	 * This is the list of cases we have to take care of when changing ipaddress field if there is already
	 * one or more exiting ipaddress that has the same name:
	 * 1:    =  to  =  (move dup ip to another dup ip - dup it )
	 * 2:    =  to  <  (move dup ip to teamed ip - team it)
	 * 3:    <  to  =  (moved teamed ip to dup ip - dup it)
	 * 4:    <  to  <  (move teamed ip from one team to another - team it)
	 * 5:    =  to  -  (move dup ip to another single ip - team it)
	 * 6:    =  to  -  (move dup ip to another single ip - dup it)
	 * 7:    <  to  -  (move teamed ip to another single ip - team it)
	 * 8:    <  to  -  (move teamed ip to another single ip - dup it)
	 * 9:    -  to  -  (move single ip to another single ip - team it)
	 * 10:   -  to  -  (move single ip to another single ip - dup it)
	 * 11:   -  to  =  (move single ip to another dup ip - dup ip)
	 * 12:   -  to  <  (move single ip to another teamed ip - team it)
	 * 
	 *  savedSrcIpAddress - ipaddress we modify
	 *  newIpAddressName - new value we have to set up in for the savedIpAddress
	 *  destIPAddresses - other ipaddresses in the location that have the same value as the one we try to assign
	 */
	@Override
	public List<IPTeaming> editIpAddress(IPTeaming team, UserInfo userInfo, Errors errors) throws BusinessValidationException {
		Map<ValidationParams, Object> paramsMap = new HashMap<ValidationParams, Object>();
		paramsMap.put(ValidationParams.OP_TYPE, ValidationParams.OP_EDIT_ASSIGNMENT);
		paramsMap.put(ValidationParams.PARAM_IPTEAM, team);
		paramsMap.put(ValidationParams.PARAM_USER_INFO, userInfo);

		ipAssgEditOpValidator.validate( paramsMap, errors);
		if( errors.hasErrors()) throwBusinessValidationException(errors);

		IPAddressDetails savedSrcIpAddress = ipAddressDetailsDAO.read(team.getIpAddress().getId());		
		String savedSrcIpAddressName = savedSrcIpAddress.getIpAddress();
		String newIpAddressName = team.getIpAddress().getIpAddress();

		List<IPTeaming> retArray = new ArrayList<IPTeaming>();

		Long locationId = ipUtils.getLocationId(team.getDataPort());
		List<IPAddressDetails> destIPAddresses = ipAddressDetailsDAO.getIpAddressByName(newIpAddressName, locationId);

		if( savedSrcIpAddressName.equals(newIpAddressName) || destIPAddresses.size() == 0){
			//just modify parameters in existing ip
			IPTeaming newTeam = modifyIPAddressDetails(team, savedSrcIpAddress);
			retArray.add(newTeam);
		}else{//handle teaming and dupping
			boolean teamIt = team.getIpAddress().getIsTeamingAllowed().booleanValue();
			boolean dupIt = team.getIpAddress().getIsDuplicatIpAllowed().booleanValue();

			IPUtils.IPStatus srcIPStatus = ipUtils.getIPStatus(savedSrcIpAddressName, locationId);	
			IPUtils.IPStatus dstIPStatus = ipUtils.getIPStatus(newIpAddressName, locationId);
			IPAddressDetails destIPAddress = destIPAddresses.get(0);

			// cases; 1), 11) and 6)
			if( dupIt && dstIPStatus != IPUtils.IPStatus.TEAMED_IP && srcIPStatus != IPUtils.IPStatus.TEAMED_IP){
				//moving one dup to another dup - equal modifying only parameters
				IPTeaming newTeam = modifyIPAddressDetails(team, savedSrcIpAddress);
				retArray.add(newTeam);
			}else if( teamIt && (dstIPStatus == IPUtils.IPStatus.TEAMED_IP ||  dstIPStatus == IPUtils.IPStatus.SINGLE_IP)
					&& srcIPStatus != IPUtils.IPStatus.TEAMED_IP){ 
				//move dup or single ip to a team - cases: 2), 5), 9) and 12)
				retArray = moveDupIpAddressToTeam(team, savedSrcIpAddress, destIPAddress);
			}else if( teamIt && srcIPStatus == IPUtils.IPStatus.TEAMED_IP && dstIPStatus != IPUtils.IPStatus.DUP_IP ){
				//move entire old team to a new one - cases: 4) and 7)
				retArray = moveTeamedIpAddressesToAnotherTeam(savedSrcIpAddress, destIPAddress);
			}else{//move entire old team to duplicate, i.e. make a dup ip for each member of the old team
				//cases: 3), 8), 10)
				retArray = moveTeamedIPAddressesToDups(team.getIpAddress(), savedSrcIpAddress);
			}
		}
		return retArray;
	}

	private IPTeaming  modifyIPAddressDetails(IPTeaming team,
			IPAddressDetails savedIpAddress) {
		IPTeaming retTeam = null;
		//moving from one set of dups to another set of dups, just update records, teaming remains
		setNewIpAddressData( savedIpAddress, team.getIpAddress() );
		IPAddressDetails retIpInfo = ipAddressDetailsDAO.merge(savedIpAddress);
		if( retIpInfo != null ) retTeam=ipTeamingDAO.getTeamForIpAndDataPort(retIpInfo.getId(), team.getDataPort().getPortId());
		
		return retTeam;
	}

	private  List<IPTeaming> moveDupIpAddressToTeam(IPTeaming team,
			IPAddressDetails savedIpAddress, IPAddressDetails destIPAddress) {
		//move single or dup ip into a new team
		List<IPTeaming> retArray = new ArrayList<IPTeaming>();
		ipAddressDetailsDAO.delete(savedIpAddress); //delete, since we will share new ip with dst team
		teamIPAddressAndDataPort(destIPAddress, team);
		IPAddressDetails retIpInfo = ipAddressDetailsDAO.merge(destIPAddress);
		if( retIpInfo != null ) team=ipTeamingDAO.getTeamForIpAndDataPort(retIpInfo.getId(), team.getDataPort().getPortId());
		retArray.add(team);
		return retArray;
	}

	
	private List<IPTeaming> moveTeamedIpAddressesToAnotherTeam(
			IPAddressDetails savedIpAddress, IPAddressDetails destIPAddress) {
		List<IPTeaming> retArray = new ArrayList<IPTeaming>();
		Set<IPTeaming> srcTeams = savedIpAddress.getIpTeaming();
		Set<IPTeaming> dstTeams = destIPAddress.getIpTeaming();
		Set<IPTeaming> modifiedTeams = new HashSet<IPTeaming>();
		
		assert( srcTeams.size() > 0 );
		//check if the same port is present in both teams. Those that are unique put in modifiedTeams
		//and remove them from srcTeam so at the end srcTeam contains only dups ports
		Set<Long> uniquePorts = new HashSet<Long>();
		for( IPTeaming t : dstTeams ) uniquePorts.add(t.getDataPort().getPortId());
				
		for( Iterator<IPTeaming> i = srcTeams.iterator(); i.hasNext(); ){
			IPTeaming t = i.next();
			if( uniquePorts.add(t.getDataPort().getPortId()) != false ){ //it is unique
				i.remove();
				t.setIpAddress(destIPAddress);
				modifiedTeams.add(t);
			}		
		}
		dstTeams.addAll(modifiedTeams);
		
		if( srcTeams.size() > 0 ){ //delete those that are duplicate
			for( IPTeaming t : srcTeams ){
				srcTeams.remove(t);
				ipTeamingDAO.delete(t);
			}
		}
		
		IPAddressDetails retIpInfo = ipAddressDetailsDAO.merge(destIPAddress);
		savedIpAddress.setIpTeaming(null);
		ipAddressDetailsDAO.delete(savedIpAddress);

		for(IPTeaming t : modifiedTeams){
			IPTeaming nt = ipTeamingDAO.getTeamForIpAndDataPort(retIpInfo.getId(), t.getDataPort().getPortId());
			retArray.add(nt);
		}
		return retArray;
	}

	private List<IPTeaming> moveTeamedIPAddressesToDups(IPAddressDetails newIpAddress,
			IPAddressDetails savedIpAddress) {
		List<IPTeaming> retArray = new ArrayList<IPTeaming>();
		
		Set<IPTeaming> newTeams = savedIpAddress.getIpTeaming();
		for( IPTeaming t : newTeams ){
			IPAddressDetails tmpIpAddress = new IPAddressDetails();
			setNewIpAddressData( tmpIpAddress, newIpAddress);	
			teamIPAddressAndDataPort(tmpIpAddress, t);
			Long retIpId = ipAddressDetailsDAO.create(tmpIpAddress);

			IPTeaming retTeam = ipTeamingDAO.getTeamForIpAndDataPort( retIpId, t.getDataPort().getPortId());
			retArray.add(retTeam);
		}
		savedIpAddress.setIpTeaming(null);
		ipAddressDetailsDAO.delete(savedIpAddress);
		return retArray;
	}

	private void setNewIpAddressData(IPAddressDetails oldIpAddress,
			IPAddressDetails newIpAddress) {

		if( newIpAddress.getComment() != null ){
			oldIpAddress.setComment(newIpAddress.getComment());
		}

		if( newIpAddress.getIpAddress() != null ){
			oldIpAddress.setIpAddress(newIpAddress.getIpAddress());
		}
		if( newIpAddress.getDnsName() != null ){
			oldIpAddress.setDnsName(newIpAddress.getDnsName());
		}
		if( newIpAddress.getGateway() != null){
			oldIpAddress.setGateway(newIpAddress.getGateway());
		}
		if( newIpAddress.getIsVirtual() != null ){
			oldIpAddress.setIsVirtual(newIpAddress.getIsVirtual());
		}
		if( newIpAddress.getIsDuplicatIpAllowed() != null ){
			oldIpAddress.setIsDuplicatIpAllowed(newIpAddress.getIsDuplicatIpAllowed());
		}
		if( newIpAddress.getIsIpBeingGatewayAllowed() != null ){
			oldIpAddress.setIsIpBeingGatewayAllowed(newIpAddress.getIsIpBeingGatewayAllowed());
		}
		if( newIpAddress.getDomain() != null ){
			oldIpAddress.setDomain(newIpAddress.getDomain());
		}
		if( newIpAddress.getMask() != null){
			oldIpAddress.setMask(newIpAddress.getMask());
		}
		//data ports should not be sent
	}


	/*** Subnet APIs implementaitons ***/
	@Override
	public List<Networks> getSubnetForIPAndLocation(String ipAddress, Long locationId, Errors errors) 
			throws BusinessValidationException{
		ipAddressValidatorCommon.validateIpAddressFormat(ipAddress, errors);
		ipAddressValidatorCommon.validateLocationId(locationId, errors);
		if( errors.hasErrors()){
			throwBusinessValidationException(errors);
		}
		//managed ip (will have 1 record only if there)
		List<Networks> retval = networksDAO.getNetworkForIpAndLocation(ipAddress, locationId);

		if( retval == null || retval.size() < 1 ){ //not in managed list, search non managed list
			retval = ipAddressDetailsDAO.getSubnetForIpAndLocation(ipAddress, locationId);
		}
		return retval;
	}

	@Override
	public List<Networks> getAllSubnetsInLocation(Long locationId,
			Errors errors) throws BusinessValidationException {
		ipAddressValidatorCommon.validateLocationId(locationId, errors);
		if( errors.hasErrors()){
			throwBusinessValidationException(errors);
		}
		return networksDAO.getAllNetworksForLocation(locationId);
	}

	@Override
	public List<String> getAvailableManagedIpAddresses(Long subnetId, Errors errors) throws BusinessValidationException{

		Networks network = ipAddressValidatorCommon.validateAndGetSubnet(subnetId,  errors);
		if( errors.hasErrors()){
			throwBusinessValidationException(errors);
		}
		List<String> allUsedIpsInLocation = ipAddressDetailsDAO.getAllUsedIPAddressesInSubnet(subnetId);

		//convert ip string into a number, then just loop and convert number into string
		long ipStart = ipToLong(network.getSubnet());
		long ipEnd = ipToLong(network.getSubnetEnd());
		List<String> allAvailableIps = new ArrayList<String>();
		//exclude start and end
		for( long i= ipStart+1; i< ipEnd; i ++){
			String n = longToIp(i);
			allAvailableIps.add(n);
		}
		if(log.isDebugEnabled()){
			log.debug("== Total number of ips in subnet: " + subnetId + " =" + allAvailableIps.size() );
			log.debug("== Number of used ips in subnet: " + subnetId + " =" +  allUsedIpsInLocation.size());
		}
		allAvailableIps.removeAll(allUsedIpsInLocation);
		if(log.isDebugEnabled()){
			log.debug("== Number of availble ips in subnet: " + subnetId + " =" + allAvailableIps.size() );
		}

		return allAvailableIps;
	}



	private String longToIp(long ip) {

		return ((ip >> 24) & 0xFF) + "." 
				+ ((ip >> 16) & 0xFF) + "." 
				+ ((ip >> 8) & 0xFF) + "." 
				+ (ip & 0xFF);

	}
	private long ipToLong(String ipAddress) {

		long result = 0;
		String[] ipAddressInArray = ipAddress.split("\\.");
		for (int i = 3; i >= 0; i--) {
			long ip = Long.parseLong(ipAddressInArray[3 - i]);

			//left shifting 24,16,8,0 and bitwise OR
			//1. 192 << 24
			//1. 168 << 16
			//1. 1   << 8
			//1. 2   << 0
			result |= ip << (i * 8);
		}	 
		return result;
	}


	public void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		//Throw exception if there is validation error or warning
		if (e.getValidationErrors().size() > 0 || e.getValidationWarnings().size() > 0) throw e;

	}

	@Override
	public IPTeaming getTeam(Long ipId, Long dataPortId) {
		return ipTeamingDAO.getTeamForIpAndDataPort(ipId, dataPortId);
	}

	@Override
	public Map<String, Object> saveIpAssignementExt(
			JSONIpAssignment ipAssignment, String createAs, Boolean isGateway,
			UserInfo userInfo, Errors errors) throws BusinessValidationException {
		if( ipAssignment  == null ){
			throw new IllegalArgumentException("ipAssignement is null, please provide ipassignement");
		}
		
		//validate that required fields are present in JSON object and are NOT null
		jsonAssgSaveOpReqFieldsValidator.validate(ipAssignment, errors);
		if( errors.hasErrors() ) throwBusinessValidationException(errors);
		
		IPTeaming team = jsonNetworkAdapter.adaptJSONIpAssgToNewIPTeaming(ipAssignment, createAs, isGateway, errors);
		if( errors.hasErrors() ) throwBusinessValidationException(errors);
		
		team.getIpAddress().setId(-1);
		
		IPTeaming retTeam = saveIpAssignment(team, userInfo, errors);
		return jsonNetworkAdapter.adaptIPTeamingToJSONAssignmtArray(retTeam);

	}

	@Override
	public JSONIpAssignment saveIpAddressAndProxyForDataPort(DataPort dataPort,
			String ipAddress, String proxyIndex, UserInfo userInfo, Errors errors) throws BusinessValidationException {
		JSONIpAssignment retVal = null;
		
		Errors localErrors = businessExceptionHelper.getErrorObject(errors);
		
		try {
			if( dataPort == null ){ //port does not exists or there are no data ports
				if( ipAddress != null || proxyIndex != null ){
					log.error("No data port; cannot set ip addr & proxyIndex");
					Object[] errorArgs = {};
					localErrors.reject("IpAddressValidator.portName", errorArgs, 
						"Specified port does not exists, or item does not have data ports");
					return retVal;
				}else return retVal;
			}
			Item item = dataPort.getItem();
			assert(item != null);
			if( proxyIndex == null || proxyIndex.equals(item.getGroupingNumber())){
				if( ipAddress == null || dataPortContainsIpAddress( ipAddress, dataPort.getPortId())){
					//nothing to do; data same as in DB
					return retVal;
				}
			}
			JSONIpAssignment ipAssignmentNew = jsonNetworkAdapter.adaptDataPortDetailsToJsonIP(dataPort, 
					ipAddress, localErrors);
					
			if( localErrors.hasErrors()) throwBusinessValidationException(localErrors) ;
			if( proxyIndex == null ) proxyIndex = item.getGroupingNumber();
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("JSONIpAssignment", ipAssignmentNew);
			args.put("proxyIndex", proxyIndex);
		
			itemIpAddrAndProxyValidator.validate(args, localErrors);
			if( localErrors.hasErrors() ) throwBusinessValidationException(localErrors);
			
			Long teamId = ipAssignmentNew.getId();
			ipAddress = ipAssignmentNew.getIpAddress();
			if( teamId != null &&  teamId.longValue() > 0){
				if(ipAddress != null && ipAddress.length() > 0){
					editIpAssignmentExtAPI(ipAssignmentNew, "team", true, userInfo, localErrors);
				}else{
					deleteIpAssignment(teamId, userInfo, localErrors);
				}
			}else if( ipAddress != null && ipAddress.length() > 0){
				saveIpAssignementExt( ipAssignmentNew, "team", true, userInfo, localErrors);
			}
			
			if( localErrors.hasErrors() ) throwBusinessValidationException(localErrors);
			
			item.setGroupingNumber(proxyIndex);
			retVal = ipAssignmentNew;
			
			List<IPAddressDetails> allIps = getAllIpAddressesForDataPort(dataPort.getPortId());
			assert( allIps.size() <= 1 );
			if( allIps.size() > 0 ){
				retVal.setIpAddress(allIps.get(0).getIpAddress());
				retVal.setPortName(dataPort.getPortName());
			}
	
			return retVal;
		}
		finally {
			
			if (localErrors.hasErrors()) {
				errors.addAllErrors(localErrors);
			}
			
		}
		
	}

}
