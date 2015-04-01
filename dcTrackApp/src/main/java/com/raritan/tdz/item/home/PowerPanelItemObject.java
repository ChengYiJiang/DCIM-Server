package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Santo Rosario
 *
 */
public class PowerPanelItemObject extends MeItemObject {
	protected PowerCircuitHome powerCircuitHome;
	protected ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	public PowerPanelItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public PowerCircuitHome getPowerCircuitHome() {
		return powerCircuitHome;
	}

	public void setPowerCircuitHome(PowerCircuitHome powerCircuitHome) {
		this.powerCircuitHome = powerCircuitHome;
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.SubClass.REMOTE);
		codes.add( SystemLookup.SubClass.LOCAL);
		codes.add( SystemLookup.SubClass.BUSWAY);
		return Collections.unmodifiableSet( codes );
	}	
	
	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		//Delete Circuits for This Item
		List<Long> cirList = new ArrayList<Long>();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setItemId(item.getItemId());
				
		for(PowerCircuit rec:powerCircuitHome.viewPowerCircuitByCriteria(cCriteria)){
			cirList.add(rec.getPowerCircuitId());
		}
		
		if(cirList.size() > 0){   //Power Panel don't have data circuits
			powerCircuitHome.deletePowerCircuitByIds(cirList, false);
		}

		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal breaker to outlet connections
		
		return super.deleteItem();
	}
	
}
