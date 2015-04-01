package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

/**
 * returns port domain object for a given port
 * @author bunty
 *
 */
public interface PortFactory {
	
	IPortInfo get(Long portId, Errors errors);
	
	IPortInfo get(Long itemId, Long portSubClass, Errors errors);
	
	IPortInfo get(Item item, Long portSubClass, Errors errors);
	
}
