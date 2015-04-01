/**
 * 
 */
package com.raritan.tdz.dctexport.integration.aggregators;

import java.util.List;

import org.springframework.integration.Message;

/**
 * @author prasanna
 * This interface can be implemented by any export functionality to handle aggregation of data
 */
public interface DCTExportObjectTypeAggregator {
	
	public static final String OBJECT_TYPE_COMMENTS_SIZE = "ObjectTypeCommentsSize";
	/**
	 *  Aggregate the individual object types
	 * @param importMsg
	 * @return
	 */
	Message<List<String>> aggregate(List<Message<String>> importMsg);
}
