package tvthek.view;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import tvthek.impl.Segment;
import tvthek.impl.Show;


public class HShowPreviewController implements Initializable {
	@FXML
	private StackPane previewPane;
	@FXML
	private ImageView previewImgView;
	@FXML
	private ImageView playImgView;
	@FXML
	private Label startTime;
	@FXML
	private Label title;
	@FXML
	private Label durationLabel;
	@FXML
	private Label description;
	
	private static final String PLAY_IMG_RES = "/tvthek/resources/play_button.png";
	
	private ArchiveController mainCntr;
	private Show show;
	private Image playImg;
	
	
	@Override
	public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
		playImg = new Image(this.getClass().getResourceAsStream(PLAY_IMG_RES));
	};
	
	protected void setMainController(ArchiveController main) {
		this.mainCntr = main;
	}
	
	protected void setShow(Show show) {
		this.show = show;
		updateContent();
	}
	
	private void updateContent() {
		DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm");
		DateTimeFormatter minFormat = DateTimeFormatter.ofPattern("mm:ss");
		
		Duration dur = Duration.ofMillis(0);
		for (Segment seg : show.getSegments()) {
			dur = dur.plus(seg.getDuration());
		}
		
		final LocalTime time = LocalTime.MIDNIGHT.plus(dur);
		String durStr;
		if (dur.compareTo(Duration.ofHours(1)) < 0) {
			durStr = time.format(minFormat) + " Min.";
		} else {
			durStr = time.format(hourFormat) + " Std.";
		}
		
		Platform.runLater(() -> {
			startTime.setText(show.getBroadcastDate().format(hourFormat));
			previewImgView.setImage(show.getPreview());
			title.setText(show.getTitle());
			durationLabel.setText(durStr);
			description.setText(show.getDescription());
		});
	}
	
	@FXML
	public void handleMouseEntered(MouseEvent ev) {
		playImgView.setImage(playImg);
		ev.consume();
	}
	
	@FXML
	public void handleMouseExited(MouseEvent ev) {
		playImgView.setImage(null);
		ev.consume();
	}
	
	@FXML
	public void handleMouseClicked(MouseEvent ev) {
		mainCntr.setShow(show);
		ev.consume();
	}
	
	
}
