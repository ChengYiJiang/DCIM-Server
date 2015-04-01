package com.raritan.tdz.item.home.itemObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.validators.ItemObjectSaveValidatorsFactory;
import com.raritan.tdz.port.home.IPortObject;
import com.raritan.tdz.port.home.IPortObjectCollection;
import com.raritan.tdz.port.home.PortDomainAdaptor;
import com.raritan.tdz.port.home.PortObjectCollectionFactory;
import com.raritan.tdz.port.home.PortObjectFactory;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class PortObjectHomeImpl implements PortObjectHome{

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortObjectFactory portObjectFactory;

	@Autowired(required=true)
	private PortObjectCollectionFactory portObjectsFactory;

	private ResourceBundleMessageSource messageSource;
	
	private PortDomainAdaptor adaptor;
	
	private Validator portValidator;
	
	public PortDomainAdaptor getAdaptor() {
		return adaptor;
	}


	public void setAdaptor(PortDomainAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	private String portType; 
	

	public PortObjectHomeImpl(ResourceBundleMessageSource messageSource, Validator portValidator, String portType){
		this.messageSource = messageSource;
		this.portType = portType;
		this.portValidator = portValidator;
	}
	
	
	private void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				{
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


	
	private void updateItemCreationInfo( Item item, UserInfo sessionUser){
		Hibernate.initialize(item.getItemServiceDetails());
		Assert.isTrue(item.getItemServiceDetails() != null ); //item details must exists
		Assert.isTrue(item.getItemId() >= 0 ); //item also must exists 
		if (sessionUser != null){
			item.getItemServiceDetails().setSysCreatedBy(sessionUser.getUserName());
		}

		item.getItemServiceDetails().setSysUpdateDate(new Date());
	}
	
	/**
	 *  IMPORTANT !!!!!!: 
	 *  DELETE This method and its invocation when DataPanels implementation is moved from
	 *  CV to Web. Currently I have to add this hack because DataPanelDataPortObjectCollection has to 
	 *  skip validation for web since we do not show data ports there, but an item can have them 
	 *  created through CV.
	 *  
	 * @param classMountingFormFactorValue
	 * @param errors
	 */

	
	/**
	 * saveItemPort()
	 * returns: port domain object (DataPort, PowerPort, SensorPort). Currently only DataPort supported.
	 * itemId - id of an item
	 * portId - -1 when we create new port, or > 0 when we edit existing port
	 * portType - a string that can be: "DataPort", "PowerPort" or "SensorPort". Currently
	 * 			only "DataPort" supported.
	 * dto - DataPortDTO or PowerPortDTO or SensorPortDTO. Currently only DataPortDTO supported
	 * userInfo - info abou the user (contaians sesssion details) 
	 */
	public Object saveItemPort(Long itemId, Long portId,  /*String portType,*/ PortInterface dto, UserInfo userInfo, Errors errors)
					throws BusinessValidationException, Throwable {
		

		Assert.isTrue(adaptor != null);
		Item item = itemDAO.loadItem(itemId);
		
		Object domainPort = adaptor.convertOnePortForAnItem( item, portId, dto);

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
		
		return domainPort;

	}

}
