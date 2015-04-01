package com.raritan.tdz.item.home;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;


/**
 * Rack PDU Unit tests.
 *  
 * @author Andrew Cohen
 */
public class RackPDUItemTests extends TestBase {

	private List<Long> itemsToCleanup;
	
	@BeforeMethod
    public void setUp() throws Throwable {
    	super.setUp();
    	itemsToCleanup = new ArrayList<Long>();
    }

    @AfterMethod
    public void tearDown() throws Throwable {
    	super.tearDown();
    	
    	for (long itemId : itemsToCleanup) {
    		itemHome.deleteItem(itemId, false, null);
    	}
    }
    
    /**
     * Test saving Rack PDU with minimum required fields.
     * @throws Throwable
     */
    //@Test
  	public final void testRackPDUBasicSave() throws Throwable {
  		List<ValueIdDTO> saveData = getRackPDUSaveFields("testRackPDUBasicSave");
  		
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		sf.getCurrentSession().flush();
  		
  		itemsToCleanup.add( SaveUtils.getItemId(itemData) );
  		
  		assertNotNull( itemData );
  	}
    
    //@Test
  	public final void testRackPDUSaveValidGroupNumber() throws Throwable {
  		saveWithValidGroupNumber( 1234 );
  	}
    
    //@Test
  	public final void testRackPDUSaveInvalidGroupNumber1() throws Throwable {
    	saveWithInvalidGroupNumber(-12);
  	}
    
    //@Test
  	public final void testRackPDUSaveInvalidGroupNumber2() throws Throwable {
    	saveWithInvalidGroupNumber(10000);
  	}
    
  	//
  	// Private methods
  	//
  	private List<ValueIdDTO> getRackPDUSaveFields(String rackPduName) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.addAll( SaveUtils.addItemHardwareFields(61L, 1446L) ); // Raritan, Dominion PX DPCR20-20
  		fields.addAll( SaveUtils.addItemIdentityFields(rackPduName, null) );
  		fields.addAll( SaveUtils.addRackablePlacementFields(
  							1L,		 						// Site A
  							10L,							// Cabinet 1H
  							SystemLookup.RailsUsed.BOTH		// Both Rails Used
  						)
  		);
  		
  		return fields;
  	}
  	
  	private void saveWithInvalidGroupNumber(Object badGroupNumber) throws Throwable {
  		try {
    		saveWithValidGroupNumber( badGroupNumber );
    		assertNotNull(null, "Saved successfully with bad group number " + badGroupNumber.toString() + "!");
    	}
    	catch (BusinessValidationException be) {
    		assertTrue( be.getValidationErrors().size() > 0 && 
    				be.getValidationErrors().get(0).toLowerCase().contains("number in group") );
    	}
  	}
  	
  	private void saveWithValidGroupNumber(Object groupNumber) throws Throwable {
  		List<ValueIdDTO> saveData = getRackPDUSaveFields("testRackPDUBasicSave");
  		
  		saveData.add( SaveUtils.getField("tiGroup", "My Group") );
  		saveData.add( SaveUtils.getField("tiNoInGroup", groupNumber) );
  		
  		Map<String,UiComponentDTO> itemData = itemHome.saveItem(-1L, saveData, getTestAdminUser());
  		sf.getCurrentSession().flush();
  		
  		itemsToCleanup.add( SaveUtils.getItemId(itemData) );
  	}
}
