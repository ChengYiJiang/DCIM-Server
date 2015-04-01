package com.raritan.tdz.reports.eventhandler;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.element.IReportDesign;

public class ImgProvReportEH extends ReportEH {
	// private static final Logger logger =
	// Logger.getLogger(ImgProvReportEH.class);

	public void afterFactory(IReportContext rc) throws ScriptException {
		super.afterFactory(rc);
		
		cleanImageProvider(rc, ReportConstants.IMAGEPROVIDERPROXY_ID);
		
		cleanImageProvider(rc, ReportConstants.CABELEFRONTIMAGEPROVIDERPROXY_ID);
		
		cleanImageProvider(rc, ReportConstants.CABELEBACKIMAGEPROVIDERPROXY_ID);
		
	}

	@SuppressWarnings("unchecked")
	public void beforeFactory(IReportDesign design, IReportContext rc)
			throws ScriptException {
		super.beforeFactory(design, rc);
		// create global image provider proxy
		rc.getAppContext().put(ReportConstants.IMAGEPROVIDERPROXY_ID,
				new ImageProviderProxy(rc));
		
		rc.getAppContext().put(ReportConstants.CABELEFRONTIMAGEPROVIDERPROXY_ID,
				new ImageProviderProxy(rc, ReportConstants.IMAGERAIL_FRONT));

		rc.getAppContext().put(ReportConstants.CABELEBACKIMAGEPROVIDERPROXY_ID,
				new ImageProviderProxy(rc, ReportConstants.IMAGERAIL_REAR));

	}
	
	private void cleanImageProvider(IReportContext rc, String imageProviderProxyID) {
		
		// release global image provider proxy
		ImageProviderProxy proxy = (ImageProviderProxy) rc.getAppContext().get(
				imageProviderProxyID);

		proxy.cleanup();
		
		rc.getAppContext().remove(imageProviderProxyID);
		
	}
}

