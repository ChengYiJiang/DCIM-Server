/**
 * 
 */
package com.raritan.tdz.unit.item.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPAddress;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class DeleteItemPortAPITest extends UnitTestBase {
	 @Autowired
	 private ItemHome itemHome;
	 
	 @Autowired
	 UnitTestDatabaseIdGenerator unitTestIdGenerator;
	 
	 @Autowired
	 ItemDAO itemDAO;
	 
	 @Autowired
	 DataPortDAO dataPortDAO;
	 
	 @Autowired
	 ItemRequestDAO itemRequestDAO;
	 
	  @Autowired
	  private ItemRequest itemRequest;
	  
	  @Autowired
	  private SystemLookupInitUnitTest systemLookupInitTest;
	  
	  private boolean skipValidation = true;
	  
	  private void mockGetFieldsValueUsed(final boolean value) {
		  @SuppressWarnings("serial")
		  final Map<String, Object> fieldValue = new HashMap<String, Object>() {{
			  put("used", (new Boolean(value)));
		  }};
			
		  jmockContext.checking(new Expectations() {{
			  oneOf(dataPortDAO).getFieldsValue( with(any(Class.class)), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});

		}
	 
	 @Test
	 public void testDeleteItemPortSuccessful() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
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
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
				
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }
	 
	 
	 @Test
	 public void testDeleteItemPortWithBadPermission() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.VIEWER);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.VIEWER));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
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
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			try{
				itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
			} catch (BusinessValidationException be){
				Assert.assertTrue(be.getErrors().containsKey("ItemValidator.itemEditability.noPermission"));
			}
	 }
	 
	 @Test
	 public void testDeleteItemPortWithBadItemId() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.VIEWER);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.VIEWER));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(null));
				allowing(itemDAO).read(with(itemId)); will(returnValue(null));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			try{
				itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
			} catch (BusinessValidationException be){
				Assert.assertTrue(be.getErrors().containsKey("PortValidator.itemNotFound"));
			}
	 }
	 
	 @Test
	 public void testDeleteItemPortWithBadPortId() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.VIEWER);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.VIEWER));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
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
				allowing(dataPortDAO).read(with(portId));will(returnValue(null));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(null));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			try{
				itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
			} catch (BusinessValidationException be){
				Assert.assertTrue(be.getErrors().containsKey("PortValidator.portNotFound"));
			}
	 }
	 
	 @Test
	 public void testDeleteItemPortWithUsedPort() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
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
		  mockGetFieldsValueUsed(true);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			try {
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
			} catch (BusinessValidationException be){
				Assert.assertTrue(be.getErrors().containsKey("PortValidator.connectedDataPortCannotDelete"));
			}
	 }
	 
	 @Test
	 public void testDeleteItemPortWithInstalledState() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setStatusLookup(systemLookupInitTest.getLks(SystemLookup.ItemStatus.INSTALLED));
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);

			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithArchivedState() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setStatusLookup(systemLookupInitTest.getLks(SystemLookup.ItemStatus.ARCHIVED));
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(1L);
		  classLookup.setLkpValueCode(SystemLookup.Class.DEVICE);
		  item.setClassLookup(classLookup);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);

			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }
	 
	 
	 @Test
	 public void testDeleteItemPortWithPlannedState() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setStatusLookup(systemLookupInitTest.getLks(SystemLookup.ItemStatus.PLANNED));
		  
		  
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
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithRequestPendingState() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setStatusLookup(systemLookupInitTest.getLks(SystemLookup.ItemStatus.PLANNED));
		  
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

			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(getDummyRequests(itemIds)));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			try {
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
			} catch (BusinessValidationException be){
				Assert.assertTrue(be.getErrors().containsKey("ItemValidator.itemEditability.incorrectStage"));
			}
	 }
	 
	 @Test
	 public void testDeleteItemPortWithIPAddressNoPIQIdRPDU() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(5L);
		  classLookup.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		  item.setClassLookup(classLookup);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};

		  mockGetFieldsValueUsed(false);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test(expectedExceptions={BusinessValidationException.class})
	 public void testDeleteItemPortWithIPAddressAndPIQIdRPDU() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setPiqId(5);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(5L);
		  classLookup.setLkpValueCode(SystemLookup.Class.RACK_PDU);
		  item.setClassLookup(classLookup);
		  
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);

			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithIPAddressAndPIQIdSkipValidationRPDU() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(5L);
		  classLookup.setLkpValueCode(SystemLookup.Class.RACK_PDU);
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
		  mockGetFieldsValueUsed(false);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }

	 @Test
	 public void testDeleteItemPortWithIPAddressNoPIQIdProbe() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(7L);
		  classLookup.setLkpValueCode(SystemLookup.Class.PROBE);
		  item.setClassLookup(classLookup);
		  
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test(expectedExceptions={BusinessValidationException.class})
	 public void testDeleteItemPortWithIPAddressAndPIQIdProbe() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setPiqId(5);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(7L);
		  classLookup.setLkpValueCode(SystemLookup.Class.PROBE);
		  item.setClassLookup(classLookup);
		  
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithIPAddressAndPIQIdSkipValidationProbe() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(7L);
		  classLookup.setLkpValueCode(SystemLookup.Class.PROBE);
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
		  mockGetFieldsValueUsed(false);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
	 }
	 @Test
	 public void testDeleteItemPortWithIPAddressNoPIQIdNetwork() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(2L);
		  classLookup.setLkpValueCode(SystemLookup.Class.NETWORK);
		  item.setClassLookup(classLookup);
		  
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithIPAddressAndPIQIdNetwork() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  item.setPiqId(5);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(2L);
		  classLookup.setLkpValueCode(SystemLookup.Class.NETWORK);
		  item.setClassLookup(classLookup);
		  
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  dataPort.setPortSubClassLookup(systemLookupInitTest.getLks(SystemLookup.PortSubClass.LOGICAL));
		  
		  Set<IPAddress> ipAddresses = new HashSet<IPAddress>();
		  IPAddress address1 = new IPAddress();
		  address1.setIpAddress("10.0.0.1");
		  IPAddress address2 = new IPAddress();
		  address2.setIpAddress("10.0.0.2");
		  ipAddresses.add(address1);
		  ipAddresses.add(address2);
		  
		  dataPort.setIpAddresses(ipAddresses);
		  
		  item.addDataPort(dataPort);
		  final List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		  final List<Long> stages = new ArrayList<Long>(){{
			  add(SystemLookup.RequestStage.REQUEST_APPROVED);
			  add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			  add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		  }};

		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, false, userInfo);
	 }
	 
	 @Test
	 public void testDeleteItemPortWithIPAddressAndPIQIdSkipValidationNetwork() throws Throwable{
		  final Long portId = unitTestIdGenerator.nextId();
		  final Long itemId = unitTestIdGenerator.nextId();
		  UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		  itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		  item.setItemServiceDetails(itemServiceDetails);
		  
		  final ModelDetails model = new ModelDetails();
		  model.setModelDetailId(225L);
		  model.setMounting("Rackable");
		  model.setFormFactor("Fixed");
		  item.setModel(model);
		  
		  final LksData classLookup = new LksData();
		  classLookup.setLksId(2L);
		  classLookup.setLkpValueCode(SystemLookup.Class.NETWORK);
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
		  mockGetFieldsValueUsed(false);
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(itemDAO).read(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).loadEvictedPort(with(portId));will(returnValue(dataPort));
				allowing(dataPortDAO).deletePortIPAddressAndTeaming(with(any(Long.class)));
				allowing(dataPortDAO).delete(with(dataPort));
				allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
				allowing(itemRequestDAO).getCurrentHistory(itemId);will(returnValue(getRequestHistory(itemId)));
			}});
		
			itemHome.deleteItemDataPortExtAPI(itemId, portId, skipValidation, userInfo);
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
	 
	 private RequestHistory getRequestHistory(Long itemId){
		 RequestHistory history = new RequestHistory();
		 history.setStageIdLookup(systemLookupInitTest.getLks(SystemLookup.RequestStage.REQUEST_APPROVED));
		 
		 return history;
	 }
	 
}
