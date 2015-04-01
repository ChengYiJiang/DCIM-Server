/**
 * 
 */
package com.raritan.tdz.dctexport.integration.splitters;

import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;

import com.raritan.tdz.dctexport.dto.ExportSplitDTO;

/**
 * @author prasanna
 *
 */
public interface DCTExportSplitter {
	
	public static String EXPORT_SPLITTER_HEADER = "ExportSplitterHeader";
	public static String EXPORT_SPLITTER_COUNT_HEADER = "ExportSplitterCountHeader";
	public static String EXPORT_SPLITTER_COUNT_TOTAL = "ExportSplitterCountTotal";
	
	/**
	 * Split the ListResultDTO into messages to list of ExportSplitDTO
	 * @param resultDTOMsg
	 * @return
	 * @throws Exception
	 */
	public List<Message<ExportSplitDTO>> split(Message<List<Map<String,Object>>> resultDTOMsg) throws Exception;
}
