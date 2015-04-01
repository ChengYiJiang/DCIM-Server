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
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessor;

/**
 * @author prasanna
 *
 */
public class ItemStateStorage extends ItemStateBase{
	
	ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public ItemStateStorage(RulesProcessor rulesProcessor, RemoteRef remoteRef,
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
		
		// when the new item is getting into storage state, do not clear the placement   
		// The SavedItemData will be available only when this code is hit via saveItem() API
		// If it try to perform onSave via different condition like request bypass update behavior then 
		// we want to continue and process the on Save on this state
		if ( item.getItemId() <= 0 ) return;
		
		Long statusValueCodeInDB = (item.getItemId() > 0 && SavedItemData.getCurrentItem() != null) ? SavedItemData.getCurrentItem().getItemStatusValueCode() : 0L;
		//CR 48912: If user is trying to save an item that is already in storage, we should not clear the fields.
		//Note that we will not come to this method unless user is trying to save this item to storage. Thus we just
		//need to check if the item in DB is in storage and is equal to the current item state (which will be storage, as
		//we cannot come to this state if is'nt). If they are not, then clear the placement info.
		if (!statusValueCodeInDB.equals(item.getStatusLookup().getLkpValueCode())){
			clearPlacement(item);
		}
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
		clearZeroUPlacement(item);
		clearNonRackablePlacement(item);
		clearDevNetFreeStandingPlacement(item);
	}
	
	/**
	 *   This function validates Item's Connections, requests, stage etc,. when moving item to storage 
	 */
	@Override
	protected void validateAdditionalPropertiesOfAnItem( Item item, UserInfo sessionUser, Errors errors) {
		// validate moving item to storage
		Validator storageValidator = itemObjectValidatorsFactory.getStorageValidators(item);
		String storageStr = "stored";
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(item.getClass().getName(), item);
		targetMap.put(UserInfo.class.getName(), sessionUser);
		targetMap.put(storageStr.getClass().getName(), storageStr);

		if (storageValidator != null) storageValidator.validate(targetMap, errors);

	}

}
