/**
 * 
 */
package com.raritan.tdz.reports.generator;

import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.reports.dto.ReportCriteriaDTO;
import com.raritan.tdz.reports.dto.ReportStatusDTO;
import com.raritan.tdz.reports.home.ReportsHome;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class ReportGeneratorTest extends TestBase {
	
	ReportsHome reportHome;

	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		reportHome = ctx.getBean("reportHome",ReportsHome.class);
		
	}
	
	@Test
	public void testGenerateReport() throws Exception, BusinessValidationException{
		ReportCriteriaDTO criteriaDTO = new ReportCriteriaDTO();
		
		criteriaDTO.setReportParams(new HashMap<String, Object>());
		criteriaDTO.setReportId(2);
		ReportStatusDTO reportStatusDTO = reportHome.generateReport(criteriaDTO, getTestAdminUser());
		Assert.assertTrue(reportStatusDTO != null);
		while (reportStatusDTO.getProgress() != 100){
			Thread.sleep(100);
			reportStatusDTO = reportHome.getReportStatus(2, getTestAdminUser());
			System.out.print("report Progress: " + reportStatusDTO.getProgress() + "\r");
		}
	}
}
