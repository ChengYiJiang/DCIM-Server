/**
 * 
 */
package com.raritan.tdz.item.validator;

import static org.testng.Assert.assertTrue;

import org.springframework.validation.Errors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.item.validators.BladePlacementValidator;
import com.raritan.tdz.item.validators.ItemRequiredFieldValidator;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.tests.TestBase;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author prasanna
 *
 */
public class ItemRequiredFieldValidatorTests extends TestBase{
	
	private ItemRequiredFieldValidator requiredFieldValidator;
	private SystemLookupFinderDAO systemLookupFinderDao;
	private ModelDAO modelDao;
	
	/*
	 * (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		requiredFieldValidator = (ItemRequiredFieldValidator)ctx.getBean("itemRequiredFieldValidator");
		systemLookupFinderDao = (SystemLookupFinderDAO)ctx.getBean("systemLookupDAO");
		modelDao = (ModelDAO)ctx.getBean("modelDAO");
	}

	@Test
	public void testNoItemName(){
		ItItem itItem = new ItItem();
		itItem.setClassLookup(systemLookupFinderDao.findByLkpValueCode(SystemLookup.Class.DEVICE).get(0));
		Map<String,Object> targetMap = getValidatorTargetMap(itItem, getTestAdminUser());
		Errors errors = getErrorObject(itItem);
		requiredFieldValidator.validate(targetMap, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("itemName"));
	}
	
	@Test
	public void testNoMake(){
		ItItem itItem = new ItItem();
		itItem.setClassLookup(systemLookupFinderDao.findByLkpValueCode(SystemLookup.Class.DEVICE).get(0));
		itItem.setItemName("UNIT_TEST_REQURED_FIELD");
		Map<String,Object> targetMap = getValidatorTargetMap(itItem, getTestAdminUser());
		Errors errors = getErrorObject(itItem);
		requiredFieldValidator.validate(targetMap, errors);
		
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("mfrName"));
		assertTrue(errors.hasFieldErrors("modelName"));
	}
}
