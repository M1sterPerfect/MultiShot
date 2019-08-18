package me.ruud.multishot.listeners;

import me.ruud.multishot.MSPlayer;
import me.ruud.multishot.MultiShot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    private MultiShot plugin;

    public Join(MultiShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.players.put(event.getPlayer(), new MSPlayer(event.getPlayer(), plugin));
    }

}
