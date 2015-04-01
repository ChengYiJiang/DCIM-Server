/**
 * 
 */
package com.raritan.tdz.item.validator;


import org.springframework.validation.Errors;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;


import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.item.validators.ItemValidatorCommon;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ValueIDToDomainAdaptor;


public class ItemValidatorCommonTests extends TestBase {
	
	protected UnitTestItemDAO unitTestItemDAO;
	private ItemValidatorCommon itemValidatorCommon;
	private SystemLookupFinderDAO systemLookupFinderDAO;
	private UserLookupFinderDAO userLookupFinderDAO;
	
	private ValueIDToDomainAdaptor itemDomainAdaptor;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		unitTestItemDAO = (UnitTestItemDAO) ctx.getBean("unitTestItemDAO");
		itemValidatorCommon = (ItemValidatorCommon)ctx.getBean("itemValidatorCommon");
		systemLookupFinderDAO = (SystemLookupFinderDAO)ctx.getBean("systemLookupDAO");
		userLookupFinderDAO = (UserLookupFinderDAO)ctx.getBean("userLookupDAO");
		itemDomainAdaptor = (ValueIDToDomainAdaptor)ctx.getBean("itemDomainAdaptor");
	}
	
	@Test
	public void testNewEmptyItemCommonValidation() throws Throwable{
		Item item = new Item();
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void testNewItItemCommonValidation() throws Throwable{
		ItItem item = getNewTestDeviceBlade("TestBLADE");
		item.setDataCenterLocation(getTransientTestLocation());
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void testNewItItemCommonValidationValidDates() throws Throwable{
		ItItem item = getNewTestDeviceBlade("TestBLADE");
		
		item.setDataCenterLocation(getTransientTestLocation());
		item.setItemServiceDetails(new ItemServiceDetails());
		item.getItemServiceDetails().setContractBeginDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setContractExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setInstallDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setPurchaseDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void testNewItItemCommonValidationInvalidDates() throws Throwable{
		ItItem item = getNewTestDeviceBlade("TestBLADE");
		
		item.setDataCenterLocation(getTransientTestLocation());
		item.setItemServiceDetails(new ItemServiceDetails());
		item.getItemServiceDetails().setContractBeginDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setContractExpireDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setInstallDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setPurchaseDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2013"));
		
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertTrue(errors.hasErrors());
		assertTrue(errors.getFieldError("contractBeginDate").getCode().contains("contractBeginDate"));
		assertTrue(errors.getFieldError("purchaseDate").getCode().contains("purchaseDate"));
		assertTrue(errors.getFieldError("installDate").getCode().contains("installDate"));
	}
	
	@Test
	public void testNewItItemCommonValidationValidTypeAndFunction() throws Throwable{
		ItItem item = getNewTestDeviceBlade("TestBLADE");
		
		item.setDataCenterLocation(getTransientTestLocation());
		item.setItemServiceDetails(new ItemServiceDetails());
		item.getItemServiceDetails().setContractBeginDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setContractExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setInstallDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setPurchaseDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		
		List<LkuData> functionList = userLookupFinderDAO.findByLkpTypeAndLkpValueCode("FUNCTION",SystemLookup.Class.DEVICE);
		LkuData function = functionList != null && functionList.size() > 0 ? functionList.get(0) : null;
		item.getItemServiceDetails().setFunctionLookup(function);
		
		List<LkuData> typeList = userLookupFinderDAO.findByLkpTypeAndLkpValueCode("TYPE",SystemLookup.Class.DEVICE);
		LkuData type = typeList != null && typeList.size() > 0 ? typeList.get(0) : null;
		item.getItemServiceDetails().setPurposeLookup(type);
		
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void testNewItItemCommonValidationInvalidTypeAndFunction() throws Throwable{
		ItItem item = getNewTestDeviceBlade("TestBLADE");
		
		item.setDataCenterLocation(getTransientTestLocation());
		item.setItemServiceDetails(new ItemServiceDetails());
		item.getItemServiceDetails().setContractBeginDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setContractExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setInstallDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		item.getItemServiceDetails().setPurchaseDate(getDate("dd/MM/yyyy","10/10/2013"));
		item.getItemServiceDetails().setExpireDate(getDate("dd/MM/yyyy","10/10/2014"));
		
		List<LkuData> functionList = userLookupFinderDAO.findByLkpTypeAndLkpValueCode("TYPE",SystemLookup.Class.DEVICE);
		LkuData function = functionList != null && functionList.size() > 0 ? functionList.get(0) : null;
		item.getItemServiceDetails().setFunctionLookup(function);
		
		List<LkuData> typeList = userLookupFinderDAO.findByLkpTypeAndLkpValueCode("FUNCTION",SystemLookup.Class.DEVICE);
		LkuData type = typeList != null && typeList.size() > 0 ? typeList.get(0) : null;
		item.getItemServiceDetails().setPurposeLookup(type);
		
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		callDummyItemAdaptor(item);
		itemValidatorCommon.validate(targetMap, errors);
		assertTrue(errors.hasErrors());
		assertTrue(errors.getFieldError("itemServiceDetails.functionLookup").getCode().contains("LkuType"));
		assertTrue(errors.getFieldError("itemServiceDetails.purposeLookup").getCode().contains("LkuType"));
	}

	
	//------------------ Private methods go here ------------------
	
	private void callDummyItemAdaptor(Item item)
			throws BusinessValidationException, IllegalArgumentException, DataAccessException, IllegalAccessException, InvocationTargetException, ClassNotFoundException{
		itemDomainAdaptor.convert(item, new ArrayList<ValueIdDTO>(), null);
	}

}
