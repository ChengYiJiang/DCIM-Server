package com.raritan.tdz.reports.eventhandler;

public class ReportConstants {
    /* Object References (IDs)*/
    public final static String SESSION_ID = "Session";
    public final static String IMAGEPROVIDERPROXY_ID = "ImageProviderProxy";
    public final static String CABELEFRONTIMAGEPROVIDERPROXY_ID = "CabEleFrontImageProviderProxy";
    public final static String CABELEBACKIMAGEPROVIDERPROXY_ID = "CabEleBackImageProviderProxy";
    
    /* Parameter Names */
    public final static String CABINET_LIST_PARAM_ID = "cabinetIdList";
    public final static String ITEM_LIST_PARAM_ID = "itemIdList";
    public final static String MODEL_LIST_PARAM_ID = "ModelList";
    public final static String MANUFACTURER_LIST_PARAM_ID = "ManufacturerList";
    public final static String MANUFACTURER_ID_PARAM_ID = "ManufacturerId";
    public final static String LOCATION_LIST_ID_PARAM_ID = "locationIdList";
    
    public final static String CUSTOM_DETAILS_PARAM_ID = "CustomDetails";
    public final static String SITECODE_PARAM_ID = "SiteCode";
    public final static String CONNECTION_TYPE_PARAM_ID = "ConnectionType";
    public final static String CAB_IMAGEMODE_PARAM_ID = "cabImageMode";
    public final static String CAB_IMAGERAIL_PARAM_ID = "cabImageRails";
    public final static String LOCATION_PARAM_ID = "Location";
    public final static String CABS_PER_PAGE_PARAM_ID = "cabsPerPage";
    
    public final static String ORIENTATION_PARAM_ID = "Orientation";
    public final static String ARRANGE_DATA_IN_ROWS_PARAM_ID = "ArrangeDataInRows";
    
    /* Parameter Group - Hide Columns */
    public final static String HIDE_STATUS = "HideStatus";
    public final static String HIDE_POSITION = "HidePosition";
    public final static String HIDE_WEIGHT = "HideWeight";
    public final static String HIDE_ASSET_TAG = "HideAssetTag";
    public final static String HIDE_SERIAL_NO = "HideSerialNo";
    public final static String HIDE_MODEL = "HideModel";
    public final static String HIDE_MAKE = "HideMake";
    public final static String HIDE_DEPARTMENT = "HideDepartment";
    public final static String HIDE_ADMIN_TEAM = "HideAdminTeam";
    public final static String HIDE_SYS_ADMIN = "HideSysAdmin";
    public final static String HIDE_OS = "HideOS";
    public final static String HIDE_FUNCTION = "HideFunction";
    public final static String HIDE_TYPE = "HideType";
    public final static String HIDE_CNAME = "HideCName";
    public final static String HIDE_RACKUNITS = "HideRackUnits";
    public final static String HIDE_PURCHASE_DATE = "HidePurchaseDate";
    public final static String HIDE_PURCHASE_PRICE = "HidePurchasePrice";
    public final static String HIDE_EXPIRATION_DATE= "HideExpirationDate";
    public final static String HIDE_COMMENTS = "HideComments";
    
    public final static String SHOW_PORTS = "ShowPorts";
    
    /* CabImageMode Options */
    public final static String IMAGERAIL_FRONT = "front";
    public final static String IMAGERAIL_REAR = "rear";
    public final static String IMAGERAIL_BOTH = "both";
    public final static String IMAGEMODE_TEXT = "text";
    public final static String IMAGEMODE_IMAGE = "image";
    
    /* ConnectionType Options */
    public final static String CONNECTION_TYPE_POWER = "power";
    public final static String CONNECTION_TYPE_DATA = "data";
    public final static String CONNECTION_TYPE_ALL = "all";
    
    /* Orientation Modes */
    public final static String ORIENTATION_LANDSCAPE = "landscape";
    public final static String ORIENTATION_PORTRAIT = "portrait";
    
    public final static int MAX_RU_HEIGHT = 54;

    public final static int ITEM_PORT_IMAGE_FRONT = 0;
    public final static int ITEM_PORT_IMAGE_REAR = 1;

}
