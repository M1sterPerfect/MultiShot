package me.ruud.multishot.listeners;

import me.ruud.multishot.Ability;
import me.ruud.multishot.MSPlayer;
import me.ruud.multishot.MultiShot;
import me.ruud.multishot.ReadyMessage;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ShootBow implements Listener {

    private MultiShot plugin;

    private HashMap<String, Long> cooldown = new HashMap<>();
    private Random random = new Random();

    public ShootBow(MultiShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBowShot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
            MSPlayer player = plugin.players.get((Player) event.getEntity());
            if (player.getCurrentAbility().getName().equalsIgnoreCase("default")) {
                return;
            }
            if (player.isSneaking()) {
                ArrayList<ItemStack> arrowStacks = new ArrayList<>();
                int totalAmountOfArrows = 0;
                ItemStack offHand = null;
                if (player.getPlayer().getInventory().getItemInOffHand().getType() == Material.ARROW) {
                    offHand = player.getPlayer().getInventory().getItemInOffHand();
                    arrowStacks.add(player.getPlayer().getInventory().getItemInOffHand());
                    totalAmountOfArrows += player.getPlayer().getInventory().getItemInOffHand().getAmount();
                }
                for (ItemStack item : player.getPlayer().getInventory().getContents()) {
                    if (item == null || item == offHand) continue;
                    if (item.getType() == Material.ARROW) {
                        arrowStacks.add(item);
                        totalAmountOfArrows += item.getAmount();
                    }
                }

                shootArrows(player, totalAmountOfArrows, arrowStacks, player.getCurrentAbility(), event);

                /*if ( plugin.shotType.get(uuid).equals("double") && player.hasPermission("multishot.skills.double")) {
                    shootArrows(player, totalAmountOfArrows, 2, arrowStacks, event);
                } else if ( plugin.shotType.get(uuid).equals("triple") && player.hasPermission("multishot.skills.triple")) {
                    shootArrows(player, totalAmountOfArrows, 3, arrowStacks, event);
                }*/
            }

        }
    }

    private void shootArrows(MSPlayer player, int totalAmountOfArrows, ArrayList<ItemStack> arrowStacks, Ability ability, EntityShootBowEvent event) {
        if (plugin.config.getBoolean("cooldownEnabled")) {
            if (cooldown.containsKey(player.getUuid()) && System.currentTimeMillis() - cooldown.get(player.getUuid()) < plugin.config.getInt("cooldown") * 1000) {
                long timeLeft = plugin.config.getInt("cooldown") * 1000 - (System.currentTimeMillis() - cooldown.get(player.getUuid()));
                plugin.messageHandler.sendMessage(player, "cooldown",  new String[][] {{"\\{skillshot}", player.getCurrentAbility().getDisplayName()},
                        {"\\{cooldown}", String.valueOf(timeLeft / 1000 + 1)}});
                return;
            }
        }

        if (totalAmountOfArrows < ability.getAmount()) {
            plugin.messageHandler.sendMessage(player, "notEnoughArrows", new String[][] {{"\\{skillshot}", player.getCurrentAbility().getDisplayName()}});
            player.getPlayer().updateInventory();
            return;
        }

        event.setCancelled(true);

        plugin.messageHandler.sendMessage(player, "performSkill", new String[][] {{"\\{skillshot}", player.getCurrentAbility().getDisplayName()}});
        int finalAmount = ability.getAmount();

        new BukkitRunnable()
        {
            int i = 0;
            @Override
            public void run() {
                if (i == finalAmount) {
                    this.cancel();
                    return;
                }
                Arrow arrow = player.getPlayer().launchProjectile(Arrow.class);
                arrow.setBounce(false);
                if (event.getForce() == 1.0)
                    arrow.setCritical(true);
                arrow.setVelocity(arrow.getVelocity().add(new Vector((random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1), (random.nextDouble() / 5) * (random.nextBoolean() ? -1 : 1))));
                i++;

            }
        }.runTaskTimer(plugin, 0,  plugin.config.getInt("delayBetweenShots"));

        cooldown.put(player.getUuid(), System.currentTimeMillis());
        new ReadyMessage(plugin, player).runTaskLater(plugin,(long) (plugin.config.getInt("cooldown") / 0.05));


        int amount = ability.getAmount();
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

}
