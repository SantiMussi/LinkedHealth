package LH.sanju;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("LinkedHealthPlugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LinkedHealthPlugin Disabled!");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damagedPlayer = (Player) event.getEntity();
            double damage = event.getFinalDamage();
            double newHealth = Math.max(0, damagedPlayer.getHealth() - damage);

            event.setCancelled(true);
            damagedPlayer.setHealth(newHealth);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(damagedPlayer)) {
                    double playerHealth = Math.max(0, player.getHealth() - damage);
                    player.setHealth(playerHealth);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player newPlayer = event.getPlayer();
        double referenceHealth = 20.0; // Default health if no players are online

        // Check if there are existing players
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            // Get the health of the first player as a reference
            for (Player player : Bukkit.getOnlinePlayers()) {
                referenceHealth = player.getHealth();
                break; // Get the health of the first player only
            }
        }

        // Set the new player's health to match the reference health
        newPlayer.setHealth(referenceHealth);
    }

    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        // Check if the entity is a player
        if (event.getEntity() instanceof Player) {
            Player healedPlayer = (Player) event.getEntity();

            // Quantity of health regained
            double healthRegained = event.getAmount();

            // Calculates the new Health the players will receive
            double newHealth = Math.min(healedPlayer.getHealth() + healthRegained, healedPlayer.getMaxHealth());

            // Cancel the event so the player doesn't get healed two times
            event.setCancelled(true);
            healedPlayer.setHealth(newHealth);

            // Establish the new health for all the players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(healedPlayer)) {
                    player.setHealth(Math.min(player.getMaxHealth(), newHealth));
                }
            }
        }
    }
}