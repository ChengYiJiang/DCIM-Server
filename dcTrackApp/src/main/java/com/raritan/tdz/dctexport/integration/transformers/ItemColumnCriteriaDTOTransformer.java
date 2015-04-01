/**
 * 
 */
package com.raritan.tdz.dctexport.integration.transformers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;

import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;

/**
 * @author prasanna
 *
 */
public class ItemColumnCriteriaDTOTransformer implements ExportColumnCriteriaTransformer{
	
	private final List<String> columnFieldNames;
	
	
	public ItemColumnCriteriaDTOTransformer(List<String> columnFieldNames) {
		this.columnFieldNames = columnFieldNames;
	}

	@Override
	public Message<List<Object>> transform(Message<List<Object>> argListMsg) {
	
		ListCriteriaDTO listCriteriaDTO = (ListCriteriaDTO) argListMsg.getPayload().get(0);
		List<Object> newPayload = new ArrayList<Object>();
		
//		final Map<String, Boolean> columnFieldNamesFoundMap = new LinkedHashMap<String, Boolean>();
//		for (String columnName:columnFieldNames){
//			columnFieldNamesFoundMap.put(columnName, false);
//		}
		
		List<ColumnCriteriaDTO> columnCriteriaDTOs = listCriteriaDTO.getColumnCriteria();
		List<ColumnCriteriaDTO> newColumnCriteriaDTOs = new ArrayList<ColumnCriteriaDTO>();
		
		final Map<String,ColumnCriteriaDTO> columnCriteriaDTOMap = new LinkedHashMap<String, ColumnCriteriaDTO>();
		if (columnCriteriaDTOs != null){
			for (ColumnCriteriaDTO columnCriteriaDTO:columnCriteriaDTOs){
				columnCriteriaDTOMap.put(columnCriteriaDTO.getName(), columnCriteriaDTO);
			}
		}
		
		//Make sure that those columnCriteriaDTO required for us to perform the sort by columnFieldNames are in the list
		//and is in an order of columnFieldNames
		for (String columnName:columnFieldNames){
			ColumnCriteriaDTO columnCriteriaDTO = columnCriteriaDTOMap.get(columnName);
			if (columnCriteriaDTO != null){
				columnCriteriaDTO.setToSort(true);
				newColumnCriteriaDTOs.add(columnCriteriaDTO);
			} else {
				columnCriteriaDTO = createColumnCriteria(columnName);
				newColumnCriteriaDTOs.add(columnCriteriaDTO);
			}
		}
		
		//Add those columnCriteriaDTO that is not there in the newColumnCriteriaDTO list to the newColumnCriteriaDTO.
		if (columnCriteriaDTOs != null){
			for (ColumnCriteriaDTO columnCriteriaDTO:columnCriteriaDTOs){
				boolean found = false;
				for (ColumnCriteriaDTO newColumnCriteriaDTO:newColumnCriteriaDTOs){
					if (columnCriteriaDTO.getName().equals(newColumnCriteriaDTO.getName()))
					{
						found = true;
						break;
					}
				}
				
				if (!found) newColumnCriteriaDTOs.add(columnCriteriaDTO);
			}
		}
		
		listCriteriaDTO.setColumnCriteria(newColumnCriteriaDTOs);
		
		return argListMsg;
	}
	
	private ColumnCriteriaDTO createColumnCriteria(String columnName){
		ColumnCriteriaDTO dto = new ColumnCriteriaDTO();
		dto.setFilter(new FilterDTO());
		dto.visible = false;
		dto.setName(columnName);
		dto.setToSort(true);
		
		return dto;
	}
}
