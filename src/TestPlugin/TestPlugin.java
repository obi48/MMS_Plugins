/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;
import mms.Pluginsystem.PluginHost.Identifier;

/**
 *
 * @author Michael Obermüller
 */
public class TestPlugin extends Plugin {

    public TestPlugin(PluginHost pluginHost) {
        super(pluginHost);
    }

    @Override
    public boolean start() {
        System.out.println("Plugin started");

        //Media m = new Media("http://tegos.kz/new/mp3_full/Eminem_feat_Rihanna_-_The_Monster.mp3");
        Media m = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");

        MediaPlayer mp = new MediaPlayer(m);
        pluginHost.setPlayer(mp);
        mp.play();

        //Register this plugin as listener on ControlPlugin
        pluginHost.registerPluginListener(this, Identifier.ControlPlugin());
        
        //Register this plugin as listener on MenuPlugin
        pluginHost.registerPluginListener(this, Identifier.MenuPlugin());
        
        //Register this plugin as listener on arbitrary Plugin
        //pluginHost.registerPluginListener(this, Identifier.Plugin("DevName,PluginName,1.0"));
        
        //Add a new Menu
//        Menu testMenu = new Menu("TestPluginMenu");
//        testMenu.getItems().add(new MenuItem("test"));
//        pluginHost.getMenus().add(testMenu);
//        
//        try {
//            Pane root = (Pane) FXMLLoader.load(getClass().getClassLoader().getResource("TestPlugin/GUI/FXML.fxml"));
//            pluginHost.addToUIStack(root);
//        } catch (IOException ex) {
//            Logger.getLogger(TestPlugin.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        return true;
    }

    @Override
    public boolean stop() {
        System.out.println("Plugin stopped");
        return true;
    }

    @Override
    public String getDeveloper() {
        return "MMS";
    }

    @Override
    public String getName() {
        return TestPlugin.class.getCanonicalName();
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
        //Ask developer of listened plugin for MessageIDs and possible arguments
        switch(eventID){
            case "message1": { 
                System.out.println("bla");
            }
        }
    }
}
