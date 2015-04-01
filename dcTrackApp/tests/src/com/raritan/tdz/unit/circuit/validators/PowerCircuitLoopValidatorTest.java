/**
 * 
 */
package com.raritan.tdz.unit.circuit.validators;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.validators.PowerCircuitLoopValidator;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;
import com.raritan.tdz.vbjavabridge.subscribers.PowerConnectionsSubscriberImpl;

/**
 * @author prasanna
 *
 */
public class PowerCircuitLoopValidatorTest extends UnitTestBase {
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private PowerCircuitLoopValidator powerCircuitLoopValidator;
	
	@Autowired
	private SystemLookupInitUnitTest systemLookupInitUnitTest;
	
	@Autowired
	private UnitTestDatabaseIdGenerator idGenerator;
	
	private Errors errors;
	  
	@BeforeMethod
	public void beforeMethod() {
	 errors = getErrorObject(CircuitPDHome.class);
	}
	
	@Test
	public void testLoopSamePortInOneConnection(){
		//First create the port
		final Long portId = idGenerator.nextId();
		PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		
		final PowerPort powerPortDB = new PowerPort();
		powerPortDB.setPortId(portId);
		powerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
		

		//Create the connection
		PowerConnection powerConnection = new PowerConnection();
		powerConnection.setSourcePowerPort(powerPort);
		powerConnection.setDestPowerPort(powerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPortDB));
		}});
		
		//Create circuit out of the connection
		List<PowerConnection> circuitConnections = new ArrayList<PowerConnection>();
		circuitConnections.add(powerConnection);
		
		PowerCircuit powerCircuit = new PowerCircuit();
		powerCircuit.setCircuitConnections(circuitConnections);
		
		//Validate
		powerCircuitLoopValidator.validate(powerCircuit, errors);
		
		//Check the validation errors
		Assert.assertTrue(errors.hasErrors());
		
	}
	
	@Test
	public void testLoopDifferentPortSameItemOneConnection(){
		//First create the port
		final Long srcPortId = idGenerator.nextId();
		final Long srcItemId = idGenerator.nextId();
		final Long dstItemId = idGenerator.nextId();
		
		PowerPort srcPowerPort = new PowerPort();
		srcPowerPort.setPortId(srcPortId);
		
		Item srcItem = new Item();
		srcItem.setItemId(srcItemId);
		
		
		final PowerPort srcPowerPortDB = new PowerPort();
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
		srcPowerPortDB.setItem(srcItem);
		
		
		
		final Long dstPortId = idGenerator.nextId();
		PowerPort dstPowerPort = new PowerPort();
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort dstPowerPortDB = new PowerPort();
		dstPowerPortDB.setPortId(srcPortId);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));
		dstPowerPortDB.setItem(srcItem);
		
		

		//Create the connection
		PowerConnection powerConnection = new PowerConnection();
		powerConnection.setSourcePowerPort(srcPowerPort);
		powerConnection.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		//Create circuit out of the connection
		List<PowerConnection> circuitConnections = new ArrayList<PowerConnection>();
		circuitConnections.add(powerConnection);
		
		PowerCircuit powerCircuit = new PowerCircuit();
		powerCircuit.setCircuitConnections(circuitConnections);
		
		//Validate
		powerCircuitLoopValidator.validate(powerCircuit, errors);
		
		//Check the validation errors
		Assert.assertTrue(errors.hasErrors());
		
	}
	
	@Test
	public void testNoLoop(){
		//First create the port
		final Long srcPortId = idGenerator.nextId();
		final Long srcItemId = idGenerator.nextId();
		final Long dstItemId = idGenerator.nextId();
		
		PowerPort srcPowerPort = new PowerPort();
		srcPowerPort.setPortId(srcPortId);
		
		Item srcItem = new Item();
		srcItem.setItemId(srcItemId);
		
		
		final PowerPort srcPowerPortDB = new PowerPort();
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
		srcPowerPortDB.setItem(srcItem);
		
		
		
		final Long dstPortId = idGenerator.nextId();
		PowerPort dstPowerPort = new PowerPort();
		dstPowerPort.setPortId(dstPortId);
		
		Item dstItem = new Item();
		dstItem.setItemId(dstItemId);
		
		final PowerPort dstPowerPortDB = new PowerPort();
		dstPowerPortDB.setPortId(srcPortId);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));
		dstPowerPortDB.setItem(dstItem);
		
		

		//Create the connection
		PowerConnection powerConnection = new PowerConnection();
		powerConnection.setSourcePowerPort(srcPowerPort);
		powerConnection.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		//Create circuit out of the connection
		List<PowerConnection> circuitConnections = new ArrayList<PowerConnection>();
		circuitConnections.add(powerConnection);
		
		PowerCircuit powerCircuit = new PowerCircuit();
		powerCircuit.setCircuitConnections(circuitConnections);
		
		//Validate
		powerCircuitLoopValidator.validate(powerCircuit, errors);
		
		//Check the validation errors
		Assert.assertFalse(errors.hasErrors());
		
	}
	

	
	//Check this:
	// src --> dst
	// dst --> src
	// The above is considered a loop!
	@Test
	public void testLoopSamePortBetweenTwoConnections(){
		//First create the port
		final Long srcPortId = idGenerator.nextId();
		final Long srcItemId = idGenerator.nextId();
		final Long dstItemId = idGenerator.nextId();
		
		PowerPort srcPowerPort = new PowerPort();
		srcPowerPort.setPortId(srcPortId);
		
		Item srcItem = new Item();
		srcItem.setItemId(srcItemId);
		
		
		final PowerPort srcPowerPortDB = new PowerPort();
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.WHIP_OUTLET));
		srcPowerPortDB.setItem(srcItem);
		
		
		
		final Long dstPortId = idGenerator.nextId();
		PowerPort dstPowerPort = new PowerPort();
		dstPowerPort.setPortId(dstPortId);
		
		Item dstItem = new Item();
		dstItem.setItemId(dstItemId);
		
		final PowerPort dstPowerPortDB = new PowerPort();
		dstPowerPortDB.setPortId(srcPortId);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
		dstPowerPortDB.setItem(dstItem);


		//Create the connection
		//Like this:
		// src --> dst
		// dst --> src
		// The above is considered a loop!
		PowerConnection powerConnection1 = new PowerConnection();
		powerConnection1.setSourcePowerPort(srcPowerPort);
		powerConnection1.setDestPowerPort(dstPowerPort);
		PowerConnection powerConnection2 = new PowerConnection();
		powerConnection2.setSourcePowerPort(dstPowerPort);
		powerConnection2.setDestPowerPort(srcPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		//Create circuit out of the connection
		List<PowerConnection> circuitConnections = new ArrayList<PowerConnection>();
		circuitConnections.add(powerConnection1);
		circuitConnections.add(powerConnection2);
		
		PowerCircuit powerCircuit = new PowerCircuit();
		powerCircuit.setCircuitConnections(circuitConnections);
		
		//Validate
		powerCircuitLoopValidator.validate(powerCircuit, errors);
		
		//Check the validation errors
		Assert.assertTrue(errors.hasErrors());
	}
	
	//Check this:
	// A --> B
	// B --> C
	// C --> D
	// D --> A
	// The above is considered a loop!
	@Test
	public void testLoopBetweenFourConnections(){
		//First create the port
		final Long portAId = idGenerator.nextId();
		final Long portAItemId = idGenerator.nextId();
		
		final Long portBId = idGenerator.nextId();
		final Long portBItemId = idGenerator.nextId();
		
		final Long portCId = idGenerator.nextId();
		final Long portCItemId = idGenerator.nextId();
		
		final Long portDId = idGenerator.nextId();
		final Long portDItemId = idGenerator.nextId();
		
		
		final Item portAItem = new Item();
		portAItem.setItemId(portAItemId);
		
		final Item portBItem = new Item();
		portAItem.setItemId(portBItemId);
		
		final Item portCItem = new Item();
		portAItem.setItemId(portCItemId);
		
		final Item portDItem = new Item();
		portAItem.setItemId(portDItemId);
		
		final PowerPort portA = new PowerPort();
		portA.setPortId(portAId);
		final PowerPort portADB = new PowerPort();
		portADB.setPortId(portAId);
		portADB.setItem(portAItem);
		
		
		final PowerPort portB = new PowerPort();
		portB.setPortId(portBId);
		final PowerPort portBDB = new PowerPort();
		portBDB.setPortId(portBId);
		portBDB.setItem(portBItem);
		
		final PowerPort portC = new PowerPort();
		portC.setPortId(portCId);
		final PowerPort portCDB = new PowerPort();
		portCDB.setPortId(portCId);
		portCDB.setItem(portCItem);
		
		final PowerPort portD = new PowerPort();
		portD.setPortId(portDId);
		final PowerPort portDDB = new PowerPort();
		portDDB.setPortId(portDId);
		portDDB.setItem(portDItem);
		
		
		PowerConnection powerConnAB = new PowerConnection();
		powerConnAB.setSourcePowerPort(portA);
		powerConnAB.setDestPowerPort(portB);
		
		PowerConnection powerConnBC = new PowerConnection();
		powerConnBC.setSourcePowerPort(portB);
		powerConnBC.setDestPowerPort(portC);
		
		PowerConnection powerConnCD = new PowerConnection();
		powerConnCD.setSourcePowerPort(portC);
		powerConnCD.setDestPowerPort(portD);
		
		PowerConnection powerConnDA = new PowerConnection();
		powerConnDA.setSourcePowerPort(portD);
		powerConnDA.setDestPowerPort(portA);
		
		
		List<PowerConnection> powerConnections = new ArrayList<PowerConnection>();
		powerConnections.add(powerConnAB);
		powerConnections.add(powerConnBC);
		powerConnections.add(powerConnCD);
		powerConnections.add(powerConnDA);
		
		PowerCircuit powerCircuit = new PowerCircuit();
		powerCircuit.setCircuitConnections(powerConnections);
		
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(portAId)); will(returnValue(portADB));
			allowing(powerPortDAO).read(with(portBId)); will(returnValue(portBDB));
			allowing(powerPortDAO).read(with(portCId)); will(returnValue(portCDB));
			allowing(powerPortDAO).read(with(portDId)); will(returnValue(portDDB));
		}});
		
		//Validate
		powerCircuitLoopValidator.validate(powerCircuit, errors);
		
		//Check the validation errors
		Assert.assertTrue(errors.hasErrors());
		
	}
}
