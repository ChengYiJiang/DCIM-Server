package com.raritan.tdz.floormaps.home;

import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.transaction.annotation.Propagation;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
/**
 * 
 * @author Randy Chen
 */
public class UsedRUsPerCabinetReport extends ReportHomeBase implements ReportHome {

	public UsedRUsPerCabinetReport(SessionFactory sessionFactory ){
		super(sessionFactory);
		log.info("UsedRUsPerCabinetReport init...");
	}

	public Map getLegendSetting(String reportType) {
	
		Map settingMap=new HashMap();
		settingMap.put("type","integer");
		settingMap.put("precision",0);
		settingMap.put("min",0.0D);
		settingMap.put("max",99.0D);
		settingMap.put("unit","RUs");
		
		Map thresholdSetting=super.getThresholdSetting(reportType);
		settingMap.putAll(thresholdSetting);
	
		return settingMap;
	}

	public ReportDataDTO getReportData(String reportType,String locationCode,String filterType,String filterValue) throws DataAccessException {
		List dataList=new ArrayList();
		
		ReportDataDTO reportDataDTO=new ReportDataDTO();
		
		Session session = this.sessionFactory.getCurrentSession();
						
		String[][] columns={
			{"column0","itemId"},
			{"column1","Cabinet"},
			{"column2","Used RUs"},
			{"color","Color"},
			{"cadHandle","CadHandle"}
		};
			
		String querySQL=
			"SELECT items.item_id  AS itemId, items.item_name AS Cabinet,(total_ru-free_ru) AS RUs,items.cad_handle AS cadHandle "+
			"FROM dct_hst_cabinetusage cabinet,dct_items items "+
			"LEFT OUTER JOIN dct_item_details ON items.item_id=dct_item_details.item_detail_id "+
			"LEFT OUTER JOIN dct_lks_data ON items.status_lks_id=dct_lks_data.lks_id "+
			"WHERE cabinet.cabinet_id=items.item_id "+
			"AND cabinet.latest='t' "+
			"AND items.location_id=(select location_id from dct_locations where code=:locationCode) "+
			"AND dct_lks_data.lkp_value_code in ("+
			SystemLookup.ItemStatus.PLANNED+","+
			SystemLookup.ItemStatus.INSTALLED+","+
			SystemLookup.ItemStatus.STORAGE+
			") "+
			"order by RUs desc";
		
		log.info("querySQL="+querySQL);
		log.info("filterValue="+filterValue);
		
		SQLQuery query = session.createSQLQuery(querySQL);

		query.addScalar("itemId", new LongType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("RUs", new IntegerType());
		query.addScalar("cadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		
		List<Object[]> results = query.list();
		
		Map valueMap=new HashMap();
		
		for ( Object[] result : results ) {
			Map map=new HashMap();
			
			Long itemId=(Long)result[0];
			Integer value=(Integer)result[2];
			
			valueMap.put(itemId, value);
		}
		
		List<Map> cabinetList=getCabinetList(reportType, locationCode, filterType, filterValue);		
		dataList=getDataList(cabinetList,valueMap);
		
		Map settingMap=getLegendSetting(reportType);
		log.info("settingMap....="+settingMap);
		
		double minValue=getMinValue(locationCode);
		double maxValue=getMaxValue(locationCode);
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

	public int getMaxValue(String locationCode) {
		int maxValue=0;
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
				
			String querySQL=
			
			"SELECT max(total_ru-free_ru) AS maxRUs "+
			"FROM dct_hst_cabinetusage cabinet,dct_items items "+
			"LEFT OUTER JOIN dct_item_details ON items.item_id=dct_item_details.item_detail_id "+
			"WHERE cabinet.cabinet_id=items.item_id "+
			"AND cabinet.latest='t' "+
			"AND items.location_id=(select location_id from dct_locations where code=:locationCode) "+
			"AND items.cad_handle IS NOT NULL";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("maxRUs", new IntegerType());
			
			query.setString("locationCode",locationCode);
		
			Integer result = (Integer)query.uniqueResult();
			if(result!=null) {
				maxValue=result.intValue();
			}
			
			log.info("maxValue="+maxValue);
			
		} catch(Exception e) {
			log.error("",e);
		}
	
		return maxValue;
	}
	
	public int getMinValue(String locationCode) {
		int minValue=0;
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
				
			String querySQL=
			
			"SELECT min(total_ru-free_ru) AS minRUs "+
			"FROM dct_hst_cabinetusage cabinet,dct_items items "+
			"LEFT OUTER JOIN dct_item_details ON items.item_id=dct_item_details.item_detail_id "+
			"WHERE cabinet.cabinet_id=items.item_id "+
			"AND cabinet.latest='t' "+
			"AND items.location_id=(select location_id from dct_locations where code=:locationCode) "+
			"AND items.cad_handle IS NOT NULL";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("minRUs", new IntegerType());
			
			query.setString("locationCode",locationCode);
		
			Integer result = (Integer)query.uniqueResult();
			if(result!=null) {
				minValue=result.intValue();
			}
			
			log.info("minValue="+minValue);
			
		} catch(Exception e) {
			log.error("",e);
		}
	
		return minValue;
	}

}
