package com.raritan.tdz.powerchain.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;

public interface PortAdaptorHelper {

	/**
	 * Adapts the values in the MeItem and creates new port
	 * @param item
	 * @param portSubClass
	 * @param errorCodeInvalidClass
	 * @param errors
	 * @return
	 */
	PowerPort convertUniquePortForMeItem(Item item, Long portSubClass,
			String errorCodeInvalidClass, Errors errors);

	/**
	 * Adapts the values in the MeItem and updates the existing port if exist or creates a new port
	 * @param item
	 * @param portSubClass
	 * @param errorCodeInvalidClass
	 * @param errors
	 * @return
	 */
	PowerPort convertSingletonPortForMeItem(Item item, Long portSubClass,
			String errorCodeInvalidClass, Errors errors);

	/**
	 * update the port voltage using the line volt of me item
	 * @param item
	 * @param port
	 * @param errorCodeInvalidClass
	 * @param errors
	 * @return
	 */
	PowerPort updateVoltUsingLineVolt(Item item, PowerPort port,
			String errorCodeInvalidClass, Errors errors);

	/**
	 * update the port voltage using the rating volt of me item
	 * @param item
	 * @param port
	 * @param errorCodeInvalidClass
	 * @param errors
	 * @return
	 */
	PowerPort updateVoltUsingRatingVolt(Item item, PowerPort port,
			String errorCodeInvalidClass, Errors errors);

	/**
	 * update the port rated amps using the rating kva and rated volt
	 * @param item
	 * @param port
	 * @param errorCodeInvalidClass
	 * @param errors
	 * @return
	 */
	PowerPort updateAmpsRatedUsingKVA(Item item, PowerPort port,
			String errorCodeInvalidClass, Errors errors);

	/**
	 * set the port name
	 * @param port
	 * @param portName
	 * @return
	 */
	PowerPort updatePortName(PowerPort port, String portName);
	
	/**
	 * update the used flag based on the number of source connected
	 * @param port
	 * @return
	 */
	PowerPort updateUsedFlag(PowerPort port, PowerPort oldSrcPort);
	
	/**
	 * update the used flag to the provided value
	 * @param port
	 * @param value
	 * @return
	 */
	PowerPort updateUsedFlag(PowerPort port, boolean value);

	public PowerPort updatePhaseLookup(Item item, PowerPort port, String string,
			Errors errors);

	public PowerPort updateAmpsRatedUsingRatingAmps(Item item, PowerPort port,
			String string, Errors errors);

	/**
	 * update the voltage value using the destination connected port.
	 * Note that if this port is connected to more than one port, it will not update the voltage
	 * @param port
	 * @param errors
	 * @return
	 */
	PowerPort updateVoltUsingDestPort(PowerPort port, Errors errors);

	/**
	 * update the phase value using the destination connected port.
	 * Note that if this port is connected to more than one port, it will not update the phase
	 * @param port
	 * @param errors
	 * @return
	 */
	PowerPort updatePhaseUsingDestPort(PowerPort port, Errors errors);

	/**
	 * update the rated amps for the port using the amps of the destination port
	 * @param port
	 * @param errors
	 * @return
	 */
	PowerPort updateAmpsRatedUsingDestPort(PowerPort port, Errors errors);

}
