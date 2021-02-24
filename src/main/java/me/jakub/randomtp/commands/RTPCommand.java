package me.jakub.randomtp.commands;

import lombok.Getter;
import me.jakub.randomtp.Randomtp;
import me.jakub.randomtp.utils.Cooldown;
import me.jakub.randomtp.utils.Log;
import me.jakub.randomtp.utils.TeleportUtils;
import me.jakub.randomtp.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;


public class RTPCommand implements CommandExecutor {


    static Randomtp plugin;

    public RTPCommand(Randomtp plugin) {
        this.plugin = plugin;
    }

    public static Map<String, Long> cooldowns = new HashMap<String, Long>();

    TeleportUtils teleportUtils = new TeleportUtils(plugin);


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (TeleportUtils.hasCountdown.contains(player)) {
                return true;
            }


            //If executed by a player
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("@everyone")) {
                    if (player.hasPermission("randomTp.rtp.everyone")) {
                        player.sendMessage(Utils.getTpEveryoneMessage());
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            teleportUtils.rtpPlayer(target, true, true, false);
                        }
                    } else {
                        player.sendMessage(Utils.getNoPermission());
                    }
                }
                if (!args[0].equalsIgnoreCase("@everyone")) {

                    if (player.hasPermission("randomTp.rtp.others")) {

                        //If player has rtp.others permission and specifies a target
                        Player target = Bukkit.getServer().getPlayer(args[0]);

                        if (target != null) {
                            if (target.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                                teleportUtils.rtpPlayer(target, true, true, false);
                                player.sendMessage(Utils.getTpMessageSender(target));
                            } else {
                                player.sendMessage("§cThat player is not in the overworld!");
                            }
                        } else {
                            player.sendMessage("§cCouldn't find that player!");
                        }
                    } else {
                        player.sendMessage(Utils.getNoPermission());
                    }
                }

            } else {
                if (player.hasPermission("randomTp.rtp")) {

                    if (!player.hasPermission("randomTp.cooldown.bypass")) {
//START Cooldown
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
                                }
                                return true;
                            }
                        }
                        // player doesn't have a cooldown
                        if (Randomtp.vaultHooked) {//Vault things
                            teleportUtils.rtpPlayer(player, false, false, true);

                        } else {
                            teleportUtils.rtpPlayer(player, false, true, true);
                        }//END Cooldown
                    } else {
                        //Has cooldown bypass perms
                        if (Randomtp.vaultHooked) {
                            teleportUtils.rtpPlayer(player, false, false, false);

                        } else {
                            teleportUtils.rtpPlayer(player, false, true, false);
                        }
                    }
                } else {
                    player.sendMessage(Utils.getNoPermission());
                }
            }

        } else {
            if (commandSender instanceof ConsoleCommandSender) {
                //If executed from console
                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null) {
                        if (target.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                            teleportUtils.rtpPlayer(target, true, true, false);
                            Log.log(Log.LogLevel.SUCCESS, "Successfully teleported " + target.getName() + " to a random location");
                        } else {
                            Log.log(Log.LogLevel.ERROR, "That player is not in the overworld!");
                        }
                    } else {
                        Log.log(Log.LogLevel.ERROR, "Couldn't find that player");
                    }
                } else {
                    Log.log(Log.LogLevel.ERROR, "Usage: rtp <player>");
                }
            }
        }


        return true;
    }
}