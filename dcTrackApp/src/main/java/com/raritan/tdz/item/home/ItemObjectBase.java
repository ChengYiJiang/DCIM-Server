package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.domain.Item;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.modelchange.ChangeModel;
import com.raritan.tdz.item.home.modelchange.ChangeModelFactory;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.validators.ItemValidator;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.home.IPortObjectCollection;
import com.raritan.tdz.port.home.PortObjectCollectionFactory;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.UniqueValidator;

/**
 * Base Item Object implementation. Ensures that the specified item domain object
 * has the correct subclass lookup value code type for the concrete item object.
 * @author Andrew Cohen
 */
public abstract class ItemObjectBase implements ItemObject {

	@Autowired
	protected ItemPlacementHome itemPlacementHome;
	
	@Autowired
	protected ChangeModelFactory changeModelFactory;
	/** The item domain object */
	protected Item item;
	
	@Autowired
	PortObjectCollectionFactory portObjectsFactory;
	
	private UniqueValidator itemNameUniqueValidator;
	private UniqueValidator uniqueValidator;
	
	private static final String CB_PROPAGATE_UI_ID = "cbPropagate";
	
	protected RulesProcessor rulesProcessor;
	protected ItemValidator itemValidator;
	protected ItemDomainAdaptor itemDomainAdaptor;
	protected ItemStateContext itemStateContext;
	protected ResourceBundleMessageSource messageSource;
	protected FieldHome fieldHome;
	protected RemoteRef remoteRef;
	protected ItemRequest itemRequest;

	protected List<String> validationWarningCodes;
	
	/** The session factory */
	protected SessionFactory sessionFactory;
	
	/* power port objects */
	private IPortObjectCollection powerPortObjectCollection = null;
	/* data port objects */
	private IPortObjectCollection dataPortObjectCollection = null;
	/* sensor port objects */
	private IPortObjectCollection sensorPortObjectCollection = null;
	
	protected static Logger log = Logger.getLogger("ItemHome");
	
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public ItemRequest getItemRequest() {
		return itemRequest;
	}

	public void setItemRequest(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}

	public UniqueValidator getItemNameUniqueValidator() {
		return itemNameUniqueValidator;
	}


	public void setItemNameUniqueValidator(
			UniqueValidator itemNameUniqueValidator) {
		this.itemNameUniqueValidator = itemNameUniqueValidator;
	}


