/**
 * 
 */
package com.raritan.tdz.piq.home;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.home.EventHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.piq.jobs.PIQJobHandler;
import com.raritan.tdz.piq.jobs.PIQJobPoller;
import com.raritan.tdz.piq.json.JobJSON;
import com.raritan.tdz.piq.json.JobJSON.Job;
import com.raritan.tdz.piq.json.JobJSON.JobStatus;
import com.raritan.tdz.piq.json.JobMessagesJSON;
import com.raritan.tdz.piq.json.JobMessagesJSON.JobMessage;
import com.raritan.tdz.piq.json.PduJSON;
import com.raritan.tdz.settings.home.ApplicationSettings;
import com.raritan.tdz.tests.TestBase;

/**
 * Test PIQ Jobs.
 * @author Andrew Cohen
 */
public class PIQJobsTest extends TestBase {

	private ApplicationSettings appSettings = null;
	private PIQJobPoller jobsPoller = null;
	private PIQJobHandler jobHandler = null;
	
	private Item pdu;
	private String pduIpAddress;
	private DataPort dataPort;
	private JobsRestClient rest;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeMethod
	public void setUp() throws Throwable {	
		super.setUp();
	    appSettings = (ApplicationSettings) ctx.getBean("appSettings");
		jobsPoller = (PIQJobPoller) ctx.getBean("piqJobsPoller");
		jobHandler = (PIQJobHandler) ctx.getBean("pduJobHandler");
	    rest = new JobsRestClient( appSettings );
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Exception {
//    	SessionHolder holder = (SessionHolder)
//            	TransactionSynchronizationManager.getResource(sf);
//            Session s = holder.getSession();
//            s.flush();
//            TransactionSynchronizationManager.unbindResource(sf);
//            SessionFactoryUtils.closeSession(s);
	}

	/**
	 * Tests adding a PDU job that should result in successfully adding the PDU to power IQ.
	 * @throws Throwable
	 */
	//@Test
	@Test
	public final void testAddPDUJobSuccess() throws Throwable {
		runPDUJobTest( false, "192.168.50.185" );
	}
	
	/**
	 * Tests adding a PDU job that should result in an error.
	 * @throws Throwable
	 */
	//@Test
	@Test
	public final void testAddPDUJobError() throws Throwable {
		runPDUJobTest( true, "192.168.151.151" );
	}
	
	//@Test
	@Test
	public final void testBulkLoadPDUS() throws Throwable {
		String badIPBase = "192.168.151.";
		int ipSuffix = 10;
		
		for (int i=0; i<5; i++) {
			runPDUJobTest( true, badIPBase + ipSuffix);
			ipSuffix++;
		}
		
		runPDUJobTest(false, "192.168.50.185");
		runPDUJobTest( true, badIPBase + ipSuffix);
	}
	
	private void runPDUJobTest(boolean expectError, String ipAddress) throws Throwable {
//		assertEquals(0, jobsPoller.getActiveJobs());
		
		// Generate PDU data with no community string - this should result in an error
		generateTestPduData( ipAddress );
		
		PduJSON pdujson = new PduJSON(pdu, dataPort, pduIpAddress, false);
		JobJSON jobJson = (JobJSON) rest.doRestPost(pdujson, "v2/pdus", "?async=true", JobJSON.class);
		assertNotNull( jobJson );
		String jobId = jobJson.getJob().getId();
		assertNotNull( jobId );
		
		List<PIQJobHandler> handlers = new LinkedList<PIQJobHandler>();
		handlers.add( jobHandler ); // The real handler that links the PIQ ID to the PDU item
		PDUJobValidator validator = new PDUJobValidator( expectError ); // Another handler for validation
		handlers.add( validator );
		
		// Submit the job
		jobId = jobJson.getJob().getId();
		
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("ipAddress", ipAddress);
		jobData.put("pduItem", pdu);
		jobsPoller.addJob(jobId, handlers, jobData);
		
		//assertEquals(1, jobsPoller.getActiveJobs());
//		int threadCount = Thread.activeCount();
		
		//assertEquals(0, jobsPoller.getActiveJobs());
		//assertEquals(threadCount - 1, Thread.activeCount());
		
		System.out.println("Tests passed with "+validator.updates+" polling updates");
	}
	
	/**
	 * A job handler used for verifying job status.
	 */
	private class PDUJobValidator implements PIQJobHandler {
		private int updates;
		private boolean expectError;
		
		public PDUJobValidator(boolean expectError) {
			this.expectError = expectError;
		}
		
		@Override
		public synchronized void onJobComplete(Job job, Map<String, Object> data) {
			assertEquals(expectError, false);
			assertNotNull( data.get("ipAddress") );
			System.out.println("Job completed: " + job.getId());
			assertNotNull( job );
			assertEquals( job.getJobStatus(), JobStatus.COMPLETED);
			this.notifyAll();
		}

		@Override
		public synchronized void onJobError(Job job, Map<String, Object> data) {
			assertEquals(expectError, true);
			assertNotNull( data.get("ipAddress") );
			System.out.println("Job completed with errors: " + job.getId());
			assertNotNull( job );
			assertEquals((Boolean)true, job.getHasErrors() );
			
			List<JobMessage> messages = null;
			try {
				ResponseEntity<?> resp = rest.doRestGet("v2/job_messages", "?job_id_eq=" +  job.getId(), JobMessagesJSON.class);
				if (resp != null) {
					JobMessagesJSON list = (JobMessagesJSON)resp.getBody();
					if (list != null) {
						messages = list.getJobMessages();
						if (messages != null) {
							Collections.reverse( messages );
							for (JobMessage msg : messages) {
								System.out.println(msg.getMessage());
							}
						}
					}
				}
			}
			catch (RemoteDataAccessException e) {
				e.printStackTrace();
			}
			
			assertNotNull( messages );
			assertTrue( !messages.isEmpty() );
			
			this.notifyAll();
		}

		@Override
		public synchronized void onJobUpdate(Job job, Map<String, Object> data) {
			assertNotNull( job );
			assertNotNull( data.get("ipAddress") );
			assertEquals( job.getJobStatus(), JobStatus.ACTIVE);
			updates++;
		}
	}
	
	private void generateTestPduData(String ipAddress) {
		// The PDU IP Address
	    pduIpAddress = ipAddress;
	    
	    // The PDU to add to PIQ
	    pdu = new Item();
	    pdu.setItemName("Test PDU " + UUID.randomUUID().toString());
	    pdu.setClassLookup( SystemLookup.getLksData(session, SystemLookup.Class.RACK_PDU) );
	    pdu.setDataCenterLocation( (DataCenterLocationDetails) session.get(DataCenterLocationDetails.class, 1L) );
	    session.save( pdu );
	    session.flush();
	    
	    // The PDU data port
	    dataPort = new DataPort();
	    dataPort.setPortName("Test PDU Data port");
	    dataPort.setIpAddress( pduIpAddress );
	    dataPort.setCommunityString( "public" );
	    
	    // Make sure the PDU to be added does not already exist in PIQ
	    //rest.doRestDelete(piqId, "v2/pdus");
	}
	
	private class JobsRestClient extends PIQRestClientBase {
		
		public JobsRestClient(ApplicationSettings settings) throws DataAccessException {
			super( settings );
			setService("v2/jobs");
		    setRestTemplate( (RestTemplate)ctx.getBean("restTemplate") );
		    setEventHome( (EventHome)ctx.getBean("eventHome") );
		    setMessageSource( (MessageSource)ctx.getBean("messageSource"));
		}
	}
}
