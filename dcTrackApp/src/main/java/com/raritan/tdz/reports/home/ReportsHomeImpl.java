package com.raritan.tdz.reports.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.dao.ReportConfigDAO;
import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.generate.ReportGenerator;
import com.raritan.tdz.reports.generate.ReportGeneratorArgumentValidator;
import com.raritan.tdz.reports.generate.exceptions.ReportAlreadyInProgressException;
import com.raritan.tdz.reports.validators.ReportsSettingsCommonValidator;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalUtils;


public class ReportsHomeImpl implements ReportsHome {

	@Autowired(required=true)
	private ReportsDAO reportsDAO;

	@Autowired(required=true)
	ReportConfigDAO reportConfigDAO;

	@Autowired( required=true)
	ReportsSettingsCommonValidator reportsValidator;

	@Autowired
	private ReportGenerator reportGeneratorGateway;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Validator reportGenArgValidator;

	@Override
	public Report getReportForUser(Long reportId, UserInfo userInfo) {

		Report report = null;
		if( reportId != null && userInfo != null && userInfo.getUserId() != null && userInfo.getUserId().length() > 0 ){
			report = reportsDAO.getReportDetached(reportId);

			filterReportConfigs(report.getReportConfig(), userInfo, false);
		}	
		return report;

	}

	@Override
	public List<Report> getAllReportsForUser(UserInfo userInfo) {
		List<Report> allReports = new ArrayList<Report>(0);
		if( userInfo != null && userInfo.getUserId() != null && userInfo.getUserId().length() > 0 ){
			allReports = reportsDAO.getAllReportsDetached();
			for( Report r : allReports ){
				filterReportConfigs(r.getReportConfig(), userInfo, false);
			}
		}
		return allReports;
	}

	/**
	 * 
	 * @param rcSet     - original ReportConfig we need to filter
	 * @param userInfo  - info about the user
	 * @param filterOut - filter ReportConfig in or out for this user
	 *  		true  - ReportConfig for this user will be taken out
	 *  		false - ReportConfig got this user will be in, and for other users will be out
	 * @return
	 */
	private void filterReportConfigs(Set<ReportConfig> rCSet, UserInfo userInfo, Boolean filterOut) {

		if( rCSet != null && rCSet.size() > 0 ){
			Iterator<ReportConfig> iterator = rCSet.iterator();
			while(iterator.hasNext()){
				ReportConfig rc = iterator.next();
				if( filterOut == false ){
					if( rc.getUserId().longValue() != userInfo.getId() ) iterator.remove();
				}else{
					if( rc.getUserId().longValue() == userInfo.getId() ) iterator.remove();
				}
			}
		}
	}

	/**
	 * 
	 * @param rCSet    - reportConfig for all users
	 * @param userInfo - info about the user
	 * @return   **new set** of ReportConfig for this user only
	 */
	private Set<ReportConfig> getReportConfigsForUser(Set<ReportConfig> rCSet, UserInfo userInfo) {
		Set<ReportConfig> retVal = new HashSet<ReportConfig>(0);
		if( rCSet != null && rCSet.size() > 0 ){
			Iterator<ReportConfig> iterator = rCSet.iterator();
			while(iterator.hasNext()){
				ReportConfig rc = iterator.next();
				if( rc.getUserId().longValue() == userInfo.getId() ) retVal.add(rc);
			}
		}

		return retVal;
	}

	@Override
	public ReportStatusDTO generateReport(ReportCriteriaDTO reportCriteria, UserInfo userInfo) throws BusinessValidationException {
		ReportStatusDTO dto = new ReportStatusDTO();
		validateReportGenArgs(reportCriteria.getReportId(), userInfo);

		Report report = getReportForUser(reportCriteria.getReportId(), userInfo);
		try {
			dto = reportGeneratorGateway.generateReport(userInfo.getSessionId(), reportCriteria);
		} catch(Exception e){
			if (e.getCause() instanceof ReportAlreadyInProgressException){
				String code = "Reports.generateReportFailed.reportAlreadyInProgress";
				Object[] args = { report.getReportName() };
				throwBusinessValidationException(code, args);
			} else {
				String code = "Reports.generateReportFailed";
				Object[] args = { report.getReportName() };
				throwBusinessValidationException(code, args);
			}
		}
		return dto;
	}


	private void throwBusinessValidationException(String code, Object[] args)
			throws BusinessValidationException {
		String msg = messageSource.getMessage(code, args, null);
		BusinessValidationException ex = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
		ex.addValidationError( msg );
		ex.addValidationError(code, msg);
		throw ex;
	}


	@Override
	public Set<ReportConfig> updateReportConfiguration(Long reportId, Set<ReportConfig> transientReportConfig, 
			UserInfo userInfo) throws BusinessValidationException {
		Report dbReport = reportsValidator.validateAndGetReport(reportId);
		filterReportConfigs( dbReport.getReportConfig(), userInfo, true );

		if( transientReportConfig != null ){
			for(ReportConfig rec : transientReportConfig) {
				rec.setReport(dbReport);
				rec.setUserId(Long.valueOf(userInfo.getUserId()));
				rec.setCreationDate(GlobalUtils.getCurrentDate());	
			}
			dbReport.getReportConfig().addAll(transientReportConfig);
		}
		dbReport = reportsDAO.merge(dbReport);
		
		return getReportConfigsForUser(dbReport.getReportConfig(), userInfo);

	}


	@Override
	public ReportStatusDTO getReportStatus(long reportId, UserInfo userInfo)
			throws BusinessValidationException {
		validateReportGenArgs(reportId,userInfo);
		ReportStatusDTO statusDTO = reportGeneratorGateway.getReportStatus(userInfo.getSessionId(), reportId);

		if (statusDTO.getErrors().hasErrors()){
			throwBusinessValidationException(statusDTO.getErrors());
		}

		return statusDTO;
	}

	@Override
	public void cancelReportGeneration(long reportId,
			UserInfo userInfo) throws BusinessValidationException {
		validateReportGenArgs(reportId, userInfo);
		reportGeneratorGateway.cancelReportGeneration(userInfo.getSessionId(), reportId);;
	}


	private void throwBusinessValidationException(Errors errors) throws BusinessValidationException {
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
		}
		//If we have validation errors for any of the DTO arguments, then throw that.

		if (e.getValidationErrors().size() > 0){
			e.setCallbackURL(null);
			throw e;
		}
	}

	private Errors getErrorsObject() {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, this.getClass().getName() );
		return errors;
	}

	private void validateReportGenArgs(Long reportId,
			UserInfo userInfo) throws BusinessValidationException {
		Map<String,Object> targetMap = new HashMap<>();
		targetMap.put(ReportGeneratorArgumentValidator.REPORT_ID, reportId);
		targetMap.put(ReportGeneratorArgumentValidator.USER_INFO, userInfo);
		Errors errors = getErrorsObject();
		reportGenArgValidator.validate(targetMap, errors);
		if (errors.hasErrors()) throwBusinessValidationException(errors);
	}


}
