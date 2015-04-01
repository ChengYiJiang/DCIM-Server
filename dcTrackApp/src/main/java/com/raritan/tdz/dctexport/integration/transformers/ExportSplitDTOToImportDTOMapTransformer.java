/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import org.springframework.integration.Message;

import com.raritan.tdz.dctexport.dto.ExportSplitDTO;

import java.util.Map;
/**
 * <p>This interface can be used by any implementation that 
 * converts the SplitDTO to ImportDTO</p>
 * @author prasanna
 *
 */
public interface ExportSplitDTOToImportDTOMapTransformer {
	
	public static final String OPERATION_HEADER = "Operation";
	public static final String OBJECT_TYPE_HEADER = "ObjectType";
	/**
	 * 
	 * @param exportSplitDTO
	 * @return
	 * @throws Exception 
	 */
	public Message<Map<String, Object>> transform(Message<ExportSplitDTO> exportSplitDTO) throws Exception;
}
