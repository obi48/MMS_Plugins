package tvthek.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tvthek.TVthekPlugin;
import javafx.scene.image.Image;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

/**
 * Implementation of a {@link MediathekDecoder} capable of decoding information
 * from the ORF TVthek.
 * @author Thomas Paireder
 */
public class ORFDecoder implements MediathekDecoder {
	private static final Duration CACHE_TIMEOUT = Duration.ofMinutes(10);
	
	// Constants to generate download links
	private static final String BASE_URL = "http://tvthek.orf.at";
	private static final String ICON_URL = "/static/images/logo_orf_header.png";
	private static final String LIVE_URL1 = "/service_api/token/aee52b773d32c6/livestreams/from/";
	private static final String LIVE_URL2 = "/till/";
	private static final String LIVE_URL3 = "?page=0&entries_per_page=1000";
	private static final String ARCHIVE_URL1 = "/service_api/token/aee52b773d32c6/episodes/by_date/";
	private static final String ARCHIVE_URL2 = "?page=0&entries_per_page=1000";
	private static final String URL_DATETIME_FORMAT = "yyyyMMddHHmm";
	private static final String URL_DATE_FORMAT = "yyyyMMdd";
	
	// Key for accessing Json entries
	private static final String KEY_EPISODE_SH_ARRAY = "episodeShorts";
	
	// Constant for channels
	private static final int CH1_ID = 1180;
	private static final String CH1_NAME = "ORF 1";
	private static final String CH1_ICON_URL = "/dynamic/get_asset_resized.php?height=30&max_age=3600&mtime=1380153790&path=orf_channels%252Flogo_color%252F6779277.png&percent=100&quality=100&width=142&x1=0&x2=204&y1=0&y2=43";
	private static final String CH1_LIVE_URL = "http://apasfiisl.apa.at/ipad/orf1_q6a/orf.sdp/playlist.m3u8";	
	private static final int CH2_ID = 1181;
	private static final String CH2_NAME = "ORF 2";
	private static final String CH2_ICON_URL = "/dynamic/get_asset_resized.php?height=30&max_age=3600&mtime=1380153791&path=orf_channels%252Flogo_color%252F6779281.png&percent=100&quality=100&width=101&x1=0&x2=145&y1=0&y2=43";
	private static final String CH2_LIVE_URL = "http://apasfiisl.apa.at/ipad/orf2_q6a/orf.sdp/playlist.m3u8";	
	private static final int CH3_ID = 3026625;
	private static final String CH3_NAME = "ORF 3";
	private static final String CH3_ICON_URL = "/dynamic/get_asset_resized.php?height=41&max_age=3600&mtime=1380153796&path=orf_channels%252Flogo_color%252F6779305.png&percent=100&quality=100&width=104&x1=0&x2=153&y1=0&y2=60";
	private static final String CH3_LIVE_URL = "http://apasfiisl.apa.at/ipad/orf3_q6a/orf.sdp/playlist.m3u8";	
	private static final int CHS_ID = 76464;
	private static final String CHS_NAME = "ORF Sport+";
	private static final String CHS_ICON_URL = "/dynamic/get_asset_resized.php?height=30&max_age=3600&mtime=1380153797&path=orf_channels%252Flogo_color%252F6779307.png&percent=100&quality=100&width=198&x1=0&x2=284&y1=0&y2=43";
	private static final String CHS_LIVE_URL = "http://apasfiisl.apa.at/ipad/orfs_q6a/orf.sdp/playlist.m3u8";	
	
	private static final int MAX_ONLINE_DAYS = 7;
	
	private boolean useCache;
	private LocalDateTime cacheDate;
	private Image icon;
	private List<Channel> channels;
	private JsonArray liveInfo;
	private JsonArray archiveInfo;
	
