package me.jakub.randomtp.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.palmergames.bukkit.towny.TownyAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import me.jakub.randomtp.Randomtp;
import me.jakub.randomtp.utils.Log;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;

public class ClaimHookManager {

    static Randomtp plugin;

    public ClaimHookManager(Randomtp plugin) {
        this.plugin = plugin;
    }

    private static boolean gpHooked = false;
    private static GriefPrevention griefPrevention = null;

    private static boolean wgHooked = false;
    private static WorldGuard worldGuard = null;

    private static boolean townyHooked = false;
    private static TownyAPI towny = null;

    private static boolean redProtectHooked = false;
    private static RedProtectAPI redProtect = null;

    private static boolean protectionStonesHooked = false;
    private static ProtectionStones protectionStones = null;

    public void initHooks() {
        gpHooked = hookGp();
        wgHooked = hookWg();
        townyHooked = hookTowny();
        redProtectHooked = hookRedProtect();
        protectionStonesHooked = hookProtectionStones();
    }

    public boolean isClaimedAt(Location location) {
        return isGpClaimed(location) || isWgClaimed(location) || isTownyClaimed(location) || isRedProtectClaimed(location) || isProtectionStonesHooked(location);
    }


    private boolean hookGp() {
        if (!(plugin.getConfig().getBoolean("Claim-protection.Griefprevention"))) return false;
        if (plugin.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            Log.log(Log.LogLevel.SUCCESS, "Successfully hooked into GriefPrevention");
            griefPrevention = GriefPrevention.instance;
            return true;
        } else {
            Log.log(Log.LogLevel.ERROR, "Couldn't hook into GriefPrevention, check if you have it installed");
            return false;
        }
    }

    private boolean hookWg() {
        if (!(plugin.getConfig().getBoolean("Claim-protection.Worldguard"))) return false;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Log.log(Log.LogLevel.SUCCESS, "Successfully hooked into WorldGuard");
            worldGuard = WorldGuard.getInstance();
            return true;
        } else {
            Log.log(Log.LogLevel.ERROR, "Couldn't hook into WorldGuard, check if you have it and WorldEdit installed");
            return false;
        }
    }

    private boolean hookTowny() {
        if (!(plugin.getConfig().getBoolean("Claim-protection.Towny"))) return false;
        if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            Log.log(Log.LogLevel.SUCCESS, "Successfully hooked into Towny");
            towny = TownyAPI.getInstance();
            return true;
        } else {
            Log.log(Log.LogLevel.ERROR, "Couldn't hook into Towny, check if you have it installed");
            return false;
        }
    }

    private boolean hookRedProtect() {
        if (!(plugin.getConfig().getBoolean("Claim-protection.Redprotect"))) return false;
        if (plugin.getServer().getPluginManager().getPlugin("RedProtect") != null) {
            Log.log(Log.LogLevel.SUCCESS, "Successfully hooked into RedProtect");
            redProtect = RedProtect.get().getAPI();
            return true;
        } else {
            Log.log(Log.LogLevel.ERROR, "Couldn't hook into RedProtect, check if you have it installed");
            return false;
        }
    }

    public static boolean hookProtectionStones() {
        if (!(plugin.getConfig().getBoolean("Claim-protection.Protectionstones"))) return false;
        if (plugin.getServer().getPluginManager().getPlugin("ProtectionStones") != null) {
            Log.log(Log.LogLevel.SUCCESS, "Successfully hooked into ProtectionStones");
            protectionStones = ProtectionStones.getInstance();
            return true;
        } else {
            Log.log(Log.LogLevel.ERROR, "Couldn't hook into ProtectionStones, check if you have it installed");
            return false;
        }
    }


    private boolean isGpClaimed(Location location) {
        if (!(gpHooked && griefPrevention != null && plugin.getConfig().getBoolean("Claim-protection.Griefprevention")))
            return false;
        Claim claim = griefPrevention.dataStore.getClaimAt(location, true, null);
        return claim != null;
    }

    private boolean isWgClaimed(Location location) {
        boolean result = true;

        if (!(wgHooked && worldGuard != null && plugin.getConfig().getBoolean("Claim-protection.Worldguard")))
            return false;
        try {
            RegionContainer container = worldGuard.getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
            result = set.size() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return !result;
    }

    private boolean isTownyClaimed(Location location) {
        if (!(townyHooked && towny != null && plugin.getConfig().getBoolean("Claim-protection.Towny")))
            return false;
        return !towny.isWilderness(location.getBlock());
    }

    private boolean isRedProtectClaimed(Location location) {
        if (!(redProtectHooked && redProtect != null && plugin.getConfig().getBoolean("Claim-protection.Redprotect")))
            return false;
        return redProtect.getRegion(location) != null;
    }

    private boolean isProtectionStonesHooked(Location location) {
        if (!(protectionStonesHooked && protectionStones != null && plugin.getConfig().getBoolean("Claim-protection.ProtectionStones")))
            return false;
        return PSRegion.fromLocation(location) != null;
    }


}
