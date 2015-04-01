package com.raritan.tdz.dctimport.integration.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.session.dao.UserSessionDAO;

import edu.emory.mathcs.backport.java.util.Arrays;

public class BeanToMapTransformer implements ImportTransformer {
	
	private String uuid;
	
	@Autowired
	private UserSessionDAO userSessionDAO;
	
	private List<String> objectKeysToIgnoreList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

	{
		add("class");
		add("operation");
		add("objectType");
		add("errorsAndWarnings");
		add("_blank_");
	}};
	
	public BeanToMapTransformer(String uuid) {
		this.uuid = uuid;
		this.objectKeysToIgnoreList.addAll(objectKeysToIgnoreList);
	}
	
	public List<String> getObjectKeysToIgnoreList() {
		return objectKeysToIgnoreList;
	}

	public void setObjectKeysToIgnoreList(List<String> objectKeysToIgnoreList) {
		this.objectKeysToIgnoreList.addAll(objectKeysToIgnoreList);
	}

	@SuppressWarnings("unchecked")
	public Message<?> transform(Message<?> message) throws Exception {
		
		DCTImport beanObj = (DCTImport)  message.getPayload();
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String,Object> objectAsMap = objectMapper.convertValue(beanObj, Map.class);
		
		// remove all elements from map where user did not pass even the header
		objectAsMap.values().removeAll(Collections.singleton(null));
	    
		for (String keyToIgnore:objectKeysToIgnoreList){
	    	objectAsMap.remove(keyToIgnore);
	    }
		UserInfo userInfo;
		try {
			userInfo = userSessionDAO.getUserInfo(uuid);
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e);
		}
		Object[] newPayLoadArray = {objectAsMap, userInfo};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	}

}
