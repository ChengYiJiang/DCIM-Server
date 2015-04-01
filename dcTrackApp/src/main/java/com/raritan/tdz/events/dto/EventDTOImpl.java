package com.raritan.tdz.events.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.lookup.SystemLookup;


public class EventDTOImpl implements Serializable, EventDTO {
	
	private static final long serialVersionUID = 1L;
	private static final String UNKNOWN_STRING_VAL = "Unknown";
	
	private long eventId;
	private Timestamp occuredAt;
	private String severity;
	private String event;
	private String summary;
	private String status;
	private String source;
	private String params; // All event parameters in a semi-colon delimited string
	
	public EventDTOImpl() {
		
	}

	public EventDTOImpl(Event event) {	
		this.eventId = event.getId();
		setOccuredAt( event.getCreatedAt() );
		setEvent( event );
		setSeverity( event.getSeverity().getLkpValue() );
		setSummary( event.getSummary() );
		setStatus( event.getClearedAt() != null ? "Cleared" : "Active" );
		setSource( event.getSource() );
		setParams( event.getEventParams() );
	}

	public EventDTOImpl(long eventId, Timestamp occuredAt, String severity,
			String event, String summary, String status, String source) {
		super();
		this.eventId = eventId;
		this.occuredAt = occuredAt;
		this.severity = severity;
		this.event = event;
		this.summary = summary;
		this.status = status;
		this.source = source;
		this.params = new String("");
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getOccuredAt()
	 */
	@Override
	public Timestamp getOccuredAt() {
		return occuredAt;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setOccuredAt(java.sql.Timestamp)
	 */
	@Override
	public void setOccuredAt(Timestamp occuredAt) {
		this.occuredAt = occuredAt; 
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getSeverity()
	 */
	@Override
	public String getSeverity() {
		return severity;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setSeverity(java.lang.String)
	 */
	@Override
	public void setSeverity(String severity) {
		this.severity = severity;
		if (severity == null) this.severity = UNKNOWN_STRING_VAL;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getEvent()
	 */
	@Override
	public String getEvent() {
		return event;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setEvent(java.lang.String)
	 */
	@Override
	public void setEvent(String event) {
		this.event = event;
		if (event == null) this.event = UNKNOWN_STRING_VAL;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setEvent(com.raritan.tdz.events.domain.Event)
	 */
	@Override
	public void setEvent(Event event) {
		StringBuffer eventType = new StringBuffer();
		LksData type = event.getType();
		LksData result = event.getEventResult();
		
		// TODO: This is asset tag specific logic that shouldn't exist here
		if (type.getLkpValueCode() == SystemLookup.EventType.ASSET_TAG_CONNECTED) {
			if (result != null) {
				eventType.append( result.getLkpValue() );
				eventType.append(" - ");
			}
		}
		
		eventType.append( type.getLkpValue() );
		
		setEvent( eventType.toString() );
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getEventId()
	 */
	@Override
	public long getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setEventId(long)
	 */
	@Override
	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getSummary()
	 */
	@Override
	public String getSummary() {
		return summary;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setSummary(java.lang.String)
	 */
	@Override
	public void setSummary(String summary) {
		this.summary = summary;
		if (summary == null) this.summary = "";
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getStatus()
	 */
	@Override
	public String getStatus() {
		return status;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setStatus(java.lang.String)
	 */
	@Override
	public void setStatus(String status) {
		this.status = status;
		if (status == null) this.status = UNKNOWN_STRING_VAL;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getSource()
	 */
	@Override
	public String getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setSource(java.lang.String)
	 */
	@Override
	public void setSource(String source) {
		this.source = source;
		if (source == null) this.source = UNKNOWN_STRING_VAL;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#getParams()
	 */
	@Override
	public String getParams() {
		return params;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.events.dto.EventDTO#setParams(java.lang.String)
	 */
	@Override
	public void setParams(String params) {
		this.params = params;
	}
	
	/**
	 * Build a semi-colon delimited string from the event parameters.
	 * @param params
	 */
	private void setParams(Map<String, EventParam> params) {
		StringBuffer b = new StringBuffer();
		
		for (String param: params.keySet()) {
			b.append( param );
			b.append(" : ");
			b.append( params.get( param ).getValue() );
			b.append("; ");
		}
		
		this.params = b.toString();
	}
}
