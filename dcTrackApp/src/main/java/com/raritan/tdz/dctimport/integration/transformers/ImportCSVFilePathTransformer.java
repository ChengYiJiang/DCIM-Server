/**
 * 
 */
package com.raritan.tdz.dctimport.integration.transformers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.context.support.ServletContextResource;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author prasanna
 * This is to just convert the relative path for CSV file to absolute path that will be used 
 * by the Import Job
 */
public class ImportCSVFilePathTransformer implements ImportTransformer {


	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		
		String inputFile = null;
		List<Object> payload = (List<Object>) message.getPayload();
		inputFile = (String) payload.get(1);
		
		
		Resource inResource = servletContext != null ? new ServletContextResource(servletContext, "/../dcTrackImport/" + inputFile):resourceLoader.getResource("../../" + inputFile);;
		
		checkFilePath(inResource);
		
		StringBuilder outputFile = new StringBuilder();
		outputFile.append(inResource.getFile().getAbsolutePath());
		
		Object[] newPayLoadArray = {payload.get(0),outputFile.toString()};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();
		
		return newMessage;
	}
	
	private void checkFilePath(Resource inResource) throws IOException {
		InputStream is = new FileInputStream(inResource.getFile());
	}

}
