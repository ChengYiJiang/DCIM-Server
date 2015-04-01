package com.raritan.tdz.vpc.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.circuit.dao.CircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.home.PortConnection;
import com.raritan.tdz.powerchain.home.PowerChainActionHandlerHelper;
import com.raritan.tdz.powerchain.home.WhipOutletToBranchCircuitBreakerActionHandler;
import com.raritan.tdz.util.GlobalUtils;

/**
 * create circuit from the power outlet using VPC
 * @author bunty
 *
 */
public class PowerOutletVPCCircuit implements VPCCircuit {

	@Autowired
	private WhipOutletToBranchCircuitBreakerActionHandler whipOutletToBranchCircuitBreakerActionHandler;
	
	@Autowired
	private PowerChainActionHandlerHelper powerChainActionHandlerHelper;
	
	@Autowired
	private PowerPortDAO powerPortDAO;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private PortConnection powerConnection;
	
	@Autowired
	private PowerConnDAO powerConnectionDAO;
	
	@Autowired
	private CircuitDAO<PowerCircuit> powerCircuitDAOExt;
	
	@Autowired
	private LksCache lksCache;

	/**
	 * 1. make a new port in the VPC power outlet at a given location and chain with the volt and phase as of the input port
	 * 2. make a connection from power outlet port to branch circuit breaker port
	 * 3. make a connection from the input port to the new power outlet port.
	 * 4. Create a circuit using these connection starting with the input port 
	 * @param srcPortId
	 * @param powerOutlet
	 */
	@Override
	public PowerCircuit create(Long srcPortId, Long locationId, String vpcChain, Errors errors, UserInfo userInfo) throws NumberFormatException, DataAccessException {

		// get the src port
		PowerPort srcPort = powerPortDAO.getPortWithConnections(srcPortId);
		if (null == srcPort) {	
			throw new IllegalArgumentException("source port do not exist");
		}
		
		PowerPort outletPort = createPowerOutletPortAndConnection(srcPort, locationId, vpcChain, errors);
		// set the port name with vpc tag
		outletPort.setPortName("vpc" + (outletPort.getSortOrder() - 1));
		powerPortDAO.update(outletPort);
		
		// create a connection from the source port to the power outlet port
		PowerConnection srcToPowerOutletConn = srcToPowerOutletConn(srcPort, outletPort, userInfo, errors);
		powerConnectionDAO.create(srcToPowerOutletConn);
		
		// get the circuit trace. first from src port to new outlet port and then from outlet port to the end (ups bank port)
		String trace = "," + srcToPowerOutletConn.getConnectionId();
		trace += powerCircuitDAOExt.getCircuitTrace("Power", outletPort.getPortId());
		List<String> connectionIds = Arrays.asList(trace.split("\\s*,\\s*"));
		
		PowerConnection startConn = getConnection(connectionIds.get(1));
		PowerConnection endConn = getConnection(connectionIds.get(connectionIds.size() - 1));
		
		// create power circuit
		PowerCircuit circuit = new PowerCircuit(startConn, endConn, trace, true);
		
		// save power circuit
		powerCircuitDAOExt.create(circuit);
		
		updateCircuitConnections(circuit);
		
		powerCircuitDAOExt.update(circuit);
		
		return circuit;
		
	}
	
