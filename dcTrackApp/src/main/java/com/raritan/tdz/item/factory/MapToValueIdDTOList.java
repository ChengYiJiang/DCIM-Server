/**
 * 
 */
package com.raritan.tdz.item.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.ValueIdDTO;

/**
 * This will convert a map to a valueIdDTOList
 * @author prasanna
 *
 */
public class MapToValueIdDTOList {
	public static List<ValueIdDTO> convert(Map<String, Object> map){
		
		List<ValueIdDTO> valueIdDTOList = new ArrayList<>();
		
		for (Map.Entry<String, Object> entry:map.entrySet()){
			valueIdDTOList.add(new ValueIdDTO(entry.getKey(), entry.getValue()));
		}
		
		return valueIdDTOList;
	}
}
