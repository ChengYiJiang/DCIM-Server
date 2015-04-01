package com.raritan.tdz.dctimport.integration.transformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.integration.exceptions.IgnoreException;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.domain.UserInfo;

import edu.emory.mathcs.backport.java.util.Arrays;


public abstract class NameToIdTransformerBase {
	
	protected ImportErrorHandler importErrorHandlerGateway;
	protected String errorCode;
	
	@SuppressWarnings("unchecked")
	public Message<?> transform(Message<?> message) throws Exception {
		
		Map<String, Object> objMap = (Map<String, Object>) ((List<Object>)message.getPayload()).get(0);
		UserInfo userInfo = (UserInfo) ((List<Object>)message.getPayload()).get(1);
		
		// get id
		Long id = getId(objMap);
		if (id == null) {
			Errors errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName() + "Error");
			errors.reject(errorCode);
			importErrorHandlerGateway.handleLineErrors(errors);
			throw new IgnoreException();
		}
		
		// update name if applicable
		objMap = updateName(objMap);
		
		// construct message payload
		Object[] newPayLoadArray = {id, objMap, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();
	    return newMessage;
	}
	
	abstract protected Long getId (Map<String, Object> m); 
	abstract protected Map<String,Object> updateName(Map<String, Object> m);

}
