package com.raritan.tdz.port;

import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.SensorPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.port.dao.SensorPortFinderDAO;
import com.raritan.tdz.port.dao.SensorPortUpdaterDAO;
import com.raritan.tdz.port.home.SensorPortHelper;
import com.raritan.tdz.util.UnitConverterLookup;
import com.raritan.tdz.util.UnitImpl;
import com.raritan.tdz.util.UnitIntf;

public class SensorPortTests extends PortTests {
	
	//private ItemDAO itemDAO;
	//private ItemFinderDAO itemFinderDAO;
	private SensorPortHelper sensorPortHelper;
	private ModelHome modelHome;
	
	private final String sensorPortsTab = "tabSensorPorts";
	private final String powerPortsTab = "tabPowerPorts";
	private final String dataPortsTab = "tabDataPorts";
	private UserInfo adminUserInfo;
	
	private static int testId;
	private Map<String, TestCaseInfo>testCasesInfo; // key is model name
	private Long lastItemId;

	SensorPortFinderDAO sensorPortFinder;
	SensorPortUpdaterDAO sensorPortUpdater;
	UnitIntf unit;
 	
	/*
	 *  This is a helper class used to build SensorPort DTO
	 */
	private class SensorPortBuilder{	
		private Map<Integer, SensorPortDTO> sensorPorts;
		
		SensorPortBuilder(){
			sensorPorts = new HashMap<Integer, SensorPortDTO>();
		}
		
		public Integer addSensor( String portName, Long sensorType, Long portId, Integer index, Long itemId ){
			SensorPortDTO port = new SensorPortDTO();
			port.setPortSubClassLksValueCode(sensorType);
			port.setPortName( portName);
			port.setPortId( portId );
			if( index != null ){
				port.setSortOrder(index.intValue());
			}
			port.setItemId(itemId);
			Integer key = sensorPorts.size();
			sensorPorts.put(key , port);
			return key;
		}
		
		public void setSensorLocation( Integer sensorKey, Boolean isInternal, Long cabinetId, Long position, String xyz){
			SensorPortDTO port = sensorPorts.get(sensorKey);
			port.setIsInternal(isInternal);
			port.setCabinetId(cabinetId);
			port.setCabLocLksValueCode(position);
			port.setXyzLocation(xyz);
		}
		
		public  ValueIdDTO getSensorsDTO(){
			ValueIdDTO sensorPortDTO = new ValueIdDTO();
			sensorPortDTO.setLabel(sensorPortsTab);
			sensorPortDTO.setData(new ArrayList<SensorPortDTO>(sensorPorts.values()));
			return sensorPortDTO;
		}
	}

	/*
	 * This is a helper class used to keep info about the model
	 */
	private class ModelInfo{
		private Long make;
		private Long model;
		private List<PowerPortDTO> powerPorts;
		private List<DataPortDTO> dataPorts;
		
		public ModelInfo( Long make, Long model) throws DataAccessException{
			this.make = make;
			this.model = model;
			this.powerPorts = modelHome.getAllPowerPort(model);
			
			for( PowerPortDTO pp : this.powerPorts ){
				pp.setItemId( new Long(-1L));
			}
			this.dataPorts =modelHome.getAllDataPort(model);
			
			for( DataPortDTO dp : this.dataPorts ){
				dp.setItemId( new Long(-1L));
			}
		}
		public List<PowerPortDTO> getPowerPorts() {
			return powerPorts;
		}

		public List<DataPortDTO> getDataPorts() {
			return dataPorts;
		}

		public Long getMake(){ return this.make; }
		public Long getModel(){ return this.model; }
	}
	
	private class TestCaseInfo{
		private List<ModelInfo> supportedItemClasses;
		private List<ModelInfo> unSupportedItemClasses;
		private String itemName;
		private Long location;
		private String psRedundancy;
		private SensorPortBuilder sensorInfo;
		
		public String getItemName() {
			return itemName;
		}

		public Long getLocation() {
			return location;
		}
		
		public String getPsRedundancy() {
			return psRedundancy;
		}

		TestCaseInfo( String itemName, Long location) throws DataAccessException{
			this.itemName= itemName;
			this.location = location;
			this.supportedItemClasses = new ArrayList<ModelInfo>();
			this.unSupportedItemClasses = new ArrayList<ModelInfo>();
			this.psRedundancy = "N";
		}

		public ModelInfo getSupportedItemClass( int index) {
			return this.supportedItemClasses.get(index);
		}

		public void addSupportedItemClass(ModelInfo supportedItemClass) {
			this.supportedItemClasses.add( supportedItemClass);
		}
		
		public ModelInfo getUnSupportedItemClass( int index) {
			return this.unSupportedItemClasses.get(index);
		}

		public void addUnSupportedItemClass(ModelInfo unSupportedItemClass) {
			this.unSupportedItemClasses.add( unSupportedItemClass);
		}		
	}

