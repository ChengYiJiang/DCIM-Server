package com.raritan.tdz.page.home;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.item.home.ItemClone;
import com.raritan.tdz.item.home.ItemHome;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.user.dao.UserDAO;

public class ItemActionMenuStatus {	

    public static String ID_NEW = "new";
    public static String ID_VIEW = "view";
    public static String ID_EDIT = "edit";
    public static String ID_DELETE = "delete";
    public static String ID_CLONE = "clone";
    public static String ID_MENU = "menu";
    public static String ID_FLOORMAPS = "floormaps";
    public static String ID_REQ_VM = "req_vm";
    public static String ID_REQ_OFF_SITE = "req_off_site";
    public static String ID_REQ_ON_SITE = "req_on_site";
    public static String ID_REQ_POWER_OFF = "req_power_off";
    public static String ID_REQ_POWER_ON = "req_power_on";
    public static String ID_REQ_TO_STORAGE = "req_to_storage";
    public static String ID_REQ_TO_ARCHIVE = "req_to_archive";
    public static String ID_REQ_INSTALL = "req_install";
    public static String ID_REQ_QUICK_ITEM_MOVE = "req_item_move_quick";
    public static String ID_REQ_ITEM_MOVE = "req_item_move";
    public static String ID_REQ_RESUBMIT = "req_resubmit";
    public static String ID_REQ_BYPASS_MENU = "req_bypass_menu";
    public static String ID_REQ_BYPASS_CHECKED = "req_bypass_checked";
    public static String ID_EXCEPTION = "exception";
    
    public static String STATUS_PlANNED = "planned";
    public static String STATUS_INSTALLED = "installed";
    public static String STATUS_POWERED_OFF = "powered-off";
    public static String STATUS_STORAGE = "storage";
    public static String STATUS_OFF_SITE = "off-site";
    public static String STATUS_ARCHIVED = "archived";
    
    public static String CLASS_CABINET = "cabinet";
    public static String CLASS_DEVICE = "device";
    public static String CLASS_NETWORK = "network";
    public static String CLASS_PROBE = "probe";
    public static String CLASS_DATA_PANEL = "data panel";
    public static String CLASS_RACK_PDU = "rack pdu";
    public static String CLASS_POWER_OUTLET = "power outlet";
    public static String CLASS_FLOOR_PDU = "floor pdu";
    public static String CLASS_UPS = "ups";
    public static String CLASS_CRAC = "crac";
	
    public static String SUBCLASS_STANDARD = "standard";
    public static String SUBCLASS_BLADE_SERVER = "blade server";
    public static String SUBCLASS_BLADE_CHASSIS = "blade chassis";
    
    public static String SUBCLASS_CHASSIS = "chassis";
    public static String SUBCLASS_BLADE = "blade";
    public static String SUBCLASS_VIRTUAL_MACHINE = "virtual machine";
    public static String SUBCLASS_NETWORKSTACK = "networkstack";
    public static String MOUNTING_RACKABLE = "rackable";
    public static String MOUNTING_BLADE = "blade";
    public static String MOUNTING_FREE_STANDING = "free-standing";
    
    public static String SUBCLASS_NONE = "";
    
    public static HashSet permittedStatesMapping = getPermittedStatesMapping();

	private static Logger log = Logger.getLogger("ItemActionMenuStatus");

    private SessionFactory sessionFactory;
	private ItemHome itemHome;	
	private ItemClone itemClone;
	
	private PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	private UserDAO userDao;
	
	public ItemActionMenuStatus( SessionFactory sessionFactory, ItemHome itemHome, ItemClone itemClone, PortMoveDAO<PowerPortMove> powerPortMoveDAO, UserDAO userDao){
		this.sessionFactory = sessionFactory;
		this.itemHome = itemHome;
		this.itemClone = itemClone;
		this.powerPortMoveDAO = powerPortMoveDAO;
		this.userDao = userDao;
	}
		
	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		
		String errMsg = "";
		
		UserInfo userInfo = FlexUserSessionContext.getUser();
		if ( userInfo!=null ) {
			String userId = userInfo.getUserId();
			boolean isAdmin = userInfo.isAdmin();
			boolean isManager = userInfo.isManager();
			boolean isMember = userInfo.isMember();
			boolean isViewer = userInfo.isViewer();
			
			log.debug( "[ getItemActionMenuStatus ] userId : " + userId + ", isAdmin : " + isAdmin + ", isManager : " + isManager + ", isMember : " + isMember + ", isViewer : " + isViewer );
		} else {
			log.debug( "[ getItemActionMenuStatus ] userInfo is null " );
			errMsg = "UserInfo is null.";
		}
		
		if ( itemIdList==null ) itemIdList = new ArrayList();
		
		List itemList = null;
		
