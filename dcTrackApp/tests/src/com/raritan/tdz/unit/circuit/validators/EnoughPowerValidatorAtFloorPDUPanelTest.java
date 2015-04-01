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
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;


public class EnoughPowerValidatorAtFloorPDUPanelTest extends UnitTestBase {
	
  @Autowired(required=true)
  private EnoughPowerValidator enoughPowerValidatorAtFloorPDUPanel;
  
  @Autowired(required=true)
  private ItemDAO itemDAO;
  
  @Autowired
  PowerPortDAO powerPortDAO;
  
  @Autowired
  PowerCircuitDAO powerCircuitDAO;
  
  @Autowired(required=true)
  private SystemLookupInitUnitTest systemLookupUnitTest;
  
  @Autowired
  private UnitTestDatabaseIdGenerator idGenerator;
  
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
		  enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(30L, null, null, false, null, errors, null);
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
		  enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(null, null, new PowerConnection(), false, null, errors, null);
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
	try {
		enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(portId, null, new PowerConnection(), false, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().equals("Power connection and portId must be provided")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }
  
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testCheckEnoughPowerInvalidPowerFactor() {
	final long itemId = idGenerator.nextId();
	final long portId = idGenerator.nextId();
	
	final MeItem floorPDU = new MeItem();
	final LksData floorPDULks = new LksData();
	floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	floorPDU.setClassLookup(floorPDULks);

	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.PANEL_BREAKER));
	powerPort.setItem(floorPDU);
	
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
	}});
	
	try {
		enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(30.0, 30, 5, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The Power Factor must be greater than 0 and less than or equal to 1:")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testInvalidCircuitNodeId(){
	final  long itemId = idGenerator.nextId();
	final long portId = idGenerator.nextId();
	final MeItem floorPDU = new MeItem();
	final LksData floorPDULks = new LksData();
	floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	floorPDU.setClassLookup(floorPDULks);
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.POWER_SUPPLY));
	powerPort.setItem(floorPDU);
	
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).getItem(with(itemId)); will(returnValue(floorPDU));
		oneOf(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
	}});
	
	try {
		enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The NodeId must be of a Panel Breaker")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }

  @Test
  public void testAmpsExceeded(){
	  final long itemId = idGenerator.nextId();
	  final long portId = idGenerator.nextId();
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData itemSubClass = getItemSubClass();
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(25L);
	  floorPDU.setItemId(itemId);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);

	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.PANEL_BREAKER));
	powerPort.setItem(floorPDU);
	powerPort.setVoltsLookup(defaultVolts);
	powerPort.setPhaseLookup(defaultPhase);
	
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).getItem(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).initializeAndUnproxy(with(floorPDU));will(returnValue(floorPDU));
		allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
		allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null),  with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, nodeInfo);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
	
	System.out.println(messageSource.getMessage(errors.getAllErrors().get(0), null));
  }
  


@Test
  public void testAmpsExceededWithConnectionBasedAPI(){
	  final long itemId = idGenerator.nextId();
	  final long psPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData portSubClassLookup = new LksData();
	  final LksData itemSubClass = getItemSubClass();
	  portSubClassLookup.setLkpValueCode(SystemLookup.PortSubClass.POWER_SUPPLY);
	  
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(25L);
	  floorPDU.setItemId(itemId);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	
	  final PowerPort psPort = new PowerPort();
	  psPort.setPowerFactor(1);
	  psPort.setPortId(psPortId);
	  psPort.setAmpsBudget(150.0);
	  psPort.setPortSubClassLookup(portSubClassLookup);
	  
	  
	  final PowerPort pduInputBreakerPort = new PowerPort();
	  pduInputBreakerPort.setPortId(inputPortId);
	  pduInputBreakerPort.setItem(floorPDU);
	  pduInputBreakerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.PANEL_BREAKER));
	  pduInputBreakerPort.setVoltsLookup(defaultVolts);
	  pduInputBreakerPort.setPhaseLookup(defaultPhase);
	  
	  final PowerConnection powerConnection = new PowerConnection();
	  powerConnection.setSourcePowerPort(psPort);
	  powerConnection.setDestPowerPort(pduInputBreakerPort);
		
	jmockContext.checking(new Expectations() {{
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).getItem(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).initializeAndUnproxy(with(floorPDU));will(returnValue(floorPDU));
		//allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.ItemStatus.INSTALLED)); will(returnValue(itemStatusInstalledList));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with(psPortId),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		oneOf(powerPortDAO).read(with(psPortId)); will(returnValue(psPort));
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(pduInputBreakerPort));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(psPortId,psPortId,powerConnection, false, null, errors, nodeInfo);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
  }
  
  @Test
  public void testAmpsAccepted() {
	  final long itemId = idGenerator.nextId();
	  final long portId = idGenerator.nextId();
	 
	  final LksData phaseLookupLks = getPhaseLookup();
	  final LksData itemSubClass = getItemSubClass();
	  
	  
	  final MeItem floorPDU = new MeItem();
	  final LksData floorPDULks = new LksData();
	  floorPDULks.setLkpValueCode(SystemLookup.Class.FLOOR_PDU);
	  floorPDU.setClassLookup(floorPDULks);
	  floorPDU.setRatingAmps(800L);
	  floorPDU.setItemId(itemId);
	  floorPDU.setPhaseLookup(phaseLookupLks);
	  floorPDU.setSubclassLookup(itemSubClass);
	  
	  final PowerPort panelBreakerPort = new PowerPort();
	  panelBreakerPort.setPortId(portId);
	  panelBreakerPort.setItem(floorPDU);
	  panelBreakerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.PANEL_BREAKER));
	  panelBreakerPort.setVoltsLookup(defaultVolts);
	  panelBreakerPort.setPhaseLookup(defaultPhase);

	jmockContext.checking(new Expectations() {{
		allowing(powerPortDAO).read(with(portId));will(returnValue(panelBreakerPort));
		oneOf(itemDAO).read(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).getItem(with(itemId)); will(returnValue(floorPDU));
		oneOf(itemDAO).initializeAndUnproxy(with(floorPDU));will(returnValue(floorPDU));
		oneOf(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
	}});
	
	PowerWattUsedSummary nodeInfo = this.getNodeInfo();
	
	enoughPowerValidatorAtFloorPDUPanel.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, nodeInfo);
	
	Assert.assertFalse(errors.hasErrors());
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
