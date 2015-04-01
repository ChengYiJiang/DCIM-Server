package com.raritan.tdz.vpc.factory;

import java.util.List;

import com.raritan.tdz.domain.Item;

/**
 * helper interface to perform some common operations in the VPC framwork
 * @author bunty
 *
 */
public interface VPCItemUpdateHelper {

	
	/**
	 * updates the class of the item
	 * @param item 
	 * @param classLkpValueCode
	 */
	public void updateClass(Item item, Long classLkpValueCode);
	
	/**
	 * update the subclass of the item
	 * @param item
	 * @param subClassLkpValueCode
	 */
	public void updateSubClass(Item item, Long subClassLkpValueCode);

	/**
	 * get new item from the vpc item factory
	 * @param vpcItemReference
	 * @return
	 */
	public Item getNewItem(String vpcItemReference);

	/**
	 * add the item to the list
	 * @param item
	 * @param items
	 */
	public void addItem(Item item, List<Item> items);

}
