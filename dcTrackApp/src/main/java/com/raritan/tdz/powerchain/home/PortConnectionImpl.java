package com.raritan.tdz.powerchain.home;

import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

/**
 * operates on the port connection 
 * @author bunty
 *
 */
public class PortConnectionImpl implements PortConnection {

	/**
	 * Supporting beans 
	 */
	@Autowired(required=true)
	private PortConnectionAdaptorFactory portConnectionAdaptorFactory;
	
	// TODO:: handle case where destPort can be null 

	/**
	 * Public Functions
	 */
	
	@Override
	public ICircuitConnection create(IPortInfo srcPort, IPortInfo destPort,
			Errors errors) {
		
		PowerConnectionAdaptor powerConnectionAdaptor = getPowerConnectionAdaptor(srcPort, destPort);
		if (null == powerConnectionAdaptor) {
			return null;
		}

		PowerPort srcBreakerPort = (PowerPort) srcPort;
		PowerPort destBreakerPort = (PowerPort) destPort;
		PowerConnection powerConnection = powerConnectionAdaptor.convert(srcBreakerPort, destBreakerPort);
		return powerConnection;
		
	}

	public ICircuitConnection updateExt(Item item, IPortInfo srcPort, IPortInfo destPort,
			Errors errors) {
		if (null == srcPort) {
			return null;
		}
		PowerConnection pc = getPowerConnection(item, srcPort, destPort);
		if (null == pc) {
			return null;
		}
		PowerConnectionAdaptor powerConnectionAdaptor = getPowerConnectionAdaptor(srcPort, destPort);
		if (null == powerConnectionAdaptor) {
			return pc;
		}
		pc = powerConnectionAdaptor.update(pc, (PowerPort) destPort);
		return pc;
	}
	
	@Override
	public ICircuitConnection update(Item item, IPortInfo srcPort, IPortInfo newDestPort,
			Errors errors) {
		if (null == srcPort) {
			return null;
		}
		PowerConnection pc = getPowerConnection(item, srcPort);
		if (null == pc) {
			return null;
		}
		PowerConnectionAdaptor powerConnectionAdaptor = getPowerConnectionAdaptor(srcPort, newDestPort);
		if (null == powerConnectionAdaptor) {
			return pc;
		}
		pc = powerConnectionAdaptor.update(pc, (PowerPort) newDestPort);
		
		return pc;
	}
	
	@Override
	public IPortInfo getDestPort(Item item, IPortInfo srcPort) {
		PowerConnection pc = getPowerConnection(item, srcPort);
		if (null == pc) {
			return null;
		}
		return pc.getDestPort();
	}


	@Override
	public void delete(Item item, IPortInfo srcPort, IPortInfo destPort,
			Errors errors) {
		
		deletePowerPortConnection(item, srcPort, destPort);
	}
	
	@Override
	public void deleteSource(Item item, IPortInfo srcPort, Errors errors) {
		
		PowerPort srcPowerPort = getItemPort(item, srcPort);
		
		if (null == srcPort || null == srcPowerPort.getSourcePowerConnections()) return;
		
		srcPowerPort.getSourcePowerConnections().clear();
		
	}

	@Override
	public void deleteDestination(Item item, IPortInfo destPort, Errors errors) {
		PowerPort destPowerPort = getItemPort(item, destPort);
		
		if (null == destPort) return;
		
		destPowerPort.getDestPowerConnections().clear();
		
	}

	@Override
	public ICircuitConnection getConnection(IPortInfo srcPort, IPortInfo destPort, Errors errors) {
		return getPowerConnection(srcPort, destPort);
	}
	
	@Override
	public boolean connectionExist(IPortInfo srcPort,
			IPortInfo destPort, Errors errors) {

		PowerConnection powerConnection = getPowerConnection(srcPort, destPort);
		
		return (null != powerConnection);
	}

	@Override
	public boolean connectionExist(IPortInfo srcPort, Errors errors) {

		Set<PowerConnection> powerConnections = getPowerConnection(srcPort);

		return (null != powerConnections && powerConnections.size() > 0);
	}
	
	/**
	 * Private Functions 
	 */
	
