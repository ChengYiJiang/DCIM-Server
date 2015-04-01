package com.raritan.tdz.vpc.factory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.location.dao.LocationDAO;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * update common across all VPC items
 * @author bunty
 *
 */
public class VPCItemUpdateCommon implements VPCItemUpdate {

	@Autowired
	private LksCache lksCache;
	
	@Autowired
	private LocationDAO locationDAO;
	
	@Override
	public void update(Item item, Map<String, Object> additionalParams) {

		// set item details
		ItemServiceDetails itemServiceDetails = new ItemServiceDetails();
		itemServiceDetails.setOriginLookup(lksCache.getLksDataUsingLkpCode(SystemLookup.ItemOrigen.VPC));
		item.setItemServiceDetails(itemServiceDetails);

		// set location id
		Long locationId = (Long) additionalParams.get(VPCLookup.ParamsKey.LOCATIONID);
		item.setDataCenterLocation(locationDAO.read(locationId));
		
		// set item name
		String path = (String) additionalParams.get(VPCLookup.ParamsKey.PATH);
		String name = "VPC-" + item.getClassLookup().getLkpValue() + "-" + path;
		item.setItemName(name);

		// set the path information in the Alias for now, we need to create a new column in me item and put the path in there
		item.setItemAlias(path);
		
		MeItem meItem = (MeItem) item;
		meItem.setChainLabel(path);
		
	}

}
