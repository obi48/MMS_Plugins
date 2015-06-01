package tvthek.impl;

import java.time.Duration;

public class ORFShowSegment implements Segment {
	private final String title;
	private final String desc;
	private final Duration duration;
	private final String streamURL;
	
	public ORFShowSegment(String title, String desc, Duration duration, String streamURL) {
		this.title = title;
		this.desc = desc;
		this.duration = duration;
		this.streamURL = streamURL;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getDescription() {
		return desc;
	}
	
	@Override
	public Duration getDuration() {
		return duration;
	}

	@Override
	public String getStreamURL() {
		return streamURL;
	}

}
