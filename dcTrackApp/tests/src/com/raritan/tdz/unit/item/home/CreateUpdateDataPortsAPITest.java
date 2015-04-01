package com.raritan.tdz.unit.item.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.BusinessInformationException;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.home.PortObjectHelper;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;
import com.raritan.tdz.util.GlobalConstants;


/**
 * @author bozana
 *
 */
public class CreateUpdateDataPortsAPITest extends UnitTestBase {
	@Autowired
	private ItemHome itemHome;

	@Autowired
	UnitTestDatabaseIdGenerator unitTestIdGenerator;

	@Autowired
	ItemDAO itemDAO;

	@Autowired
	DataPortDAO dataPortDAO;
	
	@Autowired
	private ItemRequest itemRequest;

	@Autowired
	ItemRequestDAO itemRequestDAO;

	@Autowired
	SystemLookupFinderDAO systemLookupFinderDAO;

	@Autowired
	private UserLookupFinderDAO userLookup;

	@Autowired
	private SystemLookupInitUnitTest systemLookupInitTest;

	@Autowired
	private ConnectorLookupFinderDAO connectorLookupFinderDAO;

	@Autowired
	PortObjectHelper<DataPort> dataPortObjectHelper;

	@Autowired(required=true)
	private DataCircuitDAO dataCircuitDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;

	//This class keeps info about tested items
	private class MyItemInfo{
		private ModelDetails model;
		private LksData classLookup;
		private LksData subclassLookup;
		HashMap<Long, PortInterface> portMap = new HashMap<Long, PortInterface>();
		private DataPortDTO connectedItem;
		
		public void setModel( ModelDetails model){
			this.model = model;
		}
		public void setClassLookup( LksData classLkp ){
			this.classLookup = classLkp;
		}
		public void setSubClassLookup(LksData subClassLkp ){
			this.subclassLookup = subClassLkp;
		}
		public ModelDetails getModel(){
			return model;
		}
		public LksData getClassLookup(){
			return classLookup;
		}
		public LksData getSubClassLookup(){
			return subclassLookup;
		}
		public DataPortDTO getConnectedItem() {
			return connectedItem;
		}
		public void setConnectedItem(DataPortDTO connectedItem) {
			this.connectedItem = connectedItem;
		}
		
	}
	
	//Map of LksValueCode of LksData and LksData
	final private Map<String, LksData>lksValMap;

	//Map of LkuDatas
	final private Map<String, LkuData>lkuValMap;

	//Map of connectors
	final private Map<String, ConnectorLkuData>connectorsMap;
	
	//Map containing info about tested items
	final private Map<String,MyItemInfo> myItemInfoMap;

