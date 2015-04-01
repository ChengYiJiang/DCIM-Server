package com.raritan.tdz.powerchain;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.springframework.util.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.item.home.UnitTestItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class FloorPDUTestsGUI extends PowerChainTestBase {
	private ItemDAO itemDAO;
	// private ItemFinderDAO itemFinderDAO;
	private ItemFinderDAO itemFinderDAO;

	PowerConnDAO powerConnDAO;

	PowerPortDAO powerPortDAO;

	UnitTestItemDAO unitTestItemDAO;

	private static final long cmbLocationId = 1;
	private static final long cmbMakeId = 27;
	private static final long cmbModelId = 58;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemDAO = (ItemDAO) ctx.getBean("itemDAO");
		itemFinderDAO = (ItemFinderDAO) ctx.getBean("itemDAO");
		powerConnDAO = (PowerConnDAO) ctx.getBean("powerConnectionDAO");
		powerPortDAO = (PowerPortDAO) ctx.getBean("powerPortDAO");
		unitTestItemDAO = (UnitTestItemDAO) ctx.getBean("unitTestItemDAO");
		//unitTestItemDAO.setSf(this.sf);

	}

	private class ItemDTOBuilder {
		List<ValueIdDTO> dtoList;

		List<ValueIdDTO> getValueIdDTOList() {
			return dtoList;
		}

		Map<String, Object> getValueIDDtoMap() {
			Map<String, Object> valueIdDTOMap = new HashMap<String, Object>();

			if (dtoList != null) {
				for (ValueIdDTO dto : dtoList) {
					valueIdDTOMap.put(dto.getLabel(), dto.getData());
				}
			}
			return valueIdDTOMap;
		}

		void setRating(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("tiFPDURatingkVA");
			dto.setData(value);
			dtoList.add(dto);
		}

		void setInputBreaker(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("tiFPDUBrkrAmps");
			dto.setData(value);
			dtoList.add(dto);
		}

		void setUpsBank(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("cmbFromUPSBank");
			dto.setData(value);
			dtoList.add(dto);
		}

		void setBreakerport(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("cmbFromFPDUBreaker");
			dto.setData(value);
			dtoList.add(dto);
		}

		void setOutputWiring(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("cmbFPDUOutputWiring");
			dto.setData(value);
			dtoList.add(dto);
		}

		void setItemStatus(Long value) {
			ValueIdDTO dto = new ValueIdDTO();
			dto.setLabel("cmbStatus");
			dto.setData(value);
			dtoList.add(dto);
		}

		ItemDTOBuilder(Long location, Long make, Long model, String name) {
			ValueIdDTO dto = null;
			if (name != null) {
				dtoList = new ArrayList<ValueIdDTO>();
				dto = new ValueIdDTO();
				dto.setLabel("tiName");
				dto.setData(name);
				dtoList.add(dto);
			}

			if (location != null) {
				dto = new ValueIdDTO();
				dto.setLabel("cmbLocation");
				dto.setData(location);
				dtoList.add(dto);
			}

			if (make != null) {
				dto = new ValueIdDTO();
				dto.setLabel("cmbMake");
				dto.setData(make);
				dtoList.add(dto);
			}

			if (model != null) {
				dto = new ValueIdDTO();
				dto.setLabel("cmbModel");
				dto.setData(model);
				dtoList.add(dto);
			}
			dtoList.add(createValueIdDTOObj("tiCustomField",
					createCustomFields()));
		}
	}

	protected UserInfo createGatekeeperUserInfo() {
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941", "", "1", "en-US",
				"site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}

	private long getItemIdFromDTOMap(Map<String, UiComponentDTO> itemMap) {
		long itemId = -1;

		UiComponentDTO componentDTO = itemMap.get("tiName");
		if (componentDTO != null) {
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if (uiValueIdField != null)
				itemId = (Long) uiValueIdField.getValueId();
		}
		return itemId;
	}

	protected void printItemDetailsMap(Map<String, UiComponentDTO> mapToPrint) {
		try {
			System.out.println("====== Map content: ");
			Set<String> keySet = mapToPrint.keySet();
			for (String s1 : keySet) {
				UiComponentDTO componentDTO = mapToPrint.get(s1);
				if (componentDTO != null) {
					UiValueIdField uiValueIdField = componentDTO
							.getUiValueIdField();
					if (uiValueIdField != null) {
						if (uiValueIdField.getValue() != null) {
							System.out.println("key=" + s1
									+ ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId() + ", value="
									+ uiValueIdField.getValue().toString());
							if (uiValueIdField.getValueId().equals(
									"tabSensorPorts")) {
								System.out.println("SensorPort");
							}
						} else {
							System.out.println("key=" + s1
									+ ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId());
						}
					} else {
						System.out.println("key=" + s1 + "val=");
					}
				} else
					System.out.println("key=" + s1 + ", but val not exist");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void validateItemDetailsMap(Map<String, UiComponentDTO> mapToPrint,
			long itemId, ItemDTOBuilder bldr) throws Throwable {
		try {
			Map<String, Object> dtoMap = bldr.getValueIDDtoMap();
			Map<String, Object> retValuesMap = new HashMap<String, Object>();
			Set<String> keySet = mapToPrint.keySet();
			for (String key : keySet) {
				UiComponentDTO dto = mapToPrint.get(key);
				Assert.isTrue(dto != null);
				UiValueIdField uiValueIdField = dto.getUiValueIdField();
				if (uiValueIdField != null) {
					Object obj = uiValueIdField.getValue();
					if (obj != null)
						retValuesMap.put(key, obj);
				}
			}

			Assert.isTrue(retValuesMap != null);

			MeItem fpdu = (MeItem) itemFinderDAO.findById(itemId).get(0);
			MeItem ups = (MeItem) itemFinderDAO.findById(
					fpdu.getUpsBankItem().getItemId()).get(0);
			String upsString = ups.getItemName() + " in "  + ups.getDataCenterLocation().getCode();
			Assert.isTrue(((String) retValuesMap.get("cmbFromUPSBank")).equals(upsString));
			
			// Validate that client got correct value for imput wiring

			StringBuilder phaseStr = new StringBuilder();
			if (ups.getPhaseLookup().getLkpValueCode().longValue() == SystemLookup.PhaseIdClass.THREE_WYE) {
				phaseStr.append("4-Wire + Ground");
			} else {
				phaseStr.append("3-Wire + Ground");
			}
			Assert.isTrue(phaseStr.toString().equals(
					(String) retValuesMap.get("tiFPDUInputWiring")));
			Assert.isTrue(retValuesMap.get("cmbFromFPDUBreaker") == null);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * Create FPDU and associate it with UPS bank. Next, edit FPDU and now
	 * disassociate with previous bank and not not asssociate it with any other
	 * UPS bank. Save item.
	 */
	@Test
	public void test3EditPDU() throws BusinessValidationException,
			ClassNotFoundException, Throwable {
		long itemId = 0L;
		long retItemId = 0L;
		try {
			// this call will create FPDU and associate it with UPS bank
			itemId = testCreateFPDUCore();

			// New DTO
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU9");
			itemBldr.setUpsBank(null);
			UserInfo userInfo = createGatekeeperUserInfo();

			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(retItemId > 0);
			printItemDetailsMap(retval);

			// validate connection between PDU and UPSB was broken
			MeItem fpdu = (MeItem) itemFinderDAO.findById(retItemId).get(0);

			Hibernate.initialize(fpdu.getUpsBankItem());
			itemDAO.initPowerPortsAndConnectionsProxy(fpdu);
			Assert.isTrue(fpdu.getUpsBankItem() == null);
			PowerPort fpduPort = getPort(fpdu,
					SystemLookup.PortSubClass.PDU_INPUT_BREAKER);

			Set<PowerConnection> dctPowerConnections = fpduPort
					.getDestPowerConnections();
			Assert.isTrue(dctPowerConnections == null
					|| dctPowerConnections.size() == 0);
		} finally {
			if (retItemId != 0)
				deleteFpdu(retItemId);
			if (itemId != 0)
				deleteFpdu(itemId);
		}
	}

	/*
	 * - create FPDU and connect it to the UPSBank. Validate ratings and circuit
	 */
	@Test
	public void testCreateFPDU() throws BusinessValidationException,
			ClassNotFoundException, Throwable {
		long itemId = testCreateFPDUCore();

		deleteFpdu(itemId);
	}

	@Test
	public void testReturnedUiComponents() throws BusinessValidationException,
			ClassNotFoundException, Throwable {
		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 300L;
		long upsBankId = 1L;
		UserInfo userInfo = createGatekeeperUserInfo();
		try {
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU8");
			itemBldr.setInputBreaker(ratingAmps);
			itemBldr.setRating(ratingKva);
			itemBldr.setOutputWiring(SystemLookup.PhaseIdClass.THREE_WYE);
			itemBldr.setUpsBank(upsBankId);

			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Long itemId = -1L;

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);
			printItemDetailsMap(retval);
			retItemId = getItemIdFromDTOMap(retval);
			validateItemDetailsMap(retval, retItemId, itemBldr);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			deleteFpdu(retItemId);
		}
	}

	private void deleteFpdu(long itemId) throws BusinessValidationException,
			Throwable {
		if (itemId > 0) {
			UserInfo userInfo = createGatekeeperUserInfo();
			try {
				itemHome.deleteItem(itemId, false, userInfo);
			} catch (BusinessValidationException be) {
				List<String> errors = be.getValidationErrors();
				for (String err : errors) {
					System.out.println("--- " + err);
				}
				throw be;
			} catch (Throwable e) {
				throw e;
			}
		}
	}

	private long testCreateFPDUCore() throws BusinessValidationException,
			ClassNotFoundException, Throwable {
		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 300L;
		long upsBankId = 1L;
		UserInfo userInfo = createGatekeeperUserInfo();
		try {
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU4");
			itemBldr.setInputBreaker(ratingAmps);
			itemBldr.setRating(ratingKva);
			itemBldr.setOutputWiring(SystemLookup.PhaseIdClass.THREE_WYE);
			itemBldr.setUpsBank(upsBankId);

			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Long itemId = -1L;

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(retItemId > 0);
			printItemDetailsMap(retval);

			validateResults(retItemId, upsBankId, ratingKva, ratingAmps);
			validateCircuit(retItemId);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		}
		return retItemId;
	}

	/*
	 * Create FPDU but without ratings and ups bank selected. Next update the
	 * FPDU with rating values and ups bank data. Valdiate that data is correct.
	 * Validate circuit.
	 */
	@Test
	public void test1EditPDU() throws ClassNotFoundException, Throwable {
		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 300L;
		long itemId = -1L;

		UserInfo userInfo = createGatekeeperUserInfo();
		try {
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU5");

			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(retItemId > 0);
			printItemDetailsMap(retval);

			// Now edit item
			itemId = retItemId;
			retItemId = -1;
			long upsBankId = 1L;
			itemBldr.setInputBreaker(ratingAmps);
			itemBldr.setRating(ratingKva);
			// itemBldr.setOutputWiring(SystemLookup.PhaseIdClass.THREE_WYE);
			itemBldr.setUpsBank(upsBankId);
			dtoList = itemBldr.getValueIdDTOList();
			retval = itemHome.saveItem(itemId, dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(itemId == retItemId);

			validateResults(retItemId, upsBankId, ratingKva, ratingAmps);
			validateCircuit(retItemId);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				try {
					itemHome.deleteItem(itemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			} else if (itemId > 0) {
				try {
					itemHome.deleteItem(itemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			}
		}

	}

	/*
	 * Create FPDU with specified ratings and ups bank (A) selected. Validate
	 * ratings values and circuit. Then change UPS bank to another one and also
	 * change ratings. Valdiate that new ratings took into effect and old one
	 * have been removed.
	 */
	@Test
	public void test2EditPDU() throws ClassNotFoundException, Throwable {
		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 300L;
		long upsBankId = 1L;
		long itemId = -1L;
		long phase = SystemLookup.PhaseIdClass.THREE_WYE;
		UserInfo userInfo = createGatekeeperUserInfo();

		try {
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU7");
			itemBldr.setInputBreaker(ratingAmps);
			itemBldr.setRating(ratingKva);
			itemBldr.setOutputWiring(phase);
			itemBldr.setUpsBank(upsBankId);
			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(retItemId > 0);
			printItemDetailsMap(retval);

			// validateResults(retItemId, upsBankId, ratingKva, ratingAmps,
			// phase);
			// validateCircuit(retItemId);

			// Now edit item - change bank to another one and also change
			// ratings
			itemId = retItemId;
			retItemId = -1;
			ratingAmps = 250L;
			ratingKva = 200L;
			upsBankId = 2L;
			phase = SystemLookup.PhaseIdClass.THREE_DELTA;

			ItemDTOBuilder itemBldr2 = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU3");
			itemBldr2.setInputBreaker(ratingAmps);
			itemBldr2.setRating(ratingKva);
			itemBldr2.setOutputWiring(phase);
			itemBldr2.setUpsBank(upsBankId);
			dtoList = itemBldr2.getValueIdDTOList();
			retval = itemHome.saveItem(itemId, dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(itemId == retItemId);

			validateResults(retItemId, upsBankId, ratingKva, ratingAmps);
			validateCircuit(retItemId);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				try {
					itemHome.deleteItem(itemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			} else if (itemId > 0) {
				try {
					itemHome.deleteItem(itemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			}
		}

	}

	private void validateCircuit(long fpduId) {
		// MeItem fpdu = (MeItem) itemDAO.loadItem(fpduId);
		MeItem fpdu = (MeItem) itemFinderDAO.findById(fpduId).get(0);
		// MeItem tmpFpdu = (MeItem)itemFinderDAO.findById(
		// fpdu.getItemId()).get(0);
		MeItem upsB = fpdu.getUpsBankItem();
		Long upsBItemId = upsB.getItemId();
		PowerPort upsBPort = getPort(upsB,
				SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER);
		PowerPort fpduPort = getPort(fpdu,
				SystemLookup.PortSubClass.PDU_INPUT_BREAKER);

		Assert.isTrue(upsBPort != null);
		Assert.isTrue(fpduPort != null);

		Set<PowerConnection> srcPowerConenctions = fpduPort
				.getSourcePowerConnections();
		Assert.isTrue(srcPowerConenctions != null);
		Assert.isTrue(srcPowerConenctions.size() > 0
				&& srcPowerConenctions.size() == 1);

		PowerConnection connection = srcPowerConenctions.iterator().next();
		Assert.isTrue(connection != null);
		Assert.isTrue(connection.getDestPort().getPortId().longValue() == upsBPort
				.getPortId().longValue());
		Assert.isTrue(connection.getSourcePort().getPortId().longValue() == fpduPort
				.getPortId().longValue());

		// Verify that connection terminates
		Set<PowerConnection> upsBSrcConnections = upsBPort
				.getSourcePowerConnections();
		Assert.isTrue(upsBSrcConnections != null);
		Assert.isTrue(upsBSrcConnections.size() != 0);

		// it must be the first elem
		PowerConnection terminateConn = upsBSrcConnections.iterator().next();
		Assert.isTrue(terminateConn != null);
		Assert.isTrue(terminateConn.getDestPort() == null);
		Assert.isTrue(terminateConn.getSourcePort().getPortId().longValue() == upsBPort
				.getPortId().longValue());

	}

	private PowerPort getPort(MeItem item, long portSubclass) {
		Set<PowerPort> ports = item.getPowerPorts();
		itemDAO.initPowerPortsAndConnectionsProxy(item);
		PowerPort myPort = null;
		for (PowerPort port : ports) {
			Hibernate.initialize(port);
			Hibernate.initialize(port.getPhaseLookup());
			Assert.isTrue(port.getPortSubClassLookup() != null);
			long subclass = port.getPortSubClassLookup().getLkpValueCode()
					.longValue();
			if (portSubclass == subclass) {
				myPort = port;
			}
		}
		return myPort;
	}

	private void validateResults(Long fpduItemId, long upsBankId,
			long ratingKva, long ratingAmps) {
		MeItem fpdu = (MeItem) itemFinderDAO.findById(fpduItemId).get(0);
		Hibernate.initialize(fpdu.getUpsBankItem());
		Hibernate.initialize(fpdu.getPhaseLookup());
		// MeItem fpdu = (MeItem) itemFinderDAO.findById(
		// fpdu.getItemId()).get(0);
		MeItem upsB = fpdu.getUpsBankItem();
		long inputWiring = upsB.getPhaseLookup().getLkpValueCode().longValue();

		Assert.isTrue(upsBankId == upsB.getItemId());
		Assert.isTrue((long) upsB.getRatingV() == (long) fpdu.getLineVolts());
		Assert.isTrue(fpdu.getRatingKva() == ratingKva);
		Assert.isTrue(fpdu.getRatingAmps() == ratingAmps);
		Assert.isTrue(fpdu.getPhaseLookup().getLkpValueCode().longValue() == inputWiring);

		PowerPort myPort = getPort(fpdu,
				SystemLookup.PortSubClass.PDU_INPUT_BREAKER);

		Assert.isTrue(myPort != null);
		Assert.isTrue(Long.valueOf(myPort.getVoltsLookup().getLkpValue())
				.longValue() == (long) fpdu.getLineVolts());
		Assert.isTrue(myPort.getPhaseLookup().getLksId().longValue() == fpdu
				.getPhaseLookup().getLksId().longValue());
		Assert.isTrue((long) myPort.getAmpsNameplate() == fpdu.getRatingAmps());
		Assert.isTrue(myPort.getPhaseLookup().getLkpValueCode().longValue() == inputWiring);
	}

	/*
	 * Verify the FPDU cannot be created if invlaid ratings are sent as part of
	 * the DTO
	 */
	@Test
	public void testInvalidRating() throws BusinessValidationException,
			ClassNotFoundException, Throwable {

		boolean gotException = false;
		long retItemId = -1;
		UserInfo userInfo = createGatekeeperUserInfo();

		long ratingKva = 225L;
		long ratingAmps = 300000L;

		try {
			ItemDTOBuilder itemBldr = new ItemDTOBuilder(1L, 27L, 58L,
					"BRH_FPDU6");
			itemBldr.setInputBreaker(ratingAmps);
			itemBldr.setRating(ratingKva);

			itemBldr.setUpsBank(1L);

			List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

			Long itemId = -1L;

			Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			retItemId = getItemIdFromDTOMap(retval);
			Assert.isTrue(retItemId > 0);
			printItemDetailsMap(retval);
		} catch (BusinessValidationException be) {
			gotException = true;
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId == -1) {
				/*
				 * if saveItem() above throws exception because the ratings
				 * validation error, the retItemId will remain -1 and the item
				 * that is created will never be deleted. This section reads the
				 * item by name and finds out the item Id.
				 */
				List<Item> items = itemFinderDAO.findByName("BRH_FPDU6");
				if (items != null && items.size() > 0) {
					Item item = items.get(0);
					if (item != null)
						retItemId = item.getItemId();
				}
			}
			if (retItemId > 0) {
				try {
					itemHome.deleteItem(retItemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			}
		}
		Assert.isTrue(gotException == true);
	}

	/*
	 * //@Test (groups="basic") public void testMeItemDAO(){ Long fpduItemId =
	 * 622L; MeItem fpduItem = (MeItem) itemDAO.getItem(fpduItemId);
	 * System.out.println("FPDU Item: " + fpduItem.getItemName() +
	 * " Rating(kVA)=" + fpduItem.getRatingKva());
	 * System.out.println("FPDU Item: " + fpduItem.getItemName() +
	 * " InputBreaker(A)=" + fpduItem.getRatingAmps());
	 * 
	 * Long upsBankItemId = 1L; MeItem upsItem = (MeItem)
	 * itemDAO.getItem(upsBankItemId); System.out.println("UPSB Item: " +
	 * upsItem.getItemName() + " InputVoltage(Vac)=" + upsItem.getRatingV());
	 * System.out.println("UPSB Item: " + upsItem.getItemName() +
	 * " InputWiring=" + upsItem.getPhaseLookup().getLkpValue());
	 * 
	 * //test finder methods //List<Long> upsBItems = itemDAO.f Long classLksId
	 * = 21L; //UPS BANK List<Long> upsBItems =
	 * itemFinderDAO.findItemsByClass(classLksId); assertTrue(upsBItems.size() >
	 * 0);
	 * 
	 * for( Long itemId : upsBItems ){ System.out.println("   got UPSB id: " +
	 * itemId); }
	 * 
	 * classLksId = 11L; //FPDU List<Long> fpduItems =
	 * itemFinderDAO.findItemsByClass(classLksId); assertTrue(fpduItems.size() >
	 * 0);
	 * 
	 * for( Long itemId : fpduItems ){ System.out.println("   got FPDU id: " +
	 * itemId); } }
	 */

	/*
	 * VErify we are getting all UPS banks if rating is not provided
	 */
	@Test
	public void testGetAllUPSBanksDTO() {
		List<UPSBankDTO> upsBanks = itemHome.getAllUpsBanks(null);
		assertTrue(upsBanks != null);
		assertTrue(upsBanks.size() > 0);
		for (UPSBankDTO bank : upsBanks) {
			System.out.println("-- ups bank id= " + bank.getUpsBankId());
			System.out.println("   ups bank name= " + bank.getUpsBankName());
			System.out.println("   ups bank location= " + bank.getLocation());
			System.out.println("   ups bank locationId= "
					+ bank.getLocationId());
			System.out.println("   ups bank volts= " + bank.getVolts());
			System.out.println("   ups bank capacity= " + bank.getCapacity());
			System.out.println("   ups bank wiring= "
					+ bank.getOutputWiringDesc());
		}
	}

	/*
	 * For valid ratings, verify that we are getting at least one ups bank info
	 */
	@Test
	public void testGetAllUPSBanksDTOForValidRating() {
		List<UPSBankDTO> upsBanks = itemHome.getAllUpsBanks(417L);
		assertTrue(upsBanks != null);
		for (UPSBankDTO bank : upsBanks) {
			System.out.println("-- ups bank id= " + bank.getUpsBankId());
			System.out.println("   ups bank name= " + bank.getUpsBankName());
			System.out.println("   ups bank location= " + bank.getLocation());
			System.out.println("   ups bank locationId= "
					+ bank.getLocationId());
			System.out.println("   ups bank volts= " + bank.getVolts());
			System.out.println("   ups bank capacity= " + bank.getCapacity());
			System.out.println("   ups bank wiring= "
					+ bank.getOutputWiringDesc());
			System.out.println("   ups bank wiring LkpValuecode= "
					+ bank.getOutputWiringLkpValueCode());
		}
	}

	/*
	 * Verify that if rating is too high there is no UPS bank to support it
	 */
	@Test
	public void testGetAllUPSBanksDTOForTooLargeRating() {
		List<UPSBankDTO> upsBanks = itemHome.getAllUpsBanks(5000L);

		assertTrue(upsBanks.size() == 0);
		for (UPSBankDTO bank : upsBanks) {
			System.out.println("-- ups bank id= " + bank.getUpsBankId());
			System.out.println("   ups bank name= " + bank.getUpsBankName());
			System.out.println("   ups bank location= " + bank.getLocation());
			System.out.println("   ups bank locationId= "
					+ bank.getLocationId());
			System.out.println("   ups bank volts= " + bank.getVolts());
			System.out.println("   ups bank capacity= " + bank.getCapacity());
			System.out.println("   ups bank wiring= "
					+ bank.getOutputWiringDesc());
			System.out.println("   ups bank wiring LkpValuecode= "
					+ bank.getOutputWiringLkpValueCode());
		}
	}

	// ------------------------------------------------------------------------
	// -------- FPDU connecting to another FPDU related tests below ----------
	// ------------------------------------------------------------------------

	/**
	 * Create FPDU with just input KVA and ampsRrating and to not specify source
	 * port (FPDU or UPS)
	 * 
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	@Test
	public void testCreateFPDUWithNoConnection()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {
		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 10L;
		long breakerPortId = -1; // -1, This test assumes that no source breaker
									// port (fpdu) connection specified
		String itemName = "UT_FPDU_NO_CONNECTION";
		UserInfo userInfo = createGatekeeperUserInfo();
		try {
			long itemId = -1; // creates new fpdu item
			retItemId = createNewFPDUAndConnectToBP(itemId, breakerPortId,
					ratingKva, ratingAmps, itemName, 0 /* JB */);
			Assert.isTrue(retItemId > 0);
			MeItem newItem = (MeItem) itemDAO.loadItem(retItemId);
			Assert.notNull(newItem);
			validateFPDUToFPDUConnectionResults(retItemId, breakerPortId,
					ratingKva, ratingAmps);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				itemHome.deleteItem(retItemId, false, userInfo);
			}
		}
	}

	/**
	 * create FPDU to FPDU connection. Validate ratings and circuit
	 * 
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */

	@Test
	public void testCreateFPDUToFPDUConnection()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 10L;
		UserInfo userInfo = createGatekeeperUserInfo();
		String itemName = "UT_fpdu_To_Fpdu_WithConnection1";
		try {
			BreakerPortUPS breakerPortUPS = createFpduItemPanelsBreakerAndReturnBreakerPortId();

			long itemId = -1; // creates new fpdu item
			retItemId = createNewFPDUAndConnectToBP(itemId, breakerPortUPS.breakerPortId,
					ratingKva, ratingAmps, itemName, breakerPortUPS.upsBankId);

			validateFPDUToFPDUConnectionResults(retItemId, breakerPortUPS.breakerPortId,
					ratingKva, ratingAmps);
			validateFPDUToFPDUCircuit(retItemId);
		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId == -1) {
				/*
				 * if saveItem() above throws exception because the ratings
				 * validation error, the retItemId will remain -1 and the item
				 * that is created will never be deleted. This section reads the
				 * item by name and finds out the item Id.
				 */
				session.flush();

				List<Item> items = itemFinderDAO.findByName(itemName);
				if (items != null && items.size() > 0) {
					Item item = items.get(0);
					if (item != null)
						retItemId = item.getItemId();
				}
			}
			if (retItemId > 0) {
				itemHome.deleteItem(retItemId, false, userInfo);
			}
		}
	}

	/**
	 * This test verifies port connection moving from one source FPDU to
	 * different Source FPDU
	 * 
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	@Test
	public void testConnectFPDUToDifferentFPDU()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		long retItemId1 = -1;
		long retItemId2 = -1;
		long ratingKva = 225;
		long ratingAmps = 10;
		String itemName1 = "UT_FPDU_TO_DIFFERENT_FPDU1";
		String itemName2 = "UT_FPDU_TO_DIFFERENT_FPDU2";
		UserInfo userInfo = createGatekeeperUserInfo();
		try {
			// set ups bank for this FPDU
			MeItem upsItem = createNewTestUPS("INT_TEST_UPS");

			Set<Item> panels = getPanels(upsItem);
			PowerPort breakerPort = null;

			for (Item panel : panels) {
				if (panel.getClassLookup().getLkpValueCode()
						.equals(SystemLookup.Class.FLOOR_PDU)
						&& panel.getSubclassLookup().getLkpValueCode() != null) {

					Set<PowerPort> ports = panel.getPowerPorts();

					if (ports.size() > 0) {
						for (PowerPort p : ports) {
							if (p.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
								breakerPort = p;
								break;
							}
						}
					}
					if (breakerPort != null)
						break;
				}

			}

			assertNotNull(breakerPort);

			long itemId = -1; // creates new FPDU item
			retItemId1 = createNewFPDUAndConnectToBP(itemId,
					breakerPort.getPortId(), ratingKva, ratingAmps, itemName1,
					upsItem.getItemId());
			// verify the connection
			validateFPDUToFPDUConnectionResults(retItemId1,
					breakerPort.getPortId(), ratingKva, ratingAmps);
			validateFPDUToFPDUCircuit(retItemId1);
			int i = 2;
			// get different breaker port
			for (Item panel : panels) {
				if (panel.getClassLookup().getLkpValueCode()
						.equals(SystemLookup.Class.FLOOR_PDU)
						&& panel.getSubclassLookup().getLkpValueCode() != null) {

					Set<PowerPort> ports = panel.getPowerPorts();

					if (ports.size() > 0) {
						for (PowerPort p : ports) {
							if (p.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER
									&& i-- == 0) {
								breakerPort = p;
								break;
							}
						}
					}
					if (breakerPort != null)
						break;
				}

			}

			// move to different FPDU source.
			itemId = retItemId1;
			ratingKva = 120;
			ratingAmps = 50;

			retItemId2 = createNewFPDUAndConnectToBP(itemId,
					breakerPort.getPortId(), ratingKva, ratingAmps, itemName2,
					0 /* JB */);

			// verify the connection
			validateFPDUToFPDUConnectionResults(retItemId2,
					breakerPort.getPortId(), ratingKva, ratingAmps);
			validateFPDUToFPDUCircuit(retItemId2);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId1 > 0) {
				itemHome.deleteItem(retItemId1, false, userInfo);
			}
			if (retItemId2 > 0) {
				itemHome.deleteItem(retItemId2, false, userInfo);
			}
		}
	}

	/**
	 * Verify the FPDU cannot be created if invalid breaker port rating is sent
	 * in DTO
	 * 
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	@Test
	public void testFPDUToFPDUConnectionWithInvalidRating()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		boolean gotException = false;
		long retItemId = -1;
		UserInfo userInfo = createGatekeeperUserInfo();
		String itemName = "UT_CONNECT_TO_FPDU_INVALID_RATING";

		long ratingKva = 225L;
		long ratingAmps = 300000L; // Invalid Rating specified in the request

		try {
			BreakerPortUPS breakerPortUPS = createFpduItemPanelsBreakerAndReturnBreakerPortId();

			long itemId = -1; // creates new FPDU item
			retItemId = createNewFPDUAndConnectToBP(itemId, breakerPortUPS.breakerPortId,
					ratingKva, ratingAmps, itemName, breakerPortUPS.upsBankId);
			MeItem fpdu = (MeItem) itemDAO.loadItem(retItemId);
			PowerPort fpduPort = getPort(fpdu,
					SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
			Assert.isTrue(fpduPort != null);

			// This verifies that the connection is NOT established between
			// fpdus and entry is not
			// found in dct_connections table
			PowerPort destBrkrPort = powerConnDAO.getDestinationPort(fpduPort
					.getPortId());
			Assert.isTrue(destBrkrPort == null);

			Set<PowerConnection> srcPowerConenctions = fpduPort
					.getSourcePowerConnections();
			Assert.isTrue(srcPowerConenctions == null);
		} catch (BusinessValidationException be) {
			gotException = true;
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId == -1) {
				/*
				 * if saveItem() above throws exception because the ratings
				 * validation error, the retItemId will remain -1 and the item
				 * that is created will never be deleted. This section reads the
				 * item by name and finds out the item Id.
				 */
				session.flush();

				List<Item> items = itemFinderDAO.findByName(itemName);
				if (items != null && items.size() > 0) {
					Item item = items.get(0);
					if (item != null)
						retItemId = item.getItemId();
				}
			}
			if (retItemId > 0) {
				try {
					itemHome.deleteItem(retItemId, false, userInfo);
				} catch (BusinessValidationException be) {
					List<String> errors = be.getValidationErrors();
					for (String err : errors) {
						System.out.println("--- " + err);
					}
					throw be;
				}
			}
		}
		Assert.isTrue(gotException == true);
	}

	/**
	 * create FPDU to FPDU connection AND move to archive state. Server must
	 * report error that archiving is not allowed because FPDU has connection.
	 * 
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */

	@Test
	public void testCreateFPDUToFPDUConnectionAndArchive()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		long retItemId = -1;
		long ratingKva = 225L;
		long ratingAmps = 10L;
		long breakerPortId = -1;
		UserInfo userInfo = createGatekeeperUserInfo();
		String itemName = "UT_FPDU_TO_FPDU_AND_ARCHIVE";
		try {
			BreakerPortUPS breakerPortUPS = createFpduItemPanelsBreakerAndReturnBreakerPortId();

			long itemId = -1; // creates new fpdu item
			retItemId = createNewFPDUAndConnectToBP(itemId, breakerPortUPS.breakerPortId,
					ratingKva, ratingAmps, itemName, breakerPortUPS.upsBankId);

			validateFPDUToFPDUConnectionResults(retItemId, breakerPortId,
					ratingKva, ratingAmps);
			validateFPDUToFPDUCircuit(retItemId);

			// put item to archive state
			List<ValueIdDTO> dtoList = getItemVIdDTO(itemName, ratingAmps,
					ratingKva, breakerPortId, SystemLookup.ItemStatus.ARCHIVED);
			Map<String, UiComponentDTO> retval = itemHome.saveItem(retItemId,
					dtoList, userInfo);
			Assert.isTrue(retval != null);

			// verify the circuit is closed.
			validateFPDUToFPDUConnectionResults(retItemId, breakerPortId,
					ratingKva, ratingAmps);
			validateArchiveStateFPDUCircuit(retItemId);

			// put Item to planned state or else test will not be able to delete
			// the item.
			dtoList = getItemVIdDTO(itemName, ratingAmps, ratingKva,
					breakerPortId, SystemLookup.ItemStatus.PLANNED);
			retval = itemHome.saveItem(retItemId, dtoList, userInfo);
			Assert.isTrue(retval != null);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				itemHome.deleteItem(retItemId, false, userInfo);
			}
		}
	}
	
	private PowerPort getBreakerPortIdForConnection (MeItem fpduItem) {
		// select breaker port for sourcing power to another PDU
		Set<Item> panels = fpduItem.getChildItems();
		PowerPort breakerPort = null;
		for (Item panel : panels) {
			if (panel.getClassLookup().getLkpValueCode()
					.equals(SystemLookup.Class.FLOOR_PDU)
					&& panel.getSubclassLookup().getLkpValueCode() != null) {

				Set<PowerPort> ports = panel.getPowerPorts();

				if (ports.size() > 0) {
					for (PowerPort p : ports) {
						if (p.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
							breakerPort = p;
							break;
						}
					}
				}
				if (breakerPort != null)
					break;
			}

		}
		assertNotNull(breakerPort);

		return breakerPort;
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testArchiveFPDUSourcingOtherFPDU()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {
		long retItemId = -1;
		long ratingKva = 3L;
		long ratingAmps = 10L;
		UserInfo userInfo = createGatekeeperUserInfo();

		try {
			// Create FPDU with panel and breakers
			MeItem fpduItem = createFloorPDU(true, true, false, false);
			
			MeItem upsItem = fpduItem.getUpsBankItem();

			long breakerPortId = getBreakerPortIdForConnection (fpduItem).getPortId();

			long itemId = -1;
			retItemId = createNewFPDUAndConnectToBP(itemId,
					breakerPortId, ratingKva, ratingAmps,
					"INT_TEST_this_fpdu_is_ConnectedToAnotherFpdu", upsItem.getItemId());
			
			validateFPDUToFPDUConnectionResults(retItemId,
							breakerPortId, ratingKva, ratingAmps);
			
			validateFPDUToFPDUCircuit(retItemId);
			
			session.flush();
			session.clear();

			// Move the source FPDU to Archive  
			Map<String, UiComponentDTO> rval = itemHome.getItemDetails(
					fpduItem.getItemId(), userInfo);
			
			/*unitTestItemDAO.updateValue("tiName", rval,
					"INT_TEST_FPDU_WITH_2PANELS", false);*/
			updateValue("tiName", rval,
					"INT_TEST_FPDU_WITH_2PANELS", false);

			UiComponentDTO uiDto = rval.get("cmbStatus");
			uiDto.getUiValueIdField().setValueId(
					SystemLookup.ItemStatus.ARCHIVED);
			uiDto.getUiValueIdField().setValue("Item in Archive");

			uiDto = rval.get("tiClass");
			uiDto.getUiValueIdField().setValue("Floor PDU");
			uiDto.getUiValueIdField().setValueId("Floor PDU");

			List<ValueIdDTO> vidDto = getItemUpdateValueIdDTOList(rval);
			
			rval = itemHome.saveItem(fpduItem.getItemId(), vidDto, userInfo);

			// SHOULD NOT COME HERE //
			assertTrue(false);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();

			// assertTrue(errors.size() == 1);
			String msg = errors.get(0);
			System.out.println("Received Error Msg = " + msg);
			String expectedMsg = "The following 1 Item(s) have one or more connected ports:     \n\tINT_TEST_THIS_FPDU_IS_CONNECTEDTOANOTHERFPDU";
			assertTrue(msg.contains(expectedMsg));
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				session.clear();
				itemHome.deleteItem(retItemId, false, userInfo);
			}
		}
	}

	@Test
	public void testArchiveFPDUWithPanelsAndBreakersNotPartOfCircuit()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {
		UserInfo userInfo = createGatekeeperUserInfo();

		try {
			// Create FPDU with panel and breakers
			MeItem fpduItem = createFloorPDU(true, true, false, false);

			session.clear();

			Map<String, UiComponentDTO> rval = itemHome.getItemDetails(
					fpduItem.getItemId(), userInfo);
			/*unitTestItemDAO.updateValue("tiName", rval,
					"INT_TEST_FPDU_WITH_2PANELS", false);*/
			updateValue("tiName", rval,
					"INT_TEST_FPDU_WITH_2PANELS", false);
			

			UiComponentDTO uiDto = rval.get("cmbStatus");
			uiDto.getUiValueIdField().setValueId(
					SystemLookup.ItemStatus.ARCHIVED);
			uiDto.getUiValueIdField().setValue("Item in Archive");

			uiDto = rval.get("tiClass");
			uiDto.getUiValueIdField().setValue("Floor PDU");
			uiDto.getUiValueIdField().setValueId("Floor PDU");

			List<ValueIdDTO> vidDto = getItemUpdateValueIdDTOList(rval);
			session.clear();
			rval = itemHome.saveItem(fpduItem.getItemId(), vidDto, userInfo);
			session.flush();
			session.clear();
			assertTrue(rval != null);

			assertTrue(true);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			for (String err : errors) {
				System.out.println("--- " + err);
			}
			assertTrue(false);
		} 
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testArchivePanelSourcingOtherFPDU()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {
		long retItemId = -1;
		long ratingKva = 3L;
		long ratingAmps = 10L;
		UserInfo userInfo = createGatekeeperUserInfo();

		try {
			// Create FPDU with panel and breakers
			MeItem fpduItem = createFloorPDU(true, true, false, false);
			
			MeItem upsItem = fpduItem.getUpsBankItem();

			PowerPort pp = getBreakerPortIdForConnection (fpduItem);
			long breakerPortId = pp.getPortId();
			long panelId = pp.getItem().getItemId();
			pp.getItem().setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED));
			session.update(pp.getItem());

			long itemId = -1;
			retItemId = createNewFPDUAndConnectToBP(itemId,
					breakerPortId, ratingKva, ratingAmps,
					"INT_TEST_this_fpdu_is_ConnectedToAnotherFpdu", upsItem.getItemId());
			
			validateFPDUToFPDUConnectionResults(retItemId,
							breakerPortId, ratingKva, ratingAmps);
			
			validateFPDUToFPDUCircuit(retItemId);
			
			session.flush();
			session.clear();

			// Move the source FPDU to Archive  
			Map<String, UiComponentDTO> rval = itemHome.getItemDetails(
					panelId, userInfo);
			
			/*unitTestItemDAO.updateValue("tiName", rval,
					"INT_TEST_PS2", false);*/
			updateValue("tiName", rval,
					"INT_TEST_PS2", false);

			UiComponentDTO uiDto = rval.get("cmbStatus");
			uiDto.getUiValueIdField().setValueId(
					SystemLookup.ItemStatus.ARCHIVED);
			uiDto.getUiValueIdField().setValue("Item in Archive");

			uiDto = rval.get("tiClass");
			uiDto.getUiValueIdField().setValue("Floor PDU / Local");
			uiDto.getUiValueIdField().setValueId("Floor PDU / Local");

			List<ValueIdDTO> vidDto = getItemUpdateValueIdDTOList(rval);
			
			rval = itemHome.saveItem(panelId, vidDto, userInfo);

			// SHOULD NOT COME HERE //
			assertTrue(false);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();

			// assertTrue(errors.size() == 1);
			String msg = errors.get(0);
			System.out.println("Received Error Msg = " + msg);
			String expectedMsg = "The following 1 Item(s) have one or more connected ports:     \n\tINT_TEST_THIS_FPDU_IS_CONNECTEDTOANOTHERFPDU";
			assertTrue(msg.contains(expectedMsg));
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} finally {
			if (retItemId > 0) {
				session.clear();
				itemHome.deleteItem(retItemId, false, userInfo);
			}
		}
	}

	@Test
	public void testArchivePanelWithBreakersNotpartofCircuitYet()
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		long retItemId = -1;
		long ratingKva = 3L;
		long ratingAmps = 10L;
		UserInfo userInfo = createGatekeeperUserInfo();

		try {
			// Create FPDU with panel and breakers
			MeItem fpduItem = createFloorPDU(true, true, false, false);
			
			MeItem upsItem = fpduItem.getUpsBankItem();

			PowerPort pp = getBreakerPortIdForConnection (fpduItem);
			long breakerPortId = pp.getPortId();
			long panelId = pp.getItem().getItemId();

			session.flush();
			session.clear();

			// Move the source FPDU to Archive  
			Map<String, UiComponentDTO> rval1 = itemHome.getItemDetails(
					panelId, userInfo);
			
			/*unitTestItemDAO.updateValue("tiName", rval1,
					"INT_TEST_PS2", false);*/
			updateValue("tiName", rval1,
					"INT_TEST_PS2", false);

			UiComponentDTO uiDto = rval1.get("cmbStatus");
			uiDto.getUiValueIdField().setValueId(
					SystemLookup.ItemStatus.ARCHIVED);
			uiDto.getUiValueIdField().setValue("Item in Archive");

			uiDto = rval1.get("tiClass");
			uiDto.getUiValueIdField().setValue("Floor PDU / Local");
			uiDto.getUiValueIdField().setValueId("Floor PDU / Local");

			List<ValueIdDTO> vidDto = getItemUpdateValueIdDTOList(rval1);
			
			Map<String, UiComponentDTO>  rval2 = itemHome.saveItem(panelId, vidDto, userInfo);
			
			assertTrue (rval2 != null);
			
			// Change item status back to planned so you can delete the item.
			/*unitTestItemDAO.updateValue("tiName", rval1, "INT_TEST_PS2", false);*/
			updateValue("tiName", rval1, "INT_TEST_PS2", false);

			uiDto = rval1.get("cmbStatus");
			uiDto.getUiValueIdField().setValueId(
					SystemLookup.ItemStatus.PLANNED);
			uiDto.getUiValueIdField().setValue("Item in Archive");

			uiDto = rval1.get("tiClass");
			uiDto.getUiValueIdField().setValue("Floor PDU / Local");
			uiDto.getUiValueIdField().setValueId("Floor PDU / Local");

			vidDto = getItemUpdateValueIdDTOList(rval1);
			rval2 = itemHome.saveItem(panelId, vidDto,
					userInfo);
			assertTrue(rval2 != null);
			assertTrue(true);

		} catch (BusinessValidationException be) {
			List<String> errors = be.getValidationErrors();
			String msg = errors.get(0);
			for (String err : errors) {
				System.out.println("--- " + err);
			}
		} 
	}

	private void validateArchiveStateFPDUCircuit(long fpduId)
			throws DataAccessException {
		MeItem fpdu = (MeItem) itemDAO.loadItem(fpduId);
		PowerPort fpduPort = getPort(fpdu,
				SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
		Assert.isTrue(fpduPort != null);

		// This verifies that the connection is established between fpdus and
		// entry can be
		// found in dct_connections table
		PowerPort destBrkrPort = powerConnDAO.getDestinationPort(fpduPort
				.getPortId());
		Assert.isTrue(destBrkrPort == null);

		Set<PowerConnection> srcPowerConenctions = fpduPort
				.getSourcePowerConnections();
		Assert.isTrue(srcPowerConenctions != null);
		Assert.isTrue(srcPowerConenctions.size() == 0);
	}

	private void validateFPDUToFPDUCircuit(long fpduId)
			throws DataAccessException {
		MeItem fpdu = (MeItem) itemDAO.loadItem(fpduId);
		PowerPort fpduPort = getPort(fpdu,
				SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
		Assert.isTrue(fpduPort != null);

		// This verifies that the connection is established between fpdus and
		// entry can be
		// found in dct_connections table
		PowerPort destBrkrPort = powerConnDAO.getDestinationPort(fpduPort
				.getPortId());
		Assert.isTrue(fpduPort != null);

		Set<PowerConnection> srcPowerConenctions = fpduPort
				.getSourcePowerConnections();
		Assert.isTrue(srcPowerConenctions != null);
		Assert.isTrue(srcPowerConenctions.size() > 0
				&& srcPowerConenctions.size() == 1);

		PowerConnection connection = srcPowerConenctions.iterator().next();
		Assert.isTrue(connection != null);
		Assert.isTrue(connection.getSourcePort().getPortId().longValue() == fpduPort
				.getPortId().longValue());
		Assert.isTrue(connection.getDestPort().getPortId().longValue() == destBrkrPort
				.getPortId().longValue());
	}

	private void validateFPDUToFPDUConnectionResults(Long fpduItemId,
			long breakerPortId, long ratingKva, long ratingAmps)
			throws DataAccessException {
		MeItem fpdu = (MeItem) itemDAO.loadItem(fpduItemId);
		Assert.isTrue(fpdu.getRatingKva() == ratingKva);
		Assert.isTrue(fpdu.getRatingAmps() == ratingAmps);
		if (breakerPortId != -1) {
			PowerPort port = powerPortDAO.loadPort(breakerPortId);
			LksData fpduPhaseLookup = fpdu.getPhaseLookup();

			PowerPort fpduPort = getPort(fpdu,
					SystemLookup.PortSubClass.PDU_INPUT_BREAKER);
			Assert.isTrue(fpduPort != null);

			LksData fpduPortPhaseLookup = fpduPort.getPhaseLookup();

			if (fpduPhaseLookup != null && fpduPortPhaseLookup != null) {
				Assert.isTrue(fpdu.getPhaseLookup().getLkpValueCode()
						.longValue() == port.getPhaseLookup().getLkpValueCode()
						.longValue());
				Assert.isTrue(fpduPort.getPhaseLookup().getLksId().longValue() == fpdu
						.getPhaseLookup().getLksId().longValue());
			}
			if (fpduPort.getVoltsLookup() != null) {
				Assert.isTrue(Long.valueOf(
						fpduPort.getVoltsLookup().getLkpValue()).longValue() == (long) fpdu
						.getLineVolts());
			}
			Assert.isTrue((long) fpduPort.getAmpsNameplate() == fpdu
					.getRatingAmps());
		}
	}

	/**
	 * This function create new FPDU with specified ratings and connect to
	 * breakerPort
	 * 
	 * @param breakerportId
	 * @param ratingkva
	 * @param ratingAmps
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */

	private long createNewFPDUAndConnectToBP(long itemId, long breakerPortId,
			long ratingKva, long ratingAmps, String itemName, long upsBankId)
			throws BusinessValidationException, ClassNotFoundException,
			Throwable {

		long retItemId = -1;
		UserInfo userInfo = createGatekeeperUserInfo();

		ItemDTOBuilder itemBldr = new ItemDTOBuilder(cmbLocationId, cmbMakeId,
				cmbModelId, itemName);
		itemBldr.setInputBreaker(ratingAmps);
		itemBldr.setRating(ratingKva);
		if (breakerPortId != -1) { /*
									 * -1 means test did not specify breaker
									 * port
									 */
			itemBldr.setBreakerport(breakerPortId);
		}
		itemBldr.setUpsBank(upsBankId);

		List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();

		Map<String, UiComponentDTO> retval = itemHome.saveItem(itemId, dtoList,
				userInfo);
		Assert.isTrue(retval != null);

		retItemId = getItemIdFromDTOMap(retval);
		Assert.isTrue(retItemId > 0);
		printItemDetailsMap(retval);

		return retItemId;
	}

	private List<ValueIdDTO> getItemVIdDTO(String itemName, long ratingAmps,
			long ratingKva, long breakerPortId, long itemStatus) {
		// edit item to put into archive state

		ItemDTOBuilder itemBldr = new ItemDTOBuilder(cmbLocationId, cmbMakeId,
				cmbModelId, "UT_FPDU1");
		itemBldr.setInputBreaker(ratingAmps);
		itemBldr.setRating(ratingKva);
		if (breakerPortId > -1)
			itemBldr.setBreakerport(breakerPortId);
		itemBldr.setItemStatus(itemStatus);

		List<ValueIdDTO> dtoList = itemBldr.getValueIdDTOList();
		return dtoList;
	}

	public class BreakerPortUPS {
		public long breakerPortId;
		public long upsBankId;
	}

	private BreakerPortUPS createFpduItemPanelsBreakerAndReturnBreakerPortId()
			throws Throwable {

		PowerPort breakerPort = null;
		// set ups bank for this FPDU
		MeItem upsItem = createNewTestUPS("INT_TEST_UPS");

		Set<Item> panels = getPanels(upsItem);

		for (Item panel : panels) {
			if (panel.getClassLookup().getLkpValueCode()
					.equals(SystemLookup.Class.FLOOR_PDU)
					&& panel.getSubclassLookup().getLkpValueCode() != null) {

				Set<PowerPort> ports = panel.getPowerPorts();

				if (ports.size() > 0) {
					for (PowerPort p : ports) {
						if (p.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) {
							breakerPort = p;
							break;
						}
					}
				}
				if (breakerPort != null)
					break;
			}

		}
		assertNotNull(breakerPort);
		BreakerPortUPS breakerPortUPS = new BreakerPortUPS();
		breakerPortUPS.breakerPortId = breakerPort.getPortId();
		breakerPortUPS.upsBankId = upsItem.getItemId();

		return breakerPortUPS;
	}

	private Set<Item> getPanels(MeItem upsItem) throws Throwable {
		// this call also creates breakerports inside panel
		MeItem fpduItem = createNewTestFloorPDUWithPanels("INT_TEST_FPDU_WITH_2PANELS");

		fpduItem.setUpsBankItem(upsItem);

		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		Set<Item> panels = fpduItem.getChildItems();

		for (Item panel : panels) {
			createBreakerPortAndConnections(panel);
		}
		return panels;
	}
	
	private Map<String, UiComponentDTO> updateValue(String label,
			Map<String, UiComponentDTO> origDTO, Object value, boolean changeId) {
		if (changeId){
			origDTO.get(label).getUiValueIdField().setValueId(value);
		} else {
			origDTO.get(label).getUiValueIdField().setValueId(null);
			origDTO.get(label).getUiValueIdField().setValue(value);
		}
		return origDTO;
	}

	private List<ValueIdDTO> getItemUpdateValueIdDTOList(Map<String,UiComponentDTO> itemDTO) throws Throwable {
		List<ValueIdDTO> list = new ArrayList<ValueIdDTO>();
		
		for (Map.Entry<String, UiComponentDTO> entry: itemDTO.entrySet()){
			String label = entry.getKey();
			UiComponentDTO value = entry.getValue();
			Object id = value.getUiValueIdField().getValueId();
			Object val = value.getUiValueIdField().getValue();
			
			ValueIdDTO vDto = new ValueIdDTO();
			vDto.setLabel(label);
			
			if (id != null){
				vDto.setData(id);
			} else {
				vDto.setData(val);
			}
			
			list.add(vDto);
		}
		
		return list;
	}


}
