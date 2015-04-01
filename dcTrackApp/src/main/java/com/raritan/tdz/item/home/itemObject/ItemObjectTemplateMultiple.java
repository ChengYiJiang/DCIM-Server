/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * This itemObjectTemplate implementation will perform a split of
 * valueIdDTO list to perform save on multiple items. For example
 * this can be used by Device/Network FreeStanding item  where
 * the valueIdDTO will be split for a Cabinet and a Device/Network
 * Item and will be individually processed.
 * @author prasanna
 *
 */
public class ItemObjectTemplateMultiple extends ItemObjectTemplateBase {
	private Map<String,ItemObjectTemplate> itemObjectTemplateMap;
	
	private ItemSaveDTOHelper saveDTOHelper;
	
	private ItemObjectTemplate modelChangeItemObjectTemplate;
	
	private List<String> deleteItemDomainOrderList;
	
	
	
	public ItemObjectTemplateMultiple(ItemSaveDTOHelper saveDTOHelper, Map<String,ItemObjectTemplate> itemObjectTemplateMap) {
		this.saveDTOHelper = saveDTOHelper;
		this.itemObjectTemplateMap = itemObjectTemplateMap;
	}


	public ItemSaveDTOHelper getSaveDTOHelper() {
		return saveDTOHelper;
	}



	public void setSaveDTOHelper(ItemSaveDTOHelper saveDTOHelper) {
		this.saveDTOHelper = saveDTOHelper;
	}


	public Map<String, ItemObjectTemplate> getItemObjectTemplateMap() {
		return itemObjectTemplateMap;
	}


	public void setItemObjectTemplateMap(
			Map<String, ItemObjectTemplate> itemObjectTemplateMap) {
		this.itemObjectTemplateMap = itemObjectTemplateMap;
	}
	
	public ItemObjectTemplate getModelChangeItemObjectTemplate() {
		return modelChangeItemObjectTemplate;
	}


	public void setModelChangeItemObjectTemplate(
			ItemObjectTemplate modelChangeItemObjectTemplate) {
		this.modelChangeItemObjectTemplate = modelChangeItemObjectTemplate;
	}


	public List<String> getDeleteItemDomainOrderList() {
		return deleteItemDomainOrderList;
	}


	public void setDeleteItemDomainOrderList(List<String> deleteItemDomainOrderList) {
		this.deleteItemDomainOrderList = deleteItemDomainOrderList;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#getItemDetails(java.lang.Long, java.lang.String)
	 */
	@Override
	public Map<String, UiComponentDTO> getItemDetails(Long itemId, String unit)
			throws Throwable {
		saveDTOHelper.setItemId(itemId);
		
		
		for (Map.Entry<String, ItemObjectTemplate> entry:itemObjectTemplateMap.entrySet()){
			String domainClass = entry.getKey();
			ItemObjectTemplate itemObjectTemplate = entry.getValue();
			Long domainClassItemId = saveDTOHelper.getItemId(domainClass);
			if (domainClassItemId != null)
				saveDTOHelper.setSaveItemResult(domainClass, itemObjectTemplate.getItemDetails(saveDTOHelper.getItemId(domainClass), unit));
		}
		
		return saveDTOHelper.mergeResults();
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.itemObject.ItemObjectTemplate#saveItem(java.lang.Long, java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<String, UiComponentDTO> saveItem(Long itemId,
			List<ValueIdDTO> valueIdDTOList, UserInfo userInfo)
			throws BusinessValidationException,Throwable {
		
		if ((itemId != null && itemId > 0) && hasModelChanged(itemId)){
			if (modelChangeItemObjectTemplate != null)
				return modelChangeItemObjectTemplate.saveItem(itemId, valueIdDTOList, userInfo);
		}
		
		//Call preinit
		saveDTOHelper.preInit(itemId, valueIdDTOList);
		
		//Split the valueIdDTO list with the helper
		Map<String,List<ValueIdDTO>> splitMap = saveDTOHelper.splitValueIdDTO(itemId, valueIdDTOList, userInfo);
		
		//For each entry in splitMap
		for (Map.Entry<String, ItemObjectTemplate> entry:itemObjectTemplateMap.entrySet()){
			
			String domainClass = entry.getKey();
			
			List<ValueIdDTO> itemValueIdDTOList = splitMap.get(domainClass);
			
			//Call preSave routine.
			saveDTOHelper.preSave(domainClass, itemValueIdDTOList);
			
			ValueIdDTOHolder.capture(itemValueIdDTOList);
			
			//Get the itemObjectTemplate from factory given ValueIdDTOList
			ItemObjectTemplate itemObjectTemplate = entry.getValue();
			
			//Call the template's saveItem method
			Map<String,UiComponentDTO> resultMap = itemObjectTemplate.saveItem(saveDTOHelper.getItemId(domainClass), itemValueIdDTOList, userInfo);
			
			//Save the result in the dto helper
			saveDTOHelper.setSaveItemResult(domainClass, resultMap);
			
			ValueIdDTOHolder.clearCurrent();
		}
		
		//Call the merge
		Map<String,UiComponentDTO> mergeResultMap = saveDTOHelper.mergeResults();
		
		//Return the merged map.
		
		return mergeResultMap;
	}
	
	@Override
	public void validateDeleteItem(Long itemId, boolean validate, UserInfo userInfo) throws BusinessValidationException, Throwable {
		saveDTOHelper.setItemId(itemId);
		if (deleteItemDomainOrderList != null){
			for (String domainClass: deleteItemDomainOrderList){
				ItemObjectTemplate itemObjectTemplate = itemObjectTemplateMap.get(domainClass);
				Long domainClassItemId = saveDTOHelper.getItemId(domainClass);
				if (domainClassItemId != null)
					itemObjectTemplate.validateDeleteItem(domainClassItemId, validate, userInfo);
			}
		}
	}
	
	@Override
	public void deleteItem(Long itemId, boolean validate, UserInfo userInfo) throws Throwable {
		saveDTOHelper.setItemId(itemId);
		if (deleteItemDomainOrderList != null){
			for (String domainClass: deleteItemDomainOrderList){
				ItemObjectTemplate itemObjectTemplate = itemObjectTemplateMap.get(domainClass);
				Long domainClassItemId = saveDTOHelper.getItemId(domainClass);
				if (domainClassItemId != null)
					itemObjectTemplate.deleteItem(domainClassItemId, validate, userInfo);
			}
		}
	}
}
