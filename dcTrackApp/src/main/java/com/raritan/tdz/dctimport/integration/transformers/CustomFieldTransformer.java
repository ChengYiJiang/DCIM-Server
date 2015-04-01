package com.raritan.tdz.dctimport.integration.transformers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.dctimport.integration.exceptions.IgnoreException;
import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.lookup.UserLookup;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * transforms the custom field value from the import format to the RESTAPI format the server understands
 * @author bunty
 *
 */
public class CustomFieldTransformer implements ImportTransformer {

	private String customFieldPattern = "^\\s*Custom\\s*Field\\s*";
	
	private Pattern pattern = Pattern.compile(customFieldPattern, Pattern.CASE_INSENSITIVE);
	
	private ImportErrorHandler importErrorHandler; 

	
	@Autowired
	private UserLookupFinderDAO userLookupFinderDAO;
	
	public ImportErrorHandler getImportErrorHandler() {
		return importErrorHandler;
	}

	public void setImportErrorHandler(ImportErrorHandler importErrorHandler) {
		this.importErrorHandler = importErrorHandler;
	}

	@Override
	public Message<?> transform(Message<?> message) throws Exception {
		
		List<?> payload = (List<?>) message.getPayload();
		
		@SuppressWarnings("unchecked")
		Map<String,Object> objectAsMap = (Map<String, Object>) payload.get(0);
		
		@SuppressWarnings("unchecked")
		Map<String,String> customFields = (Map<String, String>) objectAsMap.remove("tiCustomField");

		if (null == customFields) return message;
		
		for (Map.Entry<String, String> entry: customFields.entrySet()) {
			
			String customField = entry.getKey();
			
			String value = entry.getValue();

			String customFieldLku = getLkuValue(customField);
			
			List<LkuData> cfs = userLookupFinderDAO.findByLkpValueAndTypeCaseInsensitive(customFieldLku, UserLookup.LkuType.CUSTOM_FIELD);
			
			if (null == cfs || cfs.size() != 1) {
				
				Errors errors = new MapBindingResult(new HashMap<String, String>(), "cfConvertValidationErrors");
				
				Object[] errorArgs = { (null != customFieldLku && customFieldLku.length() > 0) ? customFieldLku : customField };
				errors.rejectValue("customField", "Import.CustomFieldImport.InvalidCustomField", errorArgs, "Custom Field unknown");
				
				importErrorHandler.handleLineErrors(errors);
				
				throw new IgnoreException();

			}

			objectAsMap.put("tiCustomField_" + cfs.get(0).getLkuId(), value);
			
		}
		
		Object[] newPayLoadArray = {objectAsMap, payload.get(1)};
		
		List<?> newPayload = Arrays.asList(newPayLoadArray);
		
		Message<?> newMessage = MessageBuilder.withPayload(newPayload).copyHeaders(message.getHeaders()).build();

	    return newMessage;
	}
	
	private String getLkuValue(String customField) {
		
		Matcher matcher = pattern.matcher(customField);
		
		int endIndex = 0;
		
		while (matcher.find()) {
			
			endIndex = matcher.end();
			
		}
		
		String lkuValue = customField.substring(endIndex).replaceAll("\\s+$", "");
		
		lkuValue = lkuValue.replaceAll("\\s+", " ").trim();
		
		return lkuValue;

	}

}
