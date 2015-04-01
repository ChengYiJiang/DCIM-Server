package com.raritan.tdz.item.itemState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.circuit.home.PowerCircuitHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

public class ItemStateFloorPDU implements ItemState {


	@Autowired(required = true)
	private ItemDAO itemDAO;
	
	@Autowired(required = true)
	private PowerCircuitHome powerCircuitHome;

	@Autowired(required = true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	private ItemState itemStateCommon;

	private ItemState itemStatePowerPanel;
	
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

	public ItemState getItemStatePowerPanel() {
		return itemStatePowerPanel;
	}

	public void setItemStatePowerPanel(ItemState itemStatePowerPanel) {
		this.itemStatePowerPanel = itemStatePowerPanel;
	}


	
	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		if (item.getSubclassLookup() == null) {
			onSaveFloorPDU(item);
		}
		else {
			itemStatePowerPanel.onSave(item);
		}
	}
	
	public void onSaveFloorPDU(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		
		// get the panels associated with the Floor PDU
		List<Long> panels = itemDAO.getChildItemIds(item.getItemId());
		List<Long> panelsViaPorts = itemDAO.getPanelItemIdsToDelete(item.getItemId());
		for (Long panelItemId: panelsViaPorts) {
			if (!panels.contains(panelItemId)) {
				panels.add(panelItemId);
			}
		}

		
		if (null != panels && panels.size() > 0) {
			
			// get the panels' power outlets associated with the Floor PDU
			List<Long> powerOutlets = itemDAO.getPowerPanelConnectedPowerOutlet(panels);
			
			if (null != powerOutlets && powerOutlets.size() > 0) {
				
				itemDAO.clearPowerOutletAssociationWithPanel(powerOutlets);
				
			}

			// clear the placement of all panels
			itemDAO.clearPanelPlacement(panels);
		
			// set the state of all panels to archive
			List<LksData> statusLks = systemLookupFinderDAO.findByLkpValueCode(itemState);
			itemDAO.setItemState(panels, statusLks.get(0).getLksId());
			
		}
		
		// Set FPDU port state
		// updatePortStatus(item);

		cleanupFloorPDUConnections(item);
		
		if (null != panels && panels.size() > 0) {
			powerCircuitHome.deleteItemsPowerDestConnections(panels);			
		}
		
		// clear the placement of all power outlets
		// itemDAO.clearPowerOutletPlacement(powerOutlets);
		
		// set the power outlets' state to archive
		// itemDAO.setItemState(powerOutlets, itemState);

		// perform common operation on archived / storage on save
		if (null != itemStateCommon) {
			itemStateCommon.onSave(item);
		}
		
		// clear FPDU specific placement information
		clearFloorPDUPlacement(item);
		
	}


	private void cleanupFloorPDUConnections(Item item) {
		if (item instanceof MeItem && item.getClassLookup() != null && 
				item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU) ) {

			((MeItem) item).setBreakerPortId(null);
			((MeItem) item).setUpsBankItem(null);
			
			// delete all the connections from the panel breaker to the Floor PDU breaker
			Set<PowerPort> ports = item.getPowerPorts();
			if (null != ports) {
				for (PowerPort port: ports) {
					port.setUsed(false);
					port.getDestPowerConnections().clear();
					port.getSourcePowerConnections().clear();
					
				}
			}
		}
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
				}
				port.setPortStatusLookup(systemLookupFinderDAO.findByLkpValueCode(itemState).get(0));
			}
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
		// Do not validate parent-child constraint for Floor PDU. For the floor pdu, the panels (children) must be archived with the it.
		
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo userSession,
			Errors errors) {
		itemStateCommon.validateAllButReqFields(target, userSession, errors);
		
	}
	
	/**
	 * Clear all FloorPDU placement.
	 * <p><b>NOTE:</b> Once the placement interface 
	 * is ready (3.1 release), this logic can be moved to the impl. of that interface</p>
	 * @param item
	 */
	private void clearFloorPDUPlacement(Item item){
		if (item instanceof MeItem && item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_PDU)){
			MeItem meItem = (MeItem)item;
			meItem.setFacingLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.FrontFaces.NORTH).get(0));
		}
	}
	


}
