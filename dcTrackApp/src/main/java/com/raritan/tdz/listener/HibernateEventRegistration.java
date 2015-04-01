package com.raritan.tdz.listener;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class HibernateEventRegistration {
	
    @Autowired(required=true)
    private SessionFactory sessionFactory;

    @Autowired(required=true)
    private PostUpdateEventListener auditTrailistener;
    
    public void registerListeners() {
        EventListenerRegistry registry = 
        		((SessionFactoryImpl) sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.POST_COMMIT_UPDATE).appendListener(auditTrailistener);
    }

}
