package com.raritan.tdz.ip.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.dao.IPTeamingDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.json.JSONIpAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.ip.validators.IPAddressValidatorCommon;
import com.raritan.tdz.port.dao.DataPortDAO;



public class JSONIPAdapterImpl implements JSONIPAdapter {

	@Autowired(required=true)	
	IPAddressValidatorCommon ipAddressValidatorCommon;

	@Autowired(required=true)
	DataPortDAO dataPortDAO;

	@Autowired(required=true)
	IPTeamingDAO ipTeaming;
	
	@Autowired
	IPAddressDetailsDAO ipAddressDetailsDAO;

	private final Logger log = Logger.getLogger(this.getClass());

	/** methods that adapt: Netmask <-> JSON object **/
	@Override
	public Map<String, Object> adaptNetmaskListToJSONArray(
			List<NetMask> netMasksList) {


		if( netMasksList == null ) netMasksList = new ArrayList<NetMask>();

		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("netmasks", netMasksList);

		return ret;	
	}


	/** methods that adapt ipaddresses <-> JSON ipaddresses **/
	@Override
	public Map<String, Object> adaptIpAddressListToJSONArray(
			List<IPAddressDetails> ipAddressList) {

		Map<String, Object> ret = new HashMap<String, Object>();
		List<JSONIpAddressDetails> retJson = new ArrayList<JSONIpAddressDetails>();

		if( ipAddressList != null && ipAddressList.size() > 0 ){
			for( IPAddressDetails ip : ipAddressList ){
				JSONIpAddressDetails ipJson = convertDAOToJSONIpAddress( ip );
				retJson.add(ipJson);
			}
		}
		ret.put("ipaddresses", retJson );
		return ret;
	}


	@Override
	public Map<String, Object> adaptIpAddressToJSONArray(
			IPAddressDetails ipAddress) {

		List<JSONIpAddressDetails> ipAddressList = new ArrayList<JSONIpAddressDetails>();
		if( ipAddress != null ){
			JSONIpAddressDetails jsonIp = convertDAOToJSONIpAddress(ipAddress); 
			ipAddressList.add(jsonIp);
		}
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("ipaddresses", ipAddressList );
		return ret;
	}


	/** methods that adapt ipaddresse <-> JSON ipassignement **/
	@Override
	public Map<String, Object> adaptIpAddressListToJSONAssignmtsArray(
			List<IPAddressDetails> ipAddressList) {

		Map<String, Object> ret = new HashMap<String, Object>();
		List<JSONIpAssignment> retJson = new ArrayList<JSONIpAssignment>();

		if( ipAddressList != null && ipAddressList.size() > 0 ){
			for( IPAddressDetails ip : ipAddressList ){
				List<JSONIpAssignment> ipJson = convertDAOToJSONAssignements( ip );
				retJson.addAll(ipJson);
			}
		}
		ret.put("ipassignments", retJson );
		return ret;
	}


	private JSONIpAddressDetails convertDAOToJSONIpAddress(IPAddressDetails ipAddress) {
		if( ipAddress == null ){
			log.error("ipAddress is null");
			return null;
		}
		JSONIpAddressDetails jsonIp = new JSONIpAddressDetails();	
		jsonIp.setComment(ipAddress.getComment());
		jsonIp.setId(ipAddress.getId());
		jsonIp.setIpAddress(ipAddress.getIpAddress());
		jsonIp.setGateway(ipAddress.getGateway());
		jsonIp.setDnsName(ipAddress.getDnsName());
		jsonIp.setIsVirtual(ipAddress.getIsVirtual());
		LkuData domain = ipAddress.getDomain();
		if( domain != null ){
			jsonIp.setDomainName(domain.getLkuValue());
			jsonIp.setDomainId(domain.getLkuId());
		}

		NetMask mask = ipAddress.getMask();
		if( mask != null ) {
			jsonIp.setMask(mask.getMask());
			jsonIp.setCidr(mask.getCidr());
		}
		Set<DataPort> dataPorts = ipAddress.getDataPortsUsingIP();
		//System is corrupted if there is an ipaddress record dangling, so assert asap
		assert(dataPorts != null);

		if( dataPorts != null ){
			jsonIp.setDataPortsFromDomain(dataPorts);
		}
		return jsonIp;
	}


