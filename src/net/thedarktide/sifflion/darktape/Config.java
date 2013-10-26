package net.thedarktide.sifflion.darktape;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * @author Xolsom
 */

public class Config
{

	public static int ArrowDamage = 6;
	public static boolean ArrowDamageOnlyPlayer = true;
	public static int BowCooldown = 5;
	public static int CombatDuration = 5;
	public static boolean CombatResetFoodCooldown = true;
	public static int LoginCooldown = 5;
	public static int VehicleDespawnTime = 100;

	public static int HealAmount = 1;
	public static int SecondsPerHeal = 1;

	public static String AlwaysLoginPermission = "darktide.admin.alwayslogin";
	public static String BalanceCommandPermission = "darktide.simplethings.balancecommands";
	public static String CommandAccessPermission = "darktide.admin.blockedcommands";

	public static boolean BlockFireBreaking = true;
	public static boolean BlockPistonCrossing = true;
	public static boolean BlockFromToCrossing = true;

	public static double FoodMessageCooldown = 0.5;
	public static double BowMessageCooldown = 1;

	public static boolean shouldDelayRelogMessages = true;
	public static int relogDelay = 5;

	public static String BalanceBlockedCommandMessage = "§cYou don't have enough coins to use this command.";
	public static String BlockedCommandMessage = "§7This command is not available.";
	public static String BlockedFoodMessage = "§cYou cannot eat while in combat!";
	public static String BowCooldownMessage = "§7Your bow is too strained. You can shoot again in §6%s§7 seconds.";
	public static String CombatEnterMessage = "§7You just entered combat.";
	public static String CombatFadeMessage = "§7You are no longer in combat.";
	public static String FoodCooldownMessage = "§7You are eating too fast. Try again in §6%s§7 seconds.";
	public static String LoginCooldownMessage = "You can login again in %s seconds.";

	public static List<String> BlockedCommands = new ArrayList<String>();
	public static List<String> PositiveBalanceCommands = new ArrayList<String>();
	public static List<String> RegionCrossingExceptions = new ArrayList<String>();

	public static Set<Material> BlockedFood = EnumSet.noneOf(Material.class);
	public static Map<Material, Integer> CombatCooldownFood = new EnumMap<Material, Integer>(Material.class);
	public static Integer[] blockedEnchantments;
	public static List<String> blockedEnderpearlReigons = new ArrayList<String>();

	public static String mobClearer_region = "darktidecitadel";
	@SuppressWarnings("boxing")
	public static Long mobClearer_repeatTimer = 12000L;
	public static List<String> mobClearer_exceptions = new ArrayList<String>();
	public static List<Integer> shouldHaveDurability = new ArrayList<Integer>();

	public static DarkTape plugin;

