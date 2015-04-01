/**
 * 
 */
package com.raritan.tdz.location.validator;


import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.MapBindingResult;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.location.validators.LocationRequiredFieldValidator;
import com.raritan.tdz.location.validators.LocationValidator;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.UniqueValidator;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests.
 * 
 * @author Santo Rosario
 */
public class LocationValidatorTests extends TestBase {
	
	private LocationValidator locationValidator;
	
	private LocationRequiredFieldValidator locationRequiredFieldValidator;
	
	private UniqueValidator uniqueValidator;
	
	private MapBindingResult errors;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		locationValidator = (LocationValidator)ctx.getBean("locationValidator");
		locationRequiredFieldValidator = (LocationRequiredFieldValidator)ctx.getBean("locationRequiredFieldValidator");
		uniqueValidator = (UniqueValidator)ctx.getBean("uniqueValidator");
		
		Map<String, String> errorMap = new HashMap<String, String>();
		errors = new MapBindingResult(errorMap, "com.raritan.tdz.domain.DataCenterLocationDetails");
	}
	
	@Test
	public void testValidateRequiredFieldsSunny() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(3000L);
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == false);
	}
	
	
	@Test
	public void testValidateRequiredFieldsNoLocationName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(3000L);
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateRequiredFieldsNoLocationCode() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(3000L);
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateRequiredFieldsNone() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
	
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateRequiredFieldsNoCountry() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.setCode("SITE-A");
	
		location.setArea(3000L);
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateRequiredFieldsNoArea() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		
		locationRequiredFieldValidator.validateRequiredField(location, errors, "locationValidator.fieldRequired");
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateUniqueLocationName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		
		boolean unique = uniqueValidator.isUnique("com.raritan.tdz.domain.DataCenterLocationDetails", "dcName", location.getDcName(), null, -1L, null, null);
		
		assertTrue(unique == false);
	}
	
	@Test
	public void testValidateUniqueLocationCode() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE A");
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		
		boolean unique = uniqueValidator.isUnique("com.raritan.tdz.domain.DataCenterLocationDetails", "dcName", location.getCode(), null, -1L, null, null);
		
		assertTrue(unique == true);
	}

	@Test
	public void testValidateSunny() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}
	
	@Test
	public void testValidateRequiredFieldsMissing() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
		assertTrue(errors.hasFieldErrors("code"));
		assertTrue(errors.hasFieldErrors("dcName"));
		assertTrue(errors.hasFieldErrors("country"));
		assertTrue(errors.hasFieldErrors("area"));
		
	}
	
	@Test
	public void testValidateAreaLessThanZero() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(-5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateUniqueCode() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-A");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(-5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateUniqueName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("DEMO SITE F");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.setArea(-5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateValidCountryName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("united states");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}
	
	@Test
	public void testValidateInvalidCountryName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("us");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateNullStateName() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.getDcLocaleDetails().setState(null);
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}

	@Test
	public void testValidateNullStateBlank() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.getDcLocaleDetails().setState("");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}
	
	@Test
	public void testValidateNullDwgNorth() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.getDcLocaleDetails().setState("New Jersey");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}
	
	@Test
	public void testValidateInvalidDwgNorth() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.getDcLocaleDetails().setState("New Jersey");
		location.setDwgNorth("North");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == true);
	}
	
	@Test
	public void testValidateValidDwgNorth() throws DataAccessException, ClassNotFoundException{
		DataCenterLocationDetails location = new DataCenterLocationDetails();
		location.setDcLocaleDetails(new DataCenterLocaleDetails());
		
		location.setDcName("SITE ABC");
		location.setCode("SITE-ABC");
		location.getDcLocaleDetails().setCountry("United States");
		location.getDcLocaleDetails().setState("New Jersey");
		location.setDwgNorth("3");
		location.setArea(5L);
		locationValidator.validate(location, errors);
		
		assertTrue(errors.hasErrors() == false);
	}
}
