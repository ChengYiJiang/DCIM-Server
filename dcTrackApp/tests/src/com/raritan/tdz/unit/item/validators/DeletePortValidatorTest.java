/**
 * 
 */
package com.raritan.tdz.unit.item.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.SystemLookup.RequestStage;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.validators.ItemPortValidator;
import com.raritan.tdz.port.validators.ItemPortAssociationValidator;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class DeletePortValidatorTest extends UnitTestBase {
	  private Errors errors;
	  
	  @Autowired
	  private UnitTestDatabaseIdGenerator unitTestIdGenerator;
	  
	  @Autowired
	  private ItemPortValidator<DataPort> dataPortDeleteItemPortValidator;
	  

	  @Autowired
	  private ItemDAO itemDAO;
	  
	  @Autowired
	  private ItemRequestDAO itemRequestDAO;
	  
	  @Autowired
	  private ItemRequest itemRequest;
	  
	  @Autowired
	  private DataPortDAO dataPortDAO;
	  
	  @Autowired
	  private SystemLookupInitUnitTest systemLookupInitTest;
	  
	  @BeforeMethod
	  public void beforeMethod() {
		  errors = getErrorObject(DataPort.class);
		  
	  }
	  
	  @Test(expectedExceptions={IllegalArgumentException.class})
	  public void testInvalidArgs(){
		  Item item = new Item();
		  dataPortDeleteItemPortValidator.validate(item, errors);
	  }
	  
	  @Test
	  public void testValidatePortWithCorrectAssociationToItem() throws Throwable{
		 
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  
		  //Create an admin user that will be used by the validator to make sure
		  //that the item is editable
		  Users itemAdminUser = new Users();
		  itemAdminUser.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(itemAdminUser);
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
	
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  @SuppressWarnings("serial")
			final Map<String, Object> fieldValue = new HashMap<String, Object>() {{
				put("used", (new Boolean(Boolean.FALSE)));
			}};
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
				oneOf(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
			}});
		
		  //Simulate a user who is trying to delete the port (in our case it is admin)
		  UserInfo userInfo = new UserInfo();
		  userInfo.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId, userInfo);
		  dataPortDeleteItemPortValidator.validate(targetMap, errors);
		  Assert.assertFalse(errors.hasErrors());
	  }
	  
	  @Test
	  public void testInvalidPortAssociationWithItem() throws Throwable{
		
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  
		  //Create an admin user that will be used by the validator to make sure
		  //that the item is editable
		  Users itemAdminUser = new Users();
		  itemAdminUser.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(itemAdminUser);
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(null);
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
			allowing(itemDAO).read(with(itemId)); will(returnValue(item));
			allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
			allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(getDummyRequests(itemIds)));
			allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
		}});
		
		  //Simulate a user who is trying to delete the port (in our case it is admin)
		  UserInfo userInfo = new UserInfo();
		  userInfo.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId, userInfo);
		  dataPortDeleteItemPortValidator.validate(targetMap, errors);
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testValidateDeletePortWithMemberUser() throws Throwable{
		 
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  
		  //Create an admin user that will be used by the validator to make sure
		  //that the item is editable
		  Users itemAdminUser = new Users();
		  itemAdminUser.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(itemAdminUser);
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
	
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  
		  item.addDataPort(dataPort);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(getDummyRequests(itemIds)));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
		  //Simulate a user who is trying to delete the port (in our case it is admin)
		  UserInfo userInfo = new UserInfo();
		  userInfo.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.VIEWER.getAccessLevel()).toString());
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId, userInfo);
		  dataPortDeleteItemPortValidator.validate(targetMap, errors);
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testValidateDeletePortWithNonEditableStage() throws Throwable{
		 
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  
		  //Create an admin user that will be used by the validator to make sure
		  //that the item is editable
		  Users itemAdminUser = new Users();
		  itemAdminUser.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(itemAdminUser);
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
	
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  
		  item.addDataPort(dataPort);
		  
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
			allowing(itemDAO).read(with(itemId)); will(returnValue(item));
			allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
			allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(getDummyRequests(itemIds)));
			allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
		}});
		
		  //Simulate a user who is trying to delete the port (in our case it is admin)
		  UserInfo userInfo = new UserInfo();
		  userInfo.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId, userInfo);
		  dataPortDeleteItemPortValidator.validate(targetMap, errors);
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testValidatePortWithPortUsed() throws Throwable{
		 
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  
		  //Create an admin user that will be used by the validator to make sure
		  //that the item is editable
		  Users itemAdminUser = new Users();
		  itemAdminUser.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(itemAdminUser);
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
	
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  dataPort.setUsed(true);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  @SuppressWarnings("serial")
			final Map<String, Object> fieldValue = new HashMap<String, Object>() {{
				put("used", (new Boolean(Boolean.TRUE)));
			}};
			
			final List<String> fieldList = new ArrayList<String>() {{ add("used"); }};
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
				oneOf(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
			}});
		
		  //Simulate a user who is trying to delete the port (in our case it is admin)
		  UserInfo userInfo = new UserInfo();
		  userInfo.setAccessLevelId(new Integer(UserInfo.UserAccessLevel.ADMIN.getAccessLevel()).toString());
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId, userInfo);
		  dataPortDeleteItemPortValidator.validate(targetMap, errors);
		  Assert.assertTrue(errors.hasErrors());
	  }

	  
	 private Map<Long, List<Request>> getDummyRequests(List<Long> itemIds){
		 Map<Long,List<Request>> dummyRequestMap = new HashMap<Long,List<Request>>();
		
		 for (Long itemId:itemIds){
			 
			 List<Request> requestList = new ArrayList<Request>();
			 Request request = new Request();
			 request.setItemId(itemId);
			 requestList.add(request);
			 
			 dummyRequestMap.put(itemId, requestList);
		 }
		 
		 return dummyRequestMap;
	 }
	 private Map<String,Object> getTargetMap(Long portId, Long itemId, UserInfo userInfo){
		  Map<String,Object> targetMap = new HashMap<String, Object>();
		  targetMap.put("portId",portId);
		  targetMap.put("itemId",itemId);
		  targetMap.put("UserInfo",userInfo);
		  targetMap.put("portClass", DataPort.class);
		  targetMap.put("skipValidation", new Boolean(true));
		  return targetMap;
	  }
	 
	 private RequestHistory getRequestHistory(Long itemId){
		 RequestHistory history = new RequestHistory();
		 history.setStageIdLookup(systemLookupInitTest.getLks(SystemLookup.RequestStage.REQUEST_APPROVED));
		 
		 return history;
	 }
}
