package com.raritan.tdz.request.progress;

import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.home.RequestMessage;
import com.raritan.tdz.util.GlobalUtils;

/**
 * intercept the *<request_stage>validateChannel and *<request_stage>executeChannel and update the dto against the session id
 * session information is in the payload's userInfo 
 * @author bunty
 *
 */
public class RequestProgressChannelIntercepter extends ChannelInterceptorAdapter {

	private static Logger log = Logger.getLogger("RequestProgress");
	
	private RequestProgressUpdate requestProgressUpdate;

	
	public RequestProgressChannelIntercepter(
			RequestProgressUpdate requestProgressUpdate) {
		super();
		this.requestProgressUpdate = requestProgressUpdate;
	}



	// Update by validate channel intercepter ("Validating Request") and execute channel intercepter ("Approving Request..., Issuing Work Order... , etc.") of different stages
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		if (log.isDebugEnabled()) log.debug("intercepted channel : " + channel.toString());
		
		RequestMessage requestMessage = (RequestMessage) message.getPayload();
		
		String channelName = channel.toString();
		
		// Extract request stage from the channel name: <reqType>.<reqStage>.<channelType: validateChannel/executeChannel>
		String[] channelInfo = channelName.split("\\.");
		
		Long requestStage = (null != channelInfo[1] && GlobalUtils.isNumeric(channelInfo[1])) ? new Long(channelInfo[1]) : null;
		
		UserInfo userInfo = requestMessage.getUserInfo();
		
		Request request = requestMessage.getRequest();
		
		requestProgressUpdate.updateRequestStage(request, requestStage, userInfo);
		
		return message;
	}
	

}
