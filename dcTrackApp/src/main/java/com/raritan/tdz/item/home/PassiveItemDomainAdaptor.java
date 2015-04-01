package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.DataCenterLocationDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ValueIDToDomainAdaptor;
//import org.hibernate.criterion.Criterion;

public class PassiveItemDomainAdaptor implements ValueIDToDomainAdaptor {

	private static Logger log = Logger.getLogger("PassiveItemDomainAdaptor");
	
	private SessionFactory sessionFactory;
	
	
    private MapBindingResult validationErrors = null;

    public PassiveItemDomainAdaptor(SessionFactory sessionFactory) {
    	this.sessionFactory = sessionFactory;
    }
    
    
    @Override
    public Object convert(Object dbObject, List<ValueIdDTO> valueIdDTOList,
            String unit) throws BusinessValidationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException,
            DataAccessException {

        // Domain object
        Item item = (Item)dbObject;

        Session session = this.sessionFactory.getCurrentSession();

        String userName = "";
        for (ValueIdDTO dto : valueIdDTOList) {
            switch (dto.getLabel()) {

            case "tiPassiveItemName":
                item.setItemName((String) dto.getData());
                break;

            case "cmbPassiveUPosition":
                item.setuPosition( getLongFromCode(dto.getData()));
                break;

            case "radioPassiveRailsUsed":
                item.setMountedRailLookup(SystemLookup.getLksData(session, getLongFromCode(dto.getData())));
                break;

            case "tiPassiveClassId":
                item.setClassLookup((LksData)loadData(LksData.class, "lksId", getLongFromCode(dto.getData())));
                break;

            case "tiPassiveSubClassId":
                item.setSubclassLookup((LksData)loadData(LksData.class, "lksId", getLongFromCode(dto.getData())));
                break;

            case "tiPassiveLocationId":
                DataCenterLocationDetails dataCenterLocation = new DataCenterLocationDetails();
                dataCenterLocation.setDataCenterLocationId( getLongFromCode(dto.getData()) );
                item.setDataCenterLocation(dataCenterLocation);
                break;

            case "tiCabinet":
            	Item parentItem = (Item)loadData(Item.class, "itemId", getLongFromCode(dto.getData()));
                item.setParentItem(parentItem);
                break;

            case "cmbPassiveModel":
                Long modelDetailId = getLongFromCode(dto.getData());
                ModelDetails model = (ModelDetails)loadData(ModelDetails.class, "modelDetailId", modelDetailId);
                item.setModel(model);
                break;

            case "userName":
                userName = dto.getData().toString();
                break;
            }
        }

        // ItemDetails
        ItemServiceDetails itemDetails = new ItemServiceDetails();
        itemDetails.setSysCreatedBy(userName);

        // Default values for inserting a new passive item
        if (item.getItemId() <= 0) {

            // Set default facing to Front
            item.setFacingLookup(SystemLookup.getLksData(session, SystemLookup.Orientation.ITEM_FRONT_FACES_CABINET_FRONT));

            // Set default status to Installed
            item.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));

            // Set default origin to Client
            itemDetails.setOriginLookup(SystemLookup.getLksData(session, SystemLookup.ItemOrigen.CLIENT));
        }

        item.setItemServiceDetails(itemDetails);

        return item;
    }

    private Long getLongFromCode(Object code){
    	Double did = null;
    	Long id = null;
    	try{
    		did = Double.parseDouble(code.toString());
    		id = did.longValue();
    	}catch(NumberFormatException nf){
    		id = null;
    	}catch(Exception e){
    		id = null;
    	}
    	return id;
    }    
    
    @Override
    public MapBindingResult getValidationErrors() {
        return validationErrors;
    }
    
	public Object loadData(Class type, String fieldName, Long id) throws ClassNotFoundException {
		Object result = null;

		if(id != null){
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(type);
			criteria.add(Restrictions.eq(fieldName, id));
			result = criteria.uniqueResult();
		}
		return result;
	}    
}