	private PowerPort getItemPort(Item item, IPortInfo port) {
		/*Set<PowerPort> powerPorts = item.getPowerPorts();
		PowerPort itemSrcPowerPort = null;
		for (PowerPort itemPort: powerPorts) {
			if (itemPort.getPortId().equals(port.getPortId())) {
				itemSrcPowerPort = itemPort;
				break;
			}
		}

		return itemSrcPowerPort;*/
		return (PowerPort) port;
	}

	
	private void deletePowerPortConnection(Item item, IPortInfo srcPort, IPortInfo destPort) {
		
		PowerPort srcPowerPort = getItemPort(item, srcPort);
		if (null == srcPowerPort) return;
		
		Set<PowerConnection> powerConnections = srcPowerPort.getSourcePowerConnections();
		if (null == powerConnections) return;
		
		PowerConnection pc = null;
		Iterator<PowerConnection> itr = powerConnections.iterator();
		while (itr.hasNext()) {
			pc = itr.next();
			if (null == destPort && null == pc.getDestPort()) {
				itr.remove();
			}
			else {
				if (null != pc.getDestPort() && null != pc.getDestPort().getPortId() && null != destPort && null != destPort.getPortId()) {
					if (pc.getDestPort().getPortId().equals(destPort.getPortId())) {
						itr.remove();
					}	
				}
			}
		}
	}

	private PowerConnection getPowerConnection(Item item, IPortInfo srcPort, IPortInfo destPort) {
		
		// FIXME:: test to make sure that we can operate directly on the srcPort and not grab the port from the item
		PowerPort itemSrcPowerPort = getItemPort(item, srcPort);
		
		return getPowerConnection(itemSrcPowerPort, destPort);
		
	}

	private PowerConnection getPowerConnection(Item item, IPortInfo srcPort) {
		
		PowerPort itemSrcPowerPort = getItemPort(item, srcPort);
		
		Set<PowerConnection> pcs = getPowerConnection(itemSrcPowerPort);
		
		if (null == pcs || pcs.size() > 1) {
			return null;
		}
		
		for (PowerConnection pc: pcs) {
			return pc;
		}
		return null; 
		
	}
	
	private PowerConnection getPowerConnection(IPortInfo srcPort, IPortInfo destPort) {
		
		if (null == srcPort) return null;
		Set<PowerConnection> powerConnections = ((PowerPort)srcPort).getSourcePowerConnections();
		PowerConnection powerConnection = null;
		for (PowerConnection pc: powerConnections) {
			if (null == pc) { 
				continue;
			}
			if (null == destPort && null == pc.getDestPort()) {
				powerConnection = pc;
				break;
			}
			if (null == pc.getDestPort() || null == pc.getDestPort().getPortId() || null == destPort || null == destPort.getPortId()) {
				continue;
			}
			if (pc.getDestPort().getPortId().equals(destPort.getPortId())) {
				powerConnection = pc;
				break;
			}
		}
		
		return powerConnection;
	}

	@Override
	public Set<PowerConnection> getPowerConnection(IPortInfo srcPort) {

		if (null == srcPort) return null;

		return ((PowerPort)srcPort).getSourcePowerConnections();

	}


	private PowerConnectionAdaptor getPowerConnectionAdaptor(IPortInfo srcPort, IPortInfo destPort) {

		Long srcSubClass = (null != srcPort && null != srcPort.getPortSubClassLookup() && null != srcPort.getPortSubClassLookup().getLkpValueCode()) ? srcPort.getPortSubClassLookup().getLkpValueCode() : null;
		Long destSubClass = (null != destPort && null != destPort.getPortSubClassLookup() && null != destPort.getPortSubClassLookup().getLkpValueCode()) ? destPort.getPortSubClassLookup().getLkpValueCode() : null;
		PowerConnectionAdaptor powerConnectionAdaptor = portConnectionAdaptorFactory.get(srcSubClass, destSubClass);
		return powerConnectionAdaptor;
		
	}

	@Override
	public void validate(ICircuitConnection connection, Errors errors) {
		// TODO Auto-generated method stub
		
	}
	

}
