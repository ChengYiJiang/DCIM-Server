package com.raritan.tdz.ticket.dao;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.Tickets;

public class TicketsDAOImpl extends DaoImpl<Tickets> implements TicketsDAO {

	
	@Override
	public Long getTicketStatusLkpCode(Long ticketId) {
		
		Criteria criteria = getTicketCriteria(ticketId);
		criteria.createAlias("ticketStatus.statusLookup", "ticketStatusLks");
		
		criteria.setProjection((Projections.projectionList()
				.add(Projections.alias(Projections.property("ticketStatusLks.lkpValueCode"), "statusLkpValueCode"))));
		
		return (Long) criteria.uniqueResult();

	}

	@Override
	public Long getTicketId(Long itemId) {
		
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(Tickets.class);
		criteria.createAlias("ticketFields", "ticketFields");
		criteria.add(Restrictions.eq("ticketFields.isModified", true));
		criteria.add(Restrictions.eq("ticketFields.itemId", itemId));
		
		criteria.setProjection((Projections.projectionList()
				.add(Projections.alias(Projections.property("ticketId"), "ticketId"))));
		
		return (Long) criteria.uniqueResult();

	}
	
	@Override
	public void setTicketStatus(Long ticketId, Long itemId, Long ticketStatus) {
		
		Criteria criteria = getTicketCriteria(ticketId);
		criteria.createAlias("ticketStatus", "ticketStatus");
		criteria.createAlias("ticketStatus.statusLookup", "ticketStatusLks");
		
		// ticketFields
		
		/*Session session = this.getSession();
		Query qStatus = session.createSQLQuery("update dct_tickets set status_lks_id = 1016 from dct_ticket_fields where dct_ticket_fields.ticket_id = dct_tickets.ticket_id and dct_ticket_fields.item_id = :itemId and status_lks_id <> 1026 and status_lks_id <> 1029");
		qStatus.setLong("itemId", itemId);
		qStatus.executeUpdate();*/
		
	}

	@Override
	public void resetRequestId(Long ticketId) {
		// TODO Auto-generated method stub
		
	}
	

	private Criteria getTicketCriteria(Long ticketId) {
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(Tickets.class);
		criteria.add(Restrictions.eq("ticketId", ticketId));
		
		return criteria;
		
	}
	
}
