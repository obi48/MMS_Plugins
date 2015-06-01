package tvthek.impl;

import java.util.List;

import javafx.scene.image.Image;

/**
 * Class to access the online sources of a TV station.
 * @author Thomas Paireder
 */
public interface MediathekDecoder {

	/**
	 * Returns the name of the TV station.
	 * @return the name
	 */
	public String getStationName();

	/**
	 * Return the logo of the TV station. The image may be downloaded from
	 * the TV station's webpage.
	 * @return the logo
	 */
	public Image getStationLogo();

	/**
	 * Returns a list of all channels that are offered by the TV station.
	 * @return the channels
	 */
	public List<Channel> getChannels();

	/**
	 * Specifies whether downloaded data, like the logo and all channel information,
	 * should be cached between calls of the corresponding methods.
	 * If caching is active, the class doesn't take care whether the locally
	 * stored data are up-to-date.
	 * All locally stored information is deleted when deactivating caching.
	 * @param useCache
	 */
	public void cacheMetadata(boolean useCache);
	
	public boolean isCaching();
	
	public int getMaxOnlineDays();
	
}
