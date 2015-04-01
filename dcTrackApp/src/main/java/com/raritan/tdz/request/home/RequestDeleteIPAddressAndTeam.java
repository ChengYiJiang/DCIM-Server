package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;

public class RequestDeleteIPAddressAndTeam implements RequestStageHelper {

	
	@Autowired
	private ItemDAO itemDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo)
			throws Throwable {

		Long itemId = request.getItemId();
		
		if (null == itemId) return;
		
		itemDAO.deleteItemIPAddressAndTeaming(itemId);
		
	}

}
