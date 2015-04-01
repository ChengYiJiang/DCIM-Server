package com.raritan.tdz.events.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raritan.tdz.domain.LksData;

/**
 * The domain object encapsulating the details associated with a particular event.
 * 
 * @author Andrew Cohen
 */

@Entity
@Table(name="`dct_event_params`")
public class EventParam {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_event_params_seq")
	@SequenceGenerator(name="dct_event_params_seq", sequenceName="dct_event_params_event_params_id_seq", allocationSize=1)
	@Column(name = "event_params_id", unique = true, nullable = false)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;
	
	@Column(name = "param_name", nullable = true)
	private String name;
	
	@Column(name = "param_value", nullable = true)
	private String value;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "value_lks_id", nullable = true)
	private LksData valueLks;
	
	@Column(name = "displayable", nullable = false)
	private boolean displayable;
	
	@Column(name = "display_name", nullable = true)
	private String displayName;
	
	EventParam(Event event, String name, String value) {
		this.event = event;
		this.name = name;
		this.value = value;
		this.valueLks = null;
		this.displayable = true;
		this.displayName = null;
	}
	
	EventParam(Event event, LksData valueLks) {
		this.event = event;
		this.name = null;
		this.value = null;
		this.valueLks = valueLks;
		this.displayable = true;
		this.displayName = null;
	}
	
	public EventParam() {
		this.displayable = true;
	}
	
	public long getId() {
		return id;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public String getName() {
		if (name != null)
			return name;
		if (valueLks != null)
			return valueLks.getLkpTypeName();
		return null;
	}
	
	public String getValue() {
		if (value != null) 
			return value;
		if (valueLks != null)
			return valueLks.getLkpValue();
		return null;
	}

	public boolean isDisplayable() {
		return displayable;
	}

	public void setDisplayable(boolean displayable) {
		this.displayable = displayable;
	}

	public String getDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		return getName();
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public EventParam(long id, Event event, String name, String value,
			LksData valueLks, boolean displayable, String displayName) {
		super();
		this.id = id;
		this.event = event;
		this.name = name;
		this.value = value;
		this.valueLks = valueLks;
		this.displayable = displayable;
		this.displayName = displayName;
	}
	
	
	
}
