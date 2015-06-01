package tvthek.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tvthek.impl.Channel;
import tvthek.impl.Show;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class LivePreviewController implements Initializable {
	@FXML
	private ImageView channelImgView;
	@FXML
	private StackPane previewPane;
	@FXML
	private ImageView previewImgView;
	@FXML
	private ImageView playImgView;
	@FXML
	private Label startTime;
	@FXML
	private ImageView onAirImgView;
	@FXML
	private Label title;
	@FXML
	private Label description;
	
	private static final String PLAY_IMG_RES = "/tvthek/resources/play_button.png";
	private static final String ONAIR_IMG_RES = "/tvthek/resources/onair_active.png";
	private static final String NOT_ONAIR_IMG_RES = "/tvthek/resources/onair_inactive.png";
	
	private Channel channel;
	private StartLivestreamController mainCntr;
	private Show show;
	private Image playImg;
	private Image onAirImg;
	private Image notOnAirImg;

	@Override
	public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
		playImg = new Image(this.getClass().getResourceAsStream(PLAY_IMG_RES));
		onAirImg = new Image(this.getClass().getResourceAsStream(ONAIR_IMG_RES));
		notOnAirImg = new Image(this.getClass().getResourceAsStream(NOT_ONAIR_IMG_RES));
	};
	
	protected void setChannel(Channel ch) {
		this.channel = ch;
		updateContent();
	}
	
	protected void setMainController(StartLivestreamController main) {
		this.mainCntr = main;
	}
	
	private void updateContent() {
		Image img = channel.getLogo();
		Platform.runLater(() -> channelImgView.setImage(img));

		show = channel.getLiveShow();
		if (show == null) {
			return;
		}
		
		DateTimeFormatter formatter;
		if (show.getBroadcastDate().getDayOfYear() > LocalDateTime.now().getDayOfYear()) {
			formatter = DateTimeFormatter.ofPattern("HH:mm (dd.MM.)");
		} else {
			formatter = DateTimeFormatter.ofPattern("HH:mm");
		}
		Platform.runLater(() -> {
			startTime.setText(show.getBroadcastDate().format(formatter));
			title.setText(show.getTitle());
			previewImgView.setImage(show.getPreview());
			description.setText(show.getDescription());
			onAirImgView.setImage(isShowOnAir() ? onAirImg : notOnAirImg);
		});
	}
	
	private boolean isShowOnAir() {
		return LocalDateTime.now().isAfter(show.getBroadcastDate()) && 
				LocalDateTime.now().isBefore(show.getBroadcastDate().plus(show.getSegments().get(0).getDuration()));
	}
	
	@FXML
	public void handleMouseEntered(MouseEvent ev) {
		if (isShowOnAir()) {
			playImgView.setImage(playImg);
		}
		ev.consume();
	}
	
	@FXML
	public void handleMouseExited(MouseEvent ev) {
		playImgView.setImage(null);
		ev.consume();
	}
	
	@FXML
	public void handleMouseClicked(MouseEvent ev) {
		if (isShowOnAir()) {
			mainCntr.setStream(show.getSegments().get(0).getStreamURL());
		}
		ev.consume();
	}
}
