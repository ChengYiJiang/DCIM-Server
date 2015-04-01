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
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.transaction.annotation.Propagation;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
/**
 * 
 * @author Randy Chen
 */
public class MeasuredAmpsPerCabinetReport extends ReportHomeBase implements ReportHome {

	public MeasuredAmpsPerCabinetReport(SessionFactory sessionFactory ){
		super(sessionFactory);
		log.info("MeasuredAmpsPerCabinetReport init...");
		
		defaultColorSchema.put(HIGH,"FF0000");
		defaultColorSchema.put(MEDIUM,"FFFF00");
		defaultColorSchema.put(LOW,"00FF00");

	}
	
	public Map getLegendSetting(String reportType) {
	
		Map settingMap=new HashMap();
		settingMap.put("type","float");
		settingMap.put("precision",1);
		settingMap.put("min",0.0D);
		settingMap.put("max",999.0D);
		settingMap.put("unit","Amps");
		
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
			{"column2","Measured Amps"},
			{"color","Color"},
			{"cadHandle","CadHandle"}
		};

		String querySQL=
			"SELECT cabinet.item_id AS itemId,cabinet.item_name AS Cabinet,"+
			" sum(dct_ports_power.amps_actual) AS Amps, "+
			" cabinet.cad_handle AS cadHandle "+
			"FROM ((dct_items AS cabinet "+
			" INNER JOIN dct_items ON cabinet.item_id = dct_items.parent_item_id) "+
			" INNER JOIN dct_ports_power ON dct_items.item_id = dct_ports_power.item_id) "+
			" INNER JOIN dct_lks_data ON dct_ports_power.subclass_lks_id = dct_lks_data.lks_id "+
			" INNER JOIN dct_locations ON dct_locations.location_id=dct_items.location_id "+
			" LEFT OUTER JOIN dct_item_details ON cabinet.item_id=dct_item_details.item_detail_id "+
			" LEFT OUTER JOIN dct_items_cabinet ON cabinet.item_id=dct_items_cabinet.item_id "+
			"WHERE dct_locations.code= :locationCode AND dct_ports_power.amps_actual IS NOT null "+
			" AND dct_ports_power.amps_actual>0 AND dct_lks_data.lkp_value_code = 20002 "+
			"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
			"ORDER BY Sum(dct_ports_power.amps_actual), cabinet.item_name";
		
		log.info("querySQL="+querySQL);
		log.info("filterValue="+filterValue);
		
		SQLQuery query = session.createSQLQuery(querySQL);

		query.addScalar("itemId", new LongType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("Amps", new DoubleType());
		query.addScalar("cadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		
		List<Object[]> results = query.list();
		
		Map valueMap=new HashMap();
		
		for ( Object[] result : results ) {
			Map map=new HashMap();
			
			Long itemId=(Long)result[0];
			Double value=(Double)result[2];
			
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
	
	public double getMaxValue(String locationCode) {
		double maxValue=0;
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
				
			String querySQL=
				"SELECT max(allData.Amps) as maxAmps "+
				"from ("+
				"SELECT sum(dct_ports_power.amps_actual) AS Amps "+
				"FROM ((dct_items AS cabinet "+
				" INNER JOIN dct_items ON cabinet.item_id = dct_items.parent_item_id) "+
				" INNER JOIN dct_ports_power ON dct_items.item_id = dct_ports_power.item_id) "+
				" INNER JOIN dct_lks_data ON dct_ports_power.subclass_lks_id = dct_lks_data.lks_id "+
				" INNER JOIN dct_locations ON dct_locations.location_id=dct_items.location_id "+
				"WHERE dct_locations.code= :locationCode AND dct_ports_power.amps_actual IS NOT null "+
				" AND dct_ports_power.amps_actual>0 AND dct_lks_data.lkp_value_code = 20002 "+
				"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
				") as allData";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("maxAmps", new DoubleType());
			
			query.setString("locationCode",locationCode);
		
			Double result = (Double)query.uniqueResult();
			if(result!=null) {
				maxValue=result.doubleValue();
			}
			
			log.info("maxValue="+maxValue);
			
		} catch(Exception e) {
			log.error("",e);
		}
	
		return maxValue;
	}
	
	public double getMinValue(String locationCode) {
		double minValue=0;
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
				
			String querySQL=
				"SELECT min(allData.Amps) as minAmps "+
				"from ("+
				"SELECT sum(dct_ports_power.amps_actual) AS Amps "+
				"FROM ((dct_items AS cabinet "+
				" INNER JOIN dct_items ON cabinet.item_id = dct_items.parent_item_id) "+
				" INNER JOIN dct_ports_power ON dct_items.item_id = dct_ports_power.item_id) "+
				" INNER JOIN dct_lks_data ON dct_ports_power.subclass_lks_id = dct_lks_data.lks_id "+
				" INNER JOIN dct_locations ON dct_locations.location_id=dct_items.location_id "+
				"WHERE dct_locations.code= :locationCode AND dct_ports_power.amps_actual IS NOT null "+
				" AND dct_ports_power.amps_actual>0 AND dct_lks_data.lkp_value_code = 20002 "+
				"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
				") as allData";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("minAmps", new DoubleType());
			
			query.setString("locationCode",locationCode);
		
			Double result = (Double)query.uniqueResult();
			if(result!=null) {
				minValue=result.doubleValue();
			}
			
			log.info("minValue="+minValue);
			
		} catch(Exception e) {
			log.error("",e);
		}
	
		return minValue;
	}
	
}
