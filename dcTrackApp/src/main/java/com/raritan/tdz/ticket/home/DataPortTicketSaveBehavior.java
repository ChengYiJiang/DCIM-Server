package com.raritan.tdz.ticket.home;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;

public class DataPortTicketSaveBehavior implements TicketSaveBehavior {

	@Autowired(required=true)
	private TicketPortsDataDAO ticketPortsDataDAO;
	
	@Autowired(required=true)
	private TicketUpdateHelper ticketDataPortFieldsUpdateHelperImpl;
	

	@Override
	public void updateTicketFields(Object fields, Item item) {

		@SuppressWarnings("unchecked")
		List<TicketPortsData> ticketPortsDataFields = (List<TicketPortsData>) fields;
		
		for (TicketPortsData ticketPortDataFields: ticketPortsDataFields) {
			
			// get the matching data port in the item
			DataPort dataPort = getMatchingPort(item, ticketPortDataFields);
			if (null == dataPort) continue;
			
			// get the value as a map to be updated to the ticket fields
			
			// set connector
			setConnector(ticketPortDataFields, dataPort);
			
			// set media
			setMedia(ticketPortDataFields, dataPort);
			
			// set protocol
			setProtocol(ticketPortDataFields, dataPort);
			
			// set speed
			setSpeed(ticketPortDataFields, dataPort);
			
			// set color
			setColor(ticketPortDataFields, dataPort);
			
			// set vlan
			setVLAN(ticketPortDataFields, dataPort);
			
			// set mac address
			setMacAddress(ticketPortDataFields, dataPort);
			
			// set ip address
			setIpAddress(ticketPortDataFields, dataPort);
			
			// set port type
			setPortType(ticketPortDataFields, dataPort);
			
			// merge the value
			ticketPortsDataDAO.mergeOnly(ticketPortDataFields);
			
		}

		if (null != portNameMap) {
			portNameMap.clear();
		}
		

	}


	@Override
	public void update(Item item, Object... additionalArgs) {
		
		ticketDataPortFieldsUpdateHelperImpl.update(this, item, additionalArgs);

	}

	@Override
	public void update(Object target, Item item, Object... additionalArgs) {
		
		ticketDataPortFieldsUpdateHelperImpl.update(this, target, item);

	}
	
	private Map<String, DataPort> portNameMap = null;
	
	private DataPort getMatchingPort(Item item, TicketPortsData ticketPortDataFields) {
		
		DataPort port = null;
		Set<DataPort> dataPorts = item.getDataPorts();
		
		if (null == dataPorts) return port;

		if (null == portNameMap) {
			portNameMap = new HashMap<String, DataPort>();
		}
		if (portNameMap.size() > 0) {
			return portNameMap.get(ticketPortDataFields.getPortName());
		}
		
		for (DataPort dataPort: dataPorts) {
			portNameMap.put(dataPort.getPortName(), dataPort);
			if (null == port && dataPort.getPortName().equals(ticketPortDataFields.getPortName())) {
				port = dataPort;
			}
		}
		
		return port;
	}

	// set connector
	private void setConnector(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		ConnectorLkuData connector = dataPort.getConnectorLookup();
		ticketPortDataFields.setConnectorLookup(connector);
		if (null != connector) {
			ticketPortDataFields.setConnector(connector.getConnectorName());
		}
		else {
			ticketPortDataFields.setConnector(null);
		}

	}
	
	
	// set media
	private void setMedia(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LksData media = dataPort.getMediaId();
		ticketPortDataFields.setMediaId(media);
		if (null != media) {
			ticketPortDataFields.setMedia(media.getLkpValue());
		}
		else {
			ticketPortDataFields.setMedia(null);
		}
	}

	
	// set protocol
	private void setProtocol(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LkuData protocol = dataPort.getProtocolID();
		ticketPortDataFields.setProtocolID(protocol);
		if (null != protocol) {
			ticketPortDataFields.setProtocol(protocol.getLkuValue());
		}
		else {
			ticketPortDataFields.setProtocol(null);
		}
	}

	
	// set speed
	private void setSpeed(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LkuData speed = dataPort.getSpeedId();
		ticketPortDataFields.setSpeedId(speed);
		if (null != speed) {
			ticketPortDataFields.setSpeed(speed.getLkuValue());
		}
		else {
			ticketPortDataFields.setSpeed(null);
		}
	}

	
	// set color
	private void setColor(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LkuData color = dataPort.getColorLookup();
		ticketPortDataFields.setColorLookup(color);
		if (null != color) {
			ticketPortDataFields.setColor(color.getLkuValue());
		}
		else {
			ticketPortDataFields.setColor(null);
		}
	}

	
	// set vlan
	private void setVLAN(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LkuData vlan = dataPort.getVlanLookup();
		ticketPortDataFields.setVlanLookup(vlan);
		if (null != vlan) {
			ticketPortDataFields.setVlan(vlan.getLkuValue());
		}
		else {
			ticketPortDataFields.setVlan(null);
		}
	}

	
	// set mac address
	private void setMacAddress(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		ticketPortDataFields.setMacAddress(dataPort.getMacAddress());
	}

	
	// set ip address
	private void setIpAddress(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		ticketPortDataFields.setIpAddress(dataPort.getIpAddress());
	}

	
	// set port type
	private void setPortType(TicketPortsData ticketPortDataFields, DataPort dataPort) {
		LksData subclassLks = dataPort.getPortSubClassLookup();
		ticketPortDataFields.setPortSubclassId(subclassLks);
		if (null != subclassLks) {
			ticketPortDataFields.setPortType(subclassLks.getLkpValue());
		}
		else {
			ticketPortDataFields.setPortType(null);
		}
	}



}
