/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctexport.utils.DCTImportTemplateUtil;
import com.raritan.tdz.dctimport.dto.DCTImportHeaderMapCache;

/**
 * This transforms the appropriate Item related DCTImport to
 * CSV.
 * <p>Note that this will not add the header. This is done
 * during aggregation </p>
 * @author prasanna
 *
 */
public class ItemExportImportDTOToCSVTransformer implements
		ExportImportDTOMapToCSVTransformer {
	
	@Autowired
	private DCTImportTemplateUtil dctImportTemplateUtil;
	
	@Autowired
	private DCTImportHeaderMapCache itemImportHeaderMapCache;

	
	public final static String CUSTOM_FIELD_HEADER = "CustomFieldHeader";

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctexport.integration.transformers.ExportImportDTOToCSVTransformer#transform(org.springframework.integration.Message)
	 */
	@Override
	public Message<String> transform(Message<Map<String, Object>> importDTOMsg)
			throws Exception {
		Map<String,Object> dctImport = importDTOMsg.getPayload();
		
		String objectType = (String)importDTOMsg.getHeaders().get(ExportSplitDTOToImportDTOMapTransformer.OBJECT_TYPE_HEADER);
		String operation = (String)importDTOMsg.getHeaders().get(ExportSplitDTOToImportDTOMapTransformer.OPERATION_HEADER);
		
		String[] columnNames = dctImportTemplateUtil.getColumns(objectType);
		//Setup the operation and object type to be blank
		columnNames[0] = "";
		columnNames[1] = "";
		
		StringBuilder csvString = new StringBuilder();
		csvString.append(operation);
		csvString.append(",");
		csvString.append(objectType);
		csvString.append(",");
		
		Map<String,String> headerMap = itemImportHeaderMapCache.getHeaderMap();
		
		boolean customFieldProcessed = false;
		List<String> customFieldColumnNames = new ArrayList<String>();
		for (String columnName:columnNames){
			String propertyName =  headerMap.get(normalizeHeader(columnName));
			if (propertyName != null){
				Object value = dctImport.get(propertyName);
				String valueStr = value != null ? value.toString() : "";
				if (valueStr.contains(","))
					csvString.append("\"");
				csvString.append(valueStr);
				if (valueStr.contains(","))
					csvString.append("\"");
				csvString.append(",");
			} else if (columnName.contains("Custom Field") && !customFieldProcessed){
				@SuppressWarnings("unchecked")
				Map<String,String> value = (Map<String, String>) dctImport.get("tiCustomField");
				customFieldProcessed = true;
				for (Map.Entry<String, String> entry:value.entrySet()){
					String key = entry.getKey();
					String valueStr = entry.getValue();
					if (valueStr.contains(","))
						csvString.append("\"");
					csvString.append(valueStr);
					if (valueStr.contains(","))
						csvString.append("\"");
					csvString.append(",");
					
					customFieldColumnNames.add(key);
				}
			}
			
		}
		
		String csvStringPayLoad = csvString.substring(0,csvString.lastIndexOf(","));
		
		Message<String> resultMsg = MessageBuilder.withPayload(csvStringPayLoad)
				.copyHeaders(importDTOMsg.getHeaders())
				.setHeader(CUSTOM_FIELD_HEADER, customFieldColumnNames)
				.build();
	
		return resultMsg;
	}
	
	/**
	 * Here we normalize the header line here so that this can be flexible for the user
	 * @param line
	 * @return
	 */
	private String normalizeHeader(String line) {
		line = line.toLowerCase();
		line = line.replaceAll("\\s", "");
		line = line.replaceAll("\\*", "");
		return line;
	}


}
