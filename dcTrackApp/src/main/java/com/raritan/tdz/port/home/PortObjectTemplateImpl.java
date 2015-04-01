package com.raritan.tdz.port.home;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.home.placement.ItemPlacementHome;
import com.raritan.tdz.port.dao.PortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * 
 * @author bunty
 *
 */
public class PortObjectTemplateImpl implements PortObjectTemplate {

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortObjectFactory portObjectFactory;

	@Autowired(required=true)
	private PortObjectCollectionFactory portObjectsFactory;
	
	@Resource(name="validationInformationCodes")
	protected List<String> validationInformationCodes;

	private ResourceBundleMessageSource messageSource;
	
	private PortDomainAdaptor adaptor;
	
	private Validator portValidator;
	
	private String portType;
	
	private ItemPlacementHome placementHome;
	
	@SuppressWarnings("rawtypes")
	private PortDAO portDAO;
	
	private Validator deletePortValidator;

	
	public PortObjectTemplateImpl(ResourceBundleMessageSource messageSource,
			PortDomainAdaptor adaptor, Validator portValidator, String portType, 
			@SuppressWarnings("rawtypes") PortDAO portDAO) {
		super();
		this.messageSource = messageSource;
		this.adaptor = adaptor;
		this.portValidator = portValidator;
		this.portType = portType;
		this.portDAO = portDAO;
		
	}
	
	

	public ItemPlacementHome getPlacementHome() {
		return placementHome;
	}



	public void setPlacementHome(ItemPlacementHome placementHome) {
		this.placementHome = placementHome;
	}



	public Validator getDeletePortValidator() {
		return deletePortValidator;
	}



	public void setDeletePortValidator(Validator deletePortValidator) {
		this.deletePortValidator = deletePortValidator;
	}



	@Override
	public PortInterface save(Long itemId, Long portId,
			PortInterface portDto, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException, BusinessInformationException {

		Assert.isTrue(adaptor != null);
		Item item = itemDAO.loadItem(itemId);
		
		Object domainPort = adaptor.convertOnePortForAnItem( item, portId, portDto);

		// apply common attributes to other ports
		
		Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
			
		IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portType, item, errors);

		portObjects.preValidateUpdates(errors);
		
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(domainPort.getClass().getName(), item);
		
		portValidator.validate(targetMap, errors);
		
		if (errors.hasErrors()){
			throwBusinessValidationException(errors);
		}

		updateItemCreationInfo( item, userInfo);		

		// Delete invalid ports
		portObjects.deleteInvalidPorts(errors);
		if (errors.hasErrors()){
			throwBusinessValidationException(errors);
		}

		//validate port and then save it 
		IPortObject portObject = portObjectFactory.getPortObject((IPortInfo)domainPort, errors);
		
		portObject.save();
		
		portObjects.postSave(userInfo, errors);		
		
		itemDAO.mergeOnly(item);
		
		return (PortInterface) domainPort;
		
	}

