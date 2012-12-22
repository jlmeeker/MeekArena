/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meekers.plugins.meekarena;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author jaredm
 */
public class MeekArena extends JavaPlugin {

    Map<String, ItemStack[]> inventories = new HashMap<>();
    Map<String, ItemStack[]> armors = new HashMap<>();
    Map<String, Location> locations = new HashMap<>();
    public Player[] plist;
    public boolean starting = false;
    public boolean started = false;

    // Spawn listener for player deaths (intercept)
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MeekArenaPluginListener(this), this);
    }

    public void onDisable() {
        World arena = Bukkit.getWorld("arena");

        if (arena != null) {
            // cycle through arena players and restore them
            for (Player ap : arena.getPlayers()) {
                this.restoreState(ap);
                this.fullHeal(ap);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player splayer = Bukkit.getPlayer(sender.getName());

        if (cmd.getName().equalsIgnoreCase("arena")) {
            if (args.length > 1 || args.length < 1) {
                return false;
            } else if ("start".equals(args[0]) && this.started != true) {
                if (this.starting == true) {
                    sender.sendMessage("The arena is already starting... wait for the join message.");
                    return true;
                }

                if (this.started == true) {
                    sender.sendMessage("The arena is already started.  Use /arena join to enter.");
                    return true;
                }

                Bukkit.broadcastMessage("Starting arena....");
                World arena;
                arena = Bukkit.getWorld("arena");

                if (arena == null) {
                    this.starting = true;
                    WorldCreator wcreator = new WorldCreator("arena");
                    wcreator.environment(World.Environment.NORMAL);
                    wcreator.generateStructures(false);
                    wcreator.type(WorldType.NORMAL);
                    arena = wcreator.createWorld();
                }

                if (arena != null) {
                    this.starting = false;
                    this.started = true;
                    Bukkit.broadcastMessage("The arena is live!!! To enter, type /arena join");
                } else {
                    getLogger().info("Failed to create arena.");
                }
                return true;
            } else if ("stop".equalsIgnoreCase(args[0])) {
                if (this.started != true) {
                    sender.sendMessage("The arena isn't started... nothing to stop.");
                    return true;
                }
                if (this.starting == true) {
                    sender.sendMessage("The arena is starting... stop the arena once it has fully started.");
                    return true;
                }

                List<Player> aplayers = Bukkit.getWorld("arena").getPlayers();

                for (Player ap : aplayers) {
                    ap.sendMessage("You are now leaving the arena.");
                    this.restoreState(ap);
                    this.fullHeal(ap);
                }

                // Destroy arena
                boolean result = Bukkit.unloadWorld("arena", false);
                Bukkit.broadcastMessage("Arena ended!");
                this.started = false;
                this.starting = false;
                return true;
            } else if ("join".equals(args[0])) {
                if (this.starting == true) {
                    sender.sendMessage("Please wait for the arena to fully start.");
                    return true;
                }
                if (this.started != true) {
                    sender.sendMessage("The arena was not started. Use /arena start to get it setup.");
                    return true;
                }
                if (this.testPlayer(sender)) {
                    // If player is already in the arena, tell them so
                    if ("arena".equals(splayer.getLocation().getWorld().getName())) {
                        sender.sendMessage("You are already in the arena.");
                        return true;
                    }

                    this.saveSate(splayer);
                    splayer.teleport(Bukkit.getWorld("arena").getSpawnLocation());
                    this.fullHeal(splayer);
                    this.setInventory(splayer);
                } else {
                    getLogger().info("You must be a player to join the arena.");
                }
                return true;
            } else if ("leave".equals(args[0])) {
                if (this.testPlayer(sender)) {
                    // If player is already in the arena, tell them so
                    if (!"arena".equals(splayer.getLocation().getWorld().getName()) || this.started != true) {
                        sender.sendMessage("You are not in the arena.");
                        return true;
                    }

                    splayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
                    this.fullHeal(splayer);
                    this.restoreState(splayer);
                } else {
                    getLogger().info("You must be a player to leave the arena.");
                }
                return true;
            } else if ("status".equals(args[0])) {
                String status = "";
                String players = "";
                if (this.started == true) {
                    status = "started";
                    List<Player> aplayers = Bukkit.getWorld("arena").getPlayers();

                    for (Player ap : aplayers) {
                        players += ap.getPlayerListName() + " ";
                    }
                } else if (this.starting == true) {
                    status = "starting";
                } else {
                    status = "stopped";
                }
                sender.sendMessage("The arena status is: " + status);
                sender.sendMessage("Players currently in the arena: " + players);
                return true;
            }
        }
        //sender.sendMessage("/arena [start | stop | status | join | leave]");
        return false;
    }

    public void saveSate(Player inplayer) {
        // save inventory
        this.inventories.put(inplayer.getName(), inplayer.getInventory().getContents());
        this.armors.put(inplayer.getName(), inplayer.getEquipment().getArmorContents());
//        inplayer.sendMessage("Inventory has been saved.");

        // Save location
        this.locations.put(inplayer.getName(), inplayer.getLocation());
//        inplayer.sendMessage("Location has been saved.");
    }

    public void restoreState(Player inplayer) {
        // Clear all player inventories
        inplayer.getInventory().clear();
//        inplayer.sendMessage("Inventory cleared");

        // move all players back to "world"
        Location spawn = new Location(this.locations.get(inplayer.getName()).getWorld(), this.locations.get(inplayer.getName()).getX(), this.locations.get(inplayer.getName()).getWorld().getHighestBlockYAt(this.locations.get(inplayer.getName())) + 2, this.locations.get(inplayer.getName()).getZ());
        inplayer.teleport(this.locations.get(inplayer.getName()));
//        inplayer.sendMessage("Restored to previous location");

        // reinstate all original inventory
        inplayer.getInventory().setContents(this.inventories.get(inplayer.getName()));
        inplayer.getEquipment().setArmorContents(this.armors.get(inplayer.getName()));
//        inplayer.sendMessage("Inventory restored");
    }

    public void setInventory(Player inplayer) {
        inplayer.getInventory().clear();
//        inplayer.sendMessage("Inventory has been cleared");

        // Setup default inventory
        PlayerInventory inventory = inplayer.getInventory();
        inventory.addItem(new ItemStack(Material.IRON_SWORD, 1));
        inventory.addItem(new ItemStack(Material.IRON_AXE, 1));
        inventory.addItem(new ItemStack(Material.IRON_SPADE, 1));
        inventory.addItem(new ItemStack(Material.IRON_PICKAXE, 1));
        inventory.addItem(new ItemStack(Material.BED, 1));
        inventory.addItem(new ItemStack(Material.COOKED_BEEF, 5));

        inplayer.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
        inplayer.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
        inplayer.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
        inplayer.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
//        inplayer.sendMessage("Inventory has been built");
    }

    public void fullHeal(Player inplayer) {
        // give full life and food
        inplayer.setFireTicks(0);
        inplayer.setHealth(inplayer.getMaxHealth());
        inplayer.setFoodLevel(20);
        inplayer.setExhaustion(0);
    }

    public boolean testPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        } else {
            return false;
        }
    }
}
