package com.raritan.tdz.reports.imageprovider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.raritan.tdz.domain.Item;

public class ItemImage extends ItemImageBase {

	private static final Logger logger = Logger.getLogger(ItemImage.class);

	public ItemImage(final Session session, final ImageCache cache) {
		super(session, cache);
	}

	public BufferedImage createImage(long itemid) {
		logger.debug("create item image for id " + itemid);

		Item item = getItem(itemid);

		try {
			boolean hasFront = item.getModel().getFrontImage();
			boolean hasRear = item.getModel().getRearImage();
			if (hasFront || hasRear) {
				loadImage(item, hasFront);
			} else {
				logger.debug("neither front or rear image available for item id "
						+ itemid);
			}
		} catch (IOException e) {
			logger.error(
					"can not initiate drawing area for Item "
							+ item.getItemName(), e);
		}

		return flush();
	}

	public String getImagePath(int itemid) {
		logger.debug("get item image path for id " + itemid);
		Item item = getItem(itemid);
		boolean hasFront = item.getModel().getFrontImage();
		boolean hasRear = item.getModel().getRearImage();
		if (hasFront || hasRear) {
			File file = getCache().getImageFile(item, hasFront, false);
			return file.getAbsolutePath();
		}
		return "";
	}
}