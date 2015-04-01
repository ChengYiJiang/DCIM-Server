package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * returns a port domain object
 * @author bunty
 *
 */
public abstract class PortFactoryImpl implements PortFactory {

	@Autowired(required=true)
	protected PowerPortDAO powerPortDAO;
	
	@Autowired(required=true)
	protected ItemDAO itemDAO;
	
	@Autowired(required=true)
	protected PortAdaptorFactory portAdaptorFactory;
	
	@Override
	public IPortInfo get(Long portId, Errors errors) {
		IPortInfo port = null;
		try {
			port = powerPortDAO.loadPort(portId);
		}
		catch (DataAccessException ex) {
			errors.reject("PowerChain.cannotGetPort");
		}
		return port;
	}

	@Override
	public IPortInfo get(Long itemId, Long portSubClass, Errors errors) {
		Item item = itemDAO.getItem(itemId);
		return get(item, portSubClass, errors);
		
	}

	@Override
	public IPortInfo get(Item item, Long portSubClass, Errors errors) {
		PortAdaptor portAdaptor = portAdaptorFactory.get(portSubClass);
		return portAdaptor.convert(item, errors);
		
	}
	
	protected IPortInfo getExistingSingletonPort(Item item, Long portSubClass, Errors errors) {
		Set<PowerPort> ports = item.getPowerPorts();
		if (null != ports) {
			for (PowerPort port: ports) {
				if (port.getPortSubClassLookup().getLkpValueCode().longValue() == portSubClass.longValue()) {
					return port;
				}
			}
		}
		return null;
	}


}
