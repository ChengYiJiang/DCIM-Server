package com.raritan.tdz.item.home;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelMfrDetails;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.ChassisTracerHandler;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;



public class ItemDTOAdapterTests  extends TestBase {
	private ItemDTOAdapter adapter;
	private ItemHome itemHome;
	
	private static final Map<String, Map<String,String>> constRestrictionsMap = 
			Collections.unmodifiableMap(new HashMap<String, Map<String,String>>() {{
				put("Hi", 
						new HashMap<String,String>(){{
							put("Bozana", "Radenkovic");
							put("How", "Are you");
						}}
					);
				put("Cao",
						new HashMap<String,String>(){{
							put("Thomas", "Breitfeld");
							put("feels", "good");
						}}
					);
			}});
	
	
	
	private static final Map<String, Object> itemDetails1=
			Collections.unmodifiableMap(new HashMap<String, Object>() {{
				put("tiRailWidth",0.0);
				put("cmbVMCluster",null);
				put("cmbOrientation","Item Front Faces Cabinet Front");
				put("cmbModel","42RU-TeraFrame Cabinet 42D");
				put("cmbContractNumber",null);
				put("tiRAM",null);
				put("cmbSystemAdmin",null);
				put("cmbCabinet",null);
				put("cmbChassis",null);
				put("tiRearClearance",0.0);
				put("tiFrontRailOffset",0.0);
				put("tiCpuQuantity",null);
				put("dtInstallationDate",null);
				put("dtContractEndDate",null);
				put("cmbDomain",null);
				put("tiLeftClearance",0.0);
				put("tiGroup",null);
				put("tiPONumber",null);
				put("cmbCustomer",null);
				put("tiPurchasePrice",1389.46);
				put("cmbSLAProfile",null);
				put("tiRearDoorPerforation",0.0);
				put("tiSubClass",null);
				put("cmbRowLabel","1");
				put("tiCpuType",null);
				put("cmbSlotPosition",-9);
				put("_tiSkipValidation",null);
				put("cmbUPosition",-9);
				put("tiCustomField_2012","");
				put("tiLoadCapacity",0.0);
				put("radioFrontFaces","West");
				put("cmbOperatingSystem",null);
				put("tiRightClearance",0.0);
				put("tiNoInGroup",0);
				put("cmbLocation","SITE A");
				put("cmbCabinetGrouping","Storage");
				put("cmbRowPosition",8);
				put("cmbSystemAdminTeam",null);
				put("tiUsers",null);
				put("tieAssetTag",null);
				put("tiDimension","73.5 X 24.0 X 42.3");
				put("tiFrontClearance",0.0);
				put("cmbOrder",null);
				//		put("dtPurchaseDate",1173416400000);
				put("tiNotes",null);
				put("id","10");
				put("cbPropagate",true);
				put("tiSerialNumber",null);
				put("radioDepthPosition","Front");
				put("tiLocationRef",null);
				put("radioCabinetSide",null);
				put("cmbType","Cabinet");
				put("radioRailsUsed",null);
				put("dtContractStartDate",null);
				put("cmbMake","CPI");
				put("tiName","01BRH");
				put("tiFrontDoorPerforation",0.0);
				put("tiAlias",null);
				put("cmbOSILayer",null);
				put("tiProcesses",null);
				put("tiRearRailOffset",0.0);
				put("tiServices",null);
				put("tiContractAmount",0.0);
				put("radioChassisFace","Front");
				put("tiWeight",125.0);
				put("cmbStatus","Installed");
				put("tiAssetTag",null);
				put("tiFormfactor","4-Post Enclosure");
				put("cmbProjectNumber",null);
				put("tiMounting","Free-Standing / 4-Post Enclosure");
				put("tiRackUnits",42);
				put("cmbFunction",null);
				put("tiClass","Cabinet");

			}});
	
