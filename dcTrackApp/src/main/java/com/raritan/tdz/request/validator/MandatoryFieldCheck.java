package com.raritan.tdz.request.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.MandatoryFieldStateValidator;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */
public class MandatoryFieldCheck implements RequestValidator {

	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	Map<String, MandatoryFieldStateValidator> mountingToMandatoryFieldValidator;
	
	
	

	public Map<String, MandatoryFieldStateValidator> getMountingToMandatoryFieldValidator() {
		return mountingToMandatoryFieldValidator;
	}




	public void setMountingToMandatoryFieldValidator(
			Map<String, MandatoryFieldStateValidator> mountingToMandatoryFieldValidator) {
		this.mountingToMandatoryFieldValidator = mountingToMandatoryFieldValidator;
	}


	public MandatoryFieldCheck(
			Map<String, MandatoryFieldStateValidator> mountingToMandatoryFieldValidator) {
		super();
		this.mountingToMandatoryFieldValidator = mountingToMandatoryFieldValidator;
	}


	@Override
	public void validate(RequestMessage requestMessage) throws DataAccessException, ClassNotFoundException {

		if (null == mountingToMandatoryFieldValidator || mountingToMandatoryFieldValidator.size() == 0) return;
		
		Errors errors = requestMessage.getErrors();
		
		Request request = requestMessage.getRequest();
		
		Item item = null;
		PowerPortMove moveData = powerPortMoveDAO.getPortMoveDataUsingRequest(request.getRequestId());
		if (null != moveData) {
			item = itemDAO.loadItem(moveData.getMoveItem().getItemId());
		}
		else {
			item = itemDAO.loadItem(request.getItemId());
		}
		// itemDAO.initializeAndUnproxy(item);

		String mounting = (null != item.getModel() && null != item.getModel().getMounting()) ? item.getModel().getMounting() : null;
		
		if (null == mounting) return;
		
		StringBuilder keyBuilder = new StringBuilder(mounting);
		String itemClassLkp = item.getClassLookup().getLkpValueCode().toString();
		keyBuilder.append(":").append(itemClassLkp);
		
		// Skip mandatory field validations for floorpdu and power panels
		// if (null == item.getClassLookup() || item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)) return;
		
		MandatoryFieldStateValidator mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(keyBuilder.toString());

		if (null == mandatoryFieldValidator) {
			mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(mounting);
		}
		
		if (null == mandatoryFieldValidator) {
			mandatoryFieldValidator = mountingToMandatoryFieldValidator.get(itemClassLkp);
		}
		
		mandatoryFieldValidator.validateMandatoryFields(item,  item.getStatusLookup().getLkpValueCode(), errors, "request.reqDetails", request);

	}

}
