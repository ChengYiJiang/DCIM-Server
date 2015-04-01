package com.raritan.tdz.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.powerchain.home.PortConnectionAdaptorFactory;
import com.raritan.tdz.powerchain.home.PowerConnectionAdaptor;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.util.GlobalUtils;

/**
 * Base class for JUnit tests.
 * @author Santo Rosario
 */
public class ItemFactoryImpl implements ItemFactory {
	@Autowired
	private ItemDAO itemDAO;
	@Autowired
	private ModelDAO modelDAO;
	@Autowired
	private SystemLookupFinderDAO systemLookupDAO;
	@Autowired
	public PortConnectionAdaptorFactory portConnectionAdaptorFactory;
	@Autowired
	public PowerConnectionAdaptor powerConnectionAdaptor;
	@Autowired
	
	private GenericObjectSave itemSave;
	private CabinetItem defaultCabinet;
	private DataCenterLocationDetails location;
	private int recordCount = 100;
	private List<Long> createdItemList;
	
	@Override
	public List<Long> getCreatedItemList() {
		return createdItemList;
	}

	public GenericObjectSave getItemSave() {
		return itemSave;
	}

	public void setItemSave(GenericObjectSave itemSave) {
		this.itemSave = itemSave;
	}

	@Override
	public void setDefaultCabinet(Item defaultCabinet ){
		this.defaultCabinet = (CabinetItem)defaultCabinet;		
	}
	
