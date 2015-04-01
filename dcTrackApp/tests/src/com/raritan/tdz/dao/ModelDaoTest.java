/**
 * @author Santo Rosario
 */
package com.raritan.tdz.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.port.dao.*;
import com.raritan.tdz.tests.TestBase;

public class ModelDaoTest extends TestBase {
	ModelDAO modelDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		modelDAO = (ModelDAO)ctx.getBean("modelDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	@Test
	public final void tesReadRecord() throws Throwable {
		ModelDetails rec = modelDAO.read(1L);
		
		if(rec == null){
			System.out.println("testing");
		}
		else{
			System.out.println(rec.getModelName());
		}		
	}

}
