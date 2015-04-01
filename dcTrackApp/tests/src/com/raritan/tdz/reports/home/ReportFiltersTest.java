package com.raritan.tdz.reports.home;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.validation.MapBindingResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.reports.domain.Report;
import com.raritan.tdz.reports.domain.ReportConfigDetails;
import com.raritan.tdz.reports.domain.ReportConfig;
import com.raritan.tdz.reports.home.ReportsHome;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.GlobalUtils;
import com.raritan.tdz.util.UnitConverterLookup;

/**
 * @author Santo Rosario
 *
 */
public class ReportFiltersTest extends TestBase {

	ReportsHome reportHome;

	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		reportHome = ctx.getBean("reportHome",ReportsHome.class);
		FlexUserSessionContext.setAllowMockUser(true);
	}

	private MapBindingResult getErrorsObject(Class<?> errorBindingClass) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, errorBindingClass.getName());
		return errors;
	}
	
	protected UserInfo createAdminUserInfo(){
		UserInfo userInfo = new UserInfo(1L, "1", "admin", "admin@localhost",
				"System", "Administrator", "941",
				"", ((Long)UnitConverterLookup.US_UNIT).toString(), "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}

	protected UserInfo createSamerUserInfo(){
		UserInfo userInfo = new UserInfo(18L, "18", "samern", "samer.nassoura@raritan.com",
				"Sammer", "Nassoura", "941",
				"", ((Long)UnitConverterLookup.US_UNIT).toString(), "en-US", "site_administrators",
				"IYDOMGFZGPCTDVKBWIMGOBHYFORSZFJTITLLFEBYLHRRMXSMGQNEKSXJUWJS",
				5256000, true);

		return userInfo;
	}


	@Test
	public void testSaveReportFilters() throws Exception, BusinessValidationException{
		UserInfo userInfo = createAdminUserInfo();
		List<Report> reportList = reportHome.getAllReportsForUser(userInfo);

		for(Report rec:reportList) {
			this.sf.getCurrentSession().evict(rec);
			reportHome.updateReportConfiguration(rec.getReportId(), rec.getReportConfig(), userInfo);
			break;
		}
	}

	@Test
	public void testAddNewReportFilter() throws Exception, BusinessValidationException{
		UserInfo userInfo = createAdminUserInfo();
		List<Report> reportList = reportHome.getAllReportsForUser(userInfo);

		for(Report rec:reportList) {
			this.sf.getCurrentSession().evict(rec);
			rec.getReportConfig().clear();
			rec.getReportConfig().addAll(createReportFilterGroup(rec));
			reportHome.updateReportConfiguration(rec.getReportId(), rec.getReportConfig(), userInfo);

			printReport(rec);

			//Delete Filters
			rec.getReportConfig().clear();
			reportHome.updateReportConfiguration(rec.getReportId(), rec.getReportConfig(), userInfo);
			printReport(rec);
			break;
		}
	}
	
	//NOTE: run this tests manually
	//@Test
	public void testAddReportFilterAsAdmin() throws Exception, BusinessValidationException{
		UserInfo userInfo = createAdminUserInfo();
		reportHome.updateReportConfiguration(1L, createReportFilterGroup(null), userInfo);
		Report report = reportHome.getReportForUser(1L, userInfo);
		printReport(report);
	}
	
	//NOTE: run this tests manually
	//@Test
	public void testAddReportFilterAsSamer() throws Exception, BusinessValidationException{
		UserInfo userInfo = createSamerUserInfo();
		reportHome.updateReportConfiguration(1L, createReportFilterGroup(null), userInfo);
		Report report = reportHome.getReportForUser(1L, userInfo);
		printReport(report);
	}
	//NOTE: run this tests manually
	//@Test
	public void testDeleteReportFilterAsAdmin() throws Exception, BusinessValidationException{
		UserInfo userInfo = createAdminUserInfo();
		reportHome.updateReportConfiguration(1L, null, userInfo);
		Report report = reportHome.getReportForUser(1L, userInfo);

		printReport(report);
	}

	//NOTE: run this test manually
	//@Test
	public void testDeleteReportFilterAsSamer() throws Exception, BusinessValidationException{
		UserInfo userInfo = createSamerUserInfo();
		reportHome.updateReportConfiguration(1L, null, userInfo);
		Report report = reportHome.getReportForUser(1L, userInfo);

		printReport(report);
	}


	@Test
	public void testUpdateReportFilter() throws Exception, BusinessValidationException{
		UserInfo userInfo = createAdminUserInfo();
		List<Report> reportList = reportHome.getAllReportsForUser(userInfo);
		Long reportId = null;

		for(Report rec:reportList) {
			this.sf.getCurrentSession().evict(rec);
			rec.getReportConfig().clear();
			rec.getReportConfig().addAll(createReportFilterGroup(rec));
			reportHome.updateReportConfiguration(rec.getReportId(), rec.getReportConfig(), userInfo);	
			reportId = rec.getReportId();
			break;
		}

		Report report = reportHome.getReportForUser(reportId, userInfo);

		printReport(report);
		ReportConfigDetails filter = null;

		for(ReportConfig fg:report.getReportConfig()) {
			for(ReportConfigDetails f:fg.getReportConfigDetails()) {
				this.sf.getCurrentSession().evict(f);
				filter = f;
				break;
			}	

			fg.getReportConfigDetails().clear();
			fg.getReportConfigDetails().add(filter);
		}

		printReport(report);

		reportHome.updateReportConfiguration(report.getReportId(), report.getReportConfig(), userInfo);

		printReport(report);
		
		//cleanup
		reportHome.updateReportConfiguration(report.getReportId(), null, userInfo);

	}

	private Set<ReportConfig> createReportFilterGroup(Report report) {
		Set<ReportConfig> recList = new HashSet<ReportConfig>();
		ReportConfig rec = new ReportConfig();
		rec.setConfigName("Filter Name 01");
		rec.getReportConfigDetails().addAll(createReportFilters(rec));
		rec.setReport(report);
		rec.setUserId(1L); //admin
		//rec.setCreationDate(GlobalUtils.getCurrentDate());
		recList.add(rec);

		return recList;
	}

	private Set<ReportConfigDetails> createReportFilters(ReportConfig reportConfig) {
		LksData conditionLookup = SystemLookup.getLksData(session, 50027); // =
		LksData filterTypeLookup = SystemLookup.getLksData(session, 50011); // AND

		Set<ReportConfigDetails> recList = new HashSet<ReportConfigDetails>();

		ReportConfigDetails rec = new ReportConfigDetails();
		rec.setFieldName("item_name");
		rec.setConditionLookup(conditionLookup);
		rec.setFieldValue("CLARITY03");
		rec.setReportConfig(reportConfig);
		rec.setUiPanelId("test_panel_id_1");
		rec.setIsParameter(false);
		recList.add(rec);

		rec = new ReportConfigDetails();
		rec.setFieldName("location_id");
		rec.setConditionLookup(conditionLookup);
		rec.setFieldValue("1");
		rec.setReportConfig(reportConfig);
		rec.setUiPanelId("test_panel_id_2");
		rec.setIsParameter(false);
		recList.add(rec);

		return recList;

	}

	private void printReport(Report report) {
		System.out.println("\n+++++++++++++REPORT INFO++++++++++++++++++");
		System.out.println(report.getReportName());

		for(ReportConfig fg:report.getReportConfig()) {
			System.out.println("\t" + fg.getConfigName());

			for(ReportConfigDetails f:fg.getReportConfigDetails()) {
				System.out.println("\t\t" + f.getFieldName());
			}				
		}	
		System.out.println("\n\n");
	}

}
