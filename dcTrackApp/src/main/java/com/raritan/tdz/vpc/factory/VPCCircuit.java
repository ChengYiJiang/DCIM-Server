package com.raritan.tdz.vpc.factory;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

/**
 * create circuit using VPC
 * @author bunty
 *
 */
public interface VPCCircuit {

	/**
	 * creates a new power circuit from the input cord to the vpc port in the given location and chain
	 * @param srcPortId
	 * @param locationId
	 * @param vpcChain
	 * @param errors 
	 * @param userInfo 
	 * @return new port created in the vpc 
	 * @throws DataAccessException 
	 * @throws NumberFormatException 
	 */
	public PowerCircuit create(Long srcPortId, Long locationId, String vpcChain, Errors errors, UserInfo userInfo) 
			throws NumberFormatException, DataAccessException;

	/**
	 * create a new power circuit starting from the power outlet. This creates a new port in the power outlet
	 * with matching connector as the source port.
	 * @param srcPortId
	 * @param locationId
	 * @param vpcChain
	 * @param errors
	 * @param userInfo
	 * @return
	 * @throws NumberFormatException
	 * @throws DataAccessException
	 */
	public PowerCircuit createFromPort(Long srcPortId, Long locationId, 	String vpcChain, Errors errors, UserInfo userInfo)
			throws NumberFormatException, DataAccessException;

	/**
	 * create power outlet port and connection to the branch circuit breaker
	 * @param srcPort
	 * @param locationId
	 * @param vpcChain
	 * @param errors
	 * @return
	 */
	public PowerPort createPortAndConnection(Long srcPortId, 	Long locationId, String vpcChain, Errors errors);

	/**
	 * create the port using the source port at a given location and chain
	 * @param srcPortId
	 * @param locationId
	 * @param vpcChain
	 * @param errors
	 * @return
	 */
	public PowerPort createPort(Long srcPortId, Long locationId, String vpcChain, Errors errors);
	
}
