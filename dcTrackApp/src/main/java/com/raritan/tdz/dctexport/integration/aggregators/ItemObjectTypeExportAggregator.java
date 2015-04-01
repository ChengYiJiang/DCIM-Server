/**
 * 
 */
package com.raritan.tdz.dctexport.integration.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.raritan.tdz.dctexport.integration.transformers.ItemExportImportDTOToCSVTransformer;
import com.raritan.tdz.dctexport.utils.DCTImportTemplateUtil;

/**
 * @author prasanna
 * This aggregator is specifically for item result aggregation.
 * This essentially aggregates for the individual object types
 */
public class ItemObjectTypeExportAggregator implements DCTExportObjectTypeAggregator{
	Logger log = Logger.getLogger("dctExport");
	
	@Autowired
	private DCTImportTemplateUtil dctImportTemplateUtil;

	@Override
	public Message<List<String>> aggregate(List<Message<String>> importMsg){
		log.debug("Message received in aggregator");
		
		//Based on the objectType get the header
		 String objectType = getObjectType(importMsg);
		 
		 List<String> customFieldColumnNames = getCustomFieldColumnNames(importMsg);
		
		//Find and get the header row
		String headerRow  = getHeaderRow(objectType, customFieldColumnNames);
		
		Integer commentsSize = new Integer(getComments(importMsg).size());
		
		//Create a new csvList with the header in it
		List<String> csvList = getCSVList(importMsg, headerRow);

		//Create the message and send it to the next aggregate
		Message<List<String>> message = MessageBuilder.withPayload(csvList)
				.copyHeaders(importMsg.get(0).getHeaders())
				.setHeader(DCTExportObjectTypeAggregator.OBJECT_TYPE_COMMENTS_SIZE, commentsSize)
				.build();
		
		//Clear the current importMsg List as we no longer require it
		importMsg.clear();
		
		return message;
	}

	private List<String> getCSVList(List<Message<String>> importMsg, String headerRow) {
		List<String> csvList = new ArrayList<String>();
		
		//Add the comments for this object type.
		csvList.addAll(getComments(importMsg));
		
		//Add the header
		csvList.add(headerRow);
		
		for (Message<String> msg:importMsg){
			csvList.add(msg.getPayload());
		}
		return csvList;
	}

	private List<String> getComments(List<Message<String>> importMsg){
		List<String> comments = new ArrayList<String>();
		comments.add("");
		String headerRowList[]  = dctImportTemplateUtil.getColumns(getObjectType(importMsg));
		String commentColumn = headerRowList[headerRowList.length - 1];
		if (commentColumn.contains(";")){
			String commentArray[] = commentColumn.split(";");
			for (String comment:commentArray){
				StringBuilder commentStr = new StringBuilder();
				commentStr.append(comment);
				comments.add(commentStr.toString());
			}
		}
		return comments;
	}
	private String getHeaderRow(String objectType, List<String> customFieldColumnNames) {
		String headerRow = "";
		if (objectType != null){
			String headerRowList[]  = dctImportTemplateUtil.getColumns(objectType);
			StringBuilder headerRowBuilder = new StringBuilder();
			headerRowBuilder.append("# Operation *");
			headerRowBuilder.append(", Object *, ");
			
			boolean customFieldProcessed = false;
			
			for (int cnt = 2; cnt < headerRowList.length - 1; cnt++){
				if (headerRowList[cnt].contains("Custom Field")){
					if (!customFieldProcessed){
						for (String customFieldColumnName:customFieldColumnNames){
							headerRowBuilder.append(customFieldColumnName);
							headerRowBuilder.append(",");
						}
						customFieldProcessed = true;
					}
				} else {
					headerRowBuilder.append(headerRowList[cnt]);
					headerRowBuilder.append(",");
				}
			}
			headerRow = headerRowBuilder.substring(0,headerRowBuilder.lastIndexOf(",")).toString();
		}
		return headerRow;
	}
	
	private List<String> getCustomFieldColumnNames(List<Message<String>> importMsg) {
		
		Message<String> firstMessage = importMsg.get(0);
		List<String> customFieldColumnNames = (List<String>) firstMessage.getHeaders().get(ItemExportImportDTOToCSVTransformer.CUSTOM_FIELD_HEADER);
		return customFieldColumnNames;
	}

	private String getObjectType(List<Message<String>> importMsg) {
		String objectType = null;
		Message<String> firstMessage = importMsg.get(0);
		String firstLineItems[] = firstMessage.getPayload() != null ? firstMessage.getPayload().split(",") : new String[0];
		if (firstLineItems.length > 1){
			objectType = firstLineItems[DCTImportTemplateUtil.OBJECT_TYPE_COLUMN_NUMBER];
		}
		return objectType;
	}
}
