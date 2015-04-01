package com.raritan.tdz.ticket.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;

public class TicketFieldsDAOImpl extends DaoImpl<TicketFields>  implements TicketFieldsDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<TicketFields> getTicketFieldsUsingTicket(Long ticketId) {
		
		Criteria criteria = getTicketFieldsUsingTicketCriteria(ticketId);

		return (List<TicketFields>) criteria.list();
		
	}

	@Override
	public TicketFields getTicketFieldsUsingTicket(Long ticketId, Boolean modified) {

		Criteria criteria = getTicketFieldsUsingTicketCriteria(ticketId);
		criteria.add(Restrictions.eq("isModified", modified));
		
		return (TicketFields) criteria.uniqueResult();
	}
	
	@Override
	public void createTicketFieldsUsingTicket(Tickets tickets) {
		TicketFields original = new TicketFields();
		original.setIsModified(false);
		original.setTickets(tickets);
		
		TicketFields modified = new TicketFields();
		modified.setIsModified(true);
		modified.setTickets(tickets);
		
		create(original);
		create(modified);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TicketFields> getTicketFieldUsingItem(Long itemId) {
		
		Criteria criteria = getTicketFieldUsingItemCriteria(itemId);

		return (List<TicketFields>) criteria.list();
	}


	@Override
	public TicketFields getTicketFieldUsingItem(Long itemId, Boolean modified) {
		
		Criteria criteria = getTicketFieldUsingItemCriteria(itemId);
		criteria.add(Restrictions.eq("isModified", modified));
		
		return (TicketFields) criteria.uniqueResult();
	}
	
	/* -------- Private functions ---------- */

	private Criteria getTicketFieldUsingItemCriteria(Long itemId) {
		
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(TicketFields.class);
		criteria.createAlias("item", "item");
		criteria.add(Restrictions.eq("item.itemId", itemId));
		
		return criteria;
	} 
	
	private Criteria getTicketFieldsUsingTicketCriteria(Long ticketId) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(TicketFields.class);
		criteria.createAlias("tickets", "tickets");
		criteria.add(Restrictions.eq("tickets.ticketId", ticketId));

		return criteria;
	}
	

	
}
