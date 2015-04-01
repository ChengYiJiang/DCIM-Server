package com.raritan.tdz.circuit.home;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.raritan.tdz.chassis.home.CompositeItem;
import com.raritan.tdz.chassis.home.NetworkBladeItemImpl;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitListDTO;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemObject;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.LogManager;
import com.raritan.tdz.util.MessageContext;

/**
 * Default Circuit Search implementation.
 * @author Andrew Cohen
 */
public class CircuitSearchImpl implements CircuitSearch {

	private final Logger log = Logger.getLogger("CircuitSearch");
	
	private ItemObjectFactory itemObjectFactory;
	private SessionFactory sessionFactory;
	
	@Autowired
	private ItemDAO itemDAO;
	
	public CircuitSearchImpl( 
			ItemObjectFactory itemObjectFactory,
			SessionFactory sessionFactory) {
		this.itemObjectFactory = itemObjectFactory;
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<CircuitListDTO> searchCircuits(CircuitCriteriaDTO cCriteria,
			Map<Long, CircuitRequestInfo> reqInfoData,
			Map<Long, CircuitRequestInfo> reqInfoPower,
			Set<String> sharedDataCircuitTraces,
			Set<String> sharedPowerCircuitTraces
			) throws DataAccessException {
		//long time = System.currentTimeMillis();
		final String methodName = "searchCircuits";
		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), log);
		
		CircuitResults results = executeCircuitQuery( cCriteria );
		
		if ( results.getList().isEmpty() ) {
			return Collections.emptyList();
		}
		
		results.setReqInfoData( reqInfoData );
		results.setReqInfoPower( reqInfoPower );
		results.setSharedDataCircuitTraces( sharedDataCircuitTraces );
		results.setSharedPowerCircuitTraces( sharedPowerCircuitTraces );
		
		List<CircuitListDTO> circuits = postProcessResults( cCriteria,  results );
		
