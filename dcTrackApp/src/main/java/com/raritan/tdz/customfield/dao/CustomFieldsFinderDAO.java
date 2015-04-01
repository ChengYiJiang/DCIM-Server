/**
 * 
 */
package com.raritan.tdz.customfield.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.LkuData;

/**
 * @author prasanna
 * This is a finder DAO for the custom fields
 */
public interface CustomFieldsFinderDAO extends Dao<LkuData> {
	/**
	 * Get all custom fields for a given class
	 * @param classLkpValueCode
	 * @return
	 */
	public List<LkuData>  findClassCustomFieldsByClassLkp(Long classLkpValueCode);
	
	/**
	 * Get all custom fields for a given modelName
	 * @param modelName
	 * @return
	 */
	public List<LkuData>  findClassCustomFieldsByModelName(String modelName);
	
	/**
	 * Get all custom field labels for a given modelName
	 * @param modelName
	 * @return
	 */
	public List<String> findClassCustomFieldLabelsByModelName(String modelName);
	
	/**
	 * Get all custom field labels for a given classLkpValue
	 * @param className
	 * @return
	 */
	public List<String> findClassCustomFieldLabelsByClassLkpValue(String className);
}
