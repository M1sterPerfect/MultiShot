package me.ruud.multishot;

import java.util.HashMap;

public class Ability {

    private String name;
    private String displayName;
    private int amount;
    private Ability next;

    public static HashMap<String, Ability> abilities = new HashMap<>();

    public Ability(String name, String displayName, int amount) {
        this.name = name;
        this.displayName = displayName;
        this.amount = amount;
    }

    public static void add(Ability ability) {
        abilities.put(ability.name, ability);
    }

    public static Ability addDefault() {
        Ability defaultAbility = new Ability("default", "default", 1);
        abilities.put("default", defaultAbility);
        return defaultAbility;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAmount() {
        return amount;
    }

    public static Ability get(String abilityName) {
        return abilities.get(abilityName);
    }

    public Ability next() {
        return next;
    }

    public void setNext(Ability next) {
        this.next = next;
    }
}
