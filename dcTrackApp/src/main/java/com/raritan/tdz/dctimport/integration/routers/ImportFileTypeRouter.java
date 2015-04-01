/**
 * 
 */
package com.raritan.tdz.dctimport.integration.routers;

import org.springframework.integration.MessageChannel;

/**
 * @author prasanna
 *
 */
public interface ImportFileTypeRouter {
	public MessageChannel resolve( String fileName) throws Exception;
}
