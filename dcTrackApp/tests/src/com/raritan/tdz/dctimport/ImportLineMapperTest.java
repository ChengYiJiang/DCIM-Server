/*
 * @author Santo Rosario
 */

package com.raritan.tdz.dctimport;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.tests.TestBase;
import com.raritan.tdz.dctimport.dto.DataConnImport;
import com.raritan.tdz.dctimport.dto.ItemImport;
import com.raritan.tdz.dctimport.dto.PowerConnImport;
import com.raritan.tdz.dctimport.job.DCTHeaderFieldSetMapper;
import com.raritan.tdz.dctimport.job.ImportLineMapper;

public class ImportLineMapperTest extends TestBase {
	ImportLineMapper importLineMapper;
	DCTHeaderFieldSetMapper headerFieldSetMapper;
	private UserLookupFinderDAO userLookupFinderDAO;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		importLineMapper = (ImportLineMapper)ctx.getBean("importLineMapper");
		headerFieldSetMapper = (DCTHeaderFieldSetMapper)ctx.getBean("dctImportHeaderMapper");
		userLookupFinderDAO = (UserLookupFinderDAO)ctx.getBean("userLookupDAO");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}

	  @Test
    public final void tesPowerConnProcessLineShort() throws Throwable {
        String header = "# Operation *,Object *,Starting Item Location *,Starting Item Name *,Starting Port Name *,Cord Type,Cord ID,Ending Item Location *,Ending Item Name  *,Ending Port Name  *";
        String dataLine = "add,power-connection,SITE A,CLARITY,PS1,patch cord,test001,SITEA,MYROUTER,RT001";
        
        headerFieldSetMapper.setOriginalHeader(header);
        header = normalizeHeader(header);
       
        System.out.println(header);
        Object rec1 = importLineMapper.mapLine(header, 1);
        System.out.println(header);
        System.out.println(rec1);
        

        System.out.println(dataLine);
        Object rec2 = importLineMapper.mapLine(dataLine, 2);
        printRec(rec2);         
       
    }


	  @Test
      public final void tesProcessLineShort() throws Throwable {
          String header = "# Operation *,Object *,Starting Item Location *,Starting Item Name *,Starting Port Name *,Cord 1 Type,Cord 1 ID,Ending Item Location *,Ending Item Name  *,Ending Port Name  *";
          String dataLine = "add,data-connection,SITE A,CLARITY,Net01,patch cord,test001,SITEA,MYROUTER,RT001";
          
          headerFieldSetMapper.setOriginalHeader(header);
          header = normalizeHeader(header);
         
          System.out.println(header);
          Object rec1 = importLineMapper.mapLine(header, 1);
          System.out.println(header);
          System.out.println(rec1);
          

          System.out.println(dataLine);
          Object rec2 = importLineMapper.mapLine(dataLine, 2);
          printRec(rec2);         
         
      }

	  @Test
      public final void tesProcessLineWithHop1() throws Throwable {
          String header = "# Operation *,Object *,Starting Item Location *,Starting Item Name *,Starting Port Name *,Cord 1 Type,Cord 1 ID,Hop 1: Near End Location,Hop 1: Near End Panel Name,Hop 1: Port Name,Cord 2 Type,Cord 2 ID,Ending Item Location *,Ending Item Name  *,Ending Port Name  *";
          String dataLine = "add,data-connection,SITE A,CLARITY,Net01,patch cord,test001,SITE A,cp001,port001,LC/SC Fiber Jumper,test002,SITE A,MYROUTER,RT001";

          headerFieldSetMapper.setOriginalHeader(header);
          header = normalizeHeader(header);
         
          System.out.println(header);
          Object rec1 = importLineMapper.mapLine(header, 1);
          System.out.println(header);
          System.out.println(rec1);
          
          System.out.println(dataLine);
          Object rec2 = importLineMapper.mapLine(dataLine, 2);
          printRec(rec2);      
      }	


	  @Test
      public final void tesProcessLineWithHop2A() throws Throwable {
          String header = "# Operation *,Object *,Starting Item Location  *,Starting Item Name *,Starting Port Name  *,Cord 1 Type,Cord 1 ID,Hop 1: Near End Panel Location,Hop 1: Near End Panel Name,Hop 1: Port Name,Cord 2 Type,Cord 2 ID,Hop 2: Near End Panel Location,Hop 2: Near End Panel Name,Hop 2: Port Name,Cord 3 Type,Cord 3 ID,Ending Item Location *,Ending Item Name  *,Ending Port Name  *";
          String dataLine = "add,data-connection,SITE A,CLARITY,Eth04,Patch Cord,pc-006,SITE A,1F1,A0131,Patch Cord,pc-010,SITE A,1G1,A0146,Patch Cord,pc-011,SITE A,NJA01A-2,NJ03";

          headerFieldSetMapper.setOriginalHeader(header);
          header = normalizeHeader(header);
         
          System.out.println(header);
          Object rec1 = importLineMapper.mapLine(header, 1);
          System.out.println(header);
          System.out.println(rec1);
          
          System.out.println(dataLine);
          Object rec2 = importLineMapper.mapLine(dataLine, 2);
          printRec(rec2);      
      }	

	  @Test
      public final void tesProcessLineWithHop2B() throws Throwable {
          String header = "# Operation *,Object *,Starting Item Location  *,Starting Item Name *,Starting Port Name  *,Cord 1 Type,Cord 1 ID,Hop 1: Near End Panel Location,Hop 1: Near End Panel Name,Hop 1: Port Name,Cord 2 Type,Cord 2 ID,Hop 2: Near End Panel Location,Hop 2: Near End Panel Name,Hop 2: Port Name,Cord 3 Type,Cord 3 ID,Ending Item Location *,Ending Item Name  *,Ending Port Name  *";
          String dataLine = "add,data-connection,SITE A,CLARITY,Eth04,,,SITE A,1F1,A0131,Patch Cord,pc-010,SITE A,1G1,A0146,Patch Cord,pc-011,SITE A,NJA01A-2,NJ03";

          headerFieldSetMapper.setOriginalHeader(header);
          header = normalizeHeader(header);
         
          System.out.println(header);
          Object rec1 = importLineMapper.mapLine(header, 1);
          System.out.println(header);
          System.out.println(rec1);
          
          System.out.println(dataLine);
          Object rec2 = importLineMapper.mapLine(dataLine, 2);
          printRec(rec2);      
      }	
	  
	  @Test
      public final void tesProcessLineWithCustomField() throws Throwable {
		  String header = "# Operation *,Object *,Name *,Make *,Model *,Location  *,Cabinet **,U Position **,Rails Used **,Orientation **,Status,Custom Field Maintenance Docs,Custom Field Import Test";
		  String dataLine = "add,device-rackable,IMPORT-HP-DEV,HP,Proliant DL385p G8,SITE SITE A,CAB-CF-IMPORT,1,Both,Item Front Faces Cabinet Front,Planned,Doc 4, import cf 4";

          headerFieldSetMapper.setOriginalHeader(header);
          header = normalizeHeader(header);
         
          System.out.println(header);
          Object rec1 = importLineMapper.mapLine(header, 1);
          System.out.println(header);
          System.out.println(rec1);
          
          System.out.println(dataLine);
          Object rec2 = importLineMapper.mapLine(dataLine, 2);
          printRec(rec2);      
      }		  

	/**
	 * Here we normalize the header line here so that this can be flexible for the user
	 * @param line
	 * @return
	 */
	private String normalizeHeader(String line) {
		line = line.toLowerCase();
		line = line.replaceAll("^#", "");
		line = line.replaceAll("\\s", "");
		line = line.replaceAll("\\*", "");
		return line;
	}

	private void printRec(Object rec) {
		for(String e:importLineMapper.getImportErrorHandler().getErrors()) {
			System.out.println(e);
		}
		
		if(rec instanceof DataConnImport) {
	        DataConnImport obj = (DataConnImport)rec;
	        
	        System.out.println(obj);
	        System.out.println(obj.getCordType());
	        System.out.println(obj.getCordId());
	        System.out.println(obj.getCordColor());
	        System.out.println(obj.getCordLength());
	        System.out.println(obj.getPanelLocation());
	        System.out.println(obj.getPanelName());
	        System.out.println(obj.getPanelPortName());		        
	        System.out.println(obj.getErrorsAndWarnings());
	        
			for (String entry: obj.getCordType().keySet()) {
				System.out.println(entry);
			}
		}
		else if(rec instanceof PowerConnImport) {
			PowerConnImport obj = (PowerConnImport)rec;
	        
	        System.out.println(obj);
	        System.out.println(obj.getErrorsAndWarnings());
	        
		}		
		else if(rec instanceof ItemImport) {
			ItemImport obj = (ItemImport)rec;
			
			System.out.println(obj);
			System.out.println(obj.getTiCustomField());
			System.out.println(obj.getErrorsAndWarnings());
		}else {
			System.out.println(rec);
		}
	}

}
