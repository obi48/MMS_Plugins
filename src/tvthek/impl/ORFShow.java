package tvthek.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import javafx.scene.image.Image;

class ORFShow implements Show {
	private final String title;
	private final Image preview;
	private final String desc;
	private final LocalDateTime date;
	private final List<Segment> chapters;
	
	public ORFShow(String title, Image preview, String desc,
					LocalDateTime date, List<Segment> chapters) {
		this.title = title;
		this.preview = preview;
		this.desc = desc;
		this.date = date;
		this.chapters = chapters;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Image getPreview() {
		return preview;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public LocalDateTime getBroadcastDate() {
		return date;
	}

	@Override
	public List<Segment> getSegments() {
		return Collections.unmodifiableList(chapters);
	}

}
