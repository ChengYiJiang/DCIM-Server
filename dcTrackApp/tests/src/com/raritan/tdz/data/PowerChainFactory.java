package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;

public interface PowerChainFactory {

	public abstract List<PowerConnection> createPowerChange(String name) throws Throwable;

	public abstract void createOnePoleBreakersForPanel(MeItem panel,
			PowerPort panelBreaker, String volts, int amps, int quantity,
			int startPort);

	public abstract void createTwoPoleBreakersForPanel(MeItem panel,
			PowerPort panelBreaker, String volts, int amps, int quantity,
			int startPort);

	public abstract void createThreePoleBreakersForPanel(MeItem panel,
			PowerPort panelBreaker, String volts, int amps, int quantity,
			int startPort);

	public void printPowerNode(MeItem powerPanel);

	public abstract List<PowerConnection> getEndingNodes(PowerPort port);

	public abstract MeItem getCurrentUpsBank();

	public abstract MeItem getCurrentUPS();

	public abstract void deletePowerChainConns();

}