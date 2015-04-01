/**
 * 
 */
package com.raritan.tdz.piq.dto;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

import com.raritan.tdz.piq.home.PIQSyncPorts.TYPE;

/**
 * @author prasanna
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class SyncAllPDUReadingsStatusDTO {
	private Long locationId;
	private String locationName;
	private String powerIQHost;
	private List<String> types;
	private String lastUpdatedTimeStamp;
	
	public SyncAllPDUReadingsStatusDTO() {
		
	}
	
	public SyncAllPDUReadingsStatusDTO(Long locationId, String locationName,
			String powerIQHost, List<String> types, String lastUpdatedTimeStamp) {
		super();
		this.locationId = locationId;
		this.locationName = locationName;
		this.powerIQHost = powerIQHost;
		this.types = types;
		this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
	}
	
	@JsonProperty("locationId")
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	
	@JsonProperty("locationCode")
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	
	@JsonProperty("host")
	public String getPowerIQHost() {
		return powerIQHost;
	}
	public void setPowerIQHost(String powerIQHost) {
		this.powerIQHost = powerIQHost;
	}
	
	@JsonProperty("types")
	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	
	@JsonProperty("lastUpdated")
	public String getLastUpdatedTimeStamp() {
		return lastUpdatedTimeStamp;
	}
	public void setLastUpdatedTimeStamp(String lastUpdatedTimeStamp) {
		this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
	}
	
	
}
