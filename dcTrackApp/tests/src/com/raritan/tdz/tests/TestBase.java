package com.raritan.tdz.tests;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jmock.Mockery;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.MapBindingResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.UserInfo.UserAccessLevel;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.home.PortHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.piq.service.PIQBulkSyncService;
import com.raritan.tdz.powerchain.home.FPDUBreakerToUPSBankBreakerUpdateActionHandler;
import com.raritan.tdz.powerchain.home.FloorPduCreateBreakerPortActionHandler;
import com.raritan.tdz.powerchain.home.PortConnectionAdaptorFactory;
import com.raritan.tdz.powerchain.home.PowerConnectionAdaptor;
import com.raritan.tdz.powerchain.home.PowerPanelBreakerPortActionHandler;
import com.raritan.tdz.powerchain.home.UPSBankCreateBreakerPortActionHandler;
import com.raritan.tdz.reports.generator.GenerateReport;
import com.raritan.tdz.reports.imageprovider.CabinetElevation;
import com.raritan.tdz.reports.imageprovider.ImageProvider;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestHome;
import com.raritan.tdz.request.home.RequestManager;
import com.raritan.tdz.request.home.RequestService;
import com.raritan.tdz.service.TicketService;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;
import com.raritan.tdz.ticket.home.TicketResetBehavior;
import com.raritan.tdz.ticket.home.TicketSaveBehavior;
import com.raritan.tdz.ticket.home.TicketUtility;
import com.raritan.tdz.util.DCTColumnsSchema;
import com.raritan.tdz.vbjavabridge.home.LNHome;

/**
 * Base class for JUnit tests.
 * @author Andrew Cohen
 */
public abstract class TestBase {

	protected static SessionFactory sf = null;
	protected static ClassPathXmlApplicationContext ctx = null;
	protected static Session session = null;
	protected static Logger log = null;
	
	protected ItemRequest itemRequest;
	protected ItemRequestDAO requestDAO;
	protected RequestDAO requestDAOExt;
	protected ItemHome itemHome;
	protected PortHome portHome;
	protected ItemService itemService;
	protected DCTColumnsSchema columnsSchema;
	protected ItemDAO itemDAO;
	protected ModelDAO modelDAO;
	protected SystemLookupFinderDAO systemLookupDAO;
	protected TicketsDAO ticketsDAO;
	protected TicketResetBehavior ticketResetBehaviors = null;
	protected TicketSaveBehavior requestTicketSaveBehavior = null;
	protected TicketResetBehavior ticketCompletedResetBehaviors = null;
	protected TicketUtility ticketUtility = null;
	protected ItemDeleteBehavior itemDeleteTicketBehavior = null;
	protected TicketFieldsDAO ticketFieldsDAO = null;
	protected TicketPortsDataDAO ticketPortsDataDAO = null;
	protected TicketPortsPowerDAO ticketPortsPowerDAO = null;
	protected TicketService ticketService;
	protected ItemFinderDAO itemFinderDAO;
	protected LocationDAO locationDAO;
	protected PIQBulkSyncService piqBulkSyncService;
	protected RequestManager requestManager;
	protected RequestHome requestHome;
	protected RequestHome requestHomeDirect;
	protected PortConnectionAdaptorFactory portConnectionAdaptorFactory;
	protected PowerConnectionAdaptor powerConnectionAdaptor;
	protected PowerPanelBreakerPortActionHandler powerPanelBreakerPortActionHandler;
	protected FloorPduCreateBreakerPortActionHandler floorPduCreateBreakerPortActionHandler;
	protected FPDUBreakerToUPSBankBreakerUpdateActionHandler floorPduBreakerPortToUpsUpdateConnectionActionHandler;
	protected UPSBankCreateBreakerPortActionHandler upsBankCreateBreakerPortActionHandler;
	protected Mockery jmockContext;
	protected ResourceBundleMessageSource messageSource;
	protected RequestService requestService;
	protected CabinetElevation m_cabelev;
	protected ImageProvider m_provider;

	
	protected static GenerateReport birtGenerateReport;
	
	/**
	 * A dummy location used for unit tests.
	 * All items created for unit testing will go in this location.
	 */
	private DataCenterLocationDetails location;
	
	/** 
	 * A list of items created for unit testing.
	 * This will automatically be deleted from 
	 * the database when the test is complete.
	 */
	private List<Long> testItemsId;
	
	private List<Long> badItemIds;
	
	private List<Long> testUpsItemsId;
	
	public static String PARENT_CONTEXT_PREFIX = "parentContextPathPrefix";
	public static String USER_SESSION_ID = "userSessionId";
	private static String propertiesName = "requestContextProps";
	
	private static Integer rowLabelPostfix = 1;
	
