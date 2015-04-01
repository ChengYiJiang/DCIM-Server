package com.raritan.tdz.unit.item.port;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class DataPortMockImpl implements DataPortMock {
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;
	
	@Autowired
	protected SystemLookupInitUnitTest systemLookupInitTest;

	@Autowired
	protected DataPortExpectations expectations;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortMock#createPortsForItem(org.jmock.Mockery, com.raritan.tdz.domain.Item, java.lang.Long, int, boolean)
	 */
	public DataPort createPortsForItem(Mockery jmockContext, Item item, Long portSubClassValueCode, int quantity, boolean isUsed){
		LksData portSubClassLookup = systemLookupInitTest.getLks(portSubClassValueCode);
		LksData media = systemLookupInitTest.getLks(SystemLookup.MediaType.TWISTED_PAIR);
		
		ConnectorLkuData connectorLookup = createConnector();
		LkuData speedLku = createSpeed();
		LkuData protocolLku = createProtocol();
		
		Long portId;
		List<DataPort> recList = new ArrayList<DataPort>();
		DataPort port = null;
		
		for(int i = 0; i<quantity; i++){
			port = new DataPort();
			port.setPortId(unitTestIdGenerator.nextId());
			port.setPortName("Net-" + i);
			port.setPortSubClassLookup(portSubClassLookup);			
			port.setConnectorLookup(connectorLookup);
			port.setMediaId(media);
			port.setSpeedId(speedLku);
			port.setProtocolID(protocolLku);
			port.setItem(item);
			
			item.addDataPort(port);
			
			if(isUsed){
				port.setUsed(true);
				recList.add(port);
			}
			
			portId = port.getPortId();
			
			expectations.createOneOfLoadEvictedPort(jmockContext, portId, port);
			expectations.createRead(jmockContext, portId, port);			
		}
		
		expectations.createCreate(jmockContext);
		expectations.createMerge(jmockContext);
		expectations.createMergeOnly(jmockContext);
		expectations.createFindUsedPorts(jmockContext, item.getItemId(), recList);
		
		return port;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortMock#createConnector()
	 */
	public ConnectorLkuData createConnector(){
		ConnectorLkuData c = new ConnectorLkuData();
		c.setConnectorId(10000L);
		c.setConnectorName("RJ45");
		c.setDescription("RJ45 TEST");
		c.setTypeName("data");
		return c;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortMock#createSpeed()
	 */
	public LkuData createSpeed(){
		LkuData s = new LkuData();
		s.setLkuId(20000L);
		s.setLkuValue("100 MB Bit");
		
		return s;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortMock#createProtocol()
	 */
	public LkuData createProtocol(){
		LkuData p = new LkuData();
		p.setLkuId(30000L);
		p.setLkuValue("Ethernet IP");
		
		return p;
	}
}
