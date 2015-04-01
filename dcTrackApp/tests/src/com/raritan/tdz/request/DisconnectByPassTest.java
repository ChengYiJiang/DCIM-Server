package com.raritan.tdz.request;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.DataCircuitFactory;
import com.raritan.tdz.data.RequestFactory;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class DisconnectByPassTest extends TestBase{
	
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
		
		requestFact = (RequestFactory)ctx.getBean("requestFact");
		dataCircuitFact = (DataCircuitFactory)ctx.getBean("dataCircuitFact");		
	}

    @Test
  	public final void testDevicePlannedToInstalled2() throws Throwable {
    	DataCircuit circuit = dataCircuitFact.createDeviceToNetworkCircuit(null);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit));
    	
    	// Item firstNode = itemDAO.getItem(circuit.getStartItemId());
    	
    	// Request requestItem = requestFact.createRequestInstalled(firstNode);
    	Request requestConn = requestFact.createRequestConnect(circuit);
    	
  		List<Request> requests = new ArrayList<Request>();
		// requests.add(requestItem);
		requests.add(requestConn);
		
		Errors errors = getErrorObject();
		
		// assertFalse(firstNode.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
			// requestHome.processRequests(testUser, requests, errors);
		} catch (Exception ex) {
			printError(ex);
		}
		
		// firstNode = itemDAO.loadItem(firstNode.getItemId());
		
		circuit = dataCircuitFact.getCircuit(circuit.getCircuitId());
		
		Long status = circuit.getStartConnection().getStatusLookup().getLkpValueCode();
		
		dataCircuitFact.deleteCircuit(circuit);
		
		Assert.assertFalse(status.equals(SystemLookup.ItemStatus.INSTALLED));	
		
  	}

    @Test
  	public final void testDeviceInstalledToStorage() throws Throwable {
    	DataCircuit circuit = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.INSTALLED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit));
    	
    	Item firstNode = itemDAO.getItem(circuit.getStartItemId());
    	
    	Request requestItem = requestFact.createRequestToStorage(firstNode);
    	Request requestConn = requestFact.createRequestDisconnect(circuit);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(requestItem);
		requests.add(requestConn);
		
		Errors errors = getErrorObject();
		
		Long circuitId = circuit.getCircuitId();
		
		Assert.assertTrue(firstNode.isStatusInstalled());
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
			// requestHome.processRequests(testUser, requests, errors);
		} catch (Exception ex) {
			printError(ex);
		}
		
		firstNode = itemDAO.loadItem(firstNode.getItemId());
		
		Assert.assertTrue(firstNode.isStatusStorage());	
		
		
		DataCircuit cir = dataCircuitFact.getCircuit(circuitId);
		
		if(cir != null){
			dataCircuitFact.deleteCircuit(circuit);
		}
		
		Assert.assertTrue(cir == null);		
		
  	}

    @Test
  	public final void testDisconnectDeviceToNetwork() throws Throwable {
    	DataCircuit circuit = dataCircuitFact.createDeviceToNetworkCircuit(SystemLookup.ItemStatus.INSTALLED);
    	
    	this.addTestItemList(dataCircuitFact.getCircuitItemIds(circuit));
    	
    	Request requestConn = requestFact.createRequestDisconnect(circuit);
    	
  		List<Request> requests = new ArrayList<Request>();
		requests.add(requestConn);
		
		Errors errors = getErrorObject();
		
		Long circuitId = circuit.getCircuitId();
		
		try {
			requestManager.process(requests,  testUser, errors, testUser.isRequestBypass());
			// requestHome.processRequests(testUser, requests, errors);
		} catch (Exception ex) {
			printError(ex);
		}
				
		DataCircuit cir = dataCircuitFact.getCircuit(circuitId);
		
		if(cir != null){
			dataCircuitFact.deleteCircuit(circuit);
		}
		
		Assert.assertTrue(cir == null);	
  	}
    
	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;
		
	}

}
