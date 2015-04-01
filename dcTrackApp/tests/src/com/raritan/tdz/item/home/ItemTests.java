package com.raritan.tdz.item.home;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemViewData;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.SystemLookupDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.dto.BreakerDTO;
import com.raritan.tdz.item.home.itemObject.ItemDomainFactory;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * Tests for the Item home layer. Relies on data in the demo database.
 * @author Andrew Cohen
 */
public class ItemTests extends TestBase {

	// Test UPS Bank items
	private static final long TEST_UPS_BANK_A_ID = 1L; // Bank A
	private static final long TEST_UPS_BANK_B_ID = 1L; // Bank B
	private static final long TEST_UPS_BANK_A2_ID = 3004L; // Bank A in Site B
	
	protected ItemService itemService;
	protected ItemDomainAdaptor itemAdaptor;
	private CabinetHome cabinetHome;
	private ItemDomainFactory itemDomainFactory;
	private ItemObjectFactory itemObjectTemplateFactory;
	private ItemDAO itemDao;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemService = (ItemService)ctx.getBean("itemService");
		itemAdaptor = (ItemDomainAdaptor)ctx.getBean("itemDomainAdaptor");
		cabinetHome = (CabinetHome)ctx.getBean("cabinetHome");
		itemDomainFactory = (ItemDomainFactory)ctx.getBean("itemDomainFactory");
		itemObjectTemplateFactory = (ItemObjectFactory)ctx.getBean("itemObjectTemplateFactory");
		itemDao = (ItemDAO)ctx.getBean("itemDAO");
	}
	
	@Test
	public final void testLoadVM() throws Throwable {
		Item item = itemDao.loadItem(4961L);
		assertTrue(item != null);
	}
	
	@Test
	public final void testUPSBankLinkedUPSCount() throws Throwable {
		testExpectedLinkedUPSCount(TEST_UPS_BANK_A_ID, 1); // Expecting linked 1 UPS - UPS-A 
		testExpectedLinkedUPSCount(TEST_UPS_BANK_B_ID, 1); // Expecting linked 1 UPS - UPS-B
		testExpectedLinkedUPSCount(TEST_UPS_BANK_A2_ID, 3); // Expecting 3 linked UPS items
	}
	
	//@Test
	public final void testFetchPDUItem() throws Throwable {
		final int pduPiqId = 1; // PDU from demo db
		Item pduItem = itemHome.getPDUItem( pduPiqId );
		assertNotNull( pduItem );
		assertEquals( "MY PX", pduItem.getItemName());
	}
	
	@Test
	public final void getAllItemClassLookup() throws Throwable{
		List<SystemLookupDTO> allClassLookup = itemHome.getAllItemClassLookup();
		
		
		assertNotNull(allClassLookup);
		assertTrue(allClassLookup.size() > 0);
	}

	@Test
	public final void testGetChildrenCountForParent() throws Throwable {
		List<Long> count = itemFinderDAO.findChildCountFromParentId(7L);
		assertTrue(count.size() > 0);
		System.out.println("Hi there");
	}
	
	@Test
	public final void getItemStatusSystemLookup(){
		List<SystemLookupDTO> statusLookup = itemHome.getSystemLookup(SystemLookup.LkpType.ITEM_STATUS);
		
		assertNotNull(statusLookup);
		assertTrue(statusLookup.size() > 0);
	}
	
	//@Test
	private final void deleteItem( long itemId) throws Throwable {

		Item item = itemHome.viewItemEagerById( itemId );
		assertNotNull( item );
		
		itemHome.deleteItem( itemId, false, null );
		
		Session session = sf.openSession();
		item = (Item)session.get(Item.class, itemId);
		session.flush();
		session.close();
		assertNull( item );
	}
	
	//@Test
	public final void getCabinetSensorPort() throws Throwable {
		final long cabinetItemId = 413L;
		
		CabinetItem cabinet = (CabinetItem)session.get(CabinetItem.class, cabinetItemId);
		//SensorPort sensorPort = cabinet.getSensorPort();
		
		Query q = session.createQuery("from SensorPort where cabinet_item_id = :cabinetItemId");
		q.setLong("cabinetItemId", cabinet.getItemId());
		SensorPort sensorPort = (SensorPort)q.uniqueResult();
		
		assertNotNull( sensorPort );
	}
	
	//@Test
	public final void getItemRulesEngine344() throws Throwable {
		String xmlString = itemHome.getItem(new Long(344), "uiView[@uiId='itemView']/", null);

		assertNotNull( xmlString );
	}
	
	//@Test
	public final void getItemRulesEngine444() throws Throwable {
		String xmlString = itemHome.getItem(new Long(444), "uiView[@uiId='itemView']/", null);
		assertNotNull( xmlString );
	}
	
	//@Test
	public final void getItemRulesEngine4814() throws Throwable {
		String xmlString = itemHome.getItem(new Long(4814), "uiView[@uiId='itemView']/", null);
		assertNotNull( xmlString );
	}

	//@Test
	public final void getItemRulesEngine4520() throws Throwable {
		String xmlString = itemHome.getItem(new Long(4520), "uiView[@uiId='itemView']/", null);
		assertNotNull( xmlString );
	}

	//@Test
	public final void getItemRulesEngine4520UiView() throws Throwable {
		Map<String, UiComponentDTO> componentDTOs =  itemHome.getItem(new Long(4520), null);
		assertNotNull( componentDTOs );
		assertTrue( componentDTOs.size() > 0 );
	}
	
	@Test
	public final void getCabinetForLocation() throws Throwable {
		List<ValueIdDTO> locations = itemHome.getAllLocations();
		assertTrue(locations.size() > 0);
		
		List<ValueIdDTO> cabinets = cabinetHome.getAllCabinets((Long)locations.get(0).getData());
		assertTrue(cabinets.size() > 0);
	}
	
	@Test
	public final void getAllLocations() throws Throwable {
		List<ValueIdDTO> locations = itemHome.getAllLocations();
		assertTrue(locations.size() > 0);
	}
	
	//@Test
	public final void getAvailableUPositons() throws Throwable {
		
		int ruHeight = 5;
		
		List<ValueIdDTO> locations = itemHome.getAllLocations();
		assertTrue(locations.size() > 0);
		
		
		List<ValueIdDTO> cabinets = itemService.getAllCabinets((Long)locations.get(locations.size() - 1).getData());
		
		assertTrue(cabinets.size() > 0);
		
		List<ValueIdDTO> availableUPos = itemService.getAvailableUPositions((Long)cabinets.get(0).getData(), ruHeight, -1, GlobalConstants.RAILS_BOTH_CODE, null);
		
		assertTrue(availableUPos.size() > 0);
		
		//Get the layout of the cabinet
		Criteria criteria = session.createCriteria(CabinetItem.class);
		criteria.setProjection(Projections.property("layoutHorizFront"));
		criteria.add(Restrictions.eq("itemId", cabinets.get(0).getData()));
		
		String layout = (String) criteria.uniqueResult();
		
		StringBuffer layoutBuf = new StringBuffer(layout);
		List<Integer> occupiedPositions = new ArrayList<Integer>();
		
		for (int i = 0; i < layout.length(); i++){
			if (layoutBuf.charAt(i) == '1'){
				occupiedPositions.add(i+1);
			}
		}
		
		//Check to see if we have available u positions that fit the above given RUs. If not something is wrong in the alg:-)
		for (int i = 0; i < availableUPos.size(); i++){
			int bottomUPos = Integer.parseInt(availableUPos.get(i).getLabel());
			int topUPos = bottomUPos + (ruHeight - 1);
			
			assertFalse(occupiedPositions.contains(new Integer(topUPos)), "The UPosition is in conflict: " + availableUPos.get(i).getLabel());
		}
	}
	
	@Test
	public final void getOrientation() throws Throwable {
		List<SystemLookupDTO> orientationLookup = itemService.getSystemLookup(SystemLookup.LkpType.ORIENTATION);
		
		assertTrue(orientationLookup.size() > 0);
	}
	
	@Test
	public final void getStatusLookupForNewState() throws Throwable{
		List<SystemLookupDTO> statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), null); // VM
		assertTrue(statusLookup.size() > 0);
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 981L); // Device Blade
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 983L); // Device Chassis
		assertTrue(statusLookup.size() > 0);
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 2L); // Device Rackable Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 100L); // Device Free-Standing Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 46L); // N/w Blade
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 87L); // N/w Rackable Chassis
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 18881L); // N/w FS Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 33L); // N/w Rackable Fixed
		assertTrue(statusLookup.size() > 0);

		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 27048L); // N/w Non-Rackable Fixed
		assertTrue(statusLookup.size() > 0);*/

		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 27048L); // N/w Zero-U Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 54L); // DP Rackable Fixed
		assertTrue(statusLookup.size() > 0);

		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 54L); // DP Non-Rackable Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 54L); // DP zerou Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 1278L); // Power Outlet Busway Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 1434L); // Power Outlet Non-Rackable Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 61L); // RPDU zerou Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 214L); // RPDU rackable Fixed
		assertTrue(statusLookup.size() > 0);

		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 214L); // RPDU non-rackable Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 24L); // Cabinet FS 4-Post Enclosure
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 9543L); // Probe Rackable Fixed
		assertTrue(statusLookup.size() > 0);

		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 9543L); // Probe zerou Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		/*statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 9543L); // Probe non-rackable Fixed
		assertTrue(statusLookup.size() > 0);*/
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 58L); // FPDU FS Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 25892L); // FPDU FS Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 1304L); // UPS FS Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 1255L); // CRAC FS Fixed
		assertTrue(statusLookup.size() > 0);

		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 52L); // Passive Rackable Fixed
		assertTrue(statusLookup.size() > 0);
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 3059L); // Passive Rackable Blanking plate
		assertTrue(statusLookup.size() > 0);
		
		statusLookup = itemService.getStatusLookupForCurrentState(new Long(-1), 6912L); // Passive Rackable Shelf
		assertTrue(statusLookup.size() > 0);

	}
	
	@Test
	public final void getCabinetPositionInRows() throws Throwable {
		List<Integer> positionInRows = cabinetHome.getCabinetPositionInRows(new Long(1), "1");
		assertTrue(positionInRows.size() > 0);
	}
	
	/*
	 * Test case not valid any more because new convert is not creating item
	 * It just fills out object with values from ValueIdDTO
	//@Test
	public final void testConvertItemDomainAdaptor() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData("Hello");
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.INSTALLED);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(1));
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(10));
		
		valueIdDTOList.add(dto);
		
		Item item = (Item) itemAdaptor.convert(null, valueIdDTOList);
		
		assertNotNull(item);
		assertEquals(item.getItemName(), valueIdDTOList.get(0).getData().toString());
	}
	*/
	
	//@Test
	public final void testRackableItemSave() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();
		
		//Hardware Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(20)); //HP
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(1)); //Proliant DL320 G5p
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData("12345000"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData("12345000"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData("12345000"); 
		valueIdDTOList.add(dto);
		
		//Identity Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData("Test Item");
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData("HELLO THERE!"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbType");
		dto.setData(new Long(1)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbFunction");
		dto.setData(new Long(970)); 
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdmin");
		dto.setData(new Long(20)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdminTeam");
		dto.setData(new Long(555)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCustomer");
		dto.setData(new Long(566)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.INSTALLED);
		valueIdDTOList.add(dto);
		

		
		//Placement Panel Data
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(new Long(1));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("radioRailsUsed");
		dto.setData(new Long(8003));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCabinet");
		dto.setData(new Long(10));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbUPosition");
		dto.setData("1");
		valueIdDTOList.add(dto);


		dto = new ValueIdDTO();
		dto.setLabel("cmbOrientation");
		dto.setData(new Long(7081));
		valueIdDTOList.add(dto);

		//other panels
		dto = new ValueIdDTO();
		dto.setLabel("tiPONumber");
		dto.setData("PO 1234");
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbProjectNumber");
		dto.setData("Project X");
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("tiRAM");
		dto.setData(new Integer(4));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("tiCpuType");
		dto.setData("Intel Dual Core 1.8 GHz");
		valueIdDTOList.add(dto);

		Long itemId = 60L; //NJESX04
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		
		assertNotNull(componentDTOMap);
	}
	
	//@Test
	public final void testRackableItemSaveInStorage() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();
		
		//Hardware Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(20)); //HP
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(1)); //Proliant DL320 G5p
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		//Identity Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData(null);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData("HELLO THERE!"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbType");
		dto.setData(new Long(1)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbFunction");
		dto.setData(new Long(970)); 
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdmin");
		dto.setData(new Long(20)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdminTeam");
		dto.setData(new Long(555)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCustomer");
		dto.setData(new Long(566)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.IN_STORAGE);
		valueIdDTOList.add(dto);
		

		
		//Placement Panel Data
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(new Long(1));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("radioRailsUsed");
		dto.setData(new Long(8003));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCabinet");
		dto.setData(new Long(10));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbUPosition");
		dto.setData("1");
		valueIdDTOList.add(dto);


		dto = new ValueIdDTO();
		dto.setLabel("cmbOrientation");
		dto.setData(new Long(7081));
		valueIdDTOList.add(dto);
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(new Long(4948), valueIdDTOList, getTestAdminUser());
		
		assertNotNull(componentDTOMap);
	}


	//@Test
	public final void testChassisItemSaveInStorage() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();
		
		//Hardware Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(17)); //CISCO
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(45)); //Catalyst
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData("12345"); 
		valueIdDTOList.add(dto);
		
		//Identity Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData("Cisco Chassis 01");
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData("HELLO THERE!"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbType");
		dto.setData(new Long(1)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbFunction");
		dto.setData(new Long(970)); 
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdmin");
		dto.setData(new Long(20)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdminTeam");
		dto.setData(new Long(555)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCustomer");
		dto.setData(new Long(566)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.IN_STORAGE);
		valueIdDTOList.add(dto);
		
		//Placement Panel Data
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(new Long(1));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("radioRailsUsed");
		dto.setData(new Long(8003));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCabinet");
		dto.setData(new Long(10));
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbUPosition");
		dto.setData("1");
		valueIdDTOList.add(dto);


		dto = new ValueIdDTO();
		dto.setLabel("cmbOrientation");
		dto.setData(new Long(7081));
		valueIdDTOList.add(dto);

		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(null, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
		} catch (BusinessValidationException e){
			e.printValidationErrors();
		}
	}

	
	//@Test
	public final void testCabinetItemSaveInStorage() throws Throwable {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		ValueIdDTO dto = new ValueIdDTO();
		
		//Hardware Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("cmbMake");
		dto.setData(new Long(12)); //HP
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(new Long(24)); //Proliant DL320 G5p
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData("12345678"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData("12345678"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData("12345678"); 
		valueIdDTOList.add(dto);
		
		//Identity Panel Data
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData(null);
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData("HELLO THERE!"); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbType");
		dto.setData(new Long(1)); 
		valueIdDTOList.add(dto);

		
		dto = new ValueIdDTO();
		dto.setLabel("cmbFunction");
		dto.setData(new Long(970)); 
		valueIdDTOList.add(dto);

		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdmin");
		dto.setData(new Long(20)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbSystemAdminTeam");
		dto.setData(new Long(555)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbCustomer");
		dto.setData(new Long(566)); 
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbStatus");
		dto.setData(SystemLookup.ItemStatus.IN_STORAGE);
		valueIdDTOList.add(dto);
		
		//Placement Panel Data
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbLocation");
		dto.setData(new Long(1));
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbRowLabel");
		dto.setData("2");
		valueIdDTOList.add(dto);

		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(0L, valueIdDTOList, getTestAdminUser());
		
		assertNotNull(componentDTOMap);
	}

	//@Test
	public final void testBlankArraySave() throws Throwable {
		//If this does not throw any exception we are good!
		Map<String, UiComponentDTO> componentDTOMap = itemHome.saveItem(0L,null, getTestAdminUser());
	}
	
	private void getAllAvailableCabinetForZeroUModelTest(long locationId, long zeroUModelId, long depth_lkp_value, long side_lkp_value, long zeroUId) throws Throwable {
		System.out.println("get available u pos for location " + locationId + 
				" zero u model " + zeroUModelId + 
				" depth " + ((depth_lkp_value == SystemLookup.ZeroUDepth.FRONT) ? "Front" : "Rear") +
				" side " + ((side_lkp_value == SystemLookup.RailsUsed.LEFT_REAR) ? " left": "right") + 
				" exception zero u item " + zeroUId);
		List<ValueIdDTO> dtos = itemService.getAllAvailableCabinetForZeroUModel(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		for (ValueIdDTO dto: dtos) {
			System.out.println("id = " + dto.getData() + " value = " + dto.getLabel());
		}
	}
	
	@Test
	public final void getAllAvailableCabinetForZeroUModelTest() throws Throwable {
		/*-----------------------------------------------------------------------------------------*/
		long locationId = 1;
		long zeroUModelId = 1252; // 8 RU (3080: , 29: 1520, 31: 81)
		long zeroUId = -1;
		long depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		long side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 4913;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		/*-----------------------------------------------------------------------------------------*/
		zeroUModelId = 1252; // 8 RU (3080: , 29: 1520, 31: 81)
		zeroUId = 1081;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 4913;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		/*-----------------------------------------------------------------------------------------*/
		
		/*-----------------------------------------------------------------------------------------*/
		locationId = 1;
		zeroUModelId = 3080; 
		zeroUId = -1;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 4913;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		/*-----------------------------------------------------------------------------------------*/
		zeroUModelId = 3080; 
		zeroUId = 1081;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 1081;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		/*-----------------------------------------------------------------------------------------*/

		
		
		/*-----------------------------------------------------------------------------------------*/
		locationId = 1;
		zeroUModelId = 1520; 
		zeroUId = -1;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 1081;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		/*-----------------------------------------------------------------------------------------*/
		zeroUModelId = 1520; 
		zeroUId = 1029;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 1006;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		/*-----------------------------------------------------------------------------------------*/

		
		/*-----------------------------------------------------------------------------------------*/
		locationId = 1;
		zeroUModelId = 81; 
		zeroUId = -1;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;

		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		zeroUId = 1029;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);
		
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		this.getAllAvailableCabinetForZeroUModelTest(locationId, zeroUModelId, depth_lkp_value, side_lkp_value, zeroUId);

		/*-----------------------------------------------------------------------------------------*/

	}
	
	/* depth_lkp_value: 271: zerou-front, 272: zerou-back 
	 * side_lkp_value: 8004: left on cabinet rear, 8005: right on cabinet rear 
	 * model_id: get the ru height from the model_id */
	//@Test
	public final void getAvailableZeroUPositionsTest() throws Throwable {
		Long cabinetId = 4899L;
		Long depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		Long side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		int itemRUHeight = 3;
		Collection<Long> availablePos = itemHome.getAvailableZeroUPositions(cabinetId, 
				depth_lkp_value, side_lkp_value, itemRUHeight, -1L);
		assertNotNull(availablePos != null);
		
		cabinetId = 4899L;
		depth_lkp_value = SystemLookup.ZeroUDepth.FRONT;
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		itemRUHeight = 3;
		availablePos = itemHome.getAvailableZeroUPositions(cabinetId, 
				depth_lkp_value, side_lkp_value, itemRUHeight, -1L);
		assertNotNull(availablePos != null);
		
		cabinetId = 4899L;
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.LEFT_REAR;
		itemRUHeight = 3;
		availablePos = itemHome.getAvailableZeroUPositions(cabinetId, 
				depth_lkp_value, side_lkp_value, itemRUHeight, -1L);
		assertNotNull(availablePos != null);

		cabinetId = 4899L;
		depth_lkp_value = SystemLookup.ZeroUDepth.REAR;
		side_lkp_value = SystemLookup.RailsUsed.RIGHT_REAR;
		itemRUHeight = 3;
		availablePos = itemHome.getAvailableZeroUPositions(cabinetId, 
				depth_lkp_value, side_lkp_value, itemRUHeight, -1L);
		assertNotNull(availablePos != null);
	}
	
	//@Test
	public final void getItemsDetailInCabinet() throws Throwable {
		Long cabinetId = (long) 3;
		
		Collection<Long> test = itemHome.getAvailableUPositions(cabinetId,1,-1, GlobalConstants.RAILS_FRONT_CODE, null);
		test = itemHome.getAvailableUPositions(cabinetId,1,-1, GlobalConstants.RAILS_REAR_CODE, null);
		test = itemHome.getAvailableUPositions(cabinetId,1,-1, GlobalConstants.RAILS_BOTH_CODE, null);
		test = itemHome.getAvailableUPositions(cabinetId,1,-1, -1, null);
		
		Collection<Long> itemInfo = itemHome.getAvailableUPositions(cabinetId, 1, -1, GlobalConstants.RAILS_REAR_CODE, null);
		itemInfo = itemHome.getAvailableUPositions(cabinetId, 1, -1, GlobalConstants.RAILS_FRONT_CODE, null);
		itemInfo = itemHome.getAvailableUPositions(cabinetId, 1, -1, GlobalConstants.RAILS_BOTH_CODE, null);
		itemInfo = itemHome.getAvailableUPositions(cabinetId, 1, -1, GlobalConstants.RAILS_BOTH_CODE, null);
		itemInfo = itemHome.getAvailableUPositions(cabinetId, 1, -1, (long) -1, null);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.size() > 0);
		assertTrue(test.size() > 0);
	}
	
	/**
	 * Unit test for viewing an item's details.
	 * @throws Throwable
	 */
	//@Test
	public final void viewItemDetails() throws Throwable {
		long itemId = 782; // NJAD02 (Device)
		
		// Assert item details exist
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, FlexUserSessionContext.getUser() );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// Assert item name matches what was returned from the viewItems call
		ItemViewData view = itemHome.viewItems( itemId );
		assertNotNull( view );
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		assertEquals( view.getItemName(), itemNameField.getUiValueIdField().getValue() );
	}

	//@Test
	public final void createItItem() throws Throwable{
		Item item = createTestItem("ItItem101XX", 1200, null);
		// This item is deleted during teardown in TestBase.java 
		assertNotNull( item );
	}
	
	// @Test
	public void getBladesDetailsInChassisTest() throws Throwable {
		Map<Long, String> availableSlots = null;
/*		long chassisId = 3L;
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Full", SystemLookup.ChassisFace.FRONT, 1);
		System.out.println("available slot for FULL" + availableSlots.toString());
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Full", SystemLookup.ChassisFace.FRONT, -1);
		System.out.println("available slot for FULL" + availableSlots.toString());
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Full-Double", SystemLookup.ChassisFace.FRONT, -1);
		System.out.println("available slot for FULL DOUBLE" + availableSlots.toString());
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Half", SystemLookup.ChassisFace.FRONT, -1);
		System.out.println("available slot for HALF" + availableSlots.toString());
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Half-Double", SystemLookup.ChassisFace.FRONT, -1);
		System.out.println("available slot for HALF DOUBLE" + availableSlots.toString());
		availableSlots = itemHome.getAvailableSlotPositions(chassisId, "Quarter", SystemLookup.ChassisFace.FRONT, -1);
		System.out.println("available slot for QUARTER" + availableSlots.toString());
*/
/*		availableSlots = itemService.getAvailableChassisSlotPositionsForBlade(340L, "Full", 20501L, -1L);
		System.out.println("available slot for FULL in chassis 340 = " + availableSlots.toString());

		availableSlots = itemService.getAvailableChassisSlotPositionsForBlade(340L, "Full", 20501L, 15L);
		System.out.println("available slot for FULL in chassis 340 = " + availableSlots.toString());

		availableSlots = itemService.getAvailableChassisSlotPositionsForBlade(304L, "Full", 20501L, -1L);
		System.out.println("available slot for FULL in chassis 340 = " + availableSlots.toString());

		availableSlots = itemService.getAvailableChassisSlotPositionsForBlade(304L, "Full", 20501L, 15L);
		System.out.println("available slot for FULL in chassis 340 = " + availableSlots.toString());


		assertNotNull(availableSlots);
		System.out.println(availableSlots.toString());*/
	}
	
	@Test
	public final void isEnoughLicenseAvailable() {
		Boolean licenseAvailable = itemHome.isLicenseAvailable(1);
		assertTrue(licenseAvailable, "License is not available");
	}

	@Test
	public final void licenseAvailablityForNewItemSave() throws Throwable {

		Long itemId = 0L;
		String serial = "1234567";
		String assetTag = "1234567";
		String eAssetTag = "1234567";
		String uPosition = "12";
		String tiName = "LicenceTestName";
		long status = SystemLookup.ItemStatus.PLANNED;
		List<ValueIdDTO> valueIdDTOList = getValueIdDTOListForNewItem(tiName, serial, assetTag, eAssetTag, uPosition, status, false); 

		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem( 0L, valueIdDTOList, getTestAdminUser());
			// delete newly created item after test is complete.
			Long id = getItemIdFromUiComponentDtoMap(componentDTOMap);
			if (id != null)	itemHome.deleteItem(id, false, null);
			assertNotNull(componentDTOMap);
		} catch (BusinessValidationException e){
			e.printValidationErrors();
			fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public final void testCustomFields() {
		final long itemId = 63; // NJESX12 (Device) has three custom fields in demo DB
		
		// Retrieve custom fields
		Session session = sf.openSession();
		Item item = (Item)session.get(Item.class, itemId);
		Set<CustomItemDetails> itemFields = item.getCustomFields();
		assertNotNull( itemFields );
		int size = itemFields.size();
		assertTrue(size > 0);
		session.flush();
		session.close();
		
		// Add a new custom field
		CustomItemDetails result_1 = itemFields.iterator().next();
		CustomItemDetails f = new CustomItemDetails();
		f.setItem( item );
		f.setAttrValue("Test Value");
		f.setCustomAttrNameLookup( result_1.getCustomAttrNameLookup() );
		f.setCustomDataTypeLookup( result_1.getCustomDataTypeLookup() );
		session = sf.openSession();
		session.save( f );
		item.addCustomField( f );
		session.flush();
		session.close();
		
		session = sf.openSession();
		item = (Item)session.get(Item.class, itemId);
		session.update( item );
		session.flush();
		session.close();
		
		// Check save
		session = sf.openSession();
		item = (Item)session.get(Item.class, itemId);
		itemFields = item.getCustomFields();
		assertNotNull( itemFields );
		assertEquals(size + 1, itemFields.size()); // should be an extra field
		session.flush();
		session.close();
		
		
		// Delete custom field and check
		session = sf.openSession();
		f = (CustomItemDetails)session.get(CustomItemDetails.class, f.getCustomItemDetailId());
		session.delete( f );
		session.flush();
		session.close();
		
		session = sf.openSession();
		item = (Item)session.get(Item.class, itemId);
		session.flush();
		itemFields = item.getCustomFields();
		assertNotNull( itemFields );
		assertEquals(size, itemFields.size()); // original size
		session.close();
	}
	
	@Test
	public void testUpdateHardwareInformation() throws ClassNotFoundException, Throwable{
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		
		final long itemId = 63; // NJESX12 (Device) has three custom fields in demo DB
		Session session = sf.getCurrentSession();
		Item item = (Item)session.get(Item.class, itemId);
		
		//We need to fill the ValueIdDTO with what we read from the item
		Random rand = new Random();
		Long randNumber = rand.nextLong();
		
		ValueIdDTO dto;
		
		dto = new ValueIdDTO();
		dto.setLabel("cmbModel");
		dto.setData(item.getModel().getModelDetailId());
		valueIdDTOList.add(dto);
		
		dto = new ValueIdDTO();
		dto.setLabel("tiName");
		dto.setData(item.getItemName());
		valueIdDTOList.add(dto);
		
		
		dto = new ValueIdDTO();
		dto.setLabel("tiAlias");
		dto.setData(item.getItemAlias());
		valueIdDTOList.add(dto);
		
		Long serialNumber = 123450 + randNumber;
		dto = new ValueIdDTO();
		dto.setLabel("tiSerialNumber");
		dto.setData(serialNumber.toString()); 
		valueIdDTOList.add(dto);
		
		Long assetTag = 123450 + randNumber;
		dto = new ValueIdDTO();
		dto.setLabel("tiAssetTag");
		dto.setData(assetTag.toString()); 
		valueIdDTOList.add(dto);
		
		Long eAssetTag = 123450 + randNumber;
		dto = new ValueIdDTO();
		dto.setLabel("tieAssetTag");
		dto.setData(eAssetTag.toString()); 
		valueIdDTOList.add(dto);
		
		
		try {
			Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem( itemId, valueIdDTOList, getTestAdminUser());
			assertNotNull(componentDTOMap);
			
			UiComponentDTO serialNumberDTO = componentDTOMap.get("tiSerialNumber");
			assertEquals(serialNumber.toString(), serialNumberDTO.getUiValueIdField().getValue());
			
			UiComponentDTO assetTagDTO = componentDTOMap.get("tiAssetTag");
			assertEquals(assetTag.toString(), assetTagDTO.getUiValueIdField().getValue());
			
			UiComponentDTO eAssetTagDTO = componentDTOMap.get("tieAssetTag");
			assertEquals(eAssetTag.toString(), eAssetTagDTO.getUiValueIdField().getValue());
			
		} catch (BusinessValidationException e){
			e.printValidationErrors();
		}
	}
	
	@Test
	public final void saveCustomFieldsForNewItem() throws Throwable {
		Long itemId = 0L;
		String serial = "123456";
		String assetTag = "123456";
		String eAssetTag = "123456";
		String uPosition = "11";
		String tiName = "TestName5";
		long status = SystemLookup.ItemStatus.PLANNED;
		Map<String,UiComponentDTO> componentDTOMap = null;
		List<ValueIdDTO> valueIdDTOList = getValueIdDTOListForNewItem(tiName, serial, assetTag, eAssetTag, uPosition, status, true); 
		try {
			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
			
			// delete newly created item after test is complete.
			Long id = getItemIdFromUiComponentDtoMap(componentDTOMap);
			if (id != null)	itemHome.deleteItem(id, false, null);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(componentDTOMap, "componentDTOMap is null");
	}

	//@Test
	public final void saveCustomFieldsForExistingItem() throws Throwable {
		Map<String,UiComponentDTO> componentDTOMap = null;
		Long itemId = 4807L;
		String serial = "1234";
		String assetTag = "1234";
		String eAssetTag = "1234";
		String uPosition = "15";
		String tiName = "NJA123L";
		long status = SystemLookup.ItemStatus.PLANNED;
		List<ValueIdDTO> valueIdDTOList = getValueIdDTOListForNewItem(tiName, serial, assetTag, eAssetTag, uPosition, status, true); 
		try {
 			componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(componentDTOMap, "componentDTOMap is null");
	}

	private void displayDataPortDTO(DataPortDTO dataPortDTO) {
		if (null != dataPortDTO) {
			System.out.println("data port data = " + dataPortDTO.getPortName() /*+ dataPortDTO.toString()*/);
		}
	}
	
	private void displayPowerPortDTO(PowerPortDTO powerPortDTO) {
		if (null != powerPortDTO) {
			System.out.println("power port data = " + powerPortDTO.getPortName() /*+ powerPortDTO.toString()*/);
		}
	}
	
	@Test
	public final void getItemPortDetailsTest() throws Throwable {
		long itemId = 57; // Created in dctrack 57.47 with 2 data and 2 power ports
		
		// Assert item details exist
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*FIXME: send userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// Assert item name matches what was returned from the viewItems call
		ItemViewData view = itemHome.viewItems( itemId );
		assertNotNull( view );
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		assertEquals( view.getItemName(), itemNameField.getUiValueIdField().getValue() );
		
		/* get the item's data ports and display */
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		for (DataPortDTO dataPortDTO: dataPortDTOList) {
			dataPortDTO.setComments("testing edit of data port " + dataPortDTO.getPortId());
			displayDataPortDTO(dataPortDTO);
		}

		/* get the item's power ports and display */
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			powerPortDTO.setComments("testing edit of power port " + powerPortDTO.getPortId());
			displayPowerPortDTO(powerPortDTO);
		}
	}
	
	// @Test
	public final void verifyEditItemPorts() throws Throwable {
		long itemId = 5460; // Created in dctrack 57.47 with 2 data and 2 power ports
		
		// Assert item details exist
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*FIXME: send userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// Assert item name matches what was returned from the viewItems call
		ItemViewData view = itemHome.viewItems( itemId );
		assertNotNull( view );
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		assertEquals( view.getItemName(), itemNameField.getUiValueIdField().getValue() );
		
		/* get the item's data ports and display */
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		for (DataPortDTO dataPortDTO: dataPortDTOList) {
			dataPortDTO.setComments("testing edit of data port " + dataPortDTO.getPortId());
			displayDataPortDTO(dataPortDTO);
		}

		/* test add new data port: add new data port to the data port list and save */
		if(dataPortDTOList.size() > 0) {
			DataPortDTO dataPortDTO = dataPortDTOList.get(0);
			dataPortDTO.setPortId(-1L);
			dataPortDTOList.add(dataPortDTO);
			// itemDataPortsField.getUiValueIdField().setValue(dataPortDTOList);
		}
		
		/* get the item's power ports and display */
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			powerPortDTO.setComments("testing edit of power port " + powerPortDTO.getPortId());
			displayPowerPortDTO(powerPortDTO);
		}
		/* test add new power port: add new power port to the power port list and save */
		if (powerPortDTOList.size() > 0) {
			PowerPortDTO powerPortDTO = powerPortDTOList.get(0);
			powerPortDTO.setPortId(-1L);
			powerPortDTOList.add(powerPortDTO);
		}
		
		/* test data port edit: modify the item's data port(s) and save */
		
		/* test power port edit: modify the item's power port(s) and save */
		
		/* test add new data port: add new data port to the data port list and save */
		
		/* test add new power port: add new power port to the power port list and save */
		
		/* test data port delete: remove a data port from the data port list and save */
		
		/* test power port delete: remove a power port from the power port list and save */
		 
		/* Modify the item and save : this is to test saving the ports */
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
	    @SuppressWarnings("rawtypes")
		Iterator it = item.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue(); //item.get((String) pairs.getKey());
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
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			/*else if (((String)pairs.getKey()).equals("_tiSkipValidation")) {
				dto.setData(true);
				
			}*/
			/* these are fialing validation and causing value validation error */
			/*else if (((String)pairs.getKey()).equals("tiPurchasePrice") ||
					((String)pairs.getKey()).equals("cmbSlotPosition") || 
					((String)pairs.getKey()).equals("cmbUPosition") ||
					((String)pairs.getKey()).equals("tiNoInGroup") ||
					((String)pairs.getKey()).equals("cmbOrder") ||
					((String)pairs.getKey()).equals("aboveBelowRbg")) {
				continue;
			}*/
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
		/*dto = new ValueIdDTO();
		dto.setLabel("_tiSkipValidation");
		dto.setData(true);
		valueIdDTOList.add(dto);*/

		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	// @Test
	public final void verifyNewItemPorts() throws Throwable {
		long itemId = 5478; // Created in dctrack 57.47 with 2 data and 2 power ports
		
		// Assert item details exist
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*FIXME: send userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// Assert item name matches what was returned from the viewItems call
		ItemViewData view = itemHome.viewItems( itemId );
		assertNotNull( view );
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		assertEquals( view.getItemName(), itemNameField.getUiValueIdField().getValue() );
		
		/* get the item's data ports and display */
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		for (DataPortDTO dataPortDTO: dataPortDTOList) {
			dataPortDTO.setPortId(-1L);
			dataPortDTO.setItemId(-1L);
			dataPortDTO.setComments("testing new of data port " + dataPortDTO.getPortId());
			displayDataPortDTO(dataPortDTO);
		}
		
		/* get the item's power ports and display */
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			powerPortDTO.setPortId(-1L);
			powerPortDTO.setItemId(-1L);
			powerPortDTO.setComments("testing new of power port " + powerPortDTO.getPortId());
			displayPowerPortDTO(powerPortDTO);
		}
		
		/* test data port edit: modify the item's data port(s) and save */
		
		/* test power port edit: modify the item's power port(s) and save */
		
		/* test add new data port: add new data port to the data port list and save */
		
		/* test add new power port: add new power port to the power port list and save */
		
		/* test data port delete: remove a data port from the data port list and save */
		
		/* test power port delete: remove a power port from the power port list and save */
		 
		/* Modify the item and save : this is to test saving the ports */
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
	    @SuppressWarnings("rawtypes")
		Iterator it = item.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue(); //item.get((String) pairs.getKey());
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
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			else if (((String)pairs.getKey()).equals("tiName")) {
				String name = (String) field.getUiValueIdField().getValue();
				name += "-unit-test";
				dto.setData(name);
			}
			/*else if (((String)pairs.getKey()).equals("_tiSkipValidation")) {
				dto.setData(true);
				
			}*/
			/* these are fialing validation and causing value validation error */
			/*else if (((String)pairs.getKey()).equals("tiPurchasePrice") ||
					((String)pairs.getKey()).equals("cmbSlotPosition") || 
					((String)pairs.getKey()).equals("cmbUPosition") ||
					((String)pairs.getKey()).equals("tiNoInGroup") ||
					((String)pairs.getKey()).equals("cmbOrder") ||
					((String)pairs.getKey()).equals("aboveBelowRbg")) {
				continue;
			}*/
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
		/*dto = new ValueIdDTO();
		dto.setLabel("_tiSkipValidation");
		dto.setData(true);
		valueIdDTOList.add(dto);*/
	    // LksData lksData = SystemLookup.getLksData(sessionFactory.getCurrentSession(), SystemLookup.RailsUsed.FRONT);
		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(-1L, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}
	
	// @Test
	public final void verifyDeleteItemPorts() throws Throwable {
		long itemId = 5453; // Created in dctrack 57.47 with 2 data and 2 power ports
		
		// Assert item details exist
		Map<String, UiComponentDTO> item = itemHome.getItemDetails( itemId, null /*FIXME: send userInfo */ );
		assertNotNull( item );
		assertTrue(item.size() > 0);
		
		// Assert item name matches what was returned from the viewItems call
		ItemViewData view = itemHome.viewItems( itemId );
		assertNotNull( view );
		UiComponentDTO itemNameField = item.get("tiName");
		assertNotNull( itemNameField );
		assertEquals( view.getItemName(), itemNameField.getUiValueIdField().getValue() );
		
		/* get the item's data ports and display */
		UiComponentDTO itemDataPortsField = item.get("tabDataPorts");
		assertNotNull( itemDataPortsField );
		@SuppressWarnings("unchecked")
		List<DataPortDTO> dataPortDTOList = (List<DataPortDTO>) itemDataPortsField.getUiValueIdField().getValue();
		if (null != dataPortDTOList) {
			for (DataPortDTO dataPortDTO: dataPortDTOList) {
				dataPortDTO.setComments("testing edit of data port " + dataPortDTO.getPortId());
				displayDataPortDTO(dataPortDTO);
			}
		}

		/* test add new data port: add new data port to the data port list and save */
		dataPortDTOList.clear();
/*		if(dataPortDTOList.size() > 0) {
			DataPortDTO dataPortDTO = dataPortDTOList.get(0);
			dataPortDTO.setPortId(-1L);
			dataPortDTOList.add(dataPortDTO);
			// itemDataPortsField.getUiValueIdField().setValue(dataPortDTOList);
		}
*/		
		/* get the item's power ports and display */
		UiComponentDTO itemPowerPortsField = item.get("tabPowerPorts");
		assertNotNull( itemPowerPortsField );
		@SuppressWarnings("unchecked")
		List<PowerPortDTO> powerPortDTOList = (List<PowerPortDTO>) itemPowerPortsField.getUiValueIdField().getValue();
		powerPortDTOList.clear();
/*		for (PowerPortDTO powerPortDTO: powerPortDTOList) {
			powerPortDTO.setComments("testing edit of power port " + powerPortDTO.getPortId());
			displayPowerPortDTO(powerPortDTO);
		}
		 test add new power port: add new power port to the power port list and save 
		if (powerPortDTOList.size() > 0) {
			PowerPortDTO powerPortDTO = powerPortDTOList.get(0);
			powerPortDTO.setPortId(-1L);
			powerPortDTOList.add(powerPortDTO);
		}
*/		
		/* test data port edit: modify the item's data port(s) and save */
		
		/* test power port edit: modify the item's power port(s) and save */
		
		/* test add new data port: add new data port to the data port list and save */
		
		/* test add new power port: add new power port to the power port list and save */
		
		/* test data port delete: remove a data port from the data port list and save */
		
		/* test power port delete: remove a power port from the power port list and save */
		 
		/* Modify the item and save : this is to test saving the ports */
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		ValueIdDTO dto = null;
		
	    @SuppressWarnings("rawtypes")
		Iterator it = item.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
			
	        dto = new ValueIdDTO();
			dto.setLabel((String) pairs.getKey());
			
			UiComponentDTO field = (UiComponentDTO) pairs.getValue(); //item.get((String) pairs.getKey());
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
					((String)pairs.getKey()).equals("tiRearClearance")) {
				continue;
			}
			/* value used by server is id field */
			else if (((String)pairs.getKey()).equals("cmbModel") || 
					((String)pairs.getKey()).equals("tiSubClass") || 
					((String)pairs.getKey()).equals("cmbCabinet") ||
					((String)pairs.getKey()).equals("cmbLocation") ||
					((String)pairs.getKey()).equals("radioCabinetSide") ||
					((String)pairs.getKey()).equals("radioRailsUsed") || 
					((String)pairs.getKey()).equals("cmbStatus")) {
				dto.setData(field.getUiValueIdField().getValueId());
			}
			/*else if (((String)pairs.getKey()).equals("_tiSkipValidation")) {
				dto.setData(true);
				
			}*/
			/* these are fialing validation and causing value validation error */
			/*else if (((String)pairs.getKey()).equals("tiPurchasePrice") ||
					((String)pairs.getKey()).equals("cmbSlotPosition") || 
					((String)pairs.getKey()).equals("cmbUPosition") ||
					((String)pairs.getKey()).equals("tiNoInGroup") ||
					((String)pairs.getKey()).equals("cmbOrder") ||
					((String)pairs.getKey()).equals("aboveBelowRbg")) {
				continue;
			}*/
			else {
				dto.setData(field.getUiValueIdField().getValue());
			}

			valueIdDTOList.add(dto);
	    }
		/*dto = new ValueIdDTO();
		dto.setLabel("_tiSkipValidation");
		dto.setData(true);
		valueIdDTOList.add(dto);*/

		Map<String,UiComponentDTO> componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, getTestAdminUser());
		assertNotNull(componentDTOMap);
	}

	@Test
	public void getItemClassTest() throws Throwable {
		long itemId = -11L;
		Long classLkpValueCode = itemService.getItemClass(itemId);
		assertNull(classLkpValueCode);
	}
	
	
	//
	//<-- Private methods
	//
	
	@Test(enabled = false)
	private void testExpectedLinkedUPSCount(long upsBankItemId, int expectedLinkedUPSCount) throws Throwable {
		Item item = itemHome.viewItemEagerById( upsBankItemId );
		UPSBank upsBank = (UPSBank)itemHome.getFactory().getItemObject( item );
		assertNotNull( upsBank );
		assertEquals(expectedLinkedUPSCount, upsBank.getLinkedUPSCount());
	}

	private static Long getItemIdFromUiComponentDtoMap(Map<String, UiComponentDTO> itemMap) {
		Long id = null;
		UiComponentDTO dto = itemMap.get("tiName");
		if (dto != null) {
			UiValueIdField uiField = dto.getUiValueIdField();
			if (uiField != null) {
				id = (Long) uiField.getValueId();
			}
		}
	    return id;
	}
	
	@Test
	public void testCreateItemFromFactory() throws BusinessValidationException,InstantiationException, IllegalAccessException{
		List<ValueIdDTO> dtoList = getValueIdDTOListForNewItem("XYZ", "ABC", "123", "345", "1", 0, true);
		ValueIdDTOHolder.capture(dtoList);
		Item item = itemDomainFactory.createItem((Long)ValueIdDTOHolder.getCurrent().getValue("cmbMake"));
		ValueIdDTOHolder.clearCurrent();
		assertTrue(item != null);
		assertTrue(item.getClassMountingFormFactorValue().equals(SystemLookup.ModelUniqueValue.DeviceStandardRackable));
	}
	
	@Test
	public void testGetItemObject() throws BusinessValidationException,InstantiationException, IllegalAccessException{
		List<ValueIdDTO> dtoList = getValueIdDTOListForNewItem("XYZ", "ABC", "123", "345", "1", 0, true);
		ItemObjectTemplate template = itemObjectTemplateFactory.getItemObject(-1L, dtoList);
		assertTrue(template != null);
	}
	
	@Test
	public void testSaveItemFromItemObjectTemplate() throws BusinessValidationException,Throwable{
		List<ValueIdDTO> dtoList = getValueIdDTOListForNewItem("UNIT-TEST-XYZ-1", "ABC-1", "123-1", "345-1", "10", 0, true);
		ItemObjectTemplate template = itemObjectTemplateFactory.getItemObject(-1L, dtoList);
		Map<String, UiComponentDTO> resultMap = new HashMap<String, UiComponentDTO>();
		try{
			resultMap =  template.saveItem(-1L, dtoList, getTestAdminUser());
		} finally{
			UiComponentDTO componentDTO = resultMap.get("tiName");
			if (componentDTO != null){
				Long id = (Long) componentDTO.getUiValueIdField().getValueId();
				template.deleteItem(id, false, getTestAdminUser());
			}
		}
		assertTrue(template != null);
	}
	
	@Test
	public void testGetEAssetTag() throws Throwable {
		ItemFinderDAO itemFinder = (ItemFinderDAO)itemDao;
		List<String> itemEAssetTagList = itemFinder.findEAssetTagById(10L);
		assertTrue(itemEAssetTagList != null);
	}
	
	
	private List<ValueIdDTO> getValueIdDTOListForNewItem(String tiName, String serial, String assetTag, String eAssetTag, String uPosition, long status, boolean addCustomfield) {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		valueIdDTOList.add(createValueIdDTOObj("cmbMake", 20L)); // HP
		valueIdDTOList.add(createValueIdDTOObj("cmbModel", 1L)); // Proliant DL320 G5p
		valueIdDTOList.add(createValueIdDTOObj("tiSerialNumber", serial));
		valueIdDTOList.add(createValueIdDTOObj("tiAssetTag", assetTag));
		valueIdDTOList.add(createValueIdDTOObj("tieAssetTag", eAssetTag));
		//Identity Panel Data
		valueIdDTOList.add(createValueIdDTOObj("tiName", tiName));
		valueIdDTOList.add(createValueIdDTOObj("tiAlias", "HELLO THERE!"));
		valueIdDTOList.add(createValueIdDTOObj("cmbType", 1L));
		valueIdDTOList.add(createValueIdDTOObj("cmbFunction", 970L));
		valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdmin", 20L));
		valueIdDTOList.add(createValueIdDTOObj("cmbSystemAdminTeam", 555L));
		valueIdDTOList.add(createValueIdDTOObj("cmbCustomer", 566L));
		valueIdDTOList.add(createValueIdDTOObj("cmbStatus", status /*SystemLookup.ItemStatus.IN_STORAGE*/));
		// Placement Panel Data
		valueIdDTOList.add(createValueIdDTOObj("cmbLocation", 2L));
		valueIdDTOList.add(createValueIdDTOObj("radioRailsUsed", 8003L));
		valueIdDTOList.add(createValueIdDTOObj("cmbCabinet", 27L));
		valueIdDTOList.add(createValueIdDTOObj("cmbUPosition", uPosition));
		valueIdDTOList.add(createValueIdDTOObj("cmbOrientation", 7081L));
		valueIdDTOList.add(createValueIdDTOObj("tiLocationRef", "locRef"));
		// Custom fields
		if (addCustomfield)
			valueIdDTOList.add(createValueIdDTOObj("tiCustomField", createCustomFields()));
		
		return valueIdDTOList;
 	}
	

	// JB check this Hibernate 4 expose error in this query @Test
	public void testHQLResticitonsToCriterion(){
		String qryStr = toSql("select i.itemId from Item as i left outer join i.model as m where i.itemName='1A' and i.itemId is not null and m.modelMfrDetails.modelMfrDetailId = 12");
		System.out.println(qryStr);
		qryStr = qryStr.substring(qryStr.indexOf("where")).replace("where", "");
		qryStr = qryStr.replaceAll("\\s\\(\\w+\\.", " ({alias}.");
		qryStr = qryStr.replaceAll("\\s\\w+\\."," {alias}.");
		Criterion criterion = Restrictions.sqlRestriction(qryStr);
		System.out.println(criterion.toString());
		
		Criteria criteria = session.createCriteria(Item.class);
		criteria.add(criterion);
		System.out.println(criteria.list());
	}
	
	private String toSql(String hqlQueryText){
		if (sf != null && hqlQueryText != null && hqlQueryText.trim().length() > 0) {
			QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
			SessionFactoryImplementor factory = (SessionFactoryImplementor)sf;
			QueryTranslator translator = translatorFactory.createQueryTranslator(hqlQueryText, hqlQueryText, Collections.EMPTY_MAP, factory);
			translator.compile(Collections.EMPTY_MAP, false);
			return translator.getSQLString();
		}
		return null;
	}
	
	//@Test // This test requires PIQ integration
	public void syncPduReadings() throws Throwable {
		Long[] ids = {5114L, 5115L, 5118L, 5119L};
		try {
			itemService.syncPduReadings(ids);
		}catch (ServiceLayerException e) {
			System.out.println (" Exception occured: " + e.getMessage());
			throw e;
		}
	}
	
    @Test
    public void getBreakerPorts() throws Throwable {
        // Long[] panelSubClassList = {2301L, 2302l}; //implicit
        Long[] phaseLookupList = {7022L, 7023L};
        Long[] branchCircuitBreaker = {SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER};
        Boolean[] isUsedAL = { false };
        Long ampsRating = 0L;

        List<BreakerDTO> breakers = itemHome.getAllBreakers(ampsRating, isUsedAL, branchCircuitBreaker, phaseLookupList, null, new Long(-1));
    	System.out.println ("Total Breakers : " + breakers.size());

        if (breakers.size() == 0) System.out.println ("No matching used breaker port found --");
        for (BreakerDTO b: breakers) {
        	System.out.println ("======================================");
        	System.out.println ("Breaker Id :" + b.getBreakerId());
        	System.out.println ("Breaker Name :" + b.getBreakerName());
        	System.out.println ("FPDU Id :" + b.getFpduId());
        	System.out.println ("FPDU Name :" + b.getFpduName());
        	System.out.println ("Location Id : " + b.getLocationId());
        	System.out.println ("Location Name : " + b.getLocation());
        	System.out.println ("UPB Bank Id :" + b.getUpsBankId());
        	System.out.println ("UPS Bank Name: " + b.getUpsBankName());
        	System.out.println ("PPanel Id: " + b.getPowerPanelId());
        	System.out.println ("PPanel Name:" + b.getPowerPanelName());
        	System.out.println ("Wiring valueCode:" + b.getOutputWiringLkpValueCode());
        	System.out.println ("Wiring desc: " + b.getOutputWiringDesc());
        	System.out.println ("Line Volts: " + b.getLineVolts());
        	System.out.println ("Amps Nameplate: " + b.getAmpsNameplate());
        	System.out.println ("UI BreakerName: " + b.getUiBreakerColumn());
        	System.out.println ("UI Ratig Amps: " + b.getUiRatingColumn());
        	System.out.println ("======================================");
        	
        }
        
        breakers = null;
        Boolean[] newIsUsedAL = { false };
        breakers = itemHome.getAllBreakers(ampsRating, newIsUsedAL, branchCircuitBreaker, phaseLookupList, 47805L, new Long(-1));
    	System.out.println ("+++++++++++++++++++++++++++++++++++++");
    	System.out.println ("Total Breakers : " + breakers.size());
    	System.out.println ("+++++++++++++++++++++++++++++++++++++");

    	if (breakers.size() == 0) System.out.println ("No matching unused breaker port found -");
        for (BreakerDTO b: breakers) {
        	System.out.println ("+++++++++++++++++++++++++++++++++++++");
        	System.out.println ("Breaker Id :" + b.getBreakerId());
        	System.out.println ("Breaker Name :" + b.getBreakerName());
        	System.out.println ("FPDU Id :" + b.getFpduId());
        	System.out.println ("FPDU Name :" + b.getFpduName());
        	System.out.println ("Location Id : " + b.getLocationId());
        	System.out.println ("Location Name : " + b.getLocation());
        	System.out.println ("UPB Bank Id :" + b.getUpsBankId());
        	System.out.println ("UPS Bank Name: " + b.getUpsBankName());
        	System.out.println ("PPanel Id: " + b.getPowerPanelId());
        	System.out.println ("PPanel Name:" + b.getPowerPanelName());
        	System.out.println ("Wiring valueCode:" + b.getOutputWiringLkpValueCode());
        	System.out.println ("Wiring desc: " + b.getOutputWiringDesc());
        	System.out.println ("Line Volts: " + b.getLineVolts());
        	System.out.println ("Amps Nameplate: " + b.getAmpsNameplate());
        	System.out.println ("UI BreakerName: " + b.getUiBreakerColumn());
        	System.out.println ("UI Ratig Amps: " + b.getUiRatingColumn());
        	System.out.println ("+++++++++++++++++++++++++++++++++++++");
        	
        }
    	
    	
    }


}
