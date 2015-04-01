package com.raritan.tdz.ip.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.DataException;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.ip.domain.NetMask;
import com.raritan.tdz.ip.domain.Networks;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

public class NetworksDAOImpl extends DaoImpl<Networks> implements NetworksDAO {

	private final Logger log = Logger.getLogger(this.getClass());

	/*
	 * Searching tblnetworks for subnet that where this ip belongs (managed ip)
	 */
	@Override
	public List<Networks> getNetworkForIpAndLocation(String ipAddress, Long locationId) throws BusinessValidationException {		
		List<Networks> netList = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("Networks.getNetworkByIpAndLocation");
		query.setParameter("ipAddress", ipAddress);
		query.setParameter("locationId", locationId);
		
		try{
			netList = (List<Networks>) query.list();
		}catch(DataException e ){
			//if any exception happens, most likely the ip address is invalid
			log.error("Failed to check if ipaddres: " + ipAddress + " , lcoationId=" + locationId + "belongs to any subnet");
			BusinessValidationException be =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			be.addValidationError("IpAddressValidator.dataError", "Unable to track data in database");
			throw be;
			
		}
		if( netList.size() > 0){
			for( Networks n : netList){
				n.setIsManaged(true);
			}
		}
		
		return netList;
	}

	/*
	 * Searching for all rows in tblnetworks that match specified location id
	 */
	@Override
	public List<Networks> getAllNetworksForLocation(Long locationId) throws BusinessValidationException {
		List<Networks> netList = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("Networks.getAllNetworksForLocation");
		query.setParameter("locationId", locationId);
		try{
			netList = (List<Networks>) query.list();
		}catch(DataException e ){
			log.error("Failed to obtian all subnets for location: " + locationId);
			BusinessValidationException be =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
			be.addValidationError("IpAddressValidator.dataError", "Unable to obtian subnets from database");
			throw be;
			
		}
		
		return netList;
	}

}
