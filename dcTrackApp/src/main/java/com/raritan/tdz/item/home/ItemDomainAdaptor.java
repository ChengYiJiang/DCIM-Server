/**
 *
 */
package com.raritan.tdz.item.home;


 import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.MapBindingResult;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiDefaultType;
import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.home.UtilHome;
import com.raritan.tdz.item.rulesengine.ModelItemSubclassMap;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.DCTColumnsSchema;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.JXPathAbstractFactory;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.UserIdTracerHandler;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;
import com.raritan.tdz.util.ValueIDToDomainAdaptor;

/**
 * This is an adaptor that converts the valueIDDTO sent
 * by client to the domain object.
 * @author prasanna
 *
 */
public class ItemDomainAdaptor implements ValueIDToDomainAdaptor {

	private static Logger log = Logger.getLogger("ItemDomainAdaptor");
	
	private RulesProcessor processor;

	private RemoteRef remoteRef;
	private MessageSource messageSource;

	@Autowired
	private ModelItemSubclassMap modelItemSubclassMap;

	@Autowired
	private DCTColumnsSchema dctColumnsSchema;

	@Autowired
	private FieldHome fieldHome;

	@Autowired
	private ItemHome itemHome;

	@Autowired
	private UtilHome utilHome;
	
	@Autowired
	private ChassisHome chassisHome;

	private SessionFactory sessionFactory;
	private MapBindingResult validationErrors = null;
	
	private Map<String, ValueIDFieldToDomainAdaptor> supportingAdaptors;

	public void setSupportingAdaptors(Map<String, ValueIDFieldToDomainAdaptor> supportingAdaptors) {
		this.supportingAdaptors = supportingAdaptors;
	}
	
	private ValueIDFieldToDomainAdaptor getSupportingAdaptor(String uiId) {
		return supportingAdaptors.get(uiId);
	}

	private final String TI_CUSTOM_FIELD_KEY = "tiCustomField";
	
	private final String TI_SNMP_WRITE_COMM_STR_KEY = "tiSnmpWriteCommString";

	private static final ArrayList<String> uiIdIgnoreList = new ArrayList<String>() {
		{
			add("cmbMake");
			add("tiClass");
			add("tiMounting");
			add("tiWeight");
			add("tiRackUnits");
			add("tiDimension");
			add("_piqId");
		}
	};

	private static final int MAX_CUSTOM_FIELD_LENGTH = 255;
	
	public ItemDomainAdaptor(SessionFactory sessionFactory, RulesProcessor processor, RemoteRef remoteRef,
			MessageSource messageSource){
		this.sessionFactory = sessionFactory;
		this.processor = processor;
		this.remoteRef = remoteRef;
		this.messageSource = messageSource;
	}

	@Override
	public MapBindingResult getValidationErrors() {
		return validationErrors;
	}


	public Object convert(Object item, List<ValueIdDTO> valueIdDTOList, String unit)
			throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException, BusinessValidationException, DataAccessException{
		Map<String, String> errorMap = new HashMap<String, String>();
		validationErrors = new MapBindingResult(errorMap, item.getClass().getName());

		processValueIdDtoList( item, valueIdDTOList, unit);
		changeNameToUpperCase((Item)item);
		return item;
	}

	public ModelDetails getModelDetails(ValueIdDTO dtoModel) throws ClassNotFoundException
	{
		ModelDetails modelDetails = null;
		String remoteAlias = null;

		String remoteReference = processor.getRemoteRef(dtoModel.getLabel());
		String remoteType = remoteRef.getRemoteType(remoteReference);

		if (dtoModel.getData() instanceof Long || dtoModel.getData() instanceof Integer) {
			Long id;
			if (dtoModel.getData() instanceof Integer)
				id = ((Integer) dtoModel.getData()).longValue();
			else
				id = (Long) dtoModel.getData();

			if (id > 0) {
				remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
				modelDetails = (ModelDetails)loadData(remoteType, remoteAlias, id);
			}
		}

		return modelDetails;
	}

