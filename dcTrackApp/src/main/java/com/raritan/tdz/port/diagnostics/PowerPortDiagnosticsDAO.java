package com.raritan.tdz.port.diagnostics;

import org.springframework.validation.Errors;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.diagnostics.domain.PortDiagnostics;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;

public interface PowerPortDiagnosticsDAO extends Dao<PortDiagnostics> {
	
	public void reportError(PowerPort port, Errors errors, String logLevel);
	
	public void reportError(Item item, Errors errors, String logLevel);

}
