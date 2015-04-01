package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */

public class ParentItemStatusCheck implements RequestValidator {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	private List<Long> itemParentStatusSupported;
	
	
	
	public List<Long> getItemParentStatusSupported() {
		return itemParentStatusSupported;
	}



	public void setItemParentStatusSupported(List<Long> itemParentStatusSupported) {
		this.itemParentStatusSupported = itemParentStatusSupported;
	}



	@Override
	public void validate(RequestMessage requestMessage) {

		if (null == itemParentStatusSupported || itemParentStatusSupported.size() == 0) return;
		
		Errors errors = requestMessage.getErrors();
		
		Request request = requestMessage.getRequest();
		
		PowerPortMove moveData = powerPortMoveDAO.getPortMoveDataUsingRequest(request.getRequestId());
		Item movedItem = moveData.getMoveItem();
		Item parentOfMovedItem = movedItem.getParentItem();
		Long parentStatus = parentOfMovedItem.getStatusLookup().getLkpValueCode();
		
		if (!itemParentStatusSupported.contains(parentStatus)) {
			String movingItemName = moveData.getOrigItem().getItemName();
			Object[] errorArgs = { movingItemName };
			errors.rejectValue("item", "ItemMoveValidator.DestCabinetNotInstalled", errorArgs, "Destination cabinet not in installed state to perform the move.");
		}

	}

}