	public ItemObjectBase(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	

	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}


	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}


	public UniqueValidator getUniqueValidator() {
		return uniqueValidator;
	}


	public void setUniqueValidator(UniqueValidator uniqueValidator) {
		this.uniqueValidator = uniqueValidator;
	}
	
	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}


	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}


	public ItemValidator getItemValidator() {
		return itemValidator;
	}


	public void setItemValidator(ItemValidator itemValidator) {
		this.itemValidator = itemValidator;
	}


	public ItemDomainAdaptor getItemDomainAdaptor() {
		return itemDomainAdaptor;
	}


	public void setItemDomainAdaptor(ItemDomainAdaptor itemDomainAdaptor) {
		this.itemDomainAdaptor = itemDomainAdaptor;
	}

	public ItemStateContext getItemStateContext() {
		return itemStateContext;
	}


	public void setItemStateContext(ItemStateContext itemStateContext) {
		this.itemStateContext = itemStateContext;
	}


	public FieldHome getFieldHome() {
		return fieldHome;
	}


	public void setFieldHome(FieldHome fieldHome) {
		this.fieldHome = fieldHome;
	}


	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}


	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}


	public ItemPlacementHome getItemPlacementHome() {
		return itemPlacementHome;
	}


	public void setItemPlacementHome(ItemPlacementHome itemPlacementHome) {
		this.itemPlacementHome = itemPlacementHome;
	}

	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}
	
	public ChangeModelFactory getChangeModelFactory() {
		return changeModelFactory;
	}

	public void setChangeModelFactory(ChangeModelFactory changeModelFactory) {
		this.changeModelFactory = changeModelFactory;
	}

	@Override
	public String getItemName(){
		if(item != null){
			return item.getItemName();
		}
		
		return null;
	}
	
	@Override
	public void init(Item item) {
		setItem(item);
		Set<Long> expectedCodes = this.getSubclassLookupValueCodes();
		long actualCode = item.getSubclassLookup() != null ? item.getSubclassLookup().getLkpValueCode() : item.getClassLookup().getLkpValueCode();
		
		if (actualCode > 0) {
			if (expectedCodes == null || !expectedCodes.contains( actualCode )) {
				throw new InvalidItemObjectException("Invalid subclass lkp value code: " + actualCode);
			}
		}
		else {
			actualCode = item.getClassLookup() != null ? item.getClassLookup().getLkpValueCode() : 0;
			if (expectedCodes == null || !expectedCodes.contains( actualCode )) {
				throw new InvalidItemObjectException("Invalid class lkp value code: " + actualCode);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit)
			throws Throwable {
		Map<String, UiComponentDTO> dtos = new HashMap<String, UiComponentDTO>();
		@SuppressWarnings("deprecation")
		UiView uiView = rulesProcessor.getData("uiView[@uiId='itemView']/", "itemId", itemId, "=", new LongType(), unit);
		JXPathContext jc = JXPathContext.newContext(uiView);
		List<UiComponentDTO> componentList = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent");
		
		// Fetch the item class - required fields are based on item class 
		Long itemClass = null;
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item)session.get(Item.class, itemId);
		if (item != null) {
			itemClass = item.getClassLookup().getLkpValueCode();
		}
		
		for (UiComponentDTO uiViewComponent : componentList) {
			// The item's origin file is hidden and not visible to user.
			if (uiViewComponent.isHide() != null && uiViewComponent.isHide() == true) continue;
			dtos.put(uiViewComponent.getUiId(), uiViewComponent);
			UnitConverterIntf unitConverter = remoteRef.getRemoteRefUnitConverter(uiViewComponent.getUiValueIdField().getRemoteRef());
			Object in_val = uiViewComponent.getUiValueIdField().getValue();
			if (null != unitConverter && null != in_val) {
				Object value = unitConverter.convert(in_val, unit);
				uiViewComponent.getUiValueIdField().setValue(value);
			}
			dtos.put(uiViewComponent.getUiId(), uiViewComponent);
			if (itemClass != null) {
				List<FieldDetails> fields = fieldHome.getFieldDetail(uiViewComponent.getUiId(), null, itemClass);
				if (fields != null) {
					for (FieldDetails fd : fields) {
						//We have to special treat the itemName field here.
						//TODO: Somehow we need to remove this hardcoded value of the uiId for item name.
						if (uiViewComponent.getUiId().equals("tiName") && item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.IN_STORAGE){
							uiViewComponent.setRequired( false );
                        } else if (uiViewComponent.getUiId().equals("tiName") && item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.ARCHIVED) {
                            uiViewComponent.setRequired( false);
						} else {
							uiViewComponent.setRequired( fd.getIsRequiedAtSave() );
						}
					}
				}
			}
			if (uiViewComponent.getUiId().equals("cmbSlotPosition")) {
				
			}
		}
		
		return dtos;
	}
	
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId, List<ValueIdDTO> dtoList, UserInfo sessionUser)
			throws ClassNotFoundException, BusinessValidationException, Throwable{
		
		//Note that this also validates the item Object.
		Object itemDomain = getItemDomainObject(itemId, dtoList, sessionUser);
		
		//If not save the object to database
	
		Map<String, UiComponentDTO> dtoMap = saveItem(itemDomain, (sessionUser != null) ? sessionUser.getUnits(): "1");
		
		Long savedItemId = (Long)dtoMap.get("tiName").getUiValueIdField().getValueId();
		
		Item savedItemDomain = (Item)itemDomain;
		if (null != itemDomain) {
			savedItemDomain.setItemId(savedItemId);
		}
		
		postValidateItem(savedItemId, savedItemDomain, sessionUser, (itemId != null && itemId > 0) ? true : false);
		
		postSaveItem(savedItemId, savedItemDomain, sessionUser, (itemId != null && itemId > 0) ? true : false);
		
		postProcessResult(itemId,dtoMap,dtoList,sessionUser);
		
		return dtoMap;
	}


	@Override
	public void captureItemData(Item item, ItemPlacementHome placementHome, Long itemId) throws DataAccessException{
		final boolean isUpdate = (itemId != null &&  itemId > 0);
		
		// Save the item state key for the original item data
		// We will need this later to see if we can transition.
		if (isUpdate) {
			//Item savedItem = (Item) loadItem(item.getClass(), itemId); 
			//SavedItemData.captureItemData( savedItem, placementHome );
			SavedItemData.captureItemData(item, placementHome );
		}
	}
	
	@Override
	public void clearCapturedItemData(Long itemId){
		final boolean isUpdate = (itemId != null && itemId > 0);
		if (isUpdate) {
			SavedItemData.clearCurrentItemSaveDataKey();
		}
	}
	
	//Perform any post processing of result DTO here.
	protected void postProcessResult(Long itemId,
			Map<String, UiComponentDTO> dtoMap, List<ValueIdDTO> dtoList,
			UserInfo sessionUser) {
		
		//Set any transient data to be the same as what was sent by the client
		setTransientDTO(itemId, dtoMap, dtoList, sessionUser);
		
	}

	protected void setTransientDTO(Long itemId,
			Map<String, UiComponentDTO> dtoMap, List<ValueIdDTO> dtoList,
			UserInfo sessionUser) {
		
		for (ValueIdDTO valueIdDTO: dtoList){
			if (valueIdDTO.getLabel().equals(CB_PROPAGATE_UI_ID)){
				dtoMap.get(CB_PROPAGATE_UI_ID).getUiValueIdField().setValue(valueIdDTO.getData());
			}
		}
		
	}

	protected Map<String, UiComponentDTO> saveItem(Object itemDomain, String unit)
			throws Throwable {
		Session session = sessionFactory.getCurrentSession();
		
		Item item = (Item)session.merge(itemDomain);
		
		session.flush();
		
		session.refresh(item);
		
		return getItemDetails(item.getItemId(), unit);
	}

	private void processErrors(Errors errors) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.setCallbackURL("itemService.saveItem");
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg);
				}
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		} else if (e.getValidationWarnings().size() > 0){
			throw e;
		}
	}
	
	protected void postValidateItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws BusinessValidationException {
		if (isUpdate || null == itemDomain) {
			return;
		}
		/* test for unique name */
		Item item = (Item) itemDomain;
		if (null != item) {
			BeanPropertyBindingResult errors = new BeanPropertyBindingResult(item, "item");
			if (null != errors) {
				validateItemNameUniqueness(errors, item, itemId);
				processErrors(errors);
			}
		}
	}
	
	//This is used for any post save processing. This is called by saveItem.
	protected void postSaveItem(Long itemId, Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException{
		//Base does nothing at this moment.
		//Derrived objects can override this and perform any post processing.
		if (null != powerPortObjectCollection) {
			powerPortObjectCollection.postSave(sessionUser, null);
		}
		if (null != dataPortObjectCollection) {
			dataPortObjectCollection.postSave(sessionUser, null);
		}
		if (null != sensorPortObjectCollection ){
			sensorPortObjectCollection.postSave(sessionUser, null);
		}
	}
	
	private ValueIdDTO getDtoUsingUiId(String uiId, List<ValueIdDTO> valueIdDTOList) {

		for (ValueIdDTO dto: valueIdDTOList) {
			if (dto != null && dto.getLabel() != null &&
					dto.getLabel().equals(uiId) && dto.getData() != null) {
				return dto;
			}
		}
		return null;
	}
	
	//This method assumes that item has been already populated with at least class info
	protected Object getItemDomainObject(Long itemId, List<ValueIdDTO> dtoList,
			UserInfo sessionUser) throws ClassNotFoundException,
			BusinessValidationException, DataAccessException, IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException { 
		final boolean isUpdate = (itemId != null && itemId > 0) ? true : false;

		//Get the item object out of the dto list
		itemDomainAdaptor.convert(item, dtoList, ((sessionUser != null) ? sessionUser.getUnits(): "1"));
		
		if (isUpdate) {
			item.setItemId(itemId);
			
			//check if model changes from one subclass to another
			onModelChanged();
		}
		
		setAdditionalItemData(sessionUser, item);		
		
		processPorts(item);
		
		//Perform any OnSave operation before validation
		//e.g.: Generating an itemName when the itemState is storage.
		itemStateContext.onSave(item);
		
		//Validate
		validate(item, sessionUser);
		
		deleteInvalidPorts();
		
		return item;
	}
	
	private void deleteInvalidPorts() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, item.getClass().getName());
		if (null != powerPortObjectCollection) {
			// ppObjs.delete();
			powerPortObjectCollection.deleteInvalidPorts(errors);
		}
		if (null != dataPortObjectCollection) {
			// dpObjs.delete();
			dataPortObjectCollection.deleteInvalidPorts(errors);
		}

		if (null != sensorPortObjectCollection ){
			sensorPortObjectCollection.deleteInvalidPorts(errors);
		}
	}
	
	public void processPorts(Item item) {
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
		powerPortObjectCollection = portObjectsFactory.getPortObjects(classMountingFormFactorValue, "PowerPorts", item, null);
		
		dataPortObjectCollection = portObjectsFactory.getPortObjects(classMountingFormFactorValue, "DataPorts", item, null);

		sensorPortObjectCollection = portObjectsFactory.getPortObjects(classMountingFormFactorValue, "SensorPorts", item, null);
	}
 	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		// Validate uniquness for name
		validateItemNameUniqueness(errors, item);
		
		// Validate uniquenss for asset tag
		validateAssetTagUniqueness(errors, item);
		
		// Validate uniquenss for eAsset tag
		validateEAssetTagUniqueness(errors, item);
		
		// Validate contract date, install date, etc
		validateDates(errors, item);
		
		// Validate Type and function
		validateTypeAndFunction(errors,item);
		
		// Validate the U position
		validateUPosition(item, errors);
	}
	

	protected void validateTypeAndFunction(Errors errors, Item item) {
		LksData classLksData = item.getClassLookup();
		
		if (item.getItemServiceDetails() != null && item.getItemServiceDetails().getPurposeLookup() != null){
			LkuData typeLkuData = item.getItemServiceDetails().getPurposeLookup();
			LksData typeLksData = typeLkuData.getLksData();
			// If the purposeLksData does not belong to the item class capture that as an error
			if (typeLksData == null || !typeLksData.equals(classLksData)){
				Object[] errorArgs = { typeLkuData.getLkuValue(), classLksData.getLkpValue() };
				errors.rejectValue("purposeLookup", "ItemValidator.incorrectType", errorArgs, "This type is not associated with this item class");
			}
		}
		
		if (item.getItemServiceDetails() != null && item.getItemServiceDetails().getFunctionLookup() != null){
			LkuData functionLkuData = item.getItemServiceDetails().getFunctionLookup();
			LksData functionLksData = functionLkuData.getLksData();
			// If the functionLksData does not belong to the item class capture that as an error
			if (functionLksData == null || !functionLksData.equals(classLksData)){
				Object[] errorArgs = { functionLkuData.getLkuValue(), classLksData.getLkpValue() };
				errors.rejectValue("purposeLookup", "ItemValidator.incorrectFunction", errorArgs, "This function is not associated with this item class");
			}
		}
	}

	protected void validateRequestStage(Object itemDomain, UserInfo userInfo, Errors errors, List<Long> requestStages, List<Long> itemIds, String errorCode) throws BusinessValidationException, DataAccessException {
		Map<Long,List<Request>> requestMap = itemRequest.getRequests(itemIds, requestStages, userInfo);
		List<Request> requests = null;
		if (null != requestMap) {
			requests = requestMap.get(item.getItemId());
		}
		if (null != requests && requests.size() > 0) {
			Object[] errorArgs = { };
			errors.rejectValue("cmbStatus", errorCode, errorArgs, "The item request stage do not allow item edit.");
		}
	}
	
	protected void validateRequestEditItem(Object itemDomain, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		validateRequestStage(itemDomain, userInfo, errors, requestStages, itemIds, "ItemValidator.itemHasRequestCannotEdit");
	}
	
	protected void validateRequestStateChange(Object itemDomain, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(item.getItemId());
		
		Item origItem = (item.getItemId() > 0) ? SavedItemData.getCurrentItem().getSavedItem() : null;
		if (origItem != null && null != origItem.getStatusLookup() && null != item.getStatusLookup() && 
				origItem.getStatusLookup().getLkpValueCode().longValue() != item.getStatusLookup().getLkpValueCode().longValue()) {
			validateRequestStage(itemDomain, userInfo, errors, requestStages, itemIds, "ItemValidator.itemHasRequestCannotEditStatus");
		}
	}

	protected void validate(Object itemDomain, UserInfo userInfo) throws BusinessValidationException, DataAccessException {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, itemDomain.getClass().getName());
		itemValidator.validate(itemDomain, errors);

		validateRequestEditItem(itemDomain, userInfo, errors);
		validateRequestStateChange(itemDomain, userInfo, errors);
		itemStateContext.isTransitionPermittedForUser(item, userInfo, errors);
		validatePorts(item, errors);

		processErrors(errors);
	}
	
	private void validatePorts(Item item, Errors errors) {
		if (null != powerPortObjectCollection) {
			powerPortObjectCollection.validate(errors);
		}
		if (null != dataPortObjectCollection) {
			dataPortObjectCollection.validate(errors);
		}
		if (null != sensorPortObjectCollection ){
			sensorPortObjectCollection.validate(errors);
		}
	}

	private void validateItemNameUniqueness(Errors errors, Item item, Long newItemId) {
		String dbItemName = item.getItemName();
		if (item.getDataCenterLocation() != null){
			String dbLocationCode = item.getDataCenterLocation().getCode();

			Object[] errorArgs = { item.getItemName(), item.getDataCenterLocation().getCode() };
			
			String ignoreProperty = (null == newItemId || -1 == newItemId) ? (item.getItemId() > 0 ? "itemId" : null) : "itemId"; 
			Object ignorePropertyValue = (null == newItemId || -1 == newItemId) ? (item.getItemId() > 0 ? item.getItemId() : null) : newItemId;
			
			try {
				Long parentId = item != null && item.getParentItem() != null ? item.getParentItem().getItemId() : -1L;
				if (item.getItemName() != null && !item.getItemName().isEmpty() 
						&& itemNameUniqueValidator != null 
						&& !itemNameUniqueValidator.isUnique("com.raritan.tdz.domain.Item", "itemName", item.getItemName(), item.getDataCenterLocation().getCode(), parentId, ignoreProperty, ignorePropertyValue)){
					errors.rejectValue("itemName", "ItemValidator.uniqueItemName", errorArgs, "Item already exists");
				}
			} catch (DataAccessException e) {
				errors.rejectValue("itemName", "ItemValidator.uniqueItemName", errorArgs, "Item name already exists");
			} catch (ClassNotFoundException e) {
				errors.rejectValue("itemName", "ItemValidator.uniqueItemName", errorArgs, "Item name already exists");
			}
		}
	}

	private void validateItemNameUniqueness(Errors errors, Item item) {
		validateItemNameUniqueness(errors, item, null);
	}

	private String getItemName(Long itemId){
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.setProjection(Projections.property("itemName"));
		criteria.add(Restrictions.eq("itemId", itemId));
		String result = (String) criteria.uniqueResult();
		return result;
	}
	
	private String getLocationCode(Long itemId){
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("dataCenterLocation", "location",Criteria.LEFT_JOIN);
		criteria.setProjection(Projections.property("location.code"));
		criteria.add(Restrictions.eq("itemId", itemId));
		String result = (String) criteria.uniqueResult();
		return result;
	}
	
	private void validateAssetTagUniqueness(Errors errors, Item item) {
		//If the asset tag is not set, we do not need to validate its uniqueness !
		if (item.getItemServiceDetails() == null) return;
		if (item.getItemServiceDetails().getAssetNumber() == null) return;
		
		String assetTag = item.getItemServiceDetails().getAssetNumber();
		String ignoreProperty = item.getItemId() > 0 ? "itemId": null;
		Object ignorePropertyValue = item.getItemId() > 0 ? new Long(item.getItemId()) : null;
		Object[] errorArgs = { assetTag };
		try {
			Long parentId = item != null && item.getParentItem() != null ? item.getParentItem().getItemId() : -1L;
			if (assetTag != null && !assetTag.isEmpty() && !uniqueValidator.isUnique("com.raritan.tdz.domain.Item", "assetNumber", assetTag, null, parentId, ignoreProperty, ignorePropertyValue)){
			
				errors.rejectValue("assetNumber", "ItemValidator.uniqueAssetTag", errorArgs, "Item asset tag already exists");
			}
		} catch (DataAccessException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueAssetTag", errorArgs, "Item asset tag already exists");		
		} catch (ClassNotFoundException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueAssetTag", errorArgs, "Item asset tag already exists");
		}
	}
	
	private void validateEAssetTagUniqueness(Errors errors, Item item) {
		//If the asset tag is not set, we do not need to validate its uniqueness !
		if (item.getRaritanAssetTag() == null) return;
		
		
		String assetTag = item.getRaritanAssetTag();
		String ignoreProperty = item.getItemId() > 0 ? "itemId": null;
		Object ignorePropertyValue = item.getItemId() > 0 ? new Long(item.getItemId()) : null;
		Object[] errorArgs = { assetTag };
		try {
			Long parentId = item != null && item.getParentItem() != null ? item.getParentItem().getItemId() : -1L;
			if (assetTag != null && !assetTag.isEmpty() && !uniqueValidator.isUnique("com.raritan.tdz.domain.Item", "raritanAssetTag", assetTag, null, parentId, ignoreProperty, ignorePropertyValue)){
			
				errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");
			}
		} catch (DataAccessException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");		
		} catch (ClassNotFoundException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");
		}
	}
	
	//NOTE: Set any additional data that the convert could not cover here.
	private void setAdditionalItemData(UserInfo sessionUser, Object itemDomain) {
		Item item = (Item)itemDomain;
		if (null == item.getMountedRailLookup()) {
			if (null != item.getFacingLookup()) {
				if (item.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.FRONT) {
					Session session = this.sessionFactory.getCurrentSession();
					item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT));
				}
				else if (item.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.REAR) {
					Session session = this.sessionFactory.getCurrentSession();
					item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.REAR));
				}
			}
		}
		setCreatedByUser(sessionUser, item);
	}

	private void setCreatedByUser(UserInfo sessionUser, Item item) {
		//Generally the itemservice details will always exist. If it does not, we
		//may need to add one to ensure the user who created this exist.
		if (item.getItemServiceDetails() == null){
			item.setItemServiceDetails(new ItemServiceDetails());
		}
		
		if (sessionUser != null)
			item.getItemServiceDetails().setSysCreatedBy(sessionUser.getUserName());
	}

	private void validateDates(Errors errors, Item item) {
		ItemServiceDetails detail = item.getItemServiceDetails();
		
		if(detail == null) return;
		
		java.util.Date contractStart = detail.getContractBeginDate();
		java.util.Date contractExpire = detail.getContractExpireDate();
		java.util.Date purchaseDate = detail.getPurchaseDate();
		java.util.Date installDate = detail.getInstallDate();
		
		//Contract Start Date <= Contract Expire Date And Contract Start Date >= Purchase Date 
		if ((contractStart != null && contractExpire != null) && (contractStart.equals(contractExpire) || contractStart.after(contractExpire))){
			errors.rejectValue("contractBeginDate", "ItemValidator.contractBeginDate1", null, "Contract Start Date must be less than Contract End Date");
		}

		if ((contractStart != null && purchaseDate != null) && contractStart.before(purchaseDate)){		
			errors.rejectValue("contractBeginDate", "ItemValidator.contractBeginDate2", null, "Contract Start Date must be greater than or equal to Purchase Date");
		}

		if ((contractStart != null && installDate != null) && installDate.after(contractStart)){		
			errors.rejectValue("installDate", "ItemValidator.contractBeginDate3", null, "Installation Date must be less than or equal to Contract Start Date");
		}				
		
		//Purchase Date <= Installation Date  And Contract Expired Date >= Purchase Date
		if ((purchaseDate != null && installDate != null) && purchaseDate.after(installDate)){		
			errors.rejectValue("purchaseDate", "ItemValidator.purchaseDate1", null, "Purchase Date must be less than or equal to Installation Date");
		}
		
		if ((purchaseDate != null && contractExpire != null) && (purchaseDate.equals(contractExpire) || purchaseDate.after(contractExpire))){		
			errors.rejectValue("purchaseDate", "ItemValidator.purchaseDate2", null, "Purchase Date must be less than Contract End Date");
		}
		
		//Installation Date <= Contract Start Date
		if ((installDate != null && contractExpire != null) && (installDate.equals(contractExpire) || installDate.after(contractExpire))){		
			errors.rejectValue("installDate", "ItemValidator.installDate1", null, "Installation Date must be less than Contract End Date");
		}				
	}

	@Transactional
	@Override
	public boolean deleteItem() throws Throwable {		
		ItemDeleteHelper itemDelete = new ItemDeleteHelper(this.sessionFactory);
		return itemDelete.deleteItem(item);
	}
			
	private Collection<Long> getAvailableUPositions( Item item ) throws DataAccessException {
		Collection<Long> availablePositions = null;
		
		Item origItem = null;
		if (item.getItemId() > 0) {
			SavedItemData savedData = SavedItemData.getCurrentItem();
			origItem = (null != savedData) ? savedData.getSavedItem() : null;
		}
		availablePositions = itemPlacementHome.getAvailablePositions( item, origItem );
		
		return availablePositions;
	}
	
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		try {
			isItemPlacementValid(item, errors);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Validate that the placement is still valid if the model RU Height changes on an existing item.
	 */
	public boolean isItemPlacementValid(Item item, Errors errors) throws DataAccessException {
		if (null == item) return true;
		if (item.getParentItem() == null) return true;
		if (item.getParentItem().isStatusArchived()) {
			String parentType = (null != item.getParentItem().getClassLookup() && item.getParentItem().getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET) ? "Cabinet" : 
										((null != item.getParentItem().getSubclassLookup() && (item.getParentItem().getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.CHASSIS || 
												item.getParentItem().getSubclassLookup().getLkpValueCode() == SystemLookup.SubClass.BLADE_CHASSIS)) ? "Chassis" : ""); 
			Object[] errorArgs = { parentType, item.getParentItem().getItemName() };
			errors.rejectValue("cmbCabinet", "ItemValidator.invalidParentStatus", errorArgs, "Cabinet / Chassis status do not allow placement");
			return false;
		}
		final long uPosition = item.getuPosition();
		Collection<Long> availPositions = getAvailableUPositions( item ); 
		if ( availPositions != null ) {
			availPositions.add( -1L ); // Above Cabinet
			availPositions.add( -2L ); // Below Cabinet
			availPositions.add( -9L ); // No U-position selected
		}
		if (!availPositions.contains(uPosition)) {
			Object[] errorArgs = {item.getuPosition(), item.getParentItem().getItemName() };
			errors.rejectValue("cmbCabinet", "ItemValidator.noAvailableUPosition", errorArgs, "The UPosition is not available");
			return false;
		}
		return true;
	}

	private void onModelChanged() throws BusinessValidationException{
		SavedItemData savedData = SavedItemData.getCurrentItem();

		LksData savedItemClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getClassLookup() : null;
		LksData newItemClass = item.getClassLookup();

		/* throw exception if the make (class) of an item changed as well */
		if(!newItemClass.getLksId().equals(savedItemClass.getLksId())){			
			String code = "ItemValidator.itemClassChanged";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(msg);
            ex.addValidationError(code, msg);
            throw ex;
		}
		
		LksData savedItemSubClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getSubclassLookup() : null;
		LksData newItemSubClass = item.getSubclassLookup();
		ModelDetails savedItemModel = savedData.getSavedItem() != null ? savedData.getSavedItem().getModel() : null;
		ModelDetails newItemModel = item.getModel();
		
		if (savedItemSubClass != null && newItemSubClass != null && 
				(savedItemModel != null || savedItemSubClass.getLkpValueCode().longValue() == SystemLookup.SubClass.VIRTUAL_MACHINE) && 
				(newItemModel != null || newItemSubClass.getLkpValueCode().longValue() == SystemLookup.SubClass.VIRTUAL_MACHINE) &&
				(savedItemSubClass.getLkpValueCode().longValue() != newItemSubClass.getLkpValueCode().longValue() ||
				 (savedItemModel != null && newItemModel != null && !savedItemModel.getMounting().equals(newItemModel.getMounting())))) {
		ChangeModel changeModel = changeModelFactory.getChangeModel(savedData.getSavedItem(), item);

		if(changeModel != null){ //if null, no need to do any thing speacial
			canModelChanged(savedData.getSavedItem(), item);
			
			//Make sure that we initialize the changeModel
			changeModel.init(savedData.getSavedItem(), item, null);
			
			changeModel.change(savedData.getSavedItem(), item);
			} else {
				//Throw an exception
				String code = "ItemValidator.cannotChangeModel";
				
				String savedItemClassName = savedItemClass != null ? savedItemClass.getLkpValue():"Unknown";
				String newItemClassName = newItemClass != null ? newItemClass.getLkpValue():"Unknown";
				
				String savedItemSubClassName = savedItemSubClass != null ? " " + savedItemSubClass.getLkpValue() :"";
				String newItemSubClassName = newItemSubClass != null ? " " + newItemSubClass.getLkpValue():"";
				
				String savedItemMounting = savedItemModel != null ? " " + savedItemModel.getMounting():"";
				String newItemMounting = newItemModel != null ? " " + newItemModel.getMounting():"";
				
				Object[] errorArgs = {savedItemClassName,savedItemSubClassName, savedItemMounting, newItemClassName,newItemSubClassName, newItemMounting};
		
				String msg = messageSource.getMessage(code, errorArgs, Locale.getDefault());
				BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				ex.addValidationError(msg);
	            ex.addValidationError(code, msg);
	            throw ex;
			}
		}		
	}	
	
	private void canModelChanged(Item itemDB, Item itemUpdated ) throws BusinessValidationException {

		if(!(itemUpdated.isStatusPlanned() || itemUpdated.isStatusStorage())){
			String code = "ItemValidator.itemStatusNotNew";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(msg);
            ex.addValidationError(code, msg);
            throw ex;
		}		
	}
	
	//TODO: Use new itemDAO.loadItem() function
	protected Object loadItem(Class<?> domainType, Long id){
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
			criteria.setFetchMode("customFields", FetchMode.JOIN);
			criteria.setFetchMode("cracNwGrpItem", FetchMode.JOIN);
			criteria.setFetchMode("dataPorts", FetchMode.JOIN);
			criteria.setFetchMode("powerPorts", FetchMode.JOIN);
			criteria.setFetchMode("sensorPorts", FetchMode.JOIN);
			criteria.add(Restrictions.eq("itemId", id));
			result = criteria.uniqueResult();
		}
		} finally {
			if (session != null){
				session.close();
			}
		}
		
		return result;
	}

	@Override
	public boolean isLicenseRequired(){
		if (this instanceof FreeStandingItemObject || this instanceof CabinetItemObject){
			return true;
		}		
		
		return false;
	}
}
