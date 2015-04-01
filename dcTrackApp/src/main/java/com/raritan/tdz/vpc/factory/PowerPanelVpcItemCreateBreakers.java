package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.BranchCircuitBreaker;

/**
 * create power panel breaker ports using the poles
 * @author bunty
 *
 */
public class PowerPanelVpcItemCreateBreakers implements VPCItemUpdate {

	@Autowired
	private BranchCircuitBreaker branchCircuitBreaker;

	@Override
	public void update(Item item, Map<String, Object> additionalParams) {
		
		branchCircuitBreaker.create(item, SystemLookup.PhaseIdClass.SINGLE_2WIRE, 1L, VPCLookup.DefaultValue.Current);
		branchCircuitBreaker.create(item, SystemLookup.PhaseIdClass.SINGLE_3WIRE, 2L, VPCLookup.DefaultValue.Current);
		branchCircuitBreaker.create(item, SystemLookup.PhaseIdClass.THREE_WYE, 4L, VPCLookup.DefaultValue.Current);
		
	}

}
