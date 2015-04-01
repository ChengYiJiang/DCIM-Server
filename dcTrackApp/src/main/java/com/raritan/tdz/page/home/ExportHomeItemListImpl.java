package com.raritan.tdz.page.home;

import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

import com.raritan.tdz.customfield.dao.CustomFieldsFinderDAO;
import com.raritan.tdz.dctexport.processor.ExportProcessor;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.page.dto.ListResultDTO;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.FilterDTO;

/**
 * 
 * CR54034 Add the Base64 encode/decode and change the response encode
 * 1.Client Flex: mx.utils.Base64Encoder.encodeUTFBytes the fieldLbel PaginatedNaviationBar.mxml
 * 2.Server: org.apache.commons.codec.binary.Base64.decodeBase64 ExportHomeItemListImpl
 * 3.Response: com.raritan.tdz.page.controllers.PaginatedController.exportToCSV  set character encoding to "UTF-8" 
 * by KC Jan-02 2014
 * @author Randy Chen
 */
public class ExportHomeItemListImpl implements ExportHome {

	private PaginatedHome paginatedHome;
	private PaginatedHome paginatedHomeGateway;
	private int uPositionIndex = -1;
	private int slotPositionIndex = -1;
	private int mountedRailLookupIndex = -1;
	
	@Autowired
	private ExportProcessor exportItemProcessorGateway;
	
	@Autowired
	private String itemColumnAttributes;
	
	@Autowired
	private CustomFieldsFinderDAO customFieldsFinderDAO;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;

	private Logger log = Logger.getLogger(this.getClass());
	
	
	private List<ColumnDTO> defaultColumnDTOs = new ArrayList<ColumnDTO>();
	
	

	public void init(){
		String[] attributeLines = itemColumnAttributes.split("\n");
		for (String attributeLine:attributeLines){
			if (attributeLine.isEmpty() || !attributeLine.contains(",")) continue;
			String[] attributes = attributeLine.split(",");
			
			ColumnDTO columnDTO = new ColumnDTO();
			columnDTO.setFieldName(attributes[1]);
			columnDTO.setUiComponentId(attributes[2]);
			columnDTO.setFieldLabel(attributes[3]);
			columnDTO.setDefaultColumn(Boolean.parseBoolean(attributes[4]));
			columnDTO.setSortable(Boolean.parseBoolean(attributes[6]));
			columnDTO.setFilterable(Boolean.parseBoolean(attributes[7]));
			columnDTO.setFilterType(Integer.parseInt(attributes[9]));
			columnDTO.setFormat(attributes[10]);
			columnDTO.setVisible(true);
			
			defaultColumnDTOs.add(columnDTO);
		}
		
	}
	
	public StringBuilder exportToCSV(String option, String jason, String pageType) {
		
		StringBuilder csv=new StringBuilder();
		
		try {						
			ListCriteriaDTO listCriteriaDTO = constructListCriteriaDTO(option, jason,false);
	
			List<ColumnDTO> columnDTOList=listCriteriaDTO.getColumns();			
			ColumnDTO[] columns = (ColumnDTO[])columnDTOList.toArray(new ColumnDTO[0]);
			
			ListResultDTO listResultDTO=paginatedHome.getPageList(listCriteriaDTO, pageType);
	
			
			for ( int i = 0; i<columns.length; i++ ) {
				if ( "U Position".equalsIgnoreCase( columns[i].getFieldName() ) ) {
					uPositionIndex = i;
				}
				if ( "Slot Position".equalsIgnoreCase( columns[i].getFieldName() ) ) {
					slotPositionIndex = i;
				}
				if ( "mounting".equalsIgnoreCase( columns[i].getFieldName() ) ) {
					mountedRailLookupIndex = i;
				}
			}
	
			csv.append( constructCSVContent( getHeader(columns), columns, listResultDTO ).toString() );
			log.info( "ExportHomeItemListImpl run ok......... " );
			
		} catch (DataAccessException e) {
			log.error("ExportHomeItemListImpl.exportToCSV()",e);
			csv.append(e.getMessage());
		} catch (Exception e) {
			log.error("ExportHomeItemListImpl.exportToCSV()",e);
			csv.append(e.getMessage());
		}
		
		return csv;
	}
	
	
	@Override
	@Transactional(readOnly=true)
	public String exportToCSVForImport(String option, String json,
			String pageType) throws Exception, DataAccessException {
		
		ListCriteriaDTO listCriteriaDTO = constructListCriteriaDTO("all", json,true);
		
		ListResultDTO listResultDTO = paginatedHomeGateway.getPageList(listCriteriaDTO, pageType);
		
		String url = exportItemProcessorGateway.process(listResultDTO);
		
		log.info( "ExportHomeItemListImpl run ok......... " );
			
		
		return url;
	}
	
