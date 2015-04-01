package com.raritan.tdz.data;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.data.PowerChainFactory;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.tests.TestBase;

public class PowerCircuitFactoryTest  extends TestBase  {
	PowerChainFactory powerChainFact;
	ItemFactory itemFact;
	PowerPortFactory portFact;
	PowerConnFactory powerConnFact;
	PowerPortDAO powerPortDAO;
	PowerConnDAO powerConnDAO;
	PowerCircuitFactory powerCircuitFact;
	RequestFactory requestFact;
	
	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemFact = (ItemFactory)ctx.getBean("itemFact");
		powerChainFact = (PowerChainFactory)ctx.getBean("powerChainFact");
		powerCircuitFact = (PowerCircuitFactory)ctx.getBean("powerCircuitFact");
		requestFact = (RequestFactory)ctx.getBean("requestFact");
		portFact = (PowerPortFactory)ctx.getBean("powerPortFact");
		powerConnFact = (PowerConnFactory)ctx.getBean("powerConnFact");
		powerPortDAO = (PowerPortDAO)ctx.getBean("powerPortDAO");
		powerConnDAO = (PowerConnDAO)ctx.getBean("powerConnectionDAO");
		
		testUser = this.getTestAdminUser();
		testUser.setRequestBypass(true);
		
	}
	
	@Test 
	public void testCreateAnyCircuit() throws Throwable {
		PowerCircuit circuit = powerCircuitFact.createRPDUToFloorOutlet();
		
		this.addTestItemList(powerCircuitFact.getCircuitItemIds(circuit));
		
		powerCircuitFact.printCircuit(circuit);
		
		powerCircuitFact.deleteCircuit(circuit, true);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
