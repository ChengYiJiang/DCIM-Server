package com.raritan.tdz.floormaps.home;

import java.util.*;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;

import org.springframework.transaction.annotation.Propagation;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.floormaps.domain.ReportThresholds;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
/**
 * 
 * @author Randy Chen
 */
@Transactional
public class ReportHelper {

	protected Logger log = Logger.getLogger( this.getClass() );
	protected SessionFactory sessionFactory;

	public ReportHelper() {
	
	}

	public ReportHelper(SessionFactory sessionFactory ){
		log.info("ReportHelper init...");
		this.sessionFactory = sessionFactory;
	}

	@Transactional(readOnly = true)
	public List<Map> getReports(String locationCode) {
		List<Map> reportList=new ArrayList<Map>();
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
			
			String queryHQL=" "+
									"from LkuData "+
									"where lkuTypeName='FLOORMAPS_REPORT' "+
									"order by sortOrder asc";
								
			Query query = session.createQuery( queryHQL );
				
			List<?> results = query.list();		
		
			for ( Object result : results ) {
		
				Map map=new HashMap();
				LkuData lkuData=(LkuData)result;
				String reportType=lkuData.getLkuAttribute();
				String reportName=lkuData.getLkuValue();				
			
				map.put("value",reportType);
				map.put("name",reportName);
				
				reportList.add(map);
			}	
			
			List<Map> additionList=getUPSBankReports(locationCode,reportList);
			
			reportList.addAll(additionList);
			
			
		} catch(Exception e) {
			log.error("",e);
		}
	
