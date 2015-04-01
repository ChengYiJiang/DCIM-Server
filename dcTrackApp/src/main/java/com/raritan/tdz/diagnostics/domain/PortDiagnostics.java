package com.raritan.tdz.diagnostics.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="`dct_errors_port`")
public class PortDiagnostics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7531047252028807548L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dct_errors_port_error_port_seq")
	@SequenceGenerator(name = "dct_errors_port_error_port_seq", sequenceName = "dct_errors_port_error_port_id_seq", allocationSize=1)
	@Column(name = "error_port_id", unique = true, nullable = false)
	private long id;
	
	@Column(name = "location_code")
	private String locationCode;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "port_name")
	private String portName;
	
	@Column(name = "port_subclass")
	private String portSubClass;
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "port_data_id")
	private Long portDataId;
	
	@Column(name = "connection_data_id")
	private Long connectionDataId;
	
	@Column(name = "port_power_id")
	private Long portPowerId;
	
	@Column(name = "connection_power_id")
	private Long connectionPowerId;
	
	@Column(name = "diagnosis_msg")
	private String diagnosisMsg;
	
	@Column(name = "propose_action")
	private String proposeAction;
	
	@Column(name = "connection_info")
	private String connectionInfo;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortSubClass() {
		return portSubClass;
	}

	public void setPortSubClass(String portSubClass) {
		this.portSubClass = portSubClass;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getPortDataId() {
		return portDataId;
	}

	public void setPortDataId(Long portDataId) {
		this.portDataId = portDataId;
	}

	public Long getConnectionDataId() {
		return connectionDataId;
	}

	public void setConnectionDataId(Long connectionDataId) {
		this.connectionDataId = connectionDataId;
	}

	public Long getPortPowerId() {
		return portPowerId;
	}

	public void setPortPowerId(Long portPowerId) {
		this.portPowerId = portPowerId;
	}

	public Long getConnectionPowerId() {
		return connectionPowerId;
	}

	public void setConnectionPowerId(Long connectionPowerId) {
		this.connectionPowerId = connectionPowerId;
	}

	public String getDiagnosisMsg() {
		return diagnosisMsg;
	}

	public void setDiagnosisMsg(String diagnosisMsg) {
		this.diagnosisMsg = diagnosisMsg;
	}

	public String getPurposeAction() {
		return proposeAction;
	}

	public void setPurposeAction(String purposeAction) {
		this.proposeAction = purposeAction;
	}

	public String getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(String connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((connectionDataId == null) ? 0 : connectionDataId.hashCode());
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
		PortDiagnostics other = (PortDiagnostics) obj;
		if (connectionDataId == null) {
			if (other.connectionDataId != null)
				return false;
		} else if (!connectionDataId.equals(other.connectionDataId))
			return false;
		return true;
	}

	public PortDiagnostics(long id, String locationCode, String itemName,
			String portName, String portSubClass, Long itemId, Long portDataId,
			Long connectionDataId, Long portPowerId, Long connectionPowerId,
			String diagnosisMsg, String purposeAction, String connectionInfo) {
		super();
		this.id = id;
		this.locationCode = locationCode;
		this.itemName = itemName;
		this.portName = portName;
		this.portSubClass = portSubClass;
		this.itemId = itemId;
		this.portDataId = portDataId;
		this.connectionDataId = connectionDataId;
		this.portPowerId = portPowerId;
		this.connectionPowerId = connectionPowerId;
		this.diagnosisMsg = diagnosisMsg;
		this.proposeAction = purposeAction;
		this.connectionInfo = connectionInfo;
	}

	public PortDiagnostics() {
	}
	
}

