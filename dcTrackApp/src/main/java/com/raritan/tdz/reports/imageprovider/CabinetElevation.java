package com.raritan.tdz.reports.imageprovider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.reports.imageprovider.ImageProvider.rails_t;
import com.raritan.tdz.util.LksDataConstants;

public class CabinetElevation extends ItemImageBase {

	private static final Logger logger = Logger.getLogger(CabinetElevation.class);
	/*
	 * FIXME: we should just request SansSerif but unfortunately on the linux deployment centos
	 * the font.properties of jre chooses a ugly one per default so we should actually change it there
	 */
	public static final String ITEM_FONT = Font.MONOSPACED;
	public static final String ROW_FONT = "Courier New";

	/* this is only to determine ratios (+default), actual image width can set at runtime, see setDesiredWidth*/
	public static final int SLOT_WIDTH  = 100;
	private static final int CAB_BORDER_WIDTH = 2;
	private static final int ITEM_BORDER_WIDTH = 1;
	private static final int ROW_TEXT_WIDTH = 16;    
	private static final int VERT_ITEM_WIDTH = 10;
	private static final int VERT_SPACE = 2;   
	private static final int VERT_OVERLAP_WIDTH = 2;   
	private static final int U_POS_LABEL_SPACE = 14;
	
	private static final int MAX_CAB_SLOTS = 58; // 58 slot height

	private Color m_deviceColor;
	private Color m_networkColor;
	private Color m_commColor;
	private Color m_pduColor;
	private Color m_probeColor;
	private Color m_passiveColor;
	private Color m_plateColor;

	private Color m_newColor;

	private Color m_selectorColor;

	private Color m_cabBorderColor;
	private Color m_itemBorderColor;
	private Color m_textColor;    
	private Color m_shapeColor;    

	private Color m_whiteColor;
	private Color m_blackColor;

	private Stroke m_cabStroke;
	private Stroke m_itemStroke;

	private Font m_itemFont;
	private Font m_rowFont;

	private int m_slot_width;
	private int m_slot_height;
	private int m_triangle_length;
	private int m_triangle_height;
	private int m_outer_triangle_w;
	private int m_cab_border_width;
	private int m_item_border_width;
	private double m_cab_border_center;
	private double m_item_border_center;
	private int m_slot_item_border_width;
	private int m_row_text_width;    
	private int m_vert_item_width;
	private int m_vert_width;
	private int m_vert_overlap_width;
	private int m_vert_space;
	private int m_cab_x;
	private int m_cab_width;    
	private int m_pic_width;
	private int m_pic_height;
	private int m_text_padding;
	private int m_slot_label_width;
	private BasicStroke m_backgroundItemStroke;
	private Color m_bg = null;

	public CabinetElevation(final Session session, final ImageCache cache) {
		super(session, cache);

		m_cabBorderColor = new Color(0x80,0x80,0x80);
		m_itemBorderColor = new Color(0x80,0x80,0x80);
		m_textColor = new Color(0x00,0x00,0x00);
		m_shapeColor = new Color(0xff, 0xff, 0xff);

		m_deviceColor= new Color(0xff, 0xff, 0x80);
		m_networkColor= new Color(0x80, 0xff, 0x80);
		m_commColor= new Color(0x88, 0xc4, 0xff);
		m_pduColor= new Color(0xda, 0xdb, 0xac);
		m_probeColor= new Color(0xc0 ,0x82 ,0xff);
		m_passiveColor= new Color(0xff, 0xb6, 0x6c);
		m_plateColor= new Color(0x80, 0x80, 0x80);

		m_newColor= new Color(0xff, 0x00, 0x00);

		m_selectorColor= new Color(0xff, 0x00, 0x00, 0xCC);
		m_whiteColor = new Color (0xff, 0xff, 0xff);
		m_blackColor = new Color (0x00, 0x00, 0x00);

		m_slot_width = SLOT_WIDTH;
		calcDrawingDim();//default init
		m_bg = new Color(0xff, 0xff, 0xff);
	}

