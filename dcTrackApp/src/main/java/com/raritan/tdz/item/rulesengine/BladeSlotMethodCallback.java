package com.raritan.tdz.item.rulesengine;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

public class BladeSlotMethodCallback implements RemoteRefMethodCallback {

	private SessionFactory sessionFactory;
	private ChassisHome chassisHome;
	
	public BladeSlotMethodCallback() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BladeSlotMethodCallback(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	public ChassisHome getChassisHome() {
		return chassisHome;
	}

	public void setChassisHome(ChassisHome chassisHome) {
		this.chassisHome = chassisHome;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {
		Session session = sessionFactory.getCurrentSession();
		Item item = (Item) session.get(Item.class, (Long) filterValue);

		String slotLabel = "";
		if (item instanceof ItItem) {
			slotLabel = chassisHome.getBladeSlotLabel((ItItem)item);
		}
		
		uiViewComponent.getUiValueIdField().setValue(slotLabel);
		uiViewComponent.getUiValueIdField().setValueId(item.getSlotPosition());
	
	}


}