	/*
	 * This method prepares data for almost all tests cases. 
	 * It is invoked at the startup. That data is later used by each test case
	 * to create DTO.
	 * 
	 * All test are divided into groups. See Test's group annotation (in front of each test) 
	 * to find out in which group each test belongs. Tests belonging to one
	 * group, test the same functionality, but for different type of items
	 * or different type of sensors.
	 */
	private void addTestCases() throws DataAccessException{
		testCasesInfo = new HashMap<String, TestCaseInfo>();
		
		// Test group 1 - positive test case on supported devices (RackPDU and Probe)
		// Only basic info is provided. Sensor's data is later, 
		// dynamically created and attached to testCaseInfo
		// because this test case tests all supported sensors
		String itemName1 = "BRH-001";
		Long location1 = 1L;
		TestCaseInfo test1 = new TestCaseInfo(itemName1, location1);
		
		ModelInfo modelInfo1 = new ModelInfo(61L, 20000L); //Rack PDU (Raritan/Dominion PX PX2-1508)
		ModelInfo modelInfo2 = new ModelInfo(10L, 3592L); //Probe (APC/NBRK0201)
		test1.addSupportedItemClass(modelInfo1);
		test1.addSupportedItemClass(modelInfo2);
		testCasesInfo.put("test1", test1);
		
		// test group 2 - for asset strip sensor only. It will attempt to save asset strip sensor 
		// to one item and assign it one cabinet, then will create another item and sensor
		// will try to reuse previous  cabinet
		String itemName2 = "BRH-002";
		TestCaseInfo test2 = new TestCaseInfo(itemName2, location1);

		test2.addSupportedItemClass(modelInfo1);
		test2.sensorInfo = createTest2SensorInfo(SystemLookup.PortSubClass.ASSET_STRIP, -1L );
		testCasesInfo.put("test2", test2);
		
		// test group 3 - test required fields.
		// we will reuse test1 info for this purpose and will dynamically add
		// missed required fields one by one 
		String itemName3 = "BRH-003";
		TestCaseInfo test3 = new TestCaseInfo(itemName3, location1);
		test3.addSupportedItemClass(modelInfo1);
		testCasesInfo.put("test3", test3);
		
		// Test group 5 - negative test case on supported devices (RackPDU and Probe)
		// Only basic info is provided. Sensor's data is later, 
		// dynamically created and attached to testCaseInfo
		// because this test case tests all supported sensors
		String itemName5 = "BRH-005";
		TestCaseInfo test5 = new TestCaseInfo(itemName5, location1);
		
		ModelInfo modelInfo5 = new ModelInfo(27L, 1255L); //CRACK: Liebert; Deluxe System/3 - 20 Ton
		test5.addUnSupportedItemClass(modelInfo5);
		ModelInfo modelInfo6 = new ModelInfo(27L, 1304L); //UPS: Liebert; Series 610
		test5.addUnSupportedItemClass(modelInfo6);
		ModelInfo modelInfo7 = new ModelInfo(27L, 2835L); //Floor PDU: Liebert; Precision Power 50-225kVA
		test5.addUnSupportedItemClass(modelInfo7);
	
		testCasesInfo.put("test5", test5);
	}
	
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		testId = 1;
//		itemDAO = (ItemDAO)ctx.getBean("itemDAO");
//		itemFinderDAO = (ItemFinderDAO)ctx.getBean("itemDAO");
		sensorPortHelper = (SensorPortHelper)ctx.getBean("sensorPortHelper");
		sensorPortFinder = 	(SensorPortFinderDAO) ctx.getBean("sensorPortDAO");
		sensorPortUpdater = (SensorPortUpdaterDAO)ctx.getBean("sensorPortDAO");
		unit = (UnitImpl) ctx.getBean("unit");
		modelHome = (ModelHome)ctx.getBean("modelHome");
		adminUserInfo = createGatekeeperUserInfo(UnitConverterLookup.US_UNIT);
		addTestCases();
	}

	private void createTestSensorInfo1(Long sensorType, Long itemId, String testName ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "BrhSensor1";
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType, null);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;
		Long position = SystemLookup.SensorLocation.BOTTOM_REAR;
		Boolean isInternal = true;
		String xyz = null;
		int index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}
	
	private SensorPortBuilder createTest2SensorInfo(Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		String testName = "test2";
		
		String sensorName = "BrhSensor2";
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType, null);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;
		Long position = SystemLookup.SensorLocation.MID_REAR;
		Boolean isInternal = true;
		String xyz = null;
		int index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		return sensorBuilder;
	}
	

	protected UserInfo createGatekeeperUserInfo(Long unit){
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941",
				"", unit.toString(), "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}
	
	protected void printItemDetailsMap(Map<String, UiComponentDTO> mapToPrint)
	{
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
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId() + ", value="
									+ uiValueIdField.getValue().toString());
							if(uiValueIdField.getValueId().equals("tabSensorPorts")){
								System.out.println("SensorPort");
							}
						} else {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
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
	@Test(groups="test2")
	public void testChangeSensorType() throws Throwable {
		//private void testSensor( Long sensorType, String testName, int modelInfoId ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Long sensorType = SystemLookup.PortSubClass.TEMPERATURE;
		String testName = "test2";
		Long itemId = testSensorSave( sensorType, testName,0 );
		Long sensorPort = 0L;
		Long sensorIndex = 0L;
		System.out.println("----- itemId=" + itemId);
		Long newSensorType = SystemLookup.PortSubClass.ASSET_STRIP;
		testSensorEditSensorType( itemId, testName, sensorIndex, newSensorType);
		
//		testSensorDelete( sensorPort, testName, itemId);
					
		deleteItem( itemId);

		//}
	}

	@Test(groups="test0")
	public void testsGetSensorDetaild() throws Throwable{
		Long itemId = 945L;
		Map<String, UiComponentDTO> itemDetails = itemHome.getItemDetails(itemId, adminUserInfo);
		printItemDetailsMap(itemDetails);
		assertTrue(itemDetails != null);
	}
	
	/*
	 * Ignore this test. We are just checking if item's DAO works
	 */
	@Test(groups="test0")
	public void testItem(){
		List<Item> items = itemFinderDAO.findById(13L);
		assertTrue(items.size() == 1);
		
	}
	
	/*
	 * When testing manually, to see different results (e.g. to assign certain Asset sensort to cabinets)
	 * change database by runnign the following update:
	 * 
	 * update dct_ports_sensor set subclass_lks_id=1053, port_name='AssetStrip1', cabinet_item_id=3, cab_loc_lks_id=955 where port_sensor_id=12718;
	 * update dct_ports_sensor set subclass_lks_id=1053, port_name='AssetStrip1', cabinet_item_id=3051, cab_loc_lks_id=955 where port_sensor_id=12766;
	 * update dct_ports_sensor set subclass_lks_id=1053, port_name='AssetStrip2', xyz_location='45:67:89' where port_sensor_id=10740;
	 */
	@Test(groups="test0")
	public void testGetAllCabinets(){
		String siteCode1 = "SITE B";
		String noSiteCode = null;
		
		List<Item> cabinetsInSiteCode1 = itemDAO.getAllCabinets(siteCode1);
		List<Item> cabinetsInAllSites = itemDAO.getAllCabinets(noSiteCode);
		
		Map<Long, String> cabinetsIds = new HashMap<Long, String>();
		assertTrue(cabinetsInSiteCode1.size() > 0);
		for( Item cabinet : cabinetsInSiteCode1 ){
			assertTrue( cabinet.getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET);
			cabinetsIds.put(cabinet.getItemId(), cabinet.getItemName());
		}		
		
		List<ValueIdDTO> cabinetIdDTOAll = sensorPortHelper.getAvailableCabinetsForSensor(siteCode1, SystemLookup.PortSubClass.TEMPERATURE, null, null);
		assertTrue(cabinetsInSiteCode1.size() == cabinetIdDTOAll.size());
		for( ValueIdDTO cabinetDTO : cabinetIdDTOAll){
			cabinetsIds.remove(cabinetDTO.getData());
		}
		//Now, cabinetsIds should be empty
		assertTrue(cabinetsIds.size() == 0);
		
		List<ValueIdDTO> cabinetIdDTOWithoutAssetStrip = sensorPortHelper.getAvailableCabinetsForSensor(siteCode1, SystemLookup.PortSubClass.ASSET_STRIP, null, null);
		assertTrue(cabinetIdDTOWithoutAssetStrip.size() <= cabinetsInSiteCode1.size());
		
		List<ValueIdDTO> itemHomeRetVal = itemHome.getAvailableCabinetsForSensor(siteCode1, SystemLookup.PortSubClass.TEMPERATURE, null);
		assertTrue(itemHomeRetVal.size() == cabinetIdDTOAll.size());
		
		
		List<ValueIdDTO> cabinetIdDTOForAllSitesWithoutAssetStrip = sensorPortHelper.getAvailableCabinetsForSensor(null, SystemLookup.PortSubClass.ASSET_STRIP, null, null);
		assertTrue(cabinetIdDTOForAllSitesWithoutAssetStrip.size() <= cabinetsInAllSites.size());
	}
	
 	private Item getItemFromDTOMap(Map<String, UiComponentDTO> itemMap) {
		long itemId = -1;
		Item retval = null;
		UiComponentDTO componentDTO = itemMap.get("tiName");
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId =(Long) uiValueIdField.getValueId(); 
		}
		if( itemId > 0){
			Item item = itemDAO.getItem(itemId);
		}
		return retval;
	}
	
	private long getItemIdFromDTOMap(Map<String, UiComponentDTO> itemMap) {
		long itemId = -1;
		
		UiComponentDTO componentDTO = itemMap.get("tiName");
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId =(Long) uiValueIdField.getValueId(); 
		}
		return itemId;
	}
	

	private List<ValueIdDTO> buildNewItemDTOList( String testName, ModelInfo modelInfo ){
		TestCaseInfo test = testCasesInfo.get(testName);

		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = new ValueIdDTO();

		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.PLANNED);
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(modelInfo.getMake()); 
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(modelInfo.getModel());
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData(test.getItemName());
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbPSRedundancy");
		dto.setData(test.getPsRedundancy());
		valueIdDTOList.add(dto);

		//Placement Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(test.getLocation());
		valueIdDTOList.add(dto);

		ValueIdDTO dataPortDTO = new ValueIdDTO();
		dataPortDTO.setLabel("tabDataPorts");
		dataPortDTO.setData(modelInfo.getDataPorts());
		valueIdDTOList.add(dataPortDTO);

		ValueIdDTO powerPortDTO = new ValueIdDTO();
		powerPortDTO.setLabel("tabPowerPorts");
		powerPortDTO.setData(modelInfo.getPowerPorts());
		valueIdDTOList.add(powerPortDTO);

		return valueIdDTOList;
	}

	private void testGroup1CommonTestBody(Long sensorType, String testName, int modelInfoId) throws BusinessValidationException, ClassNotFoundException, Throwable
	{
		try {
			System.out.println("##### Current method: " + getCurrentMethodName());
			testSensor( sensorType, testName, modelInfoId ) ;
		}catch(BusinessValidationException be){
			List<String> errors = be.getValidationErrors();
			for( String err : errors ){
				System.out.println("--- " + err );
			}
		}
	}

	/*
	 *  This test case goes through the list of supported item classes
	 *  (testCasesInfo.get("test1").supportedItemClass) and through the list of supported sensors
	 *  local: supportedSensors and for each of those combinations tries to create a
	 *  an item that has sensor port.
	 *  Further it trys to edit such item, then it itrys to delete the sensor port and 
	 *  at the end it deletes the item.
	 */
	@Test(groups="test1")
	public void testGroup1SensorPortsAndSupportedItems() throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		String testName = "test1";
		int numSupportedClasses = testCasesInfo.get("test1").supportedItemClasses.size();
		for( int  modelInfoId = 0;  modelInfoId < numSupportedClasses; modelInfoId++ ){
			Long[] supportedSensors = {
					SystemLookup.PortSubClass.ASSET_STRIP, 
					SystemLookup.PortSubClass.AIR_FLOW,
					SystemLookup.PortSubClass.PRESSUERE,
					SystemLookup.PortSubClass.TEMPERATURE,
					SystemLookup.PortSubClass.HUMIDITY,
					SystemLookup.PortSubClass.CONTACT_CLOSURE,
					SystemLookup.PortSubClass.WATER,
					SystemLookup.PortSubClass.VIBRATION,
					SystemLookup.PortSubClass.SMOKE
					};	
			for( Long sensorType : supportedSensors ){
				System.out.println("##### " + getCurrentMethodName() + " testing sensor: " + 
						sensorType + " model = " + 
						testCasesInfo.get(testName).getSupportedItemClass(modelInfoId).getModel() +
						" make = " + 
						testCasesInfo.get(testName).getSupportedItemClass(modelInfoId).getMake());
				testGroup1CommonTestBody(sensorType, testName, modelInfoId);
			}
		}
	}
	
	
	private void testGroup5CommonTestBody(Long sensorType, String testName, int modelInfoId) throws BusinessValidationException, ClassNotFoundException, Throwable
	{
		Long itemId = -1L;
		boolean gotException = false;
		try {
			System.out.println("##### Current method: " + getCurrentMethodName());
			//we will reuse sensor setup from test1; sensor info does nto matter here
			//since op is supposed to fail independently of sensor data
			createTestSensorInfo1( sensorType, -1L, testName );
			System.out.println("##### Current method: " + getCurrentMethodName());	
			
			Map<String, UiComponentDTO> retval = null;
			ModelInfo modelInfo = testCasesInfo.get(testName).getUnSupportedItemClass(modelInfoId) ;
			List<ValueIdDTO> itemDTOList = buildNewItemDTOList( testName, modelInfo);
			itemDTOList.add( testCasesInfo.get(testName).sensorInfo.getSensorsDTO());
			
			retval = itemHome.saveItem(itemId, itemDTOList, adminUserInfo);
			printItemDetailsMap(retval);
			itemId = getItemIdFromDTOMap(retval);
		}catch(BusinessValidationException be){
			gotException = true;
			List<String> errors = be.getValidationErrors();
			for( String err : errors ){
				System.out.println("--- " + err );
			}
		}
		finally{
			if( itemId.longValue() > 0){
				deleteItem(itemId);
			}
			assertTrue(gotException == false);
		}
	}
	
	/*
	 *  This test case goes through the list of UNsupported item classes
	 *  (testCasesInfo.get("test1").unSupportedItemClass) and through the list of supported sensors
	 *  local: supportedSensors and for each of those combinations tries to create a
	 *  an item that has sensor port.
	 *  It expect save to fail throwing Business validation error
	 */
	/* NOTE NOTE NOTE: Changing from unsupported to supported as they are supported now 
	 * e.g. CV allows user to create sensors for FPDU, UPS, CRAC items  
	 * if user edits them in WEB UI, we need to allow them to be saved  
	 */
	@Test(groups="test5") 
	public void testGroup5SensorPortsAndSupportedItems() throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		String testName = "test5";
		int numUNSupportedClasses = testCasesInfo.get("test5").unSupportedItemClasses.size();
		for( int  modelInfoId = 0;  modelInfoId < numUNSupportedClasses; modelInfoId++ ){
			Long[] supportedSensors = {
					SystemLookup.PortSubClass.TEMPERATURE,
					SystemLookup.PortSubClass.ASSET_STRIP, 
					SystemLookup.PortSubClass.AIR_FLOW,
					SystemLookup.PortSubClass.PRESSUERE,
					SystemLookup.PortSubClass.HUMIDITY,
					SystemLookup.PortSubClass.CONTACT_CLOSURE,
					SystemLookup.PortSubClass.WATER,
					SystemLookup.PortSubClass.VIBRATION,
					SystemLookup.PortSubClass.SMOKE
					};
			for( Long sensorType : supportedSensors ){
				System.out.println("##### " + getCurrentMethodName() + " testing sensor: " + 
						sensorType + " model = " + 
						testCasesInfo.get(testName).getUnSupportedItemClass(modelInfoId).getModel() +
						" make = " + 
						testCasesInfo.get(testName).getUnSupportedItemClass(modelInfoId).getMake());
				testGroup5CommonTestBody(sensorType, testName, modelInfoId);
			}
		}
	}
	
	/*
	 * This test case creates an item with asset strip and assigns a cabinet to it.
	 * Then it trys to create anothet item and another sensor and trys to 
	 * assign the same cabinet. It expects second call to fail.
	 */
	@Test(groups="test2")
	public void testAssetStripAlreadyAssigned() throws ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Boolean gotException = false;
		Long itemId1 = -1L;
		Long itemId2 = -1L;
		try {
			Long sensorType = SystemLookup.PortSubClass.ASSET_STRIP;

			String testName = "test2";
			itemId1 = testSensorSave( sensorType, testName, 0 );
			//try to save again the same sensor type into the same cabinet
			//this should throw bussiness validation exception
			itemId2 = testSensorSave( sensorType, testName, 0 );
		}catch(BusinessValidationException be){
			List<String> errors = be.getValidationErrors();
			for( String err : errors ){
				System.out.println("--- " + err );
			}
			gotException = true;
		}finally{
			if( itemId1.longValue() != -1 ){
				deleteItem(itemId1);
			}
			if( itemId2 != -1){
				deleteItem(itemId2);
			}
		}
		assertTrue( gotException == true );
	}
	
	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!!
	//      see testMissingField()
	private void createTest3MissingPortNameSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = null;
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType, null);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;
		Long position = SystemLookup.SensorLocation.BOTTOM_REAR;
		Boolean isInternal = true;
		String xyz = null;
		int index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}
	
	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!
	//      see testMissingField()
	private void createTest3MissingIndexSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "BrhSens 2";
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType, null);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;
		Long position = SystemLookup.SensorLocation.BOTTOM_REAR;
		Boolean isInternal = true;
		String xyz = null;
		Integer index = null;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}
	
