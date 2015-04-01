package com.raritan.tdz.events.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.Session;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * A domain object representing a generic dcTrack event.
 * 
 * @author Andrew Cohen
 */

@Entity
@Table(name="`dct_events`")
public class Event implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7250584882621941169L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dct_events_seq")
	@SequenceGenerator(name = "dct_events_seq", sequenceName = "dct_events_event_id_seq", allocationSize=1)
	@Column(name = "event_id", unique = true, nullable = false)
	private long id;
	
	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;
	
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = "cleared_at", nullable = true)
	private Timestamp clearedAt;
	
	@Column(name = "u_position", nullable = true)
	private Long uPosition;
	
	@Column(name = "summary", nullable = true)
	private String summary;
	
	@Column(name = "source", nullable = false)
	private String source;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade={javax.persistence.CascadeType.ALL})
	@MapKey(name = "name")
	private Map<String, EventParam> eventParams;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = true)
	private Item item;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cabinet_id", nullable = true)
	private CabinetItem cabinet;
	
	@Column(name = "cleared_by_username", nullable = true)
	private String clearedByUsername;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", nullable = true)
	private DataCenterLocationDetails location;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_type_lks_id", nullable = false)
	private LksData type;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_severity_lks_id", nullable = false)
	private LksData severity;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_status_lks_id", nullable = false)
	private LksData status;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "event_result_lks_id", nullable = true)
	private LksData eventResult;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clearing_event_id", nullable = true)
	private Event clearingEvent; // The event which cleared this event
	
	@OneToMany(mappedBy = "clearingEvent", fetch = FetchType.LAZY)
	private List<Event> eventsCleared; // All events cleared BY this event
	
	/**
	 * The set of valid event type lookups.
	 */
	public enum EventType  {
		ASSET_TAG_CONNECTED {
			public long valueCode() { return SystemLookup.EventType.ASSET_TAG_CONNECTED; }
		},
		ASSET_TAG_REMOVED {
			public long valueCode() { return SystemLookup.EventType.ASSET_TAG_REMOVED; }
		},
		ASSET_STRIP_CONNECTED {
			public long valueCode() { return SystemLookup.EventType.ASSET_STRIP_CONNECTED; }
		},
		ASSET_STRIP_REMOVED {
			public long valueCode() { return SystemLookup.EventType.ASSET_STRIP_REMOVED; }
		},
		CANNOT_ACCESS_RESOURCE {
			public long valueCode() { return SystemLookup.EventType.CANNOT_ACCESS_RESOURCE; }
		},
		COMMUNICATION_RESTORED {
			public long valueCode() { return SystemLookup.EventType.COMMUNICATION_RESTORED; }
		},
		CLIENT_ERROR {
			public long valueCode() { return SystemLookup.EventType.CLIENT_ERROR; }
		},
		SERVER_ERROR {
			public long valueCode() { return SystemLookup.EventType.SERVER_ERROR; }
		},
		PIQ_UPDATE {
			public long valueCode() { return SystemLookup.EventType.PIQ_UPDATE; }
		},
		CHASSIS_SLOT_REASSIGNMENT {
			public long valueCode() { return SystemLookup.EventType.CHASSIS_SLOT_REASSIGNMENT; }
		},
		INVALID_SENSOR_REQUEST {
			public long valueCode() { return SystemLookup.EventType.INVALID_SENSOR_REQUEST; }
		},
		INVALID_SENSOR_RESPONSE {
			public long valueCode() { return SystemLookup.EventType.INVALID_SENSOR_RESPONSE; }
		},
		SENSOR_UPDATE {
			public long valueCode() { return SystemLookup.EventType.SENSOR_UPDATE; }
		},
		SENSOR_DELETE {
			public long valueCode() { return SystemLookup.EventType.SENSOR_DELETE; }
		},
		MIGRATION {
			public long valueCode() { return SystemLookup.EventType.MIGRATION; }
		},
		SYNC_FLOORMAP_DATA {
			public long valueCode() { return SystemLookup.EventType.SYNC_FLOORMAP_DATA; }
		};
		
		public abstract long valueCode();
	}
	
	/**
	 * The set of valid event status lookups.
	 */
	public enum EventStatus  {
		ACTIVE {
			public long valueCode() { return SystemLookup.EventStatus.ACTIVE; }
		},
		CLEARED {
			public long valueCode() { return SystemLookup.EventStatus.CLEARED; }
		};
		public abstract long valueCode();
	}
	
	/**
	 * The set of valid event severity lookups.
	 */
	public enum EventSeverity  {
		INFORMATIONAL {
			public long valueCode() { return SystemLookup.EventSeverity.INFORMATIONAL; }
		},
		WARNING {
			public long valueCode() { return SystemLookup.EventSeverity.WARNING; }
		},
		CRITICAL {
			public long valueCode() { return SystemLookup.EventSeverity.CRITICAL; }
		};
		public abstract long valueCode();
	}
	
	/**
	 * Creates a new event with the default status and default severity (i.e., normal).
	 * @param session
	 * @param createdAt
	 * @param eventType
	 * @return
	 */
	public static Event createEvent(Session session, Timestamp createdAt, EventType eventType, String source) {
		return createEvent(session, createdAt, eventType, source, null);
	}
	
	/**
	 * Creates a new event with the default status and a specified severity.
	 * @param session
	 * @param createdAt
	 * @param eventType
	 * @return
	 */
	public static Event createEvent(Session session, Timestamp createdAt, EventType eventType, String source, EventSeverity eventSeverity) {
		return createEvent(session, createdAt, eventType, source, eventSeverity, null);
	}
	
	/**
	 * Creates a new event with a specified status and severity.
	 * @param session
	 * @param createdAt
	 * @param eventType
	 * @return
	 */
	public static Event createEvent(Session session, Timestamp createdAt, EventType eventType, String source, EventSeverity eventSeverity, EventStatus eventStatus) {
		if (eventType == null) 
			throw new IllegalArgumentException("eventType cannot be null!");
		LksData typeLks = SystemLookup.getLksData(session, eventType.valueCode());
		LksData statusLks = SystemLookup.getLksData(session, eventStatus != null ? eventStatus.valueCode() : EventStatus.ACTIVE.valueCode());
		LksData severityLks = SystemLookup.getLksData(session, eventSeverity != null ? eventSeverity.valueCode() : EventSeverity.INFORMATIONAL.valueCode());
		return new Event(createdAt, typeLks, statusLks, severityLks, source);
	}
	
	private Event(Timestamp createdAt, LksData type, LksData status, LksData severity, String source) {
		this();
		this.createdAt = createdAt;
		this.type = type;
		this.status = status;
		this.severity = severity;
		this.source = source;
	}
	
	public Event() {
		this.eventParams = new HashMap<String, EventParam>();
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}
	
	public Timestamp getClearedAt() {
		return clearedAt;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
	public DataCenterLocationDetails getLocation() {
		return location;
	}

	public void setLocation(DataCenterLocationDetails location) {
		this.location = location;
	}

	public CabinetItem getCabinet() {
		return cabinet;
	}

	public void setCabinet(CabinetItem cabinet) {
		this.cabinet = cabinet;
	}
	
	public Map<String, EventParam> getEventParams() {
		return eventParams;
	}

	public void setEventParams(Map<String, EventParam> eventParams) {
		this.eventParams = eventParams;
	}

	public LksData getType() {
		return type;
	}

	public LksData getSeverity() {
		return severity;
	}

	public LksData getStatus() {
		return status;
	}
	
	public String getClearedByUsername() {
		return clearedByUsername;
	}

	public void setClearedByUsername(String clearedByUsername) {
		this.clearedByUsername = clearedByUsername;
	}

	public Long getuPosition() {
		return uPosition;
	}

	public void setuPosition(Long uPosition) {
		if (uPosition != null && uPosition < 0)
			throw new IllegalArgumentException("Invalid u_position: "+uPosition);
		this.uPosition = uPosition;
	}

	public LksData getEventResult() {
		return eventResult;
	}

	public void setEventResult(LksData eventResult) {
		this.eventResult = eventResult;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public Event getClearingEvent() {
		return clearingEvent;
	}
	
	public void setClearingEvent(Event clearingEvent) {
		this.clearingEvent = clearingEvent;
	}
	
	public List<Event> getEventsCleared() {
		return eventsCleared;
	}

	public void setEventsCleared(List<Event> eventsCleared) {
		this.eventsCleared = eventsCleared;
	}
	
	//
	// Business methods
	//
	
	public void setType(Session session, EventType type) {
		this.type = SystemLookup.getLksData(session, type.valueCode());
	}
	
	public void setSeverity(Session session, EventSeverity severity) {
		this.severity = SystemLookup.getLksData(session, severity.valueCode());
	}
	
	public void setStatus(Session session, EventStatus status) {
		this.status = SystemLookup.getLksData(session, status.valueCode());
	}
	
	public void setProcessedStatus(LksData processedStatus) {
		this.eventResult = processedStatus;
	}
	
	public LksData getProcessedStatus() {
		return eventResult;
	}
	
	public boolean isCleared() {
		return clearedAt != null;
	}
	
	/**
	 * Clear this event.
	 */
	public void clear(Event clearingEvent) {
		if (clearedAt == null) {
			this.clearedAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		}
		this.clearingEvent = clearingEvent;
	}
	
	/**
	 * Add a lookup field paramclearingEventeter to this event.
	 * @param value
	 */
	public void addParam(LksData value) {
		EventParam param = new EventParam(this, value);
		eventParams.put( param.getName(), param );
	}
	
	/**
	 * Add a key value pair parameter to this event.
	 * @param name
	 * @param value
	 */
	public EventParam addParam(String name, String value) {
		EventParam param = new EventParam(this, name, value);
		eventParams.put( param.getName(), param );
		return param;
	}

	public Event(long id, Timestamp createdAt, Timestamp clearedAt,
			Long uPosition, String summary, String source,
			Map<String, EventParam> eventParams, Item item,
			CabinetItem cabinet, String clearedByUsername,
			DataCenterLocationDetails location, LksData type, LksData severity,
			LksData status, LksData eventResult, Event clearingEvent,
			List<Event> eventsCleared) {
		super();
		this.id = id;
		this.createdAt = createdAt;
		this.clearedAt = clearedAt;
		this.uPosition = uPosition;
		this.summary = summary;
		this.source = source;
		this.eventParams = eventParams;
		this.item = item;
		this.cabinet = cabinet;
		this.clearedByUsername = clearedByUsername;
		this.location = location;
		this.type = type;
		this.severity = severity;
		this.status = status;
		this.eventResult = eventResult;
		this.clearingEvent = clearingEvent;
		this.eventsCleared = eventsCleared;
	}
	
	
}
