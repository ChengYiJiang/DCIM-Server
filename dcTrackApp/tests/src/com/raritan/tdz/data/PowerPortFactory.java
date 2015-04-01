package com.raritan.tdz.data;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerPort;

public interface PowerPortFactory {

	public abstract float getDefaultDeRating();

	public abstract void setDefaultDeRating(float defaultDeRating);

	public abstract String getDefaultVolts();

	public abstract void setDefaultVolts(String defaultVolts);

	public abstract PowerPort createPorts3PhaseWYEForItem(Item item,
			Long portSubClassValueCode, int quantity);

	public abstract PowerPort createPorts3PhaseDELTAForItem(Item item,
			Long portSubClassValueCode, int quantity);

	public abstract PowerPort createPortsForItem(Item item,
			Long portSubClassValueCode, int quantity, int watts, int amps);

	public abstract PowerPort createPortsPSForItem(Item item, int quantity,
			int watts);

	public abstract PowerPort createPortsRecpForItem(Item item,
			PowerPort inputCord, int quantity, int amps, Long phaseValueCode,
			String voltsValue);

	public abstract void createFuseOnPort(PowerPort port, String fuseLkuValue,
			int amps);

	public abstract PowerPort createPortsForItem(Item item,	String startPortName, Long portSubClassValueCode, int quantity,
			Long phaseValueCode, String voltsValue, int amps);

	public abstract PowerPort createPortsForPanel(MeItem item, int quantity);

}