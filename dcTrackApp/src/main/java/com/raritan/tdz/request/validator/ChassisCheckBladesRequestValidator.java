package com.raritan.tdz.request.validator;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;

public class ChassisCheckBladesRequestValidator implements RequestValidator {


	@Autowired(required=true)
	RequestValidator parentCheckChildRequestValidator;
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	
	
	@Override
	public void validate(RequestMessage requestMessage)
			throws DataAccessException, ClassNotFoundException {
		
		Request request = requestMessage.getRequest();

		Item item = null;
		PowerPortMove moveData = powerPortMoveDAO.getPortMoveDataUsingRequest(request.getRequestId());
		if (null != moveData) {
			item = moveData.getMoveItem();
		}
		else {
			item = itemDAO.getItem(request.getItemId());
		}

		LksData subClass = item.getSubclassLookup();
		if (null != subClass && (subClass.getLkpValueCode().equals(SystemLookup.SubClass.BLADE_CHASSIS) || subClass.getLkpValueCode().equals(SystemLookup.SubClass.CHASSIS))) {
			parentCheckChildRequestValidator.validate(requestMessage);
		}

	}

}
