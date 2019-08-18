package me.ruud.multishot.listeners;

import me.ruud.multishot.Ability;
import me.ruud.multishot.MSPlayer;
import me.ruud.multishot.MultiShot;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Interact implements Listener {

    private MultiShot plugin;

    public Interact(MultiShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        MSPlayer player = plugin.players.get(event.getPlayer());
        if (event.getAction() == Action.LEFT_CLICK_AIR
                && player.isSneaking()
                && player.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOW) {
            Ability nextAbility = player.getCurrentAbility().next();
            while (!player.getPlayer().hasPermission("multishot.skills." + nextAbility.getName()) && !nextAbility.getName().equalsIgnoreCase(plugin.config.getString("defaultAbility"))) {
                nextAbility = nextAbility.next();
            }
            player.setCurrentAbility(nextAbility);
            plugin.messageHandler.sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}",  nextAbility.getDisplayName()}});
        }
    }
}
