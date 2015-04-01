package com.raritan.tdz.vpc.factory;

import com.raritan.tdz.domain.Item;

public interface ItemFactory {

	/**
	 * get the item using spring's item factory
	 * @param beanId
	 * @return
	 */
	public Item getItem(String beanId);
	
}
