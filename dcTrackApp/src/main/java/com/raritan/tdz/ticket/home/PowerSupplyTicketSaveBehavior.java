package com.raritan.tdz.ticket.home;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;

public class PowerSupplyTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketPortsPowerDAO ticketPortsPowerDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketPowerPortFieldsUpdateHelperImpl;
	
	public void updateTicketFields(Object fields, Item item) {

		if (null == fields) return;
		
		TicketPortsPower powerSupplyTicketPort = getPowerSupplyTicketField(fields);
		
		if (null == powerSupplyTicketPort) {
			return;
		}
		
		// TODO:: Get all the power supply ports if any given item class will contain other power ports (breaker, rpdu) 
		Map<String, Object> valueMap = getPowerSupplyValues(item);
		Integer size = Integer.valueOf(valueMap.get(PortValueKey.QUANTITY).toString());
		PowerPort refPowerPort = (PowerPort) valueMap.get(PortValueKey.REF_PORT);
		
		// set quantity
		powerSupplyTicketPort.setQuantity(size);
		if (size > 0) {
			
			// set portNamePrefix
			powerSupplyTicketPort.setPortNamePrefix((String) valueMap.get(PortValueKey.PORT_NAME_PREFIX));
			
			// set power factor
			powerSupplyTicketPort.setPowerFactor(refPowerPort.getPowerFactor());
			
			// set connector
			powerSupplyTicketPort.setConnector(refPowerPort.getConnectorLookup().getConnectorName());
			powerSupplyTicketPort.setConnectorLookup(refPowerPort.getConnectorLookup());
			
			// set voltage
			powerSupplyTicketPort.setVolts(refPowerPort.getVoltsLookup().getLkpValue());
			powerSupplyTicketPort.setVoltsId(refPowerPort.getVoltsLookup());
			
			// set phase
			powerSupplyTicketPort.setPhase(refPowerPort.getPhaseLookup().getLkpValue());
			powerSupplyTicketPort.setPhaseId(refPowerPort.getPhaseLookup());
			
			// set name plate watts
			powerSupplyTicketPort.setWattsNamePlate(refPowerPort.getWattsNameplate());
			
			// set budget watts
			powerSupplyTicketPort.setWattsBudget(refPowerPort.getWattsBudget());
			
			// set redundancy
			powerSupplyTicketPort.setPsRedundancy((String) valueMap.get(PortValueKey.REDUNDANCY));

			// set color if all the colors are the same
			LkuData colorLku = (LkuData) valueMap.get(PortValueKey.COLOR_LKU);
			powerSupplyTicketPort.setColorLookup(colorLku);
			if (null != colorLku) {
				powerSupplyTicketPort.setColor(refPowerPort.getColorLookup().getLkuValue());
			}
			else {
				powerSupplyTicketPort.setColor("");
			}
			
		}
		else {
			// setting the quantity should be enough
		}
		
		/* merge the updated field */
		ticketPortsPowerDAO.mergeOnly(powerSupplyTicketPort);
		
	}
	
	@Override
	public void update(Item item, Object... additionalArgs) {

		ticketPowerPortFieldsUpdateHelperImpl.update(this, item, additionalArgs);
		
	}

	@Override
	public void update(Object target, Item item, Object... additionalArgs) {
		
		ticketPowerPortFieldsUpdateHelperImpl.update(this, target, item);
		
	}
	
	/* private functions */
	// returns the length of the longest common prefix of all strings in the given array 
	private String getCommonPrefix(List<String> strings) {
		String commonPrefix = null;
		List<String> namePrefixes = new LinkedList<String>();
		Boolean namePrefixUpdated = null;
		for (String string: strings) {
			List<String> splitStr = parse(string);
			if (null == splitStr || splitStr.size() == 0) {
				commonPrefix = null;
				return commonPrefix;
			}
			commonPrefix = splitStr.get(0);
			if (null == namePrefixUpdated) {
				namePrefixes.add(commonPrefix);
				namePrefixUpdated = true;
			}
			if (!namePrefixes.contains(splitStr.get(0))) {
				commonPrefix = null;
			}
		}
		
		return commonPrefix;
	}
	
	// private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+|[A-Z]+");
	private static final Pattern VALID_PATTERN = Pattern.compile("\\d+|\\D+");

	private List<String> parse(String toParse) {
	    List<String> chunks = new LinkedList<String>();
	    Matcher matcher = VALID_PATTERN.matcher(toParse);
	    while (matcher.find()) {
	        chunks.add( matcher.group() );
	    }
	    return chunks;
	}
	
	private TicketPortsPower getPowerSupplyTicketField(Object fields) {
		TicketPortsPower powerSupplyTicketPort = null;
		
		@SuppressWarnings("unchecked")
		List<TicketPortsPower> ticketPortsPowerField = (List<TicketPortsPower>) fields;
		for (TicketPortsPower ticketPortPowerSupply: ticketPortsPowerField) {
			if (null == ticketPortPowerSupply.getPortSubclassId() || 
					ticketPortPowerSupply.getPortSubclassId().getLkpValueCode().equals(SystemLookup.PortSubClass.POWER_SUPPLY)) {
				powerSupplyTicketPort = ticketPortPowerSupply; 
			}
		}
		
		return powerSupplyTicketPort;

	}

	public static class PortValueKey {
		public static final String REF_PORT = "Reference_Port";
		public static final String REDUNDANCY = "Redundancy";
		public static final String COLOR_LKU = "Color_lku";
		public static final String QUANTITY = "Quantity";
		public static final String PORT_NAME_PREFIX = "PortNamePrefix";
	}
	
	private Map<String, Object> getPowerSupplyValues(Item item) {
		
		Map<String, Object> valueMap = new HashMap<String, Object>();
		
		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null == powerPorts) {
			valueMap.put(PortValueKey.QUANTITY, Long.valueOf(0));
			valueMap.put(PortValueKey.REF_PORT, null);
			valueMap.put(PortValueKey.COLOR_LKU, null);
			valueMap.put(PortValueKey.REDUNDANCY, null);
			valueMap.put(PortValueKey.PORT_NAME_PREFIX, null);
			return valueMap;
		}

		PowerPort powerPort = null;
		Boolean sameColor = null;
		LkuData colorLku = new LkuData();
		boolean setColorLku = false;
		List<String> portNames = new LinkedList<String>();
		for(PowerPort port: powerPorts) {
			
			powerPort = port;
			portNames.add(port.getPortName());
			LkuData portColor = port.getColorLookup();
			if (null != sameColor && !sameColor) continue;
			if (!setColorLku) {
				colorLku = portColor;
				setColorLku = true;
			}
			
			if (null == colorLku && null == portColor) {
				sameColor = true;
			}
			else if (null == colorLku && null != portColor) {
				colorLku = null;
				sameColor = false;
			}
			else if (null != colorLku && null == portColor) {
				colorLku = null;
				sameColor = false;
			}
			else if (colorLku.getLkuId() != portColor.getLkuId()) {
				colorLku = null;
				sameColor = false;
			}
			else {
				sameColor = true;
			}
		}

		Item itemUnProxy = (Item) ticketPortsPowerDAO.initializeAndUnproxy(item);
		String psRedundancy = null;
		if (itemUnProxy instanceof ItItem) {
			psRedundancy = ((ItItem) itemUnProxy).getPsredundancy();
		}
		else if (itemUnProxy instanceof MeItem) {
			psRedundancy = ((MeItem) itemUnProxy).getPsredundancy();
		}

		valueMap.put(PortValueKey.QUANTITY, powerPorts.size());
		valueMap.put(PortValueKey.REF_PORT, powerPort);
		valueMap.put(PortValueKey.COLOR_LKU, colorLku);
		valueMap.put(PortValueKey.REDUNDANCY, psRedundancy);
		valueMap.put(PortValueKey.PORT_NAME_PREFIX, getCommonPrefix(portNames));
		
		return valueMap;
	}

}
