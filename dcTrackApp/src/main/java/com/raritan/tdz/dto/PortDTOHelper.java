package com.raritan.tdz.dto;

import java.util.List;

import com.raritan.tdz.domain.Item;

/**
 * helper class to support API related to port DTO
 * @author bunty
 *
 * @param <T>
 */
public interface PortDTOHelper<T> {

	public List<T> getPortDTOList(Item item);
	
}
