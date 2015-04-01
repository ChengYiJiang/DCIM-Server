package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * A response from the PIQ outlets API call for information on a single outlet.
 * Example: https://vm1/api/v2/outlets/3
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class OutletJSON {

	private Outlet outlet;
	
	public OutletJSON() {
		super();
	}
	
	public OutletJSON(Outlet outlet) {
		setOutlet( outlet );
	}
	
	@JsonProperty(value="outlet")
	public Outlet getOutlet() {
		return outlet;
	}

	@JsonSetter(value="outlet")
	public void setOutlet(Outlet outlet) {
		this.outlet = outlet;
	}
}
