/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.page.dto.ListResultDTO;

/**
 * @author prasanna
 *
 */
public interface ExportListDTOToMapTransformer {
	/**
	 * Convert the contents of the listResultDTO to a map of string (generally uiId) and value
	 * @param listResultDTO
	 * @return
	 */
	public List<Map<String,Object>> transform(ListResultDTO listResultDTO);
}
