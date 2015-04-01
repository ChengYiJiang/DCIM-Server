/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import com.raritan.tdz.domain.LksData;

/**
 * This will handle various Items associated with the DataPorts
 * @author prasanna
 *
 */
public interface PowerPortsEventSubscriber {
	public void addItemSubscriber(LksData classLks, PowerPortSubscriber subscriber);
}
