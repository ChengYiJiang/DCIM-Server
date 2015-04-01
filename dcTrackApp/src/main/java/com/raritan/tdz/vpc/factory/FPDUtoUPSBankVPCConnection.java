package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.powerchain.home.FPDUBreakerToUPSBankBreakerUpdateActionHandler;
import com.raritan.tdz.powerchain.home.PowerChainActionHandlerHelper;

/**
 * creates connection from floor pdu to ups bank
 * @author bunty
 *
 */
public class FPDUtoUPSBankVPCConnection implements VPCConnection {

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired(required=true)
	private FPDUBreakerToUPSBankBreakerUpdateActionHandler floorPduBreakerPortToUpsUpdateConnectionActionHandler;
	
	@Override
	public void create(Map<String, List<Item>> vpcItems) throws BusinessValidationException {

		List<Item> fpdus = vpcItems.get(VPCItemFactory.FLOOR_PDU);
		List<Item> upsBanks = vpcItems.get(VPCItemFactory.UPS_BANK);
		
		if (fpdus.size() != 1 || upsBanks.size() != 1) return;
		
		Item fpdu = fpdus.get(0);
		Item upsBank = upsBanks.get(0);
		
		Errors powerChainErrors = powerChainActionHandlerHelper.getErrorObject();
		floorPduBreakerPortToUpsUpdateConnectionActionHandler.createBreakersAndConnect(fpdu, upsBank, powerChainErrors, false, false);

	}

}
