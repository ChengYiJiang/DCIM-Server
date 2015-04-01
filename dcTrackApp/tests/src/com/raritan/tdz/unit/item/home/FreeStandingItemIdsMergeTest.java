package com.raritan.tdz.unit.item.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.raritan.tdz.item.request.ItemRequestValidationAspect;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;


public class FreeStandingItemIdsMergeTest extends UnitTestBase {
	
  @Autowired(required=true)
  private ItemRequestValidationAspect itemRequestValidationAspect;
  
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
  
  private Errors errors;
  
  @BeforeMethod
  public void beforeMethod() {
	  errors = getErrorObject(CircuitPDHome.class);
	  
  }
  
  @Test
  public void testGetFreeStandingItemIdForItem0(){
	  final long fsItemId = idGenerator.nextId();
	  final long cabinetId = idGenerator.nextId();
	  
	  List<Long> itemIdList = new ArrayList<Long>();
	  	  
	  itemIdList.add(cabinetId);  //cabinet Id
	  
	  jmockContext.checking(new Expectations() {{
		allowing(itemDAO).getFreeStandingItemIdForItem(with(cabinetId));will(returnValue(fsItemId));
	  	}});
	
	  List<Long> newList = itemRequestValidationAspect.getFreeStandingItemIds(itemIdList);
	
	  Assert.assertTrue(newList.size() == itemIdList.size());
	  Assert.assertTrue(newList.size() == 1);
	  Assert.assertTrue(newList.get(0).equals(fsItemId));
  }
  
  @Test
  public void testGetFreeStandingItemIdForItem1(){
	  final long fsItemId = idGenerator.nextId();
	  final long cabinetId = idGenerator.nextId();
	  
	  List<Long> itemIdList = new ArrayList<Long>();
	  	  
	  itemIdList.add(cabinetId);  //cabinet Id
	  itemIdList.add(fsItemId);  //cabinet Id
	  
	  jmockContext.checking(new Expectations() {{
		allowing(itemDAO).getFreeStandingItemIdForItem(with(cabinetId));will(returnValue(fsItemId));
		allowing(itemDAO).getFreeStandingItemIdForItem(with(fsItemId));will(returnValue(fsItemId));
	  	}});
	
	  List<Long> newList = itemRequestValidationAspect.getFreeStandingItemIds(itemIdList);
	
	  Assert.assertTrue(newList.size() == 1);
	  Assert.assertTrue(newList.get(0).equals(fsItemId));
  }
  
  @Test
  public void testGetFreeStandingItemIdForItem2(){
	  final long fsItemId = idGenerator.nextId();
	  
	  List<Long> itemIdList = new ArrayList<Long>();
	  	  
	  itemIdList.add(fsItemId);
	  
	  jmockContext.checking(new Expectations() {{
		allowing(itemDAO).getFreeStandingItemIdForItem(with(fsItemId));will(returnValue(fsItemId));
	  	}});
	
	  List<Long> newList = itemRequestValidationAspect.getFreeStandingItemIds(itemIdList);
	
	  Assert.assertTrue(newList.size() == itemIdList.size());
	  Assert.assertTrue(newList.size() == 1);
	  Assert.assertTrue(newList.get(0).equals(fsItemId));
  }

  @Test
  public void testGetFreeStandingItemIdForItem3(){
	  final long cabinetId = idGenerator.nextId();
	  
	  List<Long> itemIdList = new ArrayList<Long>();
	  	  
	  itemIdList.add(cabinetId);
	  
	  jmockContext.checking(new Expectations() {{
		allowing(itemDAO).getFreeStandingItemIdForItem(with(cabinetId));will(returnValue(null));
	  	}});
	
	  List<Long> newList = itemRequestValidationAspect.getFreeStandingItemIds(itemIdList);
	
	  Assert.assertTrue(newList.size() == itemIdList.size());
	  Assert.assertTrue(newList.size() == 1);
	  Assert.assertTrue(newList.get(0).equals(cabinetId));
  }   
}
