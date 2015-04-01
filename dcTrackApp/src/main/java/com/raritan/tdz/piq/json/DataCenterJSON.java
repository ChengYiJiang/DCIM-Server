/**
 * 
 */
package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.DataCenterLocationDetails;

/**
 * @author prasanna
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DataCenterJSON  implements SitesJSON{
	
	DataCenter dataCenter;
	
	List<DataCenter> dataCenters;
	
	public static String objectType = "DATA_CENTER:";
	public static String externalKeyPrefix = "DCT Data Center -- "; 
	
	public DataCenterJSON(){
		super();
	}
	
	public DataCenterJSON(DataCenterLocationDetails location){
		dataCenter = new DataCenter();
		dataCenter.setName(location.getCode());
		dataCenter.setCompanyName(null);
		dataCenter.setContactName(location.getOwnerName());
		dataCenter.setContactPhone(null);
		dataCenter.setContactEmail(null);
	
		DataCenterLocaleDetails details = location.getDcLocaleDetails();
		
		if (details != null) {
			dataCenter.setCity(details.getCity());
			dataCenter.setCountry(details.getCountry());
			dataCenter.setState(details.getState());
		}
		
		dataCenter.setPeakKwhRate(null);
		dataCenter.setOffPeakKwhRate(null);
		dataCenter.setPeakBegin(null);
		dataCenter.setPeakEnd(null);
		dataCenter.setCo2Factor(null);
		dataCenter.setCustomField1(null);
		dataCenter.setCustomField2(null);
		dataCenter.setExternalKey(externalKeyPrefix + location.getDataCenterLocationId());
		dataCenter.setCoolingFactor(null);
		dataCenter.setCoolingSavings(null);
		dataCenter.setCapacity(null);
	}
	
	public String getExternalComplexKey() {
		return objectType + getExternalKey();
	}
	
	public DataCenterJSON(String id){
		dataCenter = new DataCenter();
		dataCenter.setId(id);
	}
	
	@JsonProperty(value="data_centers")
	public List<DataCenter> getDataCenters(){
		return dataCenters;
	}
	
	@JsonSetter(value="data_centers")
	public void setDataCenters(List<DataCenter> dataCenters){
		this.dataCenters = dataCenters;
	}
	
	@JsonProperty(value="data_center")
	public DataCenter getDataCenter() {
		return dataCenter;
	}

	@JsonSetter(value="data_center")
	public void setDataCenter(DataCenter data_center) {
		this.dataCenter = data_center;
	}

	@Override
	public String toString() {
		return "DataCenterJSON [data_center=" + dataCenter + "]";
	}
	
	@Override
	public String getId() {
		if (dataCenter != null) 
			return dataCenter.getId();
		
		return null;
	}
	
	@Override
	public String getExternalKey() {
		if (dataCenter != null) 
			return dataCenter.getExternalKey();
		
		return null;
	}
	
	public String getSiteId() {
		if (dataCenters != null && !dataCenters.isEmpty()) {
			return dataCenters.get(0).getId();
		}
		return null;
	}
	
	public Boolean isDataCenterInSync(DataCenterLocationDetails location) {
		if (dataCenter != null ) {
			return dataCenter.isDataCenterInSync(location);
		}
		
		return false;
	}
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class DataCenter {
		private String id;
		private String name;
		private String companyName;
		private String contactName;
		private String contactPhone;
		private String contactEmail;
		private String city;
		private String state;
		private String country;
		private Double peakKwhRate;
		private Double offPeakKwhRate;
		private Integer peakBegin;
		private Integer peakEnd;
		private Double co2Factor;
		private Double coolingFactor;
		private String customField1;
		private String customField2;
		private String externalKey;
		private String capacity;
		private String coolingSavings;
		private Double pueThresholdMinimum;
		private Double pueThresholdMaximum;
		
		public DataCenter(){
			super();
		}
		
		public Boolean isDataCenterInSync(DataCenterLocationDetails location){
			Boolean result = false;
			
			String genId = null;
			
			if (getId() != null && location.getPiqId() != null){
				String[] ids = location.getPiqId().split(":");
				String id = "";
				
				for (int l = 0; l < ids.length - 2; l++)
					id += ids[l] + ":";
				genId = id + "DataCenter:"+ getId();
			}
			
			Boolean contactNameInSync = location != null && isContactNameInSync(location.getOwnerName());
			Boolean cityInSync = location != null && location.getDcLocaleDetails() != null ? isCityInSync(location.getDcLocaleDetails().getCity()):true;
			Boolean countryInSync = location != null && location.getDcLocaleDetails() != null ? isCountryInSync(location.getDcLocaleDetails().getCountry()):true;
			
			//NOTE: We do not perform external key comparison since it breakes  when PIQ wizard from Windows client is involved in performing a sync. PIQ wizard cannot set
			//any value associated with the external key via ODBC. In future when we move the PIQ wizard part to webclient, this can be easily taken care.
			
			if (
					//TODO:There is a bug in Windows client and therefore I cannot compare the name for now.
				//(getName() != null && location.getCode() != null && getName().equalsIgnoreCase(location.getCode()))
				//&& 
				(genId != null && location.getPiqId() != null && genId.equals(location.getPiqId().toString()))
	//			&& (contactNameInSync)
	//			&& (cityInSync)
	//			&& (countryInSync)
	//			&& (getExternalKey() != null && getExternalKey().equals(externalKeyPrefix + location.getDataCenterLocationId()))
					)
			{
				result = true;
			}
			return result;
		}
		
		public Boolean isContactNameInSync(String contactName){
			return ((this.contactName == null && contactName == null) || (this.contactName != null && contactName != null && this.contactName.equals(contactName)));
		}
		
		public Boolean isCityInSync(String city){
			return ((this.city == null && city == null) || (this.city != null && city != null && this.city.equals(city)));
		}
		
		public Boolean isCountryInSync(String country){
			return ((this.country == null && country == null) || (this.country != null && country != null && this.country.equals(country)));
		}
		
		/**
		 * @return the id
		 */
		@JsonProperty(value="id")
		public String getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		@JsonSetter(value="id")
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the name
		 */
		@JsonProperty(value="name")
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		@JsonSetter(value="name")
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the companyName
		 */
		@JsonProperty(value="company_name")
		public String getCompanyName() {
			return companyName;
		}
		/**
		 * @param companyName the companyName to set
		 */
		@JsonSetter(value="company_name")
		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}
		/**
		 * @return the contactName
		 */
		@JsonProperty(value="contact_name")
		public String getContactName() {
			return contactName;
		}
		/**
		 * @param contactName the contactName to set
		 */
		@JsonSetter(value="contact_name")
		public void setContactName(String contactName) {
			this.contactName = contactName;
		}
		/**
		 * @return the contactPhone
		 */
		@JsonProperty(value="contact_phone")
		public String getContactPhone() {
			return contactPhone;
		}
		/**
		 * @param contactPhone the contactPhone to set
		 */
		@JsonSetter(value="contact_phone")
		public void setContactPhone(String contactPhone) {
			this.contactPhone = contactPhone;
		}
		/**
		 * @return the contactEmail
		 */
		@JsonProperty(value="contact_email")
		public String getContactEmail() {
			return contactEmail;
		}
		/**
		 * @param contactEmail the contactEmail to set
		 */
		@JsonSetter(value="contact_email")
		public void setContactEmail(String contactEmail) {
			this.contactEmail = contactEmail;
		}
		/**
		 * @return the city
		 */
		@JsonProperty(value="city")
		public String getCity() {
			return city;
		}
		/**
		 * @param city the city to set
		 */
		@JsonSetter(value="city")
		public void setCity(String city) {
			this.city = city;
		}
		/**
		 * @return the state
		 */
		@JsonProperty(value="state")
		public String getState() {
			return state;
		}
		/**
		 * @param state the state to set
		 */
		@JsonSetter(value="state")
		public void setState(String state) {
			this.state = state;
		}
		/**
		 * @return the country
		 */
		@JsonProperty(value="country")
		public String getCountry() {
			return country;
		}
		/**
		 * @param country the country to set
		 */
		@JsonSetter(value="country")
		public void setCountry(String country) {
			this.country = country;
		}
		/**
		 * @return the peakKwhRate
		 */
		@JsonProperty(value="peak_kwh_rate")
		public Double getPeakKwhRate() {
			return peakKwhRate;
		}
		/**
		 * @param peakKwhRate the peakKwhRate to set
		 */
		@JsonSetter(value="peak_kwh_rate")
		public void setPeakKwhRate(Double peakKwhRate) {
			this.peakKwhRate = peakKwhRate;
		}
		/**
		 * @return the offPeakKwhRate
		 */
		@JsonProperty(value="off_peak_kwh_rate")
		public Double getOffPeakKwhRate() {
			return offPeakKwhRate;
		}
		/**
		 * @param offPeakKwhRate the offPeakKwhRate to set
		 */
		@JsonSetter(value="off_peak_kwh_rate")
		public void setOffPeakKwhRate(Double offPeakKwhRate) {
			this.offPeakKwhRate = offPeakKwhRate;
		}
		/**
		 * @return the peakBegin
		 */
		@JsonProperty(value="peak_begin")
		public Integer getPeakBegin() {
			return peakBegin;
		}
		/**
		 * @param peakBegin the peakBegin to set
		 */
		@JsonSetter(value="peak_begin")
		public void setPeakBegin(Integer peakBegin) {
			this.peakBegin = peakBegin;
		}
		/**
		 * @return the peakEnd
		 */
		@JsonProperty(value="peak_end")
		public Integer getPeakEnd() {
			return peakEnd;
		}
		/**
		 * @param peakEnd the peakEnd to set
		 */
		@JsonSetter(value="peak_end")
		public void setPeakEnd(Integer peakEnd) {
			this.peakEnd = peakEnd;
		}
		/**
		 * @return the co2Factor
		 */
		@JsonProperty(value="co2_factor")
		public Double getCo2Factor() {
			return co2Factor;
		}
		/**
		 * @param co2Factor the co2Factor to set
		 */
		@JsonSetter(value="co2_factor")
		public void setCo2Factor(Double co2Factor) {
			this.co2Factor = co2Factor;
		}
		/**
		 * @return the coolingFactor
		 */
		@JsonProperty(value="cooling_factor")
		public Double getCoolingFactor() {
			return coolingFactor;
		}
		/**
		 * @param coolingFactor the coolingFactor to set
		 */
		@JsonSetter(value="cooling_factor")
		public void setCoolingFactor(Double coolingFactor) {
			this.coolingFactor = coolingFactor;
		}
		/**
		 * @return the customField1
		 */
		@JsonProperty(value="custom_field_1")
		public String getCustomField1() {
			return customField1;
		}
		/**
		 * @param customField1 the customField1 to set
		 */
		@JsonSetter(value="custom_field_1")
		public void setCustomField1(String customField1) {
			this.customField1 = customField1;
		}
		/**
		 * @return the customField2
		 */
		@JsonProperty(value="custom_field_2")
		public String getCustomField2() {
			return customField2;
		}
		/**
		 * @param customField2 the customField2 to set
		 */
		@JsonSetter(value="custom_field_2")
		public void setCustomField2(String customField2) {
			this.customField2 = customField2;
		}
		/**
		 * @return the externalKey
		 */
		@JsonProperty(value="external_key")
		public String getExternalKey() {
			return externalKey;
		}
		/**
		 * @param externalKey the externalKey to set
		 */
		@JsonSetter(value="external_key")
		public void setExternalKey(String externalKey) {
			this.externalKey = externalKey;
		}
		/**
		 * @return the capacity
		 */
		@JsonProperty(value="capacity")
		public String getCapacity() {
			return capacity;
		}
		/**
		 * @param capacity the capacity to set
		 */
		@JsonSetter(value="capacity")
		public void setCapacity(String capacity) {
			this.capacity = capacity;
		}
		/**
		 * @return the coolingSavings
		 */
		@JsonProperty(value="cooling_savings")
		public String getCoolingSavings() {
			return coolingSavings;
		}
		/**
		 * @param coolingSavings the coolingSavings to set
		 */
		@JsonSetter(value="cooling_savings")
		public void setCoolingSavings(String coolingSavings) {
			this.coolingSavings = coolingSavings;
		}
		/**
		 * @return the pueThresholdMinimum
		 */
        @JsonProperty(value="pue_threshold_minimum")
		public Double getPueThresholdMinimum() {
			return pueThresholdMinimum;
		}
        /**
         * @param pueThresholdMinimum
         */
        @JsonSetter(value="pue_threshold_minimum")
		public void setPueThresholdMinimum(Double pueThresholdMinimum) {
			this.pueThresholdMinimum = pueThresholdMinimum;
		}
        /**
         * @return the pueThresholdMaximum
         */
		@JsonProperty(value="pue_threshold_maximum")
		public Double getPueThresholdMaximum() {
			return pueThresholdMaximum;
		}
		/**
		 * @param pueThresholdMaximum
		 */
		@JsonSetter(value="pue_threshold_maximum")
		public void setPueThresholdMaximum(Double pueThresholdMaximum) {
			this.pueThresholdMaximum = pueThresholdMaximum;
		}

		@Override
		public String toString() {
			return "DataCenter [id=" + id + ", name=" + name + ", companyName="
					+ companyName + ", contactName=" + contactName
					+ ", contactPhone=" + contactPhone + ", contactEmail="
					+ contactEmail + ", city=" + city + ", state=" + state
					+ ", country=" + country + ", peakKwhRate=" + peakKwhRate
					+ ", offPeakKwhRate=" + offPeakKwhRate + ", peakBegin="
					+ peakBegin + ", peakEnd=" + peakEnd + ", co2Factor="
					+ co2Factor + ", coolingFactor=" + coolingFactor
					+ ", customField1=" + customField1 + ", customField2="
					+ customField2 + ", externalKey=" + externalKey
					+ ", capacity=" + capacity + ", coolingSavings="
					+ coolingSavings + ", pueThresholdMinimum="
					+ pueThresholdMinimum + ", pueThresholdMaximum="
					+ pueThresholdMaximum + "]";
		}
		
	}
}
