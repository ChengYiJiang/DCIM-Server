package com.raritan.tdz.model.library;

import java.math.BigInteger;

import junit.framework.Assert;

import org.hibernate.Query;
import org.testng.annotations.Test;

import com.raritan.tdz.tests.TestBase;

public class ModelLibraryTest extends TestBase {

	//@Test
	public void testAllRackPDUHasInputCord() {
		
		Query q = session.createSQLQuery(new StringBuffer()
			.append(" select count(*) from dct_models where model_id in ")
			.append(" (select distinct model_id from dct_model_ports_power ")
			.append(" where model_id in (select model_id from dct_models ")
			.append(" where class_lks_id = (select lks_id from dct_lks_data where lkp_type_name = 'CLASS' and lkp_value = 'Rack PDU')) ")
			.append(" and subclass_lks_id = (select lks_id from dct_lks_data where lkp_type_name = 'PORT_SUBCLASS' and lkp_value = 'Rack PDU Output') ")
			.append(" and input_cord_port_id is null) ")
			.toString()
	    );
		
		Long numOfRPDUWithNoInputCord = ((BigInteger)(q.uniqueResult())).longValue();
		
		Assert.assertEquals("Some Rack PDU has no input Cord", 0, numOfRPDUWithNoInputCord.longValue());
		
	}
}
