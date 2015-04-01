/**
 * 
 */
package com.raritan.tdz.unit.circuit.validators;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.validators.PowerConnCompatibilityValidator;
import com.raritan.tdz.circuit.validators.PowerConnConnectorValidator;
import com.raritan.tdz.circuit.validators.PowerConnRPDULoopValidator;
import com.raritan.tdz.circuit.validators.PowerConnRPDUOutletPhaseValidator;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class RackPDUConnCompatibilityValidatorTest extends UnitTestBase {
	
	@Autowired
	private PowerConnCompatibilityValidator rpduPowerConnCompatibilityValidator;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private PowerConnDAO powerConnDAO;
	
	@Autowired
	private UnitTestDatabaseIdGenerator idGenerator;
	
	@Autowired
	private SystemLookupInitUnitTest systemLookupUnitTest;
	
	

	  @Autowired
	  private SystemLookupFinderDAO systemLookupFinderDAO;

	  private Errors errors;
	  
	  private LksData defaultVolts;
	  private LksData defaultPhase;
	  
	  @BeforeMethod
	  public void beforeMethod() {
		  errors = getErrorObject(CircuitPDHome.class);
		  defaultVolts = systemLookupFinderDAO.findByLkpValueAndType("120", "VOLTS").get(0);
		  defaultPhase = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.PhaseIdClass.THREE_WYE).get(0);
	  }
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testInvalidArgs(){
		try {
			rpduPowerConnCompatibilityValidator.validate(new PowerConnection(),errors);
		} catch (IllegalArgumentException e){
			Assert.assertTrue(e.getMessage().contains("You must provide a Map of String and object for this validator"));
			throw e;
		}
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testInvalidPowerConnection(){
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), null);
		targetMap.put(Integer.class.getName(), 0);
		
		try {
			rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		} catch (IllegalArgumentException e){
			Assert.assertTrue(e.getMessage().contains("You must provide a power connection target"));
			throw e;
		}
	}
	
	@Test
	public void testInvalidSrcAndDstPort(){
		PowerConnection powerConn = new PowerConnection();
		final int noOfValidators = 6;
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertTrue(errors.getErrorCount() == ((2 * noOfValidators) - 1));
		Assert.assertTrue(errors.getAllErrors().get(0).getCode().equals("powerProc.missingSourcePort"));
	}
	
	@Test
	public void testRPDULoop(){
		final long srcItemId = idGenerator.nextId();
		final long srcPortId = idGenerator.nextId();
		final long dstItemId = srcItemId;
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPortSubClass = systemLookupUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD);
		
		LksData dstPortSubClass = systemLookupUnitTest.getLks(SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
		
		LksData srcPortPhase = systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE);
		LksData dstPortPhase = systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE);
		
		LksData srcPortVolts = defaultVolts;
		LksData dstPortVolts = defaultVolts;
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		
		
		Item srcItem = new Item();
		srcItem.setItemId(srcItemId);
		
		Item dstItem = new Item();
		dstItem.setItemId(dstItemId);
		
		
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setItem(srcItem);
		srcPowerPortDB.setPortSubClassLookup(srcPortSubClass);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		srcPowerPortDB.setPhaseLookup(srcPortPhase);
		srcPowerPortDB.setVoltsLookup(srcPortVolts);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setPortSubClassLookup(dstPortSubClass);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		dstPowerPortDB.setPhaseLookup(dstPortPhase);
		dstPowerPortDB.setVoltsLookup(dstPortVolts);
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.RackPDUConnectionLoop")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testIncorrectPhaseThreePhaseSinglePhase(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		LksData srcPortVolts = defaultVolts;
		LksData dstPortVolts = defaultVolts;
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPhaseLookup = new LksData();
		srcPhaseLookup.setLksId(50001L);
		srcPhaseLookup.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
		
		LksData dstPhaseLookup = new LksData();
		dstPhaseLookup.setLksId(50002L);
		dstPhaseLookup.setLkpValueCode(SystemLookup.PhaseIdClass.SINGLE_2WIRE);
		
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setPhaseLookup(srcPhaseLookup);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		srcPowerPortDB.setVoltsLookup(srcPortVolts);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setPhaseLookup(dstPhaseLookup);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		dstPowerPortDB.setVoltsLookup(dstPortVolts);
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.phaseMismatch")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testCorrectPhaseSinglePhaseSinglePhase(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPhaseLookup = systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_3WIRE);
		
		LksData dstPhaseLookup = systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE);
		
		LksData srcPortVolts = defaultVolts;
		LksData dstPortVolts = defaultVolts;
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setVoltsLookup(srcPortVolts);
		srcPowerPortDB.setPhaseLookup(srcPhaseLookup);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setPhaseLookup(dstPhaseLookup);
		dstPowerPortDB.setVoltsLookup(dstPortVolts);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void testIncorrectConnectorDeviceAndRPDUOutput(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData connectionType = new LksData();
		connectionType.setLkpValueCode(SystemLookup.LinkType.EXPLICIT);
		
		powerConn.setConnectionType(connectionType);
		
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
			allowing(powerConnDAO).areConnectorsCompatible(with(connectorSrc), with(connectorDst));will(returnValue(false));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.incompatibleConnector")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testIncompatibleVolts(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		LksData srcPortVolts = defaultVolts;
		LksData dstPortVolts = systemLookupUnitTest.getLks(SystemLookup.VoltClass.V_230);
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPhaseLookup = new LksData();
		srcPhaseLookup.setLksId(50001L);
		srcPhaseLookup.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
		
		LksData dstPhaseLookup = new LksData();
		dstPhaseLookup.setLksId(50002L);
		dstPhaseLookup.setLkpValueCode(SystemLookup.PhaseIdClass.SINGLE_2WIRE);
		
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setPhaseLookup(srcPhaseLookup);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		srcPowerPortDB.setVoltsLookup(srcPortVolts);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setPhaseLookup(dstPhaseLookup);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		dstPowerPortDB.setVoltsLookup(dstPortVolts);
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		rpduPowerConnCompatibilityValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.voltageMismatch")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
}
