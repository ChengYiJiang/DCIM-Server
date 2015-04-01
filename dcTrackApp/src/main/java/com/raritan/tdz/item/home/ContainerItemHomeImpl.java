package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.json.BasicItemInfo;
import com.raritan.tdz.lookup.SystemLookup;




public class ContainerItemHomeImpl implements ContainerItemHome {

	private Map<Long, ContainerItem> supportedContainers;


	@Autowired(required=true)
	ItemFinderDAO itemFinderDAO;

	private final Logger log = Logger.getLogger(ContainerItemHomeImpl.class);

	public Map<Long, ContainerItem> getSupportedContainers() {
		return supportedContainers;
	}

	public void setSupportedContainers(Map<Long, ContainerItem> supportedContainers) {
		this.supportedContainers = supportedContainers;
	}

	@Override
	public List<Object> getAllItemsInContainer(Item containerItem, boolean includeContainer, boolean includeGrandchildren ) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, DataAccessException {
		List<Object> retVal = new ArrayList<Object>();

		if( containerItem != null ){
			Long containerId = containerItem.getClassMountingFormFactorValue();

			ContainerItem contItem =  supportedContainers.get(containerId);
			if( contItem != null ){
				//add container
				if( includeContainer ){
					BasicItemInfo containerInfo = contItem.convertDomainItemToBasicItemInfo(containerItem);
					if(containerInfo != null ){	
						retVal.add(containerInfo);
					}
				}
				
				//add children
				List<BasicItemInfo> children =  contItem.getItemsInContainerInfo(containerItem, includeGrandchildren);
				if( children != null && children.size() > 0 ){
					retVal.addAll(children);
				}
			}else log.error("NOT supported container item: " + containerItem.getItemId() + ", " + containerId);
		}else log.error("containerItem is null");

		return retVal;
	}

}
