/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.dctrack.xsd.DataColumn;
import com.raritan.dctrack.xsd.DataRow;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiLookupFields;
import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
@Transactional
public class RULkpMethodCallback implements
		RemoteRefMethodCallback {
	
	SessionFactory sessionFactory;
	
	public RULkpMethodCallback(SessionFactory sessionFactory){
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
		
		Map itemTO = getItemTO(itemId);
		
		if (itemTO != null 
				&& itemTO.get("cabinetId") != null
				&& itemTO.get("ruHeight") != null
				&& itemTO.get("uPosition") != null){
			Collection<Long> availableUPositions = getAvailableUPositions((Long)itemTO.get("cabinetId"),
					((Integer)itemTO.get("ruHeight")).intValue(),
					((Long)itemTO.get("uPosition")).intValue(),
					((Integer)itemTO.get("ruHeight")).intValue());
		
			for (Long uPosition : availableUPositions){
				DataRow row = new DataRow();
				DataColumn column = new DataColumn();
				row.setColumn(column);
				
				UiValueIdField valueIdField = new UiValueIdField();
				valueIdField.setValue(uPosition.toString());
				column.setValueIdField(valueIdField);
				
				dataRows.add(row);
			}
		}
		
	}

	private Map getItemTO(Long itemId) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("parentItem", "parentItem",Criteria.LEFT_JOIN);
		criteria.createAlias("model", "model",Criteria.LEFT_JOIN);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("parentItem.itemId"), "cabinetId"));
		proList.add(Projections.alias(Projections.property("model.ruHeight"), "ruHeight"));
		proList.add(Projections.alias(Projections.property("uPosition"), "uPosition"));

		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		criteria.add(Restrictions.eq("itemId", itemId));
		
		Map itemTO = (Map) criteria.uniqueResult();
		
		return itemTO;
	}
	
	
	private Collection<Long> getAvailableUPositions(Long cabinetId,int blockSize, int exceptionIndex, int exceptionBlockSize) throws Throwable {

		Session session = null;

		Collection<Long> resultArray = new ArrayList<Long>();


		{

			session = this.sessionFactory.getCurrentSession();
			Criteria hibernateCriteria = session.createCriteria(CabinetItem.class);
			hibernateCriteria =hibernateCriteria.add( Restrictions.eq("itemId", cabinetId));
			CabinetItem cabinet = (CabinetItem) hibernateCriteria.uniqueResult() ;

			String value = cabinet.getLayoutHorizFront();
			if (value == null){
				//Unacceptable Data. Cannot Complete Operation
				//throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.AVAIL_UPOS_NOT_FND_FOR_ID.value(), this.getClass()));
				return resultArray;
			}

			int numberOfRU = value.length();
			StringBuffer neededBlockBuf = new StringBuffer();
			StringBuffer minBuf = new StringBuffer();
			for(int i=0; i < numberOfRU;i++ ){
				if(i < blockSize){
					neededBlockBuf.append('1');
					minBuf.append('1');
				} else {
					neededBlockBuf.append('0');
				}
			}

			String min = minBuf.toString();
			long cabinetPos = Long.parseLong( cabinet.getLayoutHorizFront().trim(),2);
			

			if(exceptionIndex!= 0 && exceptionBlockSize != 0)
			{
				//Do the exceptions now
				StringBuffer exceptionMaskBuf = new StringBuffer();
				for(int i=0; i < numberOfRU;i++ ){
					if( ( i>=(exceptionIndex-1) ) && (i<exceptionIndex-1+exceptionBlockSize) )
					{
						exceptionMaskBuf.append('1');
					} else {
						exceptionMaskBuf.append('0');
					}

				}
				long exceptionMask = Long.parseLong(exceptionMaskBuf.toString(), 2);
				//Mask out all the exceptions from the cabinetPos binary info
				cabinetPos = cabinetPos ^ exceptionMask;
			}

			long neededCabinetPos = Long.parseLong( neededBlockBuf.toString(), 2);

			//TODO Anilk on 6/11 probably not needed. May remove it later.
			//long toCompare = Long.parseLong(min, 2);

			for(int j =1; j <= numberOfRU-blockSize+1;j++ )
			{
				long temp ;
				temp = cabinetPos ^ neededCabinetPos;
				
				temp = temp & neededCabinetPos;
				
				String tmp= Long.toBinaryString(temp);
				if(tmp.indexOf(min)==0){
					resultArray.add(new Long(j));
				}
				cabinetPos = cabinetPos<<1;

			}
		}


		return resultArray;

	}

	public class ItemTO{
		private Long cabinetId;
		private Long uPosition;
		private Long ruHeight;
		public Long getCabinetId() {
			return cabinetId;
		}
		public void setCabinetId(Long cabinetId) {
			this.cabinetId = cabinetId;
		}
		public Long getuPosition() {
			return uPosition;
		}
		public void setuPosition(Long uPosition) {
			this.uPosition = uPosition;
		}
		public Long getRuHeight() {
			return ruHeight;
		}
		public void setRuHeight(Long ruHeight) {
			this.ruHeight = ruHeight;
		}
		
		
	}

}
