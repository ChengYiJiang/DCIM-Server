package com.raritan.tdz.piq.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.springframework.util.StringUtils;

import com.raritan.tdz.piq.home.PIQEvent;
import com.raritan.tdz.piq.home.PIQRestClientBase;

/**
 * The response returned by the PIQ events REST API.
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class EventsJSON {
	
	private final static Logger log = Logger.getLogger( EventsJSON.class );
	
	private List<EventJSON> events;
	
	public EventsJSON(){
		super();
	}
	
	@JsonSetter(value="events")
	@JsonDeserialize(contentAs=EventJSON.class)
	public void setEvents(List<EventJSON> events) {
		this.events = events;
	}
	
	@JsonProperty(value="events")
	public List<EventJSON> getEvents() {
		return events;
	}
	
	/**
	 * A PIQ event JSON response returned by the REST API.
	 * 
	 * @author Andrew Cohen
	 */
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class EventJSON implements PIQEvent {

		private String eventId;
		private Integer eventConfigId;
		private String createdAtStr;
		private Date createdAt;
		private String clearedAtStr;
		private Date clearedAt;
		private String clearingEventId;
		private String pduId;
		private String notificationStatus;
		private String assetStripId;
		private String rackUnitId;
		private List<Param> params;
		private SimpleDateFormat sdf;
		
		public EventJSON() {
			sdf = new SimpleDateFormat( PIQRestClientBase.DATE_FORMAT );
		}
		
		@Override
		@JsonProperty(value="id")
		public String getEventId() {
			return eventId;
		}
		@JsonSetter(value="id")
		public void setEventId(String eventId) {
			this.eventId = eventId;
		}
		
		@JsonProperty(value="created_at")
		public String getCreatedAtStr() {
			return createdAtStr;
		}
		@JsonSetter(value="created_at")
		public void setCreatedAtStr(String createdAtStr) {
			this.createdAtStr = createdAtStr;
			try {
				this.createdAt = sdf.parse( createdAtStr );
			}
			catch (ParseException e) {
				log.error("Error parsing date in PIQ Event: "+createdAtStr);
			}
		}
		
		@Override
		public Date getCreatedAt() {
			return createdAt;
		}
		
		@JsonProperty(value="cleared_at")
		public String getClearedAtStr() {
			return clearedAtStr;
		}
		@JsonSetter(value="cleared_at")
		public void setClearedAtStr(String clearedAtStr) {
			this.clearedAtStr = clearedAtStr;
			
			if (StringUtils.hasText( clearedAtStr )) {
				try {
					this.clearedAt = sdf.parse( clearedAtStr );
				}
				catch (ParseException e) {
					log.error("Error parsing date in PIQ Event: "+clearedAtStr);
				}
			}
			else {
				this.clearedAt = null;
			}
		}
		
		@Override
		public Date getClearedAt() {
			return clearedAt;
		}
		
		@Override
		@JsonProperty(value="pdu_id")
		public String getPduId() {
			return pduId;
		}
		@JsonSetter(value="pdu_id")
		public void setPduId(String pduId) {
			this.pduId = pduId;
		}
		
		@Override
		@JsonProperty(value="notification_status")
		public String getNotificationStatus() {
			return notificationStatus;
		}
		@JsonSetter(value="notification_status")
		public void setNotificationStatus(String notificationStatus) {
			this.notificationStatus = notificationStatus;
		}
		
		@Override
		@JsonProperty(value="asset_strip_id")
		public String getAssetStripId() {
			return assetStripId;
		}
		@JsonSetter(value="asset_strip_id")
		public void setAssetStripId(String assetStripId) {
			this.assetStripId = assetStripId;
		}
		
		@Override
		@JsonProperty(value="event_config_id")
		public Integer getEventConfigId() {
			return eventConfigId;
		}
		@JsonSetter(value="event_config_id")
		public void setEventConfigId(Integer eventConfigId) {
			this.eventConfigId = eventConfigId;
		}
		
		@Override
		@JsonProperty(value="rack_unit_id")
		public String getRackUnitId() {
			return rackUnitId;
		}
		@JsonSetter(value="rack_unit_id")
		public void setRackUnitId(String rackUnitId) {
			this.rackUnitId = rackUnitId;
		}
		
		@JsonProperty(value="params")
		public List<Param> getEventParams() {
			return params;
		}
		@JsonSetter(value="params")
		@JsonDeserialize(contentAs=Param.class)
		public void setEventParams(List<Param> params) {
			this.params = params;
		}
		
		@Override
		@JsonProperty(value="clearing_event_id")
		public String getClearingEventId() {
			return clearingEventId;
		}
		@JsonSetter(value="clearing_event_id")
		public void setClearingEventId(String clearingEventId) {
			this.clearingEventId = clearingEventId;
		}

		/**
		 * An event parameter.
		 */
		@JsonIgnoreProperties(ignoreUnknown=true)
		public static class Param {
			private String id;
			private String eventId;
			private String key;
			private String value;
			
			public Param() {
				super();
				// TODO Auto-generated constructor stub
			}
			
			@JsonProperty(value="id")
			public String getId() {
				return id;
			}
			@JsonSetter(value="id")
			public void setId(String id) {
				this.id = id;
			}
			
			@JsonProperty(value="event_id")
			public String getEventId() {
				return eventId;
			}
			@JsonSetter(value="event_id")
			public void setEventId(String eventId) {
				this.eventId = eventId;
			}
			
			@JsonProperty(value="key")
			public String getKey() {
				return key;
			}
			@JsonSetter(value="key")
			public void setKey(String key) {
				this.key = key;
			}
			
			@JsonProperty(value="value")
			public String getValue() {
				return value;
			}
			@JsonSetter(value="value")
			public void setValue(String value) {
				this.value = value;
			}
		}
		
		/**
		 * Override to support friendly debug logging
		 */
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("[ pdu_id: ");
			buf.append( getPduId() );
			buf.append(", pdu_name: ");
			buf.append( getPduName() );
			buf.append( ", event_config_id: " );
			buf.append( getEventConfigId() );
			buf.append( ", asset_strip_id: " );
			buf.append( getAssetStripId() );
			buf.append( ", rack_unit_id: " );
			buf.append( getAssetStripId() );
			buf.append( ", created_at: " );
			buf.append( getCreatedAt() );
			buf.append( ", notification_status: " );
			buf.append( getNotificationStatus() );
			return buf.toString();
		}
		
		
		@Override
		public int hashCode() {
			return this.getEventId().hashCode();
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof PIQEvent) {
				String eventId = this.getEventId();
				String otherEventId = ((PIQEvent)obj).getEventId();
				if (eventId != null && otherEventId != null) {
					return eventId.equals( otherEventId );
				}
			}
			return false;
		}
	
		/**
		 * Compares event by date.
		 */
		@Override
		public int compareTo(PIQEvent event) {
			if (this.equals(event)) return 0;
			
			Date date = this.getCreatedAt();
			Date oDate = event.getCreatedAt();
			int cmp = date.compareTo( oDate );
			
			if (cmp == 0) {
				String eventId = this.getEventId();
				String otherEventId = event.getEventId();
				if (eventId != null && otherEventId != null) {
					cmp = eventId.compareTo( otherEventId );
				}
				else if (eventId == null && otherEventId != null) {
					cmp = -1;
				}
				else if (eventId != null && otherEventId == null) {
					cmp = 1;
				}
			}
			
			return cmp;
		}
		
		//
		// Convenience methods for identifying event types that we dcTrack cares about
		//
		
		@Override
		public boolean isAssetEvent() {
			return eventConfigId != null &&
				(eventConfigId == ASSET_STRIP_CONNECTED ||
				eventConfigId == ASSET_STRIP_REMOVED ||
				eventConfigId == ASSET_TAG_CONNECTED ||
				eventConfigId == ASSET_TAG_REMOVED);
		}
		
		@Override
		public boolean isAssetTagEvent() {
			return eventConfigId != null &&
				(eventConfigId == ASSET_TAG_CONNECTED ||
				eventConfigId == ASSET_TAG_REMOVED);
		}
		
		@Override
		public boolean isAssetStripEvent() {
			return 
				(eventConfigId == ASSET_STRIP_CONNECTED ||
				eventConfigId == ASSET_STRIP_REMOVED);
		}
		
		@Override
		public String getPduName() {
			String pduName = null;
			if (params != null) {
				for (Param param : params) {
					String key = param.getKey();
					if (key != null && key.equals("pduName")) {
						pduName = param.getValue();
						break;
					}
				}
			}
			return pduName;
		}
	
		@Override
		public Map<String, String> getParams() {
			Map<String, String> params = new HashMap<String, String>();
			
			for (Param param : this.getEventParams()) {
				params.put(param.getKey(), param.getValue());
			}
			
			return params;
		}
	}
}
