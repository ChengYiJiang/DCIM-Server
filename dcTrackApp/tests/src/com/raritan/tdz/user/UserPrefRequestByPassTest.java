package com.raritan.tdz.user;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.user.home.UserHome;

public class UserPrefRequestByPassTest extends TestBase {

	private UserHome userHome;
	
	@Override
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		userHome = (UserHome)ctx.getBean("userHome");
	}
	
	@Test
	void testEnableRequestByPassForUser() {
		long userId = 1;
		boolean requestBypassValue = true;
		setRequestBypassSettingForUser(requestBypassValue, userId);
	}
	
	@Test
	void testDisableRequestByPassForUser() {
		long userId = 1;
		boolean requestBypassValue = false;
		setRequestBypassSettingForUser(requestBypassValue, userId);
	}

	@Test
	void testGetRequestByPassSettingForUser() {
		long userId = 1;
		boolean rb = userHome.getUserRequestByPassSetting(userId);
		System.out.println ("RB = " + rb);
		
	}
	
	private void setRequestBypassSettingForUser(boolean value, long userId) {
		boolean expected = value;
		userHome.setUserRequestByPassSetting(expected, userId);
		
		boolean actual = userHome.getUserRequestByPassSetting(userId);
		System.out.println ("RB: expected =" + expected + " actual = " + actual);
		Assert.assertEquals(actual, expected);
	}
	
	
	
}