	private List<JSONIpAssignment> convertDAOToJSONAssignements(IPAddressDetails ipAddress) {
		if( ipAddress == null ){
			log.error("ipAddress is null!");
			return null;
		}
		Set<IPTeaming> teams = ipAddress.getIpTeaming();
		if( teams == null ){
			log.error("teams == null!");
			return null;
		}
		List<JSONIpAssignment> jsonIpList  = new ArrayList<JSONIpAssignment>();
		for( IPTeaming t : teams ){
			JSONIpAssignment jsonIp = new JSONIpAssignment();
			jsonIp.setPortId(t.getDataPort().getPortId());
			jsonIp.setPortName(t.getDataPort().getPortName());
			Item i = t.getDataPort().getItem();
			assert(i != null);
			jsonIp.setItemId(i.getItemId());
			jsonIp.setItemName(i.getItemName());
			jsonIp.setComment(ipAddress.getComment());
			jsonIp.setId(t.getId());
			jsonIp.setIpaddressId(ipAddress.getId());
			jsonIp.setSubnetId(ipAddress.getNetworkId());
			jsonIp.setIpAddress(ipAddress.getIpAddress());
			jsonIp.setGateway(ipAddress.getGateway());
			jsonIp.setDnsName(ipAddress.getDnsName());
			jsonIp.setIsVirtual(ipAddress.getIsVirtual());
			LkuData domain = ipAddress.getDomain();
			if( domain != null ){
				jsonIp.setDomainName(domain.getLkuValue());
				jsonIp.setDomainId(domain.getLkuId());
			}

			NetMask mask = ipAddress.getMask();
			if( mask != null ) {
				jsonIp.setMask(mask.getMask());
				jsonIp.setCidr(mask.getCidr());
			}
			jsonIpList.add(jsonIp);
		}

		return jsonIpList;
	}


	@Override
	public Map<String, Object> adaptAvailabeIpsToJSONArray(List<String> availableIps) {
		Map<String, Object> ret = new HashMap<String, Object>();
		if( availableIps == null ) availableIps = new ArrayList<String>();
		ret.put("availableIps", availableIps );
		return ret;
	}

	/** Methods for adapting Networks <-> JSON object **/

	@Override
	public Map<String, Object> adaptNetworksToJSONArray(List<Networks> networksList) {
		Map<String, Object> ret = new HashMap<String, Object>();
		if( networksList == null ) networksList = new ArrayList<Networks>();
		ret.put("subnets", networksList );
		return ret;

	}





	/** Methods for adapting:  IPTeaming <-> JSONIPAssignement **/

	@Override
	public IPTeaming adaptJSONIpAssgToNewIPTeaming(
			JSONIpAssignment jsonIpAss, String createAs,
			Boolean isGateway, Errors errors) {

		assert(jsonIpAss.getPortId() != null );
		assert(jsonIpAss.getIpAddress() != null );

		ipAddressValidatorCommon.validateStringsSize(jsonIpAss, errors);
		if( errors.hasErrors()) return null;

		DataPort dp = ipAddressValidatorCommon.validateAndGetDataPort(jsonIpAss.getPortId(), errors);
		if( errors.hasErrors()) return null;

		IPTeaming newTeam = new IPTeaming();
		newTeam.setId(jsonIpAss.getId());
		newTeam.setDataPort(dp);

		IPAddressDetails domainIp = new IPAddressDetails();
		newTeam.setIpAddress(domainIp);
		Set<IPTeaming> teams = new HashSet<IPTeaming>();
		teams.add(newTeam);
		domainIp.setIpTeaming(teams);

		newTeam = adaptJSONIpAssgToIPTeamingCommon(jsonIpAss, createAs, isGateway, newTeam, errors );
		return newTeam;
	}

