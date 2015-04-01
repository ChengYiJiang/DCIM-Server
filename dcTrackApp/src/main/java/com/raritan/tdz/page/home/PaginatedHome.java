package com.raritan.tdz.page.home;

import java.util.*;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.*;
import com.raritan.tdz.domain.*;
import com.raritan.tdz.page.dto.*;

//temp by tonyshen
import org.springframework.transaction.annotation.Propagation;
/**
 * 
 * @author Randy Chen
 */
public interface PaginatedHome {
	
	//temp by tonyshen
	//@Transactional(propagation=Propagation.REQUIRES_NEW)
	//public void auditUpdateTest();

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListResultDTO getPageList(ListCriteriaDTO iistCriteriaDTO,String pageType) throws DataAccessException;	

	@Transactional(readOnly = true)
	public List<LookupOptionDTO> getLookupOption(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException;

	@Transactional(readOnly = true)
	public List<Map> getLookupData(String fieldName,String lkuTypeName,String pageType) throws DataAccessException;
	
	@Transactional(readOnly = true)
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException;
		
	/*
	@Transactional(readOnly = true)
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException;
	*/
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException;
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException;
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException;
	
	@Transactional(readOnly = true)
	public Map getColumnGroup(String pageType) throws DataAccessException;

	@Transactional(readOnly = true)
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType);
	
	@Transactional(readOnly = true)
	public String getItemActionMenuStatus( List<Long> itemIdList );
	
}
