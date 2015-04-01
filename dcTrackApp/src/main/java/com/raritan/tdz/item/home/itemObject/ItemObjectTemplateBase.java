/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.externalticket.dao.ExternalTicketFinderDAO;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.modelchange.ChangeModel;
import com.raritan.tdz.item.home.modelchange.ChangeModelFactory;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.item.rulesengine.ModelItemSubclassMap;
import com.raritan.tdz.item.validators.ItemUniquenessValidator;
import com.raritan.tdz.item.validators.ItemValidatorNew;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.port.home.PortObjectCollectionFactory;
import com.raritan.tdz.reservation.dao.ReservationDAO;
import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.FilterHQLImpl;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.UniqueValidator;
import com.raritan.tdz.util.UnitConverterIntf;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * @author prasanna
 *
 */
public abstract class ItemObjectTemplateBase implements ItemObjectTemplate {
	
	//Protected members
	@Autowired
	protected ItemDomainFactory itemDomainFactory;
	
	@Autowired
	protected ItemDomainAdaptor itemDomainAdaptor;

	@Autowired
	protected ItemStateContext itemStateContext;
	
	protected ItemValidatorNew itemValidator;
	
	protected ItemValidatorNew itemDeleteValidator;
	
	@Autowired
	protected ItemUniquenessValidator itemUniquenessValidator;
	
	@Autowired
	protected ItemDAO itemDao;
	
	@Autowired
	protected ModelDAO modelDao;
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;
	
	@Autowired
	protected FieldHome fieldHome;
	
	@Autowired
	protected PortObjectCollectionFactory portObjectsFactory;
	
	@Autowired
	protected ChangeModelFactory changeModelFactory;
	
	@Autowired
	protected ItemObjectFactory itemObjectTemplateFactory;
	
	@Autowired
	private ModelItemSubclassMap modelItemSubclassMap;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired
	private ExternalTicketFinderDAO externalTicketFinderDAO;
	
	@Autowired
	protected ReservationDAO reservationDAO;
	
	@Autowired
	private ErrorSizeController errorSizeController;
	
	protected List<ItemSaveBehavior> itemSaveBehaviors;
	protected List<ItemDeleteBehavior> itemDeleteBehaviors;
	protected List<ItemSaveBehavior> itemFinalBehaviors;
	protected List<String> portObjectsList;
	protected RulesProcessor rulesProcessor;
	protected RemoteRef remoteRef;
	//This is not autowired since there can be different impl. of placement home injected into this.
	protected ItemPlacementHome placementHome;
	
	protected List<String> validationWarningCodes;
	
