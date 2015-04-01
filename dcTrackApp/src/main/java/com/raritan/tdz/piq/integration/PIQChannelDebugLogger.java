/**
 * 
 */
package com.raritan.tdz.piq.integration;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;

/**
 * @author prasanna
 *
 */
public class PIQChannelDebugLogger extends ChannelInterceptorAdapter implements ApplicationContextAware {

	Logger log = Logger.getLogger(getClass());
	
	private ApplicationContext applicationContext = null;
	
	private boolean detailedLog = false;
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel){
		StringBuilder logMsg = new StringBuilder();
		if (isDetailedLog()){
			 logMsg
				.append("Before Sending Message: ")
				.append(message)
				.append(" on Channel: ")
				.append(channel)
				.append("having an application context id: ")
				.append(applicationContext.getId());
		} else {
			logMsg
				.append("Before Sending Message: ")
				.append(message)
				.append(" to ")
				.append(applicationContext.getId());
		}
		
		log.debug(logMsg);
		return super.preSend(message, channel);
	}
	
	@Override
	public Message<?> postReceive(Message<?> message, MessageChannel channel){
		StringBuilder logMsg = new StringBuilder();
		
		if (isDetailedLog()){
			logMsg
				.append("After Receiving Message: ")
				.append(message)
				.append(" on Channel: ")
				.append(channel)
				.append("having an application context id: ")
				.append(applicationContext.getId());
		} else {
			logMsg
				.append("After Sending Message: ")
				.append(message)
				.append(" on ")
				.append(applicationContext.getId());
		}
	
		log.debug(logMsg);
		return super.postReceive(message, channel);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public boolean isDetailedLog() {
		return detailedLog;
	}

	public void setDetailedLog(boolean detailedLog) {
		this.detailedLog = detailedLog;
	}
}
