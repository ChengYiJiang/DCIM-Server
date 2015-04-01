package com.raritan.tdz.page.home;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.page.dto.ColumnCriteriaDTO;
import com.raritan.tdz.page.dto.ColumnDTO;
import com.raritan.tdz.page.dto.FilterDTO;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.dto.LookupOptionDTO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.ChassisTracerHandler;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.CircuitRequestInfo;

//temp by tonyshen
import org.springframework.transaction.annotation.Propagation;
/**
 * 
 * @author Randy Chen
 */
public class PaginatedHomeCircuitListImpl extends PaginatedHomeBase implements PaginatedHome {

	private CircuitPDHome circuitPDHome;
	
	public PaginatedHomeCircuitListImpl(SessionFactory sessionFactory){
		super(sessionFactory);
		
	}	
	
	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
	
		ListResultDTO listResultDTO=new ListResultDTO();
		Map aliasMap=new HashMap();
		
		int totalRows=0;
		
		configClientUtcTime(listCriteriaDTO);
		
		listCriteriaDTO=super.getListCriteriaDTO(listCriteriaDTO,pageType);
		
		int maxLinesPerPage=listCriteriaDTO.getMaxLinesPerPage() > 0 ? listCriteriaDTO.getMaxLinesPerPage() : ListCriteriaDTO.ROWS_PER_PAGE_50;
		int pageNumber=listCriteriaDTO.getPageNumber() > 0 ? listCriteriaDTO.getPageNumber() : 1;
		List<ColumnCriteriaDTO> columnCriteriaDTOList=listCriteriaDTO.getColumnCriteria();
		List<ColumnDTO> columnDTOList=listCriteriaDTO.getColumns();
		
		ColumnCriteriaDTO[] columnCriteriaDTO=(ColumnCriteriaDTO[])columnCriteriaDTOList.toArray(new ColumnCriteriaDTO[0]);
		ColumnDTO[] columnDTO=(ColumnDTO[])columnDTOList.toArray(new ColumnDTO[0]);
		
		listCriteriaDTO.setMaxLinesPerPage(maxLinesPerPage);
		listCriteriaDTO.setPageNumber(pageNumber);
		
		List list=new ArrayList();
		
		try {
		
			log.info("checking columnCriteriaDTO..");
			for(ColumnCriteriaDTO dto : columnCriteriaDTO) {
				log.info("input ColumnCriteriaDTO.name="+dto.getName());
				log.info("input ColumnCriteriaDTO.toSort="+dto.isToSort());
			}
		
			Session session = this.sessionFactory.getCurrentSession();
			
			String domainType="com.raritan.tdz.domain.CircuitViewData";
			
			//First create a criteria  given an entity name such as com.raritan.tdz.domain.Item
			Criteria criteria = session.createCriteria(domainType);

			ProjectionList proList = Projections.projectionList();			
						
			//Now for each of the columns that you want to display, create the alias and projections for each one of them
			//Alias
			for(ColumnDTO column : columnDTO) {
			
				String fieldName=column.getFieldName();
				
				//For unusual columns
				//if(fieldName.substring(0,1).equals("$")) {
				//	fieldName=getCustomFieldName(fieldName);
				//}
				
				//Set circuitId for temporary fields
				//That will be replaced later
				fieldName=("requestStageLksCode".equalsIgnoreCase(fieldName)) ? "circuitId" :fieldName;
				fieldName=("proposeCircuitId".equalsIgnoreCase(fieldName)) ? "circuitId" :fieldName;
								
				for (String alias: getAliases(domainType,fieldName)){
								
					if (!criteria.toString().contains(alias.replace(".","_"))){
						criteria.createAlias(alias, alias.replace(".","_"), Criteria.LEFT_JOIN);
					}
				}
				
				//Projections
				String traceStr = getFieldTrace(domainType,fieldName);
								
				String alias = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : traceStr;
				String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0, traceStr.lastIndexOf(".")).replace(".","_") : "this.";
				
				proList.add(Projections.alias(Projections.property(aliasForProjection.toString() + alias), fieldName));
				
				aliasMap.put(fieldName,aliasForProjection.toString() + alias);
				
			}
			
			log.debug("aliasMap="+aliasMap);
			
			//Loop for filter
			processFilter(domainType,criteria,columnCriteriaDTO,columnDTO,aliasMap,getClientUtcTimeString());
						
			//getTotalRows
			criteria.setProjection(Projections.rowCount());
		
