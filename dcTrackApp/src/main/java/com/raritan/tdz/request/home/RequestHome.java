package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.RequestDTO;

public interface RequestHome {

	/**
	 * Process request for input request DTOs
	 * @param userInfo 
	 * @param requestDTOs
	 * @param bvex 
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public void processRequestDTO(UserInfo userInfo, List<RequestDTO> requestDTOs, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException;
	
	/**
	 * Process request for input request ids 
	 * @param userInfo 
	 * @param requestIds
	 * @return Errors 
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 */
	public Errors processRequestUsingIds(UserInfo userInfo, List<Long> requestIds) throws BusinessValidationException, DataAccessException;

	/**
	 * process request by input requests
	 * @param userInfo 
	 * @param requests
	 * @param errors 
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public void processRequests(UserInfo userInfo, List<Request> requests, Errors errors) throws BusinessValidationException, DataAccessException;

	/**
	 * process list of requests
	 * @param userInfo 
	 * @param requests
	 * @param bvex
	 * @throws BusinessValidationException
	 * @throws DataAccessException
	 */
	public void processRequests(UserInfo userInfo, List<Request> requests, BusinessValidationException bvex) throws BusinessValidationException, DataAccessException;

}
