package com.raritan.tdz.piq.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;

public interface PIQSyncPorts {
	
	public static enum TYPE { 
		INLET {
			@Override
			public String toString() { return "Inlets"; };
			
			@Override
			public String getTypeString() { return "INLET"; }
		}, INLET_POLE {
			@Override
			public String toString() { return "Inlet Poles"; };
			
			@Override
			public String getTypeString() { return "INLET_POLE"; }
		}, OUTLET {
			@Override
			public String toString() { return "Outlets"; }

			@Override
			public String getTypeString() {
				return "OUTLET";
			};
		}, SENSOR {
			@Override
			public String toString() { return "Sensors"; }

			@Override
			public String getTypeString() {
				return "SENSOR";
			};
		}, ASSET_STRIP {
			@Override
			public String toString() { return "Asset Strips"; }

			@Override
			public String getTypeString() {
				return "ASSET_STRIP";
			};
		}, ALL {
			@Override
			public String toString() { return "All"; }

			@Override
			public String getTypeString() {
				return "ALL";
			};
		}, UNKNOWN {
			@Override
			public String toString() { return "UNKNOWN"; }

			@Override
			public String getTypeString() {
				return "UNKNOWN";
			};
		};
		
		public abstract String getTypeString();
		public static TYPE fromString(String type){
			TYPE ret = UNKNOWN;
			if (type.equals(INLET.getTypeString())) ret = INLET;
			else if (type.equals(INLET_POLE.getTypeString())) ret = INLET_POLE;
			else if (type.equals(OUTLET.getTypeString())) ret = OUTLET;
			else if (type.equals(SENSOR.getTypeString())) ret = SENSOR;
			else if (type.equals(ASSET_STRIP.getTypeString())) ret = ASSET_STRIP;
			else if (type.equals(ALL.getTypeString())) ret = ALL;
			else ret = UNKNOWN;
			return ret;
		}
	}
	
	public void syncPortReadings(List<Item> items, Errors errors) throws DataAccessException, RemoteDataAccessException, BusinessValidationException;
	
	/**
	 * Sync all port readings
	 * @param chunkLimit TODO
	 * @param errors - will be filled with any errors encountered.
	 * @throws DataAccessException
	 * @throws RemoteDataAccessException
	 */
	public void syncAllPortReadings(int chunkLimit, Errors errors) throws DataAccessException, RemoteDataAccessException;
}
