package com.raritan.tdz.reports.dao;


import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.reports.domain.Report;


public interface ReportsDAO extends Dao<Report>{
	List<Report> getAllReports();
	
	List<Report> getAllReportsDetached();
	
	Report getReport(long reportId);
	
	Report getReportDetached(long reportId);
	

}
