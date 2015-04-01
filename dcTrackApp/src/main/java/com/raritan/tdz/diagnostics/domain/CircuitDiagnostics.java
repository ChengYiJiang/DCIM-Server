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
@Table(name="`dct_errors_circuit`")
public class CircuitDiagnostics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2523163960300892985L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dct_errors_circuit_error_circuit_seq")
	@SequenceGenerator(name = "dct_errors_circuit_error_circuit_seq", sequenceName = "dct_errors_circuit_error_circuit_id_seq", allocationSize=1)
	@Column(name = "error_circuit_id", unique = true, nullable = false)
	private long id;
	
	@Column(name = "location_code")
	private String locationCode;

	@Column(name = "first_node_item_name")
	private String firstNodeItemName;

	@Column(name = "first_node_port_name")
	private String firstNodePortName;

	@Column(name = "last_node_item_name")
	private String lastNodeItemName;

	@Column(name = "last_node_port_name")
	private String lastNodePortName;

	@Column(name = "circuit_data_id")
	private Long circuitDataId;
	
	@Column(name = "connection_data_id")
	private Long connectionDataId;

	@Column(name = "circuit_power_id")
	private Long circuitPowerId;
	
	@Column(name = "connection_power_id")
	private Long connectionPowerId;

	@Column(name = "diagnosis_msg")
	private String diagnosisMsg;

	@Column(name = "propose_action")
	private String proposeAction;
	
	@Column(name = "circuit_info")
	private String circuitInfo;

	
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

	public String getFirstNodeItemName() {
		return firstNodeItemName;
	}

	public void setFirstNodeItemName(String firstNodeItemName) {
		this.firstNodeItemName = firstNodeItemName;
	}

	public String getFirstNodePortName() {
		return firstNodePortName;
	}

	public void setFirstNodePortName(String firstNodePortName) {
		this.firstNodePortName = firstNodePortName;
	}

	public String getLastNodeItemName() {
		return lastNodeItemName;
	}

	public void setLastNodeItemName(String lastNodeItemName) {
		this.lastNodeItemName = lastNodeItemName;
	}

	public String getLastNodePortName() {
		return lastNodePortName;
	}

	public void setLastNodePortName(String lastNodePortName) {
		this.lastNodePortName = lastNodePortName;
	}

	public Long getCircuitDataId() {
		return circuitDataId;
	}

	public void setCircuitDataId(Long circuitDataId) {
		this.circuitDataId = circuitDataId;
	}

	public Long getConnectionDataId() {
		return connectionDataId;
	}

	public void setConnectionDataId(Long connectionDataId) {
		this.connectionDataId = connectionDataId;
	}

	public Long getCircuitPowerId() {
		return circuitPowerId;
	}

	public void setCircuitPowerId(Long circuitPowerId) {
		this.circuitPowerId = circuitPowerId;
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

	public String getProposeAction() {
		return proposeAction;
	}

	public void setProposeAction(String proposeAction) {
		this.proposeAction = proposeAction;
	}

	public String getCircuitInfo() {
		return circuitInfo;
	}

	public void setCircuitInfo(String circuitInfo) {
		this.circuitInfo = circuitInfo;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		CircuitDiagnostics other = (CircuitDiagnostics) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public CircuitDiagnostics(long id, String locationCode,
			String firstNodeItemName, String firstNodePortName,
			String lastNodeItemName, String lastNodePortName,
			Long circuitDataId, Long connectionDataId, Long circuitPowerId,
			Long connectionPowerId, String diagnosisMsg, String proposeAction,
			String circuitInfo) {
		super();
		this.id = id;
		this.locationCode = locationCode;
		this.firstNodeItemName = firstNodeItemName;
		this.firstNodePortName = firstNodePortName;
		this.lastNodeItemName = lastNodeItemName;
		this.lastNodePortName = lastNodePortName;
		this.circuitDataId = circuitDataId;
		this.connectionDataId = connectionDataId;
		this.circuitPowerId = circuitPowerId;
		this.connectionPowerId = connectionPowerId;
		this.diagnosisMsg = diagnosisMsg;
		this.proposeAction = proposeAction;
		this.circuitInfo = circuitInfo;
	}

	public CircuitDiagnostics() {

	}
	

	

	
}
