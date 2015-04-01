package com.raritan.tdz.reports.imagegenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.reports.imageprovider.CabinetElevation;
import com.raritan.tdz.reports.imageprovider.ImageProvider;
import com.raritan.tdz.tests.TestBase;

public class CabinetImageTest extends TestBase {

    private void createImage(long id, boolean cab, ImageProvider.mode_t mode, ImageProvider.rails_t rail, String suffix) {
		BufferedImage img = null;
		try {
		    if (cab) {
		    	img = m_cabelev.createImage(id, mode, rail);
		    }
		    else {
		    	img = m_cabelev.createImageByItem(id, mode, rail);
		    }
		} catch (Exception e) {	    
		    e.printStackTrace();
		    Assert.fail("can not create image");
		}
		
		String fileName = this.getClass().getName() + mode.toString() + rail.toString() + (cab ? "Cabinet" : "Item") + id + suffix;
		System.out.println("file name = " + fileName);
		
		writeFile(img, fileName);
    }

    private void writeFile(BufferedImage img, String fname) {
    	
		try {
			
		    ByteArrayOutputStream stream = m_provider.createPngByteStream(img);
		    Assert.assertTrue(stream.size() > 0, "stream empty");
		    File outputfile = new File(fname + ".png");
		    FileOutputStream foStream = new FileOutputStream(outputfile);
		    stream.writeTo(foStream);
		    stream.flush();
		    stream.close();
		    
		} catch (IOException e) {	    
		    
			Assert.fail("can not wirte file");
		}
    }

    @Test
    public void createCabinetImage() {
    	
    	boolean big = false;
    	
		if (big) m_cabelev.setSlotWidth(300);
		else m_cabelev.setSlotWidth(CabinetElevation.SLOT_WIDTH);
		
		String suffix = big ? "big" : "default";
		
		long cabinetId = 10;
	    	
		createImage(cabinetId, true, ImageProvider.mode_t.TEXT, ImageProvider.rails_t.FRONT, suffix);
		
		createImage(cabinetId, true, ImageProvider.mode_t.TEXT, ImageProvider.rails_t.REAR, suffix);
		
		createImage(cabinetId, true, ImageProvider.mode_t.IMAGE, ImageProvider.rails_t.FRONT, suffix);
		
		createImage(cabinetId, true, ImageProvider.mode_t.IMAGE, ImageProvider.rails_t.REAR, suffix);
		
    }

    @Test
    public void createItemImage() {
    	
    	boolean big = false;
    	
		if (big) m_cabelev.setSlotWidth(300);
		else m_cabelev.setSlotWidth(CabinetElevation.SLOT_WIDTH);
		
		String suffix = big ? "big" : "default";
		
		long itemId = 15;

		createImage(itemId, false, ImageProvider.mode_t.TEXT, ImageProvider.rails_t.FRONT, suffix);
		
		createImage(itemId, false, ImageProvider.mode_t.TEXT, ImageProvider.rails_t.REAR, suffix);
		
		createImage(itemId, false, ImageProvider.mode_t.IMAGE, ImageProvider.rails_t.FRONT, suffix);
		
		createImage(itemId, false, ImageProvider.mode_t.IMAGE, ImageProvider.rails_t.REAR, suffix);

    }


}
