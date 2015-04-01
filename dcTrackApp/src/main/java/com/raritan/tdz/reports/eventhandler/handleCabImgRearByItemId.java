package com.raritan.tdz.reports.eventhandler;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.eventadapter.ImageEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IImageInstance;

import com.raritan.tdz.reports.imageprovider.ImageProvider;
import com.raritan.tdz.util.GlobalConstants;

public class handleCabImgRearByItemId extends ImageEventAdapter {
	
	@Override
	public void onCreate(IImageInstance image, IReportContext reportContext) {
		try {
			
			String classcode = (image.getRowData().getColumnValue("parent_class") != null) 
					? image.getRowData().getColumnValue("parent_class").toString()
					: image.getRowData().getColumnValue("class_code").toString();
			
			if (Long.parseLong(classcode) == GlobalConstants.CABINET_VAL_CODE) {
				
				int id = Integer.parseInt(image.getRowData().getColumnValue("item_id").toString());

				ImageProviderProxy provider = (ImageProviderProxy) reportContext.getAppContext().get(ReportConstants.IMAGEPROVIDERPROXY_ID);

				provider.handleCabImgFrontByItemId(image, id, ImageProvider.rails_t.REAR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