		if ( itemIdList.size()>0 ) {	

			try {
				String itemsQueryStr = 
						"SELECT item.item_id, item.parent_item_id, lks_status.lkp_value AS status, lks_class.lkp_value AS class, lks_subclass.lkp_value AS subclass\n" +
						", lks_class.lkp_value_code AS classValueCode, lks_subclass.lkp_value_code AS subclassValueCode, model.mounting\n" +
						"FROM dct_items AS item LEFT JOIN dct_models AS model ON item.model_id = model.model_id\n" +  
						"INNER JOIN dct_lks_data AS lks_status ON item.status_lks_id=lks_status.lks_id\n" + 
						"INNER JOIN dct_lks_data AS lks_class ON item.class_lks_id=lks_class.lks_id\n" + 
						"LEFT JOIN dct_lks_data AS lks_subclass ON item.subclass_lks_id=lks_subclass.lks_id\n";
					
				Session session = sessionFactory.getCurrentSession();	
				Query query = session.createSQLQuery( itemsQueryStr );
			
				itemList = query.list();
			} catch ( Exception ex ) {
				errMsg += ex.getMessage();
			}
		}
		
		if ( itemList==null ) itemList = new ArrayList();
		
		HashMap selectedItemHashMap = new HashMap();
		HashMap childrenHashMap = new HashMap();
		HashMap parentHashMap = new HashMap();
		String rtnString;
		
		try {
			
			if ( itemIdList.size()>0 ) {
				boolean loadItem = true;
				if ( itemIdList.size()==1 ) {
					for ( Object itemId : itemIdList ) {
						loadItem = itemId!=null && new Integer( itemId.toString() ).intValue()<=0 ? false : true;
					}
				} 
				
				if ( loadItem ) {
					loadItem( itemIdList, itemList, selectedItemHashMap );
					log.debug( "[ getItemActionMenuStatus ] selectedItemHashMap.keySet() : " + selectedItemHashMap.keySet() );
					log.debug( "[ getItemActionMenuStatus ] selectedItemHashMap : " + selectedItemHashMap );
					
					if ( selectedItemHashMap.size()!=itemIdList.size() ) throw new Exception( "Some items were not found." );			
				}
			}
			
			loadChildren( itemIdList, itemList, childrenHashMap );
			log.debug( "[ getItemActionMenuStatus ] childrenHashMap.keySet() : " + childrenHashMap.keySet() );
			log.debug( "[ getItemActionMenuStatus ] childrenHashMap : " + childrenHashMap );
					
			loadParent( selectedItemHashMap.values(), itemList, parentHashMap );
			log.debug( "[ getItemActionMenuStatus ] parentHashMap.keySet() : " + parentHashMap.keySet() );
			log.debug( "[ getItemActionMenuStatus ] parentHashMap : " + parentHashMap );
		
			rtnString = "{\"" + ID_NEW            + "\":" + enableButtonNewItem( userInfo ) + 
					    ",\"" + ID_EDIT           + "\":" + enableButtonEdit( userInfo, itemIdList ) + 
					    ",\"" + ID_VIEW           + "\":" + enableButtonView( userInfo, itemIdList ) + 
					    ",\"" + ID_MENU           + "\":" + enableButtonActions( userInfo, itemIdList ) + 
					    ",\"" + ID_DELETE         + "\":" + enableButtonDelete( selectedItemHashMap, childrenHashMap, userInfo ) +
					    ",\"" + ID_FLOORMAPS      + "\":" + enableButtonFloorMaps( selectedItemHashMap, parentHashMap ) +
					    ",\"" + ID_CLONE          + "\":" + (enableButtonClone( userInfo, selectedItemHashMap ) && isNotMoveRequest(itemIdList)) +
					    ",\"" + ID_REQ_INSTALL    + "\":" + (checkReqInstall( selectedItemHashMap, itemList ) && isNotMoveRequest(itemIdList)) + 
					    ",\"" + ID_REQ_VM         + "\":" + checkReqVM( selectedItemHashMap ) + 
					    ",\"" + ID_REQ_OFF_SITE   + "\":" + checkReqOffSite( selectedItemHashMap ) +
					    ",\"" + ID_REQ_TO_STORAGE + "\":" + checkReqToStorage( selectedItemHashMap, childrenHashMap ) +
					    ",\"" + ID_REQ_POWER_OFF  + "\":" + checkReqPowerOff( selectedItemHashMap ) +
					    ",\"" + ID_REQ_POWER_ON   + "\":" + checkReqPowerOn( selectedItemHashMap, parentHashMap ) +
					    ",\"" + ID_REQ_ON_SITE    + "\":" + checkReqOnSite( selectedItemHashMap, parentHashMap ) +
					    ",\"" + ID_REQ_TO_ARCHIVE + "\":" + checkReqToArchive( selectedItemHashMap, itemList ) +
					    ",\"" + ID_REQ_ITEM_MOVE + "\":" + checkReqItemMove( selectedItemHashMap, itemList ) +
					    ",\"" + ID_REQ_QUICK_ITEM_MOVE + "\":" + checkReqQuickItemMove(selectedItemHashMap, itemList) +
					    ",\"" + ID_REQ_RESUBMIT   + "\":" + checkReqResubmit( itemIdList, sessionFactory ) +
					    ",\"" + ID_REQ_BYPASS_MENU     + "\":" + isByPassMenuEnabled (userInfo) +
					    ",\"" + ID_REQ_BYPASS_CHECKED  + "\":" + getRequestByPassFromDB (userInfo) +
					    ",\"" + ID_EXCEPTION      + "\":\"" + errMsg + "\"" +
			            "}";
		} catch ( Exception ex ) {
			errMsg += ex.getMessage();
			
			System.out.println(errMsg);
			
			rtnString = "{\"" + ID_NEW            + "\":" + enableButtonNewItem( userInfo ) + 
				        ",\"" + ID_EDIT           + "\":" + enableButtonEdit( userInfo, itemIdList ) + 
				        ",\"" + ID_VIEW           + "\":" + enableButtonView( userInfo, itemIdList ) + 
					    ",\"" + ID_MENU           + "\":" + enableButtonActions( userInfo, itemIdList ) +
					    ",\"" + ID_CLONE          + "\":" + enableButtonClone( userInfo, selectedItemHashMap ) +
					    ",\"" + ID_FLOORMAPS      + "\":" + enableButtonFloorMaps( selectedItemHashMap, parentHashMap ) +
					    ",\"" + ID_DELETE         + "\":" + false + 
					    ",\"" + ID_REQ_INSTALL    + "\":" + false + 
					    ",\"" + ID_REQ_VM         + "\":" + false + 
					    ",\"" + ID_REQ_OFF_SITE   + "\":" + false +
					    ",\"" + ID_REQ_TO_STORAGE + "\":" + false +
					    ",\"" + ID_REQ_POWER_OFF  + "\":" + false +
					    ",\"" + ID_REQ_POWER_ON   + "\":" + false +
					    ",\"" + ID_REQ_ON_SITE    + "\":" + false +
					    ",\"" + ID_REQ_TO_ARCHIVE + "\":" + false +
					    ",\"" + ID_REQ_ITEM_MOVE  + "\":" + false +
					    ",\"" + ID_REQ_QUICK_ITEM_MOVE  + "\":" + false +
					    ",\"" + ID_REQ_RESUBMIT   + "\":" + false +
					    ",\"" + ID_REQ_BYPASS_MENU     + "\":" + false +
					    ",\"" + ID_REQ_BYPASS_CHECKED  + "\":" + false +
					    ",\"" + ID_EXCEPTION      + "\":\"" + errMsg + "\"" +
			            "}";
		}
		
