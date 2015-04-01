/**
 * 
 */
package com.raritan.tdz.piq.home;

import java.util.ArrayList;
import java.util.List;

import com.raritan.tdz.domain.PowerConnection;

/**
 * This object holds all the power connection ids that are not in sync with PIQ
 * This is populated by the respective PIQSync clients (Outlet)
 * @author prasanna
 *
 */
public class PIQAssociationNotInSync {
	private List<Long> powerConnIds = new ArrayList<Long>();
	
	/**
	 * Add an connection that is *not* in sync
	 * @param powerConn that is not in sync
	 */
	public void addItem(PowerConnection powerConn){
		powerConnIds.add(powerConn.getPowerConnectionId());
	}
	
	/**
	 * Remove an connection that is *not* in sync
	 * @param powerConn that is not in sync
	 */
	public void removeItem(PowerConnection powerConn){
		powerConnIds.remove(powerConn.getPowerConnectionId());
	}
	
	/**
	 * Clear all the connections in the object
	 */
	public void clear(){
		powerConnIds.clear();
	}
	
	/**
	 * Returns true if the connection is not in sync with PowerIQ
	 * @param powerConn that is not in sync
	 */
	public boolean isNotInSync(PowerConnection powerConn){
		return (powerConn.getSourcePowerPort().getItem().getPiqId() == null 
				|| powerConn.getDestPowerPort().getPiqId() == null 
				|| powerConnIds.contains(powerConn.getPowerConnectionId()));
	}
}