	private static void setEnvironmentForRequest(ConfigurableApplicationContext ctx, String parentContextPrefix, String sessionId) throws DataAccessException {
		StandardEnvironment env = new StandardEnvironment();
		Properties props = new Properties();
		// populate properties for customer
		props.setProperty(PARENT_CONTEXT_PREFIX, parentContextPrefix);
		props.setProperty(USER_SESSION_ID, sessionId);

		PropertiesPropertySource pps = new PropertiesPropertySource(propertiesName, props);
		
		env.getPropertySources().addLast(pps);
		
		// ctx.setId(springContextPath + sessionId);
		
		ctx.setEnvironment(env);
	}

	
	@BeforeTest
	public static void setUpBeforeTest() {
		// ctx = new ClassPathXmlApplicationContext("testsContext.xml");
		
		ctx = new ClassPathXmlApplicationContext((String[]) (Arrays.asList("testsContext.xml").toArray()), false, null);
		
		ctx.registerShutdownHook();
		
		try {
			// setEnvironmentForRequest(ctx, "../../../../../test-classes", "userSessionId");
			setEnvironmentForRequest(ctx, "../../../../../../src/main/webapp/WEB-INF/spring", "userSessionId");
			
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ctx.refresh();
		
		sf = (SessionFactory) ctx.getBean("sessionFactory");
		log = Logger.getLogger("UnitTestLogger");
		
		// Allow FlexSessionContent to return a mock user since there
		// will never be a Flex Session when running unit tests.
		FlexUserSessionContext.setAllowMockUser( true );
		
	   // session = sf.openSession();
	  //  TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));

	}
	
	
	@AfterTest
	public static void tearDownAfterTest() {
    //	SessionHolder holder = (SessionHolder)	TransactionSynchronizationManager.getResource(sf);
    //    Session s = holder.getSession();

     //   TransactionSynchronizationManager.unbindResource(sf);
    //    SessionFactoryUtils.closeSession(s);
	//	 session.close();

		ctx = null;
		sf = null;
		session = null;
		log = null;
	}
	
	@BeforeClass
	public void setupBeforeClass() throws Throwable {
	    session = sf.openSession();
	}
	
	public void setupAfterClass() throws Throwable {
		session.close();
		session = null;
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
	    // sf = (SessionFactory)ctx.getBean("sessionFactory");
		session.clear();
		
	    TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	    
	    disableListenNotify();
	    
	    itemHome = (ItemHome)ctx.getBean("itemHome");
	    portHome = (PortHome)ctx.getBean("portHome");
	    requestManager = (RequestManager) ctx.getBean("requestManager");
	    requestHomeDirect = (RequestHome) ctx.getBean("requestHome");
	    requestHome = (RequestHome) ctx.getBean("requestHomeGateway");
	    itemService = (ItemService)ctx.getBean("itemService");
	    itemRequest = (ItemRequest)ctx.getBean("itemRequest");
	    requestDAO = (ItemRequestDAO) ctx.getBean("itemRequestDAO");
	    requestDAOExt = (RequestDAO) ctx.getBean("requestDAO");
	    columnsSchema = (DCTColumnsSchema)ctx.getBean("columnsSchema");
	    itemDAO = (ItemDAO)ctx.getBean("itemDAO");
	    modelDAO = (ModelDAO) ctx.getBean("modelDAO");
	    itemFinderDAO = (ItemFinderDAO)ctx.getBean("itemDAO");
	    locationDAO = (LocationDAO)ctx.getBean("locationDAO");
	    piqBulkSyncService = (PIQBulkSyncService) ctx.getBean("piqBulkUpdateGateway");
	    systemLookupDAO = (SystemLookupFinderDAO) ctx.getBean("systemLookupDAO");
	    ticketsDAO = (TicketsDAO) ctx.getBean("ticketsDAO");
	    ticketFieldsDAO = (TicketFieldsDAO) ctx.getBean("ticketFieldsDAO");
		ticketPortsDataDAO = (TicketPortsDataDAO) ctx.getBean("ticketPortsDataDAO");
		ticketPortsPowerDAO = (TicketPortsPowerDAO) ctx.getBean("ticketPortsPowerDAO");
		ticketResetBehaviors = (TicketResetBehavior) ctx.getBean("ticketResetBehaviors");
		requestTicketSaveBehavior = (TicketSaveBehavior) ctx.getBean("requestTicketSaveBehavior");
		ticketCompletedResetBehaviors = (TicketResetBehavior) ctx.getBean("ticketCompletedResetBehaviors");
		ticketUtility = (TicketUtility) ctx.getBean("ticketUtility");
		itemDeleteTicketBehavior = (ItemDeleteBehavior) ctx.getBean("itemDeleteTicketBehavior");
		ticketService = (TicketService) ctx.getBean("ticketService");
		messageSource = (ResourceBundleMessageSource) ctx.getBean("messageSource");
		requestService = (RequestService) ctx.getBean("requestService");

	    portConnectionAdaptorFactory = (PortConnectionAdaptorFactory)ctx.getBean("portConnectionAdaptorFactory");
	    powerConnectionAdaptor = (PowerConnectionAdaptor) ctx.getBean("powerConnectionAdaptor");
	    powerPanelBreakerPortActionHandler = (PowerPanelBreakerPortActionHandler) ctx.getBean("powerPanelBreakerPortActionHandler");
	    floorPduCreateBreakerPortActionHandler = (FloorPduCreateBreakerPortActionHandler) ctx.getBean("floorPduCreateBreakerPortActionHandler");
	    floorPduBreakerPortToUpsUpdateConnectionActionHandler = (FPDUBreakerToUPSBankBreakerUpdateActionHandler)  ctx.getBean("floorPduBreakerPortToUpsUpdateConnectionActionHandler");
	    upsBankCreateBreakerPortActionHandler = (UPSBankCreateBreakerPortActionHandler)  ctx.getBean("upsBankCreateBreakerPortActionHandler");
	    
    	m_provider = new ImageProvider(session);
    	m_cabelev = m_provider.getCabinetElevation();

	    birtGenerateReport = (GenerateReport) ctx.getBean("birtGenerateReport");
	    
	    columnsSchema.createUiIDPropertyLengthMap();	    
	    testItemsId = new LinkedList<Long>();
	    testUpsItemsId = new LinkedList<Long>();
	    
	}
	
	public void addTestUpsItem(Item item) {
		testUpsItemsId.add(item.getItemId());
	}

	public void addTestItemId(Long itemId){
		testItemsId.add(itemId);
	}

	public void addTestItemList(List<Long> itemIdsList){
		testItemsId.addAll(itemIdsList);
	}

	public void addTestItems(List<Item> itemsList){
		for(Item item:itemsList){
			testItemsId.add(item.getItemId());
		}
	}
	
	public void removeTestItem(Item item){
		testItemsId.remove(item.getItemId());
	}
	

	public void addTestItem(Item item) {
		testUpsItemsId.add(item.getItemId());
	}
	
	
	
	public List<Long> getTestItemsId() {
		return testItemsId;
	}


	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {

		Long badItemId = new Long(-1);
    	// SessionHolder holder = (SessionHolder)	TransactionSynchronizationManager.getResource(sf);
        // Session s = holder.getSession();
		try {	
		    	
			m_provider = null;
			m_cabelev = null;

			cleanup (testItemsId, badItemId);
			
			cleanup (testUpsItemsId, badItemId);
//		session.clear();
//		
//        TransactionSynchronizationManager.unbindResource(sf);
        /*      SessionFactoryUtils.closeSession(s);*/
		} catch (org.springframework.dao.DataAccessException he){
			Assert.assertTrue(false);
			badItemIds.add(badItemId);
			testItemsId.remove(badItemId);
		} finally {
			session.clear();
			
	        TransactionSynchronizationManager.unbindResource(sf);
		}
	}
	
	private void cleanup ( List<Long> itemIds, Long badItemId) throws Throwable {
		
	    if (null != itemIds && itemIds.size() > 0) {
	        Criteria criteria = session.createCriteria(Item.class);
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			criteria.add(Restrictions.in("itemId", itemIds));
			
			@SuppressWarnings("unchecked")
			List<Item> testItems = criteria.list();
	        
			for (Item item : testItems) {
				item.setParentItem(null);
				item.setCracNwGrpItem(null);
				
				if(item instanceof ItItem){
					ItItem it = (ItItem)item;
					it.setBladeChassis(null);
				}
			}
			
			testItems.clear();
			testItems = null;

			session.flush();
		
			for (Long itemId : itemIds) {
				try
				{
				//	refresh item before delete
					if (itemId <= 0) {
						continue;
					}
					log.debug("tearDown() delete item: " + itemId.toString());
					System.out.println("tearDown() delete item: " + itemId.toString());
					itemHome.deleteItem(itemId, false, FlexUserSessionContext.getUser());
				}
				catch(Exception ex){
				//assert
				}
				badItemId = itemId; //This will be never used unless there is a hibernate exception.
			}
			itemIds.clear();
			itemIds = null;
			
			session.flush();
			
			if (location != null) {
				session.delete( location );
				session.flush();
				location = null;
			}		
	    }
	}
	
	protected final Item createTestItem(String itemName, long classLookupValue, Integer piq_id) throws Throwable {
		return createTestItem(itemName, classLookupValue, SystemLookup.ItemStatus.INSTALLED, piq_id);
	}
			
	protected final Item createTestItem(String itemName, long classLookupValue, long statusLookupValue, Integer piq_id) throws Throwable {
		DataCenterLocationDetails loc = getUnitTestLocation();
		Item item = new Item();
		item.setItemName( itemName );
		item.setClassLookup( SystemLookup.getLksData(session, classLookupValue) );
		item.setStatusLookup( SystemLookup.getLksData(session, statusLookupValue) );
		item.setDataCenterLocation( loc );
		
		if(classLookupValue == SystemLookup.Class.DEVICE){
			item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE) );
		}
		
