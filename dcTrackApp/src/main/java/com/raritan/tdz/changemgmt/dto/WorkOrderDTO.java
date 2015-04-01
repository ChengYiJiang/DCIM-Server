package com.raritan.tdz.changemgmt.dto;

import com.raritan.tdz.domain.WorkOrder;

/**
 * Change Management Work Order DTO.
 * @author Andrew Cohen
 * @version 3.0
 */
public class WorkOrderDTO {

	private long workOrderId;
	
	private String workOrderNumber;
	
	private String requestNo;
	
	public WorkOrderDTO() {}
	
	public WorkOrderDTO(WorkOrder wo) {
		setWorkOrderId( wo.getWorkOrderId() );
		setWorkOrderNumber( wo.getWorkOrderNumber() );
		/*if (wo.getRequest() != null) {
			setRequestNo( wo.getRequest().getRequestNo() );
		}*/
	}
	
	public long getWorkOrderId() {
		return workOrderId;
	}
	
	public void setWorkOrderId(long workOrderId) {
		this.workOrderId = workOrderId;
	}
	
	public String getWorkOrderNumber() {
		return workOrderNumber;
	}
	
	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}
	
	public String getRequestNo() {
		return requestNo;
	}
	
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		
		if (obj instanceof WorkOrderDTO) {
			String wo1 = this.getWorkOrderNumber();
			String wo2 = ((WorkOrderDTO)obj).getWorkOrderNumber();
			return wo1 != null && wo2 != null && wo1.endsWith(wo2);
		}
		
		return false;
	}
	
	public int hashCode() {
		return this.getWorkOrderNumber().hashCode();
	}
}
