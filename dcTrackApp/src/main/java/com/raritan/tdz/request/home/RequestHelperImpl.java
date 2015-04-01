package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.dao.RequestHistoryDAO;
import com.raritan.tdz.user.dao.UserDAO;
import com.raritan.tdz.util.RequestDTO;

public class RequestHelperImpl implements RequestHelper {

	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired(required=true)
	private UserDAO userDAO;
	
	@Autowired(required=true)
	private RequestHistoryDAO requestHistoryDAO;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public List<Request> getRequestFromDTO(List<RequestDTO> requestDTOs) {
		
		List<Request> requests = new ArrayList<Request>();
		
		for (RequestDTO dto: (List<RequestDTO>) requestDTOs) {
			Request request = requestDAO.getRequest(dto.getRequestId());
			requests.add(request);
		}

		return requests;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public boolean getRequestBypassSetting(UserInfo userInfo) {
		
		Boolean requestBypassSetting = userDAO.getUserRequestByPassSetting(new Long(userInfo.getUserId()));
		
		if (null == requestBypassSetting || false == requestBypassSetting) return false;
		
		return true;
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public List<Request> getAssociatedRequests(Request request) {
		
		List<Request> otherRequests = requestDAO.getAssociatedPendingReqsForReq(request);
		
		if (request.getRequestTypeLookupCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
			
			List<Long> childrenItemIds = itemDAO.getChildItemIds(request.getItemId());
			
			List<Long> passiveChildren = itemDAO.getPassiveChildItemIds(request.getItemId());
			
			childrenItemIds.removeAll(passiveChildren);
			
			if (childrenItemIds.size() > 0) {
			
				// List<Request> childrenDisconnectRequests = requestDAO.getPendingRequestsForItem(childrenItemIds, Arrays.asList(SystemLookup.RequestTypeLkp.DISCONNECT));
				// List<Request> childrenDisconnectRequests = requestDAO.getPendingNonPowerRequestsForItem(childrenItemIds, Arrays.asList(SystemLookup.RequestTypeLkp.DISCONNECT));
				List<RequestInfo>  dataReqsInfo = dataCircuitDAOExt.getPendingCircuitRequestForItems(childrenItemIds, Arrays.asList(SystemLookup.RequestTypeLkp.DISCONNECT));
				
				for (RequestInfo reqInfo: dataReqsInfo) {
					Request dataCirRequest = requestDAO.getRequest(reqInfo.getRequestNumber(), false);
					if (!otherRequests.contains(dataCirRequest)) {
						otherRequests.add(dataCirRequest);
					}
				}
				
				// otherRequests.removeAll(childrenDisconnectRequests);
				// otherRequests.addAll(childrenDisconnectRequests);
			}
		}
		
		return otherRequests;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public String getRequestStageLkpValue(Request request) {
		
		return requestHistoryDAO.getRequestStageLkpValue(request);
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public String getItemName(Long itemId) {
		
		return itemDAO.getItemName(itemId);
		
	}

	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public void handleRequest(RequestManager requestManager, Request request,
			List<Request> allRequests, Errors errors, Boolean requestByPass,
			UserInfo userInfo) {

		requestManager.handleRequest(request, allRequests, errors, requestByPass, userInfo);
		
	}

}
