package com.raritan.tdz.powerchain;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainLookup;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public class UpsBankTests  extends PowerChainTestBase {

	private final void fpduValidateCountAndValue(MeItem fpdu) {

		// validate fpdu input breaker count
		validateFPDUInputBreakerCount(fpdu, 1);
		
		// validate floor pdu breaker value
		validateFPDUBreakerValue(fpdu);
		
	}
	
	private final void upsBankValidateCountAndValue(MeItem upsBank) {

		// validate fpdu input breaker count
		validateUpsBankOutputBreakerCount(upsBank, 1);
		
		// validate floor pdu breaker value
		validateupsBankBreakerValue(upsBank);
		
		
	}

	@Test
	public final void upsBankDeleteTest() throws Throwable {
		LNEvent powerPanelCreate = new LNEvent(100, insertEvt, "dct_items", 
					3004L/*6206L*/, null, null, PowerChainLookup.Action.UPS_BANK_DELETE_BREAKER_PORT);
		
		List<String> expectedErrorCodes = new ArrayList<String>();
		processEventWithExpectedErrorCode(powerPanelCreate, expectedErrorCodes);
		
	}

	@Test
	public final void floorPDUToUpsBankConnectionCreate() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		MeItem upsBankItem = createNewTestUPSBank("INT_TEST_UPSBANK"); 
		
		createFloorPDUToUPSBankConnection(fpduItem, upsBankItem);
				
		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);

		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
		// change the bank item value
		changeUpsBankValues(upsBankItem);
		
		// update the value
		updateUPSBankValue(upsBankItem);
		
		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
	}
	
	@Test
	public final void floorPDUToUpsBankConnectionDelete() throws Throwable {
		
		MeItem fpduItem = createNewTestFloorPDUWithNoPanels("INT_TEST_FPDU");
		
		MeItem upsBankItem = createNewTestUPSBank("INT_TEST_UPSBANK"); 
		
		createFloorPDUToUPSBankConnection(fpduItem, upsBankItem);
				
		// validate fpdu input breaker 
		fpduValidateCountAndValue(fpduItem);

		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
		// validate the connection 
		validateConnectionCount(fpduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, upsBankItem, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 1L);
				
		// delete the connection from fpdu to ups bank
		deleteFPDUtoUPSBankConnection(fpduItem);
		
		// validate ups Bank output breaker
		upsBankValidateCountAndValue(upsBankItem);
		
		// validate the connection 
		validateConnectionCount(fpduItem, SystemLookup.PortSubClass.PDU_INPUT_BREAKER, upsBankItem, SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER, 0L);
		
	}
	


}
