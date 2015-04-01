/**
 * 
 */
package com.raritan.tdz.externalticket.dao;

import java.util.List;

/**
 * This interface will have methods to get external ticket data
 * @author prasanna
 *
 */
public interface ExternalTicketFinderDAO {
	/**
	 * Given item id, get the ticket id.
	 * For now it returns a list of size == 0 or 1
	 * @param itemId
	 * @return
	 */
	public List<Long> findTicketIdByItemId(Long itemId);
}
