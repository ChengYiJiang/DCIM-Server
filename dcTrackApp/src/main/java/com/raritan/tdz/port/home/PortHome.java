package com.raritan.tdz.port.home;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.exception.DataAccessException;

/**
 * An extension of the Ports business layer from ManageAssetsService.
 * @author Andrew Cohen
 */
public interface PortHome extends com.raritan.tdz.home.PortHome {

	public void lockPort(IPortInfo port) throws DataAccessException;
	
	public void unlockPort(IPortInfo port) throws DataAccessException;
	
}
