package com.raritan.tdz.ip.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.dao.IPTeamingDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.home.IPHome;
import com.raritan.tdz.ip.json.JSONIpAssignment;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.DataPortDAO;


public class ItemIpAddrAandProxyValidator implements Validator {
	private final Logger log = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	IPAddressValidatorCommon ipAddressValidatorCommon;
	
	@Autowired
	ItemDAO itemDAO;

	@Autowired(required=true)
	IPTeamingDAO ipTeamingDAO;

	@Autowired
	DataPortDAO dataPortDAO;
	
	@Autowired
	IPHome ipHome;
	
	@Autowired
	IPAddressDetailsDAO ipAddressDetailsDAO;
	
	final int MAX_PROXY_INDEX_LEN = 100;
	
	private boolean isNullOrEmpty ( String str){
		boolean retval = false;
		if( str == null || (str != null && str.isEmpty()) ) retval = true;
		
		return retval;
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		Map<String, Object> paramsMap = (Map<String, Object>)target;
			
		JSONIpAssignment jsonIp = (JSONIpAssignment) paramsMap.get("JSONIpAssignment");
		if(jsonIp ==  null) {
			throw new IllegalArgumentException("IpAddressValidator: jsonIp is missing");
		}
		String proxyIndex = (String)paramsMap.get("proxyIndex");
		
		validateProxyIndexAndIpAddress( jsonIp, proxyIndex, errors );
	}

	private void validateProxyIndexAndIpAddress( JSONIpAssignment ipAssignmentNew, 
			String newProxyIndex, Errors errors){
		
		if( errors.hasErrors()) return;
		
		String newIpAddress = ipAssignmentNew.getIpAddress();
		if( newIpAddress == null && newProxyIndex == null ) return; //nothing to do 
		
		Item item = itemDAO.getItem(ipAssignmentNew.getItemId());
		assert( item != null);
		
		Long dataPortId = ipAssignmentNew.getPortId();
		assert( dataPortId != null && dataPortId.longValue() > 0);

		Long locationId = item.getDataCenterLocation().getDataCenterLocationId();
		assert( locationId != null );

		if( newProxyIndex != null){
			validateProxyIndexLength( newProxyIndex, errors );
		}
				
		if( !isNullOrEmpty(newIpAddress)){
			validateIpIsNotAlreadyDup( newIpAddress, locationId, errors);
			ipAddressValidatorCommon.validateIpAddressFormat(newIpAddress, errors);			
		}
		
		validateNoProxyWithoutIP(newIpAddress, newProxyIndex, errors);
		if( errors.hasErrors()) return;
		
		validateNoTeamingWithoutProxy(ipAssignmentNew, newProxyIndex, errors);
		
		if( !isNullOrEmpty(newIpAddress)) {
			
			validateSingleIpPerPort( ipAssignmentNew, errors);
		
			validateSingleIpPerItem( ipAssignmentNew, errors);
		}

		validateUniqueProxyIdForIp(newProxyIndex, ipAssignmentNew, errors );
	}

	//Validate the ip is not already duplicate in the site
	private void validateIpIsNotAlreadyDup(String ipAddress, Long locationId, Errors  errors){
		if( isNullOrEmpty(ipAddress)) return;
	
		//Requirement: Verify if ipaddress is not duplicate (note, it can be teamed)
		List<IPAddressDetails> dupsIps = ipAddressDetailsDAO.getIpAddressByName(ipAddress, locationId);
		if( dupsIps.size() > 1){ //more than 1 record in DB => ip addr is duplicate, cannot use it
			//find ports using it, so we can construct error msg
			log.error("ip address: " + ipAddress + "has been already used" );
			Object[] errorArgs = {getPortNamesUsingIpAddress(ipAddress, locationId).toString()};
			errors.reject("IpAddressValidator.importFileDuplicateIP", errorArgs, "Provided IP address is already used by {0}.");
		}
	}
	
