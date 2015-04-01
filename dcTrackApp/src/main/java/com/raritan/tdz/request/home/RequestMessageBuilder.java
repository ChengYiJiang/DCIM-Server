package com.raritan.tdz.request.home;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * 
 * @author bunty
 *
 */

public class RequestMessageBuilder {

	@SuppressWarnings("serial")
	public static final Map<String, Long> requestTypeToId =
			Collections.unmodifiableMap(new HashMap<String, Long>() {{
				put("Item Move", SystemLookup.RequestTypeLkp.ITEM_MOVE);
				
			}});

	
	public static Message<RequestMessage> createMessage(Request request, List<Request> requests, Boolean requestByPass) {
	
		RequestMessage requestMessage = new RequestMessage(request, requests, requestByPass);
		
		Long requestType = (null != request.getRequestTypeLookup()) ? request.getRequestTypeLookup().getLkpValueCode() : request.getRequestTypeLookupCode(); //requestTypeToId.get(request.getRequestType());
		
		Message<RequestMessage> processRequestMessage = MessageBuilder.withPayload(requestMessage)
				.setHeader("requestByPass", requestByPass)
				.setHeader("requestType", requestType)
				.build();
		
		return processRequestMessage;
		
	}
	
	public static RequestMessage createMessagePayload(Request request, List<Request> requests, Boolean requestByPass, Errors errors) {
		
		// return new RequestMessage(request, requests, requestByPass);
		
		return new RequestMessage(request, requests, errors, requestByPass);
			
	}
	
	public static RequestMessage createMessagePayload(Request request, List<Request> requests, Boolean requestByPass, Errors errors, UserInfo userInfo) {
		
		// return new RequestMessage(request, requests, requestByPass);
		
		return new RequestMessage(request, requests, errors, requestByPass, userInfo);
			
	}

}
