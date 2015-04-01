package com.raritan.tdz.events.home;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventSeverity;
import com.raritan.tdz.events.domain.Event.EventStatus;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.dto.EventDTOImpl;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Events business layer implementation.
 * 
 * @author Andrew Cohen
 */
@Transactional(rollbackFor = DataAccessException.class)
public class EventHomeImpl implements EventHome {

	// Event parameters for items
	private static final String CABINET_PARAM = "Cabinet";
	private static final String LOCATION_PARAM = "Location";
	private static final String ITEM_NAME_PARAM = "Item Name";
	private static final String ITEM_CLASS_PARAM = "Item Class";
	private static final String ITEM_STATUS_PARAM = "Item Status";
	private static final String UNKNOWN_VALUE = "Unknown";
	@SuppressWarnings("unused")
	private static final String EMPTY_VALUE = "";
	
	private SessionFactory sessionFactory;
	private ResourceBundleMessageSource messageSource;
	private ItemHome itemHome;
	private ThreadPoolTaskExecutor dctExecutor;
	
	public EventHomeImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	@Override
	public ResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}
	
	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}
	
	@Override
	public EventSummary getEvents() throws DataAccessException {
		return getEventSummary(null, null, null, null);
	}

	@Override
	public EventSummary getActiveEvents() throws DataAccessException {
		return getEventSummary(false, null, null, null);
	}
	
	@Override
	public EventSummary getClearedEvents() throws DataAccessException {
		return getEventSummary(true, null, null, null);
	}
	
	@Override
	public List<Event> filterEvents(EventType type, String paramName, String paramValue) throws DataAccessException {
		return getEvents(null, type, paramName, paramValue);
	}
	
	@Override
	public List<Event> filterActiveEvents(EventType type, String paramName, String paramValue) throws DataAccessException {
		return getEvents(false, type, paramName, paramValue);
	}

	@Override
	public List<Event> filterClearedEvents(EventType type, String paramName, String paramValue) throws DataAccessException {
		return getEvents(true, type, paramName, paramValue);
	}

	public void setDctExecutor(ThreadPoolTaskExecutor dctExecutor) {
		this.dctExecutor = dctExecutor;
	}
	
	@Override
	public Event getEventDetail(long eventId) throws DataAccessException {
		Event event = null;
		Session session = null; 
		
		try {
			session = sessionFactory.getCurrentSession();
			Criteria c = session.createCriteria(Event.class);
			c.add(Restrictions.eq("id", eventId));
			c.setFetchMode("eventParams", FetchMode.JOIN);
			event = (Event)c.uniqueResult();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		return event;
	}
	
	@Override
	public int clearEvents(List<Long> eventIds) throws DataAccessException {
		return clearEvents(eventIds, null);
	}
	
	@Override
	public int clearEvents(List<Long> eventIds, Event clearingEvent) throws DataAccessException {
		//long time = System.currentTimeMillis();
		int count = 0;
		Timestamp now = new Timestamp(Calendar.getInstance().getTimeInMillis());
		StringBuffer query = new StringBuffer("update Event set clearedAt = :clearedAt");
		
		if (clearingEvent != null) {
			query.append(", clearingEvent = :clearingEvent");
		}
		
		query.append( getEventIdsWhereClause(eventIds) );
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery( query.toString() );
			q.setTimestamp("clearedAt", now);
			if (clearingEvent != null) {
				q.setParameter("clearingEvent", clearingEvent);
			}
			count = q.executeUpdate();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CLEAR_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_CLEAR_FAILED, this.getClass(), e));
		}
		
		//System.out.println("Cleared " + count + " of " + eventIds.size() + "in " +(System.currentTimeMillis()-time) + "ms");
		return count;
	}
	
	

	@Override
	public int clearAllEvents() throws DataAccessException {
		// Run clear all task in the background
		dctExecutor.submit( new ClearAllTask() );
		return 1;
	}

	@Override
	public int purgeEvents(List<Long> eventIds) throws DataAccessException {
		//long time = System.currentTimeMillis();
		Session session = null; 
		int count = 0;
		
		try {
			session = sessionFactory.getCurrentSession();
			StringBuffer query = new StringBuffer("delete from Event ");
			query.append( getEventIdsWhereClause(eventIds) );
			
			query.append(" and clearedAt is not null");
			
			Query q = session.createQuery( query.toString() );
			count = q.executeUpdate();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_PURGE_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_PURGE_FAILED, this.getClass(), e));
		}
		
		//System.out.println("Purged " + count + " of " + eventIds.size() + "in " +(System.currentTimeMillis()-time) + "ms");
		return count;
	}
	
	@Override
	public int purgeEvents(Date beforeDate) throws DataAccessException {
		int deleted = 0;
		Session session = null; 
		
		try {
			session = sessionFactory.getCurrentSession();
			Query q = session.createQuery("delete from Event where createdAt <= :beforeDate and clearedAt is not null");
			q.setDate("beforeDate", beforeDate);
			deleted = q.executeUpdate();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_PURGE_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_PURGE_FAILED, this.getClass(), e));
		}
		
		return deleted;
	}
	
	@Override
	public int purgeAllEvents() throws DataAccessException {
		// Execute bulk purge task on a background thread since this could be a long running operation
		dctExecutor.submit( new BulkPurgeTask() );
		return 1;
	}
	
	@Override
	public Event createEvent(EventType type, EventSeverity severity, String source) throws DataAccessException {
		return createEvent(null, type, severity, source);
	}
	
	@Override
	public Event createEvent(Timestamp createdAt, EventType type, EventSeverity severity, String source) throws DataAccessException {
		Event event = null;
		Session session = null; 
		
		try {
			session = sessionFactory.getCurrentSession();
			Timestamp c = createdAt;
			if (c == null) {
				c = new Timestamp( Calendar.getInstance().getTimeInMillis() );
			}
			event = Event.createEvent(session, c, type, source, severity);
			session.save( event );
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		return event;
	}
	
	@Override
	public long getEventCount() throws DataAccessException {
		long eventCount;
		Session session = null;
		
		try {
			session = sessionFactory.getCurrentSession();
			Query q  = session.createQuery("select count(*) from Event");
			eventCount = (Long)q.uniqueResult();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		return eventCount;
	}

	@Override
	public Date getLatestEventDate(String source) throws DataAccessException {
		Date date = null;
		//long time = System.currentTimeMillis();
		
		try {
			Session session = sessionFactory.getCurrentSession();
			StringBuffer hql = new StringBuffer("select max(createdAt) from Event");
			if (source != null) {
				hql.append(" where source = :source");
			}
			
			Query q = session.createQuery( hql.toString() );
			if (source != null) {
				q.setString("source", source);
			}
			
			date = (Date)q.uniqueResult();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		//System.out.println("Time to fetch latest event date: " + (System.currentTimeMillis() - time) + "ms");
		
		return date;
	}
	
	@Override
	public void saveEvent(Event event) throws DataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();
			long id = event.getId();
			if (id > 0) {
				session.merge( event );
			}
			else {
				session.save( event );
			}
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
	}
	
	@Override
	public void setSeverity(Event event, EventSeverity severity) throws DataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();
			event.setSeverity(session, severity);
			session.merge( event );
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
	}
	
	@Override
	public void setEventType(Event event, EventType type) throws DataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();
			event.setType(session, type);
			session.merge( event );
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
	}

	@Override
	public void addItemEventParams(Event ev, Item item) {
		CabinetItem cabinet = null;
		
		if (item != null) {
			cabinet = itemHome.getCabinet( item );
		}
		
		ev.addParam( CABINET_PARAM, cabinet != null ? cabinet.getItemName(): UNKNOWN_VALUE );
		ev.addParam( ITEM_NAME_PARAM, item != null ? item.getItemName() : UNKNOWN_VALUE );
		if (item.getClassLookup() != null) {
			ev.addParam( ITEM_CLASS_PARAM, item != null ? item.getClassLookup().getLkpValue() : UNKNOWN_VALUE );
		}
		if (item.getStatusLookup() != null) {
			ev.addParam( ITEM_STATUS_PARAM, item != null ? item.getStatusLookup().getLkpValue() : UNKNOWN_VALUE );
		}
		if (cabinet != null) {
			DataCenterLocationDetails location = cabinet.getDataCenterLocation();
			ev.addParam( LOCATION_PARAM, location != null ? location.getDcName() : UNKNOWN_VALUE);	
		}
	}
	
	//
	// Private methods
	//

	private String getEventIdsWhereClause(List<Long> eventIds) {
		StringBuffer b = new StringBuffer(" where id in (");
		boolean first = true;
		for (Long eventId : eventIds) {
			if (!first) {
				b.append(",");
			}
			else {
				first = false;
			}
			b.append(eventId);
		}
		b.append(")");
		return b.toString();
	}
	
	@SuppressWarnings("unchecked")
	private List<Event> getEvents(Boolean cleared, EventType type, String paramName, String paramValue) throws DataAccessException {
		List<Event> events = null;
		Session session = null; 
		
		try {
			session = sessionFactory.getCurrentSession();
			
			Criteria c = session.createCriteria(Event.class);
			if (cleared != null) {
				if (cleared) {
					c.add(Restrictions.isNotNull("clearedAt")); // Cleared events
				}
				else {
					c.add(Restrictions.isNull("clearedAt")); // Active events
				}
			}
			
			if (type != null) {
				// Filtering by event type
				c.createAlias("type", "type");
				c.add(Restrictions.eq("type.lkpValueCode", type.valueCode()));
			}
			
			if (paramName != null && paramValue != null) {
				// Filter by a particular event parameter
				c.createAlias("eventParams", "params");
				c.add(Restrictions.eq("params.name", paramName));
				c.add(Restrictions.eq("params.value", paramValue));
			}
			
			c.addOrder( Order.desc("createdAt") );
			c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			events = c.list();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		if (events == null) {
			events = new LinkedList<Event>();
		}
		
		return events;
	}
	

	@SuppressWarnings("unchecked")
	private List<EventDTOImpl> getMappedEvents(Boolean cleared, EventType type, String paramName, String paramValue) throws DataAccessException {
		List<EventDTOImpl> events = null;
		Session session = null; 
		
		session = sessionFactory.getCurrentSession();
		
		StringBuffer sqlQuery = new StringBuffer()
			.append(" select dct_events.event_id as \"eventId\", ")
			.append(" dct_events.created_at as \"occuredAt\", ")
			.append(" (select lkp_value from dct_lks_data where lks_id = dct_events.event_severity_lks_id) as \"severity\", ")
			.append(" (select lkp_value from dct_lks_data where lks_id = dct_events.event_type_lks_id) as \"event\", ")
			.append(" dct_events.summary as \"summary\", ")
			.append(" (case when dct_events.cleared_at = null then \'Active\' else \'Cleared\' end) as \"status\", ")
			.append(" dct_events.source as \"source\" ")
			// .append(" CAST((select '') AS varchar(1)) as \"params\" ")
			.append(" from dct_events ");
		
		/*StringBuffer sqlQuery = new StringBuffer();
		// sqlQuery.append("select * from dct_events ");
		sqlQuery.append("select dct_events.event_id as \"eventId\", dct_events.created_at as \"occuredAt\", dct_events.u_position as \"uPosition\", dct_events.cleared_at as \"clearedAt\", dct_events.summary as \"summary\", dct_events.source as \"source\", dct_events.cleared_by_username as \"clearedByUsername\", dct_events.event_type_lks_id as \"type\", dct_events.event_status_lks_id as \"status\" from dct_events ");*/
		
		if (type != null) {
			sqlQuery.append(" inner join dct_lks_data on dct_events.event_type_lks_id = dct_lks_data.lks_id and dct_lks_data.lkp_value_code = :type ");
		}
		
		if (paramName != null && paramValue != null) {
			sqlQuery.append(" inner join dct_event_params on dct_events.event_id = dct_event_params.event_id and dct_event_params.param_name = :paramName and dct_event_params.param_value = :paramValue ");
		}

		if (cleared != null) {
			if (cleared) {
				sqlQuery.append(" where cleared_at is not null ");
			}
			else {
				sqlQuery.append(" where cleared_at is null ");
			}
		}

		sqlQuery.append(" order by created_at ");

		try {
		
			@SuppressWarnings("deprecation")
			Query q = session.createSQLQuery(sqlQuery	.toString())
					.addScalar("eventId", new LongType())
					.addScalar("occuredAt", new TimestampType())
					.addScalar("severity", new StringType())
					.addScalar("event", new StringType())
					.addScalar("summary", new StringType())
					.addScalar("status", new StringType())
					.addScalar("source", new StringType());
			
			if (type != null) {
				q.setParameter("type", type.valueCode());
			}
			
			if (paramName != null && paramValue != null) {
				q.setParameter("paramName", paramName);
				q.setParameter("paramValue", paramValue);
			}
			
			
			// q.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			q.setResultTransformer(Transformers.aliasToBean(EventDTOImpl.class));
			
			events = (List<EventDTOImpl>) q.list();
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		if (events == null) {
			events = new LinkedList<EventDTOImpl>();
		}
		
		return events;

	}
	
	private EventSummary getEventSummary(Boolean cleared, EventType type, String paramName, String paramValue) throws DataAccessException {
		EventSummary summary = new EventSummary();
		List<EventDTOImpl> events = null;
		Session session = null; 
		
		try {
			session = sessionFactory.getCurrentSession();
			summary.setGrandTotal( (Long)session.createQuery("select count(*) from Event").uniqueResult() );
			
			events = getMappedEvents(cleared, type, paramName, paramValue);
			
			// summary.setGrandTotal(events.size());
			
		}
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		summary.setEvents( events );
		
		return summary;
	}
	
	@Override
	public Event getMostRecentEvent(EventType type, EventStatus status, EventSeverity severity, String summaryContains) throws DataAccessException {
		Event event = null;
		boolean needsWhere = true;
		
		StringBuffer qbuf = new StringBuffer("from Event where id = ( select max(id) from Event ");
		if (type != null) {
			qbuf.append("where type.lkpValueCode = :typeLkpValueCode ");
			needsWhere = false;
		}
		if (status != null) {
			if (needsWhere) {
				qbuf.append("where ");
				needsWhere = false;
			}
			else {
				qbuf.append("and ");
			}
			qbuf.append("status.lkpValueCode = :statusLkpValueCode ");
		}
		if (severity != null) {
			if (needsWhere) {
				qbuf.append("where ");
				needsWhere = false;
			}
			else {
				qbuf.append("and ");
			}
			qbuf.append("severity.lkpValueCode = :severityLkpValueCode ");
		}
		if (summaryContains != null) {
			if (needsWhere) {
				qbuf.append("where ");
				needsWhere = false;
			}
			else {
				qbuf.append("and ");
			}
			qbuf.append("summary like :summaryContains ");
		}
		qbuf.append(")");
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery( qbuf.toString() );
			
			if (type != null) {
				q.setLong("typeLkpValueCode", type.valueCode());
			}
			
			if (status != null) {
				q.setLong("statusLkpValueCode", status.valueCode());
			}
			
			if (severity != null) {
				q.setLong("severityLkpValueCode", severity.valueCode());
			}
			
			if (summaryContains != null) {
				q.setString("summaryContains", "%" + summaryContains + "%");
			}
			
			List<?> results =  q.list();
			if (results != null && results.size() > 0) {
				event = (Event)results.get(0);
			}
			
		} 
		catch(HibernateException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.EVENT_FETCH_FAILED, this.getClass(), e));
		}
		
		return event;
	}
	
	
	//
	// Private methods and classes
	//
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void purgeAllClearedEvents() {
//		long time = System.currentTimeMillis();
		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			Query q = session.createSQLQuery("delete from dct_events where cleared_at is not null");
			q.executeUpdate();
		}
		catch (Throwable t) {
			this.writeBulkClearPurgeError("eventLog.bulkPurgeFailed", t);
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
		
//		System.out.println("Time to purge all cleared events: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void clearAllActiveEvents() {
//		long time = System.currentTimeMillis();
		Session session = null;
		
		try {
			session = sessionFactory.openSession();
			Query q = session.createSQLQuery("update dct_events set cleared_at = :clearedAt");
			q.setTimestamp("clearedAt", new Timestamp( Calendar.getInstance().getTimeInMillis() ));
			q.executeUpdate();
			session.flush();
		}
		catch (Throwable t) {
			this.writeBulkClearPurgeError("eventLog.bulkClearFailed", t);
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
		
//		System.out.println("Time to clear all active events: " + (System.currentTimeMillis() - time) + "ms");
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void writeBulkClearPurgeError(String msgCode, Throwable t) {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			Event ev = Event.createEvent(session, new Timestamp(Calendar.getInstance().getTimeInMillis()), EventType.SERVER_ERROR, "dcTrack", EventSeverity.WARNING);
			ev.setSummary( messageSource.getMessage(msgCode,
					null,
					null)
			);
			ev.addParam("Exception", t.getLocalizedMessage());
			session.save( ev );
			session.flush();
		} 
		catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	/**
	 * A runnable task to purge all cleared events in the event log.
	 */
	private class BulkPurgeTask implements Runnable {
		@Override
		public void run() {
			purgeAllClearedEvents();
		}
	}
	
	/**
	 * A runnable task to clear all active events in the event log.
	 */
	private class ClearAllTask implements Runnable {
		@Override
		public void run() {
			clearAllActiveEvents();
		}
	}
}
