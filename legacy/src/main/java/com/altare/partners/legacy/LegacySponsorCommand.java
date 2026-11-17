package com.altare.partners.legacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Opens the Altare sponsor menu on 1.8.8. */
public class LegacySponsorCommand implements CommandExecutor {

    private final LegacyPlugin plugin;
    private final LegacySponsorGui gui;

    public LegacySponsorCommand(LegacyPlugin plugin, LegacySponsorGui gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open the sponsor menu.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(LegacySponsorGui.PERMISSION)) {
            player.sendMessage(LegacyText.parse(plugin, "<red>You do not have permission to use this command.</red>"));
            return true;
        }

        gui.open(player);
        return true;
    }
}
