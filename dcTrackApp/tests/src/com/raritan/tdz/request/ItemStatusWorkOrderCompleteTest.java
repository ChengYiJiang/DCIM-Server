package com.raritan.tdz.request;

import java.util.List;

import org.testng.annotations.Test;

import com.raritan.tdz.request.home.ItemStatusWorkOrderComplete;
import com.raritan.tdz.tests.TestBase;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;

public class ItemStatusWorkOrderCompleteTest extends TestBase {
	
		ItemStatusWorkOrderComplete itemStatusWOC;
		RequestFactory requestFact;
		UserInfo testUser;
		
		@BeforeMethod
		public void setUp() throws Throwable {
			super.setUp();
			itemStatusWOC = (ItemStatusWorkOrderComplete)ctx.getBean("itemStatusWorkOrderComplete");
			testUser = this.getTestAdminUser();
			requestFact = (RequestFactory)ctx.getBean("requestFact");
		}

	    @Test
	  	public final void testDevicePlannedToInstalled() throws Throwable {
	    	Item item = this.createNewTestDevice("DEVICE-PLANNED-01", null);
	    	Request request = requestFact.createRequestInstalled(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.getStatusLookup().getLkpValueCode().equals(SystemLookup.ItemStatus.INSTALLED));	
	  	}
	    
	    @Test
	  	public final void testCabinetPlannedToInstalled() throws Throwable {
	    	Item item = this.createNewTestCabinetWithItems("CABINET-PLANNED-01", null);
	    	Request request = requestFact.createRequestInstalled(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
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
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusInstalled());
			assertTrue(item.getParentItem().isStatusInstalled());			
	  	}

	    @Test
	  	public final void testFloorPduPlannedToInstalled() throws Throwable {
	    	Item item = this.createNewTestFloorPDUWithPanels("FPDU-PLANNED-01");
	    	Request request = requestFact.createRequestInstalled(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusInstalled());	
			
			for(Item x :itemDAO.getChildrenItems(item)){
				assertTrue(x.isStatusInstalled());
			}	  	
		}

	    @Test
	  	public final void testFloorOutLetPlannedToInstalled() throws Throwable {
	    	Item item = this.createPowerOutlet("FLOOR-OUTLET-PLANNED-01", SystemLookup.ItemStatus.PLANNED);
	    	
	    	Request request = requestFact.createRequestInstalled(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusInstalled());	
			  	
		}

	    @Test
	  	public final void testFloorOutLetInstalledToStorage() throws Throwable {
	    	Item item = this.createPowerOutlet("FLOOR-OUTLET-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);
	    	Request request = requestFact.createRequestToStorage(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.STORAGE);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusPlanned());	
			  	
		}	    

	    @Test
	  	public final void testDataPanelPlannedToInstalled() throws Throwable {
	    	Item item = this.createNewTestDataPanel("DATAPANEL-PLANNED-01", null);
	    			
	    	Request request = requestFact.createRequestInstalled(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusInstalled());	
			  	
		}	   

	    @Test
	  	public final void testDataPanelInstalledToStorage() throws Throwable {
	    	Item item = this.createNewTestDataPanel("DATAPANEL-PLANNED-01", SystemLookup.ItemStatus.INSTALLED);
	    			
	    	Request request = requestFact.createRequestToStorage(item);
	    	
	  		testUser.setRequestBypass(true);
	  		
	  		itemStatusWOC.setStatus(SystemLookup.ItemStatus.STORAGE);
	  		itemStatusWOC.update(request, null, testUser);
	  					
			assertTrue(item.isStatusPlanned());	
			  	
		}
	    
	    @Test
	  	public final void testDeviceInstalledConvertToVM() throws Throwable {
	    	Item item = this.createNewTestDevice("DEVICE-01", SystemLookup.ItemStatus.INSTALLED);
	    	List<Request> reqList = requestFact.createRequestConvertToVM(item);
	    	Item vmItem = null;

			assertFalse(item.isStatusStorage());	

	  		testUser.setRequestBypass(true);

	    	for(Request request:reqList){
	    		if(request.getRequestTypeLookup().getLkpValueCode().equals(SystemLookup.RequestTypeLkp.CONVERT_TO_VM)){
	    			vmItem = itemDAO.loadItem(request.getItemId());	    			
	    			this.addTestItem(vmItem);
	    			assertFalse(vmItem.isStatusInstalled());
	    			itemStatusWOC.setStatus(SystemLookup.ItemStatus.INSTALLED);
	    		}
	    		else{
	    			itemStatusWOC.setStatus(SystemLookup.ItemStatus.IN_STORAGE);
	    		}
		  		itemStatusWOC.update(request, null, testUser);		  		
	    	}

	    	assertTrue(vmItem != null);
	    	
	    	vmItem = itemDAO.loadItem(vmItem.getItemId());
	    	item = itemDAO.loadItem(item.getItemId());
	    	
			assertTrue(item.isStatusStorage());	
			assertTrue(vmItem.isStatusInstalled());	    		
		}

}
