package com.raritan.tdz.circuit.dto;

import java.sql.Timestamp;

import com.raritan.tdz.dto.PortInterface;

public interface ConnectionDTO {

	public abstract Long getConnectionId();

	public abstract void setConnectionId(Long connectionId);

	public abstract PortInterface getLeftPort();

	public abstract void setLeftPort(PortInterface leftPort);

	public abstract PortInterface getRightPort();

	public abstract void setRightPort(PortInterface rightPort);

	public abstract int getConfidence();

	public abstract void setConfidence(int confidence);

	public abstract ConnCordDTO getConnCord();

	public abstract void setConnCord(ConnCordDTO connCord);

	public abstract boolean isImplicit();

	public abstract void setImplicit(boolean isImplicit);

	public abstract String getComments();

	public abstract void setComments(String comments);

	public abstract Timestamp getCreationDate();

	public abstract void setCreationDate(Timestamp creationDate);

	public abstract String getCreatedBy();

	public abstract void setCreatedBy(String createdBy);

	public abstract Timestamp getUpdateDate();

	public abstract void setUpdateDate(Timestamp updateDate);

	public abstract Long getConnTypeLksId();

	public abstract void setConnTypeLksId(Long connTypeLksId);

	public abstract String getConnTypeLksDesc();

	public abstract void setConnTypeLksDesc(String connTypeLksDesc);

	public abstract Long getStatusLksId();

	public abstract void setStatusLksId(Long statusLksId);

	public abstract String getStatusLksDesc();

	public abstract void setStatusLksDesc(String statusLksDesc);

	public abstract Long getCircuitId();

	public abstract void setCircuitId(Long circuitId);

}