		//System.out.println("Time to get Circuit List: "+(System.currentTimeMillis()-time)+" ms");
		return circuits;
	}
	

	@Override
	public List<CircuitViewData> searchCircuitsRaw(CircuitCriteriaDTO cCriteria) throws DataAccessException {
		return executeCircuitQuery( cCriteria ).getList();
	}
	
	//
	// Private classes and methods
	//

	private class CircuitResults {
		private List<CircuitViewData> list = null;
		private boolean requiresPostSort = false;
		private Map<Long, CircuitRequestInfo> reqInfoData;
		private Map<Long, CircuitRequestInfo> reqInfoPower;
		private Set<String> sharedDataCircuitTraces;
		private Set<String> sharedPowerCircuitTraces;
		
		CircuitResults() {
		}
		
		public boolean getRequiresPostSort() {
			return requiresPostSort;
		}
		
		public List<CircuitViewData> getList() {
			if (list == null) return Collections.emptyList();
			return list;
		}

		public void setList(List<CircuitViewData> list) {
			this.list = list;
		}

		public void setRequiresPostSort(boolean requiresPostSort) {
			this.requiresPostSort = requiresPostSort;
		}
		
		public int size() {
			return list != null ? list.size() : 0;
		}

		public Map<Long, CircuitRequestInfo> getReqInfoData() {
			if (reqInfoData == null) return Collections.emptyMap();
			return reqInfoData;
		}

		public void setReqInfoData(Map<Long, CircuitRequestInfo> reqInfoData) {
			this.reqInfoData = reqInfoData;
		}

		public Map<Long, CircuitRequestInfo> getReqInfoPower() {
			if (reqInfoPower == null) return Collections.emptyMap();
			return reqInfoPower;
		}

		public void setReqInfoPower(Map<Long, CircuitRequestInfo> reqInfoPower) {
			this.reqInfoPower = reqInfoPower;
		}

		public Set<String> getSharedDataCircuitTraces() {
			if (sharedDataCircuitTraces == null) return Collections.emptySet();
			return sharedDataCircuitTraces;
		}

		public void setSharedDataCircuitTraces(Set<String> sharedDataCircuitTraces) {
			this.sharedDataCircuitTraces = sharedDataCircuitTraces;
		}

		public Set<String> getSharedPowerCircuitTraces() {
			if (sharedPowerCircuitTraces == null) return Collections.emptySet();
			return sharedPowerCircuitTraces;
		}

		public void setSharedPowerCircuitTraces(Set<String> sharedPowerCircuitTraces) {
			this.sharedPowerCircuitTraces = sharedPowerCircuitTraces;
		}
	}
	
	/**
	 * 
	 * @param cCriteria
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	private CircuitResults executeCircuitQuery(CircuitCriteriaDTO cCriteria) throws DataAccessException {
		//long time = System.currentTimeMillis();
		CircuitResults results = new CircuitResults();
		final long itemId = cCriteria.getItemId() != null ? cCriteria.getItemId() : 0;
		final String sortCol = cCriteria.getSortColumn();
		
		try {
			Session session = this.sessionFactory.getCurrentSession();
			
			Set<Long> itemList = getItemIdList( itemId );

			Criteria cr = buildSearchCriteria(session, cCriteria, itemList);
			
			results.setRequiresPostSort( !addSortCriteria(cr, sortCol, cCriteria.getSortOrderDesc()) );
			
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			
			results.setList( cr.list() );
		}
		catch (HibernateException e) {
			throw new DataAccessException(new ExceptionContext(cCriteria.getFetchEnumError(), this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(new ExceptionContext(cCriteria.getFetchEnumError(), this.getClass(), e));
		}

		//System.out.println("Time to get Circuit List: "+(System.currentTimeMillis()-time)+" ms");
		return results;
	}
	
	/**
	 * 
	 * @param itemId
	 * @return
	 * @throws DataAccessException
	 */
	private Set<Long> getItemIdList(long itemId) throws DataAccessException {
		Set<Long> itemList = new HashSet<Long>();

		if(itemId > 0){
			// We need to check if the item is a composite item (i.e., chassis, network stack, etc.)
			// If it is, then we need to add all of its child items to the itemList that we will query.
			//Item item = itemHome.viewItemEagerById( itemId );
			
			ItemObject itemObj = itemObjectFactory.getItemObject( itemId );
			
			if (itemObj != null && (itemObj instanceof NetworkBladeItemImpl)) {
				ItItem blade =(ItItem)itemDAO.loadItem(itemId);

				long chassisId = blade.getBladeChassis() != null ? blade.getBladeChassis().getItemId() : 0;

				itemObj = itemObjectFactory.getItemObject( chassisId );
			}
			
			if (itemObj != null && (itemObj instanceof CompositeItem)) {
				List<Item> childItems = ((CompositeItem)itemObj).getChildItems();
				for (Item child : childItems) {
					itemList.add( child.getItemId() );
				}
			}
			
			// copy item to list
			itemList.add( itemId );
		}
		
		return itemList;
	}
	
	/**
	 * 
	 * @param session
	 * @param cCriteria
	 * @param itemList
	 * @return
	 * @throws DataAccessException
	 */
	private Criteria buildSearchCriteria(Session session, CircuitCriteriaDTO cCriteria, Set<Long> itemList) throws DataAccessException {
		Criteria cr = session.createCriteria(CircuitViewData.class);
		final long circuitId = cCriteria.getCircuitUID().getCircuitDatabaseId();
		
		if(cCriteria.getCircuitType() != null && cCriteria.getCircuitType() > 0) {
			cr.add(Restrictions.eq("circuitType", cCriteria.getCircuitType()));
		}
		if(circuitId > 0){
			cr.add(Restrictions.eq("circuitId", circuitId));
		}
		else if(cCriteria.getContainCircuitTrace() != null && cCriteria.getContainCircuitTrace().trim().length() > 0){
			cr.add(Restrictions.like("circuitTrace", cCriteria.getContainCircuitTrace().trim(), MatchMode.ANYWHERE));
		}			
		else{
			if(cCriteria.getLocationId() != null && cCriteria.getLocationId() > 0){
				cr.add(Restrictions.eq("locationId", cCriteria.getLocationId()));
			}
			
			if (StringUtils.hasText(cCriteria.getLocationCode())) {
				cr.add(Restrictions.eq("locationCode", cCriteria.getLocationCode()));
			}
						
			if(cCriteria.getStartWithClassCode() != null && cCriteria.getStartWithClassCode() > 0){
				cr.add(Restrictions.eq("startClassCode", cCriteria.getStartWithClassCode()));
			}
			if(cCriteria.getEndWithClassCode() != null && cCriteria.getEndWithClassCode() > 0){
				cr.add(Restrictions.eq("endClassCode", cCriteria.getEndWithClassCode()));
			}
			if(itemList.size() > 0){
				cr.add(Restrictions.or(Restrictions.in("startItemId", itemList), Restrictions.in("endItemId", itemList)));
			}
			if(cCriteria.getPortId() != null && cCriteria.getPortId() > 0){
				cr.add(Restrictions.or(Restrictions.eq("startPortId", cCriteria.getPortId()), Restrictions.eq("endPortId", cCriteria.getPortId())));
			}
			if(cCriteria.getStartPortId() != null && cCriteria.getStartPortId() > 0){
				cr.add(Restrictions.eq("startPortId", cCriteria.getStartPortId()));
			}
		}
		
		// Pagination of results
		final int maxLinesPerPage = cCriteria.getMaxLinesPerPage();
		if (maxLinesPerPage > 0) {
			cr.setFirstResult( (cCriteria.getPageNumber() - 1) * maxLinesPerPage );
			cr.setMaxResults( maxLinesPerPage );
		}
		
		return cr;
	}
	
	private boolean addSortCriteria(Criteria cr, String sortCol, boolean sortOrderDesc) {
		String sortField = sortCol;
		//Because the View and DTO has separate names for the below fields , doing some special handling below.
		if (sortField != null) {
			if(sortField.equals("startCabinetName") ){
				sortField = "cabinetName";
			}
			if(sortField.equals("startConnectorName") ){
				sortField = "connectorName";
			}
		}
		
		if (StringUtils.hasText(sortCol)) {
			
			if (postSortColumns.contains(sortCol)) {
				return false;
			}
			
			// Custom sort order
			cr.addOrder(sortOrderDesc ? Order.desc(sortField) : Order.asc(sortField));
		}
		else {
			// Default sort order is by creation date
			cr.addOrder( Order.desc("creationDate") );
		}
		
		return true;
	}
	
	private String getCircuitListDTOUniqueKey(CircuitListDTO listItem) {
		StringBuffer b = new StringBuffer( Long.toString(listItem.getStartItemId()) );
		b.append("___");
		b.append( Long.toString(listItem.getStartPortId()) );
		return b.toString();
	}
	
	private Set<Long> getCompositeItems(long itemId) {
		Set<Long> itemIds = new HashSet<Long>();

		if (itemId > 0) {
			itemIds.add( itemId );
			try {
				com.raritan.tdz.item.home.ItemObject itemObject = itemObjectFactory.getItemObject( itemId );

				if (itemObject != null && (itemObject instanceof CompositeItem)) {
					List<Item> childItems = ((CompositeItem)itemObject).getChildItems();
					for (Item child : childItems) {
						itemIds.add( child.getItemId() );
					}
				}
			}
			catch (DataAccessException e) {
				log.warn("", e);
			}
		}

		return itemIds;
	}
	
	/**
	 * 
	 * @param cCriteria
	 * @param circuitList
	 * @return
	 * @throws DataAccessException
	 */
	private List<CircuitListDTO> postProcessResults(CircuitCriteriaDTO cCriteria, CircuitResults circuitList) throws DataAccessException {
		List<CircuitListDTO> recList = new ArrayList<CircuitListDTO>( circuitList.size() );
		
		final Map<Long, CircuitRequestInfo> reqInfoData = circuitList.getReqInfoData();
		final Map<Long, CircuitRequestInfo> reqInfoPower = circuitList.getReqInfoPower();
		final Set<String> sharedDataCircuitTraces = circuitList.getSharedDataCircuitTraces();
		final Set<String> sharedPowerCircuitTraces = circuitList.getSharedPowerCircuitTraces();
		
		Set<String> visited = null;
		
		for (CircuitViewData r: circuitList.getList()) {
			CircuitListDTO cx = new CircuitListDTO();
			cx.setStartCabinetId(r.getCabinetId());
			cx.setStartCabinetName(r.getCabinetName());
			cx.setCircuitId( r.getCircuitUID().floatValue() );
			cx.setCircuitType(r.getCircuitType());
			cx.setComments(r.getComments());
			cx.setStartConnectorName(r.getConnectorName());
			cx.setCreatedBy(r.getCreatedBy());
			cx.setCreationDate(r.getCreationDate());
			cx.setStartLocationId(r.getLocationId());
			cx.setStartItemId(r.getStartItemId());
			cx.setStartItemName(r.getStartItemName());
			cx.setStartPortId(r.getStartPortId());
			cx.setStartPortName(r.getStartPortName());
			cx.setStatus(r.getStatus());

			cx.setEndCabinetName(r.getEndCabinetName());
			cx.setEndConnectorName(r.getEndConnectorName());
			cx.setEndItemId(r.getEndItemId());
			cx.setEndItemName(r.getEndItemName());
			cx.setEndPortId(r.getEndPortId());
			cx.setEndPortName(r.getEndPortName());
			cx.setStatusLksCode(r.getStatusLksCode());
			cx.setCreatedByTeam(r.getTeamDesc());
			cx.setCreatedByTeamId(r.getTeamId());
			cx.setLocationCode(r.getLocationCode());
			cx.setVisualCircuitTrace(r.getVisualCircuitTrace());

			final long circuitId = cx.getCircuitUID().getCircuitDatabaseId();
			
			if(r.getCircuitType() == SystemLookup.PortClass.DATA){
				cx.setCircuitTypeDesc(SystemLookup.PortClass.DATA_DESC);
				
				if(reqInfoData.get(circuitId) != null){
					CircuitRequestInfo req = reqInfoData.get(circuitId);
					cx.setRequestNumber(req.getRequestNo());
					cx.setRequestStage(req.getRequestStage());
					cx.setRequestStageLksCode(req.getRequestStageCode());
					cx.setProposeCircuitId(req.getProposeCircuitId());
					cx.setRequestId(req.getRequestId());
					cx.setRequestType(req.getRequestType());
				}

				cx.setPartialCircuitInUse( sharedDataCircuitTraces.contains( r.getCircuitTrace() ));
			}
			else{
				cx.setCircuitTypeDesc(SystemLookup.PortClass.POWER_DESC);
				
				if(reqInfoPower.get(circuitId) != null){
					CircuitRequestInfo req = reqInfoPower.get(circuitId);
					cx.setRequestNumber(req.getRequestNo());
					cx.setRequestStage(req.getRequestStage());
					cx.setRequestStageLksCode(req.getRequestStageCode());
					cx.setProposeCircuitId(req.getProposeCircuitId());
					cx.setRequestId(req.getRequestId());
					cx.setRequestType(req.getRequestType());
				}

				cx.setPartialCircuitInUse( sharedPowerCircuitTraces.contains( r.getCircuitTrace() ));
			}
			
			boolean okToAdd = true;
			final long itemId = cCriteria.getItemId() != null ? cCriteria.getItemId() : 0;
			final long startItemId = cx.getStartItemId() != null ? cx.getStartItemId() : 0;
			
			if (itemId > 0 && startItemId > 0) {
				if (!getCompositeItems(itemId).contains( startItemId )) {
					// Note: We are ONLY doing this for UPLINKS!
					// We swap the start and end information to ensure the correct perspective
					// in the UI when showing the uplinks for the end node of the circuit.
					cx.swapStartAndEnd();
					
					// Filter out duplicates as the result of swapping the start and end item names
					String key = getCircuitListDTOUniqueKey( cx );
					if (visited == null) {
						visited = new HashSet<String>();
					}
					
					if (visited.contains(key)) {
						okToAdd = false;
					}
					else {
						visited.add( key );
					}
				}
			}
			
			if (okToAdd) {
				recList.add( cx );
			}
		}
		
		if (circuitList.getRequiresPostSort()) {
			PostSortComparator cmp = new PostSortComparator(cCriteria.getSortColumn(), cCriteria.getSortOrderDesc());
			Collections.sort(recList, cmp);
		}
		
		return recList;
	}
	
	/**
	 * Columns that don't directly map to the CircuitViewData must be post sorted.
	 * These are assumed to be strings for comparison purposes.
	 */
	private final Set<String> postSortColumns = new HashSet<String>() {{
		add("requestStage");
		add("requestNumber");
		add("proposedString");
	}};
	
	/**
	 * Comparator for post hibernate sort of the DTO list.
	 */
	private class PostSortComparator implements Comparator<CircuitListDTO> {

		private Method sortGetter;
		private boolean sortOrderDesc;
		
		PostSortComparator(String sortColumn, boolean sortOrderDesc) {
			this.sortOrderDesc = sortOrderDesc;
			try {
//				sortGetter = CircuitListDTO.class.getMethod("get" + sortColumn);
				sortGetter = new PropertyDescriptor(sortColumn, CircuitListDTO.class).getReadMethod();
			} 
			catch (Throwable t) {
				log.error("Error post sorting circuit list", t);
				sortGetter = null;
			} 
		}
		
		@Override
		public int compare(CircuitListDTO o1, CircuitListDTO o2) {
			int cmp = 1;
			if (sortGetter == null) return cmp;
				
			String val1 = null;
			String val2 = null;
			
			try {
				val1 = (String)sortGetter.invoke(o1);
				val2 = (String)sortGetter.invoke(o2);
				val1 = val1 != null ? val1.toLowerCase() : "";
				val2 = val2 != null ? val2.toLowerCase() : "";
				
				if (sortOrderDesc) {
					cmp = val2.compareTo( val1 );
				}
				else {
					cmp = val1.compareTo( val2 );
				}
			}
			catch (Throwable t) {
				log.error("Error in post circuit list sort compare", t);
				cmp = 1;
			}
			
			// Even if values are equal, we can't treat the DTO's as equal!
			if (cmp == 0) cmp = -1;
			
			return cmp;
		}
	}
}
