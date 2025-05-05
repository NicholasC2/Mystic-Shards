package com.nick.shards.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nick.shards.MysticShard;

import net.md_5.bungee.api.ChatColor;

public class ShardsCommand implements TabCompleter, CommandExecutor {

    public HashMap<String, MysticShard> shards;

    public ShardsCommand(HashMap<String, MysticShard> shards) {
        this.shards = shards;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2 && args.length <= 3) {
            if (args[0].equalsIgnoreCase("set")) {
                Player player = Bukkit.getServer().getPlayer(args[1]);
                if (player != null) {
                    if (shards.get(args[2]) != null) {
                        MysticShard shard = shards.get(args[2]);
                        PersistentDataContainer player_data = player.getPersistentDataContainer();

                        shards.forEach((name, shard_remove) -> {
                            player.getInventory().remove(shard_remove.item);
                            player.getInventory().remove(shard_remove.upgrade_item());
                        });

                        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                            if (player.getInventory().getItemInOffHand().getItemMeta() != null) {
                                ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                                if (itemMeta != null) {
                                    if (shards.get(itemMeta.getItemName()) != null) {
                                        player.getInventory().setItemInOffHand(null);
                                    }
                                }
                            }
                        }

                        HashMap<Integer, ItemStack> failed_items = player.getInventory().addItem(shard.item);
                        failed_items.forEach((i, item) -> {
                            Location playerLocation = player.getLocation();
                            if (playerLocation != null) {
                                player.getWorld().dropItem(playerLocation, item);
                            }

                        });
                        player_data.set(new NamespacedKey("shards", "shard"), PersistentDataType.STRING, shard.name);
                        sender.sendMessage(ChatColor.GREEN + "Set " + player.getName() + " Shard to: " + shard.name);
                        return true;
                    } else {
                        sender.sendMessage("Unknown Shard: " + args[2]);
                        return true;
                    }
                } else {
                    sender.sendMessage("Unknown Player: " + args[1]);
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("reset")) {
                Player player = Bukkit.getServer().getPlayer(args[1]);
                if (player != null) {
                    PersistentDataContainer player_data = player.getPersistentDataContainer();
                    player_data.set(new NamespacedKey("shards", "shard"), PersistentDataType.STRING, "");

                    shards.forEach((name, shard_remove) -> {
                        player.getInventory().remove(shard_remove.item);
                        player.getInventory().remove(shard_remove.upgrade_item());
                    });

                    if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                        if (player.getInventory().getItemInOffHand().getItemMeta() != null) {
                            ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                            if (itemMeta != null) {
                                if (shards.get(itemMeta.getItemName()) != null) {
                                    player.getInventory().setItemInOffHand(null);
                                }
                            }
                        }
                    }

                    sender.sendMessage(ChatColor.GREEN + "Shard Reset");
                    return true;
                } else if (args[1].equalsIgnoreCase("@a")) {
                    for (Player online_player : Bukkit.getServer().getOnlinePlayers()) {
                        PersistentDataContainer player_data = online_player.getPersistentDataContainer();
                        player_data.set(new NamespacedKey("shards", "shard"), PersistentDataType.STRING, "");

                        shards.forEach((name, shard_remove) -> {
                            online_player.getInventory().remove(shard_remove.item);
                            online_player.getInventory().remove(shard_remove.upgrade_item());
                        });

                        if (online_player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                            ItemMeta itemMeta = online_player.getInventory().getItemInOffHand().getItemMeta();
                            if (itemMeta != null) {
                                if (shards.get(itemMeta.getItemName()) != null) {
                                    online_player.getInventory().setItemInOffHand(null);
                                }
                            }
                        }

                        sender.sendMessage(ChatColor.GREEN + "Shard Reset for " + online_player.getName());
                    }
                    return true;
                } else {
                    sender.sendMessage("Unknown Player: " + args[1]);
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("upgrade")) {
                Player player = Bukkit.getServer().getPlayer(args[1]);
                if (player != null) {
                    if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                        if (player.getInventory().getItemInOffHand().getItemMeta() != null) {
                            ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                            if (itemMeta != null) {
                                if (shards.get(itemMeta.getItemName()) != null) {
                                    PersistentDataContainer player_data = player.getPersistentDataContainer();
                                    if (shards.get(player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING)) != null) {
                                        MysticShard shard = shards.get(player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING));
                                        player.getInventory().setItemInOffHand(shard.upgrade_item());
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sender.sendMessage("Unknown Player: " + args[1]);
                    return true;
                }
                sender.sendMessage("Error");
                return true;
            }
            sender.sendMessage("Unkown Argument: " + args[0]);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> player_list = new ArrayList<>();
        if (args.length == 1) {
            String[] nextArgs = new String[]{"set", "reset", "upgrade"};
            return Arrays.asList(nextArgs);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                player_list.add(player.getName());
            });
            return player_list;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return new ArrayList<>(shards.keySet());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                player_list.add(player.getName());
            });
            player_list.add("@a");
            return player_list;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("upgrade")) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                player_list.add(player.getName());
            });
            return player_list;
        }
        return new ArrayList<>();
    }
}
