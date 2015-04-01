package com.raritan.tdz.ip.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
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
import com.raritan.tdz.ip.home.IPUtils;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.port.dao.DataPortDAO;

public class IPAddressValidatorCommon {
	public enum ValidationParams{
		OP_TYPE,
		OP_SAVE_IPASSIGNMENT,
		OP_DELETE_ASSIGNMENT,
		OP_EDIT_ASSIGNMENT,

		PARAM_USER_INFO,
		PARAM_DATA_PORT_ID,
		PARAM_IPTEAM,
		PARAM_IP_ADDRESS_ID,
		PARAM_TEAM_ID,
		PARAM_IP_ADDRESS_DETAILS, 
	};

	private static final String IPV4_REGEX = "^([1-9][0-9]?|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(0|[1-9][0-9]?|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
	static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

	@Autowired(required=true)
	DataPortDAO dataPortDAO;

	@Autowired(required=true)
	IPUtils ipUtils;


	@Autowired(required=true)
	IPTeamingDAO ipTeamingDAO;

	@Autowired(required=true)
	IPAddressDetailsDAO ipAddressDetailsDAO;

	@Autowired
	private LocationDAO locationDAO;

	@Autowired
	private NetworksDAO networksDAO;

	@Autowired
	private NetMaskDAO netMaskDAO;

	@Autowired(required=true)
	private UserLookupFinderDAO userLookupFinderDAO;

	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;

	private final Logger log = Logger.getLogger(this.getClass());

	public boolean isValidIp( String ip ){
		return IPV4_PATTERN.matcher(ip).matches();
	}

	public void validateLocationId( Long locationId, Errors errors ){
		if( locationDAO.read(locationId) == null ){
			log.error("location " + locationId + " is invalid, canot open");
			Object[] errorArgs = {};
			errors.reject("locationValidator.invalidLocation", errorArgs, null);
		}
	}

	public long validateAndGetLocationId(Item item, Errors errors){
		Long locationId = item.getDataCenterLocation() != null ? item.getDataCenterLocation().getDataCenterLocationId() : null;

		if( locationId == null ){
			log.error("Cannot find location for item: " + item.getItemId());
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidLocation", errorArgs, "Invalid Data Center Location Id");
		}
		return locationId;
	}

	public long validateAndGetLocationId(Long portId, Errors errors){
		DataPort port = validateAndGetDataPort( portId, errors );
		Item item = validateAndGetItem( port,  errors);
		Long locationId = item.getDataCenterLocation() != null ? item.getDataCenterLocation().getDataCenterLocationId() : null;
		if( locationId == null ){
			log.error("Cannot find location for item: " + item.getItemId());
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidLocation", errorArgs, "Invalid Data Center Location Id");
		}
		return locationId;
	}


	public Networks validateAndGetSubnet(long subnetId, Errors errors){
		Networks network = networksDAO.read(subnetId);

		if( network == null ){
			log.error("Cannot find subnet for subnetId: " + subnetId);
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.subnetId", errorArgs, "Invalid subnet id");
		}
		return network;
	}

	public IPAddressDetails validateAndGetIpAddress( Long ipId, Errors errors){
		IPAddressDetails ip = null;
		if( ipId == null || (ip = ipAddressDetailsDAO.read(ipId)) == null ){
			log.error("ipAddressId " + ipId + " is invalid, canot open");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.ipaddressId", errorArgs, "ipAddressId is invalid");
		}
		return ip;
	}

	public DataPort validateAndGetDataPort( Long dataPortId, Errors errors){
		DataPort dp = null;
		if( dataPortId == null || (dp=dataPortDAO.read(dataPortId)) == null ){
			log.error("dataPortId " + dataPortId + " is invalid, canot open");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.dataPortId", errorArgs, "dataPortId is invalid");
		}
		return dp;
	}

