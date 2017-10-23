package fr.syst3ms.labgames.killthebase.listener;

import fr.labgames.api.utils.messages.MessageManager;
import fr.syst3ms.labgames.killthebase.KillTheBase;
import fr.syst3ms.labgames.killthebase.classes.TeamManager;
import fr.syst3ms.labgames.killthebase.enums.TeamColor;
import fr.syst3ms.labgames.killthebase.enums.TeamType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyListener implements Listener {
    public static final World LOBBY_WORLD = Bukkit.getWorld("lobby");
    private static final TeamManager teamManager = new TeamManager(KillTheBase.getInstance().getTeamType());
    private static final Location LOBBY_LOCATION = new Location(LOBBY_WORLD, 0, 0, 0);
    private static final int MINIMUM_HEIGHT = 0;
    private static boolean isCountingDown;
    private static boolean firstJoin = true;
    private static ItemStack MENU_ITEM;
    private static List<ItemStack> items;

    static {
        ItemStack i = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName("Choisir son équipe");
        i.setItemMeta(meta);
        MENU_ITEM = i;
        items = Arrays.asList(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getWoolData()),
                              new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData()),
                              new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getWoolData()),
                              new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getWoolData())
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            e.setJoinMessage(e.getPlayer().getDisplayName() +
                             MessageManager.getMessageJoin().replace("LabGames.fr", "le lobby"));
            e.getPlayer().getInventory().setItem(4, MENU_ITEM);
            Bukkit.getScheduler().runTask(KillTheBase.getInstance(), () -> {
                Player p = e.getPlayer();
                p.teleport(LOBBY_LOCATION);
                BaseComponent[] title = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                                                                                                            "§5&lKillTheBase"
                ));
                p.sendTitle(new Title(title));
            });
            if (firstJoin) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(KillTheBase.getInstance(), () -> {
                    int playerAmount = LOBBY_WORLD.getPlayers().size();
                    TeamType teamType = KillTheBase.getInstance().getTeamType();
                    if (playerAmount >= teamType.getRequiredPlayerAmount()) {
                        startCountdown();
                    }
                }, 0L, 6000L);
                firstJoin = false;
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            Player p = e.getPlayer();
            if (p.getLocation().getBlockY() <= MINIMUM_HEIGHT) {
                p.teleport(LOBBY_LOCATION);
                p.sendMessage(ChatColor.GRAY + " Évitez de tomber, ça m'énerve de toujours devoir vous téléporter");
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            Player p = e.getPlayer();
            if (isCountingDown) {
                broadcastToWorld(LOBBY_WORLD, ""); // Message pour compte à rebours interrompu par X
                isCountingDown = false;
            }
            if (teamManager.getPlayerToTeamMap().containsKey(p)) {
                teamManager.removePlayer(p);
                // Message de quit
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            if (e.getAction() == Action.PHYSICAL && e.getItem().equals(MENU_ITEM)) {
                e.setCancelled(true);
                openMenu(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD)
            if (e.getItemDrop().equals(MENU_ITEM)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getLocation().getWorld() == LOBBY_WORLD) {
            if (e.getCurrentItem().getType() == Material.WOOL) {
                e.setCancelled(true);
                int index = (e.getSlot() - 1) / 2;
                TeamColor team = TeamColor.values()[index];
                teamManager.requestTeamJoin(team, (Player) e.getWhoClicked());
                ItemStack i = items.get(index);
                ItemMeta m = i.getItemMeta();
                TeamColor t = TeamColor.values()[index];
                List<String> displayedPlayers = new ArrayList<>(teamManager.getTeamToPlayerMap().get(t)).stream()
                                                                                                        .map(pl -> "  - " +
                                                                                                               pl.getName())
                                                                                                        .collect(Collectors.toList());
                m.setLore(displayedPlayers);
                i.setItemMeta(m);
                e.getClickedInventory().setItem(index, i);
            }
        }
    }

    private void startCountdown() {
        isCountingDown = true;
        broadcastToWorld(LOBBY_WORLD, MessageManager.getPrefix() + "La partie commencera dans 8 secondes !");
        Bukkit.getScheduler().scheduleSyncDelayedTask(KillTheBase.getInstance(), () -> {
            int i = 5;
            while (i > 0) {
                if (!isCountingDown) return;
                broadcastToWorld(LOBBY_WORLD, "Plus que " + i + " secondes avant le début de la partie !");
                i--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            GameListener.startGame(teamManager);
        }, 60L);
    }

    private void broadcastToWorld(World w, String message) {
        w.getPlayers().forEach(p -> p.sendMessage(message));
    }

    private void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, "Choix de l'équipe"); // OXOXOXOXO
        List<ItemMeta> metas = items.stream().map(ItemStack::getItemMeta).collect(Collectors.toList());
        metas.get(0).setDisplayName("Rejoindre l'équipe " + ChatColor.BLUE + "Bleue");
        metas.get(1).setDisplayName("Rejoindre l'équipe " + ChatColor.RED + "Rouge");
        metas.get(2).setDisplayName("Rejoindre l'équipe " + ChatColor.GREEN + "Rouge");
        metas.get(3).setDisplayName("Rejoindre l'équipe " + ChatColor.YELLOW + "Jaune");
        for (int i = 0; i < metas.size(); i++) {
            ItemMeta m = metas.get(i);
            TeamColor team = TeamColor.values()[i];
            List<String> displayedPlayers = new ArrayList<>(teamManager.getPlayers(team)).stream()
                                                                                                       .map(pl -> "  - " +
                                                                                                              pl.getName())
                                                                                                       .collect(Collectors.toList());
            m.setLore(displayedPlayers);
        }
        for (int i = 0; i < items.size(); i++) {
            ItemStack it = items.get(i);
            it.setItemMeta(metas.get(i));
            inv.setItem(i * 2 + 1, it);
        }
        p.openInventory(inv);
    }
}
