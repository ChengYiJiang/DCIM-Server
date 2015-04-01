/**
 * 
 */
package com.raritan.tdz.dctexport.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.DefaultBufferedReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResource;

import static com.raritan.tdz.dctimport.utils.XLToCSV.*;


/**
 * This singleton will provide some useful utility 
 * functions on the import template file in dctImport folder
 * @author prasanna
 *
 */
public class DCTImportTemplateUtil {
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired(required = false)
	private ServletContext servletContext;
	
	private final String templateFile;
	private final String sheetName;
	
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();
	private String encoding = DEFAULT_CHARSET;
	
	
	public static int OBJECT_TYPE_COLUMN_NUMBER = 1;
	
	private final Map<String, String[]> headerMap = new LinkedHashMap<String, String[]>();
	
	public DCTImportTemplateUtil(String templateFile,String sheetName){
		this.templateFile = templateFile;
		this.sheetName = sheetName;
	}
	
	public void init() throws Exception{
		//Load the template
		Resource inResource = getTemplateResource();
		checkFilePath(inResource);
		
		//Initialize the header map
		initHeaders(inResource);
	}
	
	public String[] getColumns(String objectType) {
		return headerMap.get(objectType);
	}
	
	public Set<String> getObjectTypes(){
		return headerMap.keySet();
	}

	private void initHeaders(Resource inResource) throws IOException,
			InvalidFormatException, Exception {
			//Convert it to a CSV file.
			StringBuilder outputFile = getOutputFileName(inResource,sheetName);
			convertTemplateSheet(inResource, outputFile.toString(), sheetName);
			initHeaderMap(outputFile.toString());
	}

	private void checkFilePath(Resource inResource) throws IOException {
		InputStream is = new FileInputStream(inResource.getFile());
	}
	
	private StringBuilder getOutputFileName(Resource inResource,String sheetName)
			throws IOException {
		StringBuilder outputFile = new StringBuilder();
		outputFile.append(inResource.getFile().getAbsolutePath()).append("-").append(sheetName).append(".csv");
		return outputFile;
	}

	private Resource getTemplateResource() {
		Resource inResource = servletContext != null ? new ServletContextResource(servletContext,"../dcTrackImport/" + templateFile) : 
			resourceLoader.getResource(templateFile);
		return inResource;
	}
	
	private void initHeaderMap(String outputFile) throws Exception{
		BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();
		
		Resource resource =  new FileSystemResource(outputFile);
		BufferedReader reader = bufferedReaderFactory.create(resource,encoding);
		
		String line = null;
		while ((line = reader.readLine()) != null){
			if (line.startsWith("#")) continue;
			
			String[] splitStr = line.split("\\|");
			if (splitStr.length > 1){
				String objectType = splitStr[OBJECT_TYPE_COLUMN_NUMBER];
				headerMap.put(objectType, splitStr);
			}
		};
	}
	
}
