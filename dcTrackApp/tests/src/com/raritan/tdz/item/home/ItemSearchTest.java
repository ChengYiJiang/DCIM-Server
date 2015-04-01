package com.raritan.tdz.item.home;

import static org.testng.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTO;
import com.raritan.tdz.item.dto.ItemSearchCriteriaDTOImpl;
import com.raritan.tdz.item.dto.ItemSearchFilterDTO;
import com.raritan.tdz.item.dto.ItemSearchFilterDTOImpl;
import com.raritan.tdz.item.dto.ItemSearchResultDTO;
import com.raritan.tdz.item.home.search.ItemSearch;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class ItemSearchTest extends TestBase {
	private ItemSearch itemSearch;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemSearch = (ItemSearch)ctx.getBean("itemSearch");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
	@Test
	public void testSearchDeviceWithPartialItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "SRV", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		
	}
	
	@Test
	public void testSearchDeviceAllItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.DEVICE);

		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() >= 0);
	}
	
	//@Test
	public void testSearchDeviceAllItemNameWithPaging() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.DEVICE);
	
		Long maxCount = itemSearch.getTotalItemCountForClass(SystemLookup.Class.DEVICE);
		
		for (int pageNumber = 0; pageNumber < 1; pageNumber++){
			
			criteria.setPageNumber(new Integer(pageNumber));
			criteria.setMaxLinesPerPage(new Integer(30));
			List<ItemSearchResultDTO> dtoList;
			try {
				dtoList = itemSearch.search(criteria);
			} catch (BusinessValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			} catch (SystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			
			AssertJUnit.assertNotNull(dtoList);
			AssertJUnit.assertTrue(dtoList.size() >= 0 && dtoList.size() <= 30);
		}
	}
	
	@Test
	public void testSearchDeviceAllItemNameMfrSorting() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.DEVICE);
		criteria.setSortField("mfrName");
		criteria.setSortDescending(true);

		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() >= 0);
	}
	
	//@Test
	public void testSearchDeviceAllItemNameWithPagingMfrSorting() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.DEVICE);
		criteria.setSortField("mfrName");
		criteria.setSortDescending(true);
	
		Long maxCount = itemSearch.getTotalItemCountForClass(SystemLookup.Class.DEVICE);
		
		for (int pageNumber = 0; pageNumber < 1; pageNumber++){
			
			criteria.setPageNumber(new Integer(pageNumber));
			criteria.setMaxLinesPerPage(new Integer(30));
			List<ItemSearchResultDTO> dtoList;
			try {
				dtoList = itemSearch.search(criteria);
			} catch (BusinessValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			} catch (SystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
			
			AssertJUnit.assertNotNull(dtoList);
			AssertJUnit.assertTrue(dtoList.size() >= 0 && dtoList.size() <= 30);
		}
	}
	
	@Test
	public void testSearchDeviceExactItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "BLCHS01", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		AssertJUnit.assertTrue(dtoList.get(0).getItemName().equals("BLCHS01"));
	}

	@Test
	public void testSearchDeviceWrongItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "ABCD", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList = null;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (Throwable e) {
			AssertJUnit.assertTrue(e instanceof BusinessValidationException);
		} 
		
		AssertJUnit.assertTrue(dtoList.size() == 0);
	}
	
	@Test
	public void testSearchDevicePartialMfrName() throws BusinessValidationException, SystemException {
	ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("mfrName", "Del", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			AssertJUnit.assertEquals("Result gave items with wrong manufacturer name", "Dell", dto.getMfrName());
		}
	}
	
	@Test
	public void testSearchDevicePartialModelName() throws BusinessValidationException, SystemException {
	ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("modelName", "PowerEdge R710", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);

		for (ItemSearchResultDTO dto: dtoList){
			AssertJUnit.assertEquals("Result gave items with wrong model name", "PowerEdge R710", dto.getModelName());
		}
	}
	
	@Test
	public void testSearchDeviceWeight() throws BusinessValidationException, SystemException {
	ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("weight", "0", ">", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			AssertJUnit.assertTrue(dto.getWeight() > 0.0);
		}
	}
	
	@Test
	public void testSearchDeviceType() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("purposeLkuValue", "Server", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			//AssertJUnit.assertEquals("Result gave items with wrong type", "Server", dto.getPurposeLkuValue());
			AssertJUnit.assertTrue(dto.getPurposeLkuValue().contains("Server"));
		}
	}
	
	@Test
	public void testDeviceSearchNoDeleteStatus() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			AssertJUnit.assertTrue(dto.getStatusLkpValue().equals("Deleted") == false);
		}
	}
	
	@Test
	public void testSearchDevicePurchasePrice() throws BusinessValidationException, SystemException {
	ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("purchasePrice", "0.0", ">", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			AssertJUnit.assertFalse(dto.getPurchasePrice() == 0.0);
		}
	}
	
	@Test
	public void testSearchChassisNameFromDevice() throws BusinessValidationException, SystemException, ParseException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("chassisName", "*", "in", SystemLookup.Class.DEVICE);
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
	}
	
	@Test
	public void testSearchChassisNameFromRPDU() throws BusinessValidationException, SystemException, ParseException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("chassisName", "*", "in", SystemLookup.Class.RACK_PDU);
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() == 0);
	}
	
	@Test
	public void testSearchDeviceCreatedOn() throws BusinessValidationException, SystemException, ParseException {
	ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("sysCreationDate", "10/16/2011", ">=", SystemLookup.Class.DEVICE);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		for (ItemSearchResultDTO dto: dtoList){
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			java.util.Date date = dateFormat.parse("5/16/2011 12:00:00");
			
			AssertJUnit.assertTrue(dto.getSysCreationDate().after(date));
		}
	}
	
	
	@Test
	public void testSearchRPDUWithPartialItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "1A", "in", SystemLookup.Class.RACK_PDU);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		
	}
	
	@Test
	public void testSearchRPDUAllItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.Class.RACK_PDU);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
	}
	
	@Test
	public void testSearchRPDUExactItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "1A-RPDU-L", "in", SystemLookup.Class.RACK_PDU);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		AssertJUnit.assertTrue(dtoList.get(0).getItemName().equals("1A-RPDU-L"));
	}

	@Test
	public void testSearchRPDUWrongItemName() throws BusinessValidationException, SystemException {
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "ABCD", "in", SystemLookup.Class.RACK_PDU);
		
		List<ItemSearchResultDTO> dtoList = null;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (Throwable e) {
			AssertJUnit.assertTrue(e instanceof BusinessValidationException);
		} 
	}
	

	
	
	private ItemSearchCriteriaDTO getItemSearchCriteriaDTO(String key, String value, String operator, Long classLkpValueCode) {
		ItemSearchCriteriaDTO criteria = new ItemSearchCriteriaDTOImpl();
		ItemSearchFilterDTO filter = new ItemSearchFilterDTOImpl();
	
		assertNotNull(key, "key was not Null");
		assertNotNull(value , "value was not Null");
		assertNotNull(operator, "operator was not Null");
		assertNotNull(classLkpValueCode, "classLkpValueCode ws not Null");
		AssertJUnit.assertTrue(classLkpValueCode >= 0);
		
		filter.setKey(key);
		filter.setValue(value);
		filter.setOperation(operator);
		criteria.setFilter(filter);
		criteria.setItemClassLkpValueCode(classLkpValueCode);
		return criteria;
	}
	
	
	@Test
	public void getTotalItemCount() {
		Long cabCount = itemHome.getTotalItemCountForClass(SystemLookup.Class.CABINET);
		assertNotNull(cabCount);
		AssertJUnit.assertTrue(cabCount >= 0);
		Long deviceCount = itemHome.getTotalItemCountForClass(SystemLookup.Class.DEVICE);
		assertNotNull(deviceCount, "deviceCount was not Null");
		AssertJUnit.assertTrue(deviceCount >= 0);
		Long allInCabCount = itemHome.getTotalItemCountForClass(SystemLookup.SpecialClass.ALL_IN_CABINET);
		assertNotNull(allInCabCount, "allInCabCount was not Null");
		AssertJUnit.assertTrue(allInCabCount >= 0);
		Long allCount = itemHome.getTotalItemCountForClass(SystemLookup.SpecialClass.ALL_IN_CABINET);
		assertNotNull(allCount, "allCount is not Null");
		AssertJUnit.assertTrue(allCount >= 0);
	}
	
	
	@Test
	public void testGetAllInCabinetsItems() throws BusinessValidationException, SystemException{
		ItemSearchCriteriaDTO criteria = getItemSearchCriteriaDTO("itemName", "*", "in", SystemLookup.SpecialClass.ALL_IN_CABINET);
		
		List<ItemSearchResultDTO> dtoList;
		try {
			dtoList = itemSearch.search(criteria);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		
		AssertJUnit.assertNotNull(dtoList);
		AssertJUnit.assertTrue(dtoList.size() > 0);
		
		//Make sure that we have each of the classes in the result set. This assumes that we have all types of classes that belong 
		//to all in cabinet.
		int deviceCnt = 0;
		int networkCnt = 0;
		int probeCnt = 0;
		int dataPanelCnt = 0;
		int rackpduCnt = 0;
		int outletCnt = 0;
		
		for (ItemSearchResultDTO dto: dtoList){
			if (dto.getClassLkpValueCode() == SystemLookup.Class.DEVICE)
				deviceCnt++;
			else if (dto.getClassLkpValueCode() == SystemLookup.Class.NETWORK)
				networkCnt++;
			else if (dto.getClassLkpValueCode() == SystemLookup.Class.PROBE)
				probeCnt++;
			else if (dto.getClassLkpValueCode() == SystemLookup.Class.DATA_PANEL)
				dataPanelCnt++;
			else if (dto.getClassLkpValueCode() == SystemLookup.Class.RACK_PDU)
				rackpduCnt++;
			else if (dto.getClassLkpValueCode() == SystemLookup.Class.FLOOR_OUTLET)
				outletCnt++;
		}
		
		AssertJUnit.assertTrue(deviceCnt > 0);
		AssertJUnit.assertTrue(networkCnt > 0);
		AssertJUnit.assertTrue(probeCnt > 0);
		AssertJUnit.assertTrue(dataPanelCnt > 0);
		AssertJUnit.assertTrue(rackpduCnt > 0);
		AssertJUnit.assertTrue(outletCnt > 0);
		
	}
	

}