	/**
	 * Load the data
	 */
	@SuppressWarnings({ "boxing", "rawtypes" })
	public static void loadData()
	{
		blockedEnderpearlReigons = plugin.getConfig().getStringList("options.enderpearl.blockedRegions");
		ArrowDamage = plugin.getConfig().getInt("options.arrowDamage", ArrowDamage);
		ArrowDamageOnlyPlayer = plugin.getConfig().getBoolean("options.arrowDamageOnlyPlayer", ArrowDamageOnlyPlayer);
		BowCooldown = plugin.getConfig().getInt("options.bowCooldown", BowCooldown);
		CombatDuration = plugin.getConfig().getInt("options.combatDuration", CombatDuration);
		CombatResetFoodCooldown = plugin.getConfig().getBoolean("options.combatResetFoodCooldown", CombatResetFoodCooldown);
		LoginCooldown = plugin.getConfig().getInt("options.log.loginCooldown", LoginCooldown);
		VehicleDespawnTime = plugin.getConfig().getInt("options.vehicleDespawnTime", VehicleDespawnTime);

		HealAmount = plugin.getConfig().getInt("options.bedHealing.HealAmount", HealAmount);
		SecondsPerHeal = plugin.getConfig().getInt("options.bedHealing.SecondsPerHeal", SecondsPerHeal);

		AlwaysLoginPermission = plugin.getConfig().getString("options.alwaysLoginPermission", AlwaysLoginPermission);
		BalanceCommandPermission = plugin.getConfig().getString("options.balanceCommandPermission", BalanceCommandPermission);
		CommandAccessPermission = plugin.getConfig().getString("options.commandAccessPermission", CommandAccessPermission);

		BlockFireBreaking = plugin.getConfig().getBoolean("options.blockFireBreaking", BlockFireBreaking);
		BlockPistonCrossing = plugin.getConfig().getBoolean("options.blockPistonCrossing", BlockPistonCrossing);
		BlockFromToCrossing = plugin.getConfig().getBoolean("options.blockFromToCrossing", BlockFromToCrossing);

		FoodMessageCooldown = plugin.getConfig().getDouble("options.foodMessageCooldown", FoodMessageCooldown);
		BowMessageCooldown = plugin.getConfig().getDouble("options.bowMessageCooldown", BowMessageCooldown);

		BalanceBlockedCommandMessage = plugin.getConfig().getString("options.balanceBlockedCommandMessage", BalanceBlockedCommandMessage);
		BlockedCommandMessage = plugin.getConfig().getString("options.blockedCommandMessage", BlockedCommandMessage);
		BlockedFoodMessage = plugin.getConfig().getString("options.blockedFoodMessage", BlockedFoodMessage);
		BowCooldownMessage = plugin.getConfig().getString("options.bowCooldownMessage", BowCooldownMessage);
		CombatEnterMessage = plugin.getConfig().getString("options.combatEnterMessage", CombatEnterMessage);
		CombatFadeMessage = plugin.getConfig().getString("options.combatFadeMessage", CombatFadeMessage);
		FoodCooldownMessage = plugin.getConfig().getString("options.foodCooldownMessage", FoodCooldownMessage);

		LoginCooldownMessage = plugin.getConfig().getString("options.loginCooldownMessage", LoginCooldownMessage);
		shouldDelayRelogMessages = plugin.getConfig().getBoolean("options.log.shouldDelayRelogMessages", shouldDelayRelogMessages);
		relogDelay = plugin.getConfig().getInt("options.log.relogDelay", relogDelay);

		BlockedCommands = plugin.getConfig().getStringList("blockedCommands");
		PositiveBalanceCommands = plugin.getConfig().getStringList("positiveBalanceCommands");
		RegionCrossingExceptions = plugin.getConfig().getStringList("regionCrossingExceptions");

		mobClearer_region = plugin.getConfig().getString("options.mobclearer.region", mobClearer_region);
		mobClearer_repeatTimer = (long) plugin.getConfig().getInt("options.mobclearer.repeatTimer", mobClearer_repeatTimer.intValue());
		mobClearer_exceptions = plugin.getConfig().getStringList("options.mobclearer.exceptionList");

		shouldHaveDurability = plugin.getConfig().getIntegerList("options.shouldHaveDurability");

		if (shouldHaveDurability == null)
			shouldHaveDurability = new ArrayList<Integer>();

		List<?> blockedFood = plugin.getConfig().getList("blockedFood");
		if (blockedFood != null)
		{
			for (Object object : blockedFood)
			{
				Material material = null;
				if (object instanceof Integer)
				{
					material = Material.getMaterial((Integer) object);
				}
				else if (object instanceof String)
				{
					material = Material.matchMaterial((String) object);
				}

				if (material != null)
					BlockedFood.add(material);
			}
		}

		Object x = plugin.getConfig().get("combatCooldownFood");
		if (x instanceof Map)
		{
			for (Object object : ((Map) x).entrySet())
			{
				Map.Entry entry = (Map.Entry) object;
				Material material = null;

				if (!(entry.getValue() instanceof Integer))
					continue;

				if (entry.getKey() instanceof Integer)
				{
					material = Material.getMaterial((Integer) entry.getKey());
				}
				else if (entry.getKey() instanceof String)
				{
					material = Material.matchMaterial((String) entry.getKey());
				}

				if (material != null)
					CombatCooldownFood.put(material, (Integer) entry.getValue());
			}
		}

	}

