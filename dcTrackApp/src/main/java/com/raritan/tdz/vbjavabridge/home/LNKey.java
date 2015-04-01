/**
 * 
 */
package com.raritan.tdz.vbjavabridge.home;

import com.raritan.tdz.domain.LksData;

/**
 * @author prasanna
 * This is a helper class that contains the key for the ListenNotifyEvent map used
 * in dcTrackListenNotifyHomeImpl.
 * The key will be a combination of eventName which could be one of the following:
 * INSERT, UPDATE, DELETE 
 * and the table Name can be any table name within the dcTrack schema. 
 */
public class LNKey {
	
	private LksData operationLks;
	private String tableName;
	private String action;
	
	public LNKey(LksData operationLks, String tableName){
		this.operationLks = operationLks;
		this.tableName = tableName;
	}

	public LNKey(LksData operationLks, String tableName, String action){
		this.operationLks = operationLks;
		this.tableName = tableName;
		this.action = action;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((operationLks == null) ? 0 : operationLks.hashCode());
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
		LNKey other = (LNKey) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
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
		return true;
	}

	@Override
	public String toString() {
		return "LNKey [operationLks=" + operationLks + ", tableName="
				+ tableName + ", action=" + action + "]";
	}
	
}
