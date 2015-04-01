/**
 * 
 */
package com.raritan.tdz.item.validator;


import org.springframework.validation.Errors;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;


import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.item.validators.BladePlacementValidator;
import com.raritan.tdz.item.validators.ItemValidatorCommon;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.tests.TestBase;


public class BladePlacementValidatorTests extends TestBase {
	private ModelDAO modelDao;
	private BladePlacementValidator bladePlacementValidator;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		modelDao = (ModelDAO)ctx.getBean("modelDAO");
		bladePlacementValidator = (BladePlacementValidator)ctx.getBean("bladePlacementValidator");
	}
	
	@Test
	public void testValidateChassisSlotsFrontRearNull(){
		Long model_id = 1614L; //PowerEdge 1955 Chassis
		ItItem bladeChassis = new ItItem();
		bladeChassis.setModel(modelDao.getModelById(model_id));
		ItItem blade = new ItItem();
		blade.setBladeChassis(bladeChassis);
		Map<String,Object> targetMap = getValidatorTargetMap(blade,getTestAdminUser());
		Errors errors =  getErrorObject(blade);
		bladePlacementValidator.validate(targetMap,errors);
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void testValidateSlotWithoutChassis() {
		ItItem blade = new ItItem();
		blade.setParentItem(new CabinetItem());
		blade.setSlotPosition(1);
		Map<String,Object> targetMap = getValidatorTargetMap(blade,getTestAdminUser());
		Errors errors =  getErrorObject(blade);
		bladePlacementValidator.validate(targetMap,errors);
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void testValidateInvalidLocation(){
		Long model_id = 22802L; //Multislot (66057)
		ItItem bladeChassis = new ItItem();
		bladeChassis.setModel(modelDao.getModelById(model_id));
		ItItem blade = new ItItem();
		blade.setBladeChassis(bladeChassis);
		blade.setParentItem(new CabinetItem());
		blade.setModel(modelDao.getModelById(258L));
		Map<String,Object> targetMap = getValidatorTargetMap(blade,getTestAdminUser());
		Errors errors =  getErrorObject(blade);
		bladePlacementValidator.validate(targetMap,errors);
		assertTrue(errors.hasErrors());
	}
	
	//TODO: Need to add more tests here. esp for cases not covered in the above test cases.
	//------------------ Private methods go here ------------------

}
