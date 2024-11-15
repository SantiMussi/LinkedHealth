package LH.sanju;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    public static String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "LinkedHealth" + ChatColor.GRAY + "] ";
    private String version = getDescription().getVersion();
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(ChatColor.translateAlternateColorCodes('&', prefix + "&aPlugin enabled! Version: " + version));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.translateAlternateColorCodes('&', prefix+ "Plugin disabled!"));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player damagedPlayer) {
            double damage = event.getFinalDamage();
            double newHealth = Math.max(0, damagedPlayer.getHealth() - damage);

            damagedPlayer.setHealth(newHealth);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if(!player.equals(damagedPlayer)) {
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
        if (event.getEntity() instanceof Player healedPlayer) {

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