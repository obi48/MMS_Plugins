/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestPlugin;

import java.util.LinkedList;
import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.GridPane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;
import mms.Pluginsystem.PluginHost.Identifier;

/**
 *
 * @author Michael Obermüller
 */
public class AudioVisu extends Plugin {
	private MediaPlayer mp;
	private int bandCount;
	private double minValue;
	private AudioSpectrumListener listener;
	private SpectrumBar[] bars;
	private double[] norms;
	private int[] counts;
	
	private final GridPane PANE = new GridPane();

	public AudioVisu(PluginHost pluginHost) {
		super(pluginHost);
		
		PANE.setAlignment(Pos.BOTTOM_CENTER);
		PANE.setTranslateY(-120);
		PANE.setMouseTransparent(true);
		
		initListener();
	}

	@Override
	public boolean start() {
		System.out.println("Visualizer started");
		
		pluginHost.addToUIStack(PANE);

		// Register this plugin as listener on ControlPlugin
		pluginHost.registerPluginListener(this, Identifier.ControlPlugin());

		// Register this plugin as listener on MenuPlugin
		pluginHost.registerPluginListener(this, Identifier.MenuPlugin());
		
		// Register this plugin as listener on MediaPlayer
		pluginHost.addMediaListener(this);

		// Register this plugin as listener on arbitrary Plugin
		// pluginHost.registerPluginListener(this,
		// Identifier.Plugin("DevName,PluginName,1.0"));

		return true;
	}
	
	private void init(){
		initBars();
		initNorm();
		initFreq();
	}
	
	private void initListener() {
		listener = new AudioSpectrumListener() {
			@Override
			public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
				int index = 0;
				int bucketIndex = 0;
				int currentBucketCount = 0;
				double sum = 0.0;
				
				while (index < magnitudes.length) {
					sum += magnitudes[index] - minValue;
					++currentBucketCount;
					if (currentBucketCount >= counts[bucketIndex]) {
						bars[bucketIndex].setValue(sum / norms[bucketIndex]);
						currentBucketCount = 0;
						sum = 0.0;
						++bucketIndex;
					}
					++index;
				}
			}
		};
	}

	private void initBars() {
		Reflection reflection = new Reflection();
		reflection.setFraction(0.32);

		for (int i = 0; i < bars.length; i++) {
			bars[i] = new SpectrumBar(100, 40);
			bars[i].setEffect(reflection);
			
			GridPane.setHalignment(bars[i], HPos.CENTER);
			PANE.add(bars[i], i, 0);
		}
	}
	
	private void initNorm(){
		double currentNorm = 0.05;
		for (int i = 0; i < norms.length; i++) {
			norms[i] = 1 + currentNorm;
			currentNorm *= 2;
		}	
	}

	private void initFreq(){
		double startFreq = 250.0;
		double bandwidth = 22050.0 / bandCount;
		double currentSpectrumFreq = bandwidth / 2.0;
		double currentEQFreq = startFreq / 2.0;
		double currentCutoff = 0.0;
		int currentBucketIndex = -1;

		for (int i = 0; i < bandCount; i++) {
			if (currentSpectrumFreq > currentCutoff) {
				currentEQFreq *= 2;
				currentCutoff = currentEQFreq + currentEQFreq / 2;
				++currentBucketIndex;

				if (currentBucketIndex == counts.length) {
					break;
				}
			}

			++counts[currentBucketIndex];
			currentSpectrumFreq += bandwidth;
		}
	}
	
	@Override
	public void onMediaPlayerChanged(MediaPlayer player){
		PANE.getChildren().removeAll(PANE.getChildren());
		
		mp = player;
		mp.audioSpectrumIntervalProperty().setValue(0.03334);
		
		List<String> list = new LinkedList<>();
		
		list.add(".mp3");
		list.add(".aif");
		list.add(".aiff");
		list.add(".wav");
		
		for(String s : list){
			if(mp.getMedia().getSource().toLowerCase().endsWith(s)){
				bandCount = mp.getAudioSpectrumNumBands();
				minValue = mp.getAudioSpectrumThreshold();
				mp.setAudioSpectrumListener(listener);
				System.out.println(bandCount);
				bars = new SpectrumBar[7];
				norms = new double[bars.length];
				counts = new int[bars.length];
				init();
				PANE.setMouseTransparent(true);
			}
		}
	}

	@Override
	public boolean stop() {
		System.out.println("Plugin stopped");
		return true;
	}

	@Override
	public String getDeveloper() {
		return "AOPP Studios";
	}

	@Override
	public String getName() {
		return AudioVisu.class.getCanonicalName();
	}

	@Override
	public String getDescription() {
		return "Test";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void onEventReceived(String eventID, Object... args) {
		// Ask developer of listened plugin for MessageIDs and possible
		// arguments
		switch (eventID) {
		case "message1": {
			System.out.println("bla");
		}
		}
	}
}
