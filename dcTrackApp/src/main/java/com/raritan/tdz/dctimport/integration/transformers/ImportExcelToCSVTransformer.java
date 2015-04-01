/**
 * 
 */
package com.raritan.tdz.dctimport.integration.transformers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.context.support.ServletContextResource;

import com.raritan.tdz.dctimport.integration.exceptions.ImportFileTypeInvalidException;
import com.raritan.tdz.dctimport.utils.XLToCSV;

import edu.emory.mathcs.backport.java.util.Arrays;
import static com.raritan.tdz.dctimport.utils.XLToCSV.*;

/**
 * This transformer will translate an excel spreadsheet to CSV file
 * @author prasanna
 *
 */
public class ImportExcelToCSVTransformer implements ImportTransformer {

	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired(required=false)
	private ServletContext servletContext;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.transformers.ImportFileTypeTransformer#transform(java.lang.String, java.lang.String)
	 */
	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		
		String inputFile = null;
		List<Object> payload = (List<Object>) message.getPayload();
		inputFile = (String) payload.get(1);
		
		Resource inResource = servletContext != null ? new ServletContextResource(servletContext, "/../dcTrackImport/" + inputFile):resourceLoader.getResource("../../" + inputFile);
		
		checkFilePath(inResource);
		
		StringBuilder outputFile = new StringBuilder();
		outputFile.append(inResource.getFile().getAbsolutePath())
			.append(".csv");
	
		
		
		try {
			convert(inResource,outputFile.toString());
		} catch (InvalidFormatException e) { //Convert invalid format exception to the file type invalid exception
			throw new ImportFileTypeInvalidException();
		}
		
		//This is to make correction of the number of commas based on the heading. 
		//Some XLS and/or XLSX do not include blank columns for rows. In this case 
		//the import will fail. In order to avoid this, we need to perform the necessary
		//correction by applying appropriate number of commas based on the heading
		applyCommaCorrection(outputFile.toString());
		
		Object[] newPayLoadArray = {payload.get(0),outputFile.toString()};
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	}
	
	private void checkFilePath(Resource inResource) throws IOException {
		InputStream is = new FileInputStream(inResource.getFile());
	}

}
