package com.raritan.tdz.request.progress;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.raritan.tdz.lookup.SystemLookup;

/**
 * Lookup for the request progress framework
 * @author bunty
 *
 */
public class RequestProgressLookup {

	public static class state {
		
		public static final long REQUEST_PROGRESS_START = 0L;
		
		public static final long REQUEST_PROGRESS_RUNNING = 1L;
		
		public static final long REQUEST_PROGRESS_FINISH = 2L;
		
	};
	
	public static class requestStageProgress {
		
		public static final String REQUEST_PROGRESS_START = "Processing Request Starts ...";
		
		public static final String REQUEST_PROGRESS_END = "Processing Completed.";
		
		public static final String REQUEST_PROGRESS_ISSUE = "Issuing Request...";
		
		public static final String REQUEST_PROGRESS_APPROVE = "Approving Request...";
		
		public static final String REQUEST_PROGRESS_WO_ISSUE = "Issuing Work Order...";
		
		public static final String REQUEST_PROGRESS_WO_COMPLETE = "Completing Work Order...";
		
		public static final String REQUEST_PROGRESS_COMPLETE = "Completing Request...";
		
		public static final String REQUEST_PROGRESS_VALIDATE = "Validating Request...";
		
		public static final String REQUEST_ABANDONED = "Request Abandoned.";
		
		public static final String REQUEST_ARCHIVED = "Request Archived.";
		
		public static final String REQUEST_REJECTED = "Request Rejected.";
		
		public static final String REQUEST_UPDATED = "Request Updated.";
		
	}
	
	
	@SuppressWarnings("serial")
	public static final Map<Long, String> itemRequestStageToRequestProgress = 
			Collections.unmodifiableMap(new HashMap<Long, String>() {{
				put(SystemLookup.RequestStage.REQUEST_ABANDONED, requestStageProgress.REQUEST_ABANDONED);
				put(SystemLookup.RequestStage.REQUEST_APPROVED, requestStageProgress.REQUEST_PROGRESS_APPROVE);
				put(SystemLookup.RequestStage.REQUEST_ARCHIVED, requestStageProgress.REQUEST_ARCHIVED);
				put(SystemLookup.RequestStage.REQUEST_COMPLETE, requestStageProgress.REQUEST_PROGRESS_COMPLETE);
				put(SystemLookup.RequestStage.REQUEST_ISSUED, requestStageProgress.REQUEST_PROGRESS_ISSUE);
				put(SystemLookup.RequestStage.REQUEST_REJECTED, requestStageProgress.REQUEST_REJECTED);
				put(SystemLookup.RequestStage.REQUEST_UPDATED, requestStageProgress.REQUEST_UPDATED);
				put(SystemLookup.RequestStage.WORK_ORDER_COMPLETE, requestStageProgress.REQUEST_PROGRESS_WO_COMPLETE);
				put(SystemLookup.RequestStage.WORK_ORDER_ISSUED, requestStageProgress.REQUEST_PROGRESS_WO_ISSUE);
				
			}});

	
}
