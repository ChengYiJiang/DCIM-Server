/**
 * 
 */
package com.raritan.tdz.piq.home;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.RemoteDataAccessException;
import com.raritan.tdz.home.DataCenterLocationHome;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class PIQSyncLocationClientTest extends TestBase {

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncLocationClientImpl#addLocation(com.raritan.tdz.domain.DataCenterLocationDetails)}.
	 */
	//@Test
	@Test
	public final void testAddLocation() {
		PIQSyncLocationClient client = (PIQSyncLocationClient)ctx.getBean("piqSyncLocationClient");
		DataCenterLocationHome dcHome = (DataCenterLocationHome)ctx.getBean("dataCenterLocationHome");
		List<DataCenterLocationDetails> locations = null;
		try {
			locations = dcHome.viewAll();
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			if (locations != null && locations.size() > 0){
				for (DataCenterLocationDetails location: locations){
					String piq_id = client.addDataCenter(location);
					assertNotNull(piq_id);
					assertTrue("Did not receive PIQ_ID for location " + location.getDcName(), (piq_id != null && !piq_id.isEmpty() ));
				}
			}
		} catch (RemoteDataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link com.raritan.tdz.piq.home.PIQSyncLocationClientImpl#isLocationInSync(DataCenterLocationDetails)}.
	 */
	//@Test
	@Test
	public final void testIsLocationInSync() {
		PIQSyncLocationClient client = (PIQSyncLocationClient)ctx.getBean("piqSyncLocationClient");
		try {
			assertTrue(client.isLocationInSync("DataCenter:1:Floor:1:Room:2"));
		} catch (RemoteDataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	@Test
	public final void testBulkLocationSync() {
		Job batchJob = (Job)ctx.getBean("piqBulkSyncJob");
		JobLauncher jobLauncher = (JobLauncher)ctx.getBean("piqSyncJobLauncher");
		try {
			JobExecution jobExecution = jobLauncher.run(batchJob, new JobParameters());
			Thread.sleep(5 *60 * 1000);
		} catch (JobExecutionAlreadyRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobRestartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobInstanceAlreadyCompleteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JobParametersInvalidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
