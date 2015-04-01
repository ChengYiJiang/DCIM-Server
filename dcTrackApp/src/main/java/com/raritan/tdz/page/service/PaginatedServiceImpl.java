package com.raritan.tdz.page.service;

import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.log4j.Logger;

import com.raritan.tdz.exception.*;
import com.raritan.tdz.page.home.*;
import com.raritan.tdz.page.dto.*;

public class PaginatedServiceImpl implements PaginatedService {

	private Logger log = Logger.getLogger(this.getClass());

	private Map homeMap;

	//Default constructor
	public PaginatedServiceImpl() {
	
	}

	public void setHomes(Map homeMap) {
		this.homeMap=homeMap;
	}

	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
		ListResultDTO listResultDTO=null;
		
		log.info("service getPageList pageType="+pageType);
		
		long time1=System.currentTimeMillis();
				
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);

		try {
		
			listResultDTO=home.getPageList(listCriteriaDTO,pageType);
			
		} catch (DataAccessException e) {
			//Prevent error influence for client display
			log.error("error from getPageList, re-fetching with null ListCriteriaDTO",e);
			home.deleteUserConfig(listCriteriaDTO,pageType); //Empty current data first
			listResultDTO=home.getPageList(null,pageType);
		}
	
		long time2=System.currentTimeMillis();
		
		log.info("-------getPageList Response time: "+(time2-time1)+"ms");
	
		return listResultDTO;
	}
	
	public List<LookupOptionDTO> getLookupOption(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
		
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);
	
		return home.getLookupOption(listCriteriaDTO,pageType);
	}
	
	public List<Map> getLookupData(String fileName,String lkuTypeName,String pageType) throws DataAccessException {

		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);
	
		return home.getLookupData(fileName,lkuTypeName,pageType);
	}
	
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException{
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.getUserConfig(pageType);
	}
	
	
	/*
	public ListCriteriaDTO getDefUserConfig(String pageType) throws DataAccessException{
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.getDefUserConfig(pageType);
	}
	*/
	
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		log.info("service saveUserConfig pageType="+pageType+"listCriteriaDTO="+itemListCriteria);
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.saveUserConfig(itemListCriteria, pageType);
	}
	
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		log.info("service deleteUserConfig pageType="+pageType+"listCriteriaDTO="+itemListCriteria);
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.deleteUserConfig(itemListCriteria, pageType);
	}
	
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		log.info("service deleteUserConfig pageType="+pageType+"listCriteriaDTO="+itemListCriteria);
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.resetUserConfig(itemListCriteria, pageType, fitRows);
	}
	
	public Map getColumnGroup(String pageType) throws DataAccessException {
		log.info("service getColumnGroup pageType="+pageType);
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.getColumnGroup(pageType);
	}
	
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType) {
		log.info("service getValueList pageType="+pageType);
		PaginatedHome home=(PaginatedHome)homeMap.get(pageType);			
		return home.getValueList(listCriteriaDTO,pageType);
	}
	
}
