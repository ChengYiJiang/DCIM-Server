package com.raritan.tdz.move.home;

import java.util.List;

import org.hibernate.Criteria;

public interface MoveHome {

	/**
	 * filter out all the move cabinets from the criteria
	 * @param criteria
	 */
	public void filterMoveCabinets(Criteria criteria);

	/**
	 * get moving and moved item ids against the given move or moving item id
	 * this can be used in the placement sharing between moved and moving items
	 * @param itemId
	 * @return
	 */
	public List<Long> getExceptionItemIds(Long itemId);

	/**
	 * get moving and moved item ids against the given move or moving item id
	 * this can be used in the placement sharing between moved and moving items
	 * @param itemId
	 * @return
	 */
	public List<Long> getExceptionItemIds(List<Long> itemId);
	
}
