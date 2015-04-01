/**
 * 
 */
package com.raritan.tdz.dctexport.integration.aggregators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.context.support.ServletContextResource;

import com.raritan.tdz.dctexport.utils.DCTImportTemplateUtil;

/**
 * @author prasanna
 * This aggregator is specifically for item result aggregation.
 * This will collect individual csvList for each object type and aggregate them
 */
public class ItemExportAggregator implements DCTExportAggregator{
	private final Logger log = Logger.getLogger("dctExport");
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired(required = false)
	private ServletContext servletContext;
	
	@Autowired
	private DCTImportTemplateUtil dctImportTemplateUtil;
	
	@Override
	public Message<String> aggregate(List<Message<List<String>>> importMsg) throws IOException{
		log.debug("Message received in aggregator");
		
		//Get the CSV List
		List<String> csvList = getCSVList(importMsg);
		
		//Write to file
		String url = writeToFile(csvList);
		
		//We no longer need csvList
		csvList.clear();

		//Create the message
		Message<String> message = MessageBuilder.withPayload(url).copyHeaders(importMsg.get(0).getHeaders()).build();
		
		//Clear the importMsgs
		importMsg.clear();
		
	
		
		return message;
	}

	private String writeToFile(List<String> csvList) throws IOException,
			FileNotFoundException, UnsupportedEncodingException {
		File newFile = createCSVFile();

		BufferedWriter bw = getBufferedWriter(newFile);
		
		for (String csvLine:csvList){
			bw.write(csvLine);
			bw.newLine();
		}
		
		bw.flush();
		bw.close();
		
		return newFile.getAbsolutePath();
	}

	private BufferedWriter getBufferedWriter(File newFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		FileOutputStream outStream = new FileOutputStream(newFile,true);
		String encoding = "UTF8";
	    OutputStreamWriter osw = new OutputStreamWriter(outStream, encoding);
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	private File createCSVFile() throws IOException {
		String filePath = getAbsoluteExportFilePath();
		File newFile = new File(filePath);
		newFile.createNewFile();
		return newFile;
	}
	
	private String getAbsoluteExportFilePath(){
		String realPath = servletContext != null ? 
				servletContext.getRealPath(".." + getURL(UUID.randomUUID().toString())) : getFileName(UUID.randomUUID().toString());
		return realPath;
	}
	
	private String getURL(String uuid) {
		return new StringBuilder("/dcTrackImport/")
		.append(getFileName(uuid))
		.toString();
	}
	
	private String getFileName(String uuid){
		return new StringBuilder()
			.append("FileExport-")
			.append(uuid)
			.append(".csv")
			.toString();
	}

	private List<String> getCSVList(List<Message<List<String>>> importMsg) {
		List<String> csvList = new ArrayList<String>();
		
		Map<String, List<String>> objectTypeMap = new LinkedHashMap<String,List<String>>();
		if (importMsg.size() > 0){
			for (Message<List<String>> msg:importMsg){
				//csvList.addAll(msg.getPayload());
				Integer commentsSize = (Integer) msg.getHeaders().get(DCTExportObjectTypeAggregator.OBJECT_TYPE_COMMENTS_SIZE) + 1;
				String[] lineStrArray = msg.getPayload().get(commentsSize).split(",");
				objectTypeMap.put(lineStrArray[1], msg.getPayload());
			}
		}
		
		Set<String> objectTypeOrderList = dctImportTemplateUtil.getObjectTypes();
		for (String objectType:objectTypeOrderList){
			if (objectTypeMap.get(objectType) != null)
				csvList.addAll(objectTypeMap.get(objectType));
		}
		return csvList;
	}
}
