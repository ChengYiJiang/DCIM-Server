/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.lookup.SystemLookup;

/**
 * @author prasanna
 * This provides a wrapper to the mapping of 
 * Mounting/Form Factor from model 
 * to Class/Subclass of an item
 */
public class ModelItemSubclassMap {
	
	private static final Map<ModelMountingFormfactor, Long> 
		modelItemSubclassMap =
			Collections.unmodifiableMap(new HashMap<ModelMountingFormfactor,Long>() {{
				put(new ModelMountingFormfactor("Device", "Blade", "*"),SystemLookup.SubClass.BLADE_SERVER);
				put(new ModelMountingFormfactor("Device", "Rackable", "Chassis"),SystemLookup.SubClass.BLADE_CHASSIS);
				put(new ModelMountingFormfactor("Device", "Rackable", "Fixed"),SystemLookup.SubClass.RACKABLE);
				put(new ModelMountingFormfactor("Device", "Rackable", "Fixed"),SystemLookup.SubClass.RACKABLE);
				put(new ModelMountingFormfactor("Device", "Free-Standing", "Fixed"),SystemLookup.SubClass.RACKABLE);
				put(new ModelMountingFormfactor("Device", "Non-Rackable", "Fixed"), SystemLookup.SubClass.RACKABLE);
				put(new ModelMountingFormfactor("Network", "Blade", "*"),SystemLookup.SubClass.BLADE);
				put(new ModelMountingFormfactor("Network", "Rackable", "Chassis"),SystemLookup.SubClass.CHASSIS);
				put(new ModelMountingFormfactor("Network", "Free-Standing", "Fixed"),SystemLookup.SubClass.NETWORK_STACK);
				put(new ModelMountingFormfactor("Network", "Non-Rackable", "Fixed"), SystemLookup.SubClass.NETWORK_STACK);
				put(new ModelMountingFormfactor("Network", "Rackable", "Fixed"),SystemLookup.SubClass.NETWORK_STACK);
				put(new ModelMountingFormfactor("Power Outlet", "Busway", "Fixed"),SystemLookup.SubClass.BUSWAY_OUTLET);
				put(new ModelMountingFormfactor("Power Outlet", "Non-Rackable", "Fixed"),SystemLookup.SubClass.WHIP_OUTLET);
				put(new ModelMountingFormfactor("Passive", "Rackable", "Fixed"), SystemLookup.SubClass.WIRE_MANAGER);
				put(new ModelMountingFormfactor("Passive", "Rackable", "Shelf"), SystemLookup.SubClass.SHELF);
				put(new ModelMountingFormfactor("Passive", "Rackable", "Blanking Plate"), SystemLookup.SubClass.BLANKING_PLATE);

				
			}});
	
	private SessionFactory sessionFactory;
	
	
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String getSubclass(String classLkpValue, String mounting, String formFactor){
		StringBuffer subClassBuffer = new StringBuffer();
		
		String _formFactor = formFactor;
		
		subClassBuffer.append(classLkpValue);
		
		if (mounting.equals("Blade"))
			_formFactor = "*";
		
		Long subclassValueCode = modelItemSubclassMap.get(new ModelMountingFormfactor(classLkpValue, mounting, _formFactor));
		if (subclassValueCode == null) return classLkpValue;
		
		LksData lksData = SystemLookup.getLksData(sessionFactory.getCurrentSession(), subclassValueCode);
		
		String subclass = lksData.getLkpValue();
		if (subclass != null){
			subClassBuffer.append(" / ");
			subClassBuffer.append(subclass);
		}
		
		return subClassBuffer.toString();
	}
	
	public Long getSubclassValueCode(String classLkpValue, String mounting, String formFactor){
		StringBuffer subClassBuffer = new StringBuffer();
		
		String _formFactor = formFactor;
		
		subClassBuffer.append(classLkpValue);
		
		if (mounting.equals("Blade"))
			_formFactor = "*";
		
		Long subclassValueCode = modelItemSubclassMap.get(new ModelMountingFormfactor(classLkpValue, mounting, _formFactor));
		return subclassValueCode;
	}
	
	private static class ModelMountingFormfactor{
		private String classLkpValue;
		private String mounting;
		private String formFactor;
		
		
		
		public ModelMountingFormfactor(String classLkpValue, String mounting,
				String formFactor) {
			super();
			this.classLkpValue = classLkpValue;
			this.mounting = mounting;
			this.formFactor = formFactor;
		}
		
		public String getClassLkpValue() {
			return classLkpValue;
		}
		public void setClassLkpValue(String classLkpValue) {
			this.classLkpValue = classLkpValue;
		}
		public String getMounting() {
			return mounting;
		}
		public void setMounting(String mounting) {
			this.mounting = mounting;
		}
		public String getFormFactor() {
			return formFactor;
		}
		public void setFormFactor(String formFactor) {
			this.formFactor = formFactor;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((classLkpValue == null) ? 0 : classLkpValue.hashCode());
			result = prime * result
					+ ((formFactor == null) ? 0 : formFactor.hashCode());
			result = prime * result
					+ ((mounting == null) ? 0 : mounting.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModelMountingFormfactor other = (ModelMountingFormfactor) obj;
			if (classLkpValue == null) {
				if (other.classLkpValue != null)
					return false;
			} else if (!classLkpValue.equals(other.classLkpValue))
				return false;
			if (formFactor == null) {
				if (other.formFactor != null)
					return false;
			} else if (!formFactor.equals(other.formFactor))
				return false;
			if (mounting == null) {
				if (other.mounting != null)
					return false;
			} else if (!mounting.equals(other.mounting))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "ModelMountingFormfactor [classLkpValue=" + classLkpValue
					+ ", mounting=" + mounting + ", formFactor=" + formFactor
					+ "]";
		}

		
		
	}
}
