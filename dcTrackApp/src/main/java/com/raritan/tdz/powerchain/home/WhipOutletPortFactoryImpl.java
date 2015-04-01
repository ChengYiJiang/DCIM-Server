package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

/**
 * whip power outlet port factory
 * @author bunty
 *
 */
public class WhipOutletPortFactoryImpl extends PortFactoryImpl {

	@Override
	public IPortInfo get(Item item, Long portSubClass, Errors errors) {
		
		return super.get(item, portSubClass, errors);
		
	}

}
