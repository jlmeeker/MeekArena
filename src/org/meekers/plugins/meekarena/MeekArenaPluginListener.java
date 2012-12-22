package org.meekers.plugins.meekarena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author jaredm
 */
class MeekArenaPluginListener implements Listener {

    MeekArena plugin;

    public MeekArenaPluginListener(MeekArena plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDeath(EntityDamageEvent event) {
        String rname = null;

        if (event.getEntity() instanceof Player) { // .getType() == EntityType.PLAYER) {
            Player receiver = (Player) event.getEntity();
            rname = receiver.getPlayerListName();
            String pworld = receiver.getWorld().getName();


            if ("arena".equals(pworld)) {
                int damage = event.getDamage();
                int healthleft = receiver.getHealth() - damage;
                //receiver.sendMessage("Dmg recd: " + damage + ", hp left: " + healthleft);                

                // Detect death blow
                if (damage > healthleft) {
                    event.setCancelled(true);

                    // Send player dead message
                    Bukkit.broadcastMessage(receiver.getPlayerListName() + " died by " + event.getCause().name());

                    // teleport player to spawn
                    Location spawn = new Location(receiver.getWorld(), receiver.getWorld().getSpawnLocation().getX(), receiver.getWorld().getHighestBlockYAt(receiver.getWorld().getSpawnLocation()) + 2, receiver.getWorld().getSpawnLocation().getZ());
                    receiver.teleport(spawn);

                    // give default inventory
                    this.plugin.setInventory(receiver);

                    // full heal
                    this.plugin.fullHeal(receiver);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String pworld = player.getWorld().getName();
        if ("arena".equals(pworld)) {
            this.plugin.restoreState(player);
            this.plugin.fullHeal(player);
        }
    }

    @EventHandler
    public void onWorldLeave(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String pworld = event.getFrom().getName();
//        player.sendMessage("you just changed from: "+pworld);
        if ("arena".equals(pworld)) {
            this.plugin.restoreState(player);
            this.plugin.fullHeal(player);
        }
    }
}
