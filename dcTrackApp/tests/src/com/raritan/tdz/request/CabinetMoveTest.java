
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

import com.raritan.tdz.data.DataCircuitFactory;
import com.raritan.tdz.data.ItemFactory;
import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class CabinetMoveTest extends TestBase{
	
	ItemFactory itemFact;
	RequestFactory requestFact;
	DataCircuitFactory dataCircuitFact;
	DataCenterLocationDetails testLocation;
	
	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		testLocation = this.getTestLocation("Demo Site A");
		testUser = this.getTestAdminUser();
		testUser.setRequestBypass(true);
		
		itemFact  = (ItemFactory)ctx.getBean("itemFact");
		dataCircuitFact = (DataCircuitFactory)ctx.getBean("dataCircuitFact");	
		requestFact  = (RequestFactory)ctx.getBean("requestFact");
		
		System.out.println("\n\nBeforeMethod....\n");
		
		//for factory to create a new cabinet
		itemFact.setDefaultCabinet(null);
		
		if(itemFact.getCreatedItemList() != null) {
			itemFact.getCreatedItemList().clear();
		}
	}

	@Test
  	public final void testMoveCabinetNoItems() throws Throwable {
    	    	
    	Item cabinet = itemFact.createCabinet("CABINET-NO-ITEM-01", SystemLookup.ItemStatus.INSTALLED);
    	DataCenterLocationDetails oldLoc = cabinet.getDataCenterLocation();

    	this.addTestItemList(itemFact.getCreatedItemList());
    	
    	Item itemWhenMove = itemFact.createCabinet("CABINET-NO-ITEM-01^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);
    	itemWhenMove.setItemToMoveId(cabinet.getItemId());
    	itemWhenMove.setDataCenterLocation(testLocation);
    	itemDAO.update(itemWhenMove);
   
    	Request request = requestFact.createRequestMove(cabinet);
    	
    	requestFact.addPortMoveToRequest(request, cabinet, itemWhenMove);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		
		cabinet = itemDAO.loadItem(cabinet.getItemId());
		
		assertFalse(oldLoc.getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
		assertTrue(cabinet.getDataCenterLocation().getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
  	}


    @Test
  	public final void testMoveCabinetWithItems() throws Throwable {
    	
    	Item cabinet = itemFact.createCabinetWithItems("CABINET-01", SystemLookup.ItemStatus.INSTALLED);
    	DataCenterLocationDetails oldLoc = cabinet.getDataCenterLocation();

    	this.addTestItemList(itemFact.getCreatedItemList());
    	
    	Item itemWhenMove = itemFact.createCabinet("CABINET-01^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);
    	itemWhenMove.setItemToMoveId(cabinet.getItemId());
    	itemWhenMove.setDataCenterLocation(testLocation);
    	itemDAO.update(itemWhenMove);
   
    	Request request = requestFact.createRequestMove(cabinet);
    	
    	requestFact.addPortMoveToRequest(request, cabinet, itemWhenMove);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		
		cabinet = itemDAO.loadItem(cabinet.getItemId());
		
		assertFalse(oldLoc.getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
		assertTrue(cabinet.getDataCenterLocation().getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
		
		for(Item x:cabinet.getChildItems()) {
			Item child = itemDAO.loadItem(x.getItemId());
			assertTrue(testLocation.getDataCenterLocationId().equals(child.getDataCenterLocation().getDataCenterLocationId()));
		}
		
		//itemFact.setDefaultValues(null, null);
  	}

    @Test
  	public final void testMoveCabinetWithDataPanel() throws Throwable {
    	
    	Item cabinet = itemFact.createCabinetWithDataPanels("CABINET-02", SystemLookup.ItemStatus.INSTALLED);
    	DataCenterLocationDetails oldLoc = cabinet.getDataCenterLocation();

    	this.addTestItemList(itemFact.getCreatedItemList());
    	
    	Item itemWhenMove = itemFact.createCabinet("CABINET-02^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);
    	itemWhenMove.setItemToMoveId(cabinet.getItemId());
    	itemWhenMove.setDataCenterLocation(testLocation);
    	itemDAO.update(itemWhenMove);
   
    	Request request = requestFact.createRequestMove(cabinet);
    	
    	requestFact.addPortMoveToRequest(request, cabinet, itemWhenMove);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		
		cabinet = itemDAO.loadItem(cabinet.getItemId());
		
		assertFalse(oldLoc.getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
		assertTrue(cabinet.getDataCenterLocation().getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));		
		assertTrue(cabinet.getChildItems() == null || cabinet.getChildItems().size() == 0);
		
		//itemFact.setDefaultValues(null, null);
  	}

    @Test
  	public final void testMoveCabinetWithFANOUTOutSide() throws Throwable {
    	Item cabinet = itemFact.createCabinet("CABINET-03", SystemLookup.ItemStatus.INSTALLED);
    	DataCenterLocationDetails oldLoc = cabinet.getDataCenterLocation();

    	itemFact.setDefaultCabinet(cabinet);
    	
    	DataCircuit circuit = dataCircuitFact.createFanoutCircuit(SystemLookup.ItemStatus.INSTALLED);
    	
    	this.addTestItemList(itemFact.getCreatedItemList());
    	
    	Item itemWhenMove = itemFact.createCabinet("CABINET-03^^WHEN-MOVE", SystemLookup.ItemStatus.PLANNED);
    	itemWhenMove.setItemToMoveId(cabinet.getItemId());
    	itemWhenMove.setDataCenterLocation(testLocation);
    	itemDAO.update(itemWhenMove);
   
    	Request request = requestFact.createRequestMove(cabinet);
    	
    	requestFact.addPortMoveToRequest(request, cabinet, itemWhenMove);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(request);
		
		Errors errors = getErrorObject();
		
		requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
		
		dataCircuitFact.deleteCircuit(circuit);
		
		cabinet = itemDAO.loadItem(cabinet.getItemId());
		
		assertFalse(oldLoc.getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));
		assertTrue(cabinet.getDataCenterLocation().getDataCenterLocationId().equals(testLocation.getDataCenterLocationId()));		
		assertTrue(cabinet.getChildItems() != null);
		assertTrue(cabinet.getChildItems().size() == 1);
		
		//itemFact.setDefaultValues(null, null);
  	}
    
	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}		
}
