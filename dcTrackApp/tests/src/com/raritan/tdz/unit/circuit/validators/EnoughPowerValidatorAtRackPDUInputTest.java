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
import com.raritan.tdz.circuit.validators.EnoughPowerValidatorRackPDUInput;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
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


public class EnoughPowerValidatorAtRackPDUInputTest extends UnitTestBase {
	
  @Autowired(required=true)
  private EnoughPowerValidator enoughPowerValidatorAtRackPDUInput;
  
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
		  enoughPowerValidatorAtRackPDUInput.checkEnoughPower(30L, null, null, false, null, errors, null);
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
		  enoughPowerValidatorAtRackPDUInput.checkEnoughPower(null, null, new PowerConnection(), false, null, errors, null);
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
	
	final MeItem rackPDU = new MeItem();
	final LksData rackPDUClass = new LksData();
	rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	rackPDU.setClassLookup(rackPDUClass);
	jmockContext.checking(new Expectations() {{
		allowing(itemDAO).read(with(itemId)); will(returnValue(rackPDU));
		allowing(powerPortDAO).read(with(portId)); will(returnValue(null));
		allowing(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
	}});
	
	PowerConnection powerconnection = new PowerConnection();
	powerconnection.setDestPowerPort(new PowerPort());
	
	try {
		enoughPowerValidatorAtRackPDUInput.checkEnoughPower(portId, null, powerconnection, false, null, errors, null);
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
	
	final MeItem rackPDU = new MeItem();
	final LksData rackPDUClassLks = new LksData();
	rackPDUClassLks.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	rackPDU.setClassLookup(rackPDUClassLks);
	
	
	final PowerPort powerPort = new PowerPort();
	powerPort.setPowerFactor(0);
	powerPort.setPortId(portId);
	
	powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
	
	jmockContext.checking(new Expectations() {{
		allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
		allowing(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
	}});
	
	try {
		enoughPowerValidatorAtRackPDUInput.checkEnoughPower(30.0, 30, 5, null, portId, null, null, 0, null, errors, null);
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
	final MeItem rackPDU = new MeItem();
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
		allowing(itemDAO).read(with(itemId)); will(returnValue(rackPDU));
		allowing(itemDAO).getItem(with(itemId)); will(returnValue(rackPDU));
		allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
		//allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.ItemStatus.INSTALLED));will(returnValue(itemStatusInstalledList));
		//allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.PortSubClass.RACK_PDU_OUTPUT));will(returnValue(portSubClassLksList));
	}});
	
	try {
		enoughPowerValidatorAtRackPDUInput.checkEnoughPower(30.0, 30, 1, null, portId, null, null, 0, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().contains("The NodeId must be of a Input Cord")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }

  @Test 
  void testAmpsExceedNameplate() throws DataAccessException{
	  final long itemId = idGenerator.nextId();
	  final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  
	  final LkuData fuseLookup = getFuseLookup();
	  
	  final LksData itemSubClass = getItemSubClass();
	  final LksData breakerPortSubClass = getPortSubClass();
	  
	  final MeItem rackPDU = new MeItem();
	  final LksData rackPDUClass = new LksData();
	  rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	  rackPDU.setClassLookup(rackPDUClass);
	  rackPDU.setItemId(itemId);
	  rackPDU.setSubclassLookup(itemSubClass);
	  
	  final PowerPort inputCord = new PowerPort();
	  inputCord.setPortId(inputPortId);
	  inputCord.setAmpsNameplate(20.0);
	  inputCord.setAmpsBudget(18.0);
	  inputCord.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(breakerPortSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setPhaseLegsLookup(getOutletPhaseLegLookup(0));
	  outputPort.setAmpsNameplate(12.0);
	
	jmockContext.checking(new Expectations() {{
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
	//	allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.ItemStatus.INSTALLED)); will(returnValue(itemStatusInstalledList));
		allowing(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		allowing(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
	}});
	
	setOutletId(outputPortId);
	
	enoughPowerValidatorAtRackPDUInput.checkEnoughPower(30.0, 30, 1, null, inputPortId, null, null, 0, null, errors, null);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.AmpsExceedsNamePlate");
	System.out.println(messageSource.getMessage(errors.getAllErrors().get(0), null));
  }

private void setOutletId(final long outputPortId) {
	EnoughPowerValidatorRackPDUInput rackPduInputValidator = (EnoughPowerValidatorRackPDUInput) enoughPowerValidatorAtRackPDUInput;
	rackPduInputValidator.setReceptacleId(outputPortId);
}
  
  @Test
  public void testAmpsExceeded() throws DataAccessException{
	  final long itemId = idGenerator.nextId();
	  final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	  
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
	  inputCord.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
	  inputCord.setVoltsLookup(defaultVolts);
	  inputCord.setPhaseLookup(defaultPhase);
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(breakerPortSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setPhaseLegsLookup(getOutletPhaseLegLookup(0));
	  outputPort.setAmpsNameplate(30.0);
	  outputPort.setVoltsLookup(defaultVolts);
	  outputPort.setPhaseLookup(defaultPhase);

	jmockContext.checking(new Expectations() {{
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		//allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.ItemStatus.INSTALLED)); will(returnValue(itemStatusInstalledList));
		allowing(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		allowing(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
	}});
	
	setOutletId(outputPortId);
	
	enoughPowerValidatorAtRackPDUInput.checkEnoughPower(15.0, 30, 1, null, inputPortId, null, null, 0, null, errors, null);
	
	Assert.assertTrue(errors.hasErrors());
	Assert.assertEquals(errors.getAllErrors().get(0).getCode(),"powerProc.insufficientPower");
	System.out.println(messageSource.getMessage(errors.getAllErrors().get(0), null));
  }
  


  @Test
  public void testAmpsAccepted() throws DataAccessException {
	  final long itemId = idGenerator.nextId();
	  final long outputPortId = idGenerator.nextId();
	  final long inputPortId = idGenerator.nextId();
	 
	  
	  final LkuData fuseLookup = getFuseLookup();
	  
	  final LksData phaseLookup = getPhaseLookup();
		
		
		
		 final LksData itemSubClass = getItemSubClass();
		 final LksData portSubClass = getPortSubClass();
	  
	  final MeItem rackPDU = new MeItem();
	  final LksData rackPDUClass = new LksData();
	  rackPDUClass.setLkpValueCode(SystemLookup.Class.RACK_PDU);
	  rackPDU.setClassLookup(rackPDUClass);
	  rackPDU.setItemId(itemId);
	  rackPDU.setSubclassLookup(itemSubClass);
	  rackPDU.setPhaseLookup(phaseLookup);
	  
	  final PowerPort inputCord = new PowerPort();
	  inputCord.setPortId(inputPortId);
	  inputCord.setItem(rackPDU);
	  inputCord.setAmpsNameplate(30.0);
	  inputCord.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.INPUT_CORD));
	  inputCord.setVoltsLookup(defaultVolts);
	  inputCord.setPhaseLookup(defaultPhase);
	
	  
	  
	  
	  final PowerPort outputPort = new PowerPort();
	  outputPort.setPortSubClassLookup(portSubClass);
	  outputPort.setPortId(outputPortId);
	  outputPort.setItem(rackPDU);
	  outputPort.setAmpsNameplate(150.0);
	  outputPort.setInputCordPort(inputCord);
	  outputPort.setFuseLookup(fuseLookup);
	  outputPort.setAmpsBudget(130.0);
	  outputPort.setPhaseLegsLookup(getOutletPhaseLegLookup(3));
	  outputPort.setVoltsLookup(defaultVolts);
	  outputPort.setPhaseLookup(defaultPhase);
	  
	jmockContext.checking(new Expectations() {{
		allowing(powerPortDAO).read(with(outputPortId)); will(returnValue(outputPort));
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
		allowing(powerPortDAO).initializeAndUnproxy(with(rackPDU)); will(returnValue(rackPDU));
		allowing(powerPortDAO).read(with(inputPortId)); will(returnValue(inputCord));
	//	allowing(systemLookupFinderDAO).findByLkpValueCode(with(SystemLookup.ItemStatus.INSTALLED)); will(returnValue(itemStatusInstalledList));
		allowing(powerCircuitDAO).getPowerWattUsedSummary(with(inputPortId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
	}});
	
	setOutletId(outputPortId);
	enoughPowerValidatorAtRackPDUInput.checkEnoughPower(30.0, 30, 1, null, inputPortId, null, null, 0, null, errors, null);
	
	Assert.assertFalse(errors.hasErrors());
  }

private LksData getOutletPhaseLegLookup(Integer legs) {
	final LksData phaseLookupLks = new LksData();
	phaseLookupLks.setLkpValueCode(SystemLookup.PhaseIdClass.THREE_DELTA);
	phaseLookupLks.setAttribute(legs.toString());
	return phaseLookupLks;
}

private LkuData getFuseLookup() {
	final LkuData fuseLookup = new LkuData();
	fuseLookup.setLkuId(5L);
	return fuseLookup;
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
		powerWattUsedSummaryList.add(summary);
	}
	
	return powerWattUsedSummaryList;
}
 
  
}
