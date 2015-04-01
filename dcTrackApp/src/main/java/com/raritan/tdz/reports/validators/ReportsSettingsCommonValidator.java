package com.raritan.tdz.reports.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.json.JSONReportConfig;
import com.raritan.tdz.reports.json.JSONReportConfigDetails;
import com.raritan.tdz.util.RequiredFieldsValidator;

public class ReportsSettingsCommonValidator {

	@Autowired(required=true)
	private ReportsDAO reportsDAO;

	@Autowired(required=true)
	SystemLookupFinderDAO systemLookupFinterDAO;

	@Autowired(required=true)
	private RequiredFieldsValidator jsonReportConfigReqFieldsValidator;

	@Autowired(required=true)
	private RequiredFieldsValidator  jsonReportConfigDetailsReqFieldsValidator;


	private final Logger log = Logger.getLogger(this.getClass());

	private MessageSource messageSource;
	
	@Resource(name="validationInformationCodes")
	protected List<String> validationInformationCodes;


	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public LksData validateAndGetConditionLookup( String lkpValue ) throws BusinessValidationException{
		LksData retval = null;
		List<LksData> lksList = systemLookupFinterDAO.findByLkpValue(lkpValue);
		if( lksList != null && lksList.size() == 1){
			retval = (LksData)lksList.get(0);
		}else{
			log.error("Cannot find condition lookup: " + lkpValue );

			String code = "Reports.invalidConditionLookup";
			Object[] args = { };
			BusinessValidationException.throwBusinessValidationException(code, args, this.getClass(), messageSource );
		}
		return retval;
	}

	public Report validateAndGetReport( Long reportId ) throws BusinessValidationException{
		Report dbReport = null;
		if( reportId != null && reportId.longValue() > 0 ){
			dbReport = reportsDAO.read(reportId);
		}
		if( dbReport == null ){
			log.error("invalid reportId: " + reportId );

			String code = "Reports.invalidReportId";
			Object[] args = { reportId };
			BusinessValidationException.throwBusinessValidationException(code, args, this.getClass(), messageSource );
		}
		return dbReport;
	}

	public void validateRequiredFields( Set<JSONReportConfig> jsonReportConfigs ) throws BusinessValidationException{
		MapBindingResult errors = new MapBindingResult( new HashMap<String, String>(),
				this.getClass().getName());

		for( JSONReportConfig jrc : jsonReportConfigs ){
			jsonReportConfigReqFieldsValidator.validate(jrc, errors);
			if( errors.hasErrors()) BusinessValidationException.throwBusinessValidationException(errors, 
					null, validationInformationCodes, (ResourceBundleMessageSource)messageSource, this.getClass(), null);
			Set<JSONReportConfigDetails> jrcdSet = jrc.getReportConfigDetails();
			if( jrcdSet != null ) for( JSONReportConfigDetails jrcd : jrcdSet ){
				jsonReportConfigDetailsReqFieldsValidator.validate(jrcd, errors);
				if( errors.hasErrors()) BusinessValidationException.throwBusinessValidationException(errors, 
						null, validationInformationCodes, (ResourceBundleMessageSource)messageSource, this.getClass(), null);
			}
		}
	}
}