	private String getPSRedundancyFromDTO (List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals("cmbPSRedundancy") && dto.getData() != null) {
				return (String) dto.getData();
			}
		}
		return null;
	}

	private boolean getSkipValidationFromDTO (List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals("_tiSkipValidation") && dto.getData() != null) {
				return ((Boolean) dto.getData()).booleanValue();
			}
		}
		return false;
	}

	private Long getChassisIdFromDTO (List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals("cmbChassis") && dto.getData() != null) {
				if ( dto.getData() instanceof Integer) {
				    return ((Integer) dto.getData()).longValue();
				}
				else if ( dto.getData() instanceof Long ) {
					return (Long)dto.getData();
				}
				else if ( dto.getData() instanceof String ) {
					if (GlobalUtils.isNumeric((String)dto.getData())) {
						return Long.parseLong((String)dto.getData());
					}
				}
			}
		}
		return null;
	}

	private Integer getChassisFaceFromDTO (List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals("radioChassisFace") && dto.getData() != null) {
				if ( dto.getData() instanceof Long ) {
					return ((Long) dto.getData()).intValue();
				}
				else if ( dto.getData() instanceof String ) {
					if (GlobalUtils.isNumeric((String)dto.getData())) {
						return Integer.parseInt((String) dto.getData());
					}
				}
				else if ( dto.getData() instanceof Integer ) {
					return (Integer)dto.getData();
				}
			}
		}
		return (new Long(SystemLookup.ChassisFace.FRONT)).intValue();
	}

	private void processValueIdDtoList (Object item, List<ValueIdDTO> valueIdDTOList, String unit)
			throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, BusinessValidationException, DataAccessException {
 		Long classMountingFormFactorValue = null;
 		
		if (item != null) { 
			classMountingFormFactorValue = ((Item)item).getClassMountingFormFactorValue();
		} else {
			/* should never come here */
			throw new IllegalArgumentException("Invalid Item");
		}
		
		List<String> validUiIds = processor.getValidUiId(classMountingFormFactorValue);
		
		for (ValueIdDTO dto: valueIdDTOList){
 			if (dto == null) continue;

			if (dto != null && uiIdIgnoreList.contains(dto.getLabel()))
				continue;
			
			// If the uiId does not belong to the type of item being processed, skip it. 
			// This will avoid data corruption especially when using with REST API
			//if (!processor.isValidUiIdForUniqueValue(dto.getLabel(), classMountingFormFactorValue)) {
			if (!validUiIds.contains(dto.getLabel())) {
				continue;
			}
			
			String remoteReference = processor.getRemoteRef(dto.getLabel());
			String remoteType = remoteRef.getRemoteType(remoteReference);

			String remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
			String trace = null;
			Object value = dto.getData();

			UnitConverterIntf unitConverter = remoteRef.getRemoteRefUnitConverter(remoteReference);

			ValueIDFieldToDomainAdaptor adaptor = getSupportingAdaptor(dto.getLabel());
			if (null != adaptor) {
				adaptor.convert(item, dto);
				continue;
			}
			if (remoteAlias != null && !dto.getLabel().equals("tiName")) {
				trace = getFieldTrace(remoteType,remoteAlias);
				if (trace.contains("/"))
					trace = trace.substring(0, trace.lastIndexOf("/"));
				if (dto.getData() != null){
					Long id = null;
					if (dto.getData() instanceof Integer)
					   id = ((Integer)dto.getData()).longValue();
					else if (dto.getData() instanceof Long)
					   id = (Long)dto.getData();
  				    else if (dto.getData() instanceof String) {
  				    	if (dto.getLabel().equals("cmbSlotPosition") && ((String)dto.getData()).length() > 0 &&
  				    			!((String)dto.getData()).equals("-9")) {
  				    		
  				    		Long chassisIdL = getChassisIdFromDTO(valueIdDTOList);
  				    		long chassisId = chassisIdL != null ? chassisIdL : -1;
  				    		long chassisFace = getChassisFaceFromDTO(valueIdDTOList);
  				    		long slotNumber = chassisHome.getChassisSlotNumber(chassisId, chassisFace, (String)dto.getData());
  				    		if (-1 == slotNumber) {
	  				  			String code ="ItemValidator.invalidSlotPosition";
	  							String msg = messageSource.getMessage(code, null, null);
	  							BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
	  							ex.addValidationError( msg );
	  							ex.addValidationError(code, msg);
	  							throw ex;
  				    		}
  				    		id = slotNumber;
  				    	}
  				    	else {
  				    		if (GlobalUtils.isNumeric((String)dto.getData())) {
  				    			id = Long.parseLong((String)dto.getData());
  				    		}
  				    	}
  				    }

					value = loadData(remoteType, remoteAlias, id);
				} else {
					value = null;
				}
			} else if (dto.getLabel().equals(TI_CUSTOM_FIELD_KEY)) {
				convertCustomFieldsForAnItem(item, dto);
				continue;
			} 
			else {
				trace = getTraceForValue(remoteReference, remoteType);
				if (!dto.getLabel().equals(TI_SNMP_WRITE_COMM_STR_KEY)) {
					value = initializeValue(remoteReference, remoteType, value);
				}

				//This will collect all validation errors and will be used elsewhere
				validate(dto.getLabel(),remoteReference,remoteType,value);


				if (!validationErrors.hasErrors()){
					if (unitConverter != null) {
						//value = unitConverter.normalize(Double.parseDouble((String)dto.getData()), unit);
						value = unitConverter.normalize(value, unit);
					}
				}
			}

			if (trace != null && item != null){
				JXPathContext jc = JXPathContext.newContext(item);
				try{
					jc.setFactory(new JXPathAbstractFactory());
					jc.createPathAndSetValue(trace, value);
				// FIXME: Hack for RESTAPI. Avoid catching exceptions!
				}catch(JXPathException e){
					log.info("## WARNING: Got JXPathException for uiId=" + dto.getLabel() + ", trace: " + trace + ", value:" + value);
					continue;
				}
			}
		}
		
		setDefaultValues((Item)item);
	}


	private Object initializeValue(String remoteReference, String remoteType,
			Object value) throws ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		//CR Number: 49232. Basically the string values were coming in as empty string from the client and we were storing
		//them as is. We should actually init them to null along with non-strings. This is the reason we are commenting out 
		//current code and just check for empty string
