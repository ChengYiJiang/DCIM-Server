/**
 * 
 */
package com.raritan.tdz.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.ValueIdDTO;

/**
 * This will hold a threadlocal of the valueIdDTOList
 * as a map and a list. This can be used by any object
 * that needs to access the valueIdDTO.
 * @author prasanna
 *
 */
public class ValueIdDTOHolder {

	private List<ValueIdDTO> valueIdDTOList;
	private Map<String, Object> valueIdDTOMap;
	
	private static ThreadLocal<ValueIdDTOHolder> valueIdDTOHolder = new ThreadLocal<ValueIdDTOHolder>();
	
	/**
	 * Capture the valueIdDTOList per thread
	 * Also create the map out of this list.
	 * @param valueIdDTOList
	 */
	public static void capture(List<ValueIdDTO> valueIdDTOList){
		
		if (valueIdDTOList == null || valueIdDTOList.isEmpty()){
			clearCurrent();
		}
		
		ValueIdDTOHolder holder = new ValueIdDTOHolder();
		holder.setValueIdDTOList(valueIdDTOList);
		
		//Convert valueIdDTOList to a map
		Map<String,Object> dtoMap = new HashMap<String, Object>();
		for (ValueIdDTO dto:valueIdDTOList){
			dtoMap.put(dto.getLabel(), dto.getData());
		}
		holder.setValueIdDTOMap(dtoMap);
		
		valueIdDTOHolder.set(holder);
	}
	
	/**
	 * Clear the current threadLocal holder of valueIdDTOList
	 */
	public static void clearCurrent() {
		if (valueIdDTOHolder != null) valueIdDTOHolder.set(null);
	}

	/**
	 * Get the current threadLocal holder of valueIdDTOList
	 * <p>For example:</p>
	 * <p>ValueIdDTOHolder.getCurrent().getValue("tiName")
	 * will return the value part of tiName from the valueIdDTOList
	 * sent by client</p>
	 * <p>You must call capture before you use this, else this 
	 * may return null</p>
	 * @return
	 */
	public static ValueIdDTOHolder getCurrent(){
		return valueIdDTOHolder.get();
	}

	public List<ValueIdDTO> getValueIdDTOList() {
		return valueIdDTOList;
	}

	public void setValueIdDTOList(List<ValueIdDTO> valueIdDTOList) {
		this.valueIdDTOList = valueIdDTOList;
	}

	public Map<String, Object> getValueIdDTOMap() {
		return valueIdDTOMap;
	}

	public void setValueIdDTOMap(Map<String, Object> valueIdDTOMap) {
		this.valueIdDTOMap = valueIdDTOMap;
	}
	
	public Object getValue(String id){
		return valueIdDTOMap.get(id);
	}
}
