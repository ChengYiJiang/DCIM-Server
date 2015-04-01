/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.searchplace.dao.*;
import com.raritan.tdz.searchplace.domain.PlaceSearch;
import com.raritan.tdz.searchplace.domain.PlaceSearchPortData;
import com.raritan.tdz.searchplace.domain.PlaceSearchPortPower;
import com.raritan.tdz.tests.TestBase;

public class PlaceSearchDaoTest extends TestBase {
	PlaceSearchDAO searchDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		searchDAO = (PlaceSearchDAO)ctx.getBean("placeSearchDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	//@Test
	public final void tesReadRecord() throws Throwable {
		//Create a new LKU record
		//LkuData lku = genericDAO.read(1L);
		PlaceSearch rec = searchDAO.read(1L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getPlaceSearchId());
		
			for(PlaceSearchPortPower p:rec.getPowerPorts()){
				System.out.println(p.getPowerPort().getPortName());
			}
			
			for(PlaceSearchPortData p:rec.getDataPorts()){
				System.out.println(p.getDataPort().getPortName());
			}
			
		}
	}
	
	//@Test
	public final void tesLoadRecord() throws Throwable {
		PlaceSearch rec = searchDAO.loadPlaceSearch(11L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getPlaceSearchId());
		}		
	}
	
	//@Test
	public final void tesInsertRecord() throws Throwable {
		PlaceSearch rec = new PlaceSearch();
		Item item = this.createNewTestDevice("TestItem01", 301L);
		Item cab = this.createNewTestCabinet("DestCab01", SystemLookup.ItemStatus.PLANNED);
		
		rec.setItem(item);
		rec.setDestCabinet(cab);
		rec.setSysCreationDate(null); //set to today dat
		rec.setSysCreatedBy(this.getTestAdminUser().getUserName());
		
		Long id = searchDAO.create(rec);
		
		System.out.println(id);
		
		rec = searchDAO.read(id);
		
		searchDAO.delete(rec);
	}
	
}