/*	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!
	//  see testMissingField()
	private void createTest3MissingCabAndLocSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "BrhSens 2";
		Long sensorId = -1L;

		Long cabinetId=null; //location;
		Long position = SystemLookup.SensorLocation.BOTTOM_REAR;
		Boolean isInternal = true;
		String xyz = null;
		Integer index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}*/
	
/*	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!
	//  see testMissingField()
	// Case when user added too much info: cabinet, position and XYZ
	private void createTest3MissingTooMuchSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "BrhSens 2";
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;Long cabinetId=null; //location;
		Long position = SystemLookup.SensorLocation.BOTTOM_REAR;
		
		Boolean isInternal = true;
		String xyz = "x=5.6,y=8,z=9.7";
		Integer index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}*/
	
/*	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!!
	//  see testMissingField()
	private void createTest3MissingPosAndLocSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "Brh sens5";
		Long sensorId = -1L;

		List<ValueIdDTO> availableCabinets = itemHome.getAvailableCabinetsForSensor("SITE A", sensorType);
		assertTrue(availableCabinets.size() >= testId);
		
		ValueIdDTO tmp = availableCabinets.get(testId++);
		Long cabinetId=(Long) tmp.getData(); //location;
		Long position = null;
		Boolean isInternal = true;
		String xyz = null;
		int index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}*/
	
