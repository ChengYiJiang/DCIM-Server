package com.raritan.tdz.request.home;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;

public class RequestDeletePlannedCircuits implements RequestStageHelper {

	private Map<Long, ItemSaveBehavior> deletePlannedCircuitBehaviors;
	
	public RequestDeletePlannedCircuits( Map<Long, ItemSaveBehavior> deletePlannedCircuitBehaviors ) {
		
		this.deletePlannedCircuitBehaviors = deletePlannedCircuitBehaviors;
	}

	@Autowired
	private ItemDAO itemDAO;
	
	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo)
			throws Throwable {
		
		Long itemClass = itemDAO.getItemClass(request.getItemId());

		ItemSaveBehavior deletePlannedCirBehavior = deletePlannedCircuitBehaviors.get(itemClass);
		
		if (null == deletePlannedCirBehavior) return;
		
		deletePlannedCirBehavior.postSave(null, userInfo, request.getItemId(), request.getRequestType());
		
	}
	
}
