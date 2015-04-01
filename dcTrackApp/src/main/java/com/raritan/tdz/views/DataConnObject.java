package com.raritan.tdz.views;

import java.sql.Timestamp;

public class DataConnObject {
	private Long connectionId;
	private DataPortObject sourcePort;
	private DataPortObject destPort;
	private int confidence;
	private ConnCordObject connCord;
	private boolean isImplicit;
	private String comments;
	private Timestamp creationDate;
	private String createdBy;
	private Timestamp updateDate;
	private Long connTypeLksId;
	private String connTypeLksDesc;
	private Long statusLksId;
	private String statusLksDesc;
	private Long circuitId;
	
	public Long getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
	}
	
	public Long getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}
	public DataPortObject getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(DataPortObject sourcePort) {
		this.sourcePort = sourcePort;
	}
	public DataPortObject getDestPort() {
		return destPort;
	}
	public void setDestPort(DataPortObject destPort) {
		this.destPort = destPort;
	}
	public int getConfidence() {
		return confidence;
	}
	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}
	public ConnCordObject getConnCord() {
		return connCord;
	}
	public void setConnCord(ConnCordObject connCord) {
		this.connCord = connCord;
	}
	public boolean isImplicit() {
		return isImplicit;
	}
	public void setImplicit(boolean isImplicit) {
		this.isImplicit = isImplicit;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Timestamp getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}
	public Long getConnTypeLksId() {
		return connTypeLksId;
	}
	public void setConnTypeLksId(Long connTypeLksId) {
		this.connTypeLksId = connTypeLksId;
	}
	public String getConnTypeLksDesc() {
		return connTypeLksDesc;
	}
	public void setConnTypeLksDesc(String connTypeLksDesc) {
		this.connTypeLksDesc = connTypeLksDesc;
	}
	public Long getStatusLksId() {
		return statusLksId;
	}
	public void setStatusLksId(Long statusLksId) {
		this.statusLksId = statusLksId;
	}
	public String getStatusLksDesc() {
		return statusLksDesc;
	}
	public void setStatusLksDesc(String statusLksDesc) {
		this.statusLksDesc = statusLksDesc;
	}
	
	
}
