/**
 * 
 */
package com.raritan.tdz.item.validator;


import org.springframework.validation.Errors;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

import java.util.Map;

import static org.testng.Assert.*;


import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.item.validators.BladePlacementValidator;
import com.raritan.tdz.item.validators.CabinetPlacementValidator;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.tests.TestBase;


public class CabinetPlacementValidatorTests extends TestBase {
	private ModelDAO modelDao;
	private ItemDAO itemDao;
	private CabinetPlacementValidator cabinetValidator;
	private ItemPlacementHome placementHome;

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		modelDao = (ModelDAO)ctx.getBean("modelDAO");
		cabinetValidator = (CabinetPlacementValidator)ctx.getBean("cabinetPlacementValidator");
		itemDao = (ItemDAO)ctx.getBean("itemDAO");
		placementHome = (ItemPlacementHome)ctx.getBean("itemPlacementHome");
	}
	
	@Test
	public void testValidateNewCabinetPlacement(){
		//First load an existing Location from demo database
		DataCenterLocationDetails siteA = getTestLocation("Demo Site A");
		//Create a cabinet where another cabinet is already occupied
		CabinetItem cabinet = new CabinetItem();
		cabinet.setDataCenterLocation(siteA);
		cabinet.setRowLabel("1");
		cabinet.setPositionInRow(1);
		//Perform validation
		Map<String,Object> targetMap = getValidatorTargetMap(cabinet,getTestAdminUser());
		Errors errors = getErrorObject(cabinet);
		cabinetValidator.validate(targetMap, errors);
		//There should be a failure.
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void testValidateExistingCabinetRowPosChange() throws DataAccessException{
		//Load an existing cabinet 1A from Demo Site A
		CabinetItem cabinet = (CabinetItem) itemDao.loadItem(3L);
		SavedItemData.captureItemData(cabinet, placementHome);
		CabinetItem item = (CabinetItem)itemDao.loadItem(3L);
		//Change the positionInRow that is not available
		item.setPositionInRow(2);
		//Perform validation
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		cabinetValidator.validate(targetMap, errors);
		//There should be a failure.
		assertTrue(errors.hasErrors());
		SavedItemData.clearCurrentItemSaveDataKey();
	}
	
	@Test
	public void testValidateExistingCabinetRowLabelChange() throws DataAccessException{
		//Load an existing cabinet 1A from Demo Site A
		CabinetItem origCabinet = (CabinetItem) itemDao.loadItem(3L);
		SavedItemData.captureItemData(origCabinet, placementHome);
		CabinetItem item = (CabinetItem) itemDao.loadItem(3L);
		//Change the rowLabel
		item.setRowLabel("2");
		//Change the positionInRow that is not available
		item.setPositionInRow(1);
		//Perform validation
		Map<String,Object> targetMap = getValidatorTargetMap(item,getTestAdminUser());
		Errors errors = getErrorObject(item);
		cabinetValidator.validate(targetMap, errors);
		//There should be a failure.
		assertFalse(errors.hasErrors());
		item = (CabinetItem) SavedItemData.getCurrentItem().getSavedItem();
		SavedItemData.clearCurrentItemSaveDataKey();
	}	
	
	//------------------ Private methods go here ------------------

}
