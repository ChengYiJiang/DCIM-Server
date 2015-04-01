/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.reports.dao.ReportsDAO;
import com.raritan.tdz.reports.domain.Report;

/**
 * @author prasanna
 *
 */
public class ReportGeneratorArgumentValidator implements Validator {
	
	@Autowired
	private ReportsDAO reportsDAO;
	
	public static String REPORT_ID = "reportId";
	public static String USER_INFO = "userInfo";

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Transactional(readOnly=true)
	@Override
	public void validate(Object target, Errors errors) {
		Map<String, Object> targetMap = (Map<String,Object>) target;
		
		Long reportId = (Long) targetMap.get(REPORT_ID);
		UserInfo userInfo = (UserInfo) targetMap.get(USER_INFO);
		
		if (reportId == null || reportId < 0){
			Object[] errorArgs = { "A valid Report Id" };
			errors.reject("Reports.generateReport.requiredParam", errorArgs, "Report Id must be provided");
		} else{
			Report report = reportsDAO.read(reportId);
			if (report == null){
				Object[] errorArgs = { "A valid Report Id" };
				errors.reject("Reports.generateReport.requiredParam", errorArgs, "Report Id must be provided");
			}
		}
		
		if (userInfo == null){
			errors.reject("Reports.generateReport.requiredParam.userInfo", null, "Please login and try again");
		}
	}

}
