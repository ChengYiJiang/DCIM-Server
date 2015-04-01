package com.raritan.tdz.powerchain.home;

import java.util.Set;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerConnection;

/**
 * operates on a connection (power or data)
 * @author bunty
 *
 */
public interface PortConnection {
	
	/**
	 * creates a connection from source to destination port
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 * @return
	 */
	ICircuitConnection create(IPortInfo srcPort, IPortInfo destPort, Errors errors);
	
	/**
	 * update the connection from the source to the new destination
	 * @param item
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 * @return
	 */
	ICircuitConnection update(Item item, IPortInfo srcPort, IPortInfo destPort, Errors errors);
	
	/**
	 * delete connection from source to destination port
	 * @param item
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 */
	void delete(Item item, IPortInfo srcPort, IPortInfo destPort, Errors errors);
	
	/**
	 * delete connections with source as srcPort
	 * @param item
	 * @param srcPort
	 * @param errors
	 */
	void deleteSource(Item item, IPortInfo srcPort, Errors errors);
	
	/**
	 * delete connections with destination as destPort
	 * @param item
	 * @param destPort
	 * @param errors
	 */
	void deleteDestination(Item item, IPortInfo destPort, Errors errors);
	
	/**
	 * informs if the connection already exist between ports 
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 * @return
	 */
	boolean connectionExist(IPortInfo srcPort, IPortInfo destPort, Errors errors);

	/**
	 * informs if any connection exist from the given source
	 * @param srcPort
	 * @param errors
	 * @return
	 */
	boolean connectionExist(IPortInfo srcPort, Errors errors);

	/**
	 * returns the destination port of the given source port
	 * @param item
	 * @param srcPort
	 * @return
	 */
	IPortInfo getDestPort(Item item, IPortInfo srcPort);

	/**
	 * get the source power connections
	 * @param srcPort
	 * @return
	 */
	Set<PowerConnection> getPowerConnection(IPortInfo srcPort);

	/**
	 * gets the connection between the ports, if exist 
	 * @param srcPort
	 * @param destPort
	 * @param errors
	 * @return
	 */
	ICircuitConnection getConnection(IPortInfo srcPort, IPortInfo destPort, Errors errors);

	/**
	 * validates the connection
	 * @param connection
	 * @param errors
	 */
	void validate(ICircuitConnection connection, Errors errors);
	
}
