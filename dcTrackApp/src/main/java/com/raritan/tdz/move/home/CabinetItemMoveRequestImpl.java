/**
 * 
 */
package com.raritan.tdz.move.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.request.ItemRequest;

/**
 * @author prasanna
 * This is a special class for CabinetItemMoveRequest
 * 
 */
public class CabinetItemMoveRequestImpl implements ItemRequest {
	
	@Autowired
	private ItemRequest itemRequest;
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	@Autowired
	private CircuitRequest circuitRequest;
	
	@Autowired
	private ItemFinderDAO itemDAO;
	
	@Autowired
	private MessageSource messageSource;
	

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#convertToVMRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> convertToVMRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		return itemRequest.convertToVMRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#takeItemOffsiteRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> takeItemOffsiteRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		return itemRequest.takeItemOffsiteRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#bringItemOnsiteRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> bringItemOnsiteRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		return itemRequest.bringItemOnsiteRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#powerOffItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> powerOffItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		return itemRequest.powerOffItemRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#powerOnItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> powerOnItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		return itemRequest.powerOnItemRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#decommisionItemToStorageRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> decommisionItemToStorageRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		return itemRequest.decommisionItemToStorageRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#decommisionItemToArchiveRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> decommisionItemToArchiveRequest(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException {
		return itemRequest.decommisionItemToArchiveRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#installItemRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> installItemRequest(List<Long> itemIds, UserInfo user)
			throws BusinessValidationException {
		return itemRequest.installItemRequest(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#resubmitRequest(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> resubmitRequest(List<Long> requestIds, UserInfo user)
			throws BusinessValidationException, DataAccessException {
		return itemRequest.resubmitRequest(requestIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getRequests(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, List<Request>> getRequests(List<Long> itemIds,
			UserInfo user) throws BusinessValidationException,
			DataAccessException {
		return itemRequest.getRequests(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getRequests(java.util.List, java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, List<Request>> getRequests(List<Long> itemIds,
			List<Long> requestStages, UserInfo user)
			throws BusinessValidationException, DataAccessException {
		return itemRequest.getRequests(itemIds, user);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getLatestRequestStage(java.lang.Long)
	 */
	@Override
	public LksData getLatestRequestStage(Long itemId)
			throws DataAccessException {
		return itemRequest.getLatestRequestStage(itemId);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getErrors()
	 */
	@Override
	public Errors getErrors() {
		return itemRequest.getErrors();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#clearErrors()
	 */
	@Override
	public void clearErrors() {
		itemRequest.clearErrors();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#moveItemRequest(java.util.Map, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Map<Long, Long> moveItemRequest(Map<Long, Long> itemIds,
			UserInfo user, boolean disconnect) throws BusinessValidationException {
		//First perform move item request
		
		Map<Long,Long> requestMap = itemRequest.moveItemRequest(itemIds, user, true);
		
		//Do disconnects
		List<Long> itemToMoveIds = new ArrayList<Long>(itemIds.keySet());
		for (Long itemId:itemToMoveIds){
			List<CircuitCriteriaDTO> circuitList = getDataCircuitsOutsideCabinet(itemId);

			for(CircuitCriteriaDTO cr:circuitList) {
				cr.setUserInfo(user);
			}
			
			try {
				circuitRequest.disconnectCircuits(circuitList, requestMap.get(itemId), ItemRequest.ItemRequestType.moveItem, null);
			} catch (DataAccessException e) {
				List<String> itemNames = itemDAO.findItemNameById(itemId);
				if (itemNames.size() == 1){
					Object[] args = {itemNames.get(0)};
					BusinessValidationException.throwBusinessValidationException("itemRequest.submitFailedOnItem", 
							args, this.getClass(), messageSource);
				}
			}
		}
		
		return requestMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getRequests(java.lang.Long, java.util.List)
	 */
	@Override
	public List<Request> getRequests(Long itemId, List<Long> requestStages)
			throws BusinessValidationException, DataAccessException {
		return itemRequest.getRequests(itemId, requestStages);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getIsMoveRequestAllowed(java.util.List, com.raritan.tdz.domain.UserInfo)
	 */
	@Override
	public Boolean getIsMoveRequestAllowed(List<Integer> itemIds,
			UserInfo userInfo) throws BusinessValidationException,
			DataAccessException {
		return itemRequest.getIsMoveRequestAllowed(itemIds, userInfo);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#setItemStatus(java.lang.Long, java.lang.Long)
	 */
	@Override
	public void setItemStatus(Long itemId, Long statusValueCode)
			throws Throwable {
		itemRequest.setItemStatus(itemId, statusValueCode);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#getItemRequestStages(long, java.util.List)
	 */
	@Override
	public List<Long> getItemRequestStages(long itemId,
			List<Long> requestStageValueCodes) throws DataAccessException {
		return itemRequest.getItemRequestStages(itemId, requestStageValueCodes);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.request.ItemRequest#itemRequestExistInStages(long, java.util.List)
	 */
	@Override
	public boolean itemRequestExistInStages(long itemId,
			List<Long> requestStageValueCodes) throws DataAccessException {
		return itemRequest.itemRequestExistInStages(itemId, requestStageValueCodes);
	}
	
	private List<CircuitCriteriaDTO> getDataCircuitsOutsideCabinet(Long cabinetId){
		return dataCircuitDAOExt.getAllCircuitsOutsideCabinet(cabinetId);
	}
}
