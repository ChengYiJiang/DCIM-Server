package com.raritan.tdz.request;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.data.RequestFactoryImpl.RequestInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class RequestManagerTest extends TestBase{
	
	RequestFactory requestFact;
	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		testUser = this.getTestAdminUser();
		requestFact = (RequestFactory)ctx.getBean("requestFact");
		
		testUser.setRequestBypass(true);
	}

    @Test
  	public final void testDevicePlannedToInstalled() throws Throwable {
    	Item item = this.createNewTestDevice("DEVICE-PLANNED-01", null);
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
		
		item = itemDAO.loadItem(item.getItemId());
		
		assertTrue(item.isStatusInstalled());	
  	}

    @Test
  	public final void testCabinetPlannedToInstalled() throws Throwable {
    	Item item = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  		
		item = itemDAO.loadItem(item.getItemId());
		
		assertTrue(item.isStatusInstalled());	
		
		for(Item x :itemDAO.getChildrenItems(item)){
			if(x.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.PASSIVE)){
				assertTrue(x.isStatusInstalled());
			}
			else{
				assertFalse(x.isStatusInstalled());
			}
		}
  	}
    
    @Test
  	public final void testFreeStandingPlannedToInstalled() throws Throwable {
    	Item item = this.createNewTestDeviceFS("FREESTANDING-PLANNED-01");
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();

		assertFalse(item.isStatusInstalled());
		assertFalse(item.getParentItem().isStatusInstalled());			
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  		
		item = itemDAO.loadItem(item.getItemId());
		
		assertTrue(item.isStatusInstalled());
		assertTrue(item.getParentItem().isStatusInstalled());			
  	}

    @Test
  	public final void testFreeStandingPlannedToOtherStatus() throws Throwable {    	
  		HashMap<Long, RequestInfo> mapStatusList = requestFact.getMapStatusToReqType();
  		List<Request> requests = new ArrayList<Request>();
		Errors errors = getErrorObject();
  		
  		for(Long statusValueCode:mapStatusList.keySet()){  			
	    	Item item = this.createNewTestDeviceFS("FREESTANDING-PLANNED-01");
	    	Request request = requestFact.createRequestForGoingToItemStatus(item, statusValueCode);
	    	
	    	System.out.println("\n\ntestFreeStandingPlannedToOtherStatus => " + mapStatusList.get(statusValueCode));
	    	
	    	requests.clear();
			requests.add(request);
			
			assertFalse(item.isStatusInstalled());
			assertFalse(item.getParentItem().isStatusInstalled());			
			
			try {
				requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
			} catch (Exception ex) {
				printError(ex);
			}
			
			item = itemDAO.loadItem(item.getItemId());
			
			assertTrue(item.getStatusLookup().getLkpValueCode().equals(statusValueCode));
			assertTrue(item.getParentItem().getStatusLookup().getLkpValueCode().equals(statusValueCode));
  		}
  	}
    
    @Test
  	public final void testFloorPduPlannedToInstalled() throws Throwable {
    	Item item = this.createNewTestFloorPDUWithPanels("FPDU-PLANNED-01");
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusInstalled());	
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  		
		item = itemDAO.loadItem(item.getItemId());
		
		assertTrue(item.isStatusInstalled());	
		
		for(Item x :itemDAO.getChildrenItems(item)){
			assertTrue(x.isStatusInstalled());
		}	  	
	}

    @Test
  	public final void testFloorOutLetPlannedToInstalled() throws Throwable {
    	Item item = this.createPowerOutlet("FLOOR-OUTLET-PLANNED-01", SystemLookup.ItemStatus.PLANNED);
    	
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  				
		item = itemDAO.loadItem(item.getItemId());
		assertTrue(item.isStatusInstalled());	
		  	
	}

    @Test
  	public final void testFloorOutLetInstalledToStorage() throws Throwable {
    	Item item = this.createPowerOutlet("FLOOR-OUTLET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);
    	Request request = requestFact.createRequestToStorage(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusPlanned());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  		
		item = itemDAO.loadItem(item.getItemId());
		assertTrue(item.isStatusPlanned());	
		  	
	}	    

    @Test
  	public final void testDataPanelPlannedToInstalled() throws Throwable {
    	Item item = this.createNewTestDataPanel("DATAPANEL-PLANNED-01", null);
    			
    	Request request = requestFact.createRequestInstalled(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
  		
		item = itemDAO.loadItem(item.getItemId());
		assertTrue(item.isStatusInstalled());	
		  	
	}	   

    @Test
  	public final void testDataPanelInstalledToStorage() throws Throwable {
    	Item item = this.createNewTestDataPanel("DATAPANEL-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);
    			
    	Request request = requestFact.createRequestToStorage(item);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertFalse(item.isStatusPlanned());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
		
		item = itemDAO.loadItem(item.getItemId());  					
		assertTrue(item.isStatusPlanned());	
		  	
	}
    
    @Test
  	public final void testDeviceInstalledConvertToVM() throws Throwable {
    	Item item = this.createNewTestDevice("DEVICE-01", SystemLookup.ItemStatus.INSTALLED);
    	List<Request> requests = requestFact.createRequestConvertToVM(item);
    	Item vmItem = null;

		assertFalse(item.isStatusStorage());	

    	for(Request request:requests){
    		if(request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.CONVERT_TO_VM)){
    			vmItem = itemDAO.loadItem(request.getItemId());	    			
    			this.addTestItem(vmItem);
    			assertFalse(vmItem.isStatusInstalled());
    			break;
    		}
    	}
		
		Errors errors = getErrorObject();
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}

    	assertTrue(vmItem != null);
    	
    	vmItem = itemDAO.loadItem(vmItem.getItemId());
    	item = itemDAO.loadItem(item.getItemId());
    	
		assertTrue(item.isStatusStorage());	
		assertTrue(vmItem.isStatusInstalled());	    		
	}	   
	
    @Test
  	public void testDevicePlannedToOtherStatus() throws Throwable {
  		HashMap<Long, RequestInfo> mapStatusList = requestFact.getMapStatusToReqType();
  		List<Request> requests = new ArrayList<Request>();
		Errors errors = getErrorObject();
  		
  		for(Long statusValueCode:mapStatusList.keySet()){  			
	    	Item item = this.createNewTestDevice("DEVICE-PLANNED-01", null);
	    	Request request = requestFact.createRequestForGoingToItemStatus(item, statusValueCode);
	    	
	    	System.out.println("\n\ntestDevicePlannedToOtherStatus => " + mapStatusList.get(statusValueCode));
	    	
	    	requests.clear();
			requests.add(request);
			
			assertTrue(item.isStatusPlanned());
			
			try {
				requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
			} catch (Exception ex) {
				printError(ex);
			}
			
			item = itemDAO.loadItem(item.getItemId());
			
			assertTrue(item.getStatusLookup().getLkpValueCode().equals(statusValueCode));
  		}
  	}

    @Test
  	public final void testChassisInstalledToPowerOff() throws Throwable {
    	Item cabinet = this.createNewTestCabinet("CAB-TEST-01", SystemLookup.ItemStatus.INSTALLED);
    	Item chassis = this.createNewTestNetworkChassis("CHASSIS-INSTALLED-01", SystemLookup.ItemStatus.INSTALLED, cabinet.getItemId());
    	
    	ItItem blade = this.createNewTestNetworkBlade("BLADE-INSTALLED-01", null);
    	blade.setStatusLookup(chassis.getStatusLookup());
    	blade.setParentItem(cabinet);
    	blade.setBladeChassis(chassis);
    	itemDAO.update(blade);
    	
    	Request request = requestFact.createRequestPowerOff(blade);
    	request = requestFact.createRequestPowerOff(chassis);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		assertTrue(chassis.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		} catch (Exception ex) {
			printError(ex);
		}
		
		chassis = itemDAO.loadItem(chassis.getItemId());
		
		assertTrue(chassis.isStatusInstalled());	
  	}
    
	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}

}
