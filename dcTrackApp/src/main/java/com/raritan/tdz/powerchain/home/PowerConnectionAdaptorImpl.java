package com.raritan.tdz.powerchain.home;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;

/**
 * adapts the value in different tables and creates a power connection domain object
 * @author bunty
 *
 */
public class PowerConnectionAdaptorImpl implements PowerConnectionAdaptor {

	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	private Map<Long, LksData> portLkpValueCodeLksData = new HashMap<Long, LksData>();

	
	@Override
	public PowerConnection convert(PowerPort srcPort, PowerPort destPort) {
		
		if (null == srcPort) {
			return null;
		}
		
		PowerConnection powerConnection = new PowerConnection();
		
		LksData connType = getLksDataUsingLkpCode(SystemLookup.LinkType.IMPLICIT);
		LksData statusLksData = getLksDataUsingLkpCode(SystemLookup.ItemStatus.INSTALLED);
		
		powerConnection.setSourcePowerPort(srcPort);
		powerConnection.setDestPowerPort(destPort);
		// List<LksData> lksData = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.LinkType.IMPLICIT);
		// powerConnection.setConnectionType(lksData.get(0));
		powerConnection.setConnectionType(connType);
		powerConnection.setStatusLookup(statusLksData);
		powerConnection.setSortOrder(0);
		java.util.Date date = new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		if (null == powerConnection.getCreationDate()) {
			powerConnection.setCreationDate(timeStamp);
		}
		// powerConnection.setUpdateDate(timeStamp);
		// powerConnection.setCreatedBy(null); // TODO:: user editing the item.
		// powerConnection.setCircuitPowerId(null);
		if (null != destPort) {
			destPort.setUsed(true);
		}
		srcPort.addSourcePowerConnections(powerConnection);
		
		return powerConnection;
		
	}

	@Override
	public PowerConnection update(PowerConnection powerConnection, PowerPort destPort) {
		if (null == powerConnection) {
			return null;
		}
		java.util.Date date = new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		powerConnection.setDestPowerPort(destPort);
		powerConnection.setUpdateDate(timeStamp);
		return powerConnection;
	}

	private LksData getLksDataUsingLkpCode(Long lkpValueCode) {
		
		LksData lksData = portLkpValueCodeLksData.get(lkpValueCode);
		if (null == lksData) {
			lksData = systemLookupFinderDAO.findByLkpValueCode(lkpValueCode).get(0);
			portLkpValueCodeLksData.put(lkpValueCode, lksData);
		}
		
		return lksData;
	}

	
}
