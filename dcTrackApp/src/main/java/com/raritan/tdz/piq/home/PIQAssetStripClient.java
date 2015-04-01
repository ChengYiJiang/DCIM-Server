package com.raritan.tdz.piq.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.AssetStrip;



/**
 * A REST client for interfacing with asset strip specific PIQ services.
 * 
 * @author Andrew Cohen
 */
public interface PIQAssetStripClient extends PIQRestClient {
	
	/**
	 * Gets an asset strips associated with a PDU.
	 * @param piqId PIQ ID of the PDU associated with the asset strip
	 * @return
	 */
	public List<AssetStrip> getAssetStrips(String piqId) throws RemoteDataAccessException;
	
	/**
	 * Turns on the LED for the specified RU of a particular asset strip.
	 * @param rackUnitId the unique rack unit number identifying the uPosition on a particular asset strip
	 * @param state - ON state of the LED
	 * @param color - color of the LED
	 */
	public void setLedOn(int rackUnitId, LedOnState state, LedColor color) throws RemoteDataAccessException;
	
	/**
	 *  Turns off the LED for the specified RU of a particular asset strip.
	 * @param rackUnitId
	 */
	public void setLedOff(int rackUnitId) throws RemoteDataAccessException;

	/**
	 * Synchronize Asset strip in PIQ with dcTrack.
	 * @param items
	 * @param errors
	 * @throws DataAccessException
	 * @throws RemoteDataAccessException
	 */
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException, RemoteDataAccessException;

	
	/**
	 * LED ON states
	 */
	public enum LedOnState {
		SOLID {
			public String toString() {
				return "on";
			}
		},
		BLINKING {
			public String toString() {
				return "blinking";
			}
		};
	};
	
	/**
	 * LED colors we will set.
	 */
	public enum LedColor {
		RED {
			public String toString() {
				return "FF0000";
			}
		},
		GREEN {
			public String toString() {
				return "00C000";
			}
		};
	};
	
}
