package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Create ports for ups bank. It uses the base abstract class for all its operations.
 * @author bunty
 *
 */
public class UpsBankPortFactoryImpl extends PortFactoryImpl {
	@Override
	public IPortInfo get(Item item, Long portSubClass, Errors errors) {
		if (portSubClass.longValue() == SystemLookup.PortSubClass.UPS_OUTPUT_BREAKER) {
			IPortInfo port = getExistingSingletonPort(item, portSubClass, errors);
			if (null != port) {
				return port;
			}
		}
		return super.get(item, portSubClass, errors);
		
	}

}