	@Override
	public IPTeaming adaptJSONIpAssgToExistingIPTeaming(
			JSONIpAssignment jsonIpAss, String createAs,
			Boolean isGateway, Errors errors) {


		Long teamId = jsonIpAss.getId();
		assert( teamId != null ); //already validated as required field

		ipAddressValidatorCommon.validateStringsSize(jsonIpAss, errors);
		if( errors.hasErrors()) return null;

		IPTeaming savedTeam = ipAddressValidatorCommon.validateAndGetTeaming(teamId, errors);
		if( errors.hasErrors()) return null;
		assert( savedTeam != null );

		IPTeaming newTeam = new IPTeaming();
		newTeam.setId(teamId);

		IPAddressDetails domainIp = new IPAddressDetails();
		newTeam.setIpAddress(domainIp);	

		domainIp.setId(savedTeam.getIpAddress().getId());
		newTeam.setDataPort(savedTeam.getDataPort());
		assert( savedTeam.getIpAddress() != null);

		domainIp.setIpTeaming(savedTeam.getIpAddress().getIpTeaming());

		newTeam = adaptJSONIpAssgToIPTeamingCommon(jsonIpAss, createAs, isGateway, newTeam, errors );
		return newTeam;
	}


	private IPTeaming adaptJSONIpAssgToIPTeamingCommon(JSONIpAssignment jsonIpAss, String createAs,
			Boolean isGateway, IPTeaming newTeam, Errors errors ){
		assert( newTeam != null);
		assert(newTeam.getIpAddress() != null);
		assert(newTeam.getDataPort() != null );

		IPAddressDetails domainIp = newTeam.getIpAddress();

		if( jsonIpAss.getComment() != null ){
			domainIp.setComment(jsonIpAss.getComment());
		}
		String ipAddress = jsonIpAss.getIpAddress();
		if( ipAddress != null ){
			domainIp.setIpAddress(ipAddress);
		}
		if( jsonIpAss.getDnsName() != null ){
			domainIp.setDnsName(jsonIpAss.getDnsName());
		}
		if( jsonIpAss.getGateway() != null){
			domainIp.setGateway(jsonIpAss.getGateway());
		}
		if( jsonIpAss.getIsVirtual() != null ){
			domainIp.setIsVirtual(jsonIpAss.getIsVirtual());
		}else domainIp.setIsVirtual( false );

		domainIp.setIsTeamingAllowed(false);
		domainIp.setIsDuplicatIpAllowed(false);

		if( createAs != null ){
			if( createAs.equals("duplicate")){
				domainIp.setIsDuplicatIpAllowed(true);
			}else if( createAs.equals("team")){
				domainIp.setIsTeamingAllowed(true);
			}
		}

		if( isGateway != null ){
			domainIp.setIsIpBeingGatewayAllowed(isGateway);
		}else domainIp.setIsIpBeingGatewayAllowed(false);

		Long domainId = jsonIpAss.getDomainId();
		if( domainId != null ){
			LkuData domain = ipAddressValidatorCommon.validateAndGetDomainById(domainId, errors);
			domainIp.setDomain(domain);
		}
		if( jsonIpAss.getCidr() != null ){
			Long cidr = jsonIpAss.getCidr();
			if( cidr != null ){
				NetMask netMask = ipAddressValidatorCommon.validateAndGetNetMaskByCidr(cidr, errors);
				domainIp.setMask( netMask );
			}
		}

		Long locationId = ipAddressValidatorCommon.validateAndGetLocationId(newTeam.getDataPort().getPortId(), errors);

		Long networkId = ipAddressValidatorCommon.validateAndGetNetworkId( ipAddress, locationId, errors);
		if( ! errors.hasErrors()){
			domainIp.setNetworkId(networkId);
		}

		return newTeam;
	}

	@Override
	public Map<String, Object> adaptIPTeamingArrayToJSONAssignmtArray(
			List<IPTeaming> retTeams){

		Map<String, Object> ret = new HashMap<String, Object>();
		List<JSONIpAssignment> retJson = new ArrayList<JSONIpAssignment>();

		if( retTeams != null && retTeams.size() > 0 ){
			for( IPTeaming t : retTeams ){
				JSONIpAssignment ipJson = convertDAOToAssignmntJSON( t );
				retJson.add(ipJson);
			}
		}
		ret.put("ipassignments", retJson );
		return ret;
	}