			//CR53063 skipped when first query 
			if(listCriteriaDTO.isFirstQuery()==false){
				totalRows= ((Long)criteria.uniqueResult()).intValue();
			}
			
			log.info("totalRows="+totalRows);
			
			//Pre-process sorting for specific column
			columnCriteriaDTO=preProcessSorting(columnCriteriaDTO);
			
			//Loop for sorting
			columnCriteriaDTO=processSorting(criteria,columnCriteriaDTO,columnDTO,aliasMap, null);
			
			criteria.setProjection(proList);
			
			//maxLinesPerPage=-1 //-> fetch all data
			if(listCriteriaDTO.getFitType() == ListCriteriaDTO.ALL) {
				maxLinesPerPage=totalRows;
			}
			
			//Compute first result
			int firstResult=(pageNumber-1)*maxLinesPerPage;

			criteria.setFirstResult(firstResult);
			//criteria.setFetchSize(maxLinesPerPage); //CR48287 fixed here - don't use this method
			criteria.setMaxResults(maxLinesPerPage);

			//CR53063 skipped when first query 
			if(listCriteriaDTO.isFirstQuery()==false){
				list = criteria.list();
			}
			
			postProcessResults(list,columnDTO);
				
			listCriteriaDTO.setColumnCriteria(Arrays.asList(columnCriteriaDTO));
			listCriteriaDTO.setColumns(Arrays.asList(columnDTO));
			
			listCriteriaDTO=updateDTOAttributeForOutput(listCriteriaDTO);
			
			listResultDTO.setListCriteriaDTO(listCriteriaDTO);
			listResultDTO.setValues(list);
			listResultDTO.setTotalRows(totalRows);

