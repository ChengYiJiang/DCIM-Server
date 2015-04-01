package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;

public interface PortCollectionHelper<T> {

	/**
	 * Get the list of ports ids getting deleted
	 * @param item
	 * @param portType
	 * @return
	 */
	public List<Long> getDeleteIds(Item item);
	
	/**
	 * Updates the sort order of the ports irrespective for a given port subclass
	 * @param item TODO
	 * @param errors TODO
	 * @param itemDAO TODO
	 */
	public void updateSortOrderByPortSubclass(Item item, Errors errors);
	
	/**
	 * Validates the sort order for a given port subclass
	 * @param itemObj
	 */
	public List<String> getPortTypeOfNonUniqueSortOrder(Item item);
	
	/**
	 * Initializes the port collection
	 * @param itemObj
	 * @param portObjectFactory
	 * @return
	 */
	public Set<IPortObject> init(Object itemObj, IPortObjectFactory portObjectFactory, Errors errors);

	/**
	 * delete the port from the item's port set
	 * @param portInfo
	 * @param item
	 */
	public void deleteItemPort(Item item, IPortInfo portInfo);

	/**
	 * 
	 * @param item
	 * @param portId
	 * @return
	 */
	public T getPort(Item item, Long portId);
	
	/**
	 * Get the list of ports given an item
	 * @param item
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	public Set<T> getPortList(Item item) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, 
			IllegalAccessException, InvocationTargetException, ClassNotFoundException;
}
