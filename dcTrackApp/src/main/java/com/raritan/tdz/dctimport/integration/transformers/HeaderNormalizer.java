/**
 * 
 */
package com.raritan.tdz.dctimport.integration.transformers;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctimport.job.DCTHeaderFieldSetMapper;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author prasanna
 *
 */
public class HeaderNormalizer implements ImportTransformer {
	
	@Autowired
	private DCTHeaderFieldSetMapper headerFieldSetMapper;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.transformers.ImportTransformer#transform(org.springframework.integration.Message)
	 */
	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		Message<?> resultMessage = message;
		
		List<?> msgList = (List<?>)message.getPayload();
		String line = (String) msgList.get(0);
		int lineNumber = (Integer) msgList.get(1);
		
		//Before we normalize, let us capture the original since we may
		//need to display errors with header to user
		headerFieldSetMapper.setOriginalHeader(line);
		
		line = normalizeHeader(line);
		
		line = normalizeHeaderBlankColumns(line);
		
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
	private String normalizeHeader(String line) {
		line = line.toLowerCase();
		line = line.replaceAll("^#", "");
		line = line.replaceAll("\\s", "");
		line = line.replaceAll("\\*", "");
		return line;
	}

	private String normalizeHeaderBlankColumns(String line){
		StringBuilder result = new StringBuilder();
		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		String[] columns = delimitedLineTokenizer.tokenize(line).getValues();
		
		for (String column:columns){
			if (column.trim().isEmpty()){
				result.append("_blank_");
			} else {
				result.append(column);
			}
			
			result.append(",");
		}
		
		if (result.lastIndexOf(",") > 0){
			return result.substring(0,result.lastIndexOf(","));
		}
		
		return result.toString();
	}
}
