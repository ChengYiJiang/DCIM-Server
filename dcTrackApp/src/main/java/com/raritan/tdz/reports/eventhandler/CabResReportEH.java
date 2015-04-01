package com.raritan.tdz.reports.eventhandler;



import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;


public class CabResReportEH extends ReportEH {
//    private static final Logger logger = Logger.getLogger(CabResReportEH.class);

    public void initialize(IReportContext inReport) throws ScriptException  {
	super.initialize(inReport);
	inReport.setPersistentGlobalVariable("railusage", new Integer[ReportConstants.MAX_RU_HEIGHT]);
    }
}
