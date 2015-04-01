package com.raritan.tdz.circuit.dto;

import java.sql.Timestamp;
import com.raritan.tdz.dto.PortInterface;

public abstract class ConnectionDTOImpl implements ConnectionDTO {
	private Long connectionId;
	private PortInterface leftPort;
	private PortInterface rightPort;
	private int confidence;
	private ConnCordDTO connCord;
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
	private boolean last; 
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getConnectionId()
	 */
	@Override
	public Long getConnectionId() {
		return connectionId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setConnectionId(java.lang.Long)
	 */
	@Override
	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getLeftPort()
	 */
	@Override
	public PortInterface getLeftPort() {
		return leftPort;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setLeftPort(com.raritan.tdz.views.PortDTO)
	 */
	@Override
	public void setLeftPort(PortInterface leftPort) {
		this.leftPort = leftPort;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getRightPort()
	 */
	@Override
	public PortInterface getRightPort() {
		return rightPort;
	}

	@Override
	public void setRightPort(PortInterface rightPort) {
		this.rightPort = rightPort;
	}
	@Override
	public int getConfidence() {
		return confidence;
	}
	@Override
	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getConnCord()
	 */
	@Override
	public ConnCordDTO getConnCord() {
		return connCord;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setConnCord(com.raritan.tdz.views.circuits.ConnCordDTO)
	 */
	@Override
	public void setConnCord(ConnCordDTO connCord) {
		this.connCord = connCord;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#isImplicit()
	 */
	@Override
	public boolean isImplicit() {
		return isImplicit;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setImplicit(boolean)
	 */
	@Override
	public void setImplicit(boolean isImplicit) {
		this.isImplicit = isImplicit;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getComments()
	 */
	@Override
	public String getComments() {
		return comments;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setComments(java.lang.String)
	 */
	@Override
	public void setComments(String comments) {
		this.comments = comments;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getCreationDate()
	 */
	@Override
	public Timestamp getCreationDate() {
		return creationDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setCreationDate(java.sql.Timestamp)
	 */
	@Override
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getCreatedBy()
	 */
	@Override
	public String getCreatedBy() {
		return createdBy;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setCreatedBy(java.lang.String)
	 */
	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getUpdateDate()
	 */
	@Override
	public Timestamp getUpdateDate() {
		return updateDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setUpdateDate(java.sql.Timestamp)
	 */
	@Override
	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getConnTypeLksId()
	 */
	@Override
	public Long getConnTypeLksId() {
		return connTypeLksId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setConnTypeLksId(java.lang.Long)
	 */
	@Override
	public void setConnTypeLksId(Long connTypeLksId) {
		this.connTypeLksId = connTypeLksId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getConnTypeLksDesc()
	 */
	@Override
	public String getConnTypeLksDesc() {
		return connTypeLksDesc;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setConnTypeLksDesc(java.lang.String)
	 */
	@Override
	public void setConnTypeLksDesc(String connTypeLksDesc) {
		this.connTypeLksDesc = connTypeLksDesc;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getStatusLksId()
	 */
	@Override
	public Long getStatusLksId() {
		return statusLksId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setStatusLksId(java.lang.Long)
	 */
	@Override
	public void setStatusLksId(Long statusLksId) {
		this.statusLksId = statusLksId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getStatusLksDesc()
	 */
	@Override
	public String getStatusLksDesc() {
		return statusLksDesc;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setStatusLksDesc(java.lang.String)
	 */
	@Override
	public void setStatusLksDesc(String statusLksDesc) {
		this.statusLksDesc = statusLksDesc;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#getCircuitId()
	 */
	@Override
	public Long getCircuitId() {
		return circuitId;
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.views.circuits.ConnectionDTO#setCircuitId(java.lang.Long)
	 */
	@Override
	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
	}
	
}
