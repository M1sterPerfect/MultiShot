package me.ruud.multishot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiShot extends JavaPlugin implements Listener, CommandExecutor {

    private HashMap<String, Boolean> isSneaking = new HashMap<>();
    private HashMap<String, String> shotType = new HashMap<>();
    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("multishot").setExecutor(this);
    }

    @Override
    public void onDisable() {

    }

    private void shootArrows(Player player, int totalAmountOfArrows, int amount, ArrayList<ItemStack> arrowStacks, EntityShootBowEvent event) {
        String uuid = player.getUniqueId().toString();

        if (totalAmountOfArrows < amount) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("notEnoughArrows")).replaceAll("\\{skillshot}", config.getString(shotType.get(uuid))));
            player.updateInventory();
            return;
        }

        event.setCancelled(true);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("skillShots").replaceAll("\\{skillshot}", config.getString(shotType.get(uuid)))));

        int finalAmount = amount;

        new BukkitRunnable()
        {
            int i = 0;
            @Override
            public void run() {
                if (i == finalAmount) {
                    this.cancel();
                    return;
                }
                player.launchProjectile(Arrow.class);
                i++;

            }
        }.runTaskTimer(this, 0, config.getInt("delay"));
        /*for (int i = 0; i < amount; i++) {
            player.launchProjectile(Arrow.class);
        }*/

        for (ItemStack arrow : arrowStacks) {
            if (amount > arrow.getAmount()) {
                amount -= arrow.getAmount();
                arrow.setAmount(0);
            } else {
                arrow.setAmount(arrow.getAmount() - amount);
                return;
            }
        }
    }

    @EventHandler
    public void onBowShot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
            Player player = (Player) event.getEntity();
            String uuid = player.getUniqueId().toString();
            if (isSneaking.get(uuid) != null && isSneaking.get(uuid)) {
                ArrayList<ItemStack> arrowStacks = new ArrayList<>();
                int totalAmountOfArrows = 0;
                ItemStack offHand = null;
                if (player.getInventory().getItemInOffHand().getType() == Material.ARROW) {
                    offHand = player.getInventory().getItemInOffHand();
                    arrowStacks.add(player.getInventory().getItemInOffHand());
                    totalAmountOfArrows += player.getInventory().getItemInOffHand().getAmount();
                }
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null || item == offHand) continue;
                    if (item.getType() == Material.ARROW) {
                        arrowStacks.add(item);
                        totalAmountOfArrows += item.getAmount();
                    }
                }

                shotType.putIfAbsent(uuid, "double");
                if (shotType.get(uuid).equals("double") && player.hasPermission("multishot.skills.double")) {
                    shootArrows(player, totalAmountOfArrows, 2, arrowStacks, event);
                } else if (shotType.get(uuid).equals("triple") && player.hasPermission("multishot.skills.triple")) {
                    shootArrows(player, totalAmountOfArrows, 3, arrowStacks, event);
                }
            }

        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        isSneaking.put(event.getPlayer().getUniqueId().toString(), event.isSneaking());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            Player player = (Player) sender;
            switch (args[0]) {
                case "double":
                    if (player.hasPermission("multishot.skills.double"))
                        shotType.put(player.getUniqueId().toString(), "double");
                    //else
                        //player.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                case "triple":
                    if (player.hasPermission("multishot.skills.triple"))
                        shotType.put(player.getUniqueId().toString(), "triple");
                    else
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("notMetSkill")));
                    return true;
            }
        }
        return false;
    }

}
