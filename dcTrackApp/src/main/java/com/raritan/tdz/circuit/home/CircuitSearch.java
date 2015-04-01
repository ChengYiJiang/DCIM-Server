package com.raritan.tdz.circuit.home;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.exception.DataAccessException;

/**
 * Search Service for circuits.
 * @author Andrew Cohen
 */
public interface CircuitSearch {

	/**
	 * Search the list of circuits and return DTOs.
	 * @param cCriteria criteria object
	 * @param reqInfoData
	 * @param reqInfoPower
	 * @param sharedDataCircuitTraces
	 * @param sharedPowerCircuitTraces
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<CircuitListDTO> searchCircuits(CircuitCriteriaDTO cCriteria,
			Map<Long, CircuitRequestInfo> reqInfoData,
			Map<Long, CircuitRequestInfo> reqInfoPower,
			Set<String> sharedDataCircuitTraces,
			Set<String> sharedPowerCircuitTraces) throws DataAccessException;
	
	
	/**
	 * Search the list of circuits and return the raw view data.
	 * @param cCriteria criteria object
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<CircuitViewData> searchCircuitsRaw(CircuitCriteriaDTO cCriteria) throws DataAccessException;
}
