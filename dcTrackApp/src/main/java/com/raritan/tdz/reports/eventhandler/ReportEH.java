package com.raritan.tdz.reports.eventhandler;

import org.apache.log4j.Logger;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ReportEventAdapter;
import org.eclipse.birt.report.model.api.activity.SemanticException;


public class ReportEH extends ReportEventAdapter {
    private static final Logger logger = Logger.getLogger(ReportEH.class);

    public void afterFactory(IReportContext rc) throws ScriptException  {
	super.afterFactory(rc);
	logger.info("close Report");
    }

    public void initialize(IReportContext inReport) throws ScriptException {
	super.initialize(inReport);
	logger.info("Initialize Report");
	try {
	    Object obj = inReport.getParameterValue(ReportConstants.ORIENTATION_PARAM_ID);
	    if (obj != null) {
		String orient = ((String)(obj)).toLowerCase();
		// change layout
		// FIXME: how about multiple master pages... 
		if (orient.equals(ReportConstants.ORIENTATION_LANDSCAPE)){
		    logger.debug("set orientation mode to landscape");
		    inReport.getDesignHandle().getMasterPages().get(0).setProperty("orientation","landscape");
		} else if (orient.equals(ReportConstants.ORIENTATION_PORTRAIT)) {
		    logger.debug("set orientation mode to portrait");
		    inReport.getDesignHandle().getMasterPages().get(0).setProperty("orientation","portrait");
		} else {
		    logger.warn("unknown orientation mode: '" + orient + "'");
		}
	    }
	} catch (SemanticException e) {
	    logger.error("error setting orientation mode", e);
	}
    }
}
