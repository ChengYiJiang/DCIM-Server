/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.dctrack.xsd.AltUiValueIdFieldMap;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.externalticket.dao.ExternalTicketFinderDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallbackUsingFilter;
import com.raritan.tdz.ticket.dao.TicketPortsPowerDAO;
import com.raritan.tdz.ticket.dao.TicketsDAO;

/**
 * @author prasanna
 *
 */
public class TicketPowerSupplyPortNamePrefixMethodCallback implements
		RemoteRefMethodCallbackUsingFilter {
	
	@Autowired
	private ExternalTicketFinderDAO ticketDAO;
	
	@Autowired
	private TicketsDAO ticketsDAO;
	
	@Autowired
	private TicketPortsPowerDAO ticketPortsDAO;
	
	private static final String altUiValueIdFieldId = "ticket";

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallbackUsingFilter#fillValue(com.raritan.dctrack.xsd.UiComponent, java.util.Map, com.raritan.tdz.rulesengine.RemoteRef, java.lang.Object)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent,
			Map<String, Filter> filterMap, RemoteRef remoteRef,
			Object additionalArgs) throws Throwable {
		//Get the UiValueIdField for the "ticket" alt data.
		UiValueIdField uiValueIdField = null;
		for (AltUiValueIdFieldMap field:uiViewComponent.getAltUiValueIdFieldMap()){
			if (field.getId().equals(altUiValueIdFieldId)){
				uiValueIdField = field.getUiValueIdField();
			}
		}
		
		String remoteEntityName = remoteRef.getRemoteType(uiValueIdField.getRemoteRef());
		String filterKey = "main";
		
		
		Filter filter = filterMap != null && filterKey != null ? filterMap.get(filterKey):null;
		String itemIdStr = filter.toSqlString().replaceAll("\\s","").substring(filter.toSqlString().lastIndexOf("=") - 1);
		if (itemIdStr != null && !itemIdStr.isEmpty()){
			Long itemId = Long.parseLong(itemIdStr);
			
			List<Long> ticketIds = ticketDAO.findTicketIdByItemId(itemId);
			
			if (ticketIds != null && ticketIds.size() > 0) {
				
				if (ticketsDAO.getTicketStatusLkpCode(ticketIds.get(0)).equals(SystemLookup.TicketStatus.TICKET_COMPLETE)){
					return;
				}
				
				List<TicketPortsPower> origPorts = ticketPortsDAO.getTicketPortsPower(ticketIds.get(0), false);
				
				if (origPorts != null && origPorts.size() > 0){
					String portNamePrefix = origPorts.get(0).getPortNamePrefix();
					
					uiValueIdField.setValue(portNamePrefix);
					uiValueIdField.setValueId(portNamePrefix);
				}
			}
		}
		
		

	}

}
