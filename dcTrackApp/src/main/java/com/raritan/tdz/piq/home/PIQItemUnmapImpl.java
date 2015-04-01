package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.util.BusinessExceptionHelper;
/**
 * 
 * @author bunty
 *
 */
public class PIQItemUnmapImpl implements PIQItemUnmap {

	@Autowired
	private ItemDAO itemDAO;
	
	private List<Validator> validators;
	
	@Autowired
	private BusinessExceptionHelper businessExceptionHelper;
	
	public PIQItemUnmapImpl(List<Validator> validators) {
		super();
		this.validators = validators;
	}

	@Override
	public void unmap(String locationCode, String itemName, UserInfo userInfo, Errors errors) throws BusinessValidationException {
		
		Long itemId = itemDAO.getItemByLocationAndName(locationCode, itemName);
		
		// validate
		validate(itemId, userInfo, locationCode, itemName, errors);
		
		// check errors and throw business exception
		businessExceptionHelper.throwBusinessValidationException(null, errors, null);
		
		// get all associated items
		List<Long> associatedItemIds = getAssociatedItems(Arrays.asList(itemId));
		
		// unmap the items to PIQ, you may also need to clear the external key
		itemDAO.unmapItemWithPIQ(associatedItemIds);
		
	}

	private void validate(Long itemId, UserInfo userInfo, String locationCode, String itemName, Errors errors) {

		if (null == itemId) {
			
			Object[] errorArgs = { locationCode, itemName };
			errors.rejectValue("itemId", "IpAddressValidator.invalidItem", errorArgs, "Item does not exist in the given location");
			return;
		}

		if (null == validators) return;
		
		Map<String,Object> targetMap = new HashMap<String,Object>();
		
		// Set the args
		targetMap.put("itemId", itemId);
		targetMap.put("UserInfo", userInfo);
		
		for (Validator validator: validators) {
			validator.validate(targetMap, errors);
			
		}
		
	}
	
	private List<Long> getAssociatedItems(List<Long> itemIds) {
		
		// if itemId is parent itemId, then get all child items for deletion
		@SuppressWarnings("unchecked")
		List<Long> itemIdList = itemDAO.getItemIdsToDelete(itemIds);

		// add power panels to the list if the parent item id is not set for the panel
		addPanelsUsingConnections(itemIds, itemIdList);

		//add current item to list
		itemIdList.removeAll(itemIds);
		itemIdList.addAll(itemIds);

		addBuswayPowerOutlets(itemIdList);

		// get extra power outlets added
		@SuppressWarnings("unused")
		List<Long> extraPowerOutlets = addLocalNRemotePowerOutlets(itemIdList);
		
		return itemIdList;
		
	}
	
	private void addPanelsUsingConnections(List<Long> itemIds, List<Long> primaryItemIdList) {
		
		List<Long> panelItemIds = itemDAO.getPanelItemIdsToDelete(itemIds);
		
		for (Long panelItemId: panelItemIds) {
			if (!primaryItemIdList.contains(panelItemId)) {
				primaryItemIdList.add(0, panelItemId);
			}
		}
		
	}

	private void addBuswayPowerOutlets(List<Long> primaryItemIdList) {
		
		// add power outlets associated with the busway panel 
		List<Long> buswayPowerOutletList = getBuswayPowerOutletItemIdsToDelete(primaryItemIdList);
		primaryItemIdList.removeAll(buswayPowerOutletList);
		primaryItemIdList.addAll(0, buswayPowerOutletList);
		
	}

	private List<Long> addLocalNRemotePowerOutlets(List<Long> primaryItemIdList) {
		
		List<Long> localPowerOutletList = getLocalPowerOutletItemIdsToDelete(primaryItemIdList);
		List<Long> remotePowerOutletList = getRemotePowerOutletItemIdsToDelete(primaryItemIdList);

		List<Long> powerOutlets = new ArrayList<Long>(localPowerOutletList); 
		powerOutlets.addAll(remotePowerOutletList);
		powerOutlets.removeAll(primaryItemIdList);

		// include all the local power outlets
		primaryItemIdList.removeAll(localPowerOutletList);
		primaryItemIdList.addAll(0, localPowerOutletList);
	
		// include all the remote power outlets
		primaryItemIdList.removeAll(remotePowerOutletList);
		primaryItemIdList.addAll(0, remotePowerOutletList);
		
		return powerOutlets;
	}

	private List<Long> getBuswayPowerOutletItemIdsToDelete(List<Long> itemIds){
		
		List<Long> powerOutlets = itemDAO.getBuswayPowerPanelConnectedPowerOutlet(itemIds);
		// itemIds.removeAll(powerOutlets);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	

	private List<Long> getLocalPowerOutletItemIdsToDelete(List<Long> itemIds){
		List<Long> powerOutlets = itemDAO.getLocalPowerPanelConnectedPowerOutlet(itemIds);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	

	private List<Long> getRemotePowerOutletItemIdsToDelete(List<Long> itemIds){
		
		List<Long> powerOutlets = itemDAO.getRemotePowerPanelConnectedPowerOutlet(itemIds);
		// powerOutlets.removeAll(itemIds);
		return powerOutlets;
	}	


}
