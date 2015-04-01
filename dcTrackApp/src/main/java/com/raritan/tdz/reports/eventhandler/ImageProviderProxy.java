package com.raritan.tdz.reports.eventhandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.script.instance.IImageInstance;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.hibernate.Session;

import com.raritan.tdz.reports.imageprovider.CabinetElevation;
import com.raritan.tdz.reports.imageprovider.ImageProvider;
import com.raritan.tdz.reports.imageprovider.ItemImage;
import com.raritan.tdz.reports.imageprovider.ItemPorts;

public class ImageProviderProxy {

	private static final Logger logger = Logger
			.getLogger(ImageProviderProxy.class);

	private ImageProvider.mode_t m_cabmode = ImageProvider.mode_t.TEXT;
	private ImageProvider.rails_t m_cabrail = ImageProvider.rails_t.FRONT;
	private ImageProvider m_provider;

	public ImageProviderProxy(IReportContext reportContext) {
		Object cabModeParam = reportContext
				.getParameterValue(ReportConstants.CAB_IMAGEMODE_PARAM_ID);
		Object cabRailParam = reportContext
				.getParameterValue(ReportConstants.CAB_IMAGERAIL_PARAM_ID);

		// Set the mode requested by the user
		if (cabModeParam != null && ((String) cabModeParam).length() > 0) {
			String mode = ((String) cabModeParam).toLowerCase();
			logger.debug("cabinet image mode is '" + mode + "'");

			if (mode.equals(ReportConstants.IMAGEMODE_TEXT)) {
				m_cabmode = ImageProvider.mode_t.TEXT;
			} else if (mode.equals(ReportConstants.IMAGEMODE_IMAGE)) {
				m_cabmode = ImageProvider.mode_t.IMAGE;
			} else {
				logger.warn("unknown cabinet image mode: '" + mode + "'");
			}
		}

		// Set the rails requested by the user
		if (cabRailParam != null && ((String) cabRailParam).length() > 0) {
			String rail = ((String) cabRailParam).toLowerCase();
			logger.debug("cabinet image rail is '" + rail + "'");

			if (rail.equals(ReportConstants.IMAGERAIL_FRONT)) {
				m_cabrail = ImageProvider.rails_t.FRONT;
			} else if (rail.equals(ReportConstants.IMAGERAIL_REAR)) {
				m_cabrail = ImageProvider.rails_t.REAR;
			} else {
				logger.warn("unknown cabinet image rail: '" + rail + "'");
			}
		}

		Object obj = reportContext.getAppContext().get(ReportConstants.SESSION_ID);
		Session session = null;
		if (obj != null) {
			logger.info("hibernate session present");
			session = (Session) obj;
		}
		m_provider = new ImageProvider(session);
	}

	public ImageProviderProxy(IReportContext reportContext, String cabRailParam) {
		Object cabModeParam = reportContext
				.getParameterValue(ReportConstants.CAB_IMAGEMODE_PARAM_ID);

		// Set the mode requested by the user
		if (cabModeParam != null && ((String) cabModeParam).length() > 0) {
			String mode = ((String) cabModeParam).toLowerCase();
			logger.debug("cabinet image mode is '" + mode + "'");

			if (mode.equals(ReportConstants.IMAGEMODE_TEXT)) {
				m_cabmode = ImageProvider.mode_t.TEXT;
			} else if (mode.equals(ReportConstants.IMAGEMODE_IMAGE)) {
				m_cabmode = ImageProvider.mode_t.IMAGE;
			} else {
				logger.warn("unknown cabinet image mode: '" + mode + "'");
			}
		}

		// Set the rails requested by the user
		if (cabRailParam != null && cabRailParam.length() > 0) {
			String rail = ((String) cabRailParam).toLowerCase();
			logger.debug("cabinet image rail is '" + rail + "'");

			if (rail.equals(ReportConstants.IMAGERAIL_FRONT)) {
				m_cabrail = ImageProvider.rails_t.FRONT;
			} else if (rail.equals(ReportConstants.IMAGERAIL_REAR)) {
				m_cabrail = ImageProvider.rails_t.REAR;
			} else {
				logger.warn("unknown cabinet image rail: '" + rail + "'");
			}
		}

		Object obj = reportContext.getAppContext().get(ReportConstants.SESSION_ID);
		Session session = null;
		if (obj != null) {
			logger.info("hibernate session present");
			session = (Session) obj;
		}
		m_provider = new ImageProvider(session);
	}

	public void cleanup() {
		m_provider.cleanup();
		m_provider = null;
	}

	public void handleCabImg(IImageInstance image, int cabid) {
		try {
			
			image.setData(m_provider.createPngByteStream(createCabImg(cabid, m_cabrail)).toByteArray());
			
		} catch (Exception e) {
			
			logger.error("Error creating Cabinet Image Id('" + cabid + "')", e);
			e.printStackTrace();
			
		}
	}

	public void handleCabImgByItemId(IImageInstance image, int itemid) {
		try {

			CabinetElevation cabelev = m_provider.getCabinetElevation();

			logger.info("create cabinet image for item id: " + itemid
					+ " mode: " + m_cabmode.toString() + " rail: " + m_cabrail);

			BufferedImage img = cabelev.createImageByItem(itemid, m_cabmode,
					m_cabrail);
			logger.debug("image resolution " + img.getWidth() + "x"
					+ img.getHeight());

			image.setData(m_provider.createPngByteStream(img).toByteArray());

		} catch (Exception e) {

			logger.error("Error creating Cabinet/Item Image Id('" + itemid
					+ "')", e);
			e.printStackTrace();
		}
	}
	
