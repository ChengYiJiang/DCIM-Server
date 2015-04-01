package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.util.RequestDTO;

public interface RequestHelper {

	public boolean getRequestBypassSetting(UserInfo userInfo);

	public List<Request> getRequestFromDTO(List<RequestDTO> requestDTOs);

	public List<Request> getAssociatedRequests(Request request);

	public String getRequestStageLkpValue(Request request);

	public String getItemName(Long itemId);

	public void handleRequest(RequestManager requestManager, Request request, List<Request> allRequests, Errors errors, Boolean requestByPass, UserInfo userInfo);

}
