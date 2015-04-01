package com.raritan.tdz.reports.imageprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;


public class ImageProvider {
	public static final boolean OPTIMIZE_FOR_READ_ONLY_CTX = true;

	private static final Logger logger = Logger.getLogger(ImageProvider.class);

	private boolean m_privateSession = false;

	private ImageCache m_cache;
	private CabinetElevation m_cab;
	private ItemPorts m_itemport;
	private ItemImage m_itemimg;

	private SessionFactory m_sessionFactory;
	private Session m_session;
	private Transaction m_tx;

	public enum mode_t {
		TEXT,
		IMAGE
	};

	public enum rails_t {
		FRONT,
		REAR,
		BOTH
	};

	public ImageProvider(final Session session) {
		m_session = session;
		m_cache = new ImageCache();
		//if no session it present create a own
		if (m_session == null) {
			logger.info("no hibernate session present - creating a new");
			m_privateSession = true;
			m_sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
			m_session = m_sessionFactory.getCurrentSession();
			m_tx = m_session.beginTransaction();
		}
	}

	public ByteArrayOutputStream createPngByteStream(BufferedImage img) throws IOException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(img, "png", stream);
		return stream;
	}

	public ByteArrayOutputStream createJpegByteStream(BufferedImage img) throws IOException
	{
		// Get a ImageWriter for jpeg format.
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
		if (!writers.hasNext()) {
			throw new IllegalStateException("No writers found");
		}
		ImageWriter writer = (ImageWriter) writers.next();

		// Create the ImageWriteParam to compress the image.
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(1);//best quality

		// The output will be a ByteArrayOutputStream (in memory)
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(stream);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(img, null, null), param);
		ios.flush(); // otherwise the buffer size will be zero! 

		return stream;
	}

	public void cleanup() {
		if (m_privateSession) {
			logger.info("close custom hibernate session");
			m_tx.commit();
			m_sessionFactory.close();
		}
		m_session = null;
		m_tx = null;
		m_sessionFactory = null;

		m_cab = null;
		m_itemport = null;
		m_itemimg = null;
		m_cache = null;
	}

	public CabinetElevation getCabinetElevation() {
		if (m_cab == null) {
			m_cab = new CabinetElevation(m_session, m_cache);
		}
		return m_cab;
	}

	public ItemPorts getItemPorts() {
		if (m_itemport == null) {
			m_itemport = new ItemPorts(m_session, m_cache);
		}
		return m_itemport;
	}

	public ItemImage getItemImage() {

		if (m_itemimg == null) {
			m_itemimg = new ItemImage(m_session, m_cache);
		}

		return m_itemimg;

	}
}
