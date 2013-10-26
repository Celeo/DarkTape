package net.thedarktide.sifflion.darktape;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class MobClearer implements Runnable
{

	private final DarkTape plugin;

	public MobClearer(DarkTape instance)
	{
		this.plugin = instance;
	}

	@Override
	public void run()
	{
		World world = plugin.getServer().getWorld("world");
		if (world == null)
			return;
		WorldGuardPlugin worldGuard = plugin.getWorldGuard().getWorldGuardPlugin();
		ProtectedRegion region = worldGuard.getRegionManager(world).getRegion(Config.mobClearer_region);
		if (region == null)
		{
			plugin.Log("DarkTape cannot find the region "
					+ Config.mobClearer_region
					+ " and has cancelled the mob clearer.");
			plugin.getServer().getScheduler().cancelTasks(plugin);
			return;
		}
		int removedEntities = 0;
		for (Entity e : world.getEntities())
		{
			if (isInBounds(region, e.getLocation()) && shouldRemove(e))
			{
				e.remove();
				removedEntities++;
			}
		}
		plugin.Log("Mob clearer killed " + removedEntities + ".");
	}

	public static boolean shouldRemove(Entity e)
	{
		if (Config.mobClearer_exceptions == null
				|| Config.mobClearer_exceptions.isEmpty())
			return true;
		for (String s : Config.mobClearer_exceptions)
			if (e.toString().contains(s))
				return false;
		return true;
	}

	/**
	 * Check if the passed location is within the bounds of the passed region
	 */
	public static boolean isInBounds(ProtectedRegion region, Location location)
	{
		Vector upper = region.getMaximumPoint();
		Vector lower = region.getMinimumPoint();
		return (location.getBlockX() <= upper.getBlockX()
				&& location.getBlockY() <= upper.getBlockY()
				&& location.getBlockZ() <= upper.getBlockZ()
				&& location.getBlockX() >= lower.getBlockX()
				&& location.getBlockY() >= lower.getBlockY() && location.getBlockZ() >= lower.getBlockZ());
	}

}