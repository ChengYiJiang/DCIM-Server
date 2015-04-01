package com.raritan.tdz.item.rulesengine;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.Modifier;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;
import static org.testng.Assert.assertNotNull;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiValueIdField;
import com.raritan.tdz.field.service.FieldService;
import com.raritan.tdz.item.service.ItemService;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty;
import com.raritan.tdz.rulesengine.RulesProcessor;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

public class RulesProcessorBaseTest extends TestBase{
	RulesProcessor rulesProcessor;
	RemoteRef remoteRefItemScreen;
    
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		rulesProcessor = (RulesProcessor) ctx.getBean("itemRulesProcessor");
		remoteRefItemScreen = (RemoteRef)ctx.getBean("remoteRefItemScreen");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}
	
  @Test
  public void testGetValidUiId() throws Throwable {

	 List<String> uiIds = rulesProcessor.getValidUiId(100L);
	 
	 AssertJUnit.assertTrue(uiIds.size() > 0);
	  
  }
  
  @Test
  public void testIsValidUiId() throws Throwable {
	  AssertJUnit.assertTrue(rulesProcessor.isValidUiIdForUniqueValue("cmbModel", 103L));
	  AssertJUnit.assertFalse(rulesProcessor.isValidUiIdForUniqueValue("cmbMake", 100L));
  }
  
  @Test
  public void testGetDefaultValues() throws Throwable {
	  List<UiComponent> components = rulesProcessor.getComponentsWithDefaults(102L);
	  
	  AssertJUnit.assertTrue(components.size() > 0);
  }
  
  @Test
  public void testGetValidUiIdNullUniqueValue() throws Throwable {
	  List<String> components = rulesProcessor.getValidUiId(null);
	  
	  AssertJUnit.assertTrue(components.size() > 0);
  }
  
  //@Test
  public void testGenerateUIIdFieldType() throws Throwable {
	  Set<String> unionResultSet = new HashSet<String>();
	  Field[] fields = SystemLookup.ModelUniqueValue.class.getDeclaredFields();
	  for (Field f : fields){
		  if (Modifier.isStatic(f.getModifiers())){
			 Long l = f.getLong(null);
			 List<String> uiIds = rulesProcessor.getValidUiId(l);
			 unionResultSet.addAll(uiIds);
		  }
	  }
	  
	  for (String uiId: unionResultSet){
		  String remoteRef = rulesProcessor.getRemoteRef(uiId);
		  String remoteType = remoteRefItemScreen.getRemoteType(remoteRef);
		  String remoteAlias = remoteRefItemScreen.getRemoteAlias(remoteRef, RemoteRefConstantProperty.FOR_ID);
		  if (remoteAlias == null){
			  remoteAlias = remoteRefItemScreen.getRemoteAlias(remoteRef, RemoteRefConstantProperty.FOR_VALUE);
		  }
		  ObjectTracer tracer = getFieldTrace(remoteType, remoteAlias);
		  List<Field> trace = tracer != null ? tracer.traceObject(Class.forName(remoteType), remoteAlias) : null;
		  String type = "";
		  if (trace != null){
			  Field lastField = trace.get(trace.size() - 1);
			  type = lastField != null ? lastField.getType().getName() : "";
		  }
		  StringBuffer buff = new StringBuffer();
		  buff.append(uiId);
		  buff.append(", ");
		  buff.append(type);
		  buff.append(", ");
		  buff.append(remoteType != null ? remoteType : "");
		  buff.append(", ");
		  buff.append(trace != null && tracer != null ? tracer.toString() : "");
		  System.out.println(buff.toString());
	  }
  }
  
	private ObjectTracer getFieldTrace(String remoteType, String fieldName) throws ClassNotFoundException{
		if (fieldName == null) return null;
		if (remoteType == null) return null;
		
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		return (objectTrace);
	}
}
