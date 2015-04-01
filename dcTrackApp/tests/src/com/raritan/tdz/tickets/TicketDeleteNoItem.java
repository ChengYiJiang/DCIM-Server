package com.raritan.tdz.tickets;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;

public class TicketDeleteNoItem extends TicketsTestBase {
	
  	
	public void deleteTicketInStateNoItem(Long testTicketStatus, List<String> errorCodes) throws ServiceLayerException {
	  
		// Create ticket with ticket fields
		Tickets tickets = createTicketFields(105L);
	  
		Long ticketId = tickets.getTicketId();
		// Set the status to the required status under test
		setTicketStatus(ticketId, testTicketStatus);
	  
		// delete the ticket
		List<Long> ticketIdsToBeDeleted = new ArrayList<Long>();
		ticketIdsToBeDeleted.add(ticketId);
		try {
			deleteTicketWithExpectedErrorCode(ticketIdsToBeDeleted, errorCodes);
		}
		finally {
			if (errorCodes.size() > 0) {
				testCleanupTicket(ticketId);
				
				session.flush();
				
			}
			else {
				validateTicketDeleted(ticketId);
			}
		}
	}
  
	private void deleteTicketInAllowedStateNoItem(Long testTicketStatus) throws ServiceLayerException {
		
		List<String> errorCodes = new ArrayList<String>();
		deleteTicketInStateNoItem(testTicketStatus, errorCodes);
	}
	
  private void deleteTicketInNotAllowedStateNoItem(Long testTicketStatus) throws ServiceLayerException {
	  	
	  List<String> errorCodes = new ArrayList<String>();
	  errorCodes.add("TICKET_DEL_FAILED_IN_STATUS");
	  deleteTicketInStateNoItem(testTicketStatus, errorCodes);
		  
  }
	
  @Test  
  public final void deleteTicketInStateReceivedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInAllowedStateNoItem(SystemLookup.TicketStatus.RECEIVED);
	  
  }

  
  @Test  
  public final void deleteTicketInStateUpdatedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInAllowedStateNoItem(SystemLookup.TicketStatus.UPDATED);
	  
  }
  
  @Test  
  public final void deleteTicketInStateIncompleteNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInAllowedStateNoItem(SystemLookup.TicketStatus.INCOMPLETE);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)
  public final void deleteTicketInStateArchivedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.TICKET_ARCHIVED);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)  
  public final void deleteTicketInStateRequestIssuedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.REQUEST_ISSUED);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)
  public final void deleteTicketInStateCompleteNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.TICKET_COMPLETE);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)
  public final void deleteTicketInStateRequestApprovedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.REQUEST_APPROVED);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)
  public final void deleteTicketInStateRequestRejectedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.REQUEST_REJECTED);
	  
  }

  @Test(expectedExceptions=ServiceLayerException.class)
  public final void deleteTicketInStateRequestUpdatedNoItem() throws ServiceLayerException {
	  	
	  deleteTicketInNotAllowedStateNoItem(SystemLookup.TicketStatus.REQUEST_UPDATED);
	  
  }


}
