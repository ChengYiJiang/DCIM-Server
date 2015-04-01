package com.raritan.tdz.powerchain.home;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.powerchain.home.PowerChainLookup.PowerPanelPoleNumbering;

/**
 * this will create the poles for the power panel
 * Features pending:
 * 1. Increase the pole count - should just keep adding poles to the panel starting with the current sort order
 * 2. Decrease the pole count - delete poles having higher sort order
 * @author bunty
 *
 */
public class PowerPanelPoleImpl implements PowerPanelPole, PortAdaptor {

	@Autowired
	private LksCache lksCache;
	
	@Autowired(required=true)
	PortAdaptorHelper portAdaptorHelper;
	
	@Autowired(required=true)
	PortConnectionFactory portConnectionFactory;
	
	@Override
	public void create(Item item, Long poleNumbering, Long startPoleNumber) {

		if (PowerPanelPoleNumbering.ODD_EVEN == poleNumbering) {
			createOddEvenNumberedPoles(item);
		}
		else if (PowerPanelPoleNumbering.SEQUENTIAL == poleNumbering) {
			createSequentialNumberedPoles(item);
		}
		else {
			
			// TODO:: handle other kind of pole numbering
		}
		
	}
	
	@Override
	public void delete(Item item) {
		
		Set<PowerPort> ppSet = item.getPowerPorts();
		if (null == ppSet) return;
		
		PowerPort pp = null;
		Iterator<PowerPort> itr = ppSet.iterator();
		while (itr.hasNext()) {
			pp = itr.next();
			if (null != pp && pp.getPortSubClassLookup().getLkpValueCode().equals(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER)) {
				itr.remove();
			}
		}
	}
	
	private void createOddEvenNumberedPoles(Item item) {
		MeItem meItem = (MeItem) item;
		int numOfPoles = meItem.getPolesQty();
		
		int sortOrder = 1;
		
		String currentPolePhase = "A";
		Long phaseLkpValueCode = meItem.getPhaseLookup().getLkpValueCode();
		
		// first create odd poles
		for (int i = 1; i <= numOfPoles; i += 2) {
			
			PowerPort pole = new PowerPort();
			
			pole.setItem(item);
			pole.setPortName((new Integer(i)).toString());
			pole.setUsed(false);
			pole.setPortSubClassLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
			pole.setSortOrder(sortOrder); sortOrder++;
			pole.setFaceLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.RailsUsed.LEFT_REAR));
			pole.setPowerFactor(1);
			pole.setAddress(item.getItemName());
			pole.setPolePhase(currentPolePhase);
			currentPolePhase = getNextPolePhase(currentPolePhase, phaseLkpValueCode);
			pole.setCreationDate(getCurrentTime());
			
			item.addPowerPort(pole);
		}

		// then create even poles
		for (int i = 2; i <= numOfPoles; i += 2) {
			
			PowerPort pole = new PowerPort();
			
			pole.setItem(item);
			pole.setPortName((new Integer(i)).toString());
			pole.setUsed(false);
			pole.setPortSubClassLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
			pole.setSortOrder(sortOrder); sortOrder++;
			pole.setFaceLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.RailsUsed.RIGHT_REAR));
			pole.setPowerFactor(1);
			pole.setAddress(item.getItemName());
			pole.setPolePhase(currentPolePhase);
			currentPolePhase = getNextPolePhase(currentPolePhase, phaseLkpValueCode);
			pole.setCreationDate(getCurrentTime());
			
			item.addPowerPort(pole);
		}

	}
	
	private void createSequentialNumberedPoles(Item item) {
		MeItem meItem = (MeItem) item;
		int numOfPoles = meItem.getPolesQty();
		
		int sortOrder = 1;
		
		String currentPolePhase = "A";
		Long phaseLkpValueCode = meItem.getPhaseLookup().getLkpValueCode();
		
		// create sequential poles
		for (int i = 1; i <= numOfPoles; i++) {
			
			PowerPort pole = new PowerPort();
			
			pole.setItem(item);
			pole.setPortName((new Integer(i)).toString());
			pole.setUsed(false);
			pole.setPortSubClassLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER));
			pole.setSortOrder(sortOrder); sortOrder++;
			pole.setFaceLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.RailsUsed.LEFT_REAR));
			pole.setPowerFactor(1);
			pole.setAddress(item.getItemName());
			pole.setPolePhase(currentPolePhase);
			currentPolePhase = getNextPolePhase(currentPolePhase, phaseLkpValueCode);
			pole.setCreationDate(getCurrentTime());
			
			item.addPowerPort(pole);
			
		}

	}

	
	private String getNextPolePhase(String currentPolePhase, Long phaseLkpValueCode) {
		
		switch(phaseLkpValueCode.intValue()) {
			case (int) SystemLookup.PhaseIdClass.SINGLE_2WIRE:
				return "A";
			
			case (int) SystemLookup.PhaseIdClass.SINGLE_3WIRE:
				if (currentPolePhase.equals("A")) return "B";
				else 	if (currentPolePhase.equals("B")) return "A";
			
			case (int) SystemLookup.PhaseIdClass.THREE_WYE:
				if (currentPolePhase.equals("A")) return "B";
				else 	if (currentPolePhase.equals("B")) return "C";
				else 	if (currentPolePhase.equals("C")) return "A";
			
			default:
				return currentPolePhase;
		}
		
	}
	
	private Timestamp getCurrentTime() {
		Date date= new java.util.Date();
		Timestamp currentTime = new Timestamp(date.getTime());
		
		return currentTime;
	}

	@Override
	public IPortInfo convert(Item item, Errors errors, Object... additionalParameters) {
		
		Long poleNumbering = (Long) additionalParameters[0];
		Long startPoleNumber = (Long) additionalParameters[1];
		
		create(item, poleNumbering, startPoleNumber);
		
		return null;
	}

	@Override
	public IPortInfo updateUsed(IPortInfo port, IPortInfo oldSrcPort,
			Errors errors) {
		
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
		// No updates are required for the volt at the pole level
		return port;
	}

	@Override
	public IPortInfo updatePhase(Item item, IPortInfo port, Errors errors, Object... additionalParameters) {
		// No updates are required for the phase at the pole level
		return port;
	}

	@Override
	public IPortInfo updateAmps(Item item, IPortInfo port, Errors errors) {
		// No updates are required for the phase at the pole level
		return port;
	}
		
}
