package com.raritan.tdz.location.home;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.MapBindingResult;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.dao.GenericDAOLoader;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.JXPathAbstractFactory;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.ValueIDToDomainAdaptor;
import com.raritan.tdz.validator.FieldValidator;

public class LocationDomainAdaptor implements ValueIDToDomainAdaptor {
	private static final String UIID_LOCATION_NAME = "tiLocationName";
	private MapBindingResult validationErrors = null;
	private RulesProcessor processor;
	private RemoteRef remoteRef;
	private MessageSource messageSource;
	private GenericDAOLoader genericDAOLoader;
	
	private FieldValidator fieldValidator;

	private final Logger log = Logger.getLogger(LocationDomainAdaptor.class);
	
	@Autowired
	private FieldHome fieldHome;
	
	public LocationDomainAdaptor( RulesProcessor processor, RemoteRef remoteRef,
			MessageSource messageSource){
		this.processor = processor;
		this.remoteRef = remoteRef;
		this.setMessageSource(messageSource);
	}
	
	private void changeCodeToUpperCase(DataCenterLocationDetails location) {
		String name = location.getCode();
		if (name != null) {
			location.setCode(name.toUpperCase(Locale.getDefault()));
		}
	}

	@Override
	public Object convert(Object location, List<ValueIdDTO> valueIdDTOList,
			String unit) throws BusinessValidationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ClassNotFoundException,
			DataAccessException {
		Map<String, String> errorMap = new HashMap<String, String>();
		validationErrors = new MapBindingResult(errorMap, location.getClass().getName());

		processValueIdDtoList( location, valueIdDTOList, unit);
		changeCodeToUpperCase((DataCenterLocationDetails)location);
		
		return location;
	}

	public String getFieldTrace(String remoteType, String fieldName) throws ClassNotFoundException{
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString().replace(".", "/");
		return trace;
	}
	
	
	private String getTraceForValue(String remoteReference, String remoteType)
			throws ClassNotFoundException {
		String remoteAlias;
		String trace;
		remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE);
		trace = getFieldTrace(remoteType,remoteAlias);
		return trace;
	}
	private Object initializeValue(String remoteReference, String remoteType,
			Object value) throws ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		if (value != null && value.equals("")){
			value = null;
		}
		return value;
	}



	private void processValueIdDtoList (Object location, List<ValueIdDTO> valueIdDTOList, String unit)
			throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, BusinessValidationException, DataAccessException {
 		
		for (ValueIdDTO dto: valueIdDTOList){
 			if (dto == null) continue;
 			
			String remoteReference = processor.getRemoteRef(dto.getLabel());
			String remoteType = remoteRef.getRemoteType(remoteReference);

			String remoteId = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
			String trace = null;
			Object value = dto.getData();
			

			//if this is a lookup (has remoteId), load data.
			//UIID_LOCATION_NAME is special since it has id, but is not lookup
			if (remoteId != null && !dto.getLabel().equals(UIID_LOCATION_NAME)){
				trace = getTraceForLookup(remoteType, remoteId);
				if (dto.getData() != null){
					Long id = getId(dto.getData());
					value = genericDAOLoader.load(remoteType, remoteId, id);
				} else {
					value = null;
				}
			}
			else {
				trace = getTraceForValue(remoteReference, remoteType);
				value = initializeValue(remoteReference, remoteType, value);

				fieldValidator.validate(dto.getLabel(),remoteReference,remoteType,value, validationErrors);
				value = convertUnits(remoteReference, value, unit);
			}

			setValue( location, trace, value);
		}
		setDefaultValues((DataCenterLocationDetails)location);
	}

	private void setDefaultValues(DataCenterLocationDetails location) throws ClassNotFoundException{
		
		//Get all uiComponents that have default values
		List<UiComponent> components = processor.getAllComponentsWithDefaultValues();
		
		for (UiComponent component: components){
			String uiId = component.getUiId();
			
			String remoteReference = processor.getRemoteRef(uiId);
			String remoteType = remoteRef.getRemoteType(remoteReference);
			String remoteAliasForValue = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE);
			String remoteAliasForId = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
			
			if( remoteAliasForId != null ){
				//TODO: Implement when needed
				Assert.isTrue(false);
			}else if (remoteAliasForValue != null){
				String trace = getFieldTrace(remoteType,remoteAliasForValue);
				
				if (trace != null){
					if (trace.contains("/")){
						trace = trace.substring(0, trace.lastIndexOf("/"));
					}
					JXPathContext jc = JXPathContext.newContext(location);
					jc.setFactory(new JXPathAbstractFactory());
					Object existingValue = jc.getValue(trace);
					//set default only if there is no existing value and default exists
					if (existingValue == null && component.getUiValueIdField() != null && component.getUiValueIdField().getUiDefaultValue() != null 
							&& component.getUiValueIdField().getUiDefaultValue() != null
							&& component.getUiValueIdField().getUiDefaultValue().size() > 0){
						
						String defaultValue = component.getUiValueIdField().getUiDefaultValue().get(0).getValue();				
						jc.createPathAndSetValue(trace,defaultValue); 
					}
				}
			}
			
		}
	}
	
	private String getTraceForLookup(String remoteType, String remoteId)
			throws ClassNotFoundException {
		String trace;
		trace = getFieldTrace(remoteType,remoteId);
		if (trace.contains("/")){
			trace = trace.substring(0, trace.lastIndexOf("/"));
		}
		return trace;
	}

	private void setValue(Object location, String trace, Object value) {
		if (trace != null && location != null && !validationErrors.hasErrors()){
			JXPathContext jc = JXPathContext.newContext(location);
			try{
				jc.setFactory(new JXPathAbstractFactory());
				jc.createPathAndSetValue(trace, value);
			// FIXME: Hack for RESTAPI. Avoid catching exceptions!
			}catch(JXPathException e){
				log.info("## WARNING: Got JXPathException for trace=" + trace + ", value:" + value);
			}
		}
	}

	private Object convertUnits(String remoteReference, Object value, String unit) {
		UnitConverterIntf unitConverter = remoteRef.getRemoteRefUnitConverter(remoteReference);
		
		if (!validationErrors.hasErrors()){
			if (unitConverter != null) {
				value = unitConverter.normalize(value, unit);
			}
		}
		return value;
	}

	private Long getId(Object data) {
		Long id = null;
		
		if (data instanceof Integer){
			id = ((Integer) data).longValue();
		}else if (data instanceof Long){
			id = (Long)data;
		}else if (data instanceof String) {
		    id = Long.parseLong((String)data);
		}
		return id;
	}

	@Override
	public MapBindingResult getValidationErrors() {
		return validationErrors;
	}

	public FieldValidator getFieldValidator() {
		return fieldValidator;
	}

	public void setFieldValidator(FieldValidator fieldValidator) {
		this.fieldValidator = fieldValidator;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public FieldHome getFieldHome() {
		return fieldHome;
	}

	public void setFieldHome(FieldHome fieldHome) {
		this.fieldHome = fieldHome;
	}

	public GenericDAOLoader getGenericDAOLoader() {
		return genericDAOLoader;
	}

	public void setGenericDAOLoader(GenericDAOLoader genericDAOLoader) {
		this.genericDAOLoader = genericDAOLoader;
	}

}
