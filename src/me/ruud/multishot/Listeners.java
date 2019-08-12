package me.ruud.multishot;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Listeners implements Listener {

    private MultiShot plugin;
    private HashMap<String, Long> cooldown = new HashMap<>();
    private HashMap<String, Boolean> isSneaking = new HashMap<>();
    private Random random = new Random();

    public Listeners(MultiShot plugin) {
        this.plugin = plugin;
    }

    private String getNext(String startString) {
        for (int i = 0; i < plugin.multiShots.length; i++) {
            if (plugin.multiShots[i].equalsIgnoreCase(startString)) {
                return plugin.multiShots[++i%plugin.multiShots.length];
            }
        }
        return "";
    }

    private void shootArrows(Player player, int totalAmountOfArrows, int amount, ArrayList<ItemStack> arrowStacks, EntityShootBowEvent event) {
        String uuid = player.getUniqueId().toString();

        if (plugin.config.getBoolean("cooldownEnable")) {
            if (cooldown.containsKey(uuid) && System.currentTimeMillis() - cooldown.get(uuid) < plugin.config.getInt("cooldown") * 1000) {
                long timeLeft = plugin.config.getInt("cooldown") * 1000 - (System.currentTimeMillis() - cooldown.get(uuid));
                plugin.messageHandler.sendMessage(player, "cooldownMSG",  new String[][] {{"\\{skillshot}", plugin.config.getString(plugin.shotType.get(uuid))},
                        {"\\{cooldown}", String.valueOf(timeLeft / 1000 + 1)}});
                return;
            }
        }

        if (totalAmountOfArrows < amount) {
            plugin.messageHandler.sendMessage(player, "notEnoughArrows", new String[][] {{"\\{skillshot}", plugin.config.getString(plugin.shotType.get(uuid))}});
            player.updateInventory();
            return;
        }

        event.setCancelled(true);

        plugin.messageHandler.sendMessage(player, "skillShots", new String[][] {{"\\{skillshot}", plugin.config.getString( plugin.shotType.get(uuid))}});
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
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setBounce(false);
                if (event.getForce() == 1.0)
                    arrow.setCritical(true);
                arrow.setVelocity(arrow.getVelocity().add(new Vector((random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1))));
                i++;

            }
        }.runTaskTimer(plugin, 0,  plugin.config.getInt("delay"));

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
            plugin.shotType.putIfAbsent(uuid, "single");
            if ( plugin.shotType.get(uuid).equals("single")) {
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

                if ( plugin.shotType.get(uuid).equals("double") && player.hasPermission("multishot.skills.double")) {
                    shootArrows(player, totalAmountOfArrows, 2, arrowStacks, event);
                } else if ( plugin.shotType.get(uuid).equals("triple") && player.hasPermission("multishot.skills.triple")) {
                    shootArrows(player, totalAmountOfArrows, 3, arrowStacks, event);
                }
            }

        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR
                && isSneaking.get(player.getUniqueId().toString()) != null
                && isSneaking.get(player.getUniqueId().toString())
                && player.getInventory().getItemInMainHand().getType() == Material.BOW) {
            plugin.shotType.putIfAbsent(player.getUniqueId().toString(), "single");
            String multiShot =  plugin.shotType.get(player.getUniqueId().toString());
            while (!player.hasPermission("multishot.skills." + (multiShot = getNext(multiShot))) && !multiShot.equalsIgnoreCase("single")) {
                multiShot = getNext(plugin.shotType.get(player.getUniqueId().toString()));
            }
            plugin.shotType.put(player.getUniqueId().toString(), multiShot);
            plugin.messageHandler.sendMessage(player, "switchSkill", new String[][] {{"\\{skillshot}",  plugin.config.getString(multiShot)}});
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        isSneaking.put(event.getPlayer().getUniqueId().toString(), event.isSneaking());
    }

}