	@Resource(name="validationInformationCodes")
	protected List<String> validationInformationCodes;
	
	
	//Private members
	//This validator is required here to post validate the uniqueness of the item 
	//just in case user double clicks on the item save button.
	private UniqueValidator itemNameUniqueValidator;
	



	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#saveItem(java.lang.Object, java.lang.String)
	 */
	@Override
	public Long saveItem(Object itemDomain, String unit)
			throws BusinessValidationException {
		
		Item item = (Item)itemDomain;
		item.setItemId(itemDao.saveItem(item));

		//Delete reservation if exist
		if(item.getReservationId() != null && item.getReservationId() > 0){
			reservationDAO.delete(item.getReservationId());
		}
		
		return (item.getItemId());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#getItemDetails(java.lang.Long, java.lang.String)
	 */
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit)
			throws Throwable {
		
		Map<String, Filter> filterMap = getFilterMap(itemId);
		
		Map<String, UiComponentDTO> dtos = new HashMap<String, UiComponentDTO>();
		List<UiComponentDTO> componentList = new ArrayList<>();
		
		//UiView uiView = rulesProcessor.getData("uiView[@uiId='itemView']/", "itemId", itemId, "=", new LongType(), unit);
		UiView uiView = rulesProcessor.getData("uiView[@uiId='itemView']/", filterMap,  "itemId", itemId, "=", new LongType(), unit);
		JXPathContext jc = JXPathContext.newContext(uiView);
		componentList = jc.selectNodes("uiViewPanel/uiViewComponents/uiViewComponent");		
		
	
		// Fetch the item class - required fields are based on item class 
		Long itemClass = null;
		Item item = itemDao.getItem(itemId);
		itemClass = item != null && item.getClassLookup().getLkpValueCode() != null ? 
				item.getClassLookup().getLkpValueCode() : null;
		
		Map<String, Boolean> fieldRequiredMap = fieldHome.getFieldRequiredDetail(itemClass);
				
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
				//List<FieldDetails> fields = fieldHome.getFieldDetail(uiViewComponent.getUiId(), null, itemClass);
				// Boolean fieldRequiredAtSave = fieldHome.isThisFieldRequiredAtSave(uiViewComponent.getUiId(), null, itemClass); 
				// Performance, get all fieldRequiredAtSave out of the loop at once the out of loop
				Boolean fieldRequiredAtSave = fieldRequiredMap.get(uiViewComponent.getUiId());
				fieldRequiredAtSave = (null != fieldRequiredAtSave) ? fieldRequiredAtSave : false; 

//				if (fields != null) {
//					for (FieldDetails fd : fields) {
						//We have to special treat the itemName field here.
						//TODO: Somehow we need to remove this hardcoded value of the uiId for item name.
						if (uiViewComponent.getUiId().equals("tiName") && null != item.getStatusLookup() && item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.IN_STORAGE){
							uiViewComponent.setRequired( false );
                        } else if (uiViewComponent.getUiId().equals("tiName") && null != item.getStatusLookup() && item.getStatusLookup().getLkpValueCode() == SystemLookup.ItemStatus.ARCHIVED) {
                            uiViewComponent.setRequired( false);
						} else {
							//uiViewComponent.setRequired( fd.getIsRequiedAtSave() );
							uiViewComponent.setRequired( fieldRequiredAtSave);
						}
//					}
//				}
			}
		}
		
		return dtos;
	}



	/**
	 * (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#validateDeleteItem(java.lang.Long)
	 */
	@Override
	public void validateDeleteItem(Long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable {
		
		if (!validate) return;
		
		Item item = itemDao.getItem(itemId);

		validateDeleteItem(item, validate, userInfo);
		
	}

	private void validateDeleteItem(Item item, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable {
		
		if (!validate) return;
		
		MapBindingResult errors = getErrorObject(item);
		Map<String, Object> targetMap = getValidatorTargetMap(item, userInfo);
		targetMap.put(String.class.getName(), "delete");
		
		if (itemDeleteValidator != null) {
			itemDeleteValidator.validate(targetMap, errors);
		}
			
		if (errors.hasErrors()){
			//Include them into the BusinessValidationException
			//Throw this right away.
			
			throwBusinessValidationException(errors, "itemService.deleteItemsExt");
		}
		
	}

	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#deleteItem(java.lang.Long)
	 */
	@Override
	public void deleteItem(Long itemId, boolean validate, UserInfo userInfo) throws Throwable {
		Item item = itemDao.getItem(itemId);
		
		validateDeleteItem(item, validate, userInfo);
		
		if (itemDeleteBehaviors != null){
			for (ItemDeleteBehavior itemDeleteBehavior: itemDeleteBehaviors){
				itemDeleteBehavior.preDelete(item);
				itemDeleteBehavior.deleteItem(item);
				itemDeleteBehavior.postDelete();
			}
		}
	}

	//------- Setters and Getters
	
	public ItemDomainFactory getItemDomainFactory() {
		return itemDomainFactory;
	}

	public void setItemDomainFactory(ItemDomainFactory itemDomainFactory) {
		this.itemDomainFactory = itemDomainFactory;
	}

	public ItemDomainAdaptor getItemDomainAdaptor() {
		return itemDomainAdaptor;
	}

	public void setItemDomainAdaptor(ItemDomainAdaptor itemDomainAdaptor) {
		this.itemDomainAdaptor = itemDomainAdaptor;
	}

	@Override
	public ItemValidatorNew getItemValidator() {
		return itemValidator;
	}

	@Override
	public void setItemValidator(ItemValidatorNew itemValidator) {
		this.itemValidator = itemValidator;
	}
	
	public ItemValidatorNew getItemDeleteValidator() {
		return itemDeleteValidator;
	}

	public void setItemDeleteValidator(ItemValidatorNew itemDeleteValidator) {
		this.itemDeleteValidator = itemDeleteValidator;
	}

	@Override
	public List<ItemSaveBehavior> getItemSaveBehaviors() {
		return itemSaveBehaviors;
	}

	@Override
	public void setItemSaveBehaviors(List<ItemSaveBehavior> itemSaveBehaviors) {
		this.itemSaveBehaviors = itemSaveBehaviors;
	}

	
	public List<ItemDeleteBehavior> getItemDeleteBehaviors() {
		return itemDeleteBehaviors;
	}

	public void setItemDeleteBehaviors(List<ItemDeleteBehavior> itemDeleteBehaviors) {
		this.itemDeleteBehaviors = itemDeleteBehaviors;
	}
	
	
	public List<ItemSaveBehavior> getItemFinalBehaviors() {
		return itemFinalBehaviors;
	}

	public void setItemFinalBehaviors(List<ItemSaveBehavior> itemFinalBehaviors) {
		this.itemFinalBehaviors = itemFinalBehaviors;
	}

	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}
	
	public List<String> getPortObjectsList() {
		return portObjectsList;
	}

	public void setPortObjectsList(List<String> portObjectsList) {
		this.portObjectsList = portObjectsList;
	}

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}

	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public RemoteRef getRemoteRef() {
		return remoteRef;
	}

	public void setRemoteRef(RemoteRef remoteRef) {
		this.remoteRef = remoteRef;
	}
	
	public UniqueValidator getItemNameUniqueValidator() {
		return itemNameUniqueValidator;
	}

	public void setItemNameUniqueValidator(UniqueValidator itemNameUniqueValidator) {
		this.itemNameUniqueValidator = itemNameUniqueValidator;
	}

	public ItemPlacementHome getPlacementHome() {
		return placementHome;
	}

	public void setPlacementHome(ItemPlacementHome placementHome) {
		this.placementHome = placementHome;
	}

	//Protected methods
	protected void throwBusinessValidationException(Errors errors, String warningCallBack) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				// String msg = messageSource.getMessage(error, Locale.getDefault());
  				String msg = errorSizeController.getMessage(error);
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg);
				} else if (validationInformationCodes.contains(error.getCode())) {
					// Ignore all the informations in the item for now
					
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
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}
	
	/**
	 * This will ensure that the item is unique even after saving. This is esp true when 
	 * user double clicks on item save to save an item and there may be a race-condition. Due
	 * to this there may be two items with same parameters created. To avoid this, we double
	 * check the uniqueness here. (CR 49558)
	 * @param itemDomain
	 * @param sessionUser
	 * @param isUpdate
	 * @param errors
	 * @throws BusinessValidationException
	 */
	protected void postValidateItem(Object itemDomain, UserInfo sessionUser, Boolean isUpdate, Errors errors) throws BusinessValidationException {
		if (isUpdate || null == itemDomain) {
			return;
		}
	
		if (null != itemDomain) {
			if (null != errors) {
				itemUniquenessValidator.validate(itemDomain, errors);
			}
		}
		
		/*if (errors.hasErrors()){
			throwBusinessValidationException(errors);
		}*/
	}
	
	
	protected void captureItemData(Long existingItemId) throws DataAccessException{
		
		Item item = null;
		final boolean isUpdate = (existingItemId != null &&  existingItemId > 0);
		
		// Save the item state key for the original item data
		// We will need this later to see if we can transition.
		if (isUpdate) {
			item = itemDao.loadItem(existingItemId);
			SavedItemData.captureItemData(item, placementHome );
		}
	}
	
	
	protected void clearCapturedItemData(Long existingItemId){
		final boolean isUpdate = (existingItemId != null && existingItemId > 0);
		if (isUpdate) {
			SavedItemData.clearCurrentItemSaveDataKey();
		}
	}
	
	protected MapBindingResult getErrorObject(Item item) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, item.getClass().getName());
		return errors;
	}

	protected Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(item.getClass().getName(), item);
		targetMap.put(UserInfo.class.getName(), userInfo);
		return targetMap;
	}
	
	protected boolean hasModelChanged(Long itemId) throws BusinessValidationException {
		Item item = itemDao.loadItem(itemId);
		return hasModelChanged(item);
	}
	
	protected boolean hasModelChanged(Item item) throws BusinessValidationException{
		
		if (item == null) return false;
		
		//Item item = itemDao.loadItem(itemId);
		
		String newItemClass = (String) ValueIdDTOHolder.getCurrent().getValue("tiClass");
		
		Long modelId = null;
		
		if (ValueIdDTOHolder.getCurrent().getValue("cmbModel") != null){
		 modelId = ValueIdDTOHolder.getCurrent().getValue("cmbModel") instanceof Long ?
				(Long)ValueIdDTOHolder.getCurrent().getValue("cmbModel") : ((Integer)ValueIdDTOHolder.getCurrent().getValue("cmbModel")).longValue();
		}
		
		
	    if (item != null && item.getModel() != null && item.getModel().getModelDetailId().equals(modelId)){
	    	return false;
	    }
		
		ModelDetails newModel = modelId != null ? modelDao.getModelById(modelId) : null;
		
		if (newModel != null && newModel.getClassLookup() != null){
			newItemClass = newModel.getClassLookup().getLkpValue();
		}
		
		ModelDetails savedModel = item.getModel();
		
		Long existingItemClassValueCode = item != null && item.getClassLookup() != null ? item.getClassLookup().getLkpValueCode() : null;
		Long newItemClassValueCode = null;
		
		if (newItemClass != null && newItemClass.contains("Virtual Machine")) {
			newItemClassValueCode = SystemLookup.Class.DEVICE;
		} else {
			newItemClassValueCode = newModel != null && newModel.getClassLookup() != null ? newModel.getClassLookup().getLkpValueCode() : null;
			
			//If the model is not provided by the user, and we have an existing Item ClassValueCode, we assume that 
			//user is not changing the item model and thus the new value code is equal to existing.
			if (newItemClassValueCode == null && existingItemClassValueCode != null){
				newItemClassValueCode = existingItemClassValueCode;
			}
		}
		
		if (newItemClassValueCode == null || !newItemClassValueCode.equals(existingItemClassValueCode)){
			String code = "ItemValidator.itemClassChanged";
			String msg = messageSource.getMessage(code, null, null);
			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			ex.addValidationError(msg);
            ex.addValidationError(code, msg);
            throw ex;
		}
		
		Long existingItemSubClassLkpValueCode = item != null && item.getSubclassLookup() != null ? item.getSubclassLookup().getLkpValueCode() : null;
		Long newItemSubclassLkpValueCode = null;
		if (newItemClass != null && newItemClass.contains("Virtual Machine")){
			newItemSubclassLkpValueCode = SystemLookup.SubClass.VIRTUAL_MACHINE;
		} else {
			if (newModel != null){
				newItemSubclassLkpValueCode = modelItemSubclassMap.getSubclassValueCode(
		            newItemClass,
		            newModel.getMounting(),
		            newModel.getFormFactor());
			}
		}
		
		if (existingItemSubClassLkpValueCode != null && newItemSubclassLkpValueCode != null && 
				(savedModel != null || existingItemSubClassLkpValueCode.equals(SystemLookup.SubClass.VIRTUAL_MACHINE)) && 
				(newModel != null || newItemSubclassLkpValueCode.equals(SystemLookup.SubClass.VIRTUAL_MACHINE)) &&
				(existingItemSubClassLkpValueCode.longValue() !=newItemSubclassLkpValueCode.longValue() ||
				 (savedModel != null && newModel != null && !savedModel.getMounting().equals(newModel.getMounting())))) {
			return true;
		}
		
		return false;
	}
	
	private void resetUPosition(Item item) {
		if (item != null && item.getParentItem() == null) {
			item.setUPosition (SystemLookup.SpecialUPositions.NOPOS);
		} 
	}
	
	protected void onModelChanged(Item item) throws BusinessValidationException, InstantiationException, IllegalAccessException{
		SavedItemData savedData = SavedItemData.getCurrentItem();
		
		Long modelId = null;
		
		if (ValueIdDTOHolder.getCurrent().getValue("cmbModel") != null){
		 modelId = ValueIdDTOHolder.getCurrent().getValue("cmbModel") instanceof Long ?
				(Long)ValueIdDTOHolder.getCurrent().getValue("cmbModel") : ((Integer)ValueIdDTOHolder.getCurrent().getValue("cmbModel")).longValue();
		}
		
		ModelDetails newModel = modelId != null ? modelDao.getModelById(modelId) : null;
		
		//Change the subclass
		matchClassSubClass(item, newModel, savedData.getSavedItem());

		if (hasModelChanged(savedData.getSavedItem())) {
			ChangeModel changeModel = changeModelFactory.getChangeModel(savedData.getSavedItem(), item);
	
			if(changeModel != null){ //if null, no need to do any thing special
				canModelChanged(savedData.getSavedItem(), item);
				
				//Make sure that we initialize the changeModel
				changeModel.init(savedData.getSavedItem(), item, null);
				
			
				
				changeModel.change(savedData.getSavedItem(), item);
				
				resetUPosition(item);
				
				//Setup the proper validator for the converted item
				
				changeValidator(item);
				
				
				//Setup the proper saveBehaviors for the converted item
				
				changeSaveBehavior(item);
				
			} else {
				LksData savedItemClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getClassLookup() : null;
				LksData newItemClass = item.getClassLookup();
				
				LksData savedItemSubClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getSubclassLookup() : null;
				LksData newItemSubClass = item.getSubclassLookup();
				ModelDetails savedItemModel = savedData.getSavedItem() != null ? savedData.getSavedItem().getModel() : null;
				ModelDetails newItemModel = item.getModel();
				
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
	


//	protected void onModelChanged(Item item) throws BusinessValidationException, InstantiationException, IllegalAccessException{
//		SavedItemData savedData = SavedItemData.getCurrentItem();
//		
//		//Change the subclass
//		matchClassSubClass(item,item.getModel());
//
//		LksData savedItemClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getClassLookup() : null;
//		LksData newItemClass = item.getClassLookup();
//
//		/* throw exception if the make (class) of an item changed as well */
//		if(newItemClass == null || !newItemClass.getLksId().equals(savedItemClass.getLksId())){			
//			String code = "ItemValidator.itemClassChanged";
//			String msg = messageSource.getMessage(code, null, null);
//			BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
//			ex.addValidationError(msg);
//            ex.addValidationError(code, msg);
//            throw ex;
//		}
//		
//		LksData savedItemSubClass = savedData.getSavedItem() != null ? savedData.getSavedItem().getSubclassLookup() : null;
//		LksData newItemSubClass = item.getSubclassLookup();
//		ModelDetails savedItemModel = savedData.getSavedItem() != null ? savedData.getSavedItem().getModel() : null;
//		ModelDetails newItemModel = item.getModel();
//		
//		if (savedItemSubClass != null && newItemSubClass != null && 
//				(savedItemModel != null || savedItemSubClass.getLkpValueCode().longValue() == SystemLookup.SubClass.VIRTUAL_MACHINE) && 
//				(newItemModel != null || newItemSubClass.getLkpValueCode().longValue() == SystemLookup.SubClass.VIRTUAL_MACHINE) &&
//				(savedItemSubClass.getLkpValueCode().longValue() != newItemSubClass.getLkpValueCode().longValue() ||
//				 (savedItemModel != null && newItemModel != null && !savedItemModel.getMounting().equals(newItemModel.getMounting())))) {
//			ChangeModel changeModel = changeModelFactory.getChangeModel(savedData.getSavedItem(), item);
//	
//			if(changeModel != null){ //if null, no need to do any thing speacial
//				canModelChanged(savedData.getSavedItem(), item);
//				
//				//Make sure that we initialize the changeModel
//				changeModel.init(savedData.getSavedItem(), item, null);
//				
//			
//				
//				changeModel.change(savedData.getSavedItem(), item);
//				
//				//Setup the proper validator for the converted item
//				
//				changeValidator(item);
//				
//			} else {
//				//Throw an exception
//				String code = "ItemValidator.cannotChangeModel";
//				
//				String savedItemClassName = savedItemClass != null ? savedItemClass.getLkpValue():"Unknown";
//				String newItemClassName = newItemClass != null ? newItemClass.getLkpValue():"Unknown";
//				
//				String savedItemSubClassName = savedItemSubClass != null ? " " + savedItemSubClass.getLkpValue() :"";
//				String newItemSubClassName = newItemSubClass != null ? " " + newItemSubClass.getLkpValue():"";
//				
//				String savedItemMounting = savedItemModel != null ? " " + savedItemModel.getMounting():"";
//				String newItemMounting = newItemModel != null ? " " + newItemModel.getMounting():"";
//				
//				Object[] errorArgs = {savedItemClassName,savedItemSubClassName, savedItemMounting, newItemClassName,newItemSubClassName, newItemMounting};
//		
//				String msg = messageSource.getMessage(code, errorArgs, Locale.getDefault());
//				BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
//				ex.addValidationError(msg);
//	            ex.addValidationError(code, msg);
//	            throw ex;
//			}
//		}		
//	}


	protected void setAdditionalItemData(UserInfo sessionUser, Object itemDomain) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		
		// handle Item's state changes related to cleaning up on save
		itemStateContext.onSave ((Item)itemDomain);
		
		Item item = (Item)itemDomain;
		if (null == item.getMountedRailLookup()) {
			if (null != item.getFacingLookup()) {
				if (item.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.FRONT) {
					item.setMountedRailLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.RailsUsed.FRONT).get(0));
				}
				else if (item.getFacingLookup().getLkpValueCode() == SystemLookup.ChassisFace.REAR) {
					item.setMountedRailLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.RailsUsed.REAR).get(0));
				}
			}
		}
		setCreatedByUser(sessionUser, item);
		
		if (item.getItemId() > 0){
			item.getItemServiceDetails().setSysUpdateDate(new Date());
		}
	}

	protected void setCreatedByUser(UserInfo sessionUser, Item item) {
		//Generally the itemservice details will always exist. If it does not, we
		//may need to add one to ensure the user who created this exist.
		if (item.getItemServiceDetails() == null){
			item.setItemServiceDetails(new ItemServiceDetails());
		}
		
		if (sessionUser != null)
			item.getItemServiceDetails().setSysCreatedBy(sessionUser.getUserName());
	}

	

	//Private method
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
	
	protected void matchClassSubClass(Item item, ModelDetails modelDetails, Item savedItem) {
		if (item == null) return;

		//if (item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)) return;

		//ModelDetails modelDetails = item.getModel();
		String itemClass = (String) ValueIdDTOHolder.getCurrent().getValue("tiClass");
		
		if (itemClass != null && itemClass.contains("Virtual Machine")){
			item.setSubclassLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.SubClass.VIRTUAL_MACHINE).get(0));
			return;
		}

		if (modelDetails != null)
		{
			LksData classLks = modelDetails.getClassLookup();
			LksData subclassLks = null;
			if (modelDetails.getClassLookup().getLkpValueCode().equals( SystemLookup.Class.FLOOR_PDU )) {
				subclassLks = item.getSubclassLookup();
			}
			else if (!modelDetails.getClassLookup().getLkpValueCode().equals( SystemLookup.Class.DATA_PANEL )){ //For a data panel, we have no subclass
				if (modelDetails != null){
					Long subclassLkpValueCode = modelItemSubclassMap.getSubclassValueCode(
			            classLks.getLkpValue(),
			            modelDetails.getMounting(),
			            modelDetails.getFormFactor());
					if (subclassLkpValueCode != null && subclassLkpValueCode > 0) {
						subclassLks = systemLookupFinderDAO.findByLkpValueCode(subclassLkpValueCode).get(0);
					}
					else {
						// if the new model request is a container or the old model was a container, the subclass will be a container
						if (classLks.getLkpValueCode().longValue() == SystemLookup.Class.CABINET && 
								((null != item.getSubclassLookup() && item.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.CONTAINER) ||
								(null != savedItem && null != savedItem.getSubclassLookup() &&savedItem.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.CONTAINER))) {
							subclassLks = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.SubClass.CONTAINER).get(0); // item.getSubclassLookup();
						}
					}
				}
			}

			item.setClassLookup(classLks);
			item.setSubclassLookup(subclassLks);
		}
	}
	
	private void changeValidator(Item item) throws InstantiationException, IllegalAccessException {
		ItemObjectTemplate changedItemObject = null;
		
		if (item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)){
			changedItemObject = itemObjectTemplateFactory.getItemObjectForVM();
		} else if (item.getModel() != null){
			changedItemObject = itemObjectTemplateFactory.getItemObjectFromModelId(item.getModel().getModelDetailId());
		}
		
		if (changedItemObject != null){
			setItemValidator(changedItemObject.getItemValidator());
		}
	}	
	
	private void changeSaveBehavior(Item item) throws InstantiationException, IllegalAccessException {
		ItemObjectTemplate changedItemObject = null;
		
		if (item.getSubclassLookup() != null && item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)){
			changedItemObject = itemObjectTemplateFactory.getItemObjectForVM();
		} else if (item.getModel() != null){
			changedItemObject = itemObjectTemplateFactory.getItemObjectFromModelId(item.getModel().getModelDetailId());
		}
		
		if (changedItemObject != null){
			setItemSaveBehaviors(changedItemObject.getItemSaveBehaviors());
		}
		
	}
	
	private Map<String, Filter> getFilterMap(Long itemId) {
		Filter filterForItem = FilterHQLImpl.createFilter();
		filterForItem.eq("itemId",itemId);

		List<Long> ticketIds = externalTicketFinderDAO.findTicketIdByItemId(itemId);
		
		Filter filterForTicketFilter = null;
		if (ticketIds != null && ticketIds.size() > 0){
			filterForTicketFilter = FilterHQLImpl.createFilter();
			filterForTicketFilter.eq("tickets.ticketId", ticketIds.get(0))
				.and(FilterHQLImpl.createFilter().eq("isModified", false))
				.and(FilterHQLImpl.createFilter().ne("tickets.ticketStatus.statusLookup.lkpValueCode", SystemLookup.TicketStatus.TICKET_COMPLETE))
				.and(FilterHQLImpl.createFilter().ne("tickets.ticketStatus.statusLookup.lkpValueCode", SystemLookup.TicketStatus.TICKET_ARCHIVED));
		}
		
		Map<String,Filter> filterMap = new HashMap<String, Filter>();
		
		filterMap.put("main",filterForItem);
		filterMap.put(TicketFields.class.getName() + ":ticket",filterForTicketFilter);
		return filterMap;
	}
	
