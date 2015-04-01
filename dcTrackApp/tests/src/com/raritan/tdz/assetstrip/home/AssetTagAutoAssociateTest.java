/**
 * 
 */
package com.raritan.tdz.assetstrip.home;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

import com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl.AssetTagStatus;
import com.raritan.tdz.assetstrip.util.AssetEventParam;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.events.domain.Event;
import com.raritan.tdz.events.domain.Event.EventType;
import com.raritan.tdz.events.domain.EventParam;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class AssetTagAutoAssociateTest extends TestBase {

	private AssetTagAutoAssociation assetTagAutoAssociation;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		assetTagAutoAssociation = (AssetTagAutoAssociation) ctx.getBean("aaAssetTagHome");
	}

	/**
	 * Test method for {@link com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl#addAssociation(com.raritan.tdz.events.domain.Event)}.
	 */
	@Test
	public final void testAddAssociationWithConflict() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_TAG_CONNECTED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(18).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(4).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A" + (r.nextInt(8)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.addAssociation(dctEvt);
		//assertEquals("Asset Tag is not valid: ", getEventParamStr("assetTagId", dctEvt.getEventParams()), associatedItem.getRaritanAssetTag());
		AssetTagStatus statusLks = AssetTagStatus.ASSET_CONFLICT_ASSET_TAG;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	/**
	 * Test method for {@link com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl#addAssociation(com.raritan.tdz.events.domain.Event)}.
	 */
	@Test
	public final void testAddAssociationWithAuthorized() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_TAG_CONNECTED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
	
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(18).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(4).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A4");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.addAssociation(dctEvt);
		AssertJUnit.assertEquals("Asset Tag is not valid: ", getEventParamStr(AssetEventParam.ASSET_TAG_ID.toString(), dctEvt.getEventParams()), associatedItem.getRaritanAssetTag());
		
		AssetTagStatus statusLks = AssetTagStatus.ASSET_AUTHORIZED;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	/**
	 * Test method for {@link com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl#addAssociation(com.raritan.tdz.events.domain.Event)}.
	 */
	@Test
	public final void testAddAssociationWithUNAUTHORIZED() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_TAG_CONNECTED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
	
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(19).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(32).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A4");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.addAssociation(dctEvt);
		
		AssetTagStatus statusLks = AssetTagStatus.ASSET_UNAUTHORIZED;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	/**
	 * Test method for {@link com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl#addAssociation(com.raritan.tdz.events.domain.Event)}.
	 */
	@Test
	public final void testAddAssociationWithUNKNOWN() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_TAG_CONNECTED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
	
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(22).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(4).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A4");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.addAssociation(dctEvt);

		AssetTagStatus statusLks = AssetTagStatus.ASSET_UNKNOWN;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	/**
	 * Test method for {@link com.raritan.tdz.assetstrip.home.AssetTagAutoAssociationImpl#addAssociation(com.raritan.tdz.events.domain.Event)}.
	 */
	@Test
	public final void testAddAssociationWithConflictUPos() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_TAG_CONNECTED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
		
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(18).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(5).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A" + (r.nextInt(8)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		
		Item associatedItem = assetTagAutoAssociation.addAssociation(dctEvt);
		//assertEquals("Asset Tag is not valid: ", getEventParamStr("assetTagId", dctEvt.getEventParams()), associatedItem.getRaritanAssetTag());
		AssetTagStatus statusLks = AssetTagStatus.ASSET_CONFLICT_ASSET_TAG;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	@Test
	public final void testRemoveAssociation() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_STRIP_REMOVED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
		
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(18).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(4).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A4");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.removeAssociation(dctEvt);
		
		AssetTagStatus statusLks = AssetTagStatus.ASSET_REMOVED;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	@Test
	public final void testRemoveAutoassociationUNKNOWN() {
		Random r = new Random();
		Timestamp createdAt = new Timestamp(Calendar.getInstance().getTimeInMillis());
		EventType eventType = EventType.ASSET_STRIP_REMOVED;
		Event dctEvt = Event.createEvent(session, createdAt, eventType, "Unit Test");
		
		
		dctEvt.addParam(AssetEventParam.PDU_NAME.toString(), "PDU #212");
		dctEvt.addParam(AssetEventParam.PDU_ID.toString(), new Integer(22).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_NUMBER.toString(), new Integer(4).toString());
		dctEvt.addParam(AssetEventParam.ASSET_TAG_ID.toString(), "000013DC11A4");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_NUMBER.toString(), "000013DC11A");
		dctEvt.addParam(AssetEventParam.ASSET_STRIP_ID.toString(), Integer.toString(r.nextInt(10000)+1) );
		dctEvt.addParam(AssetEventParam.ASSET_TAG_RACKUNIT_ID.toString(), Integer.toString(21) );
		
		Item associatedItem = assetTagAutoAssociation.removeAssociation(dctEvt);
		
		AssetTagStatus statusLks = AssetTagStatus.ASSET_REMOVED;
		
		AssertJUnit.assertEquals("Asset Tag Status is not valid: ", dctEvt.getProcessedStatus().getLkpValue() , SystemLookup.getLksData(session, statusLks.valueCode()).getLkpValue());
	}
	
	private String getEventParamStr(String key, Map<String,EventParam> map){
		String result = null;
		if (key != null){
			EventParam param = map.get(key);
			if (param != null) 
				result = param.getValue();
		}
			
		return result;
	}

}
