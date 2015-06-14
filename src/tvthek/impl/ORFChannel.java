package tvthek.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvthek.TVthekPlugin;
import javafx.scene.image.Image;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

/**
 * Implementation of a {@link Channel} capable of decoding information
 * from the OFF TVthek.
 * @author Thomas Paireder
 */
class ORFChannel implements Channel {
	private static final String DATE_TOKEN_FORMAT = "dd.MM.yyyy HH:mm:ss";
	// Keys for accessing Json entries
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTIONS_ARRAY = "descriptions";
	private static final String KEY_DESCRIPTION_NAME = "fieldName";
	private static final String VAL_DESCRIPTION_NAME_1 = "description";
	private static final String VAL_DESCRIPTION_NAME_2 = "teaser_text";
	private static final String VAL_DESCRIPTION_NAME_3 = "info";	
	private static final String KEY_DESCRIPTION_TEXT = "text";
	private static final String KEY_IMAGES_ARRAY = "images";	
	private static final String KEY_IMAGE_URL = "url";
	private static final String KEY_CH_INFO_OBJ = "channel";
	private static final String KEY_CHANNEL_ID = "channelId";
	private static final String KEY_LIVE_START_DATE = "livestreamStart";
	private static final String KEY_LIVE_END_DATE = "livestreamEnd";
	
	private static final String KEY_DETAIL_URL = "detailApiCall";
	private static final String KEY_DATE = "date";
	private static final String KEY_EPISODE_DET_ARRAY = "episodeDetail";
	private static final String KEY_SEGMENT_ARRAY = "segments";
	private static final String KEY_DURATION = "exactDuration";
	private static final String EXACT_DURATION_FORMAT1 = "PT";
	private static final String EXACT_DURATION_FORMAT2 = "S";
	private static final String KEY_VIDEO_ARRAY = "videos";
	private static final String KEY_STREAM_URL = "streamingUrl";
	private static final String STREAM_PROTOCOL = "http";
	private static final String STREAM_QUALITY = "Q4A.mp4";
	
	// Path of default preview image
	private static final String DEF_PREVIEW_IMG_RES = "/tvthek/resources/default_preview.png";
	
	
	private final int id;
	private final String name;
	private final String iconURL;
	private final String liveURL;
	private final ORFDecoder decoder;
	
	private boolean useCache;
	private Image icon;
	private Show liveShow;
	private Map<LocalDate, List<Show>> archivedShows;
	
	
	public ORFChannel(int id, String name, String iconURL, String liveURL, ORFDecoder decoder) {
		this.id = id;
		this.name = name;
		this.iconURL = iconURL;
		this.liveURL = liveURL;
		this.decoder = decoder;
		archivedShows = new HashMap<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getLogo() {
		if (icon != null) {
			return icon;
		}
		
		Image img = new Image(iconURL, false);
		if (img.isError()) {
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.WARNING, "Error while loading channel logo.");
			throw new MediathekAccessException("Couldn't load channel logo.");
		}
		if (useCache) {
			icon = img;
		}
		return img;
	}

	@Override
	public Show getLiveShow() {
		if (liveShow != null) {
			return liveShow;
		}
		
		JsonArray episodes = decoder.getLiveInfo();
		
		// Parse content
		String title = "";
		String description = "";
		String teaser = "";
		Image img = null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TOKEN_FORMAT);;
		LocalDateTime startDate = null, endDate = null;
		Duration duration = null;
		
		for (JsonValue episode : episodes) {
			try {
				JsonObject obj = episode.asObject();
				int id = obj.get(KEY_CH_INFO_OBJ).asObject().get(KEY_CHANNEL_ID).asInt();	
				
				if (this.id == id) {
					// Get title
					title = obj.get(KEY_TITLE).asString();
					
					// Get description
					for (JsonValue descVal : obj.get(KEY_DESCRIPTIONS_ARRAY).asArray()) {
						String str = descVal.asObject().get(KEY_DESCRIPTION_NAME).asString();
						if (str.matches(VAL_DESCRIPTION_NAME_1)) {
							description = descVal.asObject().get(KEY_DESCRIPTION_TEXT).asString();
							// Stop loop if a description was found
							if (!description.isEmpty()) {
								break;
							}
						} else if (str.matches(VAL_DESCRIPTION_NAME_2)) {
							// Teaser is used in case of an empty description
							teaser = descVal.asObject().get(KEY_DESCRIPTION_TEXT).asString();
						}
					}
					if (description.isEmpty()) {
						description = teaser;
					}
					
					// Get preview image
					String url = obj.get(KEY_IMAGES_ARRAY).asArray().get(0).asObject().get(KEY_IMAGE_URL).asString();
					img = new Image(url, false);
					if (img.isError()) {
						// Use default image instead of preview
						img = new Image(this.getClass().getResourceAsStream(DEF_PREVIEW_IMG_RES));
					}
					
					// Get start date/time and duration
					startDate = LocalDateTime.parse(obj.get(KEY_LIVE_START_DATE).asString(), formatter);
					endDate = LocalDateTime.parse(obj.get(KEY_LIVE_END_DATE).asString(), formatter);
					duration = Duration.between(startDate, endDate).abs();
					break;
				}
			} catch (DateTimeParseException | UnsupportedOperationException | IndexOutOfBoundsException ex) {
				// Catch all parse exceptions and continue with next entry
				Logger.getLogger(TVthekPlugin.class.getName()).log(Level.WARNING, null, ex);
			}
		}
		
