/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.home.FieldHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessor;

/**
 * @author Santo Rosario
 *
 */
public class ItemStateArchived extends ItemStateBase {
	
	public ItemStateArchived(RulesProcessor rulesProcessor, RemoteRef remoteRef,
			SessionFactory sessionFactory, FieldHome fieldHome,
			Map<Long, RoleValidator> allowableTransitionStates,
			RulesNodeEditability nodeEditability) {
		
		super(rulesProcessor, remoteRef, sessionFactory, fieldHome,
				allowableTransitionStates, nodeEditability);
		
	}

	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		
		/*if (item != null && itemHome != null && (item.getItemName() == null || item.getItemName().isEmpty()) ){
			item.setItemName(itemHome.getGeneratedStorageItemName().toUpperCase());
		}*/
		
		clearPlacement(item);
	}
	
	private void clearPlacement(Item item) throws DataAccessException{
		//NOTE: The placement varies with respect to placement
		//This variation is individually taken care in the methods
		//called below. So, even though it seems we are just calling this
		//method for all variations of placement, the individual methods
		//will take care of setting appropriate placement fields.
		//TODO: We must improve this in next release when we refactor
		//      ItemObject interface.
		clearCabinetPlacement(item);
		clearStandardPlacement(item);
		clearBladePlacement(item);
		clearVMPlacement(item);
		clearZeroUPlacement(item);
		clearUPSPlacement(item);
		clearCRACPlacement(item);
		clearNonRackablePlacement(item);
		clearDevNetFreeStandingPlacement(item);
		
	}
	
	/**
	 *  This function validates Item's Connections, requests, stage etc,. when moving item to archive
	 */
	@Override
	protected void validateAdditionalPropertiesOfAnItem( Item item, UserInfo sessionUser, Errors errors) {
		// validate Archiving of an item.
		Validator archiveValidator = itemObjectValidatorsFactory.getArchiveValidators(item);
		String archiveStr = "archived";
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(item.getClass().getName(), item);
		targetMap.put(UserInfo.class.getName(), sessionUser);
		targetMap.put(archiveStr.getClass().getName(), archiveStr);
		if (archiveValidator != null) archiveValidator.validate(targetMap, errors);

	}

}
