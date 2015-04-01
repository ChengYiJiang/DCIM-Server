package com.raritan.tdz.reports.imageprovider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.reports.eventhandler.ReportConstants;
import com.raritan.tdz.util.LksDataConstants;

public class ItemPorts extends ItemImageBase {

	private static final Logger logger = Logger.getLogger(ItemPorts.class);

	private static final int PADDING = 2;
	private static final int DEFAULT_IMAGE_WIDTH = 700;
	private static final double VBA_MAGIC_WIDTH = 14500;
	private static final double VBA_MAGIC_IMG_WIDTH = 14400;
	private static final double VBA_MAGIC_WIDTH_FACTOR = 1440;

	private Color m_defaultBgColor;
	private Color m_borderColor;
	private Color m_fontColor;
	private Font m_font;
	private BasicStroke m_borderStroke;

	private int m_max_width;

	public ItemPorts(final Session session, final ImageCache cache) {
		super(session, cache);
		m_borderColor = new Color(0, 0, 0);
		m_borderStroke = new BasicStroke(1);
		m_defaultBgColor = new Color(0xf7, 0xf4, 0x07);
		m_fontColor = new Color(0x00, 0x00, 0x00);
		m_font = new Font(Font.MONOSPACED, Font.PLAIN, 9);
		m_max_width = DEFAULT_IMAGE_WIDTH;
	}

	public BufferedImage createImage(long itemid, int imageType) throws Exception 
	{
		logger.debug("create item port image for id " +  itemid);

				Item item = getItem(itemid);
				if (item.getModel() == null) throw new Exception("item has no model");

				try {
					int height = calcImgHeight(item);
					initDrawingArea(m_max_width, height);
					boolean hasImage = false;
					if (imageType == ReportConstants.ITEM_PORT_IMAGE_FRONT) {
						hasImage = item.getModel().getFrontImage();
					} else if (imageType == ReportConstants.ITEM_PORT_IMAGE_REAR) {
						hasImage = item.getModel().getRearImage();
					}

//					boolean hasFront = item.getModel().getFrontImage();
//					boolean hasRear = item.getModel().getRearImage();
//					if (hasFront || hasRear) {
					if (hasImage) {
						int img_W = (int)((double)m_max_width * VBA_MAGIC_IMG_WIDTH/VBA_MAGIC_WIDTH + 0.5); 
						//drawImage(0, img_W, 0, height, true, !hasRear, item);
						boolean front = (imageType == ReportConstants.ITEM_PORT_IMAGE_FRONT);
						drawImage(0, img_W, 0, height, true, front, item);
						List<PowerPort> pp = getPowerPorts(item, !front);
						List<DataPort> dp = getDataPorts(item, !front);
						if (pp.size() > 0) {
							drawPowerPorts(pp);
						}
						if (dp.size() > 0) {
							drawDataPorts(dp);
						}

					} else {
						logger.debug("neither front or rear image available for item id " +  itemid);
					}
				} catch (IOException e) {
					logger.error("can not initiate drawing area for item " + item.getItemName(), e);
				}

				return flush();
	}

	public void setMaxImgWidth(int width) {
		m_max_width = width;
	}

	public int getMaxImgWidth() {
		return m_max_width;
	}

	private void drawPowerPorts(List<PowerPort> ports)
	{
		for (Iterator<PowerPort> iterator = ports.iterator(); iterator.hasNext();) {
			PowerPort port = iterator.next();
			drawPort(port.getPortName(), port.getPlacementX(), port.getPlacementY(), port.getColorLookup());
		}
	}

	private void drawDataPorts(List<DataPort> ports)
	{
		for (Iterator<DataPort> iterator = ports.iterator(); iterator.hasNext();) {
			DataPort port = iterator.next();
			drawPort(port.getPortName(), port.getPlacementX(), port.getPlacementY(), port.getColorLookup());
		}
	}


	private void drawPort(String port, int vba_x, int vba_y, LkuData bg)
	{
		double fac = m_max_width/VBA_MAGIC_WIDTH;
		int x = (int)(vba_x * fac);
		int y = (int)(vba_y * fac);
		Color color = m_defaultBgColor;
		//	logger.info(port + ": vbaX:vbaY " + vba_x + ":" + vba_y  + " X:Y " + x + ":" +y );
		if (bg != null) {
			int bgr = new Integer(bg.getLkuAttribute()).intValue();
			int r = bgr & 0xff;
			int g = (bgr>>8) & 0xff;
			int b = (bgr>>16) & 0xff;
			color = new Color(r, g, b);
		}
		fillRectAndDrawString(port, x, y, PADDING, m_font, m_fontColor, color, m_borderColor, m_borderStroke);
	}

	@SuppressWarnings("unchecked")
	private List<PowerPort> getPowerPorts(Item item, boolean facing_rear)
	{

		Query q = getSession().createQuery("select p from PowerPort p where p.item.itemId = :itemid AND " +
				"p.faceLookup.lkpValueCode = :facecode");
		q.setEntity("itemid", item);
		q.setLong("facecode", facing_rear ?  LksDataConstants.Face.REAR: LksDataConstants.Face.FRONT);
		q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);
		return (List<PowerPort>) q.list();
	}

	@SuppressWarnings("unchecked")
	private List<DataPort> getDataPorts(Item item, boolean facing_rear)
	{
		Query q = getSession().createQuery("select p from DataPort p where p.item.itemId = :itemid AND " +
				"p.faceLookup.lkpValueCode = :facecode");
		q.setEntity("itemid", item);
		q.setLong("facecode", facing_rear ?  LksDataConstants.Face.REAR: LksDataConstants.Face.FRONT);
		q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);
		return (List<DataPort>) q.list();
	}
	/*
	 * PLEASE NOTE:
	 * THIS LOGIC IS BASED ON THE VBA CODE AND HAS TO EMULATE THE BEHAVIOR TO GET ALONG WITH X & Y PORT PLACEMENT
	 * 
	 * The placement x and y number in the DB are relative the Windows Client for the Ports Placement 
	 * dialog window size where the top left of that window is 0,0 and the window width is 14500. 
	 * The height of the Window is calculated based on the RU height of the Model it is viewing, up to 8 RUs.
	 * 
	 * The code that calculates the image height is: (Twip = 1440 which is equal to 1" on the screen)
	 * 
	 *     If Me.txtRU = 0 Or Me.txtRU > 7 Or Me.txtMounting = "VStack" Then
	 *     	tRU = 7
	 *     	L = 250
	 *     Else
	 *     	tRU = Me.txtRU
	 *     If tRU = 1 Then 
	 *     	tRU = 1.25 'Enlarge the 1 RU slightly
	 *     End If
	 *     Me.DeviceImage.Height = ((tRU * (Twip + 300 / tRU)) - L)
	 *     
	 */
	private int calcImgHeight(Item item) {
		double ruFactor;
		int ru = item.getModel().getRuHeight();
		int L = 0;
		if (ru == 0 || ru > 7 || item.getModel().getMounting() == "ZeroU") {
			ruFactor = 7;
			L = 250;
		} else if (ru == 1) {
			ruFactor = 1.25;
		} else {
			ruFactor = ru;
		}
		double h = ruFactor * (VBA_MAGIC_WIDTH_FACTOR + 300/ruFactor) - L;
		return (int)(h * m_max_width/VBA_MAGIC_WIDTH + 0.5);
	}

}