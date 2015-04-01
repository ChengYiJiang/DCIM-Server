/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import org.springframework.integration.Message;

import java.util.Map;

/**
 * <p>This interface can be used by any implementation that 
 * converts the ImportDTO to CSV format strings</p>
 * @author prasanna
 *
 */
public interface ExportImportDTOMapToCSVTransformer {

	/**
	 * Converts a importDTO to a String containing CSV for the data in the importDTOMsg.
	 * Note that this will be in the order as specified in the Import template
	 * @param importDTOMsg
	 * @return
	 * @throws Exception
	 */
	public Message<String> transform(Message<Map<String, Object>> importDTOMsg) throws Exception;
}
