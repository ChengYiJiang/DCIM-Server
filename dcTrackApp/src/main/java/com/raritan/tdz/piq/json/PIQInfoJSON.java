package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class PIQInfoJSON {
	
	private SystemInfo sysInfo;
	
	PIQInfoJSON() {
		super();
	}
	
	@JsonProperty(value="system_info")

	public SystemInfo getSysInfo() {
		return sysInfo;
	}

	public String getPowerIqVersion() {
		return sysInfo.getPowerIqVersion();
	}
	
	@JsonSetter(value="system_info")
	public void setSysInfo(SystemInfo sysInfo) {
		this.sysInfo = sysInfo;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class SystemInfo {
		String powerIqVersion;
	
		public SystemInfo() {
			super();
		}

		@JsonProperty(value="poweriq_version")
		public String getPowerIqVersion() {
			return powerIqVersion;
		}
	
		@JsonSetter(value="poweriq_version")
		public void setPowerIqVersion(String powerIqVersion) {
			this.powerIqVersion = powerIqVersion;
		}
	}
	
}