	public NetMask validateAndGetNetMaskByCidr( Long cidr, Errors errors ){
		NetMask retval = null;

		List<NetMask> maskList = netMaskDAO.getByCidr(cidr);
		if( maskList != null && maskList.size() == 1 ) retval = maskList.get(0);
		else{
			log.error("Cidr "+ cidr + "is invalid");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidCidr", errorArgs, "Cidr is invalid");	
		}
		return retval;
	}

	public LkuData validateAndGetDomainById( Long domainId, Errors errors){
		LkuData retval = null;
		List<LkuData> lkuList = userLookupFinderDAO.findById(domainId);
		if( lkuList != null && lkuList.size() == 1){
			retval = (LkuData)lkuList.get(0);
		}else{
			log.error("Domain id: " + domainId + " is invalid");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidDomainId", errorArgs, "Domain id is invalid");	
		}
		return retval;
	}

	public Item validateAndGetItem( DataPort dp, Errors errors){
		Item item = dp.getItem();
		if( item == null ){
			log.error("Data port " + dp.getPortId() + " may be corrupted, there is no item" );
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidItem", errorArgs, "Item does not exist");
		}
		return item;
	}

	public IPTeaming validateAndGetTeaming(Long teamId, Errors errors) {
		IPTeaming ipTeaming = null;
		try{
			ipTeaming = ipTeamingDAO.read(teamId);
			if( ipTeaming == null ){
				log.error("IPAssignment " +teamId + " does not exist" );
				Object[] errorArgs = {};
				errors.reject("IpAddressValidator.invalidIPAssignement", errorArgs, "IPAssignement does not exist");
			}else{
				if ( ipTeaming.getDataPort() == null || ipTeaming.getDataPort().getPortId() == null){
					log.error("dataPortId is invalid, canot open");
					Object[] errorArgs = {};
					errors.reject("IpAddressValidator.dataPortId", errorArgs, "dataPortId is invalid or does not exist");
				}
				if( ipTeaming.getIpAddress() == null || ipTeaming.getIpAddress().getIpAddress() == null ){
					log.error("ipAddress in team is null" );
					Object[] errorArgs = {};
					errors.reject("IpAddressValidator.invalidIPAssignement", errorArgs, "IPAssignement does not exist");
				}
			}
		}catch(Exception ex){
			log.error("IPAssignment " +teamId + "cannot be loaded. Data port could be deleted, but team remained. Check tblipteaming");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.dataError", errorArgs, "Cannot load info about team");
			ipTeaming = null;
		}
		return ipTeaming;
	}

	public void validateIpAddressFormat( String ipAddress, Errors errors ){
		if( ipAddress == null || isValidIp(ipAddress) == false ){
			log.error("ipaddress's: " + ipAddress + " - pattern is invalid");
			Object[] errorArgs = {};
			errors.reject( "IpAddressValidator.invalidIPAddressFormat", errorArgs, " has invalid format");
		}
	}

	public void validateGWFormat( String gateway, Errors errors ){
		if( gateway != null && gateway.length() > 0 && isValidIp(gateway) == false ){
			log.error("gateway's: " + gateway + " - pattern is invalid");
			Object[] errorArgs = {};
			errors.reject( "IpAddressValidator.invalidGatewayFormat", errorArgs, " has invalid format");
		}
	}

	public void validateItemEditability( Item item, UserInfo userInfo, Errors errors){

		if( item == null ){
			log.error("Item is null");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidItem", errorArgs, "Item is invalid. Cannot find the item.");
			return;
		}
		if( itemModifyRoleValidator.canTransition(item, userInfo) == false ){
			log.error("User " + userInfo.getId() + " does not have permissions to edit item " + item.getItemName());
			Object[] errorArgs = {userInfo.getUserName(), item.getItemName()};
			errors.reject("ItemValidator.noPermission", errorArgs, "No permission to perform this operation");
		}
	}

	public void validateIPIsNotTeamed( IPAddressDetails ipDetails, DataPort dataPort, Errors errors){
		if( isATeam(dataPort, ipDetails) == true ){
			log.error("Dataport " + dataPort.getPortId() + " and ip " + ipDetails.getId() + 
					" are already teamed" );
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.alreadyTeamed", errorArgs, "Selected data port and ipaddress are already teamed");
		}
	}

