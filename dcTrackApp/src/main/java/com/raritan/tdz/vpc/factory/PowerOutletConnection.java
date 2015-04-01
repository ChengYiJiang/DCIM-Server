package com.raritan.tdz.vpc.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainActionHandlerHelper;
import com.raritan.tdz.powerchain.home.WhipOutletToBranchCircuitBreakerActionHandler;

/**
 * create connections from whip outlet to panel's branch circuit breakers
 * @author bunty
 *
 */
public class PowerOutletConnection implements VPCConnection {

	@Autowired
	private WhipOutletToBranchCircuitBreakerActionHandler whipOutletToBranchCircuitBreakerActionHandler;

	@Autowired(required=true)
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Override
	public void create(Map<String, List<Item>> vpcItems)
			throws BusinessValidationException {
		List<Item> powerOutlets = vpcItems.get(VPCItemFactory.POWER_OUTLET);
		List<Item> panels = vpcItems.get(VPCItemFactory.FLOOR_PDU_PANEL);
		
		List<PowerPort> bcbPorts = new ArrayList<PowerPort>();
		
		for (Item powerPanel: panels) {
			
			Set<PowerPort> ports = powerPanel.getPowerPorts();
			
			for (PowerPort port: ports) {
				// Create power outlet connection only with breaker ports
				if (!port.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER) ||
						null == port.getPhaseLookup()) continue;
				
				bcbPorts.add(port);
			}
		}
			
		Map<Item, PowerPort> outletToPortMap = combineListsIntoOrderedMap (powerOutlets, bcbPorts);
			
		for (Map.Entry<Item, PowerPort> entry: outletToPortMap.entrySet()) {
			Item outlet = entry.getKey();
			PowerPort bcbPort = entry.getValue();
				
			Errors powerChainErrors = powerChainActionHandlerHelper.getErrorObject();
			((MeItem) outlet).setPduPanelItem((MeItem) bcbPort.getItem());
			whipOutletToBranchCircuitBreakerActionHandler.process(outlet, bcbPort, powerChainErrors, false, false);
		}
		
	}
	
	Map<Item, PowerPort> combineListsIntoOrderedMap (List<Item> keys, List<PowerPort> values) {
		
		if (keys.size() != values.size()) 
			throw new IllegalArgumentException ("Cannot combine lists with dissimilar sizes");
		
		Map<Item, PowerPort> map = new LinkedHashMap<Item, PowerPort>();
		
		for (int i=0; i<keys.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}
		
		return map;
	}

}
