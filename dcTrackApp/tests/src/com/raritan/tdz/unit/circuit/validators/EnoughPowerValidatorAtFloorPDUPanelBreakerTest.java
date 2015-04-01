package com.raritan.tdz.unit.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;


public class EnoughPowerValidatorAtFloorPDUPanelBreakerTest extends UnitTestBase {
	
  @Autowired(required=true)
  private EnoughPowerValidator enoughPowerValidatorAtBranchCircuitBreaker;
  
  @Autowired(required=true)
  private ItemDAO itemDAO;
  
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
		  enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(30L, null, null, false, null, errors, null);
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
		  enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(null, null, new PowerConnection(), false, null, errors, null);
	  } catch (IllegalArgumentException e){
		  if (!e.getMessage().equals("Power connection and portId must be provided")){
			  Assert.fail("Did not throw a valid exception: " + e.getMessage());
		  }
		  throw e;
	  }
  }

  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerInvalidPortId() {
	final long itemId = idGenerator.nextId();
	final long portId = idGenerator.nextId();
	
	final MeItem floorPDU = new MeItem();
	final LksData floorPDUClass = new LksData();
	floorPDUClass.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	floorPDU.setClassLookup(floorPDUClass);
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(null));
	}});
	
	PowerConnection powerconnection = new PowerConnection();
	powerconnection.setDestPowerPort(new PowerPort());
	
	try {
		enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(portId, null, powerconnection, false, null, errors, null);
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
	
	final MeItem floorPDU = new MeItem();
	final LksData floorPDULks = new LksData();
	floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	floorPDU.setClassLookup(floorPDULks);
	
	final LksData portSubClassLookup = getPortSubClass();
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(portSubClassLookup);
	
	jmockContext.checking(new Expectations() {{
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
	}});
	
	try {
		enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(30.0, 30, 5, null, portId, null, null, 0, null, errors, null);
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
	
	final MeItem floorPDU = new MeItem();
	final LksData floorPDULks = new LksData();
	floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	floorPDU.setClassLookup(floorPDULks);
	
	final LksData portSubClassLookup = new LksData();
	portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(portSubClassLookup);
	
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).getItem(with(portId)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
		oneOf(powerPortDAO).initializeAndUnproxy(with(floorPDU)); will(returnValue(floorPDU));
	}});
	
	try {
		enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The NodeId must be of a Branch Circuit Breaker")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }

  @Test
  public void testAmpsExceeded() throws DataAccessException{
	  final long portId = idGenerator.nextId();
	  
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData outletPhaseLegLks = getOutletPhaseLegLookup();
	  final LksData outletSubClassLks = getOutletSubClassLookup();
	  
	  final LksData itemSubClass = getItemSubClass();
	  final LksData breakerPortSubClass = getPortSubClass();
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(25L);
	  floorPDU.setItemId(31L);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	  
	  final PowerPort breakerPort = new PowerPort();
	  breakerPort.setPortSubClassLookup(breakerPortSubClass);
	  breakerPort.setPortId(portId);
	  breakerPort.setItem(floorPDU);
	  breakerPort.setVoltsLookup(defaultVolts);
	  breakerPort.setPhaseLookup(defaultPhase);

	  final PowerPort outlet = new PowerPort();
	  outlet.setPhaseLegsLookup(outletPhaseLegLks);
	  outlet.setPortSubClassLookup(outletSubClassLks);
	  
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).loadItem(with(31L)); will(returnValue(floorPDU));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(portId),  with((Long)null), with((Long)null),  with (new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerPortDAO).initializeAndUnproxy(with(floorPDU)); will(returnValue(floorPDU));
		allowing(powerPortDAO).read(with(portId)); will(returnValue(breakerPort));
		oneOf(powerConnDAO).getSourcePort(with(portId)); will(returnValue(outlet));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, nodeInfo);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
	
	System.out.println(messageSource.getMessage(errors.getAllErrors().get(0), Locale.getDefault()));
  }
  





