/**
 * 
 */
package com.raritan.tdz.dto;

import java.util.ArrayList;
import java.util.List;

import com.raritan.dctrack.xsd.AltUiValueIdFieldMap;
import com.raritan.dctrack.xsd.UiComponent;

/**
 * @author prasanna
 *
 */
public class UiComponentDTO extends UiComponent {
	
	private Boolean isConfigurable;
	private String panelId;
	private int sortOrder;
	
	public UiComponentDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Boolean getEditable(){
		return editable;
	}
	
	public Boolean getLockStatus(){
		return lockStatus;
	}
	
	public Boolean getRequired(){
		return required;
	}

	public Boolean getIsConfigurable() {
		return isConfigurable;
	}

	public void setIsConfigurable(Boolean isConfigurable) {
		this.isConfigurable = isConfigurable;
	}

	public String getPanelId() {
		return panelId;
	}

	public void setPanelId(String panelId) {
		this.panelId = panelId;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

    public void setAltUiValueIdFieldMap(List<AltUiValueIdFieldMap> altUiValueIdFieldMap) {
       this.altUiValueIdFieldMap = altUiValueIdFieldMap;
    }
}
