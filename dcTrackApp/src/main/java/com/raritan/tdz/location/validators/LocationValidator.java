/**
 * 
 */
package com.raritan.tdz.location.validators;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.beanvalidation.home.BeanValidationHome;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.UniqueValidator;

/**
 * @author prasanna
 *
 */
public class LocationValidator implements Validator {
	
	@Autowired
	private LocationRequiredFieldValidator requiredFieldValidator;
	
	@Autowired
	private UniqueValidator uniqueValidator;
	
	@Autowired
	private UserLookupFinderDAO userLookupDAO;
	
	@Autowired
	private BeanValidationHome beanValidationHome;
	
	
	
	private final double maxArea = 100000.0;
	
	private final String USA = "United States";
	
	private final String USA_STATE = "USA_STATE";

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return DataCenterLocationDetails.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		DataCenterLocationDetails locationDetails = (DataCenterLocationDetails) target;
		
		//----- PLEASE DO NOT PUT ANY VALIDATION CODE BEFORE PERFORMING BEAN VALIDATION ---------
		
		//Validate the bean
		errors.addAllErrors(beanValidationHome.validate(locationDetails));
		
		//If we have validation errors in the bean, then it is no point in proceeding.
		if (errors.hasErrors()){
			return;
		}
		
		//----------------
		
		
		//Validate the drawingNorth has correct values
		validateDrawingNorth(locationDetails, errors);
		
		
		//Validate Required
		try {
			requiredFieldValidator.validateRequiredField(target, errors, "locationValidator.fieldRequired");
		} catch (DataAccessException e) {
			errors.reject("locationValidator.invalidLocation");
		} catch (ClassNotFoundException e) {
			errors.reject("system.error");
		}
		
		//Validate Location Name is unique
		if (locationDetails.getDcName() != null)
			validateUniqueness(errors, locationDetails, "dcName", locationDetails.getDcName(), "locationValidator.uniqueName");
		
		//Validate Location Code is unique
		if (locationDetails.getCode() != null)
			validateUniqueness(errors, locationDetails, "code", locationDetails.getCode(), "locationValidator.uniqueCode");
		
		//Location Area must be > 0 and < 1,000,000
		if (locationDetails.getArea() != null && (locationDetails.getArea() < 0 || locationDetails.getArea() > maxArea)){
			Object[] errorArgs = { locationDetails.getArea(), locationDetails.getCode()};
			errors.reject("locationValidator.areaExceeds", errorArgs, "Area must be in range of 0 to 1,000,000");
		}
		
		//Validate country against the country list
		validateCountry(locationDetails,errors);
		
		//Validate state if country is "United States"
		validateState(locationDetails,USA, USA_STATE, errors);
		
	}

	private void validateDrawingNorth(
			DataCenterLocationDetails locationDetails, Errors errors) {
		if (locationDetails.getDwgNorth() != null && !locationDetails.getDwgNorth().isEmpty()){
			if (!locationDetails.getDwgNorth().equals("1")
					&& !locationDetails.getDwgNorth().equals("3")
					&& !locationDetails.getDwgNorth().equals("5")
					&& !locationDetails.getDwgNorth().equals("7")){
				Object[] errorArgs = {locationDetails.getDwgNorth(), locationDetails.getCode() };
				errors.reject("locationValidator.invalidDwgNorth",errorArgs,"This is a invalid drawing north parameter!");
			}
		}
		
	}

	private void validateState(DataCenterLocationDetails locationDetails,
			String country, String stateLkuType, Errors errors) {
		if (locationDetails.getDcLocaleDetails() != null && locationDetails.getDcLocaleDetails() != null &&
				locationDetails.getDcLocaleDetails().getCountry() != null && locationDetails.getDcLocaleDetails().getCountry().equalsIgnoreCase(USA)){
			List<LkuData> stateLkuList = userLookupDAO.findByLkpType(stateLkuType);
			boolean found = false;
			for (LkuData stateLku:stateLkuList){
				if (stateLku.getLkuAttribute() != null && (stateLku.getLkuValue().equalsIgnoreCase(locationDetails.getDcLocaleDetails().getState()))	){
					found = true;
					break;
				}
			}
			
			if (!found){
				//If the state is not provided, it is okay since it is not a required field.
				if (locationDetails.getDcLocaleDetails().getState() != null && !locationDetails.getDcLocaleDetails().getState().isEmpty()){
					Object[] errorArgs = {locationDetails.getDcLocaleDetails().getState(),locationDetails.getDcLocaleDetails().getCountry() };
					errors.reject("locationValidator.invalidState",errorArgs,"This state does not exist!");
				}
			}
		}
		
	}

	private void validateCountry(DataCenterLocationDetails locationDetails,
			Errors errors) {
		//Get the country list
		List<LkuData> countryLkuList = userLookupDAO.findByLkpType("COUNTRY");
		
		boolean found = false;
		for (LkuData countryLku:countryLkuList){
			if (countryLku.getLkuValue().equalsIgnoreCase(locationDetails.getDcLocaleDetails().getCountry())){
				found = true;
				break;
			}
		}
		
		if (!found){
			Object[] errorArgs = {locationDetails.getDcLocaleDetails().getCountry() };
			errors.reject("locationValidator.invalidCountry",errorArgs,"This country does not exist!");
		}
	}

	private void validateUniqueness(Errors errors,
			DataCenterLocationDetails locationDetails, String entityProperty,
			String value, String errorCode) {
		try {
			boolean unique = false;
			if (locationDetails.getDataCenterLocationId() != null && locationDetails.getDataCenterLocationId() > 0){
				unique = 
					uniqueValidator.isUnique("com.raritan.tdz.domain.DataCenterLocationDetails", entityProperty, 
							value, null, -1L, "dataCenterLocationId", locationDetails.getDataCenterLocationId());
			}
			else {
				unique = 
						uniqueValidator.isUnique("com.raritan.tdz.domain.DataCenterLocationDetails", entityProperty, 
								value, null, -1L, null, null);
			}
			
			if (!unique){
				Object[] errorArgs = { value };
				errors.reject(errorCode, errorArgs, "Location with this name/code already exists");
			}
		} catch (DataAccessException e) {
			errors.reject("locationValidator.invalidLocation");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			errors.reject("system.error");
		}
	}

}
