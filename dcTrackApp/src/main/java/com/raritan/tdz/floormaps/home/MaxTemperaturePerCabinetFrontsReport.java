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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.DoubleType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.transaction.annotation.Propagation;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
/**
 * 
 * @author Randy Chen
 */
public class MaxTemperaturePerCabinetFrontsReport extends ReportHomeBase implements ReportHome {

	public MaxTemperaturePerCabinetFrontsReport(SessionFactory sessionFactory ){
		super(sessionFactory);
		log.info("MaxTemperaturePerCabinetFrontsReport init...");
		
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
		settingMap.put("unit","Degrees");
		
		Map thresholdSetting=super.getThresholdSetting(reportType);
		settingMap.putAll(thresholdSetting);
	
		return settingMap;
	}

	public ReportDataDTO getReportData(String reportType,String locationCode,String filterType,String filterValue) throws DataAccessException {
		List dataList=new ArrayList();
		
		ReportDataDTO reportDataDTO=new ReportDataDTO();
		
		String units=getUserUnits();
		
		Session session = this.sessionFactory.getCurrentSession();
				
		//{"column2","Degrees "+("1".equals(units) ? "F" : "C") } <-- cause error when Tomcat start with maven build
		String unitStr="1".equals(units) ? "F" : "C";
		String[][] columns={
			{"column0","itemId"},
			{"column1","Cabinet"},
			{"column2","Degrees "+unitStr },
			{"color","Color"},
			{"cadHandle","CadHandle"}
		};
		
		String unitsQuery="";
		if("1".equals(units)) {
			unitsQuery="round(max((sensor.value_actual) * 9/5 + 32),1) As Degrees ";
		} else {
			unitsQuery="round(max(sensor.value_actual),1) As Degrees ";
		}
		
		String querySQL=
			"SELECT cabinet.item_id AS itemId,cabinet.item_name AS Cabinet,"+
			unitsQuery+","+
			"cabinet.cad_handle AS cadHandle "+
			"FROM dct_items As cabinet "+
			"INNER JOIN dct_ports_sensor As sensor on cabinet.item_id = sensor.cabinet_item_id "+
			"INNER JOIN dct_locations ON dct_locations.location_id=cabinet.location_id "+
			"LEFT OUTER JOIN dct_item_details ON cabinet.item_id=dct_item_details.item_detail_id "+
			"LEFT OUTER JOIN dct_items_cabinet ON cabinet.item_id=dct_items_cabinet.item_id "+
			"WHERE sensor.value_actual > 0 AND sensor.value_actual Is Not Null AND sensor.value_actual<>'NaN' AND "+
			"dct_locations.code= :locationCode AND "+
			"sensor.subclass_lks_id = 424 AND "+ //-- for temperature sensor
			"sensor.cab_loc_lks_id in (950, 952, 954) "+ //-- use for Front Top / Mid / Bottom
			"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
			"ORDER BY cabinet";
		
		log.info("querySQL="+querySQL);
		log.info("filterValue="+filterValue);
		
		SQLQuery query = session.createSQLQuery(querySQL);

		query.addScalar("itemId", new LongType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("Degrees", new DoubleType());
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
				
			String units=getUserUnits();
			
			String unitsQuery="";
			if("1".equals(units)) {
				unitsQuery="max((sensor.value_actual) * 9/5 + 32) As Degrees ";
			} else {
				unitsQuery="max(sensor.value_actual) As Degrees ";
			}
				
			String querySQL=
				"SELECT max(allData.Degrees) as maxDegrees "+
				"from ("+
			"select  cabinet.item_name As cabinet,"+
			unitsQuery+
			"FROM dct_items As cabinet "+
			"INNER JOIN dct_ports_sensor As sensor on cabinet.item_id = sensor.cabinet_item_id "+
			"INNER JOIN dct_locations ON dct_locations.location_id=cabinet.location_id "+
			"LEFT OUTER JOIN dct_item_details ON cabinet.item_id=dct_item_details.item_detail_id "+
			"LEFT OUTER JOIN dct_items_cabinet ON cabinet.item_id=dct_items_cabinet.item_id "+
			"WHERE sensor.value_actual > 0 AND sensor.value_actual Is Not Null AND sensor.value_actual<>'NaN' AND "+
			"dct_locations.code= :locationCode AND "+
			"sensor.subclass_lks_id = 424 AND "+ //-- for temperature sensor
			"sensor.cab_loc_lks_id in (950, 952, 954) "+ //-- use for Front Top / Mid / Bottom
			"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
			") as allData";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("maxDegrees", new DoubleType());
			
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
				
			String units=getUserUnits();
			
			String unitsQuery="";
			if("1".equals(units)) {
				unitsQuery="max((sensor.value_actual) * 9/5 + 32) As Degrees ";
			} else {
				unitsQuery="max(sensor.value_actual) As Degrees ";
			}
				
			String querySQL=
				"SELECT min(allData.Degrees) as minDegrees "+
				"from ("+
			"select  cabinet.item_name As cabinet,"+
			unitsQuery+
			"FROM dct_items As cabinet "+
			"INNER JOIN dct_ports_sensor As sensor on cabinet.item_id = sensor.cabinet_item_id "+
			"INNER JOIN dct_locations ON dct_locations.location_id=cabinet.location_id "+
			"LEFT OUTER JOIN dct_item_details ON cabinet.item_id=dct_item_details.item_detail_id "+
			"LEFT OUTER JOIN dct_items_cabinet ON cabinet.item_id=dct_items_cabinet.item_id "+
			"WHERE sensor.value_actual > 0 AND sensor.value_actual Is Not Null AND sensor.value_actual<>'NaN' AND "+
			"dct_locations.code= :locationCode AND "+
			"sensor.subclass_lks_id = 424 AND "+ //-- for temperature sensor
			"sensor.cab_loc_lks_id in (950, 952, 954) "+ //-- use for Front Top / Mid / Bottom
			"GROUP BY cabinet.item_id,cabinet.cad_handle, cabinet.item_name "+
			") as allData";
			
			SQLQuery query = session.createSQLQuery( querySQL );
			
			query.addScalar("minDegrees", new DoubleType());
			
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
