package com.raritan.tdz.data;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;

public interface DataPortFactory {

	public abstract DataPort createPortsForItem(Item item,
			Long portSubClassValueCode, int quantity);

}