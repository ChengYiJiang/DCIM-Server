/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.MapToObjectTransformer;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.customfield.dao.CustomFieldsFinderDAO;
import com.raritan.tdz.dctexport.dto.ExportSplitDTO;
import com.raritan.tdz.dctimport.dto.DCTImport;
import com.raritan.tdz.dctimport.dto.ItemImport;

/**
 * Converts the SplitDTO to ImportDTO for Items.
 * @author prasanna
 *
 */
public class ItemExportSplitDTOToItemImportDTOTransformer  implements ExportSplitDTOToImportDTOMapTransformer{
	
	private final String operation = "EDIT";
	private final String objectType;
	private final String customFieldKey = "Custom Field";
	
	@Autowired
	private CustomFieldsFinderDAO customFieldsFinderDAO;
	
	private final Map<String,List<String>> customFieldForModelMap = new ConcurrentHashMap<String, List<String>>();
	
	public ItemExportSplitDTOToItemImportDTOTransformer(String objectType){
		this.objectType = objectType;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctexport.transformers.ExportTransformer#transform(org.springframework.integration.Message)
	 */
	@Override
	@Transactional(readOnly=true)
	public Message<Map<String, Object>> transform(Message<ExportSplitDTO> exportSplitDTO) throws Exception {
		
		Map<String,Object> mapMessage = exportSplitDTO.getPayload().getResultMap();

		fillCustomField(exportSplitDTO, mapMessage);
		
		Message<Map<String,Object>> msg = MessageBuilder.withPayload(mapMessage).copyHeaders(exportSplitDTO.getHeaders())
				.setHeader(ExportSplitDTOToImportDTOMapTransformer.OPERATION_HEADER, operation)
				.setHeader(ExportSplitDTOToImportDTOMapTransformer.OBJECT_TYPE_HEADER, objectType)
				.build();
		
		return msg;
	}

	private void fillCustomField(Message<ExportSplitDTO> exportSplitDTO,
			Map<String,Object> mapMessage) {
		mapMessage.put("tiCustomField",new LinkedHashMap<String, String>());
		String tiClass = (String) mapMessage.get("tiClass");
		List<String> customFieldForModel = customFieldForModelMap.get(tiClass);
		if (customFieldForModel == null){
			customFieldForModel = customFieldsFinderDAO.findClassCustomFieldLabelsByClassLkpValue(tiClass);
			customFieldForModelMap.put(tiClass, customFieldForModel);
		}
		
		for (Map.Entry<String, Object> mapEntry:exportSplitDTO.getPayload().getResultMap().entrySet()){
			String key = mapEntry.getKey();
			Object value = mapEntry.getValue();

			if (key.contains(customFieldKey)){
				String valueStr = value != null ? value.toString():"";
				String customFieldLabel = key.substring(customFieldKey.length() + 1);
				if (customFieldForModel.contains(customFieldLabel)){
					Map<String,String> customFieldMap = (Map<String, String>) mapMessage.get("tiCustomField");
					customFieldMap.put(key, valueStr);
				}
			}
		}
	}
}
