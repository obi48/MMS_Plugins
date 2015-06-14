/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PlaylistPlugin.GUI;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;

/**
 * FXML Controller class
 *
 * @author Dominik
 */
public class PlaylistUIController implements Initializable {
    @FXML
    private AnchorPane anchorpane;
    @FXML
    private ListView<String> PlaylistsLV;
    @FXML
    private TitledPane currenPlaylistTP;
    @FXML
    private ListView<Media> currentPlaylistLV;
    @FXML
    private SplitPane split;
    @FXML
    private Button NewPLButton;
    @FXML
    private ToolBar ToolBar;
    @FXML
    private Button AddFilesBtn;
    @FXML
    private Button URIBtn;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    public AnchorPane getAnchorpane() {
        return anchorpane;
    }

    public ListView<String> getPlaylistsLV() {
        return PlaylistsLV;
    }

    public TitledPane getCurrenPlaylistTP() {
        return currenPlaylistTP;
    }

    public ListView<Media> getCurrentPlaylistLV() {
        return currentPlaylistLV;
    }

    public SplitPane getSplit() {
        return split;
    }

    public Button getNewPLButton() {
        return NewPLButton;
    }

    public ToolBar getToolBar() {
        return ToolBar;
    }

    public Button getAddFilesBtn() {
        return AddFilesBtn;
    }

    public Button getURIBtn() {
        return URIBtn;
    }
}
