package me.ruud.multishot;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReadyMessage extends BukkitRunnable {

    private MultiShot plugin;
    private Player player;

    public ReadyMessage(MultiShot plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.messageHandler.sendMessage(player, "refreshedMSG", new String[][]{{"\\{skillshot}", plugin.config.getString(plugin.shotType.get(player.getUniqueId().toString()))}});
    }
}