/*	//NOTE: Do NOT change method name!!! Reflexion used to invoke method based on its name!!!
	//  see testMissingField()
	private void createTest3MissingCabPosAndLocSensorInfo(String testName, Long sensorType, Long itemId ){
		System.out.println("##### Current method: " + getCurrentMethodName());
		
		String sensorName = "Brh sens5";
		Long sensorId = -1L;
		Long cabinetId = null; //location;
		Long position = null;
		Boolean isInternal = true;
		String xyz = null;
		int index = 1;
		
		SensorPortBuilder sensorBuilder = new SensorPortBuilder();
		Integer sensorKey = sensorBuilder.addSensor(sensorName, sensorType, sensorId, index, itemId );
		sensorBuilder.setSensorLocation(sensorKey, isInternal, cabinetId, position, xyz);
		testCasesInfo.get(testName).sensorInfo = sensorBuilder;	
	}*/
	
	private void testMissingField(String testName, Method method) throws ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Boolean gotException = false;
		Long itemId1 = -1L;
		try {
			Long sensorType = SystemLookup.PortSubClass.ASSET_STRIP;
			method.invoke(this, testName, sensorType, -1L );
			itemId1 = testSensorSave( sensorType, testName, 0 );
		}catch(BusinessValidationException be){
			List<String> errors = be.getValidationErrors();
			for( String err : errors ){
				System.out.println("--- " + err );
			}
			gotException = true;
		}
		if( itemId1.longValue() != -1 ){
			deleteItem(itemId1);
		}
		assertTrue( gotException == true );
	}

	/*
	 * test missing portName
	 * test missing Index
	 * test missing cabinet and location
	 * test missing position and location
	 * test missing cabinet, position and location
	 */
	@Test(groups="test3")
	public void testRequiredFields() throws ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		String testName = "test3";
		
		Class<?> c = getClass();
		Method [] allMethods= c.getDeclaredMethods();
		for (Method m : allMethods){
			String mname = m.getName();
			System.out.println(" method = " + mname);
			if( mname.startsWith("createTest3Missing") && mname.endsWith("SensorInfo")){
				testMissingField(testName, m); 
			}
		}
	}
	
	private void testSensor( Long sensorType, String testName, int modelInfoId ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		createTestSensorInfo1( sensorType, -1L, testName );
		Long itemId = testSensorSave( sensorType, testName, modelInfoId );
		Long sensorPort = 0L;
		Long sensorIndex = 0L;
		System.out.println("----- itemId=" + itemId);
		Long senorPort = testSensorEdit(itemId, testName, sensorIndex);
		testSensorDelete( sensorPort, testName, itemId);
		deleteItem( itemId);
		
	}

	private void deleteItem( Long itemId ) throws DataAccessException, BusinessValidationException, Throwable {
		System.out.println("##### Current method: " + getCurrentMethodName());
		boolean retval = itemHome.deleteItem(itemId, true, adminUserInfo);
		assertTrue( retval == true );
	}
	
	private ValueIdDTO deleteSensor( Long sensorPort, Map<String, UiComponentDTO> retval){
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		Iterator<SensorPortDTO> i = sensorDTOList.iterator();
		while( i.hasNext()){
			SensorPortDTO mySensor = i.next();
			if(mySensor.getPortId().longValue() == sensorPort.longValue() ){
				i.remove();
			}
		}
		
		ValueIdDTO sensorPortValueIdDTO = new ValueIdDTO();
		sensorPortValueIdDTO.setLabel(sensorPortsTab);
		sensorPortValueIdDTO.setData(sensorDTOList);
		
		return sensorPortValueIdDTO;
	}
	
	private void verifySensorPortIsDeleted(Long sensorPort, Map<String, UiComponentDTO> retval){
		boolean found = false;
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		for( SensorPortDTO sensor : sensorDTOList ){
			if(sensor.getPortId().longValue() == sensorPort.longValue() ){
				found = true;
			}
		}
		assertTrue( found == false );
	}
	
	private void testSensorDelete( Long sensorId, String testName, Long itemId) throws Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());

		List<ValueIdDTO> itemDTO = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = new ValueIdDTO();
				
		//Read Item 
		Map<String, UiComponentDTO> retval = itemHome.getItemDetails(itemId, adminUserInfo);
		
		itemDTO.add(deleteSensor( sensorId, retval));
		
		ValueIdDTO powerPortValueIdDTO = new ValueIdDTO();
		powerPortValueIdDTO.setLabel(powerPortsTab);
		List<PowerPortDTO> powerDTOList  = (List<PowerPortDTO>) retval.get(powerPortsTab).getUiValueIdField().getValue();
		powerPortValueIdDTO.setData(powerDTOList);
		itemDTO.add(powerPortValueIdDTO);
		
		ValueIdDTO dataPortValueIdDTO = new ValueIdDTO();
		dataPortValueIdDTO.setLabel(dataPortsTab);
		List<DataPortDTO> dataDTOList  = (List<DataPortDTO>) retval.get(dataPortsTab).getUiValueIdField().getValue();
		dataPortValueIdDTO.setData(dataDTOList);
		itemDTO.add(dataPortValueIdDTO);
		
		retval = itemHome.saveItem(itemId, itemDTO, adminUserInfo);
		
		assertTrue(retval != null);
		printItemDetailsMap(retval);
		lastItemId = getItemIdFromDTOMap(retval);
		assertTrue( itemId.longValue() == lastItemId.longValue() );
		
		verifySensorPortIsDeleted( sensorId, retval);
		//TODO: Sensor has been deleted
	}
	
	private Long testSensorSave( Long sensorType, String testName, int modelInfoId ) throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Long itemId = -1L;
		
		Map<String, UiComponentDTO> retval = null;
		ModelInfo modelInfo = testCasesInfo.get(testName).getSupportedItemClass(modelInfoId);
		List<ValueIdDTO> itemDTOList = buildNewItemDTOList( testName, modelInfo );
		itemDTOList.add( testCasesInfo.get(testName).sensorInfo.getSensorsDTO());
		
		retval = itemHome.saveItem(itemId, itemDTOList, adminUserInfo);
		assertTrue(retval != null);
		printItemDetailsMap(retval);
		itemId = getItemIdFromDTOMap(retval);
		return itemId;
	}
	
	
	private ValueIdDTO changeSensorLocation( int index, Map<String, UiComponentDTO> retval){
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		//change sensor's location in cabinet
		sensorDTOList.get(index).setXyzLocation("34:67:78");
		sensorDTOList.get(index).setCabinetId(0L);
		sensorDTOList.get(index).setCabLocLksValueCode(null);
		
		ValueIdDTO sensorPortValueIdDTO = new ValueIdDTO();
		sensorPortValueIdDTO.setLabel(sensorPortsTab);
		sensorPortValueIdDTO.setData(sensorDTOList);
		
		return sensorPortValueIdDTO;
	}
	
	
	private Long testSensorEdit(Long itemId, String testName, Long index) throws Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());

		List<ValueIdDTO> itemDTO = new ArrayList<ValueIdDTO>();		
		ValueIdDTO dto = new ValueIdDTO();
				
		//Read Item 
		Map<String, UiComponentDTO> retval = itemHome.getItemDetails(itemId, adminUserInfo);

		//obtain sensor port and its portId
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		Long portId = sensorDTOList.get(index.intValue()).getPortId();
		
		//change location data for sensor port
		itemDTO.add(changeSensorLocation(index.intValue(), retval));
		
		ValueIdDTO powerPortValueIdDTO = new ValueIdDTO();
		powerPortValueIdDTO.setLabel(powerPortsTab);
		List<PowerPortDTO> powerDTOList  = (List<PowerPortDTO>) retval.get(powerPortsTab).getUiValueIdField().getValue();
		powerPortValueIdDTO.setData(powerDTOList);
		itemDTO.add(powerPortValueIdDTO);
		
		ValueIdDTO dataPortValueIdDTO = new ValueIdDTO();
		dataPortValueIdDTO.setLabel(dataPortsTab);
		List<DataPortDTO> dataDTOList  = (List<DataPortDTO>) retval.get(dataPortsTab).getUiValueIdField().getValue();
		dataPortValueIdDTO.setData(dataDTOList);
		itemDTO.add(dataPortValueIdDTO);
		
		retval = itemHome.saveItem(itemId, itemDTO, adminUserInfo);
		
		assertTrue(retval != null);
		printItemDetailsMap(retval);
		lastItemId = getItemIdFromDTOMap(retval);
		assertTrue( itemId.longValue() == lastItemId.longValue() );
		
		//TODO: verify that port data was changed
		return portId;
	}

	private Long testSensorEditSensorType(Long itemId, String testName, Long index, Long newSensorType) throws Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());

		List<ValueIdDTO> itemDTO = new ArrayList<ValueIdDTO>();		
		ValueIdDTO dto = new ValueIdDTO();
				
		//Read Item 
		Map<String, UiComponentDTO> retval = itemHome.getItemDetails(itemId, adminUserInfo);

		//obtain sensor port and its portId
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		Long portId = sensorDTOList.get(index.intValue()).getPortId();
		
		//change location data for sensor port
		itemDTO.add(changeSensorType(index.intValue(), newSensorType, retval));
		
		ValueIdDTO powerPortValueIdDTO = new ValueIdDTO();
		powerPortValueIdDTO.setLabel(powerPortsTab);
		List<PowerPortDTO> powerDTOList  = (List<PowerPortDTO>) retval.get(powerPortsTab).getUiValueIdField().getValue();
		powerPortValueIdDTO.setData(powerDTOList);
		itemDTO.add(powerPortValueIdDTO);
		
		ValueIdDTO dataPortValueIdDTO = new ValueIdDTO();
		dataPortValueIdDTO.setLabel(dataPortsTab);
		List<DataPortDTO> dataDTOList  = (List<DataPortDTO>) retval.get(dataPortsTab).getUiValueIdField().getValue();
		dataPortValueIdDTO.setData(dataDTOList);
		itemDTO.add(dataPortValueIdDTO);
		
		retval = itemHome.saveItem(itemId, itemDTO, adminUserInfo);
		
		assertTrue(retval != null);
		printItemDetailsMap(retval);
		lastItemId = getItemIdFromDTOMap(retval);
		assertTrue( itemId.longValue() == lastItemId.longValue() );
		
		//TODO: verify that port data was changed
		return portId;
	}


	private ValueIdDTO changeSensorType(int index, Long newSensorType,
			Map<String, UiComponentDTO> retval) {
		List<SensorPortDTO> sensorDTOList  = (List<SensorPortDTO>) retval.get(sensorPortsTab).getUiValueIdField().getValue();
		//change sensor's location in cabinet
		sensorDTOList.get(index).setPortStatusLksValueCode(newSensorType);

		ValueIdDTO sensorPortValueIdDTO = new ValueIdDTO();
		sensorPortValueIdDTO.setLabel(sensorPortsTab);
		sensorPortValueIdDTO.setData(sensorDTOList);
		
		return sensorPortValueIdDTO;
	}
	/*
	@Test
	public void testGetAllCabinetsWithInclude(){
		String siteCode1 = "DCT-2";
		String noSiteCode = null;
		
		List<ValueIdDTO> cabinetIdDTOAll = sensorPortHelper.getAvailableCabinetsForSensor(siteCode1, SystemLookup.PortSubClass.TEMPERATURE, null, null);

		assertTrue(cabinetIdDTOAll.size() == 2);
		
		cabinetIdDTOAll = sensorPortHelper.getAvailableCabinetsForSensor(siteCode1, SystemLookup.PortSubClass.ASSET_STRIP, null, 826L);
		assertTrue(cabinetIdDTOAll.size() == 1);
		
	}*/

	public static final Map<String, Double> expectedResultMap = 
			Collections.unmodifiableMap( new HashMap<String, Double>() {
				private static final long serialVersionUID = 1L;
			{
			    // put( SystemLookup.portSubClass.<Sensor>_UserSelectedUnit); 
				// expected result for sensor value '300.00' in SI unit to user selected US unit
				put("40001_1", 572.00);		// Temprature 
				put("40002_1", 300.00);		// Humidity 
				put("40003_1",  0.04);		// Air Pressure 
				put("40008_1", 984.25);		// Air Flow

				// expected result for sensor value '300.00' in US unit to user selected SI unit
				put("40001_2", 300.00);		// Temperature value 300 in DB is in SI format (degrees C) Not change
				put("40002_2", 300.00);		// Humidity 
				put("40003_2", 2068427.18); // Air Pressure
				put("40008_2", 91.44);		// Air Flow
				
				// expected result when user selected and sensor unit are same.
				put("40001", 572.00);		// Temprature
				put("40002", 300.00);		// Humidity
				put("40003", 300.00);		// Air Pressure 
				put("40008", 300.00);		// Air Flow
			}});
	
	/*
	 *  This test case create ports and saves item as in test group  "test1". In addition to
	 *  creating ports and item, the item is edited by changing the sensor values and unit.
	 *  Finally this test verifies the sensor value after applying conversion. 
	 */
	@Test(groups="test6")
	public void testGroup6VerifySensorValueConversion() throws BusinessValidationException, ClassNotFoundException, Throwable{
		
		createSensorsForItemAndVerifyResult( UnitConverterLookup.US_UNIT, UnitConverterLookup.SI_UNIT);
		
		//createSensorsForItemAndVerifyResult( UnitConverterLookup.SI_UNIT, UnitConverterLookup.US_UNIT); // values in DB are always in US unit except for Temp sensors values

		createSensorsForItemAndVerifyResult( UnitConverterLookup.US_UNIT, UnitConverterLookup.US_UNIT);

		//createSensorsForItemAndVerifyResult( UnitConverterLookup.SI_UNIT, UnitConverterLookup.SI_UNIT); // values in DB are always in US unit except for Temp sensors values
	}
	
	public void createSensorsForItemAndVerifyResult( Long sensorUnit, Long userSelectedUnit) throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		String testName = "test1";
		int numSupportedClasses = testCasesInfo.get("test1").supportedItemClasses.size();
		for( int  modelInfoId = 0;  modelInfoId < numSupportedClasses; modelInfoId++ ){
			Long[] supportedSensors = {
					SystemLookup.PortSubClass.ASSET_STRIP, 
					SystemLookup.PortSubClass.AIR_FLOW,
					SystemLookup.PortSubClass.PRESSUERE,
					SystemLookup.PortSubClass.TEMPERATURE,
					SystemLookup.PortSubClass.HUMIDITY,
					SystemLookup.PortSubClass.CONTACT_CLOSURE,
					SystemLookup.PortSubClass.WATER,
					SystemLookup.PortSubClass.VIBRATION,
					SystemLookup.PortSubClass.SMOKE
					};	
			for( Long sensorType : supportedSensors ){
				System.out.println("##### " + getCurrentMethodName() + " testing sensor: " + 
						sensorType + " model = " + 
						testCasesInfo.get(testName).getSupportedItemClass(modelInfoId).getModel() +
						" make = " + 
						testCasesInfo.get(testName).getSupportedItemClass(modelInfoId).getMake());
				testGroup6CommonTestBody(sensorType, testName, modelInfoId, sensorUnit, userSelectedUnit);
			}
		}
	}
	
	private void testGroup6CommonTestBody(Long sensorType, String testName, int modelInfoId, Long sensorUnit, Long userSelectedUnit) throws BusinessValidationException, ClassNotFoundException, Throwable
	{
		try {
			System.out.println("##### Current method: " + getCurrentMethodName());
			test6Sensor( sensorType, testName, modelInfoId, sensorUnit, userSelectedUnit) ;
		}catch(BusinessValidationException be){
			List<String> errors = be.getValidationErrors();
			for( String err : errors ){
				System.out.println("--- " + err );
			}
			assertTrue(false);
		}
	}
	
	private Long createItemWithSensor (Long sensorType, String testName, int modelInfoId) throws BusinessValidationException, ClassNotFoundException, Throwable {
		createTestSensorInfo1( sensorType, -1L, testName );
		return testSensorSave( sensorType, testName, modelInfoId);
		
	}
	
	/* this function checks if the values are converted properly for sensors whose values are saved in US_UNIT and 
	 * user requesting the data in SI_UNIT and vice versa
	 */
	private void test6Sensor( Long sensorType, String testName, int modelInfoId, Long sensorUnit, Long userSelectedUnit) throws BusinessValidationException, ClassNotFoundException, Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Long  itemId = createItemWithSensor(sensorType, testName, modelInfoId);
		System.out.println("----- itemId=" + itemId);
		Long sensorIndex = 1L;
		
		// Edit sensor unit
		Long sensorPort = editSensorValue(itemId, testName, sensorIndex, sensorType, sensorUnit);
		
		// Set user selected unit 
		UserInfo userInfo = createGatekeeperUserInfo(userSelectedUnit);
		
		try {
			Map< String, UiComponentDTO> rval = itemHome.getItemDetails(itemId, userInfo);
			UiComponentDTO uiComponentDto = rval.get("tabSensorPorts");

			StringBuilder sb = new StringBuilder(sensorType.toString());
			if (!sensorUnit.equals(userSelectedUnit)) {
				sb.append("_");
				sb.append(userSelectedUnit.toString());
			}
			
			SensorPortDTO spDto = (SensorPortDTO)((ArrayList)uiComponentDto.getUiValueIdField().getValue()).get(0);
			Double value = spDto.getReadingValue();
			
			if (SystemLookup.PortSubClass.AIR_FLOW == sensorType ||
				SystemLookup.PortSubClass.PRESSUERE == sensorType ||
				SystemLookup.PortSubClass.TEMPERATURE == sensorType ||
				SystemLookup.PortSubClass.HUMIDITY == sensorType ) {
				
				System.out.println ("############# RESULT ###### " + sb.toString() + " : " +  "expected_result = " + expectedResultMap.get(sb.toString()) + " returned value = " + value );
				assert(expectedResultMap.get(sb.toString()).equals(value));
			}

			System.out.println (rval);
		}
		finally {
			testSensorDelete( sensorPort, testName, itemId);
			deleteItem( itemId);
		}
		
	}

	/* Since we created only one sensor for this item we update value only for that */
	
	private Long editSensorValue(Long itemId, String testName, Long index, Long sensorType, Long setSensorUnit) throws Throwable{
		System.out.println("##### Current method: " + getCurrentMethodName());
		Long sensorPort = 0L;
		
		List<SensorPort> spList = sensorPortFinder.findSensorPortByTypeAndOrder(itemId, index.intValue(), sensorType);
		if (spList!=null && spList.size() > 0) {
			SensorPort sp = spList.get(0);
			if (sp.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.ASSET_STRIP && 
					(sp.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.CONTACT_CLOSURE ||
					 sp.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.WATER ||
					 sp.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.VIBRATION||
					 sp.getPortSubClassLookup().getLkpValueCode() != SystemLookup.PortSubClass.SMOKE)) {
				
				if (sp.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.TEMPERATURE) {
					sp.setValueActual(300);
					StringBuilder sb = new StringBuilder(sensorType.toString());
					sb.append("_");
					sb.append("2"); // Temperature values are saved in  SI units = 2

					UnitImpl unitImpl = (UnitImpl)unit;
					String sensorUnit = unitImpl.getUnitForSensor(sb.toString());
					sp.setValueActualUnit(sensorUnit);
				}
				else {
					sp.setValueActual(300);
					StringBuilder sb = new StringBuilder(sensorType.toString());
					sb.append("_");
					sb.append(setSensorUnit.toString()); // unit (US = 1 , SI = 2)

					UnitImpl unitImpl = (UnitImpl)unit;
					String sensorUnit = unitImpl.getUnitForSensor(sb.toString());
					sp.setValueActualUnit(sensorUnit);
	
				}
				portHome.saveSensorPort(sp);
			}
		}
		 
		return sensorPort;
	}
	
}
