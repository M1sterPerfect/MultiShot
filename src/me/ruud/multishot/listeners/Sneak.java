package me.ruud.multishot.listeners;

import me.ruud.multishot.MultiShot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class Sneak implements Listener {

    private MultiShot plugin;

    public Sneak(MultiShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        plugin.players.get(event.getPlayer()).setSneaking(event.isSneaking());
    }

}
