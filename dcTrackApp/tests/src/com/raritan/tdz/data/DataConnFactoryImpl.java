package com.raritan.tdz.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.util.GlobalUtils;

public class DataConnFactoryImpl implements DataConnFactory {
	@Autowired
	SystemLookupFinderDAO systemLookupDAO;
	
	public DataConnFactoryImpl() {
	}
	
	@Override
	public DataConnection createConnPlannedExplicit(DataPort sourcePort, DataPort destPort, Long statusValueCode){
		DataConnection conn = createConnection(sourcePort, destPort,SystemLookup.LinkType.EXPLICIT, statusValueCode);
		
		return conn;
	}
	
	@Override
	public DataConnection createConnPlannedImplicit(DataPort sourcePort, DataPort destPort, Long statusValueCode){
		DataConnection conn = createConnection(sourcePort, destPort,SystemLookup.LinkType.IMPLICIT, statusValueCode);
		
		return conn;
	}

	@Override
	public DataConnection createConnection(DataPort sourcePort, DataPort destPort, Long typeValueCode, Long statusValueCode){
		if(statusValueCode == null){
			statusValueCode = SystemLookup.ItemStatus.PLANNED;
		}
		
		DataConnection conn = new DataConnection();
		LksData statusLookup = systemLookupDAO.findByLkpValueCode(statusValueCode).get(0);
		LksData connectionType = systemLookupDAO.findByLkpValueCode(typeValueCode).get(0);
		
		conn.setConnectionType(connectionType);
		conn.setSourceDataPort(sourcePort);
		conn.setDestDataPort(destPort);		
		conn.setStatusLookup(statusLookup);
		conn.setComments("Unit Testing");
		conn.setCreatedBy("admin");		
		conn.setCreationDate(GlobalUtils.getCurrentDate() );
		
		return conn;
	}
	
	@Override
	public List<DataConnection> createConnectionUsingPortList(List<DataPort> portList, Long statusValueCode){
		if(statusValueCode == null){
			statusValueCode = SystemLookup.ItemStatus.PLANNED;
		}
		
		LksData statusLookup = systemLookupDAO.findByLkpValueCode(statusValueCode).get(0);
		LksData connectionType = systemLookupDAO.findByLkpValueCode(SystemLookup.LinkType.EXPLICIT).get(0);
		Timestamp currentDate = GlobalUtils.getCurrentDate();
		
		List<DataConnection> connList = new ArrayList<DataConnection>();
		DataConnection conn;
		DataConnection connPrior = null;
		
		for(DataPort port:portList){
			conn = new DataConnection();
			conn.setConnectionType(connectionType);
			conn.setSourceDataPort(port);
			conn.setDestDataPort(null);		
			conn.setStatusLookup(statusLookup);
			conn.setComments("Unit Testing");
			conn.setCreatedBy("admin");		
			conn.setCreationDate(currentDate);
			
			if(connPrior != null){
				connPrior.setDestDataPort(port);
			}
			connList.add(conn);
			
			connPrior = conn;
		}
		
		return connList;
	}

}
