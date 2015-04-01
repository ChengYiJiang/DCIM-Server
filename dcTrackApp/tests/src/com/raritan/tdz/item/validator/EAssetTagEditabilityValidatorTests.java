/**
 * 
 */
package com.raritan.tdz.item.validator;


import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.validation.Errors;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;


import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.validators.EAssetTagEditabilityValidator;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.tests.TestBase;


public class EAssetTagEditabilityValidatorTests extends TestBase {
	private ModelDAO modelDao;
	private ItemDAO itemDao;
	private EAssetTagEditabilityValidator eAssetTagValidator;
	private ItemFinderDAO itemFinderDAO;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		modelDao = (ModelDAO)ctx.getBean("modelDAO");
		itemDao = (ItemDAO)ctx.getBean("itemDAO");
		eAssetTagValidator = (EAssetTagEditabilityValidator)ctx.getBean("eAssetTagEditabilityValidator");
		itemFinderDAO = (ItemFinderDAO)ctx.getBean("itemFinderDao");
	}
	
	//@Test
	public void testAssetTagEditable(){
		ItItem item = (ItItem) itemDao.read(3139L); //AGER
		item.setIsAssetTagVerified(false);
		Map<String,Object> targetMap = getValidatorTargetMap(item, getTestAdminUser());
		Errors errors = getErrorObject(item);
		eAssetTagValidator.validate(targetMap, errors);
		assertFalse(errors.hasErrors());
	}
	
	//@Test
	public void testAssetTagEditableMock(){
		final List<Boolean> assetTagVerifiedList = new ArrayList<Boolean>(){{add(false);}};
		final List<String> assetTagList = new ArrayList<String>(){{add("testEAssetTag");}};
		final ItItem item = new ItItem();
		item.setItemId(3130L);
		
		item.setIsAssetTagVerified(false);
		
		itemFinderDAO = (ItemFinderDAO)ctx.getBean("itemFinderDao");
		
		jmockContext.checking(new Expectations() {{
			allowing(itemFinderDAO).findEAssetTagById(with(3130L)); will(returnValue(assetTagList));
			allowing(itemFinderDAO).findEAssetTagVerifiedById(with(3130L)); will(returnValue(assetTagVerifiedList));
		}});
		
		Map<String,Object> targetMap = getValidatorTargetMap(item, getTestAdminUser());
		Errors errors = getErrorObject(item);
		
		eAssetTagValidator.validate(targetMap, errors);
		assertFalse(errors.hasErrors());
		
		jmockContext.assertIsSatisfied();
	}
	
	//@Test
	public void testAssetTagNotEditable(){
		ItItem item = (ItItem) itemDao.read(3139L); //AGER
		String origAssetTag = item.getRaritanAssetTag();
		boolean origIsVerified = item.getIsAssetTagVerified();
		item.setIsAssetTagVerified(true);
		item.setRaritanAssetTag("UNIT_TEST");
		itemDao.merge(item);
		item.setRaritanAssetTag("UNIT_TEST1");
		Map<String,Object> targetMap = getValidatorTargetMap(item, getTestAdminUser());
		Errors errors = getErrorObject(item);
		eAssetTagValidator.validate(targetMap, errors);
		//This should have the error reported!
		assertTrue(errors.hasErrors());
		//Change the verified to false so that we dont overwrite this in the database
		item.setIsAssetTagVerified(origIsVerified);
		item.setRaritanAssetTag(origAssetTag);
		itemDao.merge(item);
	}
	
	//@Test
	public void testAssetTagNotEditableMock(){
		
		final List<String> assetTagList = new ArrayList<String>(){{add("testEAssetTag");}};
		final List<Boolean> assetTagVerifiedList = new ArrayList<Boolean>(){{add(true);}};
		ItItem item = new ItItem();
		item.setItemId(3139L);
		item.setRaritanAssetTag("UNIT_TEST");
		
		
		
		jmockContext.checking(new Expectations() {{
			allowing(itemFinderDAO).findEAssetTagById(with(3139L)); will(returnValue(assetTagList));
			allowing(itemFinderDAO).findEAssetTagVerifiedById(with(3139L)); will(returnValue(assetTagVerifiedList));
		}});
		
		Map<String,Object> targetMap = getValidatorTargetMap(item, getTestAdminUser());
		Errors errors = getErrorObject(item);
		
		eAssetTagValidator.validate(targetMap, errors);
		assertTrue(errors.hasErrors());
		
		jmockContext.assertIsSatisfied();
	}
		
	//------------------ Private methods go here ------------------

}
