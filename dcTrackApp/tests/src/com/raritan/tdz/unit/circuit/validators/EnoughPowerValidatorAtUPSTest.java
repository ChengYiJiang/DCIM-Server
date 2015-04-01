package com.raritan.tdz.unit.circuit.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.PowerBankInfo;
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


public class EnoughPowerValidatorAtUPSTest extends UnitTestBase {
	
  @Autowired(required=true)
  private EnoughPowerValidator enoughPowerValidatorAtUPS;
  
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
		  enoughPowerValidatorAtUPS.checkEnoughPower(30L, null, null, false, null, errors, null);
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
		  enoughPowerValidatorAtUPS.checkEnoughPower(null, null, new PowerConnection(), false, null, errors, null);
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
	
	final MeItem upsBank = new MeItem();
	final LksData upsBankClass = new LksData();
	upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
	upsBank.setClassLookup(upsBankClass);
	jmockContext.checking(new Expectations() {{
		allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
		allowing(powerPortDAO).read(with(portId)); will(returnValue(null));
	}});
	try {
		enoughPowerValidatorAtUPS.checkEnoughPower(portId, null, new PowerConnection(), false, null, errors, null);
	} catch (IllegalArgumentException e){
		if (!e.getMessage().equals("Power connection and portId must be provided")){
			Assert.fail("Did not throw a valid exception: " + e.getMessage());
		}
		throw e;
	}
  }
  
  @Test
  public void testCheckEnoughPowerNullPowerBankInfo(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		final LksData itemStatusInstalled = new LksData();
		itemStatusInstalled.setLksId(303L);
		
		final PowerBankInfo powerBankInfo = null;
		
//		final PowerBankInfo powerBankInfo = new PowerBankInfo();
//		powerBankInfo.bank = "UPSA";
//		powerBankInfo.rating_kva = 100L;
//		powerBankInfo.rating_kw = 100L;
//		powerBankInfo.rating_v = 10L;
//		powerBankInfo.redundancy = "N";
//		powerBankInfo.units = 3L;
//		powerBankInfo.ups_bank_item_id = 3004L;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			//allowing(systemLookupFinderDAO).findByLkpValueCode(with(installedStatusValueCode)); will(returnValue(itemStatusInstalledList));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
	  
  }
  
  @Test
  public void testCheckEnoughPowerNullPowerBankUnitInfo(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
		
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		final LksData itemStatusInstalled = new LksData();
		itemStatusInstalled.setLksId(303L);
		
		
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.rating_kva = 100L;
		powerBankInfo.rating_kw = 100L;
		powerBankInfo.rating_v = 10L;
		powerBankInfo.redundancy = "N";
//		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			//allowing(systemLookupFinderDAO).findByLkpValueCode(with(installedStatusValueCode)); will(returnValue(itemStatusInstalledList));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
	  
  }
  
  @Test
  public void testCheckEnoughPowerNullPowerBankRedundancyInfo(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
		
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		final LksData itemStatusInstalled = new LksData();
		itemStatusInstalled.setLksId(303L);
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.rating_kva = 100L;
		powerBankInfo.rating_kw = 100L;
		powerBankInfo.rating_v = 10L;
//		powerBankInfo.redundancy = "N";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			//allowing(systemLookupFinderDAO).findByLkpValueCode(with(installedStatusValueCode)); will(returnValue(itemStatusInstalledList));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
	  
  }
  
  @Test
  public void testCheckEnoughPowerInvalidPowerBankRedundancyInfo(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		final LksData itemStatusInstalled = new LksData();
		itemStatusInstalled.setLksId(303L);
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.rating_kva = 100L;
		powerBankInfo.rating_kw = 100L;
		powerBankInfo.rating_v = 10L;
		powerBankInfo.redundancy = "N+";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			//allowing(systemLookupFinderDAO).findByLkpValueCode(with(installedStatusValueCode)); will(returnValue(itemStatusInstalledList));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
	  
  }
  
  @Test
  public void testCheckEnoughPowerNullPowerBankRatingInfo(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
	  	
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		final LksData itemStatusInstalled = new LksData();
		itemStatusInstalled.setLksId(303L);
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.redundancy = "N";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			//allowing(systemLookupFinderDAO).findByLkpValueCode(with(installedStatusValueCode)); will(returnValue(itemStatusInstalledList));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
	  
  }
  
  @Test
  public void testUPSCapacityExceedsWithNRedundancyKvA(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
	
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.redundancy = "N";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		powerBankInfo.rating_kva = 100L;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300000000, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
  }
  
  @Test
  public void testUPSCapacityExceedsWithNPlusOneRedundancyKvA(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
	
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.redundancy = "N+1";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		powerBankInfo.rating_kva = 100L;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
	
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300000000, 1.0, null, portId, null, null, 0, null, errors, null );
		
		
		org.testng.Assert.assertTrue(errors.hasErrors());
  }
  
  @Test
  public void testUPSCapacityExceedsWithNRedundancyWatts(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
	
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.redundancy = "N";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		powerBankInfo.rating_kw = 100L;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300000000, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
  }
  
  @Test
  public void testUPSCapacityExceedsWithNPlusOneRedundancyWatts(){
		final long itemId = idGenerator.nextId();
		final long portId = idGenerator.nextId();
	
		final MeItem upsBank = new MeItem();
		final LksData upsBankClass = new LksData();
		final PowerPort powerPort = new PowerPort();
		powerPort.setPortId(portId);
		powerPort.setItem(upsBank);
		powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER));
		powerPort.setVoltsLookup(defaultVolts);
		powerPort.setPhaseLookup(systemLookupUnitTest.getLks(SystemLookup.PhaseIdClass.THREE_DELTA));
		
		upsBankClass.setLkpValueCode(SystemLookup.Class.UPS_BANK);
		upsBank.setClassLookup(upsBankClass);
		upsBank.setItemId(itemId);
		
		final PowerBankInfo powerBankInfo = new PowerBankInfo();
		powerBankInfo.bank = "UPSA";
		powerBankInfo.redundancy = "N+1";
		powerBankInfo.units = 3L;
		powerBankInfo.ups_bank_item_id = itemId;
		powerBankInfo.rating_kw = 100L;
		
		Errors errors = getErrorObject(EnoughPowerValidator.class);
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).read(with(itemId)); will(returnValue(upsBank));
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(upsBank));
			allowing(powerPortDAO).read(with(portId)); will(returnValue(powerPort));
			allowing(itemDAO).initializeAndUnproxy(with(upsBank));will(returnValue(upsBank));
			allowing(powerCircuitDAO).getPowerWattUsedSummary(with(portId),with((Long)null),with((Long)null), with(new Long(-1)), with(false)); will(returnValue(getRandomPowerWattUsedSummary(10)));
			allowing(powerCircuitDAO).getPowerBankInfo(with(itemId)); will(returnValue(powerBankInfo));
		}});
		
		enoughPowerValidatorAtUPS.checkEnoughPower(300.0, 300000000, 1.0, null, portId, null, null, 0, null, errors, null );
		
		org.testng.Assert.assertTrue(errors.hasErrors());
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
