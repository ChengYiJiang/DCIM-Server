package com.raritan.tdz.piq.home;

import java.util.Date;
import java.util.Map;

/**
 * A PIQ event.
 * @author Andrew Cohen
 */
public interface PIQEvent extends Comparable<PIQEvent> {

	// PIQ specific codes for the event type - specified in the "event_config_id" property
	public static final int ASSET_STRIP_CONNECTED = 82;
	public static final int ASSET_STRIP_REMOVED = 79;
	public static final int ASSET_TAG_CONNECTED = 73;
	public static final int ASSET_TAG_REMOVED = 74;
	
	public String getEventId();

	public Date getCreatedAt();

	public String getPduId();

	public String getPduName();

	public String getNotificationStatus();

	public String getAssetStripId();

	public Integer getEventConfigId();

	public String getRackUnitId();
	
	public Map<String, String> getParams();
	
	public Date getClearedAt();
	
	public String getClearingEventId();
	
	public boolean isAssetEvent();

	public boolean isAssetTagEvent();

	public boolean isAssetStripEvent();

}