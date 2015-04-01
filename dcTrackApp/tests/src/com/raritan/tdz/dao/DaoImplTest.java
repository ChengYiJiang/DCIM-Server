/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.tests.TestBase;

public class DaoImplTest extends TestBase {
	//Dao<LkuData, Long> genericDAO;
	//Dao<Item, Long> genericDAO;
	ItemDAO itemDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemDAO = (ItemDAO)ctx.getBean("itemDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	
	@Test
	public void testSearchItems(){
		List<Item> items = null;
		items = itemDAO.searchItemsBySearchString("1A");
		for( Item i : items ){
			System.out.println("### item name=" + i.getItemName());
		}
	}
	//@Test
	public final void testCreateLkuRecord() throws Throwable {
		//Create a new LKU record
		LkuData lku = new LkuData();
		lku.setLkuAttribute("testing");
		lku.setLkuTypeName("TESTING DAO");
		lku.setLkuValue("First LKU DAO");
		//genericDAO.create(lku);
		
	}
	
	@Test
	public final void tesReadRecord() throws Throwable {
		//Create a new LKU record
		//LkuData lku = genericDAO.read(1L);
		Item item = itemDAO.read(2L);
		
		if(item == null){
			System.out.println("testing");
		}
		
	}

}
