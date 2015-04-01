package com.raritan.tdz.item.home.itemObject;


import com.raritan.tdz.domain.Item;

public interface ItemNameGenerator {

	boolean shouldGenerateName(Item item);
	
	void generateName(Item item);
	
}
