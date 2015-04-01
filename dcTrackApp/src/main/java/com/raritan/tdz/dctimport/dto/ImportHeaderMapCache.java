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

/**
 * @author prasanna
 *
 */
public class ImportHeaderMapCache implements DCTImportHeaderMapCache {
	
	private final Map<String, String> headerMap = new LinkedHashMap<String, String>();
	
	private final Map<String, String> reverseHeaderMap = new LinkedHashMap<String, String>();
	
	private final Class<DCTImport> dctImportClass;
	
	public ImportHeaderMapCache(Class<DCTImport> dctImportClass) {
		this.dctImportClass = dctImportClass;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.dto.DCTImportHeaderMapCache#getHeaderMap()
	 */
	@Override
	public Map<String, String> getHeaderMap() {
		return headerMap;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.dto.DCTImportHeaderMapCache#getReverseHeaderMap()
	 */
	@Override
	public Map<String, String> getReverseHeaderMap() {
		return reverseHeaderMap;
	}
	
	public void setupHeaderMap() {
		List<Field> fields = new ArrayList<Field>();
		fields = getAllFields(fields,dctImportClass);
		for (Field field:fields){
			Header header = field.getAnnotation(Header.class);
			if (header != null){
				String key = header.value();
				String value = field.getName();
				headerMap.put(key, value);
				reverseHeaderMap.put(value, key);
			}
		}
	}
	
	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        fields = getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}

}