//		//First get the last field in the trace
//		List<Field> fields = traceFields(remoteType,
//					remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE));
//		Field field = fields.get(fields.size() - 1);
//
//		//If the value sent by the client is blank and the field is non string, then initialize it.
//		if (!field.getType().equals(String.class) && (value != null && value.equals(""))){
		if (value != null && value.equals("")){
			value = null;
		}
		return value;
	}

	private void validate(String uiId, String remoteReference, String remoteType, Object value) throws ClassNotFoundException, BusinessValidationException{
		if (value == null) return;

		//First get the last field in the trace
		List<Field> fields = traceFields(remoteType,
					remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE));
		Field field = (null != fields) ? (fields.size() > 0 ? fields.get(fields.size() - 1) : null) : null;

		if (field != null && field.getAnnotation(Transient.class) == null){
			if (field.getType().equals(String.class)){
				dctColumnsSchema.validate(uiId, value.toString(), validationErrors);
			} else if (field.getType().equals(Long.class) || field.getType().equals(long.class)){
				try {
					dctColumnsSchema.validate(uiId, Integer.parseInt(value.toString()), validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value, fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId)};
					validationErrors.reject("SchemaValidation.Integer.invalidValue", args, "The given value for this field is invalid");
				}
			} else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)){
				try {
					dctColumnsSchema.validate(uiId,Integer.parseInt(value.toString()), validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value,fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId)};
					validationErrors.reject("SchemaValidation.Integer.invalidValue", args, "The given value for this field is invalid");
				}
			}  else if (field.getType().equals(Double.class) || field.getType().equals(double.class)){
				try {
					double doubleValue = Double.parseDouble(value.toString());
					//Round it to the decimal places given by the database.
					doubleValue = roundDecimal(doubleValue,getNumberOfDecimals(uiId));
					dctColumnsSchema.validate(uiId, doubleValue, validationErrors);
				} catch (NumberFormatException ne){
					Object[] args = {value,fieldHome.getDefaultName(uiId),getNumberOfDigits(uiId),getNumberOfDecimals(uiId)};
					validationErrors.reject("SchemaValidation.Double.invalidValue", args, "The given value for this field is invalid");
				}
			}
		}

	}

	private double roundDecimal(double d, int numDigits){
		StringBuffer format = new StringBuffer("#.");
		for (int cnt = 0; cnt < numDigits; cnt++){
			format.append("#");
		}

		DecimalFormat df = new DecimalFormat(format.toString());
		return Double.valueOf(df.format(d));
	}

	private int getNumberOfDecimals(String uiId){
		String lengthStr = dctColumnsSchema.getPropertyLength(uiId);

		//Get the lengths before and after decimal point from the schema
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		Integer lengthAfterDecimal = scaleStr != null ? Integer.parseInt(scaleStr) : 0;

		return lengthAfterDecimal;
	}

	private int getNumberOfDigits(String uiId){
		String lengthStr = dctColumnsSchema.getPropertyLength(uiId);

		//Get the lengths before and after decimal point from the schema
		String precisionStr = lengthStr.substring(0,lengthStr.indexOf("."));
		String scaleStr = lengthStr.substring(lengthStr.lastIndexOf(".") + 1);
		Integer lengthBeforeDecimal = precisionStr != null && scaleStr != null ? Integer.parseInt(precisionStr) - Integer.parseInt(scaleStr) : 0;

		return lengthBeforeDecimal;
	}
	
	
	private void setDefaultValues(Item item) throws ClassNotFoundException{
		
		//Get the unique value
		Long uniqueValue = item.getClassMountingFormFactorValue();
				
		//Unfortunately we need to handle some special cases
		//For example Network/Device Free standing, we first create the device
		//Which should not include the FrontFaces, however the containing 
		//Cabinet needs it. For this we just skip processing the default values.
		//If not we will be putting wrong values for the wrong type (in this case DEVICE/NETWORK item)
		List<Long> specialUniqueValues = Collections.unmodifiableList(new ArrayList<Long>(){{
			add(103L);//Device FreeStanding
			add(203L);//Network FreeStanding
		}});
		
		if (specialUniqueValues.contains(uniqueValue)) return;
		
		//First get the uiComponents that corresponds to the unique value and that has default values
		List<UiComponent> components = processor.getComponentsWithDefaults(uniqueValue);
		
		//Then go through each component and get the uiId.
		for (UiComponent component: components){
			//With the uiId get the field trace
			String uiId = component.getUiId();
			
			String remoteReference = processor.getRemoteRef(uiId);
			String remoteType = remoteRef.getRemoteType(remoteReference);
			String remoteAliasForId = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
			
			//TODO: We need to handle the case when we have just a remoteAlias and no id. This is currently not required for 3.0. We will handle it when time arise.
			if (remoteAliasForId != null){
				String trace = getFieldTrace(remoteType,remoteAliasForId);
				
				//Check the given item to see if there is a value defined
				
				if (trace != null){
					if (trace.contains("/"))
						trace = trace.substring(0, trace.lastIndexOf("/"));
					JXPathContext jc = JXPathContext.newContext(item);
					jc.setFactory(new JXPathAbstractFactory());
					Object value = jc.getValue(trace);
					if (component.getUiValueIdField() != null && component.getUiValueIdField().getUiDefaultValue() != null 
							&& component.getUiValueIdField().getUiDefaultValue() != null
							&& component.getUiValueIdField().getUiDefaultValue().size() > 0){
						
						Long id = null;
						for (UiDefaultType defaultValue:component.getUiValueIdField().getUiDefaultValue()){
							if (defaultValue.getClassMountingFormFactorValue() != null && defaultValue.getClassMountingFormFactorValue().contains(uniqueValue.toString())){
								id = Long.parseLong(defaultValue.getValueId());
								break;
							}
						}
						
						//If not found assume the first one has the default.
						if (id == null){
							id = Long.parseLong(component.getUiValueIdField().getUiDefaultValue().get(0).getValueId());
						}
						
						//This means that we have an id
						if (value == null &&
								(!skipSettingDefaultOrientationValueForItem(item,component))){
							String remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
							value = loadData(remoteType, remoteAlias, id);
							jc.createPathAndSetValue(trace, value);
						}
					}
				}
			}
		}
	}
	
	private boolean skipSettingDefaultOrientationValueForItem(Item item, UiComponent component) {
	//TODO: THIS IS VERY UGLY CODE. ONCE THERE IS A REFACTOR OF ITEMOBJECT, WE WILL FIX THIS.
	// do not set Orientation DEFAULT value for item classes below, when their parent item is null. 
	 List<Long> skipOrientationForClassList = new ArrayList<Long>();
	 Long uniqueValue = item.getClassMountingFormFactorValue();
	 
	 if (item.getParentItem() == null && component.getRemoteName().equals("orientation")){
		 skipOrientationForClassList = new ArrayList<Long>(){{
			add(SystemLookup.ModelUniqueValue.NetworkChassisRackable);
			add(SystemLookup.ModelUniqueValue.NetworkStackRackable);
			add(SystemLookup.ModelUniqueValue.NetworkStackNonRackable);
			add(SystemLookup.ModelUniqueValue.DeviceBladeChassisRackable);
			add(SystemLookup.ModelUniqueValue.DeviceStandardRackable);
			add(SystemLookup.ModelUniqueValue.DeviceStandardNonRackable);
		}};
	 }
	
	 // parent item (cabinet) is not set for an item and item belongs to one the class as 
	 return (skipOrientationForClassList.contains(uniqueValue));
	}

	private void validateNumberOfCustomFields( int newNumber, int oldNumber) throws BusinessValidationException{
		if( newNumber  <  oldNumber ){
			String code = "ItemValidator.invalidNumOfCustomFields";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(msg);
			ex.addValidationError(code, msg);
			throw ex;
		}
	}
	private void loadClassDefaultCustomFields(Item item) throws BusinessValidationException{
		Session session = sessionFactory.getCurrentSession();
		Set<CustomItemDetails> existingCustomFields =  item.getCustomFields();
		List<LkuData> thisClassAllCustomFields = getClassCustomFields(item.getClassLookup());
		LkuData tmpCustomDataTypeLookup = (LkuData) session.load(LkuData.class, SystemLookup.LkuType.DATA_TYPE_STRING);

		//If there are no any custom field defined for this item
		if(thisClassAllCustomFields.size() !=  existingCustomFields.size()){
			validateNumberOfCustomFields( thisClassAllCustomFields.size(), existingCustomFields.size());
			for( LkuData thisClassCustomFields : thisClassAllCustomFields ){
				boolean found = false;
				for( CustomItemDetails existingCF : existingCustomFields ){
					if( existingCF.getCustomAttrNameLookup().getLkuId().longValue() == thisClassCustomFields.getLkuId().longValue()){
						found = true;
						break;
					}
				}
				if( found != true){
					//add it to existing set
					CustomItemDetails cfd = new  CustomItemDetails();
					cfd.setCustomDataTypeLookup(tmpCustomDataTypeLookup);
					cfd.setCustomAttrNameLookup(thisClassCustomFields);
					cfd.setAttrValue(null);
					cfd.setItem(item);
					item.addCustomField(cfd);
				}
			}
		}
	}

	private void validateCustomFieldLength( String customFieldValue ) throws BusinessValidationException{
		if (null != customFieldValue && customFieldValue.length() > MAX_CUSTOM_FIELD_LENGTH) {
			String code = "ItemValidator.moreThanMaxLength";
			Object[] args = {MAX_CUSTOM_FIELD_LENGTH};
			String msg = messageSource.getMessage(code, args, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError( msg );
			ex.addValidationError(code, msg);
			throw ex;
		}
	}
	
	private void convertCustomFieldsForAnItem (Object itemObj, ValueIdDTO dto) throws ClassNotFoundException, BusinessValidationException {
		Item item = (Item)itemObj;
		Session session = sessionFactory.getCurrentSession();

		loadClassDefaultCustomFields(item);

		@SuppressWarnings("unchecked")
		HashMap<String, String> newCustomFields = (dto == null ) ?  null :
			((HashMap<String, String>)dto.getData());
		// no changes in custom fields, just return
		if( newCustomFields == null || newCustomFields.size() == 0 ) return;

		Set<CustomItemDetails> existingCustomFields =  item.getCustomFields();
		for( Entry<String, String> newCF : newCustomFields.entrySet()){
			Long lkuid = Long.parseLong(newCF.getKey());
			String newValue = newCF.getValue();
			LkuData customAttrNameLookup = (LkuData) session.get(LkuData.class, lkuid);
			LkuData customDataTypeLookup = (LkuData) session.get(LkuData.class, SystemLookup.LkuType.DATA_TYPE_STRING);
			boolean found = false;
			//Check if custom field already exists. If so, update attr value
			
			for( CustomItemDetails existingCF : existingCustomFields ){
				if( existingCF.getCustomAttrNameLookup().getLkuId().longValue() == customAttrNameLookup.getLkuId().longValue()){								
					existingCF.setAttrValue(newValue);
					found = true;
					break;
				}
			}
			
			if( !found ){ //custom field does not exist for the item class, throw exception
				String code = "LkuType.tiCustomField";
				String msg = messageSource.getMessage(code, null, null);
				BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				ex.addValidationError( msg );
				ex.addValidationError(code, msg);
				throw ex;

			}
			
			// Check the lenght of the custom field value
			validateCustomFieldLength( newValue );

		}
	}

	@SuppressWarnings("unchecked")
	private List<LkuData> getClassCustomFields(LksData classLookup) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(LkuData.class);
		criteria.createAlias("lksData", "lksData", Criteria.LEFT_JOIN);
		Criterion se1 = Restrictions.eq("lkuTypeName", "CUSTOM_FIELD");
		Criterion se2 = Restrictions.isNull("lksData.lksId");
		Criterion se3 = Restrictions.eq("lksData.lksId", classLookup.getLksId());
		LogicalExpression or = Restrictions.or(se2, se3);
		criteria.add(Restrictions.and(se1, or));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("lkuId"));
		return criteria.list();
	}

	private void changeNameToUpperCase(Item item) {
		String name = item.getItemName();
		if (name != null) {
			item.setItemName(name.toUpperCase(Locale.getDefault()));
		}
	}

	private String getTraceForValue(String remoteReference, String remoteType)
			throws ClassNotFoundException {
		String remoteAlias;
		String trace;
		remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_VALUE);
		trace = getFieldTrace(remoteType,remoteAlias);
		return trace;
	}

	public Item createItem(Long itemId, List<ValueIdDTO> valueIdDTOList) throws ClassNotFoundException, BusinessValidationException{

		if (valueIdDTOList == null || valueIdDTOList.isEmpty()){
			String code ="ItemConvert.invalidArguments";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
            ex.addValidationError( msg );
            ex.addValidationError(code, msg);
            throw ex;
		}

		String itemClass = getClassLabelFromDTO(valueIdDTOList);
		ModelDetails itemModel = getModelFromDTO(valueIdDTOList);
		Item item = null;
		
		//handle existing item
		if(itemId != null && itemId.longValue() > 0){
			item = getExistingItem(itemId, itemClass, itemModel);
		}
		else{
			item = createNewItem(itemClass, itemModel);
		}
		
		//did not create/found item, error out
		if (item == null) {
			String code = "ItemConvert.invalidArguments.ItemNull";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
            ex.addValidationError( msg );
            ex.addValidationError(code, msg);
            throw ex;
		}

		return item;
	}

	public Object loadItem(Class<?> domainType, Long id, boolean readOnly){
		Object result = null;
		Session session = null;

		try {
		if (id != null && id > 0){
			session = sessionFactory.openSession();
			Criteria criteria = session.createCriteria(domainType);
			criteria.createAlias("itemServiceDetails", "itemServiceDetails");
			//We are doing an EAGER loading since we will be filling the data with what
			//client sends and none of them should be null;
			criteria.setFetchMode("model", FetchMode.JOIN);
			criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
			criteria.setFetchMode("parentItem", FetchMode.JOIN);
			criteria.setFetchMode("itemServiceDetails", FetchMode.JOIN);
			criteria.setFetchMode("itemServiceDetails.itemAdminUser", FetchMode.JOIN);
			criteria.setFetchMode("cracNwGrpItem", FetchMode.JOIN);
			criteria.setFetchMode("upsBankItem", FetchMode.JOIN);
			criteria.setFetchMode("customFields", FetchMode.JOIN);
			criteria.setFetchMode("dataPorts", FetchMode.JOIN);
			criteria.setFetchMode("powerPorts", FetchMode.JOIN);
			criteria.setFetchMode("sensorPorts", FetchMode.JOIN);
			criteria.setFetchMode("itemSnmp", FetchMode.JOIN);
			criteria.add(Restrictions.eq("itemId", id));
			criteria.setReadOnly(readOnly);
			
			result = criteria.uniqueResult();
		}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}
/*
	public Object getOriginalItem(Long itemId){
		Object result = null;
		Session session = null;

		try {
			if (itemId != null && itemId > 0){
				session = sessionFactory.openSession();
				Item item = (Item)session.get(Item.class, itemId);

				if(item != null){
					Criteria criteria;

					ModelDetails model = item.getModel();

					if(model != null && model.isMeItemModel()){
						criteria = session.createCriteria(MeItem.class);
					}
					else if(model != null && model.isItItemModel()){
						criteria = session.createCriteria(ItItem.class);
					}
					else if(model != null && model.isCabinetItemModel()){
						criteria = session.createCriteria(CabinetItem.class);
					}
					else {
						criteria = session.createCriteria(Item.class);
					}

					criteria.createAlias("itemServiceDetails", "itemServiceDetails");
					//We are doing an EAGER loading since we will be filling the data with what
					//client sends and none of them should be null;
					criteria.setFetchMode("model", FetchMode.JOIN);
					criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
					criteria.setFetchMode("parentItem", FetchMode.JOIN);
					criteria.setFetchMode("itemServiceDetails", FetchMode.JOIN);
					criteria.setFetchMode("itemServiceDetails.itemAdminUser", FetchMode.JOIN);
					criteria.setFetchMode("customFields", FetchMode.JOIN);
					criteria.add(Restrictions.eq("itemId", itemId));
					criteria.setReadOnly(true);
					result = criteria.uniqueResult();

					session.evict(result);
				}
			}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}
*/
	public String getFieldTrace(String remoteType, String fieldName) throws ClassNotFoundException{
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		objectTrace.addHandler("upsBankItem.*", new ParentTracerHandler());
		if (null != fieldName) {
			objectTrace.traceObject(Class.forName(remoteType), fieldName);
		}
		String trace = objectTrace.toString().replace(".", "/");
		return trace;
	}

	public Object loadData(String remoteType, String fieldName, Object id) throws ClassNotFoundException {
		Object result = null;

		if(id != null /*&& (Long)id > 0*/){
			//Create Alias
			List<Field> fields = traceFields(remoteType, fieldName);
			if (fields.size() >= 2 && ((Long)id) > 0){
				Field field = fields.get(fields.size() - 2);
				Session session = sessionFactory.getCurrentSession();
				Criteria criteria = session.createCriteria(field.getType());
				criteria.add(Restrictions.eq(fields.get(fields.size() - 1).getName(), id));
				result = criteria.uniqueResult();
			} else {
				result = id;
			}
		}
		return result;
	}


	private List<Field> traceFields(String remoteType, String fieldName)
			throws ClassNotFoundException {
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("upsBankItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		List<Field> fields = null;
		if (null != fieldName) {
			fields = objectTrace.traceObject(Class.forName(remoteType), fieldName);
		}
		return fields;
	}

	private LksData getSubClass(ModelDetails modelDetails, LksData classLks) {

		// TODO: Handle Data Panel subclass properly. For now we need this to save ZeroU data panels for US848.
		if (modelDetails.getClassLookup().getLkpValueCode().equals( SystemLookup.Class.DATA_PANEL )) return null;


		Long subclassLkpValueCode = modelItemSubclassMap.getSubclassValueCode(
            classLks.getLkpValue(),
            modelDetails.getMounting(),
            modelDetails.getFormFactor());
		// subclass = subclass.replaceAll("\\s","");
		//subclass = subclass.replaceAll(" / ", "/");
		//subclass = subclass.substring(subclass.lastIndexOf("/") + 1, subclass.length());

		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LksData.class);
		//criteria.add(Restrictions.eq("lkpValueCode", subclassLks.getLkpValueCode()));
		criteria.add(Restrictions.eq("lkpTypeName", "SUBCLASS"));
        criteria.add(Restrictions.eq("lkpValueCode", subclassLkpValueCode));

		LksData result = (LksData) criteria.uniqueResult();
		return result;
	}

	//This utility method ensures that the item's class/subclass
	//match with the model.
	private void matchClassSubClass(Item item, ModelDetails modelDetails){
		if (item == null) return;

		//if (item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)) return;

		//ModelDetails modelDetails = item.getModel();

		if (modelDetails != null)
		{
			LksData classLks = modelDetails.getClassLookup();
			LksData subclassLks = null;
			if (!modelDetails.getClassLookup().getLkpValueCode().equals( SystemLookup.Class.DATA_PANEL )){ //For a data panel, we have no subclass
				if (modelDetails != null){
					Long subclassLkpValueCode = modelItemSubclassMap.getSubclassValueCode(
			            classLks.getLkpValue(),
			            modelDetails.getMounting(),
			            modelDetails.getFormFactor());


					Session session = sessionFactory.getCurrentSession();
					Criteria criteria = session.createCriteria(LksData.class);
					criteria.add(Restrictions.eq("lkpTypeName", "SUBCLASS"));
			        criteria.add(Restrictions.eq("lkpValueCode", subclassLkpValueCode));

					subclassLks = (LksData) criteria.uniqueResult();
				}
			}

			item.setClassLookup(classLks);
			item.setSubclassLookup(subclassLks);
		}
	}


	private ModelDetails getModelFromDTO(List<ValueIdDTO> valueIdDTOList) throws ClassNotFoundException{
		for (ValueIdDTO dto: valueIdDTOList){
			if (dto != null && dto.getLabel() != null && dto.getLabel().equals("cmbModel")){
				ModelDetails itemModel =  getModelDetails(dto);
				return itemModel;
			}
		}

		return null;
	}


	private String getClassLabelFromDTO(List<ValueIdDTO> valueIdDTOList){
		for (ValueIdDTO dto: valueIdDTOList){
			if (dto != null && dto.getLabel() != null && dto.getLabel().equals("tiClass")) {
				return (String)dto.getData();
			}
		}

		return null;
	}
	
	public Item getExistingItem(Long itemId, String itemClass, ModelDetails itemModel) throws ClassNotFoundException, BusinessValidationException{
		
		Session session = sessionFactory.getCurrentSession();
		
		/* load item from db - eg. ItItem, CabinetItem, etc, */
		Item dbItem = (Item)loadItem(Item.class, itemId, false);
		
		/* 
		 * fetch transient object which is yet to be persisted. For example Cabinet of a Free standing device item
		 * is not persisted into the database 
		 */
		if (dbItem == null && (itemId != null && itemId > 0)) dbItem = (Item) session.get(Item.class, itemId);
		
		//Only when we have the itemModel changed by the user is when we need to have setup the new model to the dbItem
		//If not dont set this with null!
		if (dbItem != null && itemModel != null) {
			/* User might have changed the model, update model from dto */
			dbItem.setModel(itemModel);
	
			/* set subclass for item class VM */
			if(itemClass != null && itemClass.contains("Virtual Machine")){
				dbItem.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE ) );
			} else {
				if (dbItem.getSubclassLookup() != null && dbItem.getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.CONTAINER) {
					// do not change the subclass for the container item
				}
				else {
					matchClassSubClass(dbItem, itemModel);
				}
			}
		} else if (dbItem != null && itemClass != null && itemClass.contains("Virtual Machine")){
			dbItem.setClassLookup(SystemLookup.getLksData(session,SystemLookup.Class.DEVICE));
			dbItem.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE ) );
		}
		return dbItem;
	}

	public Item createNewItem(String itemClass, ModelDetails itemModel) throws ClassNotFoundException, BusinessValidationException{
		Item result = null;
		Session session = sessionFactory.getCurrentSession();
		
		//handle new items
		if(itemModel != null ){
			if (itemModel.isCabinetItemModel()){
				result = new CabinetItem();
			}
			else if(itemModel.isItItemModel()) {
				result = new ItItem();
				result.setSubclassLookup(getSubClass(itemModel, itemModel.getClassLookup()));
			} else if(itemModel.isMeItemModel()){
				result = new MeItem();
				result.setSubclassLookup(getSubClass(itemModel, itemModel.getClassLookup()));
			} else { //default to Item
				result = new Item();
			}

			result.setClassLookup(itemModel.getClassLookup());
			result.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED));
			result.setModel(itemModel);
		}

		// Check if this item is a VM, since VMs do not have a model!
		if (result == null && itemClass != null && itemClass.contains("Virtual Machine")) {
			result = new ItItem();
			result.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE ) );
			result.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE ) );
			result.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}

		return result;
	}

}
