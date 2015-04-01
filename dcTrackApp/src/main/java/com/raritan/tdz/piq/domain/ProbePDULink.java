package com.raritan.tdz.piq.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.raritan.tdz.domain.Item;

/**
 * 
 * @author Andrew Cohen
 */
@Entity
@Table(name="`dct_probe_rpdus`")
public class ProbePDULink implements Serializable {
	
	public ProbePDULink() {
		probe = null;
		dummyRPDU = null;
	}
	
	public ProbePDULink(Item probe, Item dummyRPDU) {
		setProbe( probe );
		setDummyRPDU( dummyRPDU );
	}
	
	public Item getProbe() {
		return probe;
	}

	public void setProbe(Item probe) {
		this.probe = probe;
	}

	public Item getDummyRPDU() {
		return dummyRPDU;
	}

	public void setDummyRPDU(Item dummyRPDU) {
		this.dummyRPDU = dummyRPDU;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getProbeId() {
		return probeId;
	}

	public void setProbeId(long probeId) {
		this.probeId = probeId;
	}

	public long getDummyRpduId() {
		return dummyRpduId;
	}

	public void setDummyRpduId(long dummyRpduId) {
		this.dummyRpduId = dummyRpduId;
	}

	public Long getUnmappedProbePiqId() {
		return unmappedProbePiqId;
	}

	public void setUnmappedProbePiqId(Long unmappedProbePiqId) {
		this.unmappedProbePiqId = unmappedProbePiqId;
	}

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="dct_probe_rpdus_seq")
	@SequenceGenerator(name="dct_probe_rpdus_seq", sequenceName="dct_probe_rpdus_probe_rpdu_id_seq", allocationSize=1)
	@Column(name="probe_rpdu_id")
	private long id;
	
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "probe_id", nullable = true)
	private Item probe;
	
	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "dummy_rpdu_id", nullable = true)
	private Item dummyRPDU;
	
	@Column(name = "probe_id", updatable = false, insertable = false)
	private long probeId;
	
	@Column(name = "dummy_rpdu_id", updatable = false, insertable = false)
	private long dummyRpduId;
	
	// This field is set by Windows Client, so it is read only here
	@Column(name = "unmapped_probe_piq_id", updatable = false, insertable = false)
	private Long unmappedProbePiqId;
}
