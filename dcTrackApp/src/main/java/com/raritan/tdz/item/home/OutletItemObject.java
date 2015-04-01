package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Santo Rosario
 *
 */
public class OutletItemObject extends MeItemObject {
	protected PowerCircuitHome powerCircuitHome;
	protected ItemHome itemHome;
	
	public ItemHome getItemHome() {
		return itemHome;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	public OutletItemObject(SessionFactory sessionFactory) {
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
		codes.add( SystemLookup.SubClass.BUSWAY_OUTLET);
		codes.add( SystemLookup.SubClass.WHIP_OUTLET);
		codes.add( SystemLookup.Class.FLOOR_OUTLET);
		return Collections.unmodifiableSet( codes );
	}	
	
	@Override
	protected void validateUPosition(Object target, Errors errors) {
		Item item = (Item)target;
		Collection<Long> availablePositions = null;
		
		availablePositions = new ArrayList<Long>();
		// Add positions that always available
		availablePositions.add( -1L ); // Above Cabinet
		availablePositions.add( -2L ); // Below Cabinet
		availablePositions.add( -9L ); // No U-position selected
		if (null != availablePositions) {
			if (!availablePositions.contains(item.getuPosition())) {
				Object[] errorArgs = {item.getuPosition(), item.getParentItem() != null ? item.getParentItem().getItemName() : "<Unknown>" };
				errors.rejectValue("cmbCabinet", "ItemValidator.noAvailableUPosition", errorArgs, "The UPosition is not available");
			}
		}
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
		
		if(cirList.size() > 0){   //Power Outlet don't have data circuits
			powerCircuitHome.deletePowerCircuitByIds(cirList, false);
		}

		powerCircuitHome.deleteItemPowerConnections(item.getItemId()); //delete internal breaker to outlet connections
		powerCircuitHome.deleteItemBuswayConnections(item.getItemId()); //delete internal breaker to outlet connections
		
		return super.deleteItem();
	}
	
}