	public void validateIPIsTeamed( IPAddressDetails ipDetails, DataPort dataPort, Errors errors){
		if( isATeam(dataPort, ipDetails) == false ){
			log.error("Dataport " + dataPort.getPortId() + " and ip " + ipDetails.getId() + 
					" are not teamed" );
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.notTeamed", errorArgs, "Selected data port and ipaddress are not teamed, cannot delete");
		}
	}


	/*
	 * Check if ip address is duplicate, but only if isIpDuplicateAllowed is not set or it is false
	 */
	public void validateIsDuplicateInSite( IPAddressDetails ipDetails, Long dataPort, Long locationId, Errors errors){
		String ipAddress = ipDetails.getIpAddress();
		List<IPAddressDetails> allIps = null;
		boolean requreTeaming = (ipDetails.getIsTeamingAllowed() != null && ipDetails.getIsTeamingAllowed() == true ) ? true : false;
		boolean requireDups = (ipDetails.getIsDuplicatIpAllowed() != null && ipDetails.getIsDuplicatIpAllowed() == true ) ? true : false;

		allIps = ipAddressDetailsDAO.getIpAddressByName(ipAddress, locationId);
		if( allIps != null && allIps.size() > 0 ){ //all ip records with that ipaddress name
			List<DataPort> portsToCheck = new ArrayList<DataPort>();
			if( allIps.size() > 1 ){//duplicate ips already exist
				for( IPAddressDetails ip : allIps ){
					portsToCheck.addAll(ip.getDataPortsUsingIP());
				}
				if( requreTeaming || requireDups == false ){ //do not allow teaming since dups are already there
					log.warn("IpAddress " + ipAddress + " is duplicate.");
					asembleDuplicateIPsErrorMsg( portsToCheck, errors);
				}
			}else{ //only one ip exists, may be teamed or single
				portsToCheck = new ArrayList<DataPort>(allIps.get(0).getDataPortsUsingIP());
				assert(portsToCheck.size() > 0);
				if( portsToCheck.size() > 1 ){ //already teamed
					if( requireDups == true || requreTeaming == false ){
						log.warn("IpAddress " + ipAddress + " has been teamed");		
						assembleErrorMsgForTeaming( portsToCheck, errors);
					}
				}
			}
			assert(portsToCheck.size() > 0);
			//verify that the same combination of ipaddress and dataport has not been already used
			for( DataPort d : portsToCheck ){
				if( d.getPortId().longValue() == dataPort.longValue()) {
					log.warn("The same IpAddress " + ipAddress + " and port have been already used.");
					asembleSameTeamErrorMsg( portsToCheck, errors);
				}
			}
		}
	}
	private void asembleSameTeamErrorMsg(List<DataPort> dupIPsPorts, Errors errors) {
		int loopCnt = dupIPsPorts.size();
		assert( loopCnt >= 1 );
		StringBuilder dupPorts = new StringBuilder();

		for( int i=0; i<loopCnt; i++){
			dupPorts.append("Port: ");
			dupPorts.append(dupIPsPorts.get(i).getPortName());
			dupPorts.append( "of Item: ");
			dupPorts.append(dupIPsPorts.get(i).getItem().getItemName());
			log.warn("duplicate: " + dupPorts.toString());
			if( i < loopCnt -1 ) dupPorts.append(", ");
		}
		Object[] errorArgs = {dupPorts.toString()};
		errors.reject("IpAddressValidator.sameIpAssignment", errorArgs, "Same ipaddress and data port already assigned");
	}
	