	private PowerConnection srcToPowerOutletConn(PowerPort srcPort, PowerPort outletPort, UserInfo userInfo, Errors errors) {
		PowerConnection srcToPowerOutletConn = (PowerConnection) powerConnection.create(srcPort, outletPort, errors);
		if (null == srcToPowerOutletConn) {
			throw new RuntimeException("cannot create connection from source port '" + srcPort.getPortName() + "' -> '" + outletPort.getPortName() + "'");
		}
		
		// set to be connection from src port to power outlet to be planned and explicit
		srcToPowerOutletConn.setStatusLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.ItemStatus.PLANNED));
		srcToPowerOutletConn.setConnectionType(lksCache.getLksDataUsingLkpCode(SystemLookup.LinkType.EXPLICIT));
		
		// set the creator of the connection
		if (null != userInfo) {
			srcToPowerOutletConn.setCreatedBy(userInfo.getUserName());
		}
		
		return srcToPowerOutletConn;
	}
	
	private void updateCircuitConnections(PowerCircuit circuit) throws DataAccessException {
		
		String trace = circuit.getCircuitTrace(); // powerCircuitDAOExt.getCircuitTrace("Power", outletPort.getPortId()); 
		List<String> connectionIds = Arrays.asList(trace.split("\\s*,\\s*"));
		List<PowerConnection> conns = new ArrayList<PowerConnection>();
		
		for (String connStrId: connectionIds) {

			PowerConnection conn = getConnection(connStrId);
			if (null != conn) {
				conns.add(conn);
			}
			
		}
		
		circuit.setCircuitConnections(conns);
		
	}

	
	private PowerConnection getConnection(String connStrId) throws DataAccessException {
		
		String connIdStr = connStrId.trim();
		
		if (!GlobalUtils.isNumeric(connIdStr)) return null;
		
		Long connId = new Long(connIdStr);
		PowerConnection conn = powerConnectionDAO.loadConn(connId);
		
		if (null == conn) {
			conn = powerConnectionDAO.read(connId);
		}
		
		return conn;
	}
	
	@Override
	public PowerCircuit createFromPort(Long srcPortId, Long locationId, String vpcChain, Errors errors, UserInfo userInfo) throws NumberFormatException, DataAccessException {
		// get the src port
		PowerPort srcPort = powerPortDAO.getPortWithConnections(srcPortId);
		if (null == srcPort) {
			throw new IllegalArgumentException("source port do not exist");
		}
		
		PowerPort outletPort = createPowerOutletPortAndConnection(srcPort, locationId, vpcChain, errors);

		String trace = powerCircuitDAOExt.getCircuitTrace("Power", outletPort.getPortId()); 
		List<String> connectionIds = Arrays.asList(trace.split("\\s*,\\s*"));
		List<PowerConnection> conns = new ArrayList<PowerConnection>();

		String startConnIdStr = connectionIds.get(1).trim();
		Long startConnId = new Long(startConnIdStr);
		PowerConnection startConn = powerConnectionDAO.loadConn(startConnId);
		startConn = outletPort.getSourcePowerConnections().iterator().next();
		startConn.setSourcePowerPort(srcPort);
		startConn.setDestPowerPort(outletPort);
		conns.add(startConn);
		
		String endConnIdStr = connectionIds.get(connectionIds.size() - 1).trim();
		Long endConnId = new Long(endConnIdStr);
		PowerConnection endConn = powerConnectionDAO.loadConn(endConnId);
		
		
		for (String connStrId: connectionIds) {
			
			String connIdStr = connStrId.trim();
			
			if (!GlobalUtils.isNumeric(connIdStr)) continue;
			
			if (connIdStr.equals(startConnIdStr)) continue;
			
			Long connId = new Long(connIdStr);
			PowerConnection conn = powerConnectionDAO.loadConn(connId);
			
			conns.add(conn);
		}
		
		// create power circuit
		PowerCircuit circuit = new PowerCircuit(startConn, endConn, trace, true);
		circuit.setCircuitConnections(conns);
		
		// save power circuit
		powerCircuitDAOExt.create(circuit);
		
		return circuit;
	}
	
	private PowerPort getDestinationPort(PowerPort srcPort) {
		
		Set<PowerConnection> srcPwrConns = srcPort.getSourcePowerConnections();
		
		if (srcPwrConns.size() != 1) return null;
		
		PowerPort port = null;
		for (PowerConnection conn: srcPwrConns) {
			port = conn.getDestPowerPort();
		}
		
		return port;
	}
	
	@Override
	public PowerPort createPortAndConnection(Long srcPortId, Long locationId, String vpcChain, Errors errors) {
		// get the src port
		PowerPort srcPort = powerPortDAO.getPortWithConnections(srcPortId);
		if (null == srcPort) {
			throw new IllegalArgumentException("source port do not exist");
		}

		PowerPort powerOutletPort = createPowerOutletPortAndConnection(srcPort, locationId, vpcChain, errors);
		// set the port name with vpc tag
		powerOutletPort.setPortName("vpc" + (powerOutletPort.getSortOrder() - 1));
		powerPortDAO.update(powerOutletPort);
		
		return powerOutletPort;
	}
	
	private PowerPort createPowerOutletPortAndConnection(PowerPort srcPort, Long locationId, String vpcChain, Errors errors) {

		// for input voltage 120, 3-wire: select 120, 2-wire
		// for input voltage 120~240, 2-wire: select 120, 2-wire
		// for input voltage 120~240, 3-wire: select 208, 3-wire
		// get the VPC item (in location and vpcChain) power outlet that has matching volt and phase port as the src port
		Item item = itemDAO.getVPCItem(locationId, vpcChain, SystemLookup.Class.FLOOR_OUTLET, srcPort);
		if (null == item || null == item.getPowerPorts() || 0 == item.getPowerPorts().size()) {
			throw new IllegalArgumentException("cannot find matching vpc outlet in the given location '" + locationId + "' and chain '" + vpcChain + "' that source port has the matching volt and phase.");
		}
		
		// get the connected branch circuit breaker port
		PowerPort bcbPort = getDestinationPort(item.getPowerPorts().iterator().next());
		if (null == bcbPort) {
			throw new RuntimeException("cannot get the branch circuit breaker for the power outlet");
		}
		
		// TODO:: check if more than 1 UNUSED port exist in the power outlet, if yes, use one of them
		
		// call power chain to make a new power outlet port and connection to the branch circuit breaker port
		Errors powerChainErrors = powerChainActionHandlerHelper.getErrorObject();
		PowerPort outletPort = whipOutletToBranchCircuitBreakerActionHandler.process(item, bcbPort, powerChainErrors, false, false);
		if (null == outletPort) {
			throw new RuntimeException("cannot create new power outlet port and connection to the panel's BC breaker");
		}
		
		// update the outlet port to have the same connector as the srcPort
		outletPort.setConnectorLookup(srcPort.getConnectorLookup());

		return outletPort;
	}
	
	@Override
	public PowerPort createPort(Long srcPortId, Long locationId, String vpcChain, Errors errors) {

		// get the src port
		PowerPort srcPort = powerPortDAO.getPortWithConnections(srcPortId);
		if (null == srcPort) {
			throw new IllegalArgumentException("source port do not exist");
		}

		// for input voltage 120, 3-wire: select 120, 2-wire
		// for input voltage 120~240, 2-wire: select 120, 2-wire
		// for input voltage 120~240, 3-wire: select 208, 3-wire
		// get the VPC item (in location and vpcChain) power outlet that has matching volt and phase port as the src port
		Item item = itemDAO.getVPCItem(locationId, vpcChain, SystemLookup.Class.FLOOR_OUTLET, srcPort);
		if (null == item || null == item.getPowerPorts() || 0 == item.getPowerPorts().size()) {
			throw new IllegalArgumentException("cannot find matching vpc outlet in the given location '" + locationId + "' and chain '" + vpcChain + "' that source port has the matching volt and phase.");
		}

		PowerPort powerOutletPort = (PowerPort) whipOutletToBranchCircuitBreakerActionHandler.createPowerOutletBreaker(item, errors);
		
		return powerOutletPort;
	}


}
