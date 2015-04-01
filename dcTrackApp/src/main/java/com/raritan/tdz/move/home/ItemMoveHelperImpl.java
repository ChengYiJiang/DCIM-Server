package com.raritan.tdz.move.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestInfo;
import com.raritan.tdz.reservation.dao.ReservationDetailsDAO;

public class ItemMoveHelperImpl implements ItemMoveHelper {

	@Autowired(required=true)
	private RequestDAO requestDAO;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	@Autowired
	private CircuitDAO<PowerCircuit> powerCircuitDAOExt;
	
	@Autowired
	private ReservationDetailsDAO reservationDetailsDAO;

	
	@Override
	public Errors getParentRequestErrors(Item item, Errors errors) {
		
		Errors parentErrors = getErrorObject(errors);
		
		try {
			
			// test placing to the moving cabinet only for new item or when cabinet placement is changed
			// if ((item.getItemId() > 0 && isCabinetChanged(item)) || item.getItemId() <= 0) 
			{
			
				List<Request> parentPendingRequests = getParentPendingRequests(item, errors);
				
				for (Request request: parentPendingRequests) {
					
					if (request.getRequestTypeLookupCode().equals(SystemLookup.RequestTypeLkp.ITEM_MOVE)) {
					
						String itemName = itemDAO.getItemName(request.getItemId());
						Object[] errorArgs = { itemName, request.getRequestNo(), request.getRequestTypeLookup().getLkpValue() };
						parentErrors.rejectValue("request", "itemRequest.cabinetMove.pendingParentRequest", errorArgs, itemName + ", Request: " + request.getRequestNo() + ", " + request.getRequestTypeLookup().getLkpValue());
					}
					
				}
			}
			
		} catch (DataAccessException e) {
		
			Object[] errorArgs = {};
			parentErrors.reject("system.error", errorArgs, "system error when getting children requests");
			
		}
		
		return parentErrors;
		
	}

