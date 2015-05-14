/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestPlugin;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import mms.Pluginsystem.Plugin;
import mms.Pluginsystem.PluginHost;

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

        Media m = new Media("http://tegos.kz/new/mp3_full/Eminem_feat_Rihanna_-_The_Monster.mp3");
//        Media m = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");

        MediaPlayer mp = new MediaPlayer(m);
        pluginHost.setPlayer(mp);
        mp.play();

        return true;
    }

    @Override
    public boolean stop() {
        System.out.println("Plugin stopped");
        return true;
    }

    @Override
    public String getDeveloper() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