	public void setPaginatedHome(PaginatedHome paginatedHome) {
		this.paginatedHome=paginatedHome;
	}
	
	
	
	public void setPaginatedHomeGateway(PaginatedHome paginatedHomeGateway) {
		this.paginatedHomeGateway = paginatedHomeGateway;
	}


	//private String getHeader(ColumnDTO[] columns,String[] fieldLabels) {
	private String getHeader(ColumnDTO[] columns) {
		
		String headerStr="";
		boolean placeComma = true;
		
		for(int i=0; i<columns.length; i++) {
			//log.info( ">> getHeader : i = " + i + " : " + columns[i].isVisible() );
			if ( columns[i].isVisible() ) {
				placeComma = true;
				//US1329: get default name from dct_fields
				if ( i!=uPositionIndex ) {
					if ( i==slotPositionIndex ) {
						headerStr+="\""+"Rail or Slot Position"+"\"";
					} else {
						headerStr+="\""+columns[i].getFieldLabel()+"\"";
					}
				} else {
					placeComma = false;
				}
			} 
			if((i+1)<columns.length && columns[i+1].isVisible() && !"".equals( headerStr.trim() ) && placeComma ) headerStr+=",";
		}
		//log.info(">> headerStr : " + headerStr );
		return headerStr;
	}
	
