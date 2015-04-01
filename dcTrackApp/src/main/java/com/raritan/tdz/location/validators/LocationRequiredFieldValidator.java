/**
 * 
 */
package com.raritan.tdz.location.validators;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.field.dao.FieldsFinderDAO;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.validator.RequiredFieldValidatorTemplate;


/**
 * @author prasanna
 *
 */
public class LocationRequiredFieldValidator extends
		RequiredFieldValidatorTemplate {
	
	private FieldsFinderDAO dao;
	
	public LocationRequiredFieldValidator(FieldsFinderDAO dao){
		this.dao = dao;
	}
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#customRequiredFieldValidate(com.raritan.tdz.field.domain.FieldDetails, java.lang.Object, org.springframework.validation.Errors, java.lang.String)
	 */
	@Override
	protected boolean customRequiredFieldValidate(FieldDetails fieldDetail,
			Object target, Errors errors, String errorCode) {
		// No custom validation so far...
		return false;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#getFieldsDetailList(java.lang.Object)
	 */
	@Override
	protected List<FieldDetails> getFieldsDetailList(Object target) {
		//First get the field ids
		List<Long> fieldIds = dao.findFieldIdsByViewId("locationView");
		
		//Now get the field detail ids
		List<FieldDetails> fieldDetails = new ArrayList<FieldDetails>();
		
		for (Long fieldId:fieldIds){
			fieldDetails.addAll(dao.findFieldDetailsByFieldId(fieldId));
		}
		
		return fieldDetails;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#getErrorArgs(com.raritan.tdz.field.domain.FieldDetails, java.lang.Object, com.raritan.tdz.validator.RequiredFieldValidatorTemplate.errorCondition)
	 */
	@Override
	protected Object[] getErrorArgs(FieldDetails fieldDetail, Object target,
			errorCondition condition) {
		DataCenterLocationDetails locations = (DataCenterLocationDetails)target;
		
		String uiComponentId = fieldDetail.getField()
				.getUiComponentId();
		
		if (condition.equals(errorCondition.CLIENT_VALUE_EMPTY) 
				|| condition.equals(errorCondition.CLIENT_VALUE_EMPTY)){
			
			Object[] errors = {getUiFieldName(uiComponentId),locations.getCode()};
			return errors;
		} else {
			Object[] errors = {getUiFieldName(uiComponentId),locations.getCode()};
			return errors;
		}
	}
	
	private String getUiFieldName(String uiComponentId) {
		String uiFieldName = "";
		
		List<String> uiFieldNameList = dao.findUiFieldName(uiComponentId);
		
		if (uiFieldNameList.size() == 1){
			uiFieldName = uiFieldNameList.get(0);
		}

		return uiFieldName;
	}
}
