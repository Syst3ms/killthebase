package fr.syst3ms.labgames.killthebase.listener;

import fr.labgames.api.libs.TitleAPI.TitleAPI;
import fr.labgames.api.utils.boards.ScoreboardSign;
import fr.labgames.api.utils.messages.MessageManager;
import fr.syst3ms.labgames.killthebase.KillTheBase;
import fr.syst3ms.labgames.killthebase.classes.TeamManager;
import fr.syst3ms.labgames.killthebase.enums.Team;
import fr.syst3ms.labgames.killthebase.enums.TeamType;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyListener implements Listener {
    public static final TeamManager teamManager = new TeamManager(KillTheBase.getInstance().getTeamType());
    public static final int MINIMUM_HEIGHT = 0;
    public static World LOBBY_WORLD;
    public static Location LOBBY_LOCATION;
    public static ItemStack MENU_OPENER;
    public static List<ItemStack> MENU_ITEMS;

    static {
        if (Bukkit.getWorld("lobby") == null) {
            WorldCreator creator = new WorldCreator("lobby");
            LOBBY_WORLD = Bukkit.createWorld(creator);
        }
        LOBBY_LOCATION = new Location(LOBBY_WORLD, 66, 54, -1);
        ItemStack i = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName("Choisir son \u00e9quipe");
        i.setItemMeta(meta);
        MENU_OPENER = i;
        MENU_ITEMS = Arrays.asList(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getWoolData()),
                new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData()),
                new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getWoolData()),
                new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getWoolData()));
    }

    private boolean isCountingDown;
    private boolean firstJoin = true;
    private List<ScoreboardSign> scoreboards = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(
                e.getPlayer().getDisplayName() + MessageManager.getMessageJoin().replace("LabGames.fr", "le lobby"));
        e.getPlayer().getInventory().setItem(4, MENU_OPENER);
        Bukkit.getScheduler().runTask(KillTheBase.getInstance(), () -> {
            Player p = e.getPlayer();
            p.teleport(LOBBY_LOCATION);
            TitleAPI.sendTitle(p, 10, 40, 10, ChatColor.translateAlternateColorCodes('&', "&5&lKillTheBase"), "");
            createScoreboard(p);
            if (firstJoin) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(KillTheBase.getInstance(), () -> {
                    int playerAmount = teamManager.getTeamToPlayerMap().values().size();
                    TeamType teamType = KillTheBase.getInstance().getTeamType();
                    if (playerAmount >= teamType.getRequiredPlayerAmount()) {
                        startCountdown();
                    }
                }, 0L, 6000L);
                firstJoin = false;
            }
        });
    }

    private void createScoreboard(Player p) {
        ScoreboardSign sc = new ScoreboardSign(p,
                ChatColor.translateAlternateColorCodes('&', "&5KillTheBase &7- &e4x3"));
        sc.create();
        sc.setLine(0, "   ");
        sc.setLine(1, ChatColor.GREEN +
                      "Encore " +
                      ChatColor.GOLD +
                      (teamManager.getTeamType().getRequiredPlayerAmount() -
                       LOBBY_WORLD.getPlayers().size()) +
                      ChatColor.GREEN +
                      " joueur(s)");
        sc.setLine(3, "   ");
        sc.setLine(4, "§7Serveur: " + Bukkit.getServerName());
        sc.setLine(6, "§6play.labgames.fr");
        scoreboards.add(sc);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            Player p = e.getPlayer();
            if (p.getLocation().getBlockY() <= MINIMUM_HEIGHT) {
                p.teleport(LOBBY_LOCATION);
                p.sendMessage(ChatColor.GRAY +
                              " \u00c9vitez de tomber, \u00e7a m'\u00e9nerve de toujours devoir vous t\u00e9l\u00e9porter");
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
            if (e.getPlayer().getItemInHand().equals(MENU_OPENER)) {
                e.setCancelled(true);
                openMenu(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (e.getPlayer().getLocation().getWorld() == LOBBY_WORLD) {
            if (e.getItemDrop().equals(MENU_OPENER)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getLocation().getWorld() == LOBBY_WORLD) {
            if (e.getCurrentItem().getType() == Material.WOOL) {
                e.setCancelled(true);
                int index = (e.getSlot() - 1) / 2;
                Team team = Team.values()[index];
                teamManager.requestTeamJoin(team, (Player) e.getWhoClicked());
                ItemStack i = MENU_ITEMS.get(index);
                ItemMeta m = i.getItemMeta();
                Team t = Team.values()[index];
                List<String> displayedPlayers = new ArrayList<>(teamManager.getTeamToPlayerMap().get(t)).stream()
                        .map(pl -> "  - " + pl.getName()).collect(Collectors.toList());
                m.setLore(displayedPlayers);
                i.setItemMeta(m);
                e.getClickedInventory().setItem(e.getSlot(), i);
            }
        }
    }

    private void startCountdown() {
        isCountingDown = true;
        broadcastToWorld(LOBBY_WORLD, MessageManager.getPrefix() + "La partie commencera dans 8 secondes !");
        Bukkit.getScheduler().scheduleSyncDelayedTask(KillTheBase.getInstance(), () -> {
            int i = 5;
            while (i > 0) {
                if (!isCountingDown) {
                    return;
                }
                broadcastToWorld(LOBBY_WORLD, "Plus que " + i + " secondes avant le d\u00e9but de la partie !");
                i--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            scoreboards.forEach(ScoreboardSign::destroy);
            GameListener.startGame(teamManager);
        }, 60L);
    }

    private void broadcastToWorld(World w, String message) {
        w.getPlayers().forEach(p -> p.sendMessage(message));
    }

    private void openMenu(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, "Choix de l'\u00e9quipe"); // OXOXOXOXO
        List<ItemMeta> metas = MENU_ITEMS.stream().map(ItemStack::getItemMeta).collect(Collectors.toList());
        metas.get(0).setDisplayName("Rejoindre l'\u00e9quipe " + ChatColor.BLUE + "Bleue");
        metas.get(1).setDisplayName("Rejoindre l'\u00e9quipe " + ChatColor.RED + "Rouge");
        metas.get(2).setDisplayName("Rejoindre l'\u00e9quipe " + ChatColor.GREEN + "Rouge");
        metas.get(3).setDisplayName("Rejoindre l'\u00e9quipe " + ChatColor.YELLOW + "Jaune");
        for (int i = 0; i < metas.size(); i++) {
            ItemMeta m = metas.get(i);
            Team team = Team.values()[i];
            List<String> displayedPlayers = new ArrayList<>(teamManager.getPlayers(team)).stream()
                    .map(pl -> "  - " + pl.getName()).collect(Collectors.toList());
            m.setLore(displayedPlayers);
        }
        for (int i = 0; i < MENU_ITEMS.size(); i++) {
            ItemStack it = MENU_ITEMS.get(i);
            it.setItemMeta(metas.get(i));
            inv.setItem(i * 2 + 1, it);
        }
        p.openInventory(inv);
    }
}
