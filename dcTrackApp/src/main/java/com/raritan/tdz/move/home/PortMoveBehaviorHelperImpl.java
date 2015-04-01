package com.raritan.tdz.move.home;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.IPortMoveInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.home.InvalidPortObjectException;

/**
 * 
 * @author bunty
 *
 * @param <T>
 */
public class PortMoveBehaviorHelperImpl<T> implements PortMoveBehaviorHelper<T> {

	
	public PortMoveBehaviorHelperImpl(Class<T> type, PortMoveDAO<Serializable> portMoveDAO, String tableName) {
		super();
		this.type = type;
		this.portMoveDAO = portMoveDAO;
		this.tableName = tableName;
	}

	@Autowired(required=true)
	private ItemDAO itemDAO;

	protected Class<T> type;
	
	private String tableName;
	
	private PortMoveDAO<Serializable> portMoveDAO;
	
	@Autowired(required=true)
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired(required=true)
	private LksCache lksCache;
	
	Map<T, T> matchingPorts = null;

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
	
	public Set<T> getPortList(Item item) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		
		String methodName = type.getSimpleName() + "s";
		methodName = Character.toLowerCase(
				methodName.charAt(0)) + (methodName.length() > 1 ? methodName.substring(1) : "");
		
		@SuppressWarnings("unchecked")
		Set<T> ports = (Set<T>) getValue(item, methodName);
		return ports; 

	}
	
	private Long getPortId(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "portId";
		return (Long) getValue(port, methodName);
	}

	private String getPortName(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "portName";
		return (String) getValue(port, methodName);
	}
	
	private Long getPortMoveAction(T port) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "moveActionLkpValueCode";
		return (Long) getValue(port, methodName);
	}
	
	private void setPortMoveAction(T port, Long moveActionLkpValueCode) {
		String methodName = "moveActionLkpValueCode";
		setValue(port, methodName, moveActionLkpValueCode);
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * creates the map of matching port against the when move ports
	 * update the move action on the original port
	 * @param origItem
	 * @param moveItem
	 * @param evictedMoveItem
	 * @param errors
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	private boolean validateAndCreateMatchingPortMap(Item origItem, Item moveItem, Item evictedMoveItem, Errors errors) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		matchingPorts = new HashMap<T, T>();
		
		Set<T> whenMovedPorts = getPortList(moveItem);
		Set<T> origPorts = getPortList(origItem);
		
		if (null == whenMovedPorts && null == origPorts) return true;
		
		// Generate business validation exception if port count do not match		
		if ((null == whenMovedPorts && (null != origPorts && origPorts.size() > 0)) || 
				(null == origPorts && (null != whenMovedPorts && whenMovedPorts.size() > 0)) || 
				(null != whenMovedPorts && null != origPorts && whenMovedPorts.size() != origPorts.size()) ) {
			
			Object[] errorArgs = { };
			errors.rejectValue("item", "ItemMoveValidator.PortCountDoNotMatch", errorArgs, "Port count do not match.");
			
			return false;
		}
		
		if (null == whenMovedPorts || null == origPorts) {
			return true;
		}
		
		Set<T> whenMovedAdaptedPorts = getPortList(evictedMoveItem);
		
		for (T whenMovePort: whenMovedPorts) { 
			for (T origPort: origPorts) {
				String whenMovedPortName = getPortName(whenMovePort);
				String origPortName = getPortName(origPort);
				if (whenMovedPortName.equals(origPortName)) {
					matchingPorts.put(whenMovePort, origPort);
					updateMoveActionValueCodeOnOrigPort(whenMovedAdaptedPorts, origPort);
				}
			}
		}
		
		if (matchingPorts.size() != whenMovedPorts.size()) {
			
			Object[] errorArgs = { };
			errors.rejectValue("item", "ItemMoveValidator.PortNameDoNotMatch", errorArgs, "The name(s) of original port do not match.");
		
			return false;
		}
		
		return true;
	}
	
	/**
	 * item is got using the dao call and therefore all transient values are lost including the move action
	 * The evicted item still has move actions against the port, therefore the original port saves the move actions 
	 * and it is then used to update the move action on the persistent port
	 * @param whenMovedAdaptedPorts
	 * @param origPort
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void updateMoveActionValueCodeOnOrigPort(
			Set<T> whenMovedAdaptedPorts, T origPort) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		for (T whenMovePort: whenMovedAdaptedPorts) { 
				String whenMovedPortName = getPortName(whenMovePort);
				String origPortName = getPortName(origPort);
				if (whenMovedPortName.equals(origPortName)) {
					Long moveActionLkpValueCode = getPortMoveAction(whenMovePort);
					setPortMoveAction(origPort, moveActionLkpValueCode);
				}
		}
		
	}

	private T getMatchingOrigPort(T whenMovedPort) {
		
		return matchingPorts.get(whenMovedPort);
		
	}
	

	@Override
	public void postSave(Item item, UserInfo sessionUser,
			Object... additionalArgs) throws BusinessValidationException,
			DataAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
		
		if (null == item.getItemToMoveId() || item.getItemToMoveId() <= 0) { //do nothing
			return;
		}
		
		Errors errors = (Errors) additionalArgs[0];
		
		Long whenMovedItemId = item.getItemId();
		
		Item whenMovedItem = itemDAO.getItem(whenMovedItemId);
		
		Long origMovedItemId = item.getItemToMoveId();
		
		Set<T> whenMovedPorts = getPortList(whenMovedItem);
		
		Item origItem = itemDAO.getItem(origMovedItemId);
		
		if (!validateAndCreateMatchingPortMap(origItem, whenMovedItem, item, errors)) return; 
		
		// TODO:: throw business validation exception when mismatch in the number of ports in the original and when moved item.
		// ItemMoveValidator.PortCountDoNotMatch
		
		// Generate the item's entry in the move table
		IPortMoveInfo moveData = (IPortMoveInfo) portMoveDAO.getPortMoveData(whenMovedItemId, origMovedItemId, null);
		if (null == moveData) {
			Request request = itemRequestDAO.getLatestRequest(origMovedItemId);
			
			portMoveDAO.createPortMoveData(origItem, whenMovedItem, null, null, null, request);
			
		}
		
		if (null == whenMovedPorts) {
			return;
		}
		
		// Generate move data for the ports
		for (T whenMovedPort: whenMovedPorts) {
			moveData = (IPortMoveInfo) portMoveDAO.getPortMoveData(whenMovedItemId, origMovedItemId, getPortId(whenMovedPort));
			
			if (null != moveData) continue;
			
			T origPort =  getMatchingOrigPort(whenMovedPort);
			
			Long portMoveAction = getPortMoveAction(origPort);
			LksData action = (null != portMoveAction) ? lksCache.getLksDataUsingLkpCode(portMoveAction) : null;

			// FIXME:: generate the request for individual ports here and separate creation of request for ports in the requestDAO
			
			// get the generated request for the port. May be generate the port request at this point and save to the moveData.
			Request request = itemRequestDAO.getLatestPortRequest(getPortId(origPort), tableName);
			if (null == action) {
				action = getMatchingMoveAction(request);
			}
			
			portMoveDAO.createPortMoveData(origItem, whenMovedItem, (IPortInfo) origPort, (IPortInfo) whenMovedPort, action, request);

		}

	}


	@SuppressWarnings("serial")
	Map<String, String> moveActionMap = 
			Collections.unmodifiableMap(new HashMap<String, String>() {{
				put("Disconnect", SystemLookup.MoveActionLkpValue.DontConnect);
				put("Disconnect       and Move", SystemLookup.MoveActionLkpValue.DontConnect);
				// TODO:: Fill me...
				put("", SystemLookup.MoveActionLkpValue.KeepConnected);
				put("", SystemLookup.MoveActionLkpValue.ReconnectToDataPanel);
				put("", SystemLookup.MoveActionLkpValue.ReconnectToNetworkItem);
				put("", SystemLookup.MoveActionLkpValue.ReconnectToPowerOutlet);
				put("", SystemLookup.MoveActionLkpValue.ReconnectToRackPdu);
	}});
	
	private LksData getMatchingMoveAction(Request request) {

		if (null == request) return null;
		
		LksData moveAction = lksCache.getLksDataUsingLkpAndType(moveActionMap.get(request.getRequestType()), SystemLookup.LkpType.MOVE_ACTION);
		return moveAction;
		
	}
	
}
