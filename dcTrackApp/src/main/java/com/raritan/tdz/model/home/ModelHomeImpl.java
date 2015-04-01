/**
 * 
 */
package com.raritan.tdz.model.home;

 import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.raritan.dctrack.xsd.UiView;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDataPorts;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.ModelMfrDetails;
import com.raritan.tdz.domain.ModelPowerPorts;
import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.dto.PowerPortDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dto.ModelDTO;
import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.FilterHQLImpl;
import com.raritan.tdz.rulesengine.RulesProcessor;


/**
 * @author prasanna
 *
 */
public class ModelHomeImpl extends com.raritan.tdz.home.ModelHomeImpl implements ModelHome {
	
	private SessionFactory sessionFactory;
	private RulesProcessor rulesProcessor;

	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	public ModelHomeImpl(SessionFactory sessionFactory){
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
	}

	public RulesProcessor getRulesProcessor() {
		return rulesProcessor;
	}

	public void setRulesProcessor(RulesProcessor rulesProcessor) {
		this.rulesProcessor = rulesProcessor;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.home.ModelHome#getAllMake()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ValueIdDTO> getAllMakes() {
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelMfrDetails.class);
		
		proList.add(Projections.property("modelMfrDetailId"), "data");
		proList.add(Projections.property("mfrName"), "label");
		
		criteria.setProjection(proList);
		criteria.addOrder(Order.asc("mfrName"));
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<ValueIdDTO> getAllModels() {
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"), "data");
		proList.add(Projections.property("modelName"), "label");
		criteria.addOrder(Order.asc("modelName"));

		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}
	
	@Override
	public List<ValueIdDTO> getModels(Long mfrId, List<Long> includeClasses, List<Long> excludeClasses) {
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"), "data");
		proList.add(Projections.property("modelName"), "label");
		criteria.createAlias("modelMfrDetails","modelMfrDetails");
		criteria.addOrder(Order.asc("modelName"));
		
		if (mfrId != null) {
			criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		}
		
		addItemClassCriteria(criteria, false, includeClasses, excludeClasses);
		
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}
	
