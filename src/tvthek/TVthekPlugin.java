package tvthek;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;
import mms.Pluginsystem.PluginHost.Identifier;
import tvthek.impl.MediathekDecoder;
import tvthek.impl.ORFDecoder;
import tvthek.impl.Segment;
import tvthek.impl.Show;
import tvthek.view.ArchiveController;
import tvthek.view.StartLivestreamController;

/**
 * Mediaplayer plugin capable of accessing the online services of the ORF.
 * @author Thomas Paireder
 */
public class TVthekPlugin extends Plugin {
	private static final String PLAYLIST_PLUGIN_ID = "AOPP Studios,PlaylistPlugin,1.0";
	private static final String MESSAGE_ID = "addTempPlaylist";
	
	private static final String MENU_NAME = "ORF TVthek";
	private static final String M_ITEM_LIVE = "Start livestream...";
	private static final String M_ITEM_ARCHIVE = "Previous shows...";
	private static final String M_ITEM_CACHING = "Cache metadata";
	
	private MediathekDecoder decoder;

	public TVthekPlugin(PluginHost pluginHost) {
		super(pluginHost);
		this.decoder = new ORFDecoder();
	}

	@Override
	public boolean start() {
		decoder.cacheMetadata(true);
		
		Menu menu = new Menu(MENU_NAME);
		pluginHost.getMenus().add(menu);
		MenuItem itemLive = new MenuItem(M_ITEM_LIVE);
		itemLive.setOnAction(new EventHandler<ActionEvent>() {	
			@Override
			public void handle(ActionEvent event) {
				showLivestreamDialog();
			}
		});
		MenuItem itemArchive = new MenuItem(M_ITEM_ARCHIVE);
		itemArchive.setOnAction(new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent event) {
				showArchiveDialog();
			}
		});
		CheckMenuItem itemCaching = new CheckMenuItem(M_ITEM_CACHING);
		itemCaching.setSelected(decoder.isCaching());
		itemCaching.setOnAction(new EventHandler<ActionEvent>() {	
			@Override
			public void handle(ActionEvent event) {
				CheckMenuItem mi = (CheckMenuItem) event.getSource();
				decoder.cacheMetadata(mi.isSelected());
			}
		});
		menu.getItems().addAll(itemLive, itemArchive, new SeparatorMenuItem(), itemCaching);
		
		return true;
	}

	@Override
	public boolean stop() {
		// Nothing todo
		return true;
	}

	@Override
	public String getDeveloper() {
		return "AOPP Studios";
	}

	@Override
	public String getName() {
		return "ORF TVthek Plugin";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getDescription() {
		return "This plugin allows to access the ORF online services.";
	}
	
	private void showLivestreamDialog() {
		try {
			// Load the fxml file and create a new stage for the popup
			FXMLLoader fxmlloader = new FXMLLoader(TVthekPlugin.class.getResource("/tvthek/view/StartLivestreamView.fxml"));
			StackPane page = (StackPane) fxmlloader.load();
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Start Livestream");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			// Temporarily add label to main GUI to get window handle
			Pane p = new Pane();
			p.setVisible(false);
			pluginHost.addToUIStack(p);
			Window owner = p.getScene().getWindow();
			dialogStage.initOwner(owner);
			((Pane)p.getParent()).getChildren().remove(p);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// Sets the plugin lists
			StartLivestreamController controller = fxmlloader.getController();
			controller.setDecoder(decoder);
			dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					controller.stop();
				}
			});

			// Show the dialog and wait until the user closes it
			//dialogStage.setResizable(false);
			dialogStage.showAndWait();

			String url = controller.getStream();
			if (url != null) {
				pluginHost.setMedia(new URI(url));
			}
		} catch (IOException ex) {
			// Exception gets thrown if the fxml file could not be loaded
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
		} catch (URISyntaxException ex) {
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void showArchiveDialog() {
		try {
			// Load the fxml file and create a new stage for the popup
			FXMLLoader fxmlloader = new FXMLLoader(TVthekPlugin.class.getResource("/tvthek/view/ArchiveBrowser.fxml"));
			StackPane page = (StackPane) fxmlloader.load();
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Select Show from Archive");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			// Temporarily add label to main GUI to get window handle
			Pane p = new Pane();
			p.setVisible(false);
			pluginHost.addToUIStack(p);
			Window owner = p.getScene().getWindow();
			dialogStage.initOwner(owner);
			((Pane)p.getParent()).getChildren().remove(p);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// Sets the plugin lists
			ArchiveController controller = fxmlloader.getController();
			controller.setDecoder(decoder);
			dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					controller.stop();
				}
			});

			// Show the dialog and wait until the user closes it
			//dialogStage.setResizable(false);
			dialogStage.showAndWait();

			Show show = controller.getShow();
			if (show != null && show.getSegments().size() >= 1) {
				List<Media> mediaList = new ArrayList<>();
				List<String> titleList = new ArrayList<>();
				for (Segment s : show.getSegments()) {
					Media m = new Media(s.getStreamURL());
					mediaList.add(m);
					titleList.add(s.getTitle());
				}
				boolean success = pluginHost.fireMessageDirectlyToPlugin(Identifier.Plugin(PLAYLIST_PLUGIN_ID), MESSAGE_ID, show.getTitle(), mediaList, titleList);
				if (!success) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("TVthek Decoder");
					alert.setHeaderText("Error");
					alert.setContentText("Couldn't add show to playlist.");
					alert.showAndWait();
				}
			}

		} catch (IOException ex) {
			// Exception gets thrown if the fxml file could not be loaded
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
