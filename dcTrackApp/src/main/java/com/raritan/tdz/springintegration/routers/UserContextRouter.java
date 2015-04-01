/**
 * 
 */
package com.raritan.tdz.springintegration.routers;

import org.springframework.integration.MessageChannel;

/**
 * @author prasanna
 *
 */
public interface UserContextRouter {
	/**
	 * Given an unique identifier, resolve the proper 
	 * ReportContext and return the channel corresponding
	 * to that context. 
	 * <p><b>For example:</b> the uuid could be user session id</p>
	 * @param uuid
	 * @return
	 */
	public MessageChannel resolve(String uuid);
}
