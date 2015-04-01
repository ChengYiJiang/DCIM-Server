/**
 * 
 */
package com.raritan.tdz.item.service;


import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.dto.CircuitNodeInterface;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */


public class ItemDeleteTest extends TestBase {
	private CircuitPDHome circuitHome;
	private CircuitPDService circuitService;
	        
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		circuitHome = (CircuitPDHome)ctx.getBean("circuitPDHome");
		circuitService = (CircuitPDService)ctx.getBean("circuitPDService");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		//clearEvents();
		clearTestItems();
		
		super.tearDown();
	}
	
		
	@Test
	public void testDeleteExistingItemWithError() throws Throwable {
		System.out.println("testDeleteExistingItemWithError()");
		
		List<Integer> itemIdList = new ArrayList<Integer>();
		itemIdList.add(4834);    //installed item 
		itemIdList.add(342);     //connected data item
		itemIdList.add(505);     //connected power item
		itemIdList.add(0);       //bad item
		itemIdList.add(2000000000);   //bad item
		
		try{
			List<Long> recList = itemService.deleteItems(itemIdList);
			
			AssertJUnit.assertTrue(recList.size() == 0);
			
	        for(Long x:recList){
	        	System.out.println(x);
	        }	        	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			
			AssertJUnit.assertTrue(be.getValidationErrors().size() >= itemIdList.size());
		}		
	}   
	

	@Test
	public void testDeleteExistingItemNoError() throws Throwable {
		//5217 new
		//5218 work

		System.out.println("testDeleteExistingItemNoError()");
		
		List<Integer> itemIdList = new ArrayList<Integer>();
		itemIdList.add(782);
		
		try{
			List<Long> recList = itemService.deleteItems(itemIdList);
			
			AssertJUnit.assertTrue(recList.size() == itemIdList.size());
			
	        for(Long x:recList){
	        	System.out.println(x);
	        }	        	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			
			AssertJUnit.assertTrue(be.getValidationErrors().size() >= itemIdList.size());
		}		
	}   
		
	@Test
	public void testDeleteCreatedItem() throws Throwable {
		System.out.println("in testDeleteCreatedItem()");
		
		try{
			Item item = this.createNewTestDevice("DELETE-TEST01XX", null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	
			
			item = this.createNewTestItem("DELETE-TEST02", SystemLookup.Class.CABINET, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	
			
			item = this.createNewTestItem("DELETE-TEST03", SystemLookup.Class.NETWORK, SystemLookup.SubClass.NETWORK_STACK);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	
			
			item = this.createNewTestItem("DELETE-TEST04", SystemLookup.Class.DATA_PANEL, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST05", SystemLookup.Class.PASSIVE, SystemLookup.SubClass.BLANKING_PLATE);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST06", SystemLookup.Class.CRAC, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			/*item = this.createNewTestItem("DELETE-TEST07", SystemLookup.Class.CRAC_GROUP, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());*/	

			item = this.createNewTestItem("DELETE-TEST08", SystemLookup.Class.FLOOR_OUTLET, SystemLookup.SubClass.WHIP_OUTLET);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST09", SystemLookup.Class.FLOOR_PDU, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());

			item = this.createNewTestItem("DELETE-TEST10", SystemLookup.Class.PASSIVE, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST11", SystemLookup.Class.PERFORATED_TILES, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST12", SystemLookup.Class.PROBE, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST13", SystemLookup.Class.RACK_PDU,null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			item = this.createNewTestItem("DELETE-TEST14", SystemLookup.Class.UPS, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());	

			/*item = this.createNewTestItem("DELETE-TEST15", SystemLookup.Class.UPS_BANK, null);
			itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());*/
			
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}
	

}
