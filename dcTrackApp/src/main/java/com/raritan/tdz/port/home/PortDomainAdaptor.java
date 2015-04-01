package com.raritan.tdz.port.home;

import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.dto.PortInterface;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public interface PortDomainAdaptor {
	
	/**
	 * update the port 
	 * @param itemObj
	 * @param portId
	 * @param portDto
	 * @return
	 * @throws DataAccessException
	 */
	public IPortInfo convertOnePortForAnItem( Object itemObj, Long portId, PortInterface portDto) throws DataAccessException;
	
	/**
	 * remove the port information from the item
	 * @param item
	 * @param dataPortId
	 * @throws BusinessValidationException
	 */
	public void deletePort(Item item, long dataPortId) throws BusinessValidationException;

	/**
	 * delete one port from the given item
	 * @param item
	 * @param dto
	 * @throws BusinessValidationException
	 */
	public void deleteOnePortForAnItem(Item item, PortInterface dto) throws BusinessValidationException;
	
}
