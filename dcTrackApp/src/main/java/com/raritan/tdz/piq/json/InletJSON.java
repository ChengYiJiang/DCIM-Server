package com.raritan.tdz.piq.json;

 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * A response from the PIQ inlets API call for information on a single inlet.
 * Example: https://vm1/api/v2/inlets/2
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class InletJSON {

	private Inlet inlet;
	
	public InletJSON() {
		super();
	}
	
	public InletJSON(Inlet inlet) {
		setInlet( inlet );
	}
	
	@JsonProperty(value="inlet")
	public Inlet getInlet() {
		return inlet;
	}

	@JsonSetter(value="inlet")
	public void setInlet(Inlet inlet) {
		this.inlet = inlet;
	}
}
