package com.raritan.tdz.reports.imageprovider;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.reports.imageprovider.ImageProvider.rails_t;

public class ImageCache {

	private static final Logger logger = Logger.getLogger(ImageCache.class);

	private static final String IMG_PATH = "/var/oculan/model_lib/images/devices";
	// System.getProperty("catalina.home") + File.separator + "webapps" +
	// File.separator + "gdcitdz" + File.separator + "images" + File.separator +
	// "devices";
	private static final String FRONT_IMG_PATH = IMG_PATH + File.separator
			+ "frontpngimages";
	private static final String REAR_IMG_PATH = IMG_PATH + File.separator
			+ "rearpngimages";

	/*
	 * control whether cache should be enabled or not
	 */
	private static final boolean IMAGE_CACHE_ENABLED = true;

	/*
	 * cache size/depth is determined by available memory, stop allocating if
	 * less than MIN_FREE_MEM of total mem is free
	 */
	private static final float MIN_FREE_MEM = 0.1f; // 10%

	/*
	 * helper classes
	 */
	public class ImageEntry {
		public int hits;
		public BufferedImage img;

		public ImageEntry() {
			hits = 0;
			img = null;
		}

		public void touch() {
			hits++;
		}

		public void free() {
			img = null;
		}

		public BufferedImage cloneImg() {
			return ImageCache.cloneImg(img);
		}
	}

	public class CabinetEntry extends ImageEntry {
		public CabinetEntry() {
			super();
			items = null;
			hit_size = 0;
		}

		public int hit_size;
		public List<CabinetElevationData> items;

		@Override
		public void touch() {
			/*
			 * complex drawing -> bigger hit size
			 */
			hits += hit_size;
		}

		@Override
		public void free() {
			super.free();
			items = null;
		}
	}

	public class ImageEntryComparator implements Comparator<ImageEntry> {
		@Override
		public int compare(ImageEntry lhs, ImageEntry rhs) {
			if (lhs.hits < rhs.hits) {
				return -1;
			}
			if (lhs.hits > rhs.hits) {
				return 1;
			}
			return 0;
		}
	}

	private class ModelEntry {
		public ImageEntry front;
		public ImageEntry rear;
		public ImageEntry front_thumb;
		public ImageEntry rear_thumb;

		public ModelEntry() {
			front = new ImageEntry();
			rear = new ImageEntry();
			front_thumb = new ImageEntry();
			rear_thumb = new ImageEntry();
		}
	}

	private PriorityQueue<ImageEntry> m_hits_queue;// order by hits to determine
	// images used most
	private HashMap<Long, ModelEntry> m_model_map;// store entry per model to
	// count hits continues
	private HashMap<Long, CabinetEntry> m_cab_cache_map;// store entry per
	// cabinet to count hits
	// continues
	private ImageProvider.mode_t m_current_cab_mode;// just support one mode at
	// a time
	private ImageProvider.rails_t m_current_cab_rail;// just support one mode at
	// a time

	public ImageCache() {
		m_model_map = new HashMap<Long, ModelEntry>();
		m_hits_queue = new PriorityQueue<ImageEntry>(25,
				new ImageEntryComparator());
		m_cab_cache_map = new HashMap<Long, CabinetEntry>();
		m_current_cab_mode = null;
		m_current_cab_rail = null;
	}

	/*
	 * do a real copy
	 */
	static public BufferedImage cloneImg(BufferedImage image) {
		return new BufferedImage(image.getColorModel(), image.copyData(null),
				image.isAlphaPremultiplied(), null);
	}

	public BufferedImage getReadOnlyImage(Item item, boolean front,
			boolean try_thumb) throws IOException {

		return getReadOnlyImage(item.getModel(), front, try_thumb);

	}

	public BufferedImage getReadOnlyImage(ModelDetails model, boolean front,
			boolean try_thumb) throws IOException {
		if (!IMAGE_CACHE_ENABLED)
			return loadModelImage(model, front, try_thumb);
		return lookUpModelImg(model, front, false, try_thumb);
	}

	public BufferedImage getWriteableImageCopy(Item item, boolean front,
			boolean try_thumb) throws IOException {
		return getWriteableImageCopy(item.getModel(), front, try_thumb);
	}

	public BufferedImage getWriteableImageCopy(ModelDetails model,
			boolean front, boolean try_thumb) throws IOException {
		if (!IMAGE_CACHE_ENABLED)
			return loadModelImage(model, front, try_thumb);
		return lookUpModelImg(model, front, true, try_thumb);
	}

	public File getImageFile(Item item, boolean front, boolean thumb) {
		return getImageFile(item.getModel(), front, thumb);
	}

	public File getImageFile(ModelDetails model, boolean front, boolean thumb) {
		
		return getImageFile(model.getModelDetailId(), front, thumb);
	}
		
	public File getImageFile(long modelid, boolean front, boolean thumb) {
		
		String path;
		String filename = String.valueOf(modelid);
		if (front) {
			path = new String(FRONT_IMG_PATH);
			filename += "_F";
			if (thumb)
				filename += "T";
		} else {
			path = new String(REAR_IMG_PATH);
			filename += "_R";
			if (thumb)
				filename += "T";
		}
		filename += ".png";
		return new File(path + File.separator + filename);
	}

	public CabinetEntry getCachedCabinetImage(Item cab,
			ImageProvider.mode_t mode, rails_t rail) {
		CabinetEntry result = null;
		if (mode == m_current_cab_mode && rail == m_current_cab_rail) {
			CabinetEntry entry = getCabinetEntry(cab.getItemId());
			updateCachePrio(entry);

			if (entry.img != null) {// check if we released the image prior (or
				// it may never was there)
				result = new CabinetEntry();
				result.img = entry.cloneImg();// deep copy needed
				result.items = entry.items;
			}
		}
		return result;
	}

