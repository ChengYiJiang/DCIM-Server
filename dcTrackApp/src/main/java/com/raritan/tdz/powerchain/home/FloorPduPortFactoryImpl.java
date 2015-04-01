package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Get and Create ports for floor pdu.
 * @author bunty
 *
 */
public class FloorPduPortFactoryImpl extends PortFactoryImpl {
	
	@Override
	public IPortInfo get(Item item, Long portSubClass, Errors errors) {
		
		if (portSubClass.longValue() == SystemLookup.PortSubClass.PDU_INPUT_BREAKER) {
			IPortInfo port = getExistingSingletonPort(item, portSubClass, errors);
			if (null != port) {
				return port;
			}
		}
		return super.get(item, portSubClass, errors);
		
	}

}
