package me.astral.strengthmain;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class StrengthMain extends JavaPlugin implements Listener {
   public double getStrengthMultiplierForPlayer(Player player) {
      return this.getStrengthMultiplier(player);
   }

   public void onEnable() {
      this.getServer().getPluginManager().registerEvents(this, this);
      this.saveDefaultConfig();
      this.registerStrengthItemRecipe();
      this.registerStrengthCheckCommand();
   }

   private void registerStrengthItemRecipe() {
      ItemStack strengthItem = this.createStrengthItem();
      ShapedRecipe recipe = new ShapedRecipe(strengthItem);
      recipe.shape(new String[]{"ABA", "BCB", "ABA"});
      recipe.setIngredient('A', Material.DIAMOND_BLOCK);
      recipe.setIngredient('C', Material.BOOK);
      recipe.setIngredient('B', Material.NETHERITE_BLOCK);
      Bukkit.addRecipe(recipe);
   }

   private ItemStack createStrengthItem() {
      ItemStack item = new ItemStack(Material.FISHING_ROD);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(ChatColor.AQUA + "Strength Booster");
         List<String> lore = new ArrayList();
         lore.add(ChatColor.GRAY + "Right-click to gain 1 strength.");
         meta.setLore(lore);
         item.setItemMeta(meta);
      }

      return item;
   }

   private void registerStrengthCheckCommand() {
      PluginCommand strengthCommand = this.getCommand("strength");
      if (strengthCommand != null) {
         strengthCommand.setExecutor(new StrengthCommand(this));
      } else {
         this.getLogger().warning("Command 'strength' is not registered.");
      }

   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      ItemStack item = event.getItem();
      if (event.getHand() == EquipmentSlot.HAND && item != null && item.isSimilar(this.createStrengthItem())) {
         Player player = event.getPlayer();
         FileConfiguration config = this.getConfig();
         double oldPlayerStrength = config.getDouble("strength." + player.getUniqueId());
         double strengthBoost = 1.0D;
         double newPlayerStrength = oldPlayerStrength + strengthBoost;
         newPlayerStrength = Math.min(newPlayerStrength, 5.0D);
         config.set("strength." + player.getUniqueId(), newPlayerStrength);
         this.saveConfig();
         this.getLogger().log(Level.INFO, "Player " + player.getName() + " used a strength booster. New strength: " + newPlayerStrength);
         player.sendMessage(ChatColor.of("#55ff21") + "You used a Strength Booster! Your strength is boosted by " + ChatColor.AQUA + ChatColor.BOLD + strengthBoost + ChatColor.of("#55ff21") + ". Your total strength is now: " + ChatColor.AQUA + ChatColor.BOLD + newPlayerStrength);
         int itemSlot = event.getHand() == EquipmentSlot.HAND ? player.getInventory().getHeldItemSlot() : -1;
         if (itemSlot != -1) {
            player.getInventory().setItem(itemSlot, new ItemStack(Material.AIR));
         }
      }

   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      Player killer = event.getEntity().getKiller();
      if (killer != null) {
         FileConfiguration config = this.getConfig();
         double oldPlayerStrength = config.getDouble("strength." + player.getUniqueId());
         double oldKillerStrength = config.getDouble("strength." + killer.getUniqueId());
         if (Math.round((oldKillerStrength - 1.0D) * 10.0D) != 5L && Math.round((oldPlayerStrength - 1.0D) * 10.0D) != -5L) {
            double newKillerStrength = oldKillerStrength + 1.0D;
            double newPlayerStrength = oldPlayerStrength - 1.0D;
            newKillerStrength = Math.min(newKillerStrength, 5.0D);
            newPlayerStrength = Math.max(newPlayerStrength, -5.0D);
            config.set("strength." + killer.getUniqueId(), newKillerStrength);
            config.set("strength." + player.getUniqueId(), newPlayerStrength);
            this.saveConfig();
            this.getLogger().log(Level.INFO, "Player " + killer.getName() + " gained strength. New strength: " + newKillerStrength);
            this.getLogger().log(Level.INFO, "Player " + player.getName() + " lost strength. New strength: " + newPlayerStrength);
         } else {
            killer.sendMessage("");
            player.sendMessage("");
            this.getLogger().log(Level.INFO, "No strength changes. Player " + killer.getName() + " and " + player.getName() + " did not gain/lose strength.");
         }

         int newPlayerStrength = (int)Math.round(oldPlayerStrength - 1.0D);
         int newKillerStrength = (int)Math.round(oldKillerStrength + 1.0D);
         newPlayerStrength = Math.max(newPlayerStrength, -5);
         newKillerStrength = Math.min(newKillerStrength, 5);
         player.sendMessage(ChatColor.of("#cc2323") + "You Lost a Strength,Your strength is : " + ChatColor.AQUA + ChatColor.BOLD + newPlayerStrength);
         killer.sendMessage(ChatColor.of("#74b72e") + "You Gained a strength,Your strength is: " + ChatColor.AQUA + ChatColor.BOLD + newKillerStrength);
      }

   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player player = (Player)event.getDamager();
         double strengthMultiplier = this.getStrengthMultiplier(player);
         this.getLogger().log(Level.INFO, "Player " + player.getName() + " dealt damage with strength multiplier: " + strengthMultiplier);
         event.setDamage(event.getDamage() * strengthMultiplier);
      }

   }

   private double getStrengthMultiplier(Player player) {
      FileConfiguration config = this.getConfig();
      double defaultStrengthMultiplier = 1.0D;
      double strengthLevel = config.getDouble("strength." + player.getUniqueId(), defaultStrengthMultiplier);
      return this.calculateStrengthMultiplier(strengthLevel);
   }

   private double calculateStrengthMultiplier(double strengthLevel) {
      double baseMultiplier = 1.0D;
      double multiplierIncrement = 0.1D;
      return baseMultiplier + (strengthLevel - 1.0D) * multiplierIncrement;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      FileConfiguration config = this.getConfig();
      if (!config.contains("strength." + player.getUniqueId())) {
         config.set("strength." + player.getUniqueId(), 1.0D);
         this.saveConfig();
         this.getLogger().log(Level.INFO, "New player joined. Setting default strength for player " + player.getName());
      }

   }
}
