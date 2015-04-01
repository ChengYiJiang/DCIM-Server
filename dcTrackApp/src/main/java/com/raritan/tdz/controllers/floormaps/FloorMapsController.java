package com.raritan.tdz.controllers.floormaps;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import javax.servlet.http.*;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.session.RESTAPIUserSessionContext;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

import com.raritan.tdz.controllers.assetmgmt.exceptions.DCTRestAPIException;
import com.raritan.tdz.controllers.base.BaseController;
import com.raritan.tdz.page.dto.*;
import com.raritan.tdz.page.service.*;
import com.raritan.tdz.floormaps.dto.*;
import com.raritan.tdz.floormaps.service.*;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.piq.dto.SyncAllPDUReadingsRequestDTO;
import com.raritan.tdz.piq.dto.SyncAllPDUReadingsStatusDTO;
import com.raritan.tdz.piq.home.PIQSyncFloorMap;
import com.raritan.tdz.piq.home.PIQSyncPorts;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;

@Controller
@RequestMapping("/floormaps")
public class FloorMapsController extends BaseController  {
	
	private final Logger log = Logger.getLogger( FloorMapsController.class );
	
	@Autowired(required=true)
	private PaginatedService paginatedService;
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private CadService cadService;
	
	@Autowired
	private PIQSyncFloorMap piqSyncFloorMap;
	
	@Autowired
	private ItemHome itemHome;
	
	/**
	 *  GET items
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/items?locationName=Demo%20SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/items?locationName=Demo%20SITE%20A
	 */
	@RequestMapping(value="/items", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getItems(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=true) String locationName
			) throws Throwable {
		
		Map outputMap=new HashMap();
		
		try {
			
			log.info("locationName="+locationName);
			
			long time1=System.currentTimeMillis();
			
			List<Map> outputList=reportService.getItemList(locationName);
			
			long time2=System.currentTimeMillis();
			
			log.info("outputList.size()="+outputList.size());
			log.info("-------getItems Response time: "+(time2-time1)+"ms");
			
			outputMap.put("items",outputList);

		} catch( Exception e ){
			log.error("",e);
		}	
		return outputMap;
	}
	
	/**
	 *  GET item with pagination criteria
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/items_page?locationName=Demo%20SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/items_page?locationName=Demo%20SITE%20A
	 */
	@RequestMapping(value="/items_page", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getItems(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=true) String locationName,
			@RequestParam(value="page",required=false) Integer page,
			@RequestParam(value="start",required=false) Integer start,
			@RequestParam(value="limit",required=false) Integer limit,
			@RequestParam(value="sort",required=false) String sort,
			@RequestParam(value="filter",required=false) String filter
			) throws Throwable {
		
		Map outputMap=new HashMap();
		
		long time1=System.currentTimeMillis();
			
		int test = 0;
		
		try {	
		
			int pageNo=0;
			int startOffset=0;
			int pageSize=0;
			
			Map<String,String> paramMap=getSortAndFilterParams(sort, filter);
					
			String filterClass=paramMap.get("filterClass");
			String filterName=paramMap.get("filterName");
			String sortColumn=paramMap.get("sortColumn");
			String sortDirection=paramMap.get("sortDirection");
		
			if(page!=null) {
				pageNo=page.intValue();
			}
		
			if(start!=null) {
				startOffset=start.intValue();
			}
			
			if(limit!=null) {
				pageSize=limit.intValue();
			}
			
			log.info("locationName="+locationName);
			log.info("pageNo="+pageNo);
			log.info("startOffset="+startOffset);
			log.info("pageSize="+pageSize);
			
			log.info("sortColumn="+sortColumn);
			log.info("sortDirection="+sortDirection);
			log.info("filterClass="+filterClass);
			log.info("filterName="+filterName);
			
			//CR58489			
			locationName=java.net.URLDecoder.decode(locationName, "UTF-8"); 
			
			outputMap=reportService.getItems(
				locationName,
				pageNo,
				startOffset,
				pageSize,
				sortColumn,
				sortDirection,
				filterClass,
				filterName
			);
			
		} catch( Exception e ){
			log.error("",e);
		}
		
		long time2=System.currentTimeMillis();	
		log.info("-------getItems Response time: "+(time2-time1)+"ms");
		
		return outputMap;
	}
	
