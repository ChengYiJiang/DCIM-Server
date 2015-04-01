package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;

public class CabinetContainerItem extends ContainerItem {

	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	/**
	 * We send items in cabinet sorted by UPosition.
	 * We send also blades that are not assigned to any chassis yet, but are in Planned state
	 * and belong to the Cabinet
	 * 
	 * If includeGrandchildren is true, we send also all blades that are placed inside a chassis 
	 * that belongs to the cabinet. Otherwise, if this falsg is false we send only chassis, but
	 * not blades belonging to the chassis.
	 */
	@Override
	public List<Item> getDomainItemsInContainer( Item container, boolean includeGrandchildren ){
		
		List<Item> items = null;
		if( includeGrandchildren ){
			items = itemDAO.getCabinetChildrenSorted(container.getItemId());
		}else{
			items = itemDAO.getCabinetChildrenWithoutBladesSorted(container.getItemId());
		}
		return items;
	}

}
