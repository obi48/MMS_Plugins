package tvthek;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;
import tvthek.impl.MediathekDecoder;
import tvthek.impl.ORFDecoder;
import tvthek.impl.Show;
import tvthek.view.ArchiveController;
import tvthek.view.StartLivestreamController;

/**
 * 
 * @author Thomas Paireder
 */
public class TVthekPlugin extends Plugin {
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
		// TODO Auto-generated method stub
		return false;
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
		return "0.2";
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

			Show s = controller.getShow();
			if (s != null) {
				System.out.println(s.getSegments().get(0).getStreamURL());
				pluginHost.setMedia(new URI(s.getSegments().get(0).getStreamURL()));
			}

		} catch (IOException ex) {
			// Exception gets thrown if the fxml file could not be loaded
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
		} catch (URISyntaxException ex) {
			Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
