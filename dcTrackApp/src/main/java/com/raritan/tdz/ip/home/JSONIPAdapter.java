package com.raritan.tdz.ip.home;

import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.ip.json.JSONIpAddressDetails;
import com.raritan.tdz.ip.json.JSONIpAssignment;

public interface JSONIPAdapter {
	/* Network (subnets) */
	Map<String, Object> adaptNetmaskListToJSONArray(List<NetMask> netMaskList);
	Map<String, Object> adaptNetworksToJSONArray(List<Networks> networksList);

	/* ipaddress */
	Map<String, Object> adaptIpAddressListToJSONArray(List<IPAddressDetails> ipAddressList);
	Map<String, Object> adaptIpAddressToJSONArray(IPAddressDetails ipAddress);
	Map<String, Object> adaptAvailabeIpsToJSONArray(List<String> availableIps);

	/* ip assignements */
	Map<String, Object> adaptIpAddressListToJSONAssignmtsArray(
			List<IPAddressDetails> ipAddressList);
	
	/* teaming */
	Map<String, Object> adaptIPTeamingToJSONAssignmtArray(IPTeaming team);
	Map<String, Object> adaptIPTeamingArrayToJSONAssignmtArray(
			List<IPTeaming> retTeams);
	IPTeaming adaptJSONIpAssgToNewIPTeaming(
			JSONIpAssignment ipAssignmentInfo, String createAs,
			Boolean isGateway, Errors errors);
	IPTeaming adaptJSONIpAssgToExistingIPTeaming(JSONIpAssignment jsonIpAss,
			String createAs, Boolean isGateway, Errors errors);
	JSONIpAssignment adaptDataPortDetailsToJsonIP(DataPort dataPort,
			String ipAddress, Errors errors);

}
