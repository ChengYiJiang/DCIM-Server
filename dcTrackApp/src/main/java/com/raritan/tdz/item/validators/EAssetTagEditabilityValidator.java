/**
 * 
 */
package com.raritan.tdz.item.validators;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.home.ItemHome;

/**
 * @author prasanna
 *
 */
public class EAssetTagEditabilityValidator implements Validator {

	@Autowired
	private ItemFinderDAO itemFinderDao;
	
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return Item.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		Map<String,Object> targetMap = (Map<String,Object>)target;

		Object itemDomainObject = targetMap.get(errors.getObjectName());
		
		validateEAssetTag(itemDomainObject,errors);
	}
	
	private void validateEAssetTag(Object target, Errors errors) {
		Item item = (Item)target;
		
		if (item.getItemId() > 0){
			
			List<String> raritanTagList = itemFinderDao.findEAssetTagById(item.getItemId());

			String raritanAssetTag = raritanTagList.size() == 1 ? raritanTagList.get(0) : null;
			
			List<Boolean> raritanTagVerifiedList = itemFinderDao.findEAssetTagVerifiedById(item.getItemId());
			
			Boolean isAssetTagVerified = raritanTagVerifiedList.size() == 1 ? raritanTagVerifiedList.get(0) : null;
			
			if (raritanAssetTag != null && isAssetTagVerified != null && isAssetTagVerified == true && !raritanAssetTag.equals(item.getRaritanAssetTag())){
				Object[] errorArgs = { item.getRaritanAssetTag(), raritanAssetTag };
				errors.rejectValue("tieAssetTag", "ItemValidator.eAssetTagLocked", errorArgs, "Electronic AssetTag cannot be changed.");
			}
		}
	}

}