	public void cacheCabinet(Item cab, ImageProvider.mode_t mode, rails_t rail,
			BufferedImage image, List<CabinetElevationData> elevation) {
		if (!IMAGE_CACHE_ENABLED)
			return;

		if (mode != m_current_cab_mode || rail != m_current_cab_rail) {// normally
			// mode
			// shall
			// not
			// change
			clearCabinetCacheEntries();
			m_current_cab_mode = mode;
			m_current_cab_rail = rail;
		}
		Long id = cab.getItemId();
		CabinetEntry entry = getCabinetEntry(id);
		entry.hit_size = elevation.size();// lean, but take item count as
		// indicator of drawing complexity
		if (handleCaching(entry, image, true)) {
			entry.items = elevation;// add elevation too if cabinet was cached
		}
	}

	public void clearCabinetCacheEntries() {
		m_hits_queue.removeAll(m_cab_cache_map.values());
		m_cab_cache_map.clear();
		m_current_cab_mode = null;
		m_current_cab_rail = null;
	}

	private BufferedImage loadModelImage(ModelDetails model, boolean front,
			boolean try_thumb) throws IOException {
		return loadModelImage ( model.getModelDetailId(), front, try_thumb);
	}
	
	private BufferedImage loadModelImage(long modelid, boolean front,
			boolean try_thumb) throws IOException {

		File file = getImageFile(modelid, front, try_thumb);
		BufferedImage img;
		logger.debug("read image file " + file.getAbsolutePath());
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			if (try_thumb)
				img = loadModelImage(modelid, front, false);// ok thumb not there
			// use try normal
			else
				throw e;
		}
		return img;
	}

	private BufferedImage lookUpModelImg(ModelDetails model, boolean front,
			boolean copy, boolean try_thumb) throws IOException {
		Long modelid = model.getModelDetailId();
		
		return lookUpModelImg (modelid, front, copy, try_thumb);
	}
		
	private BufferedImage lookUpModelImg(long modelid, boolean front,
			   boolean copy, boolean try_thumb) throws IOException {
		
		ModelEntry entry = getModelEntry(modelid);
		ImageEntry imgentry;
		if (try_thumb) {
			imgentry = front ? entry.front_thumb : entry.rear_thumb;
		} else {
			imgentry = front ? entry.front : entry.rear;
		}

		BufferedImage img;
		if (imgentry.img != null) {
			updateCachePrio(imgentry);
			logger.debug("cache hit for model: " + modelid);
			if (copy)
				img = imgentry.cloneImg();
			else
				img = imgentry.img;
		} else {
			img = loadModelImage(modelid, front, try_thumb);
			handleCaching(imgentry, img, copy);
		}
		return img;
	}

	private boolean handleCaching(ImageEntry current, BufferedImage image,
			boolean copy) {
		boolean cache = false;
		long mem_free = Runtime.getRuntime().freeMemory();
		long mem_total = Runtime.getRuntime().totalMemory();

		long img_size = getImgSize(image);

		if (!copy) {// no copy no coast, at least now...
			cache = true;
		} else if (mem_free > img_size
				&& mem_free - img_size > MIN_FREE_MEM * mem_total) {// leave at
			// least 10
			// %
			logger.debug("Enough MEM free " + mem_free + " Size " + img_size
					+ " Total " + mem_total);
			cache = true;
		} else {
			/*
			 * loop and try to make space
			 */
			while (m_hits_queue.size() > 0
					&& m_hits_queue.peek().hits < current.hits) {
				logger.debug("Remove item from cache FREE " + mem_free
						+ " Size " + img_size + " Total " + mem_total);
				ImageEntry obsolete = m_hits_queue.poll();
				mem_free += getImgSize(obsolete.img);
				obsolete.free();
				if (mem_free > img_size
						&& mem_free - img_size > MIN_FREE_MEM * mem_total) {
					cache = true;
					break;
				}
			}
		}

		if (cache) {
			logger.debug("cache item");
			addToCachePrioQueue(current, image, copy);
		}
		return cache;
	}

	private void addToCachePrioQueue(ImageEntry current, BufferedImage image,
			boolean copy) {
		if (copy)
			current.img = cloneImg(image);
		else
			current.img = image;
		current.touch();
		m_hits_queue.add(current);
	}

	private void updateCachePrio(ImageEntry current) {
		current.touch();
		if (m_hits_queue.remove(current)) {
			m_hits_queue.add(current);// re add with new prio
		}
	}

	private ModelEntry getModelEntry(Long id) {
		ModelEntry entry = m_model_map.get(id);
		if (entry == null) {
			entry = new ModelEntry();
			m_model_map.put(id, entry);
		}
		return entry;
	}

	private CabinetEntry getCabinetEntry(Long id) {
		CabinetEntry entry = m_cab_cache_map.get(id);
		if (entry == null) {
			entry = new CabinetEntry();
			m_cab_cache_map.put(id, entry);
		}
		return entry;
	}

	private long getImgSize(BufferedImage img) {
		return img.getHeight() * img.getWidth() * 4;
	}
	

   public BufferedImage getReadOnlyImage(long modelId, boolean front,
           boolean try_thumb) throws IOException {
       if (!IMAGE_CACHE_ENABLED)
           return loadModelImage(modelId, front, try_thumb);
       return lookUpModelImg(modelId, front, false, try_thumb);
   }

}
