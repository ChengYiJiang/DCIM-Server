package com.raritan.tdz.marshaller;

import java.util.Collection;

import flex.messaging.io.ArrayCollection;
import flex.messaging.io.amf.translator.ASTranslator;

/**
 * Custom marshaling between ActionScript types and Java Types.
 * 
 * @author Andrew Cohen
 *
 */
public class CustomTypeMarshaller extends ASTranslator {

	@Override
	public Object convert(Object origValue, Class type) {
		if (type.equals(Collection.class)) {
			//convertIntegerArrayToLongArrary(origValue, type);
		}
		return super.convert(origValue, type);
	}
	
	/*
	 * An ActionScript Array of Numbers should get converted
	 * to a Java List of Longs, but instead gets converted to 
	 * a list of Java Integers! 
	 * 
	 * This method is a hacky way of handling this problem in lieu
	 * of an existing better solution.
	 */
	@SuppressWarnings("unchecked")
	private void convertIntegerArrayToLongArrary(Object origValue, Class<?> type) {
		if (origValue instanceof ArrayCollection) {
			ArrayCollection arr = (ArrayCollection)origValue;
			if (arr != null && !arr.isEmpty()) {
				if (arr.get(0) instanceof Integer) {
					for (int i=0; i<arr.size(); i++) {
						arr.set(i, ((Integer)arr.get(i)).longValue());
					}
				}
			}
		}
	}
}
