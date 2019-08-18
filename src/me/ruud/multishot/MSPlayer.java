package me.ruud.multishot;

import org.bukkit.entity.Player;

public class MSPlayer {

    private Player player;
    //private MultiShot plugin;
    private Ability currentAbility;
    private boolean sneaking;

    public MSPlayer(Player player, MultiShot plugin) {
        this.player = player;
        //this.plugin = plugin;
        this.currentAbility = Ability.get(plugin.config.getString("defaultAbility"));
        sneaking = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getUuid() {
        return player.getUniqueId().toString();
    }

    public Ability getCurrentAbility() {
        return currentAbility;
    }

    public void setCurrentAbility(Ability currentAbility) {
        this.currentAbility = currentAbility;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }
}
