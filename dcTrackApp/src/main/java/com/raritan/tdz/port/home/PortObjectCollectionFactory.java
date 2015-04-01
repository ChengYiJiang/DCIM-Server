package com.raritan.tdz.port.home;

import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

public class PortObjectCollectionFactory implements IPortObjectCollectionFactory {

	// private final Logger log = Logger.getLogger( this.getClass() );
	
	private Map<String, IPortObjectCollection> portObjectsBeans;
	
	@Override
	public void setPortsIds(Map<String, IPortObjectCollection> portObjectsBeans) {
		this.portObjectsBeans = portObjectsBeans;

	}

	@Override
	public IPortObjectCollection getPortObjects(String portId, Set<IPortInfo> ports, Errors errors) {
		if (null == portId || null == portObjectsBeans) {
			return null;
		}
		IPortObjectCollection portsBean = portObjectsBeans.get(portId);
		return createPortObjects(portsBean, ports, errors);
	}

	@Override
	public IPortObjectCollection getPortObjects(Long classMountingFormFactorValue, String portId, Item item, Errors errors) {
		if (null == portId || null == portObjectsBeans) {
			return null;
		}
		String itemKeyPortType = null;
		if (null == classMountingFormFactorValue) {
			itemKeyPortType = portId;
		}
		else {
			itemKeyPortType = classMountingFormFactorValue.toString() + ":" + portId;
		}
		IPortObjectCollection portsBean = portObjectsBeans.get(itemKeyPortType);
		if (null == portsBean /*|| portsBean.length() == 0*/) {
			portsBean = portObjectsBeans.get(portId);
		}
		return createPortObjects(portsBean, item, errors);
	}

	private IPortObjectCollection createPortObjects(IPortObjectCollection portObjs, Set<IPortInfo> ports, Errors errors) {
		portObjs.init(ports, errors);
		return portObjs;
	}

	private IPortObjectCollection createPortObjects(IPortObjectCollection portObjs, Item item, Errors errors) {
		portObjs.init(item, errors);
		return portObjs;
	}

	@Override
	public IPortObjectCollection getPortObjects(String uiPortId, Set<IPortInfo> ports) {
		return getPortObjects(uiPortId, ports, null);
	}

	@Override
	public IPortObjectCollection getPortObjects(Long classMountingFormFactorValue, String uiPortId, Item item) {
		return getPortObjects(classMountingFormFactorValue, uiPortId, item, null);
	}


}
