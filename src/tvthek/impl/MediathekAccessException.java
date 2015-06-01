package tvthek.impl;

/**
 * Is thrown when errors occur during accessing the mediathek of the TV station.
 * @author Thomas Paireder
 */
public class MediathekAccessException extends RuntimeException {

	private static final long serialVersionUID = -1238921681792091923L;

	public MediathekAccessException(String message) {
		super(message);
	}
}
