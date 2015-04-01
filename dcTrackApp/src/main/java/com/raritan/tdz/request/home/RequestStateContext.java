package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemStateContext;


public class RequestStateContext implements RequestStageHelper {

	@Autowired
	ItemDAO itemDao;
	
	@Autowired
	ItemStateContext itemStateContext;

	
	
	private RequestStageHelper itemStateWorkOrderComplete;
	
	public RequestStateContext(RequestStageHelper itemStateWorkOrderComplete) {
		this.itemStateWorkOrderComplete = itemStateWorkOrderComplete;
	}

	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable {
		Item proxyItem = itemDao.getItem(request.getItemId());
		Item item = itemDao.initializeAndUnproxy(proxyItem);
	
		// update status of the item
		itemStateWorkOrderComplete.update(request, requestStage, userInfo);
		
		// clear placement information for an item before moving to storage.
		itemStateContext.onSave(item);

		itemDao.mergeOnly(item);
	}
}
