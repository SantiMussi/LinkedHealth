package LH.sanju;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private boolean isPluginEnabled = true;
    public static String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "LinkedHealth" + ChatColor.GRAY + "] ";
    private String version = getDescription().getVersion();

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage((ChatColor.translateAlternateColorCodes('&', prefix + "&aPlugin enabled! Version: " + version)));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix+ "Plugin disabled!"));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!isPluginEnabled) return;
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
        if(!isPluginEnabled) return;
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
        if(!isPluginEnabled) return;
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

            if(newHealth == 20.0){
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_CLERIC, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&aAll players are now at full health!"));
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("linkedhealth")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aLinkedHealthPlugin v"+ version + " by "+ getDescription().getAuthors() + " - Use /linkedhealth help for commands."));
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aLinkedHealth Commands:"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§e/linkedhealth help §7- Show this help message."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§e/sethealth <value> §7- Set the health for all players."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§e/syncHealth §7- Enable or disable the plugin."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§e/isSyncHealth §7- Shows if the plugin is enabled or disabled."));
                return true;
            }
        }

        //Command to set health for all players
        if (command.getName().equalsIgnoreCase("sethealth")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cOnly players can use this command."));
                return true;
            }

            if(!sender.hasPermission("linkedhealth.commands.sethealth")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cYou don't have permission to use this command!"));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cUsage: /sethealth <value>"));
                return true;
            }

            try {
                double health = Double.parseDouble(args[0]);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.setHealth(Math.min(player.getMaxHealth(), health));
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§aHealth set to " + health + " for all players."));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "§cInvalid number. Please provide a valid health value."));
            }
            return true;
        }

        //Command to enable or disable the plugin
        if(command.getName().equalsIgnoreCase("syncHealth")){
            if(!sender.hasPermission("linkedhealth.commands.sync")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cYou don't have permission to use this command!"));
                return true;
            }
            if(isPluginEnabled){
                isPluginEnabled = false;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPlugin disabled!"));
            } else {
                isPluginEnabled = true;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&aPlugin enabled!"));
            }
            return true;
        }

        //Command to check if the plugin is enabled or disabled
        if(command.getName().equalsIgnoreCase("isSyncHealth")){
            if(isPluginEnabled){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&aPlugin is enabled!"));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cPlugin is disabled!"));
            }
            return true;
        }

        return false; // If the command doesn't match any known commands, return false.
    }
}