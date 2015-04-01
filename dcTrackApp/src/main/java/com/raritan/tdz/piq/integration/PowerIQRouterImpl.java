/**
 * 
 */
package com.raritan.tdz.piq.integration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.piq.home.PIQReconfiguration;
import com.raritan.tdz.piq.home.PIQRestClientBase;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.settings.home.ApplicationSettings.Name;

/**
 * @author prasanna
 *
 */
public class PowerIQRouterImpl implements PowerIQRouter, ApplicationContextAware {
	
	public static String HOST = "powerIQHost";
	private static String propertiesName = "powerIQProps";
	
	private String springContextPath;
	private String channelName;
	
	private ApplicationContext thisApplicationContext;
	
	private List<String> endPointsToStart = null;
	
	@Autowired
	private ApplicationSettings applicationSettings;
	

	private final LinkedHashMap<String, MessageChannel> messageChannels = 
			new LinkedHashMap<String,MessageChannel>();
	private final LinkedHashMap<MessageChannel,ConfigurableApplicationContext> contexts = 
					new LinkedHashMap<MessageChannel,ConfigurableApplicationContext>();
	
	public PowerIQRouterImpl(String springContextPath, String channelName){
		this.springContextPath = springContextPath;
		this.channelName = channelName;
	}
	

	public List<String> getEndPointsToStart() {
		return endPointsToStart;
	}


	public void setEndPointsToStart(List<String> endPointsToStart) {
		this.endPointsToStart = endPointsToStart;
	}


	public void preCreateContexts() throws DataAccessException{
		List<String> piqHosts = applicationSettings.getAllPowerIQHosts();
		
		for (String piqHost:piqHosts){
			if (piqHost != null && !piqHost.isEmpty())
				resolve(piqHost);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.piq.integration.PowerIQRouter#resolve()
	 */ 
	@Override
	public MessageChannel resolve(String hostName) throws DataAccessException {
		MessageChannel channel = messageChannels.get(hostName);
		if (channel == null){
			channel = createNewPowerIQChannel(hostName);
		}
		return channel;
	}
	
	private synchronized MessageChannel createNewPowerIQChannel(String host) throws DataAccessException {
		MessageChannel channel = this.messageChannels.get(host);
		if (channel == null) {
			ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
					new String[] { springContextPath },false,
					thisApplicationContext);
			
			ctx.registerShutdownHook();
			this.setEnvironmentForPowerIQ(ctx, host);
			ctx.refresh();
			
			postSetup(host,ctx);
			
			channel = ctx.getBean(channelName, MessageChannel.class);
			this.messageChannels.put(host, channel);
			//Will works as the same reference is presented always
			this.contexts.put(channel, ctx);
		}
		return channel;
	}
	



	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		thisApplicationContext = applicationContext;
		
	}


	@Override
	public void reloadCommunicationSettings(ApplicationSettings newApplicationSettings)
			throws DataAccessException {
		
		String host = newApplicationSettings.getProperty(Name.PIQ_IPADDRESS);
		
		MessageChannel channel = messageChannels.get(host);
		ConfigurableApplicationContext ctx = contexts.get(channel); 
		
		if (ctx != null)
			reloadCommunicationSettings(ctx,newApplicationSettings);
	}
	
	@Override
	public void remove(String hostName) throws DataAccessException {
		//First remove the context
		MessageChannel channel = messageChannels.get(hostName);
		if (channel != null && contexts.get(channel) != null)
			contexts.get(channel).close();
		contexts.remove(channel); 
		
		//Then remove the channel
		
		messageChannels.remove(hostName);
	}

	/**
	 * Use Spring 3.1. environment support to set properties for the
	 * PowerIQ host-specific application context.
	 *
	 * @param ctx
	 * @param host
	 * @throws DataAccessException 
	 */
	private void setEnvironmentForPowerIQ(ConfigurableApplicationContext ctx, String host) throws DataAccessException {
		StandardEnvironment env = new StandardEnvironment();
		Properties props = new Properties();
		// populate properties for customer
		props.setProperty(HOST, host);
		PropertiesPropertySource pps = new PropertiesPropertySource(propertiesName, props);
		env.getPropertySources().addLast(pps);
		ctx.setId(springContextPath + host);
		ctx.setEnvironment(env);
	}
	
	private void postSetup(String host, ConfigurableApplicationContext ctx) throws DataAccessException{
		
		ApplicationSettings appSettings = ctx.getBean("contextAppSettings", ApplicationSettings.class);
		appSettings.setPowerIQHost(host);
		
		reloadCommunicationSettings(ctx, appSettings);
		
		startEndPoints(ctx);
	}

	private void reloadCommunicationSettings(
			ConfigurableApplicationContext ctx, ApplicationSettings appSettings)
			throws DataAccessException {
		PIQReconfiguration reconfig = ctx.getBean("piqReconfiguration",PIQReconfiguration.class);
		reconfig.reloadCommunicationSettings(appSettings);
	}
	
	private void startEndPoints(ConfigurableApplicationContext ctx){
		if (endPointsToStart != null && ctx != null){
			for (String endPointId: endPointsToStart){
				AbstractEndpoint endPoint = ctx.getBean(endPointId,AbstractEndpoint.class);
				if (endPoint != null) endPoint.start();
			}
		}
	}




}