	/**
	 *  GET row offset with specific criteria
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/row_offset?locationName=Demo%20SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/row_offset?locationName=Demo%20SITE%20A
	 */
	@RequestMapping(value="/row_offset", method=RequestMethod.GET)
	public @ResponseBody Map getRowOffset(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=true) String locationName,
			@RequestParam(value="cadHandle",required=false) String cadHandle,
			@RequestParam(value="sort",required=false) String sort,
			@RequestParam(value="filter",required=false) String filter
			) throws Throwable {
		
		Map outputMap=new HashMap();
						
		try {	
		
			Map<String,String> paramMap=getSortAndFilterParams(sort, filter);
					
			String filterClass=paramMap.get("filterClass");
			String filterName=paramMap.get("filterName");
			String sortColumn=paramMap.get("sortColumn");
			String sortDirection=paramMap.get("sortDirection");
			
			log.info("locationName="+locationName);
			log.info("cadHandle="+cadHandle);
			
			log.info("sortColumn="+sortColumn);
			log.info("sortDirection="+sortDirection);
			log.info("filterClass="+filterClass);
			log.info("filterName="+filterName);
						
			outputMap=reportService.getRowOffset(
				locationName,
				cadHandle,
				sortColumn,
				sortDirection,
				filterClass,
				filterName
			);
			
		} catch( Exception e ){
			log.error("",e);
		}
				
		return outputMap;
	}
	

	/**
	 *  Get report definition
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/api/floormaps/report_definition
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp//api/floormaps/report_definition
	 */
	@RequestMapping(value="/report_definition", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getReportDefinition(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=false) String locationName
			) throws Throwable {
		
		Map outputMap=new HashMap();
		
		try {
		
			//for DEBUG
			if(locationName==null) {
				locationName="SITE A";
			}
									
			List<Map> reportList=reportService.getReports(locationName);
			
			Map filterTypeMap=new LinkedHashMap();
			List<Map> filterTypeList=new ArrayList<Map>();
			
			Map filterMap=new HashMap();
			filterTypeMap.put("(none)",filterTypeList);
						
			for(Map reportMap : reportList) {		
				String reportName=(String)reportMap.get("name");
				String reportType=(String)reportMap.get("value");
				
				if(!"(none)".equals(reportName)) {
					filterTypeList=reportService.getFilterType(reportType);
					filterTypeMap.put(reportType,filterTypeList);
				}
			}
				
			outputMap.put("report",reportList);
			outputMap.put("filterType",filterTypeMap);

		} catch( Exception e ){
			log.error("",e);
		}	
		return outputMap;
	}
	
	/**
	 *  Get report lookup data
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/report_lookup?filterType=departmentLkuValue
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/report_lookup?filterType=departmentLkuValue
	 */
	@RequestMapping(value="/report_lookup", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getReportLookup (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="filterType",required=true) String filterType
			) throws Throwable {
		
		Map outputMap=new HashMap();
		
		try {
			
			log.info("filterType="+filterType);
			
			String lkuTypeName="";
			
			if("purposeLkuValue".equals(filterType) ||
				"functionLkuValue".equals(filterType) 
				) {
				lkuTypeName="Cabinet";
			}
			
			List outputList=new ArrayList();
			List itemList=paginatedService.getLookupData(filterType,lkuTypeName,"itemList");
			
			if(itemList!=null) {
				outputList.addAll(itemList);
			}
			
			outputMap.put("lookup",outputList);

		} catch( Exception e ){
			log.error("",e);
		}	
		return outputMap;
	}
		
	@RequestMapping(value="/report_data", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getReportData (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=true) String locationName,
			@RequestParam(value="reportName",required=false) String reportName,
			@RequestParam(value="filterType",required=false) String filterType,
			@RequestParam(value="filterValue",required=false) String filterValue
		) throws Throwable {
		
		Map outputMap=new HashMap();
		
		try {
			
			log.info("getReportData...");
			log.info("locationName="+locationName);
			log.info("reportName="+reportName);
			log.info("filterType="+filterType);
			log.info("filterValue="+filterValue);
			
			//CR58489
			locationName=java.net.URLDecoder.decode(locationName, "UTF-8"); 
			
			ReportDataDTO reportDataDTO=reportService.getReportData(reportName,locationName,filterType,filterValue);
			
			List<Map> resultList=reportDataDTO.getData();
			List<Map> columnName=reportDataDTO.getColumns();
			List<Map> legendList=reportDataDTO.getLegend();
			Map legendSetting=reportDataDTO.getLegendSetting();
			
			String timestamp = String.format(new Locale("en", "US"),
											"%1$tm/%1$td/%1$tY at %1$tI:%1$tM:%1$tS %1$Tp", new Date());
			
			outputMap.put("columnName",columnName);
			outputMap.put("data",resultList);
			outputMap.put("legend",legendList);
			outputMap.put("legendSetting",legendSetting);
			outputMap.put("timestamp",timestamp);

		} catch( Exception e ){
			log.error("",e);
		}	
		return outputMap;
	}
	
	/**
	 *  Get report threshold settings
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/thresholds?locationName=SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/thresholds?locationName=SITE%20A
	 */
	@RequestMapping(value="/thresholds", method=RequestMethod.GET)
	public @ResponseBody Map getThresholdSettings (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="locationName",required=true) String locationName
		) throws Throwable {
			
		List<Map> list=reportService.getThresholdSettings(locationName);

		Map returnMap=new HashMap();
		returnMap.put("legendSettings",list);
		
		return returnMap;
	}
	
	/**
	 *  Get report threshold settings
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/threshold?locationName=SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/threshold?locationName=SITE%20A
	 */
	@RequestMapping(value="/thresholds", method=RequestMethod.POST)
	public @ResponseBody Map updateThresholdSettings (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="reportName",required=true) String reportType,
			@RequestParam(value="thresholds",required=true) String thresholds
		) throws Throwable {
		
		int resultCode=0;
		String resultMessage="";
		Map returnMap=new HashMap();
		
		try {
		
			boolean isSuccess=reportService.updateThresholdSetting(reportType,thresholds);
			if(!isSuccess) {
				resultCode=-1;
			}
			
		} catch(Exception e) {
			log.error("",e);
			resultCode=-1;
			resultMessage=e.toString();
		}
	
		returnMap.put("resultCode",resultCode);
		returnMap.put("resultMessage",resultMessage);
		
		return returnMap;
	}
	
	/*
	private ListCriteriaDTO getCriteriaForItemList(String locationName) {
	
		String COMMA="%2C";
	
		int pageNumber=1;

		ListCriteriaDTO listCriteriaDTO=new ListCriteriaDTO();

		String[] columns={"itemId","Class","Name","Status","Location","Cabinet","CabinetId","ParentCadHandle"};

		List<ColumnDTO> columnDTO=new ArrayList<ColumnDTO>();
		List<ColumnCriteriaDTO> columnCriteriaDTO=new ArrayList<ColumnCriteriaDTO>();

		for(String columnName : columns) {
			ColumnDTO dto=new ColumnDTO();
			dto.setFieldName(columnName);
			dto.setFieldLabel(columnName);
			columnDTO.add(dto);
		}
		
		//Filter for item status
		ColumnCriteriaDTO criteriaDTO=new ColumnCriteriaDTO();
		criteriaDTO.setName("Status");
		
		FilterDTO filter = new FilterDTO();
		filter.setIsLookup(true);
		filter.setLookupCodes(
			"Installed"+COMMA+
			"Planned"+COMMA+
			"Powered-off"+COMMA+
			"Off-Site"+COMMA+
			"To Be Removed"
		);
		criteriaDTO.setFilter(filter);
		
		columnCriteriaDTO.add(criteriaDTO);
		
		//Filter for item classes
		criteriaDTO=new ColumnCriteriaDTO();
		criteriaDTO.setName("Class");
		
		filter = new FilterDTO();
		filter.setIsLookup(true);
		filter.setLookupCodes(
			"Cabinet"+COMMA+
			"CRAC"+COMMA+
			"Data Panel"+COMMA+
			"Device"+COMMA+
			"Floor PDU"+COMMA+
			"Network"+COMMA+
			"Power Outlet"+COMMA+
			"Probe"+COMMA+
			"Rack PDU"+COMMA+
			"UPS"
		);
		criteriaDTO.setFilter(filter);
		
		columnCriteriaDTO.add(criteriaDTO);
		
		//Filter for location
		if(locationName!=null && !"".equals(locationName)) {
			criteriaDTO=new ColumnCriteriaDTO();
			criteriaDTO.setName("Location");
		
			filter = new FilterDTO();
			filter.setEqual(locationName);
			criteriaDTO.setFilter(filter);
		
			columnCriteriaDTO.add(criteriaDTO);
		}

		listCriteriaDTO.setPageNumber(pageNumber);
		//listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
		listCriteriaDTO.setFitType(ListCriteriaDTO.ALL);
		listCriteriaDTO.setColumns(columnDTO);
		listCriteriaDTO.setColumnCriteria(columnCriteriaDTO);

		return listCriteriaDTO;
	}
	*/
	
	/**
	 *  Get cadhandle data
	 *  
	 *  API call (example):
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/get_cadhandles?siteCode=SITE%20A
	 *  or eclipse env:
	 *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" http://admin:raritan@localhost:8080/dcTrackApp/api/floormaps/get_cadhandles?siteCode=SITE%20A
	 */
	@RequestMapping(value="/get_cadhandles", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getCadHandles (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="siteCode",required=true) String locationId
		) throws Throwable {
		
		Map outputMap=new HashMap();
		outputMap.put("siteCode", locationId);

		try {
			CadHandleDTO cadHandleDTO = cadService.getCadHandles(locationId);
			outputMap.put("data", cadHandleDTO.getData());
		} catch( Exception e ){
			log.error("",e);
		}
		
		return outputMap;
	}	

    /**
     *   UPDATE cadhandles
     *
     *  API call (example):
     *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/set_cadhandles
     *  or in eclipse env:
     *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/floormaps/set_cadhandles
     *
     */
    @RequestMapping(value="/set_cadhandles", method=RequestMethod.POST)
    public @ResponseBody Map<String,Object> setCadHandles(
    		HttpServletRequest request, 
    		@RequestBody Map<String, Object> dataDetails
    	) throws Throwable {

		Map outputMap=new HashMap();

		try {
			String locationId = (String)dataDetails.get("siteCode");
			outputMap.put("siteCode", locationId);
			List<Map> cadhandles = (List<Map>)dataDetails.get("data");
			
			CadHandleDTO cadHandleDTO = new CadHandleDTO();
			cadHandleDTO.setLocationId(locationId);
			cadHandleDTO.setData(cadhandles);
			
			Integer cnt = cadService.setCadHandles(cadHandleDTO);
			outputMap.put("ret", "1");
			outputMap.put("update", cnt);
		} catch( Exception e ){
			log.error("",e);
			outputMap.put("ret", "0");
		}
		
    	return outputMap;
    }

    
    /**
     *   SYNC cadhandles
     *
     *  API call (example):
     *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file https://admin:raritan@192.168.51.220/dcTrackApp/api/floormaps/sync_cadhandles
     *  or in eclipse env:
     *  curl -i -k -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d@json_file http://admin:raritan@127.0.0.1:8080/dcTrackApp/api/floormaps/sync_cadhandles
     *
     */
    @RequestMapping(value="/sync_cadhandles", method=RequestMethod.POST)
    public @ResponseBody Map<String,Object> syncCadHandles(
    		HttpServletRequest request, 
    		@RequestBody Map<String, Object> dataDetails
    	) throws Throwable {

		try {
			String locationId = (String)dataDetails.get("siteCode");
			List<Map> cadhandles = (List<Map>)dataDetails.get("data");
			
			String filePath = (String)dataDetails.get("filePath");
			
			log.info("sync_cadhandles...");
			log.info("filePath="+filePath);
			log.info("locationid="+locationId);

			CadHandleDTO cadHandleDTO = new CadHandleDTO();
			cadHandleDTO.setLocationId(locationId);
			cadHandleDTO.setData(cadhandles);
			
			CadHandleDTO outDTO = cadService.syncCadHandles(cadHandleDTO);
			
			Map<String,Object> outputMap = new HashMap<String,Object>();
			outputMap.put("siteCode", outDTO.getLocationId());
			outputMap.put("data", outDTO.getData());
			
			//US2183 / TA6391
			//Update dct_locations with filePath
			if(filePath!=null) {
				boolean hasUpdated = cadService.updateLocation(locationId,filePath);
				log.info("Update location finished hasUpdated:"+hasUpdated);
			}
			
			return outputMap;
		} catch( Exception e ){
			log.error("",e);
		}
		
		// If go here, should be error
		Map outputMap=new HashMap();
		outputMap.put("ret", "0");
    	return outputMap;
    }
    
	@RequestMapping(value="/get_parameters", method=RequestMethod.GET)
	public @ResponseBody Map<String,List<Map>> getParameters (
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="siteCode",required=true) String locationId
		) throws Throwable {
		
		Map outputMap=new HashMap();
		outputMap.put("siteCode", locationId);

		try {
			outputMap.put("data", cadService.getParameters(locationId));
		} catch( Exception e ){
			log.error("",e);
		}
		
		return outputMap;
	}
	
	private Map<String,String> getSortAndFilterParams(String sort,String filter) {
		Map map=new HashMap();
		
		map.put("filterClass","");
		map.put("filterName","");
		map.put("sortColumn","");
		map.put("sortDirection","");
	
		if(sort!=null && !"".equals(sort)) {
			try {
	
				String sortString=URLDecoder.decode(sort);
	
				ObjectMapper mapper = new ObjectMapper(); 
				JsonNode jsonNode = mapper.readTree(sortString);
	
				Iterator<JsonNode> jsonNodeIterator=jsonNode.getElements();
			
				for(int i=0; jsonNodeIterator.hasNext(); i++) {
					JsonNode jn = jsonNodeIterator.next();
		
					String sortColumn=jn.get("property").getTextValue();
					String sortDirection=jn.get("direction").getTextValue();
				
					if("ShowName".equals(sortColumn)) {
						sortColumn="Name";
					}
				
					map.put("sortColumn",sortColumn);
					map.put("sortDirection",sortDirection);
				}
			
			} catch(java.io.IOException ioe) {
			
			}
		}
		
		log.info("filter="+filter);
	
		if(filter!=null) {
		
			try {
				String filterString=URLDecoder.decode(filter);
	
				ObjectMapper mapper = new ObjectMapper(); 
				JsonNode jsonNode = mapper.readTree(filterString);
	
				Iterator<JsonNode> jsonNodeIterator=jsonNode.getElements();
			
				for(int i=0; jsonNodeIterator.hasNext(); i++) {
					JsonNode jn = jsonNodeIterator.next();
		
					String property=jn.get("property").getTextValue();
					String value=jn.get("value").getTextValue();
				
					if("Class".equals(property)) {
						map.put("filterClass",value);
					}
					
					if("ShowName".equals(property)) {
						map.put("filterName",value);
					}
				}
			} catch(java.io.IOException ioe) {
			
			}
			
		}
		
		return map;
	}
	
	@Transactional
	@RequestMapping(value="/sync_floormaps", method=RequestMethod.GET)
	@ResponseBody
	public Map syncFloorMaps(
		HttpServletRequest request, 
		HttpServletResponse response,
		@RequestParam(value="location_id",required=true) String locationId
	)  {
		Map resultMap=new HashMap();

		String RESULT_CODE="result_code";
		int RESULT_OK=0;
		int RESULT_FAIL=1;
		int resultCode=RESULT_OK;
		
		log.info("syncFloorMaps...");		
				
		try {
			
			Map infoMap=cadService.getPIQLocationInfo(locationId);
			
			log.info("infoMap="+infoMap);
		
			String piqHost = (String)infoMap.get("piqHost");//ex: https://192.168.78.119
			String piqId = (String)infoMap.get("piqId");  //node key mapped to dcTrack locationId (ex:data_center-1)
			String piqUsername = (String)infoMap.get("piqUsername");
			String piqPassword = (String)infoMap.get("piqPassword");
			String dwgFileName = (String)infoMap.get("dwgFileName"); //ex: /floormaps/userdwgs/xxx.dwg
											
			//Multiple process
			String filePath="/var/oculan/"+dwgFileName;
		
			log.info("piqId="+piqId);
			log.info("piqHost="+piqHost);
			log.info("filePath="+filePath);
			log.info("piqUsername="+piqUsername);
			log.info("piqPassword="+piqPassword);

			if(piqId != null && piqHost!=null ) {
				resultMap=piqSyncFloorMap.uploadFloorMap( piqHost, filePath, piqId, piqUsername, piqPassword);
				resultMap.put(RESULT_CODE, RESULT_OK);
			} else {
				resultMap.put(RESULT_CODE, RESULT_FAIL);
			}
			
			log.info("resultMap="+resultMap);
			
		} catch (DataAccessException | RemoteDataAccessException | Exception e) {
			log.error("",e);
			resultMap.put(RESULT_CODE, RESULT_FAIL);
		}
		
		log.info("API syncFloorMaps end");

		return resultMap;
	}
    
	@Transactional
	@RequestMapping(value="/sync_pdu_readings", method=RequestMethod.PUT, headers="Accept=application/json")
	public @ResponseBody Map<String,Object> syncAllPDUReadings(HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody SyncAllPDUReadingsRequestDTO readingsDTO) throws Throwable{
		
		List<PIQSyncPorts.TYPE> portTypes = new ArrayList<>();
		for (String type:readingsDTO.getTypes()){
			portTypes.add(PIQSyncPorts.TYPE.fromString(type));
		}
		
		Map<String,Object> ret = new HashMap<String, Object>();
		
		try {
			ret.put("status",itemHome.syncAllPDUReadings(readingsDTO.getLocationId(), portTypes));
		} catch (BusinessValidationException be){
			throw (new DCTRestAPIException(HttpStatus.BAD_REQUEST, be));
		} catch (Exception e) {
			throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
	}
	
	@Transactional
	@RequestMapping(value="/sync_pdu_readings_status", method=RequestMethod.GET, headers="Accept=application/json")
	public @ResponseBody Map<String,Object> getSyncAllPDUReadingsStatus(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="location_id",required=true) Long locationId) throws Throwable {
		Map<String,Object> ret = new HashMap<String, Object>();
		
		try {
			ret.put("status",itemHome.getSyncAllPDUReadingsStatus(locationId));
		} catch (BusinessValidationException be){
			throw (new DCTRestAPIException(HttpStatus.BAD_REQUEST, be));
		} catch (Exception e) {
			throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
		
	}
	
	@Transactional
	@RequestMapping(value="/sync_pdu_readings_piq_settings", method=RequestMethod.GET, headers="Accept=application/json")
	public @ResponseBody Map<String,Object> getSyncAllPDUReadingsStatus(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value="location_id",required=true) String locationId) throws Throwable {
		Map<String,Object> ret = new HashMap<String, Object>();
		
		try {
		
			Map infoMap=cadService.getPIQLocationInfo(locationId);
			
			log.info("infoMap="+infoMap);
		
			String piqHost = (String)infoMap.get("piqHost");//ex: https://192.168.78.119
			String piqId = (String)infoMap.get("piqId");  //node key mapped to dcTrack locationId (ex:data_center-1)
			
			if(piqId==null) {
				piqId="";
			}

			Map valueMap=new HashMap();
			valueMap.put("piqId",piqId);
			valueMap.put("host",piqHost);
		
			ret.put("settings",valueMap);
		} catch (Exception e) {
			throw (new DCTRestAPIException(HttpStatus.INTERNAL_SERVER_ERROR, e));
		}
		
		return ret;
		
	}
	
}