	//c-tor -> create data needed for test cases
	CreateUpdateDataPortsAPITest(){
		lksValMap = new HashMap<String, LksData>();

		//Type -> Physical
		LksData portType1 = new LksData();
		portType1.setLksId(421L);
		portType1.setLkpValueCode(SystemLookup.PortSubClass.ACTIVE);
		lksValMap.put("Physical", portType1);

		//type -> Logical
		LksData portType2 = new LksData();
		portType2.setLksId(1052L);
		portType2.setLkpValueCode(SystemLookup.PortSubClass.LOGICAL);
		lksValMap.put("Logical", portType2);

		//type -> Virtual
		LksData portType3 = new LksData();
		portType3.setLksId(423L);
		portType3.setLkpValueCode(SystemLookup.PortSubClass.VIRTUAL);
		lksValMap.put("Virtual", portType3);

		//meadia -> Twisted Pair
		LksData media1 = new LksData();
		media1.setLksId(61L);
		media1.setLkpValueCode(7063L);
		lksValMap.put("Twisted Pair", media1);

		lkuValMap = new HashMap<String, LkuData>();
		//Protocol -> Ethernet/IP
		LkuData protocol1 = new LkuData();
		protocol1.setLkuId(1042L);
		protocol1.setLkuValue("Ethernet/IP");
		lkuValMap.put("Ethernet/IP", protocol1);

		LkuData dataRate1 = new LkuData();
		dataRate1.setLkuId(1028L);
		dataRate1.setLkuValue("10G Base-T");
		lkuValMap.put("10G Base-T", dataRate1);

		connectorsMap = new HashMap<String, ConnectorLkuData>();
		ConnectorLkuData connector1 = new ConnectorLkuData();
		connector1.setConnectorName("RJ45");
		connector1.setConnectorId(14L);
		connectorsMap.put("RJ45", connector1);
		
		myItemInfoMap = new HashMap<String, MyItemInfo>();
		
		MyItemInfo myItemInfo1 = new MyItemInfo();
		//model info
		ModelDetails model1 = new ModelDetails();
		model1.setModelDetailId(225L);
		model1.setMounting("Rackable");
		model1.setFormFactor("Fixed");
		myItemInfo1.setModel(model1);
		
		///class info
		LksData classLkp1 = new LksData();
		classLkp1.setLksId(1L);
		classLkp1.setLkpValueCode(SystemLookup.Class.DEVICE);
		myItemInfo1.setClassLookup(classLkp1);
		
		//connected Item info
		DataPortDTO connectedItem = new DataPortDTO();
		connectedItem.setConnectedItemId(9999L);
		connectedItem.setConnectedItemName("ConnectedItem1");
		connectedItem.setConnectedPortId(99999L);
		connectedItem.setConnectedPortName("ConnectedPort1");
		myItemInfo1.setConnectedItem(connectedItem);
		
		myItemInfoMap.put("StandardDevice", myItemInfo1);
		
		//Next item info
		MyItemInfo myItemInfo2 = new MyItemInfo();
		
		//model
		myItemInfo2.setModel(null);
		//class
		myItemInfo2.setClassLookup(classLkp1);
		
		//subclass
		LksData subClass2 = new LksData();
		subClass2.setLksId(106L);
		subClass2.setLkpValueCode(SystemLookup.SubClass.VIRTUAL_MACHINE);
		myItemInfo2.setSubClassLookup(subClass2);
		myItemInfoMap.put("VM", myItemInfo2);
		
	}

	private void printBusinessValidationException( BusinessValidationException be ){
		System.out.println("=== BusinessValidationException:" );
		List<String> errors = be.getValidationErrors();
		for (String err : errors) {
			System.out.println("  err: " + err);
		}
		List<String> warnings = be.getValidationWarnings();
		for( String war : warnings ){
			System.out.println("  war: " + war );
		}

	}
	
	private void printBusinessInformationException ( BusinessInformationException be ) {
		System.out.println("=== BusinessValidationException:" );
		List<String> errors = be.getValidationInformation();
		for (String err : errors) {
			System.out.println("  err: " + err);
		}
	}

	private RequestHistory getRequestHistory(Long itemId){
		RequestHistory history = new RequestHistory();
		history.setStageIdLookup(systemLookupInitTest.getLks(SystemLookup.RequestStage.REQUEST_APPROVED));

		return history;
	}

	private DataPortDTO createDTO( long portId, String portName, String portType,
			String media, String protocol, String dataRate, String connectorName ){

		DataPortDTO dto = new DataPortDTO();
		dto.setPortId(portId);
		dto.setPortName(portName);
		dto.setPortSubClassLksDesc(portType);
		dto.setMediaLksDesc(media);
		dto.setProtocolLkuDesc(protocol);
		dto.setSpeedLkuDesc(dataRate);
		PortConnectorDTO connectorDTO = new PortConnectorDTO();
		connectorDTO.setConnectorName(connectorName);
		dto.setConnector(connectorDTO);
		return dto;
	}


	private DataPort createDataPort ( Item item, DataPortDTO dto ){
		  DataPort dataPort = new DataPort();
		  dataPort.setPortId(dto.getPortId());
		  dataPort.setItem(item);
		  
		  dataPort.setPortName(dto.getPortName());
		  dataPort.setPortSubClassLookup(lksValMap.get(dto.getPortSubClassLksDesc()));
		  dataPort.setMediaId(lksValMap.get(dto.getMediaLksDesc()));
		  dataPort.setProtocolID(lkuValMap.get(dto.getProtocolLkuDesc()));
		  dataPort.setSpeedId(lkuValMap.get(dto.getSpeedLkuDesc()));
		  dataPort.setConnectorLookup(connectorsMap.get(dto.getConnector().getConnectorName()));
	  
		  item.addDataPort(dataPort);
		  return dataPort;
		
	}
	
