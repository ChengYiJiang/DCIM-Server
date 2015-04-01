package com.raritan.tdz.page.dto;

import java.util.List;

public class LookupOptionDTO {

	private String fieldName;

	private List<String> code;

	private List<String> value;
	
	public String getFieldName() {
		return fieldName;
	}
	
	public void setFieldName(String fieldName) {
		this.fieldName=fieldName;
	}
	
	public List<String> getCode() {
		return code;
	}
	
	public void setCode(List<String> code) {
		this.code=code;
	}
	
	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value=value;
	}
	
	public String toString() {
		String str = super.toString();
		StringBuffer buffer = new StringBuffer();
		buffer.append(str+"\n");
		
		buffer.append("lookupOptionDTO fieldName="+fieldName+"\n");
		
		return buffer.toString();
	}

}
