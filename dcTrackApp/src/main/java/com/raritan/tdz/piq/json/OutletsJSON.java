package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * A response from the PIQ outlets API call for information on multiple outlets (as for a single PDU).
 * Example: https://vm1/api/v2/pdu/3/outlets
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class OutletsJSON {

	private List<Outlet> outlets;

	public OutletsJSON() {
		super();
	}

	@JsonProperty(value="outlets")
	public List<Outlet> getOutlets() {
		return outlets;
	}

	@JsonSetter(value="outlets")
	@JsonDeserialize(contentAs=Outlet.class)
	public void setOutlets(List<Outlet> outlets) {
		this.outlets = outlets;
	}
}
