/**
 * 
 */

package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.externalticket.dao.ExternalTicketFinderDAO;
import com.raritan.tdz.item.home.PortsAdaptor;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;
/**
 * @author Bunty Nasta
 *
 */
public class PowerPortMethodCallback implements RemoteRefMethodCallback {
	@Autowired
	private PowerCircuitDAO powerCircuitDAO;
	
	@Autowired(required=true)
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	@Autowired
	private ExternalTicketFinderDAO ticketDAO;
	
	@Autowired
	private TicketPortsPowerDAO ticketPortsDAO;
	
	@Autowired
	private TicketsDAO ticketsDAO;
	
	private SessionFactory sessionFactory;
	
	public PowerPortMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		//First get the power ports for the given item
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item) session.get(Item.class, (Long) filterValue);
		/*List<PowerPort> powerPortList = new ArrayList<PowerPort>();
		if( item.getPowerPorts() != null){
			powerPortList.addAll(item.getPowerPorts());
		}*/
		Set<PowerPort> powerPorts = item.getPowerPorts();
		
		//get link information
		HashMap<Long, PortInterface> portMap = powerCircuitDAO.getDestinationItemsForItem(item.getItemId());
		
		//get next node actual amps information
		HashMap<Long, PortInterface> nextNodeMap = powerCircuitDAO.getNextNodeAmpsForItem(item.getItemId());
		
		//get the proposed circuit Ids associated with this item
		HashMap<Long, PortInterface> proposedMap = powerCircuitDAO.getProposedCircuitIdsForItem(item.getItemId());
		
		// Process the powerPortList and update as a list of PowerPortDTO 
		List<PowerPortDTO> powerPortDTOList = new ArrayList<PowerPortDTO>();
		
		List<Long> movePortIds = new ArrayList<Long>();
		if (null != powerPorts) {
			for (PowerPort pp: powerPorts) {
				movePortIds.add(pp.getPortId());
			}
		}
		Map<Long, LksData> portsAction = powerPortMoveDAO.getMovePortAction(movePortIds);
		
		if (null != powerPorts) {
			for (PowerPort pp: powerPorts) {
				PowerPortDTO powerPortDTO = PortsAdaptor.adaptPowerPortDomainToDTO(pp, portsAction);
				
				fillLinkInfo(portMap.get(pp.getPortId()), nextNodeMap.get(pp.getPortId()), proposedMap.get(pp.getPortId()), powerPortDTO);
				
				powerPortDTOList.add(powerPortDTO);
			}
		}
		Collections.sort(powerPortDTOList, new Comparator<PowerPortDTO>(){
			  public int compare(PowerPortDTO s1, PowerPortDTO s2) {
				  if (null != s1 && null != s2 && null != s1.getPortName() && null != s2.getPortName()) {
					  return s1.getPortName().compareToIgnoreCase(s2.getPortName());
				  }
				  else {
					  return 0;
				  }
			  }
		});

		fillAltData(powerPortDTOList, item);
		uiViewComponent.getUiValueIdField().setValue(powerPortDTOList);
	}
	

	private void fillLinkInfo(PortInterface port, PortInterface nextNode, PortInterface portProposed, PowerPortDTO powerPortDTO) {

		PowerPortDTO rec = (PowerPortDTO)port;
		
		// FIXME:: get the portMap for the original item if the itemId is of when moved item
		if (null != rec && null != powerPortDTO) {
			powerPortDTO.setConnectedItemId(rec.getConnectedItemId());
			powerPortDTO.setConnectedItemName(rec.getConnectedItemName());
			powerPortDTO.setConnectedPortId(rec.getConnectedPortId());
			powerPortDTO.setConnectedPortName(rec.getConnectedPortName());
			powerPortDTO.setConnectedCircuitId(rec.getConnectedCircuitId());
			powerPortDTO.setAmpsActualNextNode(rec.getAmpsActualNextNode());
			powerPortDTO.setProposedCircuitId(rec.getProposedCircuitId());
			powerPortDTO.setNextNodeClassValueCode(rec.getNextNodeClassValueCode());
			Long circuitStatusLksValueCode = null;
			String circuitStatusLksValue = null;
			if( rec.getCircuitStatusLksValueCode() != null && 
					(rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.INSTALLED || 
					rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.PLANNED ||
					rec.getCircuitStatusLksValueCode() == SystemLookup.ItemStatus.POWERED_OFF )){
				circuitStatusLksValueCode = rec.getCircuitStatusLksValueCode();
				circuitStatusLksValue = rec.getCircuitStatusLksValue();
			}
					
			powerPortDTO.setCircuitStatusLksValueCode(circuitStatusLksValueCode);
			powerPortDTO.setCircuitStatusLksValue(circuitStatusLksValue);
		}
		
		if(nextNode != null){		
			rec = (PowerPortDTO)nextNode;
			if (null != rec && null != powerPortDTO) {
				powerPortDTO.setAmpsActualNextNode(rec.getAmpsActualNextNode());
			}
		}
		
		rec = (PowerPortDTO)portProposed;
		
		if(rec != null){					
			powerPortDTO.setProposedCircuitId(rec.getProposedCircuitId());
		}
	}
	
	private void fillAltData(List<PowerPortDTO> powerPortDTOList, Item item ){
		if (item != null && item.getItemId() > 0){
			List<Long> ticketIds = ticketDAO.findTicketIdByItemId(item.getItemId());
			
			if (ticketIds != null && ticketIds.size() > 0) {
				
				if (ticketsDAO.getTicketStatusLkpCode(ticketIds.get(0)).equals(SystemLookup.TicketStatus.TICKET_COMPLETE)){
					return;
				}
				
				List<TicketPortsPower> origPorts = ticketPortsDAO.getTicketPortsPower(ticketIds.get(0), false);
				List<TicketPortsPower> modPorts = ticketPortsDAO.getTicketPortsPower(ticketIds.get(0), true);
				
				//TODO: This is currently dealing with power supply ports only. We need to expand this to work for all types of power  ports
				if (origPorts != null && modPorts != null
						&& modPorts.size() == 1 /*&& modPorts.get(0).getPortSubclassId().getLkpValueCode() == SystemLookup.PortSubClass.POWER_SUPPLY*/
						&& origPorts.size() > 0){
					TicketPortsPower modPort = modPorts.get(0);
					TicketPortsPower origPort = origPorts.get(0);
					
					int origPortQuantity = origPort.getQuantity();
					
					int index = 1;
					for (PowerPortDTO powerPortDTO: powerPortDTOList){
						//From modified port compare the name with the DTO's port name
						if (powerPortDTO.getPortName().equals(modPort.getPortNamePrefix() + index)){
							powerPortDTO.setAltData(PortsAdaptor.adaptTicketPowerPortDomainToDTO(origPort, index));
						}
						
						if (index == origPortQuantity) break;
						index++;
					}
				}
				
			}
			
		}
	}
}
