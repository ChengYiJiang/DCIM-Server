/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import com.raritan.tdz.domain.LksData;

/**
 * @author prasanna
 *
 */
public interface ItemsEventSubscriber {
	public void addItemSubscriber(LksData classLks, ItemSubscriber subscriber);
}
