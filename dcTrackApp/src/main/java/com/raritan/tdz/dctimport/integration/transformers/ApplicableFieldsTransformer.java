package com.raritan.tdz.dctimport.integration.transformers;

import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.domain.UserInfo;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ApplicableFieldsTransformer {

	List<String> fields;

	public ApplicableFieldsTransformer(List<String> fields) {
		super();
		this.fields = fields;
	}
	
	@SuppressWarnings("unchecked")
	public Message<?> transform(Message<?> message) throws Exception {
		
		Map<String, Object> objMap = (Map<String, Object>) ((List<Object>)message.getPayload()).get(0);
		UserInfo userInfo = (UserInfo) ((List<Object>)message.getPayload()).get(1);
		
		for (String field: fields) {
			objMap.remove(field);
		}

		Long itemId = -1L;
		// construct message payload
		Object[] newPayLoadArray = {itemId, objMap, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();
	    return newMessage;
	}
	
}
