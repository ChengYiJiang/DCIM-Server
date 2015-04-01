/**
 * 
 */
package com.raritan.tdz.ticket.service;

import com.raritan.tdz.exception.ServiceLayerException;

/**
 * @author prasanna
 *
 */
public interface TicketService extends com.raritan.tdz.service.TicketService{
	
	public void processDiscardTicketOnly(Long ticketId, Long itemId, Long typeOfRequest) throws ServiceLayerException;

}
