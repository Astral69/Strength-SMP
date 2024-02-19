package me.astral.strengthmain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StrengthCommand implements CommandExecutor {
   private final StrengthMain plugin;

   public StrengthCommand(StrengthMain plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         double strength = this.plugin.getStrengthMultiplierForPlayer(player);
         player.sendMessage("Your current strength level is: " + strength);
      } else {
         sender.sendMessage("Only players can use this command.");
      }

      return true;
   }
}
