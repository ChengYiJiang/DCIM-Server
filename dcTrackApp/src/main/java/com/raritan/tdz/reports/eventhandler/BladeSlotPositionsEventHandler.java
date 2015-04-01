package com.raritan.tdz.reports.eventhandler;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.TextItemEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.ITextItemInstance;
import org.springframework.context.ApplicationContext;

import com.raritan.tdz.chassis.home.ChassisHome;
import com.raritan.tdz.exception.DataAccessException;

public class BladeSlotPositionsEventHandler extends TextItemEventAdapter {

	private ChassisHome chassisHome;
	
	@Override
	public void onCreate(ITextItemInstance text, IReportContext reportContext)
			throws ScriptException {
		
		Integer bladeId = Integer.parseInt(((BigDecimal) text.getRowData().getColumnValue("item_id")).toString());
		Number slotPosition = (Number) text.getRowData().getColumnValue("slot_position");
		
		super.onCreate(text, reportContext);
		
		if (null == slotPosition || slotPosition.intValue() < 1 || null == bladeId || bladeId < 0) {
			
			text.setText("");
		}
		else {

			try {
				
				getBeans(reportContext);
				
				text.setText(getSortedSlotPositions(bladeId));
				
			} catch (DataAccessException e) {
				
				text.setText(slotPosition.toString());
				
				throw new ScriptException(e.getMessage());
			}
			
		}
		
	}
	
	
	private String getSortedSlotPositions(long bladeId) throws DataAccessException {
		
		String slotLabel = "slot error";
		if (null != chassisHome) {
			Map<Long, String> sortedSlotNumbers = chassisHome.getSortedSlotNumber(bladeId);
			
			Collection<String> slotPositions = sortedSlotNumbers.values();
			
			slotLabel = StringUtils.join(slotPositions, ",");
		}
		
		char[] splitSlotLabel = slotLabel.toCharArray();
		String newSplitLabel = new String();
		
		for (int i = 0; i < splitSlotLabel.length; i++) {
			newSplitLabel += splitSlotLabel[i];
			if (0 == ((i + 1) % 6)) {
				newSplitLabel += "\n";
			}
		}
		
		return newSplitLabel;
		
	}
	
	private void getBeans(ApplicationContext applicationContext) {
		
		chassisHome = (ChassisHome) applicationContext.getBean("chassisHome");
		
	}
	
	private void getBeans(IReportContext reportContext) {

		ApplicationContext applicationContext = (ApplicationContext) reportContext.getAppContext().get("reportContextAwareKey");
		
		if (null != applicationContext) {
			getBeans(applicationContext); 
		}
		
		
	}
	
	
}
