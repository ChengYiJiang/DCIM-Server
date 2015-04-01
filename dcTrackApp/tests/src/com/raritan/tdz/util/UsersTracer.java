/**
 * 
 */
package com.raritan.tdz.util;

import java.lang.reflect.Field;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.tests.TestBase;

/**
 * @author bozana
 *
 */
public class UsersTracer extends TestBase {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#setUp()
	 */

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.tests.TestBase#tearDown()
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}



	private void addTracerHandlers(ObjectTracer oTracer) {

		oTracer.addHandler( "^[a-z].*LkpValue", new LksDataValueTracerHandler() );
		oTracer.addHandler( "^[a-z].*LkuValue", new LkuDataValueTracerHandler() );
		oTracer.addHandler( "^[a-z].*LkpValueCode", new LksDataValueTracerHandler() );
		oTracer.addHandler( "^[a-z].*LkuValueCode", new LkuDataValueTracerHandler() );
		oTracer.addHandler( "itemAdminUser.*", new RestAPIUserIdTracerHandler() );
		oTracer.addHandler( "parentItem.*", new ParentTracerHandler() );
		oTracer.addHandler( "bladeChassis.*", new BladeChassisTracerHandler() );
		oTracer.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());

	}

	
	@Test
	public void test1() throws ClassNotFoundException
	{
		String uiId = "cmbSystemAdmin";
		String value="samern";
		String entity;
		String property;
		String propertyId;
		boolean validObject = false;
		RulesProcessor rulesProcessor = (RulesProcessor) ctx.getBean("itemRulesProcessor");
		RemoteRef remoteReference = (RemoteRef) ctx.getBean("remoteRefItemScreen");

		propertyId = remoteReference.getRemoteId(rulesProcessor.getRemoteRef(uiId));

		property = remoteReference.getRemoteAlias(rulesProcessor.getRemoteRef(uiId), RemoteRef.RemoteRefConstantProperty.FOR_VALUE);	
		entity =  remoteReference.getRemoteType(rulesProcessor.getRemoteRef(uiId));

		if( entity == null || property == null ){
			System.out.println("---Cannot find any property and entity corresponding to " + uiId);
			return;	
		}

		Class<?> entityClass = Class.forName(entity);	    		
		ObjectTracer oTracer = new ObjectTracer();
		addTracerHandlers(oTracer);

		List<Field>  fields =  oTracer.traceObject(entityClass, property);
		int numFields = fields.size();
		if( numFields < 1){
			return;
		}

		System.out.println("== entityName=" + entity + ", entityProperty=" + property + ", entityPropertyId=" + propertyId);
		for( int i=0; i<numFields; i++){
			System.out.println("### field[" + i + "]=" + fields.get(i).getDeclaringClass().getName());
		}

		Field correctField = fields.get(numFields-1);
		entity = correctField.getDeclaringClass().getName();

		if( correctField.getDeclaringClass().equals(LksData.class)){
			property = "lkpValue";
			propertyId = "lkpValueCode";
		}else if( correctField.getDeclaringClass().equals(LkuData.class)){
			property = "lkuValue";
			propertyId = "lkuId";
		}

		validObject = true;
		
		System.out.println("== actual values: entity=" + entity + ", property=" + property + 
					", propertyId=" + propertyId + ", validObject=" + validObject);
		Criteria criteria = sf.getCurrentSession().createCriteria(entity);
		List<String> aliases  = ObjectTracer.getAliases(property);
		for( String alias: aliases){
			String alias_name = alias.replace(".", "_");
			criteria.createAlias(alias, alias_name);
		}
		criteria.add(Restrictions.eq(property, value));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property(propertyId));
        criteria.setProjection(proList);
        List list = criteria.list();
        if( list.size() < 1 ){
        	//Assert(0);
        	System.out.println("#########FAILED");
        	org.testng.Assert.fail();
        } else{
        	System.out.println("### Got: " + (Long)list.get(0));
        }
	}
}