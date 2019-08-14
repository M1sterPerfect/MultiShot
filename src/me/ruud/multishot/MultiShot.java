package me.ruud.multishot;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class MultiShot extends JavaPlugin implements Listener, CommandExecutor {

    public HashMap<String, String> shotType = new HashMap<>();

    public FileConfiguration config = getConfig();
    public String[] multiShots = {"single", "double", "triple"};
    public MessageHandler messageHandler;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
        messageHandler = new MessageHandler(this);
        getCommand("multishot").setExecutor(new CommandHandler(this));

        initOnlinePlayers();
    }

    @Override
    public void onDisable() {

    }

    private void initOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            shotType.put(player.getUniqueId().toString(), "single");
        }
    }

}
