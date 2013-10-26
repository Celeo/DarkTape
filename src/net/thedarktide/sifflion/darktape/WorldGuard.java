package net.thedarktide.sifflion.darktape;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuard
{

	private final DarkTape plugin;
	private final WorldGuardPlugin worldGuard;

	public WorldGuard(DarkTape plugin, Plugin worldGuard)
	{
		this.plugin = plugin;
		this.worldGuard = (WorldGuardPlugin) worldGuard;
	}

	// Checks if a player is member of a region by block location
	public boolean canBuild(Player player, Block block)
	{
		return canBuild(player, block.getLocation());
	}

	public boolean canBuild(Player player, Location location)
	{
		return this.worldGuard.canBuild(player, location);
	}

	// Checks if a player is a member of a region by location
	public boolean IsMemberOnLocation(Player player, Location loc)
	{
		if (this.worldGuard.getGlobalRegionManager().canBuild(player, loc))
			return true;
		return false;
	}

	/**
	 * Gets the list of regions in a point inside a world
	 * 
	 * @deprecated Use {@link #getRegionSet(Block)} or implement a better
	 *             solution which doesn't return null
	 */
	@Deprecated
	public List<String> getRegionList(World world, Block block)
	{
		List<String> regionlist = null;
		RegionManager mgr = this.worldGuard.getGlobalRegionManager().get(world);
		regionlist = mgr.getApplicableRegionsIDs(BukkitUtil.toVector(block));
		if (regionlist.isEmpty() == true)
			regionlist = null;
		return regionlist;
	}

	/**
	 * Returns a Set of region IDs that apply to the specified block
	 * 
	 * @return A new HashSet containing all the applicable region IDs
	 */
	public Set<String> getRegionSet(Block block)
	{
		RegionManager regionManager = this.worldGuard.getGlobalRegionManager().get(block.getWorld());
		return new HashSet<String>(regionManager.getApplicableRegionsIDs(BukkitUtil.toVector(block)));
	}

	public boolean isMoveable(Block source, Block target)
	{
		RegionManager regionManager = this.worldGuard.getGlobalRegionManager().get(source.getWorld());

		// ApplicableRegionSet sourceRegions =
		// regionManager.getApplicableRegions(BukkitUtil.toVector(source));
		ApplicableRegionSet targetRegions = regionManager.getApplicableRegions(BukkitUtil.toVector(target));

		if (targetRegions.size() == 0)
			return true;
		return true;
	}

	public DarkTape getPlugin()
	{
		return plugin;
	}

	public WorldGuardPlugin getWorldGuardPlugin()
	{
		return worldGuard;
	}

}