	public ORFDecoder() {
		icon = null;
		
		channels = new ArrayList<Channel>();
		Channel ch = new ORFChannel(CH1_ID, CH1_NAME, BASE_URL+CH1_ICON_URL, CH1_LIVE_URL, this);
		channels.add(ch);
		ch = new ORFChannel(CH2_ID, CH2_NAME, BASE_URL+CH2_ICON_URL, CH2_LIVE_URL, this);
		channels.add(ch);
		ch = new ORFChannel(CH3_ID, CH3_NAME, BASE_URL+CH3_ICON_URL, CH3_LIVE_URL, this);
		channels.add(ch);
		ch = new ORFChannel(CHS_ID, CHS_NAME, BASE_URL+CHS_ICON_URL, CHS_LIVE_URL, this);
		channels.add(ch);
		
		cacheMetadata(true);
	}

	@Override
	public String getStationName() {
		return "ORF";
	}

	@Override
	public Image getStationLogo() {
		checkCacheTimeout();
		if (icon != null) {
			return icon;
		}
		
		Image img = new Image(BASE_URL+ICON_URL, false);
		if (img.isError()) {
			throw new MediathekAccessException("Couldn't load station logo.");
		}
		if (useCache) {		
			icon = img;
		}
		return img;
	}

	@Override
	public List<Channel> getChannels() {
		return Collections.unmodifiableList(channels);
	}

	@Override
	public void cacheMetadata(boolean useCache) {
		this.useCache = useCache;
		if (!useCache) {
			this.cacheDate = null;
			this.icon = null;
			this.liveInfo = null;
			this.archiveInfo = null;
		}
		channels.stream().forEach(ch -> ch.cacheMetadata(useCache));
	}
	
	@Override
	public boolean isCaching() {
		return useCache;
	}
	
	private void checkCacheTimeout() {
		if (useCache) {
			if (cacheDate == null) {
				cacheDate = LocalDateTime.now();
			} else if (LocalDateTime.now().isAfter(cacheDate.plus(CACHE_TIMEOUT))) {
				cacheMetadata(false);
				cacheMetadata(true);
			}
		}
	}
	
	@Override
	public int getMaxOnlineDays() {
		return MAX_ONLINE_DAYS;
	}

	public JsonArray getLiveInfo() {
		checkCacheTimeout();
		if (liveInfo != null) {
			return liveInfo;
		}
		
		// Download livestream information
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(URL_DATETIME_FORMAT);
		String url = BASE_URL + LIVE_URL1 + LocalDateTime.now().format(formatter) +
					 LIVE_URL2 + LocalDateTime.now().plusDays(1).format(formatter) + 
					 LIVE_URL3;
		
		JsonArray jsonArray;
		try (InputStreamReader in = new InputStreamReader(new URL(url).openStream())) {
			JsonObject obj = JsonObject.readFrom(in);
			jsonArray = obj.get(KEY_EPISODE_SH_ARRAY).asArray();
		} catch (IOException | ParseException | UnsupportedOperationException ex) {
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
			throw new MediathekAccessException("Couldn't access livestream info.\n(Cause: " + ex.getClass() + "," + ex.getMessage() + ")");
		}

		if (useCache) {
			liveInfo = jsonArray;
		}
		return jsonArray;
	}

	public JsonArray getArchivInfo() {
		checkCacheTimeout();
		if (archiveInfo != null) {
			return archiveInfo;
		}
		
		// Download archive information for last days (MAX_ONLINE_DAYS)
		JsonArray jsonArray = new JsonArray();
		
		for (int days = MAX_ONLINE_DAYS; days >= 0; days--) {
			// Download information of one day and concatenate with other days
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(URL_DATE_FORMAT);
			String url = BASE_URL + ARCHIVE_URL1 + LocalDateTime.now().minusDays(days).format(formatter) + ARCHIVE_URL2;
			
			try (InputStreamReader in = new InputStreamReader(new URL(url).openStream())) {
				JsonObject obj = JsonObject.readFrom(in);
				JsonArray arr = obj.get(KEY_EPISODE_SH_ARRAY).asArray();
				for (JsonValue val : arr) {
					jsonArray.add(val);
				}
			} catch (IOException | ParseException | UnsupportedOperationException ex) {
				Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
				throw new MediathekAccessException("Couldn't access archiv index.\n(Cause: " + ex.getClass() + "," + ex.getMessage() + ")");
			}
		}
		
		if (useCache) {
			archiveInfo = jsonArray;
		}
		return jsonArray;
	}
}
