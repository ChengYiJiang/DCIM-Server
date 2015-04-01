/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl.AssetTagStatus;

/**
 * This interface handles the LED status based on the given asset tag status.
 * @author prasanna
 *
 */
public interface AssetStripLEDControl {
	/**
	 * Set the LED based Asset tag status
	 * @param assetTagId - Asset Tag ID
	 * @param status - Status which determines the color of the LED
	 */
	public void setLED (String rackUnitId, AssetTagStatus status);
	
	/**
	 * Turn Off the LED 
	 * @param assetTagId - Asset Tag ID
	 */
	public void turnOffLED (String rackUnitNumber); 
}
