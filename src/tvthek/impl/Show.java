package tvthek.impl;

import java.time.LocalDateTime;
import java.util.List;

import javafx.scene.image.Image;

/**
 * A TV show with title, description, preview image
 * and variable number of parts.
 * @author Thomas Paireder
 */
public interface Show {
	
	/**
	 * Returns the title of the show.
	 * @return the title
	 */
	public String getTitle();
	
	/**
	 * Returns a preview image of the show.
	 * @return the preview image
	 */
	public Image getPreview();

	/**
	 * Returns the description of the show.
	 * @return the description text
	 */
	public String getDescription();
	
	/**
	 * Returns the date and time when the show was broadcasted.
	 * @return the date
	 */
	public LocalDateTime getBroadcastDate();

	/**
	 * Returns a list of {@link Segment}s of the show.
	 * @return the segments
	 */
	public List<Segment> getSegments();

}
