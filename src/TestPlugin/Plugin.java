/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestPlugin;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import mms.Pluginsystem.PluginInterface;
import mms.Pluginsystem.PluginManagerInterface;

/**
 *
 * @author Michael Obermüller
 */
public class Plugin implements PluginInterface {

    private PluginManagerInterface manager;
    
    @Override
    public boolean start() {
        System.out.println("Plugin started");
        
        Media m = new Media("http://tegos.kz/new/mp3_full/Eminem_feat_Rihanna_-_The_Monster.mp3");
//        Media m = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        
        MediaPlayer mp = new MediaPlayer(m);
        manager.setPlayer(mp);
        mp.play();
        
        return true;
    }

    @Override
    public boolean stop() {
        System.out.println("Plugin stopped");
        return true;
    }

    @Override
    public void setPluginManager(PluginManagerInterface manager) {
        this.manager = manager;
    }
    
}