	public void setSlotWidth(int width) 
	{
		logger.debug("set slot width '" + width+ "'");
		getCache().clearCabinetCacheEntries();
		m_slot_width = width;
		calcDrawingDim();
	}

	public int getSlotWidth() 
	{
		return m_slot_width;
	}

	public BufferedImage createImageByItem(long itemid, ImageProvider.mode_t mode, ImageProvider.rails_t rail) throws Exception 
	{
		logger.debug("create cabinet elevation image by item id '" + itemid + "' mode: " +  mode.toString());

		Item item = getItem(itemid);
		Item cab = (item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)) ? item : item.getParentItem();

		List<CabinetElevationData> elevation = null;

		/*
		 * right now caching just use full in ItemDetails report which uses this method
		 */
		ImageCache.CabinetEntry entry = getCachedImageEntry(cab, mode, rail);
		if (entry == null) {
			initImage(cab);

			drawCabinet(cab);

			elevation = drawItems(mode, rail, cab);
			//cache it
			cacheImageEntry(cab, mode, rail, getImg(), elevation);
		} else {
			logger.debug("use cache cabinet image for cabinet id '" + itemid + "' mode: " +  mode.toString());
			initImage(entry.img);
			elevation = entry.items;
		}
		
		CabinetElevationData cbe = getCabinetElevationData (elevation, item.getItemId() );
		if (cbe != null) {
			if (isVertical(cbe)) {
				drawSelectorVertical(cab, cbe);
			}
			else {
				if (!item.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)) {
					drawSelector(cab, cbe);
				}
			}
		}

		return flush();
	}

	public BufferedImage createImage(long cabinetid, ImageProvider.mode_t mode, rails_t face) throws Exception 
	{
		logger.debug("create cabinet elevation image id '" + cabinetid + "' mode: " +  mode.toString() + "' facing: " + face.toString());
		Item cab = getItem(cabinetid);

		initImage(cab);

		drawCabinet(cab);
		drawItems(mode, face, cab);

		return flush();
	}

	public BufferedImage createPaddingImage(long cabinetid, ImageProvider.mode_t mode, rails_t face) throws Exception 
	{
		logger.debug("create cabinet elevation image id '" + cabinetid + "' mode: " +  mode.toString() + "' facing: " + face.toString());
		Item cab = getItem(cabinetid);

		initPaddingImage(cab);
		
		return flush();
	}

	private void initPaddingImage(Item cab)
	{
		int slots = MAX_CAB_SLOTS - cab.getModel().getRuHeight();
		m_pic_height = slots * m_slot_height + (slots - 1) * 
				m_item_border_width + m_cab_border_width * 2;

		initDrawingArea(m_pic_width, m_pic_height);
		
	}

	private void initImage(Item cab)
	{
		int slots = cab.getModel().getRuHeight();
		m_pic_height = slots * m_slot_height + (slots - 1) * 
				m_item_border_width + m_cab_border_width * 2;

		initDrawingArea(m_pic_width, m_pic_height);
		
	}

	private void initImage(BufferedImage cabimg) throws IOException
	{
		m_pic_height = cabimg.getHeight();
		initDrawingArea(cabimg);
	}

	private void drawBackGroundItems(ImageProvider.mode_t mode, ImageProvider.rails_t rail, Item cab) throws Exception {

		List<CabinetElevationData> backGroundItems = null;
		
		if (ImageProvider.rails_t.FRONT == rail) {
			backGroundItems = getCabinetElevation(cab, ImageProvider.rails_t.REAR);
		}
		else if (ImageProvider.rails_t.REAR == rail) {
			backGroundItems = getCabinetElevation(cab, ImageProvider.rails_t.FRONT);
		}
		else return;

		if (null == backGroundItems) return;

		int slots = cab.getModel().getRuHeight();

		setStroke(m_backgroundItemStroke);

		for (CabinetElevationData item: backGroundItems) {
			drawSingleBackgroundItem(item, slots);
		}
	}

	private List<CabinetElevationData> drawItems(ImageProvider.mode_t mode, rails_t rail, Item cab) throws Exception 
	{	
		// draw background items (fills items with stroke)
		drawBackGroundItems(mode, rail, cab);

		// filter items front/back as selected by user
		List<CabinetElevationData> items = getCabinetElevation(cab, rail);
		logger.debug(items.size() + "Items in cabinet");

		int slots = cab.getModel().getRuHeight();

		for (CabinetElevationData item: items) {
			try {
				drawSingleItem(item, mode, rail, slots); // bunty
			} catch (Exception e) {
				logger.warn("error during drawing item " + item.getItemname() + " to cabinet", e);
			}
		}
		return items;
	}

	private void drawSingleBackgroundItem(CabinetElevationData item, int nrSlots) throws Exception {

		logger.debug("draw single background Item: " + item.getItemname());

		int x, w;

		int ru = item.getRuheight();
		int pos = (int)item.getUposition().intValue();

		//	this is a workaround for broken data which is unfortunately a use case
		if (ru + pos - 1 > nrSlots) {
			if (pos > nrSlots) {
				logger.warn("discarding Item '" + item.getItemname() +"' - u_postion out of range");
				return;
			}
			logger.warn("clipping ru height for Item '" + item.getItemname() +"'");
			ru = nrSlots - pos + 1;
		}

		int y = getItemPosY(nrSlots, pos, ru);
		int h = getAbsHeigthFromRU(ru);

		boolean vertical = isVertical(item);

		if (vertical) {
			return;
		} else {
			x = m_cab_x + m_cab_border_width;
			w = m_slot_width;

			// Handle Non-Rackables
			if (item.getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {

				Long numOfNrAtUpos = getNumOfNRAtUPos(item.getParentitemid(), (long) pos);

				Integer shelfPos = item.getShelfposition().intValue();

				int perItemWidth = (int) (m_slot_width / numOfNrAtUpos);

				int fillDiff = (int) (m_slot_width - perItemWidth * numOfNrAtUpos);

				x += (shelfPos - 1) * perItemWidth;

				w = perItemWidth + ((shelfPos.longValue() == numOfNrAtUpos) ? fillDiff : 0); 

				// drawBorder(x, y, w, h, m_itemBorderColor, m_itemStroke);

				x += m_item_border_width;

				w -= m_item_border_width;

			}

		}

		Color recColor = Color.GRAY;
		// GradientPaint gp1 = new GradientPaint(5, 5, Color.lightGray, 20, 20, Color.gray, true);
		GradientPaint gp1 = new GradientPaint(1, 1, Color.lightGray, 5, 5, Color.white, true);
		fillRect(x, w, y, h, recColor, gp1);

	}


	private void drawSingleItem(CabinetElevationData item, ImageProvider.mode_t mode, ImageProvider.rails_t rail, int nrSlots) throws Exception 
	{		
		logger.debug("draw single Item: " + item.getItemname());

		int x, w;

		int ru = item.getRuheight();
		int pos = (int)item.getUposition().intValue();

		//	this is a workaround for broken data which is unfortunately a use case
		if (ru + pos - 1 > nrSlots) {
			if (pos > nrSlots) {
				logger.warn("discarding Item '" + item.getItemname() +"' - u_postion out of range");
				return;
			}
			logger.warn("clipping ru height for Item '" + item.getItemname() +"'");
			ru = nrSlots - pos + 1;
		}

		int y = getItemPosY(nrSlots, pos, ru);
		int h = getAbsHeigthFromRU(ru);

		boolean vertical = isVertical(item);

		if (vertical) {
			if (isLeftRail(item)) {
				if (isFacingFront(item)) {
					x = m_item_border_width;
				}
				else {
					x = m_vert_width - m_vert_overlap_width;
				}
			} else {
				// x = m_pic_width - m_vert_item_width - m_item_border_width - m_outer_triangle_w;
				x = m_cab_x + m_cab_width + m_slot_label_width + m_outer_triangle_w + m_vert_space; 
				if (isFacingFront(item)) {
					x += m_vert_width - m_vert_overlap_width;
				}
			}
			w = m_vert_item_width;
		} else {
			x = m_cab_x + m_cab_border_width;
			w = m_slot_width;

			// Handle Non-Rackables
			if (item.getMounting().equals(SystemLookup.Mounting.NON_RACKABLE)) {

				Long numOfNrAtUpos = getNumOfNRAtUPos(item.getParentitemid(), (long) pos);

				Integer shelfPos = item.getShelfposition() != null ? item.getShelfposition().intValue(): 1;

				int perItemWidth = (int) (m_slot_width / numOfNrAtUpos);

				int fillDiff = (int) (m_slot_width - perItemWidth * numOfNrAtUpos);

				x += (shelfPos - 1) * perItemWidth;

				w = perItemWidth + ((shelfPos.longValue() == numOfNrAtUpos) ? fillDiff : 0); 

				drawBorder(x, y, w, h, m_itemBorderColor, m_itemStroke);

				x += m_item_border_width;

				w -= m_item_border_width;

			}

		}

		//draw border around verticals
		if (vertical) {
			drawBorder(x - m_item_border_width + m_item_border_center, 
					y - m_item_border_center - m_item_border_width % 2,
					m_vert_item_width + m_item_border_width, h + m_item_border_width, 
					m_itemBorderColor, m_itemStroke);
		}

		if (mode == ImageProvider.mode_t.TEXT || isVertical(item) ||  
				!drawImage(x, w, y, h, false, useItemFrontImg(item, mode, rail), item)/* fallback */) {

			Color recColor = getItemColor(item);
			fillRect(x, w, y, h, recColor);
			
			drawCenteredString(item.getItemname(), x, w, y, h, m_text_padding, vertical, 
					m_itemFont, getForegroundColor (recColor));
		}
	}
	
	private Color getForegroundColor (Color backgroundColor) {
		int red = backgroundColor.getRed();
		int green = backgroundColor.getGreen();
		int blue = backgroundColor.getBlue();
		Double bgLuminance = (0.299 * red) + (0.587 * green) + (0.114 * blue);
		Color fgColor = (bgLuminance < 128) ? m_whiteColor: m_blackColor;
		return fgColor;
	}

	private void drawCabinet(Item cab) 
	{
		int slots = cab.getModel().getRuHeight();
		logger.debug("Number of slots in cabinet: " + slots);

		// draw border
		drawBorder(m_cab_x + m_cab_border_center, m_cab_border_center, 
				m_slot_width + m_cab_border_width, m_pic_height - m_cab_border_width,
				m_cabBorderColor /*m_bg*/, m_cabStroke);

		// draw slot separators 
		for (int i=1; i < slots; i++) {
			double y = m_cab_border_width + i * (m_slot_item_border_width) - 
					m_item_border_center - m_item_border_width%2;
			drawLine(m_cab_x + m_cab_border_width, y, 
					m_cab_x + m_slot_width + m_cab_border_width, y, 
					m_itemBorderColor, m_itemStroke);
		}
		// draw slot numbers
		for (int i=0; i < slots; i++) {
			drawString(String.format("%02d", slots - i),
					m_cab_x + m_cab_width + m_vert_space, 
					(i + 1) * m_slot_item_border_width + m_cab_border_width - m_item_border_width - 2, /* move up by 2 pixels */ 
					m_rowFont, m_textColor);
		}
	}

	private void drawSelectorVertical(Item cab, CabinetElevationData item) {
		int x = 0;

		int ru = item.getRuheight();
		int pos = item.getUposition().intValue();
		int nrSlots = cab.getModel().getRuHeight();

		//	this is a workaround for broken data which is unfortunately a use case
		if (ru + pos - 1 > nrSlots) {
			if (pos > nrSlots) {
				logger.warn("discarding Item '" + item.getItemname() +"' - u_postion out of range");
				return;
			}
			logger.warn("clipping ru height for Item '" + item.getItemname() +"'");
			ru = nrSlots - pos + 1;
		}

		int y = getItemPosY(nrSlots, pos, ru);
		int h = getAbsHeigthFromRU(ru);
		boolean left = false;
		boolean front = false;

		boolean vertical = isVertical(item);

		if (vertical) {
			if (isLeftRail(item)) {
				left = true;
				if (isFacingFront(item)) {
					front = true;
					x = m_item_border_width;
				}
				else {
					x = m_vert_width - m_vert_overlap_width;
				}
			} else {
				// x = m_pic_width - m_vert_item_width - m_item_border_width - m_outer_triangle_w;
				x = m_cab_x + m_cab_width + m_slot_label_width + m_outer_triangle_w + m_vert_space; 
				if (isFacingFront(item)) {
					front = true;
					x += m_vert_width - m_vert_overlap_width;
				}
			}
			// w = m_vert_item_width;
		}

		Polygon triUp = createVerticalTriangle(x, y  - m_item_border_width, left, front, true);
		Polygon triDown = createVerticalTriangle(x, y + h, left, front, false);

		drawFilledPolygon(triUp, m_selectorColor, m_itemBorderColor, m_itemStroke);
		drawFilledPolygon(triDown, m_selectorColor, m_itemBorderColor, m_itemStroke);

	}

	private Polygon createVerticalTriangle(int x, int y, boolean left, boolean front, boolean up) {

		int y_offset = up ? -m_outer_triangle_w : m_outer_triangle_w;

		int[][] coords = {{x, x + m_outer_triangle_w, x + (m_outer_triangle_w / 2)},
				{y + y_offset, y + y_offset, y}};

		return new Polygon(coords[0], coords[1], 3);
	}


	private void drawSelector(Item cab, CabinetElevationData item) 
	{
		int slots = cab.getModel().getRuHeight();
		int ru = item.getRuheight();

		int y = getItemPosY(slots, (int)item.getUposition().intValue(), ru);
		y += getAbsHeigthFromRU(ru) - m_outer_triangle_w;

		int x1;
		int x2;

		if (isVertical(item)) {
			if (isLeftRail(item)) {
				x1 = m_outer_triangle_w - m_item_border_width;
				x2 = m_outer_triangle_w + m_vert_width + (int)m_item_border_center;
			} else {
				x2 = m_pic_width - m_outer_triangle_w + (int)m_item_border_center;
				x1 = m_pic_width - m_outer_triangle_w - m_vert_width - m_item_border_width;
			}
		} else {
			x1 = m_cab_x - m_item_border_width;
			x2 = m_cab_x + m_slot_width + 2 * m_cab_border_width + /*(int)m_item_border_center + m_slot_label_width*/ + m_row_text_width;
		}
		Polygon tri1 = createTriangle(x1, y, false);
		Polygon tri2 = createTriangle(x2, y, true);

		drawFilledPolygon(tri1, m_selectorColor, m_itemBorderColor, m_itemStroke);
		drawFilledPolygon(tri2, m_selectorColor, m_itemBorderColor, m_itemStroke);
	}

	private Polygon createTriangle(int x, int y, boolean corner_left) {
		int baseX = x + (corner_left ? m_triangle_height : - m_triangle_height);
		int[][] coords = {{baseX, baseX, x}, 
				{y, y + m_triangle_length, y + m_triangle_length/2}};
		return new Polygon(coords[0], coords[1], 3);
	}

	private int getItemPosY(int nrSlots, int uPos, int uHeight) 
	{
		return ((nrSlots - (uPos - 1) - uHeight) * (m_slot_item_border_width)) + 
				m_cab_border_width;
	}

	private int getAbsHeigthFromRU(int ruHeight) 
	{
		return (ruHeight == 1) ? m_slot_height : ruHeight * m_slot_height + (ruHeight - 1) * m_item_border_width;
	}

	private Color getItemDeviceColor(CabinetElevationData item) 
	{
		long type = item.getClassvaluecode();

		if (type == LksDataConstants.Class.DEVICE) {
			return m_deviceColor;
		} else if (type == LksDataConstants.Class.NETWORK) {
			return m_networkColor;
		} else if (type == LksDataConstants.Class.PROBE) {
			return m_probeColor;
		} else if (type == LksDataConstants.Class.DATA_PANEL) {
			return m_commColor;
		} else if (type == LksDataConstants.Class.RACK_PDU) {
			return m_pduColor;
		} else if (type == LksDataConstants.Class.PASSIVE) {
			return m_passiveColor;
		} else if (type == LksDataConstants.Class.BLANKING_PLATE) {
			return m_plateColor;
		} else {
			return m_shapeColor;
		} 
	}

	private Color getItemColor(CabinetElevationData item) 
	{
		long status = item.getStatusvaluecode();
		if (status == LksDataConstants.ItemStatus.NEW || 
				status == LksDataConstants.ItemStatus.APPROVED_NOT_INSTALLED) {
			return m_newColor;
		} 
		return getItemDeviceColor(item);
	}

	@SuppressWarnings("unchecked")
	private List<CabinetElevationData> getCabinetElevation(Item cab, ImageProvider.rails_t rail)   
	{
		/*
		 * fetch HORIZONTALS
		 */

		/* Filter based on mounting */
		Vector<Long> mountcnd = new Vector<Long>(); 
		mountcnd.add(LksDataConstants.RailsUsed.BOTH);
		if (rail == ImageProvider.rails_t.FRONT) {

			mountcnd.add(LksDataConstants.RailsUsed.FRONT);
		} else {//for text and rear

			mountcnd.add(LksDataConstants.RailsUsed.REAR);
		}
		/* Filter by class */
		Vector<Long> classcnd = new Vector<Long>(); 
		classcnd.add(LksDataConstants.Class.DEVICE); 
		classcnd.add(LksDataConstants.Class.NETWORK);
		classcnd.add(LksDataConstants.Class.DATA_PANEL);
		classcnd.add(LksDataConstants.Class.PROBE);
		classcnd.add(LksDataConstants.Class.RACK_PDU);
		classcnd.add(LksDataConstants.Class.BLANKING_PLATE);
		classcnd.add(LksDataConstants.Class.PASSIVE);

		List<CabinetElevationData> items = getCabinetElevationData(cab.getItemId(), mountcnd, classcnd); 
		if (items == null) items = new ArrayList<CabinetElevationData>();
		/*
		 * VERTICALS
		 */
		if (rail != rails_t.FRONT) {
			/* Filter based on mounting */
			mountcnd.clear();
			mountcnd.add(LksDataConstants.RailsUsed.LEFT_SIDE);
			mountcnd.add(LksDataConstants.RailsUsed.RIGHT_SIDE);
			/* Filter by class */
			classcnd.clear();
			classcnd.add(LksDataConstants.Class.RACK_PDU);
			classcnd.add(LksDataConstants.Class.PROBE);
			classcnd.add(LksDataConstants.Class.DATA_PANEL);
			classcnd.add(LksDataConstants.Class.DEVICE);
			classcnd.add(LksDataConstants.Class.NETWORK);

			List<CabinetElevationData> pdus = getCabinetElevationData(cab.getItemId(), mountcnd, classcnd);

			/* get the rear zeroU items first */
			for (CabinetElevationData pdu: pdus) {
				if (LksDataConstants.ZeroU.BACK == pdu.getFacingvaluecode()) {
					items.add(pdu);
				}
			}

			for (CabinetElevationData pdu: pdus) {
				if (LksDataConstants.ZeroU.FRONT == pdu.getFacingvaluecode()) {
					// items.add(pdu);
					items.add(items.size(), pdu);
				}
			}

			/*if (pdus != null) {
		Iterator itr = pdus.iterator();
		while (itr.hasNext()) {
		    Item item = (Item) itr.next();
		    // if (isPduVisible(item, pdus)) 
		    {
			items.add(item);
		    }
		}*/
		}
		return items;
	}

	private boolean isVertical(CabinetElevationData item)   
	{
		return item.getMounting().equals("ZeroU");
	}

	private boolean isLeftRail(CabinetElevationData item)   
	{
		return LksDataConstants.RailsUsed.LEFT_SIDE == item.getMountedrailsposvaluecode();
	}

	private boolean isFacingFront(CabinetElevationData item) {
		return LksDataConstants.ZeroU.FRONT == item.getFacingvaluecode();
	}

	private boolean useItemFrontImg(CabinetElevationData item, ImageProvider.mode_t mode, ImageProvider.rails_t rail)   
	{
		//prefer front for pdus
		if (isVertical(item)) {
			return item.isFrontimage();
		}
		//take facing and viewing mode into account
		long facing = (item.getFacingvaluecode() != null) ? item.getFacingvaluecode(): LksDataConstants.Orientation.ITEM_FRONT_FACES_CABINET_FRONT;
		if (rail == rails_t.FRONT) {
			return facing == LksDataConstants.Orientation.ITEM_FRONT_FACES_CABINET_FRONT;
		} else {//mode == rear
			return facing == LksDataConstants.Orientation.ITEM_REAR_FACES_CABINET_FRONT;
		}
	}

/*
	private boolean isPduVisible(Item pdu, List<Item> pdus)   
	{
		//pdu in front always visible
		if (pdu.getFacingLookup().getLkpValueCode() == LksDataConstants.ZeroU.FRONT) {
			return true;
		} else {
			//pdu in the back, check if completely visible
			boolean left = isLeftRail(pdu);
			for (Iterator<Item> iterator = pdus.iterator(); iterator.hasNext();) {
				Item item = iterator.next();
				if (item.getItemId() != pdu.getItemId()) {//self test
					boolean front = item.getFacingLookup().getLkpValueCode() == LksDataConstants.ZeroU.FRONT;
					if (left == isLeftRail(item) && front) {//just a pdu in front and same rail can cover us
						long start = pdu.getUPosition();
						long stop = start + pdu.getModel().getRuHeight() -1;
						long start2 = item.getUPosition();
						long stop2 = start2 + item.getModel().getRuHeight() -1;
						if ((start2 <= start && stop2 >= start) ||
								(start2 <= stop && stop2 >= stop)) {
							return false;//front pdu some how covers us
						}
					}
				}
			}
		}
		return true;
	}
*/

	private Long getNumOfNRAtUPos(Long cabinetId, Long uPos) {

		Query q = getSession().createSQLQuery("select count(*) from dct_items " +
				"inner join dct_models on dct_items.model_id = dct_models.model_id " +
				"where parent_item_id = :cabinetId and u_position = :uPos and dct_models.mounting = 'Non-Rackable' ");

		q.setLong("cabinetId", cabinetId);
		q.setLong("uPos", uPos);
		// q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);

		return ((BigInteger) q.uniqueResult()).longValue();

	}

	/*
	private Query createElevationQuery(Vector<Long> mountcnd, Vector<Long> classcnd, Item cabinet)   
	{
		Query q = getSession().createQuery("SELECT i " + 
				"FROM Item i inner join fetch i.model " +
				"WHERE " +
				"i.uPosition > 0 AND " +
				"i.mountedRailLookup.lkpValueCode IN (:mountcode) AND " +
				"i.classLookup.lkpValueCode IN (:classcode ) AND " +
				"i.parentItem.itemId = :cabinetid");
		q.setParameterList("mountcode", mountcnd);
		q.setParameterList("classcode", classcnd);
		q.setEntity("cabinetid", cabinet);
		q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);
		q.setCacheable(true);
		return q;
	}
	*/

	private ImageCache.CabinetEntry getCachedImageEntry(Item cab, ImageProvider.mode_t mode, ImageProvider.rails_t rail) {

		return getCache().getCachedCabinetImage(cab, mode, rail);
	}

	private void cacheImageEntry(Item cab, ImageProvider.mode_t mode, rails_t rail, BufferedImage image, List<CabinetElevationData> elevation) {

		getCache().cacheCabinet(cab, mode, rail, image, elevation);
	}

	private void calcDrawingDim() 
	{
		double scale = ((double)m_slot_width)/SLOT_WIDTH;

		m_slot_height = (int)(((double)m_slot_width) / 10 + 0.5);

		m_cab_border_width = (int) (CAB_BORDER_WIDTH * scale + 0.5);
		m_item_border_width = (int) (ITEM_BORDER_WIDTH * scale + 0.5);

		m_cab_border_center = m_cab_border_width/ 2;
		m_item_border_center =m_item_border_width/ 2;

		m_triangle_length = m_slot_height - 2*m_item_border_width; 
		m_triangle_height = (int)(Math.sqrt(3)/2 * m_triangle_length + 0.5);
		m_outer_triangle_w = (int)(Math.sqrt(3)/2 * m_slot_height + 0.5);

		m_slot_item_border_width = m_slot_height + m_item_border_width;

		m_row_text_width = (int) (ROW_TEXT_WIDTH * scale + 0.5);  // u position number   
		m_vert_item_width = (int) (VERT_ITEM_WIDTH * scale + 0.5);
		m_vert_width = m_vert_item_width + 2*m_item_border_width;
		m_vert_overlap_width = (int)(VERT_OVERLAP_WIDTH * scale); // bunty

		m_vert_space = (int) (VERT_SPACE * scale + 0.5);

		m_slot_label_width = U_POS_LABEL_SPACE;

		// m_cab_x = m_outer_triangle_w + m_vert_width + m_vert_space;
		m_cab_x = (m_vert_width * 2) - m_vert_overlap_width + m_vert_space + m_outer_triangle_w;
		m_cab_width = m_slot_width + m_cab_border_width * 2;    
		// m_pic_width = m_cab_x + m_cab_width + 2*m_vert_space + m_row_text_width + m_vert_width + m_outer_triangle_w;
		m_pic_width = m_cab_x + m_cab_width + m_row_text_width + m_outer_triangle_w + m_vert_space + (m_vert_width * 2 - m_vert_overlap_width);

		logger.debug("calculated image width: " + m_pic_width + " slot width is: " + m_slot_width);

		m_cabStroke = new BasicStroke(m_cab_border_width);
		m_itemStroke = new BasicStroke(m_item_border_width);

		m_text_padding = Math.max(2, (int)(scale + 0.5));

		m_itemFont = new Font(ITEM_FONT, Font.PLAIN, (int)(9 * scale + 0.5));
		m_rowFont = m_itemFont;
		m_backgroundItemStroke = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {2f}, 0f);  // new BasicStroke(10);

	}
	
    @SuppressWarnings("unchecked")
    private List<CabinetElevationData> getCabinetElevationData(Long cabientId, Vector<Long> mountcnd, Vector<Long> classcnd) {
        Query q = createElevationQuery(cabientId, mountcnd, classcnd);
        q.setResultTransformer(Transformers.aliasToBean(CabinetElevationData.class));
        
        List<CabinetElevationData> items = (List<CabinetElevationData>) q.list();
        return items;
    }
    
    private Query createElevationQuery(Long cabinetId, Vector<Long> mountcnd, Vector<Long> classcnd) {
        Query q = getSession().createSQLQuery(
                "SELECT item_id as itemId, item_name as itemName, " +
                " model_id as modelId, u_position as uPosition, ru_height as ruHeight, class_lks_id as classLksId, " +
                " parent_item_id as parentItemId, status_lks_id as statusLksId, mounting, facing_lks_id as facingLksId, " +
                " classname as className, classvaluecode as classValueCode, statusvaluecode as statusValueCode, " +
                " modelname as modelName, make, facingvaluecode as facingValueCode, mountedrailsposvaluecode as mountedRailsPosValueCode, " +
                " front_image as frontImage, rear_image as rearImage, shelf_position as shelfPosition " +
                "FROM dc_getcabinetelevationinfo ( "+ cabinetId + " , null)" +
        		"WHERE " +
        		"u_position > 0 AND " +
        		"mountedrailsposvaluecode IN (:mountcnd) AND " +
        		"classvaluecode IN (:classcnd )");
        		q.setParameterList("mountcnd", mountcnd);
        		q.setParameterList("classcnd",  classcnd);
                q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);
                q.setCacheable(true);
        return q;
    }
    
	private CabinetElevationData getCabinetElevationData (List<CabinetElevationData> elevation, long itemId) {
		for (CabinetElevationData d : elevation) {
			if (d != null) {
				if (d.getItemid().longValue() == itemId) {
					return d;
				}
			}
		}
		return null;
	}

}
