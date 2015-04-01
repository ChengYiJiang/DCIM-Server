package com.raritan.tdz.ip.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.ip.domain.IPAddressDetails;
import com.raritan.tdz.ip.domain.IPTeaming;
import com.raritan.tdz.ip.domain.Networks;

public class IPAddressDetailsDAOImpl extends DaoImpl<IPAddressDetails> implements IPAddressDetailsDAO {
	private final Logger log = Logger.getLogger(this.getClass());

	@Autowired( required=true )
	IPTeamingDAO ipTeamingDAO;


	@Override
	public Long create(IPAddressDetails o){
		fixStringFields(o);
		return super.create(o);
	}

	@Override
	public IPAddressDetails merge(IPAddressDetails transientObject) {
		fixStringFields(transientObject);
		return  super.merge(transientObject);
	}

	@Override
	public List<IPAddressDetails> getIpAddressForDataPort(Long dataPortId){
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllIPsForDataPort");
		query.setParameter("portId", dataPortId.longValue());

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();

		return list;
	}

	@Override
	public List<IPAddressDetails> getIPAddressUsingGateway(String gateway){
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllIPsForGateway");
		query.setParameter("gateway", gateway);

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query.list();

		return list;
	}

	@Override
	public List<IPAddressDetails> getIpAddressByName(String ipaddress, Long locationId) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findIPAddressByName");
		query.setParameter("ipaddress", ipaddress);
		query.setParameter("locationId", locationId);

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();

		return list;

	}


	@Override
	public List<IPAddressDetails> getIpAddressForItem(Long itemId) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllIPsForItem");
		query.setParameter("itemId", itemId.longValue());

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();

		/*
		 * Though we filtered ipaddress only for specific itemId, when we try to access the
		 * IpTeaming set, hibernate will load all teams that are assigned to the ipaddress
		 * So, we have to remove those that do not belong to the set.
		 */
		if( list.size() > 0 ){
			for(IPAddressDetails ip : list ){
				Set<IPTeaming> teams = ip.getIpTeaming();
				assert( teams.size() > 0 );
				Iterator<IPTeaming> iterator = teams.iterator();
				while( iterator.hasNext()){
					DataPort d = iterator.next().getDataPort();
					if( d.getItem().getItemId() != itemId ) iterator.remove();
				}
			}
		}
		return list;

	}
	@Override
	public List<IPTeaming> getIpAssignmentsForItem(Long itemId) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllTeamsForItem");
		query.setParameter("itemId", itemId.longValue());

		@SuppressWarnings("unchecked")
		List<IPTeaming> list = (List<IPTeaming>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();

		return list;

	}
	@Override
	public List<String> getAllUsedIPAddressesInSubnet( Long subnetId ){
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllUsedIpAddressesInSubnet");
		query.setParameter("subnetId", subnetId.longValue());

		@SuppressWarnings("unchecked")
		List<String>list = (List<String>)query.list();

		return list;
	}

	@Override
	public List<String> getAllGatewaysInSubnet( Long locationId, String subnet ){
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllGatewaysInSubnet");
		query.setParameter("locationId", locationId.longValue());
		query.setParameter("subnet", subnet);

		@SuppressWarnings("unchecked")
		List<String>list = (List<String>)query.list();

		return list;
	}

	@Override
	public List<IPAddressDetails> getNotManagedIpAddressDetails(String ipAddress) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findNonManagedIpAddress");
		query.setParameter("ipAddress", ipAddress);

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query.list();

		return list;
	}

	@Override
	public List<Networks> getSubnetForIpAndLocation(String ipAddress, Long locationId){
		List<Networks> networksList = new ArrayList<Networks>();
		List<IPAddressDetails> ipaddr = getNotManagedIpAddressDetails(ipAddress);
		//check if found ip
		if( ipaddr != null && ipaddr.size() > 0 ){
			for( IPAddressDetails ip : ipaddr){
				Networks networks = new Networks();
				networks.setGateway(ip.getGateway());
				networks.setIsManaged(false);
				networksList.add(networks);
			}
		}else{ // ip not there, check all non-managed gateways
			ipaddr = getNotManagedGatewayDetails(ipAddress);
			//we care only if it was found, and send always one record only
			if( ipaddr != null && ipaddr.size() > 0 ){
				Networks networks = new Networks();
				networks.setGateway(ipAddress);
				networks.setIsManaged(false);
				networksList.add(networks);
			}
		}
		return networksList;
	}

	@Override
	public List<IPAddressDetails> getNotManagedGatewayDetails(String ipAddress) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findNonManagedGateway");
		query.setParameter("gateway", ipAddress);

		@SuppressWarnings("unchecked")
		List<IPAddressDetails> list = (List<IPAddressDetails>) query.list();

		return list;
	}

	//----------- private methods ---------------------


	@SuppressWarnings("unused")
	private Criteria getIPAddressCriteria(List<String> ipAddresses) {
		Session session = this.getSession();

		Criteria criteria = session.createCriteria(IPAddressDetails.class);

		criteria.add(Restrictions.in("ipAddress", ipAddresses));

		return criteria;
	}

	/*
	 * Requirement: Ensure there are no empty strings to be saved in DB. If
	 * any found, convert it to null.
	 */
	private void fixStringFields(IPAddressDetails ipAddress){
		String ipAddressStr = ipAddress.getIpAddress();
		if( ipAddressStr != null && ipAddressStr.length() == 0 ) ipAddress.setIpAddress(null);

		String gateway = ipAddress.getGateway();
		if( gateway != null && gateway.length() == 0 ) ipAddress.setGateway(null);

		String dnsName = ipAddress.getDnsName();
		if( dnsName != null && dnsName.length() == 0 ) ipAddress.setDnsName(null);

		String comment = ipAddress.getComment();
		if( comment != null && comment.length() == 0 ) ipAddress.setComment(null);

	}

	@Override
	public List<String> getProxyIndexesForIpAddr( String ipAddress, Long locationId ){

		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllProxyIDsForIpAndLocation");
		query.setParameter("locationId", locationId.longValue());
		query.setParameter("ipAddress", ipAddress);

		@SuppressWarnings("unchecked")
		List<String>list = (List<String>)query.list();

		return list;
	}
}
