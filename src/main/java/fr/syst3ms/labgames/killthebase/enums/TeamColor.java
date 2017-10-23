package fr.syst3ms.labgames.killthebase.enums;

import org.bukkit.ChatColor;

/**
 * Created by ARTHUR on 13/10/2017.
 */
public enum TeamColor {
    BLEU("Bleu", "Bleue", ChatColor.BLUE),
    ROUGE("Rouge", "Rouge", ChatColor.RED),
    VERT("Vert", "Verte", ChatColor.GREEN),
    JAUNE("Jaune", "Jaune", ChatColor.YELLOW);

    private final String name;
    private final String feminine;
    private final ChatColor color;

    TeamColor(String name, String feminine, ChatColor color) {
        this.name = name;
        this.feminine = feminine;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getFeminine() {
        return feminine;
    }

    public ChatColor getColor() {
        return color;
    }
}
