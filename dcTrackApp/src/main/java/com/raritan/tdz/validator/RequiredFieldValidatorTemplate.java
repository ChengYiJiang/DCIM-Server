/**
 * 
 */
package com.raritan.tdz.validator;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.CustomFieldsHelper;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;
import com.raritan.tdz.validator.RequiredFieldValidatorTemplate.errorCondition;

/**
 * @author prasanna
 * This template helps in calling out the common functionality for all 
 * required fields check and give you a chance to overwrite some of the 
 * specific functionality for required field validation.
 */
public abstract class RequiredFieldValidatorTemplate implements
		RequiredFieldValidator {
	
	protected enum errorCondition { CLIENT_VALUE_NULL, CLIENT_VALUE_EMPTY, JXPATH_ERROR }; 

	//Dont auto wire as we may need different rulesProcessor engines injected!
	protected RulesProcessor rulesProcessor;
	
	//Dont auto wire as we may need different remoteRef injected!
	protected RemoteRef remoteRef;

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}
	
	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.validator.RequiredFieldValidator#validateRequired(java.lang.Object, org.springframework.validation.Errors, java.lang.String)
	 */
	@Override
	public void validateRequiredField(Object target, Errors errors, String errorCode) throws DataAccessException, ClassNotFoundException {
		// First get all the fields for the given classLkpValue obtained from
		// the item object
		List<FieldDetails> fieldDetailsList = getFieldsDetailList(target);

		// Loop through the fields to get the UiComponentId
		for (FieldDetails fieldDetail : fieldDetailsList) {
			if (fieldDetail.getIsRequiedAtSave() == true) {
				String uiComponentId = fieldDetail.getField()
						.getUiComponentId();
				
				String remoteReference = null;

				try {
					// Get the remoteRef for the UiComponentId
					remoteReference = rulesProcessor
							.getRemoteRef(uiComponentId);
				} catch (JXPathNotFoundException e) {
					continue;
				}

				String remoteType = remoteRef.getRemoteType(remoteReference);
				String remoteAlias = remoteRef.getRemoteAlias(remoteReference,
						RemoteRefConstantProperty.FOR_VALUE);
				
				if (customRequiredFieldValidate(fieldDetail, target, errors, errorCode))
					continue;
				
				if (remoteType == null || remoteAlias == null)
					continue;

				// Look at the item object to see if the field is filled
				// If not generate an error
				JXPathContext jc = JXPathContext.newContext(target);
				String xpath = getFieldTrace(remoteType, remoteAlias).replace(
						".", "/");
				if (xpath != null && !xpath.isEmpty()) {
					try {
						String clientValue = jc.getValue(xpath) != null ? jc
								.getValue(xpath).toString() : null;

						if (clientValue == null) {
							Object[] errorArgs = getErrorArgs(fieldDetail, target, errorCondition.CLIENT_VALUE_NULL);
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Required field");
						} else if (clientValue.isEmpty()) {
							Object[] errorArgs = getErrorArgs(fieldDetail, target, errorCondition.CLIENT_VALUE_EMPTY);
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Required field");
						}
					} catch (JXPathNotFoundException e) {
						Object[] errorArgs = getErrorArgs(fieldDetail, target, errorCondition.JXPATH_ERROR);
						errors.rejectValue(remoteAlias,
								errorCode, errorArgs,
								"Required field");
					}
				}
			}
		}

	}
	
	//----------- Protected methods ----------------------
	/**
	 * Any custom Required field processing can be done here
	 * @param fieldDetail - This is the field detail object that can be used by impl
	 * @param target - This is the target object
	 * @param errors - Errors object will be passed in
	 * @param errorCode - Error code will be passed in
	 * @return - Let the template know if it needs to continue to process the next field or continue
	 *           processing the rest of the template code. If you dont have any code return false
	 */
	protected abstract boolean customRequiredFieldValidate(FieldDetails fieldDetail,
			Object target, Errors errors, String errorCode);
	
	/**
	 * Getting field details can vary between impl. This will give you a chance to do special cases
	 * @param target
	 * @return
	 */
	protected abstract List<FieldDetails> getFieldsDetailList(Object target);
	
	/**
	 * This is a way to get the error args to be used by the rejectValue error. You can customize to 
	 * what you want to implement.
	 * @param fieldDetail
	 * @param target
	 * @param errorCondition
	 * @return
	 */
	protected abstract Object[] getErrorArgs(FieldDetails fieldDetail, Object target, errorCondition errorCondition);

	//-------------------- Private methods --------------------
	private String getFieldTrace(String remoteType, String fieldName)
			throws ClassNotFoundException {
		// Create Alias
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue",
				new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue",
				new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode",
				new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode",
				new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString();
		return trace;
	}
	
	private String getUiFieldName(String uiComponentId) {
		//TODO: Add DAO code here
		return null;
	}

}
