package com.raritan.tdz.util;

import org.springframework.util.StringUtils;

import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.field.domain.Fields;


/**
 * A helper class to encapsulate special handling of custom fields.
 * 
 * @author Andrew Cohen
 */
public class CustomFieldsHelper {
	
	public static final String CUSTOM_FIELD_UI_ID = "tiCustomField";
	
	/**
	 * Returns true if the uiId is for a custom field.
	 * @param uiId the uiId
	 * @return boolean
	 */
	public static boolean isCustomField(String uiId) {
		return uiId != null && uiId.startsWith( CUSTOM_FIELD_UI_ID );
	}
	
	/**
	 * Get the fieldDetail ID from the associated custom field uiId.
	 * uiId Format: "tiCustomField_<fieldDetailId>_<lkuId>"
	 * @param uiId
	 * @return field detail ID
	 */
	public static Long getCustomFieldDetailId(String uiId) {
		Long fieldDetailId = null;
		
		if (!isCustomField( uiId )) return fieldDetailId;
		
		if(uiId.indexOf("_") > 0) {
			String temp[] = uiId.split("_");
			uiId = temp[0];
			 fieldDetailId = Long.valueOf(temp[1]);
		}
		
		return fieldDetailId;
	}
	
	/**
	 * Get the uiId for the specified custom field.
	 * uiId Format: "tiCustomField_<fieldDetailId>_<lkuId>"
	 * @param field the field definition
	 * @param fieldDetailId the field detail ID
	 * @return the uiId or null if this is not a custom field
	 */
	public static String getCustomFieldUiId(Fields field, Long fieldDetailId) {
		if (field == null) return null;
		
		final String uiId = field.getUiComponentId();
		
		if (!isCustomField( uiId )) return null;
		
		StringBuffer buf = new StringBuffer( field.getUiComponentId() );
		buf.append("_");
		buf.append( fieldDetailId );
		buf.append("_");
		buf.append( field.getCustomLku().getLkuId() );
		
		return buf.toString();
	}
	
	/**
	 * Validates a required custom field has a proper value set in the item.
	 * @param customFields
	 * @param fieldDetails
	 * @return
	 */
	public static boolean validateRequired(Item item, Fields requiredCustomField) {
		boolean found = false;
		final long lkuId = requiredCustomField.getCustomLku().getLkuId();
		
		for (CustomItemDetails cid : item.getCustomFields()) {
			final long curLkuId = cid.getCustomAttrNameLookup().getLkuId();
			
			if (curLkuId == lkuId) {
				final String value = cid.getAttrValue();
				
				if (StringUtils.hasText(value)) {
					found = true; // A value is set
					break;
				}
			}
		}
		
		return found;
	}
}
