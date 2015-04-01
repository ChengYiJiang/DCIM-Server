package com.raritan.tdz.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.core.MessageProcessingContext;

import com.raritan.tdz.session.DCTrackSessionManagerInterface;
import com.raritan.tdz.session.FlexUserSessionContext;

import flex.messaging.FlexContext;
import flex.messaging.messages.Message;

public class FlexRequestInterceptor implements MessageInterceptor {
	public Message postProcess(MessageProcessingContext context, Message msg1,
			Message msg) {
		return msg;
	}

	public Message preProcess(MessageProcessingContext context, Message msg) {
//		System.out.println("START HEADERS");
//		for (Object h : msg.getHeaders().keySet()) {
//			System.out.println(h.toString()+": "+msg.getHeader((String)h));
//		}
//		System.out.println("END HEADERS");
		return msg;
	}
}
