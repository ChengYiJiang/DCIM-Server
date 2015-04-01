/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.reports.dao.*;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportParameter;
import com.raritan.tdz.tests.TestBase;

public class ReportDaoTest extends TestBase {
	ReportsDAO reportsDAO;
	ReportConfigDetailsDAO reportConfigDetailsDAO;
	ReportConfigDAO reportConfigDAO;
	ReportParamDAO reportParamDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		reportsDAO = (ReportsDAO)ctx.getBean("reportsDAO");
		reportConfigDetailsDAO = (ReportConfigDetailsDAO)ctx.getBean("reportConfigDetailsDAO");
		reportConfigDAO = (ReportConfigDAO)ctx.getBean("reportConfigDAO");
		reportParamDAO = (ReportParamDAO)ctx.getBean("reportParamDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		for(Report rec:reportsDAO.getAllReports()) {
			System.out.println("\n--------------------------------------");
			System.out.println(rec.toString());
			
			for(ReportParameter param:rec.getReportParameters()) {
				System.out.println(param.toString());
			}
		}
	}

	@Test
	public final void tesInsertRecord() throws Throwable {
		Report report = new Report();
		report.setDescription("Test Report Description");
		report.setReportName("Test Report");
		report.setTemplateName("templateName");
		
		Long reportId = reportsDAO.create(report);
		
		Report newReport = reportsDAO.read(reportId);
		
		System.out.println(newReport.toString());
		
		reportsDAO.delete(newReport);
	}

}
