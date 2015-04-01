package com.raritan.tdz.unit.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;


public class EnoughPowerValidatorAtRackPDUFuseTest extends UnitTestBase {
	
  @Autowired(required=true)
  private EnoughPowerValidator enoughPowerValidatorAtRackPDUFuse;
  
  @Autowired(required=true)
  private ItemDAO itemDAO;
  
  @Autowired(required=true)
  private SystemLookupInitUnitTest systemLookupUnitTest;
  
  @Autowired
  private UnitTestDatabaseIdGenerator idGenerator;
  
  @Autowired
  PowerPortDAO powerPortDAO;
  
  @Autowired
  PowerCircuitDAO powerCircuitDAO;
  
  @Autowired
  PowerConnDAO powerConnDAO;
  
  @Autowired
  MessageSource messageSource;
  

  @Autowired
  private SystemLookupFinderDAO systemLookupFinderDAO;

  private Errors errors;
  
  private LksData defaultVolts;
  private LksData defaultPhase;
  
  @BeforeMethod
  public void beforeMethod() {
	  errors = getErrorObject(CircuitPDHome.class);
	  defaultVolts = systemLookupFinderDAO.findByLkpValueAndType("208", "VOLTS").get(0);
	  defaultPhase = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.PhaseIdClass.THREE_WYE).get(0);
  }
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerNullNodeId() {
	  try{
		  enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(30L, null, null, false, null, errors, null);
	  } catch (IllegalArgumentException e){
		  if (!e.getMessage().equals("Power connection and portId must be provided")){
			  Assert.fail("Did not throw a valid exception: " + e.getMessage());
		  }
		  throw e;
	  }
  }
  

  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerNullPortId() {
	  try {
		  enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(null, null, new PowerConnection(), false, null, errors, null);
	  } catch (IllegalArgumentException e){
		  if (!e.getMessage().equals("Power connection and portId must be provided")){
			  Assert.fail("Did not throw a valid exception: " + e.getMessage());
		  }
		  throw e;
	  }
  }

  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerInvalidPortId() throws Throwable {
	final long itemId = idGenerator.nextId();
	final long portId = idGenerator.nextId();
	
	final ItItem rackPDU = new ItItem();
	final LksData rackPDUClass = new LksData();
	rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	rackPDU.setClassLookup(rackPDUClass);
	PowerConnection powerconnection = new PowerConnection();
	powerconnection.setDestPowerPort(new PowerPort());
	
	try {
		enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(portId, null, powerconnection, false, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().equals("Power connection and portId must be provided")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }
  
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerInvalidPowerFactor() {
	final long portId = idGenerator.nextId();
	
	final ItItem rackPDU = new ItItem();
	final LksData rackPDUClassLks = new LksData();
	rackPDUClassLks.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	rackPDU.setClassLookup(rackPDUClassLks);
	
	final LksData portSubClassLookup = getPortSubClass();
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(portSubClassLookup);
	
	jmockContext.checking(new Expectations() {{
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
	}});
	
	try {
		enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(30.0, 30, 5, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The Power Factor must be greater than 0 and less than or equal to 1:")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testInvalidCircuitNodeId(){
	final long itemId = idGenerator.nextId();
	final long portId = idGenerator.nextId();
	
	final ItItem rackPDU = new ItItem();
	final LksData rackPDUClassLks = new LksData();
	rackPDUClassLks.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	rackPDU.setClassLookup(rackPDUClassLks);
	
	final LksData portSubClassLookup = new LksData();
	portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(portSubClassLookup);
	
	jmockContext.checking(new Expectations() {{
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
	}});
	
	try {
		enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The NodeId must be of a Rack PDU Output")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }

  @Test
  public void testAmpsExceeded() throws DataAccessException{
	  final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  final long itemId = idGenerator.nextId();
	
	  final LkuData fuseLookup = getFuseLookup();
	  final LksData phaseLookup = getPhaseLookup();
	  
	  final LksData itemSubClass = getItemSubClass();
	  final LksData breakerPortSubClass = getPortSubClass();
	  
	 
	  
	  final MeItem rackPDU = new MeItem();
	  final LksData rackPDUClass = new LksData();
	  rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	  rackPDU.setClassLookup(rackPDUClass);
	  rackPDU.setItemId(itemId);
	  rackPDU.setSubclassLookup(itemSubClass);
	  rackPDU.setPhaseLookup(phaseLookup);
	  
	  final PowerPort inputCord = new PowerPort();
	  inputCord.setPortId(inputPortId);
	  inputCord.setAmpsNameplate(2.0);
	  inputCord.setAmpsBudget(18.0);
	  inputCord.setItem(rackPDU);
	  inputCord.setVoltsLookup(defaultVolts);
	  inputCord.setPhaseLookup(defaultPhase);
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(breakerPortSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setAmpsNameplate(30.0);
	  outputPort.setAmpsBudget(2.0);
	  outputPort.setVoltsLookup(defaultVolts);
	  outputPort.setPhaseLookup(defaultPhase);
	
	jmockContext.checking(new Expectations() {{
		oneOf(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with((Long)null),with(fuseLookup.getLkuId()), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
	}});
	
	enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(5.0, 30, 1, null, outputPortId, null, null, 0, null, errors, null);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
	System.out.println(messageSource.getMessage(errors.getAllErrors().get(0), null));
  }

  @Test
  public void testAmpsExceededWithConnectionBasedAPI() throws DataAccessException{
	 final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  final long itemId = idGenerator.nextId();
	  final long psPortId = idGenerator.nextId();
	  
	  final LkuData fuseLookup = getFuseLookup();
	  final LksData phaseLookup = getPhaseLookup();
	  
	  final LksData portSubClassLookup = new LksData();
	  final LksData itemSubClass = getItemSubClass();
	  final LksData outputPortSubClass = getPortSubClass();
	  final LksData lksVolts = systemLookupFinderDAO.findByLkpValueAndType("120", "VOLTS").get(0);
	  portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	  
	  final MeItem rackPDU = new MeItem();
	  final LksData rackPDUClass = new LksData();
	  rackPDUClass.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  rackPDU.setClassLookup(rackPDUClass);
	  rackPDU.setItemId(itemId);
	  rackPDU.setSubclassLookup(itemSubClass);
	  rackPDU.setPhaseLookup(phaseLookup);
	
	  final PowerPort psPort = new PowerPort();
	  psPort.setPowerFactor(1);
	  psPort.setPortId(psPortId);
	  psPort.setAmpsBudget(5.0);
	  psPort.setPortSubClassLookup(portSubClassLookup);
	  
	  final PowerPort inputCord = new PowerPort();
	  inputCord.setPortId(inputPortId);
	  inputCord.setAmpsNameplate(2.0);
	  inputCord.setAmpsBudget(18.0);
	  inputCord.setVoltsLookup(lksVolts);
	  inputCord.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE));
	  inputCord.setItem(rackPDU);
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(outputPortSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setAmpsNameplate(30.0);
	  outputPort.setAmpsBudget(12.0);
	  outputPort.setVoltsLookup(lksVolts);
	  outputPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE));

	  
	  final PowerConnection powerConnection = new PowerConnection();
	  powerConnection.setDestPowerPort(outputPort);
		
	jmockContext.checking(new Expectations() {{
		oneOf(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with(psPortId),with(fuseLookup.getLkuId()), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerPortDAO).read(with(psPortId)); will(returnValue(psPort));
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
	}});
	
	enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(psPortId,psPortId,powerConnection, false, null, errors, null);
	
	Assert.assertFalse(errors.hasErrors());
  }
  
  @Test
  public void testAmpsAccepted() throws DataAccessException {
	  final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  final long itemId = idGenerator.nextId();
	  final LkuData fuseLookup = getFuseLookup();
	  final LksData phaseLookup = getPhaseLookup();
	  final LksData itemSubClass = getItemSubClass();
	  final LksData portSubClass = getPortSubClass();
	  final MeItem rackPDU = new MeItem();
	  final LksData rackPDUClass = new LksData();
	  final LksData lksVolts = systemLookupFinderDAO.findByLkpValueAndType("120", "VOLTS").get(0);
	  
	  rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	  rackPDU.setClassLookup(rackPDUClass);
	  rackPDU.setItemId(itemId);
	  rackPDU.setSubclassLookup(itemSubClass);
	  rackPDU.setPhaseLookup(phaseLookup);
	  
	  final PowerPort inputCord = new PowerPort();
	  inputCord.setPortId(inputPortId);
	  inputCord.setAmpsNameplate(2.0);
	  inputCord.setAmpsBudget(18.0);
	  inputCord.setItem(rackPDU);
	  inputCord.setVoltsLookup(lksVolts);
	  inputCord.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.SINGLE_2WIRE));
	  
	  
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(portSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setAmpsNameplate(150.0);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setAmpsNameplate(300.0);
	  outputPort.setAmpsBudget(150.0);

	jmockContext.checking(new Expectations() {{
		 oneOf(itemDAO).loadItem(with(itemId));will(returnValue(rackPDU));
		oneOf(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
		oneOf(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with((Long)null),with(fuseLookup.getLkuId()), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
	}});
	
	enoughPowerValidatorAtRackPDUFuse.checkEnoughPower(30.0, 30, 1, null, outputPortId, null, null, 0, null, errors, null);
	
	Assert.assertFalse(errors.hasErrors());
  }

private LksData getOutletPhaseLegLookup() {
	final LksData phaseLookupLks = new LksData();
	phaseLookupLks.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
	phaseLookupLks.setAttribute("12");
	return phaseLookupLks;
}

private LkuData getFuseLookup() {
	final LkuData fuseLookup = new LkuData();
	fuseLookup.setLkuId(5L);
	return fuseLookup;
}

private LksData getOutletSubClassLookup() {
	LksData subclass = new LksData();
	subclass.setLkpValueCode(SystemLookup.PortSubClass.BUSWAY_OUTLET);
	subclass.setLkpValue("Outlet");
	return subclass;
}


private LksData getPhaseLookup() {
	final LksData phaseLookupLks = new LksData();
	phaseLookupLks.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
	return phaseLookupLks;
}

private LksData getItemStatusInstalled() {
	final LksData itemStatusInstalled = new LksData();
	itemStatusInstalled.setLksId(303L);
	return itemStatusInstalled;
}

private LksData getItemSubClass() {
	LksData subclass = new LksData();
	subclass.setLkpValueCode(SystemLookup.SubClass.LOCAL);
	subclass.setLkpValue("LOCAL");
	return subclass;
}

private LksData getPortSubClass() {
	LksData subclass = new LksData();
	subclass.setLkpValueCode(SystemLookup.PortSubClass.RACK_PDU_OUTPUT);
	subclass.setLkpValue("Rack PDU Output");
	return subclass;
}

private List<PowerWattUsedSummary> getRandomPowerWattUsedSummary(int maxCount){
	List<PowerWattUsedSummary> powerWattUsedSummaryList = new ArrayList<PowerWattUsedSummary>();
	
	Random random = new Random();
	for (int i = 0; i < maxCount; i++){
		PowerWattUsedSummary summary = new PowerWattUsedSummary();
		summary.setCurrentMax(random.nextDouble());
		summary.setCurrentRated(random.nextDouble());
		summary.setLegs("12");
		summary.setPbVolts(random.nextDouble());
		summary.setVaMax((random.nextDouble()));
		summary.setVaRated((random.nextDouble()));
		summary.setWattMax(random.nextDouble());
		summary.setWattRated(random.nextDouble());
		summary.setPbVolts(208.0);
		summary.setPbPhaseVolts(208.0);
		powerWattUsedSummaryList.add(summary);
	}
	
	return powerWattUsedSummaryList;
}

}
