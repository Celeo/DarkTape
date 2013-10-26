package net.thedarktide.sifflion.darktape;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Leaves;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

@SuppressWarnings("boxing")
public class DarkListener implements Listener
{

	private HashMap<Player, Integer> healingTask = new HashMap<Player, Integer>();
	private Map<String, Long> lastLog = new HashMap<String, Long>();
	List<Integer> halfBlocks = Arrays.asList(26, 34, 44, 53, 67, 81, 88, 108, 109, 114, 116, 118, 120, 126, 127, 128, 134, 135, 136);
	List<Integer> ignoreBlocks = Arrays.asList(8, 9, 10, 11, 12, 13, 63, 64, 68, 70, 71, 72, 85, 92, 96, 113, 101, 102, 107, 117, 118, 122, 139);

	private final DarkTape plugin;

	public DarkListener(DarkTape plugin)
	{
		this.plugin = plugin;
	}

	public void registerEvents()
	{
		PluginManager pluginManager = this.plugin.getServer().getPluginManager();
		pluginManager.registerEvents(this, plugin);
	}

	/**
	 * Extra barrier against those freaking villagers because screw villagers
	 * 
	 * @param event
	 *            - CreatureSpawnEvent
	 */
	@EventHandler
	public static void onVillagerSpawn(CreatureSpawnEvent event)
	{
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.VILLAGER)
				|| entity instanceof Villager)
			event.setCancelled(true);
		if ((entity.getType().equals(EntityType.WOLF) || entity instanceof Wolf)
				&& event.getSpawnReason().equals(SpawnReason.BREEDING))
			event.setCancelled(true);
	}

	/**
	 * Warps players back to DTC if they got past the correct border<br>
	 * <br>
	 * Displacement check of 2010 to let BorderGuard do it's thing
	 * 
	 * @param event
	 *            - PlayerMoveEvent event
	 */
	@EventHandler
	public static void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Location location = player.getLocation();
		double x = location.getX();
		double z = location.getZ();
		if (x > 2010 || x < -2010 || z > 2010 || z < -2010)
		{
			player.sendMessage("§cYou're not supposed to be there.");
			player.teleport(new Location(player.getWorld(), -7, 70, -2));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBucketEmpty(PlayerBucketEmptyEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getBlockClicked();
		if (block == null || player == null)
			return;
		if (!plugin.getWorldGuard().canBuild(player, block))
		{
			player.sendMessage("§4You cannot use that there.");
			event.setCancelled(true);
		}
		if (player.getLocation().getY() > 80)
		{
			player.sendMessage("§4You cannot use that there.");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event)
	{

		if (this.plugin.getWorldGuard() == null)
			return;

		Block blockPlaced = event.getBlock();
		Player player = event.getPlayer();

		// Portal placement permissions check
		if (blockPlaced.getTypeId() == 90)
		{
			if (!Permission.isAllowed(player, "darktide.portal.place"))
			{
				event.setCancelled(true);
				player.sendMessage(ChatColor.DARK_RED
						+ "You are not allowed to place this.");
				return;
			}
			plugin.Log(player.getName() + " placed a portal.");
		}
		// Ice placement outside regions check
		if (blockPlaced.getTypeId() == 79)
		{
			if (!Permission.isAllowed(player, "darktide.ice.place"))
			{
				if (this.plugin.getWorldGuard().getRegionSet(blockPlaced) == null)
				{
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED
							+ "You cannot use that outside of acceptable WorldGuard regions");
					return;
				}
			}
		}

		// Prevent wall climbing using blocks
		// final BlockPlaceEvent tpNext = null;
		// if(player.getGameMode() == GameMode.CREATIVE)
		// return;
		//
		// if(!event.isCancelled())
		// return;
		//
		// plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new
		// Runnable() {
		// @Override
		// public void run() {
		// int i = 0;
		// @SuppressWarnings("null")
		// Player player = tpNext.getPlayer();
		// for (int c = 0; c < 300; c++){
		// if (player.getLocation().add(0, -i, 0).getBlock().getType() ==
		// Material.AIR)
		// i++;
		// else
		// break;
		// }
		// if (i == 1)
		// return;
		// tpNext.getPlayer().teleport(player.getLocation().add(0, -i + 1, 0));
		// plugin.Log(player.getName() +
		// " has been teleported to the ground due to block placement.");
		// }
		// }, 1L);
	}

	/**
	 * No more unlimited apples from WorldGuard-protected leaves
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block == null || player == null)
			return;
		if (!plugin.getWorldGuard().canBuild(player, block))
		{
			if (block.getType().equals(Material.LEAVES)
					|| block.getTypeId() == 18 || block instanceof Leaves)
			{
				Material mat = block.getType();
				block.setType(Material.AIR);
				event.setCancelled(true);
				block.setType(mat);
			}
		}
	}

	/**
	 * Prevents players from holding an illegal potion.
	 */
	@EventHandler
	public static void onPlayerItemHeld(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		if (player.getItemInHand().getType() != Material.POTION)
			return;

		switch (player.getItemInHand().getDurability())
		{

		// Fire Resistance
		case 8195:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8227:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8259:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16387:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16419:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16451:
			player.setItemInHand(new ItemStack(0));
			break;

		// Poison
		case 8196:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8228:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8260:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16388:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16420:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16452:
			player.setItemInHand(new ItemStack(0));
			break;

		// Harming/Instant Damage
		case 8204:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8236:
			player.setItemInHand(new ItemStack(0));
			break;
		case 8268:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16396:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16428:
			player.setItemInHand(new ItemStack(0));
			break;
		case 16460:
			player.setItemInHand(new ItemStack(0));
			break;

		default:
			return;
		}
	}

	/**
	 * Checks if a login cooldown for a player that is logging in is active and
	 * prevents the login if that is the case
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		Cooldown cooldown;
		String name = event.getPlayer().getName();
		long currentTime;

		if (event.getResult() != Result.ALLOWED)
		{
			return;
		}

		cooldown = this.plugin.loginCooldowns.get(name);
		if (cooldown != null)
		{
			currentTime = System.currentTimeMillis();
			if (cooldown.isActive(currentTime))
			{
				event.disallow(Result.KICK_OTHER, Config.LoginCooldownMessage.replace("%s", Integer.toString((int) Math.ceil((cooldown.getTimeLeft(currentTime) - 0.5) / 1000))));
			}
		}
	}

	@SuppressWarnings("static-method")
	@EventHandler
	public void onPrepareItemEnchant(PrepareItemEnchantEvent event)
	{
		ItemStack item = event.getItem();
		switch (item.getTypeId())
		{

		// Bow
		case 261:
			event.setCancelled(true);
			break;

		// Helmets
		case 298:
			event.setCancelled(true);
			break;
		case 302:
			event.setCancelled(true);
			break;
		case 306:
			event.setCancelled(true);
			break;
		case 310:
			event.setCancelled(true);
			break;
		case 314:
			event.setCancelled(true);
			break;
		// Chestplates
		case 299:
			event.setCancelled(true);
			break;
		case 303:
			event.setCancelled(true);
			break;
		case 307:
			event.setCancelled(true);
			break;
		case 311:
			event.setCancelled(true);
			break;
		case 315:
			event.setCancelled(true);
			break;
		// Pants
		case 300:
			event.setCancelled(true);
			break;
		case 304:
			event.setCancelled(true);
			break;
		case 308:
			event.setCancelled(true);
			break;
		case 312:
			event.setCancelled(true);
			break;
		case 316:
			event.setCancelled(true);
			break;
		// Boots
		case 301:
			event.setCancelled(true);
			break;
		case 305:
			event.setCancelled(true);
			break;
		case 309:
			event.setCancelled(true);
			break;
		case 313:
			event.setCancelled(true);
			break;
		case 317:
			event.setCancelled(true);
			break;
		// Swords
		case 267:
			event.setCancelled(true);
			break;
		case 268:
			event.setCancelled(true);
			break;
		case 272:
			event.setCancelled(true);
			break;
		case 276:
			event.setCancelled(true);
			break;
		case 283:
			event.setCancelled(true);
			break;
		// Anything Else
		default:
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		String name = player.getName();
		if (shouldDenyMessage(name))
			event.setJoinMessage(null);
		lastLog.put(name, Long.valueOf(System.currentTimeMillis()));
		if (player.getGameMode().equals(GameMode.CREATIVE))
			if (!Permission.isAllowed(player, "sudo.admin"))
				player.setGameMode(GameMode.SURVIVAL);
	}

	/**
	 * Adds a login cooldown to the leaving player
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event)
	{
		String name = event.getPlayer().getName();
		if (!this.plugin.hasPermission(event.getPlayer(), Config.AlwaysLoginPermission))
		{
			Cooldown cooldown = new Cooldown(System.currentTimeMillis(), Config.LoginCooldown * 1000);
			this.plugin.loginCooldowns.put(name, cooldown);
		}
		Integer timer = healingTask.get(event.getPlayer());
		if (timer != null)
			this.plugin.getServer().getScheduler().cancelTask(timer.intValue());
		if (shouldDenyMessage(name))
			event.setLeaveMessage(null);
		lastLog.put(name, Long.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Adds a login cooldown to the leaving player
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		String name = event.getPlayer().getName();
		if (!this.plugin.hasPermission(event.getPlayer(), Config.AlwaysLoginPermission))
		{
			Cooldown cooldown = new Cooldown(System.currentTimeMillis(), Config.LoginCooldown * 1000);
			this.plugin.loginCooldowns.put(name, cooldown);
		}
		Integer timer = healingTask.get(event.getPlayer());
		if (timer != null)
			this.plugin.getServer().getScheduler().cancelTask(timer.intValue());
		if (shouldDenyMessage(name))
			event.setQuitMessage(null);
		lastLog.put(name, Long.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Determines whether or not the message displayed for the player logging in
	 * or out of the game should be suppressed.
	 * 
	 * @param name
	 *            of the player
	 * @return <b>True</b> if the message should be suppressed, <b>False</b>
	 *         otherwise.
	 */
	public boolean shouldDenyMessage(String name)
	{
		if (!Config.shouldDelayRelogMessages)
			return false;
		return (lastLog.get(name) != null && lastLog.get(name)
				+ Config.relogDelay >= System.currentTimeMillis());
	}

	/**
	 * Cancels commands that are specified as blocked inside the config if the
	 * player does not have the corresponding permission node
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		String fullCommand = event.getMessage();
		String command = event.getMessage();
		Player player = event.getPlayer();
		int commandEnd;
		if (event.isCancelled())
			return;
		if (fullCommand.toLowerCase().startsWith("/dtec")
				&& !fullCommand.toLowerCase().startsWith("/dtec -p")
				&& !fullCommand.toLowerCase().startsWith("/dtec p"))
		{
			if (!this.plugin.hasPermission(player, "dtec.admin"))
			{
				player.sendMessage(ChatColor.RED
						+ "You cannot use this command.");
				event.setCancelled(true);
				return;
			}
		}

		if (fullCommand.toLowerCase().startsWith("/kill")
				&& !plugin.hasPermission(player, "sudo.mod"))
		{
			player.sendMessage(ChatColor.RED + "You cannot use this command.");
			event.setCancelled(true);
			return;
		}

		if (fullCommand.toLowerCase().startsWith("/help"))
		{
			String helpMessage = plugin.getConfig().getString("Commands.Help.Message", "INSERT TEXT");
			if (!helpMessage.equals("INSERT TEXT"))
			{
				for (String s : helpMessage.split("\n"))
				{
					player.sendMessage(ChatColor.BLUE + s);
				}
			}
			else
			{
				helpMessage = "If you have a question:\n"
						+ "If you have not done the tutorial, do it.\n"
						+ "If you have done it but still have a question,\n"
						+ " then ask in chat. We have to people answer your questions.";
				plugin.getConfig().set("Commands.Help.Message", helpMessage);
				plugin.saveConfig();
				for (String s : helpMessage.split("\n"))
				{
					player.sendMessage(ChatColor.BLUE + s);
				}
			}
			event.setCancelled(true);
			return;
		}

		if (this.plugin.hasPermission(event.getPlayer(), Config.CommandAccessPermission)
				|| command.charAt(0) != '/')
		{
			return;
		}

		commandEnd = command.indexOf(' ');
		command = (commandEnd < 0) ? command.substring(1) : command.substring(1, commandEnd);

		/* FULLY BLOCKED COMMANDS */
		for (String blockedCommand : Config.BlockedCommands)
		{
			if (command.matches(blockedCommand))
			{
				event.getPlayer().sendMessage(Config.BlockedCommandMessage);
				event.setCancelled(true);
				return;
			}
		}
	}

	/**
	 * Disabling ender-pearl teleporting in the places where the usage of the
	 * teleport goes against existing mechanics.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (event.getCause().equals(TeleportCause.ENDER_PEARL))
		{
			// block all teleports in the Nether due to border guard issues
			if (event.getPlayer().getWorld().getName().toLowerCase().contains("nether"))
			{
				event.getPlayer().sendMessage(ChatColor.RED
						+ "That cannot be used here.");
				event.setCancelled(true);
				return;
			}
			// block teleports in dungeons
			if (Config.blockedEnderpearlReigons == null
					|| Config.blockedEnderpearlReigons.isEmpty())
				return;
			Set<String> fromList = plugin.getWorldGuard().getRegionSet(player.getWorld().getBlockAt(event.getFrom()));
			Set<String> toList = plugin.getWorldGuard().getRegionSet(player.getWorld().getBlockAt(event.getTo()));
			for (String region : Config.blockedEnderpearlReigons)
			{
				if (fromList.contains(region) || toList.contains(region))
				{
					event.getPlayer().sendMessage(ChatColor.RED
							+ "That cannot be used here.");
					event.setCancelled(true);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		try
		{
			Block clickedBlock = event.getClickedBlock();
			Block blockPlacedRel = event.getClickedBlock().getRelative(event.getBlockFace());
			Player player = event.getPlayer();
			World world = player.getWorld();
			ItemStack itemInHand = player.getItemInHand();
			if (clickedBlock.equals(Material.BED))
			{
				if (this.plugin.getWorldGuard().getRegionList(world, clickedBlock) == null
						|| this.plugin.getWorldGuard().getRegionList(world, clickedBlock).isEmpty()
						|| this.plugin.enforceWorldGuard(player, clickedBlock))
				{
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED
							+ "You cannot bind your home to this bed.");
					return;
				}
			}
			if (itemInHand.getType().equals(Material.BOAT))
			{
				if (this.plugin.getWorldGuard().getRegionList(world, blockPlacedRel) == null)
					return;
				else if (this.plugin.enforceWorldGuard(player, blockPlacedRel))
				{
					player.sendMessage(ChatColor.RED
							+ "You cannot use that outside of acceptable WorldGuard regions");
					event.setCancelled(true);
					return;
				}
			}
			if (Config.BlockFireBreaking
					&& event.getBlockFace() == BlockFace.UP)
			{
				if (clickedBlock.getRelative(BlockFace.UP).getType() == Material.FIRE
						&& (this.plugin.getWorldGuard() == null || !this.plugin.getWorldGuard().canBuild(event.getPlayer(), clickedBlock)))
				{
					event.setCancelled(true);
					return;
				}
			}
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK
					&& event.getClickedBlock().getType() == Material.BREWING_STAND)
				player.sendMessage(ChatColor.YELLOW
						+ "Please note: Potions of Fire Resistance, Instant Damage, and Poision are blocked.");
		}
		catch (NullPointerException e)
		{}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		final Player player = event.getPlayer();
		long runTime = Config.SecondsPerHeal * 20;
		healingTask.put(player, this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
		{
			@Override
			public void run()
			{
				int endHealth = player.getHealth() + Config.HealAmount;
				if (endHealth > 20)
					endHealth = 20;
				player.setHealth(endHealth);
			}
		}, runTime));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBedLeave(PlayerBedLeaveEvent event)
	{
		Integer timer = healingTask.get(event.getPlayer());
		if (timer != null)
			this.plugin.getServer().getScheduler().cancelTask(timer.intValue());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (!(event instanceof EntityDamageByEntityEvent))
		{
			if (!(event.getEntity() instanceof Player))
				return;
			Player p = (Player) event.getEntity();
			if (plugin.getWorldGuard().getRegionSet(p.getWorld().getBlockAt(p.getLocation()).getRelative(0, -1, 0)).contains("darktidecitadel"))
				if (event.getCause().equals(DamageCause.FIRE))
					event.setCancelled(true);
			return;
		}
		EntityDamageByEntityEvent eveEvent = (EntityDamageByEntityEvent) event;
		if (event.getEntity() instanceof Player)
			return;
		if (!(eveEvent.getDamager() instanceof Player))
			return;
		Player damager = (Player) eveEvent.getDamager();
		if (damager.getItemInHand().containsEnchantment(Enchantment.DAMAGE_ALL))
		{
			ItemStack temp = damager.getItemInHand();
			temp.removeEnchantment(Enchantment.DAMAGE_ALL);
			plugin.Log(damager.getName()
					+ "'s sharpness sword just got nerfed!");
		}
	}

	@EventHandler
	public static void onDeathDropEnchantedItems(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
			return;
		List<ItemStack> drops = event.getDrops();
		for (ItemStack i : drops)
		{
			if (i == null)
				continue;
			if (i.getEnchantments() == null || i.getEnchantments().isEmpty())
			{
				for (Enchantment e : i.getEnchantments().keySet())
				{
					i.removeEnchantment(e);
				}
			}
		}
	}

	/**
	 * Cancels the piston extension if it or a block is moving to a location
	 * which does have regions but not the exact same or if WorldGuard is
	 * disabled
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		Set<String> movingRegions, pistonRegions;

		if (event.isCancelled() || !Config.BlockPistonCrossing)
		{
			return;
		}

		if (this.plugin.getWorldGuard() == null)
		{
			event.setCancelled(true);
			return;
		}

		pistonRegions = this.plugin.getWorldGuard().getRegionSet(event.getBlock());
		for (int i = 0; i <= event.getLength(); i++)
		{
			Block block = event.getBlock().getRelative(event.getDirection(), i + 1);
			movingRegions = this.plugin.getWorldGuard().getRegionSet(block);

			if (!movingRegions.isEmpty()
					&& !pistonRegions.equals(movingRegions))
			{
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Cancels the retraction if the piston is sticky, the retracting block is
	 * really retracting, and the retracting block is is in either the same
	 * regions or in no region or if WorldGuard is disabled
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		Block movingBlock;
		Set<String> movingRegions, pistonRegions;

		if (!event.isSticky() || event.isCancelled()
				|| !Config.BlockPistonCrossing)
		{
			return;
		}

		movingBlock = event.getRetractLocation().getBlock();
		if (movingBlock == null
				|| movingBlock.getType() == Material.AIR
				|| movingBlock.getPistonMoveReaction() == PistonMoveReaction.BLOCK)
		{
			return;
		}

		if (this.plugin.getWorldGuard() == null)
		{
			event.setCancelled(true);
			return;
		}

		movingRegions = this.plugin.getWorldGuard().getRegionSet(movingBlock);
		pistonRegions = this.plugin.getWorldGuard().getRegionSet(event.getBlock());
		if (!movingRegions.isEmpty() && !pistonRegions.equals(movingRegions)
				&& !Config.RegionCrossingExceptions.containsAll(movingRegions))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Cancels BlockFromTo if the ToBlock is in a region but not in the same
	 * regions as the FromBlock
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockFromTo(BlockFromToEvent event)
	{
		Set<String> fromRegions, toRegions;
		if (event.isCancelled() || !Config.BlockFromToCrossing)
			return;

		if (this.plugin.getWorldGuard() == null)
		{
			event.setCancelled(true);
			return;
		}

		fromRegions = this.plugin.getWorldGuard().getRegionSet(event.getBlock());
		toRegions = this.plugin.getWorldGuard().getRegionSet(event.getToBlock());
		if (!toRegions.isEmpty() && !fromRegions.equals(toRegions)
				&& !Config.RegionCrossingExceptions.containsAll(toRegions))
			event.setCancelled(true);
	}

	@EventHandler
	public static void onGlitchedItemPickup(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		Item item = event.getItem();
		if (item.getItemStack().getDurability() != 0
				&& !shouldHaveDurability(item))
		{
			event.setCancelled(true);
			item.remove();
			player.getInventory().addItem(new ItemStack(item.getItemStack().getType(), item.getItemStack().getAmount()));
		}
	}

	public static boolean shouldHaveDurability(Item item)
	{
		return Config.shouldHaveDurability.contains(Integer.valueOf(item.getItemStack().getTypeId()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onRespawn(PlayerRespawnEvent evt)
	{
		if (evt.isBedSpawn())
		{
			Location loc = evt.getPlayer().getBedSpawnLocation();
			Block b = loc.getBlock();
			Location newLoc = null;

			// Are any of the 8 blocks that the player normally spawns in
			// occupied?
			// Player WILL spawn upwards if any of these blocks are obstructed
			if (!b.getRelative(0, 1, 0).isEmpty()
					|| !b.getRelative(0, 2, 0).isEmpty()
					|| !b.getRelative(0, 1, -1).isEmpty()
					|| !b.getRelative(0, 2, -1).isEmpty()
					|| !b.getRelative(-1, 1, -1).isEmpty()
					|| !b.getRelative(-1, 2, -1).isEmpty()
					|| !b.getRelative(-1, 1, 0).isEmpty()
					|| !b.getRelative(-1, 2, 0).isEmpty())
			{
				// Are the 2 blocks straight above the bed air?
				if (b.getRelative(0, 1, 0).isEmpty()
						&& b.getRelative(0, 2, 0).isEmpty())
				{
					newLoc = b.getRelative(0, 1, 0).getLocation();
				}
				else
				// How does middle north look?
				if (b.getRelative(0, 1, -1).isEmpty()
				// Are either of the blocks above or below air?
						&& (b.getRelative(0, 0, -1).isEmpty() || b.getRelative(0, 2, -1).isEmpty()))
				{
					newLoc = b.getRelative(0, b.getRelative(0, 0, -1).isEmpty() ? 0 : 1, -1).getLocation();
				}
				else
				// How does middle west look?
				if (b.getRelative(-1, 1, 0).isEmpty()
				// Are either of the blocks above or below air?
						&& (b.getRelative(-1, 0, 0).isEmpty() || b.getRelative(-1, 2, 0).isEmpty()))
				{
					newLoc = b.getRelative(-1, b.getRelative(-1, 0, 0).isEmpty() ? 0 : 1, 0).getLocation();
				}
				else
				// How does middle south look?
				if (b.getRelative(0, 1, 1).isEmpty()
				// Are either of the blocks above or below air?
						&& (b.getRelative(0, 0, 1).isEmpty() || b.getRelative(0, 2, 1).isEmpty()))
				{
					newLoc = b.getRelative(0, b.getRelative(0, 0, 1).isEmpty() ? 0 : 1, 1).getLocation();
				}
				else
				// How does middle east look?
				if (b.getRelative(1, 1, 0).isEmpty()
				// Are either of the blocks above or below air?
						&& (b.getRelative(1, 0, 0).isEmpty() || b.getRelative(1, 2, 0).isEmpty()))
				{
					newLoc = b.getRelative(1, b.getRelative(1, 0, 0).isEmpty() ? 0 : 1, 0).getLocation();
				}
				else
				{
					// No suitable spawn location found, player WILL glitch up
					evt.getPlayer().sendMessage("§cYour bed is obstructed!");
					newLoc = loc.getWorld().getSpawnLocation();
				}
			}

			if (newLoc != null)
			{
				// move the player if the spawn is invalid
				evt.setRespawnLocation(newLoc.add(.5, 0, .5));
			}
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		if (!(event.getEntered() instanceof Player))
			return;
		Player player = (Player) event.getEntered();
		if (!plugin.getWorldGuard().canBuild(player, event.getVehicle().getLocation()))
		{
			player.sendMessage("§cYou cannot enter that vehicle as you cannot build there.");
			event.setCancelled(true);
		}
	}

	// @EventHandler
	public void onPlayerMoveThroughBlock(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Vector v = player.getVelocity();
		Location l = player.getLocation().clone();
		Block b = l.getBlock();
		Block bu = b.getRelative(BlockFace.UP);

		// there is a non-air, non-ignored block at player's feet.
		if (!b.isEmpty()
				&& !ignoreBlocks.contains(Integer.valueOf(b.getTypeId()))
				// if a block at the player's head level is empty and not
				// ignored, continue. Otherwise player is being suffocated in a
				// trap.
				&& (bu.isEmpty() || ignoreBlocks.contains(Integer.valueOf(bu.getTypeId()))))
		{
			// net
			// TODO: Not working V
			Boolean solid = net.minecraft.server.v1_4_R1.Block.byId[b.getTypeId()].material.isSolid();
			double offset = halfBlocks.contains(Integer.valueOf(b.getTypeId())) ? 0.5 : 1;
			double pos = l.getY() - l.getBlockY();

			// if you're sinking into a solid block
			if (v.getY() < 0 && solid && pos < offset && pos > 0.201)
			{
				v.setY(0);
				l.setY(Math.floor(l.getY() + offset + .05));
				player.teleport(l);
				player.setVelocity(v);
			}
		}
	}
}