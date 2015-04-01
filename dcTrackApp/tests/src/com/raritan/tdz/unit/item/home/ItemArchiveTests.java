package com.raritan.tdz.unit.item.home;

 import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.item.validators.ItemObjectValidatorsFactoryImpl;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.item.ItemMock;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UserMock;

public class ItemArchiveTests extends UnitTestBase {	 
	@Autowired
	private ItemObjectValidatorsFactoryImpl itemObjectValidatorsFactory;

	@Autowired
	private ResourceBundleMessageSource messageSource;
	  
    @Autowired 
	protected ItemMock itemMock;
    
	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * Pass Null Item to validator 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:  
	 * validations pass moving Item to Archived state
	 * @throws Throwable
	 */
	
	@Test
	public final void adminArchiveDeviceItemPassingNullItemToValidator() throws Throwable {
		final Item item = itemMock.createRackableStandardDeviceItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		Map<String, Object> targetMap = getValidatorTargetMap(null, userInfo, "archive" ); //<- passing null for item in targetMap
		try {
			// Test your function
			Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
			v.validate(targetMap, errors);
	
			if (errors.hasErrors()) {
				List<ObjectError> objectErrors = errors.getAllErrors();
				for (ObjectError error: objectErrors){
					String msg = messageSource.getMessage(error, Locale.getDefault());
					System.out.println (getMethodName() + " : Result : " + msg);
				}
				assertTrue(false);			
			} else {
				assertTrue(true);
			}
		} catch (Throwable e) {
			if (e.getMessage().equals("Item cannot be null")) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		}
	}

	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * Pass Null userInfo to validator 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:  
	 * validations pass moving Item to Archived state
	 * @throws Throwable
	 */
	
	@Test
	public final void adminArchiveDeviceItemPassingNullUserInfoToValidator() throws Throwable {
		final Item item = itemMock.createRackableStandardDeviceItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = null; //<- passing null for UserInfo in targetMap
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" ); 
		try {
			// Test your function
			Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
			v.validate(targetMap, errors);
	
			if (errors.hasErrors()) {
				List<ObjectError> objectErrors = errors.getAllErrors();
				for (ObjectError error: objectErrors){
					String msg = messageSource.getMessage(error, Locale.getDefault());
					System.out.println (getMethodName() + " : Result : " + msg);
				}
				assertTrue(false);			
			} else {
				assertTrue(true);
			}
		} catch (Throwable e) {
			if (e.getMessage().equals("UserInfo cannot be null")) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		}
	}

	
	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:  
	 * validations pass moving Item to Archived state
	 * @throws Throwable
	 */
	
