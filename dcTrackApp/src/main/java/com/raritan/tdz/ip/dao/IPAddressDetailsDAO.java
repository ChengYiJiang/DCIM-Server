package com.raritan.tdz.ip.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.Networks;

public interface IPAddressDetailsDAO extends Dao<IPAddressDetails>{
	List<IPAddressDetails> getIPAddressUsingGateway(String gateway);
	List<IPAddressDetails> getIpAddressForDataPort(Long dataPortId);
	List<IPAddressDetails> getIpAddressForItem( Long itemId );
	List<String> getAllUsedIPAddressesInSubnet(Long subnetI);
	List<String> getAllGatewaysInSubnet(Long locationId, String subnet);
	List<IPAddressDetails> getNotManagedIpAddressDetails(String ipAddress);
	List<IPAddressDetails> getNotManagedGatewayDetails(String ipAddress);
	List<IPAddressDetails> getIpAddressByName(String ipaddress, Long locationId);
	List<Networks> getSubnetForIpAndLocation(String ipAddress, Long locationId);
	List<IPTeaming> getIpAssignmentsForItem(Long itemId);
	List<String> getProxyIndexesForIpAddr(String ipAddress, Long locationId);
}
