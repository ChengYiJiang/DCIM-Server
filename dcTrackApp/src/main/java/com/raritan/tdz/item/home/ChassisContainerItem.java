package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.json.BasicBladeItemInfo;
import com.raritan.tdz.item.json.BasicItemInfo;

public class ChassisContainerItem extends ContainerItem {

	@Autowired(required=true)
	ItemFinderDAO itemFinderDAO;

	@Override
	public List<Item> getDomainItemsInContainer( Item container, boolean includeGrandchildren ){
		//includeGrandchildren - not used
		Long containerItemId = container.getItemId();
		List<Item> items = itemFinderDAO.findAllBladesInChassisSorted(containerItemId);
	
		return items;	
	}

}
