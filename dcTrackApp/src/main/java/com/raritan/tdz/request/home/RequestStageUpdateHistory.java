package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.request.dao.RequestHistoryDAO;

/**
 * 
 * @author bunty
 *
 */

public class RequestStageUpdateHistory implements RequestStageHelper {

	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws DataAccessException {

		// Update the history table
		requestHistoryDAO.setRequestHistoryNotCurrent(request);
		
		requestHistoryDAO.createReqHist(request, requestStage, userInfo);

	}

}
