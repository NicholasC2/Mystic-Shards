package com.nick.shards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.nick.shards.commands.ShardsCommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MysticShards extends JavaPlugin implements Listener {

    public HashMap<String, MysticShard> shards = new HashMap<>();

    public double fire_aspect_chance = 0.2;

    @Override
    public void onEnable() {
        initShards();
        getServer().getPluginManager().registerEvents(this, this);
        ShardsCommand shardsCommand = new ShardsCommand(shards);
        getCommand("shards").setExecutor(shardsCommand);
        getCommand("shards").setTabCompleter(shardsCommand);
        getServer().getOnlinePlayers().forEach(player -> {
            showEnergy(player);
        });
        getLogger().info("Plugin Enabled");
    }

    public void initShards() {
        createShard(new MysticShard("Opal", Arrays.asList(
                "Ability: Double Jump + Dash (3 minute cooldown)",
                "When Upgraded: Cobweb-Freeing + Knocks nearby players away"
        ), 0));
        createShard(new MysticShard("Blaze", Arrays.asList(
                "Ability: Fire Aspect Chance(20% chance) + Fire Resistance",
                "When Upgraded: Fire Ball (3 minute cooldown)"
        ), 1));
        createShard(new MysticShard("Emerald", Arrays.asList(
                "Ability: Hero of the Village II + Luck",
                "When Upgraded: Mending 1.3x"
        ), 2));
        createShard(new MysticShard("Blood", Arrays.asList(
                "Ability: Weakness(20% chance, 2 seconds)",
                "When Upgraded: Phase (3 seconds, 3 minute cooldown)"
        ), 3));
        createShard(new MysticShard("Amethyst", Arrays.asList(
                "Ability: Fortune IV + Efficiency III",
                "When Upgraded: Haste II (45 seconds, 3 minute cooldown)"
        ), 4));
        createShard(new MysticShard("Netherite", Arrays.asList(
                "Ability: +2.5 Armor Toughness + Arrow Immunity",
                "When Upgraded: Slowness IV + Resistance IV, Anti-Knockback (10 seconds, 2 minute 30 second cooldown)"
        ), 5));
        createShard(new MysticShard("Echo", Arrays.asList(
                "Ability: Invisible to Sculk Sensors(Not World Actions) + Swift Sneak III + 1.3x Damage when on wool",
                "When Upgraded: Blind Enemies on Hit (20% chance)"
        ), 6));
        createShard(new MysticShard("Health", Arrays.asList(
                "Ability: +4 Hearts, +8 Saturation, Extra Absorbtion from Golden Apples",
                "When Upgraded: Steal Hearts from Players(30 seconds, 3 minute cooldown)"
        ), 7));
        createShard(new MysticShard("Wither", Arrays.asList(
                "Ability: 25% less Wither-related Damage + Double Chance of Wither Skull",
                "When Upgraded: Wither Enemies on Hit (3 seconds, 20% chance)"
        ), 8));
        createShard(new MysticShard("Frost", Arrays.asList(
                "Ability: Speed +2.5 on Ice + Ice Spawns Underneath Boat",
                "When Upgraded: Snowball (Explodes giving Slowness + Mining Fatigue)"
        ), 9));

        ItemStack upgradeItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta upgradeMeta = upgradeItem.getItemMeta();
        upgradeMeta.setItemModel(NamespacedKey.minecraft("netherite_upgrade_smithing_template"));
        upgradeMeta.setItemName("shard_upgrade");
        upgradeMeta.setLore(Arrays.asList("Will Upgrade your shard"));
        upgradeMeta.setDisplayName("Shard Upgrade");
        upgradeItem.setItemMeta(upgradeMeta);

        ShapedRecipe upgradeRecipe = new ShapedRecipe(new NamespacedKey("shards", "shard_upgrade"), upgradeItem);

        upgradeRecipe.shape("dwd", "pnp", "dwd");
        upgradeRecipe.setIngredient('d', Material.DRAGON_BREATH);
        upgradeRecipe.setIngredient('w', Material.WITHER_SKELETON_SKULL);
        upgradeRecipe.setIngredient('p', Material.PLAYER_HEAD);
        upgradeRecipe.setIngredient('n', Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

        getServer().addRecipe(upgradeRecipe);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null) {
            event.getInventory().forEach(item -> {
                if (item.equals(event.getInventory().getResult())) {
                    return;
                }
                if (item.getItemMeta() != null) {
                    if (shards.get(item.getItemMeta().getItemName()) != null) {
                        event.getInventory().setResult(null);
                    }
                    if (item.getItemMeta().getItemName().equalsIgnoreCase("shard_upgrade")) {
                        event.getInventory().setResult(null);
                    }
                }
            });
        }
    }

    public void createShard(MysticShard shard) {
        shards.put(shard.name, shard);
    }

    @EventHandler
    public void onItemHeld(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(this, () -> {
            update_offhand(event.getPlayer());
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            Bukkit.getScheduler().runTask(this, () -> {
                update_offhand((Player) event.getInventory().getHolder());
            });
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            PersistentDataContainer playerData = player.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey("shards", "fire_aspect_chance");

            if (playerData.has(key, PersistentDataType.DOUBLE)) {
                Double chance = playerData.get(key, PersistentDataType.DOUBLE);
                if (chance != null) {
                    double randomValue = Math.random();
                    if (randomValue <= chance) {
                        event.getEntity().setFireTicks(80);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            if (player.getInventory().getItemInMainHand().getItemMeta() != null) {
                switch (player.getInventory().getItemInMainHand().getItemMeta().getItemName().toLowerCase()) {
                    case "shard_upgrade":
                        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                            PersistentDataContainer player_data = player.getPersistentDataContainer();
                            if (shards.get(player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING)) != null) {
                                MysticShard shard = shards.get(player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING));
                                player.getInventory().setItemInOffHand(shard.upgrade_item());
                            }
                        }
                        break;

                    case "blaze":
                        if (is_shard(player, player.getInventory().getItemInMainHand())) {
                            if (player.getInventory().getItemInMainHand().getItemMeta().getLore().contains(ChatColor.GREEN + "Upgraded")) {
                                PersistentDataContainer player_data = player.getPersistentDataContainer();
                                if (player_data.get(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER) <= 0) {
                                    Location loc = player.getLocation();
                                    loc.setY(loc.getY() + 1);
                                    player.getWorld().spawnEntity(loc, EntityType.FIREBALL);
                                    player.sendTitle("", "Fireball Cooldown: 180s", 10, 20, 0);
                                    new BukkitRunnable() {
                                        int cooldown = 180;

                                        @Override
                                        public void run() {
                                            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                                if (is_shard(player, player.getInventory().getItemInMainHand())) {
                                                    player.sendTitle("", "Fireball Cooldown: " + cooldown + "s", 0, 20, 0);
                                                }
                                            }
                                            this.cooldown = cooldown - 1;
                                            player_data.set(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER, cooldown);
                                            if (cooldown <= 0) {
                                                player.sendTitle("", "Fireball Ready", 0, 20, 10);
                                                this.cancel();
                                            }
                                        }
                                    }.runTaskTimerAsynchronously(this, 0, 20);
                                }
                            }
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public void update_offhand(Player player) {
        PersistentDataContainer player_data = player.getPersistentDataContainer();
        NamespacedKey fireKey = new NamespacedKey("shards", "fire_aspect_chance");
        NamespacedKey healthKey = new NamespacedKey("shards", "health_enabled");

        if (player_data.get(fireKey, PersistentDataType.DOUBLE) == null) {
            player_data.set(fireKey, PersistentDataType.DOUBLE, 0.0);
        }
        if (player_data.get(healthKey, PersistentDataType.BOOLEAN) == null) {
            player_data.set(healthKey, PersistentDataType.BOOLEAN, false);
        }

        if (player_data.get(fireKey, PersistentDataType.DOUBLE) == fire_aspect_chance) {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            player_data.set(fireKey, PersistentDataType.DOUBLE, (double) 0.0);
        }
        if (player_data.get(healthKey, PersistentDataType.BOOLEAN) == true || player_data.get(healthKey, PersistentDataType.BOOLEAN) == null) {
            AttributeInstance max_hp = player.getAttribute(Attribute.MAX_HEALTH);
            if (max_hp != null) {
                max_hp.setBaseValue(20);
            }
            player.setSaturation(0);
            player_data.set(healthKey, PersistentDataType.BOOLEAN, false);
        }
        if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
            if (player.getInventory().getItemInOffHand().getItemMeta() != null) {
                if (is_shard(player, player.getInventory().getItemInOffHand())) {
                    shards.forEach((name, shard) -> {
                        ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                        if (itemMeta != null) {
                            String itemName = itemMeta.getItemName().toLowerCase();
                            switch (itemName) {
                                case "blaze":
                                    if (is_shard(player, player.getInventory().getItemInOffHand())) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
                                        player_data.set(fireKey, PersistentDataType.DOUBLE, fire_aspect_chance);
                                    }
                                    break;

                                case "health":
                                    if (is_shard(player, player.getInventory().getItemInOffHand())) {
                                        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
                                        if (maxHealthAttribute != null) {
                                            maxHealthAttribute.setBaseValue(26);
                                            player.setSaturation(0);
                                            player_data.set(healthKey, PersistentDataType.BOOLEAN, true);
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    );
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerInputEvent e) {
        Player player = e.getPlayer();
        if (is_shard(player, player.getInventory().getItemInOffHand())) {
            if (player.getInventory().getItemInOffHand().getItemMeta().getItemName().equalsIgnoreCase("opal")) {
                PersistentDataContainer player_data = player.getPersistentDataContainer();
                if (player_data.get(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER) <= 0) {
                    if ((!player.isFlying() && !player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid()) && e.getInput().isJump()) {
                        double dir = Math.toRadians(-player.getLocation().getYaw());
                        double speed = 0.5;
                        player.setVelocity(new Vector(Math.sin(dir) * speed, 0.3, Math.cos(dir) * speed));
                        player.sendTitle("", "Double Jump Cooldown: 60s", 10, 20, 0);
                        new BukkitRunnable() {
                            int cooldown = 60;

                            @Override
                            public void run() {
                                if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                                    if (is_shard(player, player.getInventory().getItemInOffHand())) {
                                        player.sendTitle("", "Double Jump Cooldown: " + cooldown + "s", 0, 20, 0);
                                    }
                                }
                                this.cooldown = cooldown - 1;
                                player_data.set(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER, cooldown);
                                if (cooldown <= 0) {
                                    player.sendTitle("", "Double Jump Ready", 0, 20, 10);
                                    this.cancel();
                                }
                            }
                        }.runTaskTimerAsynchronously(this, 0, 20);
                    }
                }
            }
        }
    }

    public boolean is_shard(@Nonnull Player player, @Nonnull ItemStack shard) {
        PersistentDataContainer player_data = player.getPersistentDataContainer();
        if (shard.getType() != Material.AIR) {
            if (shard.getItemMeta() != null) {
                if (player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING).equalsIgnoreCase(shard.getItemMeta().getItemName())) {
                    return true;
                }
            }
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!shards.isEmpty()) {
            PersistentDataContainer player_data = player.getPersistentDataContainer();
            MysticShard shard = shards.get(player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING));
            if (shard == null) {
                MysticShard random_shard = new ArrayList<>(shards.values()).get((int) Math.round(Math.random() * (shards.size() - 1)));

                player_data.set(new NamespacedKey("shards", "shard"), PersistentDataType.STRING, random_shard.name);

                player.sendTitle("", ChatColor.GREEN + "Your Shard is: " + random_shard.name, 20, 60, 30);
                player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1f);

                HashMap<Integer, ItemStack> failed_items = player.getInventory().addItem(random_shard.item);
                failed_items.forEach((i, item) -> {
                    Location loc = player.getLocation();
                    if (loc != null) {
                        player.getWorld().dropItem(loc, item);
                    }
                });

                player_data.set(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER, 3);
                player_data.set(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER, 0);
                player_data.set(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER, 0);
            }
            showEnergy(player);
        } else {
            Bukkit.getLogger().severe("Shards not initialized");
        }
    }

    public void showEnergy(Player player) {
        PersistentDataContainer player_data = player.getPersistentDataContainer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player_data.get(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER) > 3) {
                    player_data.set(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER, 3);
                }
                if (player_data.get(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER) < -3) {
                    player_data.set(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER, -3);
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                        ChatColor.LIGHT_PURPLE
                        + "Shard: "
                        + player_data.get(new NamespacedKey("shards", "shard"), PersistentDataType.STRING)
                        + " Shard, Energy: "
                        + Integer.toString(player_data.get(new NamespacedKey("shards", "energy"), PersistentDataType.INTEGER))
                ));
                if (player_data.get(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER) > 0) {
                    new BukkitRunnable() {
                        int cooldown = player_data.get(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER);

                        @Override
                        public void run() {
                            if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                                if (is_shard(player, player.getInventory().getItemInOffHand())) {
                                    player.sendTitle("", "Double Jump Cooldown: " + cooldown + "s", 0, 20, 0);
                                }
                            }
                            this.cooldown = cooldown - 1;
                            player_data.set(new NamespacedKey("shards", "can_double_jump"), PersistentDataType.INTEGER, cooldown);
                            if (cooldown <= 0) {
                                player.sendTitle("", "Double Jump Ready", 0, 20, 10);
                                this.cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("MysticShards"), 0, 20);
                }
                if (player_data.get(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER) > 0) {
                    new BukkitRunnable() {
                        int cooldown = player_data.get(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER);

                        @Override
                        public void run() {
                            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                if (is_shard(player, player.getInventory().getItemInMainHand())) {
                                    player.sendTitle("", "Fireball Cooldown: " + cooldown + "s", 0, 20, 0);
                                }
                            }
                            this.cooldown = cooldown - 1;
                            player_data.set(new NamespacedKey("shards", "can_fireball"), PersistentDataType.INTEGER, cooldown);
                            if (cooldown <= 0) {
                                player.sendTitle("", "Fireball Ready", 0, 20, 10);
                                this.cancel();
                            }
                        }
                    }.runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("MysticShards"), 0, 20);
                }
                if (!player.isOnline()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 2, 20);
    }
}
