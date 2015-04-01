package com.raritan.tdz.item.dao;

import java.sql.Timestamp;
import java.util.List;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

public interface ItemFinderDAO {
	/**
	 * Find the EAssetTag by item Id
	 * Please note that although this is returning
	 * a list of string, it is basically having one
	 * element within the list.
	 * @param itemId
	 * @return
	 */
	public List<String> findEAssetTagById(Long itemId);
	
    /**
     * Find item name by item id
     */
	public List<String> findItemNameById(Long itemId);
	
	/**
	 * Find isAssetTagVerified given a item id
	 * @param itemId
	 * @return
	 */
	public List<Boolean> findEAssetTagVerifiedById(Long itemId);
	
	/**
	 * Get the child items given parent Id
	 * @param parentId
	 * @return
	 */
	public List<Item> findChildItemsFromParentId(Long parentId);
	
	/**
	 * Given a parent id and the class, this will find out the number of children
	 * of that type within the parent. The result list will be always size of 1
	 * @param parentId
	 * @return
	 */
	public List<Long> findChildCountFromParentIdAndClass(Long parentId,Long classLkpValueCode);
	
	/**
	 * Get the child items given parent id and type of child items
	 * @param parentId
	 * @param classLkpValueCodes
	 * @return
	 */
	public List<Item> findChildItemsFromParentIdAndClass(Long parentId, Long classLkpValueCodes);
	
	/**
	 * Get the count of the child items given a parent Id
	 * @param parentId
	 * @return
	 */
	public List<Long> findChildCountFromParentId(Long parentId);
	
	/**
	 * Get Item details based on item id
	 * (There shouls be only one element in the list)
	 * @param id - item id
	 * @return  - item
	 */
	public List<Item> findById(Long id);
	
	/**
	 * Get item details using item name
	 * @param itemName
	 * @return
	 */
	public List<Item> findByName(String itemName);
	
	public List<Item> findItemsByCreationDate(Timestamp creationDate);
	
	/**
	 * return list of all items that are of specific class (e.g. all UPS banks, all fpdus, etc)
	 * 
	 * @param classLksId - item's class_lks_id
	 * @return
	 */
	public List<MeItem> findAllMeItemsByClass( Long classLksId );
	
	public List<MeItem> findMeItemsBySubClass(List<Long> subClassLksId);
	
	/*
	 * Return list of all blade items in the chassis
	 * @param - chassis item id
	 * @return - list of blade items
	 */
	public List<Item> findAllBladesInChassisSorted( Long itemId );
	
	/**
	 * 
	 * @param classLkpValueCode
	 * @return
	 */
	public List<Long> findAllItemIdsByClass( Long classLkpValueCode );
	
	
	public List<Item> findItemByPIQId(Integer piqId, String appSettingLkpValue, Long appSettingLkpValueCode);
}