	@Override
	public void delete(Long itemId, Long portId, UserInfo userInfo, Boolean skipValidation, Errors errors) throws BusinessValidationException, BusinessInformationException, DataAccessException {
		
		if (null == itemId) {
			Object errorArgs[]  = { };
			errors.rejectValue("item", "PortValidator.itemNotFound", errorArgs, "Item not found");
			throwBusinessValidationException(errors);
			return;
		}
		
		// capture the existing item is required to update the amps in case connected port is updated
		captureItemData(itemId);

		try {
			// Validate if the port is editable or not.
			// Then check to see if the port is used or not
			// If everything is good, go ahead with delete
			Map<String,Object> targetMap = new HashMap<String,Object>();
			targetMap.put("portId",portId);
			targetMap.put("itemId",itemId);
			targetMap.put("UserInfo",userInfo);
			targetMap.put("portClass", DataPort.class);
			targetMap.put("skipValidation", new Boolean(skipValidation));
			if (null != deletePortValidator) deletePortValidator.validate(targetMap, errors);
			
			if (errors.hasErrors()) throwBusinessValidationException(errors);
			
			IPortInfo dataPort = (IPortInfo) portDAO.read(portId);
			IPortObject portObject = portObjectFactory.getPortObject(dataPort, errors);
			
			if (errors.hasErrors()) throwBusinessValidationException(errors);
			
			// delete the port!
			portObject.delete();
		}
		finally {
			
			clearCapturedItemData(itemId);
			
		}
	}


	
	@Override
	public void delete(PowerPortDTO portDetails, UserInfo userInfo, Errors errors) throws BusinessValidationException, DataAccessException, BusinessInformationException {

		Long itemId = portDetails.getItemId();
		
		if (null == itemId) {
			Object errorArgs[]  = { };
			errors.rejectValue("item", "PortValidator.itemNotFound", errorArgs, "Item not found");
			throwBusinessValidationException(errors);
			return;
		}
		
		// capture the existing item is required to update the amps in case connected port is updated
		captureItemData(itemId);

		Assert.isTrue(adaptor != null);
		
		try {
			Item item = itemDAO.loadItem(itemId);
			
			if (null == portDetails.getPortId()) {
				Object errorArgs[]  = { item.getItemName() };
				errors.rejectValue("port", "PortValidator.portNotFound", errorArgs, "Port not found");
				throwBusinessValidationException(errors);
				return;
			}
			
			// adaptor.deletePort(item, portId);
			adaptor.deleteOnePortForAnItem(item, portDetails);
			
			Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
				
			IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portType, item, errors);
	
			// updates required before validations
			if (portObjects != null) portObjects.preValidateUpdates(errors);
			
			// validate the changes in the port
			portObjects.validate(errors);
			
			// informs errors found during update/save 
			if (errors.hasErrors()) throwBusinessValidationException(errors);
			
			// pre-save
			portObjects.preSave();
			
			// delete all invalid ports
			portObjects.deleteInvalidPorts(errors);
			
			// clear the port move data for deleted ports
			portObjects.clearPortMoveData(errors);
			
			// save the item's redundancy information with port changes
			itemDAO.saveItem(item);
			
			// portObjects.save();
			
			// post save operations on ports
			portObjects.postSave(userInfo, errors);
			
		}
		finally {
			
			clearCapturedItemData(itemId);
			
		}

	}

	@Override
	public synchronized IPortInfo saveApplyCommonAttribute(PortInterface portDto, UserInfo userInfo,
			Errors errors)
			throws BusinessValidationException, DataAccessException, BusinessInformationException {

		Long itemId = portDto.getItemId();
		
		if (null == itemId) {
			Object errorArgs[]  = { };
			errors.rejectValue("item", "PortValidator.itemNotFound", errorArgs, "Item not found");
			throwBusinessValidationException(errors);
			return null;
		}
		
		// capture the existing item is required to update the amps in case connected port is updated
		captureItemData(itemId);
		
		try {
			Long portId = portDto.getPortId();
			
			Assert.isTrue(adaptor != null);
			Item item = itemDAO.loadItem(itemId);
			
			IPortInfo domainPort = adaptor.convertOnePortForAnItem( item, portId, portDto);
			
			IPortObject portObject = portObjectFactory.getPortObject((IPortInfo)domainPort, errors);
	
			Long classMountingFormFactorValue = item.getClassMountingFormFactorValue();
				
			IPortObjectCollection portObjects = portObjectsFactory.getPortObjects(classMountingFormFactorValue, portType, item, errors);
	
			// apply common attributes to other ports in the item and get the new portObjects object
			portObjects.applyCommonAttributes(portObject, errors);
	
			// updates required before validations
			if (portObjects != null) portObjects.preValidateUpdates(errors);
			
			// validate the changes in the port
			portObjects.validate(errors);
			
			// informs errors found during update/save 
			if (errors.hasErrors()) throwBusinessValidationException(errors);
			
			// pre-save
			portObjects.preSave();
			
			// delete all invalid ports
			portObjects.deleteInvalidPorts(errors);
			
			// clear the port move data for deleted ports
			portObjects.clearPortMoveData(errors);
			
			// save the item's redundancy information with port changes
			itemDAO.saveItem(item);
			
			item = itemDAO.merge(item);
			
			// post save operations on ports
			portObjects.postSave(userInfo, errors);
			
			// informs errors found during post save 
			if (errors.hasErrors()) throwBusinessValidationException(errors);
			
			domainPort = portObject.refresh();
			
			if (errors.hasErrors()) throwBusinessInformationException(domainPort, errors);
			
			return domainPort;
			
		}
		finally {
			
			clearCapturedItemData(itemId);
			
		}
		
	}

	private void throwBusinessValidationException(Errors errors) throws BusinessValidationException, BusinessInformationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
  		
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationInformationCodes.contains(error.getCode())) {
					// ignore information exceptions when throwing validation exception
					
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
		}
	}

	private void throwBusinessInformationException(Object domainObject, Errors errors) throws BusinessValidationException, BusinessInformationException {

		BusinessInformationException ie =  new BusinessInformationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
  		
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationInformationCodes.contains(error.getCode())) {
					ie.addValidationInformation(msg);
					ie.addValidationInformation(error.getCode(), msg);
					
				}
			}
		}

		if (ie.getValidationInformation().size() > 0) {
			ie.setDomainObject(domainObject);
			throw ie;
		}
	}

	
	private void updateItemCreationInfo( Item item, UserInfo sessionUser){
		Hibernate.initialize(item.getItemServiceDetails());
		Assert.isTrue(item.getItemServiceDetails() != null ); //item details must exists
		Assert.isTrue(item.getItemId() >= 0 ); //item also must exists 
		if (sessionUser != null){
			item.getItemServiceDetails().setSysCreatedBy(sessionUser.getUserName());
		}

		item.getItemServiceDetails().setSysUpdateDate(new Date());
	}

	private void captureItemData(Long existingItemId) throws DataAccessException{
		
		Item item = null;
		final boolean isUpdate = (existingItemId != null &&  existingItemId > 0);
		
		// Save the item state key for the original item data
		// We will need this later to see if we can transition.
		if (isUpdate) {
			item = itemDAO.loadItem(existingItemId);
			SavedItemData.captureItemData(item, placementHome );
		}
	}
	
	
	private void clearCapturedItemData(Long existingItemId){
		final boolean isUpdate = (existingItemId != null && existingItemId > 0);
		if (isUpdate) {
			SavedItemData.clearCurrentItemSaveDataKey();
		}
	}

}
