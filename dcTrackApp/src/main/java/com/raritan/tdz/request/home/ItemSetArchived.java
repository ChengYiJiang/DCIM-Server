package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.dao.RequestDAO;

public class ItemSetArchived implements RequestStageHelper {

	@Autowired
	private RequestDAO requestDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo)
			throws Throwable {
		
		requestDAO.itemArchived(request, userInfo);

	}

}
