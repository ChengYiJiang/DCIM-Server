package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
@JsonIgnoreProperties(ignoreUnknown=true)
public class JobMessagesJSON {

	private List<JobMessage> jobMessages;
	
	public JobMessagesJSON() {
		super();
	}

	@JsonProperty(value="job_messages")
	public List<JobMessage> getJobMessages() {
		return jobMessages;
	}
	
	@JsonSetter(value="job_messages")
	@JsonDeserialize(contentAs=JobMessage.class)
	public void setJobMessages(List<JobMessage> jobMessages) {
		this.jobMessages = jobMessages;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JobMessage {
		private Long jobId;
		private String level;
//		private String trace;
//		private String startTime;
//		private String endTime;
		private boolean aborted;
		private String message;
		
		public JobMessage() {
			super();
		}
		@JsonProperty(value="job_id")
		public Long getId() {
			return jobId;
		}
		@JsonSetter(value="job_id")
		public void setId(Long jobId) {
			this.jobId = jobId;
		}
		
		@JsonProperty(value="level")
		public String getLevel() {
			return level;
		}
		@JsonSetter(value="level")
		public void setLevel(String level) {
			this.level = level;
		}
		
//		@JsonProperty(value="trace")
//		public String getTrace() {
//			return trace;
//		}
//		@JsonSetter(value="level")
//		public void setTrace(String trace) {
//			this.trace = trace;
//		}
		
//		@JsonProperty(value="start_time")
//		public String getStartTime() {
//			return startTime;
//		}
//		@JsonSetter(value="start_time")
//		public void setStartTime(String startTime) {
//			this.startTime = startTime;
//		}
//		
//		@JsonProperty(value="end_time")
//		public String getEndTime() {
//			return endTime;
//		}
//		@JsonSetter(value="end_time")
//		public void setEndTime(String endTime) {
//			this.endTime = endTime;
//		}
		
		@JsonProperty(value="aborted")
		public boolean isAborted() {
			return aborted;
		}
		@JsonSetter(value="aborted")
		public void setAborted(boolean aborted) {
			this.aborted = aborted;
		}
		
		@JsonProperty(value="message")
		public String getMessage() {
			return message;
		}
		@JsonSetter(value="message")
		public void setMessage(String message) {
			this.message = message;
		}
	}
}
