package com.raritan.tdz.item.home;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;

/**
 * A utility class providing convenience methods for building item data for testing save.
 * 
 * @author Andrew Cohen
 */
public class SaveUtils {

	/**
	 * Add hardware fields.
	 * @return
	 */
  	public static List<ValueIdDTO> addItemHardwareFields(long mfrId, long modelId) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.add( getField("cmbMake", mfrId) ); 	
  		fields.add( getField("cmbModel", modelId) );
  		
  		return fields;
  	}

  	/**
  	 * Add identity panel fields.
  	 * @param fields
  	 * @param itemName
  	 */
  	public static List<ValueIdDTO> addItemIdentityFields(String itemName, String alias) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		if (itemName != null) {
  			fields.add( getField("tiName", itemName) );
  		}
  		
  		if (alias != null) {
  			fields.add( getField("tiAlias", alias) );
  		}
  		
  		return fields;
  	}
  	
  	/**
  	 * 
  	 * @param locationId
  	 * @param cabinetId
  	 * @param railsUsedLkpValueCode
  	 */
  	public static List<ValueIdDTO> addRackablePlacementFields(long locationId, long cabinetId, long railsUsedLkpValueCode) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.add( getField("cmbLocation", locationId) );
  		fields.add( getField("cmbCabinet", cabinetId) );
  		fields.add( getField("radioRailsUsed", railsUsedLkpValueCode) );
  		
  		return fields;
  	}
  	
  	/**
  	 * 
  	 * @param label
  	 * @param value
  	 * @return
  	 */
  	public static ValueIdDTO getField(String label, Object value) {
  		ValueIdDTO dto = new ValueIdDTO();
  		dto.setLabel( label );
  		dto.setData( value );
  		return dto;
  	}
  	
  	public static long getItemId(Map<String, UiComponentDTO> itemFields) {
		long itemId = -1;
		UiComponentDTO componentDTO = itemFields.get("tiName");
		
		if( componentDTO != null){
			UiValueIdField uiValueIdField = componentDTO.getUiValueIdField();
			if(uiValueIdField != null ) itemId = (Long)uiValueIdField.getValueId(); 
		}
		
		return itemId;
	}
  	
  	public static List<ValueIdDTO> addItemField(String guiFieldName, Object value) {
  		List<ValueIdDTO> fields = new LinkedList<ValueIdDTO>();
  		
  		fields.add( getField(guiFieldName, value) ); 	
  		
  		return fields;
  	}
  	
}
