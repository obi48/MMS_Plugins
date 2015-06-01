package tvthek.impl;

import java.time.Duration;

/**
 * A part of a TV show with title, description, duration and streaming URL.
 * @author Thomas Paireder
 */
public interface Segment {
	
	/**
	 * Returns the title.
	 * @return the title
	 */
	public String getTitle();
	
	/**
	 * Returns the description.
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * The duration of the segment.
	 * @return the duration
	 */
	public Duration getDuration();
	
	/**
	 * The URL from where the segment can be streamed.
	 * @return the URL
	 */
	public String getStreamURL();

}
