package fr.syst3ms.labgames.killthebase.listener;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import fr.labgames.api.API;
import fr.labgames.api.libs.TitleAPI.TitleAPI;
import fr.labgames.api.utils.boards.ScoreboardSign;
import fr.labgames.api.utils.bungeecord.BungeeCord;
import fr.labgames.api.utils.messages.MessageManager;
import fr.syst3ms.labgames.killthebase.KillTheBase;
import fr.syst3ms.labgames.killthebase.classes.TeamManager;
import fr.syst3ms.labgames.killthebase.enums.Team;
import fr.syst3ms.labgames.killthebase.enums.TeamType;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ARTHUR on 15/10/2017.
 */
public class GameListener implements Listener {
	public static final World GAME_WORLD = Bukkit.getWorld("ktb");
	public static final Map<Team, Location> SPAWN_LOCATIONS = new HashMap<>();
	public static final Location NPC_LOCATION = new Location(GAME_WORLD, 1, 37, 0);
	private static final GameListener instance = new GameListener();
	private static Multiset<Player> photons = HashMultiset.create();

	static {
		// Spawn locations
		SPAWN_LOCATIONS.put(Team.BLEU, new Location(GAME_WORLD, -27, 37, 0));
		SPAWN_LOCATIONS.put(Team.ROUGE, new Location(GAME_WORLD, 15, 37, 28));
		SPAWN_LOCATIONS.put(Team.VERT, new Location(GAME_WORLD, 30, 37, 0));
		SPAWN_LOCATIONS.put(Team.JAUNE, new Location(GAME_WORLD, 1, 37, 29));
		// Photons
		GAME_WORLD.getPlayers().forEach(p -> photons.add(p, 50));
	}

	private TeamManager teamManager;
	private Multiset<Team> teamKillCount = HashMultiset.create();
	private Multimap<Player, Location> placedBlockLocations = HashMultimap.create();
	private int timer = 0;
	private List<ScoreboardSign> scoreboards = new ArrayList<>();

	private GameListener() {
	}

	public static GameListener getInstance() {
		return instance;
	}

	public void startGame(TeamManager manager) {
		teamManager = manager;
		List<Player> players = LobbyListener.LOBBY_WORLD.getPlayers();
		createGameScoreboard();
		players.forEach(p -> {
			p.sendMessage(MessageManager.getMessageGameStart());
			p.teleport(SPAWN_LOCATIONS.get(teamManager.getTeam(p)));
			p.getInventory().setArmorContents(getEquipment(teamManager.getTeam(p)));
			spawnNpc();
		});

		Bukkit.getScheduler().scheduleSyncRepeatingTask(KillTheBase.getInstance(), () -> timer += 1, 0L, 20L);
	}