	@Override
	public Errors getChildrenReservationErrors(Long parentItemId, Errors errors) {
		
		Errors childrenReservationErrors = getErrorObject(errors);
		
		List<String> reservationNames = reservationDetailsDAO.getReservationNumber(parentItemId);
		
		if (null == reservationNames || reservationNames.size() == 0) return childrenReservationErrors;
		
		Item parentItem = itemDAO.loadItem(parentItemId);
		
		Object[] errorArgs = { parentItem.getClassLookup().getLkpValue(), parentItem.getDataCenterLocation().getCode(), parentItem.getItemName(), reservationNames.toString() };
		childrenReservationErrors.rejectValue("itemMove", "ItemMoveValidator.hasReservations", errorArgs, 
				"The following reservation exist for " + parentItem.getItemName() + ": " + reservationNames.toString());

		return childrenReservationErrors;
		
	}

	
	private List<Request> getParentPendingRequests(Item item, Errors errors) throws DataAccessException {
		
		List<Request> parentPendingRequests = new ArrayList<Request>();
				
		if(isFreeStanding(item)) return parentPendingRequests;  //don't check
		
		Item parentOfItem = null;
		if (null != item.getSubclassLookup() && (item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE) || item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BLADE_SERVER))) {
			parentOfItem = ((ItItem)item).getBladeChassis();
		}
		else {
			parentOfItem = item.getParentItem();
		}

		while (null != parentOfItem) {
			
			List<Request> pendingRequests = requestDAO.getItemRequest(parentOfItem.getItemId());
			parentPendingRequests.addAll(pendingRequests);

			parentOfItem = parentOfItem.getParentItem();
		}
		
		return parentPendingRequests;
	}

	private boolean isFreeStanding(Item item){
		Item cabinet = item.getParentItem();
		
		if(cabinet == null) return false;
		
		Long subClass = cabinet.getSubclassLookup() == null ? -1L : cabinet.getSubclassLookup().getLkpValueCode();
		
		if(subClass.equals(SystemLookup.SubClass.CONTAINER)) return true;
		
		return false;
	}

	private MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, refErrors.getObjectName() );
		return errors;
		
	}
	
	private List<Long> getChildrenItemIds(Long parentItemId) {

		// Get all children of the parent item
		List<Long> childrenItemIds = itemDAO.getChildItemIds(parentItemId);
		
		// get all moving item of the children
		List<Long> movingItemIds = powerPortMoveDAO.getMovingItemId(childrenItemIds);
		
		// make sure itemId appears only once
		childrenItemIds.removeAll(movingItemIds);
		childrenItemIds.addAll(movingItemIds);
		
		// exclude all passive items
		List<Long> passiveChildren = itemDAO.getPassiveChildItemIds(parentItemId);
		childrenItemIds.removeAll(passiveChildren);

		return childrenItemIds;
	}
	
	private Map<Long, List<Request>> getChildrenPendingRequest(Long parentItemId, Errors errors) throws DataAccessException {
		// Get all children of the parent item
		List<Long> childrenItemIds = getChildrenItemIds(parentItemId); // itemDAO.getChildItemIds(parentItemId);
		
		if (childrenItemIds.size() == 0) return new HashMap<Long, List<Request>>();
		
		//Find pending request for this parent item
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		
		// Map<Long, List<Request>> childrenPendingRequests = itemRequestDAO.getRequest(childrenItemIds, requestStages, errors);
		
		Map<Long, List<Request>> childrenPendingRequests = requestDAO.getRequestsForItem(childrenItemIds, requestStages, errors);

		return childrenPendingRequests;
	}
	
	@Override
	public Errors getChildrenRequestErrors(Long itemId, Errors errors) {
		MapBindingResult childReqErrors = getErrorObject(errors);

		List<Long> requestTypeNoNotifications = Arrays.asList(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF, 
				SystemLookup.RequestTypeLkp.ITEM_POWER_ON, 
				SystemLookup.RequestTypeLkp.DISCONNECT, 
				SystemLookup.RequestTypeLkp.DISCONNECT_AND_MOVE, 
				SystemLookup.RequestTypeLkp.RECONNECT);
		
		List<String> requestNoProcessed = new LinkedList<String>();
		
		try {
			
			Map<Long, List<Request>> childrenRequests = getChildrenPendingRequest(itemId, errors);
			
			for (Map.Entry<Long, List<Request>> reqEntry: childrenRequests.entrySet()) {
				Long childItemId = reqEntry.getKey();
				List<Request> childRequests = reqEntry.getValue();
				
				if (null == childRequests || childRequests.size() == 0) continue;
				
				for (Request childRequest: childRequests) {
					
					if (requestTypeNoNotifications.contains(childRequest.getRequestTypeLookupCode())) continue;
					
					String itemName = itemDAO.getItemName(childItemId);
					Object[] errorArgs = { itemName, childRequest.getRequestNo(), childRequest.getRequestTypeLookup().getLkpValue() };
					childReqErrors.rejectValue("request", "itemRequest.cabinetMove.pendingChildRequest", errorArgs, itemName + ", Request: " + childRequest.getRequestNo() + ", " + childRequest.getRequestTypeLookup().getLkpValue());
					
					requestNoProcessed.add(childRequest.getRequestNo());
					
				}
				
			}
			
		} catch (DataAccessException e) {
			
			Object[] errorArgs = {};
			childReqErrors.reject("system.error", errorArgs, "system error when getting children requests");
		}
		
		// Get all children of the parent item
		List<Long> childrenItemIds = getChildrenItemIds(itemId);
		
		if (null == childrenItemIds || childrenItemIds.size() == 0) return childReqErrors;
		
		List<Long> circuitReqTypes = Arrays.asList(SystemLookup.RequestTypeLkp.CONNECT, 
				SystemLookup.RequestTypeLkp.DISCONNECT_AND_MOVE, 
				SystemLookup.RequestTypeLkp.RECONNECT);
		
		// Get the pending data circuit requests
		List<RequestInfo>  dataReqsInfo = dataCircuitDAOExt.getPendingCircuitRequestForItems(childrenItemIds, circuitReqTypes);
		
		for (RequestInfo reqInfo: dataReqsInfo) {
			
			updateChildrenError(reqInfo, childReqErrors, requestNoProcessed);			
		}
		
		// Get the pending power circuit requests
		List<RequestInfo>  powerReqsInfo = powerCircuitDAOExt.getPendingCircuitRequestForItems(childrenItemIds, circuitReqTypes);

		for (RequestInfo reqInfo: powerReqsInfo) {
			
			updateChildrenError(reqInfo, childReqErrors, requestNoProcessed);
			
		}
		
		// Get proposed data circuit requests
		List<RequestInfo>  dataProposedReqsInfo = dataCircuitDAOExt.getProposedCircuitRequest(childrenItemIds);	
		
		for (RequestInfo reqInfo: dataProposedReqsInfo) {
			
			updateChildrenError(reqInfo, childReqErrors, requestNoProcessed);
			
		}

		// Get proposed power circuit requests
		List<RequestInfo>  powerProposedReqsInfo = powerCircuitDAOExt.getProposedCircuitRequest(childrenItemIds);	
		
		for (RequestInfo reqInfo: powerProposedReqsInfo) {
			
			updateChildrenError(reqInfo, childReqErrors, requestNoProcessed);
		}

		return childReqErrors;
		
	}

	private void updateChildrenError(RequestInfo reqInfo, Errors childReqErrors, List<String> requestNoProcessed) {
		
		if (requestNoProcessed.contains(reqInfo.getRequestNumber())) return;
		
		String itemName = reqInfo.getItemName();
		String reqNum = reqInfo.getRequestNumber();
		String reqType = reqInfo.getRequestType();
		Object[] errorArgs = { itemName, reqNum, reqType };
		childReqErrors.rejectValue("request", "itemRequest.cabinetMove.pendingChildRequest", errorArgs, itemName + ", Request: " + reqNum + ", " + reqType);
		
		requestNoProcessed.add(reqInfo.getRequestNumber());
	}
	
	@Override
	public boolean isCabinetChanged( Item item ) {
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return false;
		
		if (item.getItemId() <= 0) return false;
		
		// if (null == item.getItemToMoveId()) return false;
		
		SavedItemData savedData = SavedItemData.getCurrentItem();
		Item savedItem = (null != savedData) ? savedData.getSavedItem() : null;
		
		if (null == savedItem) return false;
		
		Item cabinetToSave = item.getParentItem();
		Item savedCabinet = savedItem.getParentItem();
		
		if (null == cabinetToSave && null == savedCabinet) return false;
		
		if (null == cabinetToSave || null == savedCabinet) return true;

		Long cabinetToSaveId = cabinetToSave.getItemId();
		Long savedCabinetId = savedCabinet.getItemId();
		
		if (cabinetToSaveId.equals(savedCabinetId)) return false;
		
		return true;
		
	}

	
	@Override
	public boolean isMovingBladeChassisChanged( Item item ) {

		ItItem whenMovedItem = null;
		if (item instanceof ItItem) {
			whenMovedItem = (ItItem) item;
		}
		if (null == whenMovedItem) return false;
		Long whenMovedChassisId = (null !=  whenMovedItem.getBladeChassis()) ? whenMovedItem.getBladeChassis().getItemId() : null;

		Item movingItem = itemDAO.loadItem(item.getItemToMoveId());
		ItItem movingBlade = null;
		if (movingItem instanceof ItItem) {
			movingBlade = (ItItem) movingItem;
		}
		if (null == movingBlade) return false; 
		Long movingBladeChassisId = (null !=  movingBlade.getBladeChassis()) ? movingBlade.getBladeChassis().getItemId() : null;
		
		if (null == whenMovedChassisId && null == movingBladeChassisId) {
			return false;
		}
		
		if (null == whenMovedChassisId && null != movingBladeChassisId) {
			return true;
		}
		
		if (null != whenMovedChassisId && null == movingBladeChassisId) {
			return true;
		}
		
		return !whenMovedChassisId.equals(movingBladeChassisId);
		
	}
	
	@Override
	public boolean isChassisChanged( Item item ) {
		
		if (item.getSkipValidation() != null && item.getSkipValidation()) return false;
		
		if (item.getItemId() <= 0) return false;
		
		//if (null == item.getItemToMoveId()) return false;
		
		ItItem itItem = null;
		if (item instanceof ItItem) {
			itItem = (ItItem) item;
		}
		if (null == itItem) return false;
		
		SavedItemData savedData = SavedItemData.getCurrentItem();
		Item savedItem = (null != savedData) ? savedData.getSavedItem() : null;
		if (null == savedItem) return false;
		ItItem savedItItem = null;
		if (savedItem instanceof ItItem) {
			savedItItem = (ItItem) savedItem;
		}
		
		Item chassisToSave = itItem.getBladeChassis();
		Item savedChassis = savedItItem.getBladeChassis();
		
		if (null == chassisToSave && null == savedChassis) return false;
		
		if (null == chassisToSave || null == savedChassis) return true;

		Long cabinetToSaveId = chassisToSave.getItemId();
		Long savedCabinetId = savedChassis.getItemId();
		
		if (cabinetToSaveId.equals(savedCabinetId)) return false;
		
		return true;
		
	}

	@Override
	public Errors getPlacementInMoveCabinetError(Item item, Errors refErrors, boolean getErrorUnconditionally) {
		
		Errors placementErrors = getErrorObject(refErrors);
		
		// test placing to the moving cabinet only for new item or when cabinet placement is changed
		if ((item.getItemId() > 0 && isCabinetChanged(item)) ||
				item.getItemId() <= 0 || getErrorUnconditionally) { 
		
			Item parentItem = item.getParentItem();
			if (null == parentItem) return placementErrors;
			Long parentItemId = parentItem.getItemId();
			if (null == parentItemId) return placementErrors;
			
			// check if the parent of the item has a pending move, if yes, notify the user
			PowerPortMove portMove = powerPortMoveDAO.getPortMoveData(null, parentItemId, null);

			if (null != portMove) {
				
				Request request = portMove.getRequest();
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append(parentItem.getItemName());
				errorMsg.append(" Request: ");
				errorMsg.append(request.getRequestNo());
				errorMsg.append(" ");
				errorMsg.append(request.getRequestTypeLookup().getLkpValue());
				
				Object[] errorArgs = { errorMsg.toString() };
				placementErrors.rejectValue("itemMove", "ItemMoveValidator.parentHasPendingRequest", errorArgs, errorMsg.toString());
				
			}
		}
		
		return placementErrors;

	}

	@Override
	public Errors getPlacementInMoveChassisError(Item item, Errors refErrors, boolean getErrorUnconditionally) {
		
		Errors placementErrors = getErrorObject(refErrors);
		
		ItItem itItem = null;
		if (item instanceof ItItem) {
			itItem = (ItItem) item;
		}
		if (null == itItem) return placementErrors;
		
		// test placing to the moving chassis only for new item or when chassis placement is changed
		if ((item.getItemId() > 0 && isChassisChanged(item)) ||
				item.getItemId() <= 0 || getErrorUnconditionally) { 
		
			Item parentItem = itItem.getBladeChassis();
			if (null == parentItem) return placementErrors;
			Long parentItemId = parentItem.getItemId();
			if (null == parentItemId) return placementErrors;
			
			// check if the parent of the item has a pending move, if yes, notify the user
			PowerPortMove portMove = powerPortMoveDAO.getPortMoveData(null, parentItemId, null);

			if (null != portMove) {
				
				Request request = portMove.getRequest();
				StringBuffer errorMsg = new StringBuffer();
				errorMsg.append(parentItem.getItemName());
				errorMsg.append(" Request: ");
				errorMsg.append(request.getRequestNo());
				errorMsg.append(" ");
				errorMsg.append(request.getRequestTypeLookup().getLkpValue());
				
				Object[] errorArgs = { errorMsg.toString() };
				placementErrors.rejectValue("itemMove", "ItemMoveValidator.parentHasPendingRequest", errorArgs, errorMsg.toString());
				
			}
		}
		
		return placementErrors;

	}
}