	private void asembleDuplicateIPsErrorMsg(List<DataPort> dupIPsPorts, Errors errors) {
		int loopCnt = dupIPsPorts.size();
		assert( loopCnt >= 1 );
		StringBuilder dupPorts = new StringBuilder();

		for( int i=0; i<loopCnt; i++){
			dupPorts.append("Port: ");
			dupPorts.append(dupIPsPorts.get(i).getPortName());
			dupPorts.append( "of Item: ");
			dupPorts.append(dupIPsPorts.get(i).getItem().getItemName());
			log.warn("duplicate: " + dupPorts.toString());
			if( i < loopCnt -1 ) dupPorts.append(", ");
		}
		Object[] errorArgs = {dupPorts.toString()};
		errors.reject("IpAddressValidator.duplicate", errorArgs, "IPAddress is duplicate");
	}

	private void assembleErrorMsgForTeaming(List<DataPort> teamedIPsPorts, Errors errors) {
		int loopCnt = teamedIPsPorts.size();
		assert( loopCnt >= 2 );
		StringBuilder teamedPortsAndItems = new StringBuilder();
		for( int i=0; i<loopCnt; i++){
			teamedPortsAndItems.append("Port: ");
			teamedPortsAndItems.append(teamedIPsPorts.get(i).getPortName());
			teamedPortsAndItems.append( "of Item: ");
			teamedPortsAndItems.append(teamedIPsPorts.get(i).getItem().getItemName());
			log.warn("team members: " + teamedPortsAndItems.toString() );
			if( i < loopCnt-1 )teamedPortsAndItems.append(", ");
		}

		Object[] errorArgs = {teamedPortsAndItems.toString()};
		errors.reject("IpAddressValidator.teamed", errorArgs, "IP address has been teamed.");
	}


	public boolean isIPAddressInManagedSubnet( List<Networks> subnets, String ipAddress){
		boolean retval = false;

		//If the first record says it is managed then it is
		if( subnets != null && subnets.size() > 0 && subnets.iterator().next().getIsManaged() == true ){
			retval = true;
		}
		return retval;
	}

	private boolean isIPAddressGW( List<Networks> subnets, String ipAddress){
		boolean retval = false;

		if( subnets != null && subnets.size() > 0 ){
			for( Networks s : subnets ){
				if( s.getGateway() != null && s.getGateway().equals(ipAddress)){
					retval = true;
					break;
				}
			}
		}
		return retval;
	}


	/*
	 * Check if ip address is a gateway, but only if isIpBeingGatewayAllowed is not set or is false
	 */
	public void validateIfGW( IPAddressDetails ipDetails, Long locationId, Errors errors ){
		assert( ipDetails != null);

		if(ipDetails.getIsIpBeingGatewayAllowed() != null && ipDetails.getIsIpBeingGatewayAllowed() == true ){
			return;
		}
		try {
			if( ipDetails.getIpAddress().equals(ipDetails.getGateway())){
				log.error("provided ipAddress " + ipDetails.getIpAddress() + " and provided gateway are the same");
				Object[] errorArgs = {};
				errors.reject("IpAddressValidator.ipAddressIsGW", errorArgs, 
						"Specified ip address is a gateway");
			}
			String ipAddress = ipDetails.getIpAddress();
			//first check managed ips 
			List<Networks> subnets = networksDAO.getNetworkForIpAndLocation(ipAddress, locationId);
			//Check if it belongs to the list of managed ips
			if( subnets == null || subnets.size() < 1 ){ //not in managed list, search non managed list
				subnets = ipAddressDetailsDAO.getSubnetForIpAndLocation(ipAddress, locationId);
			}

			if( isIPAddressGW( subnets, ipAddress ) == true ){
				log.error("ipAddress " + ipAddress + " is already a gateway");
				Object[] errorArgs = {};
				errors.reject("IpAddressValidator.ipAddressIsGW", errorArgs, 
						"Specified ip address is a gateway");
			}
		} catch (BusinessValidationException e) {
			log.error("failed to obtain subnet info for ip: " + ipDetails.getIpAddress());
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.subnet", errorArgs, "Failed to obtain subnet to validate gateway");
			return; //because this is kind of system error
		}
	}

