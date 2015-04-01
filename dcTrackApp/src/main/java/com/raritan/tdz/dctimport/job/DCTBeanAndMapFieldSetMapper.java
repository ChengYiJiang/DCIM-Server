/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.dto.DCTImport;

/**
 * @author prasanna
 *
 */
public class DCTBeanAndMapFieldSetMapper implements FieldSetMapper<DCTImport> {
	
	private final BeanWrapperFieldSetMapper<DCTImport> beanFieldSetMapper;
	
	//Map of special name (such as customfield) and the property name to set to.
	private final Map<String,String> mapNames;
	
	@Autowired
	private DCTHeaderFieldSetMapper headerFieldSetMapper;
	
	
	public DCTBeanAndMapFieldSetMapper(BeanWrapperFieldSetMapper<DCTImport> beanFieldSetMapper, Map<String,String> mapNames){
		this.beanFieldSetMapper = beanFieldSetMapper;
		this.mapNames = mapNames;
	}
	
	@Override
	public DCTImport mapFieldSet(FieldSet fieldSet) throws BindException {
			
		
		Map<String,Properties> newFieldSetMap = new LinkedHashMap<String,Properties>();
		
		//Essentially we need to separate out the non map fields in the fieldSet from the map fields
		
		//First create a new fieldSet that does not contain any mapNameKeys and get the DTO
		Properties properties = fieldSet.getProperties();
		DCTImport importDTO = mapToBean(newFieldSetMap, properties, fieldSet);
		
		
		//Then create extract map from the original fieldSet for each of the mapNames key
		// and set them to the value part of the DCTImport object via reflection.
		setupMapFields(newFieldSetMap, properties, importDTO);
		
		//Return the DCTImport object
		return importDTO;
	}

	/**
	 * Create a new fieldSet that does not contain any mapName from the mapNames and return the DTO
	 * @param newFieldSetMap
	 * @param properties
	 * @param originalFieldSet TODO
	 * @return
	 * @throws BindException
	 */
	private DCTImport mapToBean(Map<String, Properties> newFieldSetMap,
			Properties properties, FieldSet originalFieldSet) throws BindException {
		@SuppressWarnings("rawtypes")
		Enumeration e = properties.propertyNames();
		
		List<String> newNames = new ArrayList<String>();
		List<String> newValues = new ArrayList<String>();
		//Initialize the newFieldSetMap with properties.
		for (Map.Entry<String, String> mapName:mapNames.entrySet()) {
			newFieldSetMap.put(mapName.getKey(), new Properties());
		}

		//Go through each mapNames to see if it is within the fieldSet
		//If so, dont put them to the newFieldSet used to create the DTO (this way the 
		//If not, add them into the newFieldSetMap which will be used later to
		//set up the map fields with in the DTO.
		while (e.hasMoreElements()){
			String key = (String) e.nextElement();
			String keyNew = key.trim();
			boolean found = false;
			
			for (Map.Entry<String, String> mapName:mapNames.entrySet()) {
				if (keyNew.matches("^" + mapName.getKey() + "$")){
					newFieldSetMap.get(mapName.getKey()).put(key, properties.getProperty(key));
					found = true;
					break;
				}
			}
			
			if (!found){
				newNames.add(key);
				newValues.add(properties.getProperty(key));
			}
		}
	
		FieldSet newFs = new DefaultFieldSet(newValues.toArray(new String[newValues.size()]),newNames.toArray(new String[newNames.size()]));
		
		//Use this to get the bean from the beanFieldSetMapper
		DCTImport importDTO = newNames.size() > 0 && newValues.size() > 0 ? beanFieldSetMapper.mapFieldSet(newFs) : beanFieldSetMapper.mapFieldSet(originalFieldSet);
		return importDTO;
	}
	
	/**
	 * All the map fields from the original fieldSet will be set to the DTO
	 * @param newFieldSetMap
	 * @param properties
	 * @param importDTO
	 * @throws BindException
	 */
	private void setupMapFields(Map<String, Properties> newFieldSetMap,
			Properties properties, DCTImport importDTO) throws BindException {
		
		if (newFieldSetMap.isEmpty()) return;
		
		Errors errors = new MapBindingResult(new HashMap<String, String>(), this.getClass().getName());
		for (Map.Entry<String, String> mapNameEntry:mapNames.entrySet()){
			String mapName = mapNameEntry.getKey();
			String propertyName = mapNameEntry.getValue();
			Map<String,Object> propertyMap = new LinkedHashMap<String, Object>();
			
			Properties newProperties = newFieldSetMap.get(mapName);
			if (null == newProperties) continue;
			
			@SuppressWarnings("rawtypes")
			Enumeration enumeration = newProperties.propertyNames();
			
			while (enumeration.hasMoreElements()){
				String key = (String) enumeration.nextElement();
				Object value = newProperties.getProperty(key);
				String originalKey = headerFieldSetMapper.getOriginalHeader(key);
				if (originalKey != null)
					propertyMap.put(originalKey, value);
				else
					propertyMap.put(key, value);
			}
			
			try {
				BeanUtils.setProperty(importDTO, propertyName, propertyMap);
			} catch (IllegalAccessException | InvocationTargetException e1) {
				Object args[] = {propertyName};
				errors.reject("Import.bind.exception",args,"Import Map bind exception");
				BindException be = new BindException((BindingResult) errors);
				throw be;
			}
		}
	}

}
