package com.raritan.tdz.events.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.events.domain.Event;

public interface EventDAO extends Dao<Event> {

	public void generatePowerChainMigrationStartEvent(String purpose);
	
	public void generatePowerChainMigrationEndEvent(String status, String purpose);

	public void generatePowerChainEvent(String message, String itemName);

	public void generatePowerChainDiagnosticsEvent(String diagnosticsType, Long numOfErrors, Long numOfWarnings, Long numOfInformations);
	
}
