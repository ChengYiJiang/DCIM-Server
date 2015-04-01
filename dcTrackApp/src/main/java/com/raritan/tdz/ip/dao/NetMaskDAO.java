package com.raritan.tdz.ip.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.ip.domain.NetMask;

public interface NetMaskDAO extends Dao<NetMask> {

	public List<NetMask> getByMask(String mask);
	public List<NetMask> getById( Long id);	
	public List<NetMask> getByCidr( Long cidr );	
	public List<NetMask> getAll();
}
