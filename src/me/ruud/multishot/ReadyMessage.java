package me.ruud.multishot;

import org.bukkit.scheduler.BukkitRunnable;

public class ReadyMessage extends BukkitRunnable {

    private MultiShot plugin;
    private MSPlayer player;

    public ReadyMessage(MultiShot plugin, MSPlayer player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.messageHandler.sendMessage(player, "refreshed", new String[][]{{"\\{skillshot}", player.getCurrentAbility().getDisplayName()}});
    }
}