		if(classLookupValue == SystemLookup.Class.NETWORK){
			item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.NETWORK_STACK) );
			item.setModel(getModel(60L));
		}

		if(classLookupValue == SystemLookup.Class.FLOOR_OUTLET){
			item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.WHIP_OUTLET) );
		}
		
		if (piq_id != null) {
			item.setPiqId( piq_id );
		}
		
		item.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED));
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	protected final Item createNewTestItem(String itemName, long classLookupValue, Long subClassLookupValue) throws Throwable {
		DataCenterLocationDetails loc = getUnitTestLocation();
		Item item = new Item();
		item.setItemName( itemName );
		item.setClassLookup( SystemLookup.getLksData(session, classLookupValue) );
		
		if(subClassLookupValue != null){
			item.setSubclassLookup( SystemLookup.getLksData(session, subClassLookupValue) );
		}
		
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );				
				
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	
	protected final DataCenterLocationDetails getUnitTestLocation() throws Throwable {
				
		if (location == null) {
			String locationName = "UnitTestDC";
			location = getTestLocation(locationName);
			
			if(location == null){
				location = new DataCenterLocationDetails();
				location.setCode(locationName);
				location.setDcName(locationName);
				location.setArea(1000L);
				location.setComponentTypeLookup( SystemLookup.getLksData(session, SystemLookup.DcLocation.SITE) );
				session.save( location );
			}
			session.flush();
		}
		return location;
	}
	
	protected final void deleteUnitTestLocation() throws Throwable {
		if (location != null){
			SessionHolder holder = (SessionHolder)	TransactionSynchronizationManager.getResource(sf);
	        Session s = holder.getSession();
			
			s.delete(location);
			location = null;
		}
	}
	
	/**
	 * Suspends listen/notify that it doesn't generate any unexpected events
	 * that might corrupt the unit test event assertions.
	 */
	protected final void disableListenNotify() {
		LNHome lnHome = (LNHome)ctx.getBean("listenNotifyHome");
		lnHome.setSuspend( true );
	}	
	
	
	protected static String getCurrentMethodName(){
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[2].getMethodName();
		
	}
	
	protected void clearTestItems(){
		testItemsId.clear();
	}
	
	protected final UserInfo getTestAdminUser() {
		UserInfo user = new UserInfo();
		user.setUserName("unitTestAdmin");
		user.setUserId("1"); //This field is the users.id, not a string field
		user.setId(1);
		user.setAccessLevelId( Integer.toString( UserAccessLevel.ADMIN.getAccessLevel() ) );
		user.setSessionId("integration test session");
		return user;
	}
	
	public DataCenterLocationDetails getTestLocation(String locationName){		
		Criteria criteria;
		
		try{						
			criteria = session.createCriteria(DataCenterLocationDetails.class);
			criteria.add(Restrictions.eq("dcName", locationName) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			@SuppressWarnings("rawtypes")
			List list = criteria.list();
			
			if(list != null && list.size() > 0)
			{
				return (DataCenterLocationDetails)list.get(0);
			}
			
		}catch(HibernateException e){
			
			 e.printStackTrace();
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			e.printStackTrace();
		}
		return null;
	}
	
	public ModelDetails getModel(long modelId){
		// session = sf.getCurrentSession();
		
		ModelDetails model = (ModelDetails)session.get(ModelDetails.class, modelId);
		model.getClassLookup().getLkpValue();
		
		return model;
		
	}
	
	public Item getItem(long itemId) {
		// session = sf.getCurrentSession();
		
		Item item = (Item)session.get(Item.class, itemId);		
		item.getCracNwGrpItem();
		item.getDataCenterLocation();
		item.getFacingLookup();
		item.getMountedRailLookup();
		
		if(item.getModel() != null){
			item.getModel().getMounting();
		}
		
		return item;		
	}

	protected final ItItem cloneItItem(ItItem item) throws Throwable {
		ItItem itemx = new ItItem();
		itemx.setItemName(item.getItemName() );
		itemx.setClassLookup( item.getClassLookup() );
		itemx.setSubclassLookup(item.getSubclassLookup());
		itemx.setModel(item.getModel());				
		itemx.setStatusLookup(item.getStatusLookup() );
		itemx.setDataCenterLocation(item.getDataCenterLocation());				
		itemx.setParentItem(item.getParentItem()); 
		itemx.setUPosition(item.getUPosition());
		itemx.setMountedRailLookup(item.getMountedRailLookup());
		
		return itemx;
	}	
	
	protected final ItItem createNewTestDevice(String itemName, Long statusValueCode) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE) );
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.FrontFaces.NORTH) );
		item.setModel(getModel(1L));
		item.setPsredundancy("N + 1");
		
		if(statusValueCode == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusValueCode) );
		}
		
		item.setDataCenterLocation( loc );				
		item.setParentItem(getItem(9L)); //cabinet 1G
		// item.setUPosition(20L);
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestPortDevice(String itemName, Long statusLksId, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE) );
		item.setModel(getModel(1L));
		item.setPsredundancy("N + 1");
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );				
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId)); 
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		item.setUPosition(-1L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	
	protected final MeItem createNewTestUPS(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.UPS) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(1304L)); // "Liebert" / "Series 610"
		item.setDataCenterLocation( loc );
		item.setRatingV(400);
		item.setRatingAmps(200);

		itemHome.saveItem( item );
		session.flush();
		
		addTestUpsItem( item );
		
		return item;
	}	

	protected final MeItem createNewTestFloorPDU(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setDataCenterLocation( loc );				
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected MeItem createBranchCircuitBreakerPanelPort(Item item, Long numBranchCircuitBreakers) throws Throwable {
		for (Long i = 0L; i < numBranchCircuitBreakers; i++) {
			item.addPowerPort(createBranchCircuitBreakerPanelPort(item, i.toString(), i.intValue()));
		}
		itemHome.saveItem( item );
		return (MeItem) item;
	}
	
	protected MeItem changePanelValues(MeItem panel) throws DataAccessException {
		// panel.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE )); -- cannot change the phase of the panel
		panel.setLineVolts(220);
		panel.setRatingAmps(45);

		itemHome.saveItem( panel );
		session.flush();
		return panel;
	}

	protected MeItem changeUpsBankValues(MeItem upsBank) throws DataAccessException {
		// panel.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE )); -- cannot change the phase of the panel
		upsBank.setRatingV(220);
		upsBank.setRatingKva(340);
		// upsBank.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.THREE_WYE ));

		itemHome.saveItem( upsBank );
		session.flush();
		return upsBank;
	}
	

	
	protected MeItem changeFPDUValues(MeItem fpdu) throws DataAccessException {
		// panel.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE )); -- cannot change the phase of the panel
		fpdu.setLineVolts(220);
		fpdu.setRatingAmps(45);

		itemHome.saveItem( fpdu );
		session.flush();
		return fpdu;
	}
	
	
	protected MeItem createPowerOutlet(String outletName, Item upsBank, Item panelItem, Item parentCabinet, PowerPort breakerPort, Long numOfPorts) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		
		// dct_items table
		item.setItemName( outletName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_OUTLET) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.WHIP_OUTLET) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(1434L)); // "Electrical Outlet Box" / "Hubbel"
		item.setDataCenterLocation(loc);
		item.setParentItem(parentCabinet);
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);

		// dct_items_me table
		item.setPduPanelItem((MeItem) panelItem);
		item.setUpsBankItem((MeItem)upsBank);
		
		item.setPhaseLookup( null );
		item.setRatingV( 0 );
		item.setLineVolts( 0 );
		item.setRatingAmps( 0 );
		
		for (Long i = 0L; i < numOfPorts; i++) {
			PowerPort outletPort = createWhipOutletPowerPort(item, "R" + i.toString(), breakerPort, i.intValue() );
			item.addPowerPort( outletPort );
			// PowerConnectionAdaptor pwrConnAdaptor = portConnectionAdaptorFactory.get(outletPort.getPortSubClassLookup().getLkpValueCode(), breakerPort.getPortSubClassLookup().getLkpValueCode());
			powerConnectionAdaptor.convert(outletPort, breakerPort);
		}
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	protected MeItem createPowerOutlet(String outletName, Long statusValueCode) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		
		// dct_items table
		item.setItemName( outletName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_OUTLET) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.WHIP_OUTLET) );	
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) );
		item.setParentItem(getItem(9L)); //cabinet 1G
		item.setUPosition(20L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );		
		item.setModel(getModel(1434L)); // "Electrical Outlet Box" / "Hubbel"
		item.setDataCenterLocation(loc);
		item.setPhaseLookup( null );
		item.setRatingV( 0 );
		item.setLineVolts( 0 );
		item.setRatingAmps( 0 );
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);
		
		if(statusValueCode == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusValueCode) );
		}		
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	protected MeItem createPowerPanel(Item parentItem, String panelName, Long numBranchCircuitBreakers, Long subClassLkpValueCode, Long status) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName( panelName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setSubclassLookup( SystemLookup.getLksData(session, subClassLkpValueCode.longValue()) );
		item.setStatusLookup( SystemLookup.getLksData(session, status) );
		item.setPhaseLookup( SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_2WIRE ));
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setRatingAmps(100);
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setDataCenterLocation(loc);
		item.setParentItem(parentItem);
		for (Long i = 0L; i < numBranchCircuitBreakers; i++) {
			item.addPowerPort(createBranchCircuitBreakerPanelPort(item, i.toString(), i.intValue()));
		}
		itemHome.saveItem( item );
		session.flush();
		addTestItem( item );
		return item;
	}
	
	protected final MeItem createNewTestFloorPDUWithPanels(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setDataCenterLocation( loc );
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE) /*new LksData(72L)*/);
		item.setRatingAmps(200);
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setDataCenterLocation(loc);
		itemHome.saveItem( item );
		session.flush();

		item.addChildItem( createPowerPanel(item, "INT_TEST_PANEL_PS1", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED));
		item.addChildItem( createPowerPanel(item, "INT_TEST_PANEL_PS2", 8L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED));

		addTestItem( item );
		/*Set<Item> panels = item.getChildItems();
		for (Item panel: panels) {
			addTestItem( panel );
		}*/
		// addTestItem( item );
		
		return item;
	}

	protected final MeItem createNewTestFloorPDUWithNoPanels(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE) /*new LksData(72L)*/);
		//item.addChildItem( createPowerPanel(item, "PS1", 0L));
		//item.addChildItem(createPowerPanel(item, "PS2", 0L));
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setDataCenterLocation(loc);
		itemHome.saveItem( item );
		session.flush();
		
		item.addChildItem(createPowerPanel(item, "PS1", 0L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED));
		item.addChildItem(createPowerPanel(item, "PS2", 0L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED));
		
		addTestItem( item );
		
		return item;
	}


	
	protected final MeItem createNewTestFloorPDUWithPanelsAndBranchCircuitBreakers(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.FLOOR_PDU) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(58L)); // "Liebert" / "Precision Power 50-225 kVA, 4PB"
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE) /*new LksData(72L)*/);
		item.setRatingV(120);
		item.setLineVolts(120);
		item.setRatingAmps(120);
		item.setDataCenterLocation(loc);
		
		itemHome.saveItem( item );
		session.flush();

		Item panel1 = createPowerPanel(item, "PS1", 4L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		Item panel2 =  createPowerPanel(item, "PS2", 8L, SystemLookup.SubClass.LOCAL, SystemLookup.ItemStatus.INSTALLED);
		item.addChildItem( panel1 );
		item.addChildItem( panel2 );
		
		// make a connection between fpdu and panel
		/*powerPanelBreakerPortActionHandler.process(panel1, null, true);
		powerPanelBreakerPortActionHandler.process(panel2, null, true);*/
		
		addTestItem( item );
		
		return item;
	}

	protected final MeItem createNewTestUPSBank(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.UPS_BANK) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setModel(getModel(1304L)); // "Series 610" / "Liebert"
		item.setDataCenterLocation( loc );
		item.setRatingV(220);
		item.setRatingKva(300);
		item.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE) /*new LksData(72L)*/);
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}


	
	protected final ItItem createNewTestDeviceVM(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE) );
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );				
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestDeviceFS(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		CabinetItem cabinet = new CabinetItem();
		cabinet.setItemName(itemName + "-CAB" );
		cabinet.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.CABINET) );
		cabinet.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.CONTAINER));
		cabinet.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		cabinet.setPositionInRow(1);
		cabinet.setRowLabel(getRowLabel());
		cabinet.setDataCenterLocation( loc );
		
		itemHome.saveItem( cabinet );
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		item.setModel(getModel(100L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );				
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.BOTH) );	
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setParentItem(cabinet);
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) );
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem(cabinet);
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createTestDeviceFS(String itemName, Long status) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		item.setModel(getModel(100L));				
		item.setStatusLookup( SystemLookup.getLksData(session, status) );
		item.setDataCenterLocation( loc );				
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.BOTH) );	
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) );
		// item.setStatusLookup( SystemLookup.getLksData(session, status) );
		// item.setParentItem(cabinet);
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestDeviceChassis(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		item.setModel(getModel(1015L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		
		// item.setParentItem(getItem(9L)); //cabinet 1G
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", SystemLookup.ItemStatus.INSTALLED);
		item.setParentItem(cabinetItem);
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);
		
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT));
		// item.setUPosition(10L);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}		

	protected final ItItem createNewTestDeviceChassis(String itemName, Long statusLksId, long cabinetId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		item.setModel(getModel(1015L));
		if (null == statusLksId) {
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else {
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		item.setDataCenterLocation( loc );	
		item.setParentItem(getItem(cabinetId));
		item.setUPosition(-1L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}		

	protected final ItItem createNewTestNetworkChassis(String itemName, Long statusLksId, long cabinetId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		item.setModel(getModel(1015L));
		if (null == statusLksId) {
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else {
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		item.setDataCenterLocation( loc );	
		item.setParentItem(getItem(cabinetId));
		item.setUPosition(-1L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}		

	
	protected final ItItem createNewTestDeviceBlade(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		item.setModel(getModel(1020L));
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );				
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setSlotPosition(8L);
		item.setPsredundancy("N + 1");

		ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		item.setBladeChassis(chassis);

		item.setParentItem(chassis.getParentItem());
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	protected final ItItem createNewTestPortDeviceBlade(String itemName, long cabinetItemId, Long slotPosition) throws Throwable { 
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		item.setModel(getModel(1020L));
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );
		
		item.setPsredundancy("N + 1");

		// set the chassis
		ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		item.setParentItem(chassis.getParentItem());
		item.setBladeChassis(chassis);
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.ChassisFace.FRONT));
		if (null != slotPosition) {
			item.setSlotPosition(slotPosition.longValue());
		}

		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );

		return item;
	}

	protected final ItItem createNewTestDeviceBladeInChassis(String itemName, long cabinetItemId, long chassisItemId, Long statusValueCode) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		item.setModel(getModel(1020L));
		
		if(statusValueCode == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusValueCode) );
		}
		
		item.setDataCenterLocation( loc );				
		//item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		//item.setSlotPosition(8L);
		item.setPsredundancy("N + 1");
		item.setParentItem(getItem(cabinetItemId));
		item.setBladeChassis(getItem(chassisItemId));

		//ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		//item.setBladeChassis(chassis);

		/*if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}*/
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	
	protected final ItItem createNewTestDeviceBlade(String itemName, long status) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		item.setModel(getModel(1020L));				
		item.setStatusLookup( SystemLookup.getLksData(session, status) );
		item.setDataCenterLocation( loc );				
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setSlotPosition(1L);
		item.setPsredundancy("N + 1");
		
		ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		item.setBladeChassis(chassis);
		
		item.setParentItem(getItem(9L)); //cabinet 1G
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	
	
	protected final ItItem getNewTestDeviceBlade(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		item.setModel(getModel(1020L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );				
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setSlotPosition(8L);
		
/*		itemHome.saveItem( item );
		session.flush();
		
		testItems.add( item );*/
		
		return item;
	} 

	protected final ItItem createNewTestNetworkChassis(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS));
		item.setModel(getModel(45L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );
		
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", SystemLookup.ItemStatus.INSTALLED);
		
		item.setParentItem(cabinetItem/*getItem(9L)*/); //cabinet 1G
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		addTestItem( cabinetItem );
		
		return item;
	}
	
	protected final ItItem createNewTestPortNetworkChassis(String itemName, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS));
		item.setModel(getModel(45L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );
		
		// CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", null);
		
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		// item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestPortDeviceChassis(String itemName, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS));
		item.setModel(getModel(45L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );
		
		// CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", null);
		
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		// item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setGroupingName(itemName);
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestPortDeviceChassis(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DEVICE) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		item.setModel(getModel(256L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );
		
		// CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", null);
		
		item.setGroupingName(itemName);
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestNetworkBlade(String itemName, Long slotPosition) throws Throwable { // bunty
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE));
		item.setModel(getModel(46L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		/*item.setParentItem(getItem(9L)); //cabinet 1G
		item.setSlotPosition(8L);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );

		ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		item.setBladeChassis(chassis);*/
		
		// set the cabinet
		CabinetItem cabinetItem = createNewTestCabinet("UNITTEST-Cabinet", SystemLookup.ItemStatus.INSTALLED);
		
		item.setParentItem(cabinetItem);
		item.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		
		// set the chassis
		ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		chassis.setParentItem(cabinetItem);
		chassis.setUPosition(SystemLookup.SpecialUPositions.ABOVE);		
		chassis.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setBladeChassis(chassis);
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.ChassisFace.FRONT));
		if (null != slotPosition) {
			item.setSlotPosition(slotPosition.longValue());
		}

		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestPortNetworkBlade(String itemName, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE));
		item.setModel(getModel(46L));				
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		/*if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		item.setSlotPosition(8L);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );*/

		/*ItItem chassis = createNewTestDeviceChassis("Unit test temp Chassis");
		item.setBladeChassis(chassis);*/
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	
	protected final ItItem createNewTestProbe(String itemName, Long status) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.PROBE) );
		item.setModel(getModel(3593L));
		item.setStatusLookup( SystemLookup.getLksData(session, status) );
		item.setDataCenterLocation( loc );	
		item.setParentItem(getItem(9L)); //cabinet 1G
		item.setUPosition(-1L); // Above		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewPortTestProbe(String itemName, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.PROBE) );
		item.setModel(getModel(3593L));
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		item.setUPosition(8L);		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setPsredundancy("N + 1");
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final Item createNewTestCRAC(String itemName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		Item item = new Item();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.CRAC) );
		item.setModel(getModel(1255L));
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}
	
	protected final ItItem createNewTestDataPanel(String itemName, Long statusValueCode) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DATA_PANEL) );
		item.setModel(getModel(54L)); // Ortronics / 2RU-48p RJ45,Flat
		
		if(statusValueCode == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusValueCode) );
		}
		item.setDataCenterLocation( loc );	
		item.setParentItem(getItem(9L)); //cabinet 1G
		item.setUPosition(-1L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT) );
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestPortDataPanel(String itemName, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.DATA_PANEL) );
		item.setModel(getModel(54L)); // Ortronics / 2RU-48p RJ45,Flat
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId)); //cabinet 1G
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		item.setUPosition(-1L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		
		itemHome.saveItem( item );
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final ItItem createNewTestNetworkStack(String itemName, String stackName) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.NETWORK_STACK));
		// item.setModel(getModel(51L));				
		item.setModel(getModel(5126L));
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		item.setDataCenterLocation( loc );	
		item.setParentItem(getItem(9L)); //cabinet 1G
		item.setUPosition(-1L); // Above		
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		
		itemHome.saveItem( item );
		
		item.setCracNwGrpItem(item);
		
		if(stackName == null){
			item.setGroupingName(itemName + "-STACK");
		}
		else{
			item.setGroupingName(stackName + "-STACK");
		}
		
		item.setGroupingNumber("Blade");
		session.update(item);
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	
	
	protected final String getRowLabel () {
		rowLabelPostfix++;
		String rowLabel = "A" + rowLabelPostfix.toString();
		return rowLabel;
	}
	

	protected final CabinetItem createNewTestCabinet(String itemName, Long statusLkpValueode) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		CabinetItem item = new CabinetItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.CABINET) );
		item.setPositionInRow(1);
		item.setRowLabel(getRowLabel());
		ModelDetails model = getModel(6400L);
		item.setModel(model);
		
		item.setLayoutHorizFront(StringUtils.leftPad("", model.getRuHeight(), '0'));
		item.setLayoutHorizRear(StringUtils.leftPad("", model.getRuHeight(), '0'));
		
		if(statusLkpValueode == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLkpValueode) );
		}
		
		item.setDataCenterLocation( loc );	
		
		itemHome.saveItem( item );
		
		session.flush();
			
		addTestItem( item );
		
		return item;
	}
	
	protected final CabinetItem getNewTestCabinet(String itemName, Long statusLksId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		CabinetItem item = new CabinetItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.CABINET) );
		item.setModel(getModel(6400L));		
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );	
		
		// itemHome.saveItem( item );
		
		// testItems.add( item );
		
		return item;
	}
	
	protected final CabinetItem createNewTestCabinetWithItems(String itemName, Long statusLksId) throws Throwable {
		CabinetItem cabinet = createNewTestCabinet(itemName, statusLksId);
		
		long idx = 1;
		
		for(; idx<5; idx++){
			ItItem item = this.createNewTestDevice(itemName + "-device-" + idx, statusLksId);
			item.setParentItem(cabinet);
			item.setUPosition(idx);
			session.update(item);
		}
		
		for(idx=1; idx<5; idx++){
			ItItem item = this.createNewTestPassive(itemName + "-passive-" + idx, cabinet.getItemId(), statusLksId);
			item.setParentItem(cabinet);
			item.setUPosition(idx + 5);
			session.update(item);
		}

		ItItem chassis = this.createNewTestDeviceChassis(itemName + "-chassis-" + idx);
		chassis.setParentItem(cabinet);
		chassis.setUPosition(idx);

		if(statusLksId == null){
			chassis.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			chassis.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}

		session.update(chassis);

		for(idx=1; idx<5; idx++){
			ItItem blade = createNewTestDeviceBladeInChassis(chassis.getItemName() + "-blade-" + idx, cabinet.getItemId(), chassis.getItemId(), statusLksId);
			blade.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.ChassisFace.FRONT));
			blade.setSlotPosition(idx);
			session.update(blade);
		}

			
		session.flush();
	
		return cabinet;
	}

	protected final CabinetItem getNewTestCabinetWithItems(String itemName, Long statusLksId) throws Throwable {
		CabinetItem cabinet = createNewTestCabinet(itemName, statusLksId);
		
		long idx = 1;
		
		for(; idx<5; idx++){
			ItItem item = this.createNewTestDevice(itemName + "-device-" + idx, statusLksId);
			item.setParentItem(cabinet);
			item.setUPosition(idx);
			session.update(item);
		}

		ItItem item = this.createNewTestDeviceChassis(itemName + "-chassis-" + idx);;
		item.setParentItem(cabinet);
		item.setUPosition(idx);
		item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		
		/*session.update(item);
			
		session.flush();
	*/
		return cabinet;
	}

	protected final Item createNewTestItemWithPorts(String itemName, Long modelId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		if(modelId == null){
			modelId = 1L;
		}
		
		Item item = new Item();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.NETWORK) );
		item.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS) );
		item.setModel(getModel(modelId));		
		item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );		
		item.setDataCenterLocation( loc );				
		//item.setParentItem(getItem(9L)); //cabinet 1G
		//item.setUPosition(20L);
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		
		//item.addDataPort(createDataPort(item, "Net1", 1));
		//item.addDataPort(createDataPort(item, "Net2", 2));
		item.addPowerPort(createPSPowerPort(item, "PS1", 1));
		//item.addPowerPort(createPSPowerPort(item, "PS2", 2));
		
		itemHome.saveItem( item );
		
		session.flush();
		
		session.refresh(item);
		
		addTestItem( item );
		
		return item;
	}	
	
	protected final DataPort createDataPort(Item item, String portName, int sortOrder) throws Throwable {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		DataPort port = new DataPort();		
		port.setPortName(portName);
		port.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.ACTIVE));
		// port.setPortStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		port.setConnectorLookup(new ConnectorLkuData(14));  //RJ45
		port.setMediaId(new LksData(61L));
		port.setProtocolID(new LkuData(1042L));
		port.setSpeedId(new LkuData(1030L));
		port.setSortOrder(sortOrder);
		port.setCreationDate(creationDate);
		port.setItem(item);
		
		return port;		
	}

	protected final PowerPort createPSPowerPort(Item item, String portName, int sortOrder) throws Throwable {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		PowerPort port = new PowerPort();		
		port.setPortName(portName);
		port.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.POWER_SUPPLY));
		// port.setPortStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		port.setConnectorLookup(new ConnectorLkuData(72));  
		port.setVoltsLookup(new LksData(71L));
		port.setPhaseLookup(new LksData(701L));
		port.setSortOrder(sortOrder);
		port.setCreationDate(creationDate);
		port.setItem(item);
		
		return port;		
	}

	protected final PowerPort createBranchCircuitBreakerPanelPort(Item item, String portName, int sortOrder) throws Throwable {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		PowerPort port = new PowerPort();		
		port.setPortName(portName);
		port.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
		// port.setPortStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		port.setConnectorLookup(new ConnectorLkuData(77));
		// port.setFaceLookup(new LksData(261L));
		port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_208, SystemLookup.LkpType.VOLTS));
		port.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE));
		port.setAmpsNameplate(90);
		port.setIsRedundant(false);
		port.setPowerFactor(1);
		port.setPhaseLegsLookup(SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
		port.setSortOrder(sortOrder);
		port.setCreationDate(creationDate);
		port.setItem(item);
		
		return port;		
	}

	protected final PowerPort createWhipOutletPowerPort(Item powerOutletItem, String portName, PowerPort breakerPort, int sortOrder) throws Throwable {
		Timestamp creationDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		PowerPort port = new PowerPort();	
		port.setPortName(portName);
		port.setPortSubClassLookup( SystemLookup.getLksData(session, SystemLookup.PortSubClass.WHIP_OUTLET));
		// port.setPortStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		port.setConnectorLookup(new ConnectorLkuData(55)); // "NEMA L6-30P" 
		// port.setFaceLookup(new LksData(261L));
		port.setVoltsLookup(SystemLookup.getLksData(session, SystemLookup.VoltClass.V_120, SystemLookup.LkpType.VOLTS));
		port.setPhaseLookup(SystemLookup.getLksData(session, SystemLookup.PhaseIdClass.SINGLE_3WIRE));
		port.setAmpsNameplate(28);
		port.setIsRedundant(false);
		port.setPowerFactor(1);
		port.setPhaseLegsLookup(SystemLookup.getLksData(session, SystemLookup.PhaseLegClass.ABC));
		port.setSortOrder(sortOrder);
		port.setCreationDate(creationDate);
		port.setItem(powerOutletItem);
		port.setBreakerPort(breakerPort);
		port.setAddress("dummy Address");
		// port.setBuswayItem(buswayItem);
		
		return port;		
	}


	
	protected final MeItem createNewTestPerfTiles(String itemName, Long statusLksId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.PERFORATED_TILES) );
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );
		
		itemHome.saveItem( item );
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final MeItem createNewTestRPDU(String itemName, Long statusLksId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
		item.setModel(getModel(1974L)); // APC / "AP7941": 1 - input cord, 24 - output cord
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );
		item.setParentItem(getItem(55L)); //cabinet BK
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.ZeroUDepth.REAR, "ZERO_U"));
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.LEFT_REAR, "RAILS_USED"));
		item.setUPosition(1L);
		
		itemHome.saveItem( item );
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final MeItem createNewTestPortRPDU(String itemName, Long statusLksId, long cabinetItemId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
		item.setModel(getModel(1974L)); // APC / "AP7941": 1 - input cord, 24 - output cord
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId)); 
		}
		else {
			item.setParentItem(getItem(9L)); //cabinet 1G
		}
		item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.ZeroUDepth.FRONT, "ZERO_U"));
		item.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.RIGHT_REAR, "RAILS_USED"));
		item.setUPosition(1L);
		
		itemHome.saveItem( item );
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	

	protected final MeItem createNewTestPortRPDU(String itemName, Long statusLksId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		MeItem item = new MeItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
		item.setModel(getModel(1974L)); // APC / "AP7941": 1 - input cord, 24 - output cord
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		item.setDataCenterLocation( loc );
		
		itemHome.saveItem( item );
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}	


	protected final ItItem createNewTestPassive(String itemName, Long cabinetItemId, Long statusLksId) throws Throwable {
		DataCenterLocationDetails loc = this.getTestLocation("Demo Site A");
		
		ItItem item = new ItItem();
		item.setItemName(itemName );
		item.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.PASSIVE) );
		
		if(statusLksId == null){
			item.setStatusLookup( SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED) );
		}
		else{
			item.setStatusLookup( SystemLookup.getLksData(session, statusLksId) );
		}
		
		if (cabinetItemId > 0) {
			item.setParentItem(getItem(cabinetItemId));
		}
		
		item.setDataCenterLocation( loc );
		
		itemHome.saveItem( item );
		
		session.flush();
		
		addTestItem( item );
		
		return item;
	}

	protected Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo) {
		Map<String,Object> targetMap = new HashMap<String, Object>();
		targetMap.put(item.getClass().getName(), item);
		targetMap.put(userInfo.getClass().getName(), userInfo);
		return targetMap;
	}
	
	protected MapBindingResult getErrorObject(Item item) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, item.getClass().getName());
		return errors;
	}
	
	protected MapBindingResult getErrorObject(Class<?> targetClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, targetClass.getName());
		return errors;
	}
	
	protected final DataCenterLocationDetails getTransientTestLocation() throws Throwable {
		DataCenterLocationDetails location = null;
		if (location == null) {
			String locationName = "UnitTestDC";
			location = getTestLocation(locationName);
			
			if(location == null){
				location = new DataCenterLocationDetails();
				location.setCode(locationName);
				location.setDcName(locationName);
				location.setArea(1000L);
				location.setComponentTypeLookup( SystemLookup.getLksData(session, SystemLookup.DcLocation.SITE) );
				location.setDataCenterLocationId(new Long(0));
			}
		}
		return location;
	}
	
	protected Date getDate(String dateFormat, String dateString) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date d = (Date) sdf.parse(dateString);
		
		return d;
	}

	protected HashMap<String, String> createCustomFields() {
//		Long lkuCustomFieldOssVersionId = 1064L;
//		String lkuCustomFieldFirmwareId = "1065";
//		Long lkuCustomFieldPatchLevelId = 1066L;
//		Long lkuCustomFieldWarrantyPeriodId = 1067L;
//		Long lkuCustomFieldFaceId = 1068L;
//		Long lkuCustomFieldTestFieldId = 1068L;
//		Long lkuCustomFieldkvmGatewayId = 2011L;
//		Long lkuCustomFieldmaintenanceDocId = 2012L;
//		Long lkuCustomFieldquickLaunchId = 2013L;
		String lkuCustomFieldmaintenanceDocId = "2012";
		HashMap<String, String> map = new HashMap<String, String>();
		
//		map.put(lkuCustomFieldOssVersionId, "1.6");
//		map.put(lkuCustomFieldFirmwareId, "1.0");
//		map.put(lkuCustomFieldPatchLevelId, "2");
//		map.put(lkuCustomFieldWarrantyPeriodId, "2years");
//		map.put(lkuCustomFieldFaceId, "1");
//		map.put(lkuCustomFieldTestFieldId, "Testing");
//		map.put(lkuCustomFieldkvmGatewayId, "TestGateway");
		map.put(lkuCustomFieldmaintenanceDocId, "TestDoc");
//		map.put(lkuCustomFieldquickLaunchId, "TestQlaunch");
		
		return map;
	}
	
	protected ValueIdDTO createValueIdDTOObj(String label, Object data) {
		ValueIdDTO dto = new ValueIdDTO();
		dto.setLabel(label);
		dto.setData(data);
		return dto;
	}
	
	protected void printError(Exception ex){
		ex.printStackTrace();
		
		if(ex.getCause() instanceof BusinessValidationException){
			BusinessValidationException be = (BusinessValidationException)ex.getCause();
			be.printValidationErrors();
		}    	
    }
    
	protected void printError(BusinessValidationException ex){    
		ex.printStackTrace();
		ex.printValidationErrors();
    }		

	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, Request.class.getName() );
		return errors;		
	}	
}
