/**
 * 
 */
package com.raritan.tdz.piq.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

/**
 * @author prasanna
 *
 */
public class TestOnGoingUpdates extends TestBase {
	
	@Test
	public void testResolvePIQChannel() throws DataAccessException {
		PowerIQRouter piqRouter = ctx.getBean("piqOngoingUpdateRouterResolver",PowerIQRouter.class);
		MessageChannel channel = piqRouter.resolve("192.168.51.63");
		Assert.assertNotNull(channel);
	}
	
	@Test
	public void testResolvePIQChannel2() {
		MessageChannel channel = ctx.getBean("piqOngoingUpdateRouterChannel",MessageChannel.class);
		LNEvent mylnEvent = new LNEvent();
		mylnEvent.setCustomField3("192.168.51.63");
		
		Message<LNEvent> myLnEvent = MessageBuilder.withPayload(mylnEvent).build();
		
		channel.send(myLnEvent);
	}
}