//	private boolean verifyDeleteItemPermission(long itemId, UserInfo userInfo) throws BusinessValidationException {
//
//		Item item = null;
//
//		if (userInfo == null) {
//			Object args[] = {"<Unknown>"};
//			String code = "ItemValidator.deleteAccessDenied";
//			throwBusinessValidationException (args, code);
//		}
//		
//		if (itemId > 0) item = (Item)sessionFactory.getCurrentSession().get(Item.class, itemId);
//		
//		if (item == null) {
//			Object args[] = {itemId};
//			String code = "ItemValidator.deleteInvalidItem";
//			throwBusinessValidationException (args, code);
//		}
//	
//		// verify if user has permission to delete items
//		if (itemModifyRoleValidator.canTransition(item, userInfo) == false) {
//			Object args[] = {userInfo.getUserName()};
//			String code = "ItemValidator.deleteAccessDenied";
//			throwBusinessValidationException (args, code);
//		}
//		return true;
//	}

	protected void OnLocationChange(Item item) {
		SavedItemData savedData = SavedItemData.getCurrentItem();

		DataCenterLocationDetails savedItemsLocation = savedData.getSavedItem()
				.getDataCenterLocation();

		DataCenterLocationDetails currentItemsLocation = item
				.getDataCenterLocation();

		if (savedItemsLocation != null
				&& currentItemsLocation != null
				&& savedItemsLocation.getDataCenterLocationId() != null
				&& currentItemsLocation.getDataCenterLocationId() != null
				&& (savedItemsLocation.getDataCenterLocationId().longValue() != currentItemsLocation
						.getDataCenterLocationId().longValue())) {
			Item currentItemsParent = item.getParentItem();
			Item saveItemsParent = savedData.getSavedItem().getParentItem();

			if (currentItemsParent != null && saveItemsParent != null &&
					currentItemsParent.getItemId() == saveItemsParent.getItemId()) {

				item.setParentItem(null);
				item.setUPosition(-9);
				item.setMountedRailLookup(null);
				item.setShelfPosition(-9);
			}
		}
	}
}
