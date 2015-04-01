package com.raritan.tdz.powerchain.validator;

import com.raritan.tdz.domain.Item;

public class ValidateObject {

		public Item item;
		public String errorCode;
		public Boolean supports;
		
		public ValidateObject(Item item, String errorCode, Boolean supports) {
			super();
			this.item = item;
			this.errorCode = errorCode;
			this.supports = supports;
		}

}
