package com.raritan.tdz.page.home;

import com.raritan.tdz.exception.DataAccessException;

/**
 * 
 * @author Randy Chen
 */
public interface ExportHome {
	
	public StringBuilder exportToCSV(String option, String jason, String pageType);
	
	public String exportToCSVForImport(String option,String json, String pageType) throws Exception, DataAccessException;
	
}
