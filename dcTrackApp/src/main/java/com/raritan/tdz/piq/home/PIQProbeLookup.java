package com.raritan.tdz.piq.home;

import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;

/**
 * A service for looking up a hidden rack PDU associated with a probe and vice versa.
 * @author Andrew Cohen
 */
@Transactional(readOnly = true)
public interface PIQProbeLookup {
	
	/**
	 * Lookup the dummy rack PDU associated with the given probe item.
	 * @param probeItemId the probe item id
	 * @return the rack PDU item, or null if there is no associated rack PDU
	 */
	public Item getDummyRackPDUForProbeItem(long probeItemId);
	
	/**
	 * Lookup the prove item associated with the given dummy rack PDU item
	 * @param pduId the rack PDU item id
	 * @return the probe item, or null if no probe item is associated
	 */
	public Item getProbeItemForDummyRackPDU(long pduId);
	
	/**
	 * Gets the PIQ ID of the probe that was just unmapped from the PIQ Import Wizard.
	 * @param probeItemId
	 * @return
	 */
	public Long getUnmappedProbePIQId(long probeItemId);
}
