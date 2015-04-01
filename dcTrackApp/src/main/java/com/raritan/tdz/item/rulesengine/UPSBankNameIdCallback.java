package com.raritan.tdz.item.rulesengine;


import java.util.Set;

import org.springframework.util.Assert;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class UPSBankNameIdCallback implements RemoteRefMethodCallback {
	private ItemDAO itemDAO;
	final String CONSTANT_UPS_BANK_OF_ITEM = "cmbFromUPSBank";
	
	public UPSBankNameIdCallback( ItemDAO itemDAO ){
		this.itemDAO = itemDAO;
	}
	
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
		
		if( ! uiViewComponent.getUiId().equals(CONSTANT_UPS_BANK_OF_ITEM)) return;
		
		Long itemId = (Long)filterValue;
		Assert.isTrue(itemId > 0);
		Item itemInstance = itemDAO.getItem(itemId);
		if( (itemInstance instanceof  MeItem ) == false ) return;
		
		MeItem item = (MeItem)itemInstance; 

        PowerPort fpduInputBreakerPort = getFPDUInputBreakerPort (item);
        
        if (fpduInputBreakerPort == null) return;
        
        item.getDataCenterLocation().getDcName();
        if ( item.getUpsBankItem() != null) {
			uiViewComponent.getUiValueIdField().setValueId(item.getUpsBankItem().getItemId());
			String value = item.getUpsBankItem().getItemName();
			if (null != item.getDataCenterLocation() && null != item.getDataCenterLocation().getCode()) {
				value += " in " + item.getDataCenterLocation().getCode();
			}
			uiViewComponent.getUiValueIdField().setValue(value);
		}	
	}
        
        private PowerPort getFPDUInputBreakerPort (MeItem item) {
        	PowerPort fpduInputBreakerPort = null;
            /* get fpdu input breaker port */
            Set<PowerPort> ppSet = item.getPowerPorts();
            if (ppSet == null) return fpduInputBreakerPort;
            for (PowerPort p : ppSet) {
    			LksData subclass = p.getPortSubClassLookup();	
            	if (subclass != null && 
    					subclass.getLkpValueCode() == SystemLookup.PortSubClass.PDU_INPUT_BREAKER) {
            		fpduInputBreakerPort = p; 
            		break;
            	}
            }
            return fpduInputBreakerPort;
        }
}
