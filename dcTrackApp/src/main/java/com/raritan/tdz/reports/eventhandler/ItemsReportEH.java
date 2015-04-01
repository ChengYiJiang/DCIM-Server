package com.raritan.tdz.reports.eventhandler;

import org.apache.log4j.Logger;

import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.element.IReportDesign;
import org.eclipse.birt.report.model.api.TableGroupHandle;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;


public class ItemsReportEH extends ReportEH {
    private static final Logger logger = Logger.getLogger(ItemsReportEH.class);

    public void beforeFactory(IReportDesign design, IReportContext rc) throws ScriptException {
	super.beforeFactory(design, rc);

	try {
	    TableHandle main = (TableHandle) rc.getDesignHandle().findElement("maintable");
	    
	    boolean showports = false;
	    boolean arrangeinrows = false;
	    
	    Object obj = rc.getParameterValue(ReportConstants.SHOW_PORTS);
	    if (obj != null) {
		Boolean param = (Boolean)obj;
		showports = param.booleanValue();
	    }
	    obj = rc.getParameterValue(ReportConstants.ARRANGE_DATA_IN_ROWS_PARAM_ID);
	    if (obj != null) {
		Boolean param = (Boolean)obj;
		arrangeinrows = param.booleanValue();
	    }
	    
	    //either drop 0 or 1 row
	    if (arrangeinrows) {
		TableGroupHandle grp = (TableGroupHandle) main.getGroups().get(0);
		grp.getHeader().get(1).drop();//header
		main.getDetail().get(0).drop();//drop normal details (1st row) in case we arrange in rows
	    } else {
		main.getDetail().get(1).drop();//drop arranged in row details (2nd row) in case we arrange not in rows
	    }
	    
	    if (!showports) {
		// index decreases per drop !!!
		main.getDetail().get(1).drop();//drop data ports
		main.getDetail().get(1).drop();//drop power ports
	    }
	    
	} catch (SemanticException e) {
	    logger.error("error droping elements", e);
	}
    }

}
