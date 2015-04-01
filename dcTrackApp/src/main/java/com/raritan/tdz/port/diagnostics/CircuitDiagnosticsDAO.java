package com.raritan.tdz.port.diagnostics;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.diagnostics.domain.CircuitDiagnostics;
import com.raritan.tdz.domain.PowerConnection;

public interface CircuitDiagnosticsDAO extends Dao<CircuitDiagnostics> {

	public void reportError(PowerConnection conn, Errors errors, String defaultLogLevel, List<String> informationErrorCode);
	
}
