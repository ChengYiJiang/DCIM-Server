package com.raritan.tdz.page.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.*;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.page.home.*;
import com.raritan.tdz.page.dto.*;

public interface PaginatedService {
	
	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException;
	
	public List<LookupOptionDTO> getLookupOption(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException;
	
	public List<Map> getLookupData(String fileName,String lkuTypeName,String pageType) throws DataAccessException;
	
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException;
	
	//public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException;
	
	public int saveUserConfig(ListCriteriaDTO listCriteriaDTO, String pageType) throws DataAccessException;
	
	public Map getColumnGroup(String pageType) throws DataAccessException;
	
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType);
	
}
