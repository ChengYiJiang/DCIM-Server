/**
 * 
 */
package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.adapter.DTOAdapterBase;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelMfrDetails;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.item.json.MobileSearchItemInfo;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author bozana
 * 
 */

public class ItemDTOAdapter extends DTOAdapterBase implements ApplicationContextAware {
	
	ApplicationContext applicationContext;
	
	private static final String UIID_CUSTOM_FIELD = "tiCustomField";
	private static final String UIID_DATA_PORT = "tabDataPorts";
	private static final String UIID_POWER_PORT = "tabPowerPorts";
	private static final String UUID_SUBCLASS = "tiSubClass";
	private static final String UUID_CLASS = "tiClass";
	private static final String UUID_MODEL = "cmbModel";
	private static final String UUID_MAKE = "cmbMake";
	private static final String UUID_FORMFACTOR = "tiFormfactor";
	private static final String UUID_NAME = "tiName";
	private static final String UUID_LOCATION = "cmbLocation";
	private static final String UUID_CUSTOM_FIELD_KEY = "tiCustomField";
	private static final String UUID_PS_REDUNDANCY = "cmbPSRedundancy";
	private static final String VM_CLASS = "Device / Virtual Machine";
	private static final String MODEL_ID = "modelId";

	private final String customFieldPattern = "^tiCustomField_[0-9]+";

	private final Logger log = Logger.getLogger(ItemDTOAdapter.class);
	private LinkedHashMap<String, String> propsRemoteNameMap;
	private HashMap<String, String> defaultRadioButtons;
	private Set<String> disabledSetUiIds;
	private Set<String> disabledGetUiIds;
	private ModelHome modelHome;
	private SessionFactory sessionFactory;
	
	private Map<Long, String> shortItemInfoClassMap;
	
	@Autowired
	private FieldHome fieldHome;

	@Autowired
	List<ItemDataResolver> itemDataResolverList;
	
	public Map<Long, String> getShortItemInfoClassMap() {
		return shortItemInfoClassMap;
	}

	public void setShortItemInfoClassMap(Map<Long, String> shortItemInfoClassMap) {
		this.shortItemInfoClassMap = shortItemInfoClassMap;
	}
	
	//FIXME: Create Validator Bean for REST API validation and move this functionality there
	public void validateSearchString(String searchString) throws BusinessValidationException {
		if( searchString == null || searchString.length() <= 0 || searchString.contains("?") 
				|| searchString.contains("*")){
			String code = "restAPI.invalidSearchString";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
            ex.addValidationError( msg );
            ex.addValidationError(code, msg);
            throw ex;
			
		}
			
	}
	
