package com.raritan.tdz.ticket.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.TicketPortsData;

public class TicketPortsDataDAOImpl extends DaoImpl<TicketPortsData>  implements TicketPortsDataDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<TicketPortsData> getTicketPortsData(Long ticketId, Boolean modified) {
		
		Criteria criteria = getTicketPortsDataCriteria(ticketId);
		
		criteria.add(Restrictions.eq("isModified", modified));
		
		return criteria.list();
		
	}
	
	@Override
	public TicketPortsData getOriginalTicketPortData(Long originalTicketFieldId, Long modifiedTicketFieldId, String portName) {
		
		String qry = "from TicketPortsData as o where o.pairIndex in (select m.pairIndex from TicketPortsData m where m.ticketFields.ticketFieldId = :modTicketFieldId and m.portName = :portName) and o.ticketFields.ticketFieldId = :origTicketFieldId";
		
		Session session = getSession();
		
		Query query = session.createQuery(qry);
		query.setLong("origTicketFieldId", originalTicketFieldId);
		query.setLong("modTicketFieldId", modifiedTicketFieldId);
		query.setString("portName", portName);
		
		return (TicketPortsData) query.uniqueResult();
	}
	
	/* -------- Private functions ---------- */
	
	private Criteria getTicketPortsDataCriteria(Long ticketId) {
    	Session session = this.getSession();

		Criteria criteria = session.createCriteria(TicketPortsData.class);
		criteria.createAlias("ticketFields", "ticketFields");
		criteria.createAlias("ticketFields.tickets", "tickets");
		
		criteria.add(Restrictions.eq("tickets.ticketId", ticketId));

		return criteria;
	}

	


}
