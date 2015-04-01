package com.raritan.tdz.floormaps.dto;

import java.util.*;

public class CadHandleDTO  implements java.io.Serializable {

	private static final long serialVersionUID = 2636137755639081350L;

	public CadHandleDTO() {
	
	}

    private String locationId;

	private List<Map> data;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String id) {
        this.locationId = id;
    }
	
	public List<Map> getData() {
		return data;
	}
	
	public void setData(List<Map> data) {
		this.data=data;
	}
}
