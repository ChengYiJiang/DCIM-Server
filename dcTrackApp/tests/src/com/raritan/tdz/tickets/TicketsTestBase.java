package com.raritan.tdz.tickets;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.junit.Assert;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.helper.TestTicketHelper;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public class TicketsTestBase extends TestBase {
	
	UserInfo userInfo = null;
	TestTicketHelper testHelper = null;

	
	private void testInit() {

		userInfo = getTestAdminUser();
		
		testHelper = new TestTicketHelper(itemHome, userInfo);
		
	}
	
	protected void setTicketStatus(Long ticketId, Long status) {
		session.flush();
		session.clear();
		
		Tickets tickets = ticketsDAO.read(ticketId);
		
		ticketsDAO.initializeAndUnproxy(tickets);
		
		LksData ticketStatus = SystemLookup.getLksData(session, status);
		
		tickets.setStatusLks(ticketStatus);
		
		ticketsDAO.merge(tickets);
		
	}
	
	protected void setTicketStatusUsingItem(Long itemId, Long status) {
		session.flush();
		session.clear();
		
		Long ticketId = ticketsDAO.getTicketId(itemId);
		
		setTicketStatus(ticketId, status);
		
	}
	
	protected void testCleanupTicket(Long ticketId) {

		session.flush();
		session.clear();
		
		deleteTickets(ticketId);
		
		testHelper = null;
		userInfo = null;
		
	}

	protected void testCleanupItem(Long itemId) throws BusinessValidationException, Throwable {
		session.flush();
		session.clear();
		
		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());
		
		testHelper = null;
		userInfo = null;

	}
	

	protected void testCleanup(Long itemId) throws BusinessValidationException, Throwable {
		session.flush();
		session.clear();
		
		Long ticketId = ticketsDAO.getTicketId(itemId);
		
		session.flush();
		session.clear();
		
		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());
		
		session.flush();
		session.clear();
		
		deleteTickets(ticketId);
		
		testHelper = null;
		userInfo = null;

	}
	
	protected void deleteTickets(Long ticketId) {
		
		Tickets tickets = ticketsDAO.read(ticketId);
		
		Tickets updatedTickets = ticketsDAO.initializeAndUnproxy(tickets);
		
		ticketsDAO.delete(updatedTickets);
		
		session.flush();
		
	}
	
	protected Tickets createTicket() {
		
		java.util.Date date= new java.util.Date();
		LksData ticketStatus = SystemLookup.getLksData(session, SystemLookup.TicketStatus.RECEIVED); 
		LksData ticketAction = SystemLookup.getLksData(session, SystemLookup.TicketAction.ADD_ITEM);
		Timestamp timestamp = new Timestamp(date.getTime());
	
		Tickets tickets = new Tickets();
		tickets.setTicketNumber("TESTTKT001");
		tickets.setDescription("test description");
		tickets.setSystemVendor("test Vendor");
		tickets.setSystemId("test System ID");
		tickets.setUserName("test user");
		tickets.setCreationDate(timestamp);
		
		Tickets tickets_ = new Tickets("TKT001", "test description", "test Vendor", "test System Id", "test user", timestamp, timestamp, 
				null, ticketStatus, ticketAction, timestamp, timestamp, "status comments", "", "Add Item", null);
		
		ticketsDAO.create(tickets_);
		
		return tickets_;
		
	}
	
	// 100
	TicketFields vmFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
			"IBM", null, "Numac Series", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
			"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
			"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	// 105
	TicketFields devBladeServerFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
			"Dell", null, "Blade PowerEdge M710", null, "2G", null, -1, "", null, -1, "test-serial", "test-asset", 
			"Server", null, "Development", null, false, "10", 10, "GUUS TEST", null, null, "Front", null, "3", null, 
			"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	// 104
	TicketFields devBladeChassisFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
			"Dell", null, "PowerEdge 1955 Chassis", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
			"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
			"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	// 101
	TicketFields devRackFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
				"HP", null, "Proliant DL140 G3", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
				"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
				"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 103
	TicketFields devFSFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
					"HP", null, "StorageWorks XP10000", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
					"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
					"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	// 205
	TicketFields nwBladeFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
						"Dell", null, "PowerEdge 1955blade", null, "2G", null, -1, "", null, -1, "test-serial", "test-asset", 
						"Network Switch", null, "Network", null, false, "10", 10, "GUUS TEST", null, null, "Front", null, "3", null, 
						"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	// 204
	TicketFields nwChassisFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Dell", null, "PowerEdge 1955", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Network Switch", null, "Network", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 203
	TicketFields nwFSFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Ciena", null, "ActivFlex 5430", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Network Switch", null, "Network", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 201
	TicketFields nwRackFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"HP", null, "StorageWorks MP Router", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Network Switch", null, "Network", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 301
	TicketFields dpRackFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Panduit", null, "1RU-Fiber Panel-FRME1U FC 24-port", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Copper PP", null, null, null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 412
	TicketFields poBuswayFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Universal Electric Corporation", null, "Starline Type 01", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"", null, "", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 411
	TicketFields poNRFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Hubbel", null, "Electrical Outlet Box", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"", null, "", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 506
	TicketFields rpduZuFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Raritan", null, "Dominion PX DPCS20-20", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Dual", null, "", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 501
	TicketFields rpduRackFixedFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"Raritan", null, "Dominion PX DPCR20-20", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Single", null, "", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 603
	TicketFields cabFSFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
							"HP", null, "40RU-HP Cabinet", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Wall Field", null, "dcT 3.0 Demo", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// 613
	TicketFields cabContainerFSFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Cabinet", null, "Container", null, 
							"HP", null, "xxxx", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"Rack", null, "dcT 3.0 Demo", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	// -1: no make/model, no class/subclass
	TicketFields invalidItemFields = new TicketFields(null, "Site A", null, "test ticket item name", null, "xxxx", null, "xxxx", null, 
							"xxxx", null, "xxxx", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
							"", null, "", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
							"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);

	TicketFields fields = new TicketFields(null, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
			"Dell", null, "Blade PowerEdge M710", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
			"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
			"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
	
	@SuppressWarnings("serial")
	final Map<Long, TicketFields> uniqueValToTicketMap = 
			Collections.unmodifiableMap(new HashMap<Long, TicketFields>() {{
				
				put(-2L, fields);
				
				put(-1L, invalidItemFields);
				
				put(100L, vmFields);
				put(105L, devBladeServerFields);
				put(104L, devBladeChassisFields);
				put(101L, devRackFixedFields);
				put(103L, devFSFixedFields);
				// put(102L, null);
				// put(106L, null);
				
				put(205L, nwBladeFields);
				put(204L, nwChassisFields);
				put(203L, nwFSFixedFields);
				put(201L, nwRackFixedFields);
				// put(202L, null);
				// put(206L, null);
				
				put(301L, dpRackFixedFields);
				put(302L, dpRackFixedFields);
				
				put(412L, poBuswayFixedFields);
				put(411L, poNRFixedFields);
				
				put(506L, rpduZuFixedFields);
				put(501L, rpduRackFixedFields);
				// put(502L, null);
				
				put(603L, cabFSFields);
				put(613L, cabContainerFSFields); // Cab Container
				
			}});
	
	private TicketFields newTicketFields(Long uniqueClassMountingFF) {
		
		return uniqueValToTicketMap.get(uniqueClassMountingFF);
		
	}
	
	@SuppressWarnings("unused")
	private TicketFields newTicketFields(Tickets tickets, Long uniqueClassMountingFF) {
		TicketFields fields = new TicketFields(tickets, "Site A", null, "test ticket item name", null, "Network", null, "networkstack", null, 
				"Dell", null, "Blade PowerEdge M710", null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
				"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
				"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
		
		return fields;
	}
	
	private TicketFields newTicketFields(Tickets tickets, String make, String model, String itemClass, String subClass) {
		TicketFields fields = new TicketFields(tickets, "Site A", null, "test ticket item name", null, itemClass, null, subClass, null, 
				make, null, model, null, "1A", null, -1, "", null, -1, "test-serial", "test-asset", 
				"Server", null, "Development", null, false, "10", 10, "BN-CH-2", null, null, "Front", null, "Tre", null, 
				"Back", null, "Left", null, 3, "Left", null, "Front", null, "North", null);
		
		return fields;
	}

	
	protected Tickets createTicketFields(Long uniqueClassMountingFF) throws BusinessValidationException {
		
		Tickets tickets = createTicket();
		
		Set<TicketFields> ticketFields = new HashSet<TicketFields>();
		
		TicketFields fields = newTicketFields(uniqueClassMountingFF);
		fields.setTickets(tickets);
		ticketFieldsDAO.create(fields);
		ticketFields.add(fields);
		
		// Create a dummy modified field
		TicketFields modTicketFields = (TicketFields) fields.clone();
		modTicketFields.setIsModified(true);
		ticketFieldsDAO.create(modTicketFields);
		ticketFields.add(modTicketFields);
		
		tickets.setTicketFields(ticketFields);
		ticketsDAO.merge(tickets);
		
		// set the correct modified fields
		resetTicket(tickets.getTicketId());
		
		ticketsDAO.merge(tickets);
		
		return tickets;
		
	}
	
	protected Tickets createTicketFields(String make, String model, String itemClass, String subClass) throws BusinessValidationException {
		
		Tickets tickets = createTicket();
		
		Set<TicketFields> ticketFields = new HashSet<TicketFields>();
		
		TicketFields fields = newTicketFields(tickets, make, model, itemClass, subClass);
		ticketFieldsDAO.create(fields);
		ticketFields.add(fields);
		
		// Create a dummy modified field
		TicketFields modTicketFields = (TicketFields) fields.clone();
		modTicketFields.setIsModified(true);
		ticketFieldsDAO.create(modTicketFields);
		ticketFields.add(modTicketFields);
		
		tickets.setTicketFields(ticketFields);
		ticketsDAO.merge(tickets);
		
		// set the correct modified fields
		resetTicket(tickets.getTicketId());
		
		ticketsDAO.merge(tickets);
		
		return tickets;
		
	}

	private List<ValueIdDTO> getNewItemDTOWithTicket(Item item, Long uniqueClassMountingFF) throws Throwable {
		
		Tickets ticket = createTicketFields(uniqueClassMountingFF);
		
		List<ValueIdDTO> itemDto = testHelper.getDto(item.getItemId());

		itemDto = testHelper.setItemTicket(itemHome, userInfo, item.getItemId(), ticket.getTicketId());
		
		List<String> errorCodesOriginal = new ArrayList<String>(); // no error expected
		testHelper.saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodesOriginal);
		
		itemDto = testHelper.setItemTicket(itemHome, userInfo, item.getItemId(), ticket.getTicketId());
		
		itemHome.deleteItem(item.getItemId(), false, userInfo);
		
		return itemDto;
		
	}
	
	protected Long testNewItemWithTicket(Item item, List<String> errorCodes, Long uniqueClassMountingFF) throws Throwable {
		
		testInit();
		
		List<ValueIdDTO> itemDto = getNewItemDTOWithTicket(item, uniqueClassMountingFF);
		
		session.flush();
		
		// save item with expected error code
		Long itemId = testHelper.saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes, new ArrayList<String>());

		session.flush();
		
		session.clear();
		
		validateOriginField(itemId);
		
		// Validate Placement
		Long ticketId = ticketsDAO.getTicketId(itemId);
		
		validatePlacement(ticketId);
		// validateTicketItemLocation(ticketId); // location
		
		// Validate Hardware Info
		validateTicketItemHardwareInfo(ticketId);
		
		// Validate Identity Info
		validateTicketItemIdentityInfo(ticketId);
		
		// TODO:: set the item ids for the ports (data, power and sensor)
		
		return itemId;

	}
	
	protected void validatePlacement(Long ticketId) {
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Long uniqueItemCode = item.getClassMountingFormFactorValue();
		
		// Free-Standing and Cabinet
		if (uniqueItemCode == 603L || uniqueItemCode == 613L || uniqueItemCode == 103L || uniqueItemCode == 203L) {
			validateTicketItemCabinetPlacement(ticketId);
		}
		
		// Blade
		else if (uniqueItemCode == 105L || uniqueItemCode == 205L) {
			validateTicketItemBladePlacement(ticketId);
		}
		
		// Rackable
		else if (uniqueItemCode == 104L || uniqueItemCode == 101L || uniqueItemCode == 204L || uniqueItemCode == 201L || uniqueItemCode == 301L || uniqueItemCode == 501L || uniqueItemCode == 701L) {
			validateTicketItemRackablePlacement(ticketId);
		}
		
		// Non Rackable
		else if (uniqueItemCode == 102L || uniqueItemCode == 202L || uniqueItemCode == 502L || uniqueItemCode == 702L) {
			validateTicketItemNonRackablePlacement(ticketId);
		}
		
		// Data Store (VM) 
		else if (uniqueItemCode == 100L) {
			validateTicketItemLocation(ticketId);
		}
		
		// Zero-U
		else if (uniqueItemCode == 106L || uniqueItemCode == 206L || uniqueItemCode == 306L || uniqueItemCode == 506L || uniqueItemCode == 706L) {
			validateTicketItemZeroUPlacement(ticketId);
		}
		
	}

	protected CabinetItem createCabinetInPlannedState() throws Throwable {
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-TICKET-CABINET-TEST", null);
		
		return cabinetItem;
	}
	
	protected CabinetItem createCabinetInInstalledState() throws Throwable {
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-TICKET-CABINET-TEST", SystemLookup.ItemStatus.INSTALLED);
		
		return cabinetItem;
	}

	
	private void validateOriginField(Long itemId) {
		Long originLkpValueCode = getItemOriginFromDB(itemId);
		Assert.assertEquals(originLkpValueCode.longValue(), SystemLookup.ItemOrigen.TICKET);
	}
	
	private Long getItemOriginFromDB(Long itemId) {
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("itemServiceDetails", "itemServiceDetails");
		criteria.createAlias("itemServiceDetails.originLookup", "originLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("originLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		return (Long) criteria.uniqueResult();
	}

	protected void validateTicketStatus(Long ticketId) {
		Long ticketStatusLkpValueCode = ticketsDAO.getTicketStatusLkpCode(ticketId); //getTicketStatusFromDB(ticketId);
		Assert.assertEquals(ticketStatusLkpValueCode.longValue(), SystemLookup.TicketStatus.RECEIVED);
	}
	
	protected void validateTicketDeleted(Long ticketId) {
		Tickets tickets = ticketsDAO.read(ticketId);
		Assert.assertNull(tickets);
	}
	
	protected void validateTicketExist(Long ticketId) {
		Tickets tickets = ticketsDAO.read(ticketId);
		Assert.assertNotNull(tickets);
	}
	
	protected void validateTicketStatus(Long ticketId, Long ticketStatus) {
		Long statusLkpValueCode = ticketsDAO.getTicketStatusLkpCode(ticketId);
		if (null == statusLkpValueCode) {
			Assert.fail("Cannot get the ticket status");
		}
		Assert.assertEquals(ticketStatus.longValue(), statusLkpValueCode.longValue());
	}

	protected void validateItemExist(Long itemId) {
		Item item = itemDAO.read(itemId);
		Assert.assertNotNull(item);
	}

	protected void validateTicketItemId(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Assert.assertNull(ticketFields.getItemId());
		Assert.assertNull(ticketFields.getItem());
	}

	protected void resetTicket(Long ticketId) throws BusinessValidationException {

		Map<String, Object> targetMap = new HashMap<String, Object>();
		
		// Long ticketId = ticketsDAO.getTicketId(item.getItemId());
		if (null == ticketId || ticketId <= 0) return;
		
		Tickets ticket = ticketsDAO.read(ticketId);
		
		targetMap.put(Tickets.class.getName(), ticket);
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		
		targetMap.put(TicketFields.class.getName(), ticketFields);
		// delete the modified ticket field
		// ticketFieldsDAO.delete(ticketFields);

		// add power supply port
		TicketPortsPower powerSupplyPort = ticketPortsPowerDAO.getTicketPortPowerSupply(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.POWER_SUPPLY, powerSupplyPort);
		
		// add ticket rack pdu inlet ports fields
		List<TicketPortsPower> ticketPortsPowerRpduInlet = ticketPortsPowerDAO.getTicketPortsPowerRpduInlets(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.INPUT_CORD, ticketPortsPowerRpduInlet);

		// add ticket rack pdu outlet ports fields
		List<TicketPortsPower> ticketPortsPowerRpduOutlet = ticketPortsPowerDAO.getTicketPortsPowerRpduOutlets(ticketId, true);
		targetMap.put(TicketPortsPower.class.getName() + SystemLookup.PortSubClass.RACK_PDU_OUTPUT, ticketPortsPowerRpduOutlet);

		// add ticket ports data fields
		List<TicketPortsData> ticketPortsData = ticketPortsDataDAO.getTicketPortsData(ticketId, true);
		targetMap.put(TicketPortsData.class.getName(), ticketPortsData);

		// check if the ticket can be updated
		if (ticketUtility.canUpdateTicket(ticket)) {
		
			ticketResetBehaviors.reset(targetMap);
		}
		else {
		
			ticketCompletedResetBehaviors.reset(targetMap);
		}
		
	}

	protected void deleteTicketWithExpectedErrorCode(List<Long> ticketIds, List<String> errorCodes) throws BusinessValidationException, ServiceLayerException {
		// delete the tickets
		try {
			
			ticketService.deleteTicketByIds(ticketIds);
			  
		} catch (BusinessValidationException be) {
			
			Map<String, String> errorMap = be.getErrors();
			boolean throwbe = false;
			for (Map.Entry<String, String> entry : errorMap.entrySet()) {
				if (errorCodes.contains(entry.getKey())) { 
					throwbe = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
			for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet()) {
				Assert.fail(entry.getKey() + " / " + entry.getValue());
			}
			if (throwbe) {
				throw be;
			}
		}
		catch (ServiceLayerException se) {
			String exceptionMessage = se.getExceptionContext().toString();
			Boolean correctException = false;
			for (String errorCode: errorCodes) {
				if (exceptionMessage.contains(errorCode)) {
					correctException = true;
				}
			}
			if (correctException) {
				throw se;
			}
			else {
				Assert.fail(se.getMessage());
				Assert.fail(exceptionMessage);
			}
		}
	}

	protected void validateLnEventTicketDeleteItem(Long itemId) {
		// LNEvent
		Criteria criteria = session.createCriteria(LNEvent.class);
		criteria.add(Restrictions.eq("tableRowId", itemId));
		LNEvent event = (LNEvent) criteria.uniqueResult();
		Assert.assertNotNull(event);
		Assert.assertTrue(event.getAction().equals("ExternalTicketsDeleteItem"));
	}
	
	// All classes of items
	private void validateTicketItemLocation(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getDataCenterLocationDetails().getDataCenterLocationId(), item.getDataCenterLocation().getDataCenterLocationId());
		Assert.assertEquals(ticketFields.getDataCenterCode(), item.getDataCenterLocation().getCode());
		
	}

	// Rackable, Non-Rackable
	private void validateTicketItemRailsUsed(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getRailsUsed(), item.getMountedRailLookup().getLkpValue()); // rails used
		Assert.assertEquals(ticketFields.getRailsUsedId(), item.getMountedRailLookup()); // rails used
		
	}
	
	private void validateTicketItemDepthPosition(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getDepthPosition(), item.getFacingLookup().getLkpValue());
		Assert.assertEquals(ticketFields.getDepthPositionId(), item.getFacingLookup());
		
	}
	
	private void validateTicketItemCabinetSide(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getCabinetSide(), item.getMountedRailLookup().getLkpValue());
		Assert.assertEquals(ticketFields.getCabinetSideId(), item.getMountedRailLookup());
		
	}


	private void validateTicketItemCabinet(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getCabinet(), item.getParentItem()); // cabinet placed
		
	}
	
	private void validateTicketItemChassis(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getChassis(), ((ItItem)item).getBladeChassis().getItemName()); // cabinet placed
		
	}

	private void validateTicketItemChassisFace(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getChassisFaceId(), item.getFacingLookup()); // u pos placed
		
	}
	
	@SuppressWarnings("unused")
	private void validateTicketItemCabinetRails(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getRailsUsedId(), item.getMountedRailLookup());
		
	}
	

	private void validateTicketItemSlotPosition(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getSlotPosition().intValue(), (new Long(item.getSlotPosition())).intValue() ); // u pos placed
		
	}

	private void validateTicketItemUPos(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getuPosition().intValue(), (new Long(item.getUPosition())).intValue() ); // u pos placed
		
	}
	
	private void validateTicketItemShelfPosition(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getShelfPosition(), item.getShelfPosition() );
		
	}
	
	private void validateTicketItemOrientation(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getOrientationId(), item.getFacingLookup()); // u pos placed
		
	}
	
	private void validateTicketItemRowLabel(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getRowLabel(), ((CabinetItem)item).getRowLabel()); // u pos placed
		
	}

	private void validateTicketItemPositionInRow(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(new Long(ticketFields.getPositionInRow().longValue()), new Long(((CabinetItem)item).getPositionInRow())); // u pos placed
		
	}

	private void validateTicketItemFrontFaces(Long ticketId) {
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getFrontFacesId(), item.getFacingLookup()); // u pos placed
		
	}
	
	private void validateTicketItemCabinetPlacement(Long ticketId) {
		
		validateTicketItemLocation(ticketId);
		
		validateTicketItemRowLabel(ticketId);
		
		validateTicketItemPositionInRow(ticketId);
		
		validateTicketItemFrontFaces(ticketId);
		
	}

	private void validateTicketItemBladePlacement(Long ticketId) {
		
		validateTicketItemLocation(ticketId);
		
		validateTicketItemCabinet(ticketId);
		
		validateTicketItemChassis(ticketId);
		
		validateTicketItemChassisFace(ticketId);
		
		validateTicketItemSlotPosition(ticketId);
		
	}

	private void validateTicketItemRackablePlacement(Long ticketId) {
		
		validateTicketItemLocation(ticketId);
		
		validateTicketItemCabinet(ticketId);
		
		// validateTicketItemCabinetRails(ticketId);
		
		validateTicketItemRailsUsed(ticketId);
		
		validateTicketItemUPos(ticketId);
		
		validateTicketItemOrientation(ticketId);
		
	}
	
	private void validateTicketItemNonRackablePlacement(Long ticketId) {
		
		validateTicketItemRackablePlacement(ticketId);
		
		validateTicketItemShelfPosition(ticketId);
		
	}


	private void validateTicketItemZeroUPlacement(Long ticketId) {
		
		validateTicketItemLocation(ticketId);
		
		validateTicketItemCabinet(ticketId);
		
		validateTicketItemDepthPosition(ticketId);
		
		validateTicketItemCabinetSide(ticketId);
		
		validateTicketItemUPos(ticketId);
		
	}

	
	private void validateTicketItemHardwareInfo(Long ticketId) {
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getClassLookup(), item.getClassLookup()); // class
		Assert.assertEquals(ticketFields.getSubclassLookup(), item.getSubclassLookup()); // subclass
		
		if (item.getSubclassLookup() == null || !item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.VIRTUAL_MACHINE)) {
			Assert.assertEquals(ticketFields.getMake(), item.getModel().getModelMfrDetails().getMfrName()); // make 
			Assert.assertEquals(ticketFields.getModelDetails().getModelDetailId(), item.getModel().getModelDetailId()); // model
		}
		
		Assert.assertEquals(ticketFields.getSerialNumber(), item.getItemServiceDetails().getSerialNumber()); // serial number
		Assert.assertEquals(ticketFields.getAssetNumber(), item.getItemServiceDetails().getAssetNumber()); // asset tag
		
	}

	private void validateTicketItemIdentityInfo(Long ticketId) {
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		Item item = ticketFields.getItem();
		Assert.assertNotNull(ticketFields.getItemId());
		Assert.assertNotNull(item);
		
		Assert.assertEquals(ticketFields.getItemId(), new Long(item.getItemId())); // item id
		Assert.assertEquals(ticketFields.getItemName(), item.getItemName()); // item name
		
		Assert.assertEquals(ticketFields.getPurposeLookup(), item.getItemServiceDetails().getPurposeLookup()); // purpose
		
		Assert.assertEquals(ticketFields.getFunctionLookup(), item.getItemServiceDetails().getPurposeLookup()); // function
		
	}
	
	protected void validateModifiedTicketFieldsWithOriginalTicket(Long ticketId) {

		TicketFields originalFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, false);
		originalFields.setIsModified(true);
		
		TicketFields ticketFields = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketId, true);
		
		if (!equals(originalFields, ticketFields)) {
			Assert.fail("Ticket Fields were not updated to the original ticket");
		}
		
	}
	
	private boolean modelExist(String modelName) {
		return (null != modelDAO.getModelByName(modelName));
	}
	
	private boolean equals(TicketFields me, TicketFields obj) {
		if (obj == null)
			return false;
		if (me.getClass() != obj.getClass()) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Class do not match");
			return false;
		}
		TicketFields other = (TicketFields) obj;
		if (me.getAssetNumber() == null) {
			if (other.getAssetNumber() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Assert number is null/non-null");
				return false;
			}
		} else if (!me.getAssetNumber().equals(other.getAssetNumber())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Assert number do not match");
			return false;
		}
		if (me.getCabinetName() == null) {
			if (other.getCabinetName() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Name null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.Cabinet) && canMatch(other, TicketFieldsMapper.Cabinet) && !me.getCabinetName().equals(other.getCabinetName())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Name no not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.Cabinet) || !canMatch(other, TicketFieldsMapper.Cabinet)) && !other.getCabinetName().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Name is not blank");
			return false;
		}
		
		if (me.getCabinetSide() == null) {
			if (other.getCabinetSide() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Side null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.CabinetSide) && canMatch(other, TicketFieldsMapper.CabinetSide) && !other.getCabinetSide().contains(me.getCabinetSide())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Side do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.CabinetSide) || !canMatch(other, TicketFieldsMapper.CabinetSide) ) && !other.getCabinetSide().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Cabinet Side is not blank");
			return false;
		}
		
		if (me.getChassis() == null) {
			if (other.getChassis() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Chassis null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.Chassis) && canMatch(other, TicketFieldsMapper.Chassis) && !me.getChassis().equals(other.getChassis())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Chassis do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.Chassis) || !canMatch(other, TicketFieldsMapper.Chassis)) && !other.getChassis().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Chassis is not blank");
			return false;
		}
		
		if (me.getChassisFace() == null) {
			if (other.getChassisFace() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Chassis Face null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.ChassisFace) && canMatch(other, TicketFieldsMapper.ChassisFace) && !me.getChassisFace().equals(other.getChassisFace())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Chassis Face do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.ChassisFace) || !canMatch(other, TicketFieldsMapper.ChassisFace)) && !other.getChassisFace().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Chassis Face is not blank");
			return false;
		}
		
		if (me.getDataCenterCode() == null) {
			if (other.getDataCenterCode() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Data Center Code null/non-null");
				return false;
			}
		} else if (!me.getDataCenterCode().equalsIgnoreCase(other.getDataCenterCode())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Data Center Code do not match");
			return false;
		}
		
		if (me.getDepthPosition() == null) {
			if (other.getDepthPosition() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Depth Position null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.DepthPosition) && canMatch(other, TicketFieldsMapper.DepthPosition) && !me.getDepthPosition().equals(other.getDepthPosition())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Depth Position do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.DepthPosition) || !canMatch(other, TicketFieldsMapper.DepthPosition)) && !other.getDepthPosition().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Depth Position is not blank");
			return false;
		}
		
		if (me.getDstCabinetName() == null) {
			if (other.getDstCabinetName() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Destination Cabinet Name null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.Cabinet) && canMatch(other, TicketFieldsMapper.Cabinet) && !me.getDstCabinetName().equals(other.getDstCabinetName())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Destination Cabinet Name do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.Cabinet) || !canMatch(other, TicketFieldsMapper.Cabinet)) && !other.getDstCabinetName().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Destination Cabinet Name is not blank");
			return false;
		}
		
		if (me.getDstUPosition() == null) {
			if (other.getDstUPosition() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Destination U position null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.UPosition) && canMatch(other, TicketFieldsMapper.UPosition) && !me.getDstUPosition().equals(other.getDstUPosition())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Destination U position do not match");
			return false;
		}
		else if (!canMatch(other, TicketFieldsMapper.UPosition) && !other.getDstUPosition().equals(-1) ) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Destination U position is not blank");
			return false;
		}
		/*else if (!canSetField(other, TicketFieldsMapper.UPosition) && !other.getDstUPosition().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Destination U position is not blank");
			return false;
		}*/
		
		if (me.getFrontFaces() == null) {
			if (other.getFrontFaces() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Front Faces null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.FrontFaces) && canMatch(other, TicketFieldsMapper.FrontFaces) && !me.getFrontFaces().equals(other.getFrontFaces())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Front Faces do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.FrontFaces) || !canMatch(other, TicketFieldsMapper.FrontFaces)) && !other.getFrontFaces().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Front Faces is not blank");
			return false;
		}
		
		if (me.getFunction() == null) {
			if (other.getFunction() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Function null/non-null");
				return false;
			}
		} else if (!me.getFunction().equals(other.getFunction())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Function do not match");
			return false;
		}
		
		if (me.getItemClass() == null) {
			if (other.getItemClass() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Item Class null/non-null");
				return false;
			}
		}
		else if (other.getModelDetails() != null && !other.getItemClass().equals(other.getModelDetails().getClassLookup().getLkpValue())) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Item Class do not match the model class");
				return false;
		}
		else if (other.getModelDetails() == null && null != other.getItemClass()  && !other.getItemClass().equals("") && !me.getItemClass().equals(other.getItemClass())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Item Class do not match");
			return false;
		}
		
		if (me.getItemName() == null) {
			if (other.getItemName() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Item Name null/non-null");
				return false;
			}
		} else if (!me.getItemName().equalsIgnoreCase(other.getItemName())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Item Name do not match");
			return false;
		}
		
		if (me.getMake() == null) {
			if (other.getMake() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Make null/non-null");
				return false;
			}
		} else if (null != other.getMake() && !other.getMake().equals("") && !me.getMake().equals(other.getMake())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Make do not match");
			return false;
		}
		
		if (me.getModel() == null) {
			if (other.getModel() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Model null/non-null");
				return false;
			}
		} else if (modelExist(me.getModel())  && !me.getModel().equals(other.getModel())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Model do not match");
			return false;
		}
		/*if (me.getOrientation() == null) {
			if (other.getOrientation() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Orientation null/non-null");
				return false;
			}
		} else if (!me.getOrientation().equals(other.getOrientation())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Orientation do not match");
			return false;
		}
			*/
		
		if (me.getPositionInRow() == null) {
			if (other.getPositionInRow() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Position In Row null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.PositionInRow) && canMatch(other, TicketFieldsMapper.PositionInRow) && !me.getPositionInRow().equals(other.getPositionInRow())) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Position In Row do not match");
				return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.PositionInRow) || !canMatch(other, TicketFieldsMapper.PositionInRow)) && other.getPositionInRow() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Position In Row is not null");
				return false;
		}
		
		if (me.getPurpose() == null) {
			if (other.getPurpose() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Purpose null/non-null");
				return false;
			}
		} else if (!me.getPurpose().equals(other.getPurpose())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Purpose do not match");
			return false;
		}
		
		if (me.getRailsUsed() == null) {
			if (other.getRailsUsed() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Rails Used null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.RailsUsed) && canMatch(other, TicketFieldsMapper.RailsUsed) && !me.getRailsUsed().equals(other.getRailsUsed())) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Rails Used do not match");
				return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.RailsUsed)  || !canMatch(other, TicketFieldsMapper.RailsUsed)) && !other.getRailsUsed().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Rails Used is not blank");
			return false;
		}
		
		if (me.getRowLabel() == null) {
			if (other.getRowLabel() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Row Label null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.RowLabel) && canMatch(other, TicketFieldsMapper.RowLabel) && !me.getRowLabel().equals(other.getRowLabel())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Row Label do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.RowLabel) || !canMatch(other, TicketFieldsMapper.RowLabel)) && null != other.getRowLabel()) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Row Label is not null");
			return false;
		}
		
		if (me.getSerialNumber() == null) {
			if (other.getSerialNumber() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Serial Number null/non-null");
				return false;
			}
		} else if (!me.getSerialNumber().equals(other.getSerialNumber())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Serial Number do not match");
			return false;
		}
		
		if (me.getShelfPosition() == null) {
			if (other.getShelfPosition() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Shelf Position null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.ShelfPosition) && canMatch(other, TicketFieldsMapper.ShelfPosition) && !me.getShelfPosition().equals(other.getShelfPosition())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Shelf Position do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.ShelfPosition) || !canMatch(other, TicketFieldsMapper.ShelfPosition)) && other.getShelfPosition() != null) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Shelf Position is not null");
			return false;
		}
		
		if (me.getSlotLabel() == null) {
			if (other.getSlotLabel() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Slot label null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.SlotPosition) && canMatch(other, TicketFieldsMapper.SlotPosition) && !me.getSlotLabel().equals(other.getSlotLabel())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Slot label do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.SlotPosition) || !canMatch(other, TicketFieldsMapper.SlotPosition)) && null != other.getSlotLabel() && !other.getSlotLabel().equals("")) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Slot label is not blank");
			return false;
		}
		
		if (me.getSubClass() == null) {
			if (other.getSubClass() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: Item Subclass null/non-null");
				return false;
			}
		} else if (null != other.getModelDetails() && null != other.getSubclassLookup() && 
				other.getSubclassLookup().getLkpValueCode().longValue() != other.getModelDetails().getSubClassUniqueValue().longValue()) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Item Subclass do not match the model's subclass");
			return false;
		} else if (!other.getSubClass().equalsIgnoreCase(other.getSubClass())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: Item Subclass do not match");
			return false;
		}
		
		if (me.getuPosition() == null) {
			if (other.getuPosition() != null) {
				Assert.fail("Ticket Fields were not updated to the original ticket: U position null/non-null");
				return false;
			}
		} else if (canSetField(other, TicketFieldsMapper.UPosition)  && canMatch(other, TicketFieldsMapper.UPosition) && !me.getuPosition().equals(other.getuPosition())) {
			Assert.fail("Ticket Fields were not updated to the original ticket: U position do not match");
			return false;
		}
		else if ((!canSetField(other, TicketFieldsMapper.UPosition) || !canMatch(other, TicketFieldsMapper.UPosition)) && other.getuPosition() != null && other.getuPosition().intValue() != -9) {
			Assert.fail("Ticket Fields were not updated to the original ticket: U position is not null");
			return false;
		}

		return true;
	}
	
	
	public static class TicketFieldsMapper {
		public static final String RowLabel = "Row Label";
		public static final String PositionInRow = "Position In Row";
		public static final String Cabinet = "Cabinet";
		public static final String UPosition = "U Position";
		public static final String Chassis = "Chassis";
		public static final String ChassisFace = "Chassis Face";
		public static final String SlotPosition = "Slot Position";
		public static final String DepthPosition = "Depth Position";
		public static final String CabinetSide = "Cabinet Side";
		public static final String Orientation = "Orientation";
		public static final String RailsUsed = "Rails Used";
		public static final String FrontFaces = "Front Faces";
		public static final String ShelfPosition = "Shelf Position";
		
	}

	@SuppressWarnings("serial")
	final Map<String, List<String>> mapFieldsSupportingMounting = 
			Collections.unmodifiableMap(new HashMap<String, List<String>>() {{
				put(TicketFieldsMapper.RowLabel, new ArrayList<String>() {{ add(SystemLookup.Mounting.FREE_STANDING); }});
				put(TicketFieldsMapper.PositionInRow, new ArrayList<String>() {{ add(SystemLookup.Mounting.FREE_STANDING); }});
				put(TicketFieldsMapper.Cabinet, new ArrayList<String>() {{ add(SystemLookup.Mounting.ZERO_U); add(SystemLookup.Mounting.RACKABLE); add(SystemLookup.Mounting.NON_RACKABLE); add(SystemLookup.Mounting.BLADE); }});
				put(TicketFieldsMapper.CabinetSide, new ArrayList<String>() {{ add(SystemLookup.Mounting.ZERO_U); }});
				// TODO:: set u position for non-supporting 
				put(TicketFieldsMapper.UPosition, new ArrayList<String>() {{ add(SystemLookup.Mounting.ZERO_U); add(SystemLookup.Mounting.RACKABLE); add(SystemLookup.Mounting.NON_RACKABLE); }});
				put(TicketFieldsMapper.Chassis, new ArrayList<String>() {{ add(SystemLookup.Mounting.BLADE); }});
				put(TicketFieldsMapper.ChassisFace, new ArrayList<String>() {{ add(SystemLookup.Mounting.BLADE); }});
				put(TicketFieldsMapper.SlotPosition, new ArrayList<String>() {{ add(SystemLookup.Mounting.BLADE); }});
				put(TicketFieldsMapper.DepthPosition, new ArrayList<String>() {{ add(SystemLookup.Mounting.ZERO_U); }});
				put(TicketFieldsMapper.RailsUsed, new ArrayList<String>() {{ add(SystemLookup.Mounting.RACKABLE); add(SystemLookup.Mounting.NON_RACKABLE); }});
				// TODO:: set shelf position for non-supporting 
				put(TicketFieldsMapper.ShelfPosition, new ArrayList<String>() {{ add(SystemLookup.Mounting.NON_RACKABLE); }});
				put(TicketFieldsMapper.FrontFaces, new ArrayList<String>() {{ add(SystemLookup.Mounting.FREE_STANDING); }});
				put(TicketFieldsMapper.Orientation, new ArrayList<String>() {{ add(SystemLookup.Mounting.RACKABLE); add(SystemLookup.Mounting.NON_RACKABLE); }});
				
			}});

	
	@SuppressWarnings("serial")
	final Map<String, List<String>> mapFieldsSupportingClassSubclass = 
			Collections.unmodifiableMap(new HashMap<String, List<String>>() {{
				
				put(TicketFieldsMapper.RowLabel, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.CABINET)).toString() ); add( (new Long(SystemLookup.Class.CABINET)).toString() + (new Long(SystemLookup.SubClass.CONTAINER)).toString() ); }});
				
				put(TicketFieldsMapper.PositionInRow, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.CABINET)).toString() ); add( (new Long(SystemLookup.Class.CABINET)).toString() + (new Long(SystemLookup.SubClass.CONTAINER)).toString() ); }});
				
				// with the class and subclass only, we can guess only for these 2 kinds. For Device:Standard, we cannot make a guess because it also includes Free-standing and cabinet is not required for FS
				put(TicketFieldsMapper.Cabinet, new ArrayList<String>() {{  }});
				
				// cannot make any guess for ZeroU Side because it is required by ZeroU placement and zero U falls under subclass Standard and Standard subclass also could be for Rackable, NR, Blade, FS
				put(TicketFieldsMapper.CabinetSide, new ArrayList<String>() {{ }});

				// with the class and subclass only, we can guess only for these 2 kinds. For Device:Standard, we cannot make a guess because it also includes Free-standing and cabinet is not required for FS
				put(TicketFieldsMapper.UPosition, new ArrayList<String>() {{  }});

				put(TicketFieldsMapper.Chassis, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.DEVICE)).toString() + (new Long(SystemLookup.SubClass.BLADE_SERVER)).toString() ); add( (new Long(SystemLookup.Class.NETWORK)).toString() + (new Long(SystemLookup.SubClass.BLADE)).toString() ); }});
				
				put(TicketFieldsMapper.ChassisFace, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.DEVICE)).toString() + (new Long(SystemLookup.SubClass.BLADE_SERVER)).toString() ); add( (new Long(SystemLookup.Class.NETWORK)).toString() + (new Long(SystemLookup.SubClass.BLADE)).toString() ); }});
				
				put(TicketFieldsMapper.SlotPosition, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.DEVICE)).toString() + (new Long(SystemLookup.SubClass.BLADE_SERVER)).toString() ); add( (new Long(SystemLookup.Class.NETWORK)).toString() + (new Long(SystemLookup.SubClass.BLADE)).toString() ); }});

				// cannot make any guess for ZeroU because it is required by ZeroU placement and zero U falls under subclass Standard and Standard subclass also could be for Rackable, NR, Blade, FS
				put(TicketFieldsMapper.DepthPosition, new ArrayList<String>() {{ }});
				
				// with the class and subclass only, we can guess only for these 2 kinds. For Device:Standard, we cannot make a guess because it also includes Free-standing and cabinet is not required for FS
				put(TicketFieldsMapper.RailsUsed, new ArrayList<String>() {{  }});

				// cannot make any guess for Non-Rackable Side because it is required by ZeroU placement and zero U falls under subclass Standard and Standard subclass also could be for Rackable, NR, Blade, FS
				put(TicketFieldsMapper.ShelfPosition, new ArrayList<String>() {{ }});

				// cannot make any guess for Free-Standing Side because it is required by ZeroU placement and zero U falls under subclass Standard and Standard subclass also could be for Rackable, NR, Blade, FS
				put(TicketFieldsMapper.FrontFaces, new ArrayList<String>() {{ add((new Long(SystemLookup.Class.CABINET)).toString() ); add( (new Long(SystemLookup.Class.CABINET)).toString() + (new Long(SystemLookup.SubClass.CONTAINER)).toString() ); }});
				
				// with the class and subclass only, we can guess only for these 2 kinds. For Device:Standard, we cannot make a guess because it also includes Free-standing and cabinet is not required for FS
				put(TicketFieldsMapper.Orientation, new ArrayList<String>() {{  }});

			}});
	
	private boolean canSetField(TicketFields ticketFields, String field) {
		Boolean canSetField = null;

		String classLkpCode = (null != ticketFields.getClassLookup() && null != ticketFields.getClassLookup().getLkpValueCode()) ? ticketFields.getClassLookup().getLkpValueCode().toString() : null;
		String subClassLkpCode = (null != ticketFields.getSubclassLookup() && null != ticketFields.getSubclassLookup().getLkpValueCode()) ? ticketFields.getSubclassLookup().getLkpValueCode().toString() : null;
		if ((null != subClassLkpCode && subClassLkpCode.equals((new Long(SystemLookup.SubClass.VIRTUAL_MACHINE)).toString()))) {
			List<String> supportedMounting = mapFieldsSupportingMounting.get(field);
			
			if (null == supportedMounting || supportedMounting.size() == 0) {
				return true;
			}
			
			return supportedMounting.contains(SystemLookup.Mounting.VSTACK);
			
		}
		
		else if (null != ticketFields.getModelDetails()) {
			
			List<String> supportedMounting = mapFieldsSupportingMounting.get(field);
			
			if (null == supportedMounting || supportedMounting.size() == 0) {
				return true;
			}
			
			ModelDetails model = (ModelDetails) session.get(ModelDetails.class, ticketFields.getModelDetails().getModelDetailId()); //ticketFields.getModelDetails();

			String mounting = model.getMounting();
			
			if (null == mounting) {
				return true;
			}
			
			canSetField = supportedMounting.contains(mounting);
			return canSetField;
			
		}
		
		else if ((null != subClassLkpCode && (subClassLkpCode.equals((new Long(SystemLookup.SubClass.BLADE_SERVER)).toString()) || subClassLkpCode.equals((new Long(SystemLookup.SubClass.BLADE)).toString())))) {
			List<String> supportedMounting = mapFieldsSupportingMounting.get(field);
			
			if (null == supportedMounting || supportedMounting.size() == 0) {
				return true;
			}
			
			return supportedMounting.contains(SystemLookup.Mounting.BLADE);
			
		}
		
		else if ((null != subClassLkpCode && (subClassLkpCode.equals((new Long(SystemLookup.SubClass.CHASSIS)).toString()) || subClassLkpCode.equals((new Long(SystemLookup.SubClass.BLADE_CHASSIS)).toString())))) {
			List<String> supportedMounting = mapFieldsSupportingMounting.get(field);
			
			if (null == supportedMounting || supportedMounting.size() == 0) {
				return true;
			}
			
			return supportedMounting.contains(SystemLookup.Mounting.RACKABLE);
			
		}
		
		else if (null != classLkpCode && classLkpCode.equals((new Long(SystemLookup.Class.CABINET)).toString())) {
			List<String> supportedMounting = mapFieldsSupportingMounting.get(field);
			
			if (null == supportedMounting || supportedMounting.size() == 0) {
				return true;
			}
			
			return supportedMounting.contains(SystemLookup.Mounting.FREE_STANDING);
			
		}

		else if (null == classLkpCode && null == subClassLkpCode && null == ticketFields.getModelDetails() ) {
			
			return false;
		}

		else {

			List<String> supportedClassSubclass = mapFieldsSupportingClassSubclass.get(field);
			// Cannot find for which kind of class / subclass we can use this field, therefore set for all
			if (null == supportedClassSubclass || supportedClassSubclass.size() == 0) {
				return true;
			}

			// String classLkpCode = (null != ticketFields.getClassLookup() && null != ticketFields.getClassLookup().getLkpValueCode()) ? ticketFields.getClassLookup().getLkpValueCode().toString() : null;
			
			// Cannot find for which kind of class / subclass we can use this field, therefore set for all
			if (null == classLkpCode) {
				return true;
			}
			
			canSetField = supportedClassSubclass.contains(classLkpCode);
			if (!canSetField) {
				if (null != subClassLkpCode) {
					canSetField = supportedClassSubclass.contains(classLkpCode + subClassLkpCode);
				}
			}
			
			return canSetField;
		}

	}
	
	@SuppressWarnings("serial")
	final Map<String, Boolean> mapFieldsCanMatch = 
			Collections.unmodifiableMap(new HashMap<String, Boolean>() {{
				put(TicketFieldsMapper.RowLabel, true);
				put(TicketFieldsMapper.PositionInRow, false);
				put(TicketFieldsMapper.Cabinet, true); // Special handling for Blade mounting - cabinet not supported for blade
				put(TicketFieldsMapper.CabinetSide, true);
				put(TicketFieldsMapper.UPosition, false);
				put(TicketFieldsMapper.Chassis, false);
				put(TicketFieldsMapper.ChassisFace, false);
				put(TicketFieldsMapper.SlotPosition, false);
				put(TicketFieldsMapper.DepthPosition, true);
				put(TicketFieldsMapper.RailsUsed, true);
				put(TicketFieldsMapper.ShelfPosition, false);
				put(TicketFieldsMapper.FrontFaces, true);
				put(TicketFieldsMapper.Orientation, true);
				
			}});

	private boolean canMatch(TicketFields ticketFields, String field) {
		
		if (null == field) {
			return false;
		}
		
		Boolean canMatch = mapFieldsCanMatch.get(field);
		if (null == canMatch) {
			return true;
		}
		
		if (field.equals(TicketFieldsMapper.Cabinet)) {
			if (null != ticketFields.getSubclassLookup() && null != ticketFields.getSubclassLookup().getLkpValueCode()) {
				Long subClassLkpValueCode = ticketFields.getSubclassLookup().getLkpValueCode();
				if (subClassLkpValueCode.equals(SystemLookup.SubClass.BLADE) || subClassLkpValueCode.equals(SystemLookup.SubClass.BLADE_SERVER)) {
					return false;
				}
			}
		}
		
		return canMatch;
		
	}
	



}
