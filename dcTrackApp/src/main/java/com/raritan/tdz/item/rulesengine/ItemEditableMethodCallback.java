package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class ItemEditableMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	private ItemRequest itemRequest;

	ProjectionList proList;

	public ItemEditableMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public ItemRequest getItemRequest() {
		return itemRequest;
	}

	public void setItemRequest(ItemRequest itemRequest) {
		this.itemRequest = itemRequest;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		/*List<Long> itemIds = new ArrayList<Long>();
		itemIds.add((Long)filterValue);
		Map<Long,List<Request>> requestMap = itemRequest.getRequests(itemIds, requestStages, null);
		List<Request> requests = null;
		if (null != requestMap) {
			requests = requestMap.get((Long)filterValue);
		}
		boolean editable = (!(null != requests && requests.size() > 0));*/
		boolean editable = itemRequest.itemRequestExistInStages((Long)filterValue, requestStages);
		// uiViewComponent.getUiValueIdField().setValueId((Long)statusMap.get("statusLkpValueCode"));
		uiViewComponent.getUiValueIdField().setValue(!editable);
	}
}