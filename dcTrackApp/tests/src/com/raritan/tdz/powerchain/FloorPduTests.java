package com.raritan.tdz.powerchain;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Floor PDU port creation and connection test
 * @author bunty
 *
 */
public class FloorPduTests extends PowerChainTestBase {

	
	private final void fpduValidateCountAndValue(MeItem fpdu) {

		// validate fpdu input breaker count
		validateFPDUInputBreakerCount(fpdu, 1);
		
		// validate floor pdu breaker value
		validateFPDUBreakerValue(fpdu);
		
	}
	
	private final void fpduValidateUsed(MeItem fpdu, boolean expected) {
		validateFPDUUsedValue(fpdu, expected);
	}
	
	private final void upsBankValidateUsed(MeItem upsBank, boolean expected) {
		validateUpsBankUsedValue(upsBank, expected);
	}
	
	
	private final void upsBankValidateCountAndValue(MeItem upsBank) {

		// validate fpdu input breaker count
		validateUpsBankOutputBreakerCount(upsBank, 1);
		
		// validate floor pdu breaker value
		validateupsBankBreakerValue(upsBank);
		
	}
	
	@Test
	public final void floorPDUCreateTest() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);
		
	}

	@Test
	public final void floorPDUCreateWhenAlreadyExistTest() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);

		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);
		
	}


	@Test
	public final void floorPDUUpdateValues() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		// Create floor pdu input breaker port
		createfloorPDUInputBreaker(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);
		
		// change the value of fpdu
		changeFPDUValues(fpduItem);
		
		// update the value of the fpdu input breaker port
		updateFloorPDUInputBreakerValue(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);

	}
	
	@Test
	public final void floorPDUUpdateValuesWithNoBreakers() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		// update the value of the fpdu input breaker port
		updateFloorPDUInputBreakerValue(fpduItem);

		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);

	}
	
	@Test
	public final void floorPDUToUpsBankConnectionCreate() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		MeItem upsBankItem = createNewTestUPSBank("INT_TEST_UPSBANK"); 
		
		createFloorPDUToUPSBankConnection(fpduItem, upsBankItem);
				
		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);
		
		// validate used as true
		fpduValidateUsed(fpduItem, false);

		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
		// validate the connection 
		validateConnectionCount(fpduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, upsBankItem, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 1L);
		
		upsBankValidateUsed(upsBankItem, true);

	}

	// deleteFPDUtoUPSBankConnection
	@Test
	public final void floorPDUToUpsBankConnectionDelete() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		MeItem upsBankItem = createNewTestUPSBank("INT_TEST_UPSBANK"); 
		
		createFloorPDUToUPSBankConnection(fpduItem, upsBankItem);
				
		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);
		
		fpduValidateUsed(fpduItem, false);

		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
		upsBankValidateUsed(upsBankItem, true);
		
		// validate the connection 
		validateConnectionCount(fpduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, upsBankItem, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 1L);
		
		// delete the connection from fpdu to ups bank
		deleteFPDUtoUPSBankConnection(fpduItem);
		
		// validate the connection 
		validateConnectionCount(fpduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, upsBankItem, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 0L);
		
		Item upsBank = getItem(upsBankItem.getItemId());
		
		upsBankValidateUsed((MeItem)upsBank, false);
		
	}

	
	// @Test
	public final void updatePowerChainForExistingItems() throws BusinessValidationException {
		powerChainHome.createPowerChainForExistingItems();
	}

}
