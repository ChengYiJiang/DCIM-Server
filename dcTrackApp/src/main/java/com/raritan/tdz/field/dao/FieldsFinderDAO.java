/**
 * 
 */
package com.raritan.tdz.field.dao;

import java.util.List;

import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.domain.Fields;

/**
 * @author prasanna
 * This interface can to fetch/find anything in fields
 * and fields details domain object.
 */
public interface FieldsFinderDAO {
	public List<Fields> findFieldsById(Long fieldId);
	public List<Fields> findFieldsByViewId(String uiViewId);
	public List<Long> findFieldIdsByViewId(String uiViewId);
	
	public List<FieldDetails> findFieldDetailsByFieldId(Long fieldId);
	public List<String> findUiFieldName(String uiComponentId);
	public List<FieldDetails> findFieldDetailsByClass(Long classLookupValueCode);
	public List<FieldDetails> findFieldDetailsByFieldIdAndClass(Long fieldId, Long classLookupValueCode);
}
