package com.raritan.tdz.unit.item;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;
import com.raritan.tdz.unit.tests.UserMock;
import com.raritan.tdz.item.request.ItemRequestDAO;

public class ItemMockImpl implements ItemMock  {
	final List<String> itemsListConnection = new ArrayList<String>();
	
	@Autowired
	protected UnitTestDatabaseIdGenerator unitTestIdGenerator;
	 
	@Autowired
	protected ItemDAO itemDAO;
		
	@Autowired
	protected Mockery jmockContext;
	 
	@Autowired
	protected SystemLookupInitUnitTest systemLookupInitTest;

	@Autowired
	protected ItemExpectations itemExpectations;
	
	class ItemType {
		public static final int CABINET_ITEM = 1;
		public static final int IT_ITEM = 2;
		public static final int ME_ITEM = 3;
	}
	
	@Override
	public Mockery getJmockContext(){
		return jmockContext;
	}
	
	@Override
	public void setJmockContext(Mockery jmockContext){
		this.jmockContext = jmockContext;
	}
	
	private Item createItem (long itemId, String itemName, LksData itemClass, LksData itemSubClass, 
			LksData itemStatus, ModelDetails model, ItemServiceDetails itemServiceDetails, int itemType) {
		Item item = null;
		if ( itemType == ItemType.CABINET_ITEM) { 
			item = new CabinetItem();
		} else if ( itemType == ItemType.IT_ITEM) {
			item = new ItItem();
		} else if ( itemType == ItemType.ME_ITEM) {
			item = new MeItem();
		} else {
			item = new Item();
		}
		item.setItemId(itemId);
		item.setItemName( itemName );
		item.setClassLookup(itemClass);
		item.setSubclassLookup(itemSubClass);
		item.setStatusLookup(itemStatus);
		item.setModel(model);
		item.setItemServiceDetails(itemServiceDetails);

		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createItem(java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, long, long, int, java.lang.Long)
	 */
	@Override
	public Item createItem(String itemName, Integer index, String mounting, String formfactor, 
			long iClassLksValueCode,  long iSubClassLksValueCode, int itemType, Long statusValueCode) {
		final Long itemId = unitTestIdGenerator.nextId();
		final Long modelId = unitTestIdGenerator.nextId();

		//default item status is PLANNED
		statusValueCode = statusValueCode == null ? SystemLookup.ItemStatus.PLANNED : statusValueCode;
		
		// Create lookup entities
		final LksData itemClass = systemLookupInitTest.getLks(iClassLksValueCode);
		final LksData itemSubClass = systemLookupInitTest.getLks(iSubClassLksValueCode);
		final LksData itemStatus = systemLookupInitTest.getLks(statusValueCode);
		
		// create item service details 
		final ItemServiceDetails itemServiceDetails = getNewItemServiceDetails (UserInfo.UserAccessLevel.ADMIN);
		
		ModelDetails model = null;
		// Create model
		if (mounting != null && formfactor != null) {
			model = createNewTestModel (modelId, mounting, formfactor);
		}
		
		// Create an item and set its properties
		if(index != null)  itemName +=  "-0" + index.toString();

		final Item item = createItem(itemId, itemName, itemClass, itemSubClass, itemStatus, model, itemServiceDetails, itemType);

		// This section creates mock data and sets up expectation 
		addExpectations(item, jmockContext);
		
		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#getValidatorTargetMap(com.raritan.tdz.domain.Item, com.raritan.tdz.domain.UserInfo, java.lang.String)
	 */
	@Override
	public Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo, String msg) {
		
		Map<String,Object> targetMap = new HashMap<String, Object>();
		
		if (item != null) targetMap.put(item.getClass().getName(), item);
		if (userInfo != null) targetMap.put(userInfo.getClass().getName(), userInfo);
		if (msg != null) targetMap.put(msg.getClass().getName(), msg);
		return targetMap;
	}
	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#getNewItemServiceDetails(com.raritan.tdz.domain.UserInfo.UserAccessLevel)
	 */
	@Override
	public ItemServiceDetails getNewItemServiceDetails (UserInfo.UserAccessLevel userAccessLevel) {
		final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		itemServiceDetails.setItemAdminUser(UserMock.getUser(userAccessLevel));
		return itemServiceDetails;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNewTestModel(java.lang.Long, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelDetails createNewTestModel (Long id, String mounting, String formFactor) {
		ModelDetails model = new ModelDetails();
		model.setModelDetailId(id);
		model.setMounting(mounting);
		model.setFormFactor(formFactor);
		return model;
	}
	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createCabinetItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createCabinetItem(Long statusValueCode, Integer index) {
		String devName = "CabinetItem";
		String mounting = "Free-Standing";
		String formFactor = "4-Post Enclosure";
		Long itemClass = SystemLookup.Class.CABINET;
		Long itemSubClass = 0L; // N/A
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass,ItemType.CABINET_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createCabinetContainerItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createCabinetContainerItem(Long statusValueCode, Integer index) {
		String devName = "CabinetItem";
		String mounting = "Free-Standing";
		String formFactor = "4-Post Enclosure";
		Long itemClass = SystemLookup.Class.CABINET;
		Long itemSubClass = SystemLookup.SubClass.CONTAINER;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.CABINET_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRackableStandardDeviceItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRackableStandardDeviceItem(Long statusValueCode, Integer index) {
		String devName = "RackableStandard";
		String mounting = "Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.RACKABLE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNonRackableStandardDevice(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNonRackableStandardDevice(Long statusValueCode, Integer index) {
		String devName = "NonRackableItem";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.RACKABLE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createDeviceRackableChassis(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createDeviceRackableChassis(Long statusValueCode, Integer index) {
		String devName = "DeviceChassis";
		String mounting = "Network";
		String formFactor = "Chassis";
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.BLADE_CHASSIS;

		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNetworkRackableChassis(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNetworkRackableChassis(Long statusValueCode, Integer index) {
		String devName = "NetworkChassis";
		String mounting = "Network";
		String formFactor = "Chassis";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.CHASSIS;

		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createDeviceBlades(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createDeviceBlades(Long statusValueCode, Integer index) {
		String devName = "DeviceBlade";
		String mounting = "Blade";
		String formFactor = "Full";
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.BLADE_SERVER;
		
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNetworkBlades(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNetworkBlades(Long statusValueCode, Integer index) {
		String devName = "NetworkBlade";
		String mounting = "BBlade";
		String formFactor = "Full";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.BLADE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createVM(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createVM(Long statusValueCode, Integer index) {
		String devName = "VM";
		String mounting = null;
		String formFactor = null;
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.VIRTUAL_MACHINE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createFreestandingRackableStandardDeviceItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createFreestandingRackableStandardDeviceItem(Long statusValueCode, Integer index) {
		String devName = "FreestandingRackableStandardDevice";
		String mounting = "Free-Standing";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DEVICE;
		Long itemSubClass = SystemLookup.SubClass.RACKABLE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createFreestandingNetworkStackItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createFreestandingNetworkStackItem(Long statusValueCode, Integer index) {
		String devName = "FreestandingNetworkStack";
		String mounting = "Free-Standing";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.NETWORK_STACK;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRackableFixedNetworkStackItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRackableFixedNetworkStackItem(Long statusValueCode, Integer index) {
		String devName = "RackableFixedNetworkStack";
		String mounting = "Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.NETWORK_STACK;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNonRackableFixedNetworkStackItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNonRackableFixedNetworkStackItem(Long statusValueCode, Integer index) {
		String devName = "ZeroUFixedNetworkStack";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.NETWORK_STACK;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createZeroUFixedNetworkStackItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createZeroUFixedNetworkStackItem(Long statusValueCode, Integer index) {
		String devName = "RackableStandard";
		String mounting = "ZeroU";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.NETWORK;
		Long itemSubClass = SystemLookup.SubClass.NETWORK_STACK;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRackableFixedDataPanelItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRackableFixedDataPanelItem(Long statusValueCode, Integer index) {
		String devName = "RackableFixedDataPanel";
		String mounting = "Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DATA_PANEL;
		Long itemSubClass = 0L; //NA
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNonRackableFixedDataPanelItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNonRackableFixedDataPanelItem(Long statusValueCode, Integer index) {
		String devName = "NonRackable";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DATA_PANEL;
		Long itemSubClass = 0L; //NA
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createZeroUDataPanelItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createZeroUDataPanelItem(Long statusValueCode, Integer index) {
		String devName = "ZeroUDataPanel";
		String mounting = "ZeroU";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.DATA_PANEL;
		Long itemSubClass = 0L; //NA
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createBuswayFixedItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createBuswayFixedItem(Long statusValueCode, Integer index) {
		String devName = "BuswayFixed";
		String mounting = "Busway";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.FLOOR_OUTLET;
		Long itemSubClass = SystemLookup.SubClass.BUSWAY_OUTLET;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createWhipOutletItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createWhipOutletItem(Long statusValueCode, Integer index) {
		String devName = "WhipOutlet";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.FLOOR_OUTLET;
		Long itemSubClass = SystemLookup.SubClass.WHIP_OUTLET;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createZeroURackPDUItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createZeroURackPDUItem(Long statusValueCode, Integer index) {
		String devName = "ZeroURackPDU";
		String mounting = "ZeroU";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.RACK_PDU;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRackableRackPDUItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRackableRackPDUItem(Long statusValueCode, Integer index) {
		String devName = "RackableRackPDU";
		String mounting = "Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.RACK_PDU;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNonRackableRackPDUItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNonRackableRackPDUItem(Long statusValueCode, Integer index) {
		String devName = "NonRackableRackPDU";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.RACK_PDU;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRackableProbeItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRackableProbeItem(Long statusValueCode, Integer index) {
		String devName = "RackableProbe";
		String mounting = "Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.PROBE;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createZeroUProbeItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createZeroUProbeItem(Long statusValueCode, Integer index) {
		String devName = "ZeroUProbe";
		String mounting = "ZeroU";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.PROBE;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createNonRackableProbeItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createNonRackableProbeItem(Long statusValueCode, Integer index) {
		String devName = "NonRackableProbe";
		String mounting = "Non-Rackable";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.PROBE;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.IT_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createFreeStandingFixedFPDUItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createFreeStandingFixedFPDUItem(Long statusValueCode, Integer index) {
		String devName = "FreeStandingFixedFPDU";
		String mounting = "Free-Standing";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.FLOOR_PDU;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createLocalPanelBoardItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createLocalPanelBoardItem(Long statusValueCode, Integer index) {
		String devName = "LocalPanelBoard";
		String mounting = "";
		String formFactor = "";
		Long itemClass = SystemLookup.Class.FLOOR_PDU;
		Long itemSubClass = SystemLookup.SubClass.LOCAL;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createRemotePaneltem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createRemotePaneltem(Long statusValueCode, Integer index) {
		String devName = "RemotePanel";
		String mounting = "";
		String formFactor = "";
		Long itemClass = SystemLookup.Class.FLOOR_PDU;
		Long itemSubClass = SystemLookup.SubClass.REMOTE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createBuswayPanelItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createBuswayPanelItem(Long statusValueCode, Integer index) {
		String devName = "BuswayPanel";
		String mounting = "";
		String formFactor = "";
		Long itemClass = SystemLookup.Class.FLOOR_PDU;
		Long itemSubClass = SystemLookup.SubClass.REMOTE;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createUPSItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createUPSItem(Long statusValueCode, Integer index) {
		String devName = "UPSItem";
		String mounting = "Free-Standing";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.UPS;
		Long itemSubClass = 0L;
		
		
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#createCRACItem(java.lang.Long, java.lang.Integer)
	 */
	@Override
	public Item createCRACItem(Long statusValueCode, Integer index) {
		String devName = "CRACItem";
		String mounting = "Free-Standing";
		String formFactor = "Fixed";
		Long itemClass = SystemLookup.Class.CRAC;
		Long itemSubClass = 0L;
		return createItem(devName, index, mounting,formFactor, itemClass, itemSubClass, ItemType.ME_ITEM, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#addDataPortsToItem(com.raritan.tdz.domain.Item, java.lang.Long)
	 */
	@Override
	public void addDataPortsToItem(Item item, Long portSubClassValueCode){
		Long portId = unitTestIdGenerator.nextId();
		LksData portSubClass = systemLookupInitTest.getLks(portSubClassValueCode); 
		
		// Create data port
		Set<DataPort> ports = new HashSet<DataPort>();
		DataPort newPort = new DataPort();
		newPort.setPortId(portId);
		newPort.setPortSubClassLookup(portSubClass);
		newPort.setUsed(true);
		newPort.setItem(item);
		ports.add(newPort);
		
		item.setDataPorts(ports);
		
		//final List<String> itemsListConnection = new ArrayList<String>();// {{ add("TestItem");}};
		itemsListConnection.add( item.getItemName() );
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.ItemMock#addExpectations(com.raritan.tdz.domain.Item, org.jmock.Mockery)
	 */
	@Override
	public void addExpectations(final Item item, Mockery currentJmockContext) {
		final long itemId = item.getItemId();
		final List<Long> idsToDelete = new ArrayList<Long>(); // {{add(0L);}};
		final List<Long> idsToDeleteForStage = new ArrayList<Long>();
		final List<Long> idsToDeleteForConnections = new ArrayList<Long>();

		final List<Long> idsToDelete1 = new ArrayList<Long>(); // {{add(0L);}};
		final List<Long> idsToDeleteForStage1 = new ArrayList<Long>();
		final List<Long> idsToDeleteForConnections1 = new ArrayList<Long>();
		
		final List<Long> itemListInvalidForStage = new ArrayList<Long>();
		
		//final List<String> itemsListConnection = new ArrayList<String>();// {{ add("TestItem");}};
		//itemsListConnection.add( item.getItemName() );
		itemsListConnection.clear();
		
		itemExpectations.createGetItemName(currentJmockContext, itemId, item.getItemName());
		itemExpectations.createGetItem(currentJmockContext, itemId, item);
		itemExpectations.createReadItem(currentJmockContext, itemId, item);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDelete);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDeleteForStage);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDeleteForConnections);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDelete1);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDeleteForStage1);
		itemExpectations.createOneOfGetItemIdsToDelete(currentJmockContext, itemId, idsToDeleteForConnections1);
		itemExpectations.createGetItemsToDeleteInvalidStages(currentJmockContext, idsToDeleteForStage, itemListInvalidForStage);
		itemExpectations.createGetFreeStandingItemIdForItem(currentJmockContext, itemId, itemId);
		                       
		//for connections
		itemExpectations.createGetItemToDeleteConnected(currentJmockContext, idsToDeleteForStage, itemsListConnection);
		itemExpectations.createGetFPDUItemToDeleteConnected(currentJmockContext, itemId, itemsListConnection);
		itemExpectations.createGetPowerPanelItemToDeleteConnected(currentJmockContext, itemId, itemsListConnection);
	}

	protected MapBindingResult getErrorObject(Class<?> targetClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, targetClass.getName());
		return errors;
	}	
}
