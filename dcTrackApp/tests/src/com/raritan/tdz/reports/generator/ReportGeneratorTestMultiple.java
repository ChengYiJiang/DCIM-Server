package com.raritan.tdz.reports.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.raritan.tdz.reports.eventhandler.ReportConstants;
import com.raritan.tdz.tests.TestBase;

/**
 * Unit test for report generation
 * @author bunty
 *
 */
public class ReportGeneratorTestMultiple extends TestBase {

	protected static ClassPathXmlApplicationContext ctx = null;
	protected static Session session = null;
	
	private static final String reportDesignsRootFolder = "/home/bunty/projects/4.0/server/dcTrackApp/src/main/webapp/WEB-INF/reports/designs/";
	
	public static class ReportTestData {
		
		String designFile;
		String outputFile;
		String reportFormat;
		String cabinetImageMode;
		String cabinetImageRail;
		String reportOrientation;
		String siteCode;
		List<Integer> itemList;
		
		public ReportTestData(String designFile, String outputFile,
				String reportFormat, String cabinetImageMode,
				String cabinetImageRail, String reportOrientation,
				String siteCode, List<Integer> itemList) {
			super();
			this.designFile = designFile;
			this.outputFile = outputFile;
			this.reportFormat = reportFormat;
			this.cabinetImageMode = cabinetImageMode;
			this.cabinetImageRail = cabinetImageRail;
			this.reportOrientation = reportOrientation;
			this.siteCode = siteCode;
			this.itemList = itemList;
		}

	};
	
	@SuppressWarnings("serial")
	public static final List<ReportTestData> testDataCollection  =
			Collections.unmodifiableList(new ArrayList<ReportTestData>() {{
				
				// add(new ReportTestData("BuswayVisualization.rptdesign", "/tmp/report/BuswayVisualization.html", "html"));

				/**
				 * Cabinet Reports 
				 * */
				
				// Text, Front, landscape, html
				add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "html", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Text, Rear, landscape, html
				add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "html", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Image, Front, landscape, html
				add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "html", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Image, Rear, landscape, html
				add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "html", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Text, Front, landscape, pdf
				// add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "pdf", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Text, Rear, landscape, pdf
				//add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "pdf", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Image, Front, landscape, pdf
				//add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "pdf", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				// Image, Rear, landscape, pdf
				//add(new ReportTestData("CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest", "pdf", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(10)));

				
				// Text, Front, landscape, html
				//add(new ReportTestData("ItemDetails.rptdesign", "/tmp/report/ItemReportTest", "html", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(15)));
				
				// Text, Rear, landscape, html
				//add(new ReportTestData("ItemDetails.rptdesign", "/tmp/report/ItemReportTest", "html", ReportConstants.IMAGEMODE_TEXT, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(15)));

				// Image, Front, landscape, html
				//add(new ReportTestData("ItemDetails.rptdesign", "/tmp/report/ItemReportTest", "html", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_FRONT, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(15)));
				
				// Image, Rear, landscape, html
				//add(new ReportTestData("ItemDetails.rptdesign", "/tmp/report/ItemReportTest", "html", ReportConstants.IMAGEMODE_IMAGE, ReportConstants.IMAGERAIL_REAR, ReportConstants.ORIENTATION_LANDSCAPE, "BN-SITE-1", Arrays.asList(15)));

				
	}});
	
	@DataProvider(name = "reportData")
	public Object[][] validDataProviders() {
		
		int dataSize = testDataCollection.size();
		
		Object[][] dataObjects = new Object[dataSize][8];
		
		int index = 0;
		for (ReportTestData testData: testDataCollection) {
			dataObjects[index][0] = testData.designFile;
			dataObjects[index][1] = testData.outputFile;
			dataObjects[index][2] = testData.reportFormat;
			dataObjects[index][3] = testData.cabinetImageMode;
			dataObjects[index][4] = testData.cabinetImageRail;
			dataObjects[index][5] = testData.reportOrientation;
			dataObjects[index][6] = testData.siteCode;
			dataObjects[index][7] = testData.itemList;
			
			index++;
		}
		
		return dataObjects;

	}

	@Test(dataProvider="reportData")
    public final void generateReportTest(String reportDesignFile, String reportOutputFile, String reportFormat, String imageMode, String cabinetRail, String orientation, String siteCode, List<Integer> itemList) throws Throwable {

		
		Map<String, Object> reportParameters = new HashMap<String, Object>();
		reportParameters.put(ReportConstants.ORIENTATION_PARAM_ID, orientation);
		reportParameters.put(ReportConstants.CAB_IMAGEMODE_PARAM_ID, imageMode);
		reportParameters.put(ReportConstants.CAB_IMAGERAIL_PARAM_ID, cabinetRail);
		reportParameters.put(ReportConstants.SITECODE_PARAM_ID, siteCode);
		if (reportDesignFile.equals("CabinetDetails.rptdesign")) {
			reportParameters.put(ReportConstants.CABINET_LIST_PARAM_ID, itemList.toArray());
		}
		else if (reportDesignFile.equals("ItemDetails.rptdesign")) {
			reportParameters.put(ReportConstants.ITEM_LIST_PARAM_ID, itemList.toArray());
		}
		
		String outFileName = reportOutputFile + imageMode + cabinetRail + orientation + "." + reportFormat;
		
    	birtGenerateReport.generateReport(reportDesignsRootFolder + reportDesignFile, outFileName, reportFormat, reportParameters);
    	
    	log.info("report generated: " + outFileName);
    	
    }
	
	
    @Test
    public final void generateCabinetDetailHtmlReport() throws Throwable {
    	
		Map<String, Object> reportParameters = new HashMap<String, Object>();
		reportParameters.put(ReportConstants.ORIENTATION_PARAM_ID, ReportConstants.ORIENTATION_LANDSCAPE);
		// reportParameters.put(ReportConstants.SITECODE_PARAM_ID, "BN-SITE-1");
		reportParameters.put(ReportConstants.CAB_IMAGEMODE_PARAM_ID, ReportConstants.IMAGEMODE_TEXT);
		reportParameters.put(ReportConstants.CAB_IMAGERAIL_PARAM_ID, ReportConstants.IMAGERAIL_FRONT);
		List<Integer> cabinetIds = new ArrayList<Integer>();
		cabinetIds.add(10);
		reportParameters.put(ReportConstants.CABINET_LIST_PARAM_ID, cabinetIds.toArray());
    	
    	birtGenerateReport.generateReport(reportDesignsRootFolder + "CabinetDetails.rptdesign", "/tmp/report/CabinetReportTest.html", "pdf", reportParameters);
    	
    	log.info("Cabinet Details report generated");
    	
    }
    
    
}
