/**
 * 
 */
package com.raritan.tdz.dctexport.dto;

import java.util.Map;

/**
 * @author prasanna
 * This DTO contains extracts from the original ListResultDTO to be 
 * processed further for export
 */
public class ExportSplitDTO {
	private Map<String,Object> resultMap;
	private Integer listCnt = new Integer(0);
	private Long uniqueValue = new Long(0);
	
	public Map<String, Object> getResultMap() {
		return resultMap;
	}
	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}
	public Integer getListCnt() {
		return listCnt;
	}
	public void setListCnt(Integer listCnt) {
		this.listCnt = listCnt;
	}
	public Long getUniqueValue() {
		return uniqueValue;
	}
	public void setUniqueValue(Long uniqueValue) {
		this.uniqueValue = uniqueValue;
	}
	
	
}