	private Item createItem( Long itemId, String myItemInfoId ){
		
		Item item = new Item();
		item.setItemId(itemId);
		final ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		itemServiceDetails.setItemAdminUser(getUser(UserInfo.UserAccessLevel.ADMIN));
		item.setItemServiceDetails(itemServiceDetails);
		item.setStatusLookup(systemLookupInitTest.getLks(SystemLookup.ItemStatus.PLANNED));

		MyItemInfo myItemInfo = myItemInfoMap.get(myItemInfoId);
		Assert.assertTrue(myItemInfo != null, "There is no info about ite, add it first!");
		item.setModel(myItemInfo.getModel());
		item.setClassLookup(myItemInfo.getClassLookup());
		if( myItemInfo.getSubClassLookup() != null ){
			item.setSubclassLookup(myItemInfo.getSubClassLookup());
		}
	  
		return item;
	}

	private void mockUpdateCreation( final DataPort port ) throws DataAccessException{	
		DataPortDTO retVal = null;
		jmockContext.checking(new Expectations() {{
			allowing(dataPortDAO).read(with(port.getPortId())); will(returnValue(port));
			allowing(dataPortDAO).loadEvictedPort(with(port.getPortId()));will(returnValue(port));
			allowing(dataPortDAO).update(with(any(DataPort.class)));
			allowing(dataPortDAO).merge(with(any(DataPort.class)));
			allowing(dataPortDAO).mergeOnly(with(any(DataPort.class)));
		}});
	}
	
	private void mockPortCreation( final Item item, final DataPortDTO portDetails, final HashMap<Long, PortInterface> connectedItems ) throws DataAccessException{	

		final List<LksData> portTypeList = new ArrayList<LksData>(){{ add(lksValMap.get(portDetails.getPortSubClassLksDesc()));}};
		final List<LksData> mediaList = new ArrayList<LksData>(){{ add(lksValMap.get(portDetails.getMediaLksDesc()));}};
		final List<LkuData> dataRateList = new ArrayList<LkuData>(){{ add(lkuValMap.get(portDetails.getSpeedLkuDesc()));}};
		final List<LkuData> protocolList = new ArrayList<LkuData>(){{ add(lkuValMap.get(portDetails.getProtocolLkuDesc()));}};
		final List<ConnectorLkuData> connectorList = new ArrayList<ConnectorLkuData>(){{ add(connectorsMap.get(portDetails.getConnector().getConnectorName()));}};
		final List<Long> itemIds = new ArrayList<Long>() {{ add(item.getItemId()); }};
		final List<Long> stages = new ArrayList<Long>(){{
			add(SystemLookup.RequestStage.REQUEST_APPROVED);
			add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
			add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		}};
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).getItem(with(item.getItemId())); will(returnValue(item));
			allowing(itemDAO).read(with(item.getItemId())); will(returnValue(item));
			//allowing(itemDAO).loadItem(with(item.getItemId())); will(returnValue(item));
			allowing(itemDAO).merge(with(any(Item.class)));
			allowing(itemDAO).mergeOnly(with(any(Item.class)));
			allowing(systemLookupFinderDAO).findByLkpValueCode(with(portDetails.getPortSubClassLksValueCode())); will(returnValue(portTypeList));
			allowing(systemLookupFinderDAO).findByLkpValueAndTypeCaseInsensitive(with(portDetails.getPortSubClassLksDesc()), with(SystemLookup.LkpType.PORT_SUBCLASS)); will(returnValue(portTypeList)); 
			allowing(systemLookupFinderDAO).findByLkpValueAndTypeCaseInsensitive(with(portDetails.getMediaLksDesc()), with(SystemLookup.LkpType.MEDIA)); will(returnValue(mediaList));
			allowing(userLookup).findByLkpValueAndTypeCaseInsensitive(with(portDetails.getSpeedLkuDesc()), with(GlobalConstants.speed));will(returnValue(dataRateList));
			allowing(userLookup).findByLkpValueAndTypeCaseInsensitive(with(portDetails.getProtocolLkuDesc()), with(GlobalConstants.protocol));will(returnValue(protocolList));
			allowing(connectorLookupFinderDAO).findByNameCaseInsensitive(with(portDetails.getConnector().getConnectorName()));will(returnValue(connectorList));
			allowing(itemRequestDAO).getRequest(with(itemIds),with(stages),with(itemRequest.getErrors())); will(returnValue(new HashMap<Long,List<Request>>()));
			allowing(itemRequestDAO).getCurrentHistory(with(item.getItemId()));will(returnValue(getRequestHistory(item.getItemId())));
			allowing(dataPortDAO).create(with(any(DataPort.class)));
			allowing(dataPortDAO).merge(with(any(DataPort.class)));
			allowing(dataPortDAO).mergeOnly(with(any(DataPort.class)));
			allowing(dataCircuitDAO).getDestinationItemsForItem(with(item.getItemId())); will(returnValue(connectedItems));

