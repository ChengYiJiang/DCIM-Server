package com.raritan.tdz.ip.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.ip.domain.IPTeaming;

public interface IPTeamingDAO extends Dao<IPTeaming>{
	List<IPTeaming> getTeamsForIp(Long ipId);
	List<IPTeaming> getTeamsForIpAddress(String ipAddress, Long locationId);
	IPTeaming getTeamForIpAndDataPort(Long ipId, Long dataPort);
	List<IPTeaming> getIpTeamsForItem(Long ipId, Long itemId);
	List<IPTeaming> getTeamsForDataPort(Long dataPort);
	List<IPTeaming> getTeamsForItem(Long itemId);
}
