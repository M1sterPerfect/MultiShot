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
            MSPlayer player = plugin.players.get((Player) sender);

            if ("default".equals(args[0])) {
                Ability ability = Ability.get(plugin.config.getString("defaultAbility"));
                player.setCurrentAbility(ability);
                plugin.messageHandler.sendMessage(player, "switchSkill", new String[][]{{"\\{skillshot}", ability.getDisplayName()}});
                return true;
            }

            Ability ability = Ability.get(args[0]);
            System.out.println(args[0]);
            if (ability == null) {
                plugin.messageHandler.sendMessage(player, "notExisting", new String[][]{{"\\{variable}", args[0]}});
                return true;
            }

            if (player.getPlayer().hasPermission("multishot.skills." + args[0])) {
                player.setCurrentAbility(ability);
                System.out.println(ability.getDisplayName());
                plugin.messageHandler.sendMessage(player, "switchSkill", new String[][]{{"\\{skillshot}", ability.getDisplayName()}});
            } else
                plugin.messageHandler.sendMessage(player, "notUnlocked", new String[][]{{"\\{skillshot}", ability.getDisplayName()}});
            return true;
        }
        return false;
    }
}
