package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.ConnectorLkuCache;
import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;

public class BranchCircuitBreakerImpl implements BranchCircuitBreaker, PortAdaptor {

	@Autowired
	private ConnectorLkuCache connectorLkuCache;
	
	@Autowired
	private LksCache lksCache;
	
	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;

	
	@Override
	public IPortInfo create(Item item, Long phaseLkpValueCode, Long startPoleNumber,
			Long maxCurrent) {

		MeItem meItem = (MeItem) item;
		PowerPort[] poles = getPolesNumbers(item, phaseLkpValueCode, startPoleNumber);
		
		if (null == poles || poles.length == 0) return null;
		
		// set start pole name 
		for (int i = 1; i < poles.length; i++) {
			poles[0].setPortName(poles[0].getPortName() + "," + poles[i].getPortName());
		}
		
		// set the connector for the start pole
		poles[0].setConnectorLookup(connectorLkuCache.getConnectorLkuData(SystemLookup.PhaseIdClass.phaseIdToConnectorLkuId.get(phaseLkpValueCode)));
		
		// set the phase
		poles[0].setPhaseLookup(lksCache.getLksDataUsingLkpCode(phaseLkpValueCode));
		
		// set the volts
		if (phaseLkpValueCode.equals(SystemLookup.PhaseIdClass.SINGLE_2WIRE)) {
			// set low voltage
			poles[0].setVoltsLookup(lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getPhaseVolts())).intValue()), SystemLookup.LkpType.VOLTS));
		}
		else {
			// set high voltage
			poles[0].setVoltsLookup(lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS));
		}
		
		// set current value
		poles[0].setAmpsNameplate(meItem.getRatingAmps());
		
		// set phase legs
		poles[0].setPhaseLegsLookup(lksCache.getLksDataUsingLkpAndType(getPhaseLegLkpValue(phaseLkpValueCode, poles[0].getPolePhase()), SystemLookup.LkpType.PHASE_LEGS));

		// set breaker port for the remainder of the poles
		for (int i = 1; i < poles.length; i++) {
			poles[i].setBreakerPort(poles[0]);
		}
		
		return poles[0];
		
	}

	private PowerPort[] getPolesNumbers(Item item, Long phaseLkpValueCode, Long startPoleNumber) {
		
		Long numOfPolesUpdating = getNumOfPolesUpdating(phaseLkpValueCode);
		int numOfPoles = numOfPolesUpdating.intValue();
		if (0 == numOfPoles) return null;
		
		PowerPort[] poles = new PowerPort[numOfPoles];
		int poleIdx = 0;
		
		PowerPort startPort = getPort(item, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, startPoleNumber.toString());
				// powerPortDAO.getPort(item.getItemId(), SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, startPoleNumber.toString());
		if (null == startPort || null != startPort.getPhaseLookup() || null != startPort.getBreakerPort()) return null; // pole is already part of some branch circuit breaker
		poles[poleIdx] = startPort; poleIdx++;
		Long startPoleRailsLkpValueCode = startPort.getFaceLookup().getLkpValueCode();
		
		int sortOrder = startPort.getSortOrder();
		for (int i = 0; i < numOfPoles - 1; i++) {
			sortOrder++;
			PowerPort port = getPort(item, SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, sortOrder);
					// powerPortDAO.getPort(item.getItemId(), SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER, sortOrder);
			
			if (null == port || null != startPort.getPhaseLookup() || null != port.getBreakerPort()) return null;  // pole is already part of some branch circuit breaker
			Long nextPoleRailsLkpValueCode = port.getFaceLookup().getLkpValueCode();
			if (!nextPoleRailsLkpValueCode.equals(startPoleRailsLkpValueCode)) return null; // poles are in different side of the panels
			
			poles[poleIdx] = port; poleIdx++;
		}
		
		return poles;
	}

	private Long getNumOfPolesUpdating(Long phaseLkpValueCode) {
		
		switch (phaseLkpValueCode.intValue()) {
		
		case (int) SystemLookup.PhaseIdClass.SINGLE_2WIRE:
			return 1L;
		
		case (int) SystemLookup.PhaseIdClass.SINGLE_3WIRE:
			return 2L; 
		
		case (int) SystemLookup.PhaseIdClass.THREE_WYE:
			return 3L;
		
		case (int) SystemLookup.PhaseIdClass.THREE_DELTA:
			
			return 3L;
		
		default:
			return 0L;
		}
		
	}

	private String getPhaseLegLkpValue(Long phaseLkpValueCode, String currentPhaseLeg) {
		
		switch (phaseLkpValueCode.intValue()) {
		
		case (int) SystemLookup.PhaseIdClass.SINGLE_2WIRE:
			return currentPhaseLeg;
		
		case (int) SystemLookup.PhaseIdClass.SINGLE_3WIRE:
			if (currentPhaseLeg.equals("A")) return "AB";
			if (currentPhaseLeg.equals("B")) return "BC";
			if (currentPhaseLeg.equals("C")) return "CA";
			return currentPhaseLeg;
				
		case (int) SystemLookup.PhaseIdClass.THREE_WYE:
		case (int) SystemLookup.PhaseIdClass.THREE_DELTA:
			return "ABC";
			
		default:
			return "";
		}
		
	}
	
	private PowerPort getPort(Item item, Long portSubClass, String portName) {
		
		Set<PowerPort> ports = item.getPowerPorts();
		
		for (PowerPort port: ports) {
			if (port.getPortSubClassLookup().getLkpValueCode().equals(portSubClass) && port.getPortName().startsWith(portName) /*port.getPortName().equals(portName)*/) {
				return port;
			}
		}
		
		return null;
	}
	
	private PowerPort getPort(Item item, Long portSubClass, int sortOrder) {

		Set<PowerPort> ports = item.getPowerPorts();
		
		for (PowerPort port: ports) {
			if (port.getPortSubClassLookup().getLkpValueCode().equals(portSubClass) && port.getSortOrder() == sortOrder) {
				return port;
			}
		}
		
		return null;
		
	}

	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		Long phaseLkpValueCode = (Long) additionalParameters[0];
		Long startPoleNumber = (Long) additionalParameters[1];
		Long maxCurrent = (Long) additionalParameters[2];
		
		create(item, phaseLkpValueCode, startPoleNumber, maxCurrent);
		
		return null;
	}

	@Override
	public IPortInfo updateUsed(IPortInfo port, IPortInfo oldSrcPort, Errors errors) {
		
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, (PowerPort) oldSrcPort);
		
		return port;	
	}

	@Override
	public IPortInfo updateUsed(IPortInfo port, boolean value, Errors errors) {
		
		port = portAdaptorHelper.updateUsedFlag((PowerPort)port, value);
		
		return port;	
	}

	@Override
	public IPortInfo updateVolt(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		Long phaseLkpValueCode = (Long) additionalParameters[0];
		PowerPort powerPort = (PowerPort) port;
		MeItem meItem = (MeItem) item; 
		
		if (phaseLkpValueCode.equals(SystemLookup.PhaseIdClass.SINGLE_2WIRE)) {
			// set low voltage
			powerPort.setVoltsLookup(lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getPhaseVolts())).intValue()), SystemLookup.LkpType.VOLTS));
		}
		else {
			// set high voltage
			powerPort.setVoltsLookup(lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS));
		}
		
		return port;
	}

	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		
		return convert(item, errors, additionalParameters);
		
	}

	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		
		PowerPort powerPort = (PowerPort) port;
		MeItem meItem = (MeItem) item;
		
		powerPort.setAmpsNameplate(meItem.getRatingAmps());
		
		return port;
		
	}

}
