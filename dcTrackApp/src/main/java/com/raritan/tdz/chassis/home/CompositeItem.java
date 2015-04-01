package com.raritan.tdz.chassis.home;

import java.util.List;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemObject;

/**
 * An item that is composed of other items.
 * @author Andrew Cohen
 */
public interface CompositeItem extends ItemObject {

	public List<Item> getChildItems() throws DataAccessException;
	
	public int getChildItemCount() throws DataAccessException;
}
