package com.raritan.tdz.unit.item;

import java.util.Map;

import org.jmock.Mockery;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.UserInfo;

public interface ItemMock {

	public abstract Item createItem(String itemName, Integer index,
			String mounting, String formfactor, long iClassLksValueCode,
			long iSubClassLksValueCode, int itemType, Long statusValueCode);

	public abstract Map<String, Object> getValidatorTargetMap(Item item,
			UserInfo userInfo, String msg);

	public abstract ItemServiceDetails getNewItemServiceDetails(
			UserInfo.UserAccessLevel userAccessLevel);

	public abstract ModelDetails createNewTestModel(Long id, String mounting,
			String formFactor);

	public abstract Item createCabinetItem(Long statusValueCode, Integer index);

	public abstract Item createCabinetContainerItem(Long statusValueCode,
			Integer index);

	public abstract Item createRackableStandardDeviceItem(Long statusValueCode,
			Integer index);

	public abstract Item createNonRackableStandardDevice(Long statusValueCode,
			Integer index);

	public abstract Item createDeviceRackableChassis(Long statusValueCode,
			Integer index);

	public abstract Item createNetworkRackableChassis(Long statusValueCode,
			Integer index);

	public abstract Item createDeviceBlades(Long statusValueCode, Integer index);

	public abstract Item createNetworkBlades(Long statusValueCode, Integer index);

	public abstract Item createVM(Long statusValueCode, Integer index);

	public abstract Item createFreestandingRackableStandardDeviceItem(
			Long statusValueCode, Integer index);

	public abstract Item createFreestandingNetworkStackItem(
			Long statusValueCode, Integer index);

	public abstract Item createRackableFixedNetworkStackItem(
			Long statusValueCode, Integer index);

	public abstract Item createNonRackableFixedNetworkStackItem(
			Long statusValueCode, Integer index);

	public abstract Item createZeroUFixedNetworkStackItem(Long statusValueCode,
			Integer index);

	public abstract Item createRackableFixedDataPanelItem(Long statusValueCode,
			Integer index);

	public abstract Item createNonRackableFixedDataPanelItem(
			Long statusValueCode, Integer index);

	public abstract Item createZeroUDataPanelItem(Long statusValueCode,
			Integer index);

	public abstract Item createBuswayFixedItem(Long statusValueCode,
			Integer index);

	public abstract Item createWhipOutletItem(Long statusValueCode,
			Integer index);

	public abstract Item createZeroURackPDUItem(Long statusValueCode,
			Integer index);

	public abstract Item createRackableRackPDUItem(Long statusValueCode,
			Integer index);

	public abstract Item createNonRackableRackPDUItem(Long statusValueCode,
			Integer index);

	public abstract Item createRackableProbeItem(Long statusValueCode,
			Integer index);

	public abstract Item createZeroUProbeItem(Long statusValueCode,
			Integer index);

	public abstract Item createNonRackableProbeItem(Long statusValueCode,
			Integer index);

	public abstract Item createFreeStandingFixedFPDUItem(Long statusValueCode,
			Integer index);

	public abstract Item createLocalPanelBoardItem(Long statusValueCode,
			Integer index);

	public abstract Item createRemotePaneltem(Long statusValueCode,
			Integer index);

	public abstract Item createBuswayPanelItem(Long statusValueCode,
			Integer index);

	public abstract Item createUPSItem(Long statusValueCode, Integer index);

	public abstract Item createCRACItem(Long statusValueCode, Integer index);

	public abstract void addDataPortsToItem(Item item,
			Long portSubClassValueCode);

	public abstract void addExpectations(Item item, Mockery currentJmockContext);

	public Mockery getJmockContext();

	public void setJmockContext(Mockery jmockContext);

}