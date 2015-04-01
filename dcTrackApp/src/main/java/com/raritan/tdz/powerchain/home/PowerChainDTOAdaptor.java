package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dto.UPSBankDTO;
import com.raritan.tdz.lookup.SystemLookup;

public class PowerChainDTOAdaptor {
	private static Logger log = Logger.getLogger("PowerChainDTOAdaptor");
	
	public static UPSBankDTO adaptUPSBankItemToDTO( Item item ){
		MeItem meItem = (MeItem)item;

		String outputWiring = null;
		if( meItem.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_DELTA ){
			outputWiring = "3-Wire + Ground";
		}else if( meItem.getPhaseLookup().getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_WYE ){
			outputWiring = "4-Wire + Ground";
		}else{
			log.error("Unsupported input wiring for the UPS Bank: " + meItem.getItemId());
			return null;
		}
	
		UPSBankDTO upsBank = new UPSBankDTO();
		upsBank.setCapacity(meItem.getRatingKva());
		upsBank.setLocation(meItem.getDataCenterLocation().getCode());
		upsBank.setLocationId(meItem.getDataCenterLocation().getDataCenterLocationId());
		upsBank.setOutputWiringDesc(outputWiring);
		upsBank.setOutputWiringLkpValueCode(meItem.getPhaseLookup().getLkpValueCode());
		upsBank.setUpsBankId(meItem.getItemId());
		upsBank.setUpsBankName(meItem.getItemName());
		upsBank.setVolts((long) meItem.getRatingV());
		return upsBank;
	}

}
