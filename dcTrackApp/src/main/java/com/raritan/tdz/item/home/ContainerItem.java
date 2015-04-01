package com.raritan.tdz.item.home;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.json.BasicItemInfo;

public abstract class ContainerItem {
		
    @Autowired(required=true)
    private ItemDTOAdapter itemDTOAdapter;

	
	abstract public List<Item> getDomainItemsInContainer( Item container, boolean includeGrandchildren );

	public BasicItemInfo convertDomainItemToBasicItemInfo( Item domainItem  ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DataAccessException{
		BasicItemInfo bi = itemDTOAdapter.convertDomainItemToBasicItemInfo(domainItem);		
		return bi;
	}

	public List<BasicItemInfo> convertDomainItemsListToBasicItemsList( List<Item> domainItems  ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, DataAccessException{
		List<BasicItemInfo> retVal = new ArrayList<BasicItemInfo>();
		for( Item i : domainItems ){
			BasicItemInfo  basicItemInfo = convertDomainItemToBasicItemInfo(i);
			if( basicItemInfo != null ){
				retVal.add(basicItemInfo);
			}
		}
		return retVal;
	}

	public List<BasicItemInfo> getItemsInContainerInfo( Item container, boolean includeGrandchildren ) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, DataAccessException{
		
		List<Item> items = getDomainItemsInContainer( container, includeGrandchildren );
		
		List<BasicItemInfo> retval = convertDomainItemsListToBasicItemsList( items );
		
		if ( retval == null || retval.size() == 0 ) return retval;
	
		Collections.sort(retval);
		return retval;
	}

}
