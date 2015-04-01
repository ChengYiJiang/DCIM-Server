/**
 * 
 */
package com.raritan.tdz.dctimport.integration.transformers;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author prasanna
 *
 */
public class OperationObjectTypeNormalizer implements ImportTransformer {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.transformers.ImportTransformer#transform(org.springframework.integration.Message)
	 */
	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		Message<?> resultMessage = message;
		
		List<?> msgList = (List<?>)message.getPayload();
		String line = (String) msgList.get(0);
		int lineNumber = (Integer) msgList.get(1);
		
		line = normalizeOperationObjectType(line);
			
		Object[] newPayloadArray = {line,lineNumber};
		List<?> newPayloadArrayList = Arrays.asList(newPayloadArray);
		
		resultMessage = MessageBuilder.withPayload(newPayloadArrayList).copyHeaders(message.getHeaders()).build();
		
		return resultMessage;
	}
	
	/**
	 * Here we normalize the header line here so that this can be flexible for the user
	 * @param line
	 * @return
	 */
	private String normalizeOperationObjectType(String line) {
		StringBuilder lineBuilder = new StringBuilder();
		String operation = line.substring(0, line.indexOf(",")).toLowerCase().toString().replaceAll("\\s", "");
		lineBuilder.append(operation);
		
		String object = line.substring(line.indexOf(","),line.indexOf(",", line.indexOf(",") + 1)).toLowerCase().toString().replaceAll("\\s", "");
		lineBuilder.append(object);
		
		lineBuilder.append(line.substring(line.indexOf(",", line.indexOf(",") + 1)));
		return lineBuilder.toString();
	}

}