	private ItemStack[] getEquipment(Team team) {
		List<ItemStack> items = Arrays.asList(new ItemStack(Material.DIAMOND_HELMET),
			new ItemStack(Material.DIAMOND_CHESTPLATE),
			new ItemStack(Material.DIAMOND_LEGGINGS),
			new ItemStack(Material.DIAMOND_BOOTS));
		List<ItemMeta> metas = items.stream().map(ItemStack::getItemMeta).collect(Collectors.toList());
		int level = teamKillCount.count(team) / 5;
		metas.forEach(m -> {
			m.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, false);
			if (level > 0) {
				m.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, level, false);
			}
		});
		for (int i = 0; i < metas.size(); i++) {
			items.get(i).setItemMeta(metas.get(i));
		}
		return items.toArray(new ItemStack[0]);
	}

	private Villager getNpc() {
		Optional<Entity> opt = GAME_WORLD
			.getEntities()
			.stream()
			.filter(e -> e.getType() == EntityType.VILLAGER)
			.findFirst();
		assert opt.isPresent();
		return (Villager) opt.get();
	}

	private String getGameTimeText() {
		int minutes = timer / 60, seconds = timer % 60;
		return (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds);
	}

	private void spawnNpc() {
		Villager npc = (Villager) GAME_WORLD.spawnEntity(NPC_LOCATION, EntityType.VILLAGER);
		npc.setMaxHealth(150);
		npc.setHealth(150);
		npc.setCustomName(getHealthBarText(150));
		npc.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999_999, 10, false, false), true);
	}

	private void createGameScoreboard() {
		for (Player p : GAME_WORLD.getPlayers()) {
			ScoreboardSign sc = new ScoreboardSign(p,
				ChatColor.translateAlternateColorCodes('&',
					"&5KillTheBase &7- &e" + teamManager.getTeamType().getDisplayName()));
			sc.create();
			int i = 0;
			sc.setLine(i++, "--- Équipes ---");
			sc.setLine(i++,
				ChatColor.GOLD + "Les Bleus : " + ChatColor.BLUE + teamManager.getPlayers(Team.BLEU).size());
			sc.setLine(i++, "Les Rouges : " + ChatColor.RED + teamManager.getPlayers(Team.ROUGE).size());
			if (teamManager.getTeamType() != TeamType.SIX) {
				sc.setLine(i++, "Les Verts : " + ChatColor.GREEN + teamManager.getPlayers(Team.VERT).size());
				sc.setLine(i++, "Les Jaunes : " + ChatColor.YELLOW + teamManager.getPlayers(Team.JAUNE).size());
			}
			sc.setLine(i++, "--- Jeu ---");
			sc.setLine(i++, ChatColor.DARK_PURPLE + "PNJ : " + ChatColor.RED + ((getNpc().getHealth() + 1) / 2) + " ❤");
			sc.setLine(i, ChatColor.AQUA + "Temps de jeu : " + ChatColor.GREEN + getGameTimeText());
			scoreboards.add(sc);
		}
	}

	private String getHealthBarText(double health) {
		float h = (float) health;
		int displayHealth = h % 2 == 0 ? Math.round(h / 2) : Math.round((h + 1) / 2);
		return ChatColor.RED + Strings.repeat("\u25ae", displayHealth) + ChatColor.WHITE + Strings.repeat("\u25ae",
			75 - displayHealth);
	}

	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld() == GAME_WORLD && e.getAction() == Action.RIGHT_CLICK_BLOCK && (e
			.getClickedBlock()
			.getType() == Material.CHEST)) {
			Team team = teamManager.getTeam(p);
			if (e
				.getClickedBlock()
				.getLocation()
				.distanceSquared(SPAWN_LOCATIONS.get(team)) > 225) { // si la distance au carré > 15 au carré
				e.setUseInteractedBlock(Event.Result.DENY);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		Entity ent = e.getEntity();
		if (ent.getWorld() == GAME_WORLD && ent.getType() == EntityType.VILLAGER) {
			if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e
				.getDamager()
				.getType() != EntityType.PLAYER) {
				e.setCancelled(true);
				return;
			}
			LivingEntity lEnt = (LivingEntity) ent;
			lEnt.setCustomName(getHealthBarText(lEnt.getHealth() - e.getFinalDamage()));
			photons.add((Player) e.getDamager(), 1);
		}
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		if (entity.getWorld() == GAME_WORLD) {
			Player attacker = entity.getKiller();
			if (entity.getType() == EntityType.PLAYER && attacker != null) {
				photons.add(attacker, 5);
				Team team = teamManager.getTeam(attacker);
				teamKillCount.add(team);
			} else if (entity.getType() == EntityType.VILLAGER && attacker != null) {
				onWin(teamManager.getTeam(attacker));
			}
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld() == GAME_WORLD) {
			Team team = teamManager.getTeam(p);
			p.teleport(SPAWN_LOCATIONS.get(team));
			p.getInventory().setArmorContents(getEquipment(team));
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (e.getBlock().getWorld() == GAME_WORLD) {
			placedBlockLocations = Multimaps.filterValues(placedBlockLocations, l -> {
				assert l != null;
				return l.getBlock().getType() != Material.AIR;
			});
			placedBlockLocations.put(e.getPlayer(), e.getBlockPlaced().getLocation());
		}
	}

	@EventHandler
	public void onMultiPlace(BlockMultiPlaceEvent e) {
		if (e.getBlock().getWorld() == GAME_WORLD) {
			placedBlockLocations = Multimaps.filterValues(placedBlockLocations, l -> {
				assert l != null;
				return l.getBlock().getType() != Material.AIR;
			});
			placedBlockLocations.putAll(e.getPlayer(),
				e.getReplacedBlockStates().stream().map(BlockState::getLocation).collect(Collectors.toList()));
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getBlock().getWorld() == GAME_WORLD) {
			if (placedBlockLocations.containsValue(e.getBlock().getLocation())) {
				placedBlockLocations.remove(e.getPlayer(), e.getBlock().getLocation());
			} else {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onExplode(BlockExplodeEvent e) {
		if (e.getBlock().getWorld() == GAME_WORLD) {
			e.blockList().removeIf(b -> !placedBlockLocations.values().contains(b.getLocation()));
		}
	}

	private void onWin(Team winning) {
		for (Player p : GAME_WORLD.getPlayers()) {
			TitleAPI.sendTitle(p,
				2,
				30,
				2,
				ChatColor.GOLD + "Victoire" + ChatColor.GRAY + " de l'équipe " + winning.getColor() + winning.getFeminine() + ChatColor.GRAY + " !");
			p.sendMessage(MessageManager.getPrefix() + ChatColor.GOLD + "Victoire" + ChatColor.GRAY + " de l'équipe " + winning
				.getColor() + winning.getFeminine() + ChatColor.GRAY + " !");
			API.sql.addPhotons(p, photons.count(p));
		}
		teamManager.getPlayers(winning).forEach(p -> API.sql.addQuarks(p, 5));
		Bukkit.getScheduler().runTaskLater(KillTheBase.getInstance(), () -> {
			scoreboards.forEach(ScoreboardSign::destroy);
			for (Player p : GAME_WORLD.getPlayers()) {
				BungeeCord.sendPlayer(p, "lobby-1");
			}
			placedBlockLocations.values().forEach(l -> l.getBlock().setType(Material.AIR));
		}, 400L);
	}
}
