/**
 * 
 */
package com.raritan.tdz.dctimport.integration.interceptors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.ChannelInterceptor;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;

import com.raritan.tdz.dctimport.logger.ImportLogger;

/**
 * @author prasanna
 *
 */
public class ImportLineLoggerInterceptor  extends ChannelInterceptorAdapter {
	
	@Autowired
	ImportLogger dctCSVImportLogger;

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preSend(org.springframework.integration.Message, org.springframework.integration.MessageChannel)
	 */
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		List<?> msg = (List<?>)message.getPayload();
		if (dctCSVImportLogger != null)
			dctCSVImportLogger.logLine((String)msg.get(0), (Integer)msg.get(1));
		return message;
	}

}
