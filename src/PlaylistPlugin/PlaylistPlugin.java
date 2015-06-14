/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PlaylistPlugin;

/**
 *
 * @author Dominik
 */
import PlaylistPlugin.GUI.MediaCellController;
import PlaylistPlugin.GUI.PlaylistUIController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import mms.Pluginsystem.Impl.DefaultControlPlugin;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;
import mms.Pluginsystem.PluginHost.Identifier;
import javafx.collections.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class PlaylistPlugin extends Plugin {

    private PlaylistUIController controller;
    private int curPlaylist = 0, curTrack = 0, displayedPL = 0;
    private boolean curplaying = false;
    private boolean finished = false;
    private boolean looping = false;
    private Runnable EOMBackup;
    private FadeTransition fade;
    private boolean hideEffects = false;
    private ObservableList<String> PlaylistNames;
    private ArrayList<ObservableList<Media>> Playlists;
    private Button prevBtn, nextBtn;
    private Image defaultImage;
    private String tempPL;
    private List<String> Titles;
    private List<MediaPlayer> inithelper;

    //handles clicks on Tracks
    private final EventHandler<MouseEvent> TrackClick = (MouseEvent event) -> {
        if (event.getButton() == MouseButton.PRIMARY) {
            int selected = controller.getCurrentPlaylistLV().getSelectionModel().getSelectedIndex();
            if (selected != curTrack || curPlaylist != displayedPL || !curplaying) {
                if (selected >= 0 && selected < Playlists.get(displayedPL).size()) {
                    curTrack = selected;
                    try {
                        curplaying = true;
                        finished = true;
                        if (prevBtn != null && nextBtn != null) {
                            if (Playlists.get(displayedPL).size() > 1) {
                                prevBtn.setDisable(false);
                                nextBtn.setDisable(false);
                            } else {
                                prevBtn.setDisable(true);
                                nextBtn.setDisable(true);
                            }
                        }

                        pluginHost.setMedia(new URI(Playlists.get(displayedPL).get(curTrack).getSource()));
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            curPlaylist = displayedPL;
        }
    };

    @Override
    public void preInit() {
        super.preInit(); //To change body of generated methods, choose Tools | Templates.
        pluginHost.registerPluginListener(this, Identifier.ControlPlugin());
    }

    public PlaylistPlugin(PluginHost pluginHost) {
        super(pluginHost);

        try {
            defaultImage = new Image(getClass().getClassLoader().getResource("PlaylistPlugin/GUI/audio_file.png").openStream());
        } catch (IOException ex) {
            Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean start() {
        FXMLLoader fxmlLoader = new FXMLLoader();

        inithelper = new ArrayList<>();
        try {
            Pane root = fxmlLoader.load(getClass().getClassLoader().getResource("PlaylistPlugin/GUI/PlaylistUI.fxml").openStream());
            pluginHost.addToUIStack(root);
            controller = fxmlLoader.getController();
        } catch (IOException ex) {
            Logger.getLogger(DefaultControlPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        PlaylistNames = FXCollections.observableArrayList();
        Playlists = new ArrayList<>();

        fade = new FadeTransition(Duration.seconds(1), controller.getAnchorpane());

        controller.getPlaylistsLV().setCellFactory(param -> new PLCell());

        //Testdata
        pluginHost.addMediaListener(this);

        //Deserialize saved Playlists
        LinkedList<String> TNames;
        LinkedList<LinkedList<String>> TPlaylists;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("Playlist.dat"))) {
            TNames = (LinkedList<String>) in.readObject();
            TPlaylists = (LinkedList<LinkedList<String>>) in.readObject();

            if (TNames != null && TPlaylists != null) {
                PlaylistNames.addAll(TNames);
                for (int i = 0; i < TPlaylists.size(); i++) {
                    Playlists.add(FXCollections.observableArrayList());
                    for (String s : TPlaylists.get(i)) {
                        try {
                            Media m = new Media(s);
                            MediaPlayer mediaPlayer = new MediaPlayer(m);
                            mediaPlayer.setOnReady(new MediaInitializer(Playlists.get(i), m, mediaPlayer));
                        } catch (MediaException e) {

                        }
                    }
                }
            }
        } catch (IOException i) {
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (PlaylistNames.size() > displayedPL) {
            controller.getCurrentPlaylistLV().setItems(Playlists.get(displayedPL));
            controller.getCurrenPlaylistTP().setText(PlaylistNames.get(displayedPL));
        }

        controller.getCurrentPlaylistLV().setCellFactory(param -> new TrackCell());
        controller.getPlaylistsLV().setItems(PlaylistNames);

        //user selected a playlist
        //change listener on selection model doesnt work
        controller.getPlaylistsLV().setOnMouseClicked((MouseEvent event) -> {
            int selected = controller.getPlaylistsLV().getSelectionModel().getSelectedIndex();
            selecPlaylist(selected);
        });

        controller.getPlaylistsLV().setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            setControlHideEffects(false);
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            } else {
                event.consume();
            }
        });

        //add Files as new playlist
        controller.getPlaylistsLV().setOnDragDropped((DragEvent event) -> {
            Optional<String> result = showPlaylistNameDialog();
            result.ifPresent(name -> {
                if (!name.isEmpty()) {
                    PlaylistNames.add(name);
                    Playlists.add(FXCollections.observableArrayList());
                    controller.getPlaylistsLV().getSelectionModel().select(PlaylistNames.size() - 1);
                    selecPlaylist(PlaylistNames.size() - 1);
                }
            });

            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath;

                for (File file : db.getFiles()) {
                    filePath = file.toURI().toString();
                    Media m = new Media(filePath);
                    MediaPlayer mediaPlayer = new MediaPlayer(m);
                    mediaPlayer.setOnReady(new MediaInitializer(Playlists.get(displayedPL), m, mediaPlayer));
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        //dragevents for current playlist
        controller.getCurrenPlaylistTP()
                .setOnDragOver((DragEvent event) -> {
                    setControlHideEffects(false);
                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        event.acceptTransferModes(TransferMode.LINK);
                    } else {
                        event.consume();
                    }
                });

        controller.getCurrenPlaylistTP()
                .setOnDragDropped((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasFiles()) {
                        success = true;
                        for (File file : db.getFiles()) {
                            Media m = new Media(file.toURI().toString());
                            MediaPlayer mediaPlayer = new MediaPlayer(m);
                            mediaPlayer.setOnReady(new MediaInitializer(Playlists.get(displayedPL), m, mediaPlayer));
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

        // User clicked on a track
        controller.getCurrentPlaylistLV()
                .setOnMouseClicked(TrackClick);

        //new playlist button
        controller.getNewPLButton()
                .setOnMouseClicked((MouseEvent event) -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        Optional<String> result = showPlaylistNameDialog();
                        result.ifPresent(name -> {
                            if (!name.isEmpty()) {
                                PlaylistNames.add(name);
                                Playlists.add(FXCollections.observableArrayList());
                                controller.getPlaylistsLV().getSelectionModel().select(PlaylistNames.size() - 1);
                                selecPlaylist(PlaylistNames.size() - 1);
                            }
                        });
                    }
                }
                );

        //User clicked on add URL
        controller.getURIBtn().setOnMouseClicked((MouseEvent event) -> {
            if (PlaylistNames.size() > 0 && event.getButton() == MouseButton.PRIMARY) {
                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Add URL to Playlist");
                dialog.setHeaderText("Enter the URL/URI to a Media File");
                dialog.setContentText("URL:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        Media m = new Media(result.get());
                        MediaPlayer mediaPlayer = new MediaPlayer(m);
                        mediaPlayer.setOnReady(new MediaInitializer(Playlists.get(displayedPL), m, mediaPlayer));
                    } catch (Exception e) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Warning");
                        alert.setHeaderText("Invalid URL");
                        alert.setContentText("The URL can't be added to the Playlist.");

                        alert.showAndWait();
                    }
                }
            }
        });

        //User clicked on add Files
        controller.getAddFilesBtn()
                .setOnMouseClicked((MouseEvent event) -> {
                    if (PlaylistNames.size() > 0 && event.getButton() == MouseButton.PRIMARY) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Add Files to selected Playlist");
                        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                        List<String> temp = new ArrayList<>();
                        temp.addAll(java.util.Arrays.asList(PluginHost.getSupportedVideoFormats()));
                        temp.addAll(java.util.Arrays.asList(PluginHost.getSupportedAudioFormats()));

                        fileChooser.getExtensionFilters().addAll(
                                new ExtensionFilter("Audio Files", PluginHost.getSupportedAudioFormats()),
                                new ExtensionFilter("Video Files", PluginHost.getSupportedVideoFormats()),
                                new ExtensionFilter("All supported Files", temp));

                        List<File> list = fileChooser.showOpenMultipleDialog(null);
                        if (list != null) {
                            for (File file : list) {
                                Media m = new Media(file.toURI().toString());
                                MediaPlayer mediaPlayer = new MediaPlayer(m);
                                Playlists.get(displayedPL).add(m);
                            }
                        }
                    }
                }
                );

        //set tooltips
        controller.getAddFilesBtn()
                .setTooltip(new Tooltip("Add Files to selected Playlist"));
        controller.getURIBtn()
                .setTooltip(new Tooltip("Add URL to selected Playlist"));
        controller.getNewPLButton()
                .setTooltip(new Tooltip("Create a new Playlist"));

        //Events for fading effect
        controller.getPlaylistsLV()
                .addEventHandler(MouseEvent.MOUSE_ENTERED, MouseEvent -> {
                    setControlHideEffects(false);
                }
                );

        controller.getSplit()
                .addEventHandler(MouseEvent.MOUSE_EXITED, MouseEvent -> {
                    setControlHideEffects(true);
                }
                );
        //loads playlists
        System.out.println(
                "Playlist Plugin started");

        return true;
    }

    private Optional<String> showPlaylistNameDialog() {
        TextInputDialog dialog = new TextInputDialog("Playlist" + (PlaylistNames.size() + 1));
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Please enter a name for the new Playlist");
        dialog.setContentText("Playlist Title:");
        return dialog.showAndWait();
    }

    private void addPlaylist(String name, List<Media> Tracks) {
        PlaylistNames.add(name);
        Playlists.add(FXCollections.observableArrayList(Tracks));
    }

    private void selecPlaylist(int selected) {
        if (selected >= 0 && selected < PlaylistNames.size()) {
            controller.getCurrenPlaylistTP().setText(PlaylistNames.get(selected));
            controller.getCurrentPlaylistLV().setItems(Playlists.get(selected));

            displayedPL = selected;
        }
    }

    private void setControlHideEffects(boolean b) {
        if (!hideEffects && b) {
            fade.setDelay(Duration.seconds(0));
            fade.setFromValue(1);
            fade.setToValue(1);
            fade.playFromStart();
            fade.setOnFinished(ActionEvent -> {
                fade.setOnFinished(null);
                fade.setDelay(Duration.seconds(1));
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.playFromStart();
            });
            hideEffects = true;
        } else if (hideEffects && !b) {
            fade.setOnFinished(null);
            fade.setDelay(Duration.seconds(0));
            fade.setFromValue(1);
            fade.setToValue(1);
            fade.playFromStart();
            hideEffects = false;
        }
    }

    @Override
    public void onMediaPlayerChanged(MediaPlayer player) {
        super.onMediaPlayerChanged(player); //To change body of generated methods, choose Tools | Templates.
        if (curplaying && finished) {
            EOMBackup = player.getOnEndOfMedia();

            player.setOnEndOfMedia(() -> {
                EOMBackup.run();

                player.setOnPlaying(() -> {
                    finished = false;
                    curplaying = true;
                    prevBtn.setDisable(false);
                    nextBtn.setDisable(false);
                });

                finished = true;
                if ((curTrack + 1) < Playlists.get(curPlaylist).size()) {
                    curTrack++;
                } else if (looping) {
                    curTrack = 0;
                } else {
                    curplaying = false;
                    if (prevBtn != null && nextBtn != null) {
                        prevBtn.setDisable(true);
                        nextBtn.setDisable(true);
                    }
                    return;
                }

                try {
                    pluginHost.setMedia(new URI(Playlists.get(curPlaylist).get(curTrack).getSource()));
                    controller.getCurrentPlaylistLV().getSelectionModel().select(curTrack);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }

            });
        } else {
            curplaying = false;
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
        }
        finished = false;
    }

    @Override
    public void onEventReceived(String eventID, Object... args) {
        switch (eventID) {
            case "CycleChanged":
                looping = (boolean) args[0];
                break;
            case "PrevButton":
                prevBtn = (Button) args[0];
                prevBtn.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        curTrack--;
                        if (curTrack < 0) {
                            curTrack = Playlists.get(curPlaylist).size();
                        }
                        curplaying = true;
                        finished = true;
                        controller.getCurrentPlaylistLV().getSelectionModel().select(curTrack);
                        try {
                            pluginHost.setMedia(new URI(Playlists.get(curPlaylist).get(curTrack).getSource()));
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                break;
            case "NextButton":
                nextBtn = (Button) args[0];
                nextBtn.setOnMouseClicked((MouseEvent event) -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        curTrack++;
                        if (curTrack >= Playlists.get(curPlaylist).size()) {
                            curTrack = 0;
                        }
                        curplaying = true;
                        finished = true;
                        controller.getCurrentPlaylistLV().getSelectionModel().select(curTrack);
                        try {
                            pluginHost.setMedia(new URI(Playlists.get(curPlaylist).get(curTrack).getSource()));
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                break;
            case "addTempPlaylist":
                String Name = (String) args[0];
                List<String> titles = (List<String>) args[2];
                this.Titles = titles;
                List<Media> clips = (List<Media>) args[1];
                PlaylistNames.add(Name);
                ObservableList<Media> mlist = FXCollections.observableArrayList();
                if (tempPL != null) {
                    int i = PlaylistNames.indexOf(tempPL);
                    PlaylistNames.remove(i);
                    Playlists.remove(i);
                }
                tempPL = Name;
                for (Media m : clips) {
                    mlist.add(m);
                }
                Playlists.add(mlist);
                selecPlaylist(PlaylistNames.indexOf(Name));
                controller.getPlaylistsLV().getSelectionModel().select(PlaylistNames.indexOf(Name));
                curTrack = 0;
                try {
                    curplaying = true;
                    finished = true;
                    if (prevBtn != null && nextBtn != null) {
                        if (Playlists.get(displayedPL).size() > 1) {
                            prevBtn.setDisable(false);
                            nextBtn.setDisable(false);
                        } else {
                            prevBtn.setDisable(true);
                            nextBtn.setDisable(true);
                        }
                    }

                    pluginHost.setMedia(new URI(Playlists.get(displayedPL).get(curTrack).getSource()));
                } catch (URISyntaxException ex) {
                    Logger.getLogger(PlaylistPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
    }

    private class TrackCell extends ListCell<Media> {

        private MediaCellController CellController;

        public TrackCell() {
            ListCell thisCell = this;
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.TOP_LEFT);
            FXMLLoader fxmlLoader = new FXMLLoader();

            try {
                Pane root = fxmlLoader.load(getClass().getClassLoader().getResource("PlaylistPlugin/GUI/MediaCell.fxml").openStream());
                CellController = fxmlLoader.getController();
            } catch (IOException ex) {
                Logger.getLogger(DefaultControlPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        protected void updateItem(Media item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                setGraphic(CellController.getAnchorpane());

                if (item.getMetadata().containsKey("image")) {
                    CellController.getImage().setImage((Image) item.getMetadata().get("image"));
                } else {
                    CellController.getImage().setImage(defaultImage);
                }

                if (item.getMetadata().containsKey("title")) {
                    CellController.getTitle().setText((String) item.getMetadata().get("title"));
                } else {
                    if (displayedPL == PlaylistNames.indexOf(tempPL)) {
                        CellController.getTitle().setText(Titles.get(this.getIndex()));
                    } else {
                        CellController.getTitle().setText(item.getSource().substring(item.getSource().lastIndexOf('/') + 1));
                    }
                }

                if (item.getMetadata().containsKey("artist")) {
                    CellController.getArtist().setText((String) item.getMetadata().get("artist"));
                } else {
                    CellController.getArtist().setText("Various Artists");
                }

                if (item.getSource().startsWith("file")) {
                    CellController.getDuration().setText("Local");
                } else {
                    CellController.getDuration().setText("Stream");
                }
                ContextMenu context = new ContextMenu();
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction((ActionEvent event) -> {
                    int index = controller.getCurrentPlaylistLV().getItems().indexOf(item);
                    Playlists.get(displayedPL).remove(index);
                    //selecPlaylist(displayedPL,true);
                });
                context.getItems().add(delete);
                this.setContextMenu(context);
            } else {
                //Empty Item, without set null this entry wont disappear
                setGraphic(null);
            }
        }
    }

    private class PLCell extends ListCell<String> {

        public PLCell() {
            ListCell thisCell = this;
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item);
                ContextMenu context = new ContextMenu();
                MenuItem delete = new MenuItem("Delete");
                delete.setOnAction((ActionEvent event) -> {
                    int index = controller.getPlaylistsLV().getItems().indexOf(item);
                    PlaylistNames.remove(item);
                    Playlists.remove(index);
                    if (Playlists.size() > 0) {
                        if (index < Playlists.size()) {
                            selecPlaylist(index);
                        } else {
                            selecPlaylist(Playlists.size() - 1);
                        }
                    } else {
                        controller.getCurrentPlaylistLV().setItems(null);
                    }

                    if (item.equals(tempPL)) {
                        tempPL = null;
                    }
                });
                context.getItems().add(delete);
                this.setContextMenu(context);
            } else {
                setText("");   // <== clear the now empty cell.
            }
        }
    }

    private class MediaInitializer implements Runnable {

        ObservableList<Media> list;
        Media m;
        MediaPlayer mp;

        public MediaInitializer(ObservableList<Media> list, Media m, MediaPlayer mp) {
            this.list = list;
            this.m = m;
            this.mp = mp;
            inithelper.add(mp);
        }

        @Override
        public void run() {
            list.add(m);
            inithelper.remove(mp);
        }
    }

    @Override
    public boolean stop() {
        System.out.println("Playlist Plugin stopped");
        //save playlist
        LinkedList<String> TNames = new LinkedList<>(PlaylistNames);
        if (tempPL != null) {
            int j = PlaylistNames.indexOf(tempPL);
            Playlists.remove(j);
            TNames.remove(tempPL);
        }
        LinkedList<LinkedList<String>> TPlaylists = new LinkedList<>();

        for (int i = 0; i < Playlists.size(); i++) {
            TPlaylists.add(new LinkedList<>());
            for (Media m : Playlists.get(i)) {
                TPlaylists.get(i).add(m.getSource());
            }
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("Playlist.dat"))) {
            out.writeObject(TNames);
            out.writeObject(TPlaylists);
        } catch (IOException i) {
            i.printStackTrace();
        }
        return true;
    }

    @Override
    public String getDeveloper() {
        return "AOPP Studios";
    }

    @Override
    public String getName() {
        return PlaylistPlugin.class.getSimpleName();
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
