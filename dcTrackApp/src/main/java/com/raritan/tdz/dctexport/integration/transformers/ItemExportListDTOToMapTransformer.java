/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.ListResultDTO;

/**
 * This transformer will be used to export the list dto data
 * as a map of uiComponentId to value
 * @author prasanna
 *
 */
public class ItemExportListDTOToMapTransformer implements ExportListDTOToMapTransformer {
	public List<Map<String,Object>> transform(ListResultDTO listResultDTO){
		List<Map<String,Object>> resultMapList = new ArrayList<Map<String,Object>>();
			
		List<ColumnDTO> columnDTOList = listResultDTO.getListCriteriaDTO().getColumns();
		List<Object[]> valueList = listResultDTO.getValues();
		
		for (Object[] valueArr:valueList){
			Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
			int columnIdx = 0;
			for (Object value:valueArr){
				if (columnDTOList.get(columnIdx).getUiComponentId() != null)
					resultMap.put(columnDTOList.get(columnIdx).getUiComponentId(), value);
				else if (columnDTOList.get(columnIdx).getFieldName().contains("custom"))
					resultMap.put("Custom Field " + columnDTOList.get(columnIdx).getFieldLabel(), value);
				columnIdx++;
			}
			resultMapList.add(resultMap);
		}
		
		return resultMapList;
	}
}