	@Override
	public List<ModelDTO> getAllModelsForMake(long mfrId, List<Long> includeClasses, List<Long> excludeClasses) {
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("classLookup","classLookup");
		criteria.createAlias("modelMfrDetails","modelMfrDetails");
		
		proList.add(Projections.property("modelDetailId"), "modelId");
		proList.add(Projections.property("modelName"), "modelName");
		proList.add(Projections.property("ruHeight"), "ruHeight");
		proList.add(Projections.property("classLookup.lksId"), "classLksId");
		proList.add(Projections.property("mounting"), "mounting");
		proList.add(Projections.property("formFactor"), "formFactor");
		proList.add(Projections.property("psredundancy"),"psredundancy");
		proList.add(Projections.property("isStandard"), "isStandard");
		
		criteria.addOrder(Order.asc("modelName"));
		
		if (mfrId >= 0) {
			criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		}
        		
		addItemClassCriteria(criteria, true, includeClasses, excludeClasses);
        
        // Filtered out the deleted models
        criteria.add(Restrictions.not(Restrictions.eq("statusLookup.lksId", new Long(2))));		

		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ModelDTO.class));
		
		return criteria.list();
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.model.home.ModelHome#getModelDetails(java.lang.Long)
	 */
	@Override
	public Map<String, UiComponentDTO> getModelDetails(Long modelId, UserInfo userInfo)
				throws Throwable {
		
		Map<String, Filter> filterMap = getFilterMap(modelId);
		
		Map<String, UiComponentDTO> dtos = new HashMap<String, UiComponentDTO>();
		UiView uiView = rulesProcessor.getData("uiView[@uiId='itemView']/uiViewPanel[@uiId='hardwarePanel']/", filterMap, "modelDetailId", modelId, "=", new LongType(), ((userInfo != null) ? userInfo.getUnits() : "1"));
		JXPathContext jc = JXPathContext.newContext(uiView);
		List<UiComponentDTO> componentListHWPanel = jc.selectNodes("uiViewPanel[@uiId='hardwarePanel']/uiViewComponents/uiViewComponent");
		
		for (UiComponentDTO uiViewComponent : componentListHWPanel){
			if (!uiViewComponent.getUiId().contains("Make") && !uiViewComponent.getUiId().contains("Model"))
				dtos.put(uiViewComponent.getUiId(), uiViewComponent);
		}
		
		uiView = rulesProcessor.getData("uiView[@uiId='itemView']/uiViewPanel[@uiId='dataPorts']/", filterMap, "modelDetailId", modelId, "=", new LongType(), ((userInfo != null) ? userInfo.getUnits() : "1"));
		jc = JXPathContext.newContext(uiView);

		List<UiComponentDTO> componentListDPPanel = jc.selectNodes("uiViewPanel[@uiId='dataPorts']/uiViewComponents/uiViewComponent");
		
		for (UiComponentDTO uiViewComponent : componentListDPPanel){
			if (!uiViewComponent.getUiId().contains("Make") && !uiViewComponent.getUiId().contains("Model"))
				dtos.put(uiViewComponent.getUiId(), uiViewComponent);
		}
		
		uiView = rulesProcessor.getData("uiView[@uiId='itemView']/uiViewPanel[@uiId='powerPorts']/", filterMap, "modelDetailId", modelId, "=", new LongType(), ((userInfo != null) ? userInfo.getUnits() : "1"));
		jc = JXPathContext.newContext(uiView);
		List<UiComponentDTO> componentListPPPanel = jc.selectNodes("uiViewPanel[@uiId='powerPorts']/uiViewComponents/uiViewComponent");
		
		for (UiComponentDTO uiViewComponent : componentListPPPanel){
			if (!uiViewComponent.getUiId().contains("Make") && !uiViewComponent.getUiId().contains("Model"))
				dtos.put(uiViewComponent.getUiId(), uiViewComponent);
		}
		
		return dtos;
	}

	@Override
	public ModelDetails createModel(ModelDetails modelDetails) {
		Session session = sessionFactory.getCurrentSession();
		
		ModelDetails model = (ModelDetails)session.merge(modelDetails);		
		session.flush();
		
		return model;
	}

	@Override
	public Long getModelIdByName(String modelName) {
		Long retval = -1L;
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"));
		criteria.add(Restrictions.eq("modelName", modelName));		
		criteria.setProjection(proList);		
		List<Long>list = criteria.list();
		Assert.isTrue(list.size() <= 1);
		if( list.size() == 1) retval = list.get(0);
		return retval;

	}

	@Override
	public ModelDetails getModelByName(String modelName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelName", modelName));		
		List<ModelDetails>list = criteria.list();
		ModelDetails model = null;
		
		Assert.isTrue(list.size() <= 1);
		
		if( list.size() == 1) model = (ModelDetails)list.get(0);
		
		return model;

	}
	
	
	@Override
	public ModelDetails getModelById(Long modelId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelDetailId", modelId));		
		List<ModelDetails>list = criteria.list();
		ModelDetails model = null;
		
		Assert.isTrue(list.size() <= 1);
		
		if( list.size() == 1) model = (ModelDetails)list.get(0);
		
		return model;

	}
	
	
	@SuppressWarnings("unchecked")
	private List<ModelDataPorts> getModelDataPorts(long modelId) throws DataAccessException {
		if (modelId <= 0) return null;
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDataPorts.class);
		criteria.createAlias("model", "model");
		criteria.createAlias("portSubclassLookup", "portSubclassLookup");
		criteria.add(Restrictions.eq("model.modelDetailId", modelId));
		criteria.add(Restrictions.isNotNull("portName"));
		criteria.add(Restrictions.isNotNull("mediaLookup"));
		criteria.add(Restrictions.isNotNull("protocolLookup"));
		criteria.add(Restrictions.isNotNull("speedLookup"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("model.modelDetailId"));
//CR Number: 49166 and 49228. We do not filter the physical ports if there are no connector lookup. Three lines of code commented
//		Criterion connCond1 = Restrictions.isNotNull("connectorLookup");
//		Criterion connCond2 = Restrictions.eq("portSubclassLookup.lkpValueCode", SystemLookup.PortSubClass.LOGICAL);
//		criteria.add(Restrictions.or(connCond1, connCond2));

		criteria.addOrder(Order.asc("portName"));
		return criteria.list();							
	}

	public List<DataPortDTO> getAllDataPort(long modelId) throws DataAccessException {
		List<ModelDataPorts> modelPorts = getModelDataPorts( modelId);
		List<DataPortDTO> dtos = null;
		
		if( modelPorts != null ){
			dtos = new ArrayList<DataPortDTO>();
			for (ModelDataPorts port: modelPorts) {
				dtos.add(ModelPortAdapter.adaptModelPortsDomainToDataPortDTO(port));
			}
		}//if
		return dtos;
	}

	@SuppressWarnings("unchecked")
	private List<ModelPowerPorts> getModelPowerPorts(long modelId) throws DataAccessException {
		if (modelId <= 0) return null;
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelPowerPorts.class);
		criteria.createAlias("model", "model");
		criteria.createAlias("portSubclassLookup", "portSubclassLookup");
		criteria.add(Restrictions.eq("model.modelDetailId", modelId));
		criteria.add(Restrictions.isNotNull("portName"));
		criteria.add(Restrictions.isNotNull("phaseLookup"));
		criteria.add(Restrictions.isNotNull("voltsLookup"));
		Criterion connCond1 = Restrictions.isNotNull("connectorLookup");
		Criterion connCond2 = Restrictions.eq("portSubclassLookup.lkpValueCode", SystemLookup.PortSubClass.LOGICAL);
		criteria.add(Restrictions.or(connCond1, connCond2));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("model.modelDetailId"));
		criteria.addOrder(Order.asc("portName"));
		return criteria.list();							
	}

	public List<PowerPortDTO> getAllPowerPort(long modelId) throws DataAccessException {
		List<ModelPowerPorts> modelPorts = getModelPowerPorts(modelId);
		List<PowerPortDTO> dtos = null;
		
		if( modelPorts != null ){
			dtos = new ArrayList<PowerPortDTO>();
			Long classLookupValueCode = getClassLkpValueCode(modelId);
		
			for (ModelPowerPorts port: modelPorts) {
				dtos.add(ModelPortAdapter.adaptModelPortsDomainToPowerPortDTO(port, classLookupValueCode));
			}
		}//if
		return dtos;
	}

	/**
	 * This may be removed once we have the seperate tables for model ports 
	 * which contain subclass.
	 * @param modelId
	 * @return
	 */
	private Long getClassLkpValueCode(long modelId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("classLookup", "classLookup");
		criteria.setProjection(Projections.property("classLookup.lkpValueCode"));
		criteria.add(Restrictions.eq("modelDetailId", modelId));
		
		Long classLookupValueCode = (Long) criteria.uniqueResult();
		return classLookupValueCode;
	}

	//
	// Private methods
	//
	
	private void addItemClassCriteria(Criteria c, boolean addedClassLookupAlias, List<Long> includeClasses, List<Long> excludeClasses) {
		
		if (includeClasses != null && !includeClasses.isEmpty()) {
			if (!addedClassLookupAlias) c.createAlias("classLookup","classLookup");
			addedClassLookupAlias = true;
			c.add( Restrictions.in("classLookup.lkpValueCode", includeClasses) );
		}
			
		if (excludeClasses != null && !excludeClasses.isEmpty()) {
			if (!addedClassLookupAlias) c.createAlias("classLookup","classLookup");
			c.add( Restrictions.not( Restrictions.in("classLookup.lkpValueCode", excludeClasses) ) );
		}
	}
	
	private Map<String, Filter> getFilterMap(Long modelDetailId) {
		Filter filterForItem = FilterHQLImpl.createFilter();
		filterForItem.eq("modelDetailId",modelDetailId);
		
		Map<String,Filter> filterMap = new HashMap<String, Filter>();
		
		filterMap.put("main",filterForItem);
		return filterMap;
	}
	
	@Override
	public ModelDetails createPhatomCabinetModel(ModelDetails itemModel) {
		StringBuffer cabModelName = new StringBuffer();
		cabModelName.append(itemModel.getRuHeight());
		cabModelName.append("RU-CABINET-");
		cabModelName.append(itemModel.getModelMfrDetails().getMfrName());
		cabModelName.append("-");
		cabModelName.append(itemModel.getDimW());
		cabModelName.append("x");
		cabModelName.append(itemModel.getDimD());

		ModelDetails cabinetModel = getModelByName(cabModelName.toString());
		
		if (cabinetModel == null) {
			cabinetModel = (ModelDetails) itemModel.clone();
			cabinetModel.setModelDetailId(null);
			cabinetModel.setModelName(cabModelName.toString());
			cabinetModel.setWeight(0);
			cabinetModel.setFormFactor("4-Post Enclosure");
			cabinetModel.setClassLookup(systemLookupFinderDAO.findByLkpValueCode(
					SystemLookup.Class.CABINET).get(0));
			
			cabinetModel = createModel(cabinetModel);
		}
		
		return cabinetModel;
	}
	
	@Override
	public void deleteModel(ModelDetails model){
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		//find out if there are items using this model
		Criteria criteria = session.createCriteria(Item.class);
		criteria.createAlias("model","model");
		criteria.add(Restrictions.eq("model.modelDetailId", model.getModelDetailId()));
		
		proList.add(Projections.property("itemName"), "itemName");		
		criteria.setProjection(proList);
		
		//TODO: Need to check if the model has ports, delete port first
		if(criteria.list().size() == 0){ //delete model
			session.delete(model);
		}	
	}

    @Override
    public List<ModelDTO> getPassiveModels() {
            Session session = sessionFactory.getCurrentSession();
            ProjectionList proList = Projections.projectionList();

            Criteria criteria = session.createCriteria(ModelDetails.class);
            criteria.createAlias("classLookup","classLookup");
            criteria.createAlias("modelMfrDetails","modelMfrDetails");

            proList.add(Projections.property("modelDetailId"), "modelId");
            proList.add(Projections.property("modelName"), "modelName");
            proList.add(Projections.property("ruHeight"), "ruHeight");
            proList.add(Projections.property("classLookup.lksId"), "classLksId");
            proList.add(Projections.property("mounting"), "mounting");
            proList.add(Projections.property("formFactor"), "formFactor");
            // proList.add(Projections.property("psredundancy"),"psredundancy");
            proList.add(Projections.property("isStandard"), "isStandard");
            proList.add(Projections.property("modelMfrDetails.modelMfrDetailId"), "makeId");
            proList.add(Projections.property("modelMfrDetails.mfrName"), "makeName");

            criteria.addOrder(Order.asc("modelName"));

            // Query Passive models only
            List<Long> includeClasses = new ArrayList<Long>();
            includeClasses.add(SystemLookup.Class.PASSIVE);
            addItemClassCriteria(criteria, true, includeClasses, new ArrayList<Long>());

            // Filtered out the deleted models
            criteria.add(Restrictions.not(Restrictions.eq("statusLookup.lksId", new Long(2))));

            criteria.setProjection(proList);
            criteria.setResultTransformer(Transformers.aliasToBean(ModelDTO.class));

            List<ModelDTO> list = criteria.list();

            // US2490 Set class and sub-class names for Cabinet Passive Items panel
            LksData classLksData = SystemLookup.getLksData(session, SystemLookup.Class.PASSIVE);
            for (ModelDTO dto : list) {
            	// class name
                dto.setClassName(classLksData.getLkpValue());
                dto.setClassLkpValueCode(classLksData.getLkpValueCode());

                // sub-class code
                if (SystemLookup.passiveFormFactorSubClassMap.containsKey(dto.getFormFactor())) {
                    dto.setSubClassLkpValueCode(SystemLookup.passiveFormFactorSubClassMap.get(dto.getFormFactor()));
                } else {
                    // default: Stardand
                    dto.setSubClassLkpValueCode(SystemLookup.SubClass.WIRE_MANAGER);
                }

                // sub-class id and name
                dto.setSubClassLksId(SystemLookup.getLksDataId(session, dto.getSubClassLkpValueCode()));
                dto.setSubClassName(SystemLookup.getLksData(session, dto.getSubClassLkpValueCode()).getLkpValue());
            }

            return list;
    }
}