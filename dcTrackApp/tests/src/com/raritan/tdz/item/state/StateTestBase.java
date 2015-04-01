package com.raritan.tdz.item.state;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.springframework.validation.Errors;
import org.testng.annotations.AfterClass;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class StateTestBase extends TestBase {

	protected List<Long> powerOutlet = new ArrayList<Long>();
	
	
	@AfterClass
	public void classTearDown() throws Throwable {
		
		
		Query updateQuery = session.createSQLQuery(
				"update dct_items_me set ups_bank_item_id = null where ups_bank_item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		@SuppressWarnings("unused")
		int updated = updateQuery.executeUpdate();
		
		Query deleteQuery = session.createSQLQuery(
			    "delete from dct_items_me where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_connections_power where source_port_id in (select port_power_id from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK'))");
		updated = deleteQuery.executeUpdate();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK'))");
		updated = deleteQuery.executeUpdate();
		
		deleteQuery = session.createSQLQuery(
			    "delete from dct_ports_power where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();

		deleteQuery = session.createSQLQuery(
			    "delete from dct_items where item_id in (select item_id from dct_items where item_name = 'INT_TEST_UPSBANK')");
		updated = deleteQuery.executeUpdate();
		
		session.flush();
		
	}
	

	///////////////////////
	/// Floor PDU Item ////
	//////////////////////
	protected MeItem createFloorPDUInPlannedState() throws Throwable {

		// create FPDU in planned state, no panels
		MeItem fpdu = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		return fpdu;
		
	}
	
	protected MeItem createFloorPDUWithPanelsInPlannedState() throws Throwable {
		
		// create FPDU in planned state, no panels
		// MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		MeItem fpdu = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		// create 2 panels and set the parent item as the fpdu
		MeItem panel1 = createPowerPanel(fpdu, "INT_TEST_PS1", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		itemHome.saveItem( panel1 );
		session.flush();
		fpdu.addChildItem(panel1);
		
		MeItem panel2 = createPowerPanel(fpdu, "INT_TEST_PS2", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		itemHome.saveItem( panel2 );
		session.flush();
		fpdu.addChildItem(panel2);

		return fpdu;
	}

	protected MeItem createFloorPDUWithPanelsAndOutletInPlannedState() throws Throwable {
		
		MeItem upsBank = createNewTestUPSBank("INT_TEST_UPSBANK");
		
		// create FPDU in planned state, no panels
		// MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		fpdu.setUpsBankItem(upsBank);
		floorPduCreateBreakerPortActionHandler.process(fpdu, null, true);
		floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, null, true, true);
		
		// create 2 panels and set the parent item as the fpdu
		MeItem panel1 = createPowerPanel(fpdu, "INT_TEST_PS1", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel1 );
		session.flush();*/
		fpdu.addChildItem(panel1);
		
		MeItem panel2 = createPowerPanel(fpdu, "INT_TEST_PS2", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel2 );
		session.flush();*/
		fpdu.addChildItem(panel2);

		Set<Item> panels = fpdu.getChildItems();
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		Long count = 1L;
		for (Item panel: panels) {
			
			powerPanelBreakerPortActionHandler.process(panel, null, true, true);
			session.update(panel);
			session.flush();
			createPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, upsBank);
			count++;
			
		}
		session.update(fpdu);
		
		session.flush();
		
		session.refresh(fpdu);
		
		return fpdu;
		
	}
	
	protected List<MeItem> createPanels(MeItem fpdu, Long subClassLkpValueCode, boolean withOutlets, boolean withOutletsConnected) throws Throwable {
		
		List<MeItem> powerOutlets = new ArrayList<MeItem>();
		
		// create 2 panels and set the parent item as the fpdu
		MeItem panel1 = createPowerPanel(fpdu, "INT_TEST_PS1", 4L, subClassLkpValueCode, SystemLookup.ItemStatus.PLANNED);
		/*itemHome.saveItem( panel1 );
		session.flush();*/
		fpdu.addChildItem(panel1);
		
		MeItem panel2 = createPowerPanel(fpdu, "INT_TEST_PS2", 4L, subClassLkpValueCode, SystemLookup.ItemStatus.PLANNED);
		/*itemHome.saveItem( panel2 );
		session.flush();*/
		fpdu.addChildItem(panel2);

		List<Item> panels = new ArrayList<Item>(); 
		panels.add(panel1);
		panels.add(panel2);
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		Long count = 1L;
		for (Item panel: panels) {
			
			powerPanelBreakerPortActionHandler.process(panel, null, true, true);
			session.update(panel);
			session.flush();
			if (withOutlets) {
				if (withOutletsConnected) {
					MeItem po = createConnectedPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, fpdu.getUpsBankItem());
					powerOutlets.add(po);
				}
				else {
					MeItem po = createPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, fpdu.getUpsBankItem());
					powerOutlets.add(po);
				}
				count++;
			}
			
		}
		
		return powerOutlets;
	}
	
	protected MeItem createFloorPDU(boolean withUpsBank, boolean withPanels, boolean withOutlets, boolean withOutletsConnected) throws Throwable {
		
		MeItem upsBank = null;
		if (withUpsBank) {
			upsBank = createNewTestUPSBank("INT_TEST_UPSBANK");
		}
		// create FPDU in planned state, no panels
		// MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		MeItem fpdu = createNewTestFloorPDU("INT_TEST_FPDU"); // createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		if (withUpsBank) {
			fpdu.setUpsBankItem(upsBank);
		}
		floorPduCreateBreakerPortActionHandler.process(fpdu, null, true);
		if (withUpsBank) {
			floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, null, true, true);
		}
		
		if (withPanels) {
			// Create Local Panels and outlets and used flag updated
			createPanels(fpdu, SystemLookup.SubClass.LOCAL, withOutlets, withOutletsConnected);
		}
		session.update(fpdu);
		
		session.flush();
		
		session.refresh(fpdu);
		
		return fpdu;
		
	}
	
	
	protected MeItem createFloorPDUWithPanelsAndOutletConnectedInPlannedState() throws Throwable {
		
		MeItem upsBank = createNewTestUPSBank("INT_TEST_UPSBANK");
		// create FPDU in planned state, no panels
		// MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		MeItem fpdu = createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers("INT_TEST_FPDU");
		fpdu.setUpsBankItem(upsBank);
		floorPduCreateBreakerPortActionHandler.process(fpdu, null, true);
		floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, null, true, true);
		
		// create 2 panels and set the parent item as the fpdu
		MeItem panel1 = createPowerPanel(fpdu, "INT_TEST_PS1", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel1 );
		session.flush();*/
		fpdu.addChildItem(panel1);
		
		MeItem panel2 = createPowerPanel(fpdu, "INT_TEST_PS2", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		/*itemHome.saveItem( panel2 );
		session.flush();*/
		fpdu.addChildItem(panel2);

		Set<Item> panels = fpdu.getChildItems();
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		Long count = 1L;
		for (Item panel: panels) {
			
			powerPanelBreakerPortActionHandler.process(panel, null, true, true);
			session.update(panel);
			session.flush();
			createConnectedPowerOutlet("INT_TEST_PO_" + count.toString(), cabinetItem, panel, upsBank);
			count++;
			
		}
		session.update(fpdu);
		
		session.flush();
		
		session.refresh(fpdu);
		
		return fpdu;
		
	}

	
	protected MeItem createPowerOutlet(String outletName, Item cabinet, Item panel, Item upsBank) throws Throwable {
/*		MeItem outlet = null;
		if (null != panel) {
			Set<PowerPort> panelPorts = panel.getPowerPorts();
			List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
		
			outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, panelPortList.get(0), 2L);
		}
		else {
			outlet = createPowerOutlet(outletName, upsBank, null, cabinet, null, 2L);
		}
		powerOutlet.add(outlet.getItemId());
		
		return outlet;*/
		
		MeItem outlet = null;
		PowerPort bcBreakerPort = null;
		
		if (null != panel) {
			Set<PowerPort> panelPorts = panel.getPowerPorts();
			bcBreakerPort = null;
			for (PowerPort port: panelPorts) {
				if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
					bcBreakerPort = port;
					break;
				}
			}
		}
			// List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
			
		outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, bcBreakerPort, 2L);
		if (null != panel) {
			if (panel.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
				bcBreakerPort.setBuswayItem(outlet);
			}
			session.update(outlet);
			session.update(panel);
		}
		powerOutlet.add(outlet.getItemId());
		
		/*Set<PowerPort> ports = outlet.getPowerPorts();
		for (PowerPort port: ports) {
			port.setUsed(true);
			// break;
		}*/
		
		return outlet;

	}

	private MeItem createConnectedPowerOutlet(String outletName, Item cabinet, Item panel, Item upsBank) throws Throwable {
		Set<PowerPort> panelPorts = panel.getPowerPorts();
		PowerPort bcBreakerPort = null;
		for (PowerPort port: panelPorts) {
			if (port.getPortSubClassLookup().getLkpValueCode().longValue() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
				bcBreakerPort = port;
				break;
			}
		}
		// List<PowerPort> panelPortList = new ArrayList<PowerPort>(panelPorts);
		
		MeItem outlet = createPowerOutlet(outletName, upsBank, panel, cabinet, bcBreakerPort, 2L);
		if (panel.getSubclassLookup().getLkpValueCode().longValue() == SystemLookup.SubClass.BUSWAY) {
			bcBreakerPort.setBuswayItem(outlet);
		}
		
		Set<PowerPort> ports = outlet.getPowerPorts();
		for (PowerPort port: ports) {
			port.setUsed(true);
			// break;
		}
		
		session.update(outlet);
		session.update(panel);
		powerOutlet.add(outlet.getItemId());
		
		return outlet;
	}

	/////////////////////
	/// Chassis Item ////
	////////////////////
	protected ItItem createChassisInPlannedState() throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Test", null, cabinetItem.getItemId());
		
		devChassisItem.setParentItem(cabinetItem);
		
		cabinetItem.addChildItem(devChassisItem);
		
		return devChassisItem;
		
	}

	protected ItItem createChassisInState(Long itemState) throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", itemState);
		
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Test", itemState, cabinetItem.getItemId());
		
		devChassisItem.setParentItem(cabinetItem);
		
		cabinetItem.addChildItem(devChassisItem);
		
		return devChassisItem;
		
	}

	protected ItItem createNWChassisInState(Long itemState) throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", itemState);
		
		ItItem devChassisItem = createNewTestNetworkChassis("UNITTEST-NW_CHASSIS-Test", itemState, cabinetItem.getItemId());
		
		devChassisItem.setParentItem(cabinetItem);
		
		cabinetItem.addChildItem(devChassisItem);
		
		return devChassisItem;
		
	}

	
	protected List<Item> createChassisWithBladesInPlannedState() throws Throwable {
		
		List<Item> itemList = new ArrayList<Item>();
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		itemList.add(cabinetItem);
		
		ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Test", null, cabinetItem.getItemId());
		itemList.add(devChassisItem);
		
		cabinetItem.addChildItem(devChassisItem);
		
		ItItem devBlade1Item = createNewTestDeviceBladeInChassis("UNITTEST-PORT-DEV_BLADE1-Test", cabinetItem.getItemId(), devChassisItem.getItemId(), null);
		itemList.add(devBlade1Item);
		
		devChassisItem.addChildItem(devBlade1Item);
		
		ItItem devBlade2Item = createNewTestDeviceBladeInChassis("UNITTEST-PORT-DEV_BLADE2-Test", cabinetItem.getItemId(), devChassisItem.getItemId(), null);
		itemList.add(devBlade2Item);
		
		devChassisItem.addChildItem(devBlade2Item);
		
		return itemList;
		
	}
	
	/////////////////////
	/// Cabinet Item ////
	////////////////////
	protected CabinetItem createCabinetInPlannedState() throws Throwable {
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", null);
		
		return cabinetItem;
	}
	
	protected CabinetItem createCabinetInInstalledState() throws Throwable {
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", SystemLookup.ItemStatus.INSTALLED);
		
		return cabinetItem;
	}

	
	protected CabinetItem createCabinetWithChildrenInPlanned(boolean withNonPassiveChildren, boolean withPassiveChildren) throws Throwable {
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", SystemLookup.ItemStatus.PLANNED);
		
		if (withNonPassiveChildren) {
			ItItem devChassisItem = createNewTestDeviceChassis("UNITTEST-DEV_CHASSIS-Test", null, cabinetItem.getItemId());
			
			ItItem probeItem = createNewPortTestProbe("UNITTEST-PROBE-Test", cabinetItem.getItemId());
	
			cabinetItem.addChildItem(devChassisItem);
			
			cabinetItem.addChildItem(probeItem);
		}
		if (withPassiveChildren) {
			ItItem passiveItem1 = createNewTestPassive("UNITTEST-PASSIVE-1-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
			ItItem passiveItem2 = createNewTestPassive("UNITTEST-PASSIVE-2-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
			
			cabinetItem.addChildItem(passiveItem1);
			cabinetItem.addChildItem(passiveItem2);
		}
		
		return cabinetItem;
		
	}
	
	protected CabinetItem createFSDevWithContainer(Long status) throws Throwable {
			
			// Create a dummy FS Device item
			ItItem fsDev = createTestDeviceFS("UNITTEST-FS-DEV", status);

			// Change the name and get the dto list
			List<ValueIdDTO> itemDto = setItemName(fsDev.getItemId(), "UNITTEST-FS-DEV-NEW");
			
			// Save this dto as a new FS Dev. Server will now create FS Dev and its Container Cabinet item
			List<String> errorCodes = new ArrayList<String>(); // no error expected
			if (SystemLookup.ItemStatus.PLANNED != status && SystemLookup.ItemStatus.STORAGE != status) {
				errorCodes.add("ItemValidator.invalidTransition.newItem");
			}
			Long itemId = saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes);

			ItItem fsDevNew = (ItItem) itemDAO.getItem(itemId);
			
			CabinetItem container = (CabinetItem) fsDevNew.getParentItem();
			container.addChildItem(fsDevNew);

			addTestItem(fsDevNew);
			addTestItem(container);
			
			return container;
	}

	protected List<ValueIdDTO> setPowerPortNewItem(Map<String, UiComponentDTO> item, Long portItemId ) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);

		editPowerPortItemId(item, portItemId, -1L);

		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
		
	}

	protected List<ValueIdDTO> setPowerPortNewItem(Long itemId, Long portItemId ) throws Throwable {
		
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		return setPowerPortNewItem(item, portItemId);

	}
	
	// Set the item state
	protected List<ValueIdDTO> setItemState(Map<String, UiComponentDTO> item, Long itemStatus) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemStatus(item, itemStatus);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	protected List<ValueIdDTO> setItemPositionInRow(Map<String, UiComponentDTO> item, Long positionInRow) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemPositionInRow(item, positionInRow);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	protected List<ValueIdDTO> setItemRowLabel(Map<String, UiComponentDTO> item, String rowLabel) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item in state
		editItemRowLabel(item, rowLabel);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	protected List<ValueIdDTO> getDto(Long itemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}
	
	protected List<ValueIdDTO> setItemState(Long itemId, Long itemStatus) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		return setItemState(item, itemStatus);
		
	}

	protected List<ValueIdDTO> setItemPositionInRow(Long itemId, Long positionInRow) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		return setItemPositionInRow(item, positionInRow);
		
	}

	protected List<ValueIdDTO> setItemRowLabel(Long itemId, String rowLabel) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		return setItemRowLabel(item, rowLabel);
		
	}

	protected List<ValueIdDTO> setItemName(Map<String, UiComponentDTO> item, String itemName) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item name
		editItemName(item, itemName);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	protected List<ValueIdDTO> setItemName(Long itemId, String itemName) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );

		return setItemName(item, itemName);
	}

	protected List<ValueIdDTO> setItemId(Map<String, UiComponentDTO> item, Long newItemId) throws Throwable {
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// set item id
		editItemId(item, newItemId);
		
		List<ValueIdDTO> dtoList = prepareItemValueIdDTOList(item);
		
		return dtoList;
	}

	protected List<ValueIdDTO> setItemId(Long itemId, Long newItemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );

		return setItemId(item, newItemId);
		
	}


	protected Long getItemStatus(Long itemId) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		return getItemStatus(item);
		
	}
	
	// Save the Item 
	protected void saveItemWithExpectedErrorCode(Long itemId, List<ValueIdDTO> valueIdDTOList, List<String> errorCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		try {
			Map<String,UiComponentDTO> componentDTOMap = null;
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			// get the item for more validation while getting the saved item and ports
			Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
			assertNotNull( item );	
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
	}

	// Delete Item(s) 
	protected void deleteItemsWithExpectedErrorCode(List<Long> itemIds, List<String> errorCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		try {

			/*List<Long> processedItems = */ itemHome.deleteItems(itemIds, getTestAdminUser());
			
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
			if (!throwbe) {
				for (Map.Entry<String, BusinessValidationException.Warning> entry : warningMap.entrySet()) {
					if (errorCodes.contains(entry.getKey())) {
						throwbe = true;
					}
					else {
						Assert.fail(entry.getKey() + " / " + entry.getValue());
					}
				}
			}
			if (throwbe) {
				throw be;
			}
		}
	}

	// Delete Item(s) 
	protected void deleteConfirmedItemsWithExpectedErrorCode(List<Long> itemIds, List<String> errorCodes, Boolean deleteAssociatedItems) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		try {

			/*List<Long> processedItems = */ itemHome.deleteItemsConfirmed(itemIds, deleteAssociatedItems, true, getTestAdminUser());
			
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
				if (errorCodes.contains(entry.getKey())) {
					throwbe = true;
				}
				else {
					Assert.fail(entry.getKey() + " / " + entry.getValue());
				}
			}
			if (throwbe) {
				throw be;
			}
		}
	}

	
	// Save the Item 
	protected Long saveNewItemWithExpectedErrorCode(Long itemId, List<ValueIdDTO> valueIdDTOList, List<String> errorCodes) throws BusinessValidationException, ClassNotFoundException, Throwable {
		// save the item
		Long newItemId = -1L;
		try {
			Map<String,UiComponentDTO> componentDTOMap = null;
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			newItemId = getItemId(componentDTOMap);
			
			// get the item for more validation while getting the saved item and ports
			// Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
			// assertNotNull( item );	
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
		return newItemId;
	}

	protected void validateStatus(Long itemId, Long statusLkpValueCode) throws Throwable {
		validateItemStatus(itemId, statusLkpValueCode);
		validatePortStatus(itemId, statusLkpValueCode); // port status should not change
	}
	
	protected void validateParentItem(Long itemId, Long parentItemId) throws Throwable {
		validateParentItemId(itemId, parentItemId);
	}

	
	protected void validatePortStatus(Long itemId, Long statusLkpValueCode) throws Throwable {
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, getTestAdminUser() );
		
		assertNotNull( item );
		assertTrue(item.size() > 0);

		// validate port status
		List<PowerPortDTO> portDtos = getPP(item);
		for (PowerPortDTO dto: portDtos) {
			Long portStatus = dto.getPortStatusLksValueCode();
			Assert.assertNull(portStatus);
			/*if (null != portStatus) {
				Assert.assertEquals(statusLkpValueCode.longValue(), portStatus.longValue());
			}*/
		}
		
	}
	
	protected void validateGeneratedItemName(Long itemId) throws Throwable {

		String itemName = getItemNameFromDB(itemId);
		Assert.assertTrue("No name generated or generated name invalid", itemName.startsWith("NONAME_"));

	}
	
	protected void validateItemStatus(Long itemId, Long statusLkpValueCode) throws Throwable {

		/*Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null userInfo  );
		
		assertNotNull( item );
		assertTrue(item.size() > 0);

		// validate item status 
		Long itemStatus = getItemStatus(item);*/
		Long itemStatus = getItemStatusFromDB(itemId);
		Assert.assertEquals(statusLkpValueCode.longValue(), itemStatus.longValue());

	}
	
	protected void validateParentItemId(Long itemId, Long expectedParentItemId) throws Throwable {

		Long parentItemId = getItemParentFromDB(itemId);
		Assert.assertEquals(expectedParentItemId.longValue(), parentItemId.longValue());

	}


	protected void validateItemConnections(Long itemId, Long expectedValue) throws Throwable {

		Long actualValue = getNumOfConnections(itemId);
		
		if (expectedValue == 0) {
			Assert.assertEquals(expectedValue.longValue(), actualValue.longValue());
		}
		
		if (expectedValue > 0 && actualValue == 0) {
			Assert.fail("number of connections do  not match, expected = " + expectedValue.toString() + " actual = " + actualValue.toString());
		}
		
	}

	protected void validateItemSourceConnections(Long itemId, Long expectedValue) throws Throwable {

		Long actualValue = getNumOfSourceConnections(itemId);
		
		if (expectedValue == 0) {
			Assert.assertEquals(expectedValue.longValue(), actualValue.longValue());
		}
		
		if (expectedValue > 0 && actualValue == 0) {
			Assert.fail("number of connections do  not match, expected = " + expectedValue.toString() + " actual = " + actualValue.toString());
		}
		
	}

	protected void validateItemDestConnections(Long itemId, Long expectedValue) throws Throwable {

		Long actualValue = getNumOfDestConnections(itemId);
		
		if (expectedValue == 0) {
			Assert.assertEquals(expectedValue.longValue(), actualValue.longValue());
		}
		
		if (expectedValue > 0 && actualValue == 0) {
			Assert.fail("number of connections do  not match, expected = " + expectedValue.toString() + " actual = " + actualValue.toString());
		}
		
	}


	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	    	return null;
	        /*throw new 
	           NullPointerException("Entity passed for initialization is null");*/
	    }

	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}

	private MeItem getMeItem(Item item, String errorCodeInvalidClass, Errors errors) {
		MeItem meItem = null;
		if (item instanceof MeItem) {
			meItem = (MeItem) item;
		}
		else {
			Item itemUnProxy = initializeAndUnproxy(item);
			if (itemUnProxy instanceof MeItem) {
				meItem = (MeItem) itemUnProxy;
			}
			else {
				if (null != errors) {
					Object[] errorArgs = { };
					errors.rejectValue("PowerChain", errorCodeInvalidClass, errorArgs, "Do not support the item class, subclass");
					
				}
				return null;
			}
		}
		return meItem;
	}
	
	protected void validatePowerOutletsCleared() {
		for (Long itemId: powerOutlet) {
			validatePowerOutletCleared(itemId);
		}
		powerOutlet.clear();

	}
	
	protected void validatePowerOutletsNotCleared() {
		for (Long itemId: powerOutlet) {
			validatePowerOutletNotCleared(itemId);
		}
		powerOutlet.clear();
		
	}

	protected void validatePowerOutletNotCleared(Long itemId) {
		Item item = itemDAO.getItemWithPortConnections(itemId);
		MeItem powerOutlet = getMeItem(item, null, null); // (MeItem) itemDAO.getItemWithPortConnections(itemId);
		
		// validate the power outlets me items table, it should not have ups item id and pdupanel item id
		Assert.assertNotNull(powerOutlet.getUpsBankItem());
		Assert.assertNotNull(powerOutlet.getPduPanelItem());
		
		// validate the power outlets ports have no address, breaker port info and busway item id
		Set<PowerPort> ports = powerOutlet.getPowerPorts();
		if (null != ports) {
			for (PowerPort port: ports) {
				Assert.assertNotNull(port.getAddress());
				Assert.assertNotNull(port.getBreakerPort());
				// Assert.assertNull(port.getBuswayItem());
				// validate the power outlets connections to panel removed
				if (null != port.getSourcePowerConnections()) {
					Assert.assertTrue(port.getSourcePowerConnections().size() > 0);
				}
			}
		}
		
		
	}
	
	protected void validatePowerOutletCleared(Long itemId) {
		Item item = itemDAO.getItemWithPortConnections(itemId);
		if (null == item) return;
		MeItem powerOutlet = getMeItem(item, null, null); // (MeItem) itemDAO.getItemWithPortConnections(itemId);
		
		// validate the power outlets me items table, it should not have ups item id and pdupanel item id
		Assert.assertNull(powerOutlet.getUpsBankItem());
		Assert.assertNull(powerOutlet.getPduPanelItem());
		
		// validate the power outlets ports have no address, breaker port info and busway item id
		Set<PowerPort> ports = powerOutlet.getPowerPorts();
		if (null != ports) {
			for (PowerPort port: ports) {
				Assert.assertNull(port.getAddress());
				Assert.assertNull(port.getBreakerPort());
				Assert.assertNull(port.getBuswayItem());
				// validate the power outlets connections to panel removed
				if (null != port.getSourcePowerConnections()) {
					Assert.assertEquals(0, port.getSourcePowerConnections().size());
				}
			}
		}
		
	}

	private List<PowerPortDTO> getPP(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		return powerPortDTOList;
	}

	private Long getItemId(Map<String, UiComponentDTO> item) {
		// get the power ports 
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		Long itemId = (Long) itemNameField.getUiValueIdField().getValueId();
		return itemId;
	}


	// Get the DTO from the item details
	private List<ValueIdDTO> prepareItemValueIdDTOList( Map<String, UiComponentDTO> item ) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
		/*for (Map.Entry<String, UiComponentDTO> pairs : item.entrySet() ) {
			
		}*/
		
	    // @SuppressWarnings("rawtypes")
		// Iterator it = item.entrySet().iterator();
	    // while (it.hasNext()) {
		for (Map.Entry<String, UiComponentDTO> pairs : item.entrySet() ) {
	        // @SuppressWarnings("rawtypes")
			// Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        // it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue();
			assertNotNull( field );
			/* these have no object trace and is not expected by the server */
			if (((String)pairs.getKey()).equals("tiRailWidth") || 
					((String)pairs.getKey()).equals("tiRearClearance") ||
					((String)pairs.getKey()).equals("tiFrontRailOffset") ||
					((String)pairs.getKey()).equals("tiRearRailOffset") || 
					((String)pairs.getKey()).equals("tiLeftClearance") || 
					((String)pairs.getKey()).equals("tiRightClearance") || 
					((String)pairs.getKey()).equals("tiCustomField") ||
					((String)pairs.getKey()).equals("tiRearDoorPerforation") ||
					((String)pairs.getKey()).equals("tiFrontDoorPerforation") ||
					((String)pairs.getKey()).equals("cmbRowLabel") ||
					((String)pairs.getKey()).equals("tiLoadCapacity") ||
					((String)pairs.getKey()).equals("cmbCabinetGrouping") ||
					((String)pairs.getKey()).equals("cmbRowPosition") ||
					((String)pairs.getKey()).equals("tiFrontClearance") ||
					// ((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbChassis") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			/*else if (((String)pairs.getKey()).equals("tabPowerPorts")) {
				dto.setData(powerPortDTOListCopy);
			}*/
			/*else if (((String)pairs.getKey()).equals("tabDataPorts")) {
				dto.setData(dataPortDTOListCopy);
			}*/
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
	    return valueIdDTOList;
	}
	
	private void editPowerPortItemId(Map<String, UiComponentDTO> item, long itemId, long portId) {
		
		// get the power ports 
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		if (null == itemPowerPortsField) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO dto: powerPortDTOList) {
			dto.setPortId(portId);
			dto.setItemId(itemId);
		}
	}

	private void editItemStatus(Map<String, UiComponentDTO> item, long itemStatus) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValueId(itemStatus);
	}

	private void editItemPositionInRow(Map<String, UiComponentDTO> item, long positionInRow) {
		UiComponentDTO itemStatusField = item.get("cmbRowPosition");
		assertNotNull(itemStatusField);
		String statusStr = itemStatusField.getUiValueIdField().getValue().toString();
		Integer statusId = (Integer) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValueId(positionInRow);
	}

	private void editItemRowLabel(Map<String, UiComponentDTO> item, String rowLabel) {
		UiComponentDTO itemStatusField = item.get("cmbRowLabel");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + ((null == statusId) ? "" : statusId.toString()) + "str = " + statusStr);
		itemStatusField.getUiValueIdField().setValue(rowLabel);
	}

	private void editItemName(Map<String, UiComponentDTO> item, String itemName) {
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull(itemNameField);
		String statusStr = (String) itemNameField.getUiValueIdField().getValue();
		Long statusId = (Long) itemNameField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		itemNameField.getUiValueIdField().setValue(itemName);
	}

	private void editItemId(Map<String, UiComponentDTO> item, Long itemId) {
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull(itemNameField);
		String nameStr = (String) itemNameField.getUiValueIdField().getValue();
		Long oldItemId = (Long) itemNameField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + oldItemId.toString() + "str = " + nameStr);
		itemNameField.getUiValueIdField().setValueId(itemId);
	}

	private Long getItemStatus(Map<String, UiComponentDTO> item) {
		UiComponentDTO itemStatusField = item.get("cmbStatus");
		assertNotNull(itemStatusField);
		String statusStr = (String) itemStatusField.getUiValueIdField().getValue();
		Long statusId = (Long) itemStatusField.getUiValueIdField().getValueId();
		System.out.println("item status id = " + statusId.toString() + "str = " + statusStr);
		
		return statusId;
	}
	
	private Long getItemStatusFromDB(Long itemId) {
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("statusLookup", "statusLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("statusLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		return (Long) criteria.uniqueResult();
	}

	private String getItemNameFromDB(Long itemId) {
		Criteria criteria = session.createCriteria(Item.class);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("itemName"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		return (String) criteria.uniqueResult();
	}

	private Long getItemParentFromDB(Long itemId) {
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("parentItem", "parentItem", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("parentItem.itemId"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		return (Long) criteria.uniqueResult();
	}

	protected Long getItemClassLkpValueCode(Long itemId) {
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.property("classLookup.lkpValueCode"))));
		criteria.add(Restrictions.eq("itemId", itemId));
		return (Long) criteria.uniqueResult();
	}

	
	private Long getNumOfConnections(Long itemId) {
		String sql =  " select count(*) from dct_connections_power where source_port_id in (select port_power_id from dct_ports_power where item_id = :itemId) or dest_port_id in (select port_power_id from dct_ports_power where item_id = :itemId) ";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("itemId", itemId);
		
		Long result = (Long) ((BigInteger) query.uniqueResult()).longValue();
		return result;
	}

	private Long getNumOfSourceConnections(Long itemId) {
		String sql =  " select count(*) from dct_connections_power where source_port_id in (select port_power_id from dct_ports_power where item_id = :itemId) ";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("itemId", itemId);
		
		Long result = (Long) ((BigInteger) query.uniqueResult()).longValue();
		return result;
	}

	private Long getNumOfDestConnections(Long itemId) {
		String sql =  "  select count(*) from dct_connections_power where dest_port_id in (select port_power_id from dct_ports_power where item_id = :itemId) ";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("itemId", itemId);
		
		Long result = (Long) ((BigInteger) query.uniqueResult()).longValue();
		return result;
	}

	@SuppressWarnings("unused")
	private Boolean getUsedFlag(Long portId) {
		String sql =  "  select is_used from dct_ports_power where port_power_id = :portId ";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("portId", portId);
		
		Boolean result =  (Boolean) query.uniqueResult();
		return result;
		
	}
	
	protected void validateItemUsedFlag(Long itemId, Boolean expectedValue, int portLksId) {
		String sql =  "  select is_used from dct_ports_power where item_id = :itemId and subclass_lks_id = :portLksId ";
		// SQLQuery query = sf.getCurrentSession().createSQLQuery(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("itemId", itemId);
		query.setParameter("portLksId", portLksId);
		
		@SuppressWarnings("unchecked")
		List<Boolean> result =  query.list();
		for (Boolean value: result) {
			Assert.assertEquals(expectedValue, value);
		}
		
	}

	
	protected void validateItemsDeleted(List<Long> itemIds) {
		String sql =  "  select true from dct_items where item_id in (:itemIds) ";

		SQLQuery q = session.createSQLQuery(sql);
		q.setParameterList("itemIds", itemIds.toArray());
		
		Boolean value =  (Boolean) q.uniqueResult();
		Assert.assertNull(value);
		
	}

	protected void setItemStatus(Item item, Long status, List<String> errorCodes) throws Throwable {
		
		List<ValueIdDTO> itemDto = setItemState(item.getItemId(), status);
		
		// save item with expected error code
		saveItemWithExpectedErrorCode(item.getItemId(), itemDto, errorCodes);
		
		validateStatus(item.getItemId(), status);
		
		// Validate the status of the passive items
		Set<Item> children = item.getChildItems();
		if (null != children) {
			for (Item child: children) {
				
				if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
					validateStatus(child.getItemId(), status);
				}
			}
		}

		
	}
	
	protected void setChildrenStatus(Item item, Long childrenStatus, List<String> errorCodes) throws Throwable {
		
		Set<Item> children = item.getChildItems();
		
		// Set the children items state to 'childrenStatus'
		for (Item child: children) {
			
			if (child.getClassLookup().getLkpValueCode().longValue() == SystemLookup.Class.PASSIVE) {
				continue;
			}

			child.setuPosition(1);
			
			setItemStatus(child, childrenStatus, errorCodes);
			
			validateParentItemId(child.getItemId(), item.getItemId());

		}
	}
	
	protected CabinetItem testContainerFSStatus(Long fromStatus, Long toStatus) throws Throwable {
		
		CabinetItem container = createFSDevWithContainer(fromStatus);
		container.setPositionInRow(1);
		container.setRowLabel("A");

		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(container, toStatus, errorCodes);
		
		Set<Item> children = container.getChildItems();
		
		for (Item child: children) {
			validateStatus(child.getItemId(), toStatus);
			validateParentItemId(child.getItemId(), container.getItemId());
		}
		
		return container;
		
	}
	
	protected CabinetItem testFSDevContainerStatus(Long fromStatus, Long toStatus) throws Throwable {
		
		CabinetItem container = createFSDevWithContainer(fromStatus);
		container.setPositionInRow(1);
		container.setRowLabel("A");

		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setChildrenStatus(container, toStatus, errorCodes);
		
		validateStatus(container.getItemId(), toStatus);
		
		return container;
	}

	protected void testNewItemInState(Item item, Long status, List<String> errorCodes) throws Throwable {
		
		// Create a cabinet item 
		// CabinetItem cabItem = createCabinetInPlannedState();
		
		// set item state to requested status
		List<ValueIdDTO> itemDto = getDto(item.getItemId()); //setItemState(item.getItemId(), status);
		// List<String> noErrorCodes = new ArrayList<String>(); // no error expected
		// saveItemWithExpectedErrorCode(item.getItemId(), itemDto, noErrorCodes);

		// validate status
		// validateStatus(item.getItemId(), status);
		
		itemDto = setItemState(item.getItemId(), status);
		
		itemHome.deleteItem(item.getItemId(), false, getTestAdminUser());
		
		// save item with expected error code
		Long itemId = saveNewItemWithExpectedErrorCode(-1L, itemDto, errorCodes);

		// TODO:: set the item ids for the ports (data, power and sensor)
		
		// delete the new item
		itemHome.deleteItem(itemId, false, getTestAdminUser());
		

	}

	protected final void testStorageCabinetWithPassiveItem(Long testStatus) throws Throwable {
		
		// Create Cabinet item with children - 2 Child
		CabinetItem cabItem = createCabinetWithChildrenInPlanned(false, true);
		
		// set cabinet state to testStatus
		List<ValueIdDTO> itemDto = setItemState(cabItem.getItemId(), testStatus);

		// save item with expected error code
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		saveItemWithExpectedErrorCode(cabItem.getItemId(), itemDto, errorCodes);

		// check the children items state as testStatus
		Set<Item> children = cabItem.getChildItems();
		for (Item child: children) {
			// Passive item should be in the same state as cabinet
			validateStatus(child.getItemId(), testStatus);
			
			// the parent item id should be still set to the cabinet
			validateParentItem(child.getItemId(), cabItem.getItemId());
			
		}
		
	}
	
	private void testItemFromStatetoRequestedState(Item item, Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		session.refresh(item);
		session.clear();
		
		List<String> errorCodes = new ArrayList<String>(); // no error expected
		setItemStatus(item, testingState, errorCodes);
		
		session.refresh(item);
		session.clear();
		// Set the children items state to 'childrenStatus'
		
		if (item instanceof CabinetItem) {
			CabinetItem cabItem = (CabinetItem) item;
			cabItem.setPositionInRow(1);
			cabItem.setRowLabel("A");
		}
		
		List<Long> setPlacementAgainStates = Arrays.asList(SystemLookup.ItemStatus.ARCHIVED, SystemLookup.ItemStatus.STORAGE);
		
		if (setPlacementAgainStates.contains(testingState)) {
			// Set the placement information for the item, because when item is placed in archive state, the placement information is cleared
			CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", SystemLookup.ItemStatus.INSTALLED);
			item.setParentItem(cabinetItem);
			item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
			item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT));
			item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
			itemHome.saveItem(item);
		}
		
		// errorCodes.add(e);
		setItemStatus(item, destinationState, expectedErrorCodes);
		
	}
	
	// Create VM in planned, move to installed and then move to other requested states
	protected void testVMFromStateToRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		ItItem vmItem = createNewTestDeviceVM("VM_FROM_INSTALLED_TO_OTHER_STATE");
		
		testItemFromStatetoRequestedState(vmItem, testingState, destinationState, expectedErrorCodes);

	}
	
	protected void testVMFromStateToStateError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testVMFromStateToRequestedState(fromState, toState, errorCodes);
		
	}
	
	protected void testVMFromStateToStateNoError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testVMFromStateToRequestedState(fromState, toState, errorCodes);
		
	}

	
	// Create Cabinet in planned, move to installed and then move to other requested states
	protected void testCabinetFromInstalledToRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		CabinetItem cabItem = createCabinetInInstalledState();
		
		session.refresh(cabItem);
		session.clear();

		setItemStatus(cabItem, destinationState, expectedErrorCodes);

	}

	
	protected void testCabinetFromStatetoRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		CabinetItem cabItem = createCabinetInPlannedState();
		
		testItemFromStatetoRequestedState(cabItem, testingState, destinationState, expectedErrorCodes);

	}
	
	protected void testCabinetFromStateToStateError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testCabinetFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	protected void testCabinetFromStateToStateNoError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		
		testCabinetFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	// Create Device Chassis in planned, move to installed and then move to other requested states
	protected void testDeviceFromStatetoRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		ItItem itItem = null;
		
		if (testingState.longValue() == SystemLookup.ItemStatus.POWERED_OFF || 
				testingState.longValue() == SystemLookup.ItemStatus.OFF_SITE || 
				testingState.longValue() == SystemLookup.ItemStatus.INSTALLED) {
			itItem = createChassisInState(testingState);
		}
		else {
			itItem = createNewTestDeviceChassis("DEV-CHASSIS-STATE_TRAN-TEST");
		}
		
		testItemFromStatetoRequestedState(itItem, testingState, destinationState, expectedErrorCodes);

	}
	
	protected void testDeviceFromStateToStateError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testDeviceFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}
	
	protected void testDeviceFromStateToStateNoError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		
		testDeviceFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	// Create Network Chassis in planned, move to installed and then move to other requested states
	protected void testNetworkFromStatetoRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		ItItem itItem = null;
		
		if (testingState.longValue() == SystemLookup.ItemStatus.POWERED_OFF || 
				testingState.longValue() == SystemLookup.ItemStatus.OFF_SITE ||
				testingState.longValue() == SystemLookup.ItemStatus.INSTALLED) {
			itItem = createNWChassisInState(testingState);
		}
		else {

			itItem = createNewTestNetworkChassis("NW-CHASSIS-STATE_TRAN-TEST");
		}
		
		testItemFromStatetoRequestedState(itItem, testingState, destinationState, expectedErrorCodes);

	}
	
	protected void testNetworkFromStateToStateError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testNetworkFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	protected void testNetworkFromStateToStateNoError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		
		testNetworkFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	// Create Network Chassis in planned, move to installed and then move to other requested states
	protected void testProbeFromStatetoRequestedState(Long testingState, Long destinationState, List<String> expectedErrorCodes) throws Throwable {
		
		Long itemInitailStatus = SystemLookup.ItemStatus.PLANNED;
		if (testingState.longValue() == SystemLookup.ItemStatus.POWERED_OFF || 
				testingState.longValue() == SystemLookup.ItemStatus.OFF_SITE ||
				testingState.longValue() == SystemLookup.ItemStatus.INSTALLED) {
			itemInitailStatus = testingState;
		}
		ItItem itItem = createNewTestProbe("PROBE-STATE_TRAN-TEST", itemInitailStatus);
		
		testItemFromStatetoRequestedState(itItem, testingState, destinationState, expectedErrorCodes);

	}
	
	protected void testProbeFromStateToStateError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.invalidTransition");
		
		testProbeFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}

	protected void testProbeFromStateToStateNoError(Long fromState, Long toState) throws Throwable {

		List<String> errorCodes = new ArrayList<String>();
		
		testProbeFromStatetoRequestedState(fromState, toState, errorCodes);
		
	}


}
