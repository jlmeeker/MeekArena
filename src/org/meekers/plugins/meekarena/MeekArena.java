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

    Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
    Map<String, ItemStack[]> armors = new HashMap<String, ItemStack[]>();
    Map<String, Location> locations = new HashMap<String, Location>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("begin")) { // If the player typed /begin then do the following...
            Bukkit.broadcastMessage("Starting arena....");
            // Generate new world and capture new spawn location
            WorldCreator newworld = new WorldCreator("arena");
            newworld.environment(World.Environment.NORMAL);
            newworld.generateStructures(false);
            newworld.type(WorldType.NORMAL);
            newworld.createWorld();
            Location newspawn = Bukkit.getWorld("arena").getSpawnLocation();

            // Get list of online players
            Player[] plist = Bukkit.getOnlinePlayers();


            for (Player op : plist) {
                // save inventory
                this.inventories.put(op.getName(), op.getInventory().getContents());
                this.armors.put(op.getName(), op.getEquipment().getArmorContents());
                op.sendMessage("Inventory has been saved.");

                // Save location
                this.locations.put(op.getName(), op.getLocation());
                op.sendMessage("Location has been saved.");

                // Send player to arena
                Bukkit.broadcastMessage("sending " + op.getPlayerListName() + " to the arena");
                op.getInventory().clear();
                op.sendMessage("Inventory has been cleared");
                op.teleport(newspawn);

                // give full life and food
                op.setHealth(op.getMaxHealth());
                op.setFoodLevel(20);
                op.setExhaustion(0);
                
                // assign default inventory items
                PlayerInventory newinventory  = op.getInventory();
                newinventory.addItem(new ItemStack(Material.IRON_SWORD, 1));
                newinventory.addItem(new ItemStack(Material.IRON_AXE, 1));
                newinventory.addItem(new ItemStack(Material.IRON_SPADE, 1));
                newinventory.addItem(new ItemStack(Material.IRON_PICKAXE, 1));
                newinventory.addItem(new ItemStack(Material.BED, 1));
                newinventory.addItem(new ItemStack(Material.COOKED_BEEF, 5));
                
                op.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
                op.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
                op.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
                op.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
                op.sendMessage("Inventory has been built");
            }
            return true;
        } //If this has happened the function will return true. 
        else if (cmd.getName().equalsIgnoreCase("end")) {
            List<Player> aplayers = Bukkit.getWorld("arena").getPlayers();

            for (Player ap : aplayers) {
                // Clear all player inventories
                ap.getInventory().clear();
                ap.sendMessage("Inventory cleared");

                // move all players back to "world"
                ap.teleport(this.locations.get(ap.getName()));
                ap.sendMessage("Restored to previous location");

                // reinstate all original inventory
                ap.getInventory().setContents(this.inventories.get(ap.getName()));
                ap.getEquipment().setArmorContents(this.armors.get(ap.getName()));
                ap.sendMessage("Inventory restored");

                // give full life and food
                ap.setHealth(ap.getMaxHealth());
                ap.setFoodLevel(20);
                ap.setExhaustion(0);

            }

            // Destroy arena
            boolean result = Bukkit.unloadWorld("arena", false);
            Bukkit.broadcastMessage("Arena ended!" + result);
            return true;
        } //If this has happened the function will return true. 

        // If this hasn't happened the a value of false will be returned.
        return false;
    }
}
