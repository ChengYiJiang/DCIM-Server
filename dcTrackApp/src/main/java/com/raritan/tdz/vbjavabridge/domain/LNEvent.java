/**
 * 
 */
package com.raritan.tdz.vbjavabridge.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raritan.tdz.domain.LksData;

/**
 * @author prasanna
 *
 */
@Entity
@Table(name="`dct_lnevents`")
public class LNEvent implements Serializable{
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1324298605512036161L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dct_lnevents_seq")
	@SequenceGenerator(name = "dct_lnevents_seq", sequenceName = "dct_lnevents_event_id_seq", allocationSize=1)
	@Column(name = "event_id", unique = true, nullable = false)
	private long eventId;

	@ManyToOne(fetch = FetchType.EAGER, targetEntity= LksData.class)
	@JoinColumn(name = "db_operation_lks_id")
	private LksData operationLks;
	
	@Column(name = "table_name", nullable = false)
	private String tableName;
	
	@Column(name = "table_row_id", nullable = false)
	private long tableRowId;
	
	@Column(name = "custom_field1", nullable = true)
	private String customField1;
	
	@Column(name = "custom_field2", nullable = true)
	private String customField2;

	@Column(name = "action", nullable = true)
	private String action;
	
	@Column(name = "custom_field3", nullable = true)
	private String customField3;
	
	public LNEvent(long eventId, LksData operationLks, String tableName,
			long tableRowId, String customField1, String customField2,
			String action) {
		super();
		this.eventId = eventId;
		this.operationLks = operationLks;
		this.tableName = tableName;
		this.tableRowId = tableRowId;
		this.customField1 = customField1;
		this.customField2 = customField2;
		this.action = action;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public long getEventId() {
		return eventId;
	}
	

	public LksData getOperationLks() {
		return operationLks;
	}

	public void setOperationLks(LksData operationLks) {
		this.operationLks = operationLks;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public long getTableRowId() {
		return tableRowId;
	}

	public void setTableRowId(long tableRowId) {
		this.tableRowId = tableRowId;
	}
	
	public String getCustomField1() {
		return customField1;
	}

	public void setCustomField1(String customField1) {
		this.customField1 = customField1;
	}

	public String getCustomField2() {
		return customField2;
	}

	public void setCustomField2(String customField2) {
		this.customField2 = customField2;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	

	public String getCustomField3() {
		return customField3;
	}

	public void setCustomField3(String customField3) {
		this.customField3 = customField3;
	}

	public LNEvent(){
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((customField1 == null) ? 0 : customField1.hashCode());
		result = prime * result
				+ ((customField2 == null) ? 0 : customField2.hashCode());
		result = prime * result
				+ ((customField3 == null) ? 0 : customField3.hashCode());
		result = prime * result + (int) (eventId ^ (eventId >>> 32));
		result = prime * result
				+ ((operationLks == null) ? 0 : operationLks.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		result = prime * result + (int) (tableRowId ^ (tableRowId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LNEvent other = (LNEvent) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (customField1 == null) {
			if (other.customField1 != null)
				return false;
		} else if (!customField1.equals(other.customField1))
			return false;
		if (customField2 == null) {
			if (other.customField2 != null)
				return false;
		} else if (!customField2.equals(other.customField2))
			return false;
		if (customField3 == null) {
			if (other.customField3 != null)
				return false;
		} else if (!customField3.equals(other.customField3))
			return false;
		if (eventId != other.eventId)
			return false;
		if (operationLks == null) {
			if (other.operationLks != null)
				return false;
		} else if (!operationLks.equals(other.operationLks))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		if (tableRowId != other.tableRowId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LNEvent [eventId=" + eventId + ", operationLks=" + operationLks
				+ ", tableName=" + tableName + ", tableRowId=" + tableRowId
				+ ", customField1=" + customField1 + ", customField2="
				+ customField2 + ", action=" + action + ", customField3="
				+ customField3 + "]";
	}	
}