		List<Segment> segments = new ArrayList<>();
		segments.add(new ORFShowSegment("", "", duration, liveURL));
		Show show = new ORFShow(title, img, description, startDate, segments);
		if (useCache) {
			liveShow = show;
		}
		return show;
	}

	@Override
	public List<Show> getArchivedShows(LocalDate date) {		
		if (archivedShows.containsKey(date)) {
			return archivedShows.get(date);
		}
		
		JsonArray episodes = decoder.getArchivInfo();
		List<Show> shows = new ArrayList<>();
		
		// Parse content
		String title = "";
		String description = "";
		Image img = null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TOKEN_FORMAT);;
		LocalDateTime startDate = null;
		
		for (JsonValue episode : episodes) {
			try {
				JsonObject jsonObj = episode.asObject();
				int id = jsonObj.get(KEY_CH_INFO_OBJ).asObject().get(KEY_CHANNEL_ID).asInt();
				if (this.id == id) {
					// Get start date
					startDate = LocalDateTime.parse(jsonObj.get(KEY_DATE).asString(), formatter);
					// Don't add show to list if the start date doesn't match the requested date
					if (!startDate.truncatedTo(ChronoUnit.DAYS).equals(date.atStartOfDay())) {
						continue;
					}
					
					// Get title
					title = jsonObj.get(KEY_TITLE).asString();
					
					// Get description
					for (JsonValue descVal : jsonObj.get(KEY_DESCRIPTIONS_ARRAY).asArray()) {
						String str = descVal.asObject().get(KEY_DESCRIPTION_NAME).asString();
						if (str.matches(VAL_DESCRIPTION_NAME_2)) {
							description = descVal.asObject().get(KEY_DESCRIPTION_TEXT).asString();
							break;
						}
					}
					
					// Get preview image
					String imgUrl = jsonObj.get(KEY_IMAGES_ARRAY).asArray().get(0).asObject().get(KEY_IMAGE_URL).asString();
					img = new Image(imgUrl, false);
					if (img.isError()) {
						// Use default image instead of preview
						img = new Image(this.getClass().getResourceAsStream(DEF_PREVIEW_IMG_RES));
					}
					
					// Get segment information and streaming URLs by decoding the episode details page
					List<Segment> segments = new ArrayList<>();
					JsonObject details;
					// Read details page
					String url = jsonObj.get(KEY_DETAIL_URL).asString();
					try (InputStreamReader in = new InputStreamReader(new URL(url).openStream())) {
						details = JsonObject.readFrom(in).get(KEY_EPISODE_DET_ARRAY).asObject();
					} catch (IOException | ParseException | UnsupportedOperationException ex) {
						Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
						throw new MediathekAccessException("Couldn't access episode details.\n(Cause: " + ex.getClass() + "," + ex.getMessage() + ")");
					}
					JsonArray segArr = details.get(KEY_SEGMENT_ARRAY).asArray();
					for (JsonValue seg : segArr) {
						// Get title, description, duration and URL for a segment
						JsonObject segObj = seg.asObject();
						String segTitle = segObj.get(KEY_TITLE).asString();
						String segDesc = "";
						String segUrl = "";
						for (JsonValue val : segObj.get(KEY_DESCRIPTIONS_ARRAY).asArray()) {
							if (val.asObject().get(KEY_DESCRIPTION_NAME).asString().matches(VAL_DESCRIPTION_NAME_3)) {
								segDesc = val.asObject().get(KEY_DESCRIPTION_TEXT).asString();
								break;
							}
						}
						Duration dur = Duration.parse(EXACT_DURATION_FORMAT1 + segObj.get(KEY_DURATION).asDouble() + EXACT_DURATION_FORMAT2);
						for (JsonValue val : segObj.get(KEY_VIDEO_ARRAY).asArray()) {
							String str = val.asObject().get(KEY_STREAM_URL).asString();
							if (str.startsWith(STREAM_PROTOCOL) && str.contains(STREAM_QUALITY)) {
								segUrl = str;
								break;
							}
						}
						Segment segment = new ORFShowSegment(segTitle, segDesc, dur, segUrl);
						segments.add(segment);
					}
					
					Show show = new ORFShow(title, img, description, startDate, segments);
					shows.add(show);
				}
			} catch (DateTimeParseException | UnsupportedOperationException | IndexOutOfBoundsException ex) {
				// Catch all parse exceptions and continue with next entry
				Logger.getLogger(TVthekPlugin.class.getName()).log(Level.WARNING, null, ex);
			}
		}
		
		if (useCache) {
			archivedShows.put(date, shows);
		}
		return shows;
	}
	
	@Override
	public void cacheMetadata(boolean useCache) {
		this.useCache = useCache;

		if (!useCache) {
			this.icon = null;
			this.liveShow = null;
			this.archivedShows.clear();
		}
	}
}