@Test
  public void testAmpsExceededWithConnectionBasedAPI() throws DataAccessException{
	  final long psPortId = idGenerator.nextId();
	  final long breakerPortId = idGenerator.nextId();
	  
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData portSubClassLookup = new LksData();
	  final LksData itemSubClass = getItemSubClass();
	  final LksData breakerPortSubClass = getPortSubClass();
	  final LksData outletPhaseLegLks = getOutletPhaseLegLookup();
	  final LksData outletSubClassLks = getOutletSubClassLookup();
	  
	  portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(25L);
	  floorPDU.setItemId(31L);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	
	  final PowerPort psPort = new PowerPort();
	  psPort.setPowerFactor(1);
	  psPort.setPortId(psPortId);
	  psPort.setAmpsBudget(150.0);
	  psPort.setPortSubClassLookup(portSubClassLookup);
	  
	  
	  final PowerPort breakerPort = new PowerPort();
	  breakerPort.setPortSubClassLookup(breakerPortSubClass);
	  breakerPort.setPortId(breakerPortId);
	  breakerPort.setItem(floorPDU);
	  breakerPort.setVoltsLookup(defaultVolts);
	  breakerPort.setPhaseLookup(defaultPhase);

	  final PowerPort outlet = new PowerPort();
	  outlet.setPhaseLegsLookup(outletPhaseLegLks);
	  outlet.setPortSubClassLookup(outletSubClassLks);
	  
	  
	  final PowerConnection powerConnection = new PowerConnection();
	  powerConnection.setSourcePowerPort(outlet);
	  powerConnection.setDestPowerPort(breakerPort);
		
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).loadItem(with(31L)); will(returnValue(floorPDU));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(breakerPortId),with(psPortId),with((Long)null), with (new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerPortDAO).initializeAndUnproxy(with(floorPDU)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(psPortId)); will(returnValue(psPort));
		allowing(powerPortDAO).read(with(breakerPortId)); will(returnValue(breakerPort));
		oneOf(powerConnDAO).getSourcePort(with(breakerPortId)); will(returnValue(outlet));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(psPortId,psPortId,powerConnection, false, null, errors, nodeInfo);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
  }

