/**
 * 
 */
package com.raritan.tdz.dctexport.integration.aggregators;

import java.io.IOException;
import java.util.List;

import org.springframework.integration.Message;

/**
 * @author prasanna
 * This interface can be implemented by any export functionality to handle aggregation of data
 */
public interface DCTExportAggregator {
	/**
	 *  Aggregate the individual object type list
	 * @param importMsg
	 * @return
	 * @throws IOException 
	 */
	public Message<String> aggregate(List<Message<List<String>>> importMsg) throws IOException;
}
