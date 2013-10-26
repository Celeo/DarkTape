package net.thedarktide.sifflion.darktape;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class DarkTape extends JavaPlugin
{

	private static final Logger Log = Logger.getLogger("Minecraft");
	private static String LogPrefix = "";

	private final DarkListener darkListener = new DarkListener(this);

	private final Map<Player, Long> informationCooldowns = new WeakHashMap<Player, Long>();

	private WorldGuard worldGuard = null;

	protected final Map<Player, Cooldown> bowCooldowns = new WeakHashMap<Player, Cooldown>();
	protected final Map<Player, Cooldown> foodCooldowns = new WeakHashMap<Player, Cooldown>();
	protected final Map<String, Cooldown> loginCooldowns = new HashMap<String, Cooldown>();

	// protected final MobClearer mobClearer = new MobClearer(this);

	@SuppressWarnings("static-method")
	public void Log(String message)
	{
		Log.info(String.format('[' + DarkTape.LogPrefix + "] " + message));
	}

	public static void Log(String message, Object... args)
	{
		Log.info(String.format('[' + DarkTape.LogPrefix + "] " + message, args));
	}

	public static void LogWarning(String message, Object... args)
	{
		Log.warning(String.format('[' + DarkTape.LogPrefix + "] " + message, args));
	}

	@Override
	public void onDisable()
	{
		this.getServer().getScheduler().cancelTasks(this);
		Log("Successfully disabled.");
	}

	@Override
	public void onEnable()
	{
		PluginManager pluginManager = getServer().getPluginManager();

		getServer().setSpawnRadius(1);

		DarkTape.LogPrefix = this.getDescription().getName();
		Config.Load(this);
		Config.loadData();
		this.darkListener.registerEvents();
		Plugin plugin = pluginManager.getPlugin("WorldGuard");
		if (plugin != null)
		{
			this.worldGuard = new WorldGuard(this, plugin);
			Log("WorldGuard detected!");
		}
		else
			DarkTape.LogWarning("WorldGuard not detected! Some functions may not work!");
		DarkTape.Log("Successfully enabled version %s.", this.getDescription().getVersion());
	}

	public void startMobClearer()
	{
		this.getServer().getScheduler().cancelTasks(this);
		// this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
		// mobClearer, 60L, 12000L);
		Log("Mob clearer enabled.");
	}

	public WorldGuard getWorldGuard()
	{
		return this.worldGuard;
	}

	@SuppressWarnings("static-method")
	public boolean hasPermission(Player player, String permission)
	{
		return player.isOp() || Permission.isAllowed(player, permission);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = null;

		if (commandLabel.equalsIgnoreCase("home"))
		{
			sender.sendMessage(ChatColor.DARK_RED
					+ "Home has been disabled. You can find warps in the mage tower though.");
			return true;
		}

		if (!(sender instanceof Player))
		{
			if (args != null && args.length > 1
					&& args[0].equalsIgnoreCase("chat"))
			{
				if (args.length > 1)
				{
					String message = "";
					for (int i = 1; i < args.length; i++)
					{
						if (message.equals(""))
							message = args[i];
						else
							message += " " + args[i];
					}
					message = message.replaceAll("c", "§");
					for (Player p : getServer().getOnlinePlayers())
					{
						p.sendMessage(message);
					}
					Log("Message sent: " + message.replaceAll("§", "§"));
				}
				Log("/darktape chat [message] | be in-game as a player for more commands");
				return true;
			}
		}
		player = (Player) sender;

		if (commandLabel.equalsIgnoreCase("darktape"))
		{
			if (args != null && args.length >= 1)
			{
				if (args[0].equalsIgnoreCase("reload"))
				{
					if (Permission.isAllowed(player, "darktape.reload"))
					{
						Config.loadData();
						player.sendMessage(ChatColor.GREEN
								+ "DarkTape configuration reloaded.");
					}
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else if (args[0].equalsIgnoreCase("flush"))
				{
					if (Permission.isAllowed(player, "darktape.flush"))
					{
						Config.Save();
						player.sendMessage(ChatColor.GREEN
								+ "DarkTape configuration saved to config.yml.");
					}
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else if (args[0].equalsIgnoreCase("xp"))
				{
					if (Permission.isAllowed(player, "darktape.getxp"))
						player.giveExp(2000);
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else if (args[0].equalsIgnoreCase("enderpearl"))
				{
					if (args.length >= 2)
					{
						if (Permission.isAllowed(player, "darktape.enderpearldisable.edit"))
						{
							if (!Config.blockedEnderpearlReigons.contains(args[1]))
								Config.blockedEnderpearlReigons.add(args[1]);
							else
								Config.blockedEnderpearlReigons.remove(args[1]);
							player.sendMessage(ChatColor.DARK_PURPLE + args[1]
									+ ChatColor.LIGHT_PURPLE
									+ " editted in the region list.");
						}
						else
							player.sendMessage(ChatColor.RED
									+ "You do not have permission for this command.");
					}
					else
					{
						if (Permission.isAllowed(player, "darktape.enderpearldisable.list"))
						{
							String message = "";
							for (String s : Config.blockedEnderpearlReigons)
								message += s + " ";
							player.sendMessage(ChatColor.DARK_PURPLE
									+ "Disabled regions:");
							player.sendMessage(ChatColor.LIGHT_PURPLE + message);
						}
						else
							player.sendMessage(ChatColor.RED
									+ "You do not have permission for this command.");
					}
				}
				else if (args[0].equalsIgnoreCase("mobclearer"))
				{
					if (Permission.isAllowed(player, "darktape.mobclearer"))
					{
						startMobClearer();
						player.sendMessage(ChatColor.GREEN
								+ "Started the mob clearer.");
					}
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else if (args[0].equalsIgnoreCase("chat"))
				{
					if (Permission.isAllowed(player, "darktape.chat"))
					{
						if (args.length > 1)
						{
							String message = "";
							for (int i = 1; i < args.length; i++)
							{
								if (message.equals(""))
									message = args[i];
								else
									message += " " + args[i];
							}
							message = message.replaceAll("&", "§");
							for (Player p : getServer().getOnlinePlayers())
							{
								p.sendMessage(message);
							}
						}
						else
							player.sendMessage(ChatColor.GRAY
									+ "/darktape chat [message]");
					}
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else if (args[0].equalsIgnoreCase("durability"))
				{
					if (Permission.isAllowed(player, "darktape.durability"))
					{
						if (args.length >= 2)
						{
							if (args[1].equalsIgnoreCase("list"))
							{
								String ids = "";
								for (Integer i : Config.shouldHaveDurability)
								{
									if (ids.equals(""))
										ids = i.toString();
									else
										ids += ", " + i.toString();
								}
								player.sendMessage("IDs: " + ids);
							}
							else if (args[1].equalsIgnoreCase("add"))
							{
								if (!Config.shouldHaveDurability.contains(Integer.valueOf(args[2])))
									Config.shouldHaveDurability.add(Integer.valueOf(args[2]));
								player.sendMessage(ChatColor.DARK_AQUA
										+ args[2] + ChatColor.GREEN
										+ " added to list");
							}
							else if (args[1].equalsIgnoreCase("remove"))
							{
								Config.shouldHaveDurability.remove(Integer.valueOf(args[2]));
								player.sendMessage(ChatColor.DARK_AQUA
										+ args[2] + ChatColor.GREEN
										+ " removed from list");
							}
						}
						else
							player.sendMessage(ChatColor.GRAY
									+ "/darktape durability [switch]");
					}
					else
						player.sendMessage(ChatColor.RED
								+ "You do not have permission for this command.");
				}
				else
					player.sendMessage(ChatColor.RED
							+ "Parameter not recognized.");
			}
			else
				player.sendMessage(ChatColor.RED + "Parameter not recognized.");
		}
		return true;
	}

	/**
	 * Sends a message to the player if the last message has passed the cooldown
	 */
	@SuppressWarnings("boxing")
	public void informPlayer(Player player, String message, double cooldown)
	{
		Long lastInformation = this.informationCooldowns.get(player);
		long currentTime = System.currentTimeMillis();

		if (lastInformation == null
				|| currentTime - lastInformation >= cooldown * 1000)
		{
			player.sendMessage(message);
			this.informationCooldowns.put(player, currentTime);
		}
	}

	public boolean enforceWorldGuard(Player player, Block block)
	{
		Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");

		try
		{
			if (plugin != null)
			{
				/*
				 * World guard is enabled.. let's boogie
				 */
				WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;

				/*
				 * Now get the region manager
				 */
				GlobalRegionManager regions = worldGuard.getGlobalRegionManager();
				RegionManager regionManager = regions.get(player.getWorld());

				/*
				 * We need to reflect into BukkitUtil.toVector
				 */
				Class<?> bukkitUtil = worldGuard.getClass().getClassLoader().loadClass("com.sk89q.worldguard.bukkit.BukkitUtil");
				Method toVector = bukkitUtil.getMethod("toVector", Block.class);
				Vector blockVector = (Vector) toVector.invoke(null, block);

				/*
				 * Now let's get the list of regions at the block we're clicking
				 */
				List<String> regionSet = regionManager.getApplicableRegionsIDs(blockVector);

				if (regionSet.isEmpty() || regions.canBuild(player, block))
					return false;
				player.sendMessage(ChatColor.RED.toString()
						+ "You cannot use that outside of acceptable WorldGuard regions");
				return true;

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

}