package com.raritan.tdz.floormaps.service;

import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.log4j.Logger;

import com.raritan.tdz.exception.*;
import com.raritan.tdz.floormaps.home.ReportHome;
import com.raritan.tdz.floormaps.home.ReportHelper;
import com.raritan.tdz.floormaps.dto.ReportDataDTO;

//import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

public class ReportServiceImpl implements ReportService {

	private Logger log = Logger.getLogger(this.getClass());

	private Map<String,ReportHome> floorMapsReports;
	
	@Autowired
	private ReportHelper reportHelper;
	
	//Default constructor
	public ReportServiceImpl() {
	
	}

	public void setFloorMapsReports(Map<String,ReportHome> floorMapsReports) {
		log.info("setFloorMapsReports...floorMapsReports="+floorMapsReports);
		this.floorMapsReports=floorMapsReports;
	}
	
	public List<Map> getItemList(String locationCode) throws Exception {
		return reportHelper.getItemList(locationCode);
	}
	
	public Map getItems(String locationCode, int pageNo, int start, int limit, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception {
		return reportHelper.getItems(locationCode, pageNo, start, limit, sortColumn, sortDirection, filterClass, filterName);
	}
	
	public Map getRowOffset(String locationCode, String cadHandle, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception {
		return reportHelper.getRowOffset(locationCode, cadHandle, sortColumn, sortDirection, filterClass, filterName);
	}
	
	public List<Map> getReports(String locationCode) {
		List<Map> reportList=new ArrayList<Map>();
					
		Map reportMap=new HashMap();
		reportMap.put("name","(none)");
		reportMap.put("value","(none)");
		reportList.add(reportMap);
		
		log.info("reportHelper="+reportHelper);
		
		List<Map> reports=reportHelper.getReports(locationCode);
		
		//intersection of floorMapsReports and reports
		for(Map map : reports) {
			String key=(String)map.get("value");
			log.info("key0="+key);
			if(key.indexOf("UPS_BANK")!=-1) {
				key="UPS_BANK";
			}
			log.info("key1="+key);
			if(floorMapsReports.containsKey(key)) {
				reportList.add(map);
			}
		}
		
		log.info("reportList="+reportList);
				
		return reportList;
	}
	
	public ReportDataDTO getReportData(String reportType,String locationName,String filterType,String filterValue) throws DataAccessException {
	
		ReportDataDTO reportDataDTO=null;
		
		log.info("service getReportData reportType="+reportType);
		log.info("service getReportData floorMapsReports="+floorMapsReports);
		
		long time1=System.currentTimeMillis();
		
		ReportHome home=getReportHome(reportType);
		
		log.info("ReportHome home="+home);

		try {
		
			reportDataDTO=home.getReportData(reportType,locationName,filterType,filterValue);
			
		} catch (DataAccessException e) {
			log.error("",e);
		}
	
		long time2=System.currentTimeMillis();
		
		log.info("-------getReportData Response time: "+(time2-time1)+"ms");
	
		return reportDataDTO;
	}

	public List<Map> getFilterType(String reportType) {
		log.info("ReportHome getFilterType() reportType="+reportType);
		ReportHome home=getReportHome(reportType);
		log.info("ReportHome getFilterType() home="+home);
		return home.getFilterType();
	}
	
	protected ReportHome getReportHome(String reportType) {
	
		ReportHome home=null;
	
		if(reportType.indexOf("UPS_BANK")!=-1) {
			home=(ReportHome)floorMapsReports.get("UPS_BANK");
		} else {
			home=(ReportHome)floorMapsReports.get(reportType);
		}
	
		return home;
	}
	
	public List<Map> getThresholdSettings(String locationCode) {
		List<Map> list=new ArrayList<Map>();
	
		List<Map> reportList=getReports(locationCode);
		
		for(Map reportMap : reportList) {
			String reportType=(String)reportMap.get("value");
			String reportName=(String)reportMap.get("name");
			
			if("(none)".equals(reportType))
				continue;
			
			ReportHome reportHome=getReportHome(reportType);
			
			log.info("reportType="+reportType+" reportHome="+reportHome);
			
			Map map=reportHome.getThresholdSetting(reportType);
			Map map2=reportHome.getLegendSetting(reportType);
			
			map.put("reportName",reportName);
			
			map.putAll(map2);
			
			list.add(map);
		}
	
		return list;
	}
	
	public boolean updateThresholdSetting(String reportType,String thresholds) throws Exception {
		boolean isSuccess=true;

		try {
			isSuccess=reportHelper.updateThresholdSetting(reportType,thresholds);
		} catch(Exception e) {
			log.error("",e);
			isSuccess=false;
			throw e;
		}
		return isSuccess;
	}
	
}
