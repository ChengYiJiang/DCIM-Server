package com.raritan.tdz.dctimport.integration.transformers;

import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.domain.UserInfo;

import edu.emory.mathcs.backport.java.util.Arrays;

public class NewLocationNameTransformer {
		
	@SuppressWarnings("unchecked")
	public Message<?> transform(Message<?> message) throws Exception {

		Long id = (Long)((List<Object>)message.getPayload()).get(0);
		Map<String, Object> objMap = (Map<String, Object>) ((List<Object>)message.getPayload()).get(1);
		UserInfo userInfo = (UserInfo) ((List<Object>)message.getPayload()).get(2);
			
		// update name if applicable
		objMap = updateLocation(objMap);
			
			// construct message payload
		Object[] newPayLoadArray = {id, objMap, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
			
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();
		return newMessage;
	}

	protected Map<String, Object> updateLocation(Map<String, Object> m) {
		String newLocation = (String)m.get("newLocation");
		if (newLocation != null && newLocation.length() > 0) 
			m.put("cmbLocation",  newLocation);
				
		m.remove("newLocation");
		return m;		
	}

}
