/**
 * 
 */
package com.raritan.tdz.reports.generate;

import java.util.LinkedHashMap;
import java.util.Properties;

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
import com.raritan.tdz.session.DCTrackUserLogoutEventHandler;

/**
 * This router will resolve the message channel for a given user session Id
 * @author prasanna
 *
 *
 */
public class ReportGeneratorUserContextRouter implements
		ReportGeneratorContextRouter, ApplicationContextAware, DCTrackUserLogoutEventHandler {
	
	public static String REPORT_USER_UUID = "reportUserUUID";
	private static String propertiesName = "reportGeneratorProps";
	
	private String springContextPath;
	private String channelName;
	
	private ApplicationContext thisApplicationContext;
	
	private final LinkedHashMap<String, MessageChannel> messageChannels = 
			new LinkedHashMap<String,MessageChannel>();
	private final LinkedHashMap<MessageChannel,ConfigurableApplicationContext> contexts = 
					new LinkedHashMap<MessageChannel,ConfigurableApplicationContext>();

	public ReportGeneratorUserContextRouter(String springContextPath, String channelName){
		this.springContextPath = springContextPath;
		this.channelName = channelName;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.reports.generate.ReportGeneratorContextRouter#resolve(java.lang.String)
	 */
	@Override
	public MessageChannel resolve(String uuid) {
		MessageChannel channel = this.messageChannels.get(uuid);
		if (channel == null) {
			ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
					new String[] { springContextPath },false,
					thisApplicationContext);
			
			ctx.registerShutdownHook();
			
			//Setup the environment variables for the new context.
			this.setEnvironment(ctx, uuid);
			ctx.refresh();
			
			channel = ctx.getBean(channelName, MessageChannel.class);
			this.messageChannels.put(uuid, channel);
			this.contexts.put(channel, ctx);
		}
		return channel;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.thisApplicationContext = applicationContext;
		
	}
	
	@Override
	public void handleUserLogout(UserInfo userInfo, String arg1) throws Throwable {
		MessageChannel channel = this.messageChannels.get(userInfo.getSessionId());
		
		if (channel != null && contexts.get(channel) != null)
			contexts.get(channel).close();
		
		contexts.remove(channel);
		messageChannels.remove(userInfo.getSessionId());
	}
	
	/**
	 * Use Spring 3.1. environment support to set properties for the
	 * user uuid specific application context.
	 *
	 * @param ctx
	 * @param uuid
	 * @throws DataAccessException 
	 */
	private void setEnvironment(ConfigurableApplicationContext ctx, String uuid){
		StandardEnvironment env = new StandardEnvironment();
		Properties props = new Properties();
		// populate properties for customer
		props.setProperty(REPORT_USER_UUID, uuid);
		PropertiesPropertySource pps = new PropertiesPropertySource(propertiesName, props);
		env.getPropertySources().addLast(pps);
		ctx.setId(springContextPath + uuid);
		ctx.setEnvironment(env);
	}



}
