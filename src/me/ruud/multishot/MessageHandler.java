package me.ruud.multishot;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageHandler {

    private MultiShot plugin;

    public MessageHandler(MultiShot plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(Player player, String configString, String[][] replacements) {
        String message = plugin.config.getString(configString);
        for (String[] replacement : replacements) {
            message = message.replaceAll(replacement[0], replacement[1]);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
