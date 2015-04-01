package com.raritan.tdz.changemgmt.dto;

import java.util.List;

import com.raritan.tdz.domain.Request;

/**
 * A change management request and all of its details.
 * @author Andrew Cohen
 * @version 3.0
 */
public class RequestDTO {

	private long requestId;
	private String requestNo;
	private List<TaskDTO> tasks;

	public RequestDTO() {}
	
	public RequestDTO(Request req) {
		setRequestId( req.getRequestId() );
		setRequestNo( req.getRequestNo() );
	}
	
	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public String getRequestNo() {
		return requestNo;
	}

	public void setRequestNo(String requestId) {
		this.requestNo = requestId;
	}

	public List<TaskDTO> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskDTO> tasks) {
		this.tasks = tasks;
	}
}
