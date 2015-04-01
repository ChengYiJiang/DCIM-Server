package com.raritan.tdz.mail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.tests.TestBase;

public class DcTrackMailTest extends TestBase {
	protected DcTrackMailClientImpl dcTrackMailClient;

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
	}
	
	//@Test
	public final void testSendMail() throws Throwable {
		dcTrackMailClient  = (DcTrackMailClientImpl)ctx.getBean("dcTrackMailClient");
		
    	String sender = "basker";
    	String[] recepient = {"basker@raritan.com"};
    	
    	dcTrackMailClient.sendSimpleMail(sender, recepient, "testing javaSendMail", "Testing java send mail -");
    	
    	dcTrackMailClient.sendMail(sender, recepient, "testing javaSendMail", "Testing java send mail =");
    	
    	dcTrackMailClient.sendMail(sender, recepient, "testing javaSendMail withAttachment", "testing javaSendMail with attachment", "/tmp/txt");
    }
	
}
