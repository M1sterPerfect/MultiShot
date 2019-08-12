package me.ruud.multishot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private MultiShot plugin;

    public CommandHandler(MultiShot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            Player player = (Player) sender;
            switch (args[0]) {
                case "single":
                    plugin.shotType.put(player.getUniqueId().toString(), "single");
                    plugin.messageHandler.sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", plugin.config.getString("single")}});
                    return true;
                case "double":
                    if (player.hasPermission("multishot.skills.double")) {
                        plugin.shotType.put(player.getUniqueId().toString(), "double");
                        plugin.messageHandler.sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", plugin.config.getString("double")}});
                    } else
                        plugin.messageHandler.sendMessage(player, "notMetSkill", new String[][] {{"\\{skillshot}", plugin.config.getString("double")}});
                    return true;
                case "triple":
                    if (player.hasPermission("multishot.skills.triple")) {
                        plugin.shotType.put(player.getUniqueId().toString(), "triple");
                        plugin.messageHandler.sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", plugin.config.getString("triple")}});
                    } else
                        plugin.messageHandler.sendMessage(player, "notMetSkill", new String[][] {{"\\{skillshot}", plugin.config.getString("triple")}});
                    return true;
            }
        }
        return false;
    }
}