		return rtnString;
	}
	
	private boolean getRequestByPassFromDB (UserInfo userInfo) {
		return userDao.getUserRequestByPassSetting(new Long(userInfo.getUserId()));
	}

	private boolean isByPassMenuEnabled (UserInfo userInfo) {
		boolean bpMenuEnabled = false;
		Long userId = new Long(userInfo.getUserId());
		
		// is user gate keeper
		BigInteger accessLevelLkpValueCode = userDao.getUserAccessLevelLkpValueCode(userId);
		boolean gk = (accessLevelLkpValueCode != null && 
						accessLevelLkpValueCode.longValue() == SystemLookup.AccessLevel.GATEKEEPER);

		// is lock Request bypass for gate keeper set
		String lock = userDao.getLockRequestByPassButtonSetting();
		boolean rbLocked = ((lock == null) || (lock != null && lock.equals(UserDAO.GKCanToggleRequestBypass)) ? false : true);
		
		// user is gate keeper and request bypass is not locked for gate keeper.
		if (gk && !rbLocked) {
			bpMenuEnabled = true;
		}
		
		return bpMenuEnabled;
	}
	
	//=====================================================================
	
	private boolean enableButtonNewItem( UserInfo userInfo ) {
		
		if ( userInfo==null ) return false;
		if ( userInfo.isViewer() ) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean enableButtonEdit( UserInfo userInfo, List<Long> itemIdList ) {
		
		if ( userInfo==null || itemIdList==null || itemIdList.size()==0 || itemIdList.size()>1  ) return false;
		if ( userInfo.isViewer() ) {
			return false;
		} else {
			for ( Object itemIdObj : itemIdList ) {
				Boolean itemEditable = true;
				try {
					itemEditable = itemHome.getEditableStatusForAnItem( new Long(itemIdObj.toString()).longValue(), userInfo );
				} catch (Throwable e) {
					log.info( "", e );
					//Hmm.. supressing exception..  should we disable the button ??
					return false;
				}
				if ( !itemEditable.booleanValue() ) {
					return false;
				}
			}
			return true;
		}
	}
	
	private boolean enableButtonView( UserInfo userInfo, List<Long> itemIdList ) {
		if ( userInfo==null || itemIdList==null || itemIdList.size()==0 || itemIdList.size()>1  ) return false;		
		return true;
	}
	
	private boolean enableButtonActions( UserInfo userInfo, List itemIdList ) {
		return (userInfo!=null && itemIdList!=null && itemIdList.size()>0 ? true : false);
	}
	
	private boolean enableButtonDelete( HashMap selectedItemHashMap, HashMap childrenHashMap, UserInfo userInfo ) {
		
		if ( userInfo==null ) return false;
		if ( userInfo.isViewer() ) {
			return false;
		}
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;
		
		HashMap checkedHasHashMap = new HashMap();
		checkedHasHashMap.putAll( selectedItemHashMap );
		//checkedHasHashMap.putAll( childrenHashMap );  //Code get the children to be process
				
		for ( Object itemObj : checkedHasHashMap.values() ) {
			Object itemIdObj = ((Object[])itemObj)[0];
			Object statusObj = ((Object[])itemObj)[2];
			
			log.debug( "[ getItemActionMenuStatus ] enableButtonDelete() > item_id : " + itemIdObj + ", status : " + statusObj );
			Long itemId = new Long(itemIdObj.toString());
			try {
				if (!itemHome.getDeletableStatusForAnItem( itemId.longValue(), userInfo )) return false;
			} catch (Throwable e) {
				log.info( "", e );
				//Hmm.. supressing exception..  should we disable the button ??
				return false;
			}
		}
		return true;
	}

	private boolean enableButtonFloorMaps( HashMap selectedItemHashMap, HashMap parentHashMap ) {
		log.debug("selectedItemHashMap.size() = " + selectedItemHashMap.size());
		
		if (selectedItemHashMap.size() != 1)
			return false;
		
		for( Object obj : selectedItemHashMap.values()) {
			// There should be only one in the list
			Object[] itemObjArray = (Object[])obj;
			long classLkpValueCode = ((BigInteger)itemObjArray[5]).longValue();
			log.debug("lkpValueCode = " + classLkpValueCode);
			
			// freestanding class
			if (classLkpValueCode == SystemLookup.Class.FLOOR_PDU || classLkpValueCode == SystemLookup.Class.CABINET ||
					classLkpValueCode == SystemLookup.Class.CRAC || classLkpValueCode == SystemLookup.Class.UPS) {
				return true;
			}
			
			if (itemObjArray[1] != null) {
				// Has parent
				String parentId = ((BigInteger)itemObjArray[1]).toString();
				log.debug("parent itemid = " + parentId);
				
				Object parentItem = parentHashMap.get(parentId);
				Object[] parentObjArray = (Object[])parentItem;
				
				long parentLkpValueCode = ((BigInteger)parentObjArray[5]).longValue();
				log.debug("parent lkpValueCode = " + parentLkpValueCode);
				if (parentLkpValueCode == SystemLookup.Class.CABINET)
					return true;
			} else {
				log.error("no parent");
			}
		}
		
		return false;
	}
	
	//=====================================================================
	
    // itemHashMap<Object, Object[]>
	static private void loadItem( List itemIdList, List itemList, HashMap itemHashMap ) {

		HashSet itemIdHashSet = new HashSet();
		for ( Object obj : itemIdList ) {
			if ( obj!=null ) {
				String itemId = obj.toString().trim();
				if ( !"".equals( itemId ) ) {
					itemIdHashSet.add( itemId.trim() );
				}
			}
		}
		
		for ( Object item : itemList ) {
			Object[] itemArray = (Object[])item;
			String itemId = itemArray[0]==null ? "" : itemArray[0].toString();
			if ( itemIdHashSet.contains( itemId ) ) {
				itemHashMap.put( itemId, item );
			}
		}
	}

	static private void loadChildren( List itemIdList, List itemList, HashMap childrenHashMap ) throws Exception {
		
		for ( Object obj : itemIdList ) {
			if ( obj!=null ) {
				String itemId = obj.toString().trim();
				if ( !"".equals( itemId ) ) {
					loadChildren( itemId, itemList, childrenHashMap );
				}
			}
		}
	}
	
	// childrenHashMap<Object, Object[]>
	static private void loadChildren( String itemId, List itemList, HashMap childrenHashMap ) throws Exception {
		
		if ( itemId==null ) return;
		HashMap temp = new HashMap();
		
		for ( Object obj : itemList ) {
			Object[] item = (Object[])obj;
			
			String parentId = item[1]==null ? null : item[1].toString().trim();
			if ( parentId!=null && parentId.equals( itemId ) ) {
				log.debug( "item_id : " + item[0] + ", parent_id : " + item[1] );
				temp.put( item[0], item );
			}
		}
		
		if ( temp.size()>0 ) {
			childrenHashMap.putAll( temp );
			for ( Object _itemId : temp.keySet() ) {
				try {
					loadChildren( _itemId.toString(), itemList, childrenHashMap );
				} catch ( StackOverflowError error ) {
					throw new Exception( "Found StackOverflowError in loadChildren: itemId(" + _itemId + ") is in child list of self.");
				}
			}
		}
	}

	static private void loadParent( Collection selectedItemList, List itemList, HashMap parentHashMap ) throws Exception {
		
		for ( Object selectedItem : selectedItemList ) {
			loadParent( selectedItem, itemList, parentHashMap );
		}
	}
	
	static private void loadParent( Object selectedItem, List itemList, HashMap parentHashMap ) throws Exception {
		
		Object parentIdObj = ((Object[])selectedItem)[1];
		if ( parentIdObj==null ) return;
		String parentId = parentIdObj.toString().trim();
		
		if ( parentId.equals( "" ) ) return;
		
		for ( Object obj : itemList ) {
			
			Object[] item = (Object[])obj;
			
			String itemId = item[0]==null ? null : item[0].toString();
			if ( parentId!=null && parentId.equals( itemId ) ) {
				log.debug( "item_id : " + item[0] + ", parent_id : " + item[1] );
				parentHashMap.put( itemId, item );

				try {
					loadParent( item, itemList, parentHashMap );
				} catch ( StackOverflowError error ) {
					throw new Exception( "Found StackOverflowError in loadParent: itemId(" + itemId + ") is in parent list of self." );
				}
			}
		}
	}
    
	//=====================================================================
	
	// Only put Power-off, Storage and Off-Site related
	static private HashSet getPermittedStatesMapping() {

		// entry format : status:class:subclass
		HashSet permittedStatesMapping = new HashSet();
		
		// Power-off related
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_DEVICE  + ":" + SUBCLASS_STANDARD ); 
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_DEVICE  + ":" + SUBCLASS_BLADE_CHASSIS ); 
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_DEVICE  + ":" + SUBCLASS_BLADE_SERVER ); 
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_DEVICE  + ":" + SUBCLASS_VIRTUAL_MACHINE ); 		
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_NETWORK + ":" + SUBCLASS_CHASSIS ); 
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_NETWORK + ":" + SUBCLASS_NETWORKSTACK ); 
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_NETWORK + ":" + SUBCLASS_BLADE );
		permittedStatesMapping.add( STATUS_POWERED_OFF + ":" + CLASS_PROBE ); 
		
		// Storage related
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_CABINET ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_DEVICE  + ":" + SUBCLASS_STANDARD ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_DEVICE  + ":" + SUBCLASS_BLADE_CHASSIS ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_DEVICE  + ":" + SUBCLASS_BLADE_SERVER ); 		
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_NETWORK + ":" + SUBCLASS_CHASSIS ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_NETWORK + ":" + SUBCLASS_BLADE ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_NETWORK + ":" + SUBCLASS_NETWORKSTACK ); 
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_PROBE );  
		permittedStatesMapping.add( STATUS_STORAGE + ":" + CLASS_RACK_PDU ); 

		// Off-Site related
		permittedStatesMapping.add( STATUS_OFF_SITE + ":" + CLASS_DEVICE + ":" + SUBCLASS_STANDARD ); 
		permittedStatesMapping.add( STATUS_OFF_SITE + ":" + CLASS_DEVICE + ":" + SUBCLASS_BLADE_CHASSIS ); 
		permittedStatesMapping.add( STATUS_OFF_SITE + ":" + CLASS_DEVICE + ":" + SUBCLASS_BLADE_SERVER ); 
		permittedStatesMapping.add( STATUS_OFF_SITE + ":" + CLASS_NETWORK + ":" + SUBCLASS_BLADE );
		
		return permittedStatesMapping;
	}
	
	//==============================================================================================
	
	//ID_REQ_INSTALL
	static private boolean checkReqInstall( HashMap selectedItemHashMap, List itemList ) throws Exception {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;
		
		
		
		//STATUS_PlANNED
		for ( Object obj : selectedItemHashMap.values() ) {
			String status = toLowerCase( ((Object[])obj)[2] );
			
			if ( !STATUS_PlANNED.equals( status ) && !STATUS_STORAGE.equals( status ) ) {
				return false;
			} /*else if ( STATUS_PlANNED.equals( status ) ) {
				HashMap parentHashMap = new HashMap();
				loadParent( obj, itemList, parentHashMap );
				//All Parent's status must be STATUS_INSTALLED
				for ( Object parentObj : parentHashMap.values() ) {
					String parentStatus = toLowerCase( ((Object[])parentObj)[2] );
					if ( !STATUS_INSTALLED.equals( parentStatus ) ) return false;
				}
			}*/
		}
		
		return true;
	}
	
	//ID_REQ_VM
	static private boolean checkReqVM( HashMap selectedItemHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;
		
		//STATUS_INSTALLED || STATUS_POWERED_OFF || STATUS_OFF_SITE
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) && 
				 !STATUS_POWERED_OFF.equals( status ) &&
				 !STATUS_OFF_SITE.equals( status ) ) {
				return false;
			} else {
			    String clazz = toLowerCase( itemObjArray[3] );
			    String subClazz = toLowerCase( itemObjArray[4] );			    
				if ( clazz==null || subClazz==null ) return false;
				
				//check Permitted States By Class
				if ( !clazz.equals( CLASS_DEVICE ) || 
					 !(subClazz.equals( SUBCLASS_STANDARD ) || subClazz.equals( SUBCLASS_BLADE_SERVER )) ) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	static private boolean permittedStatesByClass( String status, String clazz, String subClazz ) {
		
	    if ( clazz==null ) return false;
		
		//check Permitted States By Class
		if ( clazz.equals( CLASS_DEVICE ) || clazz.equals( CLASS_NETWORK ) ) {			    
			if ( subClazz==null ) return false;
			if ( !permittedStatesMapping.contains( status + ":" + clazz + ":" + subClazz ) ) {
				return false;
			}
		} else {
			if ( !permittedStatesMapping.contains( status + ":" + clazz ) ) {
				return false;
			}
		}
		
		return true;
	}
	
	//ID_REQ_OFF_SITE
	static private boolean checkReqOffSite( HashMap selectedItemHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;

		//STATUS_INSTALLED || STATUS_POWERED_OFF || STATUS_STORAGE
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) && 
				 !STATUS_POWERED_OFF.equals( status ) &&
				 !STATUS_STORAGE.equals( status ) ) {
				return false;
			} else {
			    String clazz = toLowerCase( itemObjArray[3] );
			    String subClazz = toLowerCase( itemObjArray[4] );			    
			    if ( !permittedStatesByClass( STATUS_OFF_SITE, clazz, subClazz ) ) return false;
			}
		}
		
		return true;
	}
	
    //ID_REQ_TO_STORAGE
	static private boolean checkReqToStorage( HashMap selectedItemHashMap, HashMap childrenHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;

		//STATUS_INSTALLED || STATUS_POWERED_OFF || STATUS_STORAGE
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) && 
				 !STATUS_POWERED_OFF.equals( status ) &&
				 !STATUS_OFF_SITE.equals( status ) ) {
				return false;
			} else {
			    String clazz = toLowerCase( itemObjArray[3] );
			    String subClazz = toLowerCase( itemObjArray[4] );			    
			    if ( !permittedStatesByClass( STATUS_STORAGE, clazz, subClazz ) ) return false;
			}
		}
		
		//chack Parent-Child State Constraints
		/*for ( Object childObj : childrenHashMap.values() ) {
			String statusChild = toLowerCase( ((Object[])childObj)[2] );
			if ( !STATUS_STORAGE.equals( statusChild ) ) return false;
		}*/
		
		return true;
	}
	
	//ID_REQ_POWER_OFF
	static private boolean checkReqPowerOff( HashMap selectedItemHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;

		//STATUS_INSTALLED
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) ) {
				return false;
			} else {
			    String clazz = toLowerCase( itemObjArray[3] );
			    String subClazz = toLowerCase( itemObjArray[4] );			    
			    if ( !permittedStatesByClass( STATUS_POWERED_OFF, clazz, subClazz ) ) return false;
			}
		}
		
		return true;
	}
	
	//ID_REQ_POWER_ON
	static private boolean checkReqPowerOn( HashMap selectedItemHashMap, HashMap parentHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;
		
		//STATUS_POWERED_OFF
		for ( Object obj : selectedItemHashMap.values() ) {
			String status = toLowerCase( ((Object[])obj)[2] );
			if ( !STATUS_POWERED_OFF.equals( status ) ) return false;
		}
		
		//All Parent's status must be STATUS_INSTALLED
		/*for ( Object obj : parentHashMap.values() ) {
			String status = toLowerCase( ((Object[])obj)[2] );
			if ( !STATUS_INSTALLED.equals( status ) ) return false;
		}*/
		
		return true;
	}
	
	//ID_REQ_ON_SITE
	static private boolean checkReqOnSite( HashMap selectedItemHashMap, HashMap parentHashMap ) {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;
		
		//STATUS_OFF_SITE
		for ( Object obj : selectedItemHashMap.values() ) {
			String status = toLowerCase( ((Object[])obj)[2] );
			if ( !STATUS_OFF_SITE.equals( status ) ) return false;
		}
		
		//All Parent's status must be STATUS_INSTALLED
		/*for ( Object obj : parentHashMap.values() ) {
			String status = toLowerCase( ((Object[])obj)[2] );
			if ( !STATUS_INSTALLED.equals( status ) ) return false;
		}*/
		
		return true;
	}
	
    //ID_REQ_TO_ARCHIVE
	static private boolean checkReqToArchive( HashMap selectedItemHashMap, List itemList ) throws Exception {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size()==0 ) return false;

		//STATUS_INSTALLED || STATUS_POWERED_OFF || STATUS_STORAGE
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) && 
				 !STATUS_POWERED_OFF.equals( status ) &&
				 !STATUS_OFF_SITE.equals( status ) &&
				 !STATUS_STORAGE.equals( status ) ) {
				return false;
			} /*else {

				//chack Parent-Child State Constraints
				if ( !STATUS_STORAGE.equals( status ) ) {
					String itemId = toLowerCase( itemObjArray[0] );
					HashMap childrenHashMap = new HashMap();
					loadChildren( itemId, itemList, childrenHashMap );
					for ( Object childObj : childrenHashMap.values() ) {
						String statusChild = toLowerCase( ((Object[])childObj)[2] );
						if ( !STATUS_ARCHIVED.equals( statusChild ) ) return false;
					}
				}
			}*/
		}
		
		return true;
	}
	
	//ID_REQ_RESUBMIT
	static private boolean checkReqResubmit( List<Long> itemIdList, SessionFactory sessionFactory ) {
		
		if ( itemIdList==null || itemIdList.size()==0 ) return false;
		
		String idInStr = "";
		for ( Object itemId : itemIdList ) {
			if(idInStr.length() > 0) idInStr += ", ";
			idInStr += itemId;
		}
		idInStr = "(" + idInStr + ")";
		
		String reqStageStr = "(" +
			SystemLookup.RequestStage.REQUEST_ISSUED + "," + 
			SystemLookup.RequestStage.REQUEST_REJECTED + "," + 
			SystemLookup.RequestStage.REQUEST_UPDATED + ")";

		String reqTypeStr = "(" +
				SystemLookup.RequestTypeLkp.CONNECT + "," + 
				SystemLookup.RequestTypeLkp.DISCONNECT + ")";
		
		try {
			String queryStr = 
				"select count(*)\n" +
			    "from tblrequest req inner join tblrequesthistory reqhis on req.id = reqhis.requestid " +
			    "inner join dct_lks_data lks on lks.lks_id = reqhis.stageid " + 
			    "inner join dct_lks_data lksType on lksType.lks_id = req.request_type_lks_id " +
			    "where reqhis.current = true and lks.lkp_value_code in " + reqStageStr + " and req.itemid in " + idInStr + " and lksType.lkp_value_code not in " + reqTypeStr;
			
			Session session = sessionFactory.getCurrentSession();	
			Query query = session.createSQLQuery( queryStr );
		
			System.out.println(queryStr);
			
			List reqList = query.list();
			
			if ( reqList!=null && reqList.size()>0 ) {
				int count = new Integer( reqList.get(0).toString() ).intValue();
				if ( count>0 ) return true;
			}
		} catch ( Exception ex ) {
			log.info( "", ex );
		}

		return false;
	}

	private boolean enableButtonClone( UserInfo userInfo, HashMap selectedItemHashMap ) {
		
		if ( userInfo==null || selectedItemHashMap==null || selectedItemHashMap.size()==0) return false;
		
		if (userInfo.isViewer()) {
			return false;
		}
		
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			BigInteger classLkpValueCode = (BigInteger)itemObjArray[5];
			BigInteger subclassLkpValueCode = (BigInteger)itemObjArray[6];
			
			if(subclassLkpValueCode == null) subclassLkpValueCode = BigInteger.valueOf(-1);
			
			if (!itemClone.isClonable(classLkpValueCode.longValue(), subclassLkpValueCode.longValue())){
				return false;
			} 		
		}	
		  
		return true;
	}
		
	static private String toLowerCase( Object obj ) {
		return obj==null ? null : obj.toString().trim().toLowerCase();
	}

	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemStatusForPrepareMove =
			Collections.unmodifiableMap(new HashMap<String, List<String>>() {{
				
				put(CLASS_DEVICE, Arrays.asList(STATUS_INSTALLED, STATUS_POWERED_OFF));
				
				put(CLASS_NETWORK, Arrays.asList(STATUS_INSTALLED, STATUS_POWERED_OFF));
				
			}});
	
	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemClassAndMountingForPrepareMove =
			Collections.unmodifiableMap(new HashMap<String, List<String>>() {{
				
				put(CLASS_DEVICE, Arrays.asList(MOUNTING_RACKABLE));
				
				put(CLASS_NETWORK, Arrays.asList(MOUNTING_RACKABLE));
				
			}});
	
	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemStatusForQuickMove =
	
			Collections.unmodifiableMap(new HashMap<String, List<String>>(allowedItemStatusForPrepareMove) {{
				
				/*put(CLASS_DEVICE, Arrays.asList(STATUS_INSTALLED, STATUS_POWERED_OFF));
				
				put(CLASS_NETWORK, Arrays.asList(STATUS_INSTALLED, STATUS_POWERED_OFF));*/
				
				put(CLASS_CABINET, Arrays.asList(STATUS_INSTALLED));
				
			}});
	
	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemClassAndMountingForQuickMove =
	
			Collections.unmodifiableMap(new HashMap<String, List<String>>(allowedItemClassAndMountingForPrepareMove) {{
				
				/*put(CLASS_DEVICE, Arrays.asList(MOUNTING_RACKABLE));
				
				put(CLASS_NETWORK, Arrays.asList(MOUNTING_RACKABLE));*/
				
				put(CLASS_CABINET, Arrays.asList(MOUNTING_FREE_STANDING));
				
				put(CLASS_DEVICE, Arrays.asList(MOUNTING_RACKABLE, MOUNTING_BLADE));
				
				put(CLASS_NETWORK, Arrays.asList(MOUNTING_RACKABLE, MOUNTING_BLADE));
				
			}});

	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemClassAndSubclassForPrepareMove =
	
			Collections.unmodifiableMap(new HashMap<String, List<String>>() {{
				
			}});


	@SuppressWarnings("serial")
	public static final Map<String, List<String>> allowedItemClassAndSubclassForQuickMove =
	
			Collections.unmodifiableMap(new HashMap<String, List<String>>(allowedItemClassAndSubclassForPrepareMove) {{
				
				put(CLASS_CABINET, Arrays.asList(SUBCLASS_NONE));
				
			}});
	
	
	static private boolean checkReqQuickItemMove( HashMap selectedItemHashMap, List itemList ) throws Exception {
		
		return checkReqItemMove( selectedItemHashMap, itemList, allowedItemStatusForQuickMove, allowedItemClassAndMountingForQuickMove, allowedItemClassAndSubclassForQuickMove );
		
	}
	
	static private boolean checkReqItemMove( HashMap selectedItemHashMap, List itemList ) throws Exception {
		
		return checkReqItemMove( selectedItemHashMap, itemList, allowedItemStatusForPrepareMove, allowedItemClassAndMountingForPrepareMove, null );
		
	}
	
    //ID_REQ_ITEM_MOVE
	static private boolean checkReqItemMove1( HashMap selectedItemHashMap, List itemList ) throws Exception {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size() != 1 ) return false;

		//STATUS_INSTALLED || STATUS_POWERED_OFF
		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			if ( !STATUS_INSTALLED.equals( status ) && 
				 !STATUS_POWERED_OFF.equals( status )) {
				return false;
			} else {
			    String clazz = toLowerCase( itemObjArray[3] );
			    String mounting = toLowerCase( itemObjArray[7] );			    
				if ( clazz==null || mounting==null ) return false;
				
				//check Permitted States By Class
				if ( !((clazz.equals( CLASS_DEVICE ) || clazz.equals( CLASS_NETWORK )) && mounting.equals( MOUNTING_RACKABLE )) && !clazz.equals( CLASS_CABINET ) ) {
					return false;
				}
			}
			
		}
		
		return true;
	}	

	static private boolean checkReqItemMove( HashMap selectedItemHashMap, List itemList, 
			Map<String, List<String>> allowedItemStatus, 
			Map<String, List<String>> allowedItemClassAndMounting, 
			Map<String, List<String>> allowedItemClassAndSubclass) 
					throws Exception {
		
		if ( selectedItemHashMap==null || selectedItemHashMap.size() != 1 ) return false;

		for ( Object obj : selectedItemHashMap.values() ) {
			Object[] itemObjArray = (Object[])obj;
			String status = toLowerCase( itemObjArray[2] );
			String clazz = toLowerCase( itemObjArray[3] );
			String mounting = toLowerCase( itemObjArray[7] );
			String subclass = toLowerCase(itemObjArray[4] );

			// Check valid parameters provided
			if ( clazz==null || mounting==null ) return false;

			// Check if class is allowed
			Set<String> allowedClasses = allowedItemClassAndMounting.keySet();
			if ( !allowedClasses.contains(clazz) ) return false;

			// Check item status
			List<String> allowedStatuses = allowedItemStatus.get(clazz);
			if ( !allowedStatuses.contains(status) ) return false;

			// Check mounting of the class is allowed
			List<String> mountings = allowedItemClassAndMounting.get(clazz);
			if ( !mountings.contains(mounting) ) return false;
			
			if (null != allowedItemClassAndSubclass) {
				List<String> allowedSubClasses = allowedItemClassAndSubclass.get(clazz);
				if (!( null != allowedSubClasses && allowedSubClasses.contains(SUBCLASS_NONE) && (null == subclass) )) { 
					if ( null != allowedSubClasses && !allowedSubClasses.contains(subclass) ) return false;
				}
				 
			}
			
		}
		
		return true;
	}
	
	private boolean isNotMoveRequest(List<Long> itemList){
		Long movingItemId;
		long itemId;
		
		for(Object obj:itemList){
			if(obj instanceof Long){
				itemId = ((Long)obj).longValue();
			}
			else{
				itemId = ((Integer)obj).longValue();
			}
			
			movingItemId = powerPortMoveDAO.getMovingItemId(itemId);
		
			if(movingItemId != null && movingItemId > 0) return false;
		}
		
		return true;
	}
	
}
