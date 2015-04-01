package com.raritan.tdz.item.rulesengine;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dao.PowerConnFinderDAO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class BreakerPortNameCallback implements RemoteRefMethodCallback {
    @Autowired
    private ItemDAO itemDAO;
    
    @Autowired
    private PowerPortDAO powerPortDAO;
    
    @Autowired
    PowerConnDAO powerConnFinderDAO;

    final String CONSTANT_FPDU_BREAKER_OF_ITEM = "cmbFromFPDUBreaker";

	@Override
    public void fillValue(UiComponent uiViewComponent, String filterField,
            Object filterValue, String operator, RemoteRef remoteRef,
            Object additionalArgs) throws Throwable {

        if( ! uiViewComponent.getUiId().equals(CONSTANT_FPDU_BREAKER_OF_ITEM)) return;

        Long itemId = (Long)filterValue;
        Assert.isTrue(itemId > 0);

        if((itemDAO.getItem(itemId) instanceof  MeItem ) == false ) return;
        
        /* current fpdu item */
        MeItem item = (MeItem)itemDAO.getItem(itemId);
        
        if (item.getClassLookup().getLkpValueCode() != SystemLookup.Class.FLOOR_PDU) return;
        
        /* using fpdu input breaker port id get the port connection */
        
        PowerPort fpduInputBreakerPort = getFPDUInputBreakerPort (item);
        
        if (fpduInputBreakerPort == null) return;

        /* get the branch circuit breaker port where this pdu is connected.*/
        PowerPort port = null;
        Set<PowerConnection> pcs = fpduInputBreakerPort.getSourcePowerConnections();
        if (pcs != null) {
	        for  (PowerConnection pc: pcs) {
	        	port = pc.getDestPowerPort();
	        	if (port != null && port.isBranchCircuitBreaker()) {
	        		break;
	        	}
	        }
        }
        
        //PowerPort ppp = powerConnFinderDAO.getDestinationPort(fpduInputBreakerPort.getPortId());
        //assert (ppp != null);
        if (port != null && 
        		port.getPortSubClassLookup() != null && 
        		port.isBranchCircuitBreaker()) {
        	/* get the where it is connected to  breaker port */
        	uiViewComponent.getUiValueIdField().setValueId( port.getPortId());

        	/* set the ui breaker port name */
        	String uiBreakerPortName = getUIBreakerPortName (port);
            uiViewComponent.getUiValueIdField().setValue(uiBreakerPortName);
        }
        
    }

    private String getUIBreakerPortName (PowerPort branchCircuitBreakerPort ) {
    	Item panel2 = (Item)branchCircuitBreakerPort.getItem();
    	Item fpdu2 = (Item)powerPortDAO.getFPDUItemForBreakerPortId(branchCircuitBreakerPort.getPortId());
    	
        StringBuilder uiBreakerPortName = new StringBuilder();
        uiBreakerPortName.append(fpdu2 != null ? fpdu2.getItemName() : "");
        uiBreakerPortName.append("/");
        uiBreakerPortName.append(panel2 != null ? panel2.getItemName() : "");
        uiBreakerPortName.append(":");
        uiBreakerPortName.append(branchCircuitBreakerPort != null ? branchCircuitBreakerPort.getPortName() : "");
    	
        return uiBreakerPortName.toString();
    }    	
    
    private PowerPort getFPDUInputBreakerPort (MeItem item) {
    	PowerPort fpduInputBreakerPort = null;
        /* get fpdu input breaker port */
        Set<PowerPort> ppSet = item.getPowerPorts();
        if (ppSet == null) return fpduInputBreakerPort;
        for (PowerPort p : ppSet) {
			if (p.isPduInputBreaker() == true) {
        		fpduInputBreakerPort = p; 
        		break;
        	}
        }
        return fpduInputBreakerPort;
    }


}
