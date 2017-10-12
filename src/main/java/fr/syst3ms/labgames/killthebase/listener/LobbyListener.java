package fr.syst3ms.labgames.killthebase.listener;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.github.paperspigot.Title;

public class LobbyListener implements Listener {
	public static final World LOBBY_WORLD = Bukkit.getWorld("lobby");
	public static final Location LOBBY_LOCATION = new Location(LOBBY_WORLD,0, 0, 0);
	public static final int MINIMUM_HEIGHT = 0;

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Bukkit.getScheduler().runTask(
			Bukkit.getPluginManager().getPlugin("KillTheBase"),
			() -> {
				Player p = e.getPlayer();
				p.teleport(LOBBY_LOCATION);
				BaseComponent[] title = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "§5&lKillTheBase"));
				p.sendTitle(new Title(title));
			}
		);
		if ()
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p.getLocation().getBlockY() <= MINIMUM_HEIGHT) {
			p.teleport(LOBBY_LOCATION);
			p.sendMessage(ChatColor.GRAY + " Évitez de sauter, ça m'énerve de toujours devoir vous téléporter");
		}
	}
}
