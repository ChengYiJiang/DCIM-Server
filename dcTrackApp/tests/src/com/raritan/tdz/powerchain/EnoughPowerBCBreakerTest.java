package com.raritan.tdz.powerchain;

import java.util.HashMap;
import java.util.Map;

import org.h2.java.lang.System;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.tests.TestBase;

public class EnoughPowerBCBreakerTest extends TestBase{
    private EnoughPowerValidator enoughPowerValidator;

	UserInfo testUser;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		testUser = this.getTestAdminUser();
		testUser.setRequestBypass(true);
		
		enoughPowerValidator = (EnoughPowerValidator)ctx.getBean("enoughPowerValidatorAtBranchCircuitBreaker");
	}
/*
"RPDU-X1";55227;6109;"Input Cord"

"MAIN FPDU/PB1:1,2,3";55214;6104;"R1"


"BACKUP FPDU/PB1:1,2,3";55225;6106;"R2"
"BACKUP FPDU/PB1:1,2,3";55222;6106;"R1"



 */		
    //@Test Cannot find hardcoded id .. commenting the tests.
	public final void testOverLoadCircuit() throws Throwable {
    	PowerWattUsedSummary nodeInfo = new PowerWattUsedSummary();
    	Errors errors = getErrorObject();
    	nodeInfo.setNodePortIdToExclude(55227L);
    	nodeInfo.setLegs("123");
    	nodeInfo.setPbVolts(208.0);
    	nodeInfo.setPbPhaseVolts(120.0);
    	
    	//enoughPowerValidator.checkEnoughPower(9.30, 1000, 1, 55214L, 55204L, null, null, 0, null, errors, nodeInfo);
    	
    	enoughPowerValidator.checkEnoughPower(9.30, 1000, 1, 55214L, 55204L, null, null, 0, null, errors, nodeInfo);
    	
    	for(Object obj:errors.getAllErrors()) {
    		ObjectError e = (ObjectError)obj;
    		//System.out.println(e);
    	}
	}

	protected MapBindingResult getErrorObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, PowerCircuit.class.getName() );
		return errors;
		
	}	 
}
