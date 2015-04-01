package com.raritan.tdz.floormaps.home;

import java.util.*;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.*;
import com.raritan.tdz.domain.*;
import com.raritan.tdz.floormaps.dto.ReportDataDTO;

/**
 * 
 * @author Randy Chen
 */
public interface ReportHome {
		
	@Transactional(readOnly = true)
	public ReportDataDTO getReportData(String reportType,String locationName,String filterType,String filterValue) throws DataAccessException;
	
	public List<Map> getFilterType();
	
	public Map getThresholdSetting(String reportType);
	
	public Map getLegendSetting(String reportType);

}
