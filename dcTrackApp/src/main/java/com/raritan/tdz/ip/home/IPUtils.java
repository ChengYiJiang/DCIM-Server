package com.raritan.tdz.ip.home;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.ip.dao.IPAddressDetailsDAO;
import com.raritan.tdz.ip.domain.IPAddressDetails;

public class IPUtils {
	
	@Autowired(required=true)
	IPAddressDetailsDAO ipAddressDetailsDAO;
	
	public enum IPStatus{
		SINGLE_IP,
		DUP_IP,
		TEAMED_IP,
		UNKNOWN,
	};
	
	public Long getLocationId( DataPort port ){
		assert( port != null );
		Item item = port.getItem();	
		Long locationId = item.getDataCenterLocation().getDataCenterLocationId();
		assert( locationId != null );
		return locationId;
	}
	
	public IPStatus getIPStatus( String ipAddress, Long locationId ){
		assert( ipAddress != null );
		IPStatus retval = IPStatus.UNKNOWN;
		
		List<IPAddressDetails> allips = ipAddressDetailsDAO.getIpAddressByName( ipAddress, locationId);
		if( allips.size() > 1 ){
			retval = IPStatus.DUP_IP;
			for( IPAddressDetails ip : allips ){
				assert( ip.getDataPortsUsingIP().size() == 1);
			}
		}
		else if( allips.size() < 0 ) retval = IPStatus.SINGLE_IP;
		else if( allips.size() == 1 ){
			Set<DataPort> alldps = allips.get(0).getDataPortsUsingIP();
			if( alldps.size() > 1 ) retval = IPStatus.TEAMED_IP;
			else retval = IPStatus.SINGLE_IP;
		}
		return retval;
	}
}
