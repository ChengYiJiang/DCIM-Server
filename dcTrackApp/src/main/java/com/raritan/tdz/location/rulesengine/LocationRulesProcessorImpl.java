package com.raritan.tdz.location.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.criterion.Criterion;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessorBase;


public class LocationRulesProcessorImpl extends RulesProcessorBase {
	
	@Override
	protected UiView getUiView() {
		JXPathContext jxPath = JXPathContext.newContext(dcTrack);
		UiView uiView = (UiView) jxPath.getValue("uiViews/uiView[@uiId='locationView']");
		return uiView;
	}

	@Override
	protected void applyEditability(Criterion criterion)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		// XXX Auto-generated method stub
		
	}
	
	@Override
	protected RulesNodeEditability getEditability(Criterion criterion) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		return null;
	}

}
