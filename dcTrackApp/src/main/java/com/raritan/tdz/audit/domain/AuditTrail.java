package com.raritan.tdz.audit.domain;

import java.sql.Timestamp;
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

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.field.domain.Fields;

/**
* AuditTrail generated by hbm2java
*/
@Entity
@Table(name="dct_audit_trail")
public class AuditTrail  implements java.io.Serializable {		
	 private static final long serialVersionUID = 7175928785517380814L;
	
	 public AuditTrail() {
	 }
	 
	
	 
	 public AuditTrail(long auditTrailId, DataCenterLocationDetails location,
			LksData classLookup, String dbColumnName, Fields field,
			String displayName, String auditAction, long recordId,
			String tableName, String oldValue, String newValue,
			String changeBy, Timestamp changeDate, Item item) {
		super();
		this.auditTrailId = auditTrailId;
		this.location = location;
		this.classLookup = classLookup;
		this.dbColumnName = dbColumnName;
		this.field = field;
		this.displayName = displayName;
		this.auditAction = auditAction;
		this.recordId = recordId;
		this.tableName = tableName;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.changeBy = changeBy;
		this.changeDate = changeDate;
		this.item = item;
	}
	
	
	@Id
	 @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dct_audit_trail_seq")
	 @SequenceGenerator(name = "dct_audit_trail_seq", sequenceName = "dct_audit_trail_audit_trail_id_seq", allocationSize=1)
	 @Column(name="audit_trail_id", unique=true, nullable=false)
	 private long auditTrailId;
	 
	 public long getAuditTrailId() {
	     return this.auditTrailId;
	 }
	 
	 public void setAuditTrailId(long auditTrailId) {
	     this.auditTrailId = auditTrailId;
	 }
	 
	 @ManyToOne(fetch = FetchType.LAZY)
	 @JoinColumn(name = "location_id", nullable = true)
	 private DataCenterLocationDetails location; 
	
	 public DataCenterLocationDetails getLocation() {
		return location;
	 }
	
	 public void setLocation(DataCenterLocationDetails location) {
		this.location = location;
	 }
	 
		
	 @ManyToOne(fetch = FetchType.LAZY)
	 @JoinColumn(name = "class_lks_id", nullable = false)
	 private LksData classLookup;
	
	 public LksData getClassLookup() {
		return classLookup;
	 }
	
	 public void setClassLookup(LksData classLookup) {
		this.classLookup = classLookup;
	 }
	 
		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id")
	private Item item;
	
	public Item getItem() {
		return item;
	}
	
	
	public void setItem(Item item) {
		this.item = item;
	}	 	 
	 
	 @Column(name="db_column_name")
	 private String dbColumnName;
	 
	 public String getDbColumnName() {
	     return this.dbColumnName;
	 }
	 
	 public void setDbColumnName(String dbColumnName) {
	     this.dbColumnName = dbColumnName;
	 }
	 
	 @ManyToOne(fetch = FetchType.LAZY)
	 @JoinColumn(name = "field_id", nullable = false)
	 private Fields field;
	
	 public Fields getField() {
		return field;
	 }
	
	 public void setField(Fields field) {
		this.field = field;
	 }
	 
	 
	 @Column(name="display_name")
	 private String displayName;
	 
	 public String getDisplayName() {
	     return this.displayName;
	 }
	 
	 public void setDisplayName(String displayName) {
	     this.displayName = displayName;
	 }
	 
	 @Column(name="audit_action")
	 private String auditAction;
	 
	 public String getAuditAction() {
	     return this.auditAction;
	 }
	 
	 public void setAuditAction(String auditAction) {
	     this.auditAction = auditAction;
	 }
	 
	 @Column(name="record_id")
	 private long recordId;
	 
	 public long getRecordId() {
	     return this.recordId;
	 }
	 
	 public void setRecordId(long recordId) {
	     this.recordId = recordId;
	 }
	 
	 @Column(name="table_name")
	 private String tableName;
	 
	 public String getTableName() {
	     return this.tableName;
	 }
	 
	 public void setTableName(String tableName) {
	     this.tableName = tableName;
	 }
	 
	 @Column(name="old_value")
	 private String oldValue;
	 
	 public String getOldValue() {
	     return this.oldValue;
	 }
	 
	 public void setOldValue(String oldValue) {
	     this.oldValue = oldValue;
	 }
	 
	 @Column(name="new_value")
	 private String newValue;
	 
	 public String getNewValue() {
	     return this.newValue;
	 }
	 
	 public void setNewValue(String newValue) {
	     this.newValue = newValue;
	 }
	 
	 @Column(name="change_by")
	 private String changeBy;
	 
	 public String getChangeBy() {
	     return this.changeBy;
	 }
	 
	 public void setChangeBy(String changeBy) {
	     this.changeBy = changeBy;
	 }
	 
	 @Column(name="change_date")
	 private Timestamp changeDate;
	 
	 public Timestamp getChangeDate() {
	     return this.changeDate;
	 }
	 
	 public void setChangeDate(Timestamp changeDate) {
	     this.changeDate = changeDate;
	 }
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((auditAction == null) ? 0 : auditAction.hashCode());
		result = prime * result + (int) (auditTrailId ^ (auditTrailId >>> 32));
		result = prime * result
				+ ((changeBy == null) ? 0 : changeBy.hashCode());
		result = prime * result
				+ ((changeDate == null) ? 0 : changeDate.hashCode());
		result = prime * result
				+ ((classLookup == null) ? 0 : classLookup.hashCode());
		result = prime * result
				+ ((dbColumnName == null) ? 0 : dbColumnName.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result
				+ ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result
				+ ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + (int) (recordId ^ (recordId >>> 32));
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
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
		AuditTrail other = (AuditTrail) obj;
		if (auditAction == null) {
			if (other.auditAction != null)
				return false;
		} else if (!auditAction.equals(other.auditAction))
			return false;
		if (auditTrailId != other.auditTrailId)
			return false;
		if (changeBy == null) {
			if (other.changeBy != null)
				return false;
		} else if (!changeBy.equals(other.changeBy))
			return false;
		if (changeDate == null) {
			if (other.changeDate != null)
				return false;
		} else if (!changeDate.equals(other.changeDate))
			return false;
		if (classLookup == null) {
			if (other.classLookup != null)
				return false;
		} else if (!classLookup.equals(other.classLookup))
			return false;
		if (dbColumnName == null) {
			if (other.dbColumnName != null)
				return false;
		} else if (!dbColumnName.equals(other.dbColumnName))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		if (recordId != other.recordId)
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

}