	private StringBuilder constructCSVContent(String labels, ColumnDTO[] columns, ListResultDTO listResultDTO) throws Exception {
		
		StringBuilder content=new StringBuilder();
		content.append(labels);
		
		Iterator<Object[]> valuesIterator=listResultDTO.getValues().iterator();
		
		SimpleDateFormat sf = new SimpleDateFormat();
		for (; valuesIterator.hasNext();) {
			Object[] values=(Object[])valuesIterator.next();
			content.append("\n");
			String lineStr="";
			boolean placeComma = true;
			for( int i=0; i<values.length; i++ ) {
				//CR54034 Export doesn't work - add the check to prevent the NullPointException 
				if ( ( i < columns.length ) && columns[i].isVisible() )  {
					
					if(i==0)	log.info("values length=" + values.length + " columns.length="+columns.length);
					
					placeComma = true;
					if ( columns[i].getFilterType()==ColumnDTO.DATE ) {
						String format = columns[i].getFormat();
						if ( values[i]!=null && format!=null && !format.trim().equals( "" ) ) {
							try {
								sf.applyPattern( format );
								values[i] = sf.format( (Date)values[i] );
							} catch ( Exception ex ) {
								log.info( "", ex );
							}
						} 
					} 

					if ( i!=uPositionIndex ) {
						if ( i==slotPositionIndex ) {
							Object uPositionValue = "Blade".equalsIgnoreCase( ""+values[mountedRailLookupIndex ] ) ? values[slotPositionIndex ] : values[uPositionIndex ];						
							lineStr += (uPositionValue==null?"\"\"":"\""+uPositionValue+"\"");
						} else {
							lineStr += (values[i]==null?"\"\"":"\""+values[i]+"\"");
						}
					//Equal uPosition, removed from the export	
					} else {
						placeComma = false;
					}
				}
															
				//CR54034 Export doesn't work - Add the check to prevent the NullPointerException and try catch to print the stack here.
				//if ((i+1)<values.length && columns[i+1].isVisible() && !"".equals( lineStr.trim() ) && placeComma ){
				try{
					if ((i+1)<values.length ){
						if( columns!=null && ((i+1) < columns.length) && columns[i+1].isVisible()){
							if(!"".equals( lineStr.trim() )){
								if(placeComma){
									lineStr += (",");
								}	
							}														
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}												
			}
			content.append(lineStr);
		}

		/*
		for( int i=0; i<columns.length; i++ ) {
			if ( columns[i].isVisible() )  {
				log.info( "[ constructCSVContent ] columns[" + i + "] fieldName  : " + columns[i].getFieldName() );
				log.info( "[ constructCSVContent ] columns[" + i + "] filterType : " + columns[i].getFilterType() );
				log.info( "[ constructCSVContent ] columns[" + i + "] format     : " + columns[i].getFormat() );
			}
		}
		*/
		
		return content;
	}
	
	
	private ListCriteriaDTO constructListCriteriaDTO(String option) throws Exception {
		ListCriteriaDTO listCriteriaDTO=new ListCriteriaDTO();
		
		listCriteriaDTO.setPageNumber(null!=option&&option.trim().equalsIgnoreCase("all")?-1:1);
		
		//We need to add the custom fields since default list does not have it
		defaultColumnDTOs.addAll(getCustomFields());
		
		listCriteriaDTO.setColumns(defaultColumnDTOs);
		
		if (option!=null && option.equalsIgnoreCase("all")) {
			listCriteriaDTO.setPageNumber(1);
			//Jan.3,2013 - fix  export all error
			//listCriteriaDTO.setMaxLinesPerPage(-1);
			listCriteriaDTO.setFitType(ListCriteriaDTO.ALL);
		} 
		
		String currentUtcTimeString = new Date().toString();
		
		listCriteriaDTO.setCurrentUtcTimeString(currentUtcTimeString);
		
		return listCriteriaDTO;
	}
	
	private List<ColumnDTO> getCustomFields(){
		List<ColumnDTO> customFieldsColumnDTOList = new ArrayList<ColumnDTO>();
		
		List<LksData> classLksList = systemLookupFinderDAO.findByLkpType(SystemLookup.LkpType.CLASS);
		
		for (LksData classLks:classLksList){
			List<LkuData> customFields = customFieldsFinderDAO.findClassCustomFieldsByClassLkp(classLks.getLkpValueCode());
			for (LkuData customField:customFields){
				ColumnDTO columnDTO = new ColumnDTO();
				columnDTO.setFieldName("custom_"+customField.getLkuId());
				columnDTO.setFieldLabel(customField.getLkuValue());
				columnDTO.setDefaultColumn(false);
				columnDTO.setSortable(true);
				columnDTO.setFilterable(true);
				columnDTO.setVisible(true);
				columnDTO.setFilterType(1);
				columnDTO.setWidth(-1);
				columnDTO.setFiltered(false);
				columnDTO.setHideColumn(false);
				
				customFieldsColumnDTOList.add(columnDTO);
			}
		}
		
		return customFieldsColumnDTOList;
	}
	
	/**
	 * Fixed CR51824 Add a parameter - currentUtcTimeString by KC
	 * @param option
	 * @param jason 
	 * @return
	 * @throws Exception
	 */
	
	private ListCriteriaDTO constructListCriteriaDTO(String option, String jason,boolean forceVisible) throws Exception {							
		
		if (jason == null || jason.isEmpty()) return constructListCriteriaDTO(option);
		
		ListCriteriaDTO listCriteriaDTO=new ListCriteriaDTO();
		
		listCriteriaDTO.setPageNumber(null!=option&&option.trim().equalsIgnoreCase("all")?-1:1);
		
		
		ObjectMapper mapper = new ObjectMapper(); 
		JsonNode jsonNode = mapper.readTree(jason);
		
		if (jsonNode.has("columns")) {
			JsonNode columnsJsonNode=jsonNode.get("columns");
			Iterator<JsonNode> jsonNodeIterator=columnsJsonNode.getElements();
			int size=columnsJsonNode.size();
			ColumnDTO[] columnDTOAry = new ColumnDTO[size];
			
			String fieldName = "";
			String fieldLabel = "";		
			byte[] byteArr;				
			JsonNode jn = null;
					
			for(int i=0; jsonNodeIterator.hasNext(); i++) {
				jn = jsonNodeIterator.next();
	
				fieldName=jn.get("fieldName").getTextValue();
											
				fieldLabel = jn.get("fieldLabel").getTextValue();												
								
				boolean filterable=jn.get("filterable").getBooleanValue();
				int filterType=jn.get("filterType").getIntValue();
				boolean sortable=jn.get("sortable").getBooleanValue();
				boolean visible=forceVisible ? true : jn.get("visible").getBooleanValue();
				String format = jn.get("format").getTextValue();
	
				//log.info( ">> fieldName :" + fieldName );
				//log.info( ">> i :" + i );
				//log.info( ">> visible : " + visible );
				
				fieldName = (fieldName==null) ? "" : fieldName;
				
				fieldLabel = (fieldLabel==null) ? "" : fieldLabel;												
													
				byteArr = Base64.decodeBase64(fieldLabel);
				fieldLabel = new String(byteArr);							
					
				columnDTOAry[i] = new ColumnDTO();
				columnDTOAry[i].setFieldName(fieldName);
				columnDTOAry[i].setFieldLabel(fieldLabel);
				columnDTOAry[i].setFilterable(filterable);
				columnDTOAry[i].setFilterType(filterType);
				columnDTOAry[i].setSortable(sortable);
				columnDTOAry[i].setVisible(visible);
				columnDTOAry[i].setFormat(format);
			}
	
			listCriteriaDTO.setColumns(Arrays.asList(columnDTOAry));
		} else {
			listCriteriaDTO.setColumns(defaultColumnDTOs);
		}
		
		if (jsonNode.has("columnCriteria")){
			JsonNode columnCriteriaJsonNode=jsonNode.get("columnCriteria");
			Iterator<JsonNode> jsonNodeIterator=columnCriteriaJsonNode.getElements();
			int size=columnCriteriaJsonNode.size();
			ColumnCriteriaDTO[] columnCriteriaDTOAry = new ColumnCriteriaDTO[size];
			
			
			JsonNode jn = null;
			for(int i=0; jsonNodeIterator.hasNext(); i++) {
				jn = jsonNodeIterator.next();
				
				String name=jn.get("name").getTextValue();
				boolean sortDescending=jn.get("sortDescending").getBooleanValue();
				boolean toSort=jn.get("toSort").getBooleanValue();
				
				JsonNode filter=jn.get("filter");
			    FilterDTO filterDTO = new FilterDTO();
			    try {
				    filterDTO.setGroupType(filter.get("groupType").getIntValue());
					filterDTO.setGreaterThan(filter.get("greaterThan").getTextValue());
					filterDTO.setLessThan(filter.get("lessThan").getTextValue());
					filterDTO.setEqual(filter.get("equal").getTextValue());
					filterDTO.setIsLookup(filter.get("isLookup").getBooleanValue());
	                // CR55400 The "%2C" in the lookupCodes will be converted to ",", but the PageHomeBase can 
	                // only process "%2C".
					filterDTO.setLookupCodes(filter.get("lookupCodes").getTextValue().replace(",","%2C"));				
			    } catch ( Exception ex ) {
			    	log.error(ex.getMessage());
			    }
				
				name=name==null?"":name;
				
				columnCriteriaDTOAry[i] = new ColumnCriteriaDTO();
				columnCriteriaDTOAry[i].setName(name);
				columnCriteriaDTOAry[i].setSortDescending(sortDescending);
				columnCriteriaDTOAry[i].setToSort(toSort);
				
				columnCriteriaDTOAry[i].setFilter(filterDTO);
			}		
			
			listCriteriaDTO.setColumnCriteria(Arrays.asList(columnCriteriaDTOAry));
		}
		
		if (option!=null && option.equalsIgnoreCase("all")) {
			listCriteriaDTO.setPageNumber(1);
			//Jan.3,2013 - fix  export all error
			//listCriteriaDTO.setMaxLinesPerPage(-1);
			listCriteriaDTO.setFitType(ListCriteriaDTO.ALL);
		} else {
			listCriteriaDTO.setPageNumber(jsonNode.get("pageNumber").getIntValue());
			listCriteriaDTO.setMaxLinesPerPage(jsonNode.get("maxLinesPerPage").getIntValue());
		}
		
		String currentUtcTimeString = jsonNode.get("currentUtcTimeString").getTextValue();
		
		listCriteriaDTO.setCurrentUtcTimeString(currentUtcTimeString);
		
	
		return listCriteriaDTO;
	}

	
	
}
