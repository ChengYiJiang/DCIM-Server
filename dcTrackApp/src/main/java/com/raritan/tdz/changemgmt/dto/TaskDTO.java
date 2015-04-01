package com.raritan.tdz.changemgmt.dto;

import java.util.Date;

/**
 * Represents an individual task associated with request.
 * @author Andrew Cohen
 * @version 3.0
 */
public class TaskDTO {

	private String taskId;
	private String name;
	private String assignee;
	private boolean isDone = false;
	private Date dueDate;
	private Boolean approval = null;
	private RequestDTO request;
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String id) {
		this.taskId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAssignee() {
		return assignee;
	}
	
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public boolean getIsDone() {
		return isDone;
	}
	
	public void setIsDone(boolean isDone) {
		this.isDone = isDone;
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public RequestDTO getRequest() {
		return request;
	}
	
	public void setRequest(RequestDTO requests) {
		this.request = requests;
	}

	public Boolean getApproval() {
		return approval;
	}

	public void setApproval(Boolean approval) {
		this.approval = approval;
	}
}
