package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.dao.RequestDAO;

public class WorkOrderIssueUpdater implements RequestStageHelper {

	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable {

		requestDAO.createWorkOrder(request, userInfo);
		
	}

}
