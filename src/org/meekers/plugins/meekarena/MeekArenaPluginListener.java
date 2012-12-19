package org.meekers.plugins.meekarena;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
    public void onDeath(EntityDamageByEntityEvent event) {
        String aname = null;
        String rname = null;

        if (event.getEntity().getType() == EntityType.PLAYER) {
            Player receiver = (Player) event.getEntity();
            rname = receiver.getPlayerListName();

            if (event.getDamager().getType() == EntityType.PLAYER) {
                Player attacker = (Player) event.getDamager();
                aname = attacker.getPlayerListName();
            } else {
                Entity attacker = event.getDamager();
                aname = attacker.getType().getName();
            }

            int damage = event.getDamage();

            // Detect death blow
            if (damage > receiver.getHealth()) {
                // Negate damage
                event.setDamage(0);
                
                // Send player dead message
                Bukkit.broadcastMessage(receiver.getPlayerListName() + " has been killed by " + aname);

                // teleport player to spawn
                receiver.teleport(this.plugin.newspawn);

                // give default inventory
                this.plugin.setInventory(receiver);
                
                // full heal
                this.plugin.fullHeal(receiver);
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.restoreState(player);
        this.plugin.fullHeal(player);
    }
}
