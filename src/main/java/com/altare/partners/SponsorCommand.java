package com.altare.partners;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Opens the Altare sponsor menu. */
public class SponsorCommand implements CommandExecutor {

    private final AltarePartners plugin;
    private final SponsorGui sponsorGui;

    public SponsorCommand(AltarePartners plugin, SponsorGui sponsorGui) {
        this.plugin = plugin;
        this.sponsorGui = sponsorGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can open the sponsor menu."));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(SponsorGui.PERMISSION)) {
            player.sendMessage(Text.parse(plugin, "<red>You do not have permission to use this command.</red>"));
            return true;
        }

        sponsorGui.open(player);
        return true;
    }
}
