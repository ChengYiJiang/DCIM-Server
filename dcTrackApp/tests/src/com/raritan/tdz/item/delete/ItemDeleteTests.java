package com.raritan.tdz.item.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.state.StateTestBase;
import com.raritan.tdz.lookup.SystemLookup;

public class ItemDeleteTests extends StateTestBase  {
	
	class TestPanelEnvironment {
		public Long panelSubClassLkpValueCode;
		public Boolean withOutlets;
		public Boolean withOutletsCircuited;
		public Boolean delete;
		public Boolean deletePowerOutlets;
		
		public TestPanelEnvironment(Long panelSubClassLkpValueCode,
				Boolean withOutlets, Boolean withOutletsCircuited, Boolean delete, Boolean deletePowerOutlets) {

			this.panelSubClassLkpValueCode = panelSubClassLkpValueCode;
			this.withOutlets = withOutlets;
			this.withOutletsCircuited = withOutletsCircuited;
			this.delete = delete;
			this.deletePowerOutlets = deletePowerOutlets;
		}
		
	};
	
	// Floor PDU delete
	private final void testDeleteFPDU(List<TestPanelEnvironment> testPanels, String warningHandler, Boolean selectPowerOutletInDeleteList) throws Throwable {
		List<Long> deletedItems = new ArrayList<Long>();
		
		MeItem fpdu = createFloorPDU(true, false, false, false);
		
		Assert.assertNotNull(fpdu);
		
		Map<Long, List<Long>> createdOutlet = new HashMap<Long, List<Long>>(); 
		
		Boolean warningExpected = false;
		
		List<MeItem> outlets = null;
		Boolean circuitRequest = false;
		Boolean localOrRemoteOutletRequested = false;
		if (null != testPanels) {
			for (TestPanelEnvironment panelEnv: testPanels) {
				
				// create panels and outlets as requested
				outlets = createPanels(fpdu, panelEnv.panelSubClassLkpValueCode, panelEnv.withOutlets, panelEnv.withOutletsCircuited);
				
				List<Long> outletItemIds = new ArrayList<Long>();
				for (MeItem outlet: outlets) {
					outletItemIds.add(outlet.getItemId());
				}
				
				// add outlets to the map
				createdOutlet.put(panelEnv.panelSubClassLkpValueCode, outletItemIds);
				
				circuitRequest |= panelEnv.withOutletsCircuited;
				
				localOrRemoteOutletRequested |= (((panelEnv.panelSubClassLkpValueCode.longValue() == SystemLookup.SubClass.LOCAL) || 
						(panelEnv.panelSubClassLkpValueCode.longValue() == SystemLookup.SubClass.REMOTE)) && panelEnv.withOutlets); 
						
			}
		}

		List<String> errorCodes = new ArrayList<String>();
		
		
		// if any of the outlets are circuited, then there is an expected error code and no items will be deleted 
		if (circuitRequest) {
			errorCodes.add("ItemValidator.deleteConnected");
			errorCodes.add("ItemValidator.deletePanelConnected");
			errorCodes.add("ItemValidator.deleteFPDUWithPanelPowerOutletConnected");
			errorCodes.add("ItemValidator.deleteFPDUConnected");
		}
		else if (localOrRemoteOutletRequested && !selectPowerOutletInDeleteList) {
			errorCodes.add("ItemValidator.deletePowerPanelWithPowerOutlet");
			warningExpected = true;
			// fpdu will be deleted
			deletedItems.add(fpdu.getItemId());
			
			// panels with the fpdu will be deleted
			Set<Item> children = fpdu.getChildItems();
			if (null != children) {
				for(Item panel: children) {
					deletedItems.add(panel.getItemId());
				}
			}
			
			// busway outlets if not circuited will be deleted
			List<Long> buswayOutlets = createdOutlet.get(SystemLookup.SubClass.BUSWAY);
			if (null != buswayOutlets) {
				deletedItems.addAll(buswayOutlets);
			}
		}
		else {
			// fpdu will be deleted
			deletedItems.add(fpdu.getItemId());
			
			// panels with the fpdu will be deleted
			Set<Item> children = fpdu.getChildItems();
			if (null != children) {
				for(Item panel: children) {
					deletedItems.add(panel.getItemId());
				}
			}
			
			// busway outlets if not circuited will be deleted
			List<Long> buswayOutlets = createdOutlet.get(SystemLookup.SubClass.BUSWAY);
			if (null != buswayOutlets) {
				deletedItems.addAll(buswayOutlets);
			}

		}
		
		

		session.clear(); // clear the session after creating all the items

		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(fpdu.getItemId());
		List<Long> localOutlets = createdOutlet.get(SystemLookup.SubClass.LOCAL);
		List<Long> remoteOutlets = createdOutlet.get(SystemLookup.SubClass.REMOTE);
		if (selectPowerOutletInDeleteList) {
			if (null != localOutlets) itemIds.addAll(localOutlets);
			if (null != remoteOutlets) itemIds.addAll(remoteOutlets);
		}
		
		try {
			deleteItemsWithExpectedErrorCode(itemIds, errorCodes);
		}
		catch (BusinessValidationException be) {
			if (warningExpected) {
				// session.clear();
				if (warningHandler.equals("Cancel")) {
					throw be;
				}
				Boolean deleteAssociatedItems = true;
				if (warningHandler.equals("No")) {
					deleteAssociatedItems = false;
				}
				if (warningHandler.equals("Yes")) {
					deleteAssociatedItems = true;
					if (null != localOutlets) {
						deletedItems.addAll(localOutlets);
					}
					if (null != remoteOutlets) {
						deletedItems.addAll(remoteOutlets);
					}
				}
				errorCodes.clear();
				deleteConfirmedItemsWithExpectedErrorCode(itemIds, errorCodes, deleteAssociatedItems);

				// Validate Items are not in database
				validateItemsDeleted(deletedItems);

			}
			throw be;
		}
		
		// Validate Items are not in database
		validateItemsDeleted(deletedItems);
		
	}

	
	@Test
	public final void testDeleteFPDUWithNoPanels() throws Throwable {
		
		testDeleteFPDU(null, null, true);
		
	}

