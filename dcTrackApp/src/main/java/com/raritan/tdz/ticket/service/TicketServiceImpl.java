/**
 * 
 */
package com.raritan.tdz.ticket.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.home.TicketHome;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.ResponseStatus;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.page.home.ItemActionMenuStatus;
import com.raritan.tdz.session.FlexUserSessionContext;

/**
 * @author prasanna
 *
 */
public class TicketServiceImpl extends com.raritan.tdz.service.TicketServiceImpl implements TicketService {
	@Autowired
	private ItemRequest itemRequest;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	public TicketServiceImpl(TicketHome ticketHome, ItemHome itemHome) {
		super(ticketHome, itemHome);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.ticket.service.TicketService#processDiscardTicket(java.lang.Long, java.lang.Long)
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void processDiscardTicketOnly(Long ticketId, final Long itemId, Long typeOfRequest)
			throws ServiceLayerException {
		List<Long> itemIds = new ArrayList<Long>() {{ add(itemId); }};
		
		String typeOfRequestStr = getDiscardType(typeOfRequest);
		
		// Generate the discard item change request
		
		Map<Long, Long> requestIds = new HashMap<Long, Long>();
		if (typeOfRequestStr != null && typeOfRequestStr.equals(ItemActionMenuStatus.ID_REQ_TO_ARCHIVE))
			requestIds = this.itemRequest.decommisionItemToArchiveRequest(itemIds, FlexUserSessionContext.getUser());
		else if (typeOfRequestStr != null && typeOfRequestStr.equals(ItemActionMenuStatus.ID_REQ_TO_STORAGE))
			requestIds = this.itemRequest.decommisionItemToStorageRequest(itemIds, FlexUserSessionContext.getUser());
		
		// Update the ticket status
		if (requestIds != null && requestIds.size() > 0) {
			this.ticketHome.setTicketRequest(ticketId, requestIds.get(itemId));
		}

	}
	
	private String getDiscardType(Long discardLkpValueCode){
		
		String discardType = null;
		List<LksData> discardLks = systemLookupFinderDAO.findByLkpValueCode(discardLkpValueCode);
		if (null == discardLks || discardLks.size() <= 0) {
			discardLks = systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.ARCHIVED);
		}
		
		if (discardLks.get(0).getLkpValueCode().equals(SystemLookup.ItemStatus.ARCHIVED))
			discardType = ItemActionMenuStatus.ID_REQ_TO_ARCHIVE;
		else if (discardLks.get(0).getLkpValueCode().equals(SystemLookup.ItemStatus.STORAGE))
			discardType = ItemActionMenuStatus.ID_REQ_TO_STORAGE;
		
		return discardType;
	}

}
