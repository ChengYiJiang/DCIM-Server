package com.raritan.tdz.powerchain.home;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;

public class PowerCircuitHelperImpl implements PowerCircuitHelper {

	
	@Autowired(required=true)
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	

	@Override
	public PowerCircuitInfo getCircuitTraceInfo(PowerPort port, Errors errors) {
		List<Long> existingPortIds = new ArrayList<Long>();
		PowerCircuitInfo circuitInfo = getCircuitTraceInfoRecursive(port, existingPortIds, errors);
		
		return circuitInfo;
	}

	
	private PowerCircuitInfo getCircuitTraceInfoRecursive(PowerPort port, List<Long> existingPortIds, Errors errors) {
    	PowerCircuitInfo pwrCir = new PowerCircuitInfo();
    	
    	String trace = new String(); trace = ",";
    	PowerConnection endConn = null;
    	
    	PowerPort startPort = port;
    	
    	if (null == startPort) {
    		return pwrCir;
    	}
    	if (existingPortIds.contains(port.getPortId())) {
			Object[] errorArgs = { };
			errors.rejectValue("PowerChain", "PowerChain.loopCircuit", errorArgs, "This connection cause a circuit loop.");
    		return pwrCir;
    	}
    	existingPortIds.add(port.getPortId());
    	Set<PowerConnection> conns = startPort.getSourcePowerConnections();
    	if (conns != null && conns.size() == 1) {
    		for (PowerConnection conn: conns) {
    			trace += conn.getConnectionId();
    			endConn = conn;
    		}
    	}
    	else {
    		// invalid connections in the database
    		return pwrCir;
    	}

    	PowerPort destPort = null;
    	if (null != endConn && null != endConn.getDestPort() && null != endConn.getDestPort().getPortId()) {
    		destPort = powerPortDAO.getPortWithSourceConnections(endConn.getDestPort().getPortId());
    		
    	}
    	PowerCircuitInfo tmpPwrCir =  null;
    	if (null != destPort) {
    		tmpPwrCir =  getCircuitTraceInfoRecursive(destPort, existingPortIds, errors);
    	}
    	else {
    		pwrCir.setCircuitTrace(trace + ",");
    		pwrCir.setEndConnectionId(endConn.getConnectionId());
    	}
    	if (null != tmpPwrCir) {
    		if (null != tmpPwrCir.getCircuitTrace()) {
    			pwrCir.setCircuitTrace(trace + tmpPwrCir.getCircuitTrace());
    		}
    		if (null != tmpPwrCir.getEndConnectionId()) {
    			pwrCir.setEndConnectionId(tmpPwrCir.getEndConnectionId());
    		}
    	}
    	
    	return pwrCir;
	}


	
	public PowerCircuit getCircuitTraceValues(Long portId) {
		
		PowerCircuit pwrCir = new PowerCircuit();

    	if (null == portId) {
    		return pwrCir;
    	}

		String trace = new String(); trace = ",";
		Long endConnId = 0L;
		Long destPortId = null; 
		
		List<Object[]> connsInfo = powerCircuitDAO.getConnAndDestPort(portId);
				
		if (connsInfo != null && connsInfo.size() == 1) {
			for (Object[] connInfo: connsInfo) {
				Long connId = ((BigInteger) connInfo[0]).longValue();
				destPortId = (null != connInfo[1]) ? ((BigInteger) connInfo[1]).longValue() : null;

    			trace += connId;
    			endConnId = connId;
			}
		}
		else {
			return pwrCir;
		}
		
		
    	PowerCircuit tmpPwrCir =  null;
    	if (null != destPortId) {
    		tmpPwrCir =  getCircuitTraceValues(destPortId);
    	}
    	else {
    		pwrCir.setCircuitTrace(trace + ",");
    		// pwrCir.setEndConnection(endConnId);
    		pwrCir.setCircuitId(endConnId);
    		
    	}
    	if (null != tmpPwrCir) {
    		pwrCir.setCircuitTrace(trace + tmpPwrCir.getCircuitTrace());
    		// pwrCir.setEndConnection(tmpPwrCir.getEndConnection());
    		pwrCir.setCircuitId(tmpPwrCir.getCircuitId());
    	}
    	
    	return pwrCir;
		
	}


	@Override
	public void updateCircuitTrace(PowerCircuitInfo oldPowerCirInfo, PowerCircuitInfo newPowerCirInfo) {

		if (null == oldPowerCirInfo || null == newPowerCirInfo || null == oldPowerCirInfo.getCircuitTrace() || null == newPowerCirInfo.getCircuitTrace()) {
			return;
		}
		// List<PowerCircuit> circuits = powerCircuitDAO.getCircuitsWithTrace(oldPowerCirInfo.getCircuitTrace());
		List<Object[]> circuitsInfo = powerCircuitDAO.getCircuitsInfoWithTrace(oldPowerCirInfo.getCircuitTrace());
		// List<PowerCircuitInfo> circuits = new ArrayList<PowerCircuitInfo>(); 
		String oldTrace = oldPowerCirInfo.getCircuitTrace();
		String newTrace = newPowerCirInfo.getCircuitTrace();
		Long endConnId = newPowerCirInfo.getEndConnectionId();
				
		for (Object[] circuitInfo: circuitsInfo) {
			Long circuitId = ((BigInteger)circuitInfo[0]).longValue();
			String circuitTrace = (String) circuitInfo[1];
			String sharedCircuitTrace = (String) circuitInfo[2];
			
			PowerCircuitInfo circuit = new PowerCircuitInfo(circuitTrace, sharedCircuitTrace, endConnId, circuitId);
			
			if (null != circuitTrace && circuitTrace.endsWith(oldTrace)) {
				String newCirTrace = circuitTrace;
				newCirTrace = newCirTrace.replace(oldTrace, newTrace);
				circuit.setCircuitTrace(newCirTrace);
			}
			
			if (null != sharedCircuitTrace && sharedCircuitTrace.endsWith(oldTrace)) {
				String newCirTrace = sharedCircuitTrace;
				newCirTrace = newCirTrace.replace(oldTrace, newTrace);
				circuit.setSharedCircuitTrace(newCirTrace);
			}
			
			powerCircuitDAO.changeCircuitTrace(circuit.getCircuitId(), circuit.getCircuitTrace(), circuit.getSharedCircuitTrace(), circuit.getEndConnectionId());
			
		}
		
		

	}


}
