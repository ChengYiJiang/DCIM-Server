package com.raritan.tdz.move.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.request.CircuitRequest;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.request.ItemRequest;

/**
 * issue disconnect request for all circuits in the cabinet
 * @author bunty
 *
 */
public class CabinetMoveDisconnectRequest implements MoveDisconnectRequest {

	@Autowired
	private CircuitRequest circuitRequest;
	
	@Autowired
	private ItemFinderDAO itemDAO;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private CircuitDAO<DataCircuit> dataCircuitDAOExt;
	
	@Override
	public void disconnect(Map<Long, Long> itemIds, Map<Long, Long> requestMap,
			UserInfo user) throws BusinessValidationException {
		
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

	}
	
	private List<CircuitCriteriaDTO> getDataCircuitsOutsideCabinet(Long cabinetId){
		return dataCircuitDAOExt.getAllCircuitsOutsideCabinet(cabinetId);
	}


}
