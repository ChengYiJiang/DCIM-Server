package com.raritan.tdz.views;

public class Item {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "{\"name\": \""+name+"\"}";
	}
}