	private void validateProxyIndexLength( String proxyIndex, Errors errors){
		if( proxyIndex != null && proxyIndex.length() > MAX_PROXY_INDEX_LEN){
			log.error("Proxy Index too large field");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidProxyIndex", errorArgs, 
					"ProxyIndex is too long");
		}
	}
	
	
	//ProxyIndex cannot be set without ipAddress. The ipAddress will be null or empty only if it
	//was already null/empty in DB and new value is also null/empty
	private void validateNoProxyWithoutIP(String ipAddress, String proxyIndex, Errors errors){
		if( isNullOrEmpty(ipAddress) && ! isNullOrEmpty(proxyIndex)){
			log.error("Proxy index cannot be set when ipAddress is null/empty string");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidIpProxyCombo", errorArgs, 
					"Cannot assign ProxyIndex without ipAddress");
		}
	}
	
	private void validateNoTeamingWithoutProxy(JSONIpAssignment ipAssignment, String proxyIndex, Errors errors){
		if( !isNullOrEmpty(proxyIndex)) return;
		//proxy Index is null or empty, ipAddress can be set/changed only if there is only 1 team 
		//and the team is for this dataport
		String ipAddress = ipAssignment.getIpAddress();
		
		Long dataPortId = ipAssignment.getPortId();
		assert( dataPortId != null && dataPortId.longValue() > 0);
		
		Item item = itemDAO.getItem(ipAssignment.getItemId());
		assert( item != null);
		
		Long locationId = item.getDataCenterLocation().getDataCenterLocationId();
		assert( locationId != null );

		boolean hasError = false;
		List<IPTeaming> teams = ipTeamingDAO.getTeamsForIpAddress(ipAddress, locationId);
		
		if( teams.size() > 1) hasError = true;
		//check if that team is exactly this one (editing); then ok
		else if( teams.size() == 1 ){
			IPTeaming team = teams.get(0);
			if( team.getDataPort().getPortId().longValue() != dataPortId.longValue()){
				hasError = true;
			}
		}
		if( hasError){
			log.error("IpAddress cannot be teamed without proxyIndex");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.teamingIpWithoutProxyIndex", errorArgs, 
					"Cannot team ipAddress without proxyIndex.");
		}
	}
	
	//validate selected port does not have other ipassignmenst, i.e. there are no other ips
	//assigned to this port
	private void validateSingleIpPerPort( JSONIpAssignment ipAssignment, Errors errors){
		
		Long dataPortId = ipAssignment.getPortId();
		assert( dataPortId != null &&  dataPortId.longValue() > 0);
		
		List<IPTeaming> teams = ipTeamingDAO.getTeamsForDataPort(dataPortId);
		boolean hasError = false;
		if( teams.size() > 1 ) hasError = true;
		else if( teams.size() == 1 ){
			//check it is this one
			IPTeaming t = teams.get(0);
			if( t.getId().longValue() != ipAssignment.getId().longValue()) hasError = true;
		}
		if( hasError ){
			log.error("Port " + dataPortId + " has more than one ip assigned");
			Object[] errorArgs = {};
			errors.reject("IpAddressValidator.invalidIpForPor", errorArgs, 
				"This operation supports a single assigned IP Address per data port.");
		}		
	}	
	

	//validate there is only one ipassignement per item
	private void validateSingleIpPerItem( JSONIpAssignment ipAssignment, Errors errors){
		
		Long itemId = ipAssignment.getItemId();
		assert( itemId != null &&  itemId.longValue() > 0);
		Item item = itemDAO.getItem(itemId);
		
		Long dataPortId = ipAssignment.getPortId();
		assert( dataPortId != null &&  dataPortId.longValue() > 0);
		
		List<IPTeaming> teams = ipTeamingDAO.getTeamsForItem(itemId);
		boolean hasError = false;
		if( teams.size() > 1 ) hasError = true;
		else if( teams.size() == 1 ){
			//check it is this one
			IPTeaming t = teams.get(0);
			if( ipAssignment.getId() == null || ( t.getId().longValue() != ipAssignment.getId().longValue())) hasError = true;
		}
		if ( ipAssignment.getIpAddress() == null || ipAssignment.getIpAddress().length() == 0 ) hasError = false;
		if( hasError ){
			log.error("Item " + itemId + " has more than one ip assigned");
			Object[] errorArgs = {item.getItemName()};
			errors.reject("IpAddressValidator.invalidIpForItem", errorArgs, 
				"This operation supports a single assigned IP Address per item.");
		}	
				
	}	
	
	
	//Validate that proxy has unique value for the specified ip
	private void validateUniqueProxyIdForIp(String proxyIndex, JSONIpAssignment ipAssignmentNew, Errors errors ){
		
		//Requirement: Proxy index must be unique for the ip address in the location
		if( proxyIndex == null || proxyIndex.length()== 0) return;
		
		Item item = itemDAO.getItem(ipAssignmentNew.getItemId());
		assert( item != null);
		assert(item.getDataCenterLocation() != null);
		Long locationId = item.getDataCenterLocation().getDataCenterLocationId();
		
		String ipAddress = ipAssignmentNew.getIpAddress();
		
		List<String> allProxies = ipAddressDetailsDAO.getProxyIndexesForIpAddr(ipAddress, locationId);
		if( allProxies == null || allProxies.size() <= 0 ) return;
		
		if(allProxies.contains(proxyIndex)){
			log.error("Proxy index " + proxyIndex + " already used by ipAddress " + ipAddress);
			Object[] errorArgs = {ipAddress};
			errors.reject("iIpAddressValidator.ipProxyComboAlreadyUsed", errorArgs, 
					"ProxyIndex for ipAddress {0} has been already used");
		}
	}
	
	private List<String> getPortNamesUsingIpAddress( String ipAddress, Long locationId ){
		List<String> portNames = new ArrayList<String>();
		
		List<IPTeaming> teams = ipTeamingDAO.getTeamsForIpAddress(ipAddress, locationId);
		for( IPTeaming t : teams ){
			portNames.add(t.getDataPort().getPortName());
		}
		return portNames;
	}
	

	
	@Override
	public boolean supports(Class<?> clazz) {
		return JSONIpAssignment.class.equals(clazz);
	}	
}
