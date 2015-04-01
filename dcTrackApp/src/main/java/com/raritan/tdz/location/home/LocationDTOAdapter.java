package com.raritan.tdz.location.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.adapter.DTOAdapterBase;
import com.raritan.tdz.dctimport.dto.Header;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.UnitConverterImpl;
import com.raritan.tdz.util.UnitConverterLookup;

public class LocationDTOAdapter extends  DTOAdapterBase {
	
	private static final String UIID_LOCATION_NAME = "tiLocationName";
	private static final String UIID_STATE_NAME = "tiAddressState";
	private static final String UIID_COUNTRY_NAME = "cmbAddressCountry";
	
	private final Logger log = Logger.getLogger(this.getClass());
	private final String USA_STATE = "USA_STATE";
	private final String COUNTRY = "COUNTRY";
	private final String DATA_CENTER = "DATA CENTER";
	private final String FLOOR = "FLOOR";
	private final String ROOM = "ROOM";
	private final String SI_UNIT = (new Integer((int)UnitConverterLookup.SI_UNIT)).toString();
	
	
	@Autowired
	private UserLookupFinderDAO userLookupDAO;

	@Autowired
	private UnitConverterImpl meterToInchUnitConverter;
	
	@Autowired
	private UnitConverterImpl mmToInchUnitConverter;
	
	@Autowired
	private UnitConverterImpl sqmeterToSqfeetUnitConverter;	

	private Set<String> ignoredUiIds;
	
	public Set<String> getIgnoredUiIds() {
		return ignoredUiIds;
	}

	public void setIgnoredUiIds(Set<String> ignoredUiIds) {
		this.ignoredUiIds = ignoredUiIds;
	}

	public Map<String, Object> convertLocationDetails(
			Map<String, UiComponentDTO> locationUiDTO) {
		Map<String, Object> itemDetails = new HashMap<String, Object>();
		if( locationUiDTO != null ){
			for( String uiId : locationUiDTO.keySet() ){
				UiComponentDTO dto = locationUiDTO.get(uiId);
				Object value = dto.getUiValueIdField().getValue();

				if( uiId.equals(UIID_LOCATION_NAME)){
					itemDetails.put("id", locationUiDTO.get(uiId).getUiValueIdField().getValueId());
				}
				itemDetails.put(uiId, value);
			}
		}
		if(log.isDebugEnabled()) log.debug("locationDetails=" + itemDetails);
		return itemDetails;
	}
	
	private String getStateName( String userSpecifiedStateName ){
		String validStateName = null;
		List<LkuData> stateLkuList = userLookupDAO.findByLkpType(USA_STATE);
		boolean found = false;
		for (LkuData stateLku : stateLkuList){
			if (stateLku.getLkuAttribute() != null && (stateLku.getLkuValue().equalsIgnoreCase(userSpecifiedStateName))	){
				validStateName = stateLku.getLkuValue();
				break;
			}
		}
		
		if (validStateName == null){
			validStateName = userSpecifiedStateName;
		}
		return validStateName;
	}
	
	private String getCountryName( String userSpecifiedCountryName ){
		String validCountryName = null;
		List<LkuData> countryLkuList = userLookupDAO.findByLkpType(COUNTRY);
		
		for (LkuData countryLku : countryLkuList){
			if (countryLku.getLkuValue().equalsIgnoreCase(userSpecifiedCountryName)){
				validCountryName = countryLku.getLkuValue();
				break;
			}
		}
		
		if (validCountryName == null ){
			validCountryName = userSpecifiedCountryName;
		}
		return validCountryName;
	}
	
	private void mToIn(Map<String, Object> itemDetails, String fieldName, Errors errors) {
		
		if (itemDetails.containsKey(fieldName)) {
			try {
				Object value = meterToInchUnitConverter.normalize(itemDetails.get(fieldName), SI_UNIT);
				itemDetails.put(fieldName, Math.round(Float.parseFloat(value.toString())));
			} catch (Exception ex) {
				String code = "locationValidator.cannotConvertValue";
				Object args[]  = {fieldName, "meter", "inch"};
				errors.rejectValue(fieldName, code, args, "Failed to convert value");	
			}
		}		
	}
	
	private void mmToIn(Map<String, Object> itemDetails, String fieldName, Errors errors) {
		
		if (itemDetails.containsKey(fieldName)) {
			try {
				Object value = mmToInchUnitConverter.normalize(itemDetails.get(fieldName), SI_UNIT);
				itemDetails.put(fieldName, Math.round(Float.parseFloat(value.toString())));
			} catch (Exception ex) {
				String code = "locationValidator.cannotConvertValue";
				Object args[]  = {fieldName, "millimeter", "inch"};
				errors.rejectValue(fieldName, code, args, "Failed to convert value");	
			}
		}		
	}
	
