/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.DataColumn;
import com.raritan.dctrack.xsd.DataRow;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiLookupFields;
import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
public class ModelLkpMethodCallback implements
		RemoteRefMethodCallback {
	
	SessionFactory sessionFactory;
	
	public ModelLkpMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallbackLookup#fillValueIdLookup(com.raritan.dctrack.xsd.UiLookupFields)
	 */
	@Override
	public void fillValue(UiComponent uiViewCompoent, String filterField, Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable{
		
		Long itemId = new Long(0);
		
//		if (remoteRef.getAdditionalDataKeyList().size() > 0){
//			itemId = (Long)additonalData.get(remoteRef.getAdditionalDataKeyList().get(0));
//		}
		
		
		List<DataRow> dataRows = uiViewCompoent.getUiLookupField().getDataRow();
		
		Long mfrId = getMfrId(itemId);
		List<Map> modelTOs = getModels(mfrId);
		
		for (Map map: modelTOs){
			DataRow row = new DataRow();
			DataColumn column = new DataColumn();
			row.setColumn(column);
			
			UiValueIdField valueIdField = new UiValueIdField();
			valueIdField.setValueId((Long)map.get("modelId"));
			valueIdField.setValue((String)map.get("modelName"));
			column.setValueIdField(valueIdField);
			
			dataRows.add(row);
		}
	}

	private Long getMfrId(Long itemId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("model", "model");
		criteria.createAlias("model.modelMfrDetails", "model_modelMfrDetails");
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("model_modelMfrDetails.modelMfrDetailId"), "modelMfrDetailId"));

		criteria.setProjection(proList);
		criteria.add(Restrictions.eq("itemId", itemId));
		
		Long modelMfrDetailId = (Long) criteria.uniqueResult();
		
		return modelMfrDetailId;
	}
	
	private List getModels(Long mfrId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.LEFT_JOIN);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("modelDetailId"), "modelId"));
		proList.add(Projections.alias(Projections.property("modelName"), "modelName"));

		criteria.setProjection(proList);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		List modelTOs = criteria.list();
		
		return modelTOs;
	}
}
