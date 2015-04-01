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

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.validators.PowerConnPortValidator;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;


/**
 * @author prasanna
 *
 */
public class PowerConnPortValidatorTest extends UnitTestBase {
	
	@Autowired
	private PowerConnPortValidator powerConnValidator;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private UnitTestDatabaseIdGenerator idGenerator;
	
	@Autowired
	private SystemLookupInitUnitTest systemLookupInitUnitTest;
	
	private Errors errors;
	  
	@BeforeMethod
	public void beforeMethod() {
	 errors = getErrorObject(CircuitPDHome.class);
	}
	  
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testInvalidArgs(){
		try {
			powerConnValidator.validate(new PowerConnection(),errors);
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
			powerConnValidator.validate(targetMap, errors);
		} catch (IllegalArgumentException e){
			Assert.assertTrue(e.getMessage().contains("You must provide a power connection target"));
			throw e;
		}
	}
	
	@Test
	public void testInvalidSrcAndDstPort(){
		PowerConnection powerConn = new PowerConnection();
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertTrue(errors.getErrorCount() == 1);
		Assert.assertTrue(errors.getAllErrors().get(0).getCode().equals("powerProc.missingSourcePort"));
	}
	
	// Power Connection validator no more check for the used flag, used flag validation is performed by PowerCircuitValidator 
	// @Test
	public void testPortUsed(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		final ConnectorLkuData connectorSrc = new ConnectorLkuData();
		final ConnectorLkuData connectorDst = new ConnectorLkuData();
		final LksData phaseSrc = new LksData();
		final LksData phaseDst = new LksData();
		final LksData voltSrc = new LksData();
		final LksData voltDst = new LksData();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(true);
		srcPowerPortDB.setConnectorLookup(connectorSrc);
		srcPowerPortDB.setPhaseLookup(phaseSrc);
		srcPowerPortDB.setVoltsLookup(voltSrc);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(true);
		dstPowerPortDB.setConnectorLookup(connectorDst);
		dstPowerPortDB.setPhaseLookup(phaseDst);
		dstPowerPortDB.setVoltsLookup(voltDst);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.srcPortUsed")) errorCode = true;
			if (errorCode == true && error.getCode().equals("powerProc.dstPortUsed")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testInvalidSrcAndDstConnector(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingConnector")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testInvalidSrcAndDstPhase(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingPhase")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testInvalidVoltsLookup(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		LksData phaseLks = new LksData();
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		srcPowerPortDB.setPhaseLookup(phaseLks);
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		dstPowerPortDB.setPhaseLookup(phaseLks);
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			allowing(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			allowing(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingVolts")) errorCode = true;
		}
	}
	
	@Test
	public void testInvalidWattsBudgetOnDevice(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		LksData phaseLks = new LksData();
		LksData voltsLks = new LksData();
		
		LksData srcItemClass = new LksData();
		srcItemClass.setLkpValueCode(SystemLookup.Class.DEVICE);
		
		LksData dstItemClass = new LksData();
		dstItemClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		
		Item srcItem = new Item();
		Item dstItem = new Item();
		
		srcItem.setClassLookup(srcItemClass);
		dstItem.setClassLookup(dstItemClass);
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		srcPowerPortDB.setPhaseLookup(phaseLks);
		srcPowerPortDB.setVoltsLookup(voltsLks);
		srcPowerPortDB.setItem(srcItem);
		srcPowerPortDB.setAmpsBudget(0);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.POWER_SUPPLY));
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		dstPowerPortDB.setPhaseLookup(phaseLks);
		dstPowerPortDB.setVoltsLookup(voltsLks);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setAmpsBudget(0);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			atMost(2).of(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			atMost(2).of(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingBudgetWatts")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testInvalidAmpsNamePlateOnFloorOutlet(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		LksData phaseLks = new LksData();
		LksData voltsLks = new LksData();
		
		LksData srcItemClass = new LksData();
		srcItemClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		
		LksData dstItemClass = new LksData();
		dstItemClass.setLkpValueCode(SystemLookup.Class.FLOOR_OUTLET);
		
		Item srcItem = new Item();
		Item dstItem = new Item();
		
		srcItem.setClassLookup(srcItemClass);
		dstItem.setClassLookup(dstItemClass);
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		srcPowerPortDB.setPhaseLookup(phaseLks);
		srcPowerPortDB.setVoltsLookup(voltsLks);
		srcPowerPortDB.setItem(srcItem);
		srcPowerPortDB.setAmpsBudget(150);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		dstPowerPortDB.setPhaseLookup(phaseLks);
		dstPowerPortDB.setVoltsLookup(voltsLks);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setAmpsBudget(150);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.WHIP_OUTLET));
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			atMost(2).of(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			atMost(2).of(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.misingAmpsRating")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);

	}
	
	@Test
	public void testInvalidAmpsBudgetOnRPDU(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		LkuData fuse = new LkuData();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		LksData phaseLks = new LksData();
		LksData voltsLks = new LksData();
		
		LksData srcItemClass = new LksData();
		srcItemClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		
		LksData dstItemClass = new LksData();
		dstItemClass.setLkpValueCode(SystemLookup.Class.FLOOR_OUTLET);
		
		Item srcItem = new Item();
		Item dstItem = new Item();
		
		srcItem.setClassLookup(srcItemClass);
		dstItem.setClassLookup(dstItemClass);
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		srcPowerPortDB.setPhaseLookup(phaseLks);
		srcPowerPortDB.setVoltsLookup(voltsLks);
		srcPowerPortDB.setItem(srcItem);
		srcPowerPortDB.setAmpsBudget(0);
		srcPowerPortDB.setFuseLookup(fuse);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		dstPowerPortDB.setPhaseLookup(phaseLks);
		dstPowerPortDB.setVoltsLookup(voltsLks);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setAmpsBudget(150);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.WHIP_OUTLET));
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			atMost(2).of(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			atMost(2).of(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingAmpsRatingFuse")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testNullInputCord(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		PowerPort srcPowerPort = new PowerPort();
		PowerPort dstPowerPort = new PowerPort();
		
		LkuData fuse = new LkuData();
		
		ConnectorLkuData connectorLku = new ConnectorLkuData();
		LksData phaseLks = new LksData();
		LksData voltsLks = new LksData();
		
		LksData srcItemClass = new LksData();
		srcItemClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		
		LksData dstItemClass = new LksData();
		dstItemClass.setLkpValueCode(SystemLookup.Class.FLOOR_OUTLET);
		
		Item srcItem = new Item();
		Item dstItem = new Item();
		
		srcItem.setClassLookup(srcItemClass);
		dstItem.setClassLookup(dstItemClass);
		
		
		srcPowerPort.setPortId(srcPortId);
		dstPowerPort.setPortId(dstPortId);
		
		final PowerPort srcPowerPortDB = new PowerPort();
		final PowerPort dstPowerPortDB = new PowerPort();
		
		srcPowerPortDB.setPortId(srcPortId);
		srcPowerPortDB.setUsed(false);
		srcPowerPortDB.setConnectorLookup(connectorLku);
		srcPowerPortDB.setPhaseLookup(phaseLks);
		srcPowerPortDB.setVoltsLookup(voltsLks);
		srcPowerPortDB.setItem(srcItem);
		srcPowerPortDB.setAmpsBudget(0);
		srcPowerPortDB.setFuseLookup(fuse);
		srcPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));//This is used to generate invalid data
		
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setUsed(false);
		dstPowerPortDB.setConnectorLookup(connectorLku);
		dstPowerPortDB.setPhaseLookup(phaseLks);
		dstPowerPortDB.setVoltsLookup(voltsLks);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setAmpsBudget(150);
		dstPowerPortDB.setPortSubClassLookup(systemLookupInitUnitTest.getLks(SystemLookup.PortSubClass.BUSWAY_OUTLET));
		
		
		powerConn.setSourcePowerPort(srcPowerPort);
		powerConn.setDestPowerPort(dstPowerPort);
		
		jmockContext.checking(new Expectations() {{	
			atMost(2).of(powerPortDAO).read(with(srcPortId)); will(returnValue(srcPowerPortDB));
			atMost(2).of(powerPortDAO).read(with(dstPortId)); will(returnValue(dstPowerPortDB));
		}});
		
		Map<String, Object> targetMap = new HashMap<String, Object>();
		targetMap.put(PowerConnection.class.getName(), powerConn);
		targetMap.put(Integer.class.getName(), 0);
		
		powerConnValidator.validate(targetMap, errors);
		
		Assert.assertTrue(errors.hasErrors());
		
		boolean errorCode = false;
		for (ObjectError error: errors.getAllErrors()){
			if (error.getCode().equals("powerProc.missingInputCord")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
}