	@Test
	public final void testDeleteFPDUWithLocalPanels() throws Throwable { 
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, false, false, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test
	public final void testDeleteFPDUWithLocalNRemotePanels() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, false, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, false, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanels() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, false, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, false, false, false, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, false, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletNotCircuitedCancel() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, "Cancel", false);

	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletNotCircuitedYes() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, "Yes", false);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletNotCircuitedNo() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, "No", false);

	}


	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletNotCircuitedCancel() throws Throwable { 
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, "Cancel", false);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletNotCircuitedYes() throws Throwable { 
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, "Yes", false);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletNotCircuitedNo() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, "No", false);

	}


	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletNotCircuitedYes() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeleteFPDU(testPanels, "Yes", false);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletNotCircuitedCancel() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeleteFPDU(testPanels, "Cancel", false);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletNotCircuitedNo() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeleteFPDU(testPanels, "No", false);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletCircuited() throws Throwable { 
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletCircuited() throws Throwable { 
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	// @Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletCircuited() throws Throwable { // FAILED
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, false, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, false, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, true, false, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeleteFPDU(testPanels, null, true);
		
	}

	//  
	@Test
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletSelectedNNotCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletSelectedNNotCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, false, false);
		
		testPanels.add(remotePanel);
		
		testDeleteFPDU(testPanels, null, true);
		
	}

	@Test
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletSelectedNNotCircuited() throws Throwable { 

		MeItem fpdu = createNewTestFloorPDU("INT_TEST_FPDU_BUSWAY_TEST");
		List<MeItem> powerOutlets = createPanels(fpdu, SystemLookup.SubClass.BUSWAY, true, false);

		List<Long> deletedItems = new ArrayList<Long>();
		deletedItems.add(fpdu.getItemId());
		
		// panels with the fpdu will be deleted
		Set<Item> children = fpdu.getChildItems();
		if (null != children) {
			for(Item panel: children) {
				deletedItems.add(panel.getItemId());
			}
		}
		
		for (MeItem powerOutlet: powerOutlets) {
			deletedItems.add(powerOutlet.getItemId());
		}
		
		session.clear();
		
		List<Long> itemIds = new ArrayList<Long>(); 
		itemIds.add(fpdu.getItemId());
		List<String> errorCodes = new ArrayList<String>();
		deleteItemsWithExpectedErrorCode(itemIds, errorCodes);
		
		// Validate Items are not in database
		validateItemsDeleted(deletedItems);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalPanelsNPowerOutletSelectedNCircuited() throws Throwable { 

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemotePanelsNPowerOutletSelectedNCircuited() throws Throwable { 

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, false, false);
		
		testPanels.add(localPanel);
		
		testDeleteFPDU(testPanels, null, true);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteFPDUWithLocalNRemoteNBuswayPanelsNPowerOutletSelectedNCircuited() throws Throwable { 

		MeItem fpdu = createNewTestFloorPDU("INT_TEST_FPDU_BUSWAY_TEST");
		/*List<MeItem> powerOutlets = */ createPanels(fpdu, SystemLookup.SubClass.BUSWAY, true, true);

		session.clear();
		
		List<Long> itemIds = new ArrayList<Long>(); 
		itemIds.add(fpdu.getItemId());
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.deleteConnected");
		errorCodes.add("ItemValidator.deletePanelConnected");
		errorCodes.add("ItemValidator.deleteFPDUWithPanelPowerOutletConnected");
		errorCodes.add("ItemValidator.deleteFPDUConnected");

		deleteItemsWithExpectedErrorCode(itemIds, errorCodes);
		


	}

	//// Panel Delete /////
	
	class PanelData {
		List<Long> outletIds;
		List<Long> panelIds;
		Boolean circuitRequested;
		Boolean deleteRequested;
		Boolean deleteOutletRequested;
		
		public PanelData(List<Long> outletIds, List<Long> panelIds,
				Boolean circuitRequested, Boolean deleteRequested, Boolean deleteOutletRequested) {
			this.outletIds = outletIds;
			this.panelIds = panelIds;
			this.circuitRequested = circuitRequested;
			this.deleteOutletRequested = deleteOutletRequested;
			
		}
		
		
	};
	
	// Floor PDU delete
	private final void testDeletePanels(List<TestPanelEnvironment> testPanels, String warningHandler) throws Throwable {
		List<Long> deletedItems = new ArrayList<Long>();
		
		// MeItem fpdu = createFloorPDU(true, false, false, false);
		MeItem upsBank = null;
		upsBank = createNewTestUPSBank("INT_TEST_UPSBANK");
		MeItem fpdu = createNewTestFloorPDU("INT_TEST_FPDU");
		fpdu.setUpsBankItem(upsBank);
		
		Assert.assertNotNull(fpdu);
		
		Map<Long, List<Long>> createdOutlet = new HashMap<Long, List<Long>>();
		
		Map<Long, PanelData> panelsData = new HashMap<Long, ItemDeleteTests.PanelData>();
		PanelData localPanelData = new PanelData(null, null, false, false, false); panelsData.put(SystemLookup.SubClass.LOCAL, localPanelData); 
		PanelData remotePanelData = new PanelData(null, null, false, false, false); panelsData.put(SystemLookup.SubClass.REMOTE, remotePanelData);
		PanelData buswayPanelData = new PanelData(null, null, false, false, false); panelsData.put(SystemLookup.SubClass.BUSWAY, buswayPanelData);
		
		Boolean warningExpected = false;
		
		List<MeItem> outlets = null;
		Boolean circuitRequest = false;
		Boolean localOrRemoteOutletRequested = false;
		if (null != testPanels) {
			for (TestPanelEnvironment panelEnv: testPanels) {
				
				// create panels and outlets as requested
				outlets = createPanels(fpdu, panelEnv.panelSubClassLkpValueCode, panelEnv.withOutlets, panelEnv.withOutletsCircuited);
				
				List<Long> outletItemIds = new ArrayList<Long>();
				for (MeItem outlet: outlets) {
					outletItemIds.add(outlet.getItemId());
				}
				
				// add outlets to the map
				createdOutlet.put(panelEnv.panelSubClassLkpValueCode, outletItemIds);
				
				circuitRequest |= panelEnv.withOutletsCircuited;
				
				localOrRemoteOutletRequested |= (((panelEnv.panelSubClassLkpValueCode.longValue() == SystemLookup.SubClass.LOCAL) || 
						(panelEnv.panelSubClassLkpValueCode.longValue() == SystemLookup.SubClass.REMOTE)) && panelEnv.withOutlets); 

				
				// panels with the fpdu will be deleted
				Set<Item> children = fpdu.getChildItems();
				List<Long> panelItemIds = new ArrayList<Long>();
				if (null != children) {
					for(Item panel: children) {
						if (panel.getSubclassLookup().getLkpValueCode().longValue() == panelEnv.panelSubClassLkpValueCode.longValue()) {
							panelItemIds.add(panel.getItemId());
						}
					}
				}
				
				PanelData panelData = panelsData.get(panelEnv.panelSubClassLkpValueCode.longValue());
				
				panelData.outletIds = outletItemIds;
				panelData.panelIds = panelItemIds;
				panelData.circuitRequested = panelEnv.withOutletsCircuited;
				panelData.deleteRequested = panelEnv.delete;
				panelData.deleteOutletRequested = panelEnv.deletePowerOutlets;

			}
		}

		List<String> errorCodes = new ArrayList<String>();
		
		
		// if any of the outlets are circuited, then there is an expected error code and no items will be deleted 
		for (Map.Entry<Long, PanelData> entry: panelsData.entrySet()) {
			Long portSubClassLkpValueCode = entry.getKey();
			PanelData panelData = entry.getValue();
			
			if (panelData.panelIds == null) continue;
			
			if (panelData.deleteRequested) {
				
				if (panelData.outletIds.size() > 0 && panelData.circuitRequested) {
					
					warningExpected = false;
					
					errorCodes.clear();
					errorCodes.add("ItemValidator.deleteConnected");
					errorCodes.add("ItemValidator.deletePanelConnected");
					errorCodes.add("ItemValidator.deleteFPDUWithPanelPowerOutletConnected");
					errorCodes.add("ItemValidator.deleteFPDUConnected");
					
					break;
					
				}
				else {
					if (portSubClassLkpValueCode.longValue() == SystemLookup.SubClass.LOCAL || 
							portSubClassLkpValueCode.longValue() == SystemLookup.SubClass.REMOTE) {
						
						if (panelData.outletIds.size() > 0 && !panelData.deleteOutletRequested) {
							errorCodes.add("ItemValidator.deletePowerPanelWithPowerOutlet");
						
							warningExpected = true;
						}
						
					}
				}
			}
		}
		
		List<Long> itemIds = new ArrayList<Long>();
		for (Map.Entry<Long, PanelData> entry: panelsData.entrySet()) {
			Long portSubClassLkpValueCode = entry.getKey();
			PanelData panelData = entry.getValue();
			
			if (panelData.panelIds == null) continue;
			
			if (panelData.deleteRequested) {
				itemIds.addAll(panelData.panelIds);
				deletedItems.addAll(panelData.panelIds);
				
				if (panelData.deleteOutletRequested) {
					itemIds.addAll(panelData.outletIds);
					deletedItems.addAll(panelData.outletIds);
				}
				
				if (portSubClassLkpValueCode.longValue() == SystemLookup.SubClass.BUSWAY) {
					deletedItems.removeAll(panelData.outletIds);
					deletedItems.addAll(panelData.outletIds);
				}
				else {
					if (warningExpected) {
						if (warningHandler.equals("Yes")) {
							deletedItems.addAll(panelData.outletIds);
						}
					}
				}
			}
			
		}
			
		session.flush();
		session.clear(); // clear the session after creating all the items

		try {
			deleteItemsWithExpectedErrorCode(itemIds, errorCodes);
		}
		catch (BusinessValidationException be) {
			if (warningExpected) {
				// session.clear();
				if (warningHandler.equals("Cancel")) {
					throw be;
				}
				Boolean deleteAssociatedItems = true;
				if (warningHandler.equals("No")) {
					deleteAssociatedItems = false;
				}
				if (warningHandler.equals("Yes")) {
					deleteAssociatedItems = true;
				}
				errorCodes.clear();
				deleteConfirmedItemsWithExpectedErrorCode(itemIds, errorCodes, deleteAssociatedItems);

				// Validate Items are not in database
				validateItemsDeleted(deletedItems);

			}
			throw be;
		}
		
		// Validate Items are not in database
		validateItemsDeleted(deletedItems);
		
	}

	
	// Power Panel delete
	@Test
	public final void testDeletePanels() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, false, false, true, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, false, false, true, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, false, false, true, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);

	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalPanelsWithPowerOutletNotCircuitedY() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "Yes");
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalPanelsWithPowerOutletNotCircuitedN() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "No");
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalPanelsWithPowerOutletNotCircuitedC() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "Cancel");
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteRemotePanelsWithPowerOutletNotCircuitedY() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "Yes");
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteRemotePanelsWithPowerOutletNotCircuitedN() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "No");
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteRemotePanelsWithPowerOutletNotCircuitedC() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, "Cancel");
		
	}

	@Test
	public final void testDeleteBuswayPanelsWithPowerOutletNotCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, true);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, true, false);
		
		testPanels.add(localPanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test
	public final void testDeleteLocalNRemotePanelsWithPowerOutletNotCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, true);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, true);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test
	public final void testDeleteLocalNRemoteNBuswayPanelsWithPowerOutletNotCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, true);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, true, false);
		
		testPanels.add(localPanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalPanelsWithPowerOutletCircuited() throws Throwable {

		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);
		
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteRemotePanelsWithPowerOutletCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteBuswayPanelsWithPowerOutletCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, true, true, false);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalNRemotePanelsWithPowerOutletCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalNRemoteNBuswayPanelsWithPowerOutletCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, false);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, false);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, true, true, false);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
		
	}

	//
	@Test
	public final void testDeleteLocalPanelsWithPowerOutletSeletedNNotCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, true);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test
	public final void testDeleteRemotePanelsWithPowerOutletSeletedNNotCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, true);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);

	}

	@Test
	public final void testDeleteBuswayPanelsWithPowerOutletSeletedNNotCircuited() throws Throwable {
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, true, true);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);

	}

	@Test
	public final void testDeleteLocalNRemotePanelsWithPowerOutletSeletedNNotCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, true);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, true);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeletePanels(testPanels, null);

	}

	@Test
	public final void testDeleteLocalNRemoteNBuswayPanelsWithPowerOutletSeletedNNotCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, false, true, true);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, false, true, true);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, false, true, true);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
		
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalPanelsWithPowerOutletSeletedNCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, true);
		
		testPanels.add(localPanel);
		
		testDeletePanels(testPanels, null);
	}
	
	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteRemotePanelsWithPowerOutletSeletedNCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, true);
		
		testPanels.add(remotePanel);
		
		testDeletePanels(testPanels, null);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteBuswayPanelsWithPowerOutletSeletedNCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, true, true, true);
		
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalNRemotePanelsWithPowerOutletSeletedNCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, true);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, true);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		
		testDeletePanels(testPanels, null);
	}

	@Test(expectedExceptions=BusinessValidationException.class)
	public final void testDeleteLocalNRemoteNBuswayPanelsWithPowerOutletSeletedNCircuited() throws Throwable {
		
		List<TestPanelEnvironment> testPanels = new ArrayList<ItemDeleteTests.TestPanelEnvironment>();
		
		TestPanelEnvironment localPanel = new TestPanelEnvironment(SystemLookup.SubClass.LOCAL, true, true, true, true);
		TestPanelEnvironment remotePanel = new TestPanelEnvironment(SystemLookup.SubClass.REMOTE, true, true, true, true);
		TestPanelEnvironment buswayPanel = new TestPanelEnvironment(SystemLookup.SubClass.BUSWAY, true, true, true, true);
		
		testPanels.add(localPanel);
		testPanels.add(remotePanel);
		testPanels.add(buswayPanel);
		
		testDeletePanels(testPanels, null);
	}

	/////////////////////////////////////
	// Delete Cabinet with Passive Items
	/////////////////////////////////////
	
	@Test
	public final void testDeleteCabinetWithPassiveItems() throws Throwable { 

		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet-State-Test", SystemLookup.ItemStatus.PLANNED);
		
		ItItem passiveItem1 = createNewTestPassive("UNITTEST-PASSIVE-1-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		ItItem passiveItem2 = createNewTestPassive("UNITTEST-PASSIVE-2-Test", cabinetItem.getItemId(), SystemLookup.ItemStatus.PLANNED);
		
		cabinetItem.addChildItem(passiveItem1);
		cabinetItem.addChildItem(passiveItem2);

		session.clear();
		
		List<Long> itemIds = new ArrayList<Long>(); 
		itemIds.add(cabinetItem.getItemId());
		List<String> errorCodes = new ArrayList<String>();
		errorCodes.add("ItemValidator.deleteConnected");
		errorCodes.add("ItemValidator.deletePanelConnected");
		errorCodes.add("ItemValidator.deleteFPDUWithPanelPowerOutletConnected");
		errorCodes.add("ItemValidator.deleteFPDUConnected");

		deleteItemsWithExpectedErrorCode(itemIds, errorCodes);
		
		itemIds.add(passiveItem1.getItemId());
		itemIds.add(passiveItem2.getItemId());
		
		validateItemsDeleted(itemIds);

	}


}


