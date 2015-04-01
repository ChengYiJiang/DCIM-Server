package com.raritan.tdz.move.home;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItemSaveBehavior;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;

/**
 * handle creation of disconnect request for logical circuits when blade is moving out of the current chassis
 * handle deletion of disconnect request for logical circuits when blade is moving in to the current chassis
 * @author bunty
 *
 */
public class LogicalCircuitRequestMoveBehavior implements ItemSaveBehavior {

	@Autowired
	private ItemMoveHelper itemMoveHelper;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private RequestDAO requestDAO;
	
	@Autowired
	private CircuitRequest circuitRequest;
	
	@Override
	public void preValidateUpdate(Item item, Object... additionalArgs)
			throws BusinessValidationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void preSave(Item item, Object... additionalArgs)
			throws BusinessValidationException, DataAccessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSave(Item item, UserInfo user,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException {

		@SuppressWarnings("unused")
		Errors errors = (Errors) additionalArgs[0];
		
		// not a when-moved item
		if (null == item.getItemToMoveId() || item.getItemToMoveId() <= 0) { //do nothing
			return;
		}
		
		// check if the chassis of the moving and original item is the same
		boolean chassisChanged = itemMoveHelper.isMovingBladeChassisChanged(item);
		
		// if chassis changed: issue disconnect request for all logical circuits
		if (chassisChanged) {
			
			List<CircuitCriteriaDTO> logicalCircuitCriteria = itemDAO.getBladeNonRequestLogicalCircuits(item.getItemToMoveId());
			
			List<Request> itemMoveRequest = requestDAO.getPendingRequestsForItem(Arrays.asList(item.getItemToMoveId()), Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_MOVE));
			
			if (null == itemMoveRequest || itemMoveRequest.size() != 1) return;
			

			for(CircuitCriteriaDTO cr:logicalCircuitCriteria) {
				cr.setUserInfo(user);
			}
			
			circuitRequest.disconnectCircuits(logicalCircuitCriteria, itemMoveRequest.get(0).getRequestId(), ItemRequest.ItemRequestType.moveItem, chassisChanged);
			
		}
		
		// if chassis not changed: do not disconnect the logical circuit and delete all request against the logical circuits
		else {
			
			List<Long> requestIds = itemDAO.getBladeNonApprovedLogicalCircuitsRequest(item.getItemToMoveId());
			
			if (null != requestIds && requestIds.size() > 0) {
			
				itemRequestDAO.deleteRequestList(requestIds);
			}
			
		}

	}

	@Override
	public boolean canSupportDomain(String... domainObjectNames) {
		// TODO Auto-generated method stub
		return false;
	}

}
