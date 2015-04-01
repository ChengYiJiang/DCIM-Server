package com.raritan.tdz.item.home.itemObject;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;

public interface PortObjectHome {
	public Object saveItemPort(Long itemId, Long portId, /*String portType,*/ PortInterface dto, UserInfo userInfo, Errors errors)
			throws BusinessValidationException, Throwable;

}
