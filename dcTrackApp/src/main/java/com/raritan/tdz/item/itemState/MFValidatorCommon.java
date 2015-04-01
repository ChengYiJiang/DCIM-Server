/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * @author prasanna
 *
 */
public class MFValidatorCommon implements
		MandatoryFieldStateValidator {
	
	protected RulesProcessor rulesProcessor;
	protected RemoteRef remoteRef;
	protected SessionFactory sessionFactory;
	
	//This map is used to check the mandatory fields. The key is either uiComponentId
	//or a Remote reference id. The value is the default label of the field to be
	//displayed for user.
	
	
	private Map<String,MandatoryFieldValidatorRangeCheck> mandatoryFields = new HashMap<String, MandatoryFieldValidatorRangeCheck>();
	
	
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

	public Map<String, MandatoryFieldValidatorRangeCheck> getMandatoryFields() {
		return mandatoryFields;
	}

	public void setMandatoryFields(Map<String, MandatoryFieldValidatorRangeCheck> mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
	}
	

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	

	public MFValidatorCommon(RulesProcessor rulesProcessor, RemoteRef remoteRef, SessionFactory sessionFactory){
		this.rulesProcessor = rulesProcessor;
		this.remoteRef = remoteRef;
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.MandatoryFieldStateValidator#validateMandatoryFields(com.raritan.tdz.domain.Item, org.springframework.validation.Errors)
	 */
	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode, Errors errors, String errorCodePostFix, Request request)
			throws DataAccessException, ClassNotFoundException {
		if (mandatoryFields == null) return;
		
		LksData newStatusLksData = SystemLookup.getLksData(sessionFactory.getCurrentSession(), newStatusLkpValueCode);
		
		//Next validate the mandatory fields for the item state
		for (Map.Entry<String, MandatoryFieldValidatorRangeCheck> mandatoryFieldEntry:mandatoryFields.entrySet()){
			String mandatoryField = mandatoryFieldEntry.getKey();
			MandatoryFieldValidatorRangeCheck mfRangeCheck = mandatoryFields.get(mandatoryField);
			String mandatoryDefaultLabel = mfRangeCheck.getDefaultLabel();
			
			String remoteReference = null;
			try {
				// Get the remoteRef for the mandatory field if exist
				remoteReference = rulesProcessor
						.getRemoteRef(mandatoryField);
			} catch (JXPathNotFoundException e) {
				remoteReference = mandatoryField;
			}
			
			String remoteType = remoteRef.getRemoteType(remoteReference);
			
			if (remoteType != null){
				String errorCodePrefix = mfRangeCheck.getErrorCode();
				StringBuffer errorCodeBuffer = new StringBuffer();
				if (errorCodePrefix == null) {
					errorCodeBuffer.append("ItemValidator.mandatoryForState"); 
					errorCodeBuffer.append(".");
					errorCodeBuffer.append(errorCodePostFix);
				} else {
					errorCodeBuffer.append(errorCodePrefix);
					errorCodeBuffer.append(".");
					errorCodeBuffer.append(errorCodePostFix);
				}
				
				String errorCode = errorCodeBuffer.toString();
				
				String remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE);
				// Look at the item object to see if the field is filled
				// If not generate an error
				JXPathContext jc = JXPathContext.newContext(item);
				String xpath = getFieldTrace(remoteType, remoteAlias).replace(
						".", "/");
				String uiFieldName = "";
				try {
					uiFieldName = getUiFieldName(mandatoryField);
					if (uiFieldName == null) uiFieldName = mandatoryDefaultLabel;
				} catch (HibernateException e){
					uiFieldName = mandatoryDefaultLabel;
				}
				String itemStatus = newStatusLksData != null ? newStatusLksData.getLkpValue() :"<Unknown>";
				String itemName = item.getItemName() != null ? item.getItemName() : "<Unknown>";
				if (xpath != null && !xpath.isEmpty()) {
					try {
						Object clientValue = jc.getValue(xpath) != null ? jc
								.getValue(xpath) : null;
						
						if (clientValue == null) {
							Object[] errorArgs = { uiFieldName, itemName, itemStatus, 
									(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null };
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Mandatory field");
						} else if (!mfRangeCheck.checkRange(clientValue)) {
							Object[] errorArgs = { uiFieldName, itemName, itemStatus, 
									(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null };
							errors.rejectValue(remoteAlias,
									errorCode, errorArgs,
									"Mandatory field");
						}
					} catch (JXPathNotFoundException e) {
						Object[] errorArgs = { uiFieldName, itemName, itemStatus, 
								(null != request) ? request.getRequestNo() : null,  (null != request) ? request.getDescription() : null };
						errors.rejectValue(remoteAlias,
								errorCode, errorArgs,
								"Mandatory field");
					}
				}
				
			}
		}

	}
	
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
		objectTrace.addHandler("upsBank.*", new ParentTracerHandler());
		objectTrace.addHandler("cracNwGrpItem.*", new ParentTracerHandler());
		
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString();
		return trace;
	}
	
	private String getUiFieldName(String uiComponentId) {
		String uiFieldName = "";

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Fields.class);
		criteria.setProjection(Projections.property("defaultName"));
		criteria.add(Restrictions.eq("uiComponentId", uiComponentId));

		uiFieldName = (String) criteria.uniqueResult();

		return uiFieldName;
	}

}
