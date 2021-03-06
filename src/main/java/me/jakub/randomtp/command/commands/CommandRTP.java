package me.jakub.randomtp.command.commands;

import me.jakub.randomtp.Randomtp;
import me.jakub.randomtp.command.*;
import me.jakub.randomtp.command.tabcomplete.RTPCommandTabCompleter;
import me.jakub.randomtp.utils.Cooldown;
import me.jakub.randomtp.utils.Log;
import me.jakub.randomtp.utils.TeleportUtils;
import me.jakub.randomtp.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static me.jakub.randomtp.command.Permissions.*;

public class CommandRTP extends RandomTPCommand {

    /*
     *
     * /rtp [player|@everyone|test] [world] [tier|biome]
     * /rtplugin <help|setborder|reload> [number|commands|permissions]
     * */

    public static Map<String, Long> cooldowns = new HashMap<String, Long>();

    private TeleportUtils teleportUtils;
    private Randomtp plugin;

    public CommandRTP(Randomtp plugin) {
        super(plugin, "rtp");
        this.plugin = plugin;
        this.teleportUtils = new TeleportUtils(plugin);
        setTabCompleter(new RTPCommandTabCompleter());
    }

    private CommandSender sender;
    private Player player;
    private String[] args;


    @Override
    public void execute(CommandSender sender, String label, String[] args) throws CommandExecutionException {
        this.sender = sender;
        this.args = args;

        if (sender instanceof Player) {
            this.player = (Player) sender;
            runPlayer();
        } else {
            runConsole();
        }
    }

    //-----------------------PLAYER RTP-------------------------
    private void runPlayer() throws CommandExecutionException {

        if (TeleportUtils.hasCountdown.contains(player))
            throw new CommandExecutionException(Utils.inCountdownMessage());

        switch (args.length) {
            case 0:
                runPlayerSelf();
                break;
            case 1:
                runPlayerOther();
                break;
            case 2:
                runPlayerOtherWorld();
                break;
            case 3:
                runPlayerOtherTier();
                break;
            default:
                throw new InvalidUsageException();
        }
    }


