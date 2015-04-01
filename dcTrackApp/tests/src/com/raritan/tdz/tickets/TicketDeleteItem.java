package com.raritan.tdz.tickets;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.lookup.SystemLookup;

public class TicketDeleteItem extends TicketsTestBase {

	/*
	 * Create a dummy item, create ticket and ticket fields and set to this item
	 * delete the item and use the same ticket to create a new item
	 * check the origin field
	 * delete the item 
	 * delete the ticket
	 * */
	@Test
	public final void testCreateItemWithTickets() throws Throwable {
		
		// Create dummy cabinet item
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// this will do the following
		// 1. Create a new item with the same value as dummy item
		// 2. Delete the dummy item
		// 3. Create a new ticket
		// 4. Create a new item using the ticket
		// 5. verify the origin field
		List<String> errorCodes = new ArrayList<String>();
		Long newItemId = testNewItemWithTicket(cabItem, errorCodes, 105L);

		// this will delete the created item against the ticket and the ticket
		testCleanup(newItemId);
		
	}
	
	// Delete the item when ticket is in the following states
	/*
	Received
	Incomplete
	Updated
	dcTrack Request Issued
	dcTrack Request Approved
	dcTrack Request Rejected
	dcTrack Request Updated
	dcTrack Workorder Issued
	dcTrack Workorder Complete
	Ticket Complete
	Ticket Archived 
	*/
	private void testDeleteItemInTicketStatus(Long testTicketStatus, Long uniqueClassMountingFF) throws Throwable {
		
		// Create dummy cabinet item
		CabinetItem cabItem = createCabinetInPlannedState();
		
		// this will do the following
		// 1. Create a new item with the same value as dummy item
		// 2. Delete the dummy item
		// 3. Create a new ticket
		// 4. Create a new item using the ticket
		// 5. verify the origin field
		List<String> errorCodes = new ArrayList<String>();
		Long newItemId = testNewItemWithTicket(cabItem, errorCodes, uniqueClassMountingFF);

		// Set the ticket status
		setTicketStatusUsingItem(newItemId, testTicketStatus);

		Long ticketId = ticketsDAO.getTicketId(newItemId);
		
		// Delete the item
		itemHome.deleteItem(newItemId, false, getTestAdminUser());
		
		session.flush();
		session.clear();
		
		// validate the ticket parameters
		validateTicketStatus(ticketId);
		// validate item id
		validateTicketItemId(ticketId);
		// validate other fields back to ticket
		validateModifiedTicketFieldsWithOriginalTicket(ticketId);
		
		// this will delete the created ticket
		testCleanupTicket(ticketId);
		
	}
	
	@Test
	public final void testDeleteItemInTicketReceived() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 105L);
	}

	@Test
	public final void testDeleteItemInTicketUpdated() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.UPDATED, 105L);
	}

	@Test
	public final void testDeleteItemInTicketIncomplete() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.INCOMPLETE, 105L);
	}

	@Test
	public final void testDeleteItemInTicketRequestIssued() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.REQUEST_ISSUED, 105L);
	}
	
	@Test
	public final void testModTicketDataForVM() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 100L);
	}

	@Test
	public final void testModTicketDataForDevBladeServer() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 105L);
	}

	@Test
	public final void testModTicketDataForDevBladeChassis() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 104L);
	}
	
	@Test
	public final void testModTicketDataForDevRackFixed() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 101L);
	}
	
	@Test
	public final void testModTicketDataForDevFSFixed() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 103L);
	}

	@Test
	public final void testModTicketDataForNwBlade() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 205L);
	}

	@Test
	public final void testModTicketDataForNwChassis() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 204L);
	}

	@Test
	public final void testModTicketDataForNwFsFixed() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 203L);
	}
	
	@Test
	public final void testModTicketDataForNwRackFixed() throws Throwable {
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 201L);
	}

	@Test
	public final void testModTicketDataForRpduZuFixed() throws Throwable { 
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 506L);
	}
	
	@Test
	public final void testModTicketDataForRpduRackFixed() throws Throwable { 
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 501L);
	}

	@Test
	public final void testModTicketDataForCabFs() throws Throwable { // failed
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 603L);
	}
	
	@Test
	public final void testModTicketDataForCabFsContainer() throws Throwable { // failed
		testDeleteItemInTicketStatus(SystemLookup.TicketStatus.RECEIVED, 613L);
	}

	
}
