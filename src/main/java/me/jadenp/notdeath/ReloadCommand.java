package me.jadenp.notdeath;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the functionality of the {@code /NotDeath reload} command.
 */
public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final NotDeath plugin;
    public ReloadCommand(NotDeath plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, Command command, @Nonnull String label, @Nonnull String[] args) {
        if (command.getName().equals("NotDeath")) {
            if (sender.hasPermission("notdeath.admin")) {
                plugin.readConfig();
                sender.sendMessage(ChatColor.GREEN + "Reloaded " + plugin.getDescription().getName() + " version " + plugin.getDescription().getVersion());
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, Command command, @Nonnull String label, @Nonnull String[] args) {
        List<String> tab = new ArrayList<>();
        if (command.getName().equals("NotDeath") && sender.hasPermission("notdeath.admin")) {
            tab.add("reload");
        }
        return tab;
    }
}
