/**
 * 
 */
package com.raritan.tdz.settings.validators;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.settings.dto.ApplicationSettingDTO;
import com.raritan.tdz.settings.home.ApplicationSettings;

/**
 * @author prasanna
 *
 */
public class PIQSettingsDTOValidator implements Validator {

	private static int paramCount = 4;
	
	private ApplicationSettings settingsHome;
	
	PIQSettingsDTOValidator(ApplicationSettings settingsHome){
		this.settingsHome = settingsHome;
	}
	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return ApplicationSettings.class.equals(clazz);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//The target is assumed to be a List<ApplicationSettingsDTO>
		List<ApplicationSettingDTO> applicationSettingsDTOList = (List<ApplicationSettingDTO>)target;
		
		//Collect the ParentGroupingIds
		Set<String> parentGroupingIds = new LinkedHashSet<>();
		
		for (ApplicationSettingDTO dto:applicationSettingsDTOList){
			parentGroupingIds.add(dto.getParentGroupingId());
		}
		
		//Collect the PowerIQ IPAddress List from the settings DTO List
		List<String> piqHosts = new ArrayList<>();
		
		for (ApplicationSettingDTO dto:applicationSettingsDTOList){
			if (dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS){
				piqHosts.add(dto.getAppValue());
				
				//The following will be unlikely since the GUI will be providing the proper ids. 
				//This is to make sure that we are not adding duplicates.
				try {
					if (dto.getAppSettingId() < 0 && (settingsHome.getSettingsByPIQHost(dto.getAppValue()).size()) > 0){
						Object[] errorArgs = {dto.getAppValue()};
						errors.reject("appSettings.piq.Exist",errorArgs,"PowerIQ host already exists");
					}
				} catch (DataAccessException e) {
					errors.reject("appSettings.piq.dataAccessException");
				}
			}
		}
		
		//The number of records sent by the client should be equal to five times the number of hosts
		if (piqHosts.size() * (paramCount + 1) != applicationSettingsDTOList.size())
		{
			errors.reject("appSettings.piq.insufficientArgs.generic","Cannot update PowerIQ Settings as there are insufficient attributes provided");
			return;
		}
		
		//Check to see if the powerIQHosts are valid 
		
		//TODO: This is not an efficient algorithm to find if there is corresponding user, password and polling settings in the list
		for (String groupingId:parentGroupingIds){
			int counterIPAddr = 0;
			int counterUsr = 0;
			int counterPswd = 0;
			int counterPoll = 0;
			int counterLabel = 0;
			boolean ignoreCheck = false;
			for (ApplicationSettingDTO dto:applicationSettingsDTOList){
				//If the parentAppSettingId > 0, then we are trying to insert a record for existing poweriIQ host
				//So, we need to ignore the validation for this.
				if (dto.getParentAppSettingId() > 0 && dto.getAppSettingId() == -1) {
					ignoreCheck = true;
					continue;
				}
				
				if ((dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_IPADDRESS)
						&& (dto.getParentGroupingId() != null && dto.getParentGroupingId().equals(groupingId)))
					counterIPAddr++;
				if ((dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_USERNAME)
					&& (dto.getParentGroupingId() != null && dto.getParentGroupingId().equals(groupingId)))
					counterUsr++;
				else if ((dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_PASSWORD)
						&& (dto.getParentGroupingId() != null && dto.getParentGroupingId().equals(groupingId)))
					counterPswd++;
				else if ((dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_POLLING_ENABLED)
						&& (dto.getParentGroupingId() != null && dto.getParentGroupingId().equals(groupingId)))
					counterPoll++;
				else if ((dto.getSettingLkpValueCode() == SystemLookup.ApplicationSettings.PIQ_LABEL)
						&& (dto.getParentGroupingId() != null && dto.getParentGroupingId().equals(groupingId)))
					counterLabel++;
			}
			
			if (!ignoreCheck && (counterIPAddr < 1 || counterIPAddr > 1 || counterUsr < 1 || counterUsr > 1 || counterPswd > 1 || counterPswd < 1 || counterPoll > 1 || counterPoll < 1 || counterLabel > 1 || counterLabel < 1)){
				errors.reject("appSettings.piq.insufficientArgs.generic","Cannot update PowerIQ Settings as there are insufficient attributes provided");
				break;
			}
		}
	}

}
