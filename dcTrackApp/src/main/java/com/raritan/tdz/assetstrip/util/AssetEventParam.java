package com.raritan.tdz.assetstrip.util;

import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.EventParam;

/**
 * dcTrack asset event specific parameters. 
 * The toString method is overridden to return parameter name exactly as it comes from PIQ.
 * A display name is also provided which will be displayed in the Event detail view of the GUI.
 * 
 * @author Andrew Cohen
 */
public enum AssetEventParam {
	
	CREATED_AT("created_at") {
		@Override
		public String getDisplayName() {
			return "Created At";
		}
	},
	
	PDU_ID("pdu_id") {
		@Override
		public String getDisplayName() {
			//return "PDU ID";
			return null;
		}
	},
	
	PDU_NAME("pduName") {
		@Override
		public String getDisplayName() {
			return "PDU Name";
		}
	},
	
	NOTIFICATION_STATUS("notification_status") {
		@Override
		public String getDisplayName() {
			//return "Notification Status";
			return null;
		}
	},
	
	ASSET_STRIP_ID("asset_strip_id") {
		@Override
		public String getDisplayName() {
			//return "Asset Strip ID";
			return null;
		}
	},
	
	ASSET_STRIP_STATE("assetStripState") {
		@Override
		public String getDisplayName() {
			//return "Asset Strip State";
			return null;
		}
	},
	
	ASSET_STRIP_NUMBER("assetStripNumber") {
		@Override
		public String getDisplayName() {
			return "Asset Strip Number";
		}
	},
	
	ASSET_STRIP_NAME("name") {
		@Override
		public String getDisplayName() {
			return "Asset Strip Name";
		}
	},
	
	ASSET_TAG_ID("assetTagId") {
		@Override
		public String getDisplayName() {
			return "Asset Tag ID";
		}
	},
	
	EVENT_CONFIG_ID("event_config_id") {
		@Override
		public String getDisplayName() {
			//return "Event Configuration ID";
			return null;
		}
	},
	
	ASSET_TAG_RACKUNIT_NUMBER("rackUnitNumber") {
		@Override
		public String getDisplayName() {
			return "U Position";
		}
	},
	
	ASSET_TAG_RACKUNIT_ID("rack_unit_id") {
		@Override
		public String getDisplayName() {
			//return "Rack Unit ID";
			return null;
		}
	};
	
	@Override
	public String toString() {
		return piqName;
	}
	
	/**
	 * Returns a display name for the PIQ parameter.
	 * If null, this parameter will be set to hidden in the database.
	 * @return string
	 */
	public abstract String getDisplayName();
	
	
	/**
	 * Add this parameter to the specified event.
	 * @param event the event
	 * @param value the parameter value
	 */
	public void addToEvent(Event event, String value) {
		EventParam param = event.addParam(toString(), value);
		String displayName = getDisplayName();
		
		if (displayName != null) {
			param.setDisplayName( displayName );
		}
		else {
			// Do not show asset parameters without a display name
			param.setDisplayable( false );
		}
	}
	
	/**
	 * Get the Asset event parameter by name.
	 * @param piqName the PIQ parameter name
	 * @return
	 */
	public static AssetEventParam get(String piqName) {
		return lookup.get( piqName );
	}
	
	/** A Lookup map for Asset Tag parameters by their "toString" value */
	private static final Map<String, AssetEventParam> lookup = new HashMap<String, AssetEventParam>();
	
	// Initialize lookup map
	static {
		for (AssetEventParam p : AssetEventParam.values()) {
			lookup.put(p.toString(), p);
		}
	}
	
	/** The name of the parameter as it is returned from PIQ */
	private String piqName;
	
	private AssetEventParam(String name) {
		this.piqName = name;
	}
}
