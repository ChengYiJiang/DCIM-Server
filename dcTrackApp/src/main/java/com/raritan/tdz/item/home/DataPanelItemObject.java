package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.circuit.home.DataCircuitHome;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author Santo Rosario
 *
 */
public class DataPanelItemObject extends ItItemObject {
	protected CircuitPDHome circuitPDHome;

	@Autowired(required = true)
	DataCircuitHome dataCircuitHome;

	public DataPanelItemObject(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public CircuitPDHome getCircuitPDHome() {
		return circuitPDHome;
	}

	public void setCircuitPDHome(CircuitPDHome circuitPDHome) {
		this.circuitPDHome = circuitPDHome;
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.DATA_PANEL);
		return Collections.unmodifiableSet( codes );
	}	
	
	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		//Delete Circuits for This Item
		List<Long> cirList = new ArrayList<Long>();
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setItemId(item.getItemId());
				
		for(CircuitViewData rec:circuitPDHome.viewCircuitPDList(cCriteria)){
			cirList.add(rec.getCircuitId());
		}
		
		if(cirList.size() > 0){
			dataCircuitHome.deleteDataCircuitByIds(cirList, false);
		}

		dataCircuitHome.deleteItemDataConnections(item.getItemId()); //delete internal panel to panel connections

		return super.deleteItem();
	}
	
}