	private static final Map<String, Object> deviceStandard=
			Collections.unmodifiableMap(new HashMap<String, Object>() {{
				put("tiRailWidth", null);
				put("cmbVMCluster", null);
				put("cmbOrientation", "Item Front Faces Cabinet Front");
				put("cmbModel", "Proliant DL360 G3");
				put("cmbContractNumber", null);
				put("tiRAM", null);
				put("cmbSystemAdmin", null);
				put("cmbCabinet", "2A");
				put("cmbChassis", null);
				put("tiRearClearance", null);
				put("tiFrontRailOffset", null);
				put("tiCpuQuantity", null);
				put("dtInstallationDate", null);
				put("cmbDomain", "nta.com");
				put("dtContractEndDate", null);
				put("tiLeftClearance", null);
				put("tiGroup", "1");
				put("tiPONumber", null);
				put("cmbCustomer", null);
				put("tiPurchasePrice", 0);
				put("cmbSLAProfile", null);
				put("tiRearDoorPerforation", null);
				put("cmbRowLabel", null);
				put("tiSubClass", "Standard");
				put("tiCpuType", null);
				put("tiCustomField_2011", "");
				put("_tiSkipValidation", null);
				put("tiCustomField_2013", "");
				put("tiCustomField_2012", "");
				put("tiLoadCapacity", null);
				put("radioFrontFaces", "Item Front Faces Cabinet Front");
				put("cmbOperatingSystem", null);
				put("tiRightClearance", null);
				put("tiNoInGroup", 0);
				put("cmbLocation", "SITE A");
				put("cmbCabinetGrouping", null);
				put("cmbRowPosition", null);
				put("cmbSystemAdminTeam", null);
				put("tiUsers", null);
				put("tieAssetTag", null);
				put("tiDimension", "1.75 X 16.78 X 27.25");
				put("tiFrontClearance", null);
				put("cmbOrder", null);
				put("dtPurchaseDate", null);
				put("tiNotes", null);
				put("cbPropagate", true);
				put("tiSerialNumber", null);
				put("radioDepthPosition", "Item Front Faces Cabinet Front");
				put("tiLocationRef", null);
				put("radioCabinetSide", "Both");
				put("cmbType", "Server");
				put("radioRailsUsed", "Both");
				put("dtContractStartDate", null);
				put("cmbMake", "HP");
				put("tiName", "0000-BRH-118");
				put("tiFrontDoorPerforation", null);
				put("tiAlias", "nydmw1");
				put("cmbOSILayer", null);
				put("tiProcesses", null);
				put("tiRearRailOffset", null);
				put("tiServices", null);
				put("tiContractAmount", 0);
				put("radioChassisFace", "Item Front Faces Cabinet Front");
				put("tiWeight", 37);
				put("cmbStatus", "Installed");
				put("tiAssetTag", null);
				put("tiFormfactor", "Fixed");
				put("cmbProjectNumber", null);
				put("tiMounting", "Rackable / Fixed");
			}});
	
	
	private static final Map<String, Object> testPrivileges1=
			Collections.unmodifiableMap(new HashMap<String, Object>() {{
				put("cmbModel", "Proliant DL360 G3");
				put("cmbSystemAdmin", "samern");
				put("cmbLocation", "SITE A");
				put("cmbSystemAdminTeam", "DC Staff");
				put("cmbMake", "HP");
				put("tiName", "0000-BRH-118");
			}});
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		adapter = (ItemDTOAdapter) ctx.getBean("itemDTOAdapter");
		itemHome = (ItemHome)ctx.getBean("itemHome");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test(enabled=true)
	public void testModel() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String modelName = "42RU-TeraFrame Cabinet 42D";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		Long id = adapter.getIdFromValueAndLocation("cmbModel", modelName, null, errors);
		checkErrors(errors);
		System.out.println("$$$$$$$ (" + modelName + ") id=" + id);
		Long actualModelId = getModelId(modelName);
		System.out.println("$$$ actualModelId=" + actualModelId);
		assertEquals("Unexpected result", true, id.longValue() == actualModelId.longValue());
	}

	private void checkErrors(Errors errors) {
		if(errors.hasErrors()) fail();
		
	}

	
	@Test(enabled=true)
	public void testMake() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String makeName="CPI";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());		
		Long id = adapter.getIdFromValueAndLocation("cmbMake", makeName, null, errors);
		System.out.println("$$$$$$$ (" + makeName + ") id=" + id);
		Long actualMakeId = getMakeId(makeName);
		System.out.println("$$$ actualMakeId=" + actualMakeId);
		assertEquals("Unexpected result", true, id.longValue() == actualMakeId.longValue());		
	}
	
	/**
	 * Test disabled. 
	 * Run this test only manually. It requires cabinet ABC in "SITE B" to pre-exist in DB
	 */
	@Test(enabled=false)
	public void testCabinet() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		Long siteId = adapter.getIdFromValue("cmbLocation", "SITE B", null, null, errors);
		Long id = adapter.getIdFromValueAndLocation("cmbCabinet", "ABC", siteId, errors);
		System.out.println("$$$$$$$ (ABC) id=" + id);
		//assertEquals("Unexpected result", true, id.longValue() == 12);		
	}
	@Test(enabled=true)
	public void testSysAdmin() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String userName = "samern";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());		
		Long id = adapter.getIdFromValueAndLocation("cmbSystemAdmin", userName, null, errors);
		System.out.println("$$$$$$$ (" + userName + ") id=" + id);
		Long actualUserId = getUserId(userName);
		System.out.println("$$$ actualUserId=" + actualUserId);
		assertEquals("Unexpected result", true, id.longValue() == actualUserId.longValue());		
	}
	
	@Test(enabled=true)
	public void testSysAdminTeam() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String adminTeam = "DC Staff";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());		
		Long id = adapter.getIdFromValueAndLocation("cmbSystemAdminTeam", adminTeam, null, errors);
		System.out.println("$$$$$$$ (" + adminTeam + ") id=" + id);
		Long actualAdminTeamId = getLkuId(adminTeam, "TEAM");
		System.out.println("$$$ actualAdminTeam=" + actualAdminTeamId);
		assertEquals("Unexpected result", true, id.longValue() == actualAdminTeamId.longValue());		
	}
	private Long getUserId(String userName){
		StringBuilder hql = new StringBuilder();
		hql.append("from Users where userId='");
		hql.append(userName);
		hql.append("'");
		Query query = session.createQuery(hql.toString());
		List<Users> dataList = query.list();
		assertEquals("Users not unique", true, dataList.size()==1);
		Users users = dataList.get(0);
		return users.getId();	
	}
	private Long getLkpValueCode(String lkpValue){
		StringBuilder hql = new StringBuilder();
		hql.append("from LksData where lkpValue='");
		hql.append(lkpValue);
		hql.append("'");
		Query query = session.createQuery(hql.toString());
		List<LksData> dataList = query.list();
		assertEquals("LksData not unique", true, dataList.size()==1);
		LksData lksData = dataList.get(0);
		return lksData.getLkpValueCode();	
	}
	private Long getLkuId(String lkuValue, String lkuType){
		StringBuilder hql = new StringBuilder();
		hql.append("from LkuData where lkuValue='");
		hql.append(lkuValue);
		hql.append("'");
		if( lkuType != null ){
			hql.append("and lkuTypeName='");
			hql.append(lkuType);
			hql.append("'");
		}
		Query query = session.createQuery(hql.toString());
		List<LkuData> dataList = query.list();
		assertEquals("LkuData not unique", true, dataList.size()==1);
		LkuData lkuData = dataList.get(0);
		return lkuData.getLkuId();
	}
	
	private Long getMakeId(String makeName){
		StringBuilder hql = new StringBuilder();
		hql.append("from ModelMfrDetails where mfrName='");
		hql.append(makeName);
		hql.append("'");
		Query query = session.createQuery(hql.toString());
		List<ModelMfrDetails> dataList = query.list();
		assertEquals("ModelMfrDetails not unique", true, dataList.size()==1);
		ModelMfrDetails make = dataList.get(0);
		return make.getModelMfrDetailId();	
	}
	private Long getModelId(String modelName){
		StringBuilder hql = new StringBuilder();
		hql.append("from ModelDetails where modelName='");
		hql.append(modelName);
		hql.append("'");
		Query query = session.createQuery(hql.toString());
		List<ModelDetails> dataList = query.list();
		assertEquals("ModelDetails not unique", true, dataList.size()==1);
		ModelDetails make = dataList.get(0);
		return make.getModelDetailId();	
	}
	@Test(enabled=true)
	public void testfacingLkpValue() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String lkpValue = "West";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		Long id = adapter.getIdFromValueAndLocation("radioFrontFaces", lkpValue, null, errors);
		System.out.println("$$$$$$$ (" + lkpValue + ") id=" + id);
		Long lkpValueCode =  getLkpValueCode(lkpValue);
		System.out.println("$$$$$$$ actual lkpValueCode=" + lkpValueCode);
		assertEquals("Unexpected result", true, id.longValue() == lkpValueCode.longValue());
	}
	
	@Test
	public void testFunction() throws DataAccessException, HibernateException, ClassNotFoundException, BusinessValidationException{
		Map<String, String> errorMap = new HashMap<String, String>();
		String lkuValue="Disaster Recovery";
		String lkuType = "FUNCTION";
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		Long id = adapter.getIdFromValueAndLocation("cmbFunction", lkuValue, null, errors);
		System.out.println("$$$$$$$ (Disaster Recovery) id=" + id);
		Long actualLkuId = getLkuId(lkuValue, lkuType);
		System.out.println("$$$ actualLkuId=" + actualLkuId);
		assertEquals("Unexpected result", true, id.longValue() == actualLkuId.longValue());
	}
	
	@Test
	public void testEntireList1() throws DataAccessException, ClassNotFoundException, BusinessValidationException{
		try{
			List<ValueIdDTO> ret = adapter.convertToDTOList(-1L, itemDetails1);
			assertEquals("Unexpected result", true, ret != null);
		}catch(DataAccessException e){
			e.printStackTrace();
			throw e;
		} catch (HibernateException e) {
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test(enabled=false)
	public void testRegEx(){
		String pattern = "^tiCustomField_[0-9]+";
		String uiId = "tiCustomField_2012789";
		boolean retval = uiId.matches(pattern);
		AssertJUnit.assertTrue(retval);
		uiId = "tiCustomField_0";
		retval = uiId.matches(pattern);
		AssertJUnit.assertTrue(retval);
	}
	
/*	private void addTracerHandlers(ObjectTracer oTracer) {
		
        oTracer.addHandler( "^[a-z].*LkpValue", new LksDataValueTracerHandler() );
        oTracer.addHandler( "^[a-z].*LkuValue", new LkuDataValueTracerHandler() );
        oTracer.addHandler( "^[a-z].*LkpValueCode", new LksDataValueTracerHandler() );
        oTracer.addHandler( "^[a-z].*LkuValueCode", new LkuDataValueTracerHandler() );
        oTracer.addHandler( "itemAdminUser.*", new UserIdTracerHandler() );
        oTracer.addHandler( "parentItem.*", new ParentTracerHandler() );
        oTracer.addHandler( "bladeChassis.*", new ChassisTracerHandler() );
        oTracer.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());
		
	}
	
	public void isUnique(String entityName, String entityProperty, Object value, String siteCode, String ignoreProperty, Object ignorePropertyValue) throws DataAccessException, HibernateException, ClassNotFoundException{
		Class<?> entityClass;
        entityClass = Class.forName(entityName);
		Session session = this.sf.getCurrentSession();

		ObjectTracer oTracer = new ObjectTracer();
		addTracerHandlers(oTracer);
		oTracer.traceObject(entityClass, entityProperty);
		String traceStr= oTracer.toString();		
		System.out.println("traceStr=" + traceStr + ", entityName=" + entityName + ", entityProperty=" + entityProperty + ", value=" + value);

		Criteria criteria = session.createCriteria(entityName);
		List<String> aliases  = oTracer.getAliases(traceStr);

		for( String alias: aliases){
			String alias_name = alias.replace(".", "_");
			criteria.createAlias(alias, alias_name);
			System.out.println("adding alias: " + alias_name + "for " + alias);
		}
		
		StringBuffer restrictionStr = new StringBuffer();
		if( aliases.size() > 0){
			restrictionStr.append(aliases.get(aliases.size()-1)).append(".");
		}
		restrictionStr.append(entityProperty);		
		System.out.println("restrictionStr=" + restrictionStr);

		criteria.add(Restrictions.eq(restrictionStr.toString(), value).ignoreCase());
		//addSiteRestriction(criteria, siteCode);
		
		if (ignoreProperty != null && ignorePropertyValue != null) {
			Object val = ignorePropertyValue;
			if (val instanceof Number) {
				val = ((Number)val).longValue();
			}
			criteria.add(Restrictions.ne(ignoreProperty, val));
		}
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property(entityProperty));
        criteria.setProjection(proList);
        List list = criteria.list();
        Iterator it = list.iterator();
        System.out.println("GOT: " + (String)it.next());
	}
	

	@Test
	public void testcabinetName() throws DataAccessException, HibernateException, ClassNotFoundException
	{
		//name should be considered as unique because of site restriction
		//in demo DB it exists on site A
		System.out.println("##### testCabinetName()...");
		String uiId = "cmbCabinet";
		String value = "1A";
		String siteCode = null;
		System.out.println("Testing if cabinetName: " + value + " exists (should)");
		

		String entity = adapter.getEntityForUiId(uiId);
		String property = adapter.getPropertyForUiId(uiId);
		isUnique(entity, property, value, siteCode, null, null);
	}*/
}
