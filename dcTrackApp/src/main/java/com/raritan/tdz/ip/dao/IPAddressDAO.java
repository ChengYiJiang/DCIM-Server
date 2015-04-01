package com.raritan.tdz.ip.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.IPAddress;

public interface IPAddressDAO extends Dao<IPAddress> {

	public IPAddress get(String ipAddress);
	
	public List<IPAddress> get(List<String> ipAddress);
	
}