	private void setDefaultValues(){
		if(location != null) return;
		
		location = itemSave.getTestLocation();
		
		try {
			defaultCabinet = this.createCabinet("DEFAULT-CAB-01", SystemLookup.ItemStatus.INSTALLED);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 
	 * A list of items created for unit testing.
	 * This will automatically be deleted from 
	 * the database when the test is complete.
	 */
	public ItemFactoryImpl() {
		FlexUserSessionContext.setAllowMockUser(true);
	}
	
	private ModelDetails getModel(long modelId){
		ModelDetails model = modelDAO.read(modelId);
		model.getClassLookup().getLkpValue();
		
		return model;
		
	}

	@Override
	public Long save(Object item) {
		return itemSave.save(item);
	}
	
	private Item getItem(long itemId) {
		return itemDAO.getItem(itemId);
	}

	private PowerPort createBranchCircuitBreakerPanelPort(Item item, String portName, int sortOrder) throws Throwable {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		PowerPort port = new PowerPort();		
		port.setPortName(portName);
		port.setPortSubClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER).get(0));
		port.setConnectorLookup(new ConnectorLkuData(77));
		port.setVoltsLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.VoltClass.V_208).get(0));
		port.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0));
		port.setAmpsNameplate(90);
		port.setIsRedundant(false);
		port.setPowerFactor(1);
		port.setPhaseLegsLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseLegClass.ABC).get(0));
		port.setSortOrder(sortOrder);
		port.setCreationDate(creationDate);
		port.setItem(item);
		
		return port;		
	}

	private void setItemDetail(Item item, Long statusValueCode){
		if(item.getItemName() == null){
			item.setItemName(item.getClassLookup().getLkpValue() + "-ITEM-TEST-" + String.valueOf(recordCount++));
		}

		item.setDataCenterLocation(location);
		

		if(statusValueCode == null){
			item.setStatusLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.ItemStatus.PLANNED).get(0) );
		}
		else{
			item.setStatusLookup( systemLookupDAO.findByLkpValueCode( statusValueCode).get(0) );
		}
		
		UserInfo user = FlexUserSessionContext.getUser();
		
		ItemServiceDetails detail = new ItemServiceDetails();
		detail.setSysCreationDate(GlobalUtils.getCurrentDate());
		detail.setSysUpdateDate(GlobalUtils.getCurrentDate());		
		detail.setSysCreatedBy(user.getUserName());		
		item.setItemServiceDetails(detail);	

		save(location.getDcLocaleDetails());
		save(location);
		item.setDataCenterLocation(location);
		save(item);
		
		if(createdItemList == null){
			createdItemList = new ArrayList<Long>();
		}
		
		createdItemList.add(item.getItemId());
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDevice(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDevice(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.RACKABLE).get(0) );
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.FrontFaces.NORTH).get(0) );
		item.setModel(getModel(1L));
		item.setPsredundancy("N + 1");
		item.setParentItem(defaultCabinet);
		item.setUPosition(20L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		setItemDetail(item, statusValueCode);
		
		return item;
	}	

	/**
	 * 
	 */
	@Override
	public final ItItem createDevice(String itemName, Long statusValueCode, Item cabinet) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.RACKABLE).get(0) );
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.FrontFaces.NORTH).get(0) );
		item.setModel(getModel(1L));
		item.setPsredundancy("N + 1");
		item.setParentItem(cabinet);
		item.setUPosition(20L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		setItemDetail(item, statusValueCode);
		
		return item;
	}	

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createUPS(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createUPS(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.UPS).get(0) );
		item.setModel(getModel(1304L)); // "Liebert" / "Series 610"
		
		item.setRatingV(400);
		item.setRatingAmps(200);

		setItemDetail(item, statusValueCode);
		
		
		return item;
	}	
	
	@Override
	public final MeItem createUPS3PhaseWYE(String itemName, Long statusValueCode, MeItem upsBank) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.UPS).get(0) );
		item.setModel(getModel(1304L)); // "Liebert" / "Series 610"
		
		item.setRatingAmps(0);
		item.setLineVolts(0.0);
		item.setPhaseVolts(0.0);
		item.setRatingV(480);
		item.setRatingKva(300);
		item.setRatingKW(270);
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.THREE_WYE).get(0) /*new LksData(72L)*/);
		item.setUpsBankItem(upsBank);
		
		setItemDetail(item, statusValueCode);
		
		
		return item;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDU(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createFloorPDU(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
						
		setItemDetail(item, statusValueCode);
		
		return item;
	}	
	
	@Override
	public final MeItem createFloorPDU3PhaseWYE(String itemName, Long statusValueCode, MeItem upsBank) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
					
		item.setRatingAmps(300);
		item.setLineVolts(480.0);
		item.setPhaseVolts(0.0);
		item.setRatingV(0);
		item.setRatingKva(225);
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.THREE_WYE).get(0) /*new LksData(72L)*/);
		item.setUpsBankItem(upsBank);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPowerOutlet(java.lang.String, java.lang.Long)
	 */
	@Override
	public MeItem createPowerOutlet(String outletName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		
		// dct_items table
		item.setItemName( outletName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_OUTLET).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.WHIP_OUTLET).get(0) );	
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT).get(0) );
		item.setParentItem(defaultCabinet); 
		item.setUPosition(20L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );		
		item.setModel(getModel(1434L)); // "Electrical Outlet Box" / "Hubbel"
		item.setPhaseLookup( null );
		item.setRatingV( 0 );
		item.setLineVolts( 0 );
		item.setRatingAmps( 0 );
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}

	@Override
	public MeItem createPowerPanel(Item parentItem, String panelName, Long numBranchCircuitBreakers, Long subClassLkpValueCode, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName( panelName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( subClassLkpValueCode).get(0) );
		item.setPhaseLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_2WIRE ).get(0));
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setRatingAmps(100);
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setParentItem(parentItem);

		for (Long i = 0L; i < numBranchCircuitBreakers; i++) {
			item.addPowerPort(createBranchCircuitBreakerPanelPort(item, i.toString(), i.intValue()));
		}
		setItemDetail(item, statusValueCode);
		
		return item;
	}
	
	@Override
	public MeItem createPowerPanel3PhaseWYE(Item parentItem, String panelName,  Long subClassLkpValueCode, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName( panelName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( subClassLkpValueCode).get(0) );
		item.setPhaseLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.THREE_WYE ).get(0));
		item.setRatingV(0);
		item.setLineVolts(208.0);
		item.setPhaseVolts(120.0);
		item.setRatingAmps(225);
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setParentItem(parentItem);

		setItemDetail(item, statusValueCode);
		
		return item;
	}	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithPanels(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createFloorPDUWithPanels(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0) /*new LksData(72L)*/);
		item.setRatingAmps(200);
		item.setRatingV(120);
		item.setLineVolts(120);

		setItemDetail(item, statusValueCode);
		
		item.addChildItem( createPowerPanel(item, "INT_TEST_PANEL_PS1", 4L, SystemLookup.SubClass.LOCAL, statusValueCode));
		item.addChildItem( createPowerPanel(item, "INT_TEST_PANEL_PS2", 8L, SystemLookup.SubClass.LOCAL, statusValueCode));
			
		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithNoPanels(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createFloorPDUWithNoPanels(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0) /*new LksData(72L)*/);
		//item.addChildItem( createPowerPanel(item, "PS1", 0L));
		//item.addChildItem(createPowerPanel(item, "PS2", 0L));
		item.setRatingV(120);
		item.setLineVolts(120);
		setItemDetail(item, statusValueCode);
		
		item.addChildItem(createPowerPanel(item, "PS1", 0L, SystemLookup.SubClass.LOCAL, statusValueCode));
		item.addChildItem(createPowerPanel(item, "PS2", 0L, SystemLookup.SubClass.LOCAL, statusValueCode));
		
		return item;
	}


	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithPanelsAndBranchCircuitBreakers(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createFloorPDUWithPanelsAndBranchCircuitBreakers(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.FLOOR_PDU).get(0) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0) /*new LksData(72L)*/);
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setRatingAmps(120);
		
		setItemDetail(item, statusValueCode);
		

		Item panel1 = createPowerPanel(item, "PS1", 4L, SystemLookup.SubClass.LOCAL, statusValueCode);
		Item panel2 =  createPowerPanel(item, "PS2", 8L, SystemLookup.SubClass.LOCAL, statusValueCode);
		item.addChildItem( panel1 );
		item.addChildItem( panel2 );
		
		// make a connection between fpdu and panel
		/*powerPanelBreakerPortActionHandler.process(panel1, null, true);
		powerPanelBreakerPortActionHandler.process(panel2, null, true);*/
		
		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createUPSBank(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createUPSBank(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.UPS_BANK).get(0) );
		item.setModel(getModel(1304L)); // "Series 610" / "Liebert"
		
		item.setRatingV(220);
		item.setRatingKva(300);
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.SINGLE_3WIRE).get(0) /*new LksData(72L)*/);
		setItemDetail(item, statusValueCode);
		
		return item;
	}
	
	@Override
	public final MeItem createUPSBank3PhaseWYE(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.UPS_BANK).get(0) );
		item.setModel(getModel(1304L)); // "Series 610" / "Liebert"
		
		item.setRatingAmps(0);
		item.setLineVolts(0.0);
		item.setPhaseVolts(0.0);
		item.setRatingV(480);
		item.setRatingKva(300);
		item.setRatingKW(270);
		item.setPhaseLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.PhaseIdClass.THREE_WYE).get(0) /*new LksData(72L)*/);

		setItemDetail(item, statusValueCode);
		
		return item;
	}


	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceVM(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDeviceVM(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.VIRTUAL_MACHINE).get(0) );
		item.setStatusLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.ItemStatus.PLANNED).get(0) );
						
		setItemDetail(item, statusValueCode);
		
		return item;
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceFS(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDeviceFS(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		CabinetItem cabinet = new CabinetItem();
		cabinet.setItemName(itemName + "-CAB" );
		cabinet.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.CABINET).get(0) );
		cabinet.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.CONTAINER).get(0));
		cabinet.setPositionInRow(1);
		cabinet.setRowLabel("A");
		setItemDetail(cabinet, statusValueCode);
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.RACKABLE).get(0));
		item.setModel(getModel(100L));				
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.BOTH).get(0) );	
		item.setParentItem(cabinet);
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT).get(0) );
		setItemDetail(item, statusValueCode);
				
		return item;
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceChassis(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDeviceChassis(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE_CHASSIS).get(0));
		item.setModel(getModel(1015L));				
		item.setParentItem(defaultCabinet);
		item.setUPosition(10L);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setGroupingName(itemName);
		
		setItemDetail(item, statusValueCode);
		
			
		return item;
	}		

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceChassis(java.lang.String, java.lang.Long, long)
	 */
	@Override
	public final ItItem createDeviceChassis(String itemName, Long statusValueCode, long cabinetId) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE_CHASSIS).get(0));
		item.setModel(getModel(1015L));
		item.setParentItem(getItem(cabinetId));
		item.setUPosition(-1L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setGroupingName(itemName);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}		

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkChassis(java.lang.String, java.lang.Long, long)
	 */
	@Override
	public final ItItem createNetworkChassis(String itemName, Long statusValueCode, long cabinetId) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.NETWORK).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE_CHASSIS).get(0));
		item.setModel(getModel(1015L));
		item.setParentItem(getItem(cabinetId));
		item.setUPosition(-1L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setGroupingName(itemName);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}		

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceBlade(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDeviceBlade(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE_SERVER).get(0));
		item.setModel(getModel(1020L));
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setSlotPosition(8L);
		item.setPsredundancy("N + 1");

		ItItem chassis = createDeviceChassis("Unit test temp Chassis", statusValueCode);
		item.setBladeChassis(chassis);

		item.setParentItem(defaultCabinet);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceBladeInChassis(java.lang.String, long, long, java.lang.Long)
	 */
	@Override
	public final ItItem createDeviceBladeInChassis(String itemName, long cabinetItemId, long chassisItemId, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DEVICE).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE_SERVER).get(0));
		item.setModel(getModel(1020L));
		item.setPsredundancy("N + 1");
		item.setParentItem(getItem(cabinetItemId));
		item.setBladeChassis(getItem(chassisItemId));

		setItemDetail(item, statusValueCode);
		
		return item;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkChassis(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createNetworkChassis(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.NETWORK).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.CHASSIS).get(0));
		item.setModel(getModel(45L));				
		
		item.setParentItem(defaultCabinet); 
		// item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setGroupingName(itemName);
		item.setPsredundancy("N + 1");
		
		setItemDetail(item, statusValueCode);
		
		
		return item;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkBlade(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createNetworkBlade(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.NETWORK).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.BLADE).get(0));
		item.setModel(getModel(46L));				
			
		item.setParentItem(defaultCabinet);
		item.setSlotPosition(8L);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );

		ItItem chassis = createDeviceChassis("Unit test temp Chassis", statusValueCode);
		item.setBladeChassis(chassis);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createProbe(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createProbe(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.PROBE).get(0) );
		item.setModel(getModel(3593L));
		item.setParentItem(defaultCabinet);
		item.setUPosition(8L);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setPsredundancy("N + 1");
		
		setItemDetail(item, statusValueCode);
		
		
		return item;
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCRAC(java.lang.String, java.lang.Long)
	 */
	@Override
	public final Item createCRAC(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		Item item = new Item();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.CRAC).get(0) );
		item.setModel(getModel(1255L));
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDataPanel(java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createDataPanel(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.DATA_PANEL).get(0) );
		item.setModel(getModel(54L)); // Ortronics / 2RU-48p RJ45,Flat
		item.setParentItem(defaultCabinet);
		item.setUPosition(-1L);
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT).get(0) );
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}	


	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkStack(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public final ItItem createNetworkStack(String itemName, String stackName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.NETWORK).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.NETWORK_STACK).get(0));
		item.setModel(getModel(5126L));
			
		item.setParentItem(defaultCabinet);
		item.setUPosition(8L);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		
		setItemDetail(item, statusValueCode);
		
		item.setCracNwGrpItem(item);
		
		if(stackName == null){
			item.setGroupingName(itemName + "-STACK");
		}
		else{
			item.setGroupingName(stackName + "-STACK");
		}
		
		item.setGroupingNumber("Blade");
		
		return item;
	}	

	@Override
	public final ItItem createNetworkStack(String itemName, String stackName, Long statusValueCode, Item cabinet) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.NETWORK).get(0) );
		item.setSubclassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.SubClass.NETWORK_STACK).get(0));
		item.setModel(getModel(5126L));
			
		item.setParentItem(cabinet);
		item.setUPosition(8L);		
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.FRONT).get(0) );
		
		setItemDetail(item, statusValueCode);
		
		item.setCracNwGrpItem(item);
		
		if(stackName == null){
			item.setGroupingName(itemName + "-STACK");
		}
		else{
			item.setGroupingName(stackName + "-STACK");
		}
		
		item.setGroupingNumber("Blade");
		
		return item;
	}	


	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCabinet(java.lang.String, java.lang.Long)
	 */
	@Override
	public final CabinetItem createCabinet(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		CabinetItem item = new CabinetItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.CABINET).get(0) );
		item.setPositionInRow(1);
		item.setRowLabel("A");
		ModelDetails model = getModel(6400L);
		item.setModel(model);
		
		item.setLayoutHorizFront(StringUtils.leftPad("", model.getRuHeight(), '0'));
		item.setLayoutHorizRear(StringUtils.leftPad("", model.getRuHeight(), '0'));
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCabinetWithItems(java.lang.String, java.lang.Long)
	 */
	@Override
	public final CabinetItem createCabinetWithItems(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		CabinetItem cabinet = createCabinet(itemName, statusValueCode);
		
		long idx = 1;
		
		for(; idx<5; idx++){
			ItItem item = this.createDevice(itemName + "-device-" + idx, statusValueCode);
			item.setParentItem(cabinet);
			item.setUPosition(idx);
			this.save(item);
		}
		
		for(idx=1; idx<5; idx++){
			ItItem item = this.createPassive(itemName + "-passive-" + idx, cabinet.getItemId(), statusValueCode);
			item.setParentItem(cabinet);
			item.setUPosition(idx + 5);
			this.save(item);
		}

		ItItem item = this.createDeviceChassis(itemName + "-chassis-" + idx, statusValueCode);;
		item.setParentItem(cabinet);
		item.setUPosition(idx);
		this.save(item);

		return cabinet;
	}
	
	@Override
	public final CabinetItem createCabinetWithDataPanels(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		CabinetItem cabinet = createCabinet(itemName, statusValueCode);
		
		for(long idx=1; idx<=3; idx++){
			ItItem item = this.createDataPanel(itemName + "-DataPanel-" + idx, statusValueCode);
			item.setParentItem(cabinet);
			item.setUPosition(idx);
			this.save(item);
		}
		

		return cabinet;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPerfTiles(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createPerfTiles(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.PERFORATED_TILES).get(0) );
		setItemDetail(item, statusValueCode);
		
		return item;
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createRPDU(java.lang.String, java.lang.Long)
	 */
	@Override
	public final MeItem createRPDU(String itemName, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.RACK_PDU).get(0) );
		item.setModel(getModel(1974L)); // APC / "AP7941": 1 - input cord, 24 - output cord
		item.setParentItem(getItem(55L)); //cabinet BK
		item.setFacingLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.ZeroUDepth.REAR).get(0));
		item.setMountedRailLookup(systemLookupDAO.findByLkpValueCode( SystemLookup.RailsUsed.LEFT_REAR).get(0));
		item.setUPosition(1L);
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPassive(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	@Override
	public final ItItem createPassive(String itemName, Long cabinetItemId, Long statusValueCode) throws Throwable {
		setDefaultValues();
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.Class.PASSIVE).get(0) );
		
		if(statusValueCode == null){
			item.setStatusLookup( systemLookupDAO.findByLkpValueCode( SystemLookup.ItemStatus.PLANNED).get(0) );
		}
		else{
			item.setStatusLookup( systemLookupDAO.findByLkpValueCode( statusValueCode).get(0) );
		}
		
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		
		setItemDetail(item, statusValueCode);
		
		return item;
	}

}
