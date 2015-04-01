package com.raritan.tdz.item.home;


import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import static org.testng.Assert.assertTrue;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.item.home.modelchange.*;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * 
 */

/**
 * @author Santo Rosario
 *
 */
public class ItemSubClassChangeTest extends TestBase {

	ItemHome itemHome;
	ChangeModelFactory changeModelFactory;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemHome = (ItemHome)ctx.getBean("itemHome");
		changeModelFactory = (ChangeModelFactory)ctx.getBean("changeModelFactory");
	}

	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

    //STANDARD TO VM, CHASSIS, BLADE AND FREE-STANDING(FS)
	@Test
	public void testPhysicalToVM() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDevice("PhysicalToVM-001", null); 
		//ItItem itemToSave = (ItItem) this.getItem(342L);
		ItItem itemInDB = cloneItItem(itemToSave);		
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE));
				
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);

		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardToVM);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}	
	

	@Test
	public void testPhysicalToPhysicalFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDevice("PhysicalToFS-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(100)); //free standing model
				
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardToStandardFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}	


	@Test
	public void testPhysicalToBladeChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDevice("PhysicalToFS-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1015)); //chassis model
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardToBladeChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}	
	

	@Test
	public void testPhysicalToBladeServer() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDevice("PhysicalToFS-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1020)); //blade server model
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardToBladeServer);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}
	
	//=======================================================================================
    //STANDARD FREE-STANDING TO VM, CHASSIS, BLADE, STANDARD
	@Test
	public void testPhysicalFSToPhysical() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceFS("PhysicalFSToPhysical-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1L)); //standard server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardFSToStandard);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}	

	@Test
	public void testPhysicalFSToBladeChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceFS("PhysicalFSToBladeChassis-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1015L)); //chassis server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardFSToBladeChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}
	
	@Test
	public void testPhysicalFSToBladeServer() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceFS("PhysicalFSToBladeServer-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1020L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardFSToBladeServer);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
				
	}	

	@Test
	public void testPhysicalFSToVM() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceFS("PhysicalFSToVM-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(null); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StandardFSToVM);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}
	}	
		
	//=======================================================================================
    //BLADE CHASSIS TO STANDARD FREE-STANDING, VM, BLADE, STANDARD
	@Test
	public void testBladeChassisToPhysical() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceChassis("BladeChassisToPhysical-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1L)); //standard server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeChassisToStandard);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testBladeChassisToPhysicalFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceChassis("BladeChassisToPhysicalFS-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(100L)); //standard fs server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeChassisToStandardFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testBladeChassisToBladeServer() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceChassis("BladeChassisToBladeServer-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1020L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeChassisToBlade);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	

	@Test
	public void testBladeChassisToVM() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceChassis("BladeChassisToVM-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(null); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeChassisToVM);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	

	
	//=======================================================================================	
    //BLADE TO STANDARD FREE-STANDING, VM, CHASSIS, STANDARD
	@Test
	public void testBladeServerToPhysical() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem chassis = this.createNewTestDeviceChassis("BladeServerChassis1"); 
		ItItem itemToSave = this.createNewTestDeviceBlade("BladeServerToPhysical-002"); 
		itemToSave.setBladeChassis(chassis);
		session.flush();
		
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1L)); //standard server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeServerToStandard);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
			session.update(itemToSave);
		}	
		
		session.flush();
		
		itemToSave = null;
	}	
	
	@Test
	public void testBladeServerToPhysicalFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceBlade("BladeServerToPhysicalFS-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(100L)); //standard fs server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeServerToStandardFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	

	@Test
	public void testBladeServerToBladeChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceBlade("BladeServerToBladeChassis-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1015L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeServerToBladeChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
		
	@Test
	public void testBladeServerToVM() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceBlade("BladeServerToVM-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(null); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.VIRTUAL_MACHINE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof BladeServerToVM);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	//=======================================================================================	
    //VM TO BLADE, STANDARD FREE-STANDING, CHASSIS, STANDARD
	@Test
	public void testVMToPhysical() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceVM("VMToPhysical-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1L)); //standard server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		itemToSave.setParentItem(getItem(9L)); //cabinet 1G
		itemToSave.setUPosition(20L);
		itemToSave.setMountedRailLookup(SystemLookup.getLksData(session, SystemLookup.RailsUsed.FRONT) );
		
		System.out.print("\n+++++++++++++++++++++++++\n");
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof VMToStandard);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testVMToPhysicalFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceVM("VMToPhysicalFS-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(100L)); //standard fs server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof VMToStandardFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	

	@Test
	public void testVMToBladeChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceVM("VMToBladeChassis-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1015L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof VMToBladeChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
		
	@Test
	public void testVMToBladeServer() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestDeviceVM("VMToBladeServer-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(1015L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE_SERVER));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof VMToBladeServer);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//NETWORK ITEMS
	//=======================================================================================
    //NETWORK CHASSIS TO STANDARD FREE-STANDING, VM, BLADE, STANDARD
	@Test
	public void testNetChassisNetFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkChassis("NetChassisNetFS-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(18889L)); //network fs server
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkChassisToNetworkFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testNetChassisToNetBlade() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkChassis("NetChassisToNetBlade-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(46L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkChassisToNetworkBlade);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	

	@Test
	public void testNetChassisToStackable() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkChassis("NetChassisToStackable-001"); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(51L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.NETWORK_STACK));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkChassisToStackable);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	

	//=======================================================================================
    //NETWORK BLADE TO STANDARD FREE-STANDING, VM, CHASSIS, STANDARD
	@Test
	public void testNetBladeToNetChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkBlade("NetBladeToNetChassis-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(45L)); //chassis server
		itemToSave.setSubclassLookup(SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkBladeToNetworkChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testNetBladeToNetFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkBlade("NetBladeToNetFS-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(18889L)); //model name = "CoreDirector FS"
		itemToSave.setSubclassLookup(SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkBladeToNetworkFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	

	@Test
	public void testNetBladeToStackable() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkBlade("NetBladeToStackable-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(51L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.NETWORK_STACK));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof NetworkBladeToStackable);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	//=======================================================================================
    //STACKABLE TO CHASSIS, BLADE and CHASSIS  FS
	@Test
	public void testStockableToNetChassis() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkStack("StockableToNetChassis-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(45L)); //chassis server
		itemToSave.setSubclassLookup(SystemLookup.getLksData(session, SystemLookup.SubClass.CHASSIS));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StackableToNetworkChassis);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	@Test
	public void testStockableToNetFS() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkStack("NetBladeToNetFS-001", null);		
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(18889L)); //network fs server 
		itemToSave.setSubclassLookup(SystemLookup.getLksData(session, SystemLookup.SubClass.RACKABLE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StackableToNetworkFS);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	

	@Test
	public void testStockableToNetBlade() throws Throwable {
		session = sf.getCurrentSession();
		
		ItItem itemToSave = this.createNewTestNetworkStack("NetBladeToStackable-001", null); 
		ItItem itemInDB = cloneItItem(itemToSave);
		
		session.evict(itemInDB);
		session.clear();
		
		itemToSave.setModel(getModel(46L)); 
		itemToSave.setSubclassLookup( SystemLookup.getLksData(session, SystemLookup.SubClass.BLADE));
		
		System.out.print(itemInDB.getClassMountingFormFactorValue() + ":");
		System.out.println(itemToSave.getClassMountingFormFactorValue());
		
		ChangeModel changeModel = changeModelFactory.getChangeModel(itemInDB, itemToSave);
		
		AssertJUnit.assertNotNull(changeModel);
		AssertJUnit.assertTrue(changeModel instanceof StackableToNetworkBlade);
		
		if(changeModel != null){
			changeModel.change(itemInDB, itemToSave);
		}			
	}	
	
	//@Test
	public void testFunction(){
		try {
			validatePortUsage(DataPort.class, 233L);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printValidationErrors();
			e.printStackTrace();
		}
		try {
			validatePortUsage(PowerPort.class, 233L);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printValidationErrors();
			e.printStackTrace();
		}
		
		try {
			Item item = this.getItem(5036L);
			
			validateChassisAssociation(item);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printValidationErrors();
			e.printStackTrace();
		}	
		
		try {
			Item item = getExistingItem(5315L, "Cabinet", null);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	
	//@Test
	public void testCchangeDPSubclassToVirtual() throws BusinessValidationException {
		Session session = this.sf.getCurrentSession();
		LksData subclass = SystemLookup.getLksData(session, SystemLookup.PortSubClass.VIRTUAL, "PORT_SUBCLASS");
		
		this.updateDataPortSubClass(null, 49285L, subclass);
	}
	
	private void validatePortUsage(Class portClass, long itemId) throws BusinessValidationException{
		Session session = sf.getCurrentSession();
		Criteria c = session.createCriteria(portClass);
		c.createAlias("item", "item");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("portName"), "portName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("item.itemId", itemId));
		c.add(Restrictions.eq("used", true));
		
		StringBuffer portName = new StringBuffer(); 
		int count=0;
		
		for(Object rec:c.list()){
			List row = (List) rec;

			portName.append("\t").append(row.get(0)).append("\n");
			count++;
		}
		
		if(count > 0){
			//Create a business validation exception out of the errors
			String msg = portName.toString();	
			BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
			be.addValidationError(msg);
			throw be;
		}
	}

	public void validateChassisAssociation(Item item) throws BusinessValidationException{
		Session session = sf.getCurrentSession();
		Criteria c = session.createCriteria(Item.class);
		c.createAlias("bladeChassis", "chassis");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("itemName"), "itemName");		
		c.setProjection(proList);		
		c.setResultTransformer(Transformers.TO_LIST);
		
		c.add(Restrictions.eq("chassis.itemId", item.getItemId()));
		
		StringBuffer tempName = new StringBuffer(); 
		int count=0;
		
		for(Object rec:c.list()){
			List row = (List) rec;

			tempName.append("\t").append(row.get(0)).append("\n");
			count++;
		}
		
		if(count > 0){
			//Create a business validation exception out of the errors
			Object args[]  = {tempName.toString() };
			String code = "ItemValidator.invalidDefinitionModelChassis";
			String msg = "Blade Impacted: " + tempName.toString();	
			BusinessValidationException be = new BusinessValidationException( new ExceptionContext(msg, this.getClass()) );
			be.setCallbackURL("itemService.saveItem");
			be.addValidationError(msg);
			be.addValidationWarning(code, msg);
			throw be;
		}
	}

	public Item getExistingItem(Long itemId, String itemClass, ModelDetails itemModel) throws ClassNotFoundException, BusinessValidationException{
		Item result = null;
		Session session = sf.getCurrentSession();

		Item itemDB = (Item)session.get(Item.class, itemId);

		if(itemDB != null){
			ModelDetails itemModelDB = itemDB.getModel();

			if(itemModelDB != null){
				if (itemModelDB.isCabinetItemModel()){
						result = (Item)loadItem(CabinetItem.class, itemId, false);
				} else if (itemModelDB.isItItemModel()) {
						result = (Item)loadItem(ItItem.class, itemId, false);
				} else if (itemModelDB.isMeItemModel()){
						result = (Item)loadItem(MeItem.class, itemId, false);
				}
			}
			
			if(result == null)	result = (Item)loadItem(Item.class, itemId, false);

			if(result == null) result = itemDB;


			return result;
		}
		
		return result;
	}

	public Object loadItem(Class<?> domainType, Long id, boolean readOnly){
		Object result = null;
		Session session = null;

		try {
		if (id != null && id > 0){
			session = sf.openSession();
			Criteria criteria = session.createCriteria(domainType);
			criteria.createAlias("itemServiceDetails", "itemServiceDetails");
			//We are doing an EAGER loading since we will be filling the data with what
			//client sends and none of them should be null;
			criteria.setFetchMode("model", FetchMode.JOIN);
			criteria.setFetchMode("dataCenterLocation", FetchMode.JOIN);
			criteria.setFetchMode("parentItem", FetchMode.JOIN);
			criteria.setFetchMode("itemServiceDetails", FetchMode.JOIN);
			criteria.setFetchMode("itemServiceDetails.itemAdminUser", FetchMode.JOIN);
			criteria.setFetchMode("customFields", FetchMode.JOIN);
			criteria.setFetchMode("cracNwGrpItem", FetchMode.JOIN);
			criteria.add(Restrictions.eq("itemId", id));
			criteria.setReadOnly(readOnly);
			
			result = criteria.uniqueResult();
		}
		} finally {
			if (session != null){
				session.close();
			}
		}

		return result;
	}

	
	public void updateDataPortSubClass(Item item, long portId, LksData subclass) throws BusinessValidationException{
		log.debug("updateDataPortSubClass()");
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		Session session = sf.getCurrentSession();		
		/*DataPort port = (DataPort)session.get(DataPort.class, portId);

		port.setPortSubClassLookup(subclass);		
		port.setUpdateDate(currentDate);		
		session.update(port);*/
		

		//Query q = session.createQuery("delete from PowerPort where item.itemId = :itemId");
		Query q = session.createSQLQuery("update dct_ports_data set subclass_lks_id = :subclassLksId, update_date = now() where port_data_id = :portId");
		q.setLong("subclassLksId", subclass.getLksId());
		q.setLong("portId", portId);
		
		int updated = q.executeUpdate();
		
		if (log.isDebugEnabled()) {
			log.debug("Updated " + updated + " data ports");
		}	
	}
		
}