	/**
	 * Save the data
	 */
	@SuppressWarnings("boxing")
	private static void saveData()
	{
		plugin.getConfig().set("options.arrowDamage", ArrowDamage);
		plugin.getConfig().set("options.arrowDamageOnlyPlayer", ArrowDamageOnlyPlayer);
		plugin.getConfig().set("options.bowCooldown", BowCooldown);
		plugin.getConfig().set("options.combatDuration", CombatDuration);
		plugin.getConfig().set("options.combatResetFoodCooldown", CombatResetFoodCooldown);
		plugin.getConfig().set("options.log.loginCooldown", LoginCooldown);
		plugin.getConfig().set("options.vehicleDespawnTime", VehicleDespawnTime);
		plugin.getConfig().set("options.bedHealing.HealAmount", HealAmount);
		plugin.getConfig().set("options.bedHealing.SecondsPerHeal", SecondsPerHeal);
		plugin.getConfig().set("options.alwaysLoginPermission", AlwaysLoginPermission);
		plugin.getConfig().set("options.balanceCommandPermission", BalanceCommandPermission);
		plugin.getConfig().set("options.commandAccessPermission", CommandAccessPermission);
		plugin.getConfig().set("options.blockFireBreaking", BlockFireBreaking);
		plugin.getConfig().set("options.blockPistonCrossing", BlockPistonCrossing);
		plugin.getConfig().set("options.blockFromToCrossing", BlockFromToCrossing);
		plugin.getConfig().set("options.foodMessageCooldown", FoodMessageCooldown);
		plugin.getConfig().set("options.bowMessageCooldown", BowMessageCooldown);
		plugin.getConfig().set("options.balanceBlockedCommandMessage", BalanceBlockedCommandMessage);
		plugin.getConfig().set("options.blockedCommandMessage", BlockedCommandMessage);
		plugin.getConfig().set("options.blockedFoodMessage", BlockedFoodMessage);
		plugin.getConfig().set("options.bowCooldownMessage", BowCooldownMessage);
		plugin.getConfig().set("options.combatEnterMessage", CombatEnterMessage);
		plugin.getConfig().set("options.combatFadeMessage", CombatFadeMessage);
		plugin.getConfig().set("options.foodCooldownMessage", FoodCooldownMessage);
		plugin.getConfig().set("options.loginCooldownMessage", LoginCooldownMessage);
		plugin.getConfig().set("blockedCommands", BlockedCommands);
		plugin.getConfig().set("positiveBalanceCommands", PositiveBalanceCommands);
		plugin.getConfig().set("regionCrossingExceptions", RegionCrossingExceptions);
		plugin.getConfig().set("options.enchantments.blocked", blockedEnchantments);
		plugin.getConfig().set("options.enderpearl.blockedRegions", blockedEnderpearlReigons);
		plugin.getConfig().set("options.log.shouldDelayRelogMessages", shouldDelayRelogMessages);
		plugin.getConfig().set("options.log.relogDelay", relogDelay);
		plugin.getConfig().set("options.mobclearer.region", mobClearer_region);
		plugin.getConfig().set("options.mobclearer.repeatTimer", mobClearer_repeatTimer);
		plugin.getConfig().set("options.mobclearer.exceptionList", mobClearer_exceptions);
		plugin.getConfig().set("options.shouldHaveDurability", shouldHaveDurability);
	}

	/**
	 * Load a configuration and creates a default configuration if possible
	 */

	public static void Load(DarkTape plugin)
	{
		Config.plugin = plugin;
		if (!new File(plugin.getDataFolder(), "config.yml").exists())
		{
			plugin.getDataFolder().mkdirs();
			plugin.saveDefaultConfig();
		}
	}

	/**
	 * Save the current configuration
	 */
	public static void Save()
	{
		Config.saveData();
		plugin.saveConfig();
	}

	/**
	 * Gets a color from the configuration and handles parsing of different
	 * definitions
	 */
	private static ChatColor getColor(String configPath, String defaultValue)
	{
		String colorString = plugin.getConfig().getString(configPath, defaultValue);

		try
		{
			colorString.replaceAll(" ", "_");
			ChatColor color = ChatColor.valueOf(colorString.toUpperCase());

			return color;
		}
		catch (Exception e)
		{
			// Log.log(Level.WARNING,
			// "Bad color definition in the config file. (" + configPath + ")");

			return ChatColor.valueOf(defaultValue.toUpperCase());
		}
	}

	/**
	 * Gets a color from the configuration and handles parsing of different
	 * definitions
	 */
	@SuppressWarnings("unused")
	private static ChatColor getColor(String configPath, ChatColor defaultValue)
	{
		return Config.getColor(configPath, defaultValue.name());
	}

}