package com.raritan.tdz.item.itemState;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

public class ItemStatePowerOutlet implements ItemState {

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;
	
	private ItemState baseState;
	
	public ItemStatePowerOutlet(ItemState baseState){
		this.baseState = baseState;
	}
	
	public ItemState getState() {
		return baseState;
	}

	public void setState(ItemState state) {
		this.baseState = state;
	}
	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		
		baseState.onSave(item);
		
		Set<PowerPort> ports = item.getPowerPorts();
		
		if (null != ports) {
			for (PowerPort p : ports) {
				if (p == null) continue;
				Set<PowerConnection> connections = p.getSourcePowerConnections();
				for (PowerConnection c : connections) {
					if (c == null) continue;
					IPortInfo destPort = c.getDestPort();
					IPortInfo srcPort = c.getSourcePort();
					if (destPort != null && srcPort != null) { 
						powerPortDAO.changeUsedFlag(c.getDestPort().getPortId(), c.getSourcePort().getPortId());
					}
				}
				// clear port fields (address, breakerPort, busway)
				connections.clear();
				p.setAddress(null);
				p.setBreakerPort(null);
				p.setBuswayItem(null);
			}
		}
		
		MeItem meItem = (MeItem) itemDAO.initializeAndUnproxy(item);
		if (meItem != null){
			// clear upsBank and panel for this outlet
			meItem.setUpsBankItem(null);
			meItem.setPduPanelItem(null);

			// clear placement
			clearPowerOutletPlacement(item);
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return baseState.supports(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		baseState.validate(target, errors);
		
	}

	@Override
	public RulesNodeEditability getEditability(Item item) {
		return baseState.getEditability(item);
	}

	@Override
	public boolean canTransition(Item item) {
		return baseState.canTransition(item);
	}

	@Override
	public List<Long> getAllowableStates() {
		return baseState.getAllowableStates();
	}

	@Override
	public boolean isTransitionPermittedForUser(Item item, Long newState,
			UserInfo userInfo) {
		return baseState.isTransitionPermittedForUser(item, newState, userInfo);
	}

	@Override
	public void validateMandatoryFields(Item item, Errors errors,
			Long newStatusLkpValueCode) throws DataAccessException,
			ClassNotFoundException {
		baseState.validateMandatoryFields(item, errors, newStatusLkpValueCode);
		
	}

	@Override
	public void validateParentChildConstraint(Item item, Errors errors,
			Long newStatusLkpValueCode, String errorCodePrefix)
			throws DataAccessException, ClassNotFoundException {
		baseState.validateParentChildConstraint(item, errors, newStatusLkpValueCode, errorCodePrefix);
		
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo userSession,
			Errors errors) {
		baseState.validateAllButReqFields(target, userSession, errors);
		
	}
	
	protected void clearPowerOutletPlacement(Item item) {
		if (item instanceof MeItem && item.getClassLookup() != null && item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.FLOOR_OUTLET)) {
			MeItem meItem = (MeItem)item;
			meItem.setParentItem(null);
			meItem.setPduPanelItem(null);
			meItem.setUpsBankItem(null);
		}
	}
	
}
