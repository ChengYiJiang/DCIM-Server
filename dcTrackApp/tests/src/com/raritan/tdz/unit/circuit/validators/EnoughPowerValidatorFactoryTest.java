package com.raritan.tdz.unit.circuit.validators;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorBranchCircuitBreaker;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorFactory;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorFloorPDU;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorFloorPDUPanel;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorRackPDUFuse;
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorUPS;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.UnitTestBase;


public class EnoughPowerValidatorFactoryTest extends UnitTestBase {

	@Autowired
	EnoughPowerValidatorFactory enoughPowerValidatorFactory;
  
	@Test
	public void testNullPowerConnection(){
		Assert.assertTrue(enoughPowerValidatorFactory.getValidators((PowerConnection)null).size() == 0);
	}
	
	@Test
	public void testNullDestinatonPort() {
		Assert.assertTrue(enoughPowerValidatorFactory.getValidators((PowerPort)null).size() == 0);
	}
	
	@Test
	public void testNullItemOnDestPort() {
		Assert.assertTrue(enoughPowerValidatorFactory.getValidators(new PowerPort()).size() == 0);
	}
	
	@Test
	public void testGetPowerSupplyValidators() {
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.POWER_SUPPLY));
		}});
		
		Assert.assertTrue(enoughPowerValidatorFactory.getValidators(powerConn).size() == 0);
	}
	
	@Test
	public void testGetRackPDUOutletValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
		Assert.assertTrue(enoughPowerValidators.get(0) instanceof EnoughPowerValidatorRackPDUFuse);
	}
	
	@Test
	public void testGetBranchCircuitBreakerValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
		Assert.assertTrue(enoughPowerValidators.get(0) instanceof EnoughPowerValidatorBranchCircuitBreaker);
	}
	
	@Test
	public void testGetPanelBreakerValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.PANEL_BREAKER));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
		Assert.assertTrue(enoughPowerValidators.get(0) instanceof EnoughPowerValidatorFloorPDUPanel);
	}
	
	@Test
	public void testGetFloorPDUMainBrakerValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.PDU_INPUT_BREAKER));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
		Assert.assertTrue(enoughPowerValidators.get(0) instanceof EnoughPowerValidatorFloorPDU);
	}
	
	@Test
	public void testGetUPSValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
		Assert.assertTrue(enoughPowerValidators.get(0) instanceof EnoughPowerValidatorUPS);
	}
	
	@Test
	public void testGetRackPDUInputCordValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.INPUT_CORD));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 1);
	}

	@Test
	public void testGetOutletValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.WHIP_OUTLET));
		}});
		
		List<EnoughPowerValidator> enoughPowerValidators = enoughPowerValidatorFactory.getValidators(powerConn);
		Assert.assertTrue(enoughPowerValidators.size() == 0);
	}

}