			allowing(dataPortMoveDAO).getMovePortAction(with(any(Long.class)));// will(returnValue(null));
			allowing(dataPortMoveDAO).getWhenMovedItemName(with(any(Long.class)));// will(returnValue("<Unknown>"));
			allowing(dataPortMoveDAO).getMovingItemName(with(any(Long.class)));// will(returnValue("<Unknown>"));
			allowing(dataPortMoveDAO).getMovePortAction(with(any(List.class)));will(returnValue(new HashMap<Long, LksData>()));
			
			oneOf(connectorLookupFinderDAO).findById(with(14L));will(returnValue(connectorList));
			oneOf(userLookup).findById(with(1028L));will(returnValue(dataRateList));
			oneOf(userLookup).findById(with(1042L));will(returnValue(protocolList));
		}});
	}

	private void mockGetFieldsValueUsed() {
		@SuppressWarnings("serial")
		final Map<String, Object> usedFieldValue = new HashMap<String, Object>() {{
			put("used", (new Boolean(Boolean.FALSE)));
		}};
		
		jmockContext.checking(new Expectations() {{
			oneOf(dataPortDAO).getFieldsValue( with(any(Class.class)), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(usedFieldValue) );
		}});

	}
	
	private void mockLoadItem(final Item item) {
		jmockContext.checking(new Expectations() {{
			oneOf(itemDAO).loadItem(with(item.getItemId())); will(returnValue(item));
		}});
	}

	private DataPortDTO createAndValidatePort(final Long itemId, final DataPortDTO portDetails, final UserInfo userInfo){
		DataPortDTO retVal = null;
		try{
			retVal = itemHome.createItemDataPortExtAPI(itemId, portDetails, userInfo);
		}catch(BusinessInformationException be){
			printBusinessInformationException(be);
		}catch(BusinessValidationException be){
			printBusinessValidationException(be);
			Assert.assertTrue(false);
		} catch (DataAccessException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		Assert.assertTrue(retVal != null, "createItemDataPortExtAPI returned null");
		Assert.assertTrue(retVal.getPortName().equals(portDetails.getPortName()), "names do not match");
		Assert.assertTrue(retVal.getPortSubClassLksValueCode().longValue() == lksValMap.get(portDetails.getPortSubClassLksDesc()).getLkpValueCode().longValue(), "port type does not match");
		Assert.assertTrue(retVal.getMediaLksValueCode().longValue() == lksValMap.get(portDetails.getMediaLksDesc()).getLkpValueCode().longValue(), "port media does not match");
		Assert.assertTrue(retVal.getProtocolLkuId().longValue() == lkuValMap.get(portDetails.getProtocolLkuDesc()).getLkuId().longValue(), "protocols do not match");

		Assert.assertTrue(retVal.getSpeedLkuId().longValue() == lkuValMap.get(portDetails.getSpeedLkuDesc()).getLkuId().longValue(), "speeds do not match");
		return retVal;
	}
	
	
	private DataPortDTO updateAndValidatePort(final Long itemId, final Long portId, final DataPortDTO portDetails, final UserInfo userInfo){
		DataPortDTO retVal = null;
		try{
			retVal = itemHome.updateItemDataPortExtAPI(itemId, portId, portDetails, userInfo);
		}catch(BusinessValidationException be){
			printBusinessValidationException(be);
			Assert.assertTrue(false);
		} catch (DataAccessException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		Assert.assertTrue(retVal != null, "updateItemDataPortExtAPI returned null");
		Assert.assertTrue(retVal.getPortName().equals(portDetails.getPortName()), "names do not match");
		Assert.assertTrue(retVal.getPortSubClassLksValueCode().longValue() == lksValMap.get(portDetails.getPortSubClassLksDesc()).getLkpValueCode().longValue(), "port type does not match");
		Assert.assertTrue(retVal.getProtocolLkuId().longValue() == lkuValMap.get(portDetails.getProtocolLkuDesc()).getLkuId().longValue(), "protocols do not match");

		Assert.assertTrue(retVal.getSpeedLkuId().longValue() == lkuValMap.get(portDetails.getSpeedLkuDesc()).getLkuId().longValue(), "speeds do not match");
		return retVal;
	}
	

	private void createPortAndValidateMissingField(final Long itemId, final DataPortDTO portDetails, final UserInfo userInfo, String patternError){
		DataPortDTO retVal = null;
		try{
			retVal = itemHome.createItemDataPortExtAPI(itemId, portDetails, userInfo);
			Assert.assertTrue(retVal == null, "createPortAndValidateMissingField is suppoed to hit BusinessValidationException");
		}catch(BusinessInformationException be){
			printBusinessInformationException(be);
		}catch(BusinessValidationException be){
			printBusinessValidationException(be);
			String key = "PortValidator.dataPortFieldRequired";
			Assert.assertTrue(be.getErrors().containsKey(key));
			String value = be.getErrors().get(key);
			Assert.assertTrue(value.matches(patternError));
		} catch (DataAccessException e) {
			e.printStackTrace();
			Assert.assertTrue(false, "createPortAndValidateMissingField is suppoed to hit BusinessValidationException");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.assertTrue(false, "createPortAndValidateMissingField is suppoed to hit BusinessValidationException");
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.assertTrue(false, "createPortAndValidateMissingField is suppoed to hit BusinessValidationException");
		}


	}
	
	// TEST cases
	//@Test
	public void testCreateGoodPhysicalPort() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		
		DataPortDTO dto = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		mockPortCreation( item, dto, null);
		mockLoadItem (item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		createAndValidatePort( itemId, dto, userInfo );
	}
	
	
	@Test
	public void testUpdateGoodPhysicalPort() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		Long portId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";

		DataPortDTO dto = createDTO( portId, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		final Item item = createItem( itemId, myItemInfoId );
		DataPort dataPort = createDataPort ( item, dto );
		
		Assert.assertTrue(item != null, "Failed to create Item");

		mockPortCreation( item, dto, null);
		mockLoadItem (item);
		mockUpdateCreation( dataPort );
		mockGetFieldsValueUsed();
		
		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		
		DataPortDTO dto2 = createDTO( portId, "portBrh2", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO retVal = updateAndValidatePort( itemId, portId, dto2, userInfo );
		long retPortId = retVal.getPortId();
		String portName = retVal.getPortName();
		
		Assert.assertTrue( portId == retPortId, "Set portId and returned portId do not match");
		Assert.assertTrue( dto2.getPortName().equals(retVal.getPortName()), "Port names do not match");	
	}
	
	
	@Test
	public void testUpdateGoodVirtualPort() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		Long portId = unitTestIdGenerator.nextId();
		String myItemInfoId = "VM";
		
		DataPortDTO dto = createDTO( portId, "portBrh1", "Virtual", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		final Item item = createItem( itemId, myItemInfoId );
		DataPort dataPort = createDataPort ( item, dto );
		
		Assert.assertTrue(item != null, "Failed to create Item");

		mockPortCreation( item, dto, null);
		mockLoadItem(item);
		mockGetFieldsValueUsed();
		mockUpdateCreation( dataPort );
		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		
		DataPortDTO dto2 = createDTO( portId, "portBrh2", "Virtual", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO retVal = updateAndValidatePort( itemId, portId, dto2, userInfo );
		long retPortId = retVal.getPortId();
		
		Assert.assertTrue( portId == retPortId, "Set portId and returned portId do not match");
			
	}
	
	@Test
	public void testCreatePhysicalPortMissingPortName() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		
		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, null, "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Port Name\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );

	}
	
	@Test
	public void testCreatePhysicalPortMissingPortType() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";

		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, "portBrh1", null, "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);
		
		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Port Type\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );
	}
	
	@Test
	public void testCreatePhysicalPortMissingMedia() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";

		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, "portBrh1", "Physical", null, "Ethernet/IP", "10G Base-T", "RJ45");

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Media\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );
	}
	
	@Test
	public void testCreatePhysicalPortMissingProtocol() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		
		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", null, "10G Base-T", "RJ45");

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Protocol\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );
	}
	
	@Test
	public void testCreatePhysicalPortMissingDataRate() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		
		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", null, "RJ45");

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Data rate\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );
	}
	
	@Test
	public void testCreatePhysicalPortMissingConnector() throws Throwable{
		Long itemId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		
		DataPortDTO goodDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO badDTO = createDTO( -1L, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", null);

		final Item item = createItem( itemId, myItemInfoId );
		Assert.assertTrue(item != null, "Failed to create Item");

		//Mocking always done with good one
		mockPortCreation( item, goodDTO, null);
		mockLoadItem(item);

		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		String patternError = "(.*)\'Connector\'(.*)";
		createPortAndValidateMissingField( itemId, badDTO, userInfo, patternError );
	}
	
	
	// TEST cases
	@Test
	public void testCheckConnectedItem() throws Throwable{
		final Long itemId = unitTestIdGenerator.nextId();
		final Long portId = unitTestIdGenerator.nextId();
		String myItemInfoId = "StandardDevice";
		DataPortDTO dto = createDTO( portId, "portBrh1", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		final Item item = createItem( itemId, myItemInfoId );
		DataPort dataPort = createDataPort ( item, dto );
		
		Assert.assertTrue(item != null, "Failed to create Item");
		
		MyItemInfo myItemInfo = myItemInfoMap.get(myItemInfoId);
		HashMap<Long, PortInterface> connectedItems = new HashMap<Long, PortInterface>();
		DataPortDTO connectedItem = myItemInfo.getConnectedItem();
		connectedItems.put(portId, connectedItem);

		mockPortCreation( item, dto, connectedItems);
		mockLoadItem(item);
		mockUpdateCreation( dataPort );
		mockGetFieldsValueUsed();
		
		UserInfo userInfo = getUserInfo(UserInfo.UserAccessLevel.ADMIN);
		
		DataPortDTO dto2 = createDTO( portId, "portBrh2", "Physical", "Twisted Pair", "Ethernet/IP", "10G Base-T", "RJ45");
		DataPortDTO retVal = updateAndValidatePort( itemId, portId, dto2, userInfo );
		long retPortId = retVal.getPortId();
		String portName = retVal.getPortName();
		
		Assert.assertTrue( portId == retPortId, "Set portId and returned portId do not match");
		Assert.assertTrue( dto2.getPortName().equals(retVal.getPortName()), "Port names do not match");
		
		//check connected Item info
		Assert.assertTrue( retVal.getConnectedItemId() == connectedItem.getConnectedItemId(), "Connected Item ids do not match");
		Assert.assertTrue( retVal.getConnectedPortId() == connectedItem.getConnectedPortId(), "Connected Port ids do not match");
		Assert.assertTrue( retVal.getConnectedItemName().equals(connectedItem.getConnectedItemName()), "Connected Item names do not match");
		Assert.assertTrue( retVal.getConnectedPortName().equals(connectedItem.getConnectedPortName()), "Connected Port names do not match");
	
	}
}
