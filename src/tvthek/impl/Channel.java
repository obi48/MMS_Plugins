package tvthek.impl;

import java.time.LocalDate;
import java.util.List;

import javafx.scene.image.Image;

/**
 * A TV channel with name and logo.
 * @author Thomas Paireder
 */
public interface Channel {
	
	/**
	 * Returns the full name of the channel.
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Returns the logo of the channel. The image may be downloaded from
	 * the TV station's webpage.
	 * @return the logo image
	 */
	public Image getLogo();
	
	/**
	 * Returns the next show on this channel that
	 * is available as a livestream.
	 * Accessing this information may take some time.
	 * @return the show
	 */
	public Show getLiveShow();
	
	/**
	 * Returns all shows of the given date that are available for streaming
	 * in the online archive of the TV station.
	 * Accessing this information may take some time.
	 * @param date the broadcast date
	 * @return list of shows
	 */
	public List<Show> getArchivedShows(LocalDate date);
	
	/**
	 * Specifies whether downloaded information, like the logo, the live show
	 * and the list of archived shows, should be cached between calls
	 * of the corresponding methods.
	 * If caching is active, the class doesn't take care whether the locally
	 * stored data are up-to-date.
	 * All locally stored information is deleted when deactivating caching.
	 * @param useCache
	 */
	public void cacheMetadata(boolean useCache);
	
}
