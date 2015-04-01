/**
 * 
 */
package com.raritan.tdz.model.home;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;

import com.raritan.dctrack.xsd.AltUiValueIdFieldMap;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiView;
import com.raritan.dctrack.xsd.UiViewPanel;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.rulesengine.RulesNodeEditability;
import com.raritan.tdz.rulesengine.RulesProcessorBase;
import com.raritan.tdz.rulesengine.RulesQryMgr;
import com.raritan.tdz.rulesengine.RulesQryMgrHQLImpl;

/**
 * @author prasanna
 *
 */
public class ItemModelPanelRuleProcessor extends RulesProcessorBase {
	
	List<String> panelList;
	
	
	public List<String> getPanelList() {
		return panelList;
	}


	public void setPanelList(List<String> panelList) {
		this.panelList = panelList;
	}


	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessor#configure(java.lang.String)
	 */
	@Override
	public void configure(String fileName) 
			throws JAXBException, HibernateException, ClassNotFoundException, IOException {
		if (fileName != null)
			rulesTemplateFileName = fileName;
		
		//Parse the template
		parseTemplate();
		
		for (String panelId:panelList){
			//Get the UiViewPanel for physicalPanel in the Itemscreen.
			UiViewPanel panel = getUiViewPanel(panelId);
			
			//Now create the queries.
			RulesQryMgr qryMgrForPanel = new RulesQryMgrHQLImpl(remoteReference);
			for (UiComponent uiComponent: panel.getUiViewComponents().getUiViewComponent()){
				String remoteRef = uiComponent.getUiValueIdField().getRemoteRef();
				if (remoteReference.getRemoteRefMethodCallback(remoteRef) != null){
					callbacks.put(uiComponent.getUiId(), remoteReference.getRemoteRefMethodCallback(remoteRef));
				} else {
					qryMgrForPanel.createCriteria(uiComponent.getUiValueIdField().getRemoteRef(), 
							getXPathComponent(getUiView().getUiId(), panel.getUiId(), uiComponent.getUiId()), "main");
					
					qryMgrForPanel.addProjection(uiComponent.getUiValueIdField().getRemoteRef(), 
							getXPathComponent(getUiView().getUiId(), panel.getUiId(), uiComponent.getUiId()), "main");
				}
				
				//Loop through the alternate data list and add criteria and projection for that as well
				for (AltUiValueIdFieldMap altField: uiComponent.getAltUiValueIdFieldMap()){
					String remoteRefForAlt = altField.getUiValueIdField().getRemoteRef();
					if (remoteReference.getRemoteRefMethodCallbackUsingFilter(remoteRefForAlt) != null){
						callbacksUsingFilter.put(uiComponent.getUiId(), remoteReference.getRemoteRefMethodCallbackUsingFilter(remoteRefForAlt));
					}else{
						qryMgrForPanel.createCriteria(remoteRefForAlt,
								getXPathComponentAltList(getUiView().getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
						
						qryMgrForPanel.addProjection(remoteRefForAlt, 
								getXPathComponentAltList(getUiView().getUiId(),panel.getUiId(),uiComponent.getUiId(),altField.getId()), altField.getId());
					}
				}
			}
	
			qryMgrsForXPath.put(getXPathPanel(getUiView().getUiId(), panel.getUiId()),qryMgrForPanel);
		}
	}
	
	
	private UiViewPanel getUiViewPanel(String panelId) {
		JXPathContext jxPath = JXPathContext.newContext(getUiView());
		StringBuffer xpath = new StringBuffer();
		xpath.append("uiViewPanel[@uiId='");
		xpath.append(panelId);
		xpath.append("']");
		UiViewPanel uiViewPanel = (UiViewPanel) jxPath.getValue(xpath.toString());
		return uiViewPanel;
	}

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessorBase#getUiView()
	 */
	@Override
	protected UiView getUiView() {
		JXPathContext jxPath = JXPathContext.newContext(dcTrack);
		UiView uiView = (UiView) jxPath.getValue("uiViews/uiView[@uiId='itemView']");
		return uiView;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RulesProcessorBase#applyEditability(org.hibernate.criterion.Criterion)
	 */
	@Override
	protected void applyEditability(Criterion criterion)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected RulesNodeEditability getEditability(Criterion criterion) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		return null;
	}

}
