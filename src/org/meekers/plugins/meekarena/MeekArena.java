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
    public WorldCreator newworld = new WorldCreator("arena");
    public Location newspawn;
    public Player[] plist;
    PlayerInventory newinventory;
    public boolean started = false;

    // Spawn listener for player deaths (intercept)
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MeekArenaPluginListener(this), this);
    }

    public void onDisable() {
        // cycle through arena players and restore them
        for (Player ap : Bukkit.getWorld("arena").getPlayers()) {
            // Restore to original state
            this.restoreState(ap);

            // give full life and food
            this.fullHeal(ap);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player splayer = Bukkit.getPlayer(sender.getName());

        if (cmd.getName().equalsIgnoreCase("arena")) {
            if (args.length > 1) {
                sender.sendMessage("Too many arguments!");
                return false;
            }
            if (args.length < 1) {
                sender.sendMessage("Not enough arguments!");
                return false;
            }
            if ("start".equals(args[0]) && this.started != true) {
                Bukkit.broadcastMessage("Starting arena....");
                // Generate new world and capture new spawn location
                this.newworld.environment(World.Environment.NORMAL);
                this.newworld.generateStructures(false);
                this.newworld.type(WorldType.NORMAL);
                this.newworld.createWorld();
                this.newspawn = Bukkit.getWorld("arena").getSpawnLocation();
                this.started = true;
                Bukkit.broadcastMessage("The area is live!!! To enter, type /arena join");
                return true;
            } else if ("start".equals(args[0]) && this.started == true) {
                sender.sendMessage("The arena is already running. Use: /arena join to get into the action!");
                return true;
            } else if ("stop".equalsIgnoreCase(args[0]) && this.started == true) {
                List<Player> aplayers = Bukkit.getWorld("arena").getPlayers();

                for (Player ap : aplayers) {
                    // Restore to original state
                    this.restoreState(ap);

                    // give full life and food
                    this.fullHeal(ap);
                }

                // Destroy arena
                boolean result = Bukkit.unloadWorld("arena", false);
                Bukkit.broadcastMessage("Arena ended!");
                this.started = false;
                return true;
            } else if ("status".equalsIgnoreCase(args[0])) {
                if (this.started == true) {
                    sender.sendMessage("The arena is live!");
                } else {
                    sender.sendMessage("The arena is not available.");
                }
                return true;
            } else if ("join".equalsIgnoreCase(args[0]) && this.started == true) {
                this.saveSate(splayer);
                this.setInventory(splayer);
                splayer.teleport(newspawn);
                this.fullHeal(splayer);
                return true;
            } else if ("join".equalsIgnoreCase(args[0]) && this.started == false) {
                sender.sendMessage("The arena is not available");
                return true;
            } else if ("leave".equalsIgnoreCase(args[0]) && this.started == true) {
                this.restoreState(splayer);
                this.fullHeal(splayer);
                return true;
            } else {
                sender.sendMessage("/arena [start | stop | status | join | leave]");
                return false;
            }
        }
        return false;
    }

    public void saveSate(Player inplayer) {
        // save inventory
        this.inventories.put(inplayer.getName(), inplayer.getInventory().getContents());
        this.armors.put(inplayer.getName(), inplayer.getEquipment().getArmorContents());
        inplayer.sendMessage("Inventory has been saved.");

        // Save location
        this.locations.put(inplayer.getName(), inplayer.getLocation());
        inplayer.sendMessage("Location has been saved.");
    }

    public void restoreState(Player inplayer) {
        // Clear all player inventories
        inplayer.getInventory().clear();
        inplayer.sendMessage("Inventory cleared");

        // move all players back to "world"
        inplayer.teleport(this.locations.get(inplayer.getName()));
        inplayer.sendMessage("Restored to previous location");

        // reinstate all original inventory
        inplayer.getInventory().setContents(this.inventories.get(inplayer.getName()));
        inplayer.getEquipment().setArmorContents(this.armors.get(inplayer.getName()));
        inplayer.sendMessage("Inventory restored");
    }

    public void setInventory(Player inplayer) {
        inplayer.getInventory().clear();
        inplayer.sendMessage("Inventory has been cleared");

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
        inplayer.sendMessage("Inventory has been built");
    }

    public void fullHeal(Player inplayer) {
        // give full life and food
        inplayer.setHealth(inplayer.getMaxHealth());
        inplayer.setFoodLevel(20);
        inplayer.setExhaustion(0);
    }
}
