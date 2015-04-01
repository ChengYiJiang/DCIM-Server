package com.raritan.tdz.move.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.itemObject.ItemDeleteBehavior;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class MoveItemDeleteBehavior implements ItemDeleteBehavior {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;
	
	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private RequestDAO requestDAO;
	
	@Override
	public void deleteItem(Item item) throws BusinessValidationException,
			Throwable {
		// TODO Auto-generated method stub

	}

	@Override
	public void preDelete(Item item) throws BusinessValidationException {
		
		// FIXME:: move deletion of individual request from item delete helper to here.
		
		List<Request> dataPortRequests = dataPortMoveDAO.getRequest(item.getItemId());
		
		List<Request> associatedPendingRequests = requestDAO.getAssociatedPendingReqsForReqs(dataPortRequests);
		
		dataPortMoveDAO.deleteMoveData(item.getItemId());
		
		try {
			itemRequestDAO.deleteRequests(dataPortRequests, null);
			
		} catch (DataAccessException e) {
			
			BusinessValidationException bve =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			bve.addValidationError("Cannot delete the data port move item request");
			
		}
		
		List<Request> powerPortRequests = powerPortMoveDAO.getRequest(item.getItemId());
		
		associatedPendingRequests.addAll(requestDAO.getAssociatedPendingReqsForReqs(powerPortRequests));
		
		powerPortMoveDAO.deleteMoveData(item.getItemId());
		
		try {
			itemRequestDAO.deleteRequests(powerPortRequests, null);
			
		} catch (DataAccessException e) {
			
			BusinessValidationException bve =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			bve.addValidationError("Cannot delete the power port move item request");
			
		}

		try {
			itemRequestDAO.deleteRequests(associatedPendingRequests, null);
			
		} catch (DataAccessException e) {
			
			BusinessValidationException bve =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			bve.addValidationError("Cannot delete the associated move item request");
			
		}

	}

	@Override
	public void postDelete() throws BusinessValidationException {
		// TODO Auto-generated method stub

	}
	
}
