/**
 * 
 */
package com.raritan.tdz.dctimport.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.raritan.tdz.dctimport.dto.DCTImport;

/**
 * @author prasanna
 *
 */
public class DCTImportUtil {

	/**
	 * Using reflection find out the names and return them
	 * @param dctImportClass
	 * @return
	 */
	public static List<String> getNames(Class<? extends DCTImport> dctImportClass){
		List<String> names = new ArrayList<String>();
		
		for (Field field: dctImportClass.getDeclaredFields()){
			names.add(field.getName());
		}
		
		return names;
	}
}
