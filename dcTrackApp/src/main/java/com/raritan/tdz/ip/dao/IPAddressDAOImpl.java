package com.raritan.tdz.ip.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.IPAddress;

public class IPAddressDAOImpl extends DaoImpl<IPAddress>  implements IPAddressDAO {

	@Override
	public IPAddress get(String ipAddress) {
		
		Criteria criteria = getIPAddressCriteria(ipAddress);
		
		IPAddress ip = (IPAddress) criteria.uniqueResult();
		
		return ip;
	}

	@Override
	public List<IPAddress> get(List<String> ipAddresses) {
		Criteria criteria = getIPAddressCriteria(ipAddresses);
		
		@SuppressWarnings("unchecked")
		List<IPAddress> ips = criteria.list();
		
		return ips;	
	}
	
	/* -------- Private functions ---------- */
	
	private Criteria getIPAddressCriteria(String ipAddress) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(IPAddress.class);
		
		criteria.add(Restrictions.eq("ipAddress", ipAddress));

		return criteria;
	}

	private Criteria getIPAddressCriteria(List<String> ipAddresses) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(IPAddress.class);
		
		criteria.add(Restrictions.in("ipAddress", ipAddresses));

		return criteria;
	}


}
