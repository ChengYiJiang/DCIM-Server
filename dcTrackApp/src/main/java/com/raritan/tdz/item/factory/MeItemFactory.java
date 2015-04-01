/**
 * 
 */
package com.raritan.tdz.item.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemDomainAdaptor;

/**
 * @author prasanna
 *
 */
public class MeItemFactory implements ItemFactory {
	
	@Autowired
	ItemDomainAdaptor itemDomainAdaptor;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.home.ItemFactory#createItem(java.util.List)
	 */
	@Override
	public Item createItem(Map<String,Object> propertyMap, String unit)
			throws IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException, ClassNotFoundException, 
			BusinessValidationException, DataAccessException {
		MeItem item = new MeItem();
		itemDomainAdaptor.convert(item, MapToValueIdDTOList.convert(propertyMap), unit);
		return item;
	}

}
