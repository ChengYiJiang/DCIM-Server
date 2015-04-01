package com.raritan.tdz.floormaps.home;

import java.util.*;
import java.beans.*;
import java.text.*;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SQLQuery;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.exception.DataAccessException;

import com.raritan.tdz.floormaps.dto.ReportDataDTO;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.DCTrackSessionManager;
import com.raritan.tdz.session.DCTrackSessionManagerInterface;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

/**
 * 
 * @author Randy Chen
 */
public class ReportHomeBase {

	protected Logger log = Logger.getLogger( this.getClass() );
	protected SessionFactory sessionFactory;
	
	protected Map<String,String> defaultColorSchema=new HashMap<String,String>();
	
	public static final String LOW="Low";
	public static final String MEDIUM="Medium";
	public static final String HIGH="High";
	public static final String NO_DATA="No data";
	
	@Autowired
	protected ReportHelper reportHelper;
	
	public ReportHomeBase( SessionFactory sessionFactory ) {
		this.sessionFactory = sessionFactory;
		log.info("Init ReportHomeBase");
		
		defaultColorSchema.put(LOW,"00FF00");
		defaultColorSchema.put(MEDIUM,"FFFF00");
		defaultColorSchema.put(HIGH,"FF0000");
		defaultColorSchema.put(NO_DATA,"AAAAAA");
		
	}
	
	public List<Map> getFilterType() {
	
		List filterTypeList=new ArrayList();
		
		Map filterMap=new HashMap();
		filterMap.put("name","(none)");
		filterMap.put("type","(none)");
		filterTypeList.add(filterMap);
			
		filterMap=new HashMap();
		filterMap.put("name","Customer");
		filterMap.put("type","departmentLkuValue");
		filterTypeList.add(filterMap);
		
		filterMap=new HashMap();
		filterMap.put("name","Function");
		filterMap.put("type","functionLkuValue");
		filterTypeList.add(filterMap);
		
		filterMap=new HashMap();
		filterMap.put("name","Grouping");
		filterMap.put("type","cabinetGrpLkuValue");
		filterTypeList.add(filterMap);
		
		filterMap=new HashMap();
		filterMap.put("name","Type");
		filterMap.put("type","purposeLkuValue");
		filterTypeList.add(filterMap);
				
		return filterTypeList;	
	}
	
	//For common used
	public int getMaxValue(List<Map> dataList) {
		int maxValue=0;
	
		for(Map map : dataList) {
			int value=(Integer)map.get("column2");
			
			if(value>maxValue) {
				maxValue=value;
			}
			
		}
	
		return maxValue;
	}
	
	public Map getThresholdSetting(String reportType) {
	
		Map returnMap=new HashMap();
	
		Map map=reportHelper.getThresholdSetting(reportType);
		log.info("*** getLegendSetting map="+map);
		
		String thresholds=(String)map.get("thresholds");
		
		JsonNode jsonNode=null;
		
		JsonNode isCustomNode=null;
		JsonNode highNode=null;
		JsonNode lowNode=null;
		
		try {
			ObjectMapper mapper = new ObjectMapper(); 
			jsonNode = mapper.readTree(thresholds);
			
			isCustomNode=jsonNode.get("isCustom");
			highNode=jsonNode.get("high");
			lowNode=jsonNode.get("low");
			
		} catch(Exception e) {
			//log.error("",e);
			log.info("No setting data, will use the default settings");
		}
		
		log.info("isCustomNode="+isCustomNode);
			
		boolean isCustom=false;
		if(isCustomNode != null) {
			isCustom=isCustomNode.getBooleanValue();
		}
		
		double high=0.0D;
		if(highNode != null) {
			high=highNode.getDoubleValue();
		}
		
		double low=0.0D;
		if(lowNode != null) {
			low=lowNode.getDoubleValue();
		}
		
		returnMap.put("reportType",reportType);
		returnMap.put("isCustom",isCustom);
		returnMap.put("high",high);
		returnMap.put("low",low);
		
		log.info("returnMap="+returnMap);

		return returnMap;	
	}
	
