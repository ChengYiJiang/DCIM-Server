package com.raritan.tdz.piq.home;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.piq.domain.ProbePDULink;

/**
 * Probe Lookup default implementation.
 * @author Andrew Cohen
 */
public class PIQProbeLookupImpl implements PIQProbeLookup {

	private final Logger log = Logger.getLogger("PIQProbeLookup");
	
	private SessionFactory sessionFactory;
	
	public PIQProbeLookupImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Item getDummyRackPDUForProbeItem(long probeItemId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery("from ProbePDULink where probeId = :probeId");
			q.setLong("probeId", probeItemId);
			ProbePDULink link = (ProbePDULink)q.uniqueResult();
			
			if (link != null) {
				Item pdu = link.getDummyRPDU();
				pdu.getItemId();
				return pdu;
			}
		}
		catch (ObjectNotFoundException e) {
			log.debug("No dummy rack PDU found for probe");
			return null;
		}
		catch (Throwable t) {
			log.error("Error fetching dummy Rack PDU for probe with id = " + probeItemId, t);
		}
		
		return null;
	}

	@Override
	public Item getProbeItemForDummyRackPDU(long pduId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			// log.error(">>>>> -getProbeItemForDummyRackPDU() Thread id: " + Thread.currentThread().getId() +
			//          "Session Ptr = " + Integer.toHexString(System.identityHashCode(session)));
			Query q = session.createQuery("from ProbePDULink where dummyRpduId = :pduId");
			q.setLong("pduId", pduId);
			ProbePDULink link = (ProbePDULink)q.uniqueResult();
			
			if (link != null) {
				Item probe = link.getProbe();
				probe.getItemId();
				return probe;
			}
		}
		catch (ObjectNotFoundException e) {
			log.debug("No probe found for dummy rack PDU");
			return null;
		}
		catch (Throwable t) {
			log.error("Error fetching probe for dummy Rack PDU with id = " + pduId, t);
		}
		
		return null;
	}

	@Override
	public Long getUnmappedProbePIQId(long probeItemId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria c = session.createCriteria( ProbePDULink.class );
		c.add(Restrictions.eq("probeId",  probeItemId));
		ProbePDULink link = (ProbePDULink)c.uniqueResult();
		if (link != null) {
			return link.getUnmappedProbePiqId();
		}
		return null;
	}
}
