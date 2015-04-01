package com.raritan.tdz.unit.item.port;

import org.jmock.Mockery;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;

public interface DataPortMock {

	public abstract DataPort createPortsForItem(Mockery jmockContext, Item item,
			Long portSubClassValueCode, int quantity, boolean isUsed);

	public abstract ConnectorLkuData createConnector();

	public abstract LkuData createSpeed();

	public abstract LkuData createProtocol();

}