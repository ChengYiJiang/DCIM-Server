package com.raritan.tdz.unit.circuit.validators;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;


import com.raritan.tdz.circuit.validators.PowerConnCompatibilityValidator;
import com.raritan.tdz.circuit.validators.PowerConnCompatibilityValidatorFactory;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.UnitTestBase;


public class PowerConnCompatibilityValidatorFactoryTest extends UnitTestBase {

	@Autowired
	PowerConnCompatibilityValidatorFactory validatorFactory;
  
	@Test
	public void testNullPowerConnection(){
		Assert.assertTrue(validatorFactory.getCompatibilityValidator((PowerConnection)null) == null);
	}
	
	@Test
	public void testNullDestinatonPort() {
		Assert.assertTrue(validatorFactory.getCompatibilityValidator((PowerPort)null) == null);
	}
	
	@Test
	public void testNullDestinatonPortSubClass() {
		Assert.assertTrue(validatorFactory.getCompatibilityValidator(new PowerPort()) == null);
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
		
		Assert.assertTrue(validatorFactory.getCompatibilityValidator(powerConn) == null);
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
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator != null);
	}
	
	@Test
	public void testGetRackWhipFloorOutletValidators(){
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
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator != null);
	}
	
	@Test
	public void testGetRackBuswayFloorOutletValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.BUSWAY_OUTLET));
		}});
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator != null);
	}
	
	@Test
	public void testGetRackFloorOutletValidators(){
		Mockery jmockContext = new Mockery();
		jmockContext.setImposteriser(ClassImposteriser.INSTANCE);
		jmockContext.setThreadingPolicy(new org.jmock.lib.concurrent.Synchroniser());
		
		final PowerConnection powerConn = new PowerConnection();
		final PowerPort destPort = new PowerPort();
		final LksData destPortSubClass = jmockContext.mock(LksData.class);
		
		destPort.setPortSubClassLookup(destPortSubClass);
		powerConn.setDestPowerPort(destPort);
		
		jmockContext.checking(new Expectations() {{
			oneOf(destPortSubClass).getLkpValueCode();will(returnValue(SystemLookup.PortSubClass.BUSWAY_OUTLET));
		}});
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator != null);
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
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator == null);
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
		
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator == null);
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
		
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator == null);
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
		
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator == null);
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
		
		PowerConnCompatibilityValidator powerConnValidator = validatorFactory.getCompatibilityValidator(powerConn);
		Assert.assertTrue(powerConnValidator == null);
	}

}
