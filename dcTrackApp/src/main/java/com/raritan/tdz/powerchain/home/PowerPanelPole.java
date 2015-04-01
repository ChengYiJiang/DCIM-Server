package com.raritan.tdz.powerchain.home;

import com.raritan.tdz.domain.Item;

public interface PowerPanelPole {

	/**
	 * create the poles for the power panel
	 * @param item
	 * @param poleNumbering
	 * @param startPoleNumber
	 * @param poleLayout
	 */
	public void create(Item item, Long poleNumbering, Long startPoleNumber);

	/**
	 * deletes all poles in the power panel
	 * @param item
	 */
	void delete(Item item);
	
}
