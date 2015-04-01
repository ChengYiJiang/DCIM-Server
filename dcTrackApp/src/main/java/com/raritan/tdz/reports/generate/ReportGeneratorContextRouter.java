/**
 * 
 */
package com.raritan.tdz.reports.generate;

import org.springframework.integration.MessageChannel;

/**
 * @author prasanna
 *
 */
public interface ReportGeneratorContextRouter {
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
