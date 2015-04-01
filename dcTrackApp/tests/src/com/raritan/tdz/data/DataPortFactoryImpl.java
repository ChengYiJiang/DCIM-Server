package com.raritan.tdz.data;

import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.util.GlobalUtils;


public class DataPortFactoryImpl implements DataPortFactory {
	@Autowired
	SystemLookupFinderDAO systemLookupDAO;
	
	@Autowired
	UserLookupFinderDAO userLookupDAO;
	
	@Autowired
	ConnectorLookupFinderDAO connectorLookupDAO;
	
	public DataPortFactoryImpl() {
		
	}

	@Override
	public DataPort createPortsForItem(Item item, Long portSubClassValueCode, int quantity){
		LksData media = systemLookupDAO.findByLkpValueCode(SystemLookup.MediaType.TWISTED_PAIR).get(0);
		LksData portSubClassLookup = systemLookupDAO.findByLkpValueCode(portSubClassValueCode).get(0);
		ConnectorLkuData connectorLookup = connectorLookupDAO.findByNameCaseInsensitive("RJ45").get(0);
		LkuData speedLku = userLookupDAO.findByLkpValueAndTypeCaseInsensitive("100/1000/10G Base-T", "SPEED").get(0);
		LkuData protocolLku = userLookupDAO.findByLkpValueAndTypeCaseInsensitive("Ethernet/IP", "PROTOCOL").get(0);

		DataPort port = null;
		DataPort firstPort = null;

		int portCount = item.getDataPorts() == null ? 1 : item.getDataPorts().size();

		for(int i = 0; i<quantity; i++){
			port = new DataPort();
			port.setPortName("Net-" + i + portCount);
			port.setPortSubClassLookup(portSubClassLookup);
			port.setConnectorLookup(connectorLookup);
			port.setMediaId(media);
			port.setSpeedId(speedLku);
			port.setProtocolID(protocolLku);
			port.setItem(item);
			port.setCreationDate(GlobalUtils.getCurrentDate());
			port.setUpdateDate(GlobalUtils.getCurrentDate());
			port.setSortOrder(i + portCount);
			item.addDataPort(port);

			if(firstPort == null){
				firstPort = port;
			}
		}

		return firstPort;
	}

}
