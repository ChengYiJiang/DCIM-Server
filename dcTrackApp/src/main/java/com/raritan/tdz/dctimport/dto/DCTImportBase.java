/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import com.raritan.tdz.dctimport.integration.exceptions.HeaderNotFoundException;
import com.raritan.tdz.dctimport.integration.exceptions.IncorrectHeaderException;

/**
 * @author prasanna
 *
 */
public abstract class DCTImportBase implements DCTImport {
	
	@Header(value="operation")
	private String operation;
	
	@Header(value="object")
	private String objectType;
	
	@Header(value="errorsandwarnings")
	private String errorsAndWarnings;
	
	@Header(value="_blank_")
	private String blankField;
	
	private final Map<String,String> headerMap;
	
	private final List<String> specialHeaders;
	
	private List<List<String>> unsupportedField;
	
	public DCTImportBase(){
		this.headerMap = new LinkedHashMap<String, String>();
		this.specialHeaders = new ArrayList<String>();
		setupHeaderMap();
	}

	public DCTImportBase(ImportHeaderMapCache importHeaderCache){
		this.headerMap = importHeaderCache.getHeaderMap();
		this.specialHeaders = new ArrayList<String>();
	}
	
	public DCTImportBase(Map<String,String> headerMap){
		this.headerMap = headerMap;
		this.specialHeaders = new ArrayList<String>();
	}
	
	public DCTImportBase(List<String> specialHeaders){
		this.headerMap = new LinkedHashMap<String, String>();
		this.specialHeaders = specialHeaders;
		setupHeaderMap();
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.domain.DCTImport#getOperation()
	 */
	@Override
	public String getOperation() {
		return operation;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctImport.domain.DCTImport#getObjectType()
	 */
	@Override
	public String getObjectType() {
		return objectType;
	}
	
	

	public String getErrorsAndWarnings() {
		return errorsAndWarnings;
	}



	public void setErrorsAndWarnings(String errorsAndWarnings) {
		this.errorsAndWarnings = errorsAndWarnings;
	}

	


	public String getBlankField() {
		return blankField;
	}

	public void setBlankField(String blankField) {
		this.blankField = blankField;
	}

	@Override
	public List<String> getNames(String headerRow, String originalHeaderRow) throws IncorrectHeaderException,HeaderNotFoundException {
		if (headerRow == null || originalHeaderRow == null)
			throw new HeaderNotFoundException();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		
		String[] headerRows = tokenizer.tokenize(headerRow).getValues();
		String[] originalHeaderRows = tokenizer.tokenize(originalHeaderRow).getValues();
		
		List<String> names = new ArrayList<String>();
		
		List<String> invalidNames = new ArrayList<String>();
		
		int cnt = 0;
		
		for (String header:headerRows){
			String key = header.replaceAll("\\*|\\s|\\#", "").toLowerCase();
			String name = headerMap.get(key);
			if (name != null){
				names.add(name);
			} else {
				if (header != null && isSpecialHeader(header))
					names.add(header);
				else if (cnt < originalHeaderRows.length)
					invalidNames.add(originalHeaderRows[cnt]);
				else
					names.add(header);
			}
			
			cnt++;
		}
		
		if (invalidNames.size() > 0)
			throw new IncorrectHeaderException(invalidNames.toString());
		
		return names;
	}

	@Override
	public String getFieldName(String originalHeaderName){
		String key = originalHeaderName.replaceAll("\\*|\\s|\\#", "").toLowerCase();
		return headerMap.get(key);
	}

	private boolean isSpecialHeader(String name) {
		if (specialHeaders != null && specialHeaders.size() > 0){
			String nameNew = name.trim();
			
			for (String specialHeader:specialHeaders){
				if (nameNew.matches("^" + specialHeader + "$")){
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String toString() {
		return "DCTImportBase [operation=" + operation + ", objectType="
				+ objectType + "]";
	}
	
	protected void removeHeaders(List<List<String>> headerKey) {
		
		if (null == headerKey) return;
		
		for (List<String> unsupportedHeaders: headerKey) {
			headerMap.keySet().removeAll(unsupportedHeaders);
		}
	}

	private void setupHeaderMap() {
		List<Field> fields = new ArrayList<Field>();
		fields = getAllFields(fields,this.getClass());
		for (Field field:fields){
			Header header = field.getAnnotation(Header.class);
			if (header != null){
				String key = header.value();
				String value = field.getName();
				headerMap.put(key, value);
			}
		}
		
//		Field[] baseFields = this.getClass().getSuperclass().getFields();
//		for (Field field:baseFields){
//			Header header = field.getAnnotation(Header.class);
//			if (header != null){
//				String key = header.value();
//				String value = field.getName();
//				headerMap.put(key, value);
//			}
//		}
	}
	
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}

	public List<List<String>> getUnsupportedField() {
		return unsupportedField;
	}

	public void setUnsupportedField(List<List<String>> unsupportedField) {
		this.unsupportedField = unsupportedField;
		
		removeHeaders(unsupportedField);
	}
	
}
