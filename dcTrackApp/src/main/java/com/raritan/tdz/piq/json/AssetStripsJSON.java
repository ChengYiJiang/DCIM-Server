package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * A response from the PIQ asset strips API call.
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AssetStripsJSON {

	private List<AssetStrip> assetStrips;
	
	public AssetStripsJSON(){
		super();
	}

	@JsonProperty(value="asset_strips")
	public List<AssetStrip> getAssetStrips() {
		return assetStrips;
	}

	@JsonSetter(value="asset_strips")
	@JsonDeserialize(contentAs=AssetStrip.class)
	public void setAssetStrips(List<AssetStrip> assetStrips) {
		this.assetStrips = assetStrips;
	}
}
