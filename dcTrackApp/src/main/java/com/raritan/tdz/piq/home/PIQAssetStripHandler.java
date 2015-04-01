package com.raritan.tdz.piq.home;

import org.apache.commons.lang.WordUtils;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.json.AssetStrip;
import com.raritan.tdz.piq.json.SensorBase;

public class PIQAssetStripHandler extends PIQSensorHandlerBase {

	@Override
	public boolean canProcess(SensorBase sb) {
		AssetStrip as = (AssetStrip)sb;
		return (as.getState().equals("available") == true);
	}

	@Override
	protected void setSensorSpecificData(SensorPort sp, Item item, SensorBase sb, Errors errors) {

		AssetStrip as = (AssetStrip)sb;
		as.setAttributeName("ASSET_STRIP");
		setSensorName(sp, item, as.getName(), as.getAttributeName(), as.getOrdinal(), errors);
		
		sp.setStatusActual (WordUtils.capitalizeFully(as.getState()));
		sp.setValueActual(-1.0);
	}
	
	protected Long getPortSubClass(SensorBase sb) {
		return SystemLookup.PortSubClass.ASSET_STRIP;
	}

}

