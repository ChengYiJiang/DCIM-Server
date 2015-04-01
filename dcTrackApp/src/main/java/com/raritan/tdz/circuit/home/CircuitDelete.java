package com.raritan.tdz.circuit.home;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;

public interface CircuitDelete {

	/**
	 * Delete the power circuits from the system
	 * @param circuitIdsToBeDeleted
	 * @throws DataAccessException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public abstract Long deletePowerCircuitByIds(
			Collection<Long> circuitIdsToBeDeleted, boolean isUpdate)
			throws DataAccessException, BusinessValidationException;

	@Transactional(propagation = Propagation.REQUIRED)
	public abstract Long deleteDataCircuitByIds(
			Collection<Long> circuitIdsToBeDeleted, boolean isUpdate)
			throws DataAccessException, BusinessValidationException;

}