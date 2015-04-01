package com.raritan.tdz.location.home;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;

public interface LocationHome {
	
	/**
	 * Get location details 
	 * 
	 * @param locationId
	 * @param userInfo
	 * @return
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO>getLocationDetails( Long locationId, UserInfo userInfo) throws Throwable;
	
	/**
	 * Get details of all locations and then convert results into human readable format (i.e. call adapter to 
	 * change all Lks/Lku ids/codes into lkp_values
	 *  
	 * @param userInfo
	 * @return
	 * @throws Throwable
	 */
	public Map<String, Object>getLocationDetailsExt( Long locationId, UserInfo userInfo) throws Throwable;
	
	
	/**
	 * Get details of all locations 
	 * 
	 * @param userInfo
	 * @return
	 * @throws Throwable
	 */
	public List<Map<String, UiComponentDTO>>getAllLocationsDetails(UserInfo userInfo) throws Throwable;
	
	/**
	 * Get location details and then convert results into human readable format (i.e. call adapter to 
	 * change all Lks/Lku ids/codes into lkp_values
	 *  
	 * @param locationId
	 * @param userInfo
	 * @return
	 * @throws Throwable
	 */
	public Object getAllLocationsDetailsExt( UserInfo userInfo) throws Throwable;
	
	
	/**
	 * Save/Update location with provided details.
	 * This API is currently used by Flex client.
	 * 
	 * @param locationId  -	Must be > 0 or -1. 
	 * 						If -1 then create new location. 
	 * 						Otherwise, update existing location wose id is locationId
	 * @param dtoList	  -	Location details, list of name value pairs where name is uiId
	 * @param sessionUser - user session info	
	 * @return
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException - invalid data provided
	 * @throws Throwable
	 */
	public Map<String, UiComponentDTO> saveLocation(Long locationId, List<ValueIdDTO> dtoList, UserInfo sessionUser) 
			throws ClassNotFoundException, BusinessValidationException, Throwable;

	/**
	 * Extended API. Save location with provided details. Details are in human-readable form,
	 * i.e. lks/lku values are provided instead of ids
	 * This API is currently used by REST calls.   
	 * 
	 * @param locationId	
	 * @param locationDetails
	 * @param user
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	public Map<String, Object> saveLocationExtAPI(long locationId, Map<String, Object> locationDetails,
			UserInfo user) throws BusinessValidationException,
			ClassNotFoundException, Throwable;

	/**
	 * Extended API. Update location with provided details. Details are in human-readable form,
	 * i.e. lks/lku values are provided instead of ids
	 * This API is currently used by REST calls.   
	 * 
	 * @param locationId	
	 * @param locationDetails
	 * @param user
	 * @return
	 * @throws BusinessValidationException
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 */
	public Map<String, Object> updateLocationExtAPI(long locationId, Map<String, Object> locationDetails,
			UserInfo user) throws BusinessValidationException,
			ClassNotFoundException, Throwable;

    /**
     * Extended API. Delete location with provided ids.
     * This API is currently used by REST calls.
     * 
     * @param locationId
     * @param user
     * @return
     * @throws BusinessValidationException
     * @throws ClassNotFoundException
     * @throws Throwable
     */
    public void deleteLocationExtAPI(long locationId, UserInfo user)
            throws BusinessValidationException, ClassNotFoundException,
            Throwable;
    
    /**
     * unmap the location and all its item from PIQ
     * @param locationId
     * @param userInfo TODO
     */
    public void unmapLocationExtAPI(Long locationId, UserInfo userInfo) throws BusinessValidationException;
    
}