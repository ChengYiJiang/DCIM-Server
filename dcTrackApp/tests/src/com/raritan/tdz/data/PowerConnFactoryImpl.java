package com.raritan.tdz.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.util.GlobalUtils;

public class PowerConnFactoryImpl implements PowerConnFactory {
	@Autowired
	SystemLookupFinderDAO systemLookupDAO;
	
	public PowerConnFactoryImpl() {
	}
	
	@Override
	public PowerConnection createConnExplicit(PowerPort sourcePort, PowerPort destPort, Long statusValueCode){
		PowerConnection conn = createConnection(sourcePort, destPort,SystemLookup.LinkType.EXPLICIT, statusValueCode);
		
		return conn;
	}
	
	@Override
	public PowerConnection createConnImplicit(PowerPort sourcePort, PowerPort destPort, Long statusValueCode){
		PowerConnection conn = createConnection(sourcePort, destPort,SystemLookup.LinkType.IMPLICIT, statusValueCode);
		
		return conn;
	}
	
	@Override
	public PowerConnection createConnection(PowerPort sourcePort, PowerPort destPort, Long typeValueCode, Long statusValueCode){
		if(statusValueCode == null){
			statusValueCode = SystemLookup.ItemStatus.PLANNED;
		}
		
		PowerConnection conn = new PowerConnection();
		LksData statusLookup = systemLookupDAO.findByLkpValueCode(statusValueCode).get(0);
		LksData connectionType = systemLookupDAO.findByLkpValueCode(typeValueCode).get(0);
		
		conn.setConnectionType(connectionType);
		conn.setSourcePowerPort(sourcePort);
		conn.setDestPowerPort(destPort);		
		conn.setStatusLookup(statusLookup);
		conn.setComments("Unit Testing");
		conn.setCreatedBy("admin");		
		conn.setCreationDate(GlobalUtils.getCurrentDate() );
		
		return conn;
	}
	
	@Override
	public List<PowerConnection> createConnectionUsingPortList(List<PowerPort> portList, Long statusValueCode){
		if(statusValueCode == null){
			statusValueCode = SystemLookup.ItemStatus.PLANNED;
		}
		
		LksData statusLookup = systemLookupDAO.findByLkpValueCode(statusValueCode).get(0);
		LksData connectionType = systemLookupDAO.findByLkpValueCode(SystemLookup.LinkType.EXPLICIT).get(0);
		Timestamp currentDate = GlobalUtils.getCurrentDate();
		
		List<PowerConnection> connList = new ArrayList<PowerConnection>();
		PowerConnection conn;
		PowerConnection connPrior = null;
		
		for(PowerPort port:portList){
			conn = new PowerConnection();
			conn.setConnectionType(connectionType);
			conn.setSourcePowerPort(port);
			conn.setDestPowerPort(null);		
			conn.setStatusLookup(statusLookup);
			conn.setComments("Unit Testing");
			conn.setCreatedBy("admin");		
			conn.setCreationDate(currentDate);
			
			if(connPrior != null){
				connPrior.setDestPowerPort(port);
			}
			connList.add(conn);
			
			connPrior = conn;
		}
		
		return connList;
	}

}
