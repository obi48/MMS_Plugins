package tvthek.impl;

import java.util.List;

import javafx.scene.image.Image;

public interface MediathekDecoder {

	public String getStationName();

	public Image getStationLogo();

	public List<Channel> getChannels();

	public void cacheMetadata(boolean useCache);
	
	public boolean isCaching();
	
	public int getMaxOnlineDays();
	
}
