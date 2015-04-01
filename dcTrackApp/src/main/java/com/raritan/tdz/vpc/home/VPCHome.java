/**
 * 
 */
package com.raritan.tdz.vpc.home;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

/**
 * This contains APIs that can be used to manage Virtual Power Chains (a.k.a VPC)
 * @author prasanna
 *
 */
public interface VPCHome {
	/**
	 * This creates a Virtual Power Chain given a location and persists it.
	 * @param locationId
	 * @param userInfo 
	 * @throws BusinessValidationException
	 */
	public void create(Long locationId, UserInfo userInfo) throws BusinessValidationException;
	
	/**
	 * This deletes the Virtual Power Chain. This assumes that the VPC partial circuits
	 * are deleted.
	 * @param locationId
	 * @param userInfo 
	 * @throws BusinessValidationException
	 * @throws Throwable 
	 * @throws ClassNotFoundException 
	 */
	public void delete(Long locationId, UserInfo userInfo) throws BusinessValidationException, ClassNotFoundException, Throwable;
	
	/**
	 * <p>This creates a VPC Partial circuit with the outlet socket created automatically.</p>
	 * <p>If the VPC Partial circuit already exists, then it just creates the outlet socket
	 * and return back the VPC Partial circuit.</p>
	 * @param vpcPath
	 * @param locationId
	 * @param inputPortId - input port Id to create a compatible power outlet
	 * @param userInfo 
	 * @return
	 * @throws BusinessValidationException
	 * @throws DataAccessException 
	 * @throws NumberFormatException 
	 */
	public PowerCircuit getVPCPartialCircuit(String vpcPath, Long locationId, Long inputPortId, UserInfo userInfo) throws BusinessValidationException, NumberFormatException, DataAccessException;

	/**
	 * update the vpc power chain with the new settings
	 * @param locationId
	 * @param info
	 * @throws ClassNotFoundException
	 * @throws BusinessValidationException
	 * @throws Throwable
	 */
	public void update(Long locationId, UserInfo info) throws ClassNotFoundException, BusinessValidationException, Throwable;

	/**
	 * create a power circuit that connects from the input port to the VPC in the given chain and location
	 * @param vpcPath
	 * @param locationId
	 * @param inputPortId
	 * @param userInfo
	 * @return
	 * @throws BusinessValidationException
	 * @throws NumberFormatException
	 * @throws DataAccessException
	 */
	public PowerCircuit createVPCCircuit(String vpcPath, Long locationId, Long inputPortId, UserInfo userInfo) 
			throws BusinessValidationException, NumberFormatException, DataAccessException;

	/**
	 * creates new power port and connect to the branch circuit breaker port
	 * @param srcPortId
	 * @param locationId
	 * @param vpcChain
	 * @param errors
	 * @return
	 */
	public PowerPort createPowerOutletPortAndConnection(Long srcPortId, Long locationId, String vpcChain, Errors errors);

	/**
	 * creates new power outlet port 
	 * @param srcPortId
	 * @param locationId
	 * @param vpcChain
	 * @param errors
	 * @return
	 */
	public PowerPort createPowerOutletPort(Long srcPortId, Long locationId, String vpcChain, Errors errors);

	/**
	 * create the poewr outlet port on the power outlet item
	 * @param itemId
	 * @param srcPortId
	 * @param errors
	 * @return
	 */
	public PowerPort createPowerOutletPort(Long itemId, Long srcPortId, Errors errors);

	/**
	 * deletes all but one unused VPC power outlet port(s)
	 */
	public void clearPowerOutletPort();
	
}