	@Override
	public Map<String, Object> adaptIPTeamingToJSONAssignmtArray(IPTeaming team) {
		Map<String, Object> ret = new HashMap<String, Object>();
		JSONIpAssignment ipJson = null;
		ipJson = convertDAOToAssignmntJSON( team );
		ret.put("ipassignment", ipJson );
		return ret;		
	}


	private JSONIpAssignment convertDAOToAssignmntJSON(IPTeaming team) {
		if( team == null || team.getIpAddress() == null || team.getDataPort() == null ){
			return null;
		}
		IPAddressDetails ipAddress = team.getIpAddress();
		DataPort dataPort = team.getDataPort();

		JSONIpAssignment jsonIp = new JSONIpAssignment();
		jsonIp.setPortId(dataPort.getPortId());
		jsonIp.setPortName(dataPort.getPortName());
		Item i = dataPort.getItem();
		assert(i != null);
		jsonIp.setItemId(i.getItemId());
		jsonIp.setItemName(i.getItemName());
		jsonIp.setComment(ipAddress.getComment());
		jsonIp.setId(team.getId());
		jsonIp.setIpaddressId(ipAddress.getId());
		jsonIp.setIpAddress(ipAddress.getIpAddress());
		jsonIp.setGateway(ipAddress.getGateway());
		jsonIp.setDnsName(ipAddress.getDnsName());
		jsonIp.setIsVirtual(ipAddress.getIsVirtual());
		LkuData domain = ipAddress.getDomain();
		if( domain != null ){
			jsonIp.setDomainName(domain.getLkuValue());
			jsonIp.setDomainId(domain.getLkuId());
		}

		NetMask mask = ipAddress.getMask();
		if( mask != null ) {
			jsonIp.setMask(mask.getMask());
			jsonIp.setCidr(mask.getCidr());
		}

		return jsonIp;
	}




	@Override
	public JSONIpAssignment adaptDataPortDetailsToJsonIP(DataPort dataPort,
			String ipAddress, Errors errors) {
		
		if( dataPort == null ){
			log.error("Specified Port does not exist or item does not have data ports");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.portName", errorArgs, 
					"Specified port does not exist, or item does not have data ports");
			return null;
		}
		Long dataPortId = dataPort.getPortId();
		assert( dataPortId != null);
		
		JSONIpAssignment ipAssignment = new JSONIpAssignment();
		Item item = dataPort.getItem();
		assert(item != null);
		ipAssignment.setItemId(item.getItemId());
		ipAssignment.setItemName(item.getItemName());

		ipAssignment.setPortName(dataPort.getPortName());
		ipAssignment.setPortId(dataPortId);
		
		List<IPAddressDetails> ipDetails = ipAddressDetailsDAO.getIpAddressForDataPort(dataPortId);
		if( ipDetails.size() > 1 ){
			// Error port has more than one ip address assigned cannot update ip
			log.error("Port " + dataPortId + " has more than one ip assigned, canot edit");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidIpForPort", errorArgs, 
					"This operation supports a single assigned IP Address per data port.");
			return null;
		}else if( ipDetails.size() == 1 ){
			IPAddressDetails detail = ipDetails.get(0);
			ipAssignment.setIpaddressId(detail.getId());
			if( ipAddress == null )	ipAddress = detail.getIpAddress();
			
			//If this is edit, then team already exist; read it
			List<IPTeaming> teams = (List<IPTeaming>) ipTeaming.getTeamsForDataPort(dataPortId);
			for( IPTeaming t: teams){
				if( t.getIpAddress().getId() == detail.getId()){
					ipAssignment.setId(t.getId());
					break;
				}
			}
		}
		ipAssignment.setIpAddress(ipAddress);
		
		return ipAssignment;
	}
}