    private void runPlayerSelf() throws NoPermissionException {
        if (!player.hasPermission(RTP_SELF.get()))
            throw new NoPermissionException();
        if (!(player.hasPermission(BYPASS_COOLDOWN.get()))) {

            if (cooldowns.containsKey(player.getName())) {
                // player is inside hashmap
                if (cooldowns.get(player.getName()) > System.currentTimeMillis()) {
                    // they still have time left in the cooldown
                    long timeLeft = (cooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000;
                    try {
                        String formatted = Cooldown.getStr(timeLeft, Cooldown.FormatType.valueOf(plugin.getConfig().getString("Cooldown.msg-format-type")));
                        player.sendMessage(Utils.getCooldownMessage(formatted));
                    } catch (Exception e) {
                        Log.log(Log.LogLevel.ERROR, "Wrong message cooldown type was used in the config, use either SECONDS, MINUTES, HOURS or AUTO");
                        return;
                    }
                    return;
                }
            }
            //END Cooldown
            teleportUtils.rtpPlayer(player, null, false, !Randomtp.vaultHooked, true, null, false, false, false, null, false, null);
        } else {
            //Has cooldown bypass perms
            teleportUtils.rtpPlayer(player, null, false, !Randomtp.vaultHooked, false, null, false, false, false, null, false, null);
        }
    }


    private void runPlayerOther() throws CommandExecutionException {
        if (args[0].equalsIgnoreCase("@everyone")) {
            if (!player.hasPermission(RTP_EVERYONE.get()))
                throw new NoPermissionException();
            player.sendMessage(Utils.getTpEveryoneMessage());
            for (Player target : Bukkit.getOnlinePlayers()) {
                teleportUtils.rtpPlayer(target, player, true, true, false, null, false, true, true, null, true, null);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("test")) {
            if (!player.hasPermission(RTP_TEST.get()))
                throw new NoPermissionException();
            player.sendMessage(teleportUtils.testRTP(player));
            return;
        }

        //Player
        if (!player.hasPermission(RTP_OTHERS.get()))
            throw new NoPermissionException();
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCouldn't find that player");
        teleportUtils.rtpPlayer(target, player, true, true, false, null, false, true, true, null, true, null);
        player.sendMessage(Utils.getTpMessageSender(target));
    }

    private void runPlayerOtherWorld() throws CommandExecutionException {
        if (args[0].equalsIgnoreCase("@everyone")) {
            if (!player.hasPermission(RTP_EVERYONE.get()))
                throw new NoPermissionException();
            player.sendMessage(Utils.getTpEveryoneMessage());
            for (Player target : Bukkit.getOnlinePlayers()) {
                World world = Utils.getWorldFromString(args[1]);
                if (world == null)
                    throw new CommandExecutionException(Utils.getWrongWorldMessage());
                teleportUtils.rtpPlayer(target, player, true, true, false, null, false, true, true, world, true, null);
            }
            return;
        }
        if (!player.hasPermission(RTP_OTHERS.get()))
            throw new NoPermissionException();
        //Player
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCouldn't find that player");
        World world = Utils.getWorldFromString(args[1]);
        if (world == null)
            throw new CommandExecutionException(Utils.getWrongWorldMessage());
        teleportUtils.rtpPlayer(target, player, true, true, false, null, true, true, true, world, true, null);
        player.sendMessage(Utils.getTpMessageSender(target));
    }

    private void runPlayerOtherTier() throws CommandExecutionException {
        if (args[0].equalsIgnoreCase("@everyone")) {
            if (!player.hasPermission(RTP_EVERYONE.get()))
                throw new NoPermissionException();
            player.sendMessage(Utils.getTpEveryoneMessage());
            World world = Utils.getWorldFromString(args[1]);
            if (world == null)
                throw new CommandExecutionException(Utils.getWrongWorldMessage());
            boolean tierRTP = false;
            Biome b = Utils.getBiomeFromString(args[2]);
            Utils.RTPTier tier = Utils.getTierFromString(args[2]);
            if (b == null && tier == null)
                throw new CommandExecutionException(Utils.getWrongTierOrBiomeNameMessage());
            if (b == null) {
                tierRTP = true;
            }
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (tierRTP)
                    teleportUtils.rtpPlayer(target, player, true, true, false, null, false, true, true, world, true, tier);
                else
                    teleportUtils.rtpPlayer(target, player, true, true, false, b.toString(), false, true, true, world, true, null);
            }
            return;
        }
        if (!player.hasPermission(RTP_OTHERS.get()))
            throw new NoPermissionException();
        //Player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCouldn't find that player");
        World world = Utils.getWorldFromString(args[1]);
        if (world == null)
            throw new CommandExecutionException(Utils.getWrongWorldMessage());

        boolean tierRTP = false;
        Biome b = Utils.getBiomeFromString(args[2]);
        Utils.RTPTier tier = Utils.getTierFromString(args[2]);
        if (b == null && tier == null)
            throw new CommandExecutionException(Utils.getWrongTierOrBiomeNameMessage());
        if (b == null) {
            tierRTP = true;
        }

        if (!tierRTP) {
            teleportUtils.rtpPlayer(target, player, true, true, false, b.toString(), true, true, true, world, true, null);
            player.sendMessage(Utils.getTpMessageSender(target));
        } else {
            teleportUtils.rtpPlayer(target, player, true, true, false, null, true, true, true, world, true, tier);
            player.sendMessage(Utils.getTpMessageSender(target));
        }
    }


    //-----------------------CONSOLE RTP-------------------------
    private void runConsole() throws CommandExecutionException {
        switch (args.length) {
            case 1:
                runConsoleOther();
                break;
            case 2:
                runConsoleOtherWorld();
                break;
            case 3:
                runConsoleOtherTier();
                break;
            default:
                throw new InvalidUsageException("(You can't RTP yourself)");
        }
    }

    private void runConsoleOther() throws CommandExecutionException {
        if (args[0].equalsIgnoreCase("test")){
            sender.sendMessage(teleportUtils.testRTP());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCouldn't find that player");
        teleportUtils.rtpPlayer(target, null, true, true, false, null, false, true, true, null, true, null);
        Log.log(Log.LogLevel.SUCCESS, "Teleporting player " + target.getName() + " to a random location");
    }

    private void runConsoleOtherWorld() throws CommandExecutionException {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCouldn't find that player");
        World world = Utils.getWorldFromString(args[1]);
        if (world == null)
            throw new CommandExecutionException(Utils.getWrongWorldMessage());
        teleportUtils.rtpPlayer(target, null, true, true, false, null, false, true, true, world, true, null);
        Log.log(Log.LogLevel.SUCCESS, "Teleporting player " + target.getName() + " to a random location in world " + world.getName());
    }

    private void runConsoleOtherTier() throws CommandExecutionException {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)
            throw new CommandExecutionException("§cCoudln't find that player");
        World world = Utils.getWorldFromString(args[1]);
        if (world == null)
            throw new CommandExecutionException(Utils.getWrongWorldMessage());

        boolean tierRTP = false;
        Biome b = Utils.getBiomeFromString(args[2]);
        Utils.RTPTier tier = Utils.getTierFromString(args[2]);
        if (b == null && tier == null)
            throw new CommandExecutionException(Utils.getWrongTierOrBiomeNameMessage());
        if (b == null) {
            tierRTP = true;
        }

        if (tierRTP) {
            teleportUtils.rtpPlayer(target, null, true, true, false, null, false, true, true, world, true, tier);
            Log.log(Log.LogLevel.SUCCESS, "Teleporting player " + target.getName() + " to a random location in world " + world.getName() + " with RTP Tier " + tier.toString().toLowerCase());
        } else {
            teleportUtils.rtpPlayer(target, null, true, true, false, b.toString(), false, true, true, world, true, null);
            Log.log(Log.LogLevel.SUCCESS, "Teleporting player " + target.getName() + " to a random location in world " + world.getName() + " to biome " + b.toString().toLowerCase());
        }

    }

    //wow this took a long time...
}
