package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * A response from the PIQ inlets API call for information on multiple inlets (as for a single PDU).
 * Example: https://vm1/api/v2/pdu/3/inlets
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class InletsJSON {

	private List<Inlet> inlets;

	public InletsJSON() {
		super();
	}

	@JsonProperty(value="inlets")
	public List<Inlet> getInlets() {
		return inlets;
	}

	@JsonSetter(value="inlets")
	@JsonDeserialize(contentAs=Inlet.class)
	public void setInlets(List<Inlet> inlets) {
		this.inlets = inlets;
	}
}
