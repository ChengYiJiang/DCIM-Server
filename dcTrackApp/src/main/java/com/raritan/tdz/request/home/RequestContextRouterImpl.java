package com.raritan.tdz.request.home;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.integration.MessageChannel;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;

public class RequestContextRouterImpl implements RequestContextRouter, ApplicationContextAware {

	private static Logger rpLog = Logger.getLogger("RequestProgress");
	
	public static String PARENT_CONTEXT_PREFIX = "parentContextPathPrefix";
	public static String USER_SESSION_ID = "userSessionId";
	private static String propertiesName = "requestContextProps";
	
	private String parentContextPrefix;
	
	private String springContextPath;
	private String channelName;
	
	private Map<String, MessageChannel> sessionToChannel = new HashMap<String, MessageChannel>();;
	private Map<MessageChannel, ConfigurableApplicationContext> channelToContext = new HashMap<MessageChannel, ConfigurableApplicationContext>();
	private Map<String, Lock> sessionContextLock = new HashMap<String, Lock>();
	
	private ApplicationContext thisApplicationContext;
	
	// private Lock contextLock;
	
	public RequestContextRouterImpl(String springContextPath, String channelName, String parentContextPrefix) {
		super();
		this.springContextPath = springContextPath;
		this.channelName = channelName;
		this.parentContextPrefix = parentContextPrefix;
		// this.contextLock = new ReentrantLock();
		
	}
	
	

	@Override
	public MessageChannel getContextChannel(UserInfo userInfo) throws DataAccessException {
		
		MessageChannel channel = createRequestContextChannel(userInfo.getSessionId());;
		
		rpLog.warn("channel = " + channel);
		
		return channel;
	}

	private synchronized MessageChannel createRequestContextChannel(String sessionId) throws DataAccessException {
		
		Lock lock = this.sessionContextLock.get(sessionId);
		if (null != lock) lock.lock(); 
		
		try {
		
			MessageChannel channel = this.sessionToChannel.get(sessionId);
		
			 if (channel == null) 
			{
				 
				 if (null == lock) {
					 lock = new ReentrantLock();
					 this.sessionContextLock.put(sessionId, lock);
					 lock.lock();
				 }
	
				ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
						new String[] { this.springContextPath },
						false, 
						thisApplicationContext);
			
				ctx.registerShutdownHook();
			
				this.setEnvironmentForRequest(ctx, this.parentContextPrefix, sessionId);
	
				ctx.refresh();
				
				channel = ctx.getBean(channelName, MessageChannel.class);
				
				this.sessionToChannel.put(sessionId, channel);
	
				this.channelToContext.put(channel, ctx);
				
			}
			
			return channel;
		}
		finally {
			
			lock.unlock();
			
		}
	}



	@Override
	public synchronized void deleteContext(UserInfo userInfo) {
		
		String sessionId = userInfo.getSessionId();
		
		Lock lock = this.sessionContextLock.get(sessionId);
		if (null != lock) lock.lock();
		
		try {
			
			
			MessageChannel channel = sessionToChannel.get(sessionId);
			ConfigurableApplicationContext ctx = channelToContext.get(channel);
	
			// clean the channel info in the map
			channelToContext.remove(sessionId);
			
			//Then remove the channel from the session map
			sessionToChannel.remove(sessionId);
			
			sessionContextLock.remove(sessionId);
			
			if (null == ctx) return;
					
			// close the context, releasing all the resources. 
			// The channel will also be released because it is inside the context
			ctx.close();
			ctx = null;
			
			if (rpLog.isDebugEnabled()) rpLog.debug("Context closed for session" + sessionId +"...");
		}
		finally {
			
			if (null != lock) lock.unlock();
			
		}
		
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		
		this.thisApplicationContext = applicationContext;
		
	}
	
	private void setEnvironmentForRequest(ConfigurableApplicationContext ctx, String parentContextPrefix, String sessionId) throws DataAccessException {
		StandardEnvironment env = new StandardEnvironment();
		Properties props = new Properties();
		// populate properties for customer
		props.setProperty(PARENT_CONTEXT_PREFIX, this.parentContextPrefix);
		props.setProperty(USER_SESSION_ID, sessionId);

		PropertiesPropertySource pps = new PropertiesPropertySource(propertiesName, props);
		
		env.getPropertySources().addLast(pps);
		
		ctx.setId(springContextPath + sessionId);
		
		ctx.setEnvironment(env);
	}


}
