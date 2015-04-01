package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * A response from the PIQ inlet poles API call for information on multiple inlet poles (as for a single PDU).
 * Example: https://vm1/api/v2/pdu/3/inlet_poles
 * @author bunty
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public class InletPolesJSON {
	private List<InletPole> inletPoles;

	public InletPolesJSON() {
		super();
	}

	@JsonProperty(value="inlet_poles")
	public List<InletPole> getInletPoles() {
		return inletPoles;
	}

	@JsonSetter(value="inlet_poles")
	@JsonDeserialize(contentAs=InletPole.class)
	public void setInletPoles(List<InletPole> inletPoles) {
		this.inletPoles = inletPoles;
	}

}
