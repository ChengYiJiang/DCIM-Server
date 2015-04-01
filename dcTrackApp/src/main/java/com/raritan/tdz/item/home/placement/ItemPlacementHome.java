package com.raritan.tdz.item.home.placement;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Generic home for retrieving available placement information for a given item.
 * 
 * @author Andrew Cohen
 */
@Transactional(readOnly = true)
public interface ItemPlacementHome {

	/**
	 * Gets a collection of available positions for an item.
	 * This collection of positions is context dependent on the item class.
	 * For Example:
	 * 		For cabinet item, this is positions in row for current row label.
	 * 		For rackable items, it is available U-positions based on current cabinet.
	 * 
	 * @param item the item to get available positions for
	 * @param expItem the item currently being modified
	 * @throws DataAccessException
	 * @return
	 */
	public Collection<Long> getAvailablePositions( Item item, Item expItem ) throws DataAccessException;

}
