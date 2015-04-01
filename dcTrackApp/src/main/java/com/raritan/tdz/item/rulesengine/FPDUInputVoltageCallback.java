package com.raritan.tdz.item.rulesengine;

import java.util.Set;

import org.springframework.util.Assert;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class FPDUInputVoltageCallback implements RemoteRefMethodCallback {
		private ItemDAO itemDAO;
		final String  CONSTANT_FPDU_INPUT_VOLTAGE_OF_ITEM= "tiFPDUInputVoltage";
		
		public FPDUInputVoltageCallback( ItemDAO itemDAO ){
			this.itemDAO = itemDAO;
		}
		
		@Override
		public void fillValue(UiComponent uiViewComponent, String filterField,
				Object filterValue, String operator, RemoteRef remoteRef,
				Object additionalArgs) throws Throwable {
			
			if( ! uiViewComponent.getUiId().equals(CONSTANT_FPDU_INPUT_VOLTAGE_OF_ITEM)) return;
			
			Long itemId = (Long)filterValue;
			Assert.isTrue(itemId > 0);
			
			if((itemDAO.getItem(itemId) instanceof  MeItem) == false ) return;
			
			MeItem item = (MeItem)itemDAO.getItem(itemId); 

			Set<PowerPort> ports =  item.getPowerPorts();
			if( ports == null ) return;
				
			for ( PowerPort port : ports  ){
				long portSubClass = port.getPortSubClassLookup() != null ? port.getPortSubClassLookup().getLkpValueCode() : 0;
				if( portSubClass == SystemLookup.PortSubClass.PDU_INPUT_BREAKER ){
					if( port.getVoltsLookup() != null){
						uiViewComponent.getUiValueIdField().setValue(port.getVoltsLookup().getLkpValue());
					}
				}
			}
		}

}
