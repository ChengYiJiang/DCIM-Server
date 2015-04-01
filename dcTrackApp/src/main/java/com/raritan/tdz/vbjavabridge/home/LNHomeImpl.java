package com.raritan.tdz.vbjavabridge.home;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.mchange.v2.c3p0.C3P0ProxyConnection;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;
import com.raritan.tdz.vbjavabridge.subscribers.LNEventSubscriber;



@Transactional
public class LNHomeImpl implements LNHome, Work {
	private enum connWork {
		LISTEN,
		NOTIFY,
		UNKNOWN
	}
	
	private Boolean suspend = false;
	
	Logger log = Logger.getLogger(this.getClass());
	private SessionFactory sessionFactory = null;
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}



	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the suspend
	 */
	@Override
	public final Boolean getSuspend() {
		synchronized(this){
			return suspend;
		}
	}



	/**
	 * @param suspend the suspend to set
	 */
	@Override
	public final void setSuspend(Boolean suspend) {
		synchronized(this){
			this.suspend = suspend;
		}
	}

	private connWork work = connWork.UNKNOWN;
	
	private int cnt = 0;

	
	//Thread pool to process an event is perhaps not necessary and will cause issues
	//For example if we get a bunch of notifications, each notification will try to process
	//all records and will possibly conflict with a second thread for the second notification
	//Moreover for each thread we are opening and closing a session which is not correct. If we ever 
	//require threads to handle each event, we can pull this back. For now I will comment this out
	//Thread pool stuff
  /*  private ThreadPoolExecutor service;
    private BlockingQueue<Runnable> workQueue;
    
    private static final int threadCorePool = 2;
    private static final int threadMaxPool = 2;
    private static final int threadKeepAlive = 30;
  */
    
    LinkedHashMap<LNKey, List<LNEventSubscriber>> eventSubscribers;
	
	public LNHomeImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
		eventSubscribers = new LinkedHashMap<LNKey, List<LNEventSubscriber>>();
		
	/*	workQueue = new LinkedBlockingQueue<Runnable>();
		service = new ThreadPoolExecutor(
					threadCorePool, //Core pool size
					threadMaxPool, //Max pool size
					threadKeepAlive, //Keep alive time in seconds
					TimeUnit.SECONDS,
					workQueue
				);
	 */
	}
	
	
	@Override
	public void subscribe(LksData operationLks, String tableName,
			LNEventSubscriber eventHandler) {
		LNKey dbEventNameKey = new LNKey(operationLks, tableName);
		this.subscribe(dbEventNameKey, eventHandler);
	}

	@Override
	public void subscribe(LksData operationLks, String tableName, String action, 
			LNEventSubscriber eventHandler) {
		LNKey dbEventNameKey = new LNKey(operationLks, tableName, action);
		this.subscribe(dbEventNameKey, eventHandler);
	}

	
	public void subscribe(LNKey dbEventNameKey, LNEventSubscriber eventHandler) {
		if (dbEventNameKey != null && eventHandler != null)
		{
			List<LNEventSubscriber> s = null;
			if (eventSubscribers.containsKey(dbEventNameKey)) {
				s = eventSubscribers.get(dbEventNameKey);
				if (s != null) s.add(eventHandler);
			} else {
				s =  new LinkedList<LNEventSubscriber>();
				s.add(eventHandler);
				eventSubscribers.put(dbEventNameKey, s);
			}
			// Following commented code is OLD way of processing notifications is 
			// replaced with just polling on the lnEvents table
			//listenDBEvent();
		}
	}





	@Override
	@Scheduled(fixedDelay=10*1000)
	public void processEvents() {
		// Following commented code is OLD way of processing notifications is 
		// replaced with just polling on the lnEvents table
		//notifyDBEvent();
		
		if (sessionFactory != null && !getSuspend()){
			Session session = sessionFactory.getCurrentSession();
			
			//Note: There may be a little performance hit since we should ideally get
			//one notification entry in the database per notification received. However,
			//the processNotifications will be processing all at once 
			//(Please see processNotifications implementation). 
			//This is probably a good thing since if we by-chance miss any of the notifications
			//the database always contains all the notification entries and therefore 
			//we will always process all notifications. 
			//Value of processing all the events outweighs a little performance hit due to the
			//blank queries. Note that we always check the size before calling processNotification
			List lnEvents = getAllLnEvents(session);
			if (lnEvents.size() > 0) {
				processNotifications(lnEvents);
			}else {
				log.debug("No new events to be processed...");
			}
		} else {
			log.debug("Process listenNotify Events suspended...");
		}
	}

	@Override
	public void processEvent(LNEvent lnEvent) {
		log.debug("Processing Notification: " + lnEvent);
		
		//Construct the key from the lnEvent and get the listener
		LNKey lnKey = new LNKey(lnEvent.getOperationLks(), "\"" + lnEvent.getTableName() + "\"", lnEvent.getAction());
		log.debug("lnKey = " + lnKey);
		
		List<LNEventSubscriber> lnEventListners = eventSubscribers.get(lnKey);
		log.debug("eventSubscribers = " + eventSubscribers);
		
		//Notify in a thread by sending the event and the listner.
		//NotifyExecutor executor = new NotifyExecutor(lnEventListner, lnEvent);
		//service.execute(executor);
		
		if (lnEventListners != null){
			for (LNEventSubscriber l : lnEventListners) {
				if (l != null) l.handleEvent(lnEvent);
			}
			cnt++;
		}
	}


	/**
	 * @throws HibernateException
	 */
	private void listenDBEvent() throws HibernateException {
		if (sessionFactory != null){
			work = connWork.LISTEN;
			Session session = sessionFactory.getCurrentSession();
			session.doWork(this);
		}
	}

	/**
	 * @throws HibernateException
	 */
	private void notifyDBEvent() throws HibernateException {
		if (sessionFactory != null){
			work = connWork.NOTIFY;
			Session session = sessionFactory.getCurrentSession();
			session.doWork(this);
		}
	}
	
	
	@Override
	public void execute(Connection connection) throws SQLException {
		switch (work){
		case LISTEN:
			for (Entry<LNKey, List<LNEventSubscriber>> dbEventKey : eventSubscribers.entrySet() ){
				//When we subscribe to an event, we listen to that in the database
				//Else we will not receive any notifications!
				if (dbEventKey != null && dbEventKey.getKey() != null){
					
					String dbEventName = dbEventKey.getKey().getOperationLks().getLkpValue();

					Statement stmt = connection.createStatement();
					String dbevt = dbEventName.toLowerCase();
					stmt.execute("LISTEN " + dbevt.toLowerCase());
					stmt.close();
				}
			}
			break;
		case NOTIFY:
			
			//Handle all the notifications.
			//We use a threadpool since if any of the calls block for some reason,
			//We do not block the whole subsystem :-)
			
			//Remember that the connection we get from hibernate is not direct JDBC connection. Rather
			//it is from the C3P0 connection pool. We need to use the C3P0 rawConnectionOperation to
			//execute PGConnection methods.
			
			C3P0ProxyConnection proxyConn = (C3P0ProxyConnection)connection;
			try {
				Method m = PGConnection.class.getMethod("getNotifications", new Class[]{});
				Object[] args = new Object[] {}; 
				
				PGNotification[] notifications = (PGNotification[]) proxyConn.rawConnectionOperation(m, C3P0ProxyConnection.RAW_CONNECTION, args);
				
				if (notifications != null){
					for (int i = 0; i < notifications.length; i++){
						log.debug("Received Notification[" + i + "]: " + notifications[i].getName());
						
						//Get the listenNotify Event from the database
						if (sessionFactory != null){
							Session session = sessionFactory.getCurrentSession();
							
							//Note: There may be a little performance hit since we should ideally get
							//one notification entry in the database per notification received. However,
							//the processNotifications will be processing all at once 
							//(Please see processNotifications implementation). 
							//This is probably a good thing since if we by-chance miss any of the notifications
							//the database always contains all the notification entries and therefore 
							//we will always process all notifications. 
							//Value of processing all the events outweighs a little performance hit due to the
							//blank queries. Note that we always check the size before calling processNotification
							List lnEvents = getLnEventsForNotification(notifications[i].getName(), session);
							if (lnEvents.size() > 0) {
								processNotifications(lnEvents);
							}
						}
					}
				}
				else{
					log.debug("No new notifications to be processed");
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}
		
	}



	/**
	 * @param lnEvents
	 */
	private void processNotifications(List<LNEvent> lnEvents) {
		cnt = 0;
		//Note that there may be several events for the given notification
		//For example insert dct_items, insert dct_items_it, etc.
		//Iterate through them and process them.
		for (LNEvent lnEvent:lnEvents){
			if (!getSuspend()){
				processEvent(lnEvent);
			} else {
				log.debug("Process listenNotify Events suspended...");
			}
		}
		
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			try {
				session.flush();
			}
			catch (StaleStateException e) {
				// Eat this exception - we don't care if something in memory has already been deleted!
			}
		}
		log.debug("Delegated " + cnt + " records to be processed from dct_lnevent table");
	}





	/**
	 * @param notificationName
	 * @param session
	 * @return
	 * @throws HibernateException
	 * Get the notification for a given operation
	 */
	private List getLnEventsForNotification(String operation,
			Session session) throws HibernateException {
		// try to get notification from database for a given notification Name
		Criteria criteria = session.createCriteria(LNEvent.class);
		
		criteria.createAlias("operationLks", "lks");
		
		criteria.add(Restrictions.eq("lks.lkpTypeName", "LN_OPERATION"))
			.add(Restrictions.eq("lks.lkpValue", operation.toUpperCase()));
		
		// If there is any, then throw an error to the client
		List lnEvents = criteria.list();
		return lnEvents;
	}
	
	/**
	 * @param notificationName
	 * @param session
	 * @return
	 * @throws HibernateException
	 * Get the notification for a given operation
	 */
	private List getAllLnEvents(Session session) throws HibernateException {
		// try to get notification from database for a given notification Name
		Criteria criteriaActionNotNull = session.createCriteria(LNEvent.class);
	
		// get the Migration event at the top - TODO
		criteriaActionNotNull.add(Restrictions.isNotNull("action"));
		
		// If there is any, then throw an error to the client
		List lnEvents = criteriaActionNotNull.list();
		
		if (null == lnEvents || lnEvents.size() == 0) {
			Criteria criteriaActionNull = session.createCriteria(LNEvent.class);
			
			// get the Migration event at the top - TODO
			criteriaActionNull.add(Restrictions.isNull("action"));
			
			// If there is any, then throw an error to the client
			if (null == lnEvents) {
				lnEvents = criteriaActionNull.list();
			}
			else {
				lnEvents.addAll(criteriaActionNull.list());
			}
		}
		
		return lnEvents;
		
	}


}
