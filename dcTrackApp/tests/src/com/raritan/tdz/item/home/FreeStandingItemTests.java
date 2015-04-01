package com.raritan.tdz.item.home;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.home.ModelHome;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;
import com.rits.cloning.Cloner;

public class FreeStandingItemTests extends TestBase  {
    private RulesProcessor processor;
    private RemoteRef remoteRef;
    private ModelHome modelHome;
	private Long m_itemId;
	private ItemDomainAdaptor itemDomainAdaptor;

    @BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	processor = (RulesProcessor) ctx.getBean("itemRulesProcessor");
    	remoteRef = (RemoteRef) ctx.getBean("remoteRefItemScreen");
    	modelHome = (ModelHome)ctx.getBean("modelHome");
    	itemDomainAdaptor = (ItemDomainAdaptor)ctx.getBean("itemDomainAdaptor");
    }

    @AfterMethod
    public void tearDown() throws Throwable {
    	super.tearDown();
    }
    

    private String getFieldTrace(String remoteType, String fieldName) throws ClassNotFoundException{
		//Create Alias
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString().replace(".", "/");
		return trace;
	}
  

    @Test
    public void foo() throws ClassNotFoundException {
    	String label = "cmbModel";
    	String remoteReference = processor.getRemoteRef(label);
    	String remoteType = remoteRef.getRemoteType(remoteReference);
			
    	Long id = 45L;
    	String remoteAlias = null;
    	String trace = null;
    	ModelDetails value;			

		remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
		trace = getFieldTrace(remoteType,remoteAlias);
		trace = trace.substring(0, trace.lastIndexOf("/"));
		System.out.println("remoteAlias=" + remoteAlias +", remoteType=" + remoteType);
		value = (ModelDetails) loadData(remoteType, remoteAlias,id);
		System.out.println("name: " + value.getModelName());
		System.out.println("modelId=" + value.getModelDetailId());
		
    }
    

    @Test
    public void testGettingModelByName() throws ClassNotFoundException {
    	String modelName = "Catalyst C6509-E";
		Long modelId = modelHome.getModelIdByName(modelName);
    	System.out.println("modelId=" + modelId + " for model: " + modelName);
    	
    	String label = "cmbModel";
		String remoteReference = processor.getRemoteRef(label);
		String remoteType = remoteRef.getRemoteType(remoteReference);
		
		String remoteAlias = remoteRef.getRemoteAlias(remoteReference, RemoteRefConstantProperty.FOR_ID);
		String trace = getFieldTrace(remoteType,remoteAlias);
		trace = trace.substring(0, trace.lastIndexOf("/"));
		ModelDetails model = (ModelDetails) loadData(remoteType, remoteAlias, modelId);
    	System.out.println("modelId= " + model.getModelDetailId());
    	System.out.println("modelName=" + model.getModelName());
    }

    
    private Object loadData(String remoteType, String fieldName, Object id) throws ClassNotFoundException {
		Object result = null;
		
		if(id != null && (Long)id > 0){
			//Create Alias
			ObjectTracer objectTrace = new ObjectTracer();
			objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
			objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
			objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
			objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
			objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
			objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
			List<Field> fields = objectTrace.traceObject(Class.forName(remoteType), fieldName);
			Field field = fields.get(fields.size() - 2);
			Session session = sf.getCurrentSession();
			Criteria criteria = session.createCriteria(field.getType());
			criteria.add(Restrictions.eq(fields.get(fields.size() - 1).getName(), id));
			result = criteria.uniqueResult();
		}
		return result;
	}
	 
	private Item getItemFromDTOMap(Map<String, UiComponentDTO> cabinetItemMap) {
		long itemId = -1;
		Item retval = null;
		UiComponentDTO componentDTO = cabinetItemMap.get("tiName");
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId =(Long) uiValueIdField.getValueId(); 
		}
		if( itemId > 0){
			Session session = sf.getCurrentSession();
			retval = (Item) session.load(Item.class, itemId);
		}
		return retval;
	}

	/*Note: We should explore other ways of deep copy, but test cloning
	 * performances in case do not find better solution
	 */
	@Test
	public void test_cloning()
	{
		Long l1 = new Long(90);
		Cloner cloner=new Cloner();
		Long l2 = cloner.deepClone(l1);
		System.out.println("l1=" + l1 + ", l2=" + l2);
		
	}
	
	@Test
	public void getCabinetContainerLksData() {
		Session session = sf.getCurrentSession();
		Criteria criteria = session.createCriteria(LksData.class);
		criteria.setProjection(Projections.property("lksId"));
		criteria.add(Restrictions.eq("lkpValueCode", SystemLookup.SubClass.CONTAINER));
        List<Long> list = criteria.list();
        assert (list.size() == 1);
        Long lksId = list.get(0);
        LksData subclass = (LksData)session.load(LksData.class, lksId);
        System.out.println("---- lks_id=" + subclass.getLksId() + ", lkp_value_code=" +
        		subclass.getLkpValueCode() + ", lkp_value=" + subclass.getLkpValue());
	}
    @Test
    public void testSaveDevice() throws BusinessValidationException, ClassNotFoundException, Throwable{
    	String itemName = "BRH Test Item1";
    	Long itemId = new Long(-1L);
    	long make = 7; //Sun
    	long model = 1843; //Enterprise 6500
    	long function = 970; //Device
    	long type = 1; //Device
    	
    	Map<String,UiComponentDTO> componentDTOMap = saveItem(itemName, itemId, make, model, function, type);
		AssertJUnit.assertNotNull(componentDTOMap);
		System.out.println("== list size=" + componentDTOMap.size());
		AssertJUnit.assertTrue(componentDTOMap.size() > 0);
		System.out.println("--testSaveDevice():");
		printUiComponentDTOMap(componentDTOMap);

		//cleanup
		m_itemId = -1L;
		Item item = getItemFromDTOMap(componentDTOMap);
		m_itemId = item.getItemId();
		cleanFreeStandingItem();
    }
    
    private long createNetworkModel(){
    	long modelId = -1L;
    	
        String SQL_INSERT_QUERY = "insert into dct_models ( model_name, class_lks_id, " +
        		"mounting, form_factor, ru_height, weight, " +
        		"num_ps_max, clearance_left, clearance_right, clearance_front, " + 
        		"clearance_rear, dim_h, dim_w, dim_d, rear_image, front_image, creation_date," + 
        		"created_by, status_lks_id, mfr_id, library_version, " +
        		"is_never_check, is_standard) " +
        		"VALUES ('BozanaFreeStandingNetwork', 2 , 'Free-Standing', 'Fixed', 40," +
        		" 881, 0, 0.00, 0.00, 0.00, 0.00, 70.00, 24.00, 20.00, FALSE," +
        		"FALSE, CURRENT_TIMESTAMP, 'admin', 1, 10, 0.000, FALSE, FALSE)";
        try{
        	SQLQuery qry = session.createSQLQuery(SQL_INSERT_QUERY);
        	assert (qry.executeUpdate() == 1);
        	System.out.println (qry.toString());
        	
        }catch(Exception e){
        	//FIXME: Hibernate creates this model and also throws excpetion complainign about
        	//SQL. Find out why!
        	System.out.println("Fix me..");
        }
        String SQL_SELECT_QUERY = "select model_id from dct_models where model_name " +
        		  "like 'BozanaFreeStandingNetwork'";
        
        List resp = session.createSQLQuery(SQL_SELECT_QUERY).list();
 //       AssertJUnit.assertTrue(resp.size() == 1);
        Number modelIdNum = (Number)resp.get(0);
        	
    	modelId = modelIdNum.longValue();
    	
    	return modelId;
    }
    
    private void deleteNetworkModel(){
    	Long modelId = -1L;
    	
        String SQL_DELETE_QUERY = "delete from dct_models where model_name " +
        		  "like 'BozanaFreeStandingNetwork'";
        try{
        	session.createSQLQuery(SQL_DELETE_QUERY).executeUpdate();
        }catch(Exception e){
        	//Hibernate deletes the model and also throws out exception
        	//complaining about SQL. Find out why.
        	System.out.println("Fix me..");
        }
    }
    
    @Test
    public void testSaveNetoworkItem() throws BusinessValidationException, ClassNotFoundException, Throwable{
    	String itemName = "BRH Test Item2";
    	Long itemId = new Long(-2L);
    	long make = 10; //APC
    	long model = createNetworkModel();
    	long function = 8; //Network switch
    	long type = 8;
    	
    	Map<String,UiComponentDTO> componentDTOMap = saveItem(itemName, itemId, make, model, function, type);
		AssertJUnit.assertNotNull(componentDTOMap);
		System.out.println("===== list size=" + componentDTOMap.size());
		AssertJUnit.assertTrue(componentDTOMap.size() > 0);
		
		System.out.println("##### testSaveNetworkItem():");
		printUiComponentDTOMap(componentDTOMap);

		//cleanup
		m_itemId = -1L;
		Item item = getItemFromDTOMap(componentDTOMap);
		m_itemId = item.getItemId();
		cleanFreeStandingItem();
		deleteNetworkModel();
    }
    
    
    @Test
    public void testEditDevice() throws BusinessValidationException, ClassNotFoundException, Throwable{
    	String itemName1 = "Test Item1";
    	System.out.println("--- Editing item: " + m_itemId);
    	m_itemId = -1L;
    	Long itemId = new Long(m_itemId);
    	long make = 7; //SUN
    	long model= 1843; //Enterprise 6500
    	long function = 970; //Device
    	long type = 1; //Device
    	Map<String,UiComponentDTO> componentDTOMap = saveItem(itemName1, itemId, make, model, function, type);
		AssertJUnit.assertNotNull(componentDTOMap);
		System.out.println("===== list size=" + componentDTOMap.size());
		AssertJUnit.assertTrue(componentDTOMap.size() > 0);
		
		System.out.println("##### testEditDevice():");
		printUiComponentDTOMap(componentDTOMap);
		
		//save for cleanup
		Item item = getItemFromDTOMap(componentDTOMap);
		m_itemId = item.getItemId();
		System.out.println("=== m_itemId=" + m_itemId);
		
		String newName = "Test Item2";
		String actualName = null;

    	Map<String,UiComponentDTO> componentDTOMap2 = saveItem(newName, m_itemId, make, model, function, type);
		AssertJUnit.assertNotNull(componentDTOMap2);
		System.out.println("===== list size=" + componentDTOMap2.size());
		AssertJUnit.assertTrue(componentDTOMap2.size() > 0);
		printUiComponentDTOMap(componentDTOMap2);

		//Parse through the returned list to find out actual item name
		//and verify it is the same as the one we set
		System.out.println("##### testSaveDevice() returned:");
		Set<String>keySet4 = componentDTOMap.keySet();
		for( String s: keySet4){
			UiComponentDTO componentDTO = componentDTOMap2.get(s);
			if( componentDTO != null){
				UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
				if(uiValueIdField != null ){
					if( uiValueIdField.getValue() != null){
						if( s.equals("tiName")){
							actualName = new String(uiValueIdField.getValue().toString());
						}
						System.out.println("key=" + s + ", uiValueIdField.valueId=" + 
							uiValueIdField.getValueId() + ", value=" + uiValueIdField.getValue().toString());
					}else{
						System.out.println("key=" + s + ", uiValueIdField.valueId=" + 
							uiValueIdField.getValueId());
					}
				}else{
					System.out.println("key=" + s + "val=");			
				}
			}else System.out.println("key=" + s + ", but val not exist");
		}

		//cleanup
		cleanFreeStandingItem();
		AssertJUnit.assertTrue(componentDTOMap2.size() > 0);
		AssertJUnit.assertTrue(actualName != null);
		System.out.println("actualName=" + actualName + ", expectedName=" + newName.toUpperCase());
		AssertJUnit.assertTrue(actualName.equals(newName.toUpperCase()) == true);
    }

  
    private List<ValueIdDTO> createValueIdDTOList(String newName, long make, long model, long function, long type){
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();
		
		//This is item model that will contain info if it is FreeStanding or not
		//I will use that one to create cabinet model that is also FreeStanding, but a little bit different
		
		valueIdDTOList.addAll( SaveUtils.addItemHardwareFields(make, model) ); //7 - Sun Micro, 10 - APC, //1843 - Enterprise 6500, 21496 - BozanaTestFreeSTandingNetworkItem
		valueIdDTOList.add( SaveUtils.getField("tiSerialNumber", "12111222233") );
		valueIdDTOList.add( SaveUtils.getField("tiAssetTag", "12234523103") );
		valueIdDTOList.add( SaveUtils.getField("tieAssetTag", "12234550000") );
		valueIdDTOList.add( SaveUtils.getField("tiName", newName) );
		valueIdDTOList.add( SaveUtils.getField("tiAlias", "BOZANA") );
		//valueIdDTOList.add( SaveUtils.getField("cmbType", new Long(type)) );
		//valueIdDTOList.add( SaveUtils.getField("cmbFunction", new Long(function)) );
		valueIdDTOList.add( SaveUtils.getField("cmbSystemAdmin", new Long(20)) );
		valueIdDTOList.add( SaveUtils.getField("cmbSystemAdminTeam", new Long(555)) );
		valueIdDTOList.add( SaveUtils.getField("cmbCustomer", new Long(566) ) );
		valueIdDTOList.add( SaveUtils.getField("cmbStatus", SystemLookup.ItemStatus.PLANNED ) );
		
		//Placement Panel Data
		valueIdDTOList.add( SaveUtils.getField("cmbLocation", new Long(1) ) );
		valueIdDTOList.add( SaveUtils.getField("cmbRowLabel", "A" ) );
		valueIdDTOList.add( SaveUtils.getField("cmbRowPosition", 11 ) );
		valueIdDTOList.add( SaveUtils.getField("radioFrontFaces", 7081) );
		valueIdDTOList.add( SaveUtils.getField("tiLocationRef", "My_Location_1") );
		
		return valueIdDTOList;
    }

	private void printUiComponentDTOMap(Map<String, UiComponentDTO> xyzMap) 
	{
		try {
			System.out.println("====== Map content: ");
			Set<String> keySet = xyzMap.keySet();
			for (String s1 : keySet) {
				UiComponentDTO componentDTO = xyzMap.get(s1);
				if (componentDTO != null) {
					UiValueIdField uiValueIdField = componentDTO
							.getUiValueIdField();
					if (uiValueIdField != null) {
						if (uiValueIdField.getValue() != null) {
							System.out.println("key=" + s1 + ", uiValueIdField.valueId="
									+ uiValueIdField.getValueId() + ", value="
									+ uiValueIdField.getValue().toString());
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
	
	/*
	 * CR46048 - If getItemDetails() is invoked twice on different items, the
	 * returned map in the fist call gets overwitten with data from second call
	 * temp fix - use cloner
	 */
	@Test
	public void test_fix_for_cr46048() throws Throwable {
		Long itemId = 57L; 
		Session session = sf.getCurrentSession();
		Item item = (Item) session.load(Item.class, itemId);
		String origItemName = item.getItemName();
		String expectedMap1Name = null;
		String dbName = null;
		String actualMap1Name1=null, actualMap1Name2=null;
		Cloner cloner = new Cloner();
		UserInfo userInfo = null; /*FIXME: Send userInfo */
		Map<String, UiComponentDTO> map1 = cloner.deepClone(itemHome.getItemDetails(item.getItemId(), userInfo));
		printUiComponentDTOMap(map1);
		System.out.println("====================");
		UiComponentDTO uiCmp1 = map1.get("tiName");
		
		if( uiCmp1 != null){
			UiValueIdField uiValueIdField = uiCmp1.getUiValueIdField();
			if(uiValueIdField != null ){
				if( uiValueIdField.getValue() != null){
					dbName = new String(item.getItemName());
					expectedMap1Name = new String((String)(uiValueIdField.getValue()));
					System.out.println("-- Reading map1 first time; expected name=" + dbName + ", actualName=" + 
							uiValueIdField.getValue().toString() + ", itemId=" + uiValueIdField.getValueId());
					actualMap1Name1 = new String((String)(uiValueIdField.getValue()));
					
					System.out.println("-- Actual name in DB is: item1.name=" + dbName);
				}else{
					System.out.println("key=tiName, uiValueIdField.valueId=" + 
							uiValueIdField.getValueId());
				}
			}else{
				System.out.println("key=tiName, val=");			
			}
		}else System.out.println("key=tiName, but val not exist");
		
		//Now change item name
		item.setItemName("HiBozana");		
		Item item2 = (Item)session.merge(item);
		session.flush();
		Map<String, UiComponentDTO> map2 = cloner.deepClone(itemHome.getItemDetails(item.getItemId(), null /* FIXME: sendUserInfo */));
		System.out.println("====================");
		printUiComponentDTOMap(map2);
		System.out.println("====================");
		UiComponentDTO uiCmp2 = map2.get("tiName");
		
		if( uiCmp2 != null){
			UiValueIdField uiValueIdField = uiCmp2.getUiValueIdField();
			if(uiValueIdField != null ){
				if( uiValueIdField.getValue() != null){
					dbName = new String(item2.getItemName());
					System.out.println("-- Reading map2 first time; expected name=" + dbName + ", actualName=" + 
							uiValueIdField.getValue().toString() + ", itemId=" + uiValueIdField.getValueId());
					System.out.println("Name in DB name2=" + dbName);
				}else{
					System.out.println("key=tiName, uiValueIdField.valueId=" + 
							uiValueIdField.getValueId());
				}
			}else{
				System.out.println("key=tiName, val=");			
			}
		}else System.out.println("key=tiName, but val not exist");
	
		//Revert the name back to original
		item2.setItemName(origItemName);
		item2 = (Item)session.merge(item2);
		session.flush();
		
		//Now check again map1:
		uiCmp1 = map1.get("tiName");
		System.out.println("--------------------------------");
		if( uiCmp1 != null){
			UiValueIdField uiValueIdField = uiCmp1.getUiValueIdField();
			if(uiValueIdField != null ){
				if( uiValueIdField.getValue() != null){
					actualMap1Name2 = new String((String)(uiValueIdField.getValue().toString()));
				
					System.out.println("###### Reading map1 again; expected name=" + expectedMap1Name + ", actualName=" + 
							actualMap1Name2 + ", itemId=" + uiValueIdField.getValueId());
					System.out.println("##### Actual name in DB is: item2.name=" + item2.getItemName());
				}else{
					System.out.println("key=tiName, uiValueIdField.valueId=" + 
							uiValueIdField.getValueId());
				}
			}else{
				System.out.println("key=tiName, val=");			
			}
		}else System.out.println("key=tiName, but val not exist");
		
		System.out.println("actualMap1Name1=" + actualMap1Name1 + ", actualMap1Name2=" + actualMap1Name2);
		AssertJUnit.assertNotSame(actualMap1Name1, actualMap1Name2);
	}
	
	protected UserInfo createGatekeeperUserInfo(){
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941",
				"", "1", "en-US", "site_administrators", 
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);
		
		return userInfo;
	}

    private Map<String,UiComponentDTO> saveItem(String itemName, Long itemId, long make, long model, long function, long type) throws BusinessValidationException, ClassNotFoundException, Throwable{
    	Map<String,UiComponentDTO> componentDTOMap = null;
    	//Create ValueDTO list
		List<ValueIdDTO> valueIdDTOList = createValueIdDTOList(itemName, make, model, function, type);
		UserInfo userInfo = createGatekeeperUserInfo();
    	componentDTOMap = itemHome.saveItem(itemId, valueIdDTOList, userInfo);
		return componentDTOMap;
    }
    
    private void cleanFreeStandingItem(){
    	System.out.println("$$$$$$ cleanup");

		try{
    		Session session = sf.getCurrentSession();
    		Item item = (Item)session.load(Item.class, m_itemId);
    		CabinetItem cabinet = (CabinetItem)item.getParentItem();
			ModelDetails model = cabinet.getModel();
    		/* commenting out local variables that are not used. */
    		//ItemServiceDetails cabinetDetails = (ItemServiceDetails)cabinet.getItemServiceDetails();
    		//ItItem itItem = (ItItem)session.load(ItItem.class, m_itemId);
    		//ItemServiceDetails itemDetails = (ItemServiceDetails)session.load(ItemServiceDetails.class, m_itemId);
    		
			itemHome.deleteItem(m_itemId, false, null);
			
    		/* deleteItem above takes care of deleting cabinet.item therefore commenting line below. */
			//itemHome.deleteItem(cabinet.getItemId(), false, null);
			
    		if( model != null ){
    			System.out.println("--- deleting model: " + model.getModelName() + ", model id=" + model.getModelDetailId());
    			session.delete(model);
    		}
    		session.flush();			
		}
		catch(Exception ex){
			ex.printStackTrace();
		} catch (BusinessValidationException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }
    
    @Test
	public void getFreeStandingItem() {
    	try{
    		//Map<String,UiComponentDTO> cabinet = this.itemService.getItemDetails(5034L);
    		Map<String,UiComponentDTO> item = this.itemService.getItemDetails(5035L);
    		
    		printUiComponentDTOMap(item);
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	} catch (Throwable e) {
			e.printStackTrace();
		}
    }
}

