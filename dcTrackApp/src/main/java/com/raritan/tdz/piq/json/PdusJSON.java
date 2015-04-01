/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * List of PDUs as returned by the pdus search API.
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PdusJSON {
	private List<Pdu> pdus;
	
	public PdusJSON() {
		super();
	}

	@JsonProperty(value="pdus")
	public List<Pdu> getPdus() {
		return pdus;
	}
	
	@JsonSetter(value="pdus")
	@JsonDeserialize(contentAs=Pdu.class)
	public void setPdus(List<Pdu> pdus) {
		this.pdus = pdus;
	}
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Pdu {
		private Integer id;
		private Integer proxyIndex;
		
		public Pdu() {
			super();
		}

		@JsonProperty(value="id")
		public Integer getId() {
			return id;
		}

		@JsonSetter(value="id")
		public void getId(Integer id) {
			this.id = id;
		}
		
		@JsonProperty(value="proxy_index")
		public Integer getProxyIndex() {
			return proxyIndex;
		}
		
		@JsonSetter(value="proxy_index")
		public void setProxyIndex(Integer proxyIndex) {
			this.proxyIndex = proxyIndex;
		}
	}
}
