package tvthek.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tvthek.TVthekPlugin;
import tvthek.impl.Channel;
import tvthek.impl.MediathekDecoder;
import tvthek.impl.Show;

/**
 * GUI controller for the ArchiveBrowser view.
 * Sets up the content and handles user interactions.
 * @author Thomas Paireder
 */
public class ArchiveController {
	@FXML
	private StackPane stackPane;
	@FXML
	private ImageView stationIcon;
	@FXML
	private Button refreshBtn;
	@FXML
	private FlowPane refreshOverlay;
	@FXML
	private HBox chSelectPane;
	@FXML
	private HBox daySelectPane;
	@FXML
	private GridPane listPane;
	
	private ToggleButton[] chSelectBtns;
	private ToggleGroup chSelectGroup;
	private ToggleButton[] daySelectBtns;
	private ToggleGroup daySelectGroup;
	
	private MediathekDecoder decoder;
	private Show show;
	private Task<Void> updateTask;
	private Thread updateThread;
	
	
	private void createChannelSelector () {
		chSelectGroup = new ToggleGroup();
		
		// Create channel select buttons
		int idx = 0;
		chSelectBtns = new ToggleButton[decoder.getChannels().size()];
		for (Channel ch : decoder.getChannels()) {
			chSelectBtns[idx] = new ToggleButton();
			chSelectBtns[idx].getStylesheets().add(getClass().getResource("/tvthek/resources/togglebutton.css").toExternalForm());
			chSelectBtns[idx].setToggleGroup(chSelectGroup);
			chSelectBtns[idx].setUserData(ch);
			idx++;
		}
		chSelectBtns[0].setSelected(true);
		chSelectPane.getChildren().clear();
		chSelectPane.getChildren().addAll(chSelectBtns);
		
		// Add listener after selecting the default item
		chSelectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle toggle, Toggle new_toggle) {
				updateContent();
			}
		});
	}
	
	private void createDaySelector () {
		daySelectGroup = new ToggleGroup();
		
		// Create day select buttons
		int idx = 0;
		DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EE");
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		daySelectBtns = new ToggleButton[decoder.getMaxOnlineDays()+1];
		for (int days = decoder.getMaxOnlineDays(); days >= 0; days--) {
			LocalDate date = LocalDate.now().minusDays(days);
			Label dayLabel = new Label(date.format(dayFormat).toUpperCase());
			dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18");
			Label dateLabel = new Label(date.format(dateFormat));
			dateLabel.setStyle("-fx-font-size: 11");
			VBox box = new VBox(dayLabel, dateLabel);
			box.setAlignment(Pos.CENTER);
			daySelectBtns[idx] = new ToggleButton();
			daySelectBtns[idx].getStylesheets().add(getClass().getResource("/tvthek/resources/togglebutton.css").toExternalForm());
			daySelectBtns[idx].setGraphic(box);
			daySelectBtns[idx].setToggleGroup(daySelectGroup);
			daySelectBtns[idx].setUserData(date);
			idx++;
		}
		daySelectBtns[daySelectBtns.length-1].setSelected(true);
		daySelectPane.getChildren().clear();
		daySelectPane.getChildren().addAll(daySelectBtns);
		
		// Add listener after selecting the default item
		daySelectGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle toggle, Toggle new_toggle) {
				updateContent();
			}
		});
	}

	private void updateContent() {
		final ArchiveController thisController = this;
		updateTask = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					// Display progress indicator
					Platform.runLater(() -> {
						if (!stackPane.getChildren().contains(refreshOverlay)) {
							stackPane.getChildren().add(refreshOverlay);
						}
					});
					
					// Set station icon
					Image img = decoder.getStationLogo();
					Platform.runLater(() -> {
						stationIcon.setImage(img);
						listPane.getChildren().clear();
					});
					
					// Set channel logos
					for (ToggleButton btn : chSelectBtns) {
						if (Thread.currentThread().isInterrupted()) {
							return null;
						}
						Platform.runLater(() -> {
							ImageView logo = new ImageView(((Channel)btn.getUserData()).getLogo());
							logo.setFitHeight(20);
							logo.setFitWidth(80);
							logo.setSmooth(true);
							logo.setPreserveRatio(true);
							HBox box = new HBox(logo);
							box.setPrefSize(80, 20);
							box.setAlignment(Pos.CENTER);
							btn.setGraphic(box);
						});
					}
					// Refresh day select buttons if the a new day has started
					LocalDate date = (LocalDate) daySelectBtns[daySelectBtns.length-1].getUserData();
					if (!date.equals(LocalDate.now())) {
						Platform.runLater(() -> createDaySelector());
					}
					
					// Display the archive show information for the selected channel and day
					date = ((LocalDate) daySelectGroup.getSelectedToggle().getUserData());
					List<Show> archive = ((Channel) chSelectGroup.getSelectedToggle().getUserData()).getArchivedShows(date);
					int row = 0;
					for (Show s : archive) {
						if (Thread.currentThread().isInterrupted()) {
							return null;
						}
						
						FXMLLoader fxmlloader = new FXMLLoader(TVthekPlugin.class.getResource("/tvthek/view/HShowPreviewPane.fxml"));
						GridPane pane = (GridPane) fxmlloader.load();
						final int r = row;
						Platform.runLater(() -> {
							listPane.addRow(r, pane);
						});
						row++;
						
						// Sets the main controller and the show to display
						HShowPreviewController controller = fxmlloader.getController();
						controller.setMainController(thisController);
						controller.setShow(s);
					}
					
					Platform.runLater(() -> stackPane.getChildren().remove(refreshOverlay));
				} catch(Exception ex) {
					// Close the dialog on error
					Logger.getLogger(TVthekPlugin.class.getName()).log(Level.SEVERE, null, ex);
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("TVthek Decoder");
						alert.setHeaderText("Error");
						alert.setContentText(ex.getClass() + "\n" + ex.getMessage());
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
		createChannelSelector();
		createDaySelector();
		updateContent();
	}
	
	public Show getShow() {
		return show;
	}
	
	public void setShow(Show s) {
		this.show = s;
		Stage stage = (Stage) stationIcon.getScene().getWindow();
		stage.close();
	}
	
	public void stop() {
		if (updateThread != null) {
			updateThread.interrupt();
		}
	}
	
	@FXML
	private void handleRefresh(ActionEvent ev) {
		// Force reload from online source
		decoder.cacheMetadata(false);
		decoder.cacheMetadata(true);

		updateContent();
	}
}
