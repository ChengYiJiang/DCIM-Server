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
import com.raritan.tdz.circuit.validators.PowerConnConnectorValidator;
import com.raritan.tdz.circuit.validators.PowerConnRPDULoopValidator;
import com.raritan.tdz.circuit.validators.PowerConnRPDUOutletPhaseValidator;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class PowerConnRPDULoopValidatorTest extends UnitTestBase {
	
	@Autowired
	private PowerConnRPDULoopValidator powerConnValidator;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private PowerConnDAO powerConnDAO;
	
	@Autowired
	private UnitTestDatabaseIdGenerator idGenerator;
	
	
	
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
		Assert.assertTrue(errors.getErrorCount() == 2);
		Assert.assertTrue(errors.getAllErrors().get(0).getCode().equals("powerProc.missingSourcePort"));
		Assert.assertTrue(errors.getAllErrors().get(1).getCode().equals("powerProc.missingDestPort"));
	}
	
	@Test
	public void testRPDULoop(){
		final long srcItemId = idGenerator.nextId();
		final long srcPortId = idGenerator.nextId();
		final long dstItemId = srcItemId;
		final long dstPortId = idGenerator.nextId();
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPortSubClass = new LksData();
		srcPortSubClass.setLkpValueCode(SystemLookup.PortSubClass.INPUT_CORD);
		
		LksData dstPortSubClass = new LksData();
		dstPortSubClass.setLkpValueCode(SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
		
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
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setItem(dstItem);
		dstPowerPortDB.setPortSubClassLookup(dstPortSubClass);
		
		
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
			if (error.getCode().equals("powerProc.RackPDUConnectionLoop")) errorCode = true;
		}
		
		Assert.assertTrue(errorCode);
	}
	
	@Test
	public void testCorrectPhaseSinglePhaseSinglePhase(){
		final long srcPortId = idGenerator.nextId();
		final long dstPortId = idGenerator.nextId();
		
		
		PowerConnection powerConn = new PowerConnection();
		
		LksData srcPhaseLookup = new LksData();
		srcPhaseLookup.setLksId(50001L);
		srcPhaseLookup.setLkpValueCode(SystemLookup.PhaseIdClass.SINGLE_3WIRE);
		
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
		dstPowerPortDB.setPortId(dstPortId);
		dstPowerPortDB.setPhaseLookup(dstPhaseLookup);
		
		
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
		
		Assert.assertFalse(errors.hasErrors());
	}
}
