package com.raritan.tdz.events.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.events.dto.EventDetailDTO;
import com.raritan.tdz.events.dto.EventDetailDTOImpl;
import com.raritan.tdz.events.dto.EventSummaryDTO;
import com.raritan.tdz.events.dto.EventSummaryDTOImpl;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;

/**
 * Event Service implementation.
 * 
 * @author Andrew Cohen
 */

@Transactional(rollbackFor = ServiceLayerException.class)
public class EventServiceImpl implements EventService {

	private EventHome home;

	public EventServiceImpl(EventHome home) {
		this.home = home;
	}

	@Override
	public EventSummaryDTO getEvents() throws ServiceLayerException {
		return new EventSummaryDTOImpl(home.getEvents());
	}

	@Override
	public EventSummaryDTO getActiveEvents() throws ServiceLayerException {
		return new EventSummaryDTOImpl(home.getActiveEvents());
	}

	@Override
	public EventSummaryDTO getClearedEvents() throws ServiceLayerException {
		return new EventSummaryDTOImpl(home.getClearedEvents());
	}

	@Override
	public List<EventDetailDTO> getEventDetails(long eventId) throws ServiceLayerException {
		Event event = home.getEventDetail( eventId );
		if (event == null) {
			return new LinkedList<EventDetailDTO>();
		}
		
		List<EventDetailDTO> details = new LinkedList<EventDetailDTO>();
		
		// Add event parameters
		Map<String, EventParam> params = event.getEventParams();
		for (String name : params.keySet()) {
			EventParam param = params.get( name );
			if (param.isDisplayable()) {
				details.add( new EventDetailDTOImpl(param.getDisplayName(), param.getValue()) );
			}
		}
		
		return details;
	}

	@Override
	public int clearEvents(List<Integer> eventIds) throws ServiceLayerException {
		List<Long> ids = new LinkedList<Long>();
		for (Integer id : eventIds) {
			ids.add(id.longValue());
		}
		return home.clearEvents(ids);
	}

	@Override
	public int purgeEvents(List<Integer> eventIds) throws ServiceLayerException {
		List<Long> ids = new LinkedList<Long>();
		for (Integer id : eventIds) {
			ids.add(id.longValue());
		}
		return home.purgeEvents(ids);
	}

	@Override
	public int purgeEvents(Date beforeDate) throws ServiceLayerException {
		return home.purgeEvents(beforeDate);
	}

	@Override
	public long getEventCount() throws ServiceLayerException {
		return home.getEventCount();
	}

	@Override
	public int clearAllEvents() throws DataAccessException {
		return home.clearAllEvents();
	}

	@Override
	public int purgeAllEvents() throws DataAccessException {
		return home.purgeAllEvents();
	}
}
