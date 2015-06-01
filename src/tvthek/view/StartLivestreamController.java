package tvthek.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tvthek.TVthekPlugin;
import tvthek.impl.Channel;
import tvthek.impl.MediathekDecoder;

public class StartLivestreamController {
	@FXML
	private StackPane stackPane;
	@FXML
	private ImageView stationIcon;
	@FXML
	private Button refreshBtn;
	@FXML
	private FlowPane refreshOverlay;
	@FXML
	private GridPane selectionPane;
	
	private MediathekDecoder decoder;
	private String streamURL;
	private Task<Void> updateTask;
	private Thread updateThread;

	
	private void updateContent() {
		final StartLivestreamController thisController = this;
		updateTask = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					Platform.runLater(() -> {
						if (!stackPane.getChildren().contains(refreshOverlay)) {
							stackPane.getChildren().add(refreshOverlay);
						}
					});
					
					Image img = decoder.getStationLogo();
					Platform.runLater(() -> {
						stationIcon.setImage(img);
						selectionPane.getChildren().clear();
					});
	
					int column = 0;
					for (Channel ch : decoder.getChannels()) {
						if (Thread.currentThread().isInterrupted()) {
							return null;
						}
						
						final int col = column;
						// Load the fxml file of the pane
						FXMLLoader fxmlloader = new FXMLLoader(TVthekPlugin.class.getResource("/tvthek/view/LivePreviewPane.fxml"));
						BorderPane pane = (BorderPane) fxmlloader.load();
						Platform.runLater(() -> selectionPane.addColumn(col, pane));
						column++;
						
						// Set the main controller and the channel to display
						LivePreviewController controller = fxmlloader.getController();
						controller.setMainController(thisController);
						controller.setChannel(ch);
					}
					
					Platform.runLater(() -> stackPane.getChildren().remove(refreshOverlay));
				} catch(Exception ex) {
					Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("TVthek Decoder");
						alert.setHeaderText("Error");
						alert.setContentText(ex.getMessage());
						alert.showAndWait();
						Stage stage = (Stage) stationIcon.getScene().getWindow();
						stage.close();
					});
				}
				return null;
			}
		};
		updateThread = new Thread(updateTask);
		updateThread.start();
	}
	
	public void setDecoder(MediathekDecoder decoder) {
		this.decoder = decoder;
		updateContent();
	}
	
	public void stop() {
		if (updateThread != null) {
			updateThread.interrupt();
		}
	}
	
	public String getStream() {
		return streamURL;
	}
	
	public void setStream(String url) {
		this.streamURL = url;
		Stage stage = (Stage) stationIcon.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private void handleRefresh(ActionEvent ev) {
		// Force reload from online source
		decoder.cacheMetadata(false);
		decoder.cacheMetadata(true);

		updateContent();
	}
}
