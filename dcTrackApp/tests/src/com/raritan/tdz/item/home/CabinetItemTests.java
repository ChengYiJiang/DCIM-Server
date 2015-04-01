package com.raritan.tdz.item.home;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.tests.TestBase;


/**
 * Cabinet Unit tests.
 *  
 * @author Andrew Cohen
 */
public class CabinetItemTests extends TestBase {

	private List<Long> itemsToCleanup;
	private CabinetHome cabinetHome;
	
	@BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	itemsToCleanup = new ArrayList<Long>();
    	cabinetHome = (CabinetHome)ctx.getBean("cabinetHome");
    }

    @AfterMethod
    public void tearDown() throws Throwable {
    
    	
    	for (long itemId : itemsToCleanup) {
    		itemHome.deleteItem(itemId, false, null);
    	}
    	
    	super.tearDown();
    }
    
    /**
     * Test saving Rack PDU with minimum required fields.
     * @throws Throwable
     */
    @Test
  	public final void testCabinetBasicSave() throws Throwable {
  		List<ValueIdDTO> saveData = getCabinetSaveFields("testCabinetBasicSave");
  		
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		sf.getCurrentSession().flush();
  		
  		itemsToCleanup.add( SaveUtils.getItemId(itemData) );
  		
  		assertNotNull( itemData );
  	}
    
    @Test
  	public final void testCabinetSaveWithValidConfigurationInfo() throws Throwable {
    	List<ValueIdDTO> saveData = getCabinetSaveFields("testCabinetSaveWithValidConfigurationInfo");
  		
    	final long cabinetGrpLkuUd = 908L; 	// Cabinet Grouping - Storage
    	final int loadingCapacity = 201; 	// Loading Capacity
    	final int railWidth = 36;			// Rail Width
    	final int frontRailOffset = 1;		// Front Rail Offset
    	final int rearRailOffset = 2;		// Rear Rail Offset
    	final double frontDoorPerf = 25.0;	// Front Door Perforation %
    	final double rearDoorPerf = 50.0;	// Rear Door Perforation %
    	final int frontClearance = 1;		// Front Clearance
    	final int rearClearance = 2;		// Rear Clearance
    	final int leftClearance = 3;		// Left Clearance
    	final int rightClearance = 4;		// Right Clearance
    	
    	saveData.addAll( addCabinetConfigurationFields(
    			cabinetGrpLkuUd,
    			loadingCapacity,
    			railWidth,
    			frontRailOffset,
    			rearRailOffset,
    			frontDoorPerf,
    			rearDoorPerf,
    			frontClearance,
    			rearClearance,
    			leftClearance,
    			rightClearance
    		)
    	); 
    	
    	
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		sf.getCurrentSession().flush();
  		
  		itemsToCleanup.add( SaveUtils.getItemId(itemData) );
  		
  		assertNotNull( itemData );
    }
    
    @Test 
  	public final void testCabinetSaveWithInvalidConfigurationInfo() throws Throwable {
    	List<ValueIdDTO> saveData = getCabinetSaveFields("testCabinetSaveWithValidConfigurationInfo");
  		
    	final long cabinetGrpLkuUd = 908L; 	// Cabinet Grouping - Storage
    	final int loadingCapacity = 201; 	// Loading Capacity
    	final int railWidth = 36;			// Rail Width
    	final int frontRailOffset = 1;		// Front Rail Offset
    	final int rearRailOffset = 2;		// Rear Rail Offset
    	final double frontDoorPerf = 101.3;	// INVALID Front Door Perforation %
    	final double rearDoorPerf = -2.2;	// INVALID Rear Door Perforation %
    	final int frontClearance = 1;		// Front Clearance
    	final int rearClearance = 2;		// Rear Clearance
    	final int leftClearance = 3;		// Left Clearance
    	final int rightClearance = 4;		// Right Clearance
    	
    	saveData.addAll( addCabinetConfigurationFields(
    			cabinetGrpLkuUd,
    			loadingCapacity,
    			railWidth,
    			frontRailOffset,
    			rearRailOffset,
    			frontDoorPerf,
    			rearDoorPerf,
    			frontClearance,
    			rearClearance,
    			leftClearance,
    			rightClearance
    		)
    	); 
    	
    	Map<String,UiComponentDTO> itemData = null;
    	try {
    		itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
    		sf.getCurrentSession().flush();
    	}
    	catch (BusinessValidationException be) {
    		// Check that we get the validation errors for both front and rear door perforation
    		List<String> errors = be.getValidationErrors();
    		assertNotNull( errors );
    		assertEquals(errors.size(), 2);
    		for (String err : errors) {
    			assertTrue( err != null && err.toLowerCase().contains("perforation") );
    		}
    	}
    	finally {
    		if (itemData != null) {
    			itemsToCleanup.add( SaveUtils.getItemId(itemData) );
    		}
    	}
    	
    	assertNull( itemData );
    }
    
  	//
  	// Private methods
  	//
  	private List<ValueIdDTO> getCabinetSaveFields(String cabinetName) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.addAll( SaveUtils.addItemHardwareFields(20L, 1259L) ); 			// HP / 40RU-HP Cabinet
  		fields.add( SaveUtils.getField("cmbLocation", 1L) ); 					// Site A
  		fields.addAll( SaveUtils.addItemIdentityFields(cabinetName, null) );
  		
  		return fields;
  	}
  	
  	private List<ValueIdDTO> addCabinetConfigurationFields(long cabGrpLkuId, int loadCapacity, int railWidth,
  			int frontRailOffset, int rearRailOffset, double frontDoorPerf, double rearDoorPerf,
  			int frontClearance, int rearClearance, int leftClearance, int rightClearance) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.add( SaveUtils.getField("cmbCabinetGrouping", cabGrpLkuId) );
  		fields.add( SaveUtils.getField("tiLoadCapacity", loadCapacity) );
  		fields.add( SaveUtils.getField("tiRailWidth", railWidth) );
  		fields.add( SaveUtils.getField("tiFrontRailOffset", frontRailOffset) );
  		fields.add( SaveUtils.getField("tiRearRailOffset", rearRailOffset) );
  		fields.add( SaveUtils.getField("tiFrontDoorPerforation", frontDoorPerf) );
  		fields.add( SaveUtils.getField("tiRearDoorPerforation", rearDoorPerf) );
  		fields.add( SaveUtils.getField("tiFrontClearance", frontClearance) );
  		fields.add( SaveUtils.getField("tiRearClearance", rearClearance) );
  		fields.add( SaveUtils.getField("tiLeftClearance", leftClearance) );
  		fields.add( SaveUtils.getField("tiRightClearance", rightClearance) );
  		
  		return fields;
  	}
  	/*
  	@Test void testGetAllCabinet(){
  		List<ValueIdDTO> recList = cabinetHome.getAllCabinets(1L);
  		
  		for(ValueIdDTO c:recList){
  			System.out.println(c.toString());
  		}
  	}
  	*/
  	
}
