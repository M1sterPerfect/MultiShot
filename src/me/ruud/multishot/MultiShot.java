package me.ruud.multishot;

import me.ruud.multishot.listeners.Interact;
import me.ruud.multishot.listeners.Join;
import me.ruud.multishot.listeners.ShootBow;
import me.ruud.multishot.listeners.Sneak;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MultiShot extends JavaPlugin {

    public HashMap<Player, MSPlayer> players = new HashMap<>();

    public FileConfiguration config = getConfig();
    private FileConfiguration abilities;
    public FileConfiguration messages;
    public MessageHandler messageHandler;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();

        registerEvents();

        messageHandler = new MessageHandler(this);
        getCommand("multishot").setExecutor(new CommandHandler(this));

        initOnlinePlayers();
    }

    @Override
    public void onDisable() {

    }

    private void loadConfig() {
        abilities = loadCustomConfig("abilities", false);
        messages = loadCustomConfig("messages", true);
        loadAbilities();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new Interact(this), this);
        Bukkit.getPluginManager().registerEvents(new Join(this), this);
        Bukkit.getPluginManager().registerEvents(new ShootBow(this), this);
        Bukkit.getPluginManager().registerEvents(new Sneak(this), this);
    }

    private void initOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.put(player, new MSPlayer(player, this));
        }
    }

    private FileConfiguration loadCustomConfig(String fileName, boolean keepDefaults) {
        try {
            File File = new File(getDataFolder(), fileName + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(File);
            if (!File.exists() || keepDefaults) {
                Reader defConfigStream = new InputStreamReader(this.getResource(fileName + ".yml"), StandardCharsets.UTF_8);
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                config.setDefaults(defConfig);
                config.options().copyDefaults(true);
                config.save(File);
            }
            config = YamlConfiguration.loadConfiguration(File);
            return config;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadAbilities() {
        Ability previous = null;
        Ability first = null;
        if (config.getString("defaultAbility").equalsIgnoreCase("default")) {
            Ability defaultAbility = Ability.addDefault();
            first = defaultAbility;
            previous = defaultAbility;
        }

        for (String key : abilities.getConfigurationSection("abilities").getKeys(false)) {
            System.out.println(key);
            Ability ability = new Ability(key, abilities.getString("abilities." + key + ".name"), abilities.getInt("abilities." + key + ".arrows"));
            Ability.add(ability);
            if (previous != null) {
                previous.setNext(ability);
            }
            if (first == null)
                first = ability;
            previous = ability;
        }
        previous.setNext(first);
    }

}
