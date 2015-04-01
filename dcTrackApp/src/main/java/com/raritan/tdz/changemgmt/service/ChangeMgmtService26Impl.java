package com.raritan.tdz.changemgmt.service;

import java.util.List;
import java.util.ArrayList;
import com.raritan.tdz.changemgmt.home.ChangeMgmtHome26;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.port.home.PortHome;


/**
 * Change management service implementation which delegates operations to a workflow service.
 *
 * @author Andrew Cohen
 * @deprecated To be replaced by new Change Management Service implementation in 3.0
 * @version 2.6.1
 */
public class ChangeMgmtService26Impl implements ChangeMgmtService26 {

	private ChangeMgmtHome26 changeMgmtHome;
	private PortHome portHome;
	
	public ChangeMgmtService26Impl(ChangeMgmtHome26 changeMgmtHome, PortHome portHome) {
		this.changeMgmtHome = changeMgmtHome;
		this.portHome = portHome;
	}
	
	@Override
	public long disconnectRequest(CircuitViewData circuit) throws ServiceLayerException {	
		return changeMgmtHome.disconnectRequest( circuit, null );
	}

	@Override
	public long disconnectAndMoveRequest(Long itemId, List<Long> connList, long portClassValueCode, String portName)
			throws ServiceLayerException {
		return changeMgmtHome.disconnectAndMoveRequest(itemId, connList, portClassValueCode, portName);
	}

	@Override
	public long connectRequest(CircuitViewData circuit)	throws ServiceLayerException {
		return changeMgmtHome.connectRequest( circuit );
	}

	@Override
	public long reconnectRequest(Long itemId, Long newCircuitId, long portClassValueCode, String portName) throws ServiceLayerException {
		return changeMgmtHome.reconnectRequest(itemId, newCircuitId, portClassValueCode, portName, null);
	}
	
	@Override
	public void deleteRequest(Long requestId, boolean doAssociatedRequests) throws ServiceLayerException{
		List<IPortInfo> ports = this.changeMgmtHome.deleteRequest(requestId, doAssociatedRequests);
		if (ports != null) {
			for (IPortInfo port : ports) {
				portHome.unlockPort( port );
			}
		}
	}
	
	
	@Override
	public List<Request> viewRequest(Request request) throws ServiceLayerException {
		return this.changeMgmtHome.viewRequest(request);
	}

	@Override
	public long getRequestStage(Request request) throws ServiceLayerException {
		return this.changeMgmtHome.getRequestStage(request);
	}

	private List<Long> convertList(List<Integer> list){
		List<Long> temp = new ArrayList<Long>(list.size());
		
		for(Integer i:list){
			temp.add(i.longValue());
		}
		
		return temp;
	}
}
