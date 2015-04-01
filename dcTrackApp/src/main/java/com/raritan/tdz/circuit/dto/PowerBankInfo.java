/**
 * 
 */
package com.raritan.tdz.circuit.dto;

/**
 * @author prasanna
 *
 */
public class PowerBankInfo {
	public Long ups_bank_item_id;
	public String bank;
	public Long units;
	public String redundancy;
	public Long rating_kva;
	public Long rating_kw;
	public Long rating_v;
	public Long getUps_bank_item_id() {
		return ups_bank_item_id;
	}
	public void setUps_bank_item_id(Long ups_bank_item_id) {
		this.ups_bank_item_id = ups_bank_item_id;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public Long getUnits() {
		return units;
	}
	public void setUnits(Long units) {
		this.units = units;
	}
	public String getRedundancy() {
		return redundancy;
	}
	public void setRedundancy(String redundancy) {
		this.redundancy = redundancy;
	}
	public Long getRating_kva() {
		return rating_kva;
	}
	public void setRating_kva(Long rating_kva) {
		this.rating_kva = rating_kva;
	}
	public Long getRating_kw() {
		return rating_kw;
	}
	public void setRating_kw(Long rating_kw) {
		this.rating_kw = rating_kw;
	}
	public Long getRating_v() {
		return rating_v;
	}
	public void setRating_v(Long rating_v) {
		this.rating_v = rating_v;
	}
	
	
}
