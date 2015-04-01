package com.raritan.tdz.reports.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.reports.domain.ReportConfig;

public interface ReportConfigDAO extends Dao<ReportConfig>{

	public ReportConfig loadReportConfig(long reportConfigId);
		
}
