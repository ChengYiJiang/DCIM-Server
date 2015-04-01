package com.raritan.tdz.field.home;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.dto.FieldDTO;

@Transactional(rollbackFor = DataAccessException.class)
public interface FieldHome {
	Map<String, UiComponentDTO> getItemFields(Long classLkpValueCode);  //not use, santo
	
	Map<String, UiComponentDTO> updateItemFields(List<FieldDTO> values); //not use, santo

	List<FieldDetails> getFieldDetailsList(long classValueCode)	throws DataAccessException;

	void updateFieldDetail(FieldDetails fieldDetail) throws DataAccessException;

	List<FieldDetails> getFieldDetail(String uiField, Long fieldId, Long classValueCode);

	List<FieldDetails> getFieldDetail(Long classValueCode);
	
	/**
	 * Get Default Name from the fields table given a ui_component_id
	 * @param uiId
	 * @return
	 */
	String getDefaultName(String uiId);
	
	public Boolean isThisFieldRequiredAtSave( String uiComponentId, Long fieldDetailId, Long classValueCode);

	/**
	 * get the required field information for a given class of item
	 * @param classValueCode
	 * @return
	 */
	public Map<String, Boolean> getFieldRequiredDetail(Long classValueCode);
}