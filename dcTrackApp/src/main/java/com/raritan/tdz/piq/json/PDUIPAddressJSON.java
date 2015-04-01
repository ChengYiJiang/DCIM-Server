package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown=true)
public class PDUIPAddressJSON {
	private List<PduIpAddress> pduIpAddresses = null;

	public PDUIPAddressJSON() {
		super();
	}

	public PDUIPAddressJSON(List<PduIpAddress> pduIpAddresses) {
		this.pduIpAddresses =  pduIpAddresses;
	}
	
	@JsonProperty(value="ip_addresses")
	public List<PduIpAddress> getPduIpAddresses() {
		return pduIpAddresses;
	}

	@JsonSetter(value="ip_addresses")
	@JsonDeserialize(contentAs=PduIpAddress.class)
	public void setPduIpAddresses(List<PduIpAddress> pduIpAddresses) {
		this.pduIpAddresses = pduIpAddresses;
	}
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class PduIpAddress {
		String oldIpAddress;
		String newIpAddress;

		public PduIpAddress () {
			super();
		}
		
		public PduIpAddress (String oldIpAddress, String newIpAddress) {
			this.oldIpAddress = oldIpAddress;
			this.newIpAddress = newIpAddress;
		}
		
		@JsonProperty(value="old_ip_address")
		public String getOldIpAddress() {
			return oldIpAddress;
		}
		
		@JsonSetter(value="old_ip_address")
		public void setOldIpAddress(String oldIpAddress) {
			this.oldIpAddress = oldIpAddress;
		}
		
		@JsonProperty(value="new_ip_address")
		public String getNewIpAddress() {
			return newIpAddress;
		}
		
		@JsonSetter(value="new_ip_address")
		public void setNewIpAddress(String newIpAddress) {
			this.newIpAddress = newIpAddress;
		}
	}

}
