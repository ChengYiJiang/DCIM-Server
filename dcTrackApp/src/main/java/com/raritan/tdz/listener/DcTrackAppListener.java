package com.raritan.tdz.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.raritan.tdz.changemgmt.home.workflow.WorkflowHome;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.SystemException;
import com.raritan.tdz.piq.home.PIQAssetStripEventBoot;
import com.raritan.tdz.util.DCTColumnsSchema;

/**
 * An listener for invoking code whenever dcTrackApp is initialized or refreshed.
 * This is currently just used for initializing the configured Workflow service.
 * 
 * @author Andrew Cohen
 * @version 3.0
 *
 */
public class DcTrackAppListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private WorkflowHome workflowHome;
	
	@Autowired
	private DCTColumnsSchema columnsSchema;
	
	@Autowired
	private PIQAssetStripEventBoot piqAssetStripEventBoot;
	
	private static String mainContextId = "org.springframework.web.context.WebApplicationContext:/dcTrackApp";
	
	public DcTrackAppListener() {}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		columnsSchema.createUiIDPropertyLengthMap();
		if (event.getApplicationContext().getId().equals(mainContextId)){
			try {
				piqAssetStripEventBoot.init();
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