			log.info("pageNumber="+pageNumber);
			log.info("maxLinesPerPage="+maxLinesPerPage);
			log.info("columnCriteriaDTO.length="+columnCriteriaDTO.length);
			log.info("columnDTO.length="+columnDTO.length);
			log.info("listResultDTO="+listResultDTO);
			log.info("totalRows="+totalRows);
			log.info("list="+list.size());
			
		}catch(ClassNotFoundException e){
		
			log.error("ClassNotFoundException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(QueryException e){
		
			log.error("QueryException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(HibernateException e){
		
			log.error("HibernateException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
		
			log.error("DataAccessException:",e);
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			
		}

		return listResultDTO;
	}
	
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType) {
	
		ListResultDTO listResultDTO=null;
	
		try {
			listResultDTO=getPageList(listCriteriaDTO,pageType);
		} catch (DataAccessException dae) {
			log.error("",dae);
		} catch (Exception e) {
			log.error("Error from getPageList:",e);
		}
		
		return super.getValueList(listResultDTO,pageType);
	}
	
	protected void postProcessResults(List list,ColumnDTO[] columnDTO) {
	
		int indexCircuitId=getColumnIndex(columnDTO,"circuitId");
		int indexCircuitType=getColumnIndex(columnDTO,"circuitType");
//		int indexCircuitTypeDesc=getColumnIndex(columnDTO,"circuitTypeDesc");
//		int indexRequestNumber=getColumnIndex(columnDTO,"requestNumber");
		int indexRequestStage=getColumnIndex(columnDTO,"requestStage");
		int indexRequestStageLksCode=getColumnIndex(columnDTO,"requestStageLksCode");
		int indexProposeCircuitId=getColumnIndex(columnDTO,"proposeCircuitId");
		
		Map<Long, CircuitRequestInfo> reqInfoPower=circuitPDHome.getCircuitRequestInfo(SystemLookup.PortClass.POWER, false);
		Map<Long, CircuitRequestInfo> reqInfoData=circuitPDHome.getCircuitRequestInfo(SystemLookup.PortClass.DATA, false);
		
		for(int i=0;i<list.size();i++) {
			Object[] values=(Object[])list.get(i);
						
			if(indexCircuitType!=-1 && indexCircuitId!=-1) {
			
				Long circuitId=(Long)values[indexCircuitId];
				
				Long circuitType=(Long)values[indexCircuitType];
				if(circuitType == SystemLookup.PortClass.POWER) {
					
					CircuitRequestInfo info=(CircuitRequestInfo)reqInfoPower.get(circuitId);
					
					if(indexRequestStageLksCode!=-1) {
						if(values[indexRequestStage]==null || "".equals(values[indexRequestStage])) {
							values[indexRequestStageLksCode]="";
						} else {
							values[indexRequestStageLksCode]=(info != null && info.getRequestStageCode() != null) ? info.getRequestStageCode() : "";
						}
					}
					if(indexProposeCircuitId!=-1) {
						//Fix CR48434 - No Proposed (.1) suffix
						values[indexProposeCircuitId]=(info != null && info.getProposeCircuitId() !=null) ? info.getProposeCircuitId()+".1" : "" ;
						//boolean isProposed=!"".equals(values[indexProposeCircuitId]) ? true : false;
						boolean isProposed=false;
						values[indexCircuitId]=(new CircuitUID(circuitId, SystemLookup.PortClass.POWER,  isProposed)).floatValue();
					}
					
				} else if(circuitType == SystemLookup.PortClass.DATA) {
					
					CircuitRequestInfo info=(CircuitRequestInfo)reqInfoData.get(circuitId);
					
					if(indexRequestStageLksCode!=-1) {
						if(values[indexRequestStage]==null || "".equals(values[indexRequestStage])) {
							values[indexRequestStageLksCode]="";
						} else {
							values[indexRequestStageLksCode]=(info != null && info.getRequestStageCode() != null) ? info.getRequestStageCode() : "";
						}
					}
					if(indexProposeCircuitId!=-1) {
						//Fix CR48434 - No Proposed (.1) suffix
						values[indexProposeCircuitId]=(info != null && info.getProposeCircuitId() !=null) ? info.getProposeCircuitId()+".1" : "" ;
						//boolean isProposed=!"".equals(values[indexProposeCircuitId]) ? true : false;
						boolean isProposed=false;
						values[indexCircuitId]=(new CircuitUID(circuitId, SystemLookup.PortClass.DATA,  isProposed)).floatValue();
					}					
					
				}
				
				
			}
			
		}
		
	}
	
	private int getColumnIndex(ColumnDTO[] columnDTO,String checkFieldName) {
	
		for(int i=0;i<columnDTO.length;i++) {					
						
			ColumnDTO column=columnDTO[i];
			
			if(column==null) {
				continue;
			}
			
			String fieldName=column.getFieldName();
			if(fieldName.equalsIgnoreCase(checkFieldName)) {
				return i;
			}
			
		}
	
		return -1;
	}

	public void setCircuitPDHome(CircuitPDHome circuitPDHome) {
		this.circuitPDHome=circuitPDHome;
	}
	
	/*
	public ColumnCriteriaDTO[] preProcessSorting(ColumnCriteriaDTO[] columnCriteriaDTO) {
	
		log.info("preProcessSorting..columnCriteriaDTO.length="+columnCriteriaDTO.length);
	
		//Default sort for item list
		if ( columnCriteriaDTO==null || columnCriteriaDTO.length==0 ) {
			//US949 and CR48142 default sort
			log.info("No column criteria->apply default sort");
			columnCriteriaDTO=new ColumnCriteriaDTO[1];
			ColumnCriteriaDTO dto = new ColumnCriteriaDTO();
			
			//dto.setName("creationDate");
			dto.setName("Creation Date");
			dto.setToSort(true);
			dto.setSortDescending(true);
			
			columnCriteriaDTO[0]=dto;
		}
	
		return columnCriteriaDTO;
	}
	*/

	public ColumnCriteriaDTO[] preProcessSorting(ColumnCriteriaDTO[] columnCriteriaDTO) {
	
		if(isNoSorting(columnCriteriaDTO)) {
		
			//US949 and CR48142 default sort
			ColumnCriteriaDTO dto = new ColumnCriteriaDTO();
			dto.setName("Creation Date");
			dto.setToSort(true);
			dto.setSortDescending(true);
		
			columnCriteriaDTO=getDefaultSortColumnCriteriaDTO(columnCriteriaDTO,dto);
		}
		
		return columnCriteriaDTO;
	}

	//temp by tonyshen
	public void auditUpdateTest() {
	}
	
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException{
		return super.getUserConfig(pageType);
	}
	
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException{
		return super.getDefUserConfig(pageType);
	}
	
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return super.saveUserConfig(itemListCriteria, pageType);
	}
	
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return super.deleteUserConfig(itemListCriteria, pageType);
	}
	
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		return super.resetUserConfig(itemListCriteria, pageType, fitRows);
	}
	
	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		return "";
	}
}
