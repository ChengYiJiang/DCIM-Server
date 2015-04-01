package com.raritan.tdz.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.DataPortMove;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.TicketPortsData;
import com.raritan.tdz.externalticket.dao.ExternalTicketFinderDAO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.ticket.dao.TicketFieldsDAO;
import com.raritan.tdz.ticket.dao.TicketPortsDataDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;

/**
 * helper for the data port dto
 * @author bunty
 *
 */
public class DataPortDTOHelper implements PortDTOHelper<DataPortDTO> {

	@Autowired
	private DataCircuitDAO dataCircuitDAO;
	
	@Autowired
	private TicketPortsDataDAO ticketPortsDAO;
	
	@Autowired
	private ExternalTicketFinderDAO ticketDAO;
	
	@Autowired
	private TicketFieldsDAO ticketFieldsDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<DataPortMove> dataPortMoveDAO;
	
	@Autowired
	private TicketsDAO ticketsDAO;
	
	@Autowired
	private DataPortDAO dataPortDAO;
	
	@Override
	public List<DataPortDTO> getPortDTOList(Item item) {

		Set<DataPort> dataPorts = item.getDataPorts();
		
		//Get link information
		HashMap<Long, PortInterface> portMap = dataCircuitDAO.getDestinationItemsForItem(item.getItemId());
		HashMap<Long, PortInterface> proposedMap = dataCircuitDAO.getProposedCircuitIdsForItem(item.getItemId());
		
		List<Long> movePortIds = new ArrayList<Long>();
		if (null != dataPorts) {
			for (DataPort dp: dataPorts) {
				movePortIds.add(dp.getPortId());
			}
		}
		Map<Long, LksData> portsAction = dataPortMoveDAO.getMovePortAction(movePortIds);
		
		// Process the dataPortList and update as a list of DataPortDTO
		List<DataPortDTO> dataPortDTOList = new ArrayList<DataPortDTO>();

		if (null != dataPorts) {
			for (DataPort dp: dataPorts) {
				dp.setIpAddressUsingIpAddressTable(dataPortDAO.getSession());
				
				DataPortDTO dataPortDTO = PortsAdaptor.adaptDataPortDomainToDTO(dp, portsAction);
				
				fillLinkInfo(portMap.get(dp.getPortId()), proposedMap.get(dp.getPortId()), dataPortDTO);
				
				dataPortDTOList.add(dataPortDTO);
			}
		}

		Collections.sort(dataPortDTOList, new Comparator<DataPortDTO>(){
			  public int compare(DataPortDTO s1, DataPortDTO s2) {
			    return s1.getPortName().compareToIgnoreCase(s2.getPortName());
			  }
		});

		fillAltData(dataPortDTOList,item);
		
		return dataPortDTOList;

	}
	
	private void fillLinkInfo(PortInterface portConnected, PortInterface portProposed, DataPortDTO dataPortDTO) {		
		DataPortDTO rec = (DataPortDTO)portConnected;
		
		// FIXME:: get the portMap for the original item if the itemId is of when moved item
		if(rec != null){
			dataPortDTO.setConnectedItemId(rec.getConnectedItemId());
			dataPortDTO.setConnectedItemName(rec.getConnectedItemName());
			dataPortDTO.setConnectedPortId(rec.getConnectedPortId());
			dataPortDTO.setConnectedPortName(rec.getConnectedPortName());
			dataPortDTO.setConnectedCircuitId(rec.getConnectedCircuitId());
			dataPortDTO.setNextNodeClassValueCode(rec.getNextNodeClassValueCode());
			Long circuitStatusLksValueCode = null;
			String circuitStatusLksValue = null;
			if( rec.getCircuitStatusLksValueCode() != null && 
					(rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.INSTALLED || 
					rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.PLANNED ||
					rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.POWERED_OFF )){
				circuitStatusLksValueCode = rec.getCircuitStatusLksValueCode();
				circuitStatusLksValue = rec.getCircuitStatusLksValue();
			}
			dataPortDTO.setCircuitStatusLksValueCode(circuitStatusLksValueCode);
			dataPortDTO.setCircuitStatusLksValue(circuitStatusLksValue);
		}
		
		rec = (DataPortDTO)portProposed;
		
		if(rec != null){					
			dataPortDTO.setProposedCircuitId(rec.getProposedCircuitId());
		}
	}
	
	private void fillAltData(List<DataPortDTO> dataPortDTOList, Item item ){
		if (item != null && item.getItemId() > 0){
			List<Long> ticketIds = ticketDAO.findTicketIdByItemId(item.getItemId());
			
			if (ticketIds != null && ticketIds.size() > 0){
				
				if (ticketsDAO.getTicketStatusLkpCode(ticketIds.get(0)).equals(SystemLookup.TicketStatus.TICKET_COMPLETE)){
					return;
				}
				
				TicketFields origTicketField = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketIds.get(0),false);
				TicketFields modTicketField = ticketFieldsDAO.getTicketFieldsUsingTicket(ticketIds.get(0),true);
				
				for (DataPortDTO dataPortDTO: dataPortDTOList){
					if (origTicketField != null && modTicketField != null){
						TicketPortsData origTicketPortData = ticketPortsDAO.getOriginalTicketPortData(origTicketField.getTicketFieldId(), modTicketField.getTicketFieldId(), dataPortDTO.getPortName());
						if (origTicketPortData != null){
							dataPortDTO.setAltData(PortsAdaptor.adaptTicketDataPortDomainToDTO(origTicketPortData));
						}
					}
				}
			}
		}
	}
	


}
