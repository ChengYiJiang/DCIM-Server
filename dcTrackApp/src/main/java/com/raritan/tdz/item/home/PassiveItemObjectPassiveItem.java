package com.raritan.tdz.item.home;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.validators.ItemValidatorNew;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class PassiveItemObjectPassiveItem implements ItemObjectPassiveItem {

	private static Logger log = Logger.getLogger("PassiveItemObjectPassiveItem");
	
	@Autowired
	protected PassiveItemDomainAdaptor passiveItemDomainAdaptor;	

	@Autowired
	protected ItemDAO itemDao;
	
	@Autowired
	protected ResourceBundleMessageSource messageSource;	
	
	protected List<String> validationWarningCodes;
	
	protected ItemValidatorNew itemValidator;
	
	public ItemValidatorNew getItemValidator() {
		return itemValidator;
	}

	public List<String> getValidationWarningCodes() {
		return validationWarningCodes;
	}

	public void setValidationWarningCodes(List<String> validationWarningCodes) {
		this.validationWarningCodes = validationWarningCodes;
	}	
	
	public void setItemValidator(ItemValidatorNew itemValidator) {
		this.itemValidator = itemValidator;
	}		
	
	public PassiveItemObjectPassiveItem() {
		log.error("init PassiveItemObjectPassiveItem");
	}

    @Override
    public Long saveItem(Long itemId,
            List<ValueIdDTO> dtoList, UserInfo sessionUser)
            throws ClassNotFoundException, BusinessValidationException,
            Throwable {

        // Get user name from UserInfo
        ValueIdDTO userNameDto = new ValueIdDTO("userName", sessionUser.getUserName());
        dtoList.add(userNameDto);

        // Domain object
        Item item = new Item();
        if (itemId > 0) {
            item.setItemId(itemId);
        }

        // Convert List<ValudIdDTO> to domain object Item
        item = (Item)passiveItemDomainAdaptor.convert(item, dtoList, sessionUser.getUnits());

        MapBindingResult errors = getErrorObject(item);
		Map<String,Object> targetMap = getValidatorTargetMap(item, sessionUser);

		if (itemValidator != null) {
			itemValidator.validate(targetMap, errors);
		}

		if (errors.hasErrors()){
			//Include them into the BusinessValidationException
			//Throw this right away.
			throwBusinessValidationException(errors, "itemService.savePassiveItem");
		}

        return itemDao.savePassiveItem(item);
    }

    @Override
    public void deleteItem(Long itemId) throws Throwable {
        itemDao.deletePassiveItem(itemId);
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
	protected void throwBusinessValidationException(Errors errors, String warningCallBack) throws BusinessValidationException {
  		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
  				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
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
			e.setCallbackURL(warningCallBack);
			throw e;
		}
	}	
}