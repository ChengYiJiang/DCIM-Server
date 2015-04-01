package com.raritan.tdz.field.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.util.CustomFieldsHelper;
import com.raritan.tdz.field.home.FieldHome;

public class FieldServiceImpl implements FieldService {
	private FieldHome fieldHome;
	// private FieldDetails redFd;
	
	public FieldServiceImpl(FieldHome fieldHome) {
		this.fieldHome = fieldHome;
	}


	@Override
	public Map<String, UiComponentDTO> saveFields(List<ValueIdDTO> recList, long classValueCode) throws ServiceLayerException {
		String uiId;
		Long fieldDetailId = null;
		
		for(ValueIdDTO rec:recList){
			uiId = rec.getLabel();
			fieldDetailId = null;
			
			if (CustomFieldsHelper.isCustomField(uiId)) {
				fieldDetailId = CustomFieldsHelper.getCustomFieldDetailId( uiId );
			}
			
			List<FieldDetails> fdList = fieldHome.getFieldDetail(uiId, fieldDetailId, classValueCode);
			for (FieldDetails recFd:fdList) {
				recFd.setIsRequiedAtSave(rec.getData().toString().equals("true"));
				fieldHome.updateFieldDetail(recFd);
			}
		}
		
		return getFields(classValueCode);
	}
	
	@Override
	public Map<String, UiComponentDTO> getFields(long classValueCode) throws ServiceLayerException {
		Map<String, UiComponentDTO> recList = new HashMap<String, UiComponentDTO>();
		
		List<FieldDetails> fdList = fieldHome.getFieldDetailsList(classValueCode);
		//This code assume that first "field=value" pair is for the required field. 
		for(FieldDetails rec:fdList){
			UiComponentDTO dto = getDTOFromFieldDetail( rec );
			//TODO: You may have more records with same uiComponentId. Therefore,
			//      map have fewer entries then actual number.
			recList.put(dto.getUiId(), dto);
		}
		
		return recList;
	}
	
	@Override
	public Map<String, UiComponentDTO> getFields(String uiId, long classValueCode) throws DataAccessException {
		Map<String, UiComponentDTO > fields = new HashMap<String, UiComponentDTO>();
		List<FieldDetails> details = fieldHome.getFieldDetail(uiId, null, classValueCode);
		
		if (details != null) {
			for(FieldDetails rec : details) {
				UiComponentDTO dto = getDTOFromFieldDetail( rec );
				fields.put( dto.getUiId(), dto );	
			}
		}
		
		return fields;
	}
	
	@Override
	public List<UiComponentDTO> getFieldDetail(Long classValueCode) throws DataAccessException {
		List<UiComponentDTO> recList = new ArrayList<UiComponentDTO>();
		
		List<FieldDetails> fdList = fieldHome.getFieldDetailsList(classValueCode);
		//This code assume that first "field=value" pair is for the required field. 
		for(FieldDetails rec:fdList){
			recList.add( getDTOFromFieldDetail(rec) );
		}
		
		return recList; 
	}
	
	
	private UiComponentDTO getDTOFromFieldDetail(FieldDetails rec) {
		UiComponentDTO dto = new UiComponentDTO();
		Fields field = rec.getField();
		dto.setRequired(rec.getIsRequiedAtSave());
		dto.setUiId(field.getUiComponentId());
		dto.setIsConfigurable(rec.getIsConfigurable());
		dto.setPanelId(field.getUiViewPanelId());
		dto.setSortOrder(field.getSortOrder());
		
		if(rec.getDisplayName() == null){
			dto.setUiLabel(field.getDefaultName());
		}
		else{
			dto.setUiLabel(rec.getDisplayName());
		}
		
		// If this is a custom field, set the dynamic uiId format
		if (CustomFieldsHelper.isCustomField( field.getUiComponentId() )) {
			dto.setUiId( CustomFieldsHelper.getCustomFieldUiId(field, rec.getFieldDetailId()) );
		}
		
		return dto;
	}
}
