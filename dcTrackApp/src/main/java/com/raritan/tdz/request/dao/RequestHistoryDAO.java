package com.raritan.tdz.request.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.UserInfo;

/**
 * 
 * @author bunty
 *
 */
public interface RequestHistoryDAO extends Dao<RequestHistory> {

	/**
	 * create the new request history with current as true for the given request in a given request stage
	 * @param request
	 * @param requestStageValueCode
	 * @param userInfo TODO
	 * @return
	 */
	public RequestHistory createReqHist(Request request, long requestStageValueCode, UserInfo userInfo);

	/**
	 * set the current to false for the given request
	 * @param request
	 */
	public void setRequestHistoryNotCurrent(Request request);

	/**
	 * set the request history comment for the current stage
	 * @param request
	 * @param comment
	 */
	public void setCurrentRequestHistoryComment(Request request, String comment);

	/**
	 * get the current history comment for a given request
	 * @param request
	 * @return
	 */
	public String getCurrentRequestHistoryComment(Request request);

	/**
	 * get request stage lkp value
	 * @param request
	 * @return
	 */
	public String getRequestStageLkpValue(Request request);
	
}