	private void sqmToSqft(Map<String, Object> itemDetails, String fieldName, Errors errors) {
		
		if (itemDetails.containsKey(fieldName)) {
			try {
				Object value = sqmeterToSqfeetUnitConverter.normalize(itemDetails.get(fieldName), SI_UNIT);
				itemDetails.put(fieldName, Math.round(Float.parseFloat(value.toString())));
			} catch (Exception ex) {
				String code = "locationValidator.cannotConvertValue";
				Object args[]  = {fieldName, "sq meter", "sq feet"};
				errors.rejectValue(fieldName, code, args, "Failed to convert value");			
			}
		}		
	}
	
	private void transferByUnits(Map<String, Object> itemDetails, Errors errors) {	
		String units = (String)itemDetails.get("units");
		if (units != null) {
			units = units.toUpperCase().trim();
			if (units.equals("SI")) {
				sqmToSqft(itemDetails, "tiLocationArea", errors);
				mToIn(itemDetails, "tiRoomLength", errors);
				mToIn(itemDetails, "tiRoomWidth", errors);
				mmToIn(itemDetails, "tiRaisedFloorHeight", errors);
				mmToIn(itemDetails, "tiFloorCeilingHeight", errors);
				mmToIn(itemDetails, "tiPlenumCeilingHeight", errors);
			}
		}
	}
	
	private void transferExternalKey(Map<String, Object> itemDetails, Errors errors) {	
		
		Object piqExternalKey = itemDetails.get("tiPiqExternalKey");		
		String piqExternalKeyStr = piqExternalKey != null ? piqExternalKey.toString().trim() : "";
		
		if (!piqExternalKeyStr.isEmpty()) {	
			Object piqObjectType = itemDetails.get("piqObjectType");
			String piqObjectTypeStr = piqObjectType != null ? piqObjectType.toString().trim() : "";
			
			if (!piqObjectTypeStr.isEmpty()) {
				itemDetails.put("tiPiqExternalKey", piqObjectTypeStr + ": " + piqExternalKeyStr);
			} else {
				String objectType = "";
				String piqExternalKeyUCStr = piqExternalKeyStr.toUpperCase();
				if (piqExternalKeyUCStr.startsWith(DATA_CENTER)) {
					objectType = DATA_CENTER;
				} else if (piqExternalKeyUCStr.startsWith(FLOOR)) {
					objectType = FLOOR;
				} else if (piqExternalKeyUCStr.startsWith(ROOM)) {
					objectType = ROOM;
				} else {
					String code = "LocationConvert.invalidPiqObjectType";
					Object args[]  = {piqExternalKeyStr};
					errors.rejectValue("tiPiqExternalKey", code, args, "Invalid PowerIQ Object Type");	
				}
				
				itemDetails.put("tiPiqExternalKey", objectType + ": " + piqExternalKeyStr);
			}		
		}
	}
	
	/**
	 * Json Req -> dcTrackApp DTO
	 * Used to convert location requests (save/edit) to dcTrackApp DTO
	 *
	 * id - location is
	 * itemDetails - uiId/value pairs map
	 * 
	 * returns ValueIdDTO list that is used by dcTrackApp
	 **/
	public List<ValueIdDTO> convertToDTOList(long id,
			Map<String, Object> itemDetails) throws DataAccessException,
			HibernateException, ClassNotFoundException, BusinessValidationException {
		List<ValueIdDTO> valueIdDTOList = new ArrayList<ValueIdDTO>();

		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, this.getClass().getName());
		
		transferByUnits(itemDetails, errors);
		transferExternalKey(itemDetails, errors);
		
		for (Entry<String, Object> entry : itemDetails.entrySet()) {
			
			ValueIdDTO dto = null;
			String uiId = entry.getKey();
			
			if (ignoredUiIds.contains(uiId)) continue;

			dto = new ValueIdDTO();
			dto.setLabel(entry.getKey());
			Object value = entry.getValue();

			// If value is String has has id, we have to convert it to id (all cmb fields)
			// UUID_LOCATION_NAME is exception it has id which we have to ignore here
			if( value != null && value instanceof String ){
				if(uiId.equals( UIID_STATE_NAME )){
					value = getStateName((String)value );
				}else if(uiId.equals( UIID_COUNTRY_NAME )){
					value = getCountryName((String) value);
				}else if (!uiId.equals(UIID_LOCATION_NAME)){
					Long idValue = getIdFromValue(uiId, value.toString(), null, null, errors);
					if( idValue != null ) {
						value = idValue;
					}
				}
			} //if
			dto.setData(value);
			valueIdDTOList.add(dto);
		}// for		

		checkForErrors(errors);
		return valueIdDTOList;
	}
	
	public Long getIdFromValue(String uiId, String value, Map<String, Object> additionalAlias, Map<String,Object> additionalRestrictions, Errors errors )
			throws DataAccessException, HibernateException,
			ClassNotFoundException {

		Long idValue = getIdFromValue(uiId, value.toString(), null, null );	
		if( idValue != null && idValue.longValue() < 0 ){
			log.error("Failed to convert \"" + uiId + ": " + value + "\" to propper id");
			String code = "LocationValidator.invalidValue";
			Object args[]  = {uiId, value};
			errors.rejectValue(uiId, code, args, "Failed to convert uiId to id");			
		}
		return idValue;
	}
}
