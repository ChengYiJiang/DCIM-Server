package com.raritan.tdz.item.home.placement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.raritan.tdz.cabinet.home.CabinetHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * Item Placement Home implementation.
 * 
 * @author Andrew Cohen
 */
public class ItemPlacementHomeImpl implements ItemPlacementHome {

	private ItemHome itemHome;
	private CabinetHome cabinetHome;
	
	public ItemPlacementHomeImpl() {
	}
	
	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	
	public void setCabinetHome(CabinetHome cabinetHome) {
		this.cabinetHome = cabinetHome;
	}

	@Override
	public Collection<Long> getAvailablePositions( Item item, Item expItem ) throws DataAccessException {
		
		if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.CABINET)
			return getCabinetAvailablePositionsInRow( (CabinetItem)item ); 
		
		if (item.getClassLookup().getLkpValueCode() == SystemLookup.Class.FLOOR_OUTLET) {
			return getPowerOutletAvailableUPositions( item );
		}
		
		final ModelDetails model = item.getModel();
		if (model == null) return Collections.emptyList();
		final String mounting = model.getMounting() != null ? model.getMounting().toLowerCase() : null;
		
		if (mounting == null) return new LinkedList<Long>();
		
		if (mounting.contains("free-standing")) {
			return getFreeStandingAvailableUPositions(item, model);
		}
		
		if (mounting.contains("blade")) {
			// TODO: For blade items, should fix to get available slot positions for this blade in the current chassis
			return new LinkedList<Long>();
		}
		
		if (mounting.contains("zerou")) {
			return getAvailableZeroUPositions(item, expItem, model);
		}
		
		if (mounting.contains("non-rackable")) {
			return getNonRackableItemAvailableUPositions(item, expItem, model);
		}
		
		if (mounting.contains("rackable")) {
			return getRackableItemAvailableUPositions(item, expItem, model);
		}
		
		return new LinkedList<Long>();
	}
	
	//
	// Private methods
	//
	

	//Free Standing devices can have only UPosition 1 as available
	private Collection<Long> getFreeStandingAvailableUPositions(Item item,
			ModelDetails model) {
		List<Long> ret = new ArrayList<Long>(1);
		ret.add(1L);
		return ret;
	}
	
	private List<Long> getPowerOutletAvailableUPositions( Item item ) {
		List<Long> uPositions = new LinkedList<Long>();
		uPositions.add(-1L); // Above Cabinet
		uPositions.add(-2L); // Below Cabinet
		return uPositions;
	}
	
	private List<Long> getCabinetAvailablePositionsInRow(CabinetItem cabinet) {
		final String rowLabel = cabinet.getRowLabel();
		if (rowLabel == null) return new LinkedList<Long>();
		
		List<Integer> rowPositions = cabinetHome.getCabinetPositionInRows(
				cabinet.getDataCenterLocation().getDataCenterLocationId(),
				rowLabel
		);
		
		List<Long> ret = new ArrayList<Long>( rowPositions.size() );
		for (Integer i : rowPositions) {
			ret.add( i.longValue() );
		}
		
		return ret;
	}
	
	private Collection<Long> getRackableItemAvailableUPositions(Item item, Item expItem, ModelDetails model) throws DataAccessException {
		final Item cabinet = item.getParentItem();
		if (cabinet == null || model == null) return new LinkedList<Long>();
		LksData mountingLks = item.getMountedRailLookup();
		Long mountingRailsLkupValueCode = SystemLookup.RailsUsed.BOTH; /* when mounting rails not specified for an item use default */ 
		if (mountingLks != null) { 
			mountingRailsLkupValueCode = mountingLks.getLkpValueCode();
		}
		// if the item is the -when moved item and a new item, then get the original item and pass that information.
		Long itemId = item.getItemId();
		if (null != item.getItemToMoveId() && item.getItemToMoveId() > 0 && item.getItemId() <= 0) {
			itemId = item.getItemToMoveId();
		}

		return itemHome.getAvailableUPositions(
				cabinet.getItemId(),
				model.getRuHeight(), 
				itemId, 
				mountingRailsLkupValueCode, item.getReservationId()
		);
	}
	
	private Collection<Long> getAvailableZeroUPositions(Item zeroUItem, Item exceptionItem, ModelDetails model) throws DataAccessException {
		final Item cabinet = zeroUItem.getParentItem();
		if (cabinet == null || model == null) return new LinkedList<Long>();
		// final long uPos = zeroUItem.getUPosition();
		final long depthPositionLkpValueCode = zeroUItem.getFacingLookup() != null ? zeroUItem.getFacingLookup().getLkpValueCode() : 0;
		final long cabinetSideLkpValueCode = zeroUItem.getMountedRailLookup() != null ? zeroUItem.getMountedRailLookup().getLkpValueCode() : 0;
		
		return itemHome.getAvailableZeroUPositions(
				cabinet.getItemId(),
				depthPositionLkpValueCode,
				cabinetSideLkpValueCode,
				model.getRuHeight(),
				(null != exceptionItem) ? exceptionItem.getItemId() : null
		);
	}
	
	
	private Collection<Long> getNonRackableItemAvailableUPositions(Item item, Item expItem, ModelDetails model) throws DataAccessException {
		final Item cabinet = item.getParentItem();
		Long railsUsed = (null != item.getMountedRailLookup()) ? item.getMountedRailLookup().getLkpValueCode() : SystemLookup.RailsUsed.FRONT;
		if (cabinet == null || model == null) return new LinkedList<Long>();
		return itemHome.getNonRackableUPosition(cabinet.getItemId(), model.getModelDetailId(), railsUsed, (null != expItem) ? expItem.getItemId() : -1);
	}

}