	public void handleCabImgFrontByItemId(IImageInstance image, int itemid, ImageProvider.rails_t rails) {
		try {

			CabinetElevation cabelev = m_provider.getCabinetElevation();

			logger.info("create cabinet image for item id: " + itemid
					+ " mode: " + m_cabmode.toString() + " rail: " + m_cabrail);

			BufferedImage img = cabelev.createImageByItem(itemid, m_cabmode, rails);
					//m_cabrail);
			logger.debug("image resolution " + img.getWidth() + "x"
					+ img.getHeight());

			image.setData(m_provider.createPngByteStream(img).toByteArray());

		} catch (Exception e) {

			logger.error("Error creating Cabinet/Item Image Id('" + itemid
					+ "')", e);
			e.printStackTrace();
		}
	}


	public void handleItemPortsImg(IImageInstance image, int itemid, int imagetype) {
		try {
			ItemPorts itemports = m_provider.getItemPorts();
			logger.info("create item ports image for item id: " + itemid);
			BufferedImage img = itemports.createImage(itemid, imagetype);
			if (img == null) {
				logger.warn("image creation failed - hide image");
				/*
				 * workaround, otherwise birt pdf renderer would add an error
				 * message to the report
				 */
				img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
				/*
				 * in case of error hide picture
				 */
				image.setHeight("0px");
			} else {
				int w = img.getWidth();
				int h = img.getHeight();
				logger.debug("original image resolution " + w + "x" + h);
				image.setHeight(h + "px");
			}
			ByteArrayOutputStream imgstream = m_provider
					.createPngByteStream(img);
			logger.debug("result image size " + imgstream.size());
			image.setData(imgstream.toByteArray());
		} catch (Exception e) {
			logger.error(
					"Error creating Item Ports Image Id('" + itemid + "')", e);
			e.printStackTrace();
		}
	}

	public byte[] createCabImgByteStream(int cabid, ImageProvider.rails_t rails) {
		byte[] stream = null;
		try {
			stream = m_provider.createPngByteStream(createCabImg(cabid, rails))
					.toByteArray();
			
		} catch (Exception e) {
		}
		return stream;
	}
	
	public byte[] createItemImgByteStream(int itemid) {
		byte[] stream = null;
		try {
			stream = m_provider.createPngByteStream(createItemImg(itemid))
					.toByteArray();
		} catch (Exception e) {
		}
		return stream;
	}

	public String getItemImagePath(int itemid) {
		
		return m_provider.getItemImage().getImagePath(itemid);
		
	}

	private BufferedImage createCabImg(int cabid, ImageProvider.rails_t rails) {
		BufferedImage img = null;
		try {

			CabinetElevation cabelev = m_provider.getCabinetElevation();
			logger.info("create cabinet image  id: " + cabid + " mode: "
					+ m_cabmode.toString());
			
			//img = cabelev.createImage(cabid, m_cabmode, m_cabrail);
			img = cabelev.createImage(cabid, m_cabmode, rails);
			logger.debug("image resolution " + img.getWidth() + "x"
					+ img.getHeight());
		} catch (Exception e) {
			logger.error("Error creating Cabinet Image Id('" + cabid + "')", e);
			e.printStackTrace();
		}
		return img;
	}

	public byte[] createCabinetElevationImgByteStream(int cabid, ImageProvider.rails_t rails) {
		byte[] stream = null;
		try {
			stream = m_provider.createPngByteStream(createCabinetElevationImg(cabid, rails))
					.toByteArray();
			
		} catch (Exception e) {
		}
		return stream;
	}
	
	private BufferedImage createCabinetElevationImg(int cabid, ImageProvider.rails_t rails) {
		BufferedImage img = null;
		try {
			logger.info("create cabinet image  id: " + cabid + " mode: "
					+ m_cabmode.toString());

			CabinetElevation cabelev = m_provider.getCabinetElevation();
			BufferedImage cabImg = cabelev.createImage(cabid, m_cabmode, rails);
			BufferedImage paddingImg = cabelev.createPaddingImage(cabid, m_cabmode, rails);
			img = concatenateImage (paddingImg, cabImg);
			logger.debug("image resolution " + img.getWidth() + "x" + img.getHeight());
		} catch (Exception e) {
			logger.error("Error creating Cabinet Image Id('" + cabid + "')", e);
			e.printStackTrace();
		}
		return img;
	}


    private BufferedImage concatenateImage(BufferedImage paddingImage, BufferedImage cabinet)
    {
    	BufferedImage img = null;
		int paddingImageWidth = paddingImage.getWidth();
		int paddingImageHeight = paddingImage.getHeight();
			
		//int cabinetImageWidth = cabinet.getWidth();
		int cabbinetImageHeight = cabinet.getHeight();
		try {
			img = new BufferedImage(paddingImageWidth, paddingImageHeight+cabbinetImageHeight, BufferedImage.TYPE_INT_RGB);
			img.createGraphics().drawImage(paddingImage, 0, 0, null);
			img.createGraphics().drawImage(cabinet, 0, paddingImageHeight, null);
		} catch (Exception e) {
			logger.error("Error concatenating image", e);
			e.printStackTrace();
		}
		return img;
    }
	
	
	private BufferedImage createItemImg(int itemid) {
		
		BufferedImage img = null;
		
		try {
			
			ItemImage itemimg = m_provider.getItemImage();
			logger.info("create image  id: " + itemid);
			
			img = itemimg.createImage(itemid);
			
			logger.debug("image resolution " + img.getWidth() + "x" + img.getHeight());
			
		} catch (Exception e) {
			logger.error("Error creating Item Image Id('" + itemid + "')", e);
			e.printStackTrace();
		}
		return img;
	}
}