	/**
	 * Converts domainItem (can me Item, MeItem ItItem) into either BasicItemInfo
	 * or one of its derived classes as defined by shortInfoClassMap
	 * 
	 * @param domainItem
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws DataAccessException 
	 */
	public BasicItemInfo convertDomainItemToBasicItemInfo( Item domainItem  ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DataAccessException{
		Long itemUniqueId = domainItem.getClassMountingFormFactorValue();

		//BasicItemInfo 
		String beanId = shortItemInfoClassMap.get(itemUniqueId);
		if( beanId == null ) beanId = shortItemInfoClassMap.get(0L); //default
		
		BasicItemInfo bi = beanId != null ? (BasicItemInfo) applicationContext.getBean(beanId) : null;
		bi.collectItemInfo(domainItem);
		
		return bi;
	}
	
	
	
	//Chengyi
	
	public MobileSearchItemInfo convertDomainItemToMobileSearchItemInfo ( Item domainItem ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DataAccessException{
		//get bean by string
		MobileSearchItemInfo msi = (MobileSearchItemInfo) applicationContext.getBean("mobileSearchItemInfo");
		msi.collectItemInfo(domainItem);
		
		return msi;
	}
	
	
	
	
	
	private static final Map<String, Map<String,Object>> constAliasMap = 
		Collections.unmodifiableMap(new HashMap<String, Map<String,Object>>() {
			private static final long serialVersionUID = 1L;
		{
			put("cmbCabinet", 
					new HashMap<String,Object>(){
						private static final long serialVersionUID = 1L;
					{
						put("classLookup", "classLookup");
					}}
				);
		}}
	);
	
	private static final Map<String, Map<String,Object>> constRestrictionsMap = 
			Collections.unmodifiableMap(new HashMap<String, Map<String,Object>>() {{
				put("cmbFunction", 
						new HashMap<String,Object>(){{
							put("lkuTypeName", "FUNCTION");
						}}
					);
				put("cmbSystemAdminTeam", 
						new HashMap<String,Object>(){{
							put("lkuTypeName", "TEAM");
						}}
					);
				put("cmbType", 
						new HashMap<String,Object>(){{
							put("lkuTypeName", "TYPE");
						}}
					);
				put("cmbCustomer", 
						new HashMap<String,Object>(){{
							put("lkuTypeName", "DEPARTMENT");
						}}
					);
				
				put("radioRailsUsed",
						new HashMap<String,Object>(){{
							put("lkpTypeName","RAILS_USED");
						}}
					);

				put("cmbOrientation",
						new HashMap<String,Object>(){{
							put("lkpTypeName","ORIENTATION");
						}}
					);

				put("radioDepthPosition",
						new HashMap<String,Object>(){{
							put("lkpTypeName","ZERO_U");
						}}
					);

				put("radioCabinetSide",
						new HashMap<String,Object>(){{
							put("lkpTypeName","RAILS_USED");
						}}
					);

				put("radioFrontFaces",
						new HashMap<String,Object>(){{
							put("lkpTypeName","FACING");
						}}
					);

				put("radioChassisFace",
						new HashMap<String,Object>(){{
							put("lkpTypeName","FACE");
						}}
					);
				put("cmbCabinet",
						new HashMap<String,Object>() {{
							put("classLookup.lkpValueCode", 1100L);
						}}
					);
			}});


	public Map<String, String> getPropsRemoteNameMap() {
		return propsRemoteNameMap;
	}

	public void setPropsRemoteNameMap(
			Map<String, String> propsRemoteNameMap) {
		this.propsRemoteNameMap = (LinkedHashMap<String, String>) propsRemoteNameMap;
	}
	
	/***************** OUT METHODS - convert data from DCT to Client ***************************/

	/**
	 * Converts Map containing (key:uiId, val:UiComponentDTO) into map
	 * containing (key: uiId, val: value) Currently the returned map is used by
	 * REST API. Custom fields have key in the form: tiCustomField_lkuId
	 * 
	 * 
	 * @param itemUiDTO
	 * @return Map<String,String> -> key: uiId, val: value
	 */
	public Map<String, Object> convertItemDetails(
			Map<String, UiComponentDTO> itemUiDTO) {
		Map<String, Object> itemDetails = new HashMap<String, Object>();
		if( itemUiDTO != null ){
			for( String uiId : itemUiDTO.keySet() ){
				UiComponentDTO dto = itemUiDTO.get(uiId);
				Object value = dto.getUiValueIdField().getValue();
				if( disabledGetUiIds.contains(uiId)) continue; 

				if( uiId.equals("tiName")){
					itemDetails.put("id", itemUiDTO.get(uiId).getUiValueIdField().getValueId());
				}else if ( uiId.equals("cmbModel")){
					itemDetails.put("modelId", itemUiDTO.get(uiId).getUiValueIdField().getValueId());
				}
				if( value != null && uiId.equals(UIID_CUSTOM_FIELD) ){
					Map<String, String> customFields = getCustomFields(value);
					if( customFields != null) itemDetails.putAll(customFields);
					continue;
				}

				itemDetails.put(uiId, value);
			}
		}
		if(log.isDebugEnabled()) log.debug("itemDetails=" + itemDetails);
		return itemDetails;
	}
	
	/**
	 * Converts custom fileds form DCT notation to REST API notation in the for
	 * key:value where key is "tiCustomField_<lkuId>"
	 * 
	 * @param customFieldObj
	 * @return
	 */
	private Map<String, String> getCustomFields(Object customFieldObj) {
		Map<String, String>customFields = null;
		
		if( customFieldObj instanceof Map<?,?> ){
			customFields = new HashMap<String, String>();
			String retVal;
			StringBuilder retKey = new StringBuilder();
			
			@SuppressWarnings("unchecked")
			Map<String,Object> customFieldMap = (Map<String, Object>)customFieldObj;
			for( String key : customFieldMap.keySet() ){
				retKey.setLength(0);
				Object val = customFieldMap.get(key);
				retKey.append("tiCustomField_");

				String lkuId = new String();
				if( val instanceof Map<?,?>){
					@SuppressWarnings("unchecked")
					Map<String,Object> customFieldValMap = (Map<String, Object>) val;
					lkuId = customFieldValMap.get("lkuId").toString();
					retVal = (String)customFieldValMap.get("value");
					retKey.append(lkuId);			
					customFields.put(retKey.toString(), retVal);
				}
			}
		}
		return customFields;
	}

	/***************** IN METHODS - convert data from Client to DCT ***************************/

	/**
	 * Invoked when calling get_all_items Creates Criteria DTO (all items) that
	 * Paginated
	 * 
	 * @return DTO Criteria
	 */
	public ListCriteriaDTO createCriteriaForGetAllItems() {
		ListCriteriaDTO listCriteria = new ListCriteriaDTO();
		listCriteria.setMaxLinesPerPage(-1);
		listCriteria.setFitType(ListCriteriaDTO.ALL);
		listCriteria.setPageNumber(0);
		int numProperties = propsRemoteNameMap.size();

		ColumnDTO[] columns = createColumnRequest();
		listCriteria.setColumns(Arrays.asList(columns));
		listCriteria.setColumnCriteria(null);
		return listCriteria;
	}

	/**
	 * Creates ColumnDTO that needs to be passed to PaginatedService as part of
	 * ListCriteriaDTO.
	 */
	private ColumnDTO[] createColumnRequest() {
		int numColumns = propsRemoteNameMap.size();
		ColumnDTO[] columns = new ColumnDTO[numColumns];
		int i = 0;
		for(String prop : propsRemoteNameMap.keySet()){
			columns[i] = new ColumnDTO();
			columns[i].setFieldName(prop);
			columns[i].setFieldLabel(propsRemoteNameMap.get(prop).toString());
			++i;
		}
		return columns;
	}

	/**
	 * Converts DTO returned by PagainatedService to a map whose value is an
	 * array of maps. Each inner map will contain info about one item. We
	 * require all items to be send at once. propsRemoteNameMap contains list of
	 * properties and their corresponding remote names that will be returned to
	 * the client for each item. It is hard-coded in homes.xml and reflects
	 * current GUI. When new properties are added into GUI, the same should be
	 * also added to the propsRemoteNameMap
	 * 
	 * @param resultList
	 *            - DTO returned by PaaginatedService
	 * @return - Map
	 */
	public Object convertItemList(ListResultDTO resultList) {

		Map<String, Object> retval = new LinkedHashMap<String, Object>();

		int numItems = resultList.getTotalRows();
		ArrayList<Map<String, String>> retArray = new ArrayList<Map<String, String>>();

		// FIXME: The paginated service API is not returning labels that we sent
		// so, we have to use the local copy
		Object labels[] = propsRemoteNameMap.values().toArray();
		int numColumns = labels.length;
		for( int i=0; i< numItems; i++ ){
			retArray.add(new LinkedHashMap<String, String>());
		}

		List<Object[]>values = resultList.getValues();
		Assert.isTrue(values.size() == numItems, "Number of items != list size");

		int j=0;
		for( Object[]item : values){
			for( int i=0; i<numColumns; i++){
				String key = (String) labels[i];
				String val = null;
				if( item[i] != null){
					val = item[i].toString();
				}
				retArray.get(j).put(key,val);
			}
			++j;
		}
		String key = "items";
		retval.put(key, retArray);

		return retval;
	}

	/**
	 * 
	 * @param uiId
	 *            - uiID as received form client
	 * @param value
	 *            - value that corresponds to uiId sent as a String and needs to
	 *            be converted to corresponding id (usually lku_id or
	 *            lkp_value_code, but can be also e.g. cabinet_id
	 * 
	 * @return Long -> id corresponding to the value
	 * 
	 * @throws DataAccessException
	 * @throws HibernateException
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException 
	 */
	public Long getIdFromValueAndLocation(String uiId, String value, Long locationId, Errors errors)
			throws DataAccessException, HibernateException,
			ClassNotFoundException {
		Map<String,Object> additionalRestrictions = null;
		Map<String,Object> additionalAlias = null;
		
		Map<String, Object> siteRestrictions = null;
		if( locationId != null ) siteRestrictions = getSiteRestrictions(locationId).get(uiId);
		Map<String, Object> constRestrictions = constRestrictionsMap.get(uiId);
		if( siteRestrictions != null || constRestrictions != null){
			additionalRestrictions = new HashMap<String, Object>();
			if( siteRestrictions != null) additionalRestrictions.putAll(siteRestrictions);
			if( constRestrictions != null) additionalRestrictions.putAll(constRestrictions);
		}
		Map<String, Object> constAlias = constAliasMap.get(uiId);
		if (constAlias != null) {
			additionalAlias = new HashMap<String, Object>();
			additionalAlias.putAll(constAlias); 
		}
		
		return getIdFromValue(uiId, value, additionalAlias, additionalRestrictions, errors);
	}

	public Long getIdFromValue(String uiId, String value, java.util.Map<String, Object> additionalAlias, Map<String,Object> additionalRestrictions, Errors errors )
			throws DataAccessException, HibernateException,
			ClassNotFoundException {

		Long idValue = getIdFromValue(uiId, value.toString(), additionalAlias, additionalRestrictions);	
		if( idValue != null && idValue.longValue() < 0 ){
			String label = getDefaultValue(uiId);
			log.error("Failed to convert \"" + label + ": " + value + "\" to propper id");
			String code = "ItemValidator.invalidValue";
			Object args[]  = {label, value, label};
			errors.rejectValue(label, code, args, "Failed to convert uiId to id");			
		}
		return idValue;
	}

	public boolean itemExists(long itemId) {
		boolean retval = true;
		Item item = (Item) getSessionFactory().getCurrentSession().get(Item.class, itemId);
		if (null == item) retval = false;
		return retval;
	}

	private boolean isVM(Map<String, Object> itemDetails){
		boolean retval = false;
		String val = (String) itemDetails.get("tiClass");
		if( val != null && itemDetails.get("tiClass").equals("Device / Virtual Machine")){
			retval = true;
		}
		return retval;
	}

	private Long getLocationId(Map<String, Object> itemDetails, Errors errors) throws DataAccessException, HibernateException, ClassNotFoundException{
		Long locationId = null;
		String value = (String) itemDetails.get(UUID_LOCATION);
		if( value != null ){
			locationId = getIdFromValue(UUID_LOCATION, value, null, null, errors);
		}
		return locationId;
	}
	
	private Map<String, Map<String,Object>> getSiteRestrictions(Long locationId){		
		Map<String, Map<String,Object>> retval = null;
		if( locationId != null){
			retval = new HashMap<String, Map<String, Object>>();
			Map<String, Object> cabinetRestrictions = new HashMap<String, Object>();
			cabinetRestrictions.put("dataCenterLocation.dataCenterLocationId", locationId);
			Map<String, Object> chassisRestrictions = new HashMap<String, Object>();
			chassisRestrictions.put("dataCenterLocation.dataCenterLocationId", locationId);
			retval.put("cmbCabinet", cabinetRestrictions);
			retval.put("cmbChassis", chassisRestrictions);
		}
		return retval;
	}
	
	
	private boolean isIgnored( boolean isVM, String uiId, Errors errors){
		boolean retval = false;
		if( uiId.equals("id")) retval = true;
		else if( uiId.equals(UUID_SUBCLASS)) retval = true;
		else if( uiId.equals(MODEL_ID)) retval = true;
		else if (uiId.equals("_isItemEditable")) retval = true; //TODO: Ask Bozana as to where this is used.
		else if( disabledSetUiIds.contains(uiId)) {
			Object[] errorArgs = { uiId };
			errors.reject("ItemValidator.unsupportedField", errorArgs, "Unsupported field");
			retval = true;
		}
		else if( isVM){
			if( uiId.equals(UUID_MODEL) || uiId.equals(UUID_MAKE)
					|| uiId.equals(UUID_FORMFACTOR)) {
				retval = true;
			}
		}
		return retval;
	}
	
	public List<ValueIdDTO> convertToDTOList(long id,
			Map<String, Object> itemDetails) throws DataAccessException,
			HibernateException, ClassNotFoundException, BusinessValidationException {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		Map<String, String> customFields = new HashMap<String, String>();
		boolean isVM = isVM(itemDetails);
		long modelId = -1;
		String mfrName = null, className = null;

		
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		
		resolveItemData(itemDetails, errors);
		
		Long locationId = getLocationId( itemDetails, errors );
		
		for (Entry<String, Object> entry : itemDetails.entrySet()) {
			ValueIdDTO dto = null;
			String uiId = entry.getKey();

			if (isIgnored(isVM, uiId, errors))
				continue;

			dto = new ValueIdDTO();
			dto.setLabel(entry.getKey());
			Object value = entry.getValue();
			
		

			if (uiId.matches(customFieldPattern)) {
				addCustomField(customFields, uiId, value);
				continue;
			}
			
			//This must be checked after checking for the custom field pattern.
			if (!isValidUiId(uiId, errors))
				continue;
			
			// If value is String has has id, we have to convert it to id (all cmb fields)
			// UUID_NAME is exception it has id which we have to ignore here
			if( value != null && !value.toString().isEmpty() && value instanceof String && !uiId.equals(UUID_NAME)){
				Long idValue = getIdFromValueAndLocation(uiId, value.toString(), locationId, errors);
				if( idValue != null ) {
					//remember manufacturer name, we will check if it matches model later
					if(uiId.equals(UUID_MAKE)) mfrName = value.toString();
					
					value = idValue;
				
					//remember model id since we need it for power/data ports
					if(uiId.equals(UUID_MODEL)) modelId = idValue.longValue();
					
				}//if
			}
			if(uiId.equals(UUID_CLASS)) className = (String)value;
			dto.setData(value);
			valueIdDTOList.add(dto);
		}// for
		
		//For create_item (id < 0) make and model are required
		//For update_item (id >= 0) either they are not provide at all, or both must be provided
		if ( id < 0 || (id > 0 && (mfrName != null || modelId > 0))){
			validateMakeAndModel(mfrName, modelId, className, errors);
		}
		checkForErrors(errors);
		// At the end add custom fields
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel(UUID_CUSTOM_FIELD_KEY);
		dto.setData(customFields);
		valueIdDTOList.add(dto);

		//If this is create() and model has data/power ports we have to load them from the model
		//library and them to the valueIdDTOList
		if (id == -1) {
			addDataAndPowerPorts(modelId, valueIdDTOList);
		}
		return valueIdDTOList;
	}

	private boolean isValidUiId(String uiId, Errors errors) {
		boolean isValid = true;
		try {
			String remoteRefString = rulesProcessor.getRemoteRef(uiId);
		} catch (JXPathNotFoundException e){
			log.error("The JXPath for UiId not found : " + uiId);
/*			Object[] errorArgs = { uiId };
			errors.reject("ItemValidator.unknownField", errorArgs, "Unsupported field");
*/			isValid = false;
		}
		return isValid;
	}

	private void addMissingFieldError( String uuId, Errors errors ){
		String errorCode = "ItemValidator.fieldRequired";
		String label = getDefaultValue(uuId);
		Object args[]  = {label, "Required field"};
		errors.rejectValue(null, errorCode, args, "required field");		
	}

	private void validateMakeAndModel(String mfrName, long modelId, String itemClass, Errors errors ){
		//for VM we ignore make and model
		if( itemClass != null && itemClass.equalsIgnoreCase(VM_CLASS)) return;
	
		//make and model are required uiIds
		if (modelId < 0) addMissingFieldError( UUID_MODEL, errors);
		if( mfrName == null ) addMissingFieldError( UUID_MAKE, errors);

		if( mfrName != null && modelId > 0){
			ModelDetails model = modelHome.getModelById(modelId); 
			if( model != null ){
				ModelMfrDetails mfr = model.getModelMfrDetails();
				//make and model match
				if( mfr != null && mfrName.equalsIgnoreCase(mfr.getMfrName())) return;
			}
			String code = "ItemValidator.invalidMakeModel";
			errors.rejectValue(null, code, null, "Make and model do not match");			
		}
		Assert.isTrue(modelId != 0);
	}
	


	private void addDataAndPowerPorts(long modelId, List<ValueIdDTO> valueIdDTOList) throws DataAccessException {	
		List<DataPortDTO> dataPorts = modelHome.getAllDataPort(modelId);
		List<PowerPortDTO> powerPorts = modelHome.getAllPowerPort(modelId);
		
		if( dataPorts != null && dataPorts.size() > 0 ){
			// set item id and portIds to -1
			for( DataPortDTO dp :  dataPorts ){
				dp.setItemId(new Long(-1));
				dp.setPortId(new Long(-1));
			}
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel(UIID_DATA_PORT);
			dto.setData(dataPorts);
			valueIdDTOList.add(dto);
		}
		if( powerPorts != null && powerPorts.size() > 0){
			for( PowerPortDTO pp: powerPorts ){
				pp.setItemId(new Long(-1));
				pp.setPortId(new Long(-1));
			}
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel(UIID_POWER_PORT);
			dto.setData(powerPorts);
			valueIdDTOList.add(dto);
			
			//add redundancy
			ModelDetails modelDetails = modelHome.getModelById(modelId);
			String redundancy = modelDetails.getPsredundancy();
			//If DB does not have info about redundancy, set default
			if( redundancy == null ){
				if(powerPorts.size() == 1) redundancy = new String("N");
				else redundancy = new String("N+1");
			}
			dto = new ValueIdDTO();
			dto.setLabel(UUID_PS_REDUNDANCY);
			dto.setData(redundancy);
			valueIdDTOList.add(dto);
		} 
	}

	private void addCustomField(Map<String, String> customFields, String uiId,
			Object value) {
		String[] strArray = uiId.split("_");
		String valueStr = value != null ? value.toString() : null;
		customFields.put(strArray[1], valueStr);
	}

	public Set<String> getDisabledSetUiIds() {
		return disabledSetUiIds;
	}

	public void setDisabledSetUiIds(Set<String> disabledSetUiIds) {
		this.disabledSetUiIds = disabledSetUiIds;
	}

	public Set<String> getDisabledGetUiIds() {
		return disabledGetUiIds;
	}

	public void setDisabledGetUiIds(Set<String> disabledGetUiIds) {
		this.disabledGetUiIds = disabledGetUiIds;
	}

	public ModelHome getModelHome() {
		return modelHome;
	}

	public void setModelHome(ModelHome modelHome) {
		this.modelHome = modelHome;
	}



	public HashMap<String, String> getDefaultRadioButtons() {
		return defaultRadioButtons;
	}

	public void setDefaultRadioButtons(HashMap<String, String> defaultRadioButtons) {
		this.defaultRadioButtons = defaultRadioButtons;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	private String getDefaultValue (String uiId) {
		String label = fieldHome.getDefaultName(uiId);
		if (label == null || label.isEmpty()) label = uiId;

		return label;
	}
	
	private void resolveItemData(Map<String, Object> data, Errors errors) throws BusinessValidationException, DataAccessException {
		for (ItemDataResolver r : itemDataResolverList) {
			r.resolve(data, errors);
		}
	}

}
