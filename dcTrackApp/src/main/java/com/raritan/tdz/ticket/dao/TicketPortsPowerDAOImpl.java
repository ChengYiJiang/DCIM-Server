package com.raritan.tdz.ticket.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.TicketPortsPower;
import com.raritan.tdz.lookup.SystemLookup;

public class TicketPortsPowerDAOImpl extends DaoImpl<TicketPortsPower>   implements TicketPortsPowerDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<TicketPortsPower> getTicketPortsPower(Long ticketId, Boolean modified) {

		Criteria criteria = getTicketPortsPowerCriteria(ticketId);
		
		criteria.add(Restrictions.eq("isModified", modified));
		
		return criteria.list();
	}

	@Override
	public TicketPortsPower getTicketPortPowerSupply(Long ticketId, Boolean modified) {

		Criteria criteria = getTicketPortSubclassModifiedCriteria(ticketId, modified);
		
		criteria.add(Restrictions.or(Restrictions.eq("portSubclassId.lkpValueCode", SystemLookup.PortSubClass.POWER_SUPPLY), Restrictions.isNull("portSubclassId.lkpValueCode")));
		
		return (TicketPortsPower) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TicketPortsPower> getTicketPortsPowerRpduInlets(Long ticketId, Boolean modified) {

		Criteria criteria = getTicketPortSubclassModifiedCriteria(ticketId, modified);
		
		criteria.add(Restrictions.eq("portSubclassId.lkpValueCode", SystemLookup.PortSubClass.INPUT_CORD));
		
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TicketPortsPower> getTicketPortsPowerRpduOutlets(Long ticketId, Boolean modified) {

		Criteria criteria = getTicketPortSubclassModifiedCriteria(ticketId, modified);
		
		criteria.add(Restrictions.eq("portSubclassId.lkpValueCode", SystemLookup.PortSubClass.RACK_PDU_OUTPUT));
		
		return criteria.list();
	}


	/* -------- Private functions ---------- */
	
	private Criteria getTicketPortsPowerCriteria(Long ticketId) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(TicketPortsPower.class);
		criteria.createAlias("ticketFields", "ticketFields");
		criteria.createAlias("ticketFields.tickets", "tickets");
		
		criteria.add(Restrictions.eq("tickets.ticketId", ticketId));

		return criteria;
	}
	
	private Criteria getTicketPortSubclassModifiedCriteria(Long ticketId, Boolean modified) {
		Criteria criteria = getTicketPortsPowerCriteria(ticketId);
		
		criteria.createAlias("portSubclassId", "portSubclassId");
		
		criteria.add(Restrictions.eq("isModified", modified));

		return criteria;
	}

}
