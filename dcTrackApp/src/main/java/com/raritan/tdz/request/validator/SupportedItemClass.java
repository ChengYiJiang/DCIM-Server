package com.raritan.tdz.request.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.request.home.RequestMessage;

/**
 * 
 * @author bunty
 *
 */

public class SupportedItemClass implements RequestValidator {

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	private List<Long> itemClassSupported;
	
	public List<Long> getItemClassSupported() {
		return itemClassSupported;
	}

	public void setItemClassSupported(List<Long> itemClassSupported) {
		this.itemClassSupported = itemClassSupported;
	}

	@Override
	public void validate(RequestMessage requestMessage) {
		
		if (null == itemClassSupported || itemClassSupported.size() == 0) return;
		
		Errors errors = requestMessage.getErrors();
		
		Request request = requestMessage.getRequest();
		
		Long itemClass = itemDAO.getItemClass(request.getItemId());
		
		if (!itemClassSupported.contains(itemClass)) {
			Object[] errorArgs = { };
			errors.rejectValue("item", "ItemMoveValidator.ItemClassSupportedForMove", errorArgs, "Move not supported for these items.");
		}
		
	}

}
