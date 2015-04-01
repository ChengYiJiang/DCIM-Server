package com.raritan.tdz.item.itemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

public class ItemStatePowerPanel implements ItemState {

	@Autowired(required=true)
	private ItemDAO itemDAO;

	@Autowired(required = true)
	private SystemLookupFinderDAO systemLookupFinderDAO;

	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;

	@Autowired(required = true)
	private PowerCircuitHome powerCircuitHome;

	private ItemState itemStateCommon;
	
	private long itemState;
	
	public long getItemState() {
		return itemState;
	}

	public void setItemState(long itemState) {
		this.itemState = itemState;
	}

	
	public ItemState getItemStateCommon() {
		return itemStateCommon;
	}

	
	public void setItemStateCommon(ItemState itemStateCommon) {
		this.itemStateCommon = itemStateCommon;
	}
	

	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		
		// set the port status to Archived / Storage
		// updatePortStatus(item);
		
		// set the ups bank id to null
		updateUpsBankId(item);

		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null != powerPorts) {
			for (PowerPort port: powerPorts) {
				if (null != port.getSourcePowerConnections()) {
					port.getSourcePowerConnections().clear();
				}
				if (null != port.getDestPowerConnections()) {
					port.getDestPowerConnections().clear();
				}
				port.setUsed(false);
			}
			// powerPorts.clear();
		}
		
		// circuitPDHome.deleteItemPowerConnections(item.getItemId()); //delete internal breaker to outlet connections
		powerCircuitHome.deleteItemBuswayConnections(item.getItemId()); //delete internal breaker to outlet connections

		// clear the connections
		// circuitPDHome.deleteItemPowerConnections(item.getItemId());
		
		// get the panels' power outlets associated with the Floor PDU
		List<Long> panels = new ArrayList<Long>();
		panels.add(item.getItemId());
		List<Long> powerOutlets = itemDAO.getPowerPanelConnectedPowerOutlet(panels);
		
		if (null != powerOutlets && powerOutlets.size() > 0) {
			
			itemDAO.clearPowerOutletAssociationWithPanel(powerOutlets);
			
		}
		
		// clear the placement of all power outlets
		// itemDAO.clearPowerOutletPlacement(powerOutlets);
		
		// set the power outlets' state to archive
		// itemDAO.setItemState(powerOutlets, itemState);

		// clear placement
		clearPowerPanelPlacement(item);
		
		// perform common operation on archived / storage on save - clear placement
		itemStateCommon.onSave(item);
		
	}


	private void clearPowerPanelPlacement(Item item) {
		item.setParentItem(null);
		item.setUPosition(-9);
		item.setFacingLookup(null);
	}
	
	@SuppressWarnings("unused")
	private void updatePortStatus(Item item) {
		itemDAO.initPowerPortsAndSourceConnectionsProxy(item);
		
		Map<Long, Long> usedFlagUpdateMap = new HashMap<Long, Long>();
		
		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null != powerPorts) {
			for (PowerPort port: powerPorts) {
				Set<PowerConnection> conns = port.getSourcePowerConnections();
				if (null != conns) {
					// collect all the dest port that needs used flags update
					for (PowerConnection conn: conns) {
						usedFlagUpdateMap.put(conn.getDestPort().getPortId(), conn.getSourcePort().getPortId());
					}
					conns.clear();
				}
				if (null != port.getDestPowerConnections()) {
					port.getDestPowerConnections().clear();
				}
				port.setPortStatusLookup(systemLookupFinderDAO.findByLkpValueCode(itemState).get(0));
			}
		}
		
		// update the used flag of the current destination port
		for (Map.Entry<Long, Long> entry : usedFlagUpdateMap.entrySet()) {
		    Long destId = entry.getKey();
		    Long srcId = entry.getValue();
		    powerPortDAO.changeUsedFlag(destId, srcId);
		}

	}
	
	private void updateUpsBankId(Item item) {
		if (item instanceof MeItem && item.getClassLookup() != null && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) &&
				item.getSubclassLookup() != null && (item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.LOCAL) ||
						item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.REMOTE) ||
						item.getSubclassLookup().getLkpValueCode().equals(SystemLookup.SubClass.BUSWAY))) {

			MeItem meItem = (MeItem) item;
			meItem.setUpsBankItem(null);
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return itemStateCommon.supports(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		itemStateCommon.validate(target, errors);
		
	}

	@Override
	public RulesNodeEditability getEditability(Item item) {
		return itemStateCommon.getEditability(item);
	}

	@Override
	public boolean canTransition(Item item) {
		return itemStateCommon.canTransition(item);
	}

	@Override
	public List<Long> getAllowableStates() {
		return itemStateCommon.getAllowableStates();
	}

	@Override
	public boolean isTransitionPermittedForUser(Item item, Long newState,
			UserInfo userInfo) {
		return itemStateCommon.isTransitionPermittedForUser(item, newState, userInfo);
	}

	@Override
	public void validateMandatoryFields(Item item, Errors errors,
			Long newStatusLkpValueCode) throws DataAccessException,
			ClassNotFoundException {
		itemStateCommon.validateMandatoryFields(item, errors, newStatusLkpValueCode);
		
	}

	@Override
	public void validateParentChildConstraint(Item item, Errors errors,
			Long newStatusLkpValueCode, String errorCodePrefix)
			throws DataAccessException, ClassNotFoundException {
		// Power Panels will not validate the parent-child constraint
		
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo userSession,
			Errors errors) {
		itemStateCommon.validateAllButReqFields(target, userSession, errors);
		
	}


}
