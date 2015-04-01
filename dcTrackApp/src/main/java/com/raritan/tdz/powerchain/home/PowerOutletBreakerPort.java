package com.raritan.tdz.powerchain.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;

public interface PowerOutletBreakerPort {

	
	/**
	 * create the power outlet breaker port 
	 * @param powerOutlet
	 * @param panelBreakerPort
	 */
	public void create(Item powerOutlet, PowerPort panelBreakerPort);
	
}
