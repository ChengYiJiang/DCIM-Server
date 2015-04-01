/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl.AssetTagStatus;
import com.raritan.tdz.piq.home.PIQAssetStripClient;
import com.raritan.tdz.piq.home.PIQAssetStripClient.LedColor;
import com.raritan.tdz.piq.home.PIQAssetStripClient.LedOnState;

/**
 * @author prasanna
 *
 */
public class AssetStripLEDControlImpl implements AssetStripLEDControl {
	
	private PIQAssetStripClient piqAssetStripClient = null;
	
	public AssetStripLEDControlImpl(PIQAssetStripClient piqAssetStripClient) {
		this.setPiqAssetStripClient(piqAssetStripClient);
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.assetstrip.home.AssetStripLEDControl#setLED(java.lang.String, com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl.AssetTagStatus)
	 */
	@Override
	public void setLED(String rackUnitNumber, AssetTagStatus status) {
		// FIXME: Enable below when PIQ interface for setting LEDs is working
		/*
		switch (status){
		case ASSET_AUTHORIZED:
			//TODO: call the PIQClient and set the LED status to Green.
			piqAssetStripClient.setLedOn(new Integer(rackUnitNumber), LedOnState.SOLID, LedColor.GREEN);
			System.out.println("setLED Green" );
			break;
		case ASSET_UNAUTHORIZED:
		case ASSET_CONFLICT_ASSET_TAG:
		case ASSET_CONFLICT_UPOSITION:
		case ASSET_UNKNOWN:
		default:
			//TODO: call the PIQClient and set the LED status to Red + Blink
			piqAssetStripClient.setLedOn(new Integer(rackUnitNumber), LedOnState.BLINKING, LedColor.RED);
			System.out.println("setLED Red" );
		}
		*/
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.assetstrip.home.AssetStripLEDControl#turnOffLED(java.lang.String)
	 */
	@Override
	public void turnOffLED(String rackUnitNumber) {
		// FIXME: Enable below when PIQ interface for setting LEDs is working
		//piqAssetStripClient.setLedOff(new Integer(rackUnitNumber));
	}

	public PIQAssetStripClient getPiqAssetStripClient() {
		return piqAssetStripClient;
	}

	public void setPiqAssetStripClient(PIQAssetStripClient piqAssetStripClient) {
		this.piqAssetStripClient = piqAssetStripClient;
	}

}
