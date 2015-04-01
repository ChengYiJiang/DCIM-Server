/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathNotFoundException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.dao.FieldDetailsDAO;
import com.raritan.tdz.field.dao.FieldsFinderDAO;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.util.CustomFieldsHelper;
import com.raritan.tdz.validator.RequiredFieldValidatorTemplate;

import flex.messaging.log.Log;


/**
 * @author prasanna
 *
 */
public class ItemRequiredFieldValidator extends RequiredFieldValidatorTemplate implements Validator {
	
	private FieldsFinderDAO dao;
	
	private FieldDetailsDAO fieldDetailsDAO;
	
	// Fields not required for VMs
	private final List<String> vmNonRequiredFields = Arrays.asList("cmbMake", "cmbModel","tiSerialNumber","tiAssetTag","tieAssetTag");
	
	
	public ItemRequiredFieldValidator(FieldsFinderDAO dao, FieldDetailsDAO fieldDetailsDAO){
		this.dao = dao;
		this.fieldDetailsDAO = fieldDetailsDAO;
	}

	@Override
	public void validate(Object target, Errors errors) {
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> targetMap = (Map<String,Object>)target;

			Object itemDomainObject = targetMap.get(errors.getObjectName());
			
			validateRequiredField(itemDomainObject, errors, "ItemValidator.fieldRequired");
		} catch (DataAccessException e) {
			if (Log.isDebug())
				e.printStackTrace();
			errors.reject("ItemValidator.invalidItem");
		} catch (ClassNotFoundException e) {
			if (Log.isDebug())
				e.printStackTrace();
			errors.reject("ItemValidator.invalidItem");
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#customRequiredFieldValidate(com.raritan.tdz.field.domain.FieldDetails, java.lang.Object, org.springframework.validation.Errors, java.lang.String)
	 */
	@Override
	protected boolean customRequiredFieldValidate(FieldDetails fieldDetail,
			Object target, Errors errors, String errorCode) {
		Item item = (Item)target;
		String remoteReference;
		final long subClass = item.getSubclassLookup() != null ? item.getSubclassLookup().getLkpValueCode() : 0;
		try {
			// Get the remoteRef for the UiComponentId
			remoteReference = rulesProcessor
					.getRemoteRef(fieldDetail.getField().getUiComponentId());
		} catch (JXPathNotFoundException e) {
			return true;
		}

		String remoteType = remoteRef.getRemoteType(remoteReference);
		String remoteAlias = remoteRef.getRemoteAlias(remoteReference,
				RemoteRefConstantProperty.FOR_VALUE);
	
		// Special handling for custom fields
		if (CustomFieldsHelper.isCustomField(fieldDetail.getField().getUiComponentId())) {
			if (!CustomFieldsHelper.validateRequired( item, fieldDetail.getField() )) {
				Object[] errorArgs = { fieldDetail.getField().getCustomLku().getLkuValue(), item.getItemName() };
				errors.rejectValue(remoteAlias,
						errorCode, errorArgs,
						"Required field");
			}
			return true;
		}
		
		// Special handling for VMs
		if (subClass == SystemLookup.SubClass.VIRTUAL_MACHINE) {
			if (vmNonRequiredFields.contains(fieldDetail.getField().getUiComponentId())) return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#getFieldsDetailList(java.lang.Object)
	 */
	@Override
	protected List<FieldDetails> getFieldsDetailList(Object target) {
		Item item = (Item) target;
		//First get the field ids
		List<Long> fieldIds = dao.findFieldIdsByViewId("itemView");
		
		//Now get the field detail ids
		List<FieldDetails> fieldDetails = new ArrayList<FieldDetails>();
		
		if (item.getClassLookup() != null){
			/*for (Long fieldId:fieldIds){
				fieldDetails.addAll(dao.findFieldDetailsByFieldIdAndClass(fieldId, item.getClassLookup().getLkpValueCode()));
			}*/
			// List<FieldDetails> fds = dao.findFieldsDetailsByFieldIdAndClass(fieldIds, item.getClassLookup().getLkpValueCode());
			List<FieldDetails> fds = fieldDetailsDAO.getFieldDetails(fieldIds, item.getClassLookup().getLkpValueCode());
			fieldDetails.addAll(fds);
		}
		
		return fieldDetails;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidatorTemplate#getErrorArgs(com.raritan.tdz.field.domain.FieldDetails, java.lang.Object, com.raritan.tdz.validator.RequiredFieldValidatorTemplate.errorCondition)
	 */
	@Override
	protected Object[] getErrorArgs(FieldDetails fieldDetail, Object target,
			errorCondition errorCondition) {
		
		Item item = (Item)target;
		String uiComponentId = fieldDetail.getField()
				.getUiComponentId();
		
		if (errorCondition.equals(errorCondition.CLIENT_VALUE_EMPTY) 
				|| errorCondition.equals(errorCondition.CLIENT_VALUE_EMPTY)){
			
			Object[] errors = {getUiFieldName(uiComponentId),item.getItemName()};
			return errors;
		} else {
			Object[] errors = {getUiFieldName(uiComponentId),item.getItemName()};
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

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}



}
