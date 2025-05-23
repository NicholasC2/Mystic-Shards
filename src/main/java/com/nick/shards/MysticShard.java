package com.nick.shards;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class MysticShard {

    public String name;
    public List<String> description;
    public ItemStack item;

    public MysticShard(String name, List<String> description, int customModel) {
        this.name = name;

        List<String> fixedDesc = new ArrayList<>();
        description.forEach(desc -> {
            fixedDesc.add(ChatColor.RESET + desc);
        });
        fixedDesc.add(ChatColor.RED + "Non-Upgraded");

        this.description = description;

        ItemStack newItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = newItem.getItemMeta();
        if (meta != null) {
            meta.setItemName(name);
            meta.setDisplayName(ChatColor.RESET + name.replace("_", " ") + " Shard");
            meta.setLore(fixedDesc);
            meta.setCustomModelData(customModel);
            item.setItemMeta(meta);
        }

        this.item = newItem;
    }

    public ItemStack upgrade_item() {
        List<String> fixedDesc = new ArrayList<>();
        description.forEach(desc -> {
            fixedDesc.add(ChatColor.RESET + desc);
        });
        fixedDesc.add(ChatColor.GREEN + "Upgraded");

        ItemStack newItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = newItem.getItemMeta();
        if (meta != null) {
            meta.setItemName(name);
            meta.setDisplayName(ChatColor.RESET + name.replace("_", " ") + " Shard");
            meta.setLore(fixedDesc);
            newItem.setItemMeta(meta);
        }

        return newItem;
    }

    @Override
    public String toString() {
        return this.name + ":" + this.description;
    }
}
