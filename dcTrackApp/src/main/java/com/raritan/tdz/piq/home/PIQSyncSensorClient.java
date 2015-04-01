package com.raritan.tdz.piq.home;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.piq.json.Sensor;


/**
 * An interface for synchronizing sensor connections in dcTrack with those in PIQ.
 * @author basker
 */
public interface PIQSyncSensorClient extends PIQRestClient {

	/**
	 * Find a specific sensor connected to a PDU in PIQ.
	 * @param pduPiqId the PIQ ID of the PDU
	 * @param sensor ordinal (sensor number)
	 * @return
	 * @throws RemoteDataAccessException
	 */
	public Sensor findSensor(long pduPiqId, String address, int ordinal) throws RemoteDataAccessException;
	
	/**
	 * Gets a specific Sensor by its unique id in PIQ.
	 * @param ordinal (sensor number)
	 * @return the sensor or null if the sensor doesn't exist.
	 * @throws RemoteDataAccessException
	 */
	public Sensor getSensor(long ordinal /*sensor number*/) throws RemoteDataAccessException;
	
	/**
	 * Link PDU sensors discovered during PIQ integration
	 * @param pduItem - PDU Item added to dcTrack
	 * @throws BusinessValidationException 
	 */
	public void linkSensorPorts(Item probeItem, Item pduItem);

}
