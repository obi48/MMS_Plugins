/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PlaylistPlugin.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author Dominik
 */
public class MediaCellController {
    @FXML
    private AnchorPane anchorpane;
    @FXML
    private ImageView Image;
    @FXML
    private Label Title;
    @FXML
    private Label Duration;
    @FXML
    private Label Artist;

    public MediaCellController() {
    }

    public AnchorPane getAnchorpane() {
        return anchorpane;
    }

    public ImageView getImage() {
        return Image;
    }

    public Label getTitle() {
        return Title;
    }

    public Label getDuration() {
        return Duration;
    }

    public Label getArtist() {
        return Artist;
    }
    
    
}
