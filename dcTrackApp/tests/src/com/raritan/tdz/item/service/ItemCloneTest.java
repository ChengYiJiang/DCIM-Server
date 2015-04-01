
package com.raritan.tdz.item.service;


import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.tests.TestBase;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */

public class ItemCloneTest extends TestBase {
	@BeforeMethod
	public void setUp() throws Throwable {		
		super.setUp();		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
	@Test 
	public void testGetLicenseCount(){
		System.out.println("\ntestGetLicenseCount()\n");
		
		System.out.println(this.itemHome.getLicenseCount());
	}
	
	@Test
	public void testCloneSingleItem() throws Throwable {
		System.out.println("\ntestCloneSingleItem()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item item = this.createNewTestDevice("CloneX0101", null);
		item.setCadHandle("1018201");
		item.setIsAssetTagVerified(true);
		item.setPiqId(2828);
		item.setItemAlias("Santo Rosario");
		item.setRaritanAssetTag("RT2983289");
		item.setLocationReference("2,3,8");
		
		CustomItemDetails cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("testing this item");
		cfield.setCustomAttrNameLookup(new LkuData(1064L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("more testing");
		cfield.setCustomAttrNameLookup(new LkuData(1066L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		itemDAO.update(item);
		
		CloneItemDTO rec = new CloneItemDTO(item.getItemId());	
		rec.setIncludeCustomFieldData(false);
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        for(Item cloneItem:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        	checkNonCloneableFields(cloneItem, rec, false, false);
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneExistingBusWayOutlet() throws Throwable {
		System.out.println("\n testCloneExistingBusWayOutlet()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		MeItem outlet = (MeItem)itemDAO.loadItem(4338L); //4338;"S05-A1"
		
		CloneItemDTO rec = new CloneItemDTO(outlet.getItemId());	
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        MeItem newOutlet = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	newOutlet = (MeItem)item;
	        	break;
	        }
	        
	        
	        AssertJUnit.assertFalse(newOutlet == null);
	        
	       this.addTestItem(newOutlet);  //add clone items such that they are deleted
	        	        
	        AssertJUnit.assertFalse(newOutlet.getUpsBankItem() == null);
	        AssertJUnit.assertFalse(newOutlet.getPduPanelItem() == null);
	        
        	//check that new Outlet has the same bank as the orignal Outlet
        	AssertJUnit.assertTrue(outlet.getUpsBankItem().getItemId() == newOutlet.getUpsBankItem().getItemId());
        	AssertJUnit.assertTrue(outlet.getNumPorts() == newOutlet.getNumPorts());
        	
        	checkNonCloneableFields(newOutlet, rec, false, false);
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   


	@Test
	public void testCloneExistingFloorOutlet() throws Throwable {
		System.out.println("\n testCloneExistingFloorOutlet()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		MeItem outlet = (MeItem)itemDAO.loadItem(627L); //627;"PDU-2A/PB1:1,3"
		
		CloneItemDTO rec = new CloneItemDTO(outlet.getItemId());	
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        MeItem newOutlet = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	newOutlet = (MeItem)item;
	        	break;
	        }
	        
	        AssertJUnit.assertFalse(newOutlet == null);
	        
	        this.addTestItem(newOutlet);  //add clone items such that they are deleted
	        	        
	        AssertJUnit.assertFalse(newOutlet.getUpsBankItem() == null);
	        AssertJUnit.assertFalse(newOutlet.getPduPanelItem() == null);
	        
        	//check that new Outlet has the same bank as the orignal Outlet
        	AssertJUnit.assertTrue(outlet.getUpsBankItem().getItemId() == newOutlet.getUpsBankItem().getItemId());
        	
        	checkNonCloneableFields(newOutlet, rec, false, false);
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   


	@Test
	public void testCloneExistingUPS() throws Throwable {
		System.out.println("\n testCloneExistingUPS()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		MeItem ups = (MeItem)itemDAO.loadItem(1088L); //1088;"UPS-A"
		
		CloneItemDTO rec = new CloneItemDTO(ups.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        MeItem newUps = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	newUps = (MeItem)item;
	        	break;
	        }
	        
	        AssertJUnit.assertFalse(newUps == null);
	        
	        this.addTestItem(newUps);  //add clone items such that they are deleted
	        	        
	        AssertJUnit.assertFalse(newUps.getUpsBankItem() == null);
	        
        	//check that new UPS has the same bank as the orignal UPS
        	AssertJUnit.assertTrue(ups.getUpsBankItem().getItemId() == newUps.getUpsBankItem().getItemId());
        	
        	rec.setKeepParentChildAssoc(false);
        	checkNonCloneableFields(newUps, rec, false, false);
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneExistingFloorPDU() throws Throwable {
		System.out.println("\n testCloneExistingFloorPDU()\n");
		
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item pdu = itemDAO.loadItem(620L); //620;"PDU-2A"
		
		CloneItemDTO rec = new CloneItemDTO(pdu.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        Item newPdu = null;
	        List<Item> panelList = new ArrayList<Item>();
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	if(item.getItemName().toLowerCase().startsWith(pdu.getItemName().toLowerCase())){
	        		//found cabinet
	        		newPdu = item;
	        	}else{
	        		//found panels
	        		panelList.add(item);
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertFalse(newPdu == null);
	        AssertJUnit.assertFalse(panelList.size() == 0);
	        
	        rec.setKeepParentChildAssoc(false);
	        checkNonCloneableFields(newPdu, rec, false, false);
	        
	        rec.setKeepParentChildAssoc(true);
	        
	        for(Item x:panelList){
	        	AssertJUnit.assertFalse(x.getParentItem() == null);
	        
	        	//check parent-child association
	        	AssertJUnit.assertTrue(newPdu.getItemId() == x.getParentItem().getItemId());
	        	
	        	checkNonCloneableFields(x, rec, false, false);
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   


	@Test
	public void testCloneExistingFloorPDUNoChildren() throws Throwable {
		System.out.println("\n testCloneExistingFloorPDUNoChildren()\n");
		
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item pdu = itemDAO.loadItem(620L); //620;"PDU-2A"
		
		CloneItemDTO rec = new CloneItemDTO(pdu.getItemId());	
		rec.setIncludeChildren(false);
		rec.setKeepParentChildAssoc(false);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        Item newPdu = null;
	        List<Item> cloneList = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        for(Item item:cloneList){	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertTrue(cloneList.size() == 1);	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   
	
	@Test
	public void testCloneCabinetItem() throws Throwable {
		System.out.println("\ntestCloneCabinetItem()\n");
		
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		CabinetItem cabinet = this.createNewTestCabinetWithItems("CloneCabinetX01X", null);
		
		CloneItemDTO rec = new CloneItemDTO(cabinet.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        CabinetItem newCabinet= null;
	        List<Item> newItems = new ArrayList<Item>();
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	if(item instanceof CabinetItem){
	        		//found cabinet
	        		newCabinet = (CabinetItem)item;
	        	}else{
	        		//found children
	        		newItems.add(item);
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertFalse(newCabinet == null);
	        AssertJUnit.assertFalse(newItems.size() == 0);
	        
	        rec.setKeepParentChildAssoc(false);
	        checkNonCloneableFields(newCabinet, rec, false, false);
	        
	        rec.setKeepParentChildAssoc(true);
	        
	        for(Item x:newItems){
	        	AssertJUnit.assertFalse(x.getParentItem() == null);
	        
	        	//check parent-child association
	        	AssertJUnit.assertTrue(newCabinet.getItemId() == x.getParentItem().getItemId());
	        	
	        	checkNonCloneableFields(x, rec, false, false);
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneStacksItem() throws Throwable {
		System.out.println("\ntestCloneStacksItem()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String stackName = "CloneStack01X";
		
		ItItem stack = this.createNewTestNetworkStack("CloneStack01X-01", stackName);
		ItItem sibling = this.createNewTestNetworkStack("CloneStack01X-02", stackName);
		sibling.setCracNwGrpItem(stack);
		
		itemDAO.update(sibling);
		
		CloneItemDTO rec = new CloneItemDTO(stack.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        ItItem newStack= null;
	        ItItem newSibling = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	if(newStack == null){
	        		//found main stack
	        		newStack = (ItItem)item.getCracNwGrpItem();
	        	}else{
	        		//found sibling stack
	        		newSibling = (ItItem)item;
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertFalse(newStack == null);
	        AssertJUnit.assertFalse(newSibling == null);
	        AssertJUnit.assertFalse(newSibling.getCracNwGrpItem() == null);
	        
	        //check parent-child association
	        AssertJUnit.assertTrue(newStack.getItemId() == newSibling.getCracNwGrpItem().getItemId());
	        
	        checkNonCloneableFields(newStack, rec, true, false);        
	        checkNonCloneableFields(newSibling, rec, true, false);
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneChassisItem() throws Throwable {
		System.out.println("\ntestCloneChassisItem()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String chassisName = "CloneChassis01X";
		ItItem chassis = this.createNewTestDeviceChassis(chassisName);
		ItItem blade = this.createNewTestDeviceBlade("CloneBlade01X");
		blade.setBladeChassis(chassis);
		
		itemDAO.update(blade);
		
		CloneItemDTO rec = new CloneItemDTO(chassis.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        ItItem newChassis = null;
	        ItItem newBlade = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	if(item.getItemName().toLowerCase().startsWith(chassisName.toLowerCase())){
	        		//found chassis
	        		newChassis = (ItItem)item;
	        	}else{
	        		//found blade
	        		newBlade = (ItItem)item;
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertFalse(newChassis == null);
	        AssertJUnit.assertFalse(newBlade == null);
	        AssertJUnit.assertFalse(newBlade.getBladeChassis() == null);
	        
	        AssertJUnit.assertTrue(blade.getSlotPosition() == newBlade.getSlotPosition());
	        
	        //check parent-child association
	        AssertJUnit.assertTrue(newChassis.getItemId() == newBlade.getBladeChassis().getItemId());
	        
	        rec.setKeepParentChildAssoc(false);
	        checkNonCloneableFields(newChassis, rec, false, true);
	        
	        rec.setKeepParentChildAssoc(true);
	        checkNonCloneableFields(newBlade, rec, false, true);
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   
	
	@Test
	public void testCloneSingleChassisItem() throws Throwable {
		System.out.println("\ntestCloneSingleChassisItem()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String chassisName = "CloneSingleChassis01X";
		ItItem chassis = this.createNewTestNetworkChassis(chassisName);
		
		CloneItemDTO rec = new CloneItemDTO(chassis.getItemId());	
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        ItItem newChassis = null;
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
        		newChassis = (ItItem)item;
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        	
	        	checkNonCloneableFields(item, rec, false, false);
	        }
	        
	        AssertJUnit.assertFalse(newChassis == null);
	        AssertJUnit.assertFalse(newChassis.getGroupingName() == null);
	        
	        System.out.println(newChassis.getGroupingName() + " == " + chassis.getGroupingName());
	        
	        AssertJUnit.assertFalse(newChassis.getGroupingName().equals(chassis.getGroupingName()));
	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}
	
	@Test
	public void testCloneFreeStandingItem() throws Throwable {
		System.out.println("\ntestCloneFreeStandingItem()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item itemFS = this.createNewTestDeviceFS("CloneFSDevice10X10");
		
		CloneItemDTO rec = new CloneItemDTO(itemFS.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);

		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        for(Item item:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	if(!(item instanceof CabinetItem)){
	        		AssertJUnit.assertTrue(item.getParentItem() != null);
	        		rec.setKeepParentChildAssoc(true);
	        	}
	        	else{
	        		rec.setKeepParentChildAssoc(false);
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        	
	        	checkNonCloneableFields(item, rec, false, false);
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testValidate() throws Throwable {
		System.out.println("\ntestValidate()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		//bad quantity
		CloneItemDTO rec = new CloneItemDTO(34);
		rec.setQuantity(1555);
		itemList.add(rec);
		
		//bad status
		rec = new CloneItemDTO(34);
		rec.setStatusValueCode(1000);
		itemList.add(rec);

		//bad item
		rec = new CloneItemDTO(0);
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == 0);			
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			
			AssertJUnit.assertTrue(be.getValidationErrors().size() >= itemList.size());
		}		
	} 

	@Test
	public void testNonCloneableClasses() throws Throwable {
		System.out.println("\ntestNonCloneableClasses()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item item = this.createNewTestPassive("ClonePassive01X01", -1L, null);
		//bad class
		CloneItemDTO rec = new CloneItemDTO(item.getItemId());
		itemList.add(rec);
		
		item = this.createNewTestPerfTiles("ClonePerfTile01X01", null);
		rec = new CloneItemDTO(item.getItemId());
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == 0);			
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			
			AssertJUnit.assertTrue(be.getValidationErrors().size() >= itemList.size());
		}		
	} 

	@Test
	public void testCloningCustomFieldNoData() throws Throwable {
		System.out.println("\ntestCloningCustomFieldNoData()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item item = this.createNewTestDevice("CloneX0101", null);

		CustomItemDetails cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("testing this item");
		cfield.setCustomAttrNameLookup(new LkuData(1064L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("more testing");
		cfield.setCustomAttrNameLookup(new LkuData(1066L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		itemDAO.update(item);
		
		CloneItemDTO rec = new CloneItemDTO(item.getItemId());	
		rec.setIncludeCustomFieldData(false);
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        for(Item cloneItem:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        	checkNonCloneableFields(cloneItem, rec, false, false);
	        	
	        	AssertJUnit.assertTrue(cloneItem.getCustomFields().size() == item.getCustomFields().size());
	        	
	        	for(CustomItemDetails cf:cloneItem.getCustomFields()){
	        		AssertJUnit.assertTrue(cf.getAttrValue() == null);
	        	}
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloningCustomFieldWithData() throws Throwable {
		System.out.println("\ntestCloningCustomFieldWithData()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item item = this.createNewTestDevice("CloneX0101", null);

		CustomItemDetails cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("testing this item");
		cfield.setCustomAttrNameLookup(new LkuData(1064L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		cfield = new CustomItemDetails();
		cfield.setItem(item);
		cfield.setAttrValue("more testing");
		cfield.setCustomAttrNameLookup(new LkuData(1066L));
		cfield.setCustomDataTypeLookup(new LkuData(530L));
		item.addCustomField(cfield);
		
		itemDAO.update(item);
		
		CloneItemDTO rec = new CloneItemDTO(item.getItemId());	
		rec.setIncludeCustomFieldData(true);
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        for(Item cloneItem:itemFinderDAO.findItemsByCreationDate(rec.getCreationDate())){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        	checkNonCloneableFields(cloneItem, rec, false, false);
	        	
	        	AssertJUnit.assertTrue(cloneItem.getCustomFields().size() == item.getCustomFields().size());
	        	
	        	for(CustomItemDetails cf:cloneItem.getCustomFields()){
	        		AssertJUnit.assertFalse(cf.getAttrValue() == null);
	        	}
	        }
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testLicenseLimit() throws Throwable {
		System.out.println("\ntestLicenseLimit()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		Item item = this.createNewTestCabinet("CabinetX0Y01", null);
		CloneItemDTO rec = new CloneItemDTO(item.getItemId());
		rec.setQuantity(9999);
		itemList.add(rec);		
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == 0);			
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			
			AssertJUnit.assertTrue(be.getValidationErrors().size() >= itemList.size());
		}		
	} 
	

	@Test
	public void testCloneCabinetWithChassis() throws Throwable {
		System.out.println("\ntestCloneCabinetWithChassis()\n");
				
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String chassisName = "CloneChassis01X";
		
		CabinetItem cabinet = this.createNewTestCabinet("CloneCabinetX01X", null);
		ItItem chassis = this.createNewTestDeviceChassis(chassisName);
		ItItem blade = this.createNewTestDeviceBlade("CloneBlade01X");

		chassis.setParentItem(cabinet);
		blade.setParentItem(cabinet);
		blade.setBladeChassis(chassis);
		
		itemDAO.update(chassis);
		itemDAO.update(blade);
		
		CloneItemDTO rec = new CloneItemDTO(cabinet.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        List<Item> newList = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        for(Item cloneItem:newList){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertTrue(newList.size() == 3);	        
	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   
	

	@Test
	public void testCloneCabinetWithRackPdu() throws Throwable {
		System.out.println("\ntestCloneCabinetWithRackPdu()\n");
				
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		CabinetItem cabinet = this.createNewTestCabinet("CloneCabinetX01X", null);
		MeItem rackPdu = this.createNewTestRPDU("CloneRackPdu01X", null);

		rackPdu.setParentItem(cabinet);
		
		itemDAO.update(rackPdu);
		
		CloneItemDTO rec = new CloneItemDTO(cabinet.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        List<Item> newList = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        for(Item cloneItem:newList){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        	
	        	if(cloneItem instanceof CabinetItem){
	        		AssertJUnit.assertTrue(cloneItem.getParentItem() == null);
	        	}
	        	else{
	        		AssertJUnit.assertFalse(cloneItem.getParentItem() == null);
	        	}
	        }
	        
	        AssertJUnit.assertTrue(newList.size() == 2);	        
	        
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneCabinetWithStack() throws Throwable {
		System.out.println("\ntestCloneCabinetWithStack()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String stackName = "CloneStack01X";
		
		CabinetItem cabinet = this.createNewTestCabinet("CloneCabinetX01X", null);
		ItItem stack = this.createNewTestNetworkStack("CloneStack01X-01", stackName);
		ItItem sibling1 = this.createNewTestNetworkStack("CloneStack01X-02", stackName);
		ItItem sibling2 = this.createNewTestNetworkStack("CloneStack01X-03", stackName);
		
		stack.setParentItem(cabinet);
		sibling1.setCracNwGrpItem(stack);
		sibling1.setParentItem(cabinet);
		sibling2.setCracNwGrpItem(stack);
		sibling2.setParentItem(cabinet);
		
		itemDAO.update(stack);
		itemDAO.update(sibling1);
		itemDAO.update(sibling2);
		
		CloneItemDTO rec = new CloneItemDTO(cabinet.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        List<Item> newList = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        for(Item cloneItem:newList){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        }
	        
	        AssertJUnit.assertTrue(newList.size() == 4);
	        long siblingItemId = 0;
	        
	        for(Item cloneItem:newList){
	        	if(!(cloneItem instanceof CabinetItem)){
	        		AssertJUnit.assertFalse(cloneItem.getCracNwGrpItem() == null);
	        		
	        		if(siblingItemId == 0){
	        			siblingItemId = cloneItem.getCracNwGrpItem().getItemId();
	        		}
	        		else{
	        			AssertJUnit.assertTrue(siblingItemId == cloneItem.getCracNwGrpItem().getItemId());
	        		}
	        	}
	        }
	        
	        
	        //clear these fields to avoid problem with delete if stack is not clone correctly
	        for(Item cloneItem:newList){
	        	cloneItem.setParentItem(null); 
	        	cloneItem.setCracNwGrpItem(null);
	        	itemDAO.merge(cloneItem);
	        }
	        
	    	sibling1.setCracNwGrpItem(null);
			sibling1.setParentItem(null);
			sibling2.setCracNwGrpItem(null);
			sibling2.setParentItem(null);
			
			itemDAO.update(sibling1);
			itemDAO.update(sibling2);
	        
	          
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneCabinetWithDuplicate() throws Throwable {
		System.out.println("\ntestCloneCabinetWithDuplicate()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		String stackName = "CloneStack01X";
		
		CabinetItem cabinet = this.createNewTestCabinet("CloneCabinetX01X", null);
		ItItem stack = this.createNewTestNetworkStack("CloneStack01X-01", stackName);
		
		stack.setParentItem(cabinet);
		
		itemDAO.update(stack);
		
		CloneItemDTO rec = new CloneItemDTO(cabinet.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		
		itemList.add(rec);
		
		rec = new CloneItemDTO(stack.getItemId());	
		rec.setIncludeChildren(true);
		rec.setKeepParentChildAssoc(true);
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == 1);
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");
	        	
	        }
	        
	        List<Item> newList = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        for(Item cloneItem:newList){
	        	this.addTestItem(cloneItem);  //add clone items such that there are deleted
	        }	        
	          
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneExistingDataPanelWith1FarEnd() throws Throwable {
		System.out.println("\n testCloneExistingDataPanelWith1FarEnd()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		ItItem panel = (ItItem)itemDAO.loadItem(4562L); //4562;"S77-CP2"
		
		CloneItemDTO rec = new CloneItemDTO(panel.getItemId());	
		rec.setIncludeFarEndDataPanel(true);
			
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        List<Item> items = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        ItItem newPanel1 = null;
	        
	        for(Item item:items){
	        	if(item.getItemName().toLowerCase().startsWith(panel.getItemName().toLowerCase())){
	        		//found cabinet
	        		newPanel1 = (ItItem)item;
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
		        
	        AssertJUnit.assertTrue(items.size() == 2);
	        
	        ItItem newPanel2 = (ItItem)items.get(1);
	        
	        AssertJUnit.assertFalse(newPanel1 == null);
	        AssertJUnit.assertFalse(newPanel2 == null);
	        
	        AssertJUnit.assertTrue(newPanel1.getParentItem() == null);
	        AssertJUnit.assertTrue(newPanel2.getParentItem() == null); 
	        
        	checkNonCloneableFields(newPanel1, rec, false, false);
        	checkNonCloneableFields(newPanel2, rec, false, false);
        	
        	AssertJUnit.assertTrue(panel.getDataPorts().size() == newPanel1.getDataPorts().size()); 
        	AssertJUnit.assertTrue(newPanel1.getDataPorts().size() == newPanel2.getDataPorts().size());
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneExistingDataPanelWith2FarEnd() throws Throwable {
		System.out.println("\n testCloneExistingDataPanelWith2FarEnd()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		ItItem panel = (ItItem)itemDAO.loadItem(513L); //513 1A1 connect to two data panels
		
		CloneItemDTO rec = new CloneItemDTO(panel.getItemId());	
		rec.setIncludeFarEndDataPanel(true);
			
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        List<Item> items = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        ItItem newPanel1 = null;
	        ItItem otherPanels[] = new ItItem[2];
	        int count = 0;
	        
	        for(Item item:items){
	        	if(item.getItemName().toLowerCase().startsWith(panel.getItemName().toLowerCase())){
	        		//found cabinet
	        		newPanel1 = (ItItem)item;
	        	}
	        	else {
	        		otherPanels[count] = (ItItem) item;
	        		count++;
	        	}
	        	
	        	this.addTestItem(item);  //add clone items such that there are deleted
	        }
		        
	        AssertJUnit.assertTrue(items.size() == 3);
	        
	        ItItem newPanel2 = otherPanels[0]; //(ItItem)items.get(1);
	        ItItem newPanel3 = otherPanels[1]; //(ItItem)items.get(2);
	        
	        AssertJUnit.assertFalse(newPanel1 == null);
	        AssertJUnit.assertFalse(newPanel2 == null);
	        AssertJUnit.assertFalse(newPanel3 == null);
	        
	        AssertJUnit.assertTrue(newPanel1.getParentItem() == null);
	        AssertJUnit.assertTrue(newPanel2.getParentItem() == null); 
	        AssertJUnit.assertTrue(newPanel3.getParentItem() == null);
	        
        	checkNonCloneableFields(newPanel1, rec, false, false);
        	checkNonCloneableFields(newPanel2, rec, false, false);
        	checkNonCloneableFields(newPanel3, rec, false, false);
        	
        	AssertJUnit.assertTrue(panel.getDataPorts().size() == newPanel1.getDataPorts().size()); 
        	AssertJUnit.assertTrue((newPanel1.getDataPorts().size() / 2) == newPanel2.getDataPorts().size());
        	AssertJUnit.assertTrue((newPanel1.getDataPorts().size() / 2) == newPanel3.getDataPorts().size());
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   

	@Test
	public void testCloneExistingDataPanelWithNoFarEnd() throws Throwable {
		System.out.println("\n testCloneExistingDataPanelWithNoFarEnd()\n");
		
		List<CloneItemDTO> itemList = new ArrayList<CloneItemDTO>();
		
		ItItem panel = (ItItem)itemDAO.loadItem(513L); //513 1A
		
		CloneItemDTO rec = new CloneItemDTO(panel.getItemId());	
		rec.setIncludeFarEndDataPanel(false);
			
		itemList.add(rec);
		
		try{
			List<CloneItemDTO> recList = itemService.cloneItems(itemList);
			
			AssertJUnit.assertTrue(recList.size() == itemList.size());
			
	        for(CloneItemDTO x:recList){
	        	System.out.println("\n" + x + "\n");	       
	        }
	        
	        List<Item> items = itemFinderDAO.findItemsByCreationDate(rec.getCreationDate());
	        
	        AssertJUnit.assertTrue(items.size() == 1);
	        
	        ItItem newPanel1 = (ItItem)items.get(0);

	        for(Item item:items){
	        	this.addTestItem(item);  //add clone items such that they are deleted
	        }
	        
	        
	        AssertJUnit.assertFalse(newPanel1 == null);
	        	        	        
	        AssertJUnit.assertTrue(newPanel1.getParentItem() == null);
	        
        	checkNonCloneableFields(newPanel1, rec, false, false);
        	
        	AssertJUnit.assertTrue(newPanel1.getDataPorts().size() == 0); 
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
		}		
	}   
	 			
	private void checkNonCloneableFields(Item item, CloneItemDTO criteria, boolean stackable, boolean chassis){
		AssertJUnit.assertFalse(item == null);
		AssertJUnit.assertTrue(item.getItemAlias() == null);
		AssertJUnit.assertTrue(item.getCadHandle() == null);
		AssertJUnit.assertTrue(item.getLogicalPath() == null);
		AssertJUnit.assertTrue(item.getPiqId() == null);
		
		if(criteria.isKeepParentChildAssoc()){
			if(stackable){
				AssertJUnit.assertFalse(item.getCracNwGrpItem() == null);
			}else if(chassis){
				//do nothing
			}
			else{
				AssertJUnit.assertFalse(item.getParentItem() == null);
			}
		}else{
			AssertJUnit.assertTrue(item.getParentItem() == null);
			AssertJUnit.assertTrue(item.getUPosition() < 0);
			AssertJUnit.assertTrue(item.getSlotPosition() == -9);
			
			if(!stackable){
				AssertJUnit.assertTrue(item.getCracNwGrpItem() == null);
			}
		}
		
		AssertJUnit.assertTrue(item.getRaritanAssetTag() == null);
		AssertJUnit.assertTrue(item.getIsAssetTagVerified() == false);
		AssertJUnit.assertTrue(item.getLocationReference() == null);
		
		
		ItemServiceDetails detail = item.getItemServiceDetails();
		
		AssertJUnit.assertFalse(detail == null);
		AssertJUnit.assertTrue(detail.getSerialNumber() == null);
		AssertJUnit.assertTrue(detail.getAssetNumber() == null);
		AssertJUnit.assertTrue(detail.getItemAdminTeamLookup() == null);
		AssertJUnit.assertTrue(detail.getItemAdminUser() == null);
		
		if(item instanceof ItItem){
			ItItem r = (ItItem)item;
			AssertJUnit.assertTrue(r.getVmClusterLookup() == null);
			
			if(criteria.isKeepParentChildAssoc() == false){
				AssertJUnit.assertTrue(r.getBladeChassis() == null);
			}
		}
		
		if(item instanceof MeItem){
			MeItem r = (MeItem)item;
			AssertJUnit.assertTrue(r.getLocationXY() == null);
		}
		
		if(item instanceof CabinetItem){
			CabinetItem r = (CabinetItem)item;
			AssertJUnit.assertTrue(r.getPositionInRow() == 0);
			AssertJUnit.assertTrue(r.getRowLabel() == null);
		}
		
		if(criteria.isIncludeDataPort()){
			for(DataPort port:item.getDataPorts()){
				checkNonCloneableFields(port);
			}
		}

		if(criteria.isIncludePowerPort()){
			for(PowerPort port:item.getPowerPorts()){
				checkNonCloneableFields(port);
			}
		}
		
		if(criteria.isIncludeSensorPort()){
			for(SensorPort port:item.getSensorPorts()){
				checkNonCloneableFields(port);
			}
		}			
	}
	
	private void checkNonCloneableFields(DataPort port){
		AssertJUnit.assertFalse(port == null);
		AssertJUnit.assertTrue(port.getPortStatusLookup() == null);
		AssertJUnit.assertTrue(port.getMacAddress() == null);
		AssertJUnit.assertTrue(port.getAddress() == null);
	}

	private void checkNonCloneableFields(PowerPort port){
		AssertJUnit.assertFalse(port == null);
		AssertJUnit.assertTrue(port.getPortStatusLookup() == null);
		AssertJUnit.assertTrue(port.getPiqId() == null);
		AssertJUnit.assertTrue(port.getWattsActual() == -1);
		AssertJUnit.assertTrue(port.getAmpsActual() == -1);
		AssertJUnit.assertTrue(port.getAmpsActualA() == -1);
		AssertJUnit.assertTrue(port.getAmpsActualB() == -1);
		AssertJUnit.assertTrue(port.getAmpsActualC() == -1);
		AssertJUnit.assertTrue(port.getAmpsActualN() == -1);
		AssertJUnit.assertTrue(port.getPowerFactorActual() == -1);
		
	}

	private void checkNonCloneableFields(SensorPort port){
		AssertJUnit.assertFalse(port == null);
		AssertJUnit.assertTrue(port.getPortStatusLookup() == null);
		AssertJUnit.assertFalse(port.getXyzLocation() == null);
		AssertJUnit.assertTrue(port.getValueActualUnit() == null);
		AssertJUnit.assertFalse(port.getValueActual() == -1);		
	}
		
}