	public boolean isATeam(DataPort dataPort, IPAddressDetails ipDetails) {
		boolean retval = false;
		Set<DataPort> ipDPs = ipDetails.getDataPortsUsingIP();
		for( DataPort current : ipDPs ){
			if( current.getPortId() == dataPort.getPortId()){
				retval = true;
			}
		}
		return retval;
	}

	public boolean hasTeams(IPAddressDetails ipDetails) {
		boolean retval = false;
		Set<DataPort> ipDPs = ipDetails.getDataPortsUsingIP();
		if( ipDPs.size() > 1) retval = true;
		return retval;
	}


	public void validateEditIsAllowed(IPTeaming team, Long locationId, Errors errors) {

		assert( team != null );
		IPAddressDetails ipDetails = team.getIpAddress();

		Long newIpId = ipDetails.getId();
		String newIpAddressName = ipDetails.getIpAddress();		

		IPAddressDetails existingIpAddress = validateAndGetIpAddress(newIpId, errors);
		if( newIpAddressName.equals(existingIpAddress)) {
			// no changes in ipaddress, ok
			return;
		}
		IPUtils.IPStatus dstIpStatus = ipUtils.getIPStatus(ipDetails.getIpAddress(), locationId);

		boolean teamIt = ipDetails.getIsTeamingAllowed();
		boolean dupIt = ipDetails.getIsDuplicatIpAllowed();
		assert(( teamIt == true && dupIt == false) || (teamIt == false && dupIt == true ));

		if( dstIpStatus == IPUtils.IPStatus.DUP_IP && teamIt ){
			//send error
			log.error( "Cannot team " + newIpAddressName + " with " + existingIpAddress + 
					" b-cause dest ip has dups"); 
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.duplicate", errorArgs, 
					"Dest has duplicates, cannot team");
		}else if( dstIpStatus == IPUtils.IPStatus.TEAMED_IP && dupIt ){
			//send error
			log.error("Cannot dup " + newIpAddressName + " to " + existingIpAddress + 
					" b-cause dest ip has been teamed");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.teamed", errorArgs, 
					"Dest ip has been teamed, cannot duplicate");
		}
	}


	public Long validateAndGetNetworkId(String ipAddress, Long locationId,
			Errors errors) {

		Long subnetId = null;
		if( locationId != null && ipAddress != null ){
			try {
				List<Networks> networks = networksDAO.getNetworkForIpAndLocation(ipAddress, locationId);
				if( networks.size() > 0 ){
					subnetId = networks.get(0).getId();
				}

			} catch (BusinessValidationException e) {
				log.error("Failed to obtain subnet for ip: " + ipAddress + " and location " + locationId);
				Object[] errorArgs = {};
				errors.reject("IpAddressValidator.subnet", errorArgs, 
						"Cannot obtain subnet");
			}
		}
		return subnetId;
	}

	public void validateStringsSize(JSONIpAssignment jsonIpAss,
			Errors errors) {
		final int maxLength1=64;
		final int maxLength2=500;

		StringBuilder tooBigFields = new StringBuilder();
		if( jsonIpAss.getComment() != null && jsonIpAss.getComment().length() > maxLength2 ){
			tooBigFields.append("comment,");
		}
		if( jsonIpAss.getDnsName() != null && jsonIpAss.getIpAddress().length() > maxLength1 ){
			tooBigFields.append("ipaddress,");
		}
		if( jsonIpAss.getDomainName() != null && jsonIpAss.getDomainName().length() > maxLength1 ){
			tooBigFields.append("dnsName,");
		}
		if( jsonIpAss.getGateway() != null && jsonIpAss.getGateway().length() > maxLength1 ){
			tooBigFields.append("gateway,");	
		}
		if( tooBigFields.length() > 0 ){
			tooBigFields.delete(tooBigFields.length()-1, tooBigFields.length());
			log.error("Fields : " + tooBigFields.toString() + " are too big");
			Object[] errorArgs = {tooBigFields.toString()};
			errors.reject("IpAddressValidator.tooBigField", errorArgs, 
					"Too big fields");
		}
	}
	
	

}
