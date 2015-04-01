package com.raritan.tdz.data;

import java.util.List;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;

public interface ItemFactory {

	public abstract List<Long> getCreatedItemList();

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDevice(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDevice(String itemName, Long statusValueCode)
			throws Throwable;

	/**
	 * 
	 */
	public abstract ItItem createDevice(String itemName, Long statusValueCode,
			Item cabinet) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createUPS(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createUPS(String itemName, Long statusValueCode)
			throws Throwable;

	public abstract MeItem createUPS3PhaseWYE(String itemName,
			Long statusValueCode, MeItem upsBank) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDU(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createFloorPDU(String itemName, Long statusValueCode)
			throws Throwable;

	public abstract MeItem createFloorPDU3PhaseWYE(String itemName,
			Long statusValueCode, MeItem upsBank) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPowerOutlet(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createPowerOutlet(String outletName,
			Long statusValueCode) throws Throwable;

	public abstract MeItem createPowerPanel(Item parentItem, String panelName,
			Long numBranchCircuitBreakers, Long subClassLkpValueCode,
			Long statusValueCode) throws Throwable;

	public abstract MeItem createPowerPanel3PhaseWYE(Item parentItem,
			String panelName, Long subClassLkpValueCode, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithPanels(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createFloorPDUWithPanels(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithNoPanels(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createFloorPDUWithNoPanels(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createFloorPDUWithPanelsAndBranchCircuitBreakers(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createFloorPDUWithPanelsAndBranchCircuitBreakers(
			String itemName, Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createUPSBank(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createUPSBank(String itemName, Long statusValueCode)
			throws Throwable;

	public abstract MeItem createUPSBank3PhaseWYE(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceVM(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDeviceVM(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceFS(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDeviceFS(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceChassis(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDeviceChassis(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceChassis(java.lang.String, java.lang.Long, long)
	 */
	public abstract ItItem createDeviceChassis(String itemName,
			Long statusValueCode, long cabinetId) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkChassis(java.lang.String, java.lang.Long, long)
	 */
	public abstract ItItem createNetworkChassis(String itemName,
			Long statusValueCode, long cabinetId) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceBlade(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDeviceBlade(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDeviceBladeInChassis(java.lang.String, long, long, java.lang.Long)
	 */
	public abstract ItItem createDeviceBladeInChassis(String itemName,
			long cabinetItemId, long chassisItemId, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkChassis(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createNetworkChassis(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkBlade(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createNetworkBlade(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createProbe(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createProbe(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCRAC(java.lang.String, java.lang.Long)
	 */
	public abstract Item createCRAC(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createDataPanel(java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createDataPanel(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createNetworkStack(java.lang.String, java.lang.String, java.lang.Long)
	 */
	public abstract ItItem createNetworkStack(String itemName,
			String stackName, Long statusValueCode) throws Throwable;

	public abstract ItItem createNetworkStack(String itemName,
			String stackName, Long statusValueCode, Item cabinet)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCabinet(java.lang.String, java.lang.Long)
	 */
	public abstract CabinetItem createCabinet(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createCabinetWithItems(java.lang.String, java.lang.Long)
	 */
	public abstract CabinetItem createCabinetWithItems(String itemName,
			Long statusValueCode) throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPerfTiles(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createPerfTiles(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createRPDU(java.lang.String, java.lang.Long)
	 */
	public abstract MeItem createRPDU(String itemName, Long statusValueCode)
			throws Throwable;

	/* (non-Javadoc)
	 * @see com.raritan.tdz.data.ItemFactory#createPassive(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	public abstract ItItem createPassive(String itemName, Long cabinetItemId,
			Long statusValueCode) throws Throwable;

	public abstract Long save(Object object);
	
	public abstract CabinetItem createCabinetWithDataPanels(String itemName, Long statusValueCode) throws Throwable;

	public abstract void setDefaultCabinet(Item defaultCabinet);


}