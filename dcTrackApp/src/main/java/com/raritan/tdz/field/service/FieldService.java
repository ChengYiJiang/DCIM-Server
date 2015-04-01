package com.raritan.tdz.field.service;

import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

public interface FieldService {

	Map<String, UiComponentDTO> saveFields(List<ValueIdDTO> recList,
			long classValueCode) throws ServiceLayerException;

	Map<String, UiComponentDTO> getFields(long classValueCode)
			throws ServiceLayerException;

	List<UiComponentDTO> getFieldDetail(Long classValueCode) throws DataAccessException;
	
	public Map<String, UiComponentDTO> getFields(String uiId, long classValueCode) throws DataAccessException;
}


