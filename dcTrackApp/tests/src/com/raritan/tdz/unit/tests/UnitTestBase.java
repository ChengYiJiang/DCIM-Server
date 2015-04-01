package com.raritan.tdz.unit.tests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.validation.MapBindingResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.domain.cmn.Users;
import com.raritan.tdz.vbjavabridge.home.LNHome;

@ContextConfiguration(locations = {"classpath:unitTestsContext.xml"})
public class UnitTestBase extends AbstractTestNGSpringContextTests{

  private final String testContextXML = "unitTestsContext.xml";
  protected static ClassPathXmlApplicationContext ctx = null;
  protected static Logger log = null;
  @Autowired
  protected Mockery jmockContext;
  
  @Autowired
  protected LNHome listenNotifyHome;
  
//  
  @BeforeMethod
  public void beforeMethod() {
	  // jmockContext = (Mockery)ctx.getBean("jmockContext");
	  listenNotifyHome.setSuspend(true);
  }

//  @AfterMethod
//  public void afterMethod() {
//  }

  @BeforeClass
  public void beforeClass() {
	  listenNotifyHome.setSuspend(true);
  }

//  @AfterClass
//  public void afterClass() {
//  }

//  @BeforeTest
//  public void beforeTest() {
//	  
//  }
//
//  @AfterTest
//  public void afterTest() {
//	  
//  }
//
  @BeforeSuite
  public void beforeSuite() {
	 
  }

  @AfterSuite
  public void afterSuite() {
  }
  
  
  protected MapBindingResult getErrorObject(Class<?> targetClass) {
	Map<String, String> errorMap = new HashMap<String, String>();
	MapBindingResult errors = new MapBindingResult(errorMap, targetClass.getName());
	return errors;
  }
	
//  @Test
//  public void test(){
//	  Assert.fail();
//  }
  
	  protected UserInfo getUserInfo(UserInfo.UserAccessLevel accessLevel){
		  return UserMock.getUserInfo(accessLevel);
	  }
	  
	  protected Users getUser(UserInfo.UserAccessLevel accessLevel){
		  return UserMock.getUser(accessLevel);
	  }
		
	  protected static String getMethodName() 	{
			final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			return ste[2].getMethodName();
	  }
	
	protected Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo, String msg) {
		
		Map<String,Object> targetMap = new HashMap<String, Object>();
		
		if (item != null) targetMap.put(item.getClass().getName(), item);
		if (userInfo != null) targetMap.put(userInfo.getClass().getName(), userInfo);
		if (msg != null) targetMap.put(msg.getClass().getName(), msg);
		return targetMap;
	}

}
