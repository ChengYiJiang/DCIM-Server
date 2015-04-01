package com.raritan.tdz.port.home;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.item.home.SavedItemData;

public class PortCollectionHelperImpl<T> implements PortCollectionHelper<T> {
	
	
	public PortCollectionHelperImpl(Class<T> type) {
		super();
		this.type = type;
	}

	protected Class<T> type;

	public Class<T> getType() {
		return type;
	}

	public void setType(Class<T> type) {
		this.type = type;
	}

	private Object getValue(Object port, String methodName) {
		Object value = null;
		try {
			value = PropertyUtils.getProperty(port, methodName);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + methodName + ": Internal Error");
		}
		return value;
	}

	private void setValue(Object port, String methodName, Object value) {
		try {
			PropertyUtils.setProperty(port, methodName, value);
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot find getter for " + methodName + ": Internal Error");
		}
	}

	// Will invoke getPowerPorts()/getDataPorts()/getSensorPorts() on Item
	@Override
	public Set<T> getPortList(Item item) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		String methodName = type.getSimpleName() + "s";
		methodName = Character.toLowerCase(
				methodName.charAt(0)) + (methodName.length() > 1 ? methodName.substring(1) : "");
		
		@SuppressWarnings("unchecked")
		Set<T> ports = (Set<T>) getValue(item, methodName);
		return ports; 
	}

	private void removePort(Item item, T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		String methodName = "remove" + type.getSimpleName();
		@SuppressWarnings("rawtypes")
		Class cls = Class.forName("com.raritan.tdz.domain.Item");
		
		// port parameter
		@SuppressWarnings("rawtypes")
		Class[] paramPort = new Class[1];	
		paramPort[0] = getType();
		@SuppressWarnings("unchecked")
		Method m = cls.getDeclaredMethod(methodName, paramPort);
		
		Object[] params = new Object[]{port};
		
		@SuppressWarnings({ "unused" })
		Object object = m.invoke(item, params);
	}

	private Long getPortId(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "portId";
		return (Long) getValue(port, methodName);
	}
	
	private Long getPortSubClassLkpValueCode(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "portSubClassLookup";
		LksData lksPortSubClass = (LksData) getValue(port, methodName);
		return (null != lksPortSubClass) ? lksPortSubClass.getLkpValueCode() : null;
		
	}

	private String getPortSubClassLkpValue(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "portSubClassLookup";
		LksData lksPortSubClass = (LksData) getValue(port, methodName);
		return (null != lksPortSubClass) ? lksPortSubClass.getLkpValue() : null;
		
	}

	private int getSortOrder(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "sortOrder";
		return ((Integer) getValue(port, methodName)).intValue();
	}
	
	private void setSortOrder(T port, int value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "sortOrder";
		setValue(port, methodName, value);
		
	}
	
	@Override
	public Set<IPortObject> init(Object itemObj, IPortObjectFactory portObjectFactory, Errors errors) {
		Item item = (Item) itemObj;
		try {
			Set<T> portObjs = getPortList(item);
			Set<IPortObject> ports = new HashSet<IPortObject>();
			if (null == portObjs) {
				return ports;
			}
			for (T port: portObjs) {
				ports.add(portObjectFactory.getPortObject((IPortInfo) port, errors));
			}
			return ports;
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot initialize the ports: Internal Error");
		}
	}

	@Override
	public List<Long> getDeleteIds(Item item) {
		// get deleted power port ids
		List<Long> delPortIds = new ArrayList<Long>();
		Item origItem = (null != SavedItemData.getCurrentItem()) ? SavedItemData.getCurrentItem().getSavedItem() : null;
		if (null == origItem) {
			return delPortIds;
		}
		try {
			Set<T> dbDataPorts = getPortList(origItem);
			Set<T> dataPorts = getPortList(item);
			List<Long> portIds = new ArrayList<Long>();
			if (null != dataPorts) {
				for (T pp: dataPorts) {
					portIds.add(getPortId(pp));
				}
			}
			if (null != dbDataPorts) {
				for (T dp: dbDataPorts) {
					if (!portIds.contains(getPortId(dp))) {
						delPortIds.add(getPortId(dp).longValue());
					}
				}
			}
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot determine the delete port list: Internal Error");
		}
		return delPortIds;
	}

	@Override
	public void updateSortOrderByPortSubclass(Item item, Errors errors) {
		try {
			Item savedItem = item;
			Set<T> savedPorts = getPortList(savedItem);
			if (null == savedPorts) {
				return;
			}
			Map<Long, Integer> lastSortOderMap = new HashMap<Long, Integer>();
			Map<Long, Map<Integer, List<T>>> dupSortOrderBySubClass = new HashMap<Long, Map<Integer,List<T>>>();
			Map<Long, List<Integer>> indexUsed = new HashMap<Long, List<Integer>>();
			
			for (T dp: savedPorts) {
				Long key = getPortSubClassLkpValueCode(dp); 
				int sortOrder = getSortOrder(dp);
				if (null == lastSortOderMap.get(key)) {
					lastSortOderMap.put(key, new Integer(0));
					
				}
				
				// Create a new list to store all occupied indexes for the given port subclass
				if (null == indexUsed.get(key)) {
					indexUsed.put(key, new ArrayList<Integer>());
				}
				
				if (sortOrder > lastSortOderMap.get(key).intValue()) {
					lastSortOderMap.put(key, sortOrder);
				}
				
				if (sortOrder > 0) {
					
					// Add used indexes to the list 
					indexUsed.get(key).add(sortOrder);
					
					if (null == dupSortOrderBySubClass.get(key)) {
						Map<Integer, List<T>> dupSortOrder = new HashMap<Integer, List<T>>();
						List<T> portList = new ArrayList<T>();
						portList.add(dp);
						dupSortOrder.put(sortOrder, portList);
						dupSortOrderBySubClass.put(key, dupSortOrder);
					}
					else {
						
						if (null == dupSortOrderBySubClass.get(key).get(sortOrder)) {
							List<T> portList = new ArrayList<T>();
							portList.add(dp);
							dupSortOrderBySubClass.get(key).put(sortOrder, portList);
						}
						else {
							
							dupSortOrderBySubClass.get(key).get(sortOrder).add(dp);
						}
					}
				}
			}
			
			// Sort and remove duplicates in the occupied index list
			for (Entry<Long, List<Integer>> entry: indexUsed.entrySet()) {
				List<Integer> indexList = entry.getValue();
				Collections.sort(indexList);
				Set<Integer> unDupIndexList = new LinkedHashSet<Integer>(indexList);
				indexList.clear();
				indexList.addAll(unDupIndexList);
			}
			
			// correct the invalid index
			for (T dp: savedPorts) {
				int sortOrder = getSortOrder(dp);
				if (sortOrder <= 0) {
					Long key = getPortSubClassLkpValueCode(dp);
					Integer newSortOrder = findNextAvailableIndex(indexUsed.get(key));
					setSortOrder(dp, newSortOrder.intValue());
					indexUsed.get(key).add(newSortOrder);
					Collections.sort(indexUsed.get(key));
					
					// add information about the index change to the user here
					String portName = (String) getValue(dp, "portName");
					Object[] errorArgs = { portName, sortOrder, newSortOrder };
					errors.rejectValue("sort_order", "PortValidator.invalidIndex", errorArgs, "Resolved invalid index for port " + portName + " from " + sortOrder + " to " + newSortOrder + ".");
				}
			}
			
			// correct the duplicate index by port subclass
			for (Entry<Long, Map<Integer, List<T>>> entryBySubClass: dupSortOrderBySubClass.entrySet()) {
				Map<Integer, List<T>> duplicateSortOrder = entryBySubClass.getValue();
				for (Map.Entry<Integer, List<T>> entry: duplicateSortOrder.entrySet()) {
					List<T> dupPorts = entry.getValue();
					int numOfDup = dupPorts.size(); 
					if (numOfDup > 1) {
						StringBuffer portNames = new StringBuffer();
						for (T port: dupPorts) {
							portNames.append(getValue(port, "portName"));
							if (numOfDup == 1) {
								break;
							}
							portNames.append(", ");
							Long key = getPortSubClassLkpValueCode(port);
							Integer newSortOrder = findNextAvailableIndex(indexUsed.get(key));
							setSortOrder(port, newSortOrder.intValue());
							indexUsed.get(key).add(newSortOrder);
							Collections.sort(indexUsed.get(key));
							numOfDup--;
							/*if (numOfDup == 1) {
								break;
							}*/
						}
						
						Object[] errorArgs = { portNames.toString() };
						errors.rejectValue("sort_order", "PortValidator.duplicateIndex", errorArgs, "Resolved duplicate indexes between ports " + portNames.toString());
					}
				}
			}

			// correct the invalid index
			/*for (T dp: savedPorts) {
				if (getSortOrder(dp) <= 0) {
					Long key = getPortSubClassLkpValueCode(dp);
					Integer newSortOrder = lastSortOderMap.get(key);
					if (null == newSortOrder) {
						lastSortOderMap.put(key, new Integer(0));
						newSortOrder = lastSortOderMap.get(key);
					}
					newSortOrder++;
					setSortOrder(dp, newSortOrder.intValue());
					lastSortOderMap.put(key, newSortOrder);
				}
			}*/

			// correct the duplicate index by port subclass
			/*for (Entry<Long, Map<Integer, List<T>>> entryBySubClass: dupSortOrderBySubClass.entrySet()) {
				Map<Integer, List<T>> duplicateSortOrder = entryBySubClass.getValue();
				for (Map.Entry<Integer, List<T>> entry: duplicateSortOrder.entrySet()) {
					List<T> dupPorts = entry.getValue();
					int numOfDup = dupPorts.size(); 
					if (numOfDup > 1) {
						for (T port: dupPorts) {
							Long key = getPortSubClassLkpValueCode(port);
							Integer newSortOrder = lastSortOderMap.get(key);
							if (null == newSortOrder) {
								lastSortOderMap.put(key, new Integer(0));
								newSortOrder = lastSortOderMap.get(key);
							}
							newSortOrder++;
							setSortOrder(port, newSortOrder.intValue());
							lastSortOderMap.put(key, newSortOrder);
							numOfDup--;
							if (numOfDup == 1) {
								break;
							}
						}
					}
				}
			}*/
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot set the sort order: Internal Error");
		}
	}
	
	private Integer findNextAvailableIndex(List<Integer> indexList) {
		
		Integer expected = 1;
		
		for (Integer index: indexList) {
			if (!index.equals(expected))
				break;
			expected++;
		}
		
		return expected;
	}

	@Override
	public List<String> getPortTypeOfNonUniqueSortOrder(Item item) {
		try {
			Set<T> ports = getPortList(item);
			ArrayList<String> portTypeNotUnique = new ArrayList<String>();
			if (null == ports) {
				return portTypeNotUnique;
			}
			
			Map<Long, ArrayList<Long>> sortOderMap = new HashMap<Long, ArrayList<Long>>();
			ArrayList<Long> processPortType = new ArrayList<Long>();
	
			for (T port: ports) {
				Long key = getPortSubClassLkpValueCode(port);
				
				if (getSortOrder(port) <= 0) {
					portTypeNotUnique.add( getPortSubClassLkpValue(port) );
					continue;
				}
				
				if (null == sortOderMap.get(key)) {
					ArrayList<Long> sortOrderList = new ArrayList<Long>();
					sortOrderList.add(new Long(getSortOrder(port)));
					sortOderMap.put(key, sortOrderList);
				}
				else {
					if (!processPortType.contains(key) && sortOderMap.get(key).contains(new Long(getSortOrder(port)))) {
						processPortType.add(key);
						portTypeNotUnique.add( getPortSubClassLkpValue(port) );
					}
					else {
						sortOderMap.get(key).add(new Long(getSortOrder(port)));
					}
				}
			}
			return portTypeNotUnique;
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot validate the sort order: Internal Error");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deleteItemPort(Item item, IPortInfo portInfo) {
		try {
			if (portInfo.getPortId() == null || portInfo.getPortId() <= 0) {
				return;
			}
			removePort(item, ((T)portInfo));
			
			if (portInfo.getPortId().longValue() > 0) {
				if (null == getPortList(item)) {
					return;
				}
				Iterator<T> i = getPortList(item).iterator();
				while(i.hasNext()){
				    T port = i.next();
				    if (getPortId(port).longValue() == getPortId((T)portInfo).longValue()) {
				    	i.remove();
				    }
				}
			}
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot delete port: Internal Error");
		}
	}

	@Override
	public T getPort(Item item, Long portId) {
		if (null == item || null == portId || portId.longValue() <= 0) {
			return null;
		}
		try {
			Set<T> ports = getPortList(item);
			if (null == ports) {
				return null;
			}
			for (T port: ports) {
				if (portId == getPortId(port).longValue()) {
					return port;
				}
			}
		}
		catch (Exception e) {
			throw new InvalidPortObjectException("Cannot delete port: Internal Error");
		}
		
		return null;
	}

	
}