		return reportList;
	}
	
	protected List<Map> getUPSBankReports(String locationCode,List<Map> reportList) {
	
			List<Map> bankList=getUPSBanks(locationCode);
				
			List additionList=new ArrayList();
			
			//Index to be removed
			int removeIndex=-1;
			
			for(int index=0; index<reportList.size(); index++) {
				Map map=(Map)reportList.get(index);
				String reportType=(String)map.get("value");
				String reportName=(String)map.get("name");

				if("UPS_BANK".equals(reportType)) {
					
					Map newMap=new HashMap();
					
					if(bankList.size()>1) {
						String addReportName=reportName+" (All UPS Banks)";
						String addReportType=reportType+"_ALL";
						newMap=new HashMap();
						newMap.put("value",addReportType);
						newMap.put("name",addReportName);
						additionList.add(newMap);
						
					}
					
					for(Map bankMap : bankList ) {
						Long itemId=(Long)bankMap.get("itemId");
						String itemName=(String)bankMap.get("itemName");
					
						String addReportName=reportName+" (UPS Bank "+itemName+")";
						String addReportType=reportType+"_"+itemId;
						
						newMap=new HashMap();
						newMap.put("value",addReportType);
						newMap.put("name",addReportName);
						additionList.add(newMap);
					}
					
					//Remove existed UPS_BANK 
					removeIndex=index;
					
				}
			
			}
						
			if(removeIndex>0) {
				reportList.remove(removeIndex);
			}
	
			return additionList;
	}
	
	protected List<Map> getUPSBanks(String locationCode) {
	
		List<Map> list=new ArrayList<Map>();
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
			
			String queryHQL=" "+
									"from Item "+
									"where classLookup.lkpValueCode=:lkpValueCode and "+
									"dataCenterLocation.code=:locationCode";

			Query query = session.createQuery( queryHQL );
								
			query.setLong("lkpValueCode",SystemLookup.Class.UPS_BANK);
			query.setString("locationCode",locationCode);
				
			List<?> results = query.list();		
		
			for ( Object result : results ) {
		
				Map map=new HashMap();
				Item item=(Item)result;
				long itemId=item.getItemId();
				String itemName=item.getItemName();				
			
				map.put("itemId",itemId);
				map.put("itemName",itemName);
				
				list.add(map);
			}	
			
		
		} catch(Exception e) {
			log.error("",e);
		}
	
		return list;
	}
	
	public Map getThresholdSetting(String reportType) {
		Map map=new HashMap();
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
			
			String queryHQL="";
			Query query=null;
			
			if(reportType.indexOf("UPS_BANK")!=-1) {
				//For UPS Bank
				if("UPS_BANK_ALL".equals(reportType)) {
					queryHQL=" "+
									"from ReportThresholds "+
									"where "+
									"item.itemId is NULL and "+
									"reportLookup.lkuAttribute=:lkuAttribute ";				
					query = session.createQuery( queryHQL );

				} else {
				
					queryHQL=" "+
									"from ReportThresholds "+
									"where "+
									"item.itemId=:itemId and "+
									"reportLookup.lkuAttribute=:lkuAttribute ";
					query = session.createQuery( queryHQL );
					
					long itemId=Long.parseLong(reportType.substring(9));
					query.setLong("itemId",itemId);
				}
				query.setString("lkuAttribute","UPS_BANK");
								
			} else {
				//For Non-UPS Bank
				queryHQL=" "+
								"from ReportThresholds "+
								"where "+
								"reportLookup.lkuAttribute=:lkuAttribute ";	
				 query = session.createQuery( queryHQL );
				 query.setString("lkuAttribute",reportType);
			}
							
			List<?> results = query.list();		
		
			for ( Object result : results ) {
		
				ReportThresholds reportThresholds=(ReportThresholds)result;
				long id=reportThresholds.getId();
				String thresholds=reportThresholds.getThresholds();
				Timestamp updateDate=reportThresholds.getUpdateDate();
							
				map.put("id",id);
				map.put("thresholds",thresholds);
				map.put("updateDate",updateDate);
				
			}
		
		} catch(Exception e) {
			log.error("",e);
		}
	
		return map;
	}
		
	public Map getItems(String locationCode, int pageNo, int start, int limit, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception {

		Map outputMap=new HashMap();
		
		List<Map> dataList=new ArrayList();
		
		int totalCount=0;
		
		Session session = this.sessionFactory.getCurrentSession();
		
		String statusCriteria=getStatusCriteria();
		String classCriteria=getClassCriteria();
			
		StringBuffer pageCriteria=new StringBuffer();
		if(start!=0) {
			pageCriteria.append(" OFFSET "+start);
		}
		if(limit!=0) {
			pageCriteria.append(" LIMIT "+limit);
		}
		
		StringBuffer sortCriteria=new StringBuffer();
		if(sortColumn!=null && !"".equals(sortColumn)) {
			sortCriteria.append(" ORDER BY "+sortColumn+" "+sortDirection);
		}
		
		StringBuffer filterCriteria=new StringBuffer();
		if(filterClass!=null && !"".equals(filterClass) && !"(all)".equals(filterClass)) {
			filterCriteria.append(" AND class.lkp_value='"+filterClass+"'");
		}
		
		if(filterName!=null && !"".equals(filterName)) {
			filterCriteria.append(" AND UPPER(items.item_name) like '%"+filterName.toUpperCase()+"%' ");
		}
		
		String countSQL="SELECT count(*) as totalCount "+
			"FROM dct_items items "+
			"LEFT OUTER JOIN dct_lks_data class ON items.class_lks_id=class.lks_id "+
			"LEFT OUTER JOIN dct_lks_data status ON items.status_lks_id= status.lks_id "+
			"LEFT OUTER JOIN dct_locations location ON items.location_id=location.location_id "+
			"WHERE status.lkp_value_code IN "+statusCriteria+
			" AND class.lkp_value_code IN "+classCriteria+
			" AND location.code=:locationCode AND items.item_id>-1 "+
			filterCriteria;
			
			log.info("countSQL="+countSQL);
			
			SQLQuery query = session.createSQLQuery( countSQL );
			
			query.addScalar("totalCount", new IntegerType());
			
			query.setString("locationCode",locationCode);
		
			Integer countResult = (Integer)query.uniqueResult();
			if(countResult!=null) {
				totalCount=countResult.intValue();
			}
			
		log.info("totalCount="+totalCount);
			
		//CR53783 and CR53857 - Retrieve correct FloorPDU parent cadHandle value
		String querySQL="SELECT "+
			"items.item_id  AS itemId, "+
			"class.lkp_value as Class, "+
			"items.item_name AS Name, "+
			"status.lkp_value as Status, "+
			"location.code as Location, "+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.item_name "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_name from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as Cabinet,"+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.item_id "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_id from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as CabinetId, "+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.cad_handle "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select cad_handle from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as parentCadHandle "+
			"FROM dct_items items "+
			"LEFT OUTER JOIN dct_lks_data class ON items.class_lks_id=class.lks_id "+
			"LEFT OUTER JOIN dct_lks_data status ON items.status_lks_id= status.lks_id "+
			"LEFT OUTER JOIN dct_locations location ON items.location_id=location.location_id "+
			"WHERE status.lkp_value_code IN "+statusCriteria+
			" AND class.lkp_value_code IN "+classCriteria+
			" AND location.code=:locationCode AND items.item_id>-1 "+
			filterCriteria+
			sortCriteria+
			pageCriteria;
		
		log.info("querySQL="+querySQL);
		
		query = session.createSQLQuery(querySQL);

		query.addScalar("itemId", new LongType());
		query.addScalar("Class", new StringType());
		query.addScalar("Name", new StringType());
		query.addScalar("Status", new StringType());
		query.addScalar("Location", new StringType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("CabinetId", new StringType());
		query.addScalar("ParentCadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		
		List<Object[]> results = query.list();
		
		for ( Object[] result : results ) {
		
			Map map=new HashMap();
			
			String itemName=(String)result[2];
			String showName=itemName.replaceAll(" ","&nbsp;");

			map.put("itemId",((Long)result[0]).longValue());
			map.put("Class",(String)result[1]);
			map.put("Name",itemName);
			map.put("ShowName",showName);
			map.put("Status",(String)result[3]);
			map.put("Location",(String)result[4]);
			map.put("Cabinet",(String)result[5]);
			map.put("CabinetId",(String)result[6]);
			map.put("ParentCadHandle",(String)result[7]);
			
			dataList.add(map);
			
		}
		
		log.info("rowCount="+dataList.size());
		
		outputMap.put("totalCount",""+totalCount);
		outputMap.put("items",dataList);
		
		return outputMap;
	}
	
	public List<Map> getItemList(String locationCode) throws Exception {
		List<Map> dataList=new ArrayList();
		
		Session session = this.sessionFactory.getCurrentSession();
				
		//CR53783 and CR53857 - Retrieve correct FloorPDU parent cadHandle value
		String querySQL="SELECT "+
			"items.item_id  AS itemId, "+
			"class.lkp_value as Class, "+
			"items.item_name AS Name, "+
			"status.lkp_value as Status, "+
			"location.code as Location, "+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.item_name "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_name from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as Cabinet,"+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.item_id "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select item_id from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as CabinetId, "+
			"(CASE WHEN  items.class_lks_id in (6,12,13) OR (items.class_lks_id=11 AND items.subclass_lks_id is null) THEN  items.cad_handle "+
			"WHEN  items.class_lks_id in (1,2,3,4,5,7,11) THEN (select cad_handle from dct_items parentitem where items.parent_item_id=parentitem.item_id) "+
			"ELSE null END)  as parentCadHandle "+
			"FROM dct_items items "+
			"LEFT OUTER JOIN dct_lks_data class ON items.class_lks_id=class.lks_id "+
			"LEFT OUTER JOIN dct_lks_data status ON items.status_lks_id= status.lks_id "+
			"LEFT OUTER JOIN dct_locations location ON items.location_id=location.location_id "+
			"WHERE status.lkp_value_code IN ( "+
				SystemLookup.ItemStatus.INSTALLED+","+
				SystemLookup.ItemStatus.PLANNED+","+
				SystemLookup.ItemStatus.POWERED_OFF+","+
				SystemLookup.ItemStatus.OFF_SITE+","+
				SystemLookup.ItemStatus.TO_BE_REMOVED+
			") AND "+
			"class.lkp_value_code IN ( "+
				SystemLookup.Class.CABINET+","+
				SystemLookup.Class.CRAC+","+
				SystemLookup.Class.DATA_PANEL+","+
				SystemLookup.Class.DEVICE+","+
				SystemLookup.Class.FLOOR_PDU+","+
				SystemLookup.Class.NETWORK+","+
				SystemLookup.Class.FLOOR_OUTLET+","+
				SystemLookup.Class.PROBE+","+
				SystemLookup.Class.RACK_PDU+","+
				SystemLookup.Class.UPS+
			") "+
			"AND location.code=:locationCode and items.item_id>-1 "+
			"ORDER BY items.item_name ASC";
		
		log.info("querySQL="+querySQL);
		
		SQLQuery query = session.createSQLQuery(querySQL);

		query.addScalar("itemId", new LongType());
		query.addScalar("Class", new StringType());
		query.addScalar("Name", new StringType());
		query.addScalar("Status", new StringType());
		query.addScalar("Location", new StringType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("CabinetId", new StringType());
		query.addScalar("ParentCadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		
		List<Object[]> results = query.list();
		
		for ( Object[] result : results ) {
		
			Map map=new HashMap();
			
			map.put("itemId",((Long)result[0]).longValue());
			map.put("Class",(String)result[1]);
			map.put("Name",(String)result[2]);
			map.put("Status",(String)result[3]);
			map.put("Location",(String)result[4]);
			map.put("Cabinet",(String)result[5]);
			map.put("CabinetId",(String)result[6]);
			map.put("ParentCadHandle",(String)result[7]);
			
			dataList.add(map);
			
		}
		
		return dataList;
	}
	
	public Map getRowOffset(String locationCode, String cadHandle, String sortColumn, String sortDirection, String filterClass, String filterName) throws Exception {
	
		Map map=new HashMap();
		
		Session session = this.sessionFactory.getCurrentSession();
		
		String statusCriteria=getStatusCriteria();
		String classCriteria=getClassCriteria();
		
		StringBuffer sortCriteria=new StringBuffer();
		if(sortColumn!=null && !"".equals(sortColumn)) {
			if("Name".equals(sortColumn)) {
				sortColumn="items.item_name";
			} else if("Class".equals(sortColumn)) {
				sortColumn="class.lkp_value";
			}
			sortCriteria.append(" ORDER BY "+sortColumn+" "+sortDirection);
		}
		
		StringBuffer filterCriteria=new StringBuffer();
		if(filterClass!=null && !"".equals(filterClass) && !"(all)".equals(filterClass)) {
			filterCriteria.append(" AND class.lkp_value='"+filterClass+"'");
		}
		
		if(filterName!=null && !"".equals(filterName)) {
			filterCriteria.append(" AND UPPER(items.item_name) like '%"+filterName.toUpperCase()+"%' ");
		}
		
		String querySQL="SELECT rowNum,itemId,Name,cadHandle FROM ("+
			"SELECT ROW_NUMBER() over ("+sortCriteria+")-1 as rowNum,"+
			"items.item_id AS itemId, items.item_name AS Name, items.cad_handle AS cadHandle "+
			"FROM dct_items items "+
			"LEFT OUTER JOIN dct_lks_data class ON items.class_lks_id=class.lks_id "+
			"LEFT OUTER JOIN dct_lks_data status ON items.status_lks_id= status.lks_id "+
			"LEFT OUTER JOIN dct_locations location ON items.location_id=location.location_id "+
			"WHERE status.lkp_value_code IN "+statusCriteria+
			" AND class.lkp_value_code IN "+classCriteria+
			" AND location.code=:locationCode AND items.item_id>-1 "+
			filterCriteria+
			") as result "+
			"WHERE result.cadHandle=:cadHandle";
			
		log.info("querySQL="+querySQL);
		
		SQLQuery query = session.createSQLQuery( querySQL );
		
		query.addScalar("rowNum", new LongType());
		query.addScalar("itemId", new LongType());
		query.addScalar("Name", new StringType());
		query.addScalar("cadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		query.setString("cadHandle",cadHandle);
	
		List<Object[]> results = query.list();
	
		for ( Object[] result : results ) {
			map.put("rowNum",((Long)result[0]).longValue());
			map.put("itemId",((Long)result[1]).longValue());
			map.put("Name",(String)result[2]);
			map.put("cadHandle",(String)result[3]);
		}
		
		if(map.size()==0) {
			map.put("rowNum",new Long(-1));
			map.put("itemId",new Long(-1));;
			map.put("Name","");
			map.put("cadHandle","");
		}
		
		log.info("map="+map);

		return map;
	}
	
	public boolean updateThresholdSetting(String reportType, String thresholds) throws Exception {
		boolean isSuccess=true;
		
		//Check threshold existing
		Map map=getThresholdSetting(reportType);
		
		String hql="";
		Query query=null;
		
		Session session = this.sessionFactory.getCurrentSession();
				
		//Setting exists
		if(map!=null && map.size()>0) {
			//Update
			if(reportType.indexOf("UPS_BANK")!=-1) {
				
				if("UPS_BANK_ALL".equals(reportType)) {
					hql="update ReportThresholds a set a.thresholds = :thresholds, a.updateDate =:updateDate "+
							"where a.reportLookup.lkuId=("+
							"	select b.lkuId from LkuData b where b.lkuAttribute=:lkuAttribute"+
							") and "+
							"a.item.itemId is NULL";
					query = session.createQuery( hql );
					query.setString("lkuAttribute","UPS_BANK");
					
				} else {
				
					hql="update ReportThresholds a set a.thresholds = :thresholds, a.updateDate =:updateDate "+
							"where a.reportLookup.lkuId=("+
							"	select b.lkuId from LkuData b where b.lkuAttribute=:lkuAttribute"+
							") and "+
							"a.item.itemId=:itemId";
					query = session.createQuery( hql );
					long itemId=Long.parseLong(reportType.substring(9));
					query.setString("lkuAttribute","UPS_BANK");
					query.setLong("itemId",itemId);
				}
			
			} else {
				hql="update ReportThresholds a set a.thresholds = :thresholds, a.updateDate =:updateDate "+
						"where a.reportLookup.lkuId=("+
						"	select b.lkuId from LkuData b where b.lkuAttribute=:lkuAttribute"+
						")";
				query = session.createQuery( hql );
				query.setString("lkuAttribute",reportType);
				
			}
			
			if(query!=null) {
				query.setString("thresholds",thresholds);
				query.setTimestamp("updateDate",new Timestamp( new Date().getTime() ) );
				
				int rowCount = query.executeUpdate();
				log.info("executeUpdate rowCount="+rowCount);
			}
			 
		} else {
			//Insert
			ReportThresholds reportThresholds=new ReportThresholds();
			reportThresholds.setThresholds(thresholds);
			
			if(reportType.indexOf("UPS_BANK")!=-1) {
			
				LkuData reportLookup = (LkuData)(session.createCriteria(LkuData.class).add( Restrictions.eq("lkuAttribute", "UPS_BANK")).uniqueResult());
				reportThresholds.setReportLookup(reportLookup);
				
				if(!"UPS_BANK_ALL".equals(reportType)) {
					long itemId=Long.parseLong(reportType.substring(9));
					Item item = (Item)(session.createCriteria(Item.class).add( Restrictions.eq("itemId", itemId)).uniqueResult());
					reportThresholds.setItem(item);
				}
				
			} else {
			
				LkuData reportLookup = (LkuData)(session.createCriteria(LkuData.class).add( Restrictions.eq("lkuAttribute", reportType)).uniqueResult());
				reportThresholds.setReportLookup(reportLookup);
				
			}
			
			reportThresholds.setUpdateDate( new Timestamp( new Date().getTime() ) );
			session.save(reportThresholds);	
			session.flush();
			log.info("session.save:(reportThresholds)");
			
		}
				
		return isSuccess;
	}
	
	private String getStatusCriteria() {
		String statusCriteria=
			"( "+
				SystemLookup.ItemStatus.INSTALLED+","+
				SystemLookup.ItemStatus.PLANNED+","+
				SystemLookup.ItemStatus.POWERED_OFF+","+
				SystemLookup.ItemStatus.OFF_SITE+","+
				SystemLookup.ItemStatus.TO_BE_REMOVED+
			") ";
		return statusCriteria;
	}
	
	private String getClassCriteria() {
		String classCriteria=
			"( "+
				SystemLookup.Class.CABINET+","+
				SystemLookup.Class.CRAC+","+
				SystemLookup.Class.DATA_PANEL+","+
				SystemLookup.Class.DEVICE+","+
				SystemLookup.Class.FLOOR_PDU+","+
				SystemLookup.Class.NETWORK+","+
				SystemLookup.Class.FLOOR_OUTLET+","+
				SystemLookup.Class.PROBE+","+
				SystemLookup.Class.RACK_PDU+","+
				SystemLookup.Class.UPS+
			") ";
		return classCriteria;
	}
	
}
