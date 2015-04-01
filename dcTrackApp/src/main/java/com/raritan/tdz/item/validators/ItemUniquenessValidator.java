/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.Map;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.UniqueValidator;

/**
 * Validate the uniquness of itemName, serialNumber and EAssetTag
 * @author prasanna
 *
 */
public class ItemUniquenessValidator implements Validator {

	private UniqueValidator itemNameUniqueValidator;
	private UniqueValidator uniqueValidator;
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		
		Map<String, Object> targetMap = (Map<String,Object>)target;
		
		Item item = (Item)targetMap.get(errors.getObjectName());
		UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());

		
		// Validate uniquness for name
		validateItemNameUniqueness(errors, item);
		
		// Validate uniquenss for asset tag
		validateAssetTagUniqueness(errors, item);
		
		// Validate uniquenss for eAsset tag
		validateEAssetTagUniqueness(errors, item);

	}

	
	public UniqueValidator getItemNameUniqueValidator() {
		return itemNameUniqueValidator;
	}

	public void setItemNameUniqueValidator(UniqueValidator itemNameUniqueValidator) {
		this.itemNameUniqueValidator = itemNameUniqueValidator;
	}

	public UniqueValidator getUniqueValidator() {
		return uniqueValidator;
	}

	public void setUniqueValidator(UniqueValidator uniqueValidator) {
		this.uniqueValidator = uniqueValidator;
	}

	private void validateItemNameUniqueness(Errors errors, Item item) {
		validateItemNameUniqueness(errors, item, null);
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
	
	private void validateAssetTagUniqueness(Errors errors, Item item) {
		//If the asset tag is not set, we do not need to validate its uniqueness !
		if (item.getItemServiceDetails() == null) return;
		if (item.getItemServiceDetails().getAssetNumber() == null) return;
		
		String assetTag = item.getItemServiceDetails().getAssetNumber();
		String ignoreProperty = item.getItemId() > 0 ? "itemId": null;
		Object ignorePropertyValue = item.getItemId() > 0 ? new Long(item.getItemId()) : null;
		Object[] errorArgs = { assetTag };
		Long parentId = item != null && item.getParentItem() != null ? item.getParentItem().getItemId() : -1L;
		try {
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
		Long parentId = item != null && item.getParentItem() != null ? item.getParentItem().getItemId() : -1L;
		try {
			if (assetTag != null && !assetTag.isEmpty() && !uniqueValidator.isUnique("com.raritan.tdz.domain.Item", "raritanAssetTag", assetTag, null, parentId, ignoreProperty, ignorePropertyValue)){
			
				errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");
			}
		} catch (DataAccessException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");		
		} catch (ClassNotFoundException e) {
			errors.rejectValue("assetNumber", "ItemValidator.uniqueEAssetTag", errorArgs, "Item eAsset tag already exists");
		}
	}

}
