/**
 * 
 */
package com.raritan.tdz.vbjavabridge.subscribers;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 * This Interface provides a notify method to notify all the observers that Windows client
 * modified data.
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
public interface LNEventSubscriber {
	public void handleEvent(LNEvent event);
	public void subscribe();
}
