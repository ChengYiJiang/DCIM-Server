package com.raritan.tdz.reports.imageprovider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.raritan.tdz.domain.Item;

public class ItemImageBase {
	/*
	 * right now the thumbs are to ugly -> 0
	 */
	public static final int THUMB_WIDTH = 0;// normal 120, 0 means don't use
	// thumbs
	private static final Logger logger = Logger.getLogger(ItemImageBase.class);

	private static final String TRUNCATE_TO_FIT_INDICATOR = "...";

	private Graphics2D m_g2;

	private Session m_session = null;
	private ImageCache m_cache = null;

	public ImageCache getCache() {
		return m_cache;
	}

	private Color m_bg = null;

	private BufferedImage m_img = null;

	public ItemImageBase(final Session session, final ImageCache cache) {
		m_session = session;
		m_cache = cache;
		m_bg = new Color(0xff, 0xff, 0xff);
	}

	protected Session getSession() {
		return m_session;
	}

	protected BufferedImage getImg() {
		return m_img;
	}

	protected void initDrawingArea(int w, int h) {
		m_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		m_g2 = m_img.createGraphics();
		m_g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		m_g2.setBackground(m_bg);
		m_g2.clearRect(0, 0, w, h);
	}

	protected void initDrawingArea(Item item, boolean front) throws IOException {
		m_img = m_cache.getWriteableImageCopy(item, front, false);

		m_g2 = m_img.createGraphics();
		m_g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	protected void initDrawingArea(BufferedImage img) throws IOException {
		m_img = ImageCache.cloneImg(img);

		m_g2 = m_img.createGraphics();
		m_g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	protected void loadImage(Item item, boolean front) throws IOException {
		m_img = m_cache.getReadOnlyImage(item, front, false);
	}

	protected BufferedImage flush() {
		logger.debug("flushing image ...");
		BufferedImage temp = m_img;
		m_g2 = null;
		m_img = null;

		return temp;
	}
	
	protected boolean drawImage(int x, int w, int y, int h, boolean keepAspect,
			boolean front, Item item) throws Exception {
		
		CabinetElevationData c = new CabinetElevationData();
		c.setItemid(new BigInteger(new Long(item.getItemId()).toString()));
		c.setItemname(item.getItemName() != null ? item.getItemName() : "");
		Long modelid = item.getModel() != null ? item.getModel().getModelDetailId() : -1;
		c.setModelid(new BigInteger(modelid.toString()));
		c.setMounting(item.getModel() != null ? item.getModel().getMounting() : "");
		c.setFrontimage(item.getModel() != null ? item.getModel().getFrontImage(): false);
		c.setRearimage(item.getModel() != null ? item.getModel().getRearImage(): false);

		return drawImage (x, w, y, h, keepAspect, front, c);

	}

	protected boolean drawImage(int x, int w, int y, int h, boolean keepAspect,
			boolean front, CabinetElevationData item) throws Exception {
		boolean success = false;

		if ((front && item.isFrontimage())
				|| (!front && item.isRearimage())) {
			try {
				BufferedImage slotImg = m_cache.getReadOnlyImage(item.getModelid(), front,
						w <= THUMB_WIDTH && item.getMounting().equals("Rackable"));
				if (keepAspect) {
					int imgW = slotImg.getWidth();
					int imgH = slotImg.getHeight();
					double ratio = (double) imgH / (double) imgW;
					double ratio2 = (double) imgW / (double) imgH;
					if (w * ratio <= h) {
						h = (int) (w * ratio + 0.5);
					} else if (h * ratio2 <= w) {
						w = (int) (h * ratio2 + 0.5);
					} else {
						int diff = (int) (0.1 * h);
						do {
							h -= diff;
							if (h <= 0)
								throw new Exception(
										"error calculating image size");
						} while (h * ratio2 > w);
						w = (int) (h * ratio2 + 0.5);
					}
				}
				m_g2.drawImage(slotImg, x, y, w, h, null);
				success = true;
			} catch (IOException e) {
				logger.error("IO Error for Model: '"
						+ item.getModelname() + "' mode: "
						+ (front ? "front" : "rear"));
			}
		}
		return success;
	}

	protected void fillRect(int x, int w, int y, int h, Color color) {
		m_g2.setColor(color);
		Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
		m_g2.fill(rect);
	}

	protected void fillRect(int x, int w, int y, int h, Color color,
			GradientPaint gp) {
		m_g2.setColor(color);
		Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
		m_g2.setPaint(gp);
		m_g2.fill(rect);

		// TexturePaint backGrdPaint = new TexturePaint("backgrditem.png",
		// anchor)
		/*
		 * File imgFile = new File("/home/bunty/backgrditem.png"); BufferedImage
		 * backImg; try {
		 * 
		 * backImg = ImageIO.read(imgFile);
		 * 
		 * TexturePaint backGrdPaint = new TexturePaint(backImg, new
		 * Rectangle2D.Double(x, y, w, h));
		 * 
		 * m_g2.setPaint(backGrdPaint);
		 * 
		 * m_g2.fill(rect);
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

	}

	protected void drawCenteredString(String str, int x, int w, int y, int h,
			int padding, boolean vertical, Font font, Color color) {
		m_g2.setColor(color);
		m_g2.setFont(font);

		Rectangle2D rect;
		TextLayout layout;

		int wrapwidth = vertical ? h : w;
		wrapwidth -= 2 * padding;
		int maxH = vertical ? w : h;
		int maxfH = 0;

		FontRenderContext ctx = m_g2.getFontRenderContext();
		AttributedString styled_str = new AttributedString(str,
				font.getAttributes());
		LineBreakMeasurer measurer = new LineBreakMeasurer(
				styled_str.getIterator(), ctx);

		int lastpos = 0;
		int currentpos;
		ArrayList<TextLayout> lines = new ArrayList<TextLayout>();

		// 1st round check what we can draw
		while (true) {
			currentpos = measurer.getPosition();
			layout = measurer.nextLayout(wrapwidth);

			if (layout == null)
				break;

			rect = layout.getPixelBounds(ctx, 0, 0);
			if (lines.size() > 0) {
				maxfH = (int) Math.max(maxfH, rect.getHeight() + padding);
			} else {
				maxfH = (int) rect.getHeight();
			}
			if (maxH >= (lines.size() + 1) * maxfH) {
				lines.add(layout);
				lastpos = currentpos;
			} else {
				if (lines.size() > 0) {// truncate line before
					lines.remove(lines.size() - 1);
					lines.add(truncate(str.substring(lastpos), wrapwidth, font,
							ctx));
				}
				break;
			}
		}

		if (lines.size() == 0) {
			logger.error("can not draw '" + str + "' - not enough space to fit");
			return;
		}

		int offset = (maxH - maxfH * lines.size()) / 2;
		if (vertical) {
			x += offset;
		} else {
			y += offset;
		}
		// do not allow to float...just to make sure ;-)
		m_g2.setClip(x, y, w, h);
		// 2nd round - draw
		Iterator<TextLayout> itr = lines.iterator();
		while (itr.hasNext()) {
			layout = itr.next();
			rect = layout.getPixelBounds(ctx, 0, 0);
			int fw = (int) rect.getWidth();
			int fx, fy;

			if (vertical) {
				x += maxfH;
				fx = x;
				fy = y + (wrapwidth - fw) / 2 + fw + padding;
			} else {
				y += maxfH;
				fx = x + (wrapwidth - fw) / 2 + padding;
				fy = y;
			}
			drawTextLayout(layout, fx, fy, vertical);
		}
		m_g2.setClip(null);
	}

	protected TextLayout truncate(String str, int max_w, Font font,
			FontRenderContext ctx) {
		AttributedString styled_str = new AttributedString(str,
				font.getAttributes());
		LineBreakMeasurer measurer = new LineBreakMeasurer(
				styled_str.getIterator(), ctx);
		TextLayout layout = measurer.nextLayout(max_w);
		Rectangle2D rect = layout.getPixelBounds(ctx, 0, 0);

		int pos = measurer.getPosition();
		int last = (int) (Math.floor(max_w / rect.getWidth() * pos));// roughly
		// initial
		// cut
		str = str.substring(0, Math.min(str.length() - 1, last));

		int length;
		do {
			styled_str = new AttributedString(str + TRUNCATE_TO_FIT_INDICATOR,
					font.getAttributes());
			measurer = new LineBreakMeasurer(styled_str.getIterator(), ctx);
			layout = measurer.nextLayout(max_w);
			pos = measurer.getPosition();
			length = str.length() + TRUNCATE_TO_FIT_INDICATOR.length();
			if (pos < length) {
				str = str.substring(0, str.length() - 1);// accurate way
			} else {
				break;
			}
		} while (str.length() > 0);

		return layout;
	}

	protected void fillRectAndDrawString(String str, int x, int y, int padding,
			Font font, Color fontcolor, Color bgcolor, Color borderColor,
			BasicStroke border) {
		FontRenderContext ctx = m_g2.getFontRenderContext();
		GlyphVector vec = font.createGlyphVector(ctx, str);
		Rectangle2D rect = vec.getPixelBounds(ctx, 0, 0);
		int bw = (int) border.getLineWidth();

		int fw = (int) rect.getWidth();
		int fh = (int) rect.getHeight();
		fillRect(x + bw, fw + 2 * padding, y + bw, fh + 2 * padding, bgcolor);
		drawBorder(x, y, fw + 2 * padding + bw, fh + 2 * padding + bw,
				borderColor, border);

		m_g2.setColor(fontcolor);
		m_g2.setFont(font);
		drawGlyphVector(vec, x + padding, y + fh + padding + bw, false);
	}

	protected void drawTextLayout(TextLayout layout, int x, int y,
			boolean vertical) {
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (vertical)
			m_g2.rotate(-Math.PI / 2, x, y);
		layout.draw(m_g2, x, y);
		if (vertical)
			m_g2.rotate(Math.PI / 2, x, y);
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	protected void drawGlyphVector(GlyphVector vec, int x, int y,
			boolean vertical) {
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (vertical)
			m_g2.rotate(-Math.PI / 2, x, y);
		m_g2.drawGlyphVector(vec, x, y);
		if (vertical)
			m_g2.rotate(Math.PI / 2, x, y);
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	protected void drawString(String str, int x, int y, Font font, Color color) {
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		m_g2.setColor(color);
		m_g2.setFont(font);
		m_g2.drawString(str, x, y);
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	protected void drawLine(double x1, double y1, double x2, double y2,
			Color color, Stroke stroke) {
		m_g2.setColor(color);
		m_g2.setStroke(stroke);
		m_g2.draw(new Line2D.Double(x1, y1, x2, y2));
	}

	protected void drawBorder(double x, double y, double w, double h,
			Color color, Stroke stroke) {
		m_g2.setColor(color);
		m_g2.setStroke(stroke);
		Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
		m_g2.draw(rect);
	}

	protected void drawFilledPolygon(Polygon poly, Color fillcolor,
			Color bordercolor, Stroke stroke) {
		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		m_g2.setStroke(stroke);
		m_g2.setColor(bordercolor);
		m_g2.drawPolygon(poly);

		m_g2.setColor(fillcolor);
		m_g2.fill(poly);

		m_g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	protected Item getItem(long itemid) {
		Query q = m_session
				.createQuery("select i from Item i inner join fetch i.model where i.itemId = :itemid");
		q.setLong("itemid", itemid);
		q.setCacheable(true);
		q.setReadOnly(ImageProvider.OPTIMIZE_FOR_READ_ONLY_CTX);
		q.setMaxResults(1);

		return (Item) q.uniqueResult();
	}

	protected boolean isRackable(Item item) {
		return item.getModel().getMounting().equals("Rackable");
	}
	
	protected void setStroke(Stroke stroke) {

		m_g2.setStroke(stroke);
	}

}
