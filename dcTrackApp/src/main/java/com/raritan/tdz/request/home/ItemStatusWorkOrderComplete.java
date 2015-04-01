
package com.raritan.tdz.request.home;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.request.ItemRequest;

public class ItemStatusWorkOrderComplete implements RequestStageHelper {

	@Autowired
	ItemRequest itemRequest;
	
	
	
	private Long status;
	
	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public ItemStatusWorkOrderComplete(Long status) {
		super();
		this.status = status;
	}

	public ItemStatusWorkOrderComplete() {
		super();
	}

	@Override
	public void update(Request request, Long requestStage, UserInfo userInfo) throws Throwable {
		if (request != null) {
			itemRequest.setItemStatus(request.getItemId(), status);
		}
	}

}

