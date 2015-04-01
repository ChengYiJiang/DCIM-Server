package com.raritan.tdz.request.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Router;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * 
 * @author bunty
 *
 */

public class RequestStageRouter {

	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	private Map<Long, Long> requestByPassNextStage;
	
	
	
	public Map<Long, Long> getRequestByPassNextStage() {
		return requestByPassNextStage;
	}

	public void setRequestByPassNextStage(Map<Long, Long> requestByPassNextStage) {
		this.requestByPassNextStage = requestByPassNextStage;
	}

	@Router
	public String validatorRouter(RequestMessage requestMessage) throws DataAccessException {
		
		String initialChannel = getInitialChannel(requestMessage);
		if (initialChannel.equals("nullChannel")) {
			return initialChannel;
		}
		return getInitialChannel(requestMessage) + "validateChannel";
		
	}

	@Router
	public String executorRouter(RequestMessage requestMessage) throws DataAccessException {
		
		String initialChannel = getInitialChannel(requestMessage);
		if (initialChannel.equals("nullChannel")) {
			return initialChannel;
		}
		return getInitialChannel(requestMessage) + "executeChannel";
		
	}

	private String getInitialChannel(RequestMessage requestMessage) throws DataAccessException {

		Request request = requestMessage.getRequest();
		Boolean requestBypass = requestMessage.getRequestByPass();
		
		LksData latestRequest = getLatestRequestStage(request.getRequestId(), requestBypass);

		Long requestStageLkpCode = (null != latestRequest && null != latestRequest.getLkpValueCode()) ? latestRequest.getLkpValueCode() : SystemLookup.RequestStage.REQUEST_ISSUED;
		
		if (null != requestByPassNextStage && null != requestBypass && requestBypass) {
			requestStageLkpCode = requestByPassNextStage.get(requestStageLkpCode);
		}
		
		//TODO: find correct place to check the request bypass 
		// String channel = (null != requestStageLkpCode) ? SystemLookup.RequestTypeLkp.ITEM_MOVE + requestStageLkpCode.toString() : "nullChannel";
		String channel = (requestBypass == true && null != requestStageLkpCode) ? request.getRequestTypeLookup().getLkpValueCode() + "." + requestStageLkpCode.toString() + ".": "nullChannel";
		return channel;
		
	}
	
	private LksData getLatestRequestStage(Long requestId, Boolean requestBypass) throws DataAccessException {
		LksData requestStageLookup = null;
		RequestHistory requestHistory = itemRequestDAO.getCurrentHistoryUsingRequest(requestId);
		if (null != requestHistory) {
			requestStageLookup = requestHistory.getStageIdLookup();
		}

		return requestStageLookup;

	}
}
