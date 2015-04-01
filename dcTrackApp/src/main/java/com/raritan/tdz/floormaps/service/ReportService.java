package com.raritan.tdz.floormaps.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.stereotype.Service;

@Service
public interface ReportService {

	public List<Map> getItemList(String locationCode) throws Exception;
	
	public Map getItems(String locationCode, int pageNo, int start, int limit, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception;
	
	public Map getRowOffset(String locationCode, String cadHandle, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception;

	public List<Map> getReports(String locationCode);
	
	public ReportDataDTO getReportData(String reportType,String locationCode,String filterType,String filterValue) throws DataAccessException;
	
	public List<Map> getFilterType(String reportType);
	
	public List<Map> getThresholdSettings(String locationCode);
	
	public boolean updateThresholdSetting(String reportType,String thresholds) throws Exception;

		
}