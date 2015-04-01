package com.raritan.tdz.request.progress;

import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * Intercepter of the error channel of the error manager
 * @author bunty
 *
 */
public class RequestErrorChannelIntercepter extends ChannelInterceptorAdapter {

	private static Logger log = Logger.getLogger("RequestProgress");
	
	private RequestProgressUpdate requestProgressUpdate;
	
	
	public RequestErrorChannelIntercepter(
			RequestProgressUpdate requestProgressUpdate) {
		super();
		this.requestProgressUpdate = requestProgressUpdate;
	}



	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		
		if (log.isDebugEnabled()) {
			log.debug("update req description and stage");
			log.debug("hijacked channel : " + channel.toString());
		}
		
		RequestMessage requestMessage = (RequestMessage) message.getPayload();
		
		Errors requestErrors = requestMessage.getErrors();
		
		UserInfo userInfo = requestMessage.getUserInfo();
		
		requestProgressUpdate.updateErrors(requestErrors, userInfo);
		
		return message;
	}


}
