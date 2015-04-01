package com.raritan.tdz.adapter;


import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dao.GenericDAOLoader;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.RestAPIUserIdTracerHandler;

/**
 * @author bozana
 * 
 */

public class DTOAdapterBase {

	private static final int Map = 0;
	private final Logger log = Logger.getLogger(DTOAdapterBase.class);
	public Logger getLog() {
		return log;
	}

	protected RulesProcessor rulesProcessor;
	protected RemoteRef remoteReference;
	protected ResourceBundleMessageSource messageSource;
	protected GenericDAOLoader genericDAOLoader;
	
	protected void throwBusinessValidationException (Object args[], String code) throws BusinessValidationException {
		String msg = messageSource.getMessage(code, args, null);	
		BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
		be.addValidationError( msg );
		be.addValidationError(code, msg);
		throw be;
	}

	/**
	 * Helper class for finding out entity, property and propertyId from uiId
	 * 
	 * @author bozana
	 * 
	 */
	private class EntityPropertyIdBuilder {
		private String entity;
		private String property;
		private String propertyId;
		private boolean validObject;

		public boolean isValid() {
			return validObject;
		}
		public String getEntity() {
			return entity;
		}
		public String getProperty() {
			return property;
		}
		public String getPropertyId() {
			return propertyId;
		}

		public EntityPropertyIdBuilder(){};

		/**
		 * helper method that builds EntityPropertyBuilder object knowing uiId
		 * It generates property, propertyId and entity
		 * 
		 * @param uiId
		 * @throws ClassNotFoundException
		 */
		public void buildObject(String uiId, boolean idRequired)
				throws ClassNotFoundException {
			validObject = false;

			propertyId = remoteReference.getRemoteId(rulesProcessor.getRemoteRef(uiId));
			if( propertyId == null && idRequired == true ){
				return;
			}
			property = remoteReference.getRemoteAlias(rulesProcessor.getRemoteRef(uiId), RemoteRef.RemoteRefConstantProperty.FOR_VALUE);

			entity = remoteReference.getRemoteType(rulesProcessor.getRemoteRef(uiId));

			if( entity == null || property == null ){
				log.error("Cannot find any property and entity corresponding to " + uiId);
				return;
			}

			Class<?> entityClass = Class.forName(entity);
			ObjectTracer oTracer = new ObjectTracer();
			addTracerHandlers(oTracer);

			List<Field> fields = oTracer.traceObject(entityClass, property);
			int numFields = fields.size();
			if( numFields < 1){
				Assert.isTrue(numFields > 0, "Tracer object failed to find property");
				return;
			}
			if(log.isDebugEnabled()){
				log.debug("== entityName=" + entity + ", entityProperty=" + property + ", entityPropertyId=" + propertyId);
				for( int i=0; i<numFields; i++){
					log.debug("field[" + i + "]=" + fields.get(i).getDeclaringClass().getName());
				}
			}
			Field correctField = fields.get(numFields-1);
			entity = correctField.getDeclaringClass().getName();

			if( correctField.getDeclaringClass().equals(LksData.class)){
				property = "lkpValue";
				propertyId = "lkpValueCode";
			}else if( correctField.getDeclaringClass().equals(LkuData.class)){
				property = "lkuValue";
				propertyId = "lkuId";
			}

			validObject = true;
			if( log.isDebugEnabled()){
				log.debug("== actual values: entity=" + entity + ", property=" + property + 
						", propertyId=" + propertyId + ", validObject=" + validObject);
			}
		}//buildObject
	}//class EntityPropertyIdBuilder

	public GenericDAOLoader getGenericDAOLoader() {
		return genericDAOLoader;
	}

	public void setGenericDAOLoader(GenericDAOLoader genericDAOLoader) {
		this.genericDAOLoader = genericDAOLoader;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public RemoteRef getRemoteReference() {
		return remoteReference;
	}

	public void setRemoteReference(RemoteRef remoteReference) {
		this.remoteReference = remoteReference;
	}

	private void addTracerHandlers(ObjectTracer oTracer) {

		oTracer.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		oTracer.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		oTracer.addHandler("^[a-z].*LkpValueCode", new LksDataValueTracerHandler());
		oTracer.addHandler("^[a-z].*LkuValueCode", new LkuDataValueTracerHandler());
		oTracer.addHandler("itemAdminUser.*", new RestAPIUserIdTracerHandler());
		oTracer.addHandler("parentItem.*", new ParentTracerHandler());
		oTracer.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		oTracer.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());

	}

	/**
	 *  Returns:
	 *  	- id converted from cmb if conversion was successful
	 *  	- null if there is no id (this is not cmb field)
	 *  	- -1L if this is cmb field, but string does not match any entry in DB, i.e. it is invalid
	 * @param uiId
	 * @param value
	 * @param additionalAlias
	 * @param additionalRestrictions
	 * @return
	 * @throws DataAccessException
	 * @throws HibernateException
	 * @throws ClassNotFoundException
	 */
	public Long getIdFromValue(String uiId, String value, java.util.Map<String, Object> additionalAlias, Map<String,Object> additionalRestrictions )
			throws DataAccessException, HibernateException,
			ClassNotFoundException {
		EntityPropertyIdBuilder builder = this.new EntityPropertyIdBuilder();
		builder.buildObject(uiId, true);
		if( ! builder.isValid()) return null;
		String entity = builder.getEntity();
		String property = builder.getProperty();
		String propertyId = builder.getPropertyId();
		Long id = null;
		if( entity != null && property != null && propertyId != null ){
			id = getId(entity, property, propertyId, value, additionalAlias, additionalRestrictions);
			if ( id == null ) id = new Long(-1L);
		}

		return id;
	}


	public void checkForErrors(MapBindingResult errors) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();

			StringBuilder allMsgs = new StringBuilder();
			int numMsgs = objectErrors.size();
			String errorCode = "";
			if( numMsgs > 0 ){
				allMsgs.append("[");
				for (ObjectError error: objectErrors){
					String msg = messageSource.getMessage(error, Locale.getDefault());
					if ( !allMsgs.toString().contains(msg) ) {
						allMsgs.append(msg);
					}
					if( numMsgs-- > 1 ) allMsgs.append(",");
					else errorCode=error.getCode();
				}
				allMsgs.append("]");
			}
			e.addValidationError(errorCode, allMsgs.toString());
			throw e;
		}
	}

	protected Long getId(String entity, String property, String propertyId, String value, 
			java.util.Map<String, Object> additionalAlias, Map<String, Object> additionalRestriction)
						throws ClassNotFoundException {
		return genericDAOLoader.getId(entity, property, propertyId, value, additionalAlias, additionalRestriction);
	}

}
