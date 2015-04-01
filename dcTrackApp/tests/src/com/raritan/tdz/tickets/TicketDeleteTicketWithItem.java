package com.raritan.tdz.tickets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.request.ItemRequest.ItemRequestType;
import com.raritan.tdz.lookup.SystemLookup;

public class TicketDeleteTicketWithItem  extends TicketsTestBase {

	Map<Long, Long> matchingStages = null;
	
	private void initStageMap() {
		if (null == matchingStages) {
			matchingStages = new HashMap<Long, Long>();
			matchingStages.put(SystemLookup.TicketStatus.REQUEST_ISSUED, SystemLookup.RequestStage.REQUEST_ISSUED);
			matchingStages.put(SystemLookup.TicketStatus.REQUEST_APPROVED, SystemLookup.RequestStage.REQUEST_APPROVED);
			matchingStages.put(SystemLookup.TicketStatus.WORKORDER_ISSUED, SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			matchingStages.put(SystemLookup.TicketStatus.WORKORDER_COMPLETE, SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
			matchingStages.put(SystemLookup.TicketStatus.TICKET_COMPLETE, SystemLookup.RequestStage.REQUEST_COMPLETE);
			matchingStages.put(SystemLookup.TicketStatus.REQUEST_REJECTED, SystemLookup.RequestStage.REQUEST_REJECTED);
			matchingStages.put(SystemLookup.TicketStatus.REQUEST_UPDATED, SystemLookup.RequestStage.REQUEST_UPDATED);
		}
	}
	
	protected void validateTicketStatusWithItem(Long ticketId, Long itemId) throws DataAccessException {
		session.flush();
		session.clear();
		
		LksData currentItemStage = itemRequest.getLatestRequestStage(itemId);
		
		Long ticketStatus = ticketsDAO.getTicketStatusLkpCode(ticketId);
		
		Assert.assertTrue(currentItemStage.getLkpValueCode().longValue() == matchingStages.get(ticketStatus.longValue()));
		
	}
	
	private void setItemStage(Long itemId, Long stageLkpValueCode) throws DataAccessException {
		
		RequestHistory currentHistory = requestDAO.getCurrentHistory(itemId);
		currentHistory.setCurrent(false);
		
		session.update(currentHistory);
		
		RequestHistory updateCurrHistory = new RequestHistory();
		updateCurrHistory.setRequestDetail(currentHistory.getRequestDetail());
		updateCurrHistory.setRequestedBy(currentHistory.getRequestedBy());
		updateCurrHistory.setRequestedOn(currentHistory.getRequestedOn());
		updateCurrHistory.setStageIdLookup(SystemLookup.getLksData(session, stageLkpValueCode));
		updateCurrHistory.setCurrent(true);
		
		session.save(updateCurrHistory);
		
		session.flush();
		session.clear();
		
	}
	
	private void setItemStageAndValidate(Long itemId, Long ticketId, Long destRequestStage) throws DataAccessException {
		
		final List<Long> requestStagesTillComplete = new ArrayList<Long>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			add(SystemLookup.RequestStage.REQUEST_APPROVED);
			add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
			add(SystemLookup.RequestStage.REQUEST_COMPLETE);
			
		}};
		
		final List<Long> requestStagesUpdated = new ArrayList<Long>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			add(SystemLookup.RequestStage.REQUEST_UPDATED);
			
		}};

		final List<Long> requestStagesRejected = new ArrayList<Long>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			add(SystemLookup.RequestStage.REQUEST_REJECTED);
			
		}};
		
		List<Long> requestStages = null;
		if (requestStagesTillComplete.contains(destRequestStage)) {
			requestStages = requestStagesTillComplete;
		}
		else if (requestStagesUpdated.contains(destRequestStage)) {
			requestStages = requestStagesUpdated;
		}
		else if (requestStagesRejected.contains(destRequestStage)) {
			requestStages = requestStagesRejected;
		}
		else {
			requestStages = new ArrayList<Long>();
		}
		
		for (Long requestStage: requestStages) {
			setItemStage(itemId, requestStage);
			
			LksData currentItemStage = itemRequest.getLatestRequestStage(itemId);
	
			validateTicketStatusWithItem(ticketId, itemId);
			
			if (destRequestStage.equals(currentItemStage.getLkpValueCode())) {
				break;
			}
		}
		
	}
	
	private void setItemRequestStage(Long itemId, Long ticketId, Long itemRequestStage) throws BusinessValidationException, DataAccessException {
		
		if (null == itemRequestStage) return;
			
		initStageMap();
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		
		
		Map<Long,Long> requestIdMap = requestDAO.insertRequests(itemIds, ItemRequestType.installItem, "New Item", /*errors*/null, false, SystemLookup.ItemStatus.INSTALLED, false);
		requestTicketSaveBehavior.updateTicketFields(requestIdMap, null);
		// List<RequestDTO> requestDTOs = itemHome.itemRequest(itemIds, ItemActionMenuStatus.ID_REQ_INSTALL);
		
		session.flush();
		
		validateTicketStatusWithItem(ticketId, itemId);
		
		LksData currentItemStage = itemRequest.getLatestRequestStage(itemId);

		if (itemRequestStage.equals(currentItemStage.getLkpValueCode())) {
			return;
		}
		
		setItemStageAndValidate(itemId, ticketId, itemRequestStage);
		
	}
	
	// Delete the ticket when ticket is in the following states and has an item associated with the ticket
	/*
	Received
	Incomplete
	Updated
	dcTrack Request Issued
	dcTrack Request Approved
	dcTrack Request Rejected
	dcTrack Request Updated
	dcTrack Work order Issued
	dcTrack Work order Complete
	Ticket Complete
	Ticket Archived 
	*/
	private void testDeleteTicketInStatusWithItem(Long testTicketStatus, List<String> errorCodes, Long itemRequestStage) throws Throwable {
		
		// Create dummy cabinet item
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// this will do the following
		// 1. Create a new item with the same value as dummy item
		// 2. Delete the dummy item
		// 3. Create a new ticket
		// 4. Create a new item using the ticket
		// 5. verify the origin field
		Long newItemId = testNewItemWithTicket(cabItem, new ArrayList<String>(), 105L);

		// Set the ticket status
		if (null == itemRequestStage) {
			setTicketStatusUsingItem(newItemId, testTicketStatus);
		}

		Long ticketId = ticketsDAO.getTicketId(newItemId);

		// set the request stage for the item
		setItemRequestStage(newItemId, ticketId, itemRequestStage);

		// Delete the item
		// itemHome.deleteItem(newItemId, false, getTestAdminUser());
		
		session.flush();
		session.clear();
		
		// delete the ticket
		List<Long> ticketIdsToBeDeleted = new ArrayList<Long>();
		ticketIdsToBeDeleted.add(ticketId);
		try {
			
			deleteTicketWithExpectedErrorCode(ticketIdsToBeDeleted, errorCodes);
		}
		finally {
			if (errorCodes.size() > 0) {
				
				validateItemExist(newItemId);
				validateTicketExist(ticketId);
				
				testCleanup(newItemId);
				
				session.flush();
				
			}
			else {
				validateTicketDeleted(ticketId);
				validateLnEventTicketDeleteItem(newItemId);
				// Delete the item
				itemHome.deleteItem(newItemId, false, getTestAdminUser());

			}
		}
		
	}
	
	private void deleteTicketInAllowedStateItem(Long testTicketStatus) throws Throwable {
		
		List<String> errorCodes = new ArrayList<String>();
		testDeleteTicketInStatusWithItem(testTicketStatus, errorCodes, null);
		
	}
	
	private void deleteTicketInNotAllowedStateItem(Long testTicketStatus) throws Throwable {
	  	
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("TICKET_DEL_FAILED_IN_STATUS");
		initStageMap();
		Long itemRequestStage = matchingStages.get(testTicketStatus.longValue());
		testDeleteTicketInStatusWithItem(testTicketStatus, errorCodes, itemRequestStage);
			  
	}

	  @Test  
	  public final void deleteTicketInStateReceivedItem() throws Throwable {
		  	
		  deleteTicketInAllowedStateItem(SystemLookup.TicketStatus.RECEIVED);
		  
	  }

	  @Test  
	  public final void deleteTicketInStateUpdatedItem() throws Throwable {
		  	
		  deleteTicketInAllowedStateItem(SystemLookup.TicketStatus.UPDATED);
		  
	  }

	  @Test  
	  public final void deleteTicketInStateIncompleteItem() throws Throwable {
		  	
		  deleteTicketInAllowedStateItem(SystemLookup.TicketStatus.INCOMPLETE);
		  
	  }
	  
	  @Test(expectedExceptions=ServiceLayerException.class)
	  public final void deleteTicketInStateArchivedItem() throws Throwable {
		  	
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.TICKET_ARCHIVED);
		  
	  }

	  @Test(expectedExceptions=ServiceLayerException.class)  
	  public final void deleteTicketInStateRequestIssuedItem() throws Throwable {
		  	
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.REQUEST_ISSUED);
		  
	  }

	  @Test(expectedExceptions=ServiceLayerException.class)
	  public final void deleteTicketInStateCompleteItem() throws Throwable {
		  	
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.TICKET_COMPLETE);
		  
	  }

	  @Test(expectedExceptions=ServiceLayerException.class)
	  public final void deleteTicketInStateRequestApprovedItem() throws Throwable {
		  	
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.REQUEST_APPROVED);
		  
	  }

	  @Test(expectedExceptions=ServiceLayerException.class)
	  public final void deleteTicketInStateRequestRejectedItem() throws Throwable {
		  
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.REQUEST_REJECTED);
		  
	  }

	  @Test(expectedExceptions=ServiceLayerException.class)
	  public final void deleteTicketInStateRequestUpdatedItem() throws Throwable {
		  
		  deleteTicketInNotAllowedStateItem(SystemLookup.TicketStatus.REQUEST_UPDATED);
		  
	  }



}
