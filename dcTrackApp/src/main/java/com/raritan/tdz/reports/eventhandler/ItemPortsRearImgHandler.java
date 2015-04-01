package com.raritan.tdz.reports.eventhandler;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.eventadapter.ImageEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IImageInstance;

public class ItemPortsRearImgHandler extends ImageEventAdapter {
    @Override
    public void onCreate(IImageInstance image, IReportContext reportContext) {
	try {
	    //no model - no image
	    if (image.getRowData().getColumnValue("model_id") != null) {
		int id = Integer.parseInt(image.getRowData().getColumnValue("item_id").toString());
		ImageProviderProxy provider = 
		    (ImageProviderProxy) reportContext.getAppContext().get(ReportConstants.IMAGEPROVIDERPROXY_ID);
		provider.handleItemPortsImg(image, id, ReportConstants.ITEM_PORT_IMAGE_REAR);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }    
}
