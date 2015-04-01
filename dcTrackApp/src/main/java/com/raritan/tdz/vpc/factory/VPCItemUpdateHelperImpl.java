package com.raritan.tdz.vpc.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;

/**
 * vpc items update helper
 * @author bunty
 *
 */
public class VPCItemUpdateHelperImpl implements VPCItemUpdateHelper {

	@Autowired
	private ItemFactory itemFactory;

	@Autowired
	private ItemDAO itemDAO;

	@Autowired
	private LksCache lksCache;
	
	@Override
	public void updateClass(Item item, Long classLkpValueCode) {
		
		item.setClassLookup(lksCache.getLksDataUsingLkpCode(classLkpValueCode));

	}

	@Override
	public void updateSubClass(Item item, Long subClassLkpValueCode) {
		
		item.setSubclassLookup(lksCache.getLksDataUsingLkpCode(subClassLkpValueCode));

	}
	
	@Override
	public Item getNewItem(String vpcItemReference) {
		Item item = itemFactory.getItem(vpcItemReference);
		
		return item;
	}
	
	@Override
	public void addItem(Item item, List<Item> items) {
		
		Long itemId = itemDAO.saveItem(item);
		item = itemDAO.getItem(itemId);
		
		items.add(item);
	}

}