@Test
public void testAmpsExceededWithConnectionBasedAPIIncludeFirstNode() throws DataAccessException{
	  final long psPortId = idGenerator.nextId();
	  final long breakerPortId = idGenerator.nextId();
	  final long firstNodePortId = idGenerator.nextId();
	  
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData portSubClassLookup = new LksData();
	  final LksData itemSubClass = getItemSubClass();
	  final LksData breakerPortSubClass = getPortSubClass();
	  final LksData outletPhaseLegLks = getOutletPhaseLegLookup();
	  final LksData outletSubClassLks = getOutletSubClassLookup();
	  
	  portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(25L);
	  floorPDU.setItemId(31L);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	
	  final PowerPort psPort = new PowerPort();
	  psPort.setPowerFactor(1);
	  psPort.setPortId(psPortId);
	  psPort.setAmpsBudget(150.0);
	  psPort.setPortSubClassLookup(portSubClassLookup);
	  
	  
	  final PowerPort breakerPort = new PowerPort();
	  breakerPort.setPortSubClassLookup(breakerPortSubClass);
	  breakerPort.setPortId(breakerPortId);
	  breakerPort.setItem(floorPDU);
	  breakerPort.setVoltsLookup(defaultVolts);
	  breakerPort.setPhaseLookup(defaultPhase);
	  
	  final MeItem newFloorPDU = new MeItem();
	  final LksData newFloorPDULks = new LksData();
	  newFloorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  newFloorPDU.setClassLookup(newFloorPDULks);
	  newFloorPDU.setRatingAmps(25L);
	  newFloorPDU.setItemId(32L);
	  newFloorPDU.setPhaseLookup(phaseLookupLks);
	  newFloorPDU.setSubclassLookup(itemSubClass);
	  
	  final PowerPort newFloorPDUPort = new PowerPort();
	  newFloorPDUPort.setPortId(firstNodePortId);
	  newFloorPDUPort.setPhaseLegsLookup(outletPhaseLegLks);
	  newFloorPDUPort.setPortSubClassLookup(outletSubClassLks);
	  newFloorPDUPort.setItem(newFloorPDU);
	  newFloorPDUPort.setVoltsLookup(defaultVolts);
	  newFloorPDUPort.setPhaseLookup(defaultPhase);
	  
	  
	  final PowerConnection powerConnection = new PowerConnection();
	  powerConnection.setSourcePowerPort(newFloorPDUPort);
	  powerConnection.setDestPowerPort(breakerPort);
		
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).loadItem(with(31L)); will(returnValue(floorPDU));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(breakerPortId),with(psPortId),with((Long)null), with (new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(firstNodePortId),with((Long)null),with((Long)null), with (new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerPortDAO).initializeAndUnproxy(with(floorPDU)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(psPortId)); will(returnValue(psPort));
		allowing(powerPortDAO).read(with(breakerPortId)); will(returnValue(breakerPort));
		allowing(powerPortDAO).read(with(firstNodePortId)); will(returnValue(newFloorPDUPort));
		oneOf(powerConnDAO).getSourcePort(with(breakerPortId)); will(returnValue(newFloorPDUPort));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(psPortId,psPortId,powerConnection, true, null, errors, nodeInfo);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
}
  
  @Test
  public void testAmpsAccepted() throws DataAccessException {
	  final long breakerPortId = idGenerator.nextId();
	  final LksData outletPhaseLegLks = getOutletPhaseLegLookup();
	  final LksData outletSubClassLks = getOutletSubClassLookup();
		
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData itemSubClass = getItemSubClass();
	  final LksData portSubClass = getPortSubClass();
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(800L);
	  floorPDU.setItemId(32L);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	  
	  final PowerPort breakerPort = new PowerPort();
	  breakerPort.setPortSubClassLookup(portSubClass);
	  breakerPort.setPortId(breakerPortId);
	  breakerPort.setItem(floorPDU);
	  breakerPort.setAmpsNameplate(800.0);
	  breakerPort.setVoltsLookup(defaultVolts);
	  breakerPort.setPhaseLookup(defaultPhase);

	  final PowerPort outlet = new PowerPort();
	  outlet.setPhaseLegsLookup(outletPhaseLegLks);
	  outlet.setPortSubClassLookup(outletSubClassLks);

	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).loadItem(with(32L)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).initializeAndUnproxy(with(floorPDU)); will(returnValue(floorPDU));
		allowing(powerPortDAO).read(with(breakerPortId)); will(returnValue(breakerPort));
		oneOf(powerConnDAO).getSourcePort(with(breakerPortId)); will(returnValue(outlet));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(breakerPortId),with((Long)null),with((Long)null), with (new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtBranchCircuitBreaker.checkEnoughPower(30.0, 30, 1, null, breakerPortId, null, null, 0, null, errors, nodeInfo);
	
	Assert.assertFalse(errors.hasErrors());
  }

private LksData getOutletPhaseLegLookup() {
	final LksData phaseLookupLks = new LksData();
	phaseLookupLks.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
	phaseLookupLks.setAttribute("12");
	return phaseLookupLks;
}

private LksData getOutletSubClassLookup() {
	LksData subclass = new LksData();
	subclass.setLkpValueCode(SystemLookup.PortSubClass.WHIP_OUTLET);
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
	subclass.setLkpValueCode(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER);
	subclass.setLkpValue("BREAKER");
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

private PowerWattUsedSummary getNodeInfo(){
	PowerWattUsedSummary nodeInfo = new PowerWattUsedSummary();
	nodeInfo.setPbVolts(208.0);
	nodeInfo.setPbPhaseVolts(208.0);
	nodeInfo.setLegs("12");
	
	return nodeInfo;
}

}