	public List<Map> getLegend(double minValue, double maxValue, String[] orderArray, Map thresholdSetting, List<Map> dataList) {
		List<Map> legendList=new ArrayList<Map>();
		
		boolean isCustom=(Boolean)thresholdSetting.get("isCustom");
		String type=(String)thresholdSetting.get("type");
		
		double highThreshold=0;
		double lowThreshold=0;
		
		String pattern="";
		if("integer".equals(type)) {
			pattern="#";
		} else if("float".equals(type)) {
			pattern="0.0";
		}
		
		DecimalFormat nf = new DecimalFormat(pattern);
		
		if(isCustom) {
			minValue=(Double)thresholdSetting.get("min");
			maxValue=(Double)thresholdSetting.get("max");
			highThreshold=(Double)thresholdSetting.get("high");
			lowThreshold=(Double)thresholdSetting.get("low");
		} else {
			highThreshold=((maxValue-minValue)*2.0D)/3.0D+minValue;
			lowThreshold=((maxValue-minValue)*1.0D)/3.0D+minValue;
		}
		
		double[] lowRange={ minValue , lowThreshold };
		double[] mediumRange={ lowThreshold , highThreshold};
		double[] highRange={ highThreshold , maxValue };
		
		/*
		if("integer".equals(type)) {
			mediumRange[0]=mediumRange[0]+1;
			highRange[0]=highRange[0]+1;
		}
		*/
		
		Map highMap=new HashMap();
		highMap.put("label",HIGH);
		if(maxValue<=0) {
			highMap.put("range","");
		} else {
		
			//CR53457 Keep constant judgement with CV
			highMap.put("range","> "+nf.format(highRange[0]));

			/*
			if(isCustom) 
				highMap.put("range",">= "+nf.format(highRange[0]));
			else
				highMap.put("range",nf.format(highRange[0])+" - "+nf.format(highRange[1]));
			*/	
		}
		highMap.put("color","#"+defaultColorSchema.get(HIGH));
				
		Map medMap=new HashMap();
		medMap.put("label",MEDIUM);
		if(maxValue<=0) {
			medMap.put("range","");
		} else {
			medMap.put("range",nf.format(mediumRange[0])+" - "+nf.format(mediumRange[1]));
		}
		medMap.put("color","#"+defaultColorSchema.get(MEDIUM));
		
		Map lowMap=new HashMap();
		lowMap.put("label",LOW);
		if(maxValue<=0) {
			lowMap.put("range","");
		} else {
		
			//CR53457 Keep constant judgement with CV
			lowMap.put("range","< "+nf.format(lowRange[1]));
			
			/*
			if(isCustom) 
				lowMap.put("range","<= "+nf.format(lowRange[1]));
			else
				lowMap.put("range",nf.format(lowRange[0])+" - "+nf.format(lowRange[1]));
			*/
		}
		lowMap.put("color","#"+defaultColorSchema.get(LOW));
		
		Map noDataMap=new HashMap();
		noDataMap.put("label",NO_DATA);
		noDataMap.put("range","");
		noDataMap.put("color","#"+defaultColorSchema.get(NO_DATA));
		
		for(String order : orderArray) {
			if(order.equals(HIGH)) legendList.add(highMap); else
			if(order.equals(MEDIUM)) legendList.add(medMap); else
			if(order.equals(LOW)) legendList.add(lowMap); else
			if(order.equals(NO_DATA)) legendList.add(noDataMap);
		}
		
		for(Map dataMap : dataList) {
		
			Object valueObj=dataMap.get("column2");
			
			if(valueObj != null) {
			
				double value=Double.parseDouble(valueObj.toString());
				
				//CR53457 Keep constant judgement with CV
				if(value >= lowRange[0] && value < lowRange[1]) {
					dataMap.put("color","0x"+defaultColorSchema.get(LOW));
				} else if(value >= mediumRange[0] && value <= mediumRange[1]) {
					dataMap.put("color","0x"+defaultColorSchema.get(MEDIUM));
				} else if(value > highRange[0] && value<=highRange[1]) {
					dataMap.put("color","0x"+defaultColorSchema.get(HIGH));
				}
				
				/*
				if("integer".equals(type)) {
					if(value>=lowRange[0] && value<=lowRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(LOW));
					} else if(value>=mediumRange[0] && value<=mediumRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(MEDIUM));
					} else if(value>=highRange[0] && value<=highRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(HIGH));
					}
				} else {
					if(value>=lowRange[0] && value<=lowRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(LOW));
					} else if(value>mediumRange[0] && value<mediumRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(MEDIUM));
					} else if(value>=highRange[0] && value<=highRange[1]) {
						dataMap.put("color","0x"+defaultColorSchema.get(HIGH));
					}
				}
				*/
			
			} else {
				dataMap.put("color","0x"+defaultColorSchema.get(NO_DATA));
			}
			
		}
		
		return legendList;
	}
	
