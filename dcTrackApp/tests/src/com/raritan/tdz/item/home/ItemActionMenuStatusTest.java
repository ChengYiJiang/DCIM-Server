package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.page.home.PaginatedHome;
import com.raritan.tdz.tests.TestBase;

public class ItemActionMenuStatusTest extends TestBase{
	private PaginatedHome paginatedHome;
	
	@BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	paginatedHome = (PaginatedHome)ctx.getBean("itemListPaginatedHome");
    }

    @AfterMethod
    public void tearDown() throws Throwable {
    	super.tearDown();
    }
    	
    @Test
    void testCloneItemMenu(){
    	List<Long> itemIdList = new ArrayList<Long>();
    	
    	//5176;"CLARITY^^WHEN-MOVED"
    	//itemIdList.add(3L);
    	//itemIdList.add(9L);
    	itemIdList.add(1084L);
    	//itemIdList.add(5176L);
    	String status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	
    	System.out.println("Status = " + status);
    	Assert.assertTrue(status.indexOf("req_install\":true") > 0);
    }
 	
   // @Test
    void testItemMoveMenu(){
    	List<Long> itemIdList = new ArrayList<Long>();
    	
    	itemIdList.add(325L); //device rackable
    	String status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	System.out.println("Status = " + status);
    	Assert.assertTrue(status.indexOf("req_item_move\":true") > 0);
    	
    	itemIdList.clear();
    	itemIdList.add(9L);    	//Cabinet
    	status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	System.out.println("Status = " + status);
    	Assert.assertFalse(status.indexOf("req_item_move\":true") > 0);
    	
    	itemIdList.clear();
    	itemIdList.add(4815L);  //Chassis-Rackable    	
    	status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	System.out.println("Status = " + status);
    	Assert.assertTrue(status.indexOf("req_item_move\":true") > 0);
    	
    	itemIdList.clear();
    	itemIdList.add(4954L);  //Blade    	
    	status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	System.out.println("Status = " + status);
    	Assert.assertFalse(status.indexOf("req_item_move\":true") > 0);
    	
    	//multiple items
    	itemIdList.clear();
    	itemIdList.add(325L); //device rackable
    	itemIdList.add(4815L);  //Chassis-Rackable     	
    	status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	System.out.println("Status = " + status);
    	Assert.assertFalse(status.indexOf("req_item_move\":true") > 0);    	
    	
    }
	
    //@Test
    void testResubmitItemMenu(){
    	List<Long> itemIdList = new ArrayList<Long>();
    	
    	//CLARITY
    	itemIdList.add(233L);
    	String status = paginatedHome.getItemActionMenuStatus(itemIdList);
    	
    	System.out.println("Status = " + status);
    	Assert.assertTrue(status.indexOf("req_resubmit\":false") > 0);
    }    
}
