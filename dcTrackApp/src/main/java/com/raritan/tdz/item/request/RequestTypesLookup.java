package com.raritan.tdz.item.request;

public class RequestTypesLookup {
	// These are the request types const used in messages returned on error condition.
	// also it could be used for construction Action menus.
	public static class RequestType {
		public static final String CONVERT_TO_VM_REQUEST = "Convert Item to VM Request";
		public static final String PREPARE_MOVE_ITEM_REQUEST = "Prepare Move Item Request";
		public static final String QUICK_MOVE_ITEM_REQUEST = "Quick Move Item Request";
		public static final String TAKE_ITEM_OFF_SITE_REQUEST = "Take Item Off-site Request";
		public static final String BRING_ITEM_ON_SITE_REQUEST = "Bring Item On-site Request";
		public static final String POWER_OFF_ITEM_REQUEST = "Power-off Item Request";
		public static final String POWER_ON_ITEM_REQUEST = "Power-on Item Request";
		public static final String DECOMMISSION_ITEM_TO_STORAGE_REQUEST = "Decommission Item To Storage Request";
		public static final String DECOMMISSION_ITEM_TO_ARCHIVE_REQUEST = "Decommission Item To Archive Request";
		public static final String INSTALL_ITEM_REQUEST = "Install Item Request";
		public static final String RESUBMIT_REQUEST = "Resubmit Request";
		
	}

}
