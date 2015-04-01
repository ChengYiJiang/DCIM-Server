/**
 * 
 */
package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JobJSON {
	
	private Job job;
	
	public enum JobStatus {
		ACTIVE,
		COMPLETED,
		ABORTED
	};
	
	
	/**
	 * @return the job
	 */
	@JsonProperty(value="job")
	public final Job getJob() {
		return job;
	}



	/**
	 * @param job the job to set
	 */
	@JsonSetter(value="job")
	public final void setJob(Job job) {
		this.job = job;
	}


	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Job {
		private String id;
		private String userId;
		private String status;
		private String description;
		private String startTime;
		private String endTime;
		private Boolean hasErrors;
		private Double percentComplete;
		private Boolean completed;
		private String lastMessage;
		private Integer errorCount;
	
		public Job() {
			super();
		}
		/**
		 * @return the id
		 */
		@JsonProperty(value="id")
		public final String getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		@JsonSetter(value="id")
		public final void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the userId
		 */
		@JsonProperty(value="user_id")
		public final String getUserId() {
			return userId;
		}
		/**
		 * @param userId the userId to set
		 */
		@JsonSetter(value="user_id")
		public final void setUserId(String userId) {
			this.userId = userId;
		}
		/**
		 * @return the status
		 */
		@JsonProperty(value="status")
		public final String getStatus() {
			return status;
		}
		/**
		 * @param status the status to set
		 */
		@JsonSetter(value="status")
		public final void setStatus(String status) {
			this.status = status;
		}
		/**
		 * @return the description
		 */
		@JsonProperty(value="description")
		public final String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		@JsonSetter(value="description")
		public final void setDescription(String description) {
			this.description = description;
		}
		/**
		 * @return the startTime
		 */
		@JsonProperty(value="start_time")
		public final String getStartTime() {
			return startTime;
		}
		/**
		 * @param startTime the startTime to set
		 */
		@JsonSetter(value="start_time")
		public final void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		/**
		 * @return the endTime
		 */
		@JsonProperty(value="end_time")
		public final String getEndTime() {
			return endTime;
		}
		/**
		 * @param endTime the endTime to set
		 */
		@JsonSetter(value="end_time")
		public final void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		/**
		 * @return the hasErrors
		 */
		@JsonProperty(value="has_errors")
		public final Boolean getHasErrors() {
			return hasErrors;
		}
		/**
		 * @param hasErrors the hasErrors to set
		 */
		@JsonSetter(value="has_errors")
		public final void setHasErrors(Boolean hasErrors) {
			this.hasErrors = hasErrors;
		}
		/**
		 * @return the percentComplete
		 */
		@JsonProperty(value="percent_complete")
		public final Double getPercentComplete() {
			return percentComplete;
		}
		/**
		 * @param percentComplete the percentComplete to set
		 */
		@JsonSetter(value="percent_complete")
		public final void setPercentComplete(Double percentComplete) {
			this.percentComplete = percentComplete;
		}
		/**
		 * @return the completed
		 */
		@JsonProperty(value="completed")
		public final Boolean getCompleted() {
			return completed;
		}
		/**
		 * @param completed the completed to set
		 */
		@JsonSetter(value="completed")
		public final void setCompleted(Boolean completed) {
			this.completed = completed;
		}
		/**
		 * @return the lastMessage
		 */
		@JsonProperty(value="last_message")
		public final String getLastMessage() {
			return lastMessage;
		}
		/**
		 * @param lastMessage the lastMessage to set
		 */
		@JsonSetter(value="last_message")
		public final void setLastMessage(String lastMessage) {
			this.lastMessage = lastMessage;
		}
		/**
		 * @return the errorCount
		 */
		@JsonProperty(value="error_count")
		public final Integer getErrorCount() {
			return errorCount;
		}
		/**
		 * @param errorCount the errorCount to set
		 */
		@JsonSetter(value="error_count")
		public final void setErrorCount(Integer errorCount) {
			this.errorCount = errorCount;
		}
		
		public JobStatus getJobStatus() {
			return JobStatus.valueOf( getStatus() );
		}
	}
}