	@Test
	public final void adminArchiveDeviceItemInPlannedState() throws Throwable {
		final Item item = itemMock.createRackableStandardDeviceItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(false);			
		} else {
			assertTrue(true);
		}
		
	}
	
	/**
	 * This test simulates condition of Viewer user changing status of an item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveDeviceItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableStandardDeviceItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (has connections)
	 * 
	 * Expected result:  
	 * validations pass moving Item to Archived state
	 * @throws Throwable
	 */
	
	@Test
	public final void adminArchiveDeviceItemInPlannedStateWithConnection() throws Throwable {
		final Item item = itemMock.createRackableStandardDeviceItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);			
		} else {
			assertTrue(false);
		}
		
	}


	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveCabinetItemInPlannedState() throws Throwable {
		Item item = itemMock.createCabinetItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		MapBindingResult errors = getErrorObject (CabinetItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getStorageValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.			
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of ADMIN user changing status of an item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state because viewer does not 
	 * have permission to move this item to archive state. 
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveCabinetItemInPlannedState() throws Throwable {
		Item item = itemMock.createCabinetItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		MapBindingResult errors = getErrorObject (CabinetItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getStorageValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of ADMIN user changing status of an container item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create cabinet item
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveCabinetContainerItemInPlannedState() throws Throwable {
		Item item = itemMock.createCabinetContainerItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		MapBindingResult errors = getErrorObject (CabinetItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getStorageValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.			
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of ADMIN user changing status of an container item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Cabinet container item
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state because viewer does not 
	 * have permission to move this item to archive state. 
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveCabinetContainerItemInPlannedState() throws Throwable {
		Item item = itemMock.createCabinetContainerItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );

		MapBindingResult errors = getErrorObject (CabinetItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getStorageValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	
	/**
	 * This test simulates condition of Admin changing status of an Non Rackable item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Non-Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNRDeviceItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableStandardDevice(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(false);
		} else {
			// Test failed.
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of viewer changing status of an Non Rackable item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Non-Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (No connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNRDeviceItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableStandardDevice(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed. with one logical data port.
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Non Rackable item from Planned -> Archived
	 * 
	 * Precondition:
	 * Create Standard Non-Rackable Device item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNRDeviceItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableStandardDevice(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Device Chassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Standard Device Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveChassisItemInPlannedState() throws Throwable {
		Item item = itemMock.createDeviceRackableChassis(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an deviceChassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Standard Device Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveChassisItemInPlannedState() throws Throwable {
		Item item = itemMock.createDeviceRackableChassis(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Device Chassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Standard Device Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveChassisItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createDeviceRackableChassis(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Network Chassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNetworkChassisItemInPlannedState() throws Throwable {
		Item item = itemMock.createNetworkRackableChassis(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an network Chassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNetworkChassisItemInPlannedState() throws Throwable {
		Item item = itemMock.createDeviceRackableChassis(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Network Chassis item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Chassis item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNetworkChassisItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNetworkRackableChassis(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Network Bladeitem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Blade item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNetworkBladeItemInPlannedState() throws Throwable {
		Item item = itemMock.createNetworkBlades(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an network Blade item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Bladeitem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNetworkBladeItemInPlannedState() throws Throwable {
		Item item = itemMock.createNetworkBlades(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Network Bladeitem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Network Bladeitem with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)createNetworkBladesInPlannedState
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNetworkBladeItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNetworkBlades(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an Device Blade item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Device Blade item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveDeviceBladeItemInPlannedState() throws Throwable {
		Item item = itemMock.createDeviceBlades(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an Device Blade item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Device Bladeitem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveDeviceBladeItemInPlannedState() throws Throwable {
		Item item = itemMock.createDeviceBlades(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an Device Blade item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create Device Blade item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveDeviceBladeItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createDeviceBlades(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an VM from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create VM item with data port
	 * Set Item state to Planned
	 * Item is not part of circuit (No connection)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveVMInPlannedState() throws Throwable {
		Item item = itemMock.createVM(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(false);
		} else {
			// Test failed.
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an VM from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create VM item with data port with connection
	 * Set Item state to Planned
	 * Item is part of circuit (has connection)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveVMInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createVM(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.VIRTUAL);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an FreestandingRackableStandardDevice item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreestandingRackableStandardDevice item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveFreestandingRackableStandardDeviceInPlannedState() throws Throwable {
		Item item = itemMock.createFreestandingRackableStandardDeviceItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an FreestandingRackableStandardDeviceitem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreestandingRackableStandardDevice with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveFreestandingRackableStandardDeviceInPlannedState() throws Throwable {
		Item item = itemMock.createFreestandingRackableStandardDeviceItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an FreestandingRackableStandardDevice from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreestandingRackableStandardDevice item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveFreestandingRackableStandardDeviceInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createFreestandingRackableStandardDeviceItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an ArchiveFreeStandingNetworkStackItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ArchiveFreeStandingNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveFreeStandingNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createFreestandingNetworkStackItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ArchiveFreeStandingNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ArchiveFreeStandingNetworkStackItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveFreeStandingNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createFreestandingRackableStandardDeviceItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ArchiveFreeStandingNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ArchiveFreeStandingNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveFreeStandingNetworkStackItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createFreestandingRackableStandardDeviceItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an RackableFixedNetworkStackItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRackableFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableFixedNetworkStackItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ArchiveFreeStandingNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ArchiveFreeStandingNetworkStackItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveRackableFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableFixedNetworkStackItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an RackableFixedNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRackableFixedNetworkStackItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createRackableFixedNetworkStackItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableFixedNetworkStackItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNonRackableFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedNetworkStackItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an NonRackableFixedNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedNetworkStackItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNonRackableFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedNetworkStackItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableFixedNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNonRackableFixedNetworkStackItemPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableFixedNetworkStackItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ZeroUFixedNetworkStackItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveZeroUFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroUFixedNetworkStackItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ZeroUFixedNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUFixedNetworkStackItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveZeroUFixedNetworkStackItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroUFixedNetworkStackItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ZeroUFixedNetworkStackItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUFixedNetworkStackItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminZeroUFixedNetworkStackItemPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createZeroUFixedNetworkStackItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an RackableFixedDataPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableFixedDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRackableFixedDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableFixedDataPanelItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an RackableFixedDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableFixedDataPanelItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveRackableFixedDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableFixedDataPanelItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an RackableFixedDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableFixedDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveRackableFixedDataPanelItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createRackableFixedDataPanelItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableFixedDataPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNonRackableFixedDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an NonRackableFixedDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedDataPanelItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNonRackableFixedDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableFixedDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableFixedDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveNonRackableFixedDataPanelItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	

	/**
	 * This test simulates condition of Admin changing status of an ZeroUDataPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveZeroUDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroUDataPanelItem(null, null);
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ZeroUDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUDataPanelItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveZeroUDataPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ZeroUDataPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveZeroUDataPanelItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.PASSIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an ZeroUDataPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveBuswayFixedItemInPlannedState() throws Throwable {
		Item item = itemMock.createBuswayFixedItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an BuswayFixedItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create BuswayFixedItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveBuswayFixedItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableFixedDataPanelItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an BuswayFixedItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create BuswayFixedItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveBuswayFixedItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createBuswayFixedItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	
	/**
	 * This test simulates condition of Admin changing status of an ZeroUDataPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUDataPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveWhipOutletItemInPlannedState() throws Throwable {
		Item item = itemMock.createWhipOutletItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an WhipOutletItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create WhipOutletItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveWhipOutletItemInPlannedState() throws Throwable {
		Item item = itemMock.createWhipOutletItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an WhipOutletItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create WhipOutletItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveWhipOutletItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createWhipOutletItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an ZeroURackPDUItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroURackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveZeroURackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroURackPDUItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ZeroURackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroURackPDUItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveZeroURackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroURackPDUItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ZeroURackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroURackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveZeroURackPDUItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createZeroURackPDUItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an RackableRackPDUItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableRackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRackableRackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableRackPDUItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an RackableRackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableRackPDUItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveRackableRackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableRackPDUItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an RackableRackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableRackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveRackableRackPDUItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createRackableRackPDUItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an NonRackableRackPDUItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableRackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNonRackableRackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableRackPDUItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an NonRackableRackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableRackPDUItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNonRackableRackPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableRackPDUItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableRackPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableRackPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveNonRackableRackPDUItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableRackPDUItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an RackableProbeItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRackableProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableProbeItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an RackableProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableProbeItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveRackableProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createRackableProbeItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an RackableProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RackableProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveRackableProbeItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createRackableProbeItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an ZeroUProbeItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveZeroUProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroUProbeItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an ZeroUProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUProbeItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveZeroUProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createZeroUProbeItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an ZeroUProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create ZeroUProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveZeroUProbeItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createZeroUProbeItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an NonRackableProbeItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveNonRackableProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableProbeItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an NonRackableProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableProbeItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveNonRackableProbeItemInPlannedState() throws Throwable {
		Item item = itemMock.createNonRackableProbeItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an NonRackableProbeItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create NonRackableProbeItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveNonRackableProbeItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createNonRackableProbeItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (ItItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an FreeStandingFixedFPDUItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreeStandingFixedFPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveFreeStandingFixedFPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createFreeStandingFixedFPDUItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an FreeStandingFixedFPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreeStandingFixedFPDUItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveFreeStandingFixedFPDUItemInPlannedState() throws Throwable {
		Item item = itemMock.createFreeStandingFixedFPDUItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an FreeStandingFixedFPDUItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create FreeStandingFixedFPDUItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveFreeStandingFixedFPDUItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createFreeStandingFixedFPDUItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
					
	/**
	 * This test simulates condition of Admin changing status of an LocalPanelBoardItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create LocalPanelBoardItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveLocalPanelBoardItemInPlannedState() throws Throwable {
		Item item = itemMock.createLocalPanelBoardItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an LocalPanelBoardItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create LocalPanelBoardItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveLocalPanelBoardItemInPlannedState() throws Throwable {
		Item item = itemMock.createLocalPanelBoardItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an LocalPanelBoardItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create LocalPanelBoardItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveLocalPanelBoardItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createLocalPanelBoardItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an RemotePaneltem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create RemotePaneltem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveRemotePaneltemInPlannedState() throws Throwable {
		Item item = itemMock.createRemotePaneltem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an LocalPanelBoardItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create LocalPanelBoardItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveRemotePaneltemInPlannedState() throws Throwable {
		Item item = itemMock.createRemotePaneltem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an LocalPanelBoardItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create LocalPanelBoardItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveRemotePaneltemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createRemotePaneltem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an BuswayPanelItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create BuswayPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveBuswayPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createBuswayPanelItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an BuswayPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create BuswayPanelItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveBuswayPanelItemInPlannedState() throws Throwable {
		Item item = itemMock.createBuswayPanelItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an BuswayPanelItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create BuswayPanelItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveBuswayPanelItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createBuswayPanelItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an UPSItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create UPSItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveUPSItemmInPlannedState() throws Throwable {
		Item item = itemMock.createUPSItem(null, null);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an UPSItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create UPSItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveUPSItemInPlannedState() throws Throwable {
		Item item = itemMock.createUPSItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an UPSItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create UPSItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveUPSItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createUPSItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
	
	/**
	 * This test simulates condition of Admin changing status of an CRACItem item from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create CRACItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * succesfully Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void adminArchiveCRACItemInPlannedState() throws Throwable {
		Item item = itemMock.createCRACItem(null, null);
		
		// create a mocked session user createCRACItemInPlannedState
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			// Test failed.
			assertTrue(false);
		} else {
			assertTrue(true);
		}
	}

	/**
	 * This test simulates condition of Viewer changing status of an CRACItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create CRACItem with one logical data port.
	 * Set Item state to Planned
	 * Item is not part of circuit (Without connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	@Test
	public final void viewerArchiveCRACItemInPlannedState() throws Throwable {
		Item item = itemMock.createCRACItem(null, null);

		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("ViewerId", "ViewerUser", UserAccessLevel.VIEWER);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}

	/**
	 * This test simulates condition of Admin changing status of an CRACItem from Planned -> Archived
	 * TA5672
 
	 * Precondition:
	 * Create CRACItem item with one logical data port.
	 * Set Item state to Planned
	 * Item is part of circuit (With connections)
	 * 
	 * Expected result:
	 * validations fail moving Item to Archived state  
	 * 
	 * @throws Throwable
	 * 
	 */
	
	@Test
	public final void adminArchiveCRACItemInPlannedStateWithConnection() throws Throwable {
		Item item = itemMock.createCRACItem(null, null);
		itemMock.addDataPortsToItem(item, SystemLookup.PortSubClass.ACTIVE);
		
		// create a mocked session user 
		UserInfo userInfo = UserMock.getTestAdminUser("AdminId", "AdminUser", UserAccessLevel.ADMIN);
		
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo, "archive" );
		
		MapBindingResult errors = getErrorObject (MeItem.class);
		
		// Test your function
		Validator v = itemObjectValidatorsFactory.getArchiveValidators(item);
		v.validate(targetMap, errors);

		if (errors.hasErrors()) {
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				System.out.println (getMethodName() + " : Result : " + msg);
			}
			assertTrue(true);
		} else {
			// Test failed.
			assertTrue(false);
		}
	}
											

}