	//Get cabinets from a site in specific criterion 
	public List<Map> getCabinetList(String reportType,String locationCode,String filterType,String filterValue) throws DataAccessException {
		List<Map> cabinetList=new ArrayList<Map>();
				
		Session session = this.sessionFactory.getCurrentSession();
		
		log.info("getCabinetList locationCode="+locationCode);
		log.info("getCabinetList filterType="+filterType);
		log.info("getCabinetList filterValue="+filterValue);

		String joinStr="";
		String criteriaStr="";
		
		if("departmentLkuValue".equals(filterType)) {
			joinStr="LEFT OUTER JOIN dct_lku_data as department ON department.lku_id=details.department_lku_id ";
			criteriaStr="and department.lku_value=:lkuValue ";
		} else if("functionLkuValue".equals(filterType)) {
			joinStr="LEFT OUTER JOIN dct_lku_data as function ON function.lku_id=details.function_lku_id ";
			criteriaStr="and function.lks_id = 6 and function.lku_value=:lkuValue "; //CR52755 select cabinet class only
		} if("purposeLkuValue".equals(filterType)) {
			joinStr="LEFT OUTER JOIN dct_lku_data as purpose ON purpose.lku_id=details.purpose_lku_id ";
			criteriaStr="and purpose.lks_id = 6 and purpose.lku_value=:lkuValue ";
		} if("cabinetGrpLkuValue".equals(filterType)) {
			joinStr="LEFT OUTER JOIN dct_lku_data ON dct_lku_data.lku_id=dct_items_cabinet.cabinet_grp_lku_id ";
			criteriaStr="and dct_lku_data.lku_value=:lkuValue ";
		}

		//Fix CR58128: remove unnecessary inner join
		StringBuilder querySQL= new StringBuilder()
			.append("SELECT ")
			.append("cabinet.item_id AS itemId, ")
			.append("cabinet.item_name As Cabinet,")
			.append("cabinet.cad_handle AS cadHandle ")
			.append("FROM dct_items AS cabinet ")
			.append("INNER JOIN dct_locations AS locations ON cabinet.location_id=locations.location_id ")
			.append("INNER JOIN dct_lks_data AS class_lks ON cabinet.class_lks_id=class_lks.lks_id ")
			.append("INNER JOIN dct_lks_data AS status_lks ON cabinet.status_lks_id=status_lks.lks_id ")
			.append("INNER JOIN dct_items_cabinet on cabinet.item_id=dct_items_cabinet.item_id ")
			.append("INNER JOIN dct_item_details details on cabinet.item_id = details.item_detail_id ")
			.append(joinStr)
			.append("WHERE locations.code= :locationCode ")
			.append("AND cabinet.cad_handle IS NOT NULL ")
			.append("AND class_lks.lkp_value_code in (")
			.append(SystemLookup.Class.CABINET)
			.append(") ")
			.append("AND status_lks.lkp_value_code in (")
			.append(SystemLookup.ItemStatus.PLANNED)
			.append(",")
			.append(SystemLookup.ItemStatus.INSTALLED)
			.append(",")
			.append(SystemLookup.ItemStatus.STORAGE)
			.append(") ")
			.append(criteriaStr)
			.append("GROUP BY cabinet.item_id, cabinet.item_name, cabinet.cad_handle ")
			.append("ORDER BY Cabinet ASC");
		
		log.info("querySQL="+querySQL);
		log.info("filterValue="+filterValue);
		
		SQLQuery query = session.createSQLQuery(querySQL.toString());

		query.addScalar("itemId", new LongType());
		query.addScalar("Cabinet", new StringType());
		query.addScalar("cadHandle", new StringType());
		
		query.setString("locationCode",locationCode);
		
		if(!"".equals(criteriaStr)) {
			query.setString("lkuValue",filterValue);
		}
		
		List<Object[]> results = query.list();
		
		for ( Object[] result : results ) {
		
			Map map=new HashMap();
			
			Long itemId=(Long)result[0];
			String itemName=(String)result[1];
			String cadHandle=(String)result[2];
			
			if(cadHandle==null) {
				cadHandle="NONE";
			}
			
			map.put("itemId",itemId);
			map.put("itemName",itemName);
			map.put("cadHandle",cadHandle);
			
			cabinetList.add(map);
			
		}
		
		return cabinetList;
	}
	
	//Get intersection of two sets - cabinetList and valueMap
	protected List<Map> getDataList(List<Map> cabinetList, Map valueMap) {
		
		List<Map> dataList=new ArrayList<Map>();
		
		for(Map map : cabinetList) {
		
			Map dataMap=new HashMap();
		
			Long itemId=(Long)map.get("itemId");
			String itemName=(String)map.get("itemName");
			String cadHandle=(String)map.get("cadHandle");
			
			dataMap.put("column0",itemId);
			dataMap.put("column1",itemName);
			dataMap.put("color","0x00FF00");
			dataMap.put("cadHandle",cadHandle);
			
			Object value=valueMap.get(itemId);
			dataMap.put("column2", value);
		
			dataList.add(dataMap);
		}
	
		return 	dataList;
	}
	
	protected String getUserUnits() {
	
		//units 1= US
		//units 2 = SI
		String units="1";
		try {
			UserInfo user = RESTAPIUserSessionContext.getUser();
			units=user.getUnits();
		} catch(Exception e) {
			log.error("",e);
			units="1";
		}
		
		log.info("getUserUnits="+units);
		
		return units;
	}
}
