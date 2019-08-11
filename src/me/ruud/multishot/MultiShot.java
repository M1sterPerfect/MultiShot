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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MultiShot extends JavaPlugin implements Listener, CommandExecutor {

    private HashMap<String, Boolean> isSneaking = new HashMap<>();
    private HashMap<String, String> shotType = new HashMap<>();
    private HashMap<String, Long> cooldown = new HashMap<>();
    private FileConfiguration config = getConfig();
    private Random random = new Random();

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

        if (config.getBoolean("cooldownEnable")) {
            if (cooldown.containsKey(uuid) && System.currentTimeMillis() - cooldown.get(uuid) < config.getInt("cooldown") * 1000) {
                long timeLeft = config.getInt("cooldown") * 1000 - (System.currentTimeMillis() - cooldown.get(uuid));
                sendMessage(player, "cooldownMSG",  new String[][] {{"\\{skillshot}", config.getString(shotType.get(uuid))},
                                                                                {"\\{cooldown}", String.valueOf(timeLeft / 1000 + 1)}});
                return;
            }
        }

        if (totalAmountOfArrows < amount) {
            sendMessage(player, "notEnoughArrows", new String[][] {{"\\{skillshot}", config.getString(shotType.get(uuid))}});
            player.updateInventory();
            return;
        }

        event.setCancelled(true);

        sendMessage(player, "skillShots", new String[][] {{"\\{skillshot}", config.getString(shotType.get(uuid))}});
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
                Projectile projectile = player.launchProjectile(Arrow.class);
                projectile.setBounce(false);
                if (event.getForce() == 1.0)
                    ((Arrow) projectile).setCritical(true);
                projectile.setVelocity(projectile.getVelocity().add(new Vector((random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1))));
                i++;

            }
        }.runTaskTimer(this, 0, config.getInt("delay"));

        cooldown.put(uuid, System.currentTimeMillis());

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
            shotType.putIfAbsent(uuid, "single");
            if (shotType.get(uuid).equals("single")) {
                return;
            }
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

                if (shotType.get(uuid).equals("double") && player.hasPermission("multishot.skills.double")) {
                    shootArrows(player, totalAmountOfArrows, 2, arrowStacks, event);
                } else if (shotType.get(uuid).equals("triple") && player.hasPermission("multishot.skills.triple")) {
                    shootArrows(player, totalAmountOfArrows, 3, arrowStacks, event);
                }
            }

        }
    }

    private void sendMessage(Player player, String configString, String[][] replacements) {
        String message = config.getString(configString);
        for (String[] replacement : replacements) {
            message = message.replaceAll(replacement[0], replacement[1]);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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
                case "single":
                    shotType.put(player.getUniqueId().toString(), "single");
                    sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", config.getString("single")}});
                    return true;
                case "double":
                    if (player.hasPermission("multishot.skills.double")) {
                        shotType.put(player.getUniqueId().toString(), "double");
                        sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", config.getString("double")}});
                    } else
                        sendMessage(player, "notMetSkill", new String[][] {{"\\{skillshot}", config.getString("double")}});
                    return true;
                case "triple":
                    if (player.hasPermission("multishot.skills.triple")) {
                        shotType.put(player.getUniqueId().toString(), "triple");
                        sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}", config.getString("triple")}});
                    } else
                        sendMessage(player, "notMetSkill", new String[][] {{"\\{skillshot}", config.getString("triple")}});
                    return true;
            }
        }
        return false;
    }

}
