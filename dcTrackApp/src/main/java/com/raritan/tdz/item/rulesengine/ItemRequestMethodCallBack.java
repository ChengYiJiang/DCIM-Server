package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.util.RequestDTO;

public class ItemRequestMethodCallBack implements RemoteRefMethodCallback {

	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired
	private RequestDAO requestDAO;
	
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {

		List<RequestDTO> reqList = new ArrayList<RequestDTO>();
		List<Long> itemIds = new ArrayList<Long>();
		
		Long itemId = (Long) filterValue;
		itemIds.add(itemId);
		Long movingItemId = powerPortMoveDAO.getMovingItemId(itemId);
		if (null != movingItemId && movingItemId > 0) {
			itemIds.add(movingItemId);
		}
		
		List<Long> requestStages = new ArrayList<Long>();
		
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		
		List<RequestDTO> dtos = itemRequestDAO.getRequestDTO(itemIds, requestStages);
		reqList.addAll(dtos);
		
		List<String> requestNos = new ArrayList<String>();
		for (RequestDTO dto: dtos) {
			requestNos.add(dto.getRequestNo());
		}
		
		List<RequestDTO> associatedDtos = requestDAO.getAssociatedRequestDTO(requestNos, requestStages);
		
		for (RequestDTO dto: associatedDtos) {
			if (!requestNos.contains(dto.getRequestNo()))
					reqList.add(dto);
		}
		
		/*Map<Long, List<Request>> itemRequestsMap = itemRequestDAO.getRequest(itemIds, requestStages, null);
		
		for (Map.Entry<Long, List<Request>> entry: itemRequestsMap.entrySet()) {
			Long reqItemId = entry.getKey();
			List<Request> itemRequests = entry.getValue(); // itemRequestsMap.get(itemId);
			
			for (Request request: itemRequests) {
				RequestDTO reqdto = new RequestDTO();
				reqdto.setRequestId( request.getRequestId() );
				reqdto.setRequestNo( request.getRequestNo() );
				reqdto.setItemId( request.getItemId() );
				reqdto.setItemName( itemDAO.getItemName(request.getItemId()) );
				
				reqList.add( reqdto );
			}
		}*/

		uiViewComponent.getUiValueIdField().setValue(reqList);

	}

}
