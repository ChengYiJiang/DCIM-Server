package com.raritan.tdz.floormaps.home;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.DoubleType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.transaction.annotation.Propagation;
import com.raritan.tdz.component.inspector.dao.CabinetMetricDao;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
/**
 * 
 * @author Randy Chen
 */
public class BudgetedPowerPerCabinetReport extends ReportHomeBase implements ReportHome {
    private static Logger log = Logger.getLogger(BudgetedPowerPerCabinetReport.class);

    @Autowired
    private CabinetMetricDao cabinetMetricDao;

	public BudgetedPowerPerCabinetReport(SessionFactory sessionFactory ){
		super(sessionFactory);
		log.info("BudgetedPowerPerCabinetReport init...");
		
		defaultColorSchema.put(HIGH,"FF0000");
		defaultColorSchema.put(MEDIUM,"FFFF00");
		defaultColorSchema.put(LOW,"00FF00");

	}
	
	public Map getLegendSetting(String reportType) {
	
		Map settingMap=new HashMap();
		settingMap.put("type","float");
		settingMap.put("precision",3);
		settingMap.put("min",0.0D); //put double anyway even if integer type
		settingMap.put("max",99.0D); //put double anyway even if integer type
		settingMap.put("unit","kW");
		
		Map thresholdSetting=super.getThresholdSetting(reportType);
		settingMap.putAll(thresholdSetting);
	
		return settingMap;
	}
	
	@Transactional(readOnly = false)
	public ReportDataDTO getReportData(String reportType,String locationCode,String filterType,String filterValue) throws DataAccessException {
		List dataList=new ArrayList();
		
		ReportDataDTO reportDataDTO=new ReportDataDTO();
		
		String[][] columns={
			{"column0","itemId"},
			{"column1","Cabinet"},
			{"column2","Budgeted kW"},
			{"color","Color"},
			{"cadHandle","CadHandle"}
		};

        // Get budgeted power of each cabinet
        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) cabinetMetricDao.getCabinetListBudgetedPower(locationCode);

		Map valueMap=new HashMap();
		for ( Object[] result : results ) {
			Map map=new HashMap();
			
			Long itemId = (Long) result[0];
			Double budgetedPower = (Double) result[2];
			
			valueMap.put(itemId, budgetedPower);
		}

        // Minimum and Maximum budgeted power
        double minValue = (Double) results.get(0)[2];
        double maxValue = (Double) results.get(results.size() - 1)[2];
		
		List<Map> cabinetList=getCabinetList(reportType, locationCode, filterType, filterValue);
		dataList=getDataList(cabinetList,valueMap);
		
		Map settingMap=getLegendSetting(reportType);
		log.info("settingMap....="+settingMap);
		
		String[] orderArray={HIGH, MEDIUM, LOW, NO_DATA};
		List<Map> legentList=getLegend(minValue, maxValue, orderArray, settingMap, dataList);
		
		reportDataDTO.setData(dataList);
		reportDataDTO.setLegend(legentList);
		reportDataDTO.setLegendSetting(settingMap);
		
		List<Map> columnNameList=new ArrayList<Map>();
		
		for(String[] columnNames : columns) {
			Map columnNameMap=new HashMap();
			columnNameMap.put(columnNames[0],columnNames[1]);
			columnNameList.add(columnNameMap);
		}
		
		reportDataDTO.setColumns(columnNameList);
		
		return reportDataDTO;
	}
}