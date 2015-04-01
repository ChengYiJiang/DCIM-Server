package com.raritan.tdz.unit.powerchain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.powerchain.home.PowerConnectionAdaptor;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

public class PowerConnectionAdaptorTest extends UnitTestBase {
	
	@Autowired
	private UnitTestDatabaseIdGenerator unitTestIdGenerator;
	
	@Autowired
	private PowerConnectionAdaptor powerConnectionAdaptor;
	
	@Test
	public void testNewConnectionInvalidSource() {
		PowerConnection conn = powerConnectionAdaptor.convert(null, null);
		Assert.isTrue(conn == null, "Connection should not have established");
	}
	
	@Test
	public void testNewConnection() {
		
		final Long srcPortId = unitTestIdGenerator.nextId();
		final Long destPortId = unitTestIdGenerator.nextId();
		
		final PowerPort srcPort = new PowerPort();
		srcPort.setPortId(srcPortId);
		srcPort.setItem(null);

		final PowerPort destPort = new PowerPort();
		destPort.setPortId(destPortId);
		destPort.setItem(null);

		PowerConnection conn = powerConnectionAdaptor.convert(srcPort, destPort);
		Assert.notNull(conn, "Connection should have established");
		
	}

}
